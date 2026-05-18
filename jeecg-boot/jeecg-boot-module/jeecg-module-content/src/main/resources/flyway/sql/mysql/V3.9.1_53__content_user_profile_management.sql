ALTER TABLE `content_user_profile`
  ADD COLUMN `profile_completion_state` varchar(32) NOT NULL DEFAULT 'INCOMPLETE' COMMENT '资料完善状态' AFTER `avatar_history_json`,
  ADD COLUMN `profile_review_status` varchar(32) NOT NULL DEFAULT 'NONE' COMMENT '资料审核状态' AFTER `profile_completion_state`,
  ADD COLUMN `profile_version` int NOT NULL DEFAULT 0 COMMENT '资料版本号' AFTER `profile_review_status`;

ALTER TABLE `content_user_privacy_setting`
  ADD COLUMN `personal_link_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '个人链接可见性' AFTER `profession_visibility`,
  ADD COLUMN `verification_badge_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '认证标识可见性' AFTER `personal_link_visibility`,
  ADD COLUMN `contact_badge_visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE' COMMENT '绑定标识可见性' AFTER `verification_badge_visibility`;

CREATE TABLE IF NOT EXISTS `content_user_profile_review` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `review_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
  `review_type` varchar(32) NOT NULL DEFAULT 'PROFILE' COMMENT '审核类型',
  `risk_reason` varchar(255) DEFAULT NULL COMMENT '风险原因',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `original_snapshot_json` text COMMENT '原始资料快照JSON',
  `target_snapshot_json` text COMMENT '目标资料快照JSON',
  `reviewed_by` varchar(32) DEFAULT NULL COMMENT '审核人',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_profile_review_user` (`user_id`,`review_status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户资料审核';

CREATE TABLE IF NOT EXISTS `content_user_homepage_module` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `module_key` varchar(64) NOT NULL COMMENT '模块编码',
  `module_name` varchar(64) DEFAULT NULL COMMENT '模块名称',
  `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_homepage_module` (`user_id`,`module_key`),
  KEY `idx_content_user_homepage_module_sort` (`user_id`,`visible`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户主页模块';

CREATE TABLE IF NOT EXISTS `content_user_verification_badge` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `badge_type` varchar(32) NOT NULL COMMENT '认证类型',
  `badge_label` varchar(64) NOT NULL COMMENT '认证文案',
  `visual_style_key` varchar(64) DEFAULT NULL COMMENT '视觉样式编码',
  `description` varchar(255) DEFAULT NULL COMMENT '认证描述',
  `metadata_json` text COMMENT '认证元数据JSON',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `verified_at` datetime DEFAULT NULL COMMENT '认证时间',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_verification_badge_user` (`user_id`,`status`,`badge_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户认证标识';

CREATE TABLE IF NOT EXISTS `content_user_profile_history` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `history_type` varchar(32) NOT NULL COMMENT '历史类型',
  `history_value` varchar(500) NOT NULL COMMENT '历史值',
  `source_review_id` varchar(32) DEFAULT NULL COMMENT '来源审核ID',
  `source_update_id` varchar(32) DEFAULT NULL COMMENT '来源更新ID',
  `expired` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否过期',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_profile_history_user` (`user_id`,`history_type`,`expired`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户资料历史';
