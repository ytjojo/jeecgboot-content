-- Rollback: 圈子成长激励体系 — 补充徽章与邀请追踪
DROP TABLE IF EXISTS `circle_invite_record`;
DELETE FROM `circle_member_achievement` WHERE `achievement_type` IN ('CONTENT_MILESTONE', 'SOCIAL_BUTTERFLY');
DELETE FROM `circle_achievement` WHERE `achievement_type` IN ('CONTENT_MILESTONE', 'SOCIAL_BUTTERFLY');
