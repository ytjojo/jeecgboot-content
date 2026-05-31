package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleReport;

import java.util.List;

/**
 * 圈子内容举报 Mapper 接口。
 */
@Mapper
public interface CircleReportMapper extends BaseMapper<CircleReport> {

    /**
     * 按圈子ID和状态查询举报列表，按创建时间降序排列。
     * status 为 null 时返回该圈子所有举报。
     *
     * @param circleId 圈子ID
     * @param status   举报状态（可为null）
     * @return 举报列表
     */
    List<CircleReport> selectByCircleAndStatus(@Param("circleId") String circleId,
                                                @Param("status") String status);
}
