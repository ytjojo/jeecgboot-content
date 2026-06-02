-- ============================================================
-- 1. 圈子主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_circle` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '圈子名称',
  `description` varchar(500) DEFAULT NULL COMMENT '圈子简介',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '图标URL',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图URL',
  `category` varchar(64) DEFAULT NULL COMMENT '分类标签',
  `privacy_type` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '隐私类型: PUBLIC/PRIVATE/PASSWORD',
  `join_type` varchar(32) NOT NULL DEFAULT 'DIRECT' COMMENT '加入方式: DIRECT/APPROVAL/INVITE/PASSWORD',
  `password_hash` varchar(255) DEFAULT NULL COMMENT '密码保护密码哈希(BCrypt)',
  `creator_id` varchar(32) NOT NULL COMMENT '创建者用户ID',
  `member_count` int NOT NULL DEFAULT 1 COMMENT '成员数',
  `max_member_count` int NOT NULL DEFAULT 10000 COMMENT '最大成员数',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_circle_name` (`name`),
  KEY `idx_content_circle_creator` (`creator_id`),
  KEY `idx_content_circle_status` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子';

-- ============================================================
-- 2. 圈子成员表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_circle_member` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `role` varchar(32) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: CREATOR/MODERATOR/MEMBER',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/MUTED/REMOVED',
  `mute_end_time` datetime DEFAULT NULL COMMENT '禁言结束时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_circle_member` (`circle_id`, `user_id`),
  KEY `idx_content_circle_member_user` (`user_id`),
  KEY `idx_content_circle_member_status` (`circle_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员';

-- ============================================================
-- 3. 圈子治理日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_circle_governance_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
  `operator_id` varchar(32) NOT NULL COMMENT '操作者用户ID',
  `target_user_id` varchar(32) NOT NULL COMMENT '目标用户ID',
  `action` varchar(32) NOT NULL COMMENT '动作: MUTE/UNMUTE/REMOVE/ROLE_CHANGE',
  `reason` varchar(500) DEFAULT NULL COMMENT '操作原因',
  `duration` varchar(32) DEFAULT NULL COMMENT '禁言时长(如: 1h/24h/7d/PERMANENT)',
  `extra_data_json` text DEFAULT NULL COMMENT '额外数据JSON(如角色变更前后)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_circle_governance_circle` (`circle_id`, `create_time`),
  KEY `idx_content_circle_governance_target` (`target_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子治理操作日志';
