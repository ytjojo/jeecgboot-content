-- 回滚：删除索引和表
DROP INDEX IF EXISTS `idx_status_record_end_time` ON `content_user_status_record`;
DROP TABLE IF EXISTS `content_user_status_audit_log`;
