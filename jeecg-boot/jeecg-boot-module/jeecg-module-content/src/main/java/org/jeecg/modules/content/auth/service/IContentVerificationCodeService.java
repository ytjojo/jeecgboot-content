package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;

/**
 * 验证码服务接口。
 */
public interface IContentVerificationCodeService {

    /**
     * 生成并存储验证码到Redis。
     *
     * @param scene  场景
     * @param target 目标（手机号或邮箱）
     * @return 生成的验证码
     */
    String generateCode(VerificationCodeSceneEnum scene, String target);

    /**
     * 校验验证码。成功后删除验证码并清除失败计数。
     * 失败时累加失败计数，超过3次返回false。
     *
     * @param scene  场景
     * @param target 目标
     * @param code   用户提交的验证码
     * @return 是否校验通过
     */
    boolean verifyCode(VerificationCodeSceneEnum scene, String target, String code);

    /**
     * 检查是否处于冷却期（发送验证码后60秒内不可重发）。
     *
     * @param scene  场景
     * @param target 目标
     * @return 是否在冷却期
     */
    boolean isInCooldown(VerificationCodeSceneEnum scene, String target);

    /**
     * 获取当前验证码失败次数。
     *
     * @param scene  场景
     * @param target 目标
     * @return 失败次数
     */
    int getFailCount(VerificationCodeSceneEnum scene, String target);
}
