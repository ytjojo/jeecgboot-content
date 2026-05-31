-- 频道内容发布关联表
CREATE TABLE channel_content_publish (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型：article/post/video/note/question/answer',
    publisher_id VARCHAR(36) NOT NULL COMMENT '发布者ID',
    publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED' COMMENT '发布状态：PUBLISHED/PENDING/REJECTED/RECYCLED',
    is_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    pin_order INT DEFAULT 0 COMMENT '置顶排序',
    is_featured TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否精华',
    source_type VARCHAR(32) DEFAULT 'DIRECT' COMMENT '来源类型：DIRECT/SCHEDULED/MOVE/ADD_EXISTING',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel_content (channel_id, content_id),
    INDEX idx_channel_status (channel_id, publish_status),
    INDEX idx_publisher (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道内容发布关联';

-- 待审区表
CREATE TABLE channel_content_review (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    submitter_id VARCHAR(36) NOT NULL COMMENT '提交者ID',
    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
    reviewer_id VARCHAR(36) COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    source_scene VARCHAR(32) DEFAULT 'PUBLISH' COMMENT '来源场景：PUBLISH/ADD_EXISTING/MOVE',
    hit_rule VARCHAR(128) COMMENT '命中规则：PUBLIC_SUBMIT/PRE_REVIEW',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_status (channel_id, review_status),
    INDEX idx_submitter (submitter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道待审区';

-- 定时发布任务表
CREATE TABLE channel_scheduled_publish (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    publisher_id VARCHAR(36) NOT NULL COMMENT '发布者ID',
    scheduled_time DATETIME NOT NULL COMMENT '计划发布时间',
    publish_status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态：SCHEDULED/PUBLISHED/FAILED/CANCELLED',
    fail_reason VARCHAR(500) COMMENT '失败原因',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_scheduled_time (scheduled_time, publish_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道定时发布任务';

-- 发布限额配置表
CREATE TABLE channel_publish_limit (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    hourly_limit INT DEFAULT 0 COMMENT '每小时发布上限，0表示不限',
    daily_limit INT DEFAULT 0 COMMENT '每日发布上限，0表示不限',
    min_word_count INT DEFAULT 0 COMMENT '内容字数下限，0表示不限',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道发布限额配置';

-- 频道回收站表
CREATE TABLE channel_recycle_bin (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    original_author_id VARCHAR(36) NOT NULL COMMENT '原作者ID',
    deleted_by VARCHAR(36) NOT NULL COMMENT '删除人ID',
    delete_time DATETIME NOT NULL COMMENT '删除时间',
    delete_reason VARCHAR(500) COMMENT '删除原因',
    expire_time DATETIME NOT NULL COMMENT '过期时间（删除后30天）',
    is_restored TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已恢复',
    restored_by VARCHAR(36) COMMENT '恢复人ID',
    restore_time DATETIME COMMENT '恢复时间',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_expire (channel_id, expire_time),
    INDEX idx_content (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道回收站';

-- 内容治理日志表
CREATE TABLE channel_content_governance_log (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) COMMENT '内容ID',
    operator_id VARCHAR(36) NOT NULL COMMENT '操作者ID',
    action VARCHAR(32) NOT NULL COMMENT '操作类型：PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST/ANNOUNCEMENT_CREATE/ANNOUNCEMENT_UPDATE/ANNOUNCEMENT_DELETE',
    action_detail VARCHAR(1000) COMMENT '操作详情JSON',
    reason VARCHAR(500) COMMENT '操作原因',
    result VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_action (channel_id, action),
    INDEX idx_operator (operator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道治理日志';

-- 频道公告表
CREATE TABLE channel_announcement (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容（富文本）',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DELETED',
    created_by VARCHAR(36) NOT NULL COMMENT '创建人ID',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel_active (channel_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道公告';
