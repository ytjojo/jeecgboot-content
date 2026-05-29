package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentUserCredential;

/**
 * 用户凭证服务接口。
 */
public interface IContentUserCredentialService {

    /**
     * 根据ID查询用户凭证。
     */
    ContentUserCredential getById(String id);
}
