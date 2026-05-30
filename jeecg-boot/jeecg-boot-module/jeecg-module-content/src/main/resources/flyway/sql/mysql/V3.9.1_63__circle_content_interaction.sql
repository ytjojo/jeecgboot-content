-- 圈子内容表（含置顶/精华字段）
CREATE TABLE IF NOT EXISTS circle_content (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    user_id VARCHAR(32) NOT NULL COMMENT '发布者ID',
    content TEXT COMMENT '内容',
    content_type VARCHAR(20) DEFAULT 'POST' COMMENT '内容类型 POST/COMMENT',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶 0否 1是',
    pinned_at DATETIME DEFAULT NULL COMMENT '置顶时间',
    is_featured TINYINT(1) DEFAULT 0 COMMENT '是否精华 0否 1是',
    featured_at DATETIME DEFAULT NULL COMMENT '精华标记时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除 0否 1是',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle (circle_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子内容表';

-- 圈子公告表
CREATE TABLE IF NOT EXISTS circle_announcement (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    content TEXT NOT NULL COMMENT '公告内容',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
    expire_at DATETIME DEFAULT NULL COMMENT '有效期截止时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle_status (circle_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子公告表';

-- 圈子加入申请表
CREATE TABLE IF NOT EXISTS circle_join_request (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    user_id VARCHAR(32) NOT NULL COMMENT '申请人ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED/EXPIRED',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    operator_id VARCHAR(32) DEFAULT NULL COMMENT '审核人ID',
    operate_time DATETIME DEFAULT NULL COMMENT '审核时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle_status (circle_id, status),
    INDEX idx_user (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子加入申请表';

-- 圈子内容举报表
CREATE TABLE IF NOT EXISTS circle_report (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    content_id VARCHAR(32) NOT NULL COMMENT '被举报内容ID',
    reporter_id VARCHAR(32) NOT NULL COMMENT '举报者ID',
    reason VARCHAR(500) DEFAULT NULL COMMENT '举报原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/RESOLVED/IGNORED',
    operator_id VARCHAR(32) DEFAULT NULL COMMENT '处理人ID',
    operate_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handle_action VARCHAR(30) DEFAULT NULL COMMENT '处理动作 DELETE/IGNORE/MUTE',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle_status (circle_id, status),
    INDEX idx_content (content_id),
    INDEX idx_reporter (reporter_id),
    UNIQUE KEY uk_reporter_content (reporter_id, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子内容举报表';

-- 圈子审核日志表
CREATE TABLE IF NOT EXISTS circle_audit_log (
    log_id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '日志ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人ID',
    action VARCHAR(30) NOT NULL COMMENT '操作类型',
    target_id VARCHAR(32) NOT NULL COMMENT '操作对象ID',
    target_type VARCHAR(20) NOT NULL COMMENT '操作对象类型 CONTENT/ANNOUNCEMENT/JOIN_REQUEST/REPORT/USER',
    result VARCHAR(20) NOT NULL COMMENT '操作结果',
    reason VARCHAR(500) DEFAULT NULL COMMENT '原因/备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_circle (circle_id),
    INDEX idx_target (target_id, target_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子审核日志表';

-- 圈子成员表（如 circle-core 未创建）
CREATE TABLE IF NOT EXISTS circle_member (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色 CREATOR/MODERATOR/MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/MUTED/REMOVED',
    muted_until DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_circle_user (circle_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员表';
