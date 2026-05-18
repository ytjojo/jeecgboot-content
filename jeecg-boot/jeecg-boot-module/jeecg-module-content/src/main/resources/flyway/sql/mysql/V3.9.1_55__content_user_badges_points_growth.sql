CREATE TABLE IF NOT EXISTS `content_user_reward_rule` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_code` varchar(64) NOT NULL COMMENT '奖励规则编码',
  `source_type` varchar(64) NOT NULL COMMENT '行为来源类型',
  `point_amount` int NOT NULL DEFAULT 0 COMMENT '奖励积分',
  `growth_amount` int NOT NULL DEFAULT 0 COMMENT '奖励成长值',
  `daily_point_cap` int DEFAULT NULL COMMENT '每日积分上限',
  `daily_growth_cap` int DEFAULT NULL COMMENT '每日成长值上限',
  `rule_description` varchar(255) DEFAULT NULL COMMENT '规则说明',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_reward_rule_code` (`rule_code`),
  KEY `idx_content_user_reward_rule_source` (`source_type`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户奖励规则';

CREATE TABLE IF NOT EXISTS `content_user_reward_event` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `event_id` varchar(64) NOT NULL COMMENT '奖励事件ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `source_type` varchar(64) NOT NULL COMMENT '行为来源类型',
  `rule_code` varchar(64) DEFAULT NULL COMMENT '命中奖励规则编码',
  `point_delta` int NOT NULL DEFAULT 0 COMMENT '实际奖励积分',
  `growth_delta` int NOT NULL DEFAULT 0 COMMENT '实际奖励成长值',
  `daily_bucket` varchar(32) DEFAULT NULL COMMENT '每日统计桶',
  `process_status` varchar(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '处理状态',
  `skip_reason` varchar(255) DEFAULT NULL COMMENT '跳过原因',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_reward_event_event` (`event_id`),
  KEY `idx_content_user_reward_event_user` (`user_id`,`source_type`,`create_time`),
  KEY `idx_content_user_reward_event_bucket` (`source_type`,`daily_bucket`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户奖励事件';

CREATE TABLE IF NOT EXISTS `content_user_level_config` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `level` int NOT NULL COMMENT '等级',
  `level_name` varchar(64) NOT NULL COMMENT '等级名称',
  `growth_threshold` int NOT NULL COMMENT '成长值门槛',
  `badge_style_key` varchar(64) DEFAULT NULL COMMENT '等级标识样式KEY',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_level_config_level` (`level`),
  KEY `idx_content_user_level_config_threshold` (`enabled`,`growth_threshold`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户等级配置';

CREATE TABLE IF NOT EXISTS `content_user_level_benefit_config` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `level` int NOT NULL COMMENT '适用等级',
  `benefit_key` varchar(64) NOT NULL COMMENT '权益编码',
  `benefit_value` varchar(255) NOT NULL COMMENT '权益值',
  `benefit_config_json` text COMMENT '权益扩展配置JSON',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_level_benefit` (`level`,`benefit_key`),
  KEY `idx_content_user_level_benefit_key` (`benefit_key`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户等级权益配置';

CREATE TABLE IF NOT EXISTS `content_user_exchange_goods` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `goods_code` varchar(64) NOT NULL COMMENT '商品编码',
  `goods_name` varchar(128) NOT NULL COMMENT '商品名称',
  `goods_type` varchar(32) NOT NULL COMMENT '商品类型',
  `point_price` int NOT NULL COMMENT '积分价格',
  `stock_quantity` int DEFAULT NULL COMMENT '库存数量',
  `benefit_config_json` text COMMENT '权益发放配置JSON',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_exchange_goods_code` (`goods_code`),
  KEY `idx_content_user_exchange_goods_type` (`goods_type`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区积分兑换商品';

CREATE TABLE IF NOT EXISTS `content_user_exchange_order` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '兑换订单号',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `goods_id` varchar(32) NOT NULL COMMENT '商品ID',
  `goods_code` varchar(64) NOT NULL COMMENT '商品编码',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '兑换数量',
  `point_cost` int NOT NULL COMMENT '消耗积分',
  `order_status` varchar(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '订单状态',
  `benefit_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '权益发放状态',
  `failure_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_exchange_order_no` (`order_no`),
  KEY `idx_content_user_exchange_order_user` (`user_id`,`create_time`),
  KEY `idx_content_user_exchange_order_goods` (`goods_id`,`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区积分兑换订单';

CREATE TABLE IF NOT EXISTS `content_user_feature_unlock` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `feature_code` varchar(64) NOT NULL COMMENT '功能编码',
  `source_order_id` varchar(32) DEFAULT NULL COMMENT '来源兑换订单ID',
  `valid_from` datetime DEFAULT NULL COMMENT '生效时间',
  `valid_until` datetime DEFAULT NULL COMMENT '失效时间',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_feature_unlock` (`user_id`,`feature_code`),
  KEY `idx_content_user_feature_unlock_valid` (`feature_code`,`enabled`,`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户功能解锁';

CREATE TABLE IF NOT EXISTS `content_user_virtual_gift_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `sender_user_id` varchar(32) NOT NULL COMMENT '赠送人用户ID',
  `receiver_user_id` varchar(32) NOT NULL COMMENT '接收人用户ID',
  `gift_goods_id` varchar(32) NOT NULL COMMENT '礼物商品ID',
  `gift_code` varchar(64) NOT NULL COMMENT '礼物编码',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '礼物数量',
  `point_cost` int NOT NULL COMMENT '消耗积分',
  `message` varchar(255) DEFAULT NULL COMMENT '赠言',
  `notification_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '通知状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_virtual_gift_sender` (`sender_user_id`,`create_time`),
  KEY `idx_content_user_virtual_gift_receiver` (`receiver_user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区虚拟礼物记录';

CREATE TABLE IF NOT EXISTS `content_user_growth_decay_state` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `decay_count` int NOT NULL DEFAULT 0 COMMENT '衰减次数',
  `last_decay_time` datetime DEFAULT NULL COMMENT '最后衰减时间',
  `protection_started_at` datetime DEFAULT NULL COMMENT '降级保护开始时间',
  `protection_until` datetime DEFAULT NULL COMMENT '降级保护结束时间',
  `status` varchar(32) NOT NULL DEFAULT 'NORMAL' COMMENT '衰减状态',
  `rule_snapshot_json` text COMMENT '衰减规则快照JSON',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_growth_decay_user` (`user_id`),
  KEY `idx_content_user_growth_decay_candidate` (`status`,`last_active_time`),
  KEY `idx_content_user_growth_decay_protection` (`status`,`protection_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区成长值衰减状态';

ALTER TABLE `content_user_badge_definition`
  ADD COLUMN `category` varchar(32) DEFAULT NULL COMMENT '勋章分类',
  ADD COLUMN `icon_url` varchar(500) DEFAULT NULL COMMENT '勋章图标URL',
  ADD COLUMN `effect_key` varchar(64) DEFAULT NULL COMMENT '展示特效KEY',
  ADD COLUMN `condition_description` varchar(255) DEFAULT NULL COMMENT '获得条件说明',
  ADD COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  ADD KEY `idx_content_user_badge_definition_category` (`category`,`enabled`,`sort_order`);

ALTER TABLE `content_user_badge_grant`
  ADD COLUMN `display_order` int DEFAULT NULL COMMENT '佩戴展示排序',
  ADD COLUMN `recycled_by` varchar(32) DEFAULT NULL COMMENT '回收操作人',
  ADD COLUMN `recycle_reason` varchar(255) DEFAULT NULL COMMENT '回收原因',
  ADD KEY `idx_content_user_badge_grant_display` (`user_id`,`displaying`,`display_order`);

ALTER TABLE `content_user_point_ledger`
  ADD COLUMN `source_description` varchar(255) DEFAULT NULL COMMENT '来源说明',
  ADD COLUMN `event_id` varchar(64) DEFAULT NULL COMMENT '奖励事件ID',
  ADD COLUMN `rule_snapshot_json` text COMMENT '规则快照JSON',
  ADD KEY `idx_content_user_point_ledger_event` (`event_id`);

ALTER TABLE `content_user_growth_ledger`
  ADD COLUMN `source_description` varchar(255) DEFAULT NULL COMMENT '来源说明',
  ADD COLUMN `event_id` varchar(64) DEFAULT NULL COMMENT '奖励事件ID',
  ADD COLUMN `rule_snapshot_json` text COMMENT '规则快照JSON',
  ADD KEY `idx_content_user_growth_ledger_event` (`event_id`);
