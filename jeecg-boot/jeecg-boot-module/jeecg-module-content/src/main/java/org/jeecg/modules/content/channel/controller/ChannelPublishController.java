package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.req.publish.ChannelAddExistingContentReq;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.vo.publish.AvailableChannelVO;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "频道内容发布", description = "频道内容发布相关接口")
@Validated
@RestController
@RequestMapping("/content/channel/publish")
public class ChannelPublishController {

    @Resource
    private ChannelPublishBiz channelPublishBiz;

    @Operation(summary = "发布内容到频道")
    @PostMapping
    public Result<List<ChannelPublishResultVO>> publish(@Valid @RequestBody ChannelPublishReq req) {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(channelPublishBiz.publish(req, userId));
    }

    @Operation(summary = "将已发布内容添加到频道")
    @PostMapping("/add-existing")
    public Result<List<ChannelPublishResultVO>> addExistingContent(@Valid @RequestBody ChannelAddExistingContentReq req) {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(channelPublishBiz.addExistingContent(req, userId));
    }

    @Operation(summary = "获取用户可发布频道列表")
    @GetMapping("/available")
    public Result<List<AvailableChannelVO>> getAvailableChannels() {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(channelPublishBiz.getAvailableChannels(userId));
    }
}
