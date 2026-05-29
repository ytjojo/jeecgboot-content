package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.entity.ContentUserPasswordHistory;
import org.jeecg.modules.content.auth.mapper.ContentUserPasswordHistoryMapper;
import org.jeecg.modules.content.auth.service.IContentUserPasswordHistoryService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 用户密码历史服务实现。
 */
@Slf4j
@Service
public class ContentUserPasswordHistoryServiceImpl implements IContentUserPasswordHistoryService {

    @Resource
    private ContentUserPasswordHistoryMapper passwordHistoryMapper;

    @Override
    public ContentUserPasswordHistory getById(String id) {
        return passwordHistoryMapper.selectById(id);
    }
}
