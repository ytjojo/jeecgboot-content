package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.req.support.ContentServiceSessionQueryReq;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentChangelogVO;
import org.jeecg.modules.content.user.vo.ContentHelpSearchResultVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内容社区用户支持接口。
 */
@Tag(name = "内容社区用户支持")
@RestController
@RequestMapping("/api/v1/content/user/support")
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

    /**
     * 查询指定用户的举报列表（分页）。
     */
    @Operation(summary = "查询举报列表")
    @GetMapping("/report/list")
    public Result<ContentUserReportPageVO> listReportsForUser(@RequestParam("userId") String userId,
                                                              @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
                                                              @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        return Result.OK(supportService.listReportsForUser(userId, pageNo, pageSize));
    }

    /**
     * 查询指定举报的详情。
     */
    @Operation(summary = "查询举报详情")
    @GetMapping("/report/{reportId}")
    public Result<ContentUserReportDetailVO> getReportDetailForUser(@RequestParam("userId") String userId,
                                                                    @PathVariable("reportId") String reportId) {
        return Result.OK(supportService.getReportDetailForUser(userId, reportId));
    }

    /**
     * 查询指定申诉的详情。
     */
    @Operation(summary = "查询申诉详情")
    @GetMapping("/appeal/{appealId}")
    public Result<ContentUserAppealDetailVO> getAppealDetail(@RequestParam("userId") String userId,
                                                              @PathVariable("appealId") String appealId) {
        return Result.OK(supportService.getAppealDetail(userId, appealId));
    }

    /**
     * 创建客服会话。
     */
    @Operation(summary = "创建客服会话")
    @PostMapping("/customer-service/session")
    public Result<String> createServiceSession(@RequestParam("userId") String userId,
                                                @RequestParam("sessionType") String sessionType) {
        return Result.OK(supportService.createServiceSession(userId, sessionType));
    }

    /**
     * 查询客服会话列表（分页）。
     */
    @Operation(summary = "查询客服会话列表")
    @GetMapping("/customer-service/sessions")
    public Result<ContentServiceSessionPageVO> listServiceSessions(@Valid ContentServiceSessionQueryReq req) {
        return Result.OK(supportService.listServiceSessions(req));
    }

    /**
     * 搜索帮助文章。
     */
    @Operation(summary = "搜索帮助文章")
    @GetMapping("/help/search")
    public Result<List<ContentHelpSearchResultVO>> searchHelpArticles(@RequestParam("userId") String userId,
                                                                       @RequestParam("keyword") String keyword) {
        return Result.OK(supportService.searchHelpArticles(userId, keyword));
    }

    /**
     * 提交客服会话评分。
     */
    @Operation(summary = "提交客服评分")
    @PostMapping("/customer-service/session/{sessionId}/rating")
    public Result<String> rateService(@RequestParam("userId") String userId,
                                       @PathVariable("sessionId") String sessionId,
                                       @RequestParam("rating") Integer rating,
                                       @RequestParam(value = "comment", required = false) String comment) {
        return Result.OK(supportService.rateService(userId, sessionId, rating, comment));
    }

    /**
     * 获取更新日志。
     */
    @Operation(summary = "获取更新日志")
    @GetMapping("/changelog/list")
    public Result<List<ContentChangelogVO>> getChangelog(@RequestParam("userId") String userId) {
        return Result.OK(supportService.getChangelog(userId));
    }

    /**
     * 撤回举报。
     */
    @Operation(summary = "撤回举报")
    @PostMapping("/report/{reportId}/withdraw")
    public Result<String> withdrawReport(@RequestParam("userId") String userId,
                                          @PathVariable("reportId") String reportId) {
        return Result.OK(supportService.withdrawReport(userId, reportId));
    }

    /**
     * 撤回申诉。
     */
    @Operation(summary = "撤回申诉")
    @PostMapping("/appeal/{appealId}/withdraw")
    public Result<String> withdrawAppeal(@RequestParam("userId") String userId,
                                          @PathVariable("appealId") String appealId) {
        return Result.OK(supportService.withdrawAppeal(userId, appealId));
    }

    /**
     * 获取帮助中心分类列表。
     */
    @Operation(summary = "获取帮助中心分类")
    @GetMapping("/help/categories")
    public Result<List<ContentHelpCenterEntryVO>> getHelpCategories(@RequestParam("userId") String userId) {
        return Result.OK(supportService.getHelpCategories(userId));
    }

    /**
     * 获取帮助文章详情。
     */
    @Operation(summary = "获取帮助文章详情")
    @GetMapping("/help/article/{articleId}")
    public Result<ContentHelpSearchResultVO> getHelpArticleDetail(@RequestParam("userId") String userId,
                                                                    @PathVariable("articleId") String articleId) {
        return Result.OK(supportService.getHelpArticleDetail(userId, articleId));
    }

    /**
     * 提交帮助文章反馈。
     */
    @Operation(summary = "提交文章反馈")
    @PostMapping("/help/article/{articleId}/feedback")
    public Result<String> submitArticleFeedback(@RequestParam("userId") String userId,
                                                 @PathVariable("articleId") String articleId,
                                                 @RequestParam("helpful") Boolean helpful) {
        return Result.OK(supportService.submitArticleFeedback(userId, articleId, helpful));
    }
}
