package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;

public interface ICircleGovernanceLogService extends IService<CircleGovernanceLog> {

    void logMute(String circleId, String operatorId, String targetUserId, String reason, String duration);

    void logUnmute(String circleId, String operatorId, String targetUserId);

    void logRemove(String circleId, String operatorId, String targetUserId, String reason);

    void logRoleChange(String circleId, String operatorId, String targetUserId, String fromRole, String toRole);
}
