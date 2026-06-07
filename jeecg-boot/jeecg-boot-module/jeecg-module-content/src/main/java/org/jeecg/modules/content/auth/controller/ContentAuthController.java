package org.jeecg.modules.content.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.auth.biz.ContentAuthBizService;
import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.req.*;
import org.jeecg.modules.content.auth.service.IContentDeviceSessionService;
import org.jeecg.modules.content.auth.vo.DeviceSessionVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 内容社区认证控制器。
 */
@Tag(name = "内容社区认证", description = "用户注册、登录、设备管理、账号绑定等认证相关接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/auth")
public class ContentAuthController {

    @Resource
    private ContentAuthBizService contentAuthBizService;

    @Resource
    private IContentDeviceSessionService contentDeviceSessionService;

    @Operation(summary = "手机号验证码注册")
    @PostMapping("/register/mobile")
    public Result<String> registerByMobile(@Valid @RequestBody ContentAuthMobileRegisterReq req) {
        return Result.OK(contentAuthBizService.registerByMobile(req));
    }

    @Operation(summary = "密码登录")
    @PostMapping("/login/password")
    public Result<AuthLoginResult> loginByPassword(@Valid @RequestBody ContentAuthLoginReq req) {
        return Result.OK(contentAuthBizService.loginByPassword(req));
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/login/sms")
    public Result<AuthLoginResult> loginBySms(@Valid @RequestBody ContentAuthSmsLoginReq req) {
        return Result.OK(contentAuthBizService.loginBySms(req));
    }

    @Operation(summary = "绑定手机号")
    @PostMapping("/bind/phone")
    public Result<String> bindMobile(@Valid @RequestBody ContentAuthBindMobileReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.bindMobile(req);
        return Result.OK("手机号绑定成功");
    }

    @Operation(summary = "绑定邮箱")
    @PostMapping("/bind/email")
    public Result<String> bindEmail(@Valid @RequestBody ContentAuthBindEmailReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.bindEmail(req);
        return Result.OK("邮箱绑定成功");
    }

    @Operation(summary = "获取设备列表", description = "查询当前用户所有活跃设备会话，标记当前设备")
    @GetMapping("/devices")
    public Result<List<DeviceSessionVO>> listDevices(
            @NotBlank(message = "当前tokenJti不能为空") @Parameter(description = "当前token的JTI") @RequestParam("currentTokenJti") String currentTokenJti) {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(contentDeviceSessionService.listDevices(userId, currentTokenJti));
    }

    @Operation(summary = "下线指定设备", description = "下线非当前设备的其他设备会话，不可下线当前设备")
    @PostMapping("/devices/revoke")
    public Result<String> revokeDevice(
            @NotBlank(message = "会话ID不能为空") @Parameter(description = "要下线的会话ID") @RequestParam("sessionId") String sessionId,
            @NotBlank(message = "当前tokenJti不能为空") @Parameter(description = "当前token的JTI") @RequestParam("currentTokenJti") String currentTokenJti) {
        String userId = SecureUtil.currentUser().getId();
        contentDeviceSessionService.revokeOtherDevice(userId, sessionId, currentTokenJti);
        return Result.OK("设备已下线");
    }

    @Operation(summary = "邮箱密码注册", description = "使用邮箱和密码注册，注册后发送确认邮件")
    @PostMapping("/register/email")
    public Result<String> registerByEmail(@Valid @RequestBody ContentAuthEmailRegisterReq req) {
        return Result.OK(contentAuthBizService.registerByEmail(req));
    }

    @Operation(summary = "确认邮箱", description = "通过邮件中的验证链接确认邮箱")
    @GetMapping("/confirm-email")
    public Result<String> confirmEmail(
            @NotBlank(message = "验证token不能为空") @Parameter(description = "邮箱验证token") @RequestParam("token") String token) {
        return Result.OK(contentAuthBizService.confirmEmail(token));
    }

    @Operation(summary = "第三方登录", description = "使用第三方平台(微信等)登录或自动注册")
    @PostMapping("/login/third-party")
    public Result<?> loginByThirdParty(
            @NotBlank(message = "平台不能为空") @Parameter(description = "第三方平台代码") @RequestParam("provider") String provider,
            @NotBlank(message = "开放ID不能为空") @Parameter(description = "第三方开放ID") @RequestParam("openId") String openId,
            @Parameter(description = "第三方联合ID") @RequestParam(value = "unionId", required = false) String unionId,
            @Parameter(description = "第三方昵称") @RequestParam(value = "nickname", required = false) String nickname,
            @Parameter(description = "第三方头像") @RequestParam(value = "avatar", required = false) String avatar,
            @Parameter(description = "原始授权JSON") @RequestParam(value = "rawJson", required = false) String rawJson) {
        return Result.OK(contentAuthBizService.loginByThirdParty(provider, openId, unionId, nickname, avatar, rawJson));
    }

    @Operation(summary = "换绑手机号", description = "验证旧手机和新手机验证码后，将凭证迁移到新手机号")
    @PostMapping("/rebind/phone")
    public Result<String> rebindMobile(@Valid @RequestBody ContentAuthRebindMobileReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.rebindMobile(req);
        return Result.OK("手机号换绑成功");
    }

    @Operation(summary = "换绑邮箱", description = "验证旧邮箱和新邮箱验证码后，将凭证迁移到新邮箱")
    @PostMapping("/rebind/email")
    public Result<String> rebindEmail(@Valid @RequestBody ContentAuthRebindEmailReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.rebindEmail(req);
        return Result.OK("邮箱换绑成功");
    }

    @Operation(summary = "解绑手机号", description = "验证验证码后禁用手机号凭证，不允许解绑最后一种登录方式")
    @PostMapping("/unbind/phone")
    public Result<String> unbindMobile(@Valid @RequestBody ContentAuthUnbindMobileReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.unbindMobile(req);
        return Result.OK("手机号解绑成功");
    }

    @Operation(summary = "解绑邮箱", description = "验证验证码后禁用邮箱凭证，不允许解绑最后一种登录方式")
    @PostMapping("/unbind/email")
    public Result<String> unbindEmail(@Valid @RequestBody ContentAuthUnbindEmailReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.unbindEmail(req);
        return Result.OK("邮箱解绑成功");
    }

    @Operation(summary = "绑定第三方账号", description = "将第三方平台账号绑定到当前用户")
    @PostMapping("/bind/third-party")
    public Result<String> bindThirdParty(@Valid @RequestBody ContentAuthBindThirdPartyReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.bindThirdParty(req);
        return Result.OK("第三方账号绑定成功");
    }

    @Operation(summary = "解绑第三方账号", description = "解除当前用户与第三方平台账号的绑定关系")
    @PostMapping("/unbind/third-party")
    public Result<String> unbindThirdParty(@Valid @RequestBody ContentAuthUnbindThirdPartyReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.unbindThirdParty(req);
        return Result.OK("第三方账号解绑成功");
    }

    @Operation(summary = "手机号重置密码", description = "通过短信验证码重置密码")
    @PostMapping("/reset-password/mobile")
    public Result<String> resetPasswordByMobile(@Valid @RequestBody ContentAuthResetPasswordByMobileReq req) {
        contentAuthBizService.resetPasswordByMobile(req);
        return Result.OK("密码重置成功");
    }

    @Operation(summary = "邮箱重置密码", description = "通过邮箱重置链接token重置密码")
    @PostMapping("/reset-password/email")
    public Result<String> resetPasswordByEmail(@Valid @RequestBody ContentAuthResetPasswordByEmailReq req) {
        contentAuthBizService.resetPasswordByEmail(req);
        return Result.OK("密码重置成功");
    }

    @Operation(summary = "通用密码重置", description = "根据重置类型(手机号/邮箱)通用密码重置接口")
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@Valid @RequestBody ContentAuthPasswordResetReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        contentAuthBizService.resetPassword(req);
        return Result.OK("密码重置成功");
    }

    @Operation(summary = "发送短信验证码", description = "发送手机验证码，支持图形验证码校验和60秒限频")
    @PostMapping("/sms/send")
    public Result<?> sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String countryCode = params.getOrDefault("countryCode", "+86");
        String captchaId = params.get("captchaId");
        String captchaCode = params.get("captchaCode");
        contentAuthBizService.sendSmsCode(phone, countryCode, captchaId, captchaCode);
        return Result.OK("验证码已发送");
    }

    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码，支持图形验证码校验和60秒限频")
    @PostMapping("/email/send")
    public Result<?> sendEmailCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String captchaId = params.get("captchaId");
        String captchaCode = params.get("captchaCode");
        contentAuthBizService.sendEmailCode(email, captchaId, captchaCode);
        return Result.OK("验证码已发送");
    }

    @Operation(summary = "刷新Token", description = "使用refreshToken获取新的accessToken")
    @PostMapping("/token/refresh")
    public Result<?> refreshToken(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refreshToken");
        AuthLoginResult result = contentAuthBizService.refreshToken(refreshToken);
        return Result.OK(result);
    }

    @Operation(summary = "登出", description = "清除当前用户的会话和token")
    @PostMapping("/logout")
    public Result<?> logout() {
        String userId = SecureUtil.currentUser().getId();
        contentAuthBizService.logout(userId);
        return Result.OK("已登出");
    }

    @Operation(summary = "获取验证码图片", description = "获取图形验证码图片的base64编码")
    @PostMapping("/captcha/image")
    public Result<?> getCaptchaImage() {
        Map<String, String> captcha = contentAuthBizService.getCaptchaImage();
        return Result.OK(captcha);
    }

    @Operation(summary = "校验验证码", description = "校验用户输入的图形验证码")
    @PostMapping("/captcha/verify")
    public Result<?> verifyCaptcha(@RequestBody Map<String, String> params) {
        String captchaId = params.get("captchaId");
        String captchaCode = params.get("captchaCode");
        boolean passed = contentAuthBizService.verifyCaptcha(captchaId, captchaCode);
        return passed ? Result.OK("验证通过") : Result.error("验证码错误");
    }

    @Operation(summary = "查询锁定状态", description = "查询账号的登录失败次数和锁定状态")
    @GetMapping("/captcha/lock-status")
    public Result<?> getLockStatus(@RequestParam String account) {
        Map<String, Object> status = contentAuthBizService.getLockStatus(account);
        return Result.OK(status);
    }
}
