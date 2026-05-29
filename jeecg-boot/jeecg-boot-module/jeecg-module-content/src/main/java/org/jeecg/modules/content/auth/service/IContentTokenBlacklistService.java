package org.jeecg.modules.content.auth.service;

/**
 * Token 黑名单服务接口。
 * 用于管理已吊销 token 的黑名单，防止已下线 token 继续访问。
 */
public interface IContentTokenBlacklistService {

    /**
     * 将 token jti 加入黑名单。
     *
     * @param jti        Token JTI
     * @param ttlSeconds 过期时间（秒）
     */
    void addToBlacklist(String jti, long ttlSeconds);

    /**
     * 检查 token jti 是否在黑名单中。
     *
     * @param jti Token JTI
     * @return true=在黑名单中
     */
    boolean isBlacklisted(String jti);

    /**
     * 校验 token 是否可用（不在黑名单中且会话有效）。
     *
     * @param jti Token JTI
     * @return true=可用, false=已失效
     */
    boolean validateToken(String jti);
}
