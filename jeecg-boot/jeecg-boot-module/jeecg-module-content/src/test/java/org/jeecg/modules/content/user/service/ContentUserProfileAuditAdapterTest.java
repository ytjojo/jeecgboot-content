package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.impl.ContentUserProfileAuditAdapterImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 资料敏感词审核适配器测试。
 */
class ContentUserProfileAuditAdapterTest {

    private final ContentUserProfileAuditAdapterImpl adapter = new ContentUserProfileAuditAdapterImpl();

    @Test
    void shouldReturnNotSuspiciousForCleanProfile() {
        var req = new ContentUserProfileUpdateReq()
            .setNickname("正常用户")
            .setBio("热爱生活")
            .setAvatar("https://cdn.example.com/avatar.jpg");

        var result = adapter.review(req);

        assertThat(result.suspicious()).isFalse();
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldDetectRiskWordInBio() {
        var req = new ContentUserProfileUpdateReq()
            .setNickname("正常用户")
            .setBio("这里是违规内容")
            .setAvatar("https://cdn.example.com/avatar.jpg");

        var result = adapter.review(req);

        assertThat(result.suspicious()).isTrue();
        assertThat(result.reason()).contains("违规");
    }

    @Test
    void shouldDetectRiskWordInNickname() {
        var req = new ContentUserProfileUpdateReq()
            .setNickname("正常用户")
            .setBio("违规内容")
            .setAvatar("https://cdn.example.com/avatar.jpg");

        var result = adapter.review(req);

        assertThat(result.suspicious()).isTrue();
        assertThat(result.reason()).isEqualTo("资料命中风险规则：违规");
    }

    @Test
    void shouldDetectRiskWordInAvatar() {
        var req = new ContentUserProfileUpdateReq()
            .setNickname("正常用户")
            .setBio("正常简介")
            .setAvatar("https://cdn.example.com/spam.jpg");

        var result = adapter.review(req);

        assertThat(result.suspicious()).isTrue();
        assertThat(result.reason()).contains("spam");
    }

    @Test
    void shouldBeCaseInsensitive() {
        var req = new ContentUserProfileUpdateReq()
            .setNickname("NORMAL")
            .setBio("contains SPAM here")
            .setAvatar("https://cdn.example.com/avatar.jpg");

        var result = adapter.review(req);

        assertThat(result.suspicious()).isTrue();
        assertThat(result.reason()).contains("spam");
    }

    @Test
    void shouldHandleNullFields() {
        var req = new ContentUserProfileUpdateReq();

        var result = adapter.review(req);

        assertThat(result.suspicious()).isFalse();
        assertThat(result.reason()).isNull();
    }

    @Test
    void shouldReturnFirstMatch() {
        var req = new ContentUserProfileUpdateReq()
            .setNickname("违规昵称")
            .setBio("也有敏感内容")
            .setAvatar("https://cdn.example.com/avatar.jpg");

        var result = adapter.review(req);

        assertThat(result.suspicious()).isTrue();
        assertThat(result.reason()).isEqualTo("资料命中风险规则：违规");
    }
}
