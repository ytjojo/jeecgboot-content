-- =====================================================
-- PostgreSQL版本：用户档案扩展表自动同步触发器和存储过程
-- 功能：当sys_user表插入新用户时，自动在user_profile_extension表创建对应记录
-- 作者：system
-- 创建时间：2024-01-20
-- =====================================================

-- 1. 创建存储过程：插入用户档案扩展记录
CREATE OR REPLACE FUNCTION sp_insert_user_profile_extension(
    p_user_id VARCHAR(32),
    p_username VARCHAR(100),
    p_realname VARCHAR(100),
    p_avatar VARCHAR(255),
    p_create_by VARCHAR(32),
    p_create_time TIMESTAMP
) RETURNS VOID AS $$
DECLARE
    v_count INTEGER := 0;
    v_nickname VARCHAR(100);
BEGIN
    -- 检查记录是否已存在，避免重复插入（现在检查id字段）
    SELECT COUNT(*) INTO v_count 
    FROM user_profile_extension 
    WHERE id = p_user_id;
    
    -- 如果记录不存在，则插入新记录
    IF v_count = 0 THEN
        -- 设置昵称，优先使用realname，否则使用username
        v_nickname := COALESCE(NULLIF(p_realname, ''), p_username);
        
        INSERT INTO user_profile_extension (
            id,
            nickname,
            avatar,
            bio,
            website,
            tags,
            interests,
            social_links,
            privacy_settings,
            notification_settings,
            del_flag,
            ext_data,
            create_by,
            create_time,
            update_by,
            update_time,
            last_active_time
        ) VALUES (
            p_user_id,                 -- 直接使用sys_user的id作为主键
            v_nickname,
            COALESCE(p_avatar, ''),
            '',                        -- bio 个人简介
            '',                        -- website 个人网站
            '[]'::JSONB,               -- tags 标签JSON数组
            '[]'::JSONB,               -- interests 兴趣JSON数组
            '{}'::JSONB,               -- social_links 社交链接JSON对象
            '{}'::JSONB,               -- privacy_settings 隐私设置JSON对象
            '{}'::JSONB,               -- notification_settings 通知设置JSON对象
            0,                         -- del_flag 删除标志：0-正常，1-已删除
            '{}'::JSONB,               -- ext_data 扩展数据JSON对象
            p_create_by,               -- create_by 创建人
            p_create_time,             -- create_time 创建时间
            p_create_by,               -- update_by 更新人
            p_create_time,             -- update_time 更新时间
            p_create_time              -- last_active_time 最后活跃时间
        );
        
        -- 记录日志（可选）
        INSERT INTO sys_log (
            id,
            log_type,
            log_content,
            operate_type,
            userid,
            username,
            ip,
            method,
            request_url,
            request_param,
            request_type,
            cost_time,
            create_by,
            create_time,
            update_by,
            update_time
        ) VALUES (
            REPLACE(gen_random_uuid()::TEXT, '-', ''),
            2,                         -- log_type: 1-登录日志，2-操作日志
            CONCAT('自动创建用户档案扩展记录，用户ID：', p_user_id),
            1,                         -- operate_type: 1-添加
            p_user_id,
            p_username,
            '127.0.0.1',
            'TRIGGER',
            '/system/user/add',
            '',
            'POST',
            0,
            'system',
            CURRENT_TIMESTAMP,
            'system',
            CURRENT_TIMESTAMP
        );
        
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        -- 记录错误日志
        RAISE NOTICE '创建用户档案扩展记录失败，用户ID：%, 错误：%', p_user_id, SQLERRM;
END;
$$ LANGUAGE plpgsql;

-- 2. 创建触发器函数
CREATE OR REPLACE FUNCTION fn_sys_user_after_insert()
RETURNS TRIGGER AS $$
BEGIN
    -- 调用存储过程创建用户档案扩展记录
    PERFORM sp_insert_user_profile_extension(
        NEW.id,
        NEW.username,
        NEW.realname,
        NEW.avatar,
        NEW.create_by,
        NEW.create_time
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. 创建触发器：监听sys_user表的INSERT操作
DROP TRIGGER IF EXISTS tr_sys_user_after_insert ON sys_user;

CREATE TRIGGER tr_sys_user_after_insert
    AFTER INSERT ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION fn_sys_user_after_insert();

-- 4. 验证脚本（可选执行）
-- 查看触发器是否创建成功
-- SELECT * FROM information_schema.triggers WHERE trigger_name = 'tr_sys_user_after_insert';

-- 查看存储过程是否创建成功
-- SELECT * FROM information_schema.routines WHERE routine_name = 'sp_insert_user_profile_extension';

-- 测试触发器功能（请谨慎执行，会插入测试数据）
/*
INSERT INTO sys_user (
    id, username, realname, password, salt, avatar, birthday, sex, email, phone, 
    org_code, status, del_flag, third_id, third_type, activiti_sync, work_no, 
    post, telephone, create_by, create_time, update_by, update_time, user_identity, 
    depart_ids, rel_tenant_ids, client_id
) VALUES (
    REPLACE(gen_random_uuid()::TEXT, '-', ''),
    'test_trigger_user_pg',
    '触发器测试用户PG',
    'e10adc3949ba59abbe56e057f20f883e',  -- 123456的MD5
    'RCGTeGiH',
    '',
    NULL,
    1,
    'test_pg@example.com',
    '13800138001',
    'A01',
    1,
    0,
    NULL,
    NULL,
    1,
    'TEST002',
    '',
    '',
    'admin',
    CURRENT_TIMESTAMP,
    'admin',
    CURRENT_TIMESTAMP,
    1,
    '',
    '',
    ''
);
*/

-- 使用说明：
-- 1. 执行此脚本前，请确保user_profile_extension表已存在
-- 2. PostgreSQL需要启用uuid-ossp扩展：CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- 3. 或者使用gen_random_uuid()函数（PostgreSQL 13+）
-- 4. 触发器会在每次向sys_user表插入记录时自动执行
-- 5. 存储过程会检查是否已存在相同user_id的记录，避免重复插入
-- 6. 如需删除触发器：DROP TRIGGER IF EXISTS tr_sys_user_after_insert ON sys_user;
-- 7. 如需删除触发器函数：DROP FUNCTION IF EXISTS fn_sys_user_after_insert();
-- 8. 如需删除存储过程：DROP FUNCTION IF EXISTS sp_insert_user_profile_extension(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP);