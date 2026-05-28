package org.jeecg.modules.content.userstatus.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.model.UserStatusTransition;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 用户状态机服务实现。
 * 提供状态转换验证和状态变更执行功能。
 */
@Service
public class UserStatusServiceImpl implements UserStatusService {

    @Override
    public boolean isValidTransition(UserStatusEnum from, UserStatusEnum to) {
        return UserStatusTransition.isValidTransition(from, to);
    }

    @Override
    public boolean isValidAdminForceTransition(UserStatusEnum from, UserStatusEnum to) {
        // 管理员强制转换允许从任意状态到任意状态
        return true;
    }

    @Override
    public void validateStatusChange(UserStatusEnum from, UserStatusEnum to, String reason, boolean isAdmin) {
        if (from == null || to == null) {
            throw new JeecgBootException("状态不能为空");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new JeecgBootException("变更原因不能为空");
        }

        // 管理员强制转换跳过转换规则验证
        if (isAdmin) {
            return;
        }

        // 验证状态转换是否合法
        if (!isValidTransition(from, to)) {
            throw new JeecgBootException(String.format(
                "非法的状态转换：从 %s 到 %s 不允许",
                from.getDisplayName(),
                to.getDisplayName()
            ));
        }
    }

    @Override
    public boolean detectConcurrentConflict(Long currentVersion, Long expectedVersion) {
        if (currentVersion == null || expectedVersion == null) {
            return false;
        }
        return !currentVersion.equals(expectedVersion);
    }

    @Override
    public Set<UserStatusEnum> getAllowedTransitions(UserStatusEnum from) {
        return UserStatusTransition.getAllowedTransitions(from);
    }
}
