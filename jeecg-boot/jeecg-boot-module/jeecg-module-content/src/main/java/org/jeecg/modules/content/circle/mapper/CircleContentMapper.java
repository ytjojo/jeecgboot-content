package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleContent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 圈子内容 Mapper 接口。
 */
@Mapper
public interface CircleContentMapper extends BaseMapper<CircleContent> {

    /**
     * 查询圈子内容列表，按置顶状态、置顶时间、创建时间排序。
     *
     * @param circleId 圈子ID
     * @return 内容列表
     */
    List<CircleContent> selectCircleContentList(@Param("circleId") String circleId);

    /**
     * 聚合查询所有圈子的帖子统计
     * 返回 List of Map: {circle_id, post_count, new_post_count}
     */
    List<Map<String, Object>> selectPostStatsGroupByCircle(@Param("todayStart") LocalDateTime todayStart);

    /**
     * 聚合查询所有圈子的活跃用户统计（近30天有发帖/评论的用户数）
     * 返回 List of Map: {circle_id, active_count}
     */
    List<Map<String, Object>> selectActiveUserStatsGroupByCircle(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}
