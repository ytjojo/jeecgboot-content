package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;

/**
 * 圈子公告服务接口。
 * 提供公告发布、替换和查询功能。
 */
public interface ICircleAnnouncementService extends IService<CircleAnnouncement> {

    /**
     * 发布公告。
     * 会先将该圈子旧的 ACTIVE 公告置为 INACTIVE，再保存新公告（状态为 ACTIVE）。
     *
     * @param announcement 公告对象（circleId 和 content 必填）
     */
    void publish(CircleAnnouncement announcement);

    /**
     * 查询圈子当前有效的公告
     *
     * @param circleId 圈子ID
     * @return 有效公告，无则返回null
     */
    CircleAnnouncement getActiveByCircleId(String circleId);

    /**
     * 查询圈子历史公告（状态为 INACTIVE），按创建时间倒序
     *
     * @param circleId 圈子ID
     * @return 历史公告列表
     */
    java.util.List<CircleAnnouncement> getHistoryByCircleId(String circleId);
}
