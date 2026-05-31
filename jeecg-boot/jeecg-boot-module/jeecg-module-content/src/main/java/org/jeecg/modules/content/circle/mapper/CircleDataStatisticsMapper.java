package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.content.circle.entity.CircleDataStatistics;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CircleDataStatisticsMapper extends BaseMapper<CircleDataStatistics> {

    @Select("SELECT * FROM circle_data_statistics WHERE circle_id = #{circleId} AND stat_date BETWEEN #{startDate} AND #{endDate} ORDER BY stat_date")
    List<CircleDataStatistics> selectByCircleIdAndDateRange(@Param("circleId") String circleId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);
}
