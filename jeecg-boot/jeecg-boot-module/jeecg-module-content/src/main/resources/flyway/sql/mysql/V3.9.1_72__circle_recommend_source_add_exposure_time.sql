-- 为圈子推荐来源表添加曝光时间字段
ALTER TABLE `circle_recommend_source` ADD COLUMN `exposure_time` datetime DEFAULT NULL COMMENT '曝光时间' AFTER `source_id`;
