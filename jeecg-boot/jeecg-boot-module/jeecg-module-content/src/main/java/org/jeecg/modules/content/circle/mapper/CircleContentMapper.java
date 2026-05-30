package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleContent;

import java.util.List;

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
}
