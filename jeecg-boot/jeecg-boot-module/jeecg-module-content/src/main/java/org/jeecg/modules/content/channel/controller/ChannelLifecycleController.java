package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz;
import org.jeecg.modules.content.channel.req.ChannelLifecycleActionReq;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/jeecg-boot/api/v1/content/channel/lifecycle")
@Tag(name = "频道生命周期", description = "频道生命周期管理接口")
public class ChannelLifecycleController {

    @Resource
    private ChannelLifecycleBiz lifecycleBiz;

    @PostMapping("/freeze")
    @Operation(summary = "冻结频道")
    public Result<Void> freeze(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.freeze(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    @PostMapping("/unfreeze")
    @Operation(summary = "解冻频道")
    public Result<Void> unfreeze(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.unfreeze(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    @PostMapping("/hide")
    @Operation(summary = "强制隐藏频道")
    public Result<Void> hide(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.hide(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    @PostMapping("/close")
    @Operation(summary = "永久关闭频道")
    public Result<Void> close(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.close(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
