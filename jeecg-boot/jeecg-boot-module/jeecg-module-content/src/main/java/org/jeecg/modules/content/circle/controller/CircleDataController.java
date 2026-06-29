package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.ICircleDataService;
import org.jeecg.modules.content.circle.util.CircleSecurityUtil;
import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "圈子数据统计")
@RestController
@RequestMapping("/api/v1/content/circle")
public class CircleDataController {

    @Resource
    private ICircleDataService circleDataService;

    @Resource
    private CircleMapper circleMapper;

    @Resource
    private CircleMemberMapper circleMemberMapper;

    @Operation(summary = "获取圈子数据统计")
    @GetMapping("/{circleId}/data/statistics")
    public Result<CircleDataStatisticsVO> getStatistics(
            @PathVariable String circleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Result<CircleDataStatisticsVO> dateCheck = validateDateRange(startDate, endDate);
        if (dateCheck != null) {
            return dateCheck;
        }
        Result<?> authCheck = checkCircleAdmin(circleId);
        if (authCheck != null) {
            return Result.error(510, authCheck.getMessage());
        }
        return Result.OK(circleDataService.getStatistics(circleId, startDate, endDate));
    }

    @Operation(summary = "导出圈子数据统计CSV")
    @GetMapping("/{circleId}/data/export")
    public void exportCsv(
            @PathVariable String circleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        if (!validateDateRangeForExport(startDate, endDate, response)) {
            return;
        }
        if (!checkCircleAdminForExport(circleId, response)) {
            return;
        }
        Circle circle = circleMapper.selectById(circleId);
        String circleName = circle != null ? circle.getName() : circleId;
        String csv = circleDataService.exportCsv(circleId, startDate, endDate);
        String filename = circleName + "_" + startDate + "_" + endDate + ".csv";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        PrintWriter writer = response.getWriter();
        writer.write("﻿");
        writer.write(csv);
        writer.flush();
    }

    private Result<CircleDataStatisticsVO> validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Result.error("日期参数不能为空");
        }
        if (endDate.isBefore(startDate)) {
            return Result.error("结束日期不能早于开始日期");
        }
        if (startDate.plusDays(90).isBefore(endDate)) {
            return Result.error("日期范围不能超过90天");
        }
        return null;
    }

    private boolean validateDateRangeForExport(LocalDate startDate, LocalDate endDate, HttpServletResponse response) throws IOException {
        if (startDate == null || endDate == null) {
            response.sendError(400, "日期参数不能为空");
            return false;
        }
        if (endDate.isBefore(startDate)) {
            response.sendError(400, "结束日期不能早于开始日期");
            return false;
        }
        if (startDate.plusDays(90).isBefore(endDate)) {
            response.sendError(400, "日期范围不能超过90天");
            return false;
        }
        return true;
    }

    private Result<?> checkCircleAdmin(String circleId) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null) {
            return Result.error("圈子不存在");
        }
        if (userId.equals(circle.getCreatorId())) {
            return null;
        }
        CircleMember member = circleMemberMapper.selectOne(
                new LambdaQueryWrapper<CircleMember>()
                        .eq(CircleMember::getCircleId, circleId)
                        .eq(CircleMember::getUserId, userId)
                        .eq(CircleMember::getStatus, CircleMember.Status.ACTIVE)
                        .in(CircleMember::getRole, CircleMember.Role.CREATOR, CircleMember.Role.MODERATOR)
        );
        if (member == null) {
            return Result.error("无权限访问");
        }
        return null;
    }

    private boolean checkCircleAdminForExport(String circleId, HttpServletResponse response) throws IOException {
        String userId = CircleSecurityUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            response.sendError(401, "用户未登录");
            return false;
        }
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null) {
            response.sendError(404, "圈子不存在");
            return false;
        }
        if (userId.equals(circle.getCreatorId())) {
            return true;
        }
        CircleMember member = circleMemberMapper.selectOne(
                new LambdaQueryWrapper<CircleMember>()
                        .eq(CircleMember::getCircleId, circleId)
                        .eq(CircleMember::getUserId, userId)
                        .eq(CircleMember::getStatus, CircleMember.Status.ACTIVE)
                        .in(CircleMember::getRole, CircleMember.Role.CREATOR, CircleMember.Role.MODERATOR)
        );
        if (member == null) {
            response.sendError(403, "无权限访问");
            return false;
        }
        return true;
    }
}
