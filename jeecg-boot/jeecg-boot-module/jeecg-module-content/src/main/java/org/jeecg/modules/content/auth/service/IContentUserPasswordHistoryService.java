package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentUserPasswordHistory;

/**
 * 用户密码历史服务接口。
 */
public interface IContentUserPasswordHistoryService {

    /**
     * 根据ID查询密码历史。
     */
    ContentUserPasswordHistory getById(String id);
}
