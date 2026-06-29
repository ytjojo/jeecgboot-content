package org.jeecg.modules.content.channel.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.ChannelMemberService;

@Slf4j
public class ChannelSecurityUtil {

    private ChannelSecurityUtil() {
    }

    public static String getCurrentUserIdOrThrow() {
        try {
            return SecureUtil.currentUser().getId();
        } catch (Exception e) {
            throw new JeecgBootException("用户未登录");
        }
    }

    public static void checkChannelAdminPermission(ChannelMemberService memberService, String channelId, String operatorId) {
        if (channelId == null || operatorId == null) {
            throw new JeecgBootException("参数不完整");
        }
        ChannelMember operator = memberService.getByChannelAndUser(channelId, operatorId);
        if (operator == null) {
            throw new JeecgBootException("您不是该频道成员");
        }
        if (operator.getRole() == null || operator.getRole() > MemberRole.ADMIN.getCode()) {
            throw new JeecgBootException("权限不足，需要频道管理员权限");
        }
    }

    public static void checkChannelManagePermission(ChannelMemberService memberService, String channelId, String operatorId) {
        checkChannelAdminPermission(memberService, channelId, operatorId);
    }

    public static boolean isChannelAdmin(ChannelMemberService memberService, String channelId, String userId) {
        if (channelId == null || userId == null) {
            return false;
        }
        ChannelMember member = memberService.getByChannelAndUser(channelId, userId);
        return member != null && member.getRole() != null && member.getRole() <= MemberRole.ADMIN.getCode();
    }
}
