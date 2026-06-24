package org.jeecg.modules.content.user.growth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.modules.content.user.growth.vo.CircleLevelVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelConfigVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "圈子等级信息")
@RestController
@RequestMapping("/api/v1/content/circle/growth/level")
public class CircleLevelController {

    @Resource
    private ICircleLevelService circleLevelService;

    @Resource
    private IContentUserLevelBenefitService levelBenefitService;

    @Resource
    private IContentUserLevelConfigService levelConfigService;

    @Operation(summary = "获取圈子等级信息")
    @GetMapping("/info")
    public Result<CircleLevelVO> getLevelInfo(@RequestParam String circleId) {
        return Result.OK(circleLevelService.getLevelInfo(circleId));
    }

    @Operation(summary = "查询等级权益摘要")
    @GetMapping("/benefit")
    public Result<ContentUserLevelBenefitSummaryVO> levelBenefit(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId) {
        return Result.OK(levelBenefitService.getBenefitSummary(userId));
    }

    @Operation(summary = "查询等级配置")
    @GetMapping("/config")
    public Result<List<ContentUserLevelConfigVO>> levelConfigs() {
        return Result.OK(levelConfigService.listValidEnabledLevels().stream().map(this::toLevelConfigVO).toList());
    }

    private ContentUserLevelConfigVO toLevelConfigVO(ContentUserLevelConfig config) {
        return new ContentUserLevelConfigVO()
            .setLevel(config.getLevel())
            .setLevelName(config.getLevelName())
            .setGrowthThreshold(config.getGrowthThreshold())
            .setBadgeStyleKey(config.getBadgeStyleKey());
    }
}
