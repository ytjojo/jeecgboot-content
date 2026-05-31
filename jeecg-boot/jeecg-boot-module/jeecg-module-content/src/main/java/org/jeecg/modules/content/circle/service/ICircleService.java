package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.Circle;

public interface ICircleService extends IService<Circle> {

    void checkNameUnique(String name);

    void incrementMemberCount(String circleId);

    void decrementMemberCount(String circleId);
}
