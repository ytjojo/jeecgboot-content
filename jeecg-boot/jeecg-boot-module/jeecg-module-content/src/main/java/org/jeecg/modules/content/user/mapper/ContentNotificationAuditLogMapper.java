package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.user.entity.ContentNotificationAuditLog;

/**
 * 通知发送审计日志Mapper。
 */
@Mapper
public interface ContentNotificationAuditLogMapper extends BaseMapper<ContentNotificationAuditLog> {
}
