package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.service.impl.ContentUserMediaAdapterImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内容社区资料素材校验测试。
 */
class ContentUserMediaAdapterTest {

    private final ContentUserMediaAdapterImpl mediaAdapter = new ContentUserMediaAdapterImpl();

    @Test
    void shouldValidateAvatarFormatSizeAndDimensions() {
        mediaAdapter.validateAvatar("https://cdn.example.com/a.jpg?size=5242880&width=4096&height=4096");
        mediaAdapter.validateAvatar("https://cdn.example.com/a.png");
        mediaAdapter.validateAvatar("https://cdn.example.com/a.webp");

        assertThatThrownBy(() -> mediaAdapter.validateAvatar(""))
            .hasMessageContaining("头像不能为空");
        assertThatThrownBy(() -> mediaAdapter.validateAvatar("https://cdn.example.com/a.gif"))
            .hasMessageContaining("头像仅支持 JPG、PNG、WebP");
        assertThatThrownBy(() -> mediaAdapter.validateAvatar("https://cdn.example.com/a.jpg?size=5242881"))
            .hasMessageContaining("头像大小不能超过5MB");
        assertThatThrownBy(() -> mediaAdapter.validateAvatar("https://cdn.example.com/a.jpg?width=0"))
            .hasMessageContaining("头像宽度不合法");
        assertThatThrownBy(() -> mediaAdapter.validateAvatar("https://cdn.example.com/a.jpg?height=4097"))
            .hasMessageContaining("头像高度不合法");
    }

    @Test
    void shouldValidateHomepageBackgroundFormatSizeAndDimensions() {
        mediaAdapter.validateHomepageBackground(null);
        mediaAdapter.validateHomepageBackground("https://cdn.example.com/bg.webp?size=5242880&width=8192&height=8192");

        assertThatThrownBy(() -> mediaAdapter.validateHomepageBackground(""))
            .hasMessageContaining("主页背景图不能为空");
        assertThatThrownBy(() -> mediaAdapter.validateHomepageBackground("https://cdn.example.com/bg.bmp"))
            .hasMessageContaining("主页背景图仅支持 JPG、PNG、WebP");
        assertThatThrownBy(() -> mediaAdapter.validateHomepageBackground("https://cdn.example.com/bg.png?size=5242881"))
            .hasMessageContaining("主页背景图大小不能超过5MB");
        assertThatThrownBy(() -> mediaAdapter.validateHomepageBackground("https://cdn.example.com/bg.png?height=8193"))
            .hasMessageContaining("主页背景图高度不合法");
    }
}
