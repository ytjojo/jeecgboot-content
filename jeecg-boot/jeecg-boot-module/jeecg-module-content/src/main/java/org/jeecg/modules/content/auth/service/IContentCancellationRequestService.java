package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentCancellationRequest;

/**
 * 注销申请服务接口。
 */
public interface IContentCancellationRequestService {

    /**
     * 根据ID查询注销申请。
     */
    ContentCancellationRequest getById(String id);
}
