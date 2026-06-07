package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * ReST endpoints for content user support administration.
 */
@Tag(name = "内容社区用户支持管理")
@RestController
@RequestMapping("/api/v1/content/user/support/admin")
public class ContentUserSupportAdminController {

    @Resource
    private IContentUserSupportService supportService;

    /**
     * Handles an appeal and writes back the final result.
     */
    @Operation(summary = "处理申诉")
    @PostMapping("/appeal/handle")
    public Result<String> handleAppeal(@Valid @RequestBody ContentAppealHandleReq req) {
        return Result.OK(supportService.handleAppeal(req));
    }

    /**
     * Handles a report and writes back the final result.
     */
    @Operation(summary = "处理举报")
    @PostMapping("/report/handle")
    public Result<String> handleReport(@Valid @RequestBody ContentReportHandleReq req) {
        return Result.OK(supportService.handleReport(req));
    }

    /**
     * Lists reports for admin review.
     */
    @Operation(summary = "查询举报列表")
    @GetMapping("/report/list")
    public Result<ContentUserReportAdminPageVO> listReports(@Valid ContentUserReportAdminQueryReq req) {
        return Result.OK(supportService.listReportsForAdmin(req));
    }

    /**
     * Returns report detail for admin review.
     */
    @Operation(summary = "查询举报详情")
    @GetMapping("/report/detail")
    public Result<ContentUserReportAdminDetailVO> getReportDetail(@RequestParam("reportId") String reportId) {
        return Result.OK(supportService.getReportDetailForAdmin(reportId));
    }
}
