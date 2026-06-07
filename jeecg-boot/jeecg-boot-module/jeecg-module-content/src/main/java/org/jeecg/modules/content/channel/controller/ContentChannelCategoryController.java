package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道分类管理")
@RestController
@RequestMapping("/api/v1/content/channel/category")
public class ContentChannelCategoryController {

    @Resource
    private IContentChannelCategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<ChannelCategoryTreeVO>> getCategoryTree() {
        return Result.OK(categoryService.getCategoryTree());
    }

    @Operation(summary = "创建分类")
    @PostMapping("/create")
    public Result<ContentChannelCategory> createCategory(@Valid @RequestBody ChannelCategoryCreateReq req) {
        return Result.OK(categoryService.createCategory(req));
    }

    @Operation(summary = "更新分类")
    @PostMapping("/update")
    public Result<Void> updateCategory(@Valid @RequestBody ChannelCategoryUpdateReq req) {
        categoryService.updateCategory(req);
        return Result.OK();
    }

    @Operation(summary = "停用分类")
    @PostMapping("/disable")
    public Result<Void> disableCategory(@RequestParam String categoryId) {
        categoryService.disableCategory(categoryId);
        return Result.OK();
    }

    @Operation(summary = "启用分类")
    @PostMapping("/enable")
    public Result<Void> enableCategory(@RequestParam String categoryId) {
        categoryService.enableCategory(categoryId);
        return Result.OK();
    }
}
