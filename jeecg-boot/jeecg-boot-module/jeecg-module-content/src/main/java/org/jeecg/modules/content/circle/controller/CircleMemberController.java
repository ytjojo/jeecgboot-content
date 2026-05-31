package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "圈子成员管理", description = "成员角色变更、禁言、移除等接口")
@Validated
@RestController
@RequestMapping("/content/circle/member")
public class CircleMemberController {

    @Resource
    private ICircleMemberBiz circleMemberBiz;

    @Operation(summary = "变更成员角色")
    @PostMapping("/change-role")
    public Result<String> changeRole(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.changeRole(req, operatorId);
        return Result.OK("角色变更成功");
    }

    @Operation(summary = "禁言成员")
    @PostMapping("/mute")
    public Result<String> muteMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.muteMember(req, operatorId);
        return Result.OK("禁言成功");
    }

    @Operation(summary = "解除禁言")
    @PostMapping("/unmute")
    public Result<String> unmuteMember(@RequestParam String circleId, @RequestParam String targetUserId) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.unmuteMember(circleId, targetUserId, operatorId);
        return Result.OK("解除禁言成功");
    }

    @Operation(summary = "移除成员")
    @PostMapping("/remove")
    public Result<String> removeMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.removeMember(req, operatorId);
        return Result.OK("移除成功");
    }
}
