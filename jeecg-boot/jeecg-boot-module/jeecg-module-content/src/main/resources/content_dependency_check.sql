-- =============================================
-- 内容社区系统依赖关系检查脚本
-- 用于验证表结构依赖关系和执行前置条件
-- 作者: JeecgBoot团队
-- 创建时间: 2024-01-15
-- 版本: 1.0.0
-- =============================================

DELIMITER $$

-- =============================================
-- 1. 创建依赖检查存储过程
-- =============================================

-- 检查表是否存在的存储过程
DROP PROCEDURE IF EXISTS check_table_exists$$
CREATE PROCEDURE check_table_exists(
    IN table_name VARCHAR(100),
    IN error_message VARCHAR(500)
)
BEGIN
    DECLARE table_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = table_name;
    
    IF table_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_message;
    END IF;
END$$

-- 检查字段是否存在的存储过程
DROP PROCEDURE IF EXISTS check_column_exists$$
CREATE PROCEDURE check_column_exists(
    IN table_name VARCHAR(100),
    IN column_name VARCHAR(100),
    IN error_message VARCHAR(500)
)
BEGIN
    DECLARE column_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns 
    WHERE table_schema = DATABASE() 
    AND table_name = table_name 
    AND column_name = column_name;
    
    IF column_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_message;
    END IF;
END$$

-- 检查索引是否存在的存储过程
DROP PROCEDURE IF EXISTS check_index_exists$$
CREATE PROCEDURE check_index_exists(
    IN table_name VARCHAR(100),
    IN index_name VARCHAR(100),
    IN error_message VARCHAR(500)
)
BEGIN
    DECLARE index_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics 
    WHERE table_schema = DATABASE() 
    AND table_name = table_name 
    AND index_name = index_name;
    
    IF index_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_message;
    END IF;
END$$

-- 检查数据库版本的存储过程
DROP PROCEDURE IF EXISTS check_mysql_version$$
CREATE PROCEDURE check_mysql_version()
BEGIN
    DECLARE version_number DECIMAL(3,1);
    
    SELECT CAST(SUBSTRING_INDEX(VERSION(), '.', 2) AS DECIMAL(3,1)) INTO version_number;
    
    IF version_number < 5.7 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'MySQL版本必须 >= 5.7，当前版本过低';
    END IF;
END$$

-- =============================================
-- 2. 各模块依赖检查存储过程
-- =============================================

-- 检查content_module_init.sql的前置条件
DROP PROCEDURE IF EXISTS check_content_module_dependencies$$
CREATE PROCEDURE check_content_module_dependencies()
BEGIN
    -- 检查MySQL版本
    CALL check_mysql_version();
    
    -- 检查sys_user表是否存在（JeecgBoot系统表）
    CALL check_table_exists('sys_user', 'content_module_init.sql依赖sys_user表，请先初始化JeecgBoot系统模块');
    
    -- 检查必要的系统字段
    CALL check_column_exists('sys_user', 'id', 'sys_user表缺少id字段');
    CALL check_column_exists('sys_user', 'username', 'sys_user表缺少username字段');
    
    SELECT 'content_module_init.sql 依赖检查通过' as check_result;
END$$

-- 检查content_community_init.sql的前置条件
DROP PROCEDURE IF EXISTS check_content_community_dependencies$$
CREATE PROCEDURE check_content_community_dependencies()
BEGIN
    -- 检查基础表是否存在
    CALL check_table_exists('contents', 'content_community_init.sql依赖contents表，请先执行content_module_init.sql');
    CALL check_table_exists('sys_user', 'content_community_init.sql依赖sys_user表，请先初始化JeecgBoot系统模块');
    
    -- 检查关键字段
    CALL check_column_exists('contents', 'id', 'contents表缺少id字段');
    CALL check_column_exists('contents', 'author_id', 'contents表缺少author_id字段');
    
    SELECT 'content_community_init.sql 依赖检查通过' as check_result;
END$$

-- 检查content_qa_init.sql的前置条件
DROP PROCEDURE IF EXISTS check_content_qa_dependencies$$
CREATE PROCEDURE check_content_qa_dependencies()
BEGIN
    -- 检查基础表
    CALL check_table_exists('contents', 'content_qa_init.sql依赖contents表，请先执行content_module_init.sql');
    CALL check_table_exists('communities', 'content_qa_init.sql依赖communities表，请先执行content_community_init.sql');
    CALL check_table_exists('user_profile_extension', 'content_qa_init.sql依赖user_profile_extension表，请先执行content_community_init.sql');
    
    SELECT 'content_qa_init.sql 依赖检查通过' as check_result;
END$$

-- 检查content_Interaction_init.sql的前置条件
DROP PROCEDURE IF EXISTS check_content_interaction_dependencies$$
CREATE PROCEDURE check_content_interaction_dependencies()
BEGIN
    -- 检查基础表
    CALL check_table_exists('contents', 'content_Interaction_init.sql依赖contents表，请先执行content_module_init.sql');
    CALL check_table_exists('media_files', 'content_Interaction_init.sql依赖media_files表，请先执行content_module_init.sql');
    
    SELECT 'content_Interaction_init.sql 依赖检查通过' as check_result;
END$$

-- 检查content_channel_init.sql的前置条件
DROP PROCEDURE IF EXISTS check_content_channel_dependencies$$
CREATE PROCEDURE check_content_channel_dependencies()
BEGIN
    -- 频道模块相对独立，只需要基础的sys_user表
    CALL check_table_exists('sys_user', 'content_channel_init.sql依赖sys_user表，请先初始化JeecgBoot系统模块');
    
    SELECT 'content_channel_init.sql 依赖检查通过' as check_result;
END$$

-- 检查content_search_init.sql的前置条件
DROP PROCEDURE IF EXISTS check_content_search_dependencies$$
CREATE PROCEDURE check_content_search_dependencies()
BEGIN
    -- 检查依赖的表
    CALL check_table_exists('contents', 'content_search_init.sql依赖contents表，请先执行content_module_init.sql');
    CALL check_table_exists('user_profile_extension', 'content_search_init.sql依赖user_profile_extension表，请先执行content_community_init.sql');
    CALL check_table_exists('communities', 'content_search_init.sql依赖communities表，请先执行content_community_init.sql');
    
    SELECT 'content_search_init.sql 依赖检查通过' as check_result;
END$$

-- =============================================
-- 3. 综合依赖检查存储过程
-- =============================================

-- 执行所有依赖检查
DROP PROCEDURE IF EXISTS check_all_dependencies$$
CREATE PROCEDURE check_all_dependencies()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE error_occurred INT DEFAULT FALSE;
    DECLARE error_msg TEXT DEFAULT '';
    
    -- 异常处理
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET error_occurred = TRUE;
        GET DIAGNOSTICS CONDITION 1 error_msg = MESSAGE_TEXT;
    END;
    
    -- 创建检查结果临时表
    DROP TEMPORARY TABLE IF EXISTS dependency_check_results;
    CREATE TEMPORARY TABLE dependency_check_results (
        script_name VARCHAR(100),
        check_status VARCHAR(20),
        error_message TEXT,
        check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    -- 检查content_module_init.sql
    SET error_occurred = FALSE;
    CALL check_content_module_dependencies();
    IF error_occurred THEN
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_module_init.sql', 'FAILED', error_msg);
    ELSE
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_module_init.sql', 'PASSED', '');
    END IF;
    
    -- 检查content_community_init.sql
    SET error_occurred = FALSE;
    CALL check_content_community_dependencies();
    IF error_occurred THEN
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_community_init.sql', 'FAILED', error_msg);
    ELSE
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_community_init.sql', 'PASSED', '');
    END IF;
    
    -- 检查content_qa_init.sql
    SET error_occurred = FALSE;
    CALL check_content_qa_dependencies();
    IF error_occurred THEN
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_qa_init.sql', 'FAILED', error_msg);
    ELSE
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_qa_init.sql', 'PASSED', '');
    END IF;
    
    -- 检查content_Interaction_init.sql
    SET error_occurred = FALSE;
    CALL check_content_interaction_dependencies();
    IF error_occurred THEN
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_Interaction_init.sql', 'FAILED', error_msg);
    ELSE
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_Interaction_init.sql', 'PASSED', '');
    END IF;
    
    -- 检查content_channel_init.sql
    SET error_occurred = FALSE;
    CALL check_content_channel_dependencies();
    IF error_occurred THEN
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_channel_init.sql', 'FAILED', error_msg);
    ELSE
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_channel_init.sql', 'PASSED', '');
    END IF;
    
    -- 检查content_search_init.sql
    SET error_occurred = FALSE;
    CALL check_content_search_dependencies();
    IF error_occurred THEN
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_search_init.sql', 'FAILED', error_msg);
    ELSE
        INSERT INTO dependency_check_results (script_name, check_status, error_message) 
        VALUES ('content_search_init.sql', 'PASSED', '');
    END IF;
    
    -- 显示检查结果
    SELECT 
        script_name as '脚本名称',
        check_status as '检查状态',
        error_message as '错误信息',
        check_time as '检查时间'
    FROM dependency_check_results
    ORDER BY check_time;
    
    -- 统计结果
    SELECT 
        COUNT(*) as '总检查数',
        SUM(CASE WHEN check_status = 'PASSED' THEN 1 ELSE 0 END) as '通过数',
        SUM(CASE WHEN check_status = 'FAILED' THEN 1 ELSE 0 END) as '失败数'
    FROM dependency_check_results;
    
    -- 清理临时表
    DROP TEMPORARY TABLE dependency_check_results;
END$$

-- =============================================
-- 4. 数据完整性检查存储过程
-- =============================================

-- 检查数据完整性
DROP PROCEDURE IF EXISTS check_data_integrity$$
CREATE PROCEDURE check_data_integrity()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    
    -- 创建完整性检查结果表
    DROP TEMPORARY TABLE IF EXISTS integrity_check_results;
    CREATE TEMPORARY TABLE integrity_check_results (
        check_item VARCHAR(100),
        check_result VARCHAR(20),
        issue_count INT,
        description TEXT,
        check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    -- 检查contents表的author_id是否都存在于sys_user表中
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'contents') THEN
        INSERT INTO integrity_check_results (check_item, check_result, issue_count, description)
        SELECT 
            'contents.author_id外键完整性',
            CASE WHEN COUNT(*) = 0 THEN 'PASSED' ELSE 'FAILED' END,
            COUNT(*),
            CASE WHEN COUNT(*) = 0 THEN '所有作者ID都有效' ELSE CONCAT('发现', COUNT(*), '个无效的作者ID') END
        FROM contents c
        LEFT JOIN sys_user u ON c.author_id = u.id
        WHERE u.id IS NULL AND c.del_flag = 0;
    END IF;
    
    -- 检查community_members表的user_id完整性
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'community_members') THEN
        INSERT INTO integrity_check_results (check_item, check_result, issue_count, description)
        SELECT 
            'community_members.user_id外键完整性',
            CASE WHEN COUNT(*) = 0 THEN 'PASSED' ELSE 'FAILED' END,
            COUNT(*),
            CASE WHEN COUNT(*) = 0 THEN '所有用户ID都有效' ELSE CONCAT('发现', COUNT(*), '个无效的用户ID') END
        FROM community_members cm
        LEFT JOIN sys_user u ON cm.user_id = u.id
        WHERE u.id IS NULL AND cm.del_flag = 0;
    END IF;
    
    -- 检查user_profile_extension表的user_id唯一性
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user_profile_extension') THEN
        INSERT INTO integrity_check_results (check_item, check_result, issue_count, description)
        SELECT 
            'user_profile_extension.user_id唯一性',
            CASE WHEN COUNT(*) = 0 THEN 'PASSED' ELSE 'FAILED' END,
            COUNT(*),
            CASE WHEN COUNT(*) = 0 THEN '用户ID都是唯一的' ELSE CONCAT('发现', COUNT(*), '个重复的用户ID') END
        FROM (
            SELECT user_id, COUNT(*) as cnt
            FROM user_profile_extension 
            WHERE del_flag = 0
            GROUP BY user_id
            HAVING cnt > 1
        ) duplicates;
    END IF;
    
    -- 显示完整性检查结果
    SELECT 
        check_item as '检查项目',
        check_result as '检查结果',
        issue_count as '问题数量',
        description as '描述',
        check_time as '检查时间'
    FROM integrity_check_results
    ORDER BY check_time;
    
    -- 清理临时表
    DROP TEMPORARY TABLE integrity_check_results;
END$$

DELIMITER ;

-- =============================================
-- 使用示例
-- =============================================
/*
-- 检查所有依赖关系
CALL check_all_dependencies();

-- 检查特定模块依赖
CALL check_content_module_dependencies();
CALL check_content_community_dependencies();

-- 检查数据完整性
CALL check_data_integrity();

-- 检查单个表是否存在
CALL check_table_exists('contents', '缺少contents表');

-- 检查单个字段是否存在
CALL check_column_exists('contents', 'author_id', 'contents表缺少author_id字段');
*/