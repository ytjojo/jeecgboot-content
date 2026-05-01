package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserReportMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Service implementation for content user support.
 */
@Service
public class ContentUserSupportServiceImpl implements IContentUserSupportService {

    @Resource
    private ContentUserAppealMapper appealMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private ContentUserReportMapper reportMapper;

    /**
     * Creates a user appeal record and returns its identifier.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAppeal(ContentAppealCreateReq req) {
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
    public List<ContentUserAppealProgressVO> listAppeals(String userId) {
        return appealMapper.selectByUserId(userId).stream()
            .map(this::toAppealProgress)
            .toList();
    }

    /**
     * Creates a user report record and returns its identifier.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createReport(ContentReportCreateReq req) {
        ContentUserReport report = ContentUserReport.from(req);
        report.setId(UUIDGenerator.generate());
        reportMapper.insert(report);
        auditLogMapper.insert(ContentUserAuditLog.reportCreated(report.getId(), req));
        return report.getId();
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
     * Returns module-local help center metadata.
     */
    @Override
    public ContentHelpCenterVO getHelpCenter() {
        return new ContentHelpCenterVO()
            .setFaqCategories(List.of("账号安全", "举报申诉", "隐私设置"))
            .setGuideEntries(List.of("新手指南", "社区规范", "功能使用说明"))
            .setReleaseNotes(List.of("产品更新", "功能上新", "规则公告"));
    }

    /**
     * Returns the default customer service routing entry.
     */
    @Override
    public ContentCustomerServiceVO getCustomerServiceEntry(String userId) {
        return new ContentCustomerServiceVO()
            .setRouteType("SMART_FIRST")
            .setTitle("在线客服")
            .setDescription("优先进入智能客服，可转人工处理")
            .setManualSupported(Boolean.TRUE);
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
        appeal.setStatus(req.getStatus());
        appeal.setResultStatus(req.getResultStatus());
        appeal.setResultNote(req.getResultNote());
        appeal.setProgressNote(req.getProgressNote());
        appeal.setResolvedBy(req.getOperatorUserId());
        appeal.setResolvedAt(new Date());
        appealMapper.updateById(appeal);
        auditLogMapper.insert(ContentUserAuditLog.appealHandled(appeal, req));
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
        report.setStatus(req.getStatus());
        report.setResultStatus(req.getResultStatus());
        report.setResultNote(req.getResultNote());
        report.setProgressNote(req.getProgressNote());
        report.setResolvedBy(req.getOperatorUserId());
        report.setResolvedAt(new Date());
        reportMapper.updateById(report);
        auditLogMapper.insert(ContentUserAuditLog.reportHandled(report, req));
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
}
