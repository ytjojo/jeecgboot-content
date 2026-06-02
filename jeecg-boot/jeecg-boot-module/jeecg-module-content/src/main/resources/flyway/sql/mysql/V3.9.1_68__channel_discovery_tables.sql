-- V3.9.1_67__channel_discovery_tables.sql

-- 1. 平台分类表
CREATE TABLE IF NOT EXISTS `content_channel_category` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父级分类ID，null表示根分类',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `path` varchar(255) NOT NULL COMMENT '分类路径，如 /001/002/003',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '分类层级 1-4',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=停用 1=启用',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否特殊分类 0=普通 1=特殊',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_channel_category_parent` (`parent_id`),
  KEY `idx_channel_category_status` (`status`, `level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台频道分类表';

-- 2. 频道标签表
CREATE TABLE IF NOT EXISTS `content_channel_tag` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `name` varchar(20) NOT NULL COMMENT '标签名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=已删除 1=正常',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_tag_name` (`channel_id`, `name`),
  KEY `idx_channel_tag_channel` (`channel_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道内标签表';

-- 3. 标签-内容关联表
CREATE TABLE IF NOT EXISTS `content_channel_tag_relation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tag_id` varchar(32) NOT NULL COMMENT '标签ID',
  `content_id` varchar(32) NOT NULL COMMENT '内容ID',
  `content_type` varchar(32) NOT NULL COMMENT '内容类型: article/post/video/note/question',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_content` (`tag_id`, `content_id`, `content_type`),
  KEY `idx_tag_relation_content` (`content_id`, `content_type`),
  KEY `idx_tag_relation_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签-内容关联表';

-- 4. 推荐缓存表
CREATE TABLE IF NOT EXISTS `content_channel_recommendation_cache` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `ranking_score` decimal(10,4) NOT NULL DEFAULT 0 COMMENT '推荐评分',
  `recommendation_rule` varchar(64) NOT NULL COMMENT '推荐规则: SIMILARITY/PREFERENCE/POPULAR/COLD_START',
  `recommendation_reason` varchar(255) DEFAULT NULL COMMENT '推荐理由',
  `recommendation_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=已消费 1=有效',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommendation_user` (`user_id`, `recommendation_status`),
  KEY `idx_recommendation_channel` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道推荐缓存表';

-- 5. 不感兴趣反馈表
CREATE TABLE IF NOT EXISTS `content_channel_not_interested` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `category_id` varchar(32) DEFAULT NULL COMMENT '频道主分类ID，用于降低同分类权重',
  `expire_time` datetime NOT NULL COMMENT '过期时间，30天后',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_not_interested_user_channel` (`user_id`, `channel_id`),
  KEY `idx_not_interested_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道不感兴趣反馈表';

-- 6. 排行榜快照表
CREATE TABLE IF NOT EXISTS `content_channel_ranking_snapshot` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `ranking_type` varchar(32) NOT NULL COMMENT '榜单类型: HOT/NEW/SYSTEM',
  `dimension` varchar(16) NOT NULL COMMENT '维度: DAILY/WEEKLY/MONTHLY',
  `rank_position` int NOT NULL COMMENT '排名位置',
  `score` decimal(12,4) NOT NULL DEFAULT 0 COMMENT '综合得分',
  `snapshot_date` date NOT NULL COMMENT '快照日期',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ranking_snapshot` (`ranking_type`, `dimension`, `snapshot_date`, `rank_position`),
  KEY `idx_ranking_channel` (`channel_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道排行榜快照表';

-- 7. 编辑精选表
CREATE TABLE IF NOT EXISTS `content_channel_editorial_pick` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `recommendation_text` varchar(255) DEFAULT NULL COMMENT '推荐语',
  `start_time` datetime NOT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间，null表示永久',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=下线 1=上线',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人ID',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_editorial_pick_channel` (`channel_id`),
  KEY `idx_editorial_pick_status` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道编辑精选表';
