DROP TABLE IF EXISTS `content_user_profile_history`;
DROP TABLE IF EXISTS `content_user_verification_badge`;
DROP TABLE IF EXISTS `content_user_homepage_module`;
DROP TABLE IF EXISTS `content_user_profile_review`;

ALTER TABLE `content_user_privacy_setting`
  DROP COLUMN `contact_badge_visibility`,
  DROP COLUMN `verification_badge_visibility`,
  DROP COLUMN `personal_link_visibility`;

ALTER TABLE `content_user_profile`
  DROP COLUMN `profile_version`,
  DROP COLUMN `profile_review_status`,
  DROP COLUMN `profile_completion_state`;
