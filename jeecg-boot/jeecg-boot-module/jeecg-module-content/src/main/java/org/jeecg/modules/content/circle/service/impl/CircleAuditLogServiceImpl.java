package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.mapper.CircleAuditLogMapper;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 圈子审核日志服务实现。
 * 提供审核日志写入和查询功能。
 */
@Service
public class CircleAuditLogServiceImpl extends ServiceImpl<CircleAuditLogMapper, CircleAuditLog>
        implements ICircleAuditLogService {

    @Override
    public void writeAuditLog(CircleAuditLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }
        save(log);
    }

    @Override
    public List<CircleAuditLog> queryByTarget(String targetId, String targetType) {
        return baseMapper.selectByTarget(targetId, targetType);
    }

    @Override
    public List<CircleAuditLog> queryByTimeRange(Date startTime, Date endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }
}
