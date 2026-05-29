package org.jeecg.modules.content.auth.service;

/**
 * 登录token生成端口。
 * 抽象token生成以便于测试mock。
 */
@FunctionalInterface
public interface LoginTokenGeneratorPort {

    /**
     * 生成登录访问token。
     *
     * @param username   用户名（userId）
     * @param clientType 客户端类型（PC/APP）
     * @return JWT token字符串
     */
    String generateToken(String username, String clientType);
}
