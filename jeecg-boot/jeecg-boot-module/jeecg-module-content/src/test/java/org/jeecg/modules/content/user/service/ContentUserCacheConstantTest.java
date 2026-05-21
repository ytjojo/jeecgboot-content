package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.constant.ContentUserCacheConstant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内容社区资料缓存 Key 契约测试。
 */
class ContentUserCacheConstantTest {

    @Test
    void shouldBuildSafeProfilePrivacyAndViewerCacheKeys() {
        assertThat(ContentUserCacheConstant.profileCacheKey("u1")).isEqualTo("content:user:profile:u1");
        assertThat(ContentUserCacheConstant.privacyCacheKey("u1")).isEqualTo("content:user:privacy:u1");
        assertThat(ContentUserCacheConstant.publicProfileCacheKey("u1")).isEqualTo("content:user:profile:public:u1");
        assertThat(ContentUserCacheConstant.viewerProfileCacheKey("u1", "OWNER")).isEqualTo("content:user:profile:u1:OWNER");
    }

    @Test
    void shouldRejectUnsafeCacheKeyParts() {
        assertThatThrownBy(() -> ContentUserCacheConstant.profileCacheKey(null)).hasMessageContaining("用户ID不能为空");
        assertThatThrownBy(() -> ContentUserCacheConstant.profileCacheKey("")).hasMessageContaining("用户ID不能为空");
        assertThatThrownBy(() -> ContentUserCacheConstant.profileCacheKey("a".repeat(65))).hasMessageContaining("用户ID不合法");
        assertThatThrownBy(() -> ContentUserCacheConstant.viewerProfileCacheKey("u1", null)).hasMessageContaining("查看者范围不能为空");
        assertThatThrownBy(() -> ContentUserCacheConstant.viewerProfileCacheKey("u1", "BAD")).hasMessageContaining("查看者范围不合法");
    }
}
