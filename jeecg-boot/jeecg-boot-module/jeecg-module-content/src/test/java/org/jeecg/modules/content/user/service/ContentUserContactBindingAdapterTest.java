package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.service.impl.ContentUserContactBindingAdapterImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 账号绑定状态适配器测试。
 */
class ContentUserContactBindingAdapterTest {

    private final ContentUserContactBindingAdapterImpl adapter = new ContentUserContactBindingAdapterImpl();

    @Test
    void shouldAlwaysReturnUnverified() {
        var result = adapter.getBindingState("user-123");

        assertThat(result.mobileVerified()).isFalse();
        assertThat(result.emailVerified()).isFalse();
    }

    @Test
    void shouldHandleNullUserId() {
        var result = adapter.getBindingState(null);

        assertThat(result.mobileVerified()).isFalse();
        assertThat(result.emailVerified()).isFalse();
    }

    @Test
    void shouldHandleEmptyUserId() {
        var result = adapter.getBindingState("");

        assertThat(result.mobileVerified()).isFalse();
        assertThat(result.emailVerified()).isFalse();
    }
}
