package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.account.ContentAccountBindEmailReq;
import org.jeecg.modules.content.user.req.account.ContentAccountBindMobileReq;
import org.jeecg.modules.content.user.req.account.ContentAccountUnbindEmailReq;
import org.jeecg.modules.content.user.req.account.ContentAccountUnbindMobileReq;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
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
     * Registers a community user by email and initializes the user profile.
     */
    @Operation(summary = "邮箱注册并初始化社区资料")
    @PostMapping("/register/email")
    public Result<String> registerByEmail(@Valid @RequestBody ContentEmailRegisterReq req) {
        return Result.OK(contentAccountService.registerByEmail(req));
    }

    /**
     * Binds a mobile number to the target account.
     */
    @Operation(summary = "绑定手机号")
    @PostMapping("/bind/mobile")
    public Result<String> bindMobile(@Valid @RequestBody ContentAccountBindMobileReq req) {
        contentAccountService.bindMobile(req);
        return Result.OK("手机号绑定成功");
    }

    /**
     * Binds an email to the target account.
     */
    @Operation(summary = "绑定邮箱")
    @PostMapping("/bind/email")
    public Result<String> bindEmail(@Valid @RequestBody ContentAccountBindEmailReq req) {
        contentAccountService.bindEmail(req);
        return Result.OK("邮箱绑定成功");
    }

    /**
     * Unbinds the current mobile number from the target account.
     */
    @Operation(summary = "解绑手机号")
    @PostMapping("/unbind/mobile")
    public Result<String> unbindMobile(@Valid @RequestBody ContentAccountUnbindMobileReq req) {
        contentAccountService.unbindMobile(req);
        return Result.OK("手机号解绑成功");
    }

    /**
     * Unbinds the current email from the target account.
     */
    @Operation(summary = "解绑邮箱")
    @PostMapping("/unbind/email")
    public Result<String> unbindEmail(@Valid @RequestBody ContentAccountUnbindEmailReq req) {
        contentAccountService.unbindEmail(req);
        return Result.OK("邮箱解绑成功");
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
