-- 回滚：删除新增表、索引和列

-- 1. 删除注销申请表
DROP TABLE IF EXISTS `content_cancellation_request`;

-- 2. 删除风控事件表
DROP TABLE IF EXISTS `content_risk_event`;

-- 3. 删除 content_user_device_session 新增索引
ALTER TABLE `content_user_device_session`
  DROP INDEX IF EXISTS `idx_content_user_device_session_jti`,
  DROP INDEX IF EXISTS `idx_content_user_device_session_status`;

-- 4. 删除 content_user_device_session 新增列
ALTER TABLE `content_user_device_session`
  DROP COLUMN IF EXISTS `token_jti`,
  DROP COLUMN IF EXISTS `os_type`,
  DROP COLUMN IF EXISTS `os_version`,
  DROP COLUMN IF EXISTS `browser_type`,
  DROP COLUMN IF EXISTS `device_fingerprint`,
  DROP COLUMN IF EXISTS `trusted`,
  DROP COLUMN IF EXISTS `session_status`,
  DROP COLUMN IF EXISTS `offline_time`,
  DROP COLUMN IF EXISTS `offline_reason`;
