package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentCustomerServiceSession;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.mapper.ContentCustomerServiceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserReportMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentServiceSessionQueryReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.impl.ContentUserSupportServiceImpl;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
import org.jeecg.modules.content.user.vo.ContentChangelogVO;
import org.jeecg.modules.content.user.vo.ContentHelpSearchResultVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionPageVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for content user support service.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserSupportServiceTest {

    @Mock
    private ContentUserAppealMapper appealMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @Mock
    private ContentUserReportMapper reportMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Mock
    private ContentCustomerServiceSessionMapper serviceSessionMapper;

    @Mock
    private IContentUserGrowthPenaltyRecoveryService growthPenaltyRecoveryService;

    @Mock
    private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @Mock
    private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;

    @Mock
    private IContentNotificationService notificationService;
    @InjectMocks
    private ContentUserSupportServiceImpl supportService;

    @Test
    void shouldCreateAppealAgainstPenaltyAndRecordProgress() {
        String appealId = supportService.createAppeal(createAppealReq());

        assertThat(appealId).isNotBlank();
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) -> "USER_APPEAL_CREATED".equals(it.getEventType())));
    }

    @Test
    void shouldRejectAppealWhenExceedingMaxAttemptCount() {
        when(appealMapper.selectCount(any())).thenReturn(3L);

        assertThatThrownBy(() -> supportService.createAppeal(createAppealReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("同一事项最多申诉 3 次，已达上限");
    }

    @Test
    void shouldQueryAppealProgressForOwner() {
        Date resolvedAt = new Date();
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PROCESSING")
            .setProgressNote("客服已受理")
            .setResultStatus("APPROVED")
            .setResultNote("处罚已撤销")
            .setResolvedBy("admin-1")
            .setResolvedAt(resolvedAt);
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        ContentUserAppealProgressVO result = supportService.getAppealProgress("u1", "appeal-1");

        assertThat(result.getAppealId()).isEqualTo("appeal-1");
        assertThat(result.getStatus()).isEqualTo("PROCESSING");
        assertThat(result.getProgressNote()).isEqualTo("客服已受理");
        assertThat(result.getResultStatus()).isEqualTo("APPROVED");
        assertThat(result.getResultNote()).isEqualTo("处罚已撤销");
        assertThat(result.getResolvedBy()).isEqualTo("admin-1");
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void shouldRejectAppealProgressQueryWhenUserDoesNotOwnAppeal() {
        ContentUserAppeal appeal = new ContentUserAppeal().setUserId("u2");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        assertThatThrownBy(() -> supportService.getAppealProgress("u1", "appeal-1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("申诉不存在或无权查看");
    }

    @Test
    void shouldPageAppealsForUser() {
        Date resolvedAt = new Date();
        ContentUserAppeal processingAppeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PROCESSING")
            .setProgressNote("处理中")
            .setResolvedAt(resolvedAt);
        processingAppeal.setId("appeal-1");
        ContentUserAppeal pendingAppeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setProgressNote("等待处理");
        pendingAppeal.setId("appeal-2");
        when(appealMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserAppeal> page = invocation.getArgument(0);
            page.setRecords(List.of(processingAppeal));
            page.setTotal(2L);
            return page;
        });

        ContentUserAppealPageVO result = supportService.listAppeals("u1", 2L, 1L);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getPageNo()).isEqualTo(2L);
        assertThat(result.getPageSize()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getAppealId()).isEqualTo("appeal-1");
        assertThat(result.getRecords().get(0).getResolvedAt()).isEqualTo(resolvedAt);
        verify(appealMapper).selectPage(argThat(page -> page.getCurrent() == 2L && page.getSize() == 1L),
            argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldCreateReportAndWriteAuditLog() {
        String reportId = supportService.createReport(createReportReq());

        assertThat(reportId).isNotBlank();
        verify(reportMapper).insert(argThat((ContentUserReport it) ->
            "u1".equals(it.getUserId())
                && "CONTENT".equals(it.getTargetType())
                && "post-1".equals(it.getTargetId())));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_REPORT_CREATED".equals(it.getEventType())
                && "u1".equals(it.getUserId())));
    }

    @Test
    void shouldRejectDuplicateReportForSameUserAndTarget() {
        when(reportMapper.selectOne(any())).thenReturn(new ContentUserReport().setUserId("u1"));

        assertThatThrownBy(() -> supportService.createReport(createReportReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("您已对此内容提交过举报，请勿重复提交");
    }

    @Test
    void shouldHandlePendingReportToResolved() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("PENDING")
            .setReportType("SPAM");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        String handledReportId = supportService.handleReport(createHandleReportReq());

        assertThat(handledReportId).isEqualTo("report-1");
        verify(reportMapper).updateById(argThat((ContentUserReport it) ->
            "RESOLVED".equals(it.getStatus())
                && "CONFIRMED".equals(it.getResultStatus())
                && "违规成立".equals(it.getResultNote())
                && "已处理完成".equals(it.getProgressNote())
                && "admin-1".equals(it.getResolvedBy())
                && it.getResolvedAt() != null));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_REPORT_HANDLED".equals(it.getEventType())
                && "admin-1".equals(it.getOperatorUserId())));
    }

    @Test
    void shouldCreateGrowthPenaltyRecordWhenReportIsConfirmed() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("PENDING")
            .setReportType("SPAM")
            .setTargetType("CONTENT")
            .setTargetId("post-1");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        supportService.handleReport(createHandleReportReq());

        verify(growthPenaltyRecordService).createFromReportHandle(
            argThat(it -> "report-1".equals(it.getId()) && "CONFIRMED".equals(it.getResultStatus())),
            argThat(it -> "report-1".equals(it.getReportId()) && "CONFIRMED".equals(it.getResultStatus())),
            isNull(),
            any(Date.class)
        );
    }

    @Test
    void shouldNotCreateGrowthPenaltyRecordWhenReportResultIsRejected() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("PENDING")
            .setReportType("SPAM");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        ContentReportHandleReq req = createHandleReportReq()
            .setResultStatus("REJECTED")
            .setResultNote("证据不足");

        supportService.handleReport(req);

        verifyNoInteractions(growthPenaltyRecordService);
    }

    @Test
    void shouldSendNotificationWhenReportHandled() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("PENDING")
            .setReportType("SPAM");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        supportService.handleReport(createHandleReportReq());

        verify(notificationService).sendNotification("u1", "REPORT_RESULT", "举报处理结果", "您的举报已处理，结果：CONFIRMED");
    }

    @Test
    void shouldSendNotificationWhenAppealApproved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setAppealType("PENALTY");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        supportService.handleAppeal(createHandleReq());

        verify(notificationService).sendNotification("u1", "APPEAL_RESULT", "申诉处理结果", "您的申诉已处理，结果：APPROVED");
    }

    @Test
    void shouldQueryReportProgressForOwner() {
        Date resolvedAt = new Date();
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("RESOLVED")
            .setProgressNote("已处理完成")
            .setResultStatus("CONFIRMED")
            .setResultNote("违规成立")
            .setResolvedBy("admin-1")
            .setResolvedAt(resolvedAt);
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        ContentUserReportProgressVO result = supportService.getReportProgress("u1", "report-1");

        assertThat(result.getReportId()).isEqualTo("report-1");
        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        assertThat(result.getResultStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void shouldListReportsForAdminWithDefaultPage() {
        Date resolvedAt = new Date();
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setStatus("PENDING")
            .setResultStatus("INIT")
            .setResolvedBy("admin-1")
            .setResolvedAt(resolvedAt);
        report.setId("report-1");
        report.setCreateTime(new Date(2000L));
        when(reportMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserReport> page = invocation.getArgument(0);
            page.setRecords(List.of(report));
            page.setTotal(1L);
            return page;
        });

        ContentUserReportAdminPageVO result =
            supportService.listReportsForAdmin(new ContentUserReportAdminQueryReq());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPageNo()).isEqualTo(1L);
        assertThat(result.getPageSize()).isEqualTo(10L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getReportId()).isEqualTo("report-1");
        verify(reportMapper).selectPage(argThat(page -> page.getCurrent() == 1L && page.getSize() == 10L),
            argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldListReportsForAdminByExtendedFilters() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResolvedBy("admin-1");
        report.setId("report-1");
        when(reportMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserReport> page = invocation.getArgument(0);
            page.setRecords(List.of(report));
            page.setTotal(1L);
            return page;
        });

        ContentUserReportAdminQueryReq req = new ContentUserReportAdminQueryReq()
            .setPageNo(2L)
            .setPageSize(20L)
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setResolvedBy("admin-1")
            .setCreateTimeStart(new Date(1000L))
            .setCreateTimeEnd(new Date(2000L));
        ContentUserReportAdminPageVO result = supportService.listReportsForAdmin(req);

        assertThat(result.getPageNo()).isEqualTo(2L);
        assertThat(result.getPageSize()).isEqualTo(20L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo("RESOLVED");
        verify(reportMapper).selectPage(argThat(page -> page.getCurrent() == 2L && page.getSize() == 20L),
            argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldReturnEmptyPageWhenNoReportMatched() {
        when(reportMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserReport> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0L);
            return page;
        });

        ContentUserReportAdminPageVO result = supportService.listReportsForAdmin(
            new ContentUserReportAdminQueryReq().setStatus("PENDING"));

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void shouldRejectListReportsForAdminWhenCreateTimeRangeIsInvalid() {
        ContentUserReportAdminQueryReq req = new ContentUserReportAdminQueryReq()
            .setCreateTimeStart(new Date(2000L))
            .setCreateTimeEnd(new Date(1000L));

        assertThatThrownBy(() -> supportService.listReportsForAdmin(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("创建时间范围非法");
    }

    @Test
    void shouldGetReportDetailForAdmin() {
        Date resolvedAt = new Date();
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setReason("垃圾内容")
            .setEvidenceJson("{\"screenshot\":true}")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResultNote("违规成立")
            .setProgressNote("已处理完成")
            .setResolvedBy("admin-1")
            .setResolvedAt(resolvedAt);
        report.setId("report-1");
        report.setCreateTime(new Date(1000L));
        when(reportMapper.selectById("report-1")).thenReturn(report);

        ContentUserReportAdminDetailVO result = supportService.getReportDetailForAdmin("report-1");

        assertThat(result.getReportId()).isEqualTo("report-1");
        assertThat(result.getReason()).isEqualTo("垃圾内容");
        assertThat(result.getEvidenceJson()).isEqualTo("{\"screenshot\":true}");
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void shouldRejectGetReportDetailForAdminWhenReportDoesNotExist() {
        when(reportMapper.selectById("report-404")).thenReturn(null);

        assertThatThrownBy(() -> supportService.getReportDetailForAdmin("report-404"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报不存在");
    }

    @Test
    void shouldRejectReportProgressQueryWhenUserDoesNotOwnReport() {
        ContentUserReport report = new ContentUserReport().setUserId("u2");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        assertThatThrownBy(() -> supportService.getReportProgress("u1", "report-1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报不存在或无权查看");
    }

    @Test
    void shouldRejectHandledReportWhenReportAlreadyResolved() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("RESOLVED");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        assertThatThrownBy(() -> supportService.handleReport(createHandleReportReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报已处理完成，请勿重复处理");
    }

    @Test
    void shouldRejectHandledReportWhenTargetStatusIsInvalid() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("PENDING");
        report.setId("report-1");
        when(reportMapper.selectById("report-1")).thenReturn(report);

        ContentReportHandleReq req = createHandleReportReq().setStatus("PROCESSING");

        assertThatThrownBy(() -> supportService.handleReport(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报处理仅支持流转到RESOLVED");
    }

    @Test
    void shouldRejectHandledReportWhenReportDoesNotExist() {
        when(reportMapper.selectById("report-1")).thenReturn(null);

        assertThatThrownBy(() -> supportService.handleReport(createHandleReportReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报不存在");
    }

    @Test
    void shouldHandlePendingAppealToResolved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setAppealType("PENALTY");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        String handledAppealId = supportService.handleAppeal(createHandleReq());

        assertThat(handledAppealId).isEqualTo("appeal-1");
        verify(appealMapper).updateById(argThat((ContentUserAppeal it) ->
            "appeal-1".equals(it.getId())
                && "RESOLVED".equals(it.getStatus())
                && "APPROVED".equals(it.getResultStatus())
                && "处罚撤销".equals(it.getResultNote())
                && "admin-1".equals(it.getResolvedBy())
                && "已处理完成".equals(it.getProgressNote())
                && it.getResolvedAt() != null));
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
            "USER_APPEAL_HANDLED".equals(it.getEventType())
                && "admin-1".equals(it.getOperatorUserId())
                && "u1".equals(it.getUserId())));
    }

    @Test
    void shouldHandleProcessingAppealToResolved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PROCESSING")
            .setAppealType("PENALTY");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        String handledAppealId = supportService.handleAppeal(createHandleReq());

        assertThat(handledAppealId).isEqualTo("appeal-1");
        verify(appealMapper).updateById(argThat((ContentUserAppeal it) ->
            "RESOLVED".equals(it.getStatus())
                && "APPROVED".equals(it.getResultStatus())));
    }

    @Test
    void shouldRestoreGovernanceStatusWhenAppealIsApproved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setAppealType("PENALTY")
            .setTargetType("GOVERNANCE_STATUS")
            .setTargetId("record-1");
        appeal.setId("appeal-1");
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setStatus("FROZEN");
        ContentUserStatusRecord targetRecord = new ContentUserStatusRecord()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("FROZEN")
            .setRecoverable(Boolean.TRUE);
        targetRecord.setId("record-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(statusRecordMapper.selectById("record-1")).thenReturn(targetRecord);

        supportService.handleAppeal(createHandleReq());

        verify(statusRecordMapper).selectById("record-1");
        verify(statusRecordMapper, org.mockito.Mockito.never()).selectLatestByUserId("u1");
        verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
            "u1".equals(it.getUserId())
                && "NORMAL".equals(it.getStatus())));
        verify(statusRecordMapper).insert(argThat((ContentUserStatusRecord it) ->
            "u1".equals(it.getUserId())
                && "FROZEN".equals(it.getCurrentStatus())
                && "NORMAL".equals(it.getTargetStatus())
                && "APPEAL_APPROVED".equals(it.getTriggerSource())
                && "admin-1".equals(it.getOperatorUserId())));
    }

    @Test
    void shouldRecoverGrowthPenaltyWhenAppealApproved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setAppealType("PENALTY")
            .setTargetType("GROWTH_PENALTY")
            .setTargetId("penalty-1");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        supportService.handleAppeal(createHandleReq());

        verify(growthPenaltyRecoveryService).recoverByAppeal(
            argThat(it -> "appeal-1".equals(it.getId())
                && "GROWTH_PENALTY".equals(it.getTargetType())
                && "penalty-1".equals(it.getTargetId())
                && "APPROVED".equals(it.getResultStatus())),
            argThat(it -> "admin-1".equals(it)),
            any(Date.class),
            argThat(it -> "处罚撤销".equals(it))
        );
    }

    @Test
    void shouldNotRestoreGovernanceStatusFromLatestRecordWhenAppealTargetsAnotherObject() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setAppealType("PENALTY")
            .setTargetType("GROWTH_PENALTY")
            .setTargetId("penalty-1");
        appeal.setId("appeal-2");
        when(appealMapper.selectById("appeal-2")).thenReturn(appeal);

        supportService.handleAppeal(createHandleReq().setAppealId("appeal-2"));

        verify(statusRecordMapper, org.mockito.Mockito.never()).selectLatestByUserId("u1");
        verify(statusRecordMapper, org.mockito.Mockito.never()).selectById(any());
        verify(profileMapper, org.mockito.Mockito.never()).updateById(any(ContentUserProfile.class));
    }

    @Test
    void shouldRejectHandledAppealWhenAppealAlreadyResolved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("RESOLVED");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        assertThatThrownBy(() -> supportService.handleAppeal(createHandleReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("申诉已处理完成，请勿重复处理");
    }

    @Test
    void shouldRejectHandledAppealWhenTargetStatusIsInvalid() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING");
        appeal.setId("appeal-1");
        when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

        ContentAppealHandleReq req = createHandleReq().setStatus("PROCESSING");

        assertThatThrownBy(() -> supportService.handleAppeal(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("申诉处理仅支持流转到RESOLVED");
    }

    @Test
    void shouldRejectHandledAppealWhenAppealDoesNotExist() {
        when(appealMapper.selectById("appeal-1")).thenReturn(null);

        assertThatThrownBy(() -> supportService.handleAppeal(createHandleReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("申诉不存在");
    }

    @Test
    void shouldReturnSmartFirstHelpCenterForDefaultUser() {
        when(profileMapper.selectByUserId("u1")).thenReturn(null);

        ContentHelpCenterVO result = supportService.getHelpCenter("u1");

        assertThat(findEntry(result.getFaqCategories(), "ACCOUNT_SECURITY").getRecommendedRouteType())
            .isEqualTo("SMART_FIRST");
        assertThat(findEntry(result.getFaqCategories(), "REPORT_APPEAL").getRecommendedRouteType())
            .isEqualTo("SMART_FIRST");
        assertThat(findEntry(result.getGuideEntries(), "BEGINNER_GUIDE").getRecommendedRouteType())
            .isEqualTo("SMART_FIRST");
        assertThat(findEntry(result.getGuideEntries(), "COMMUNITY_RULES").getRecommendedRouteType())
            .isEqualTo("SMART_FIRST");
    }

    @Test
    void shouldReturnManualPriorityHelpCenterForHighLevelUser() {
        when(profileMapper.selectByUserId("u100"))
            .thenReturn(new ContentUserProfile()
                .setUserId("u100")
                .setLevel(15)
                .setGrowthValue(420)
                .setStatus("NORMAL"));

        ContentHelpCenterVO result = supportService.getHelpCenter("u100");

        assertThat(findEntry(result.getFaqCategories(), "ACCOUNT_SECURITY").getRecommendedRouteType())
            .isEqualTo("MANUAL_PRIORITY");
        assertThat(findEntry(result.getFaqCategories(), "REPORT_APPEAL").getRecommendedRouteType())
            .isEqualTo("MANUAL_PRIORITY");
        assertThat(findEntry(result.getFaqCategories(), "PRIVACY_SETTINGS").getRecommendedRouteType())
            .isEqualTo("SMART_FIRST");
    }

    @Test
    void shouldReturnAppealPriorityHelpCenterForGovernanceUser() {
        when(profileMapper.selectByUserId("u200"))
            .thenReturn(new ContentUserProfile()
                .setUserId("u200")
                .setLevel(6)
                .setGrowthValue(600)
                .setStatus("FROZEN"));

        ContentHelpCenterVO result = supportService.getHelpCenter("u200");

        assertThat(findEntry(result.getFaqCategories(), "ACCOUNT_SECURITY").getRecommendedRouteType())
            .isEqualTo("APPEAL_PRIORITY");
        assertThat(findEntry(result.getFaqCategories(), "REPORT_APPEAL").getRecommendedRouteType())
            .isEqualTo("APPEAL_PRIORITY");
        assertThat(findEntry(result.getGuideEntries(), "COMMUNITY_RULES").getRecommendedRouteType())
            .isEqualTo("APPEAL_PRIORITY");
        assertThat(findEntry(result.getGuideEntries(), "FEATURE_GUIDE").getRecommendedRouteType())
            .isEqualTo("SMART_FIRST");
    }

    @Test
    void shouldKeepReleaseNotesWithoutRecommendedRouteFields() {
        when(profileMapper.selectByUserId("u1")).thenReturn(null);

        ContentHelpCenterVO result = supportService.getHelpCenter("u1");

        ContentHelpCenterEntryVO releaseNote = findEntry(result.getReleaseNotes(), "PRODUCT_UPDATE");
        assertThat(releaseNote.getRecommendedRouteType()).isNull();
        assertThat(releaseNote.getRecommendedRouteTitle()).isNull();
        assertThat(releaseNote.getManualSupported()).isNull();
    }

    @Test
    void shouldSearchHelpArticlesByKeyword() {
        List<ContentHelpSearchResultVO> results = supportService.searchHelpArticles("u1", "账号");

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.getTitle().contains("账号安全") || r.getDescription().contains("账号"));
    }

    @Test
    void shouldReturnEmptyListWhenNoHelpArticleMatched() {
        List<ContentHelpSearchResultVO> results = supportService.searchHelpArticles("u1", "xyznonexistent");

        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenHelpSearchKeywordIsBlank() {
        List<ContentHelpSearchResultVO> results = supportService.searchHelpArticles("u1", "  ");

        assertThat(results).isEmpty();
    }

    @Test
    void shouldCreateServiceSession() {
        String sessionId = supportService.createServiceSession("u1", "SMART_BOT");

        assertThat(sessionId).isNotBlank();
        verify(serviceSessionMapper).insert(argThat((ContentCustomerServiceSession it) ->
            "u1".equals(it.getUserId())
                && "SMART_BOT".equals(it.getSessionType())
                && "ACTIVE".equals(it.getStatus())));
    }

    @Test
    void shouldListServiceSessionsForUser() {
        ContentCustomerServiceSession session = new ContentCustomerServiceSession();
        session.setId("session-1");
        session.setUserId("u1");
        session.setSessionType("SMART_BOT");
        session.setStatus("CLOSED");
        session.setRating(5);
        session.setStartTime(new Date());
        session.setCreateTime(new Date());
        when(serviceSessionMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentCustomerServiceSession> page = invocation.getArgument(0);
            page.setRecords(List.of(session));
            page.setTotal(1L);
            return page;
        });

        ContentServiceSessionPageVO result = supportService.listServiceSessions(
            new ContentServiceSessionQueryReq().setUserId("u1").setPageNo(1L).setPageSize(10L));

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getSessionId()).isEqualTo("session-1");
        assertThat(result.getRecords().get(0).getExpired()).isFalse();
    }

    @Test
    void shouldRateClosedServiceSession() {
        ContentCustomerServiceSession session = new ContentCustomerServiceSession();
        session.setId("session-1");
        session.setUserId("u1");
        session.setStatus("CLOSED");
        when(serviceSessionMapper.selectById("session-1")).thenReturn(session);

        String result = supportService.rateService("u1", "session-1", 5, "很好");

        assertThat(result).isEqualTo("session-1");
        verify(serviceSessionMapper).updateById(argThat((ContentCustomerServiceSession it) ->
            it.getRating() == 5 && "很好".equals(it.getRatingComment())));
    }

    @Test
    void shouldRejectRatingWhenSessionNotClosed() {
        ContentCustomerServiceSession session = new ContentCustomerServiceSession();
        session.setId("session-1");
        session.setUserId("u1");
        session.setStatus("ACTIVE");
        when(serviceSessionMapper.selectById("session-1")).thenReturn(session);

        assertThatThrownBy(() -> supportService.rateService("u1", "session-1", 5, "很好"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("仅已结束的会话可以评分");
    }

    @Test
    void shouldRejectRatingWhenRatingOutOfRange() {
        ContentCustomerServiceSession session = new ContentCustomerServiceSession();
        session.setId("session-1");
        session.setUserId("u1");
        session.setStatus("CLOSED");
        when(serviceSessionMapper.selectById("session-1")).thenReturn(session);

        assertThatThrownBy(() -> supportService.rateService("u1", "session-1", 6, "好评"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("评分范围为 1-5");
    }

    @Test
    void shouldMarkExpiredWhenSessionOlderThan30Days() {
        ContentCustomerServiceSession session = new ContentCustomerServiceSession();
        session.setId("session-old");
        session.setUserId("u1");
        session.setSessionType("SMART_BOT");
        session.setStatus("CLOSED");
        session.setCreateTime(new Date(System.currentTimeMillis() - 31L * 24 * 60 * 60 * 1000));
        session.setStartTime(new Date(System.currentTimeMillis() - 31L * 24 * 60 * 60 * 1000));
        when(serviceSessionMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentCustomerServiceSession> page = invocation.getArgument(0);
            page.setRecords(List.of(session));
            page.setTotal(1L);
            return page;
        });

        ContentServiceSessionPageVO result = supportService.listServiceSessions(
            new ContentServiceSessionQueryReq().setUserId("u1").setPageNo(1L).setPageSize(10L));

        assertThat(result.getRecords().get(0).getExpired()).isTrue();
    }

    @Test
    void shouldReturnDefaultCustomerServiceEntry() {
        when(profileMapper.selectByUserId("u1")).thenReturn(null);

        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

        assertThat(result.getRouteType()).isEqualTo("SMART_FIRST");
        assertThat(result.getManualSupported()).isTrue();
    }

    @Test
    void shouldReturnManualPriorityCustomerServiceEntryWhenUserIsHighLevel() {
        when(profileMapper.selectByUserId("u100"))
            .thenReturn(new ContentUserProfile()
                .setUserId("u100")
                .setLevel(15)
                .setGrowthValue(420)
                .setStatus("NORMAL"));

        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u100");

        assertThat(result.getRouteType()).isEqualTo("MANUAL_PRIORITY");
        assertThat(result.getTitle()).isEqualTo("专属客服");
        assertThat(result.getDescription()).isEqualTo("高等级用户优先进入人工客服通道");
        assertThat(result.getManualSupported()).isTrue();
    }

    @Test
    void shouldRouteToManualPriorityWhenExplicitBenefitEnabled() {
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setStatus("NORMAL")
            .setLevel(1)
            .setGrowthValue(0));
        when(levelBenefitService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(true);

        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

        assertThat(result.getRouteType()).isEqualTo("MANUAL_PRIORITY");
        assertThat(result.getTitle()).isEqualTo("专属客服");
        verify(levelBenefitService).hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE");
    }

    @Test
    void shouldNotFallbackToHighLevelRuleWhenBenefitExplicitlyDisabled() {
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setStatus("NORMAL")
            .setLevel(6)
            .setGrowthValue(600));
        when(levelBenefitService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(false);
        when(levelBenefitService.isBenefitExplicitlyDisabled("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(true);

        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

        assertThat(result.getRouteType()).isEqualTo("SMART_FIRST");
        verify(levelBenefitService).isBenefitExplicitlyDisabled("u1", "PRIORITY_CUSTOMER_SERVICE");
    }

    @Test
    void shouldStillFallbackToLevelRuleWhenBenefitRecordMissing() {
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
            .setUserId("u1")
            .setStatus("NORMAL")
            .setLevel(15)
            .setGrowthValue(300));
        when(levelBenefitService.isBenefitExplicitlyDisabled("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(false);

        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

        assertThat(result.getRouteType()).isEqualTo("MANUAL_PRIORITY");
    }
    void shouldReturnGovernancePriorityCustomerServiceEntryWhenUserStatusIsRestricted() {
        when(profileMapper.selectByUserId("u200"))
            .thenReturn(new ContentUserProfile()
                .setUserId("u200")
                .setLevel(6)
                .setGrowthValue(600)
                .setStatus("FROZEN"));

        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u200");

        assertThat(result.getRouteType()).isEqualTo("APPEAL_PRIORITY");
        assertThat(result.getTitle()).isEqualTo("治理申诉专线");
        assertThat(result.getDescription()).isEqualTo("当前账号状态异常，优先进入申诉与人工复核通道");
        assertThat(result.getManualSupported()).isTrue();
    }

    private ContentAppealCreateReq createAppealReq() {
        return new ContentAppealCreateReq()
            .setUserId("u1")
            .setAppealType("PENALTY")
            .setTargetId("penalty_1")
            .setTargetType("STATUS_RECORD")
            .setReason("需要复核")
            .setEvidenceJson("{\"proof\":true}");
    }

    private ContentReportCreateReq createReportReq() {
        return new ContentReportCreateReq()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setReason("垃圾内容")
            .setEvidenceJson("{\"screenshot\":true}");
    }

    private ContentAppealHandleReq createHandleReq() {
        return new ContentAppealHandleReq()
            .setAppealId("appeal-1")
            .setOperatorUserId("admin-1")
            .setStatus("RESOLVED")
            .setResultStatus("APPROVED")
            .setResultNote("处罚撤销")
            .setProgressNote("已处理完成");
    }

    private ContentReportHandleReq createHandleReportReq() {
        return new ContentReportHandleReq()
            .setReportId("report-1")
            .setOperatorUserId("admin-1")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResultNote("违规成立")
            .setProgressNote("已处理完成");
    }

    @Test
    void shouldListReportsForUserWithPagination() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("c1")
            .setReportType("SPAM")
            .setReason("垃圾内容")
            .setStatus("PENDING");
        report.setId("r1");
        report.setCreateTime(new Date());

        IPage<ContentUserReport> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        page.setRecords(List.of(report));
        page.setTotal(1);
        when(reportMapper.selectPage(any(), any())).thenReturn(page);

        var result = supportService.listReportsForUser("u1", 1L, 10L);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getReportId()).isEqualTo("r1");
        assertThat(result.getRecords().get(0).getStatusLabel()).isEqualTo("待处理");
    }

    @Test
    void shouldReturnEmptyPageWhenNoReportsForUser() {
        IPage<ContentUserReport> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(reportMapper.selectPage(any(), any())).thenReturn(page);

        var result = supportService.listReportsForUser("u1", 1L, 10L);

        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void shouldGetReportDetailForUser() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("c1")
            .setReportType("SPAM")
            .setReason("垃圾内容")
            .setStatus("PENDING");
        report.setId("r1");
        report.setCreateTime(new Date());
        when(reportMapper.selectById("r1")).thenReturn(report);

        var result = supportService.getReportDetailForUser("u1", "r1");

        assertThat(result.getReportId()).isEqualTo("r1");
        assertThat(result.getReportTypeLabel()).isEqualTo("垃圾内容");
        assertThat(result.getStatusLabel()).isEqualTo("待处理");
    }

    @Test
    void shouldRejectReportDetailQueryWhenUserDoesNotOwnReport() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("other-user")
            .setTargetType("CONTENT")
            .setTargetId("c1")
            .setReportType("SPAM")
            .setReason("垃圾内容")
            .setStatus("PENDING");
        report.setId("r1");
        report.setCreateTime(new Date());
        when(reportMapper.selectById("r1")).thenReturn(report);

        assertThatThrownBy(() -> supportService.getReportDetailForUser("u1", "r1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报不存在或无权查看");
    }

    @Test
    void shouldGetAppealDetail() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setAppealType("BAN")
            .setTargetType("STATUS_RECORD")
            .setTargetId("rec1")
            .setReason("误判申诉")
            .setStatus("PENDING");
        appeal.setId("a1");
        appeal.setCreateTime(new Date());
        when(appealMapper.selectById("a1")).thenReturn(appeal);

        var result = supportService.getAppealDetail("u1", "a1");

        assertThat(result.getAppealId()).isEqualTo("a1");
        assertThat(result.getAppealType()).isEqualTo("BAN");
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldRejectAppealDetailQueryWhenUserDoesNotOwnAppeal() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("other-user")
            .setAppealType("BAN")
            .setTargetType("STATUS_RECORD")
            .setTargetId("rec1")
            .setReason("误判申诉")
            .setStatus("PENDING");
        appeal.setId("a1");
        appeal.setCreateTime(new Date());
        when(appealMapper.selectById("a1")).thenReturn(appeal);

        assertThatThrownBy(() -> supportService.getAppealDetail("u1", "a1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("申诉不存在或无权查看");
    }

    @Test
    void shouldWithdrawPendingReport() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("PENDING")
            .setReportType("SPAM");
        report.setId("r1");
        when(reportMapper.selectById("r1")).thenReturn(report);

        String result = supportService.withdrawReport("u1", "r1");

        assertThat(result).isEqualTo("r1");
        verify(reportMapper).updateById(argThat((ContentUserReport it) -> "WITHDRAWN".equals(it.getStatus())));
    }

    @Test
    void shouldRejectWithdrawReportWhenNotOwner() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("other-user")
            .setStatus("PENDING")
            .setReportType("SPAM");
        report.setId("r1");
        when(reportMapper.selectById("r1")).thenReturn(report);

        assertThatThrownBy(() -> supportService.withdrawReport("u1", "r1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报不存在或无权操作");
    }

    @Test
    void shouldRejectWithdrawReportWhenNotPending() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setStatus("RESOLVED")
            .setReportType("SPAM");
        report.setId("r1");
        when(reportMapper.selectById("r1")).thenReturn(report);

        assertThatThrownBy(() -> supportService.withdrawReport("u1", "r1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("仅待处理状态的举报可撤回");
    }

    @Test
    void shouldWithdrawPendingAppeal() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PENDING")
            .setAppealType("BAN");
        appeal.setId("a1");
        when(appealMapper.selectById("a1")).thenReturn(appeal);

        String result = supportService.withdrawAppeal("u1", "a1");

        assertThat(result).isEqualTo("a1");
        verify(appealMapper).updateById(argThat((ContentUserAppeal it) -> "WITHDRAWN".equals(it.getStatus())));
    }

    @Test
    void shouldWithdrawProcessingAppeal() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("PROCESSING")
            .setAppealType("BAN");
        appeal.setId("a1");
        when(appealMapper.selectById("a1")).thenReturn(appeal);

        String result = supportService.withdrawAppeal("u1", "a1");

        assertThat(result).isEqualTo("a1");
        verify(appealMapper).updateById(argThat((ContentUserAppeal it) -> "WITHDRAWN".equals(it.getStatus())));
    }

    @Test
    void shouldRejectWithdrawAppealWhenNotOwner() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("other-user")
            .setStatus("PENDING")
            .setAppealType("BAN");
        appeal.setId("a1");
        when(appealMapper.selectById("a1")).thenReturn(appeal);

        assertThatThrownBy(() -> supportService.withdrawAppeal("u1", "a1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("申诉不存在或无权操作");
    }

    @Test
    void shouldRejectWithdrawAppealWhenResolved() {
        ContentUserAppeal appeal = new ContentUserAppeal()
            .setUserId("u1")
            .setStatus("RESOLVED")
            .setAppealType("BAN");
        appeal.setId("a1");
        when(appealMapper.selectById("a1")).thenReturn(appeal);

        assertThatThrownBy(() -> supportService.withdrawAppeal("u1", "a1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("仅待处理或处理中的申诉可撤回");
    }

    @Test
    void shouldReturnHelpCategories() {
        when(profileMapper.selectByUserId("u1")).thenReturn(null);

        List<ContentHelpCenterEntryVO> result = supportService.getHelpCategories("u1");

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(e -> "ACCOUNT_SECURITY".equals(e.getCode()));
        assertThat(result).anyMatch(e -> "REPORT_APPEAL".equals(e.getCode()));
        assertThat(result).anyMatch(e -> "PRIVACY_SETTINGS".equals(e.getCode()));
    }

    @Test
    void shouldGetHelpArticleDetailByCode() {
        when(profileMapper.selectByUserId("u1")).thenReturn(null);

        ContentHelpSearchResultVO result = supportService.getHelpArticleDetail("u1", "ACCOUNT_SECURITY");

        assertThat(result.getCode()).isEqualTo("ACCOUNT_SECURITY");
        assertThat(result.getTitle()).isEqualTo("账号安全");
        assertThat(result.getDescription()).isEqualTo("账号登录、密码与设备安全相关问题");
    }

    @Test
    void shouldRejectArticleDetailWhenCodeNotFound() {
        when(profileMapper.selectByUserId("u1")).thenReturn(null);

        assertThatThrownBy(() -> supportService.getHelpArticleDetail("u1", "NONEXISTENT"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("文章不存在");
    }

    @Test
    void shouldSubmitArticleFeedback() {
        String result = supportService.submitArticleFeedback("u1", "ACCOUNT_SECURITY", true);

        assertThat(result).isEqualTo("反馈已提交");
    }

    @Test
    void shouldReturnChangelogByVersionDescending() {
        List<ContentChangelogVO> result = supportService.getChangelog("user-1");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getVersion()).isEqualTo("3.2.0");
        assertThat(result.get(0).getAdditions()).contains("新增内容社区举报功能");
        assertThat(result.get(0).getImprovements()).contains("优化帮助中心搜索体验");
        assertThat(result.get(0).getFixes()).isNotEmpty();
        assertThat(result.get(1).getVersion()).isEqualTo("3.1.0");
        assertThat(result.get(2).getVersion()).isEqualTo("3.0.0");
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getReleaseDate()).isAfterOrEqualTo(result.get(i + 1).getReleaseDate());
        }
    }

    private ContentHelpCenterEntryVO findEntry(List<ContentHelpCenterEntryVO> entries, String code) {
        return entries.stream()
            .filter(entry -> code.equals(entry.getCode()))
            .findFirst()
            .orElseThrow();
    }
}
