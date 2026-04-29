package org.jeecg.modules.content.user.service.impl;

import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.enums.ContentUserVisibilityEnum;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Objects;

@Service
public class ContentUserVisibilityPolicyServiceImpl implements IContentUserVisibilityPolicyService {

    @Resource
    private ContentUserRelationMapper relationMapper;

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
        return viewerToOwner == null || !Boolean.TRUE.equals(viewerToOwner.getMuted());
    }

    @Override
    public boolean canSearchUser(String ownerUserId, String viewerUserId) {
        ContentUserRelation ownerToViewer = relationMapper.selectByPair(ownerUserId, viewerUserId);
        return ownerToViewer == null || !Boolean.TRUE.equals(ownerToViewer.getBlockedByOwner());
    }

    @Override
    public boolean canSendPrivateMessage(String ownerUserId, String viewerUserId) {
        return canViewContent(ownerUserId, viewerUserId);
    }

    @Override
    public boolean canMention(String ownerUserId, String viewerUserId) {
        return canViewContent(ownerUserId, viewerUserId);
    }
}
