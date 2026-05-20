package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.entity.ContentUserRelationGroup;
import org.jeecg.modules.content.user.mapper.ContentUserActivitySnapshotMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationGroupMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.relation.ContentRelationGroupReq;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.jeecg.modules.content.user.vo.ContentFollowFeedItemVO;
import org.jeecg.modules.content.user.vo.ContentFollowFeedPageVO;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentRelationBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentRelationGroupVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserItemVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.jeecg.modules.content.user.vo.ContentUserRelationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation for content user relation.
 */
@Service
public class ContentUserRelationServiceImpl implements IContentUserRelationService {

    private static final int USER_ID_MAX_LENGTH = 64;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final String ACTIVE_STATUS = "ACTIVE";

    @Resource
    private ContentUserRelationMapper relationMapper;

    @Resource
    private ContentUserRelationGroupMapper relationGroupMapper;

    @Resource
    private ContentUserActivitySnapshotMapper activitySnapshotMapper;

    @Resource
    private ContentUserFeedSettingMapper feedSettingMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    /**
     * Creates or refreshes a follow relationship to the target user.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(String operatorUserId, String targetUserId, String relationGroupId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        validateTargetExists(targetUserId);
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        validateFollowAllowed(operatorUserId, targetUserId, relation);
        boolean wasFollowed = Boolean.TRUE.equals(relation.getFollowed());
        relation.setRelationGroupId(resolveRelationGroupId(operatorUserId, relationGroupId));
        relation.setFollowed(Boolean.TRUE);
        relation.setRelationStatus(ACTIVE_STATUS);
        relation.setFollowedAt(new Date());
        relationMapper.updateById(relation);
        syncFollowCount(operatorUserId, targetUserId, wasFollowed, true);
    }

    /**
     * Marks the target user as special follow.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void specialFollow(String operatorUserId, String targetUserId, String relationGroupId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        validateTargetExists(targetUserId);
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        validateFollowAllowed(operatorUserId, targetUserId, relation);
        boolean wasFollowed = Boolean.TRUE.equals(relation.getFollowed());
        boolean wasSpecialFollow = Boolean.TRUE.equals(relation.getSpecialFollow());
        relation.setRelationGroupId(resolveRelationGroupId(operatorUserId, relationGroupId));
        relation.setFollowed(Boolean.TRUE);
        relation.setSpecialFollow(Boolean.TRUE);
        relation.setRelationStatus(ACTIVE_STATUS);
        if (relation.getFollowedAt() == null) {
            relation.setFollowedAt(new Date());
        }
        relation.setSpecialFollowAt(new Date());
        relationMapper.updateById(relation);
        syncFollowCount(operatorUserId, targetUserId, wasFollowed, true);
        syncSpecialFollowCount(operatorUserId, wasSpecialFollow, true);
    }

    /**
     * Cancels the special follow flag while retaining the follow relationship.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSpecialFollow(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
        if (relation == null) {
            return;
        }
        boolean wasSpecialFollow = Boolean.TRUE.equals(relation.getSpecialFollow());
        relation.setSpecialFollow(Boolean.FALSE);
        relation.setSpecialFollowAt(null);
        relationMapper.updateById(relation);
        syncSpecialFollowCount(operatorUserId, wasSpecialFollow, false);
    }

    /**
     * Cancels the follow relationship to the target user.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
        if (relation == null) {
            return;
        }
        boolean wasFollowed = Boolean.TRUE.equals(relation.getFollowed());
        boolean wasSpecialFollow = Boolean.TRUE.equals(relation.getSpecialFollow());
        relation.setFollowed(Boolean.FALSE);
        relation.setSpecialFollow(Boolean.FALSE);
        relation.setSpecialFollowAt(null);
        relationMapper.updateById(relation);
        syncFollowCount(operatorUserId, targetUserId, wasFollowed, false);
        syncSpecialFollowCount(operatorUserId, wasSpecialFollow, false);
    }

    /**
     * Blacklists the target user and cuts off related interactions.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blacklist(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        ContentUserRelation reverseRelation = getOrCreate(targetUserId, operatorUserId);
        boolean wasFollowed = Boolean.TRUE.equals(relation.getFollowed());
        boolean wasSpecialFollow = Boolean.TRUE.equals(relation.getSpecialFollow());
        boolean reverseWasFollowed = Boolean.TRUE.equals(reverseRelation.getFollowed());
        boolean reverseWasSpecialFollow = Boolean.TRUE.equals(reverseRelation.getSpecialFollow());
        relation.setBlacklisted(Boolean.TRUE);
        relation.setMuted(Boolean.TRUE);
        relation.setBlockedByOwner(Boolean.TRUE);
        relation.setFollowed(Boolean.FALSE);
        relation.setSpecialFollow(Boolean.FALSE);
        relation.setBlacklistedAt(new Date());
        relationMapper.updateById(relation);
        reverseRelation.setFollowed(Boolean.FALSE);
        reverseRelation.setSpecialFollow(Boolean.FALSE);
        relationMapper.updateById(reverseRelation);
        syncFollowCount(operatorUserId, targetUserId, wasFollowed, false);
        syncSpecialFollowCount(operatorUserId, wasSpecialFollow, false);
        syncFollowCount(targetUserId, operatorUserId, reverseWasFollowed, false);
        syncSpecialFollowCount(targetUserId, reverseWasSpecialFollow, false);
    }

    /**
     * Removes the blacklist flag and reopens normal interactions.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblacklist(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setBlacklisted(Boolean.FALSE);
        relation.setMuted(Boolean.FALSE);
        relation.setBlockedByOwner(Boolean.FALSE);
        relation.setBlacklistedAt(null);
        relationMapper.updateById(relation);
    }

    /**
     * Mutes the target user for one-way noise reduction.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mute(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setMuted(Boolean.TRUE);
        relation.setMutedAt(new Date());
        relationMapper.updateById(relation);
    }

    /**
     * Removes the mute flag for the target user.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unmute(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setMuted(Boolean.FALSE);
        relation.setMutedAt(null);
        relationMapper.updateById(relation);
    }

    /**
     * Gets the relation details between the current user and the target user.
     */
    @Override
    public ContentUserRelationVO getRelation(String operatorUserId, String targetUserId) {
        validateRelationIdentity(operatorUserId, targetUserId);
        ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
        if (relation == null) {
            relation = new ContentUserRelation()
                .setOwnerUserId(operatorUserId)
                .setTargetUserId(targetUserId)
                .setFollowed(Boolean.FALSE)
                .setSpecialFollow(Boolean.FALSE)
                .setMuted(Boolean.FALSE)
                .setBlacklisted(Boolean.FALSE)
                .setBlockedByOwner(Boolean.FALSE)
                .setRelationStatus(ACTIVE_STATUS);
        }
        return ContentUserRelationVO.from(relation);
    }

    /**
     * 查询用户关注分组，缺省默认分组会被幂等补齐。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ContentRelationGroupVO> listGroups(String operatorUserId) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        ensureDefaultGroup(operatorUserId);
        return relationGroupMapper.selectList(Wrappers.<ContentUserRelationGroup>lambdaQuery()
                .eq(ContentUserRelationGroup::getOwnerUserId, operatorUserId)
                .eq(ContentUserRelationGroup::getGroupStatus, ACTIVE_STATUS)
                .orderByAsc(ContentUserRelationGroup::getSortOrder)
                .orderByAsc(ContentUserRelationGroup::getCreateTime))
            .stream()
            .map(ContentRelationGroupVO::from)
            .toList();
    }

    /**
     * 创建自定义关注分组。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentRelationGroupVO createGroup(String operatorUserId, ContentRelationGroupReq req) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateGroupReq(req);
        rejectDuplicateGroupName(operatorUserId, req.getGroupName(), null);
        ContentUserRelationGroup group = new ContentUserRelationGroup()
            .setOwnerUserId(operatorUserId)
            .setGroupName(req.getGroupName().trim())
            .setSortOrder(req.getSortOrder())
            .setIsDefault(Boolean.FALSE)
            .setGroupStatus(ACTIVE_STATUS);
        group.setId(UUIDGenerator.generate());
        relationGroupMapper.insert(group);
        return ContentRelationGroupVO.from(group);
    }

    /**
     * 重命名并重排自定义关注分组。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentRelationGroupVO renameGroup(String operatorUserId, String groupId, ContentRelationGroupReq req) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateGroupId(groupId);
        validateGroupReq(req);
        ContentUserRelationGroup group = requireOwnedActiveGroup(operatorUserId, groupId);
        if (Boolean.TRUE.equals(group.getIsDefault()) && !group.getGroupName().equals(req.getGroupName().trim())) {
            throw new JeecgBootException("默认分组不允许重命名");
        }
        rejectDuplicateGroupName(operatorUserId, req.getGroupName(), groupId);
        group.setGroupName(req.getGroupName().trim());
        group.setSortOrder(req.getSortOrder());
        relationGroupMapper.updateById(group);
        return ContentRelationGroupVO.from(group);
    }

    /**
     * 删除自定义分组，并将其中关注关系回退到默认分组。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String operatorUserId, String groupId) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateGroupId(groupId);
        ContentUserRelationGroup group = requireOwnedActiveGroup(operatorUserId, groupId);
        if (Boolean.TRUE.equals(group.getIsDefault())) {
            throw new JeecgBootException("默认分组不允许删除");
        }
        ContentUserRelationGroup defaultGroup = ensureDefaultGroup(operatorUserId);
        List<ContentUserRelation> relations = relationMapper.selectList(Wrappers.<ContentUserRelation>lambdaQuery()
            .eq(ContentUserRelation::getOwnerUserId, operatorUserId)
            .eq(ContentUserRelation::getRelationGroupId, groupId)
            .eq(ContentUserRelation::getFollowed, Boolean.TRUE));
        for (ContentUserRelation relation : relations) {
            relation.setRelationGroupId(defaultGroup.getId());
            relationMapper.updateById(relation);
        }
        group.setGroupStatus("DELETED");
        relationGroupMapper.updateById(group);
    }

    /**
     * 将关注对象移动到指定分组。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentRelationBatchResultVO moveTargetsToGroup(String operatorUserId, List<String> targetUserIds, String groupId) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateGroupId(groupId);
        requireOwnedActiveGroup(operatorUserId, groupId);
        return moveRelations(operatorUserId, targetUserIds, groupId);
    }

    /**
     * 将关注对象移出当前分组并回退到默认分组。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentRelationBatchResultVO removeTargetsFromGroup(String operatorUserId, List<String> targetUserIds) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        ContentUserRelationGroup defaultGroup = ensureDefaultGroup(operatorUserId);
        return moveRelations(operatorUserId, targetUserIds, defaultGroup.getId());
    }

    /**
     * 批量取消关注，逐条返回处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentRelationBatchResultVO batchUnfollow(String operatorUserId, List<String> targetUserIds) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateBatchTargetList(targetUserIds);
        ContentRelationBatchResultVO result = new ContentRelationBatchResultVO();
        for (String targetUserId : targetUserIds) {
            ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
            if (relation == null || !Boolean.TRUE.equals(relation.getFollowed())) {
                result.addFailure(targetUserId, "关注关系不存在");
                continue;
            }
            boolean wasSpecialFollow = Boolean.TRUE.equals(relation.getSpecialFollow());
            relation.setFollowed(Boolean.FALSE);
            relation.setSpecialFollow(Boolean.FALSE);
            relation.setSpecialFollowAt(null);
            relationMapper.updateById(relation);
            syncFollowCount(operatorUserId, targetUserId, true, false);
            syncSpecialFollowCount(operatorUserId, wasSpecialFollow, false);
            result.addSuccess();
        }
        return result;
    }

    /**
     * 批量取消特别关注，保留普通关注关系。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentRelationBatchResultVO batchCancelSpecialFollow(String operatorUserId, List<String> targetUserIds) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateBatchTargetList(targetUserIds);
        ContentRelationBatchResultVO result = new ContentRelationBatchResultVO();
        for (String targetUserId : targetUserIds) {
            ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
            if (relation == null || !Boolean.TRUE.equals(relation.getSpecialFollow())) {
                result.addFailure(targetUserId, "特别关注关系不存在");
                continue;
            }
            relation.setSpecialFollow(Boolean.FALSE);
            relation.setSpecialFollowAt(null);
            relationMapper.updateById(relation);
            syncSpecialFollowCount(operatorUserId, true, false);
            result.addSuccess();
        }
        return result;
    }

    /**
     * 分页查询当前用户的关注列表，支持分组和昵称关键词筛选。
     */
    @Override
    public ContentRelationUserPageVO listFollowedUsers(String operatorUserId, String relationGroupId, String keyword, Long pageNo, Long pageSize) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        validateKeyword(keyword);
        if (relationGroupId != null && !relationGroupId.trim().isEmpty()) {
            validateGroupId(relationGroupId);
            requireOwnedActiveGroup(operatorUserId, relationGroupId);
        }
        List<String> keywordTargetIds = findProfileUserIdsByKeyword(keyword);
        if (keyword != null && !keyword.trim().isEmpty() && keywordTargetIds.isEmpty()) {
            return emptyRelationPage(pageNo, pageSize, null);
        }
        return queryRelationUsers(operatorUserId, relationGroupId, keywordTargetIds, false, pageNo, pageSize, null);
    }

    /**
     * 分页查询特别关注列表，并在空列表时返回引导状态码。
     */
    @Override
    public ContentRelationUserPageVO listSpecialFollowedUsers(String operatorUserId, Long pageNo, Long pageSize) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        return queryRelationUsers(operatorUserId, null, Collections.emptyList(), true, pageNo, pageSize, "NO_SPECIAL_FOLLOW");
    }

    /**
     * 查询关注流，按特别关注优先和动态时间倒序返回可见动态。
     */
    @Override
    public ContentFollowFeedPageVO listFollowFeed(String operatorUserId, Long pageNo, Long pageSize) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);
        List<ContentUserRelation> followedRelations = relationMapper.selectList(Wrappers.<ContentUserRelation>lambdaQuery()
            .eq(ContentUserRelation::getOwnerUserId, operatorUserId)
            .eq(ContentUserRelation::getFollowed, Boolean.TRUE)
            .eq(ContentUserRelation::getRelationStatus, ACTIVE_STATUS));
        List<ContentUserRelation> visibleRelations = followedRelations.stream()
            .filter(this::isFeedRelationVisible)
            .toList();
        if (visibleRelations.isEmpty()) {
            return emptyFeedPage(currentPage, currentSize);
        }
        Map<String, ContentUserRelation> relationMap = visibleRelations.stream()
            .collect(Collectors.toMap(ContentUserRelation::getTargetUserId, Function.identity(), (left, right) -> left));
        List<String> actorUserIds = visibleRelations.stream()
            .map(ContentUserRelation::getTargetUserId)
            .toList();
        Set<String> enabledTypes = enabledFeedTypes(operatorUserId);
        List<ContentUserActivitySnapshot> snapshots = activitySnapshotMapper.selectList(Wrappers.<ContentUserActivitySnapshot>lambdaQuery()
            .in(ContentUserActivitySnapshot::getActorUserId, actorUserIds)
            .in(ContentUserActivitySnapshot::getActivityType, enabledTypes)
            .eq(ContentUserActivitySnapshot::getSnapshotStatus, ACTIVE_STATUS));
        List<ContentFollowFeedItemVO> allItems = snapshots.stream()
            .filter(snapshot -> relationMap.containsKey(snapshot.getActorUserId()))
            .filter(snapshot -> isSnapshotVisible(operatorUserId, snapshot))
            .map(snapshot -> ContentFollowFeedItemVO.from(snapshot, relationMap.get(snapshot.getActorUserId())))
            .sorted(Comparator.comparing(ContentFollowFeedItemVO::getSpecialFollow, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ContentFollowFeedItemVO::getActivityTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ContentFollowFeedItemVO::getSnapshotId, Comparator.nullsLast(String::compareTo)))
            .toList();
        int fromIndex = (int) Math.min((currentPage - 1L) * currentSize, allItems.size());
        int toIndex = (int) Math.min(fromIndex + currentSize, allItems.size());
        return new ContentFollowFeedPageVO()
            .setRecords(allItems.subList(fromIndex, toIndex))
            .setTotal((long) allItems.size())
            .setPageNo(currentPage)
            .setPageSize(currentSize)
            .setHasMore(toIndex < allItems.size());
    }

    private void validateRelationIdentity(String operatorUserId, String targetUserId) {
        requireValidUserId(operatorUserId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        requireValidUserId(targetUserId, "目标用户ID不能为空", "目标用户ID长度不能超过64位");
        if (operatorUserId.equals(targetUserId)) {
            throw new JeecgBootException("不能关注或操作自己");
        }
    }

    private void requireValidUserId(String userId, String blankMessage, String lengthMessage) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException(blankMessage);
        }
        if (userId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException(lengthMessage);
        }
    }

    private void validateGroupReq(ContentRelationGroupReq req) {
        if (req == null) {
            throw new JeecgBootException("分组请求不能为空");
        }
        if (req.getGroupName() == null || req.getGroupName().trim().isEmpty()) {
            throw new JeecgBootException("分组名称不能为空");
        }
        if (req.getGroupName().length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("分组名称长度不能超过64位");
        }
        if (req.getSortOrder() == null) {
            throw new JeecgBootException("排序值不能为空");
        }
        if (req.getSortOrder() < 0) {
            throw new JeecgBootException("排序值不能小于0");
        }
    }

    private void validateGroupId(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new JeecgBootException("关系分组ID不能为空");
        }
        if (groupId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("关系分组ID长度不能超过64位");
        }
    }

    private void validateKeyword(String keyword) {
        if (keyword != null && keyword.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("关键词长度不能超过64位");
        }
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1L ? 1L : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        long currentSize = pageSize == null || pageSize < 1L ? DEFAULT_PAGE_SIZE : pageSize;
        if (currentSize > MAX_PAGE_SIZE) {
            throw new JeecgBootException("分页大小不能超过100");
        }
        return currentSize;
    }

    private List<String> findProfileUserIdsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return profileMapper.selectList(Wrappers.<ContentUserProfile>lambdaQuery()
                .like(ContentUserProfile::getNickname, keyword.trim()))
            .stream()
            .map(ContentUserProfile::getUserId)
            .toList();
    }

    private ContentRelationUserPageVO queryRelationUsers(String operatorUserId, String relationGroupId,
                                                         List<String> keywordTargetIds, boolean specialOnly,
                                                         Long pageNo, Long pageSize, String emptyStateCode) {
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);
        IPage<ContentUserRelation> page = relationMapper.selectPage(new Page<>(currentPage, currentSize),
            Wrappers.<ContentUserRelation>lambdaQuery()
                .eq(ContentUserRelation::getOwnerUserId, operatorUserId)
                .eq(ContentUserRelation::getFollowed, Boolean.TRUE)
                .eq(ContentUserRelation::getRelationStatus, ACTIVE_STATUS)
                .eq(relationGroupId != null && !relationGroupId.trim().isEmpty(), ContentUserRelation::getRelationGroupId, relationGroupId)
                .eq(specialOnly, ContentUserRelation::getSpecialFollow, Boolean.TRUE)
                .in(keywordTargetIds != null && !keywordTargetIds.isEmpty(), ContentUserRelation::getTargetUserId, keywordTargetIds)
                .orderByDesc(ContentUserRelation::getFollowedAt));
        List<ContentRelationUserItemVO> records = buildRelationUserItems(page.getRecords());
        return new ContentRelationUserPageVO()
            .setRecords(records)
            .setTotal(page.getTotal())
            .setPageNo(currentPage)
            .setPageSize(currentSize)
            .setEmptyStateCode(page.getTotal() == 0L ? emptyStateCode : null);
    }

    private ContentRelationUserPageVO emptyRelationPage(Long pageNo, Long pageSize, String emptyStateCode) {
        return new ContentRelationUserPageVO()
            .setPageNo(normalizePageNo(pageNo))
            .setPageSize(normalizePageSize(pageSize))
            .setEmptyStateCode(emptyStateCode);
    }

    private ContentFollowFeedPageVO emptyFeedPage(long pageNo, long pageSize) {
        return new ContentFollowFeedPageVO()
            .setRecords(Collections.emptyList())
            .setTotal(0L)
            .setPageNo(pageNo)
            .setPageSize(pageSize)
            .setHasMore(Boolean.FALSE);
    }

    private boolean isFeedRelationVisible(ContentUserRelation relation) {
        return !Boolean.TRUE.equals(relation.getMuted())
            && !Boolean.TRUE.equals(relation.getBlacklisted())
            && !Boolean.TRUE.equals(relation.getBlockedByOwner());
    }

    private boolean isSnapshotVisible(String operatorUserId, ContentUserActivitySnapshot snapshot) {
        if (snapshot == null || "PRIVATE".equals(snapshot.getVisibleScope())) {
            return false;
        }
        return visibilityPolicyService == null || visibilityPolicyService.canViewContent(snapshot.getActorUserId(), operatorUserId);
    }

    private Set<String> enabledFeedTypes(String operatorUserId) {
        ContentUserFeedSetting setting = feedSettingMapper == null ? null : feedSettingMapper.selectByUserId(operatorUserId);
        if (setting == null || setting.getActivityTypes() == null || setting.getActivityTypes().trim().isEmpty()) {
            return Set.of("PUBLISH", "LIKE", "FAVORITE");
        }
        return java.util.Arrays.stream(setting.getActivityTypes().split(","))
            .map(String::trim)
            .filter(type -> !type.isEmpty())
            .collect(Collectors.toSet());
    }

    private List<ContentRelationUserItemVO> buildRelationUserItems(List<ContentUserRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> targetUserIds = relations.stream()
            .map(ContentUserRelation::getTargetUserId)
            .toList();
        Map<String, ContentUserProfile> profileMap = profileMapper.selectList(Wrappers.<ContentUserProfile>lambdaQuery()
                .in(ContentUserProfile::getUserId, targetUserIds))
            .stream()
            .collect(Collectors.toMap(ContentUserProfile::getUserId, Function.identity(), (left, right) -> left));
        Map<String, ContentUserActivitySnapshot> activityMap = activitySnapshotMapper.selectList(Wrappers.<ContentUserActivitySnapshot>lambdaQuery()
                .in(ContentUserActivitySnapshot::getActorUserId, targetUserIds)
                .eq(ContentUserActivitySnapshot::getSnapshotStatus, ACTIVE_STATUS)
                .orderByDesc(ContentUserActivitySnapshot::getActivityTime))
            .stream()
            .collect(Collectors.toMap(ContentUserActivitySnapshot::getActorUserId, Function.identity(), (left, right) -> left));
        return relations.stream()
            .map(relation -> ContentRelationUserItemVO.from(relation, profileMap.get(relation.getTargetUserId()), activityMap.get(relation.getTargetUserId())))
            .toList();
    }

    private void rejectDuplicateGroupName(String operatorUserId, String groupName, String currentGroupId) {
        ContentUserRelationGroup duplicate = relationGroupMapper.selectOne(Wrappers.<ContentUserRelationGroup>lambdaQuery()
            .eq(ContentUserRelationGroup::getOwnerUserId, operatorUserId)
            .eq(ContentUserRelationGroup::getGroupName, groupName.trim())
            .eq(ContentUserRelationGroup::getGroupStatus, ACTIVE_STATUS)
            .last("limit 1"));
        if (duplicate != null && !Objects.equals(duplicate.getId(), currentGroupId)) {
            throw new JeecgBootException("分组名称已存在");
        }
    }

    private ContentUserRelationGroup requireOwnedActiveGroup(String operatorUserId, String groupId) {
        ContentUserRelationGroup group = relationGroupMapper.selectById(groupId);
        if (group == null || !operatorUserId.equals(group.getOwnerUserId()) || !ACTIVE_STATUS.equals(group.getGroupStatus())) {
            throw new JeecgBootException("关系分组不存在或无权操作");
        }
        return group;
    }

    private ContentUserRelationGroup ensureDefaultGroup(String operatorUserId) {
        ContentUserRelationGroup defaultGroup = relationGroupMapper.selectOne(Wrappers.<ContentUserRelationGroup>lambdaQuery()
            .eq(ContentUserRelationGroup::getOwnerUserId, operatorUserId)
            .eq(ContentUserRelationGroup::getIsDefault, Boolean.TRUE)
            .eq(ContentUserRelationGroup::getGroupStatus, ACTIVE_STATUS)
            .last("limit 1"));
        if (defaultGroup != null) {
            return defaultGroup;
        }
        ContentUserRelationGroup group = new ContentUserRelationGroup()
            .setOwnerUserId(operatorUserId)
            .setGroupName("默认分组")
            .setSortOrder(0)
            .setIsDefault(Boolean.TRUE)
            .setGroupStatus(ACTIVE_STATUS);
        group.setId(UUIDGenerator.generate());
        relationGroupMapper.insert(group);
        return group;
    }

    private String resolveRelationGroupId(String operatorUserId, String relationGroupId) {
        if (relationGroupId == null || relationGroupId.trim().isEmpty()) {
            return ensureDefaultGroup(operatorUserId).getId();
        }
        validateGroupId(relationGroupId);
        return requireOwnedActiveGroup(operatorUserId, relationGroupId).getId();
    }

    private ContentRelationBatchResultVO moveRelations(String operatorUserId, List<String> targetUserIds, String targetGroupId) {
        ContentRelationBatchResultVO result = new ContentRelationBatchResultVO();
        validateBatchTargetList(targetUserIds);
        for (String targetUserId : targetUserIds) {
            ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
            if (relation == null || !Boolean.TRUE.equals(relation.getFollowed())) {
                result.addFailure(targetUserId, "关注关系不存在");
                continue;
            }
            relation.setRelationGroupId(targetGroupId);
            relationMapper.updateById(relation);
            result.addSuccess();
        }
        return result;
    }

    private void validateBatchTargetList(List<String> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new JeecgBootException("目标用户ID列表不能为空");
        }
        if (targetUserIds.size() > 100) {
            throw new JeecgBootException("目标用户ID数量不能超过100个");
        }
        Set<String> seen = new HashSet<>();
        for (String targetUserId : targetUserIds) {
            if (targetUserId == null || targetUserId.trim().isEmpty()) {
                throw new JeecgBootException("目标用户ID不能为空");
            }
            if (targetUserId.length() > USER_ID_MAX_LENGTH) {
                throw new JeecgBootException("目标用户ID长度不能超过64位");
            }
            if (!seen.add(targetUserId)) {
                throw new JeecgBootException("目标用户ID不能重复");
            }
        }
    }

    private void validateTargetExists(String targetUserId) {
        if (profileMapper != null && profileMapper.selectByUserId(targetUserId) == null) {
            throw new JeecgBootException("目标用户不存在");
        }
    }

    private void validateFollowAllowed(String operatorUserId, String targetUserId, ContentUserRelation relation) {
        if (Boolean.TRUE.equals(relation.getBlacklisted()) || Boolean.TRUE.equals(relation.getBlockedByOwner())) {
            throw new JeecgBootException("拉黑关系中不可关注");
        }
        ContentUserRelation reverseRelation = relationMapper.selectByPair(targetUserId, operatorUserId);
        if (reverseRelation != null
            && (Boolean.TRUE.equals(reverseRelation.getBlacklisted()) || Boolean.TRUE.equals(reverseRelation.getBlockedByOwner()))) {
            throw new JeecgBootException("对方已拉黑当前用户，无法关注");
        }
    }

    private void syncFollowCount(String operatorUserId, String targetUserId, boolean before, boolean after) {
        if (before == after) {
            return;
        }
        int delta = after ? 1 : -1;
        // 关注数和粉丝数必须同源更新，避免列表统计与主页计数长期漂移。
        profileMapper.changeFollowingCount(operatorUserId, delta);
        profileMapper.changeFollowerCount(targetUserId, delta);
    }

    private void syncSpecialFollowCount(String operatorUserId, boolean before, boolean after) {
        if (before == after) {
            return;
        }
        profileMapper.changeSpecialFollowCount(operatorUserId, after ? 1 : -1);
    }

    private ContentUserRelation getOrCreate(String operatorUserId, String targetUserId) {
        ContentUserRelation relation = relationMapper.selectByPair(operatorUserId, targetUserId);
        if (relation != null) {
            return relation;
        }
        relation = new ContentUserRelation();
        relation.setId(UUIDGenerator.generate());
        relation.setOwnerUserId(operatorUserId);
        relation.setTargetUserId(targetUserId);
        relation.setFollowed(Boolean.FALSE);
        relation.setSpecialFollow(Boolean.FALSE);
        relation.setMuted(Boolean.FALSE);
        relation.setBlacklisted(Boolean.FALSE);
        relation.setBlockedByOwner(Boolean.FALSE);
        relation.setRelationStatus(ACTIVE_STATUS);
        relationMapper.insert(relation);
        return relation;
    }
}
