CREATE TABLE IF NOT EXISTS `content_user_level_benefit_penalty_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `penalty_record_id` varchar(32) NOT NULL COMMENT '成长处罚记录ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `benefit_code` varchar(64) NOT NULL COMMENT '等级权益编码',
  `previous_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处罚前是否启用',
  `current_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处罚后是否启用',
  `recover_status` varchar(32) NOT NULL DEFAULT 'PENDING_RECOVER' COMMENT '恢复状态',
  `recover_reason` varchar(255) DEFAULT NULL COMMENT '恢复原因',
  `recovered_by` varchar(32) DEFAULT NULL COMMENT '恢复操作人',
  `recovered_at` datetime DEFAULT NULL COMMENT '恢复时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_level_benefit_penalty_record_penalty` (`penalty_record_id`, `recover_status`),
  KEY `idx_content_user_level_benefit_penalty_record_user` (`user_id`, `benefit_code`, `recover_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区等级权益处罚恢复记录';
