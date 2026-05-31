package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelAnnouncementBiz;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道公告", description = "频道公告管理接口")
@Validated
@RestController
@RequestMapping("/content/channel/announcement")
public class ChannelAnnouncementController {

    @Resource
    private ChannelAnnouncementBiz channelAnnouncementBiz;

    @Operation(summary = "创建公告")
    @PostMapping
    public Result<ChannelAnnouncement> create(@Valid @RequestBody ChannelAnnouncementReq req) {
        return Result.OK(channelAnnouncementBiz.create(req, "current-user-id"));
    }

    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @Valid @RequestBody ChannelAnnouncementReq req) {
        channelAnnouncementBiz.update(id, req);
        return Result.OK();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        channelAnnouncementBiz.delete(id);
        return Result.OK();
    }

    @Operation(summary = "获取频道公告")
    @GetMapping("/channel/{channelId}")
    public Result<ChannelAnnouncement> getByChannelId(@PathVariable String channelId) {
        return Result.OK(channelAnnouncementBiz.getByChannelId(channelId));
    }
}
