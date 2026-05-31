package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleReport;

import java.util.List;

/**
 * 圈子内容举报服务接口。
 */
public interface ICircleReportService extends IService<CircleReport> {

    /**
     * 提交举报。若同一用户对同一内容已举报过则抛出异常。
     *
     * @param report 举报对象
     */
    void submitReport(CircleReport report);

    /**
     * 处理举报：删除被举报内容。
     *
     * @param reportId   举报ID
     * @param operatorId 处理人ID
     */
    void handleDeleteContent(String reportId, String operatorId);

    /**
     * 处理举报：忽略。
     *
     * @param reportId   举报ID
     * @param operatorId 处理人ID
     */
    void handleIgnore(String reportId, String operatorId);

    /**
     * 处理举报：禁言被举报用户。
     *
     * @param reportId   举报ID
     * @param operatorId 处理人ID
     */
    void handleMute(String reportId, String operatorId);

    /**
     * 查询圈子举报列表，可按状态过滤。
     *
     * @param circleId 圈子ID
     * @param status   举报状态（可为null，返回全部）
     * @return 举报列表
     */
    List<CircleReport> getReports(String circleId, String status);
}
