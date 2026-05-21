package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserVerificationBadge;
import org.jeecg.modules.content.user.mapper.ContentUserVerificationBadgeMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserVerificationBadgeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 内容社区认证标识服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserVerificationBadgeServiceTest {

    @Mock
    private ContentUserVerificationBadgeMapper verificationBadgeMapper;

    @InjectMocks
    private ContentUserVerificationBadgeServiceImpl badgeService;

    @Test
    void shouldAggregateSupportedActiveBadges() {
        when(verificationBadgeMapper.selectActiveByUserId("u1")).thenReturn(List.of(
            badge("PERSONAL", "个人认证", null),
            badge("ENTERPRISE", "企业认证", null),
            badge("CREATOR", "达人认证", null),
            badge("OFFICIAL", "官方认证", null),
            badge("REAL_NAME", "实名认证", null)
        ));

        assertThat(badgeService.listVisibleBadges("u1"))
            .extracting("badgeType")
            .containsExactly("PERSONAL", "ENTERPRISE", "CREATOR", "OFFICIAL", "REAL_NAME");
    }

    @Test
    void shouldHideInvalidExpiredOrIncompleteBadges() {
        when(verificationBadgeMapper.selectActiveByUserId("u1")).thenReturn(List.of(
            badge(null, "空类型", null),
            badge("", "空类型", null),
            badge("UNKNOWN", "未知类型", null),
            badge("PERSONAL", "", null),
            badge("PERSONAL", "已过期", new Date(System.currentTimeMillis() - 1000L)),
            badge("EMAIL", "邮箱认证", null)
        ));

        assertThat(badgeService.listVisibleBadges("u1"))
            .extracting("badgeType")
            .containsExactly("EMAIL");
    }

    @Test
    void shouldReturnVisibleDetailAndRejectInvisibleDetail() {
        ContentUserVerificationBadge visible = badge("OFFICIAL", "官方", null);
        visible.setId("b1");
        when(verificationBadgeMapper.selectById("b1")).thenReturn(visible);
        when(verificationBadgeMapper.selectById("bad")).thenReturn(badge("UNKNOWN", "未知", null));

        assertThat(badgeService.getBadgeDetail("b1").getBadgeLabel()).isEqualTo("官方");
        assertThatThrownBy(() -> badgeService.getBadgeDetail("bad"))
            .hasMessageContaining("认证标识不存在或不可见");
    }

    private ContentUserVerificationBadge badge(String type, String label, Date expiresAt) {
        return new ContentUserVerificationBadge()
            .setUserId("u1")
            .setBadgeType(type)
            .setBadgeLabel(label)
            .setDescription(label)
            .setStatus("ACTIVE")
            .setVerifiedAt(new Date())
            .setExpiresAt(expiresAt);
    }
}
