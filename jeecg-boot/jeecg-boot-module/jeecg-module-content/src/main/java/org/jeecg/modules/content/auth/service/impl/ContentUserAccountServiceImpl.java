package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.service.IContentUserAccountService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 用户认证账号服务实现。
 */
@Slf4j
@Service
public class ContentUserAccountServiceImpl implements IContentUserAccountService {

    @Resource
    private ContentUserAccountMapper accountMapper;

    @Override
    public ContentUserAccount getById(String id) {
        return accountMapper.selectById(id);
    }
}
