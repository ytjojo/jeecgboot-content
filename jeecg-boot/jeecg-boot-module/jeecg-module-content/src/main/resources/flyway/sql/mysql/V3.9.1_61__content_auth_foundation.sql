-- 1. 用户账号主表
CREATE TABLE IF NOT EXISTS `content_user_account` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `account_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态: ACTIVE/CANCELLING/CANCELLED',
  `cancellation_status` varchar(32) DEFAULT NULL COMMENT '注销状态',
  `cancel_apply_time` datetime DEFAULT NULL COMMENT '注销申请时间',
  `cancel_complete_time` datetime DEFAULT NULL COMMENT '注销完成时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_location` varchar(128) DEFAULT NULL COMMENT '最后登录地点',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `locked_until` datetime DEFAULT NULL COMMENT '锁定截止时间',
  `risk_level` int NOT NULL DEFAULT 0 COMMENT '风险等级 0-100',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_account_user_id` (`user_id`),
  KEY `idx_content_user_account_status` (`account_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户账号';

-- 2. 用户登录凭证表
CREATE TABLE IF NOT EXISTS `content_user_credential` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `credential_type` varchar(32) NOT NULL COMMENT '凭证类型: MOBILE/EMAIL/PASSWORD',
  `credential_value` varchar(255) NOT NULL COMMENT '凭证值(手机号/邮箱/密码哈希)',
  `salt` varchar(64) DEFAULT NULL COMMENT '密码盐值',
  `verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已验证',
  `verify_time` datetime DEFAULT NULL COMMENT '验证时间',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_user_credential_type_value` (`credential_type`, `credential_value`),
  KEY `idx_content_user_credential_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户登录凭证';

-- 3. 第三方授权表扩展字段
ALTER TABLE `content_user_third_party_auth`
  ADD COLUMN `open_id` varchar(128) DEFAULT NULL COMMENT '第三方开放ID',
  ADD COLUMN `union_id` varchar(128) DEFAULT NULL COMMENT '第三方联合ID',
  ADD COLUMN `nickname` varchar(64) DEFAULT NULL COMMENT '第三方昵称',
  ADD COLUMN `avatar` varchar(500) DEFAULT NULL COMMENT '第三方头像',
  ADD COLUMN `raw_data_json` text DEFAULT NULL COMMENT '原始授权数据JSON摘要';

ALTER TABLE `content_user_third_party_auth`
  ADD KEY `idx_content_user_third_party_open_id` (`app_name`, `open_id`);

-- 4. 密码历史记录表
CREATE TABLE IF NOT EXISTS `content_user_password_history` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `salt` varchar(64) DEFAULT NULL COMMENT '盐值',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_pwd_history_user` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户密码历史';
