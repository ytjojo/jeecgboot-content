-- ============================================================
-- 1. 扩展 content_user_device_session 表：新增会话追踪与风控字段
-- ============================================================
ALTER TABLE `content_user_device_session`
  ADD COLUMN `token_jti` varchar(64) DEFAULT NULL COMMENT 'Token JTI唯一标识',
  ADD COLUMN `os_type` varchar(32) DEFAULT NULL COMMENT '操作系统类型',
  ADD COLUMN `os_version` varchar(32) DEFAULT NULL COMMENT '操作系统版本',
  ADD COLUMN `browser_type` varchar(32) DEFAULT NULL COMMENT '浏览器类型',
  ADD COLUMN `device_fingerprint` varchar(255) DEFAULT NULL COMMENT '设备指纹',
  ADD COLUMN `trusted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否信任设备',
  ADD COLUMN `session_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态: ACTIVE/OFFLINE/EXPIRED',
  ADD COLUMN `offline_time` datetime DEFAULT NULL COMMENT '下线时间',
  ADD COLUMN `offline_reason` varchar(64) DEFAULT NULL COMMENT '下线原因';

-- 新增索引
ALTER TABLE `content_user_device_session`
  ADD INDEX `idx_content_user_device_session_jti` (`token_jti`),
  ADD INDEX `idx_content_user_device_session_status` (`user_id`, `session_status`);

-- ============================================================
-- 2. 风控事件表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_risk_event` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '关联用户ID',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型: LOGIN_FAIL/BATCH_REGISTER/ABNORMAL_LOGIN/BRUTE_FORCE',
  `risk_level` int NOT NULL DEFAULT 0 COMMENT '风险等级 0-100',
  `risk_score` int DEFAULT NULL COMMENT '风险评分(预留模型)',
  `risk_reason` varchar(500) DEFAULT NULL COMMENT '风险原因',
  `decision` varchar(32) NOT NULL COMMENT '决策: ALLOW/CHALLENGE/BLOCK',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `device_fingerprint` varchar(255) DEFAULT NULL COMMENT '设备指纹',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'UserAgent',
  `extra_data_json` text DEFAULT NULL COMMENT '额外数据JSON',
  `resolved` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已处理',
  `resolved_by` varchar(32) DEFAULT NULL COMMENT '处理人',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理时间',
  `resolve_note` varchar(500) DEFAULT NULL COMMENT '处理说明',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_risk_event_user` (`user_id`, `event_type`),
  KEY `idx_content_risk_event_ip` (`ip_address`, `create_time`),
  KEY `idx_content_risk_event_unresolved` (`resolved`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区风控事件';

-- ============================================================
-- 3. 账号注销申请表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_cancellation_request` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/COMPLETED/CANCELLED',
  `apply_reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `cooldown_days` int NOT NULL DEFAULT 7 COMMENT '冷静期天数',
  `cooldown_deadline` datetime NOT NULL COMMENT '冷静期截止时间',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因(用户取消注销时)',
  `revoke_time` datetime DEFAULT NULL COMMENT '取消时间',
  `complete_time` datetime DEFAULT NULL COMMENT '最终注销完成时间',
  `operator_user_id` varchar(32) DEFAULT NULL COMMENT '最终操作人',
  `anonymized` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已匿名化',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_cancellation_user_status` (`user_id`, `status`),
  KEY `idx_content_cancellation_deadline` (`status`, `cooldown_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区账号注销申请';
