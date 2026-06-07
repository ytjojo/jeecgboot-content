package org.jeecg.modules.content.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.auth.biz.IContentAccountCancellationBizService;
import org.jeecg.modules.content.auth.req.ContentCancelApplyReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 账号注销控制器。
 */
@Tag(name = "账号注销", description = "账号注销申请、冷静期管理、取消注销等接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/account-cancellation")
public class ContentAccountCancellationController {

    @Resource
    private IContentAccountCancellationBizService cancellationBizService;

    @Operation(summary = "发起注销申请", description = "提交账号注销申请，进入冷静期(7-30天)")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody ContentCancelApplyReq req) {
        req.setUserId(SecureUtil.currentUser().getId());
        cancellationBizService.applyCancellation(req);
        return Result.OK();
    }

    @Operation(summary = "查询注销状态", description = "查询当前用户的注销申请状态和冷静期剩余天数")
    @GetMapping("/status")
    public Result<String> status() {
        String userId = SecureUtil.currentUser().getId();
        String status = cancellationBizService.checkCooldownStatus(userId);
        return Result.OK(status);
    }

    @Operation(summary = "取消注销", description = "在冷静期内取消注销申请，恢复正常状态")
    @PostMapping("/cancel")
    public Result<Void> cancelCancellation() {
        String userId = SecureUtil.currentUser().getId();
        cancellationBizService.revokeCancellation(userId);
        return Result.OK();
    }

    @Operation(summary = "检查注销资格", description = "检查积分余额、待处理订单、风控状态等注销前置条件")
    @GetMapping("/eligibility")
    public Result<?> checkEligibility() {
        String userId = SecureUtil.currentUser().getId();
        Map<String, Object> eligibility = cancellationBizService.checkEligibility(userId);
        return Result.OK(eligibility);
    }
}
