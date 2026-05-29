package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentUserAccount;

/**
 * 用户认证账号服务接口。
 */
public interface IContentUserAccountService {

    /**
     * 根据ID查询用户账号。
     */
    ContentUserAccount getById(String id);
}
