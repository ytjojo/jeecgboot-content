package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.content.user.service.IContentAccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ReST endpoints for content account.
 */
@Tag(name = "内容社区账号编排")
@Validated
@RestController
@RequestMapping("/content/user/account")
public class ContentAccountController {

    @Resource
    private  IContentAccountService contentAccountService;

 

    /**
     * Registers a community user by mobile and initializes the user profile.
     */
    @Operation(summary = "手机号注册并初始化社区资料")
    @PostMapping("/register/mobile")
    public Result<String> registerByMobile(@Valid @RequestBody ContentRegisterReq req) {
        return Result.OK(contentAccountService.registerByMobile(req));
    }

    /**
     * Resets the account password for the matched platform user.
     */
    @Operation(summary = "重置密码")
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@Valid @RequestBody ContentPasswordResetReq req) {
        contentAccountService.resetPassword(req);
        return Result.OK("密码重置申请已提交");
    }

    /**
     * Starts the account cancellation flow for the target user.
     */
    @Operation(summary = "发起账号注销")
    @PostMapping("/cancel/apply")
    public Result<String> applyCancel(@RequestParam("userId") String userId,
                                      @RequestParam(value = "operatorUserId", required = false) String operatorUserId,
                                      @RequestParam(value = "reason", required = false) String reason) {
        contentAccountService.initiateCancel(userId, operatorUserId, reason);
        return Result.OK("注销流程已发起");
    }

    /**
     * Completes the irreversible cancellation after the cooling period ends.
     */
    @Operation(summary = "完成账号注销")
    @PostMapping("/cancel/complete")
    public Result<String> completeCancel(@RequestParam("userId") String userId,
                                         @RequestParam(value = "operatorUserId", required = false) String operatorUserId) {
        contentAccountService.completeCancel(userId, operatorUserId);
        return Result.OK("账号已完成注销");
    }

    /**
     * Revokes the pending cancellation request during the cooling period.
     */
    @Operation(summary = "撤销账号注销")
    @PostMapping("/cancel/revoke")
    public Result<String> revokeCancel(@RequestParam("userId") String userId,
                                       @RequestParam(value = "operatorUserId", required = false) String operatorUserId,
                                       @RequestParam(value = "reason", required = false) String reason) {
        contentAccountService.revokeCancel(userId, operatorUserId, reason);
        return Result.OK("注销申请已撤销");
    }
}
