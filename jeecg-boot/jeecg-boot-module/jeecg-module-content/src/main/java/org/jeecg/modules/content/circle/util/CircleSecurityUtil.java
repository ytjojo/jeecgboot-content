package org.jeecg.modules.content.circle.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class CircleSecurityUtil {

    private CircleSecurityUtil() {
    }

    public static String getCurrentUserIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return null;
            }
            String name = authentication.getName();
            if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
                return null;
            }
            LoginUser user = JSONObject.parseObject(name, LoginUser.class);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.debug("Failed to get current user id, returning null", e);
            return null;
        }
    }

    public static String getCurrentUserIdOrThrow() {
        String userId = getCurrentUserIdOrNull();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
        return userId;
    }
}
