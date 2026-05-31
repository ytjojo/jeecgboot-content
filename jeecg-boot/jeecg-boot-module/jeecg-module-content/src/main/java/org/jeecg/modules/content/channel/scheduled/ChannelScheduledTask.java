package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz;
import org.jeecg.modules.content.channel.entity.*;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.TransferStatus;
import org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.service.*;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelScheduledTask {

    @Resource
    private ChannelService channelService;

    @Resource
    private ChannelTransferService channelTransferService;

    @Resource
    private ScheduledPublishDispatchBiz scheduledPublishDispatchBiz;

    @Resource
    private IChannelReviewService channelReviewService;

    @Resource
    private IChannelExportTaskService exportTaskService;

    @Resource
    private IChannelAppealService appealService;

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Resource
    private ChannelLifecycleBiz lifecycleBiz;

    @Resource
    private IContentNotificationService notificationService;

    /**
     * 每小时扫描冷静期到期的频道，批量处理为 Deleted
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processDeleteCoolingExpired() {
        boolean updated = channelService.update(new LambdaUpdateWrapper<Channel>()
            .eq(Channel::getStatus, ChannelStatus.DELETE_COOLING)
            .le(Channel::getDeleteCoolingEndTime, new Date())
            .set(Channel::getStatus, ChannelStatus.DELETED));
        if (updated) {
            log.info("冷静期到期处理完成");
        }
    }

    /**
     * 每小时扫描超时的转让请求（错开5分钟执行）
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void processTransferExpired() {
        boolean updated = channelTransferService.update(new LambdaUpdateWrapper<ChannelTransfer>()
            .eq(ChannelTransfer::getStatus, TransferStatus.PENDING)
            .le(ChannelTransfer::getExpireTime, new Date())
            .set(ChannelTransfer::getStatus, TransferStatus.EXPIRED));
        if (updated) {
            log.info("转让请求超时处理完成");
        }
    }

    /**
     * 每分钟扫描到期的定时发布任务（错开10分钟执行）
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void processScheduledPublish() {
        scheduledPublishDispatchBiz.dispatch();
    }

    /**
     * 每 5 分钟刷新频道统计数据到 channel_stats 汇总表
     * TODO: 实际聚合逻辑需要从各业务表（订阅、内容发布、互动等）汇总数据
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void refreshChannelStats() {
        log.info("开始刷新频道统计数据: {}", LocalDate.now());
        // 统计刷新逻辑将在各业务数据源对接完成后实现
        log.info("频道统计数据刷新完成");
    }

    /**
     * 每 10 分钟扫描超过 24 小时未处理的待审核记录，标记为超时
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void processReviewTimeout() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24);
        List<ChannelReview> pendingReviews = channelReviewService.list(
                new LambdaQueryWrapper<ChannelReview>()
                        .eq(ChannelReview::getStatus, "pending")
                        .eq(ChannelReview::getTimeoutFlag, 0)
                        .lt(ChannelReview::getSubmitTime, deadline));

        for (ChannelReview review : pendingReviews) {
            review.setTimeoutFlag(1);
            review.setUpdatedTime(LocalDateTime.now());
        }
        if (!pendingReviews.isEmpty()) {
            channelReviewService.updateBatchById(pendingReviews);
            log.info("审核超时标记完成，处理数量: {}", pendingReviews.size());
        }
    }

    /**
     * 每小时清理过期的导出文件（expireTime 已过的 completed 任务）
     */
    @Scheduled(cron = "0 30 * * * ?")
    public void cleanupExpiredExports() {
        List<ChannelExportTask> expiredTasks = exportTaskService.list(
                new LambdaQueryWrapper<ChannelExportTask>()
                        .eq(ChannelExportTask::getStatus, "completed")
                        .lt(ChannelExportTask::getExpireTime, LocalDateTime.now()));

        int cleaned = 0;
        for (ChannelExportTask task : expiredTasks) {
            if (task.getFilePath() != null) {
                File file = new File(task.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
            }
            task.setStatus("expired");
            task.setUpdatedTime(LocalDateTime.now());
            cleaned++;
        }
        if (!expiredTasks.isEmpty()) {
            exportTaskService.updateBatchById(expiredTasks);
            log.info("过期导出文件清理完成，处理数量: {}", cleaned);
        }
    }

    /**
     * 每天上午 9 点检查申诉 SLA：首次响应超过 3 个工作日未处理的申诉
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAppealSla() {
        // 3 个工作日 ≈ 5 个自然日（含周末），保守取 3 个自然日
        LocalDateTime deadline = LocalDateTime.now().minusDays(3);
        List<ChannelAppeal> slaViolations = appealService.list(
                new LambdaQueryWrapper<ChannelAppeal>()
                        .eq(ChannelAppeal::getStatus, "pending")
                        .isNull(ChannelAppeal::getFirstResponseTime)
                        .lt(ChannelAppeal::getCreatedTime, deadline));

        if (!slaViolations.isEmpty()) {
            log.warn("申诉 SLA 超时告警：{} 条申诉超过 3 天未首次响应", slaViolations.size());
            // TODO: 发送告警通知给管理员
        }
    }

    // ===== 不活跃治理 (9.1-9.6) =====

    private static final Set<String> INACTIVITY_TERMINAL = new HashSet<>(
            Arrays.asList("Archived", "Closed", "Merged", "Deleted"));

    /**
     * 每天凌晨 2 点扫描不活跃频道（9.1）
     * - 连续 6 个月无内容发布活动的频道，发送不活跃提醒（9.2）
     * - 已提醒超过 1 个月仍无改善的个人频道，自动归档（9.3）
     * - 已提醒超过 1 个月仍无改善的组织频道，通知管理员（9.4）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scanInactiveChannels() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        LocalDateTime sevenMonthsAgo = LocalDateTime.now().minusMonths(7);

        // 查询 6 个月内有发布活动的频道ID
        List<ChannelContentPublish> recentPublishes = publishMapper.selectList(
                new LambdaQueryWrapper<ChannelContentPublish>()
                        .ge(ChannelContentPublish::getCreateTime, sixMonthsAgo)
                        .select(ChannelContentPublish::getChannelId));
        Set<String> activeChannelIds = recentPublishes.stream()
                .map(ChannelContentPublish::getChannelId)
                .collect(Collectors.toSet());

        // 查询所有未归档/关闭/合并/删除的频道
        List<Channel> allChannels = channelService.list(
                new LambdaQueryWrapper<Channel>()
                        .ne(Channel::getStatus, ChannelStatus.DELETED));

        // 筛选不活跃频道（排除已有终端生命周期状态的）
        List<Channel> inactiveChannels = allChannels.stream()
                .filter(ch -> !activeChannelIds.contains(ch.getId()))
                .filter(ch -> {
                    ChannelLifecycleStatus lcStatus = getCurrentLifecycleStatus(ch.getId());
                    return !INACTIVITY_TERMINAL.contains(lcStatus.getCode());
                })
                .collect(Collectors.toList());

        if (inactiveChannels.isEmpty()) {
            return;
        }

        // 分离：已提醒过的 vs 首次发现的
        Set<String> remindedChannelIds = findRemindedChannelIds();
        List<Channel> firstDiscovery = inactiveChannels.stream()
                .filter(ch -> !remindedChannelIds.contains(ch.getId()))
                .collect(Collectors.toList());
        List<Channel> alreadyReminded = inactiveChannels.stream()
                .filter(ch -> remindedChannelIds.contains(ch.getId()))
                .collect(Collectors.toList());

        // 9.2: 首次发现的不活跃频道 → 发送提醒
        for (Channel channel : firstDiscovery) {
            if (channel.getOwnerId() == null) {
                continue;
            }
            String title = "频道不活跃提醒";
            String content = String.format(
                    "您的频道「%s」已超过 6 个月无新内容发布。如再持续 1 个月无活动，%s。",
                    channel.getName(),
                    channel.getChannelType() == ChannelType.PERSONAL
                            ? "频道将被自动归档" : "将通知组织管理员处理");
            notificationService.sendNotification(channel.getOwnerId(), "channel_inactive_remind", title, content);

            // 记录提醒日志 (9.6)
            lifecycleLogService.save(new ChannelLifecycleLog()
                    .setChannelId(channel.getId())
                    .setActionType("inactivity_remind")
                    .setFromStatus(getCurrentLifecycleStatus(channel.getId()).getCode())
                    .setToStatus(getCurrentLifecycleStatus(channel.getId()).getCode())
                    .setOperatorId("system")
                    .setReason("连续6个月无内容发布活动"));

            log.info("不活跃提醒已发送: channel={}, owner={}", channel.getId(), channel.getOwnerId());
        }

        // 9.3 / 9.4: 已提醒超过 1 个月仍无改善
        handleProlongedInactivity(alreadyReminded, sevenMonthsAgo);

        log.info("不活跃频道扫描完成: 总计发现 {} 个不活跃频道, 首次提醒 {} 个, 持续不活跃处理 {} 个",
                inactiveChannels.size(), firstDiscovery.size(), alreadyReminded.size());
    }

    private void handleProlongedInactivity(List<Channel> remindedChannels, LocalDateTime sevenMonthsAgo) {
        List<String> orgChannelNames = new ArrayList<>();

        for (Channel channel : remindedChannels) {
            // 检查是否在提醒之后仍无活动
            if (hasActivitySince(channel.getId(), sevenMonthsAgo)) {
                continue;
            }

            if (channel.getChannelType() == ChannelType.PERSONAL) {
                // 9.3: 个人频道 → 自动归档
                try {
                    lifecycleBiz.archive(channel.getId(), "system", "连续7个月无活动，自动归档");
                    log.info("个人频道自动归档: channel={}", channel.getId());
                } catch (Exception e) {
                    log.error("自动归档失败: channel={}", channel.getId(), e);
                }
            } else if (channel.getChannelType() == ChannelType.ORGANIZATION) {
                // 9.4: 组织频道 → 收集后统一通知管理员
                orgChannelNames.add(channel.getName());
            }
        }

        // 9.4: 组织频道统一通知管理员
        if (!orgChannelNames.isEmpty()) {
            log.warn("组织频道不活跃告警：{} 个组织频道超过 7 个月无活动: {}",
                    orgChannelNames.size(), String.join("、", orgChannelNames));
            // TODO: 通知组织管理员
        }
    }

    /**
     * 查找已收到过不活跃提醒的频道ID集合
     */
    private Set<String> findRemindedChannelIds() {
        List<ChannelLifecycleLog> remindLogs = lifecycleLogService.list(
                new LambdaQueryWrapper<ChannelLifecycleLog>()
                        .eq(ChannelLifecycleLog::getActionType, "inactivity_remind")
                        .select(ChannelLifecycleLog::getChannelId));
        return remindLogs.stream()
                .map(ChannelLifecycleLog::getChannelId)
                .collect(Collectors.toSet());
    }

    /**
     * 检查频道在指定时间之后是否有新的内容发布活动
     */
    private boolean hasActivitySince(String channelId, LocalDateTime since) {
        return publishMapper.selectCount(
                new LambdaQueryWrapper<ChannelContentPublish>()
                        .eq(ChannelContentPublish::getChannelId, channelId)
                        .ge(ChannelContentPublish::getCreateTime, since)) > 0;
    }

    /**
     * 获取频道当前生命周期状态
     */
    private ChannelLifecycleStatus getCurrentLifecycleStatus(String channelId) {
        ChannelLifecycleLog latestLog = lifecycleLogService.getOne(
                new LambdaQueryWrapper<ChannelLifecycleLog>()
                        .eq(ChannelLifecycleLog::getChannelId, channelId)
                        .orderByDesc(ChannelLifecycleLog::getCreatedTime)
                        .last("LIMIT 1"));
        if (latestLog == null) {
            return ChannelLifecycleStatus.ACTIVE;
        }
        return ChannelLifecycleStatus.fromCode(latestLog.getToStatus());
    }
}
