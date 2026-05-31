package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ChannelLifecycleBiz {

    private static final Set<ChannelLifecycleStatus> FREEZE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> UNFREEZE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> HIDE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> CLOSE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN, ChannelLifecycleStatus.HIDDEN)
    );

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Transactional(rollbackFor = Exception.class)
    public void freeze(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.READONLY_FROZEN, FREEZE_ALLOWED_FROM);
        saveLog(channelId, "freeze", currentStatus.getCode(), ChannelLifecycleStatus.READONLY_FROZEN.getCode(), operatorId, reason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.ACTIVE, UNFREEZE_ALLOWED_FROM);
        saveLog(channelId, "unfreeze", currentStatus.getCode(), ChannelLifecycleStatus.ACTIVE.getCode(), operatorId, reason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void hide(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.HIDDEN, HIDE_ALLOWED_FROM);
        saveLog(channelId, "hide", currentStatus.getCode(), ChannelLifecycleStatus.HIDDEN.getCode(), operatorId, reason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.CLOSED, CLOSE_ALLOWED_FROM);
        saveLog(channelId, "close", currentStatus.getCode(), ChannelLifecycleStatus.CLOSED.getCode(), operatorId, reason, null);
    }

    private ChannelLifecycleStatus getCurrentStatus(String channelId) {
        ChannelLifecycleLog latestLog = lifecycleLogService.getOne(
                new LambdaQueryWrapper<ChannelLifecycleLog>()
                        .eq(ChannelLifecycleLog::getChannelId, channelId)
                        .orderByDesc(ChannelLifecycleLog::getCreatedTime)
                        .last("LIMIT 1")
        );
        if (latestLog == null) {
            return ChannelLifecycleStatus.ACTIVE;
        }
        return ChannelLifecycleStatus.fromCode(latestLog.getToStatus());
    }

    private void validateTransition(ChannelLifecycleStatus from, ChannelLifecycleStatus to, Set<ChannelLifecycleStatus> allowedFrom) {
        if (!allowedFrom.contains(from)) {
            throw new IllegalStateException(
                    String.format("不允许从 %s 状态转换到 %s 状态", from.getCode(), to.getCode()));
        }
    }

    private void saveLog(String channelId, String actionType, String fromStatus, String toStatus,
                         String operatorId, String reason, String targetChannelId) {
        ChannelLifecycleLog log = new ChannelLifecycleLog()
                .setChannelId(channelId)
                .setActionType(actionType)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setOperatorId(operatorId)
                .setReason(reason)
                .setTargetChannelId(targetChannelId);
        lifecycleLogService.save(log);
    }
}
