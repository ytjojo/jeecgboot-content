-- 1. 在线状态可见性扩展：新增 VARCHAR 列，旧 Boolean 数据迁移
ALTER TABLE `content_user_privacy_setting`
  ADD COLUMN `online_status_visibility` varchar(32) DEFAULT 'PUBLIC' COMMENT '在线状态可见性：PUBLIC/HIDDEN/MUTUAL_ONLY';

UPDATE `content_user_privacy_setting`
   SET `online_status_visibility` = CASE WHEN `online_status_visible` = 0 THEN 'HIDDEN' ELSE 'PUBLIC' END
 WHERE `online_status_visible` IS NOT NULL;

-- 2. 第三方授权管理表
CREATE TABLE IF NOT EXISTS `content_user_third_party_auth` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `app_name` varchar(128) NOT NULL COMMENT '第三方应用名称',
  `auth_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `scopes` varchar(512) DEFAULT NULL COMMENT '授权范围(JSON数组)',
  `token_hash` varchar(256) DEFAULT NULL COMMENT 'Access Token 哈希',
  `refresh_token_hash` varchar(256) DEFAULT NULL COMMENT 'Refresh Token 哈希',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '授权状态: ACTIVE/REVOKED',
  `revoked_at` datetime DEFAULT NULL COMMENT '撤销时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_third_party_auth_user` (`user_id`, `status`),
  KEY `idx_third_party_auth_app` (`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户第三方授权记录';

-- 3. 通知审计日志表
CREATE TABLE IF NOT EXISTS `content_notification_audit_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `notice_type` varchar(32) NOT NULL COMMENT '通知类型',
  `channel` varchar(32) DEFAULT NULL COMMENT '通知渠道',
  `decision` varchar(16) NOT NULL COMMENT '发送决策: SEND/SKIP',
  `reason` varchar(128) DEFAULT NULL COMMENT '决策原因',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_audit_log_user` (`user_id`, `create_time`),
  KEY `idx_audit_log_type` (`notice_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区通知审计日志';

-- 4. 浏览历史可见性
ALTER TABLE `content_user_privacy_setting`
  ADD COLUMN `browse_history_visibility` varchar(32) DEFAULT 'PRIVATE' COMMENT '浏览历史可见性：PUBLIC/FOLLOWERS_ONLY/MUTUAL_ONLY/PRIVATE';

-- 5. 点赞活动可见性
ALTER TABLE `content_user_privacy_setting`
  ADD COLUMN `like_activity_visibility` varchar(32) DEFAULT 'PRIVATE' COMMENT '点赞活动可见性：PUBLIC/FOLLOWERS_ONLY/MUTUAL_ONLY/PRIVATE';

-- 6. 收藏可见性
ALTER TABLE `content_user_privacy_setting`
  ADD COLUMN `favorite_visibility` varchar(32) DEFAULT 'PRIVATE' COMMENT '收藏可见性：PUBLIC/FOLLOWERS_ONLY/MUTUAL_ONLY/PRIVATE';
