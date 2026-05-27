-- 1. 邀请码表
CREATE TABLE IF NOT EXISTS `content_invite_code` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `invite_code` varchar(8) NOT NULL COMMENT '邀请码（8位字母数字）',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  UNIQUE KEY `uk_user_invite` (`user_id`),
  KEY `idx_invite_code_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区邀请码';

-- 2. 邀请记录表
CREATE TABLE IF NOT EXISTS `content_invite_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `inviter_user_id` varchar(32) NOT NULL COMMENT '邀请人用户ID',
  `invitee_user_id` varchar(32) NOT NULL COMMENT '被邀请人用户ID',
  `invite_code` varchar(8) NOT NULL COMMENT '使用的邀请码',
  `registered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `reward_point` int DEFAULT 0 COMMENT '发放积分',
  `reward_status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '奖励状态: PENDING/SENT/SKIPPED',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitee_user` (`invitee_user_id`),
  KEY `idx_inviter_user` (`inviter_user_id`),
  KEY `idx_invite_record_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区邀请记录';

-- 3. 粉丝趋势日聚合表
CREATE TABLE IF NOT EXISTS `content_fan_trend_daily` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '统计日期',
  `new_follower_count` int NOT NULL DEFAULT 0 COMMENT '当日新增粉丝数',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `date`),
  KEY `idx_fan_trend_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区粉丝趋势日聚合';

-- 4. 用户资料表新增社区角色字段
ALTER TABLE `content_user_profile`
  ADD COLUMN `community_role` varchar(32) DEFAULT 'NORMAL' COMMENT '社区角色: NORMAL/CREATOR/MODERATOR/ADMIN';
