CREATE TABLE IF NOT EXISTS `content_user_block` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '拉黑发起用户ID',
  `blocked_user_id` varchar(32) NOT NULL COMMENT '被拉黑用户ID',
  `block_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '拉黑状态',
  `reason` varchar(255) DEFAULT NULL COMMENT '拉黑原因',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_block_pair` (`user_id`,`blocked_user_id`),
  KEY `idx_content_user_block_user` (`user_id`,`status`,`block_time`),
  KEY `idx_content_user_block_blocked` (`blocked_user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户拉黑关系';

CREATE TABLE IF NOT EXISTS `content_user_mute` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '屏蔽发起用户ID',
  `muted_user_id` varchar(32) NOT NULL COMMENT '被屏蔽用户ID',
  `mute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '屏蔽时间',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '屏蔽状态',
  `reason` varchar(255) DEFAULT NULL COMMENT '屏蔽原因',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_mute_pair` (`user_id`,`muted_user_id`),
  KEY `idx_content_user_mute_user` (`user_id`,`status`,`mute_time`),
  KEY `idx_content_user_mute_muted` (`muted_user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户屏蔽关系';

CREATE TABLE IF NOT EXISTS `content_user_filter_rule` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `rule_type` varchar(32) NOT NULL COMMENT '规则类型',
  `rule_value` varchar(255) NOT NULL COMMENT '规则原始值',
  `normalized_value` varchar(255) NOT NULL COMMENT '规则归一化值',
  `match_scope` varchar(32) NOT NULL DEFAULT 'FEED' COMMENT '匹配范围',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '规则状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_filter_rule` (`user_id`,`rule_type`,`normalized_value`,`status`),
  KEY `idx_content_user_filter_rule_user` (`user_id`,`status`,`rule_type`),
  KEY `idx_content_user_filter_rule_expire` (`status`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户屏蔽规则';

CREATE TABLE IF NOT EXISTS `content_user_not_interested` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `content_id` varchar(64) NOT NULL COMMENT '内容ID',
  `content_type` varchar(32) NOT NULL COMMENT '内容类型',
  `topic` varchar(128) DEFAULT NULL COMMENT '关联话题或Tag',
  `reason` varchar(255) DEFAULT NULL COMMENT '不感兴趣原因',
  `feedback_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '反馈时间',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '反馈状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_not_interested` (`user_id`,`content_id`,`content_type`),
  KEY `idx_content_user_not_interested_user` (`user_id`,`status`,`feedback_time`),
  KEY `idx_content_user_not_interested_content` (`content_type`,`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户不感兴趣反馈';
