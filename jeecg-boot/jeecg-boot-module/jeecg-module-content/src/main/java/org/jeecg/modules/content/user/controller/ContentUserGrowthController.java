package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.growth.ContentPointAdjustReq;
import org.jeecg.modules.content.user.service.IContentUserGrowthService;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内容社区用户成长")
@RestController
@RequestMapping("/content/user/growth")
public class ContentUserGrowthController {

    @Resource
    private IContentUserGrowthService growthService;

    @Operation(summary = "记录积分与成长行为")
    @PostMapping("/record")
    public Result<String> record(@RequestBody ContentPointAdjustReq req) {
        growthService.recordBehavior(
            req.getUserId(),
            req.getSourceType(),
            req.getPointDelta() == null ? 0 : req.getPointDelta(),
            req.getGrowthDelta() == null ? 0 : req.getGrowthDelta()
        );
        return Result.OK("记录成功");
    }

    @Operation(summary = "查询成长汇总")
    @GetMapping("/summary")
    public Result<ContentUserGrowthVO> summary(@RequestParam("userId") String userId) {
        return Result.OK(growthService.getGrowthSummary(userId));
    }
}
