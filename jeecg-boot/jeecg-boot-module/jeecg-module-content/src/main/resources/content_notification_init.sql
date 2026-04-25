-- =============================================
-- 内容社区系统 - 通知模块数据库初始化脚本
-- 数据库类型为mysql 版本8.0+
-- =============================================

START TRANSACTION;
-- 通知表
CREATE TABLE notifications (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type ENUM('SYSTEM', 'INTERACTION', 'CONTENT', 'QA', 'AUDIT', 'ACTIVITY') NOT NULL COMMENT '通知类型：SYSTEM-系统通知，INTERACTION-互动通知，CONTENT-内容通知，QA-问答通知，AUDIT-审核通知，ACTIVITY-活动通知',
    sub_type VARCHAR(50) COMMENT '具体的通知子类型',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    data JSON COMMENT '通知相关的数据',
    action_url VARCHAR(500) COMMENT '点击通知后的跳转链接',
    priority ENUM('HIGH', 'NORMAL', 'LOW') DEFAULT 'NORMAL' COMMENT '通知优先级：HIGH-高，NORMAL-普通，LOW-低',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读：0-未读，1-已读',
    del_flag TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_del_flag (del_flag),
    INDEX idx_created_at (created_at),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 通知设置表
CREATE TABLE notification_settings (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '设置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    email_notification BOOLEAN DEFAULT TRUE COMMENT '邮件通知开关：0-关闭，1-开启',
    push_notification BOOLEAN DEFAULT TRUE COMMENT '推送通知开关：0-关闭，1-开启',
    sms_notification BOOLEAN DEFAULT FALSE COMMENT '短信通知开关：0-关闭，1-开启',
    interaction_notification BOOLEAN DEFAULT TRUE COMMENT '互动通知开关：0-关闭，1-开启',
    content_notification BOOLEAN DEFAULT TRUE COMMENT '内容通知开关：0-关闭，1-开启',
    qa_notification BOOLEAN DEFAULT TRUE COMMENT '问答通知开关：0-关闭，1-开启',
    system_notification BOOLEAN DEFAULT TRUE COMMENT '系统通知开关：0-关闭，1-开启',
    audit_notification BOOLEAN DEFAULT TRUE COMMENT '审核通知开关：0-关闭，1-开启',
    activity_notification BOOLEAN DEFAULT TRUE COMMENT '活动通知开关：0-关闭，1-开启',
    quiet_hours_start TIME COMMENT '免打扰开始时间',
    quiet_hours_end TIME COMMENT '免打扰结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知设置表';

-- 通知模板表
CREATE TABLE notification_templates (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '模板ID',
    type ENUM('SYSTEM', 'INTERACTION', 'CONTENT', 'QA', 'AUDIT', 'ACTIVITY') NOT NULL COMMENT '通知类型：SYSTEM-系统通知，INTERACTION-互动通知，CONTENT-内容通知，QA-问答通知，AUDIT-审核通知，ACTIVITY-活动通知',
    sub_type VARCHAR(50) NOT NULL COMMENT '通知子类型',
    title_template VARCHAR(200) NOT NULL COMMENT '标题模板',
    content_template TEXT NOT NULL COMMENT '内容模板',
    action_url_template VARCHAR(500) COMMENT '跳转链接模板',
    priority ENUM('HIGH', 'NORMAL', 'LOW') DEFAULT 'NORMAL' COMMENT '通知优先级：HIGH-高，NORMAL-普通，LOW-低',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用：0-禁用，1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_subtype (type, sub_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- WebSocket连接表
CREATE TABLE websocket_connections (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '连接ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    server_node VARCHAR(50) COMMENT '服务器节点标识',
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后心跳时间',
    status ENUM('CONNECTED', 'DISCONNECTED') DEFAULT 'CONNECTED' COMMENT '连接状态：CONNECTED-已连接，DISCONNECTED-已断开',
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_server_node (server_node),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WebSocket连接表';

-- 推送设备表
CREATE TABLE push_devices (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '设备ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_token VARCHAR(200) NOT NULL COMMENT '设备令牌',
    device_type ENUM('IOS', 'ANDROID', 'WEB') NOT NULL COMMENT '设备类型：IOS-苹果设备，ANDROID-安卓设备，WEB-网页端',
    device_info JSON COMMENT '设备信息',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活：0-未激活，1-已激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_device_token (device_token),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推送设备表';

-- =============================================
-- 初始化通知模板数据
-- =============================================

-- -- 系统通知模板
-- INSERT INTO notification_templates (type, sub_type, title_template, content_template, action_url_template, priority) VALUES
-- ('SYSTEM', 'ANNOUNCEMENT', '系统公告', '{content}', '/system/announcement/{id}', 'HIGH'),
-- ('SYSTEM', 'MAINTENANCE', '系统维护通知', '系统将于{time}进行维护，预计维护时间{duration}', '/system/maintenance', 'HIGH'),
-- ('SYSTEM', 'POLICY_UPDATE', '政策更新通知', '平台政策已更新，请及时查看', '/system/policy', 'NORMAL');

-- -- 互动通知模板
-- INSERT INTO notification_templates (type, sub_type, title_template, content_template, action_url_template, priority) VALUES
-- ('INTERACTION', 'LIKE', '点赞通知', '{username}赞了你的{contentType}', '/content/{contentId}', 'NORMAL'),
-- ('INTERACTION', 'DISLIKE', '反对通知', '{username}反对了你的{contentType}', '/content/{contentId}', 'NORMAL'),
-- ('INTERACTION', 'COMMENT', '评论通知', '{username}评论了你的{contentType}', '/content/{contentId}', 'NORMAL'),
-- ('INTERACTION', 'FOLLOW', '关注通知', '{username}关注了你', '/user/{userId}', 'NORMAL'),
-- ('INTERACTION', 'MENTION', '提及通知', '{username}在{contentType}中提到了你', '/content/{contentId}', 'NORMAL');

-- -- 内容通知模板
-- INSERT INTO notification_templates (type, sub_type, title_template, content_template, action_url_template, priority) VALUES
-- ('CONTENT', 'NEW_CONTENT', '新内容通知', '{username}发布了新的{contentType}', '/content/{contentId}', 'NORMAL'),
-- ('CONTENT', 'CHANNEL_UPDATE', '频道更新通知', '你关注的频道{channelName}有新内容', '/channel/{channelId}', 'NORMAL');

-- -- 问答通知模板
-- INSERT INTO notification_templates (type, sub_type, title_template, content_template, action_url_template, priority) VALUES
-- ('QA', 'NEW_ANSWER', '新回答通知', '{username}回答了你的问题', '/qa/{questionId}', 'NORMAL'),
-- ('QA', 'ANSWER_ADOPTED', '答案被采纳通知', '你的回答被{username}采纳为最佳答案', '/qa/{questionId}', 'NORMAL'),
-- ('QA', 'ANSWER_LIKED', '回答被赞通知', '{username}赞了你的回答', '/qa/{questionId}', 'NORMAL'),
-- ('QA', 'ANSWER_DISLIKED', '回答被反对通知', '{username}反对了你的回答', '/qa/{questionId}', 'NORMAL');

-- -- 审核通知模板
-- INSERT INTO notification_templates (type, sub_type, title_template, content_template, action_url_template, priority) VALUES
-- ('AUDIT', 'CONTENT_APPROVED', '内容审核通过', '你的{contentType}已通过审核', '/content/{contentId}', 'NORMAL'),
-- ('AUDIT', 'CONTENT_REJECTED', '内容审核未通过', '你的{contentType}未通过审核，原因：{reason}', '/content/{contentId}', 'HIGH'),
-- ('AUDIT', 'CONTENT_NEED_MODIFY', '内容需要修改', '你的{contentType}需要修改，请查看详情', '/content/{contentId}', 'NORMAL');

-- -- 活动通知模板
-- INSERT INTO notification_templates (type, sub_type, title_template, content_template, action_url_template, priority) VALUES
-- ('ACTIVITY', 'POINTS_CHANGE', '积分变化通知', '你的积分发生变化：{change}，当前积分：{total}', '/user/points', 'NORMAL'),
-- ('ACTIVITY', 'LEVEL_UP', '等级提升通知', '恭喜你升级到{level}级！', '/user/profile', 'NORMAL'),
-- ('ACTIVITY', 'BADGE_EARNED', '徽章获得通知', '恭喜你获得徽章：{badgeName}', '/user/badges', 'NORMAL');

-- -- =============================================
-- -- 初始化字典数据
-- -- =============================================

-- -- 通知类型字典
-- -- 通知类型字典
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, tenant_id) VALUES
-- ('notification_type_dict', '通知类型', 'notification_type', '通知类型字典', 0, 'admin', NOW(), 0);

-- -- 通知类型字典项
-- ('notification_type_system', 'notification_type_dict', '系统通知', '1', 'SYSTEM', 1, 1, 'admin', NOW()),
-- ('notification_type_interaction', 'notification_type_dict', '互动通知', '2', 'INTERACTION', 2, 1, 'admin', NOW()),
-- ('notification_type_content', 'notification_type_dict', '内容通知', '3', 'CONTENT', 3, 1, 'admin', NOW()),
-- ('notification_type_qa', 'notification_type_dict', '问答通知', '4', 'QA', 4, 1, 'admin', NOW()),
-- ('notification_type_audit', 'notification_type_dict', '审核通知', '5', 'AUDIT', 5, 1, 'admin', NOW()),
-- ('notification_type_activity', 'notification_type_dict', '活动通知', '6', 'ACTIVITY', 6, 1, 'admin', NOW()),
-- ('notification_type_dislike', 'notification_type_dict', '反对通知', '7', 'DISLIKE', 7, 1, 'admin', NOW());

-- -- 通知优先级字典
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, tenant_id) VALUES
-- ('notification_priority_dict', '通知优先级', 'notification_priority', '通知优先级字典', 0, 'admin', NOW(), 0);

-- -- 通知优先级字典项
-- ('notification_priority_high', 'notification_priority_dict', '高优先级', '1', 'HIGH', 1, 1, 'admin', NOW()),
-- ('notification_priority_normal', 'notification_priority_dict', '普通优先级', '2', 'NORMAL', 2, 1, 'admin', NOW()),
-- ('notification_priority_low', 'notification_priority_dict', '低优先级', '3', 'LOW', 3, 1, 'admin', NOW());

-- -- 通知子类型字典
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, tenant_id) VALUES
-- ('notification_sub_type_dict', '通知子类型', 'notification_sub_type', '通知子类型字典', 0, 'admin', NOW(), 0);

-- -- 通知子类型字典项
-- ('notification_sub_type_like', 'notification_sub_type_dict', '点赞', 'LIKE', '点赞通知', 1, 1, 'admin', NOW()),
-- ('notification_sub_type_dislike', 'notification_sub_type_dict', '反对', 'DISLIKE', '反对通知', 2, 1, 'admin', NOW()),
-- ('notification_sub_type_comment', 'notification_sub_type_dict', '评论', 'COMMENT', '评论通知', 3, 1, 'admin', NOW()),
-- ('notification_sub_type_follow', 'notification_sub_type_dict', '关注', 'FOLLOW', '关注通知', 4, 1, 'admin', NOW()),
-- ('notification_sub_type_mention', 'notification_sub_type_dict', '提及', 'MENTION', '提及通知', 5, 1, 'admin', NOW());

-- -- 设备类型字典
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, tenant_id) VALUES
-- ('device_type_dict', '设备类型', 'device_type', '推送设备类型字典', 0, 'admin', NOW(), 0);

-- -- 设备类型字典项
-- ('device_type_ios', 'device_type_dict', 'iOS设备', '1', 'IOS', 1, 1, 'admin', NOW()),
-- ('device_type_android', 'device_type_dict', 'Android设备', '2', 'ANDROID', 2, 1, 'admin', NOW()),
-- ('device_type_web', 'device_type_dict', 'Web端', '3', 'WEB', 3, 1, 'admin', NOW());

-- -- 连接状态字典
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, tenant_id) VALUES
-- ('connection_status_dict', '连接状态', 'connection_status', 'WebSocket连接状态字典', 0, 'admin', NOW(), 0);

-- -- 连接状态字典项
-- ('connection_status_connected', 'connection_status_dict', '已连接', '1', 'CONNECTED', 1, 1, 'admin', NOW()),
-- ('connection_status_disconnected', 'connection_status_dict', '已断开', '2', 'DISCONNECTED', 2, 1, 'admin', NOW());


COMMIT;

-- =============================================
-- 表结构说明
-- =============================================

/*
通知模块包含以下5个核心表：

1. notifications（通知表）
   - 存储所有类型的通知信息
   - 支持系统通知、互动通知、内容通知、问答通知、审核通知、活动通知等多种类型
   - 包含通知标题、内容、相关数据、跳转链接等完整信息
   - 支持优先级设置和已读状态管理

2. notification_settings（通知设置表）
   - 管理用户的通知偏好设置
   - 支持邮件、推送、短信等多种通知方式的开关控制
   - 支持按通知类型进行细粒度的开关设置
   - 支持免打扰时间段设置

3. notification_templates（通知模板表）
   - 存储各种通知类型的消息模板
   - 支持标题模板、内容模板、跳转链接模板
   - 支持模板变量替换，提高通知内容的动态性
   - 支持模板的启用/禁用状态管理

4. websocket_connections（WebSocket连接表）
   - 管理用户的实时连接状态
   - 支持多服务器节点的连接管理
   - 包含心跳检测机制，确保连接有效性
   - 支持连接状态的实时监控

5. push_devices（推送设备表）
   - 管理用户的推送设备信息
   - 支持iOS、Android、Web等多种设备类型
   - 存储设备令牌和设备信息，支持精准推送
   - 支持设备的激活状态管理
*/

-- =============================================
-- 设计特点
-- =============================================

/*
1. 统一主键类型：所有表均使用BIGINT类型的自增主键，确保数据一致性和扩展性

2. 完善的索引设计：
   - 为高频查询字段建立索引（如user_id、type、is_read等）
   - 为时间字段建立索引，支持时间范围查询
   - 为状态字段建立索引，提高筛选查询性能

3. 软删除机制：通知表支持软删除，保留历史数据的同时支持逻辑删除

4. 审计字段：包含创建时间、更新时间、阅读时间等时间戳字段，支持数据审计

5. JSON数据支持：通知表的data字段使用JSON类型，支持存储复杂的通知相关数据

6. 枚举类型约束：使用ENUM类型约束通知类型、优先级、设备类型等字段值

7. 模板化设计：通过通知模板表实现消息内容的模板化管理，提高维护效率

8. 多渠道通知支持：支持邮件、推送、短信、WebSocket等多种通知渠道

9. 用户偏好管理：通过通知设置表实现用户个性化的通知偏好配置

10. 实时通信支持：通过WebSocket连接表支持实时通知推送

11. 设备管理：支持多设备推送，每个用户可以绑定多个推送设备

12. 优先级机制：支持通知优先级设置，确保重要通知优先处理

13. 免打扰功能：支持用户设置免打扰时间段，提升用户体验

14. 心跳检测：WebSocket连接支持心跳检测，确保连接的有效性

15. 服务器节点标识：支持多服务器部署，通过节点标识实现负载均衡

16. 模板变量替换：通知模板支持变量替换，实现动态内容生成

17. 字符集支持：使用utf8mb4字符集，支持emoji等特殊字符

18. 数据字典集成：与系统数据字典集成，便于前端展示和管理

19. 约束保护：通过唯一键约束防止重复数据，确保数据完整性

20. 扩展性设计：表结构设计考虑了未来功能扩展的需要，预留了扩展字段
*/