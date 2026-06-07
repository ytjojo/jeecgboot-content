package org.jeecg.modules.content.auth.biz;

import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.dto.ThirdPartyAuthResult;
import org.jeecg.modules.content.auth.req.*;

import java.util.Map;

/**
 * 认证业务编排服务接口。
 */
public interface ContentAuthBizService {

    /**
     * 手机号验证码注册。
     *
     * @param req 注册请求
     * @return 用户ID
     */
    String registerByMobile(ContentAuthMobileRegisterReq req);

    /**
     * 邮箱密码注册，注册后发送确认邮件。
     *
     * @param req 注册请求
     * @return 用户ID
     */
    String registerByEmail(ContentAuthEmailRegisterReq req);

    /**
     * 确认邮箱验证链接。
     *
     * @param token 验证token
     * @return 用户ID
     */
    String confirmEmail(String token);

    /**
     * 密码登录（手机号或邮箱）。
     *
     * @param req 登录请求
     * @return 登录结果（含token）
     */
    AuthLoginResult loginByPassword(ContentAuthLoginReq req);

    /**
     * 短信验证码登录。
     *
     * @param req 短信登录请求
     * @return 登录结果（含token）
     */
    AuthLoginResult loginBySms(ContentAuthSmsLoginReq req);

    /**
     * 第三方登录或注册。
     *
     * @param provider           第三方平台
     * @param openId             第三方开放ID
     * @param unionId            第三方联合ID(可选)
     * @param thirdPartyNickname 第三方昵称
     * @param thirdPartyAvatar   第三方头像
     * @param rawJson            原始授权数据JSON
     * @return 登录结果
     */
    ThirdPartyAuthResult loginByThirdParty(String provider, String openId, String unionId,
                                            String thirdPartyNickname, String thirdPartyAvatar, String rawJson);

    /**
     * 绑定手机号：验证短信验证码后，将手机号绑定到当前用户。
     *
     * @param req 绑定请求
     */
    void bindMobile(ContentAuthBindMobileReq req);

    /**
     * 绑定邮箱：验证邮箱验证码后，将邮箱绑定到当前用户。
     *
     * @param req 绑定请求
     */
    void bindEmail(ContentAuthBindEmailReq req);

    /**
     * 换绑手机号：验证旧手机和新手机的验证码后，将凭证从旧手机号迁移到新手机号。
     *
     * @param req 换绑请求
     */
    void rebindMobile(ContentAuthRebindMobileReq req);

    /**
     * 换绑邮箱：验证旧邮箱和新邮箱的验证码后，将凭证从旧邮箱迁移到新邮箱。
     *
     * @param req 换绑请求
     */
    void rebindEmail(ContentAuthRebindEmailReq req);

    /**
     * 解绑手机号：验证验证码后，禁用手机号凭证。不允许解绑最后一种登录方式。
     *
     * @param req 解绑请求
     */
    void unbindMobile(ContentAuthUnbindMobileReq req);

    /**
     * 解绑邮箱：验证验证码后，禁用邮箱凭证。不允许解绑最后一种登录方式。
     *
     * @param req 解绑请求
     */
    void unbindEmail(ContentAuthUnbindEmailReq req);

    /**
     * 手机号重置密码：验证短信验证码后重置密码，并检查密码历史。
     *
     * @param req 重置请求
     */
    void resetPasswordByMobile(ContentAuthResetPasswordByMobileReq req);

    /**
     * 邮箱重置密码：验证重置token后重置密码，并检查密码历史。
     *
     * @param req 重置请求
     */
    void resetPasswordByEmail(ContentAuthResetPasswordByEmailReq req);

    /**
     * 绑定第三方账号：校验provider有效性，检查重复绑定，创建授权记录和凭证。
     *
     * @param req 绑定请求
     */
    void bindThirdParty(ContentAuthBindThirdPartyReq req);

    /**
     * 解绑第三方账号：查找绑定记录，检查最后登录方式保护，撤销绑定并禁用凭证。
     *
     * @param req 解绑请求
     */
    void unbindThirdParty(ContentAuthUnbindThirdPartyReq req);

    /**
     * 通用密码重置：根据resetType校验验证码，检查密码强度和历史，更新密码。
     *
     * @param req 重置请求
     */
    void resetPassword(ContentAuthPasswordResetReq req);

    /**
     * 发送手机验证码。
     *
     * @param phone       手机号
     * @param countryCode 国际区号
     * @param captchaId   图形验证码ID(可选)
     * @param captchaCode 图形验证码(可选)
     */
    void sendSmsCode(String phone, String countryCode, String captchaId, String captchaCode);

    /**
     * 发送邮箱验证码。
     *
     * @param email       邮箱
     * @param captchaId   图形验证码ID(可选)
     * @param captchaCode 图形验证码(可选)
     */
    void sendEmailCode(String email, String captchaId, String captchaCode);

    /**
     * 刷新 token。
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录结果
     */
    AuthLoginResult refreshToken(String refreshToken);

    /**
     * 登出，清除会话。
     *
     * @param userId 用户ID
     */
    void logout(String userId);

    /**
     * 获取验证码图片。
     *
     * @return captchaId 和 imageBase64
     */
    Map<String, String> getCaptchaImage();

    /**
     * 校验验证码。
     *
     * @param captchaId   验证码ID
     * @param captchaCode 用户输入的验证码
     * @return 是否通过
     */
    boolean verifyCaptcha(String captchaId, String captchaCode);

    /**
     * 查询锁定状态。
     *
     * @param account 账号（手机号或邮箱）
     * @return 锁定状态信息
     */
    Map<String, Object> getLockStatus(String account);
}
