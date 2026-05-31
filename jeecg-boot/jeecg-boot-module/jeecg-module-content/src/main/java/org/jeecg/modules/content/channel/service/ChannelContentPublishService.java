package org.jeecg.modules.content.channel.service;

public interface ChannelContentPublishService {
    /**
     * 校验发布权限
     * @return ALLOW/REJECT/REVIEW
     */
    String checkPublishPermission(String userRole, String publishPermission, boolean isMuted, boolean isBlacklisted);
}
