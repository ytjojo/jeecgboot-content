package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentNotificationAuditLog;
import org.jeecg.modules.content.user.mapper.ContentNotificationAuditLogMapper;
import org.jeecg.modules.content.user.service.impl.ContentNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 通知发送服务测试。
 * 验证通知审计日志写入、字段设置和唯一ID生成。
 */
@ExtendWith(MockitoExtension.class)
class ContentNotificationServiceTest {

    @Mock
    private ContentNotificationAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentNotificationServiceImpl notificationService;

    @Test
    void shouldSendNotification() {
        // Given
        when(auditLogMapper.insert(any(ContentNotificationAuditLog.class))).thenReturn(1);

        // When
        notificationService.sendNotification("user001", "COMMENT", "新评论", "张三评论了你的文章");

        // Then
        ArgumentCaptor<ContentNotificationAuditLog> captor = ArgumentCaptor.forClass(ContentNotificationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ContentNotificationAuditLog log = captor.getValue();
        assertThat(log.getId()).isNotNull();
        assertThat(log.getUserId()).isEqualTo("user001");
        assertThat(log.getNoticeType()).isEqualTo("COMMENT");
        assertThat(log.getChannel()).isEqualTo("SYSTEM");
        assertThat(log.getDecision()).isEqualTo("SEND");
        assertThat(log.getReason()).isEqualTo("新评论: 张三评论了你的文章");
    }

    @Test
    void shouldHandleNullTitleAndContent() {
        // Given
        when(auditLogMapper.insert(any(ContentNotificationAuditLog.class))).thenReturn(1);

        // When
        notificationService.sendNotification("user002", "FOLLOW", null, null);

        // Then
        ArgumentCaptor<ContentNotificationAuditLog> captor = ArgumentCaptor.forClass(ContentNotificationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ContentNotificationAuditLog log = captor.getValue();
        assertThat(log.getReason()).isEqualTo("null: null");
    }

    @Test
    void shouldGenerateUniqueId() {
        // Given
        when(auditLogMapper.insert(any(ContentNotificationAuditLog.class))).thenReturn(1);

        // When
        notificationService.sendNotification("user001", "LIKE", "点赞", "有人点赞");
        notificationService.sendNotification("user001", "COMMENT", "评论", "有人评论");

        // Then
        ArgumentCaptor<ContentNotificationAuditLog> captor = ArgumentCaptor.forClass(ContentNotificationAuditLog.class);
        verify(auditLogMapper, times(2)).insert(captor.capture());

        String id1 = captor.getAllValues().get(0).getId();
        String id2 = captor.getAllValues().get(1).getId();
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }
}
