-- ============================================================
-- 圈子成长激励体系
-- ============================================================

-- 1. 圈子等级表
CREATE TABLE IF NOT EXISTS `circle_level` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `level` int NOT NULL DEFAULT 1 COMMENT '等级 1-5',
    `growth_score` int NOT NULL DEFAULT 0 COMMENT '成长分 0-1000',
    `member_score` int NOT NULL DEFAULT 0 COMMENT '成员规模得分',
    `content_score` int NOT NULL DEFAULT 0 COMMENT '内容贡献得分',
    `activity_score` int NOT NULL DEFAULT 0 COMMENT '活跃互动得分',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_level_circle_id` (`circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子等级';

-- 2. 成员成长记录表
CREATE TABLE IF NOT EXISTS `circle_member_growth` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `exp_points` int NOT NULL DEFAULT 0 COMMENT '经验值',
    `contribution_points` int NOT NULL DEFAULT 0 COMMENT '贡献值',
    `level` int NOT NULL DEFAULT 1 COMMENT '成员等级',
    `post_count` int NOT NULL DEFAULT 0 COMMENT '发帖数',
    `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
    `featured_count` int NOT NULL DEFAULT 0 COMMENT '精华数',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_member_growth_circle_user` (`circle_id`, `user_id`),
    KEY `idx_circle_member_growth_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员成长记录';

-- 3. 成长行为流水表
CREATE TABLE IF NOT EXISTS `circle_growth_log` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `action_type` varchar(32) NOT NULL COMMENT '行为类型: POST/COMMENT/LIKE/FEATURED',
    `exp_points` int NOT NULL DEFAULT 0 COMMENT '获得经验值',
    `contribution_points` int NOT NULL DEFAULT 0 COMMENT '获得贡献值',
    `biz_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID',
    `biz_date` date NOT NULL COMMENT '业务日期',
    `revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已撤销: 0-否 1-是',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_growth_log_circle_user_date_action_biz` (`circle_id`, `user_id`, `biz_date`, `action_type`, `biz_id`),
    KEY `idx_growth_log_circle_user_date` (`circle_id`, `user_id`, `biz_date`),
    KEY `idx_growth_log_circle_date` (`circle_id`, `biz_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长行为流水';

-- 4. 成就徽章配置表
CREATE TABLE IF NOT EXISTS `circle_achievement` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `achievement_type` varchar(32) NOT NULL COMMENT '徽章类型',
    `name` varchar(64) NOT NULL COMMENT '徽章名称',
    `description` varchar(256) DEFAULT NULL COMMENT '徽章描述',
    `icon_url` varchar(256) DEFAULT NULL COMMENT '徽章图标URL',
    `condition_desc` varchar(256) DEFAULT NULL COMMENT '达成条件描述',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_achievement_type` (`achievement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就徽章配置';

-- 5. 成员已获得徽章表
CREATE TABLE IF NOT EXISTS `circle_member_achievement` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `achievement_type` varchar(32) NOT NULL COMMENT '徽章类型',
    `revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已撤销: 0-否 1-是',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_achievement_circle_user_type` (`circle_id`, `user_id`, `achievement_type`),
    KEY `idx_member_achievement_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员已获得徽章';

-- 6. 排行榜快照表
CREATE TABLE IF NOT EXISTS `circle_leaderboard_snapshot` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `dimension` varchar(32) NOT NULL COMMENT '维度: EXP/CONTRIBUTION/POST',
    `period` varchar(16) NOT NULL COMMENT '周期: WEEK/MONTH/ALL',
    `score` int NOT NULL DEFAULT 0 COMMENT '得分',
    `rank_num` int NOT NULL DEFAULT 0 COMMENT '排名',
    `snapshot_time` datetime NOT NULL COMMENT '快照时间',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_leaderboard_circle_dim_period_user` (`circle_id`, `dimension`, `period`, `user_id`),
    KEY `idx_leaderboard_circle_dim_period_rank` (`circle_id`, `dimension`, `period`, `rank_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行榜快照';

-- 初始化成就徽章配置
INSERT IGNORE INTO `circle_achievement` (`id`, `achievement_type`, `name`, `description`, `condition_desc`) VALUES
('ach_001', 'CONTINUOUS_CREATOR', '持续创作者', '累计发布10篇可见内容', '累计发布10篇可见内容'),
('ach_002', 'QUALITY_CONTRIBUTOR', '优质贡献者', '累计获得5篇精华内容', '累计获得5篇精华内容'),
('ach_003', 'ACTIVE_PARTICIPANT', '活跃参与者', '近7天至少3天完成有效参与行为', '近7天至少3天完成有效参与行为'),
('ach_004', 'RISING_STAR', '圈内新星', '近7天经验值增长排名前10', '近7天经验值增长排名前10');
