package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ReST endpoints for content user support.
 */
@Tag(name = "内容社区用户支持")
@RestController
@RequestMapping("/content/user/support")
public class ContentUserSupportController {

    @Resource
    private IContentUserSupportService supportService;

    /**
     * Creates a user appeal record and returns its identifier.
     */
    @Operation(summary = "创建处罚申诉")
    @PostMapping("/appeal/create")
    public Result<String> createAppeal(@Valid @RequestBody ContentAppealCreateReq req) {
        return Result.OK(supportService.createAppeal(req));
    }

    /**
     * Creates a report submission and returns its identifier.
     */
    @Operation(summary = "创建举报")
    @PostMapping("/report/create")
    public Result<String> createReport(@Valid @RequestBody ContentReportCreateReq req) {
        return Result.OK(supportService.createReport(req));
    }

    /**
     * Queries the current progress of the specified appeal.
     */
    @Operation(summary = "查询申诉进度")
    @GetMapping("/appeal/progress")
    public Result<ContentUserAppealProgressVO> getAppealProgress(@RequestParam("userId") String userId,
                                                                 @RequestParam("appealId") String appealId) {
        return Result.OK(supportService.getAppealProgress(userId, appealId));
    }

    /**
     * Lists all appeals of the specified user.
     */
    @Operation(summary = "查询申诉列表")
    @GetMapping("/appeal/list")
    public Result<List<ContentUserAppealProgressVO>> listAppeals(@RequestParam("userId") String userId) {
        return Result.OK(supportService.listAppeals(userId));
    }

    /**
     * Queries the current progress of the specified report.
     */
    @Operation(summary = "查询举报进度")
    @GetMapping("/report/progress")
    public Result<ContentUserReportProgressVO> getReportProgress(@RequestParam("userId") String userId,
                                                                 @RequestParam("reportId") String reportId) {
        return Result.OK(supportService.getReportProgress(userId, reportId));
    }

    /**
     * Returns help-center metadata for self-service support.
     */
    @Operation(summary = "查询帮助中心")
    @GetMapping("/help-center")
    public Result<ContentHelpCenterVO> getHelpCenter() {
        return Result.OK(supportService.getHelpCenter());
    }

    /**
     * Returns the customer-service entry for the specified user.
     */
    @Operation(summary = "查询客服入口")
    @GetMapping("/customer-service")
    public Result<ContentCustomerServiceVO> getCustomerServiceEntry(@RequestParam("userId") String userId) {
        return Result.OK(supportService.getCustomerServiceEntry(userId));
    }
}
