-- =============================================
-- content_user_core_init.sql
-- 说明：内容模块用户核心表初始化脚本，集中创建以下三张功能关联表：
-- 1) user_profile_extension  用户资料扩展表
-- 2) user_relation           用户关系表（包含关注、订阅、屏蔽、拉黑、屏蔽搜索、屏蔽私信）
-- 3) user_relation_stats     用户关系统计表
-- 依赖：需先存在系统用户表 sys_user
-- 执行顺序：请在运行 content_community_init.sql 与 content_Interaction_init.sql 之前执行本脚本
-- =============================================

-- =============================================
-- 1. 用户资料扩展表 (user_profile_extension)
--   说明：存储用户在内容模块的扩展资料及统计字段，避免与系统用户表强耦合
-- =============================================
CREATE TABLE IF NOT EXISTS user_profile_extension (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '用户ID（关联sys_user.id）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像',
    bio TEXT COMMENT '个人简介',
    website VARCHAR(200) COMMENT '个人网站',
    location VARCHAR(100) COMMENT '所在地',
    occupation VARCHAR(100) COMMENT '职业',
    company VARCHAR(100) COMMENT '公司',
    school VARCHAR(100) COMMENT '学校',
    following_count BIGINT DEFAULT 0 COMMENT '关注数',
    followers_count BIGINT DEFAULT 0 COMMENT '粉丝数',
    likes_count BIGINT DEFAULT 0 COMMENT '获赞数',
    content_count BIGINT DEFAULT 0 COMMENT '发布内容数',
    points BIGINT DEFAULT 0 COMMENT '积分',
    level INTEGER DEFAULT 1 COMMENT '等级',
    verify_status INTEGER DEFAULT 0 COMMENT '认证状态：0-未认证 1-认证中 2-已认证 3-认证失败',
    verify_type INTEGER DEFAULT 0 COMMENT '认证类型：0-无 1-个人认证 2-大V认证 3-系统官方认证 4-企业认证 5-机构认证',
    verify_info VARCHAR(200) COMMENT '认证信息',
    is_vip INTEGER DEFAULT 0 COMMENT '是否VIP：0-否 1-是',
    vip_expire_time TIMESTAMP COMMENT 'VIP到期时间',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    last_active_time TIMESTAMP COMMENT '最后活跃时间',
    tags JSON COMMENT '个人标签（JSON数组格式）',
    interests JSON COMMENT '兴趣爱好（JSON数组格式）',
    social_links JSON COMMENT '社交媒体链接（JSON格式）',
    privacy_settings JSON COMMENT '隐私设置（JSON格式）',
    notification_settings JSON COMMENT '通知设置（JSON格式）',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    ext_data JSON COMMENT '扩展字段（JSON格式）',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 索引
CREATE INDEX  idx_user_profile_nickname ON user_profile_extension(nickname);
CREATE INDEX  idx_user_profile_verify_status ON user_profile_extension(verify_status);
CREATE INDEX  idx_user_profile_is_vip ON user_profile_extension(is_vip);
CREATE INDEX  idx_user_profile_level ON user_profile_extension(level);
CREATE INDEX  idx_user_profile_del_flag ON user_profile_extension(del_flag);


-- =============================================
-- 2. 用户关系表 (user_relation)
--   说明：统一管理用户间各种关系与屏蔽/拉黑控制，满足细粒度隐私与交互控制需求
--   重要说明：拉黑为组合操作，不对应单独字段；执行拉黑时需同时将以下字段置为1：
--            is_block_view、is_block_viewed、is_block_search、is_block_message；
--            取消拉黑则将上述字段统一置为0。
-- =============================================
CREATE TABLE IF NOT EXISTS user_relation (
    id VARCHAR(32) NOT NULL COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID（行为发起方）',
    target_user_id VARCHAR(32) NOT NULL COMMENT '目标用户ID（行为对象）',
    -- 关系控制（关注、订阅）
    is_follow TINYINT(1) DEFAULT 0 COMMENT '是否关注：1-关注，0-未关注',
    is_subscribe TINYINT(1) DEFAULT 0 COMMENT '是否订阅：1-订阅，0-未订阅（特别关注）',
    -- 可见性控制（不看TA、不让TA看）
    is_block_view TINYINT(1) DEFAULT 0 COMMENT '是否不看TA：1-屏蔽其内容，0-不屏蔽',
    is_block_viewed TINYINT(1) DEFAULT 0 COMMENT '是否不让TA看：1-限制其看我的内容，0-不限制',
    -- 黑名单与细粒度控制（搜索、私信）
    is_block_search TINYINT(1) DEFAULT 0 COMMENT '是否屏蔽搜索：1-屏蔽，0-不屏蔽',
    is_block_message TINYINT(1) DEFAULT 0 COMMENT '是否屏蔽私信：1-屏蔽，0-不屏蔽',
    -- 审计字段
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target (user_id, target_user_id),
    KEY idx_user_id (user_id),
    KEY idx_target_user_id (target_user_id),
    KEY idx_follow (is_follow),
    KEY idx_subscribe (is_subscribe),
    KEY idx_block_view (is_block_view),
    KEY idx_block_viewed (is_block_viewed),
    KEY idx_block_search (is_block_search),
    KEY idx_block_message (is_block_message),
    CONSTRAINT chk_user_relation_not_self CHECK (user_id != target_user_id)
) COMMENT='用户关系表（关注/订阅/屏蔽/拉黑/搜索与私信屏蔽）';


-- =============================================
-- 3. 用户关系统计表 (user_relation_stats)
--   说明：存储用户关系的聚合统计，支持快速查询与展示
-- =============================================
CREATE TABLE IF NOT EXISTS user_relation_stats (
    id VARCHAR(32) NOT NULL COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    -- 基本关系统计
    follow_count INTEGER DEFAULT 0 COMMENT '关注数（我关注的人数）',
    subscribe_count INTEGER DEFAULT 0 COMMENT '订阅数（我特别关注的人数）',
    fans_count INTEGER DEFAULT 0 COMMENT '粉丝数（关注我的人数）',
    subscribers_count INTEGER DEFAULT 0 COMMENT '订阅者数（特别关注我的人数）',
    -- 屏蔽与限制统计
    block_view_count INTEGER DEFAULT 0 COMMENT '不看TA数（我屏蔽他人数）',
    block_viewed_count INTEGER DEFAULT 0 COMMENT '不让TA看数（我限制他人数）',
    -- 黑名单与细粒度统计
    blacklist_count INTEGER DEFAULT 0 COMMENT '我拉黑人数',
    blacklisted_count INTEGER DEFAULT 0 COMMENT '拉黑我的人数',
    block_search_count INTEGER DEFAULT 0 COMMENT '屏蔽搜索人数（我不让他搜到我）',
    block_message_count INTEGER DEFAULT 0 COMMENT '屏蔽私信人数（我不让他给我发私信）',
    -- 审计字段
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) COMMENT='用户关系统计表';

-- =============================================
-- 4. 用户关系操作记录表 (user_relation_operation_logs)
--   说明：记录用户之间关系变更的操作日志，用于审计追踪、问题定位与合规要求；
--        含操作前/后状态快照、操作者、来源、请求链路等信息，便于高效检索与回溯。
-- =============================================
CREATE TABLE IF NOT EXISTS user_relation_operation_logs (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    relation_id VARCHAR(32) COMMENT '关联 user_relation.id（可能为空：删除前操作或尚未插入关系）',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID（行为发起方，变更 my->other 的关系）',
    target_user_id VARCHAR(32) NOT NULL COMMENT '目标用户ID（行为对象，变更 my->other 的关系）',
    action_type INTEGER NOT NULL CHECK (action_type IN (1,2,3,4,5,6,7,8,11,12,13,14)) COMMENT '操作类型：1-关注，2-取消关注，3-订阅，4-取消订阅，5-不看TA开启，6-不看TA关闭，7-不让TA看开启，8-不让TA看关闭，11-屏蔽搜索开启，12-屏蔽搜索关闭，13-屏蔽私信开启，14-屏蔽私信关闭；说明：拉黑/取消拉黑为组合操作，不作为单独类型，需记录为一组原子操作序列（例如：5、7、11、13的组合）',
    before_state JSON COMMENT '操作前状态快照（user_relation 相关字段：is_follow/is_subscribe/is_block_view/is_block_viewed/is_block_search/is_block_message 等；拉黑操作请记录各字段组合的整体状态）',
    after_state JSON COMMENT '操作后状态快照（user_relation 相关字段：is_follow/is_subscribe/is_block_view/is_block_viewed/is_block_search/is_block_message 等；拉黑操作请记录各字段组合的整体状态）',
    reason VARCHAR(500) COMMENT '操作原因（可选：用户输入或系统判定）',
    operator_id VARCHAR(32) COMMENT '操作者ID（用户自助则为 user_id；后台或系统操作时记录对应操作者）',
    channel INTEGER DEFAULT 1 CHECK (channel IN (1,2,3,4)) COMMENT '操作来源：1-用户端，2-管理后台，3-系统任务，4-其他',
    request_id VARCHAR(64) COMMENT '请求ID/追踪ID（用于审计链路与问题定位）',
    ip_address VARCHAR(45) COMMENT '操作IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理信息',
    device_info JSON COMMENT '设备信息（设备类型、系统版本等）',
    success TINYINT(1) DEFAULT 1 COMMENT '操作是否成功：1-成功，0-失败',
    remark VARCHAR(500) COMMENT '备注信息',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT chk_user_relation_op_not_self CHECK (user_id != target_user_id)
) COMMENT='用户关系操作记录表（user_relation 变更审计日志）';

-- 索引（优化常用审计与检索场景）
CREATE INDEX  idx_user_relation_op_relation_created ON user_relation_operation_logs (relation_id, create_time DESC);
CREATE INDEX  idx_user_relation_op_user_target_created ON user_relation_operation_logs (user_id, target_user_id, create_time DESC);
CREATE INDEX  idx_user_relation_op_action_created ON user_relation_operation_logs (action_type, create_time DESC);
CREATE INDEX  idx_user_relation_op_operator_created ON user_relation_operation_logs (operator_id, create_time DESC);
CREATE INDEX  idx_user_relation_op_request ON user_relation_operation_logs (request_id);
CREATE INDEX  idx_user_relation_op_channel ON user_relation_operation_logs (channel);
CREATE INDEX  idx_user_relation_op_success ON user_relation_operation_logs (success);
CREATE INDEX  idx_user_relation_op_del_flag ON user_relation_operation_logs (del_flag);



-- 同步现有sys_user用户到user_profile_extension表
INSERT INTO user_profile_extension (
    id,
    nickname,
    avatar,
    bio,
    website,
    location,
    occupation,
    company,
    school,
    following_count,
    followers_count,
    likes_count,
    content_count,
    points,
    level,
    verify_status,
    verify_type,
    verify_info,
    is_vip,
    vip_expire_time,
    last_login_time,
    last_active_time,
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
    update_time
)
SELECT 
    u.id as id,                      -- 关联sys_user表的id作为主键
    COALESCE(NULLIF(u.realname, ''), u.username) as nickname,  -- 优先使用真实姓名，否则使用用户名
    u.avatar as avatar,              -- 使用sys_user表的头像
    '' as bio,                       -- 个人简介初始为空
    '' as website,                   -- 个人网站初始为空
    '' as location,                  -- 所在地初始为空
    '' as occupation,                -- 职业初始为空
    '' as company,                   -- 公司初始为空
    '' as school,                    -- 学校初始为空
    0 as following_count,            -- 初始关注数为0
    0 as followers_count,            -- 初始粉丝数为0
    0 as likes_count,                -- 初始获赞数为0
    0 as content_count,              -- 初始发布内容数为0
    0 as points,                     -- 初始积分为0
    1 as level,                      -- 初始等级为1
    0 as verify_status,              -- 初始认证状态为未认证
    0 as verify_type,                -- 初始认证类型为无
    '' as verify_info,               -- 认证信息初始为空
    0 as is_vip,                     -- 初始VIP状态为否
    NULL as vip_expire_time,         -- VIP到期时间初始为空
    NULL as last_login_time,         -- 最后登录时间暂为空
    NOW() as last_active_time,       -- 最后活跃时间设为当前时间
    JSON_ARRAY() as tags,            -- 个人标签初始为空数组
    JSON_ARRAY() as interests,       -- 兴趣爱好初始为空数组
    JSON_OBJECT() as social_links,   -- 社交链接初始为空对象
    JSON_OBJECT() as privacy_settings,    -- 隐私设置初始为空对象
    JSON_OBJECT() as notification_settings, -- 通知设置初始为空对象
    u.del_flag as del_flag,          -- 继承sys_user表的删除标识
    JSON_OBJECT() as ext_data,       -- 扩展字段初始为空对象
    u.create_by as create_by,        -- 继承sys_user表的创建人
    u.create_time as create_time,    -- 继承sys_user表的创建时间
    u.update_by as update_by,        -- 继承sys_user表的更新人
    u.update_time as update_time     -- 继承sys_user表的更新时间
FROM sys_user u
WHERE u.del_flag = 0                -- 只同步未删除的用户
  AND NOT EXISTS (                  -- 避免重复插入，只插入不存在的用户
    SELECT 1 FROM user_profile_extension upe 
    WHERE upe.id = u.id
  );


-- =============================================
-- 表结构说明（文件末尾补充）
-- 本脚本集中创建 3 张用户核心功能表：
-- 1) user_profile_extension：用户资料扩展与统计字段，主键与 sys_user.id 对齐；提供多类 JSON 配置字段与审计字段
-- 2) user_relation：用户间关系与隐私控制（关注/订阅/不看TA/不让TA看/拉黑/屏蔽搜索/屏蔽私信），包含唯一约束 uk_user_target 与自校验约束 chk_user_relation_not_self
-- 3) user_relation_stats：用户关系聚合统计，一行一用户，便于查询与展示
-- 初始数据同步：
--   - 末尾 INSERT 语句按 sys_user 补齐 user_profile_extension（幂等：NOT EXISTS 防重），首次执行即可完成基础资料扩展
-- 执行顺序建议：
--   - 请在 content_community_init.sql 与 content_Interaction_init.sql 之前执行本脚本，以保证依赖关系完整性
-- 索引与性能：
--   - 针对关系标志位（关注、订阅、屏蔽、拉黑、搜索/私信屏蔽）建立细粒度索引，支持高并发查询与统计
--   - 统计表使用 UNIQUE(user_id) 约束保证一用户一行，便于 UPSERT 维护与一致性保障
-- 版本与兼容性：
--   - MySQL 8.0+（使用 CHECK 与 JSON 类型），兼容主流云数据库（需确认 JSON/约束支持情况）
-- 并发与数据一致性：
--   - 关系表声明 UNIQUE(user_id,target_user_id) 防止重复关系；更新建议采用事务与悲观锁/乐观锁策略
--   - 统计更新建议通过事务与触发器/异步定时任务保证最终一致性，避免热点写导致锁冲突
-- 注意事项：
--   - 如需扩展新的隐私开关或关系类型，请遵循现有命名、约束及索引规范，避免与互动域重复建设
-- 注意事项：
--   - 拉黑（BLACKLIST）为原子组合，不作为单独字段或单独操作类型存储；
--     在操作日志中请记录为一组原子操作序列（例如：不看TA开启、不让TA看开启、屏蔽搜索开启、屏蔽私信开启）。
