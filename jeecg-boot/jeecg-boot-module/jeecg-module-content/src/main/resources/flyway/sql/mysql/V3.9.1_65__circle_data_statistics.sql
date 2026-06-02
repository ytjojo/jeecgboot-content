-- 圈子数据统计表
CREATE TABLE IF NOT EXISTS `circle_data_statistics` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `stat_date` date NOT NULL COMMENT '统计日期',
    `member_count` int NOT NULL DEFAULT 0 COMMENT '成员总数',
    `new_member_count` int NOT NULL DEFAULT 0 COMMENT '新增成员数',
    `post_count` int NOT NULL DEFAULT 0 COMMENT '帖子总数',
    `new_post_count` int NOT NULL DEFAULT 0 COMMENT '新增帖子数',
    `active_count` int NOT NULL DEFAULT 0 COMMENT '活跃用户数',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_stat_date` (`circle_id`, `stat_date`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='圈子数据统计';
