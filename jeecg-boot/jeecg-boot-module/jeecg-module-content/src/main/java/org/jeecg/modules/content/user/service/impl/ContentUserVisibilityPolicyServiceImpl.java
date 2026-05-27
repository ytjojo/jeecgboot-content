package org.jeecg.modules.content.user.service.impl;

import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.enums.ContentUserVisibilityEnum;
import org.jeecg.modules.content.user.mapper.ContentUserPrivacySettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Objects;

/**
 * Service implementation for content user visibility policy.
 */
@Service
public class ContentUserVisibilityPolicyServiceImpl implements IContentUserVisibilityPolicyService {

    @Resource
    private ContentUserRelationMapper relationMapper;

    @Resource
    private ContentUserPrivacySettingMapper privacySettingMapper;

    /**
     * Checks whether a profile field is visible to the current viewer.
     */
    @Override
    public boolean canViewField(String ownerUserId, String viewerUserId, String visibility) {
        if (Objects.equals(ownerUserId, viewerUserId)) {
            return true;
        }
        if (ContentUserVisibilityEnum.PUBLIC.getCode().equals(visibility)) {
            return true;
        }
        if (ContentUserVisibilityEnum.PRIVATE.getCode().equals(visibility)) {
            return false;
        }
        ContentUserRelation viewerToOwner = relationMapper.selectByPair(viewerUserId, ownerUserId);
        if (ContentUserVisibilityEnum.FOLLOWERS_ONLY.getCode().equals(visibility)) {
            return viewerToOwner != null && Boolean.TRUE.equals(viewerToOwner.getFollowed());
        }
        if (ContentUserVisibilityEnum.MUTUAL_ONLY.getCode().equals(visibility)) {
            ContentUserRelation ownerToViewer = relationMapper.selectByPair(ownerUserId, viewerUserId);
            return viewerToOwner != null && ownerToViewer != null
                && Boolean.TRUE.equals(viewerToOwner.getFollowed())
                && Boolean.TRUE.equals(ownerToViewer.getFollowed());
        }
        return false;
    }

    /**
     * Checks whether content is visible to the current viewer.
     */
    @Override
    public boolean canViewContent(String ownerUserId, String viewerUserId) {
        if (Objects.equals(ownerUserId, viewerUserId)) {
            return true;
        }
        ContentUserRelation ownerToViewer = relationMapper.selectByPair(ownerUserId, viewerUserId);
        if (ownerToViewer != null && Boolean.TRUE.equals(ownerToViewer.getBlockedByOwner())) {
            return false;
        }
        ContentUserRelation viewerToOwner = relationMapper.selectByPair(viewerUserId, ownerUserId);
        if (viewerToOwner != null
            && (Boolean.TRUE.equals(viewerToOwner.getBlockedByOwner()) || Boolean.TRUE.equals(viewerToOwner.getBlacklisted()))) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the target user can be found by the current viewer.
     */
    @Override
    public boolean canSearchUser(String ownerUserId, String viewerUserId) {
        if (Objects.equals(ownerUserId, viewerUserId)) {
            return true;
        }
        if (privacySettingMapper != null) {
            var privacySetting = privacySettingMapper.selectByUserId(ownerUserId);
            if (privacySetting != null && Boolean.FALSE.equals(privacySetting.getAllowUserSearch())) {
                return false;
            }
        }
        ContentUserRelation ownerToViewer = relationMapper.selectByPair(ownerUserId, viewerUserId);
        return ownerToViewer == null || !Boolean.TRUE.equals(ownerToViewer.getBlockedByOwner());
    }

    /**
     * Checks whether a private message can be sent to the target user.
     */
    @Override
    public boolean canSendPrivateMessage(String ownerUserId, String viewerUserId) {
        return canViewContent(ownerUserId, viewerUserId);
    }

    /**
     * Checks whether the current viewer can mention the target user.
     */
    @Override
    public boolean canMention(String ownerUserId, String viewerUserId) {
        return canViewContent(ownerUserId, viewerUserId);
    }

    /**
     * 判断查看者是否可以看到目标用户的在线状态。
     * 支持三级可见性：PUBLIC（公开）、HIDDEN（隐藏）、MUTUAL_ONLY（仅互关可见）。
     */
    @Override
    public boolean canViewOnlineStatus(String ownerUserId, String viewerUserId) {
        if (Objects.equals(ownerUserId, viewerUserId)) {
            return true;
        }
        var privacySetting = privacySettingMapper.selectByUserId(ownerUserId);
        String visibility = (privacySetting != null && privacySetting.getOnlineStatusVisibility() != null)
            ? privacySetting.getOnlineStatusVisibility() : "PUBLIC";
        if ("PUBLIC".equals(visibility)) {
            return true;
        }
        if ("HIDDEN".equals(visibility)) {
            return false;
        }
        if ("MUTUAL_ONLY".equals(visibility)) {
            ContentUserRelation viewerToOwner = relationMapper.selectByPair(viewerUserId, ownerUserId);
            ContentUserRelation ownerToViewer = relationMapper.selectByPair(ownerUserId, viewerUserId);
            return viewerToOwner != null && ownerToViewer != null
                && Boolean.TRUE.equals(viewerToOwner.getFollowed())
                && Boolean.TRUE.equals(ownerToViewer.getFollowed());
        }
        return false;
    }

    /**
     * 判断查看者是否可以看到目标用户的活动（浏览历史、点赞、收藏等）。
     * 直接复用 canViewField 的可见性判断逻辑。
     */
    @Override
    public boolean canViewActivity(String ownerUserId, String viewerUserId, String visibility) {
        return canViewField(ownerUserId, viewerUserId, visibility);
    }

    /**
     * 判断资料页面是否应返回 noindex 指令。
     * 当 allowSearchEngineIndex 为 false 或未设置时返回 true（默认不索引）。
     */
    @Override
    public boolean shouldNoindexProfile(String profileUserId) {
        var privacySetting = privacySettingMapper.selectByUserId(profileUserId);
        if (privacySetting == null) {
            return true;
        }
        return !Boolean.TRUE.equals(privacySetting.getAllowSearchEngineIndex());
    }
}
