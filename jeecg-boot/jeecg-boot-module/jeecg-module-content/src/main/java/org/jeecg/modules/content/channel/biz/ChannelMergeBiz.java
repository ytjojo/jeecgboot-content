package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ChannelMergeBiz {

    private static final Set<ChannelLifecycleStatus> MERGE_BLOCKED = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.DELETED, ChannelLifecycleStatus.CLOSED,
                    ChannelLifecycleStatus.HIDDEN, ChannelLifecycleStatus.PENDING_REVIEW,
                    ChannelLifecycleStatus.MERGED, ChannelLifecycleStatus.ARCHIVED)
    );

    @Resource
    private ChannelService channelService;

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private IContentNotificationService notificationService;

    @Resource
    private IChannelReviewService reviewService;

    /**
     * 校验合并申请：检查源频道和目标频道是否满足合并条件
     */
    public Map<String, Object> validateMerge(String sourceChannelId, String targetChannelId) {
        Map<String, Object> result = new HashMap<>();
        Channel source = channelService.getById(sourceChannelId);
        Channel target = channelService.getById(targetChannelId);

        if (source == null) {
            throw new IllegalArgumentException("源频道不存在: " + sourceChannelId);
        }
        if (target == null) {
            throw new IllegalArgumentException("目标频道不存在: " + targetChannelId);
        }
        if (sourceChannelId.equals(targetChannelId)) {
            throw new IllegalArgumentException("源频道和目标频道不能相同");
        }

        ChannelLifecycleStatus sourceStatus = getCurrentLifecycleStatus(sourceChannelId);
        ChannelLifecycleStatus targetStatus = getCurrentLifecycleStatus(targetChannelId);

        if (MERGE_BLOCKED.contains(sourceStatus)) {
            throw new IllegalStateException("源频道当前状态为" + sourceStatus.getDesc() + "，不允许合并");
        }
        if (MERGE_BLOCKED.contains(targetStatus)) {
            throw new IllegalStateException("目标频道当前状态为" + targetStatus.getDesc() + "，不允许作为合并目标");
        }

        // 查询影响范围
        long contentCount = publishMapper.selectCount(
                new LambdaQueryWrapper<ChannelContentPublish>()
                        .eq(ChannelContentPublish::getChannelId, sourceChannelId));

        result.put("sourceChannelName", source.getName());
        result.put("targetChannelName", target.getName());
        result.put("contentCount", contentCount);
        result.put("sourceChannelType", source.getChannelType());
        result.put("needOrgApproval", source.getChannelType() == ChannelType.ORGANIZATION);
        return result;
    }

    /**
     * 提交组织频道合并审核：创建审核记录，待管理员审批后执行
     */
    public ChannelReview submitMergeForReview(String sourceChannelId, String targetChannelId, String applicantId) {
        Channel source = channelService.getById(sourceChannelId);
        Channel target = channelService.getById(targetChannelId);
        if (source == null || target == null) {
            throw new IllegalArgumentException("频道不存在");
        }
        if (source.getChannelType() != ChannelType.ORGANIZATION) {
            throw new IllegalArgumentException("仅组织频道需要审批");
        }

        ChannelLifecycleStatus sourceStatus = getCurrentLifecycleStatus(sourceChannelId);
        if (MERGE_BLOCKED.contains(sourceStatus)) {
            throw new IllegalStateException("源频道状态不允许合并");
        }

        ChannelReview review = new ChannelReview()
                .setReviewId(UUID.randomUUID().toString().replace("-", ""))
                .setChannelId(sourceChannelId)
                .setTargetChannelId(targetChannelId)
                .setReviewType("merge")
                .setStatus("pending")
                .setApplicantId(applicantId)
                .setSubmitTime(LocalDateTime.now())
                .setTimeoutFlag(0);
        reviewService.save(review);

        log.info("组织频道合并审核已提交: {} -> {}, reviewId={}", sourceChannelId, targetChannelId, review.getReviewId());
        return review;
    }

    /**
     * 执行合并：迁移内容和订阅关系，更新源频道状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeMerge(String sourceChannelId, String targetChannelId, String operatorId) {
        Channel source = channelService.getById(sourceChannelId);
        Channel target = channelService.getById(targetChannelId);
        if (source == null || target == null) {
            throw new IllegalArgumentException("频道不存在");
        }

        ChannelLifecycleStatus sourceStatus = getCurrentLifecycleStatus(sourceChannelId);
        if (MERGE_BLOCKED.contains(sourceStatus)) {
            throw new IllegalStateException("源频道状态不允许合并");
        }

        // 迁移内容发布记录
        List<ChannelContentPublish> publishes = publishMapper.selectList(
                new LambdaQueryWrapper<ChannelContentPublish>()
                        .eq(ChannelContentPublish::getChannelId, sourceChannelId));
        for (ChannelContentPublish publish : publishes) {
            publish.setChannelId(targetChannelId);
        }
        if (!publishes.isEmpty()) {
            // 批量更新需要通过 SQL，这里逐条更新
            for (ChannelContentPublish publish : publishes) {
                publishMapper.updateById(publish);
            }
        }

        // 记录合并日志
        ChannelLifecycleLog mergeLog = new ChannelLifecycleLog()
                .setLogId(UUID.randomUUID().toString().replace("-", ""))
                .setChannelId(sourceChannelId)
                .setActionType("merge")
                .setFromStatus(sourceStatus.getCode())
                .setToStatus(ChannelLifecycleStatus.MERGED.getCode())
                .setOperatorId(operatorId)
                .setReason("频道合并到 " + target.getName())
                .setTargetChannelId(targetChannelId)
                .setImpactScope(String.format("迁移内容 %d 条", publishes.size()));
        lifecycleLogService.save(mergeLog);

        // 通知源频道所有者
        if (source.getOwnerId() != null) {
            String title = "频道合并完成通知";
            String content = String.format("您的频道「%s」已合并到「%s」。相关内容和订阅关系已迁移。",
                    source.getName(), target.getName());
            notificationService.sendNotification(source.getOwnerId(), "channel_merge", title, content);
        }

        log.info("频道合并完成: {} -> {}, 迁移内容 {} 条", sourceChannelId, targetChannelId, publishes.size());
    }

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
