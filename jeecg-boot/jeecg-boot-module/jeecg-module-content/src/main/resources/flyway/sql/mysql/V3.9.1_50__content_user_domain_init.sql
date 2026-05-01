CREATE TABLE IF NOT EXISTS `content_user_profile` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `gender` tinyint DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `region` varchar(128) DEFAULT NULL COMMENT '地区',
  `profession` varchar(128) DEFAULT NULL COMMENT '职业',
  `personal_link` varchar(255) DEFAULT NULL COMMENT '个人链接',
  `homepage_background` varchar(500) DEFAULT NULL COMMENT '主页背景图',
  `theme_color` varchar(32) DEFAULT NULL COMMENT '主题色',
  `module_order_json` text COMMENT '主页模块排序JSON',
  `certification_type` varchar(32) DEFAULT NULL COMMENT '认证类型',
  `certification_label` varchar(128) DEFAULT NULL COMMENT '认证展示文案',
  `nickname_history_json` text COMMENT '昵称历史JSON',
  `avatar_history_json` text COMMENT '头像历史JSON',
  `status` varchar(32) NOT NULL DEFAULT 'REGISTERED_INCOMPLETE' COMMENT '当前用户状态',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级',
  `point_balance` int NOT NULL DEFAULT 0 COMMENT '积分余额',
  `growth_value` int NOT NULL DEFAULT 0 COMMENT '成长值',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_profile_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户资料';

CREATE TABLE IF NOT EXISTS `content_user_privacy_setting` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `birthday_visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE' COMMENT '生日可见性',
  `gender_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '性别可见性',
  `region_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '地区可见性',
  `profession_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '职业可见性',
  `homepage_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '主页可见性',
  `dynamic_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '动态可见性',
  `online_status_visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '在线状态是否可见',
  `allow_search_engine_index` tinyint(1) NOT NULL DEFAULT 1 COMMENT '允许搜索引擎索引',
  `allow_user_search` tinyint(1) NOT NULL DEFAULT 1 COMMENT '允许站内搜索',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_privacy_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户隐私设置';

CREATE TABLE IF NOT EXISTS `content_user_relation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `owner_user_id` varchar(32) NOT NULL COMMENT '关系拥有者用户ID',
  `target_user_id` varchar(32) NOT NULL COMMENT '目标用户ID',
  `relation_group_id` varchar(32) DEFAULT NULL COMMENT '关系分组ID',
  `followed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关注',
  `special_follow` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否特别关注',
  `muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否屏蔽',
  `blacklisted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否拉黑',
  `blocked_by_owner` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否由拥有者阻断',
  `recommendation_reason` varchar(255) DEFAULT NULL COMMENT '推荐理由',
  `followed_at` datetime DEFAULT NULL COMMENT '关注时间',
  `special_follow_at` datetime DEFAULT NULL COMMENT '特别关注时间',
  `muted_at` datetime DEFAULT NULL COMMENT '屏蔽时间',
  `blacklisted_at` datetime DEFAULT NULL COMMENT '拉黑时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_relation_pair` (`owner_user_id`,`target_user_id`),
  KEY `idx_content_user_relation_target` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户关系';

CREATE TABLE IF NOT EXISTS `content_user_relation_group` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `owner_user_id` varchar(32) NOT NULL COMMENT '拥有者用户ID',
  `group_name` varchar(64) NOT NULL COMMENT '分组名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认分组',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_relation_group_owner` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区关注分组';

CREATE TABLE IF NOT EXISTS `content_user_subscription` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `source_type` varchar(32) NOT NULL COMMENT '订阅源类型',
  `source_id` varchar(64) NOT NULL COMMENT '订阅源ID',
  `source_name` varchar(128) DEFAULT NULL COMMENT '订阅源名称',
  `notification_channels` varchar(255) DEFAULT NULL COMMENT '通知渠道',
  `notification_frequency` varchar(32) DEFAULT NULL COMMENT '通知频率',
  `paused` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否暂停',
  `recommendation_reason` varchar(255) DEFAULT NULL COMMENT '订阅推荐理由',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_subscription` (`user_id`,`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户订阅';

CREATE TABLE IF NOT EXISTS `content_user_notification_setting` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `like_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '点赞通知开关',
  `comment_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '评论通知开关',
  `follow_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '关注通知开关',
  `favorite_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '收藏通知开关',
  `mention_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '@通知开关',
  `private_message_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '私信通知开关',
  `channel_config_json` text COMMENT '渠道配置JSON',
  `dnd_rule_json` text COMMENT '免打扰规则JSON',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_notification_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区通知设置';

CREATE TABLE IF NOT EXISTS `content_user_point_ledger` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `source_type` varchar(64) NOT NULL COMMENT '来源类型',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务ID',
  `point_delta` int NOT NULL COMMENT '积分变动值',
  `balance_after` int DEFAULT NULL COMMENT '变更后余额',
  `daily_bucket` varchar(32) DEFAULT NULL COMMENT '每日统计桶',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_point_ledger_user` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区积分台账';

CREATE TABLE IF NOT EXISTS `content_user_growth_ledger` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `source_type` varchar(64) NOT NULL COMMENT '来源类型',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务ID',
  `growth_delta` int NOT NULL COMMENT '成长值变动',
  `growth_after` int DEFAULT NULL COMMENT '变更后成长值',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_growth_ledger_user` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区成长值台账';

CREATE TABLE IF NOT EXISTS `content_user_badge_definition` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `badge_code` varchar(64) NOT NULL COMMENT '勋章编码',
  `badge_name` varchar(128) NOT NULL COMMENT '勋章名称',
  `badge_type` varchar(32) NOT NULL COMMENT '勋章类型',
  `rule_config_json` text COMMENT '规则配置JSON',
  `valid_days` int DEFAULT NULL COMMENT '有效天数',
  `auto_grant` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动发放',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_badge_definition_code` (`badge_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区勋章定义';

CREATE TABLE IF NOT EXISTS `content_user_badge_grant` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `badge_definition_id` varchar(32) NOT NULL COMMENT '勋章定义ID',
  `badge_code` varchar(64) NOT NULL COMMENT '勋章编码',
  `grant_source` varchar(64) DEFAULT NULL COMMENT '发放来源',
  `grant_reason` varchar(255) DEFAULT NULL COMMENT '发放原因',
  `displaying` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否佩戴展示',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `recycled_at` datetime DEFAULT NULL COMMENT '回收时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_badge_grant_user` (`user_id`,`badge_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区勋章发放记录';

CREATE TABLE IF NOT EXISTS `content_user_status_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `current_status` varchar(32) DEFAULT NULL COMMENT '当前状态',
  `target_status` varchar(32) NOT NULL COMMENT '目标状态',
  `trigger_source` varchar(64) DEFAULT NULL COMMENT '触发来源',
  `operator_user_id` varchar(32) DEFAULT NULL COMMENT '操作人',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因',
  `rule_code` varchar(64) DEFAULT NULL COMMENT '规则编码',
  `effective_start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `effective_end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `recoverable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可恢复',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_status_record_user` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户状态记录';

CREATE TABLE IF NOT EXISTS `content_user_appeal` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `appeal_type` varchar(32) NOT NULL COMMENT '申诉类型',
  `target_id` varchar(64) DEFAULT NULL COMMENT '申诉目标ID',
  `target_type` varchar(32) DEFAULT NULL COMMENT '申诉目标类型',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '申诉状态',
  `reason` varchar(500) DEFAULT NULL COMMENT '申诉原因',
  `evidence_json` text COMMENT '证据JSON',
  `progress_note` varchar(500) DEFAULT NULL COMMENT '处理进度说明',
  `result_status` varchar(32) DEFAULT NULL COMMENT '处理结果状态',
  `result_note` varchar(500) DEFAULT NULL COMMENT '处理结果说明',
  `resolved_by` varchar(32) DEFAULT NULL COMMENT '处理人',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理完成时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_appeal_user` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户申诉';

CREATE TABLE IF NOT EXISTS `content_user_report` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '举报用户ID',
  `target_type` varchar(32) NOT NULL COMMENT '举报目标类型',
  `target_id` varchar(64) NOT NULL COMMENT '举报目标ID',
  `report_type` varchar(32) NOT NULL COMMENT '举报类型',
  `reason` varchar(500) DEFAULT NULL COMMENT '举报原因',
  `evidence_json` text COMMENT '举报证据JSON',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '举报状态',
  `result_status` varchar(32) DEFAULT NULL COMMENT '处理结果状态',
  `result_note` varchar(500) DEFAULT NULL COMMENT '处理结果说明',
  `progress_note` varchar(500) DEFAULT NULL COMMENT '处理进度说明',
  `resolved_by` varchar(32) DEFAULT NULL COMMENT '处理人',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理完成时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_report_user` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户举报';

CREATE TABLE IF NOT EXISTS `content_user_audit_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '关联用户ID',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `operator_user_id` varchar(32) DEFAULT NULL COMMENT '操作人',
  `event_content` varchar(500) DEFAULT NULL COMMENT '事件内容',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '追踪ID',
  `extra_data_json` text COMMENT '额外数据JSON',
  `event_time` datetime DEFAULT NULL COMMENT '事件时间',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `device_info` varchar(255) DEFAULT NULL COMMENT '设备信息',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_audit_log_user` (`user_id`,`event_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户审计日志';

CREATE TABLE IF NOT EXISTS `content_user_device_session` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `session_token` varchar(128) DEFAULT NULL COMMENT '会话令牌',
  `device_id` varchar(128) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(64) DEFAULT NULL COMMENT '设备类型',
  `login_ip` varchar(64) DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(128) DEFAULT NULL COMMENT '登录地点',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `offline` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已下线',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_device_session_user` (`user_id`,`offline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户设备会话';
