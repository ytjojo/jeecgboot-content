package org.jeecg.modules.content.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.auth.biz.IContentRiskControlBizService;
import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
import org.jeecg.modules.content.auth.req.ContentConfirmAbnormalLoginReq;
import org.jeecg.modules.content.auth.req.ContentRiskAppealReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 风控与异常登录控制器。
 */
@Tag(name = "风控与异常登录", description = "登录风控、风险事件申诉、异常登录确认等接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/account-security")
public class ContentRiskControlController {

    @Resource
    private IContentRiskControlBizService riskControlBizService;

    @Operation(summary = "申诉风险事件", description = "对误判的风险事件提交申诉，审核通过后解除限制")
    @PostMapping("/anomaly/appeal")
    public Result<String> appeal(@Valid @RequestBody ContentRiskAppealReq req) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.appealRiskEvent(req.getEventId(), userId, req.getNote());
        return Result.OK("申诉成功");
    }

    @Operation(summary = "确认异常登录", description = "收到异常登录通知后，确认是否本人操作，非本人将下线其他设备")
    @PostMapping("/anomaly/confirm")
    public Result<String> confirmLogin(@Valid @RequestBody ContentConfirmAbnormalLoginReq req) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.confirmAbnormalLogin(userId, req.getEventId(), Boolean.TRUE.equals(req.getIsSelf()));
        return Result.OK("确认成功");
    }

    @Operation(summary = "获取待处理通知", description = "查询用户待处理的异常登录通知列表")
    @GetMapping("/anomaly/list")
    public Result<List<ContentRiskEvent>> notifications() {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(riskControlBizService.getPendingNotifications(userId));
    }

    @Operation(summary = "获取账户安全状态", description = "聚合查询当前用户的手机/邮箱/第三方绑定状态")
    @GetMapping("/status")
    public Result<?> getAccountSecurityStatus() {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(riskControlBizService.getAccountSecurityStatus(userId));
    }

    @Operation(summary = "信任设备", description = "标记指定设备为可信设备")
    @PostMapping("/devices/trust")
    public Result<?> trustDevice(@RequestBody Map<String, String> params) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.trustDevice(userId, params.get("deviceId"));
        return Result.OK("设备已信任");
    }

    @Operation(summary = "取消信任设备", description = "取消指定设备的可信标记")
    @PostMapping("/devices/untrust")
    public Result<?> untrustDevice(@RequestBody Map<String, String> params) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.untrustDevice(userId, params.get("deviceId"));
        return Result.OK("已取消信任");
    }

    @Operation(summary = "修改密码", description = "验证旧密码后修改为新密码")
    @PostMapping("/password/change")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.OK("密码修改成功");
    }

    @Operation(summary = "发送安全操作验证码", description = "根据类型发送短信或邮箱验证码")
    @PostMapping("/send-code")
    public Result<?> sendSecurityCode(@RequestBody Map<String, String> params) {
        riskControlBizService.sendSecurityCode(params.get("type"), params.get("target"), params.get("purpose"));
        return Result.OK("验证码已发送");
    }

    @Operation(summary = "否认异常登录", description = "否认异常登录并可选踢出设备")
    @PostMapping("/anomaly/deny")
    public Result<?> denyAnomaly(@RequestBody Map<String, String> params) {
        riskControlBizService.denyAnomaly(params.get("id"), params.get("revokeDeviceId"));
        return Result.OK("已否认异常登录");
    }
}
