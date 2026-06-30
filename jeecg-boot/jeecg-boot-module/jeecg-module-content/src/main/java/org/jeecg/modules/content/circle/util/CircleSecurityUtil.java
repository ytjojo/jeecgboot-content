package org.jeecg.modules.content.circle.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.config.security.utils.SecureUtil;

@Slf4j
public class CircleSecurityUtil {

    private CircleSecurityUtil() {
    }

    public static String getCurrentUserIdOrNull() {
        try {
            return SecureUtil.currentUser().getId();
        } catch (Exception e) {
            log.debug("Failed to get current user id, returning null", e);
            return null;
        }
    }

    public static String getCurrentUserIdOrThrow() {
        try {
            return SecureUtil.currentUser().getId();
        } catch (Exception e) {
            throw new JeecgBootException("用户未登录");
        }
    }
}
