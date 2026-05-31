package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelMergeBiz;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.req.ChannelMergeReq;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/jeecg-boot/api/v1/content/channel/merge")
@Tag(name = "频道合并", description = "频道合并管理接口")
public class ChannelMergeController {

    @Resource
    private ChannelMergeBiz mergeBiz;

    @PostMapping("/validate")
    @Operation(summary = "校验合并条件")
    public Result<Map<String, Object>> validateMerge(@Valid @RequestBody ChannelMergeReq req) {
        Map<String, Object> impact = mergeBiz.validateMerge(req.getSourceChannelId(), req.getTargetChannelId());
        return Result.OK(impact);
    }

    @PostMapping("/execute")
    @Operation(summary = "执行合并（组织频道将提交审核）")
    public Result<Object> executeMerge(@Valid @RequestBody ChannelMergeReq req) {
        Map<String, Object> impact = mergeBiz.validateMerge(req.getSourceChannelId(), req.getTargetChannelId());
        if (Boolean.TRUE.equals(impact.get("needOrgApproval"))) {
            ChannelReview review = mergeBiz.submitMergeForReview(
                    req.getSourceChannelId(), req.getTargetChannelId(), getCurrentUserId());
            return Result.OK("组织频道合并已提交审核，审核ID: " + review.getReviewId());
        }
        mergeBiz.executeMerge(req.getSourceChannelId(), req.getTargetChannelId(), getCurrentUserId());
        return Result.OK();
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
