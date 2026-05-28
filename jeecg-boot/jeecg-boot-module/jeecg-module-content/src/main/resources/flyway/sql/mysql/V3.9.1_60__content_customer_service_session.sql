-- 客服会话表
CREATE TABLE IF NOT EXISTS `content_customer_service_session` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `session_type` varchar(32) NOT NULL COMMENT '会话类型',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态: ACTIVE/CLOSED',
  `rating` int DEFAULT NULL COMMENT '评分 1-5',
  `rating_comment` varchar(500) DEFAULT NULL COMMENT '评分内容',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_user` (`user_id`),
  KEY `idx_session_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区客服会话';
