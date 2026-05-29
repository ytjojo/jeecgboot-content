-- 回滚：删除密码历史记录表
DROP TABLE IF EXISTS `content_user_password_history`;

-- 回滚：撤销第三方授权表扩展字段
ALTER TABLE `content_user_third_party_auth`
  DROP INDEX `idx_content_user_third_party_open_id`;

ALTER TABLE `content_user_third_party_auth`
  DROP COLUMN `open_id`,
  DROP COLUMN `union_id`,
  DROP COLUMN `nickname`,
  DROP COLUMN `avatar`,
  DROP COLUMN `raw_data_json`;

-- 回滚：删除用户登录凭证表
DROP TABLE IF EXISTS `content_user_credential`;

-- 回滚：删除用户账号主表
DROP TABLE IF EXISTS `content_user_account`;
