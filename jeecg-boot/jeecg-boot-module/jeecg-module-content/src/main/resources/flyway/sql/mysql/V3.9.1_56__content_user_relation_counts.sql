ALTER TABLE `content_user_profile`
  ADD COLUMN `following_count` int NOT NULL DEFAULT 0 COMMENT '关注数' AFTER `growth_value`,
  ADD COLUMN `follower_count` int NOT NULL DEFAULT 0 COMMENT '粉丝数' AFTER `following_count`,
  ADD COLUMN `special_follow_count` int NOT NULL DEFAULT 0 COMMENT '特别关注数' AFTER `follower_count`;

UPDATE `content_user_profile` p
LEFT JOIN (
  SELECT `owner_user_id`, COUNT(1) AS `cnt`
  FROM `content_user_relation`
  WHERE `followed` = 1 AND `relation_status` = 'ACTIVE'
  GROUP BY `owner_user_id`
) r ON r.`owner_user_id` = p.`user_id`
SET p.`following_count` = IFNULL(r.`cnt`, 0);

UPDATE `content_user_profile` p
LEFT JOIN (
  SELECT `target_user_id`, COUNT(1) AS `cnt`
  FROM `content_user_relation`
  WHERE `followed` = 1 AND `relation_status` = 'ACTIVE'
  GROUP BY `target_user_id`
) r ON r.`target_user_id` = p.`user_id`
SET p.`follower_count` = IFNULL(r.`cnt`, 0);

UPDATE `content_user_profile` p
LEFT JOIN (
  SELECT `owner_user_id`, COUNT(1) AS `cnt`
  FROM `content_user_relation`
  WHERE `special_follow` = 1 AND `relation_status` = 'ACTIVE'
  GROUP BY `owner_user_id`
) r ON r.`owner_user_id` = p.`user_id`
SET p.`special_follow_count` = IFNULL(r.`cnt`, 0);
