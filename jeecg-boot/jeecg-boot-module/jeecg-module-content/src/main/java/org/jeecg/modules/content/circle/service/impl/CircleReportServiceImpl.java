package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleReportStatusEnum;
import org.jeecg.modules.content.circle.mapper.CircleReportMapper;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 圈子内容举报服务实现。
 */
@Service
public class CircleReportServiceImpl extends ServiceImpl<CircleReportMapper, CircleReport>
        implements ICircleReportService {

    @Resource
    private IContentNotificationService contentNotificationService;

    @Override
    public void submitReport(CircleReport report) {
        QueryWrapper<CircleReport> qw = new QueryWrapper<>();
        qw.eq("reporter_id", report.getReporterId());
        qw.eq("content_id", report.getContentId());
        long count = baseMapper.selectCount(qw);
        if (count > 0) {
            throw new IllegalArgumentException("您已举报过该内容，请勿重复举报");
        }
        report.setStatus(CircleReportStatusEnum.PENDING.getCode());
        save(report);
    }

    @Override
    public void handleDeleteContent(String reportId, String operatorId) {
        CircleReport report = getById(reportId);
        report.setStatus(CircleReportStatusEnum.RESOLVED.getCode());
        report.setHandleAction("DELETE");
        report.setOperatorId(operatorId);
        report.setOperateTime(new Date());
        updateById(report);

        contentNotificationService.sendNotification(
                report.getReporterId(), "REPORT_RESOLVED",
                "举报已处理", "你举报的内容已被删除");
    }

    @Override
    public void handleIgnore(String reportId, String operatorId) {
        CircleReport report = getById(reportId);
        report.setStatus(CircleReportStatusEnum.IGNORED.getCode());
        report.setHandleAction("IGNORE");
        report.setOperatorId(operatorId);
        report.setOperateTime(new Date());
        updateById(report);

        contentNotificationService.sendNotification(
                report.getReporterId(), "REPORT_IGNORED",
                "举报已审核", "你举报的内容经审核未违规");
    }

    @Override
    public void handleMute(String reportId, String operatorId) {
        CircleReport report = getById(reportId);
        report.setStatus(CircleReportStatusEnum.RESOLVED.getCode());
        report.setHandleAction("MUTE");
        report.setOperatorId(operatorId);
        report.setOperateTime(new Date());
        updateById(report);
        // TODO: 调用禁言服务对被举报用户执行禁言

        contentNotificationService.sendNotification(
                report.getReporterId(), "REPORT_RESOLVED",
                "举报已处理", "你举报的内容已处理，相关用户已被禁言");
    }

    @Override
    public List<CircleReport> getReports(String circleId, String status) {
        return baseMapper.selectByCircleAndStatus(circleId, status);
    }
}
