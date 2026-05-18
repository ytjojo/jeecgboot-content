DROP TABLE IF EXISTS `content_subscription_notification_preference`;
DROP TABLE IF EXISTS `content_subscription_source`;
DROP TABLE IF EXISTS `content_user_follow_recommendation`;
DROP TABLE IF EXISTS `content_user_activity_snapshot`;
DROP TABLE IF EXISTS `content_user_feed_setting`;

ALTER TABLE `content_user_notification_setting`
  DROP COLUMN `subscription_default_frequency`,
  DROP COLUMN `subscription_default_channels`,
  DROP COLUMN `subscription_notice_enabled`;

ALTER TABLE `content_user_subscription`
  DROP KEY `idx_content_user_subscription_source_status`,
  DROP KEY `idx_content_user_subscription_user_status`,
  DROP COLUMN `last_update_time`,
  DROP COLUMN `subscribed_at`,
  DROP COLUMN `subscription_status`;

ALTER TABLE `content_user_relation_group`
  DROP KEY `idx_content_user_relation_group_sort`,
  DROP KEY `uk_content_user_relation_group_name`,
  DROP COLUMN `group_status`;

ALTER TABLE `content_user_relation`
  DROP KEY `idx_content_user_relation_status`,
  DROP KEY `idx_content_user_relation_special`,
  DROP KEY `idx_content_user_relation_follow_group`,
  DROP COLUMN `last_interaction_time`,
  DROP COLUMN `relation_status`;
