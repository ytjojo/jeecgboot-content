package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleReportBizService;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.req.CircleReportReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.jeecg.modules.content.circle.vo.CircleReportVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 圈子内容举报控制器。
 */
@Tag(name = "圈子内容举报", description = "内容举报提交与处理")
@RestController
@RequestMapping("/api/v1/content/circle/report")
public class CircleReportController {

    @Resource
    private CircleReportBizService circleReportBizService;

    @Resource
    private ICircleReportService circleReportService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Operation(summary = "提交举报")
    @PostMapping("/")
    public Result<String> submitReport(@RequestBody @Valid CircleReportReq req,
                                       HttpServletRequest request) {
        String reporterId = JwtUtil.getUserNameByToken(request);
        CircleReport report = new CircleReport();
        BeanUtils.copyProperties(req, report);
        circleReportBizService.submitReport(report, reporterId);
        return Result.OK("举报已提交");
    }

    @Operation(summary = "获取举报列表")
    @GetMapping("/list/{circleId}")
    public Result<List<CircleReportVO>> getReports(
            @PathVariable @Parameter(description = "圈子ID") String circleId,
            @RequestParam(required = false) @Parameter(description = "状态过滤") String status,
            HttpServletRequest request) {
        String userId = JwtUtil.getUserNameByToken(request);
        CircleMember member = circleMemberService.findByCircleAndUser(circleId, userId);
        if (member == null || member.getRole() == CircleMember.Role.MEMBER) {
            throw new JeecgBootException("权限不足，仅创建者和版主可查看举报列表");
        }
        List<CircleReport> reports = circleReportService.getReports(circleId, status);
        List<CircleReportVO> voList = reports.stream().map(r -> {
            CircleReportVO vo = new CircleReportVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.OK(voList);
    }

    @Operation(summary = "处理举报 - 删除被举报内容")
    @PostMapping("/{reportId}/delete-content")
    public Result<String> handleDeleteContent(
            @PathVariable @Parameter(description = "举报ID") String reportId,
            @RequestParam @Parameter(description = "圈子ID") String circleId,
            HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleReportBizService.handleDeleteContent(reportId, operatorId, circleId);
        return Result.OK("已删除举报内容");
    }

    @Operation(summary = "处理举报 - 忽略")
    @PostMapping("/{reportId}/ignore")
    public Result<String> handleIgnore(
            @PathVariable @Parameter(description = "举报ID") String reportId,
            @RequestParam @Parameter(description = "圈子ID") String circleId,
            HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleReportBizService.handleIgnore(reportId, operatorId, circleId);
        return Result.OK("已忽略举报");
    }

    @Operation(summary = "处理举报 - 禁言用户")
    @PostMapping("/{reportId}/mute")
    public Result<String> handleMute(
            @PathVariable @Parameter(description = "举报ID") String reportId,
            @RequestParam @Parameter(description = "圈子ID") String circleId,
            HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleReportBizService.handleMute(reportId, operatorId, circleId);
        return Result.OK("已禁言用户");
    }
}
