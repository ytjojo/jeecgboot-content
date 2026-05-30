-- 频道隐私、订阅与成员管理迁移脚本

-- 扩展 content_channel 表：加入方式字段
ALTER TABLE content_channel
ADD COLUMN join_method TINYINT NOT NULL DEFAULT 1 COMMENT '加入方式: 1=自由加入 2=审核加入 3=邀请加入';

-- 订阅表
CREATE TABLE content_channel_subscription (
    id VARCHAR(32) NOT NULL COMMENT '订阅ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    source TINYINT NOT NULL DEFAULT 1 COMMENT '来源: 1=主动订阅 2=默认关注',
    remind_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '提醒开关: 0=关闭 1=开启',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_user (channel_id, user_id),
    KEY idx_channel_id (channel_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道订阅表';

-- 订阅分组表
CREATE TABLE content_channel_subscription_group (
    id VARCHAR(32) NOT NULL COMMENT '分组ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    group_name VARCHAR(50) NOT NULL COMMENT '分组名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅分组表';

-- 订阅分组关联表
CREATE TABLE content_channel_subscription_group_rel (
    id VARCHAR(32) NOT NULL COMMENT '关联ID',
    subscription_id VARCHAR(32) NOT NULL COMMENT '订阅ID',
    group_id VARCHAR(32) NOT NULL COMMENT '分组ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sub_group (subscription_id, group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅分组关联表';

-- 成员表
CREATE TABLE content_channel_member (
    id VARCHAR(32) NOT NULL COMMENT '成员ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role TINYINT NOT NULL DEFAULT 4 COMMENT '角色: 1=频道主 2=管理员 3=内容编辑 4=普通成员',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    cooling_end_time DATETIME DEFAULT NULL COMMENT '冷却期结束时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_user (channel_id, user_id),
    KEY idx_channel_id (channel_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道成员表';

-- 加入申请表
CREATE TABLE content_channel_join_application (
    id VARCHAR(32) NOT NULL COMMENT '申请ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    reason VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=待审核 2=已批准 3=已拒绝',
    reviewer_id VARCHAR(32) DEFAULT NULL COMMENT '审核人ID',
    review_time DATETIME DEFAULT NULL COMMENT '审核时间',
    review_reason VARCHAR(500) DEFAULT NULL COMMENT '审核理由',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_channel_id (channel_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='加入申请表';

-- 禁言表
CREATE TABLE content_channel_mute (
    id VARCHAR(32) NOT NULL COMMENT '禁言ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人ID',
    reason VARCHAR(500) DEFAULT NULL COMMENT '禁言原因',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间，NULL表示永久禁言',
    unmute_type TINYINT DEFAULT NULL COMMENT '解除方式: 1=自动到期 2=手动解除',
    unmute_time DATETIME DEFAULT NULL COMMENT '解除时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_channel_user (channel_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='禁言记录表';

-- 黑名单表
CREATE TABLE content_channel_blacklist (
    id VARCHAR(32) NOT NULL COMMENT '黑名单ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人ID',
    reason VARCHAR(500) DEFAULT NULL COMMENT '拉黑原因',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_user (channel_id, user_id),
    KEY idx_channel_id (channel_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道黑名单表';

-- 治理日志表
CREATE TABLE content_channel_governance_log (
    id VARCHAR(32) NOT NULL COMMENT '日志ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    action TINYINT NOT NULL COMMENT '操作类型: 1=移除 2=禁言 3=解除禁言 4=加入黑名单 5=移出黑名单',
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人ID',
    target_user_id VARCHAR(32) NOT NULL COMMENT '目标用户ID',
    reason VARCHAR(500) DEFAULT NULL COMMENT '操作原因',
    extra_data JSON DEFAULT NULL COMMENT '扩展数据（冷却期结束时间等）',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_channel_id (channel_id),
    KEY idx_target_user (target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治理操作日志表';

-- 邀请表
CREATE TABLE content_channel_invite (
    id VARCHAR(32) NOT NULL COMMENT '邀请ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    code VARCHAR(32) NOT NULL COMMENT '邀请码',
    type TINYINT NOT NULL COMMENT '类型: 1=邀请码 2=邀请链接',
    max_uses INT DEFAULT NULL COMMENT '最大使用次数，NULL表示不限',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用次数',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间，NULL表示不过期',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效 2=已用完 3=已撤销 4=已过期',
    creator_id VARCHAR(32) NOT NULL COMMENT '创建人ID',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_channel_id (channel_id),
    KEY idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道邀请表';
