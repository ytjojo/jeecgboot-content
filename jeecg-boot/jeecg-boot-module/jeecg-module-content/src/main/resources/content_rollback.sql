-- =============================================
-- 内容社区系统回滚脚本 (增强版)
-- 用于清理和回滚所有内容模块的数据库更改
-- 作者: JeecgBoot团队
-- 创建时间: 2024-01-15
-- 更新时间: 2024-12-19
-- 版本: 2.0.0
-- =============================================

-- ⚠️  警告：此脚本将删除所有内容模块相关的表和数据，请谨慎使用！
-- 🔒 安全提示：执行前请务必备份数据库
-- 📋 建议：在测试环境先验证脚本执行效果

-- =============================================
-- 0. 安全检查和环境验证
-- =============================================

-- 检查MySQL版本
SELECT VERSION() as '当前MySQL版本';

-- 检查当前数据库
SELECT DATABASE() as '当前数据库';

-- 检查是否存在内容模块表
SELECT 
    COUNT(*) as '内容模块表数量',
    GROUP_CONCAT(table_name) as '表列表'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND (
    table_name LIKE 'content%' 
    OR table_name LIKE 'community%'
    OR table_name LIKE 'user_profile_extension'
    OR table_name LIKE 'media_files'
    OR table_name LIKE 'search_%'
    OR table_name LIKE 'channel%'
    OR table_name LIKE 'qa_%'
    OR table_name LIKE 'poll%'
);

-- 安全确认提示
SELECT '⚠️  即将开始回滚操作，请确认以下事项：' as '安全提示';
SELECT '1. 已备份重要数据' as '检查项1';
SELECT '2. 确认要删除所有内容模块数据' as '检查项2';
SELECT '3. 当前环境为测试环境或可安全操作' as '检查项3';
SELECT '如需继续，请手动执行后续SQL语句' as '操作说明';

-- 设置字符集和外键检查
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET sql_mode = 'TRADITIONAL';

-- 开始事务
START TRANSACTION;

-- =============================================
-- 1. 创建回滚日志表和记录回滚开始
-- =============================================

-- 创建回滚日志表（如果不存在）
CREATE TABLE IF NOT EXISTS content_rollback_log (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '回滚日志ID',
    rollback_session_id VARCHAR(32) NOT NULL COMMENT '回滚会话ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：DROP_VIEW, DROP_PROCEDURE, DROP_TABLE, DROP_DATA',
    object_name VARCHAR(100) NOT NULL COMMENT '对象名称',
    object_type VARCHAR(20) NOT NULL COMMENT '对象类型：VIEW, PROCEDURE, TABLE, DATA',
    operation_status INTEGER DEFAULT 0 COMMENT '操作状态：0-开始 1-成功 2-失败 3-跳过',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    affected_rows BIGINT DEFAULT 0 COMMENT '影响行数',
    operation_order INTEGER NOT NULL COMMENT '操作顺序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 生成回滚会话ID
SET @rollback_session_id = REPLACE(UUID(), '-', '');

-- 记录回滚开始
INSERT INTO content_rollback_log (id, rollback_session_id, operation_type, object_name, object_type, operation_status, operation_order) 
VALUES (REPLACE(UUID(), '-', ''), @rollback_session_id, 'ROLLBACK_START', 'content_rollback.sql', 'SCRIPT', 0, 0);

-- 显示回滚会话信息
SELECT 
    @rollback_session_id as '回滚会话ID',
    NOW() as '回滚开始时间',
    USER() as '执行用户',
    CONNECTION_ID() as '连接ID';

-- =============================================
-- 2. 删除视图（按依赖关系逆序删除）
-- =============================================

-- 记录删除视图操作开始
INSERT INTO content_rollback_log (id, rollback_session_id, operation_type, object_name, object_type, operation_status, operation_order) 
VALUES (REPLACE(UUID(), '-', ''), @rollback_session_id, 'DROP_VIEW', 'v_content_statistics', 'VIEW', 0, 1);

DROP VIEW IF EXISTS v_content_statistics;

-- 更新操作状态
UPDATE content_rollback_log 
SET operation_status = 1, end_time = CURRENT_TIMESTAMP 
WHERE rollback_session_id = @rollback_session_id AND object_name = 'v_content_statistics' AND operation_status = 0;

-- 删除用户活跃度统计视图
INSERT INTO content_rollback_log (id, rollback_session_id, operation_type, object_name, object_type, operation_status, operation_order) 
VALUES (REPLACE(UUID(), '-', ''), @rollback_session_id, 'DROP_VIEW', 'v_user_activity_statistics', 'VIEW', 0, 2);

DROP VIEW IF EXISTS v_user_activity_statistics;

UPDATE content_rollback_log 
SET operation_status = 1, end_time = CURRENT_TIMESTAMP 
WHERE rollback_session_id = @rollback_session_id AND object_name = 'v_user_activity_statistics' AND operation_status = 0;

-- 删除社区统计视图
INSERT INTO content_rollback_log (id, rollback_session_id, operation_type, object_name, object_type, operation_status, operation_order) 
VALUES (REPLACE(UUID(), '-', ''), @rollback_session_id, 'DROP_VIEW', 'v_community_statistics', 'VIEW', 0, 3);

DROP VIEW IF EXISTS v_community_statistics;

UPDATE content_rollback_log 
SET operation_status = 1, end_time = CURRENT_TIMESTAMP 
WHERE rollback_session_id = @rollback_session_id AND object_name = 'v_community_statistics' AND operation_status = 0;

-- =============================================
-- 3. 删除存储过程（带错误处理）
-- =============================================

-- 定义存储过程列表
SET @procedures = 'check_table_exists,check_column_exists,check_index_exists,check_mysql_version,check_content_module_dependencies,check_content_community_dependencies,check_content_qa_dependencies,check_content_interaction_dependencies,check_content_channel_dependencies,check_content_search_dependencies,check_all_dependencies,check_data_integrity';

-- 删除存储过程的通用处理
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS drop_procedure_safe(IN proc_name VARCHAR(100), IN session_id VARCHAR(32), IN order_num INT)
BEGIN
    DECLARE proc_exists INT DEFAULT 0;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
    BEGIN
        UPDATE content_rollback_log 
        SET operation_status = 2, end_time = CURRENT_TIMESTAMP, error_message = 'Failed to drop procedure'
        WHERE rollback_session_id = session_id AND object_name = proc_name AND operation_status = 0;
    END;
    
    -- 检查存储过程是否存在
    SELECT COUNT(*) INTO proc_exists 
    FROM information_schema.routines 
    WHERE routine_schema = DATABASE() AND routine_name = proc_name AND routine_type = 'PROCEDURE';
    
    -- 记录操作开始
    INSERT INTO content_rollback_log (id, rollback_session_id, operation_type, object_name, object_type, operation_status, operation_order) 
    VALUES (REPLACE(UUID(), '-', ''), session_id, 'DROP_PROCEDURE', proc_name, 'PROCEDURE', 0, order_num);
    
    IF proc_exists > 0 THEN
        SET @sql = CONCAT('DROP PROCEDURE IF EXISTS ', proc_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 更新成功状态
        UPDATE content_rollback_log 
        SET operation_status = 1, end_time = CURRENT_TIMESTAMP 
        WHERE rollback_session_id = session_id AND object_name = proc_name AND operation_status = 0;
    ELSE
        -- 标记为跳过
        UPDATE content_rollback_log 
        SET operation_status = 3, end_time = CURRENT_TIMESTAMP, error_message = 'Procedure does not exist'
        WHERE rollback_session_id = session_id AND object_name = proc_name AND operation_status = 0;
    END IF;
END$$

DELIMITER ;

-- 执行删除存储过程
CALL drop_procedure_safe('check_table_exists', @rollback_session_id, 10);
CALL drop_procedure_safe('check_column_exists', @rollback_session_id, 11);
CALL drop_procedure_safe('check_index_exists', @rollback_session_id, 12);
CALL drop_procedure_safe('check_mysql_version', @rollback_session_id, 13);
CALL drop_procedure_safe('check_content_module_dependencies', @rollback_session_id, 14);
CALL drop_procedure_safe('check_content_community_dependencies', @rollback_session_id, 15);
CALL drop_procedure_safe('check_content_qa_dependencies', @rollback_session_id, 16);
CALL drop_procedure_safe('check_content_interaction_dependencies', @rollback_session_id, 17);
CALL drop_procedure_safe('check_content_channel_dependencies', @rollback_session_id, 18);
CALL drop_procedure_safe('check_content_search_dependencies', @rollback_session_id, 19);
CALL drop_procedure_safe('check_all_dependencies', @rollback_session_id, 20);
CALL drop_procedure_safe('check_data_integrity', @rollback_session_id, 21);

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS drop_procedure_safe;

-- =============================================
-- 4. 删除搜索模块表（content_search_init.sql）
-- =============================================

-- 创建通用的表删除存储过程
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS drop_table_safe(IN table_name VARCHAR(100), IN session_id VARCHAR(32), IN order_num INT)
BEGIN
    DECLARE table_exists INT DEFAULT 0;
    DECLARE row_count BIGINT DEFAULT 0;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
    BEGIN
        UPDATE content_rollback_log 
        SET operation_status = 2, end_time = CURRENT_TIMESTAMP, error_message = CONCAT('Failed to drop table: ', table_name)
        WHERE rollback_session_id = session_id AND object_name = table_name AND operation_status = 0;
    END;
    
    -- 检查表是否存在
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = table_name;
    
    -- 记录操作开始
    INSERT INTO content_rollback_log (id, rollback_session_id, operation_type, object_name, object_type, operation_status, operation_order) 
    VALUES (REPLACE(UUID(), '-', ''), session_id, 'DROP_TABLE', table_name, 'TABLE', 0, order_num);
    
    IF table_exists > 0 THEN
        -- 获取表中数据行数（用于日志记录）
        SET @count_sql = CONCAT('SELECT COUNT(*) FROM ', table_name, ' INTO @row_count');
        PREPARE count_stmt FROM @count_sql;
        EXECUTE count_stmt;
        DEALLOCATE PREPARE count_stmt;
        SET row_count = @row_count;
        
        -- 删除表
        SET @sql = CONCAT('DROP TABLE IF EXISTS ', table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 更新成功状态
        UPDATE content_rollback_log 
        SET operation_status = 1, end_time = CURRENT_TIMESTAMP, affected_rows = row_count
        WHERE rollback_session_id = session_id AND object_name = table_name AND operation_status = 0;
    ELSE
        -- 标记为跳过
        UPDATE content_rollback_log 
        SET operation_status = 3, end_time = CURRENT_TIMESTAMP, error_message = 'Table does not exist'
        WHERE rollback_session_id = session_id AND object_name = table_name AND operation_status = 0;
    END IF;
END$$

DELIMITER ;


-- =============================================
-- 新增缺失的表回滚代码（共 77 个表）
-- =============================================
CALL drop_table_safe('browse_records', @rollback_session_id, 200);
CALL drop_table_safe('channel_activity_logs', @rollback_session_id, 201);
CALL drop_table_safe('channel_categories', @rollback_session_id, 202);
CALL drop_table_safe('channel_content_relations', @rollback_session_id, 203);
CALL drop_table_safe('channel_invitations', @rollback_session_id, 204);
CALL drop_table_safe('channel_members', @rollback_session_id, 205);
CALL drop_table_safe('channel_permissions', @rollback_session_id, 206);
CALL drop_table_safe('channel_subscriptions', @rollback_session_id, 207);
CALL drop_table_safe('channels', @rollback_session_id, 208);
CALL drop_table_safe('comment_media_relations', @rollback_session_id, 209);
CALL drop_table_safe('comment_mentions', @rollback_session_id, 210);
CALL drop_table_safe('comments', @rollback_session_id, 211);
CALL drop_table_safe('communities', @rollback_session_id, 212);
CALL drop_table_safe('community_announcement_reads', @rollback_session_id, 213);
CALL drop_table_safe('community_announcements', @rollback_session_id, 214);
CALL drop_table_safe('community_invitations', @rollback_session_id, 215);
CALL drop_table_safe('community_join_requests', @rollback_session_id, 216);
CALL drop_table_safe('community_members', @rollback_session_id, 217);
CALL drop_table_safe('community_rules', @rollback_session_id, 218);
CALL drop_table_safe('community_statistics', @rollback_session_id, 219);
CALL drop_table_safe('content_ads', @rollback_session_id, 220);
CALL drop_table_safe('content_appeal_records', @rollback_session_id, 221);
CALL drop_table_safe('content_audit_records', @rollback_session_id, 222);
CALL drop_table_safe('content_audit_rules', @rollback_session_id, 223);
CALL drop_table_safe('content_drafts', @rollback_session_id, 224);
CALL drop_table_safe('content_inline_entities', @rollback_session_id, 225);
CALL drop_table_safe('content_media_relations', @rollback_session_id, 226);
CALL drop_table_safe('content_mentions', @rollback_session_id, 227);
CALL drop_table_safe('content_report_records', @rollback_session_id, 228);
CALL drop_table_safe('content_stats', @rollback_session_id, 229);
CALL drop_table_safe('content_stock_relations', @rollback_session_id, 230);
CALL drop_table_safe('content_tag_relations', @rollback_session_id, 231);
CALL drop_table_safe('content_tags', @rollback_session_id, 232);
CALL drop_table_safe('content_topic_relations', @rollback_session_id, 233);
CALL drop_table_safe('content_topics', @rollback_session_id, 234);
CALL drop_table_safe('content_versions', @rollback_session_id, 235);
CALL drop_table_safe('contents', @rollback_session_id, 236);
CALL drop_table_safe('forbidden_words', @rollback_session_id, 237);
CALL drop_table_safe('media_files', @rollback_session_id, 238);
CALL drop_table_safe('moderation_logs', @rollback_session_id, 239);
CALL drop_table_safe('moderation_queue', @rollback_session_id, 240);
CALL drop_table_safe('moderation_templates', @rollback_session_id, 241);
CALL drop_table_safe('moderator_stats', @rollback_session_id, 242);
CALL drop_table_safe('notification_settings', @rollback_session_id, 243);
CALL drop_table_safe('notification_templates', @rollback_session_id, 244);
CALL drop_table_safe('notifications', @rollback_session_id, 245);
CALL drop_table_safe('poll_options', @rollback_session_id, 246);
CALL drop_table_safe('polls', @rollback_session_id, 247);
CALL drop_table_safe('push_devices', @rollback_session_id, 248);
CALL drop_table_safe('qa_extended_stats', @rollback_session_id, 249);
CALL drop_table_safe('qa_follows', @rollback_session_id, 250);
CALL drop_table_safe('qa_invitations', @rollback_session_id, 251);
CALL drop_table_safe('qa_relations', @rollback_session_id, 252);
CALL drop_table_safe('qa_rewards', @rollback_session_id, 253);
CALL drop_table_safe('qa_topic_relations', @rollback_session_id, 254);
CALL drop_table_safe('report_records', @rollback_session_id, 255);
CALL drop_table_safe('search_configs', @rollback_session_id, 256);
CALL drop_table_safe('search_indexes', @rollback_session_id, 257);
CALL drop_table_safe('search_records', @rollback_session_id, 258);
CALL drop_table_safe('search_result_clicks', @rollback_session_id, 259);
CALL drop_table_safe('search_statistics', @rollback_session_id, 260);
CALL drop_table_safe('search_suggestions', @rollback_session_id, 261);
CALL drop_table_safe('search_templates', @rollback_session_id, 262);
CALL drop_table_safe('temp_batch_reactions', @rollback_session_id, 263);
CALL drop_table_safe('trending_keywords', @rollback_session_id, 264);
CALL drop_table_safe('user_collections', @rollback_session_id, 265);
CALL drop_table_safe('user_poll_votes', @rollback_session_id, 266);
CALL drop_table_safe('user_profile_extension', @rollback_session_id, 267);
CALL drop_table_safe('user_punishments', @rollback_session_id, 268);
CALL drop_table_safe('user_reactions', @rollback_session_id, 269);
CALL drop_table_safe('user_relation', @rollback_session_id, 270);
CALL drop_table_safe('user_relation_stats', @rollback_session_id, 271);
CALL drop_table_safe('user_search_history', @rollback_session_id, 272);
CALL drop_table_safe('user_stats', @rollback_session_id, 273);
CALL drop_table_safe('user_tag_follows', @rollback_session_id, 274);
CALL drop_table_safe('user_topic_follows', @rollback_session_id, 275);
CALL drop_table_safe('websocket_connections', @rollback_session_id, 276);
CALL drop_table_safe('user_relation_operation_logs', @rollback_session_id, 277);

-- =============================================
-- 11. 删除临时存储过程
-- =============================================
DROP PROCEDURE IF EXISTS drop_table_safe;

-- =============================================
-- 12. 删除日志表（最后删除）
-- =============================================
-- 记录回滚完成状态
UPDATE content_rollback_log 
SET operation_status = 1, end_time = CURRENT_TIMESTAMP 
WHERE rollback_session_id = @rollback_session_id AND operation_type = 'ROLLBACK_START';

-- 显示回滚结果统计
SELECT 
    operation_type,
    object_type,
    COUNT(*) as total_operations,
    SUM(CASE WHEN operation_status = 1 THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN operation_status = 2 THEN 1 ELSE 0 END) as failed,
    SUM(CASE WHEN operation_status = 3 THEN 1 ELSE 0 END) as skipped,
    SUM(IFNULL(affected_rows, 0)) as total_affected_rows
FROM content_rollback_log 
WHERE rollback_session_id = @rollback_session_id
GROUP BY operation_type, object_type
ORDER BY operation_type, object_type;

-- 显示失败的操作详情
SELECT 
    operation_type,
    object_name,
    object_type,
    error_message,
    start_time
FROM content_rollback_log 
WHERE rollback_session_id = @rollback_session_id AND operation_status = 2
ORDER BY start_time;

-- 保留日志表供查询，不删除
-- DROP TABLE IF EXISTS content_rollback_log;

-- 提交事务
COMMIT;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 11. 显示回滚结果
-- =============================================
SELECT '内容社区系统数据库回滚完成！' as '回滚结果';
SELECT '所有内容模块相关的表、视图、存储过程已被删除' as '操作说明';

-- =============================================
-- 12. 验证回滚结果
-- =============================================
SELECT 
    COUNT(*) as '剩余内容相关表数量'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND (
    table_name LIKE 'content%' 
    OR table_name LIKE 'community%'
    OR table_name LIKE 'user_profile_extension'
    OR table_name LIKE 'media_files'
    OR table_name LIKE 'search_%'
    OR table_name LIKE 'channel%'
    OR table_name LIKE 'qa_%'
    OR table_name LIKE 'poll%'
);

-- 显示剩余的相关表（如果有的话）
SELECT 
    table_name as '剩余表名',
    table_comment as '表注释'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND (
    table_name LIKE 'content%' 
    OR table_name LIKE 'community%'
    OR table_name LIKE 'user_profile_extension'
    OR table_name LIKE 'media_files'
    OR table_name LIKE 'search_%'
    OR table_name LIKE 'channel%'
    OR table_name LIKE 'qa_%'
    OR table_name LIKE 'poll%'
)
ORDER BY table_name;

-- =============================================
-- 脚本说明
-- =============================================
/*
回滚脚本使用说明：

1. 执行前准备：
   - 确保有足够的数据库权限
   - 强烈建议先备份数据库
   - 确认要删除的数据不再需要

2. 回滚顺序：
   - 先删除视图和存储过程
   - 按模块依赖关系逆序删除表
   - 最后删除日志表

3. 删除的模块：
   - 搜索模块 (search_*)
   - 频道模块 (channel_*)
   - 互动模块 (content_interaction_*, content_likes, content_comments等)
   - 问答模块 (qa_*)
   - 社区模块 (community_*, user_profile_extension)
   - 核心内容模块 (content_*, media_files, polls等)

4. 注意事项：
   - 此脚本不会删除JeecgBoot系统核心表（如sys_user等）
   - 删除操作不可逆，请谨慎使用
   - 如果某些表被其他模块引用，可能删除失败

5. 重新初始化：
   - 回滚完成后，可以重新执行content_init_master.sql进行初始化
   - 建议先执行依赖检查脚本验证环境

6. 故障排除：
   - 如果删除失败，检查是否有外键约束
   - 可以手动设置SET FOREIGN_KEY_CHECKS = 0;
   - 检查是否有其他应用正在使用这些表
*/