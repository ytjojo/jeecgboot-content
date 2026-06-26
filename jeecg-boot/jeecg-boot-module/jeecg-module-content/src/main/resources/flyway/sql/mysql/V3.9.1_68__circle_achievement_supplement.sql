-- ============================================================
-- 圈子成长激励体系 — 补充徽章与邀请追踪
-- ============================================================

-- 1. 圈子邀请记录表
CREATE TABLE IF NOT EXISTS `circle_invite_record` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `inviter_id` varchar(32) NOT NULL COMMENT '邀请人用户ID',
    `invitee_id` varchar(32) DEFAULT NULL COMMENT '被邀请人用户ID',
    `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '邀请状态: PENDING/JOINED/EXPIRED',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_circle_invite_inviter` (`circle_id`, `inviter_id`),
    KEY `idx_circle_invite_invitee` (`circle_id`, `invitee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子邀请记录';

-- 2. 补充成就徽章配置：内容里程碑、社交达人
INSERT IGNORE INTO `circle_achievement` (`id`, `achievement_type`, `name`, `description`, `condition_desc`) VALUES
('ach_005', 'CONTENT_MILESTONE', '内容里程碑', '累计发布50篇可见内容', '累计发布50篇可见内容'),
('ach_006', 'SOCIAL_BUTTERFLY', '社交达人', '邀请5人加入圈子', '邀请5人加入圈子');
