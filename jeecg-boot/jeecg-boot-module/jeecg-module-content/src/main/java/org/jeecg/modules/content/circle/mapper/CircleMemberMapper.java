package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleMember;

import java.util.List;

/**
 * 圈子成员 Mapper 接口。
 */
@Mapper
public interface CircleMemberMapper extends BaseMapper<CircleMember> {

    /**
     * 查询圈子所有活跃成员的用户ID。
     *
     * @param circleId 圈子ID
     * @return 活跃成员用户ID列表
     */
    List<String> selectMemberUserIds(@Param("circleId") String circleId);
}
