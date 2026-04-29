package org.jeecg.modules.content.user.service.impl;

import jakarta.annotation.Resource;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for content user support.
 */
@Service
public class ContentUserSupportServiceImpl implements IContentUserSupportService {

    @Resource
    private ContentUserAppealMapper appealMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    /**
     * Creates a user appeal record and returns its identifier.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAppeal(ContentAppealCreateReq req) {
        ContentUserAppeal appeal = ContentUserAppeal.from(req);
        appeal.setId(UUIDGenerator.generate());
        appeal.setProgressNote("已提交，等待处理");
        appealMapper.insert(appeal);
        auditLogMapper.insert(ContentUserAuditLog.appealCreated(appeal));
        return appeal.getId();
    }
}
