package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.dto.ContentUserPointLedgerQueryDTO;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.req.growth.ContentUserBadgeRecycleReq;
import org.jeecg.modules.content.user.req.growth.ContentUserBadgeWearReq;
import org.jeecg.modules.content.user.req.growth.ContentUserExchangeReq;
import org.jeecg.modules.content.user.req.growth.ContentUserFeatureUnlockReq;
import org.jeecg.modules.content.user.req.growth.ContentPointAdjustReq;
import org.jeecg.modules.content.user.req.growth.ContentUserVirtualGiftReq;
import org.jeecg.modules.content.user.service.IContentUserBadgeService;
import org.jeecg.modules.content.user.service.IContentUserGrowthDecayStateService;
import org.jeecg.modules.content.user.service.IContentUserGrowthService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.jeecg.modules.content.user.service.IContentUserPointSpendService;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.jeecg.modules.content.user.vo.ContentUserDistributionWeightVO;
import org.jeecg.modules.content.user.vo.ContentUserExchangeGoodsVO;
import org.jeecg.modules.content.user.vo.ContentUserFeatureUnlockVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelConfigVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerPageVO;
import org.jeecg.modules.content.user.vo.ContentUserPointSpendResultVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 内容社区用户成长体系接口。
 */
@Tag(name = "内容社区用户成长")
@Validated
@RestController
@RequestMapping("/content/user/growth")
public class ContentUserGrowthController {

    @Resource
    private IContentUserGrowthService growthService;

    @Resource
    private IContentUserBadgeService badgeService;

    @Resource
    private IContentUserPointSpendService pointSpendService;

    @Resource
    private IContentUserLevelBenefitService levelBenefitService;

    @Resource
    private IContentUserLevelConfigService levelConfigService;

    @Resource
    private IContentUserGrowthDecayStateService growthDecayStateService;

    /**
     * 记录用户行为触发的积分和成长值变更。
     */
    @Operation(summary = "记录积分与成长行为")
    @PostMapping("/record")
    public Result<String> record(@Valid @RequestBody ContentPointAdjustReq req) {
        growthService.recordBehavior(
            req.getUserId(),
            req.getSourceType(),
            req.getPointDelta() == null ? 0 : req.getPointDelta(),
            req.getGrowthDelta() == null ? 0 : req.getGrowthDelta()
        );
        return Result.OK("记录成功");
    }

    /**
     * 查询目标用户积分、成长值和等级汇总。
     */
    @Operation(summary = "查询成长汇总")
    @GetMapping("/summary")
    public Result<ContentUserGrowthVO> summary(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId) {
        return Result.OK(growthService.getGrowthSummary(userId));
    }

    /**
     * 查询按分类分组的勋章目录和用户进度。
     */
    @Operation(summary = "查询勋章分类目录")
    @GetMapping("/badge/catalog")
    public Result<Map<String, List<ContentUserBadgeVO>>> badgeCatalog(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId) {
        return Result.OK(badgeService.listBadgeCatalog(userId));
    }

    /**
     * 查询单个勋章详情和获取进度。
     */
    @Operation(summary = "查询勋章详情")
    @GetMapping("/badge/detail")
    public Result<ContentUserBadgeVO> badgeDetail(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId,
        @Parameter(description = "勋章编码", required = true)
        @NotBlank(message = "勋章编码不能为空")
        @Size(max = 64, message = "勋章编码长度不能超过64位")
        @RequestParam("badgeCode") String badgeCode) {
        return Result.OK(badgeService.getBadgeDetail(userId, badgeCode));
    }

    /**
     * 保存用户当前佩戴展示的勋章。
     */
    @Operation(summary = "保存佩戴勋章")
    @PostMapping("/badge/wear")
    public Result<List<ContentUserBadgeVO>> saveWornBadges(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId,
        @Valid @RequestBody ContentUserBadgeWearReq req) {
        return Result.OK(badgeService.saveWornBadges(userId, req.getGrantIds()));
    }

    /**
     * 查询主页、帖子和评论展示面使用的佩戴勋章。
     */
    @Operation(summary = "查询佩戴勋章")
    @GetMapping("/badge/worn")
    public Result<List<ContentUserBadgeVO>> listWornBadges(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId) {
        return Result.OK(badgeService.listWornBadges(userId));
    }

    /**
     * 管理员回收用户违规勋章。
     */
    @Operation(summary = "回收用户勋章")
    @PostMapping("/badge/recycle")
    public Result<String> recycleBadge(@Valid @RequestBody ContentUserBadgeRecycleReq req) {
        badgeService.recycleBadge(req.getGrantId(), req.getOperatorUserId(), req.getReason());
        return Result.OK("回收成功");
    }

    /**
     * 查询可兑换商品列表。
     */
    @Operation(summary = "查询积分兑换商品")
    @GetMapping("/point/exchange/goods")
    public Result<List<ContentUserExchangeGoodsVO>> listExchangeGoods(
        @Parameter(description = "商品类型")
        @Size(max = 32, message = "商品类型长度不能超过32位")
        @RequestParam(value = "goodsType", required = false) String goodsType) {
        return Result.OK(pointSpendService.listExchangeGoods(goodsType));
    }

    /**
     * 使用积分兑换商品。
     */
    @Operation(summary = "积分兑换商品")
    @PostMapping("/point/exchange")
    public Result<ContentUserPointSpendResultVO> exchangeGoods(@Valid @RequestBody ContentUserExchangeReq req) {
        return Result.OK(pointSpendService.exchangeGoods(req.getUserId(), req.getGoodsId(), req.getQuantity()));
    }

    /**
     * 使用积分解锁功能。
     */
    @Operation(summary = "积分解锁功能")
    @PostMapping("/point/feature/unlock")
    public Result<ContentUserPointSpendResultVO> unlockFeature(@Valid @RequestBody ContentUserFeatureUnlockReq req) {
        return Result.OK(pointSpendService.unlockFeature(req.getUserId(), req.getGoodsId()));
    }

    /**
     * 查询用户功能解锁状态。
     */
    @Operation(summary = "查询功能解锁状态")
    @GetMapping("/point/feature/unlock")
    public Result<ContentUserFeatureUnlockVO> getFeatureUnlock(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId,
        @Parameter(description = "功能编码", required = true)
        @NotBlank(message = "功能编码不能为空")
        @Size(max = 64, message = "功能编码长度不能超过64位")
        @RequestParam("featureCode") String featureCode) {
        return Result.OK(pointSpendService.getFeatureUnlock(userId, featureCode));
    }

    /**
     * 赠送虚拟礼物。
     */
    @Operation(summary = "赠送虚拟礼物")
    @PostMapping("/point/gift/send")
    public Result<ContentUserPointSpendResultVO> sendVirtualGift(@Valid @RequestBody ContentUserVirtualGiftReq req) {
        return Result.OK(pointSpendService.sendVirtualGift(req.getSenderUserId(), req.getReceiverUserId(),
            req.getGiftGoodsId(), req.getQuantity(), req.getMessage()));
    }

    /**
     * 分页查询用户积分明细。
     */
    @Operation(summary = "查询积分明细")
    @GetMapping("/point/ledger")
    public Result<ContentUserPointLedgerPageVO> pointLedger(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId,
        @Parameter(description = "明细类型：EARN 获取，SPEND 消耗")
        @Size(max = 16, message = "明细类型长度不能超过16位")
        @RequestParam(value = "type", required = false) String type,
        @Parameter(description = "开始时间")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @RequestParam(value = "startTime", required = false) Date startTime,
        @Parameter(description = "结束时间")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @RequestParam(value = "endTime", required = false) Date endTime,
        @Parameter(description = "当前页，从1开始")
        @RequestParam(value = "current", required = false) Integer current,
        @Parameter(description = "每页条数，最大100")
        @RequestParam(value = "size", required = false) Integer size) {
        ContentUserPointLedgerQueryDTO query = new ContentUserPointLedgerQueryDTO()
            .setUserId(userId)
            .setType(type)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setCurrent(current)
            .setSize(size);
        return Result.OK(pointSpendService.queryPointLedger(query));
    }

    /**
     * 查询当前等级权益摘要。
     */
    @Operation(summary = "查询等级权益摘要")
    @GetMapping("/level/benefit")
    public Result<ContentUserLevelBenefitSummaryVO> levelBenefit(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId) {
        return Result.OK(levelBenefitService.getBenefitSummary(userId));
    }

    /**
     * 查询可用等级阈值配置。
     */
    @Operation(summary = "查询等级配置")
    @GetMapping("/level/config")
    public Result<List<ContentUserLevelConfigVO>> levelConfigs() {
        return Result.OK(levelConfigService.listValidEnabledLevels().stream().map(this::toLevelConfigVO).toList());
    }

    /**
     * 查询等级推荐分发加权信号。
     */
    @Operation(summary = "查询等级推荐分发权重")
    @GetMapping("/level/distribution-weight")
    public Result<ContentUserDistributionWeightVO> distributionWeight(
        @Parameter(description = "用户ID", required = true)
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64, message = "用户ID长度不能超过64位")
        @RequestParam("userId") String userId,
        @Parameter(description = "内容质量分", required = true)
        @DecimalMin(value = "0.00", message = "内容质量分不能小于0")
        @RequestParam("qualityScore") BigDecimal qualityScore) {
        return Result.OK(levelBenefitService.resolveDistributionWeight(userId, qualityScore));
    }

    /**
     * 查询成长值衰减和降级保护规则说明。
     */
    @Operation(summary = "查询成长值衰减规则")
    @GetMapping("/decay/rule")
    public Result<ContentUserGrowthDecayRuleVO> decayRule() {
        return Result.OK(growthDecayStateService.getDecayRule());
    }

    private ContentUserLevelConfigVO toLevelConfigVO(ContentUserLevelConfig config) {
        return new ContentUserLevelConfigVO()
            .setLevel(config.getLevel())
            .setLevelName(config.getLevelName())
            .setGrowthThreshold(config.getGrowthThreshold())
            .setBadgeStyleKey(config.getBadgeStyleKey());
    }
}
