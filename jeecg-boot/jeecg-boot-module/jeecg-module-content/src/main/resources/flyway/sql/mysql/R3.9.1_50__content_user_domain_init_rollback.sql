-- 回滚 V3.9.1_50__content_user_domain_init
-- 删除 V3.9.1_50 创建的全部 15 张表（按依赖关系逆序）

DROP TABLE IF EXISTS `content_user_device_session`;
DROP TABLE IF EXISTS `content_user_audit_log`;
DROP TABLE IF EXISTS `content_user_report`;
DROP TABLE IF EXISTS `content_user_appeal`;
DROP TABLE IF EXISTS `content_user_status_record`;
DROP TABLE IF EXISTS `content_user_badge_grant`;
DROP TABLE IF EXISTS `content_user_badge_definition`;
DROP TABLE IF EXISTS `content_user_growth_ledger`;
DROP TABLE IF EXISTS `content_user_point_ledger`;
DROP TABLE IF EXISTS `content_user_notification_setting`;
DROP TABLE IF EXISTS `content_user_subscription`;
DROP TABLE IF EXISTS `content_user_relation_group`;
DROP TABLE IF EXISTS `content_user_relation`;
DROP TABLE IF EXISTS `content_user_privacy_setting`;
DROP TABLE IF EXISTS `content_user_profile`;
