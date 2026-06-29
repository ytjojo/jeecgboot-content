package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.req.*;
import org.jeecg.modules.content.channel.service.IChannelAppealService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.util.ChannelSecurityUtil;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/content/channel/lifecycle")
@Tag(name = "频道生命周期", description = "频道生命周期管理接口")
public class ChannelLifecycleController {

    @Resource
    private ChannelLifecycleBiz lifecycleBiz;

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Resource
    private IChannelAppealService appealService;

    @Resource
    private IContentNotificationService notificationService;

    @Resource
    private ChannelMemberService memberService;

    @PostMapping("/freeze")
    @Operation(summary = "冻结频道（平台管理员）")
    public Result<Void> freeze(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.freeze(req.getChannelId(), operatorId, req.getReason());
        return Result.OK();
    }

    @PostMapping("/unfreeze")
    @Operation(summary = "解冻频道（平台管理员）")
    public Result<Void> unfreeze(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.unfreeze(req.getChannelId(), operatorId, req.getReason());
        return Result.OK();
    }

    @PostMapping("/hide")
    @Operation(summary = "强制隐藏频道（平台管理员）")
    public Result<Void> hide(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.hide(req.getChannelId(), operatorId, req.getReason());
        return Result.OK();
    }

    @PostMapping("/close")
    @Operation(summary = "永久关闭频道（平台管理员）")
    public Result<Void> close(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.close(req.getChannelId(), operatorId, req.getReason());
        return Result.OK();
    }

    @PostMapping("/archive")
    @Operation(summary = "归档频道（平台管理员）")
    public Result<Void> archive(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.archive(req.getChannelId(), operatorId, req.getReason());
        return Result.OK();
    }

    @PostMapping("/restrict-recommend")
    @Operation(summary = "限制推荐（平台管理员）")
    public Result<Void> restrictRecommend(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.restrictRecommend(req.getChannelId(), operatorId, req.getReason());
        return Result.OK();
    }

    @PostMapping("/restore-visibility")
    @Operation(summary = "恢复频道活跃状态（平台管理员）")
    public Result<Void> restoreVisibility(@Valid @RequestBody ChannelLifecycleActionReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        lifecycleBiz.restoreActivity(req.getChannelId());
        return Result.OK();
    }

    @GetMapping("/logs")
    @Operation(summary = "查询生命周期变更日志（平台管理员）")
    public Result<IPage<ChannelLifecycleLog>> queryLogs(ChannelLifecycleLogQueryReq req) {
        LambdaQueryWrapper<ChannelLifecycleLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getChannelId())) {
            wrapper.eq(ChannelLifecycleLog::getChannelId, req.getChannelId());
        }
        if (StringUtils.hasText(req.getOperatorId())) {
            wrapper.eq(ChannelLifecycleLog::getOperatorId, req.getOperatorId());
        }
        if (StringUtils.hasText(req.getActionType())) {
            wrapper.eq(ChannelLifecycleLog::getActionType, req.getActionType());
        }
        if (req.getStartTime() != null) {
            wrapper.ge(ChannelLifecycleLog::getCreatedTime, req.getStartTime());
        }
        if (req.getEndTime() != null) {
            wrapper.le(ChannelLifecycleLog::getCreatedTime, req.getEndTime());
        }
        wrapper.orderByDesc(ChannelLifecycleLog::getCreatedTime);
        IPage<ChannelLifecycleLog> page = lifecycleLogService.page(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return Result.OK(page);
    }

    @PostMapping("/appeal/submit")
    @Operation(summary = "提交申诉")
    public Result<ChannelAppeal> submitAppeal(@Valid @RequestBody ChannelAppealSubmitReq req) {
        String userId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelMember member = memberService.getByChannelAndUser(req.getChannelId(), userId);
        if (member == null || member.getRole() == null || member.getRole() > MemberRole.ADMIN.getCode()) {
            return Result.error("只有频道管理员或所有者可以提交申诉");
        }
        ChannelAppeal appeal = appealService.submitAppeal(
                req.getChannelId(), req.getLifecycleLogId(), userId,
                req.getAppealReason(), req.getAttachmentUrls());
        return Result.OK(appeal);
    }

    @PostMapping("/appeal/handle")
    @Operation(summary = "处理申诉（平台管理员）")
    public Result<ChannelAppeal> handleAppeal(@Valid @RequestBody ChannelAppealHandleReq req) {
        String operatorId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelAppeal appeal = appealService.handleAppeal(
                req.getAppealId(), operatorId, req.getAction(), req.getHandleResult());

        if (appeal.getApplicantId() != null) {
            String actionLabel = "approved".equals(req.getAction()) ? "通过" : "拒绝";
            String title = "频道申诉结果通知";
            String content = String.format("您的频道（ID: %s）申诉已%s。%s",
                    appeal.getChannelId(), actionLabel,
                    req.getHandleResult() != null ? "处理说明：" + req.getHandleResult() : "");
            notificationService.sendNotification(appeal.getApplicantId(), "channel_appeal", title, content);
        }
        return Result.OK(appeal);
    }

    @GetMapping("/appeal/detail/{id}")
    @Operation(summary = "查询申诉详情")
    public Result<ChannelAppeal> getAppealDetail(@PathVariable String id) {
        String userId = ChannelSecurityUtil.getCurrentUserIdOrThrow();
        ChannelAppeal appeal = appealService.getById(id);
        if (appeal == null) {
            return Result.error("申诉记录不存在");
        }
        ChannelMember member = memberService.getByChannelAndUser(appeal.getChannelId(), userId);
        if (member == null || member.getRole() == null || member.getRole() > MemberRole.ADMIN.getCode()) {
            return Result.error("无权查看此申诉");
        }
        return Result.OK(appeal);
    }

    @GetMapping("/appeal/list")
    @Operation(summary = "查询申诉列表（平台管理员）")
    public Result<IPage<ChannelAppeal>> queryAppeals(
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<ChannelAppeal> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(channelId)) {
            wrapper.eq(ChannelAppeal::getChannelId, channelId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ChannelAppeal::getStatus, status);
        }
        wrapper.orderByDesc(ChannelAppeal::getCreatedTime);
        IPage<ChannelAppeal> page = appealService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.OK(page);
    }
}
