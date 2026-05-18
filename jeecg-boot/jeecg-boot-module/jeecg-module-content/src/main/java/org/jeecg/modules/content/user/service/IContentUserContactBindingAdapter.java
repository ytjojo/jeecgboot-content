package org.jeecg.modules.content.user.service;

/**
 * 手机号和邮箱绑定状态读取适配接口。
 */
public interface IContentUserContactBindingAdapter {

    BindingState getBindingState(String userId);

    /**
     * 绑定状态。
     */
    record BindingState(boolean mobileVerified, boolean emailVerified) {
    }
}
