package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.service.IContentUserCredentialService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 用户凭证服务实现。
 */
@Slf4j
@Service
public class ContentUserCredentialServiceImpl implements IContentUserCredentialService {

    @Resource
    private ContentUserCredentialMapper credentialMapper;

    @Override
    public ContentUserCredential getById(String id) {
        return credentialMapper.selectById(id);
    }
}
