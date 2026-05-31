package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
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

    private static final Set<ChannelLifecycleStatus> ARCHIVE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> RESTRICT_RECOMMEND_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE)
    );

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Resource
    private ChannelService channelService;

    @Resource
    private IContentNotificationService notificationService;

    @Transactional(rollbackFor = Exception.class)
    public void freeze(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.READONLY_FROZEN, FREEZE_ALLOWED_FROM);
        saveLog(channelId, "freeze", currentStatus.getCode(), ChannelLifecycleStatus.READONLY_FROZEN.getCode(), operatorId, reason, null);

        // 通知频道所有者
        Channel channel = channelService.getById(channelId);
        if (channel != null && channel.getOwnerId() != null) {
            String title = "频道冻结通知";
            String content = String.format("您的频道「%s」已被冻结。原因：%s。如有异议，可通过申诉入口提交申诉。",
                    channel.getName(), reason != null ? reason : "未说明");
            notificationService.sendNotification(channel.getOwnerId(), "channel_freeze", title, content);
        }
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
        sendViolationNotification(channelId, "永久关闭", reason);
    }

    @Transactional(rollbackFor = Exception.class)
    public void archive(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.ARCHIVED, ARCHIVE_ALLOWED_FROM);
        saveLog(channelId, "archive", currentStatus.getCode(), ChannelLifecycleStatus.ARCHIVED.getCode(), operatorId, reason, null);
        Channel channel = channelService.getById(channelId);
        if (channel != null && channel.getOwnerId() != null) {
            String title = "频道归档通知";
            String content = String.format("您的频道「%s」已被归档。原因：%s。归档后频道不再出现在发现入口。",
                    channel.getName(), reason != null ? reason : "长期不活跃");
            notificationService.sendNotification(channel.getOwnerId(), "channel_archive", title, content);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void restrictRecommend(String channelId, String operatorId, String reason) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        validateTransition(currentStatus, ChannelLifecycleStatus.RESTRICTED, RESTRICT_RECOMMEND_ALLOWED_FROM);
        saveLog(channelId, "restrict_recommend", currentStatus.getCode(), ChannelLifecycleStatus.RESTRICTED.getCode(), operatorId, reason, null);
        sendViolationNotification(channelId, "限制推荐", reason);
    }

    /**
     * 恢复活跃状态：当频道有新内容发布时，清除不活跃提醒状态（9.5）
     * 仅在频道当前处于 ACTIVE 且有过 inactivity_remind 记录时生效
     */
    public void restoreActivity(String channelId) {
        ChannelLifecycleStatus currentStatus = getCurrentStatus(channelId);
        if (currentStatus != ChannelLifecycleStatus.ACTIVE) {
            return;
        }
        // 检查是否有未清除的不活跃提醒
        ChannelLifecycleLog lastRemind = lifecycleLogService.getOne(
                new LambdaQueryWrapper<ChannelLifecycleLog>()
                        .eq(ChannelLifecycleLog::getChannelId, channelId)
                        .eq(ChannelLifecycleLog::getActionType, "inactivity_remind")
                        .orderByDesc(ChannelLifecycleLog::getCreatedTime)
                        .last("LIMIT 1"));
        if (lastRemind == null) {
            return;
        }
        // 检查提醒之后是否已有恢复记录
        ChannelLifecycleLog lastRestore = lifecycleLogService.getOne(
                new LambdaQueryWrapper<ChannelLifecycleLog>()
                        .eq(ChannelLifecycleLog::getChannelId, channelId)
                        .eq(ChannelLifecycleLog::getActionType, "activity_restored")
                        .orderByDesc(ChannelLifecycleLog::getCreatedTime)
                        .last("LIMIT 1"));
        if (lastRestore != null && lastRestore.getCreatedTime().isAfter(lastRemind.getCreatedTime())) {
            return;
        }
        // 记录恢复活跃日志
        saveLog(channelId, "activity_restored", currentStatus.getCode(), currentStatus.getCode(),
                "system", "频道有新内容发布，解除不活跃风险状态", null);
        log.info("频道活跃恢复: channel={}", channelId);
    }

    private void sendViolationNotification(String channelId, String actionLabel, String reason) {
        Channel channel = channelService.getById(channelId);
        if (channel != null && channel.getOwnerId() != null) {
            String title = "频道违规处理通知";
            String content = String.format("您的频道「%s」已被%s。原因：%s。如有异议，可通过申诉入口提交申诉。",
                    channel.getName(), actionLabel, reason != null ? reason : "未说明");
            notificationService.sendNotification(channel.getOwnerId(), "channel_violation", title, content);
        }
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
        ChannelLifecycleLog lifecycleLog = new ChannelLifecycleLog()
                .setChannelId(channelId)
                .setActionType(actionType)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setOperatorId(operatorId)
                .setReason(reason)
                .setTargetChannelId(targetChannelId);
        lifecycleLogService.save(lifecycleLog);
    }
}
