-- ============================================================
-- 圈子成长激励体系 — 修复徽章初始化数据
-- 问题：V3.9.1_67中SOCIAL_BUTTERFLY条件描述错误，V3.9.1_68使用INSERT IGNORE无法更新
-- ============================================================

-- 确保CONTENT_MILESTONE和SOCIAL_BUTTERFLY徽章存在，并修正SOCIAL_BUTTERFLY的条件描述
INSERT INTO `circle_achievement` (`id`, `achievement_type`, `name`, `description`, `condition_desc`) VALUES
('ach_005', 'CONTENT_MILESTONE', '内容里程碑', '累计发布50篇可见内容', '累计发布50篇可见内容'),
('ach_006', 'SOCIAL_BUTTERFLY', '社交达人', '邀请5人加入圈子', '邀请5人加入圈子')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`condition_desc` = VALUES(`condition_desc`);
