package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;
import org.jeecg.modules.content.circle.mapper.CircleGovernanceLogMapper;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CircleGovernanceLogServiceImpl extends ServiceImpl<CircleGovernanceLogMapper, CircleGovernanceLog>
        implements ICircleGovernanceLogService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void logMute(String circleId, String operatorId, String targetUserId, String reason, String duration) {
        saveLog(circleId, operatorId, targetUserId, CircleGovernanceLog.Action.MUTE, reason, duration, null);
    }

    @Override
    public void logUnmute(String circleId, String operatorId, String targetUserId) {
        saveLog(circleId, operatorId, targetUserId, CircleGovernanceLog.Action.UNMUTE, null, null, null);
    }

    @Override
    public void logRemove(String circleId, String operatorId, String targetUserId, String reason) {
        saveLog(circleId, operatorId, targetUserId, CircleGovernanceLog.Action.REMOVE, reason, null, null);
    }

    @Override
    public void logRoleChange(String circleId, String operatorId, String targetUserId, String fromRole, String toRole) {
        String extraJson;
        try {
            extraJson = MAPPER.writeValueAsString(Map.of("from", fromRole, "to", toRole));
        } catch (Exception e) {
            extraJson = "{\"from\":\"" + fromRole + "\",\"to\":\"" + toRole + "\"}";
        }
        saveLog(circleId, operatorId, targetUserId, CircleGovernanceLog.Action.ROLE_CHANGE, null, null, extraJson);
    }

    private void saveLog(String circleId, String operatorId, String targetUserId,
                         CircleGovernanceLog.Action action, String reason, String duration, String extraDataJson) {
        CircleGovernanceLog log = new CircleGovernanceLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setTargetUserId(targetUserId);
        log.setAction(action);
        log.setReason(reason);
        log.setDuration(duration);
        log.setExtraDataJson(extraDataJson);
        save(log);
    }
}
