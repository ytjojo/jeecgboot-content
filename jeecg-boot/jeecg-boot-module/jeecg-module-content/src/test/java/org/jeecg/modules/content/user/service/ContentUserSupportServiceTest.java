package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserReportMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.impl.ContentUserSupportServiceImpl;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContentUserSupportServiceTest {

    @Mock
    private ContentUserAppealMapper appealMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @Mock
    private ContentUserReportMapper reportMapper;

    @InjectMocks
    private ContentUserSupportServiceImpl supportService;

    @Test
    void shouldCreateAppealAgainstPenaltyAndRecordProgress() {
        String appealId = supportService.createAppeal(createAppealReq());

        assertThat(appealId).isNotBlank();
        verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) -> "USER_APPEAL_CREATED".equals(it.getEventType())));
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
    void shouldListAppealsForUser() {
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
        when(appealMapper.selectByUserId("u1")).thenReturn(List.of(processingAppeal, pendingAppeal));

        List<ContentUserAppealProgressVO> result = supportService.listAppeals("u1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ContentUserAppealProgressVO::getAppealId)
            .containsExactly("appeal-1", "appeal-2");
        assertThat(result.get(0).getResolvedAt()).isEqualTo(resolvedAt);
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
    void shouldReturnHelpCenterMetadata() {
        ContentHelpCenterVO result = supportService.getHelpCenter();

        assertThat(result.getFaqCategories()).contains("账号安全", "举报申诉", "隐私设置");
        assertThat(result.getGuideEntries()).contains("新手指南", "社区规范", "功能使用说明");
        assertThat(result.getReleaseNotes()).contains("产品更新", "功能上新", "规则公告");
    }

    @Test
    void shouldReturnDefaultCustomerServiceEntry() {
        ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

        assertThat(result.getRouteType()).isEqualTo("SMART_FIRST");
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
}
