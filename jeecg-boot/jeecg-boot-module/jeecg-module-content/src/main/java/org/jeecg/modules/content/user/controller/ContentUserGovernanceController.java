package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "内容社区用户治理")
@RestController
@RequestMapping("/content/user/governance")
public class ContentUserGovernanceController {

    @Resource
    private IContentUserGovernanceService governanceService;

    @Operation(summary = "变更用户状态")
    @PostMapping("/status/change")
    public Result<String> changeStatus(@RequestBody ContentUserStatusChangeReq req) {
        governanceService.changeStatus(req);
        return Result.OK("状态变更成功");
    }

    @Operation(summary = "检查当前用户行为权限")
    @GetMapping("/permission/check")
    public Result<Boolean> checkPermission(@RequestParam("actionType") String actionType,
                                           @RequestParam(value = "userId", required = false) String userId) {
        return Result.OK(governanceService.canExecuteAction(resolveUserId(userId), actionType));
    }

    @Operation(summary = "查询用户当前状态")
    @GetMapping("/status/current")
    public Result<ContentUserStatusVO> currentStatus(@RequestParam("userId") String userId) {
        return Result.OK(governanceService.getCurrentStatus(userId));
    }

    @Operation(summary = "查询设备会话列表")
    @GetMapping("/device/sessions")
    public Result<List<ContentUserDeviceSession>> listDeviceSessions(@RequestParam("userId") String userId) {
        return Result.OK(governanceService.listDeviceSessions(userId));
    }

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
