package org.jeecg.modules.content.user.service;

/**
 * 第三方 Token 撤销端口。
 * 实现方负责向第三方平台发起 token 撤销请求。
 */
public interface ContentThirdPartyTokenRevocationPort {

    /**
     * 撤销指定授权关联的 access token 和 refresh token。
     *
     * @param authId 授权记录 ID
     * @param tokenHash access token 哈希（可能为 null）
     * @param refreshTokenHash refresh token 哈希（可能为 null）
     * @return 撤销是否成功
     */
    boolean revokeTokens(String authId, String tokenHash, String refreshTokenHash);
}
