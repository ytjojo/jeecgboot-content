package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;

import java.util.List;

/**
 * 圈子加入申请 Mapper 接口。
 */
@Mapper
public interface CircleJoinRequestMapper extends BaseMapper<CircleJoinRequest> {

    /**
     * 查询圈子待审核的加入申请，按创建时间升序排列。
     *
     * @param circleId 圈子ID
     * @return 待审核申请列表
     */
    List<CircleJoinRequest> selectPendingByCircleId(@Param("circleId") String circleId);

    /**
     * 查询超时未处理的加入申请（超过3天仍为PENDING状态）。
     *
     * @return 超时申请列表
     */
    List<CircleJoinRequest> selectTimedOutRequests();
}
