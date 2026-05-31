package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.springframework.stereotype.Service;

@Service
public class CircleServiceImpl extends ServiceImpl<CircleMapper, Circle> implements ICircleService {

    @Override
    public void checkNameUnique(String name) {
        long count = count(new LambdaQueryWrapper<Circle>().eq(Circle::getName, name));
        if (count > 0) {
            throw new JeecgBootException("该圈子名称已存在，请修改");
        }
    }

    @Override
    public void incrementMemberCount(String circleId) {
        int rows = baseMapper.incrementMemberCount(circleId);
        if (rows == 0) {
            throw new JeecgBootException("圈子已满员，无法加入");
        }
    }

    @Override
    public void decrementMemberCount(String circleId) {
        baseMapper.decrementMemberCount(circleId);
    }
}
