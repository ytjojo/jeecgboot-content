package org.jeecg.modules.content.userstatus.service;

import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;

import java.util.Set;

/**
 * 用户状态机服务接口。
 * 提供状态转换验证和状态变更执行功能。
 */
public interface UserStatusService {

    /**
     * 检查状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 如果转换合法返回 true，否则返回 false
     */
    boolean isValidTransition(UserStatusEnum from, UserStatusEnum to);

    /**
     * 检查管理员强制状态转换是否合法
     * 管理员强制转换允许从任意状态到任意状态
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 始终返回 true
     */
    boolean isValidAdminForceTransition(UserStatusEnum from, UserStatusEnum to);

    /**
     * 验证状态变更请求
     *
     * @param from      当前状态
     * @param to        目标状态
     * @param reason    变更原因
     * @param isAdmin   是否为管理员强制变更
     * @throws org.jeecg.common.exception.JeecgBootException 如果状态变更不合法
     */
    void validateStatusChange(UserStatusEnum from, UserStatusEnum to, String reason, boolean isAdmin);

    /**
     * 检测并发冲突
     *
     * @param currentVersion 当前版本号
     * @param expectedVersion 期望版本号
     * @return 如果存在冲突返回 true，否则返回 false
     */
    boolean detectConcurrentConflict(Long currentVersion, Long expectedVersion);

    /**
     * 获取允许的目标状态集合
     *
     * @param from 当前状态
     * @return 允许的目标状态集合
     */
    Set<UserStatusEnum> getAllowedTransitions(UserStatusEnum from);
}
