package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.entity.ContentCustomerServiceSession;
import org.jeecg.modules.content.user.mapper.ContentCustomerServiceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserReportMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentServiceSessionQueryReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.jeecg.modules.content.user.service.IContentUserGrowthPenaltyRecordService;
import org.jeecg.modules.content.user.service.IContentUserGrowthPenaltyRecoveryService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitRecoveryService;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentChangelogVO;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentHelpSearchResultVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionPageVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportListItemVO;
import org.jeecg.modules.content.user.vo.ContentUserReportPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service implementation for content user support.
 */
@Service
public class ContentUserSupportServiceImpl implements IContentUserSupportService {

    private static final List<String> PUNISHING_REPORT_RESULTS = List.of("CONFIRMED");
    private static final String APPEAL_TARGET_TYPE_GOVERNANCE_STATUS = "GOVERNANCE_STATUS";
    private static final String ROUTE_SMART_FIRST = "SMART_FIRST";
    private static final String ROUTE_MANUAL_PRIORITY = "MANUAL_PRIORITY";
    private static final String ROUTE_APPEAL_PRIORITY = "APPEAL_PRIORITY";
    private static final String BENEFIT_PRIORITY_CUSTOMER_SERVICE = "PRIORITY_CUSTOMER_SERVICE";

    private static final String ROUTE_TITLE_SMART_FIRST = "在线客服";
    private static final String ROUTE_TITLE_MANUAL_PRIORITY = "专属客服";
    private static final String ROUTE_TITLE_APPEAL_PRIORITY = "治理申诉专线";

    private static final String HELP_CODE_ACCOUNT_SECURITY = "ACCOUNT_SECURITY";
    private static final String HELP_CODE_REPORT_APPEAL = "REPORT_APPEAL";
    private static final String HELP_CODE_PRIVACY_SETTINGS = "PRIVACY_SETTINGS";
    private static final String HELP_CODE_BEGINNER_GUIDE = "BEGINNER_GUIDE";
    private static final String HELP_CODE_COMMUNITY_RULES = "COMMUNITY_RULES";
    private static final String HELP_CODE_FEATURE_GUIDE = "FEATURE_GUIDE";
    private static final String HELP_CODE_PRODUCT_UPDATE = "PRODUCT_UPDATE";
    private static final String HELP_CODE_FEATURE_RELEASE = "FEATURE_RELEASE";
    private static final String HELP_CODE_POLICY_NOTICE = "POLICY_NOTICE";

    private static final List<ContentHelpSearchResultVO> STATIC_HELP_ENTRIES = List.of(
        buildStaticSearchResult(HELP_CODE_ACCOUNT_SECURITY, "账号安全", "账号登录、密码与设备安全相关问题"),
        buildStaticSearchResult(HELP_CODE_REPORT_APPEAL, "举报申诉", "举报违规内容、处罚申诉与进度跟踪"),
        buildStaticSearchResult(HELP_CODE_PRIVACY_SETTINGS, "隐私设置", "隐私可见性、通知偏好与账号保护设置"),
        buildStaticSearchResult(HELP_CODE_BEGINNER_GUIDE, "新手指南", "帮助新用户快速了解社区基础功能"),
        buildStaticSearchResult(HELP_CODE_COMMUNITY_RULES, "社区规范", "社区规则、治理边界与处罚说明"),
        buildStaticSearchResult(HELP_CODE_FEATURE_GUIDE, "功能使用说明", "发布、互动与个人主页等功能操作说明"),
        buildStaticSearchResult(HELP_CODE_PRODUCT_UPDATE, "产品更新", "版本更新与产品能力变更记录"),
        buildStaticSearchResult(HELP_CODE_FEATURE_RELEASE, "功能上新", "新功能发布与体验优化说明"),
        buildStaticSearchResult(HELP_CODE_POLICY_NOTICE, "规则公告", "平台规则与治理公告更新"));

    private static final List<ContentChangelogVO> CHANGELOG = List.of(
        new ContentChangelogVO()
            .setVersion("3.2.0")
            .setReleaseDate(Date.from(LocalDate.of(2026, 5, 20).atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setAdditions(List.of("新增内容社区举报功能", "新增用户申诉通道"))
            .setImprovements(List.of("优化帮助中心搜索体验"))
            .setFixes(List.of("修复客服会话过期判断不准确的问题")),
        new ContentChangelogVO()
            .setVersion("3.1.0")
            .setReleaseDate(Date.from(LocalDate.of(2026, 4, 15).atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setAdditions(List.of("新增客服会话管理", "新增用户等级权益体系"))
            .setImprovements(List.of("优化举报处理流程", "提升治理优先级路由准确度"))
            .setFixes(List.of("修复申诉进度查询偶现空指针")),
        new ContentChangelogVO()
            .setVersion("3.0.0")
            .setReleaseDate(Date.from(LocalDate.of(2026, 3, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setAdditions(List.of("内容社区模块上线"))
            .setImprovements(List.of("基础架构搭建"))
            .setFixes(List.of()));

    @Resource
    private ContentUserAppealMapper appealMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private ContentUserReportMapper reportMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Resource
    private ContentCustomerServiceSessionMapper serviceSessionMapper;

    @Resource
    private IContentUserGrowthPenaltyRecoveryService growthPenaltyRecoveryService;

    @Resource
    private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

    @Resource
    private IContentUserLevelBenefitService levelBenefitService;

    @Resource
    private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;

    @Resource
    private IContentNotificationService notificationService;
    /**
     * Creates a user appeal record and returns its identifier.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAppeal(ContentAppealCreateReq req) {
        LambdaQueryWrapper<ContentUserAppeal> countCheck = new LambdaQueryWrapper<>();
        countCheck.eq(ContentUserAppeal::getUserId, req.getUserId())
            .eq(ContentUserAppeal::getTargetId, req.getTargetId());
        Long existingCount = appealMapper.selectCount(countCheck);
        if (existingCount != null && existingCount >= 3) {
            throw new JeecgBootException("同一事项最多申诉 3 次，已达上限");
        }
        ContentUserAppeal appeal = ContentUserAppeal.from(req);
        appeal.setId(UUIDGenerator.generate());
        appeal.setProgressNote("已提交，等待处理");
        appealMapper.insert(appeal);
        auditLogMapper.insert(ContentUserAuditLog.appealCreated(appeal));
        return appeal.getId();
    }

    /**
     * Queries the current progress of the specified appeal.
     */
    @Override
    public ContentUserAppealProgressVO getAppealProgress(String userId, String appealId) {
        ContentUserAppeal appeal = appealMapper.selectById(appealId);
        if (appeal == null || !userId.equals(appeal.getUserId())) {
            throw new JeecgBootException("申诉不存在或无权查看");
        }
        return toAppealProgress(appeal);
    }

    /**
     * Lists all appeals for the specified user.
     */
    @Override
    public ContentUserAppealPageVO listAppeals(String userId, Long pageNo, Long pageSize) {
        long currentPage = pageNo == null || pageNo < 1L ? 1L : pageNo;
        long currentSize = pageSize == null || pageSize < 1L ? 10L : pageSize;
        LambdaQueryWrapper<ContentUserAppeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentUserAppeal::getUserId, userId)
            .orderByDesc(ContentUserAppeal::getCreateTime);
        IPage<ContentUserAppeal> page = appealMapper.selectPage(new Page<>(currentPage, currentSize), queryWrapper);
        return new ContentUserAppealPageVO()
            .setRecords(page.getRecords().stream().map(this::toAppealProgress).toList())
            .setTotal(page.getTotal())
            .setPageNo(page.getCurrent())
            .setPageSize(page.getSize());
    }

    /**
     * Creates a user report record and returns its identifier.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createReport(ContentReportCreateReq req) {
        LambdaQueryWrapper<ContentUserReport> duplicateCheck = new LambdaQueryWrapper<>();
        duplicateCheck.eq(ContentUserReport::getUserId, req.getUserId())
            .eq(ContentUserReport::getTargetType, req.getTargetType())
            .eq(ContentUserReport::getTargetId, req.getTargetId());
        if (reportMapper.selectOne(duplicateCheck) != null) {
            throw new JeecgBootException("您已对此内容提交过举报，请勿重复提交");
        }
        ContentUserReport report = ContentUserReport.from(req);
        report.setId(UUIDGenerator.generate());
        reportMapper.insert(report);
        auditLogMapper.insert(ContentUserAuditLog.reportCreated(report.getId(), req));
        return "举报已提交，举报编号：" + report.getId();
    }

    /**
     * Queries the current progress of the specified report.
     */
    @Override
    public ContentUserReportProgressVO getReportProgress(String userId, String reportId) {
        ContentUserReport report = reportMapper.selectById(reportId);
        if (report == null || !userId.equals(report.getUserId())) {
            throw new JeecgBootException("举报不存在或无权查看");
        }
        return toReportProgress(report);
    }

    /**
     * Lists reports for the specified user with pagination.
     */
    @Override
    public ContentUserReportPageVO listReportsForUser(String userId, Long pageNo, Long pageSize) {
        long currentPage = pageNo == null || pageNo < 1L ? 1L : pageNo;
        long currentSize = pageSize == null || pageSize < 1L ? 10L : pageSize;
        LambdaQueryWrapper<ContentUserReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentUserReport::getUserId, userId)
            .orderByDesc(ContentUserReport::getCreateTime);
        IPage<ContentUserReport> page = reportMapper.selectPage(new Page<>(currentPage, currentSize), queryWrapper);
        return new ContentUserReportPageVO()
            .setRecords(page.getRecords().stream().map(this::toUserReportListItem).toList())
            .setTotal(page.getTotal())
            .setPageNo(page.getCurrent())
            .setPageSize(page.getSize());
    }

    /**
     * Queries report detail for the specified user.
     */
    @Override
    public ContentUserReportDetailVO getReportDetailForUser(String userId, String reportId) {
        ContentUserReport report = reportMapper.selectById(reportId);
        if (report == null || !userId.equals(report.getUserId())) {
            throw new JeecgBootException("举报不存在或无权查看");
        }
        return toUserReportDetail(report);
    }

    /**
     * Queries appeal detail for the specified user.
     */
    @Override
    public ContentUserAppealDetailVO getAppealDetail(String userId, String appealId) {
        ContentUserAppeal appeal = appealMapper.selectById(appealId);
        if (appeal == null || !userId.equals(appeal.getUserId())) {
            throw new JeecgBootException("申诉不存在或无权查看");
        }
        return toAppealDetail(appeal);
    }

    /**
     * Lists reports for admin with minimal filters.
     */
    @Override
    public ContentUserReportAdminPageVO listReportsForAdmin(ContentUserReportAdminQueryReq req) {
        if (req == null) {
            req = new ContentUserReportAdminQueryReq();
        }
        validateAdminQueryTimeRange(req);
        long pageNo = req.getPageNo() == null ? 1L : req.getPageNo();
        long pageSize = req.getPageSize() == null ? 10L : req.getPageSize();

        LambdaQueryWrapper<ContentUserReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(req.getStatus()), ContentUserReport::getStatus, req.getStatus())
            .eq(StringUtils.hasText(req.getResultStatus()), ContentUserReport::getResultStatus, req.getResultStatus())
            .eq(StringUtils.hasText(req.getUserId()), ContentUserReport::getUserId, req.getUserId())
            .eq(StringUtils.hasText(req.getTargetType()), ContentUserReport::getTargetType, req.getTargetType())
            .eq(StringUtils.hasText(req.getTargetId()), ContentUserReport::getTargetId, req.getTargetId())
            .eq(StringUtils.hasText(req.getReportType()), ContentUserReport::getReportType, req.getReportType())
            .eq(StringUtils.hasText(req.getResolvedBy()), ContentUserReport::getResolvedBy, req.getResolvedBy())
            .ge(req.getCreateTimeStart() != null, ContentUserReport::getCreateTime, req.getCreateTimeStart())
            .le(req.getCreateTimeEnd() != null, ContentUserReport::getCreateTime, req.getCreateTimeEnd())
            .orderByDesc(ContentUserReport::getCreateTime);
        IPage<ContentUserReport> page = reportMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        return new ContentUserReportAdminPageVO()
            .setRecords(page.getRecords().stream().map(this::toAdminListItem).toList())
            .setTotal(page.getTotal())
            .setPageNo(page.getCurrent())
            .setPageSize(page.getSize());
    }

    /**
     * Queries report detail for admin.
     */
    @Override
    public ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId) {
        ContentUserReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new JeecgBootException("举报不存在");
        }
        return toAdminDetail(report);
    }

    /**
     * 按用户分层返回帮助中心分类与推荐客服摘要。
     */
    @Override
    public ContentHelpCenterVO getHelpCenter(String userId) {
        ContentUserProfile profile = selectUserProfile(userId);
        String userRouteType = resolveUserRouteType(profile);
        return new ContentHelpCenterVO()
            .setFaqCategories(List.of(
                buildHelpCenterEntry(HELP_CODE_ACCOUNT_SECURITY, "账号安全", "账号登录、密码与设备安全相关问题", userRouteType),
                buildHelpCenterEntry(HELP_CODE_REPORT_APPEAL, "举报申诉", "举报违规内容、处罚申诉与进度跟踪", userRouteType),
                buildHelpCenterEntry(HELP_CODE_PRIVACY_SETTINGS, "隐私设置", "隐私可见性、通知偏好与账号保护设置", userRouteType)))
            .setGuideEntries(List.of(
                buildHelpCenterEntry(HELP_CODE_BEGINNER_GUIDE, "新手指南", "帮助新用户快速了解社区基础功能", userRouteType),
                buildHelpCenterEntry(HELP_CODE_COMMUNITY_RULES, "社区规范", "社区规则、治理边界与处罚说明", userRouteType),
                buildHelpCenterEntry(HELP_CODE_FEATURE_GUIDE, "功能使用说明", "发布、互动与个人主页等功能操作说明", userRouteType)))
            .setReleaseNotes(List.of(
                buildReleaseNoteEntry(HELP_CODE_PRODUCT_UPDATE, "产品更新", "版本更新与产品能力变更记录"),
                buildReleaseNoteEntry(HELP_CODE_FEATURE_RELEASE, "功能上新", "新功能发布与体验优化说明"),
                buildReleaseNoteEntry(HELP_CODE_POLICY_NOTICE, "规则公告", "平台规则与治理公告更新")));
    }

    /**
     * 基于关键词搜索帮助文章，使用忽略大小写的子串匹配。
     */
    @Override
    public List<ContentHelpSearchResultVO> searchHelpArticles(String userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return STATIC_HELP_ENTRIES.stream()
            .filter(e -> matchesKeyword(e, lowerKeyword))
            .toList();
    }

    /**
     * Returns the customer service routing entry based on user profile priority.
     */
    @Override
    public ContentCustomerServiceVO getCustomerServiceEntry(String userId) {
        ContentUserProfile profile = selectUserProfile(userId);
        String routeType = resolveUserRouteType(profile);
        if (ROUTE_APPEAL_PRIORITY.equals(routeType)) {
            return new ContentCustomerServiceVO()
                .setRouteType(ROUTE_APPEAL_PRIORITY)
                .setTitle(ROUTE_TITLE_APPEAL_PRIORITY)
                .setDescription("当前账号状态异常，优先进入申诉与人工复核通道")
                .setManualSupported(Boolean.TRUE);
        }
        if (ROUTE_MANUAL_PRIORITY.equals(routeType)) {
            return new ContentCustomerServiceVO()
                .setRouteType(ROUTE_MANUAL_PRIORITY)
                .setTitle(ROUTE_TITLE_MANUAL_PRIORITY)
                .setDescription("高等级用户优先进入人工客服通道")
                .setManualSupported(Boolean.TRUE);
        }
        return new ContentCustomerServiceVO()
            .setRouteType(ROUTE_SMART_FIRST)
            .setTitle(ROUTE_TITLE_SMART_FIRST)
            .setDescription("优先进入智能客服，可转人工处理")
            .setManualSupported(Boolean.TRUE);
    }

    /**
     * Lists service sessions for the specified user, with 30-day expiration flag.
     */
    @Override
    public ContentServiceSessionPageVO listServiceSessions(ContentServiceSessionQueryReq req) {
        long currentPage = req.getPageNo() == null || req.getPageNo() < 1L ? 1L : req.getPageNo();
        long currentSize = req.getPageSize() == null || req.getPageSize() < 1L ? 10L : req.getPageSize();
        LambdaQueryWrapper<ContentCustomerServiceSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentCustomerServiceSession::getUserId, req.getUserId())
            .orderByDesc(ContentCustomerServiceSession::getCreateTime);
        IPage<ContentCustomerServiceSession> page = serviceSessionMapper.selectPage(
            new Page<>(currentPage, currentSize), queryWrapper);
        long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        return new ContentServiceSessionPageVO()
            .setRecords(page.getRecords().stream().map(s -> toSessionVO(s, thirtyDaysAgo)).toList())
            .setTotal(page.getTotal())
            .setPageNo(page.getCurrent())
            .setPageSize(page.getSize());
    }

    /**
     * Creates a new customer service session.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createServiceSession(String userId, String sessionType) {
        ContentCustomerServiceSession session = new ContentCustomerServiceSession();
        session.setId(UUIDGenerator.generate());
        session.setUserId(userId);
        session.setSessionType(sessionType);
        session.setStatus("ACTIVE");
        session.setStartTime(new Date());
        serviceSessionMapper.insert(session);
        return session.getId();
    }

    /**
     * Rates a completed service session.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rateService(String userId, String sessionId, Integer rating, String comment) {
        ContentCustomerServiceSession session = serviceSessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在或无权操作");
        }
        if (!"CLOSED".equals(session.getStatus())) {
            throw new JeecgBootException("仅已结束的会话可以评分");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new JeecgBootException("评分范围为 1-5");
        }
        session.setRating(rating);
        session.setRatingComment(comment);
        serviceSessionMapper.updateById(session);
        return session.getId();
    }

    /**
     * 返回更新日志列表，按版本号倒序排列。
     */
    @Override
    public List<ContentChangelogVO> getChangelog(String userId) {
        return CHANGELOG;
    }

    /**
     * Handles the specified appeal and writes back the final result.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAppeal(ContentAppealHandleReq req) {
        ContentUserAppeal appeal = appealMapper.selectById(req.getAppealId());
        if (appeal == null) {
            throw new JeecgBootException("申诉不存在");
        }
        validateAppealHandle(req, appeal);
        Date resolvedAt = new Date();
        appeal.setStatus(req.getStatus());
        appeal.setResultStatus(req.getResultStatus());
        appeal.setResultNote(req.getResultNote());
        appeal.setProgressNote(req.getProgressNote());
        appeal.setResolvedBy(req.getOperatorUserId());
        appeal.setResolvedAt(resolvedAt);
        appealMapper.updateById(appeal);
        restoreGovernanceStatusIfNecessary(appeal, req);
        growthPenaltyRecoveryService.recoverByAppeal(appeal, req.getOperatorUserId(), resolvedAt, req.getResultNote());
        auditLogMapper.insert(ContentUserAuditLog.appealHandled(appeal, req));
        notificationService.sendNotification(appeal.getUserId(), "APPEAL_RESULT",
            "申诉处理结果", "您的申诉已处理，结果：" + req.getResultStatus());
        return appeal.getId();
    }

    /**
     * Handles the specified report and writes back the final result.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleReport(ContentReportHandleReq req) {
        ContentUserReport report = reportMapper.selectById(req.getReportId());
        if (report == null) {
            throw new JeecgBootException("举报不存在");
        }
        validateReportHandle(req, report);
        Date resolvedAt = new Date();
        report.setStatus(req.getStatus());
        report.setResultStatus(req.getResultStatus());
        report.setResultNote(req.getResultNote());
        report.setProgressNote(req.getProgressNote());
        report.setResolvedBy(req.getOperatorUserId());
        report.setResolvedAt(resolvedAt);
        reportMapper.updateById(report);
        if (growthPenaltyRecordService != null && PUNISHING_REPORT_RESULTS.contains(req.getResultStatus())) {
            growthPenaltyRecordService.createFromReportHandle(report, req, null, resolvedAt);
        }
        auditLogMapper.insert(ContentUserAuditLog.reportHandled(report, req));
        notificationService.sendNotification(report.getUserId(), "REPORT_RESULT",
            "举报处理结果", "您的举报已处理，结果：" + req.getResultStatus());
        return report.getId();
    }

    private ContentUserAppealProgressVO toAppealProgress(ContentUserAppeal appeal) {
        return new ContentUserAppealProgressVO()
            .setAppealId(appeal.getId())
            .setStatus(appeal.getStatus())
            .setProgressNote(appeal.getProgressNote())
            .setResultStatus(appeal.getResultStatus())
            .setResultNote(appeal.getResultNote())
            .setResolvedBy(appeal.getResolvedBy())
            .setResolvedAt(appeal.getResolvedAt());
    }

    private ContentUserReportProgressVO toReportProgress(ContentUserReport report) {
        return new ContentUserReportProgressVO()
            .setReportId(report.getId())
            .setStatus(report.getStatus())
            .setProgressNote(report.getProgressNote())
            .setResultStatus(report.getResultStatus())
            .setResultNote(report.getResultNote())
            .setResolvedBy(report.getResolvedBy())
            .setResolvedAt(report.getResolvedAt());
    }

    private ContentUserReportListItemVO toUserReportListItem(ContentUserReport report) {
        return new ContentUserReportListItemVO()
            .setReportId(report.getId())
            .setReportNo(report.getId())
            .setTargetSummary(report.getTargetType() + ":" + report.getTargetId())
            .setReportTypeLabel(resolveReportTypeLabel(report.getReportType()))
            .setStatusLabel(resolveReportStatusLabel(report.getStatus()))
            .setStatus(report.getStatus())
            .setResultStatus(report.getResultStatus())
            .setCreateTime(report.getCreateTime());
    }

    private ContentUserReportDetailVO toUserReportDetail(ContentUserReport report) {
        return new ContentUserReportDetailVO()
            .setReportId(report.getId())
            .setReportNo(report.getId())
            .setTargetType(report.getTargetType())
            .setTargetId(report.getTargetId())
            .setTargetSummary(report.getTargetType() + ":" + report.getTargetId())
            .setReportType(report.getReportType())
            .setReportTypeLabel(resolveReportTypeLabel(report.getReportType()))
            .setReason(report.getReason())
            .setEvidenceJson(report.getEvidenceJson())
            .setStatus(report.getStatus())
            .setStatusLabel(resolveReportStatusLabel(report.getStatus()))
            .setResultStatus(report.getResultStatus())
            .setResultNote(report.getResultNote())
            .setProgressNote(report.getProgressNote())
            .setResolvedBy(report.getResolvedBy())
            .setResolvedAt(report.getResolvedAt())
            .setCreateTime(report.getCreateTime());
    }

    private ContentUserAppealDetailVO toAppealDetail(ContentUserAppeal appeal) {
        return new ContentUserAppealDetailVO()
            .setAppealId(appeal.getId())
            .setAppealType(appeal.getAppealType())
            .setTargetType(appeal.getTargetType())
            .setTargetId(appeal.getTargetId())
            .setReason(appeal.getReason())
            .setStatus(appeal.getStatus())
            .setProgressNote(appeal.getProgressNote())
            .setResultStatus(appeal.getResultStatus())
            .setResultNote(appeal.getResultNote())
            .setResolvedBy(appeal.getResolvedBy())
            .setResolvedAt(appeal.getResolvedAt())
            .setCreateTime(appeal.getCreateTime());
    }

    private String resolveReportTypeLabel(String reportType) {
        if (reportType == null) {
            return "其他";
        }
        return switch (reportType) {
            case "SPAM" -> "垃圾内容";
            case "ABUSE" -> "辱骂骚扰";
            case "PORNOGRAPHY" -> "色情低俗";
            case "VIOLENCE" -> "暴力血腥";
            case "FRAUD" -> "欺诈诈骗";
            case "INFRINGEMENT" -> "侵权";
            default -> "其他";
        };
    }

    private String resolveReportStatusLabel(String status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case "PENDING" -> "待处理";
            case "REVIEWING" -> "审核中";
            case "RESOLVED" -> "已处理";
            case "WITHDRAWN" -> "已撤回";
            default -> "未知";
        };
    }

    private ContentUserReportAdminListItemVO toAdminListItem(ContentUserReport report) {
        return new ContentUserReportAdminListItemVO()
            .setReportId(report.getId())
            .setUserId(report.getUserId())
            .setTargetType(report.getTargetType())
            .setTargetId(report.getTargetId())
            .setReportType(report.getReportType())
            .setStatus(report.getStatus())
            .setResultStatus(report.getResultStatus())
            .setResolvedBy(report.getResolvedBy())
            .setResolvedAt(report.getResolvedAt())
            .setCreateTime(report.getCreateTime());
    }

    private ContentUserReportAdminDetailVO toAdminDetail(ContentUserReport report) {
        return new ContentUserReportAdminDetailVO()
            .setReportId(report.getId())
            .setUserId(report.getUserId())
            .setTargetType(report.getTargetType())
            .setTargetId(report.getTargetId())
            .setReportType(report.getReportType())
            .setReason(report.getReason())
            .setEvidenceJson(report.getEvidenceJson())
            .setStatus(report.getStatus())
            .setResultStatus(report.getResultStatus())
            .setResultNote(report.getResultNote())
            .setProgressNote(report.getProgressNote())
            .setResolvedBy(report.getResolvedBy())
            .setResolvedAt(report.getResolvedAt())
            .setCreateTime(report.getCreateTime());
    }

    private void validateAdminQueryTimeRange(ContentUserReportAdminQueryReq req) {
        if (req.getCreateTimeStart() != null
            && req.getCreateTimeEnd() != null
            && req.getCreateTimeStart().after(req.getCreateTimeEnd())) {
            throw new JeecgBootException("创建时间范围非法");
        }
    }

    private void validateAppealHandle(ContentAppealHandleReq req, ContentUserAppeal appeal) {
        if (!"RESOLVED".equals(req.getStatus())) {
            throw new JeecgBootException("申诉处理仅支持流转到RESOLVED");
        }
        if ("RESOLVED".equals(appeal.getStatus())) {
            throw new JeecgBootException("申诉已处理完成，请勿重复处理");
        }
        if (!"PENDING".equals(appeal.getStatus()) && !"PROCESSING".equals(appeal.getStatus())) {
            throw new JeecgBootException("当前申诉状态不允许处理");
        }
    }

    private void validateReportHandle(ContentReportHandleReq req, ContentUserReport report) {
        if (!"RESOLVED".equals(req.getStatus())) {
            throw new JeecgBootException("举报处理仅支持流转到RESOLVED");
        }
        if ("RESOLVED".equals(report.getStatus())) {
            throw new JeecgBootException("举报已处理完成，请勿重复处理");
        }
        if (!"PENDING".equals(report.getStatus())) {
            throw new JeecgBootException("当前举报状态不允许处理");
        }
    }

    private void restoreGovernanceStatusIfNecessary(ContentUserAppeal appeal, ContentAppealHandleReq req) {
        if (!"APPROVED".equalsIgnoreCase(req.getResultStatus())
            || appeal == null
            || !APPEAL_TARGET_TYPE_GOVERNANCE_STATUS.equals(appeal.getTargetType())
            || !StringUtils.hasText(appeal.getTargetId())
            || profileMapper == null
            || statusRecordMapper == null) {
            return;
        }
        ContentUserStatusRecord targetRecord = statusRecordMapper.selectById(appeal.getTargetId());
        if (targetRecord == null
            || !appeal.getUserId().equals(targetRecord.getUserId())
            || !Boolean.TRUE.equals(targetRecord.getRecoverable())) {
            return;
        }
        ContentUserProfile profile = profileMapper.selectByUserId(appeal.getUserId());
        if (!shouldRestoreGovernanceStatus(profile)) {
            return;
        }
        String restoredStatus = StringUtils.hasText(targetRecord.getCurrentStatus())
            ? targetRecord.getCurrentStatus()
            : ContentUserStatusEnum.NORMAL.getCode();
        if (restoredStatus.equals(profile.getStatus())) {
            return;
        }
        String currentStatus = profile.getStatus();
        profile.setStatus(restoredStatus);
        profileMapper.updateById(profile);

        ContentUserStatusRecord restoreRecord = new ContentUserStatusRecord();
        restoreRecord.setId(UUIDGenerator.generate());
        restoreRecord.setUserId(appeal.getUserId());
        restoreRecord.setCurrentStatus(currentStatus);
        restoreRecord.setTargetStatus(restoredStatus);
        restoreRecord.setTriggerSource("APPEAL_APPROVED");
        restoreRecord.setOperatorUserId(req.getOperatorUserId());
        restoreRecord.setReason(req.getResultNote());
        restoreRecord.setRecoverable(Boolean.FALSE);
        restoreRecord.setEffectiveStartTime(new Date());
        statusRecordMapper.insert(restoreRecord);
    }
    private ContentUserProfile selectUserProfile(String userId) {
        if (profileMapper == null || !StringUtils.hasText(userId)) {
            return null;
        }
        return profileMapper.selectByUserId(userId);
    }

    private String resolveUserRouteType(ContentUserProfile profile) {
        if (shouldRouteToGovernancePriority(profile)) {
            return ROUTE_APPEAL_PRIORITY;
        }
        if (shouldRouteToManualPriority(profile)) {
            return ROUTE_MANUAL_PRIORITY;
        }
        return ROUTE_SMART_FIRST;
    }

    private ContentHelpCenterEntryVO buildHelpCenterEntry(String code, String title, String description, String userRouteType) {
        String recommendedRouteType = resolveHelpCenterRouteType(code, userRouteType);
        return new ContentHelpCenterEntryVO()
            .setCode(code)
            .setTitle(title)
            .setDescription(description)
            .setRecommendedRouteType(recommendedRouteType)
            .setRecommendedRouteTitle(resolveRouteTitle(recommendedRouteType))
            .setManualSupported(Boolean.TRUE);
    }

    private ContentHelpCenterEntryVO buildReleaseNoteEntry(String code, String title, String description) {
        return new ContentHelpCenterEntryVO()
            .setCode(code)
            .setTitle(title)
            .setDescription(description)
            .setRecommendedRouteType(null)
            .setRecommendedRouteTitle(null)
            .setManualSupported(null);
    }

    private String resolveHelpCenterRouteType(String code, String userRouteType) {
        if (ROUTE_APPEAL_PRIORITY.equals(userRouteType)) {
            if (HELP_CODE_ACCOUNT_SECURITY.equals(code)
                || HELP_CODE_REPORT_APPEAL.equals(code)
                || HELP_CODE_COMMUNITY_RULES.equals(code)) {
                return ROUTE_APPEAL_PRIORITY;
            }
            return ROUTE_SMART_FIRST;
        }
        if (ROUTE_MANUAL_PRIORITY.equals(userRouteType)) {
            if (HELP_CODE_ACCOUNT_SECURITY.equals(code) || HELP_CODE_REPORT_APPEAL.equals(code)) {
                return ROUTE_MANUAL_PRIORITY;
            }
            return ROUTE_SMART_FIRST;
        }
        return ROUTE_SMART_FIRST;
    }

    private String resolveRouteTitle(String routeType) {
        if (ROUTE_APPEAL_PRIORITY.equals(routeType)) {
            return ROUTE_TITLE_APPEAL_PRIORITY;
        }
        if (ROUTE_MANUAL_PRIORITY.equals(routeType)) {
            return ROUTE_TITLE_MANUAL_PRIORITY;
        }
        return ROUTE_TITLE_SMART_FIRST;
    }

    private boolean shouldRouteToManualPriority(ContentUserProfile profile) {
        if (profile == null) {
            return false;
        }
        if (levelBenefitService != null && StringUtils.hasText(profile.getUserId())) {
            if (levelBenefitService.hasEnabledBenefit(profile.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE)) {
                return true;
            }
            if (levelBenefitService.isBenefitExplicitlyDisabled(profile.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE)) {
                return false;
            }
        }
        int level = profile.getLevel() == null ? 1 : profile.getLevel();
        int growthValue = profile.getGrowthValue() == null ? 0 : profile.getGrowthValue();
        return level >= 15 || growthValue >= 400;
    }

    private boolean shouldRouteToGovernancePriority(ContentUserProfile profile) {
        if (profile == null || !StringUtils.hasText(profile.getStatus())) {
            return false;
        }
        return ContentUserStatusEnum.FROZEN.getCode().equals(profile.getStatus())
            || ContentUserStatusEnum.BANNED.getCode().equals(profile.getStatus())
            || ContentUserStatusEnum.CANCEL_PENDING.getCode().equals(profile.getStatus());
    }

    private boolean shouldRestoreGovernanceStatus(ContentUserProfile profile) {
        if (profile == null || !StringUtils.hasText(profile.getStatus())) {
            return false;
        }
        return !ContentUserStatusEnum.NORMAL.getCode().equals(profile.getStatus())
            && !ContentUserStatusEnum.GUEST.getCode().equals(profile.getStatus())
            && !ContentUserStatusEnum.REGISTERED_INCOMPLETE.getCode().equals(profile.getStatus())
            && !ContentUserStatusEnum.CANCELLED.getCode().equals(profile.getStatus());
    }

    private static ContentHelpSearchResultVO buildStaticSearchResult(String code, String title, String description) {
        return new ContentHelpSearchResultVO()
            .setCode(code)
            .setTitle(title)
            .setDescription(description)
            .setSnippet(description);
    }

    private boolean matchesKeyword(ContentHelpSearchResultVO entry, String lowerKeyword) {
        return (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(lowerKeyword))
            || (entry.getDescription() != null && entry.getDescription().toLowerCase().contains(lowerKeyword));
    }

    private ContentServiceSessionVO toSessionVO(ContentCustomerServiceSession session, long thirtyDaysAgoMillis) {
        boolean expired = session.getCreateTime() != null && session.getCreateTime().getTime() < thirtyDaysAgoMillis;
        return new ContentServiceSessionVO()
            .setSessionId(session.getId())
            .setSessionType(session.getSessionType())
            .setStatus(session.getStatus())
            .setRating(session.getRating())
            .setRatingComment(session.getRatingComment())
            .setStartTime(session.getStartTime())
            .setEndTime(session.getEndTime())
            .setExpired(expired);
    }

    @Override
    public String withdrawReport(String userId, String reportId) {
        ContentUserReport report = reportMapper.selectById(reportId);
        if (report == null || !userId.equals(report.getUserId())) {
            throw new JeecgBootException("举报不存在或无权操作");
        }
        if (!"PENDING".equals(report.getStatus())) {
            throw new JeecgBootException("仅待处理状态的举报可撤回");
        }
        report.setStatus("WITHDRAWN");
        reportMapper.updateById(report);
        return reportId;
    }

    @Override
    public String withdrawAppeal(String userId, String appealId) {
        ContentUserAppeal appeal = appealMapper.selectById(appealId);
        if (appeal == null || !userId.equals(appeal.getUserId())) {
            throw new JeecgBootException("申诉不存在或无权操作");
        }
        if (!"PENDING".equals(appeal.getStatus()) && !"PROCESSING".equals(appeal.getStatus())) {
            throw new JeecgBootException("仅待处理或处理中的申诉可撤回");
        }
        appeal.setStatus("WITHDRAWN");
        appealMapper.updateById(appeal);
        return appealId;
    }

    @Override
    public List<ContentHelpCenterEntryVO> getHelpCategories(String userId) {
        return getHelpCenter(userId).getFaqCategories();
    }

    @Override
    public ContentHelpSearchResultVO getHelpArticleDetail(String userId, String articleId) {
        ContentHelpCenterVO center = getHelpCenter(userId);
        return Stream.of(
                center.getFaqCategories() != null ? center.getFaqCategories().stream() : Stream.<ContentHelpCenterEntryVO>empty(),
                center.getGuideEntries() != null ? center.getGuideEntries().stream() : Stream.<ContentHelpCenterEntryVO>empty(),
                center.getReleaseNotes() != null ? center.getReleaseNotes().stream() : Stream.<ContentHelpCenterEntryVO>empty()
            )
            .flatMap(s -> s)
            .filter(entry -> articleId.equals(entry.getCode()))
            .findFirst()
            .map(entry -> new ContentHelpSearchResultVO()
                .setCode(entry.getCode())
                .setTitle(entry.getTitle())
                .setDescription(entry.getDescription())
                .setSnippet(entry.getDescription()))
            .orElseThrow(() -> new JeecgBootException("文章不存在"));
    }

    @Override
    public String submitArticleFeedback(String userId, String articleId, Boolean helpful) {
        // 简单实现，后续可持久化到数据库
        return "反馈已提交";
    }
}
