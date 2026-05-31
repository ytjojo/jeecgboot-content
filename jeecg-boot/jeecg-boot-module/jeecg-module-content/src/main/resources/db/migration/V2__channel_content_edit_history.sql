-- 频道内容编辑历史表
CREATE TABLE channel_content_edit_history (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    editor_id VARCHAR(36) NOT NULL COMMENT '编辑者ID',
    field_name VARCHAR(64) NOT NULL COMMENT '修改字段：title/tags/summary',
    old_value TEXT COMMENT '修改前的值',
    new_value TEXT COMMENT '修改后的值',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_content (channel_id, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道内容编辑历史';
