package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道编辑精选")
@RestController
@RequestMapping("/content/channel/editorial-pick")
public class ContentChannelEditorialPickController {

    @Resource
    private IContentChannelEditorialPickService editorialPickService;

    @Operation(summary = "获取有效精选列表")
    @GetMapping("/list")
    public Result<List<ChannelEditorialPickVO>> listActivePicks() {
        return Result.OK(editorialPickService.listActivePicks());
    }

    @Operation(summary = "分页查询精选列表（管理端）")
    @GetMapping("/page")
    public Result<Page<ContentChannelEditorialPick>> pagePicks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<ContentChannelEditorialPick> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ContentChannelEditorialPick::getCreateTime);
        return Result.OK(editorialPickService.page(new Page<>(current, size), wrapper));
    }

    @Operation(summary = "创建编辑精选")
    @PostMapping("/create")
    public Result<ContentChannelEditorialPick> createPick(@Valid @RequestBody ChannelEditorialPickCreateReq req) {
        return Result.OK(editorialPickService.createPick(req));
    }

    @Operation(summary = "更新编辑精选")
    @PostMapping("/update")
    public Result<Void> updatePick(@Valid @RequestBody ChannelEditorialPickUpdateReq req) {
        editorialPickService.updatePick(req);
        return Result.OK();
    }

    @Operation(summary = "移除编辑精选")
    @PostMapping("/remove")
    public Result<Void> removePick(@RequestParam String pickId) {
        editorialPickService.removePick(pickId);
        return Result.OK();
    }
}
