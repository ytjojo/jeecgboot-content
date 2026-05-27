package org.jeecg.modules.content.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.service.ContentThirdPartyTokenRevocationPort;
import org.springframework.stereotype.Component;

/**
 * Token 撤销端口的空实现。
 * 记录待撤销状态，直到接入真实的第三方 auth-module adapter。
 */
@Slf4j
@Component
public class ContentNoopThirdPartyTokenRevocationPort implements ContentThirdPartyTokenRevocationPort {

    @Override
    public boolean revokeTokens(String authId, String tokenHash, String refreshTokenHash) {
        log.info("Token 撤销端口（Noop）：授权 {} 标记为待撤销，等待接入真实 adapter", authId);
        return true;
    }
}
