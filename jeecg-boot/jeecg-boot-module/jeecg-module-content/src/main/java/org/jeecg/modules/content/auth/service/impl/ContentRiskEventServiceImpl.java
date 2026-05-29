package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
import org.jeecg.modules.content.auth.mapper.ContentRiskEventMapper;
import org.jeecg.modules.content.auth.service.IContentRiskEventService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 风控事件服务实现。
 */
@Slf4j
@Service
public class ContentRiskEventServiceImpl implements IContentRiskEventService {

    @Resource
    private ContentRiskEventMapper riskEventMapper;

    @Override
    public ContentRiskEvent getById(String id) {
        return riskEventMapper.selectById(id);
    }
}
