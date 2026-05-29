package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.entity.ContentCancellationRequest;
import org.jeecg.modules.content.auth.mapper.ContentCancellationRequestMapper;
import org.jeecg.modules.content.auth.service.IContentCancellationRequestService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 注销申请服务实现。
 */
@Slf4j
@Service
public class ContentCancellationRequestServiceImpl implements IContentCancellationRequestService {

    @Resource
    private ContentCancellationRequestMapper cancellationRequestMapper;

    @Override
    public ContentCancellationRequest getById(String id) {
        return cancellationRequestMapper.selectById(id);
    }
}
