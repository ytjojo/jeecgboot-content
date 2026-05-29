package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentRiskEvent;

/**
 * 风控事件服务接口。
 */
public interface IContentRiskEventService {

    /**
     * 根据ID查询风控事件。
     */
    ContentRiskEvent getById(String id);
}
