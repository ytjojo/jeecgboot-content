package org.jeecg.modules.content.user.service.impl;

import org.jeecg.modules.content.user.service.IContentUserContactBindingAdapter;
import org.springframework.stereotype.Service;

/**
 * 默认账号绑定状态适配器，账号域完成前返回不可见降级状态。
 */
@Service
public class ContentUserContactBindingAdapterImpl implements IContentUserContactBindingAdapter {

    @Override
    public BindingState getBindingState(String userId) {
        return new BindingState(false, false);
    }
}
