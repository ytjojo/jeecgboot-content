package org.jeecg.modules.content.user.service.impl;

import jakarta.annotation.Resource;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentNotificationAuditLog;
import org.jeecg.modules.content.user.mapper.ContentNotificationAuditLogMapper;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;

/**
 * 通知发送服务实现，当前通过审计日志记录通知事件。
 */
@Service
public class ContentNotificationServiceImpl implements IContentNotificationService {

    @Resource
    private ContentNotificationAuditLogMapper auditLogMapper;

    @Override
    public void sendNotification(String userId, String noticeType, String title, String content) {
        ContentNotificationAuditLog log = new ContentNotificationAuditLog();
        log.setId(UUIDGenerator.generate());
        log.setUserId(userId);
        log.setNoticeType(noticeType);
        log.setChannel("SYSTEM");
        log.setDecision("SEND");
        log.setReason(title + ": " + content);
        auditLogMapper.insert(log);
    }
}
