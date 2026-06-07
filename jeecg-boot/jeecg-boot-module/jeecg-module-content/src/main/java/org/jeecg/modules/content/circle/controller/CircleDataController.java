package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.service.ICircleDataService;
import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@Tag(name = "圈子数据统计")
@RestController
@RequestMapping("/api/v1/content/circle")
public class CircleDataController {

    @Resource
    private ICircleDataService circleDataService;

    @Operation(summary = "获取圈子数据统计")
    @GetMapping("/{circleId}/data/statistics")
    public Result<CircleDataStatisticsVO> getStatistics(
            @PathVariable String circleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.OK(circleDataService.getStatistics(circleId, startDate, endDate));
    }

    @Operation(summary = "导出圈子数据统计CSV")
    @GetMapping("/{circleId}/data/export")
    public void exportCsv(
            @PathVariable String circleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        String csv = circleDataService.exportCsv(circleId, startDate, endDate);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=circle_data_" + circleId + ".csv");
        PrintWriter writer = response.getWriter();
        writer.write("﻿"); // BOM for Excel
        writer.write(csv);
        writer.flush();
    }
}
