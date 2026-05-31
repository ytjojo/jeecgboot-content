package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.enums.PublishPermissionEnum;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.springframework.stereotype.Service;

@Service
public class ChannelContentPublishServiceImpl implements ChannelContentPublishService {

    @Override
    public String checkPublishPermission(String userRole, String publishPermission, boolean isMuted, boolean isBlacklisted) {
        if (isMuted || isBlacklisted) {
            return "REJECT";
        }
        if (PublishPermissionEnum.PRE_REVIEW.getCode().equals(publishPermission)) {
            return "REVIEW";
        }
        if (PublishPermissionEnum.ADMIN_ONLY.getCode().equals(publishPermission)) {
            return "ADMIN".equals(userRole) || "OWNER".equals(userRole) || "EDITOR".equals(userRole) ? "ALLOW" : "REJECT";
        }
        if (PublishPermissionEnum.PUBLIC_SUBMIT.getCode().equals(publishPermission)) {
            if ("NON_MEMBER".equals(userRole)) {
                return "REVIEW";
            }
            return "ALLOW";
        }
        if (PublishPermissionEnum.ALL_MEMBERS.getCode().equals(publishPermission)) {
            return "NON_MEMBER".equals(userRole) ? "REJECT" : "ALLOW";
        }
        return "REJECT";
    }
}
