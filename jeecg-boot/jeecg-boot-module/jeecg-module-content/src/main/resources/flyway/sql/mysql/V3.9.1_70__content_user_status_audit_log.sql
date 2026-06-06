-- 用户状态审计日志表
CREATE TABLE IF NOT EXISTS `content_user_status_audit_log` (
    `log_id` VARCHAR(36) NOT NULL COMMENT '日志ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `from_status` VARCHAR(32) NOT NULL COMMENT '原状态',
    `to_status` VARCHAR(32) NOT NULL COMMENT '新状态',
    `operator_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
    `operator_type` VARCHAR(16) DEFAULT NULL COMMENT '操作人类型（SYSTEM/ADMIN）',
    `trigger_reason` VARCHAR(500) DEFAULT NULL COMMENT '触发原因',
    `rule_id` VARCHAR(64) DEFAULT NULL COMMENT '规则ID',
    `start_time` DATETIME DEFAULT NULL COMMENT '状态开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '状态结束时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '操作人IP地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_operator_type` (`operator_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户状态审计日志';

-- 在 content_user_status_record 表上添加索引以支持到期查询
CREATE INDEX IF NOT EXISTS `idx_status_record_end_time` ON `content_user_status_record` (`target_status`, `effective_end_time`);
