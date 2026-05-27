package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContentUserThirdPartyAuth 实体字段测试。
 * 验证 Lombok 生成的 getter/setter 和链式调用正常工作。
 */
class ContentUserThirdPartyAuthEntityTest {

    /**
     * 所有自定义字段均可正常读写。
     */
    @Test
    void shouldSetAndGetAllFields() {
        Date now = new Date();
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setAppName("微信")
            .setAuthTime(now)
            .setScopes("[\"read\",\"write\"]")
            .setTokenHash("abc123hash")
            .setStatus("ACTIVE");

        assertThat(auth.getUserId()).isEqualTo("user-1");
        assertThat(auth.getAppName()).isEqualTo("微信");
        assertThat(auth.getAuthTime()).isEqualTo(now);
        assertThat(auth.getScopes()).isEqualTo("[\"read\",\"write\"]");
        assertThat(auth.getTokenHash()).isEqualTo("abc123hash");
        assertThat(auth.getStatus()).isEqualTo("ACTIVE");
    }

    /**
     * ACTIVE 和 REVOKED 是合法的状态值。
     */
    @Test
    void shouldAcceptValidStatusValues() {
        ContentUserThirdPartyAuth active = new ContentUserThirdPartyAuth().setStatus("ACTIVE");
        ContentUserThirdPartyAuth revoked = new ContentUserThirdPartyAuth().setStatus("REVOKED");

        assertThat(active.getStatus()).isEqualTo("ACTIVE");
        assertThat(revoked.getStatus()).isEqualTo("REVOKED");
    }
}
