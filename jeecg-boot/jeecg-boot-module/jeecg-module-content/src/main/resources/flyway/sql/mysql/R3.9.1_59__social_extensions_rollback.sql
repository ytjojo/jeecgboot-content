-- 回滚：删除新增表和列
DROP TABLE IF EXISTS `content_invite_record`;
DROP TABLE IF EXISTS `content_invite_code`;
DROP TABLE IF EXISTS `content_fan_trend_daily`;
ALTER TABLE `content_user_profile` DROP COLUMN IF EXISTS `community_role`;
