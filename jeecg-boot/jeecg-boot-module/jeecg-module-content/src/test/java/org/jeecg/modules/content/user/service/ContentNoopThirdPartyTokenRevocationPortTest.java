package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.service.impl.ContentNoopThirdPartyTokenRevocationPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Token 撤销端口空实现测试。
 */
class ContentNoopThirdPartyTokenRevocationPortTest {

    private final ContentNoopThirdPartyTokenRevocationPort port = new ContentNoopThirdPartyTokenRevocationPort();

    @Test
    void shouldAlwaysReturnTrue() {
        boolean result = port.revokeTokens("auth-001", "token-hash", "refresh-hash");

        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleNullAuthId() {
        boolean result = port.revokeTokens(null, "token-hash", "refresh-hash");

        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleNullTokens() {
        boolean result = port.revokeTokens("auth-001", null, null);

        assertThat(result).isTrue();
    }
}
