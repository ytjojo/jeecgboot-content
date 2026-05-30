-- V_channel_infrastructure.sql
-- 频道基础架构表结构

-- 频道主表
CREATE TABLE content_channel (
    id VARCHAR(32) NOT NULL COMMENT '频道ID',
    name VARCHAR(100) NOT NULL COMMENT '频道名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '频道简介',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT '频道图标URL',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '频道封面URL',
    channel_type TINYINT NOT NULL COMMENT '频道类型: 1=system, 2=personal, 3=organization',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=Draft, 1=PendingReview, 2=Active, 3=Rejected, 4=DeleteCooling, 5=Deleted',
    privacy TINYINT NOT NULL DEFAULT 1 COMMENT '隐私设置: 1=公开, 2=私有',
    category_id VARCHAR(32) DEFAULT NULL COMMENT '归属分类ID',
    owner_id VARCHAR(32) NOT NULL COMMENT '频道主用户ID',
    organization_id VARCHAR(32) DEFAULT NULL COMMENT '组织ID(组织频道必填)',
    pin_weight INT NOT NULL DEFAULT 0 COMMENT '置顶权重(系统频道)',
    delete_cooling_end_time DATETIME DEFAULT NULL COMMENT '冷静期结束时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_channel_type (channel_type),
    KEY idx_status (status),
    KEY idx_owner_id (owner_id),
    KEY idx_organization_id (organization_id),
    KEY idx_category_id (category_id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道表';

-- 频道审核记录表
CREATE TABLE content_channel_review (
    id VARCHAR(32) NOT NULL COMMENT '审核记录ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '审核人ID',
    result TINYINT NOT NULL COMMENT '审核结果: 1=Pass, 2=Reject, 3=ReturnForEdit',
    reason VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_channel_id (channel_id),
    KEY idx_reviewer_id (reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道审核记录表';

-- 频道转让记录表
CREATE TABLE content_channel_transfer (
    id VARCHAR(32) NOT NULL COMMENT '转让记录ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    from_user_id VARCHAR(32) NOT NULL COMMENT '发起转让用户ID',
    to_user_id VARCHAR(32) NOT NULL COMMENT '目标用户ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=Pending, 1=Accepted, 2=Rejected, 3=Expired',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_channel_id (channel_id),
    KEY idx_from_user_id (from_user_id),
    KEY idx_to_user_id (to_user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道转让记录表';
