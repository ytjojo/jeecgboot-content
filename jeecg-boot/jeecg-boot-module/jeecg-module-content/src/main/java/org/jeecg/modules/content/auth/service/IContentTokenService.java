package org.jeecg.modules.content.auth.service;

/**
 * 内容社区Token服务接口。
 * 用于生成和验证邮箱验证链接token、密码重置token。
 */
public interface IContentTokenService {

    /**
     * 生成邮箱验证链接token。
     * @param userId 用户ID
     * @param email 邮箱
     * @return token字符串
     */
    String generateEmailVerifyToken(String userId, String email);

    /**
     * 生成密码重置token。
     * @param userId 用户ID
     * @return token字符串
     */
    String generatePasswordResetToken(String userId);

    /**
     * 验证并消费token。成功后标记为已使用。
     * @param token token字符串
     * @param expectedType 期望的token类型(EMAIL_VERIFY/PASSWORD_RESET)
     * @return token关联的用户ID，无效或过期返回null
     */
    String validateAndConsumeToken(String token, String expectedType);

    /**
     * 检查token是否已被使用。
     * @param token token字符串
     * @return true表示已使用
     */
    boolean isTokenUsed(String token);
}
