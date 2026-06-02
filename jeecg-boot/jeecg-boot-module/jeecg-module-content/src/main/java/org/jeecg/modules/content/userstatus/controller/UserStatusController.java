package org.jeecg.modules.content.userstatus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.userstatus.biz.UserStatusBizManageService;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.req.UserStatusChangeReq;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.jeecg.modules.content.userstatus.vo.UserStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 用户状态管理控制器。
 * 提供状态查询、状态变更、状态历史等接口。
 */
@Slf4j
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
        @RequestParam("operatorId") String operatorId,
        @RequestBody UserStatusChangeReq req) {
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
        @RequestParam("operatorId") String operatorId,
        @RequestBody UserStatusChangeReq req) {
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
}
