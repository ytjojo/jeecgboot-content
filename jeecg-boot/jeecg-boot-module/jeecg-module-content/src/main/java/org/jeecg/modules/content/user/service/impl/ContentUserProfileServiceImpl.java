package org.jeecg.modules.content.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.biz.ContentUserRelationBoundaryBizService;
import org.jeecg.modules.content.user.constant.ContentUserCacheConstant;
import org.jeecg.modules.content.user.entity.ContentUserHomepageModule;
import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserProfileReview;
import org.jeecg.modules.content.user.entity.ContentUserVerificationBadge;
import org.jeecg.modules.content.user.enums.ContentProfileHistoryTypeEnum;
import org.jeecg.modules.content.user.enums.ContentProfileReviewStatusEnum;
import org.jeecg.modules.content.user.enums.ContentUserVisibilityEnum;
import org.jeecg.modules.content.user.mapper.ContentUserHomepageModuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPrivacySettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileReviewMapper;
import org.jeecg.modules.content.user.mapper.ContentUserVerificationBadgeMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq;
import org.jeecg.modules.content.user.service.IContentUserMediaAdapter;
import org.jeecg.modules.content.user.service.IContentUserProfileAuditAdapter;
import org.jeecg.modules.content.user.service.IContentUserProfileHistoryService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserVerificationBadgeService;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.jeecg.modules.content.user.vo.ContentUserHomepageModuleVO;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.jeecg.modules.content.user.vo.ContentUserVerificationBadgeVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 内容社区用户资料服务实现。
 */
@Service
public class ContentUserProfileServiceImpl implements IContentUserProfileService {

    private static final int PROFILE_DAILY_LIMIT = 5;
    private static final int PRIVACY_HOURLY_LIMIT = 10;
    private static final List<String> DEFAULT_MODULES = List.of("POSTS", "COLLECTIONS", "BADGES", "ABOUT");
    private static final Set<String> SUPPORTED_VISIBILITIES = Set.of("PUBLIC", "FOLLOWERS_ONLY", "MUTUAL_ONLY", "PRIVATE");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserPrivacySettingMapper privacyMapper;

    @Resource
    private ContentUserProfileReviewMapper profileReviewMapper;

    @Resource
    private ContentUserHomepageModuleMapper homepageModuleMapper;

    @Resource
    private ContentUserVerificationBadgeMapper verificationBadgeMapper;

    @Resource
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    @Resource
    private ContentUserRelationBoundaryBizService relationBoundaryBizService;

    @Resource
    private IContentUserProfileAuditAdapter profileAuditAdapter;

    @Resource
    private IContentUserMediaAdapter mediaAdapter;

    @Resource
    private IContentUserProfileHistoryService historyService;

    @Resource
    private IContentUserVerificationBadgeService verificationBadgeService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 按查看者身份返回经过隐私裁剪的用户资料。
     */
    @Override
    public ContentUserProfileVO getProfile(String ownerUserId, String viewerUserId) {
        assertHomepageBlockBoundary(ownerUserId, viewerUserId);
        ContentUserProfile profile = requireProfile(ownerUserId);
        ContentUserPrivacySetting privacy = defaultPrivacy(privacyMapper.selectByUserId(ownerUserId), ownerUserId);
        boolean birthdayVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getBirthdayVisibility());
        boolean genderVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getGenderVisibility());
        boolean regionVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getRegionVisibility());
        boolean professionVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getProfessionVisibility());
        boolean personalLinkVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getPersonalLinkVisibility());
        boolean verificationVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getVerificationBadgeVisibility());
        ContentUserProfileVO vo = ContentUserProfileVO.from(
            profile,
            birthdayVisible,
            genderVisible,
            regionVisible,
            professionVisible,
            personalLinkVisible,
            verificationVisible
        );
        if (verificationVisible) {
            List<ContentUserVerificationBadgeVO> badges = verificationBadgeService.listVisibleBadges(ownerUserId);
            vo.setVerificationBadges(badges);
        }
        if (visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getHomepageVisibility())) {
            List<ContentUserHomepageModuleVO> modules = homepageModuleMapper.selectByUserId(ownerUserId).stream()
                .map(ContentUserHomepageModuleVO::from)
                .toList();
            vo.setHomepageModules(modules);
        } else {
            vo.setHomepageBackground(null);
            vo.setThemeColor(null);
            vo.setModuleOrderJson(null);
        }
        return vo;
    }

    private void assertHomepageBlockBoundary(String ownerUserId, String viewerUserId) {
        if (relationBoundaryBizService == null || Objects.equals(ownerUserId, viewerUserId)
            || !relationBoundaryBizService.isBlockedEitherWay(viewerUserId, ownerUserId)) {
            return;
        }
        if (relationBoundaryBizService.isBlockedBy(viewerUserId, ownerUserId)) {
            throw new JeecgBootException("您已拉黑该用户，无法查看其内容");
        }
        throw new JeecgBootException("用户不存在");
    }

    /**
     * 更新用户资料；疑似风险内容进入待审核，低风险内容直接生效。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userId, ContentUserProfileUpdateReq req) {
        ContentUserProfile profile = requireProfile(userId);
        ContentUserProfileUpdateReq normalizedReq = mergeAndNormalize(profile, req);
        validateProfileReq(normalizedReq);
        if (profileReviewMapper.selectPendingByUserId(userId) != null) {
            throw new JeecgBootException("资料正在审核中，请稍后再修改");
        }
        assertProfileUpdateQuota(userId);
        mediaAdapter.validateAvatar(normalizedReq.getAvatar());
        mediaAdapter.validateHomepageBackground(normalizedReq.getHomepageBackground());
        IContentUserProfileAuditAdapter.AuditResult auditResult = profileAuditAdapter.review(normalizedReq);
        if (auditResult.suspicious()) {
            createPendingReview(userId, profile, normalizedReq, auditResult.reason());
            return;
        }
        applyProfileUpdate(profile, normalizedReq, null);
        evictProfileCache(userId);
    }

    /**
     * 更新隐私设置，并删除公共资料缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacy(String userId, ContentUserPrivacyUpdateReq req) {
        assertPrivacyUpdateQuota(userId);
        ContentUserPrivacySetting privacy = privacyMapper.selectByUserId(userId);
        if (privacy == null) {
            privacy = defaultPrivacy(new ContentUserPrivacySetting(), userId);
            applyPrivacyUpdate(privacy, req);
            privacy.setId(UUIDGenerator.generate());
            privacyMapper.insert(privacy);
            evictProfileCache(userId);
            return;
        }
        applyPrivacyUpdate(privacy, req);
        privacyMapper.updateById(privacy);
        evictProfileCache(userId);
    }

    /**
     * 处理资料审核结果，审核通过才发布新资料。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleProfileReview(ContentUserReviewHandleReq req) {
        ContentUserProfileReview review = profileReviewMapper.selectById(req.getReviewId());
        if (review == null || !ContentProfileReviewStatusEnum.PENDING.getCode().equals(review.getReviewStatus())) {
            throw new JeecgBootException("待审核资料不存在");
        }
        ContentUserProfile profile = requireProfile(review.getUserId());
        if (ContentProfileReviewStatusEnum.APPROVED.getCode().equals(req.getReviewStatus())) {
            ContentUserProfileUpdateReq targetReq = readReviewTarget(review);
            applyProfileUpdate(profile, targetReq, review.getId());
            review.setReviewStatus(ContentProfileReviewStatusEnum.APPROVED.getCode());
        } else if (ContentProfileReviewStatusEnum.REJECTED.getCode().equals(req.getReviewStatus())) {
            profile.setProfileReviewStatus(ContentProfileReviewStatusEnum.REJECTED.getCode());
            profileMapper.updateById(profile);
            review.setReviewStatus(ContentProfileReviewStatusEnum.REJECTED.getCode());
            review.setRejectReason(req.getRejectReason());
        } else {
            throw new JeecgBootException("审核结果不合法");
        }
        review.setReviewedBy(req.getOperatorUserId());
        review.setReviewedAt(new Date());
        profileReviewMapper.updateById(review);
        evictProfileCache(review.getUserId());
    }

    /**
     * 将旧资料表中的认证字段和缺省主页模块初始化到新扩展表，重复执行不产生重复数据。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initializeCompatibilityData() {
        int changed = 0;
        List<ContentUserProfile> profiles = profileMapper.selectList(null);
        for (ContentUserProfile profile : profiles) {
            if (homepageModuleMapper.selectByUserId(profile.getUserId()).isEmpty()) {
                for (int i = 0; i < DEFAULT_MODULES.size(); i++) {
                    ContentUserHomepageModule module = new ContentUserHomepageModule()
                        .setUserId(profile.getUserId())
                        .setModuleKey(DEFAULT_MODULES.get(i))
                        .setModuleName(DEFAULT_MODULES.get(i))
                        .setVisible(Boolean.TRUE)
                        .setSortOrder(i);
                    module.setId(UUIDGenerator.generate());
                    homepageModuleMapper.insert(module);
                    changed++;
                }
            }
            if (!isBlank(profile.getCertificationType()) && verificationBadgeMapper.selectActiveByUserId(profile.getUserId()).isEmpty()) {
                ContentUserVerificationBadge badge = new ContentUserVerificationBadge()
                    .setUserId(profile.getUserId())
                    .setBadgeType(profile.getCertificationType())
                    .setBadgeLabel(firstNonNull(profile.getCertificationLabel(), profile.getCertificationType()))
                    .setDescription(profile.getCertificationLabel())
                    .setStatus("ACTIVE")
                    .setVerifiedAt(new Date());
                badge.setId(UUIDGenerator.generate());
                verificationBadgeMapper.insert(badge);
                changed++;
            }
        }
        return changed;
    }

    private ContentUserProfileUpdateReq mergeAndNormalize(ContentUserProfile profile, ContentUserProfileUpdateReq req) {
        ContentUserProfileUpdateReq normalizedReq = new ContentUserProfileUpdateReq();
        normalizedReq.setNickname(trimToNull(firstNonNull(req.getNickname(), profile.getNickname())));
        normalizedReq.setAvatar(trimToNull(firstNonNull(req.getAvatar(), profile.getAvatar())));
        normalizedReq.setBio(trimToNull(firstNonNull(req.getBio(), profile.getBio())));
        normalizedReq.setGender(firstNonNull(req.getGender(), profile.getGender()));
        normalizedReq.setBirthday(firstNonNull(req.getBirthday(), profile.getBirthday()));
        normalizedReq.setRegion(trimToNull(firstNonNull(req.getRegion(), profile.getRegion())));
        normalizedReq.setProfession(trimToNull(firstNonNull(req.getProfession(), profile.getProfession())));
        normalizedReq.setPersonalLink(trimToNull(firstNonNull(req.getPersonalLink(), profile.getPersonalLink())));
        normalizedReq.setHomepageBackground(trimToNull(firstNonNull(req.getHomepageBackground(), profile.getHomepageBackground())));
        normalizedReq.setThemeColor(trimToNull(firstNonNull(req.getThemeColor(), profile.getThemeColor())));
        normalizedReq.setModuleOrderJson(trimToNull(firstNonNull(req.getModuleOrderJson(), profile.getModuleOrderJson())));
        normalizedReq.setCertificationType(trimToNull(firstNonNull(req.getCertificationType(), profile.getCertificationType())));
        normalizedReq.setCertificationLabel(trimToNull(firstNonNull(req.getCertificationLabel(), profile.getCertificationLabel())));
        return normalizedReq;
    }

    private void validateProfileReq(ContentUserProfileUpdateReq req) {
        if (isBlank(req.getNickname())) {
            throw new JeecgBootException("昵称不能为空");
        }
        if (req.getNickname().length() > 20) {
            throw new JeecgBootException("昵称长度不能超过20位");
        }
        if (isBlank(req.getAvatar())) {
            throw new JeecgBootException("头像不能为空");
        }
        if (req.getBio() != null && req.getBio().length() > 500) {
            throw new JeecgBootException("个人简介长度不能超过500位");
        }
        if (req.getGender() != null && req.getGender() != 0 && req.getGender() != 1 && req.getGender() != 2) {
            throw new JeecgBootException("性别取值不合法");
        }
        if (req.getBirthday() != null && req.getBirthday().after(new Date())) {
            throw new JeecgBootException("生日不能晚于当前日期");
        }
        if (req.getRegion() != null && req.getRegion().length() > 64) {
            throw new JeecgBootException("地区长度不能超过64位");
        }
        if (req.getProfession() != null && req.getProfession().length() > 64) {
            throw new JeecgBootException("职业长度不能超过64位");
        }
        if (req.getPersonalLink() != null) {
            validatePersonalLink(req.getPersonalLink());
        }
    }

    private void validatePersonalLink(String personalLink) {
        try {
            URI uri = new URI(personalLink);
            if (uri.getScheme() == null || !List.of("http", "https").contains(uri.getScheme())) {
                throw new JeecgBootException("个人链接格式不合法");
            }
        } catch (URISyntaxException ex) {
            throw new JeecgBootException("个人链接格式不合法");
        }
    }

    private void createPendingReview(String userId,
                                     ContentUserProfile profile,
                                     ContentUserProfileUpdateReq req,
                                     String riskReason) {
        ContentUserProfileReview review = new ContentUserProfileReview()
            .setUserId(userId)
            .setReviewStatus(ContentProfileReviewStatusEnum.PENDING.getCode())
            .setReviewType("PROFILE")
            .setRiskReason(riskReason)
            .setOriginalSnapshotJson(writeProfileSnapshot(profile))
            .setTargetSnapshotJson(writeReqSnapshot(req));
        review.setId(UUIDGenerator.generate());
        profileReviewMapper.insert(review);
        profile.setProfileReviewStatus(ContentProfileReviewStatusEnum.PENDING.getCode());
        profileMapper.updateById(profile);
    }

    private void applyProfileUpdate(ContentUserProfile profile, ContentUserProfileUpdateReq req, String sourceUpdateId) {
        if (!Objects.equals(profile.getNickname(), req.getNickname())) {
            historyService.recordEffectiveChange(profile.getUserId(), ContentProfileHistoryTypeEnum.NICKNAME.getCode(),
                profile.getNickname(), sourceUpdateId);
        }
        if (!Objects.equals(profile.getAvatar(), req.getAvatar())) {
            historyService.recordEffectiveChange(profile.getUserId(), ContentProfileHistoryTypeEnum.AVATAR.getCode(),
                profile.getAvatar(), sourceUpdateId);
        }
        profile.setNickname(req.getNickname());
        profile.setAvatar(req.getAvatar());
        profile.setBio(req.getBio());
        profile.setGender(req.getGender());
        profile.setBirthday(req.getBirthday());
        profile.setRegion(req.getRegion());
        profile.setProfession(req.getProfession());
        profile.setPersonalLink(req.getPersonalLink());
        profile.setHomepageBackground(req.getHomepageBackground());
        profile.setThemeColor(req.getThemeColor());
        profile.setModuleOrderJson(req.getModuleOrderJson());
        profile.setCertificationType(req.getCertificationType());
        profile.setCertificationLabel(req.getCertificationLabel());
        profile.setProfileCompletionState("COMPLETE");
        profile.setProfileReviewStatus(ContentProfileReviewStatusEnum.NONE.getCode());
        profile.setProfileVersion(profile.getProfileVersion() == null ? 1 : profile.getProfileVersion() + 1);
        profileMapper.updateById(profile);
    }

    private void applyPrivacyUpdate(ContentUserPrivacySetting privacy, ContentUserPrivacyUpdateReq req) {
        setIfPresent(req.getBirthdayVisibility(), privacy::setBirthdayVisibility);
        setIfPresent(req.getGenderVisibility(), privacy::setGenderVisibility);
        setIfPresent(req.getRegionVisibility(), privacy::setRegionVisibility);
        setIfPresent(req.getProfessionVisibility(), privacy::setProfessionVisibility);
        setIfPresent(req.getPersonalLinkVisibility(), privacy::setPersonalLinkVisibility);
        setIfPresent(req.getVerificationBadgeVisibility(), privacy::setVerificationBadgeVisibility);
        setIfPresent(req.getContactBadgeVisibility(), privacy::setContactBadgeVisibility);
        setIfPresent(req.getHomepageVisibility(), privacy::setHomepageVisibility);
        setIfPresent(req.getDynamicVisibility(), privacy::setDynamicVisibility);
        privacy.setOnlineStatusVisible(firstNonNull(req.getOnlineStatusVisible(), privacy.getOnlineStatusVisible()));
        privacy.setAllowSearchEngineIndex(firstNonNull(req.getAllowSearchEngineIndex(), privacy.getAllowSearchEngineIndex()));
        privacy.setAllowUserSearch(firstNonNull(req.getAllowUserSearch(), privacy.getAllowUserSearch()));
    }

    private void assertProfileUpdateQuota(String userId) {
        String key = ContentUserCacheConstant.PROFILE_UPDATE_COUNT_PREFIX + userId + ":" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        incrementOrReject(key, PROFILE_DAILY_LIMIT, 1, TimeUnit.DAYS, "今日资料修改次数已达上限");
    }

    private void assertPrivacyUpdateQuota(String userId) {
        String key = ContentUserCacheConstant.PRIVACY_UPDATE_COUNT_PREFIX + userId + ":" + System.currentTimeMillis() / 3_600_000;
        incrementOrReject(key, PRIVACY_HOURLY_LIMIT, 1, TimeUnit.HOURS, "隐私设置修改过于频繁");
    }

    private void incrementOrReject(String key, int limit, long timeout, TimeUnit unit, String message) {
        if (redisTemplate == null) {
            return;
        }
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, timeout, unit);
        }
        if (count != null && count > limit) {
            throw new JeecgBootException(message);
        }
    }

    private void evictProfileCache(String userId) {
        if (redisTemplate == null || isBlank(userId)) {
            return;
        }
        redisTemplate.delete(ContentUserCacheConstant.PROFILE_CACHE_PREFIX + userId);
        redisTemplate.delete(ContentUserCacheConstant.PRIVACY_CACHE_PREFIX + userId);
        redisTemplate.delete(ContentUserCacheConstant.PROFILE_PUBLIC_CACHE_PREFIX + userId);
    }

    private ContentUserProfile requireProfile(String userId) {
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在");
        }
        return profile;
    }

    private ContentUserPrivacySetting defaultPrivacy(ContentUserPrivacySetting privacy, String userId) {
        if (privacy == null) {
            privacy = new ContentUserPrivacySetting();
        }
        privacy.setUserId(userId);
        if (privacy.getBirthdayVisibility() == null) {
            privacy.setBirthdayVisibility(ContentUserVisibilityEnum.PRIVATE.getCode());
        }
        if (privacy.getGenderVisibility() == null) {
            privacy.setGenderVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getRegionVisibility() == null) {
            privacy.setRegionVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getProfessionVisibility() == null) {
            privacy.setProfessionVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getPersonalLinkVisibility() == null) {
            privacy.setPersonalLinkVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getVerificationBadgeVisibility() == null) {
            privacy.setVerificationBadgeVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getContactBadgeVisibility() == null) {
            privacy.setContactBadgeVisibility(ContentUserVisibilityEnum.PRIVATE.getCode());
        }
        if (privacy.getHomepageVisibility() == null) {
            privacy.setHomepageVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getDynamicVisibility() == null) {
            privacy.setDynamicVisibility(ContentUserVisibilityEnum.PUBLIC.getCode());
        }
        if (privacy.getOnlineStatusVisible() == null) {
            privacy.setOnlineStatusVisible(Boolean.TRUE);
        }
        if (privacy.getAllowSearchEngineIndex() == null) {
            privacy.setAllowSearchEngineIndex(Boolean.TRUE);
        }
        if (privacy.getAllowUserSearch() == null) {
            privacy.setAllowUserSearch(Boolean.TRUE);
        }
        return privacy;
    }

    private String writeProfileSnapshot(ContentUserProfile profile) {
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname(profile.getNickname())
            .setAvatar(profile.getAvatar())
            .setBio(profile.getBio())
            .setGender(profile.getGender())
            .setBirthday(profile.getBirthday())
            .setRegion(profile.getRegion())
            .setProfession(profile.getProfession())
            .setPersonalLink(profile.getPersonalLink())
            .setHomepageBackground(profile.getHomepageBackground())
            .setThemeColor(profile.getThemeColor())
            .setModuleOrderJson(profile.getModuleOrderJson())
            .setCertificationType(profile.getCertificationType())
            .setCertificationLabel(profile.getCertificationLabel());
        return writeReqSnapshot(req);
    }

    private String writeReqSnapshot(ContentUserProfileUpdateReq req) {
        try {
            return OBJECT_MAPPER.writeValueAsString(req);
        } catch (JsonProcessingException ex) {
            throw new JeecgBootException("资料快照序列化失败");
        }
    }

    private ContentUserProfileUpdateReq readReviewTarget(ContentUserProfileReview review) {
        try {
            return OBJECT_MAPPER.readValue(review.getTargetSnapshotJson(), ContentUserProfileUpdateReq.class);
        } catch (JsonProcessingException ex) {
            throw new JeecgBootException("资料审核快照解析失败");
        }
    }

    private void setIfPresent(String value, java.util.function.Consumer<String> consumer) {
        if (value != null) {
            if (value.trim().isEmpty()) {
                throw new JeecgBootException("可见范围不能为空");
            }
            String normalized = value.trim();
            if (!SUPPORTED_VISIBILITIES.contains(normalized)) {
                throw new JeecgBootException("可见范围取值不合法");
            }
            consumer.accept(normalized);
        }
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
