package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentNotificationAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.mapper.ContentNotificationAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserNotificationSettingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通知审计日志测试。
 * 验证审计日志实体字段、写入行为以及写入失败不影响主流程。
 */
@ExtendWith(MockitoExtension.class)
class ContentNotificationAuditLogTest {

    @Mock
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Mock
    private ContentNotificationAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentUserNotificationSettingServiceImpl notificationSettingService;

    /**
     * 测试实体字段可以正确设置和读取。
     */
    @Test
    void shouldSetAndGetEntityFields() {
        ContentNotificationAuditLog log = new ContentNotificationAuditLog()
            .setUserId("u1")
            .setNoticeType("LIKE")
            .setChannel("IN_APP")
            .setDecision("SEND")
            .setReason("allowed");
        log.setId("log-1");

        assertThat(log.getId()).isEqualTo("log-1");
        assertThat(log.getUserId()).isEqualTo("u1");
        assertThat(log.getNoticeType()).isEqualTo("LIKE");
        assertThat(log.getChannel()).isEqualTo("IN_APP");
        assertThat(log.getDecision()).isEqualTo("SEND");
        assertThat(log.getReason()).isEqualTo("allowed");
    }

    /**
     * 测试 canSendNotice 被调用时会写入审计日志。
     */
    @Test
    void shouldWriteAuditLogWhenCanSendNoticeCalled() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(10, 0));

        ArgumentCaptor<ContentNotificationAuditLog> captor = ArgumentCaptor.forClass(ContentNotificationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        ContentNotificationAuditLog log = captor.getValue();
        assertThat(log.getUserId()).isEqualTo("u1");
        assertThat(log.getNoticeType()).isEqualTo("LIKE");
        assertThat(log.getChannel()).isEqualTo("IN_APP");
        assertThat(log.getDecision()).isEqualTo("SEND");
        assertThat(log.getReason()).isEqualTo("allowed");
    }

    /**
     * 测试当审计日志写入抛出异常时，canSendNotice 仍然返回正确结果。
     */
    @Test
    void shouldReturnCorrectValueWhenAuditLogInsertFails() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setCommentNoticeEnabled(Boolean.FALSE);
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);
        when(auditLogMapper.insert(any(ContentNotificationAuditLog.class))).thenThrow(new RuntimeException("DB error"));

        // 通知被禁用，应返回 false，即使审计日志写入失败
        boolean allowed = notificationSettingService.canSendNotice("u1", "COMMENT", "IN_APP", LocalTime.of(10, 0));

        assertThat(allowed).isFalse();
    }

    /**
     * 测试当审计日志写入抛出异常时，允许发送的情况也返回正确结果。
     */
    @Test
    void shouldReturnTrueWhenAuditLogInsertFailsForAllowedNotice() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1");
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);
        when(auditLogMapper.insert(any(ContentNotificationAuditLog.class))).thenThrow(new RuntimeException("DB error"));

        boolean allowed = notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(10, 0));

        assertThat(allowed).isTrue();
    }

    /**
     * 测试 SKIP 决策的审计日志内容。
     */
    @Test
    void shouldWriteSkipAuditLogWhenNoticeBlocked() {
        ContentUserNotificationSetting setting = ContentUserNotificationSetting.defaults("u1")
            .setLikeNoticeEnabled(Boolean.FALSE);
        when(notificationSettingMapper.selectByUserId("u1")).thenReturn(setting);

        notificationSettingService.canSendNotice("u1", "LIKE", "IN_APP", LocalTime.of(10, 0));

        ArgumentCaptor<ContentNotificationAuditLog> captor = ArgumentCaptor.forClass(ContentNotificationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        ContentNotificationAuditLog log = captor.getValue();
        assertThat(log.getDecision()).isEqualTo("SKIP");
        assertThat(log.getReason()).isEqualTo("blocked_by_preference");
    }
}
