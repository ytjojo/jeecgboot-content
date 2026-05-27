package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.service.IContentFanAnalyticsService;
import org.jeecg.modules.content.user.vo.ContentFanProfileVO;
import org.jeecg.modules.content.user.vo.ContentFanTrendVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 粉丝数据分析接口。
 */
@Tag(name = "内容社区粉丝分析")
@RestController
@RequestMapping("/content/user/fan")
public class ContentFanAnalyticsController {

    @Resource
    private IContentFanAnalyticsService fanAnalyticsService;

    /**
     * 分页查询粉丝列表。
     */
    @Operation(summary = "分页查询粉丝列表")
    @GetMapping("/list")
    public Result<ContentRelationUserPageVO> listFans(
            @RequestParam("userId") String userId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(fanAnalyticsService.listFans(userId, keyword, pageNo, pageSize));
    }

    /**
     * 查询粉丝趋势数据。
     */
    @Operation(summary = "查询粉丝趋势")
    @GetMapping("/trend")
    public Result<List<ContentFanTrendVO>> getFanTrend(
            @RequestParam("userId") String userId,
            @RequestParam(value = "period", required = false, defaultValue = "day") String period,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        return Result.OK(fanAnalyticsService.getFanTrend(userId, period, startDate, endDate));
    }

    /**
     * 查询粉丝画像分析。
     */
    @Operation(summary = "查询粉丝画像")
    @GetMapping("/profile")
    public Result<ContentFanProfileVO> getFanProfile(@RequestParam("userId") String userId) {
        return Result.OK(fanAnalyticsService.getFanProfile(userId));
    }

    /**
     * 导出粉丝列表为CSV。
     */
    @Operation(summary = "导出粉丝列表")
    @GetMapping("/export")
    public void exportFans(@RequestParam("userId") String userId, HttpServletResponse response) {
        fanAnalyticsService.exportFans(userId, response);
    }
}
