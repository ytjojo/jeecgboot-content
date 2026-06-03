package org.jeecg.modules.content.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;
import org.jeecg.modules.content.user.mapper.ContentUserFilterRuleMapper;
import org.jeecg.modules.content.user.service.IContentUserFilterRuleService;
import org.jeecg.modules.content.user.vo.ContentFilterRuleItemVO;
import org.jeecg.modules.content.user.vo.ContentFilterRulePageVO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容社区用户屏蔽规则 Controller。
 */
@Tag(name = "内容社区用户屏蔽规则")
@RestController
@RequestMapping("/content/user/filter-rule")
public class ContentUserFilterRuleController {

    @Resource
    private IContentUserFilterRuleService filterRuleService;

    @Resource
    private ContentUserFilterRuleMapper filterRuleMapper;

    /**
     * 添加屏蔽规则。根据 ruleType 分发到不同的 service 方法。
     */
    @Operation(summary = "添加屏蔽规则")
    @PostMapping
    public Result<String> addRule(@RequestParam("userId") String userId,
                                  @RequestParam("ruleType") String ruleType,
                                  @RequestParam("value") String value,
                                  @RequestParam(value = "daysValid", required = false) Integer daysValid) {
        switch (ruleType) {
            case "KEYWORD":
                filterRuleService.saveKeywordRule(userId, value);
                break;
            case "REGEX":
                filterRuleService.saveRegexRule(userId, value);
                break;
            case "TOPIC":
                if (daysValid != null && daysValid > 0) {
                    filterRuleService.saveTopicRuleWithExpiry(userId, value, daysValid);
                } else {
                    filterRuleService.saveTopicRule(userId, value);
                }
                break;
            case "CONTENT_TYPE":
                filterRuleService.saveContentTypeRule(userId, value);
                break;
            default:
                return Result.error("不支持的规则类型: " + ruleType);
        }
        return Result.OK("规则添加成功");
    }

    /**
     * 删除单条屏蔽规则。
     */
    @Operation(summary = "删除屏蔽规则")
    @PostMapping("/delete")
    public Result<String> deleteRule(@RequestParam("userId") String userId,
                                     @RequestParam("ruleId") String ruleId) {
        filterRuleService.cancelRule(userId, ruleId);
        return Result.OK("规则删除成功");
    }

    /**
     * 批量删除屏蔽规则。
     */
    @Operation(summary = "批量删除屏蔽规则")
    @PostMapping("/batch-delete")
    public Result<String> batchDeleteRules(@RequestParam("userId") String userId,
                                           @RequestBody List<String> ruleIds) {
        filterRuleService.batchCancelRules(userId, ruleIds);
        return Result.OK("批量删除成功");
    }

    /**
     * 查询用户屏蔽规则列表。
     */
    @Operation(summary = "查询屏蔽规则列表")
    @GetMapping("/list")
    public Result<ContentFilterRulePageVO> listRules(@RequestParam("userId") String userId,
                                                      @RequestParam(value = "ruleType", required = false) String ruleType,
                                                      @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        Page<ContentUserFilterRule> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ContentUserFilterRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentUserFilterRule::getUserId, userId);
        if (StringUtils.hasText(ruleType)) {
            wrapper.eq(ContentUserFilterRule::getRuleType, ruleType);
        }
        wrapper.orderByDesc(ContentUserFilterRule::getCreateTime);
        filterRuleMapper.selectPage(page, wrapper);

        List<ContentFilterRuleItemVO> records = page.getRecords().stream()
            .map(rule -> new ContentFilterRuleItemVO()
                .setId(rule.getId())
                .setRuleType(rule.getRuleType())
                .setRuleValue(rule.getRuleValue())
                .setMatchScope(rule.getMatchScope())
                .setExpiresAt(rule.getExpiresAt())
                .setStatus(rule.getStatus())
                .setCreateTime(rule.getCreateTime()))
            .collect(Collectors.toList());

        ContentFilterRulePageVO result = new ContentFilterRulePageVO()
            .setRecords(records)
            .setTotal(page.getTotal())
            .setPageNo(pageNo)
            .setPageSize(pageSize);
        return Result.OK(result);
    }
}
