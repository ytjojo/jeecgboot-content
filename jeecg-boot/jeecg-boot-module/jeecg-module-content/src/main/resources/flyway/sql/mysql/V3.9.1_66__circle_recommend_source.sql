-- 圈子推荐来源追踪表
CREATE TABLE IF NOT EXISTS `circle_recommend_source` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `source_type` varchar(32) NOT NULL COMMENT '来源类型：RECOMMEND-推荐, HOT-热门榜单, NEW-新增榜单',
    `source_id` varchar(32) DEFAULT NULL COMMENT '来源ID',
    `click_time` datetime DEFAULT NULL COMMENT '点击时间',
    `join_time` datetime DEFAULT NULL COMMENT '加入时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_circle_user` (`circle_id`, `user_id`),
    KEY `idx_source_type` (`source_type`),
    KEY `idx_click_time` (`click_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='圈子推荐来源追踪';
