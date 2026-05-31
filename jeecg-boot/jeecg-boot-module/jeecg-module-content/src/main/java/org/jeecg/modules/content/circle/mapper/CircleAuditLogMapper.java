package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;

import java.util.Date;
import java.util.List;

/**
 * 圈子审核日志 Mapper 接口。
 */
@Mapper
public interface CircleAuditLogMapper extends BaseMapper<CircleAuditLog> {

    /**
     * 根据目标ID和目标类型查询审核日志
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 审核日志列表
     */
    List<CircleAuditLog> selectByTarget(@Param("targetId") String targetId, @Param("targetType") String targetType);

    /**
     * 根据时间范围查询审核日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审核日志列表
     */
    List<CircleAuditLog> selectByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
