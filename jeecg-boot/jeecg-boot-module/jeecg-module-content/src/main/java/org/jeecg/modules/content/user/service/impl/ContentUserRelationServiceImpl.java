package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentUserRelationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;

/**
 * Service implementation for content user relation.
 */
@Service
public class ContentUserRelationServiceImpl implements IContentUserRelationService {

    @Resource
    private ContentUserRelationMapper relationMapper;

    /**
     * Creates or refreshes a follow relationship to the target user.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(String operatorUserId, String targetUserId, String relationGroupId) {
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setRelationGroupId(relationGroupId);
        relation.setFollowed(Boolean.TRUE);
        relation.setSpecialFollow(Boolean.FALSE);
        relation.setFollowedAt(new Date());
        relationMapper.updateById(relation);
    }

    /**
     * Cancels the follow relationship to the target user.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(String operatorUserId, String targetUserId) {
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setFollowed(Boolean.FALSE);
        relation.setSpecialFollow(Boolean.FALSE);
        relationMapper.updateById(relation);
    }

    /**
     * Blacklists the target user and cuts off related interactions.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blacklist(String operatorUserId, String targetUserId) {
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setBlacklisted(Boolean.TRUE);
        relation.setMuted(Boolean.TRUE);
        relation.setBlockedByOwner(Boolean.TRUE);
        relation.setFollowed(Boolean.FALSE);
        relation.setSpecialFollow(Boolean.FALSE);
        relation.setBlacklistedAt(new Date());
        relationMapper.updateById(relation);
    }

    /**
     * Mutes the target user for one-way noise reduction.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mute(String operatorUserId, String targetUserId) {
        ContentUserRelation relation = getOrCreate(operatorUserId, targetUserId);
        relation.setMuted(Boolean.TRUE);
        relation.setMutedAt(new Date());
        relationMapper.updateById(relation);
    }

    /**
     * Gets the relation details between the current user and the target user.
     */
    @Override
    public ContentUserRelationVO getRelation(String operatorUserId, String targetUserId) {
        return ContentUserRelationVO.from(getOrCreate(operatorUserId, targetUserId));
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
        relationMapper.insert(relation);
        return relation;
    }
}
