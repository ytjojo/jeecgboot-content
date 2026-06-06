package org.jeecg.modules.content.userstatus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.userstatus.biz.UserStatusBizManageService;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.mapper.UserStatusAuditLogMapper;
import org.jeecg.modules.content.userstatus.model.UserStatusTransition;
import org.jeecg.modules.content.userstatus.req.SendVerifyCodeReq;
import org.jeecg.modules.content.userstatus.req.UserStatusChangeReq;
import org.jeecg.modules.content.userstatus.req.VerifySecurityReq;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.jeecg.modules.content.userstatus.vo.UserStatusVO;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 用户状态管理控制器。
 * 提供状态查询、状态变更、状态历史等接口。
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/content/user-status")
@Tag(name = "用户状态管理", description = "用户状态查询和管理接口")
public class UserStatusController {

    @Resource
    private UserStatusService userStatusService;

    @Resource
    private UserStatusBizManageService bizManageService;

    @Resource
    private UserStatusAuditLogService auditLogService;

    @Resource
    private ContentUserProfileMapper userProfileMapper;

    @Resource
    private UserStatusAuditLogMapper auditLogMapper;

    @Resource
    private IContentVerificationCodeService verificationCodeService;

    @GetMapping("/current")
    @Operation(summary = "查询当前用户状态")
    public Result<UserStatusVO> getCurrentUserStatus(@RequestParam("userId") String userId) {
        return Result.OK(buildUserStatusVO(userId));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "查询指定用户状态（管理员）")
    public Result<UserStatusVO> getUserStatus(@PathVariable String userId) {
        return Result.OK(buildUserStatusVO(userId));
    }

    private UserStatusVO buildUserStatusVO(String userId) {
        ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在: " + userId);
        }
        UserStatusVO vo = new UserStatusVO();
        vo.setUserId(userId);
        String statusName = profile.getStatus();
        vo.setStatus(statusName);
        UserStatusEnum statusEnum = UserStatusEnum.fromNameOrThrow(statusName);
        vo.setStatusDisplayName(statusEnum.getDisplayName());
        return vo;
    }

    @PostMapping("/{userId}/change")
    @Operation(summary = "变更用户状态（管理员）")
    public Result<Void> changeUserStatus(
        @PathVariable String userId,
        @Valid @RequestBody UserStatusChangeReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在: " + userId);
        }
        UserStatusEnum currentStatus = UserStatusEnum.fromNameOrThrow(profile.getStatus());

        bizManageService.changeStatus(
            userId,
            currentStatus,
            req.getToStatus(),
            req.getReason(),
            operatorId,
            "ADMIN",
            null
        );

        return Result.OK();
    }

    @GetMapping("/{userId}/history")
    @Operation(summary = "查询用户状态历史")
    public Result<List<UserStatusAuditLog>> getUserStatusHistory(@PathVariable String userId) {
        List<UserStatusAuditLog> history = auditLogService.queryByUserId(userId);
        return Result.OK(history);
    }

    @PostMapping("/{userId}/release")
    @Operation(summary = "人工解禁")
    public Result<Void> releaseUserStatus(
        @PathVariable String userId,
        @Valid @RequestBody UserStatusChangeReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在: " + userId);
        }
        UserStatusEnum currentStatus = UserStatusEnum.fromNameOrThrow(profile.getStatus());

        bizManageService.changeStatus(
            userId,
            currentStatus,
            UserStatusEnum.NORMAL,
            req.getReason(),
            operatorId,
            "ADMIN",
            null
        );

        return Result.OK();
    }

    // ==================== P0 端点 ====================

    /**
     * 获取可转换状态列表。
     * 根据当前状态返回所有允许转换到的目标状态。
     */
    @GetMapping("/transitions/{currentStatus}")
    @Operation(summary = "获取可转换状态列表")
    public Result<Set<UserStatusEnum>> getTransitions(@PathVariable String currentStatus) {
        UserStatusEnum status = UserStatusEnum.fromNameOrThrow(currentStatus);
        return Result.OK(UserStatusTransition.getAllowedTransitions(status));
    }

    /**
     * 分页查询用户状态列表。
     * 支持按状态筛选，返回用户资料中的状态信息。
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户状态列表")
    public Result<Page<ContentUserProfile>> getStatusList(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        Page<ContentUserProfile> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ContentUserProfile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userId)) {
            wrapper.eq(ContentUserProfile::getUserId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ContentUserProfile::getStatus, status);
        }
        wrapper.orderByDesc(ContentUserProfile::getCreateTime);
        userProfileMapper.selectPage(pageParam, wrapper);
        return Result.OK(pageParam);
    }

    /**
     * 发送安全核验验证码。
     * 生成6位验证码，通过短信发送，存入Redis（5分钟有效期），60秒冷却。
     */
    @PostMapping("/send-verify-code")
    @Operation(summary = "发送安全核验验证码")
    public Result<Void> sendVerifyCode(@Valid @RequestBody SendVerifyCodeReq req) {
        // 检查冷却期
        if (verificationCodeService.isInCooldown(VerificationCodeSceneEnum.SECURITY_VERIFY, req.getPhone())) {
            throw new JeecgBootException("验证码发送过于频繁，请稍后再试");
        }
        verificationCodeService.generateCode(VerificationCodeSceneEnum.SECURITY_VERIFY, req.getPhone());
        return Result.OK();
    }

    /**
     * 安全核验（验证码校验）。
     * 校验验证码通过后，将 FROZEN 状态用户恢复为 NORMAL。
     */
    @PostMapping("/verify-security")
    @Operation(summary = "安全核验")
    public Result<Void> verifySecurity(@Valid @RequestBody VerifySecurityReq req) {
        boolean valid = verificationCodeService.verifyCode(
            VerificationCodeSceneEnum.SECURITY_VERIFY, req.getPhone(), req.getVerifyCode());
        if (!valid) {
            throw new JeecgBootException("验证码错误或已过期");
        }
        // 确定要恢复的用户ID
        String userId = req.getUserId();
        if (!StringUtils.hasText(userId)) {
            userId = SecureUtil.currentUser().getId();
        }
        // 查询用户当前状态
        ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在: " + userId);
        }
        UserStatusEnum currentStatus = UserStatusEnum.fromNameOrThrow(profile.getStatus());
        if (currentStatus != UserStatusEnum.FROZEN) {
            throw new JeecgBootException("当前账号状态不是冻结状态，无需核验");
        }
        // 恢复为正常状态
        bizManageService.changeStatus(
            userId, currentStatus, UserStatusEnum.NORMAL,
            "安全核验通过，自动恢复", "SYSTEM", "SYSTEM", null);
        return Result.OK();
    }

    // ==================== P1 端点 ====================

    /**
     * 分页查询审计日志列表。
     * 支持按用户ID、时间范围、操作人类型筛选。
     */
    @GetMapping("/audit-logs")
    @Operation(summary = "分页查询审计日志列表")
    public Result<Page<UserStatusAuditLog>> getAuditLogList(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "startTime", required = false) Date startTime,
            @RequestParam(value = "endTime", required = false) Date endTime,
            @RequestParam(value = "operatorType", required = false) String operatorType,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        Page<UserStatusAuditLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserStatusAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userId)) {
            wrapper.eq(UserStatusAuditLog::getUserId, userId);
        }
        if (startTime != null) {
            wrapper.ge(UserStatusAuditLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(UserStatusAuditLog::getCreatedAt, endTime);
        }
        if (StringUtils.hasText(operatorType)) {
            wrapper.eq(UserStatusAuditLog::getOperatorType, operatorType);
        }
        wrapper.orderByDesc(UserStatusAuditLog::getCreatedAt);
        auditLogMapper.selectPage(pageParam, wrapper);
        return Result.OK(pageParam);
    }

    /**
     * 查询审计日志详情。
     */
    @GetMapping("/audit-logs/{logId}")
    @Operation(summary = "查询审计日志详情")
    public Result<UserStatusAuditLog> getAuditLogDetail(@PathVariable String logId) {
        UserStatusAuditLog auditLog = auditLogMapper.selectById(logId);
        if (auditLog == null) {
            throw new JeecgBootException("审计日志不存在: " + logId);
        }
        return Result.OK(auditLog);
    }

    /**
     * 批量解禁用户。
     * 将指定用户状态批量恢复为 NORMAL。
     */
    @PostMapping("/batch-release")
    @Operation(summary = "批量解禁用户")
    public Result<Void> batchReleaseUsers(
            @RequestBody List<String> userIds,
            @RequestParam("reason") String reason) {
        String operatorId = SecureUtil.currentUser().getId();
        if (userIds == null || userIds.isEmpty()) {
            throw new JeecgBootException("用户ID列表不能为空");
        }
        List<String> failedUsers = new ArrayList<>();
        for (String userId : userIds) {
            ContentUserProfile profile = userProfileMapper.selectByUserId(userId);
            if (profile == null) {
                failedUsers.add(userId);
                continue;
            }
            UserStatusEnum currentStatus = UserStatusEnum.fromNameOrThrow(profile.getStatus());
            try {
                bizManageService.changeStatus(
                    userId, currentStatus, UserStatusEnum.NORMAL,
                    reason, operatorId, "ADMIN", null);
            } catch (Exception e) {
                log.warn("批量解禁失败 userId={}, error={}", userId, e.getMessage());
                failedUsers.add(userId);
            }
        }
        if (!failedUsers.isEmpty()) {
            log.warn("部分用户解禁失败: {}", failedUsers);
        }
        return Result.OK();
    }

    /**
     * 导出审计日志。
     * 支持按用户ID、时间范围筛选，返回 CSV 格式数据。
     */
    @GetMapping("/audit-logs/export")
    @Operation(summary = "导出审计日志")
    public Result<List<UserStatusAuditLog>> exportAuditLogs(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "startTime", required = false) Date startTime,
            @RequestParam(value = "endTime", required = false) Date endTime,
            @RequestParam(value = "format", defaultValue = "excel") String format) {
        LambdaQueryWrapper<UserStatusAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userId)) {
            wrapper.eq(UserStatusAuditLog::getUserId, userId);
        }
        if (startTime != null) {
            wrapper.ge(UserStatusAuditLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(UserStatusAuditLog::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(UserStatusAuditLog::getCreatedAt);
        List<UserStatusAuditLog> logs = auditLogMapper.selectList(wrapper);
        return Result.OK(logs);
    }

    /**
     * 查询指定用户的审计日志（分页）。
     */
    @GetMapping("/users/{userId}/audit-logs")
    @Operation(summary = "查询指定用户审计日志")
    public Result<Page<UserStatusAuditLog>> getUserAuditLogs(
            @PathVariable String userId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        Page<UserStatusAuditLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserStatusAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStatusAuditLog::getUserId, userId);
        wrapper.orderByDesc(UserStatusAuditLog::getCreatedAt);
        auditLogMapper.selectPage(pageParam, wrapper);
        return Result.OK(pageParam);
    }
}
