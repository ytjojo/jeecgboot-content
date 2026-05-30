package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;

/**
 * 圈子公告 Mapper 接口。
 */
@Mapper
public interface CircleAnnouncementMapper extends BaseMapper<CircleAnnouncement> {

    /**
     * 查询圈子当前有效的公告（未过期且状态为ACTIVE）
     *
     * @param circleId 圈子ID
     * @return 有效公告，无则返回null
     */
    CircleAnnouncement selectActiveByCircleId(@Param("circleId") String circleId);
}
