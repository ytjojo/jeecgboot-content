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

/**
 * 风控与异常登录控制器。
 */
@Tag(name = "风控与异常登录", description = "登录风控、风险事件申诉、异常登录确认等接口")
@Validated
@RestController
@RequestMapping("/content/auth/risk")
public class ContentRiskControlController {

    @Resource
    private IContentRiskControlBizService riskControlBizService;

    @Operation(summary = "申诉风险事件", description = "对误判的风险事件提交申诉，审核通过后解除限制")
    @PostMapping("/appeal")
    public Result<String> appeal(@Valid @RequestBody ContentRiskAppealReq req) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.appealRiskEvent(req.getEventId(), userId, req.getNote());
        return Result.OK("申诉成功");
    }

    @Operation(summary = "确认异常登录", description = "收到异常登录通知后，确认是否本人操作，非本人将下线其他设备")
    @PostMapping("/confirm-login")
    public Result<String> confirmLogin(@Valid @RequestBody ContentConfirmAbnormalLoginReq req) {
        String userId = SecureUtil.currentUser().getId();
        riskControlBizService.confirmAbnormalLogin(userId, req.getEventId(), Boolean.TRUE.equals(req.getIsSelf()));
        return Result.OK("确认成功");
    }

    @Operation(summary = "获取待处理通知", description = "查询用户待处理的异常登录通知列表")
    @GetMapping("/notifications")
    public Result<List<ContentRiskEvent>> notifications() {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(riskControlBizService.getPendingNotifications(userId));
    }
}
