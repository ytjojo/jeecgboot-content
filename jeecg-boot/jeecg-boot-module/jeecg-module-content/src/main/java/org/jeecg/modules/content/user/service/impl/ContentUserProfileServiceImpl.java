package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.enums.ContentUserVisibilityEnum;
import org.jeecg.modules.content.user.mapper.ContentUserPrivacySettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Service
public class ContentUserProfileServiceImpl implements IContentUserProfileService {

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserPrivacySettingMapper privacyMapper;

    @Resource
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    @Override
    public ContentUserProfileVO getProfile(String ownerUserId, String viewerUserId) {
        ContentUserProfile profile = requireProfile(ownerUserId);
        ContentUserPrivacySetting privacy = defaultPrivacy(privacyMapper.selectByUserId(ownerUserId), ownerUserId);
        boolean birthdayVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getBirthdayVisibility());
        return ContentUserProfileVO.from(profile, birthdayVisible);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userId, ContentUserProfileUpdateReq req) {
        ContentUserProfile profile = requireProfile(userId);
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
        profileMapper.updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacy(String userId, ContentUserPrivacyUpdateReq req) {
        ContentUserPrivacySetting privacy = privacyMapper.selectByUserId(userId);
        if (privacy == null) {
            privacy = defaultPrivacy(new ContentUserPrivacySetting(), userId);
            privacy.setId(UUIDGenerator.generate());
            applyPrivacyUpdate(privacy, req);
            privacyMapper.insert(privacy);
            return;
        }
        applyPrivacyUpdate(privacy, req);
        privacyMapper.updateById(privacy);
    }

    private void applyPrivacyUpdate(ContentUserPrivacySetting privacy, ContentUserPrivacyUpdateReq req) {
        privacy.setBirthdayVisibility(req.getBirthdayVisibility());
        privacy.setGenderVisibility(req.getGenderVisibility());
        privacy.setRegionVisibility(req.getRegionVisibility());
        privacy.setProfessionVisibility(req.getProfessionVisibility());
        privacy.setHomepageVisibility(req.getHomepageVisibility());
        privacy.setDynamicVisibility(req.getDynamicVisibility());
        privacy.setOnlineStatusVisible(req.getOnlineStatusVisible());
        privacy.setAllowSearchEngineIndex(req.getAllowSearchEngineIndex());
        privacy.setAllowUserSearch(req.getAllowUserSearch());
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
}
