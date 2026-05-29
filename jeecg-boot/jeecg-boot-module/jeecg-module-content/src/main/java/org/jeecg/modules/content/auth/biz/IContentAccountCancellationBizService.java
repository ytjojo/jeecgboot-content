package org.jeecg.modules.content.auth.biz;

import org.jeecg.modules.content.auth.req.ContentCancelApplyReq;

/**
 * 账号注销业务编排服务接口。
 */
public interface IContentAccountCancellationBizService {

    /**
     * 发起注销申请。
     *
     * @param req 注销申请请求
     */
    void applyCancellation(ContentCancelApplyReq req);

    /**
     * 检查用户是否在冷静期。
     *
     * @param userId 用户ID
     * @return 冷静期状态描述，无申请时返回null
     */
    String checkCooldownStatus(String userId);

    /**
     * 取消注销申请。
     *
     * @param userId 用户ID
     */
    void revokeCancellation(String userId);

    /**
     * 执行最终注销（冷静期结束后）。
     *
     * @param userId 用户ID
     */
    void completeCancellation(String userId);

    /**
     * 校验冷静期天数配置。
     *
     * @param days 冷静期天数
     */
    void validateCooldownDays(Integer days);
}
