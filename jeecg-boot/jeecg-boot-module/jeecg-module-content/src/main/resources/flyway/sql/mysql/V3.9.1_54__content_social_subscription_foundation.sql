ALTER TABLE `content_user_relation`
  ADD COLUMN `relation_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '关系记录状态' AFTER `blocked_by_owner`,
  ADD COLUMN `last_interaction_time` datetime DEFAULT NULL COMMENT '最近互动时间' AFTER `blacklisted_at`,
  ADD KEY `idx_content_user_relation_follow_group` (`owner_user_id`,`followed`,`relation_group_id`,`followed_at`),
  ADD KEY `idx_content_user_relation_special` (`owner_user_id`,`special_follow`,`special_follow_at`),
  ADD KEY `idx_content_user_relation_status` (`relation_status`,`update_time`);

ALTER TABLE `content_user_relation_group`
  ADD COLUMN `group_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '分组状态' AFTER `is_default`,
  ADD UNIQUE KEY `uk_content_user_relation_group_name` (`owner_user_id`,`group_name`,`group_status`),
  ADD KEY `idx_content_user_relation_group_sort` (`owner_user_id`,`group_status`,`sort_order`);

ALTER TABLE `content_user_subscription`
  ADD COLUMN `subscription_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '订阅状态' AFTER `paused`,
  ADD COLUMN `subscribed_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间' AFTER `recommendation_reason`,
  ADD COLUMN `last_update_time` datetime DEFAULT NULL COMMENT '订阅源最近更新时间' AFTER `subscribed_at`,
  ADD KEY `idx_content_user_subscription_user_status` (`user_id`,`subscription_status`,`paused`,`update_time`),
  ADD KEY `idx_content_user_subscription_source_status` (`source_type`,`source_id`,`subscription_status`);

ALTER TABLE `content_user_notification_setting`
  ADD COLUMN `subscription_notice_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '订阅更新通知开关' AFTER `private_message_notice_enabled`,
  ADD COLUMN `subscription_default_channels` varchar(255) NOT NULL DEFAULT 'IN_APP,PUSH' COMMENT '订阅默认通知渠道' AFTER `subscription_notice_enabled`,
  ADD COLUMN `subscription_default_frequency` varchar(32) NOT NULL DEFAULT 'REALTIME' COMMENT '订阅默认通知频率' AFTER `subscription_default_channels`;

CREATE TABLE IF NOT EXISTS `content_user_feed_setting` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `publish_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示发布动态',
  `like_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示点赞动态',
  `favorite_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示收藏动态',
  `activity_types` varchar(255) NOT NULL DEFAULT 'PUBLISH,LIKE,FAVORITE' COMMENT '启用动态类型列表',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_feed_setting_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区关注流设置';

CREATE TABLE IF NOT EXISTS `content_user_activity_snapshot` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `actor_user_id` varchar(32) NOT NULL COMMENT '动态用户ID',
  `activity_type` varchar(32) NOT NULL COMMENT '动态类型',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型',
  `biz_id` varchar(64) NOT NULL COMMENT '业务ID',
  `summary` varchar(255) DEFAULT NULL COMMENT '动态摘要',
  `activity_time` datetime NOT NULL COMMENT '动态发生时间',
  `visible_scope` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '可见范围',
  `snapshot_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '快照状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_activity_snapshot_biz` (`activity_type`,`biz_type`,`biz_id`),
  KEY `idx_content_user_activity_snapshot_actor` (`actor_user_id`,`snapshot_status`,`activity_time`),
  KEY `idx_content_user_activity_snapshot_time` (`snapshot_status`,`activity_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户动态快照';

CREATE TABLE IF NOT EXISTS `content_user_follow_recommendation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '被推荐用户ID',
  `target_user_id` varchar(32) NOT NULL COMMENT '推荐关注用户ID',
  `recommendation_rule` varchar(32) NOT NULL COMMENT '推荐规则',
  `recommendation_reason` varchar(255) NOT NULL COMMENT '推荐理由',
  `ranking_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '排序分数',
  `recommendation_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '推荐状态',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_follow_recommendation_pair` (`user_id`,`target_user_id`),
  KEY `idx_content_user_follow_recommendation_rank` (`user_id`,`recommendation_status`,`ranking_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区关注推荐';

CREATE TABLE IF NOT EXISTS `content_subscription_source` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `source_type` varchar(32) NOT NULL COMMENT '订阅源类型',
  `source_id` varchar(64) NOT NULL COMMENT '订阅源ID',
  `source_name` varchar(128) NOT NULL COMMENT '订阅源名称',
  `source_description` varchar(500) DEFAULT NULL COMMENT '订阅源介绍',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面地址',
  `subscriber_count` int NOT NULL DEFAULT 0 COMMENT '订阅人数',
  `heat_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '热度分',
  `latest_update_time` datetime DEFAULT NULL COMMENT '最近更新时间',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_subscription_source` (`source_type`,`source_id`),
  KEY `idx_content_subscription_source_hot` (`enabled`,`category`,`heat_score`),
  KEY `idx_content_subscription_source_update` (`enabled`,`source_type`,`latest_update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区订阅源目录';

CREATE TABLE IF NOT EXISTS `content_subscription_notification_preference` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `subscription_id` varchar(32) NOT NULL COMMENT '订阅ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `notification_channels` varchar(255) DEFAULT NULL COMMENT '通知渠道',
  `notification_frequency` varchar(32) DEFAULT NULL COMMENT '通知频率',
  `dnd_start_time` varchar(8) DEFAULT NULL COMMENT '免打扰开始时间',
  `dnd_end_time` varchar(8) DEFAULT NULL COMMENT '免打扰结束时间',
  `preference_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '偏好状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_subscription_notification_subscription` (`subscription_id`),
  KEY `idx_content_subscription_notification_user` (`user_id`,`preference_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区订阅通知偏好';
