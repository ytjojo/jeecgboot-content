package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.service.IContentChannelTagService;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道标签管理")
@RestController
@RequestMapping("/content/channel/tag")
public class ContentChannelTagController {

    @Resource
    private IContentChannelTagService tagService;

    @Operation(summary = "获取频道标签列表")
    @GetMapping("/list")
    public Result<List<ChannelTagVO>> listByChannel(@RequestParam String channelId) {
        return Result.OK(tagService.listByChannel(channelId));
    }

    @Operation(summary = "创建标签")
    @PostMapping("/create")
    public Result<ContentChannelTag> createTag(@Valid @RequestBody ChannelTagCreateReq req) {
        return Result.OK(tagService.createTag(req));
    }

    @Operation(summary = "删除标签")
    @PostMapping("/delete")
    public Result<Void> deleteTag(@RequestParam String tagId) {
        tagService.deleteTag(tagId);
        return Result.OK();
    }
}
