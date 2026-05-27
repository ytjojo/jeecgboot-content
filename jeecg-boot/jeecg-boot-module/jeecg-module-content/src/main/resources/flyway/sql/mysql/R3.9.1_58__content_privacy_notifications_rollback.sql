-- 回滚：删除新增表和列
DROP TABLE IF EXISTS `content_notification_audit_log`;
DROP TABLE IF EXISTS `content_user_third_party_auth`;
ALTER TABLE `content_user_privacy_setting` DROP COLUMN IF EXISTS `online_status_visibility`;
ALTER TABLE `content_user_privacy_setting` DROP COLUMN IF EXISTS `browse_history_visibility`;
ALTER TABLE `content_user_privacy_setting` DROP COLUMN IF EXISTS `like_activity_visibility`;
ALTER TABLE `content_user_privacy_setting` DROP COLUMN IF EXISTS `favorite_visibility`;
