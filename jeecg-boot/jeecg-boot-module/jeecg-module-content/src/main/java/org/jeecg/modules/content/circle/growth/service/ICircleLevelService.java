package org.jeecg.modules.content.circle.growth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.growth.entity.CircleLevel;
import org.jeecg.modules.content.circle.growth.vo.CircleLevelVO;

public interface ICircleLevelService extends IService<CircleLevel> {

    void calculateGrowthScore(String circleId);

    void updateLevel(String circleId);

    CircleLevelVO getLevelInfo(String circleId);

    /** 计算成长分并更新等级（单次DB查询，供定时任务使用） */
    void recalculateAndUpdateLevel(String circleId);
}
