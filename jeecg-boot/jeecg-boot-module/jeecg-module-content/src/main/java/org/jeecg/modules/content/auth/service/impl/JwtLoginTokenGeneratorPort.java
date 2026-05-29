package org.jeecg.modules.content.auth.service.impl;

import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.auth.service.LoginTokenGeneratorPort;
import org.springframework.stereotype.Component;

/**
 * 基于JwtUtil的登录token生成实现。
 */
@Component
public class JwtLoginTokenGeneratorPort implements LoginTokenGeneratorPort {

    @Override
    public String generateToken(String username, String clientType) {
        return JwtUtil.sign(username, "", clientType);
    }
}
