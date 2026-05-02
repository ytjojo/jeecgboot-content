package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryPageVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ReST endpoints for content user governance.
 */
@Tag(name = "内容社区用户治理")
@RestController
@RequestMapping("/content/user/governance")
public class ContentUserGovernanceController {

    @Resource
    private IContentUserGovernanceService governanceService;

    /**
     * Changes the lifecycle status of the target user and records governance logs.
     */
    @Operation(summary = "变更用户状态")
    @PostMapping("/status/change")
    public Result<String> changeStatus(@Valid @RequestBody ContentUserStatusChangeReq req) {
        governanceService.changeStatus(req);
        return Result.OK("状态变更成功");
    }

    /**
     * Checks whether the user can execute the requested action.
     */
    @Operation(summary = "检查当前用户行为权限")
    @GetMapping("/permission/check")
    public Result<Boolean> checkPermission(@RequestParam("actionType") String actionType,
                                           @RequestParam(value = "userId", required = false) String userId) {
        return Result.OK(governanceService.canExecuteAction(resolveUserId(userId), actionType));
    }

    /**
     * Gets the current lifecycle status snapshot for the target user.
     */
    @Operation(summary = "查询用户当前状态")
    @GetMapping("/status/current")
    public Result<ContentUserStatusVO> currentStatus(@RequestParam("userId") String userId) {
        return Result.OK(governanceService.getCurrentStatus(userId));
    }

    /**
     * Queries paged status history for the target user.
     */
    @Operation(summary = "分页查询用户状态历史")
    @GetMapping("/status/history")
    public Result<ContentUserStatusHistoryPageVO> listStatusHistory(@RequestParam("userId") String userId,
                                                                    @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(governanceService.listStatusHistory(userId, pageNo, pageSize));
    }

    /**
     * Lists recent device sessions for the target user.
     */
    @Operation(summary = "查询设备会话列表")
    @GetMapping("/device/sessions")
    public Result<List<ContentUserDeviceSession>> listDeviceSessions(@RequestParam("userId") String userId) {
        return Result.OK(governanceService.listDeviceSessions(userId));
    }

    /**
     * Marks the specified device session as offline.
     */
    @Operation(summary = "下线指定设备会话")
    @PostMapping("/device/offline")
    public Result<String> offlineDeviceSession(@RequestParam("userId") String userId,
                                               @RequestParam("sessionId") String sessionId) {
        governanceService.offlineDeviceSession(userId, sessionId);
        return Result.OK("下线成功");
    }

    private String resolveUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        try {
            return SecureUtil.currentUser().getId();
        } catch (Exception ignored) {
            return null;
        }
    }
}
