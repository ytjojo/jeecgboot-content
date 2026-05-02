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
import org.jeecg.modules.content.user.vo.ContentUserAppealPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容社区用户支持接口。
 */
@Tag(name = "内容社区用户支持")
@RestController
@RequestMapping("/content/user/support")
public class ContentUserSupportController {

    @Resource
    private IContentUserSupportService supportService;

    /**
     * 创建用户申诉并返回申诉 ID。
     */
    @Operation(summary = "创建处罚申诉")
    @PostMapping("/appeal/create")
    public Result<String> createAppeal(@Valid @RequestBody ContentAppealCreateReq req) {
        return Result.OK(supportService.createAppeal(req));
    }

    /**
     * 创建用户举报并返回举报 ID。
     */
    @Operation(summary = "创建举报")
    @PostMapping("/report/create")
    public Result<String> createReport(@Valid @RequestBody ContentReportCreateReq req) {
        return Result.OK(supportService.createReport(req));
    }

    /**
     * 查询指定申诉的处理进度。
     */
    @Operation(summary = "查询申诉进度")
    @GetMapping("/appeal/progress")
    public Result<ContentUserAppealProgressVO> getAppealProgress(@RequestParam("userId") String userId,
                                                                 @RequestParam("appealId") String appealId) {
        return Result.OK(supportService.getAppealProgress(userId, appealId));
    }

    /**
     * 查询指定用户的申诉列表。
     */
    @Operation(summary = "查询申诉列表")
    @GetMapping("/appeal/list")
    public Result<ContentUserAppealPageVO> listAppeals(@RequestParam("userId") String userId,
                                                       @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                       @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(supportService.listAppeals(userId, pageNo, pageSize));
    }

    /**
     * 查询指定举报的处理进度。
     */
    @Operation(summary = "查询举报进度")
    @GetMapping("/report/progress")
    public Result<ContentUserReportProgressVO> getReportProgress(@RequestParam("userId") String userId,
                                                                 @RequestParam("reportId") String reportId) {
        return Result.OK(supportService.getReportProgress(userId, reportId));
    }

    /**
     * 按用户上下文返回帮助中心分类与客服推荐信息。
     */
    @Operation(summary = "查询帮助中心")
    @GetMapping("/help-center")
    public Result<ContentHelpCenterVO> getHelpCenter(
            @RequestParam(value = "userId", required = false) String userId) {
        return Result.OK(supportService.getHelpCenter(userId));
    }

    /**
     * 查询指定用户的客服入口。
     */
    @Operation(summary = "查询客服入口")
    @GetMapping("/customer-service")
    public Result<ContentCustomerServiceVO> getCustomerServiceEntry(@RequestParam("userId") String userId) {
        return Result.OK(supportService.getCustomerServiceEntry(userId));
    }
}
