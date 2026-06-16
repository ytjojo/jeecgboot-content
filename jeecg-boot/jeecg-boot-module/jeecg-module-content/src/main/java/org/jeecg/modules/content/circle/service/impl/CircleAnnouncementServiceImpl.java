package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.mapper.CircleAnnouncementMapper;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 圈子公告服务实现。
 * 提供公告发布（含旧公告替换）和查询功能。
 */
@Service
public class CircleAnnouncementServiceImpl extends ServiceImpl<CircleAnnouncementMapper, CircleAnnouncement>
        implements ICircleAnnouncementService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(CircleAnnouncement announcement) {
        // 将该圈子旧的 ACTIVE 公告置为 INACTIVE
        lambdaUpdate()
                .eq(CircleAnnouncement::getCircleId, announcement.getCircleId())
                .eq(CircleAnnouncement::getStatus, "ACTIVE")
                .set(CircleAnnouncement::getStatus, "INACTIVE")
                .update();

        // 保存新公告
        announcement.setStatus("ACTIVE");
        save(announcement);
    }

    @Override
    public CircleAnnouncement getActiveByCircleId(String circleId) {
        return baseMapper.selectActiveByCircleId(circleId);
    }

    @Override
    public java.util.List<CircleAnnouncement> getHistoryByCircleId(String circleId) {
        return lambdaQuery()
                .eq(CircleAnnouncement::getCircleId, circleId)
                .eq(CircleAnnouncement::getStatus, "INACTIVE")
                .orderByDesc(CircleAnnouncement::getCreateTime)
                .list();
    }
}
