ALTER TABLE `content_user_growth_ledger`
  DROP INDEX `idx_content_user_growth_ledger_event`,
  DROP COLUMN `rule_snapshot_json`,
  DROP COLUMN `event_id`,
  DROP COLUMN `source_description`;

ALTER TABLE `content_user_point_ledger`
  DROP INDEX `idx_content_user_point_ledger_event`,
  DROP COLUMN `rule_snapshot_json`,
  DROP COLUMN `event_id`,
  DROP COLUMN `source_description`;

ALTER TABLE `content_user_badge_grant`
  DROP INDEX `idx_content_user_badge_grant_display`,
  DROP COLUMN `recycle_reason`,
  DROP COLUMN `recycled_by`,
  DROP COLUMN `display_order`;

ALTER TABLE `content_user_badge_definition`
  DROP INDEX `idx_content_user_badge_definition_category`,
  DROP COLUMN `sort_order`,
  DROP COLUMN `condition_description`,
  DROP COLUMN `effect_key`,
  DROP COLUMN `icon_url`,
  DROP COLUMN `category`;

DROP TABLE IF EXISTS `content_user_growth_decay_state`;
DROP TABLE IF EXISTS `content_user_virtual_gift_record`;
DROP TABLE IF EXISTS `content_user_feature_unlock`;
DROP TABLE IF EXISTS `content_user_exchange_order`;
DROP TABLE IF EXISTS `content_user_exchange_goods`;
DROP TABLE IF EXISTS `content_user_level_benefit_config`;
DROP TABLE IF EXISTS `content_user_level_config`;
DROP TABLE IF EXISTS `content_user_reward_event`;
DROP TABLE IF EXISTS `content_user_reward_rule`;
