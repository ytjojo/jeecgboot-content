-- V3.9.1_69__channel_lifecycle_stats.sql
-- 频道统计汇总表
CREATE TABLE IF NOT EXISTS `channel_stats` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(20) NOT NULL COMMENT '统计类型：daily/weekly/monthly',
    `subscriber_count` INT UNSIGNED DEFAULT 0 COMMENT '订阅数',
    `content_count` INT UNSIGNED DEFAULT 0 COMMENT '内容数',
    `pv` BIGINT UNSIGNED DEFAULT 0 COMMENT '浏览量',
    `uv` BIGINT UNSIGNED DEFAULT 0 COMMENT '访客数',
    `like_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `comment_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '评论数',
    `favorite_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '收藏数',
    `share_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '分享数',
    `effective_visit_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '有效访问数',
    `new_subscriber_count` INT UNSIGNED DEFAULT 0 COMMENT '新增订阅数',
    `lost_subscriber_count` INT UNSIGNED DEFAULT 0 COMMENT '流失订阅数',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_stat_date` (`stat_date`),
    UNIQUE KEY `uk_channel_date_type` (`channel_id`, `stat_date`, `stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道统计汇总表';

-- 频道导出任务表
CREATE TABLE IF NOT EXISTS `channel_export_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '导出用户ID',
    `export_type` VARCHAR(30) NOT NULL COMMENT '导出类型：core_stats/interaction/user_analysis',
    `file_format` VARCHAR(10) NOT NULL COMMENT '文件格式：xlsx/csv',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/processing/completed/failed',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `file_size` BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小(字节)',
    `row_count` INT UNSIGNED DEFAULT NULL COMMENT '数据行数',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `expire_time` DATETIME DEFAULT NULL COMMENT '文件过期时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道导出任务表';

-- 频道审核记录表
CREATE TABLE IF NOT EXISTS `channel_review` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `review_id` VARCHAR(64) NOT NULL COMMENT '审核ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `review_type` VARCHAR(30) NOT NULL COMMENT '审核类型：create/update_field/archive/merge',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/returned',
    `applicant_id` VARCHAR(64) NOT NULL COMMENT '申请人ID',
    `reviewer_id` VARCHAR(64) DEFAULT NULL COMMENT '审核人ID',
    `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
    `submit_time` DATETIME NOT NULL COMMENT '提交时间',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `timeout_flag` TINYINT(1) DEFAULT 0 COMMENT '是否超时：0-否 1-是',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_id` (`review_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道审核记录表';

-- 频道生命周期变更日志表
CREATE TABLE IF NOT EXISTS `channel_lifecycle_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `log_id` VARCHAR(64) NOT NULL COMMENT '日志ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `action_type` VARCHAR(30) NOT NULL COMMENT '操作类型：freeze/unfreeze/hide/restrict_recommend/close/archive/merge/delete',
    `from_status` VARCHAR(20) NOT NULL COMMENT '变更前状态',
    `to_status` VARCHAR(20) NOT NULL COMMENT '变更后状态',
    `operator_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '操作原因',
    `impact_scope` VARCHAR(200) DEFAULT NULL COMMENT '影响范围',
    `target_channel_id` VARCHAR(64) DEFAULT NULL COMMENT '目标频道ID(合并时)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_log_id` (`log_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_action_type` (`action_type`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道生命周期变更日志表';

-- 频道申诉记录表
CREATE TABLE IF NOT EXISTS `channel_appeal` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `appeal_id` VARCHAR(64) NOT NULL COMMENT '申诉ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `lifecycle_log_id` VARCHAR(64) NOT NULL COMMENT '关联的生命周期日志ID',
    `applicant_id` VARCHAR(64) NOT NULL COMMENT '申诉人ID',
    `appeal_reason` VARCHAR(1000) NOT NULL COMMENT '申诉理由',
    `attachment_urls` VARCHAR(2000) DEFAULT NULL COMMENT '附件URL(JSON数组)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/processing/approved/rejected',
    `handler_id` VARCHAR(64) DEFAULT NULL COMMENT '处理人ID',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `first_response_time` DATETIME DEFAULT NULL COMMENT '首次响应时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appeal_id` (`appeal_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道申诉记录表';
