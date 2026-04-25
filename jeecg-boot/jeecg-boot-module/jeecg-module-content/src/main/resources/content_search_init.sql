-- =============================================
-- 内容社区系统 - 搜索服务数据库初始化脚本
-- =============================================
-- 说明：本文件包含搜索服务相关的所有数据库表定义
-- 依赖：contents表、user_profile_extension表、community表
-- 版本：1.0
-- 数据库类型为mysql 版本8.0+
-- 创建时间：2024-12-16
-- 更新时间：2024-12-16
-- =============================================

START TRANSACTION;

-- =============================================
-- 1. 搜索记录表 (search_records)
-- =============================================
-- 用于记录用户的搜索行为，支持搜索统计和分析
CREATE TABLE IF NOT EXISTS search_records (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '搜索记录ID',
    user_id VARCHAR(32) COMMENT '用户ID（未登录用户为NULL）',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    search_type VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '搜索类型：CONTENT-内容搜索 USER-用户搜索 CHANNEL-频道搜索 TOPIC-话题搜索 ALL-综合搜索',
    result_count INT DEFAULT 0 COMMENT '搜索结果数量',
    search_filters JSON COMMENT '搜索筛选条件（JSON格式）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理信息',
    search_duration INT COMMENT '搜索耗时（毫秒）',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 搜索记录表索引
CREATE INDEX idx_search_records_user_id ON search_records(user_id);
CREATE INDEX idx_search_records_keyword ON search_records(keyword);
CREATE INDEX idx_search_records_search_type ON search_records(search_type);
CREATE INDEX idx_search_records_create_time ON search_records(create_time DESC);
CREATE INDEX idx_search_records_del_flag ON search_records(del_flag);
CREATE INDEX idx_search_records_ip ON search_records(ip_address);

-- =============================================
-- 2. 热门搜索词表 (trending_keywords)
-- =============================================
-- 用于统计和展示热门搜索关键词
CREATE TABLE IF NOT EXISTS trending_keywords (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '热门关键词ID',
    keyword VARCHAR(200) NOT NULL COMMENT '关键词',
    search_count BIGINT DEFAULT 0 COMMENT '搜索次数',
    period_type VARCHAR(10) NOT NULL COMMENT '统计周期类型：HOUR-小时 DAY-天 WEEK-周 MONTH-月',
    period_start TIMESTAMP NOT NULL COMMENT '统计周期开始时间',
    period_end TIMESTAMP NOT NULL COMMENT '统计周期结束时间',
    rank_position INT COMMENT '排名位置',
    trend_change INT DEFAULT 0 COMMENT '排名变化：正数上升，负数下降，0无变化',
    category VARCHAR(50) COMMENT '关键词分类',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 热门搜索词表索引
CREATE UNIQUE INDEX uk_trending_keyword_period ON trending_keywords(keyword, period_type, period_start);
CREATE INDEX idx_trending_keywords_period_rank ON trending_keywords(period_type, period_start, rank_position);
CREATE INDEX idx_trending_keywords_search_count ON trending_keywords(search_count DESC);
CREATE INDEX idx_trending_keywords_category ON trending_keywords(category);
CREATE INDEX idx_trending_keywords_del_flag ON trending_keywords(del_flag);

-- =============================================
-- 3. 搜索建议表 (search_suggestions)
-- =============================================
-- 用于提供搜索自动补全和建议功能
CREATE TABLE IF NOT EXISTS search_suggestions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '搜索建议ID',
    keyword VARCHAR(200) NOT NULL COMMENT '原始关键词',
    suggestion VARCHAR(200) NOT NULL COMMENT '建议关键词',
    suggestion_type VARCHAR(20) NOT NULL COMMENT '建议类型：CONTENT-内容建议 USER-用户建议 CHANNEL-频道建议 TOPIC-话题建议',
    weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '权重，用于排序（越大越靠前）',
    source VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '建议来源：SYSTEM-系统生成 MANUAL-人工添加 AI-AI生成',
    click_count BIGINT DEFAULT 0 COMMENT '点击次数',
    is_active INTEGER DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 搜索建议表索引
CREATE INDEX idx_search_suggestions_keyword ON search_suggestions(keyword);
CREATE INDEX idx_search_suggestions_suggestion ON search_suggestions(suggestion);
CREATE INDEX idx_search_suggestions_type ON search_suggestions(suggestion_type);
CREATE INDEX idx_search_suggestions_weight ON search_suggestions(weight DESC);
CREATE INDEX idx_search_suggestions_is_active ON search_suggestions(is_active);
CREATE INDEX idx_search_suggestions_del_flag ON search_suggestions(del_flag);
CREATE INDEX idx_search_suggestions_click_count ON search_suggestions(click_count DESC);

-- =============================================
-- 4. 用户搜索历史表 (user_search_history)
-- =============================================
-- 用于记录用户的个人搜索历史，支持搜索历史功能
CREATE TABLE IF NOT EXISTS user_search_history (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '搜索历史ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    search_type VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '搜索类型：CONTENT-内容搜索 USER-用户搜索 CHANNEL-频道搜索 TOPIC-话题搜索 ALL-综合搜索',
    search_filters JSON COMMENT '搜索筛选条件（JSON格式）',
    search_count INT DEFAULT 1 COMMENT '搜索次数（同一关键词重复搜索会累加）',
    last_search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后搜索时间',
    is_pinned INTEGER DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 用户搜索历史表索引
CREATE UNIQUE INDEX uk_user_search_history ON user_search_history(user_id, keyword, search_type);
CREATE INDEX idx_user_search_history_user_id ON user_search_history(user_id);
CREATE INDEX idx_user_search_history_last_search ON user_search_history(last_search_time DESC);
CREATE INDEX idx_user_search_history_search_count ON user_search_history(search_count DESC);
CREATE INDEX idx_user_search_history_is_pinned ON user_search_history(is_pinned);
CREATE INDEX idx_user_search_history_del_flag ON user_search_history(del_flag);

-- =============================================
-- 5. 搜索结果点击记录表 (search_result_clicks)
-- =============================================
-- 用于记录用户点击搜索结果的行为，支持搜索结果优化
CREATE TABLE IF NOT EXISTS search_result_clicks (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '点击记录ID',
    search_record_id VARCHAR(32) COMMENT '关联的搜索记录ID',
    user_id VARCHAR(32) COMMENT '用户ID',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    result_type VARCHAR(20) NOT NULL COMMENT '结果类型：CONTENT-内容 USER-用户 CHANNEL-频道 TOPIC-话题',
    result_id VARCHAR(32) NOT NULL COMMENT '结果对象ID',
    result_position INT COMMENT '结果在搜索列表中的位置',
    result_title VARCHAR(500) COMMENT '结果标题',
    click_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点击时间',
    session_id VARCHAR(64) COMMENT '会话ID',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 搜索结果点击记录表索引
CREATE INDEX idx_search_result_clicks_search_record ON search_result_clicks(search_record_id);
CREATE INDEX idx_search_result_clicks_user_id ON search_result_clicks(user_id);
CREATE INDEX idx_search_result_clicks_keyword ON search_result_clicks(keyword);
CREATE INDEX idx_search_result_clicks_result ON search_result_clicks(result_type, result_id);
CREATE INDEX idx_search_result_clicks_click_time ON search_result_clicks(click_time DESC);
CREATE INDEX idx_search_result_clicks_del_flag ON search_result_clicks(del_flag);

-- =============================================
-- 6. 搜索配置表 (search_configs)
-- =============================================
-- 用于配置搜索相关的参数和规则
CREATE TABLE IF NOT EXISTS search_configs (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型：STRING-字符串 NUMBER-数字 BOOLEAN-布尔 JSON-JSON对象',
    config_group VARCHAR(50) DEFAULT 'DEFAULT' COMMENT '配置分组',
    description TEXT COMMENT '配置描述',
    is_system INTEGER DEFAULT 0 COMMENT '是否系统配置：0-否 1-是（系统配置不可删除）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 搜索配置表索引
CREATE UNIQUE INDEX uk_search_configs_key ON search_configs(config_key);
CREATE INDEX idx_search_configs_group ON search_configs(config_group);
CREATE INDEX idx_search_configs_is_system ON search_configs(is_system);
CREATE INDEX idx_search_configs_sort ON search_configs(sort_order);
CREATE INDEX idx_search_configs_del_flag ON search_configs(del_flag);

-- =============================================
-- 7. 搜索索引表 (search_indexes)
-- =============================================
-- 用于存储搜索索引数据，提高搜索性能
CREATE TABLE IF NOT EXISTS search_indexes (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '索引ID',
    object_type VARCHAR(20) NOT NULL COMMENT '对象类型：CONTENT-内容 USER-用户 CHANNEL-频道 TOPIC-话题',
    object_id VARCHAR(32) NOT NULL COMMENT '对象ID',
    title VARCHAR(500) COMMENT '标题',
    content TEXT COMMENT '内容（用于全文搜索）',
    keywords VARCHAR(1000) COMMENT '关键词（空格分隔）',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    author_id VARCHAR(32) COMMENT '作者ID',
    author_name VARCHAR(100) COMMENT '作者名称',
    category VARCHAR(100) COMMENT '分类',
    status INTEGER DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '权重（影响搜索排序）',
    
    -- 统计字段（用于搜索排序）
    view_count BIGINT DEFAULT 0 COMMENT '浏览数',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    share_count BIGINT DEFAULT 0 COMMENT '分享数',
    
    -- 时间字段
    publish_time TIMESTAMP COMMENT '发布时间',
    last_update_time TIMESTAMP COMMENT '最后更新时间',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 搜索索引表索引
CREATE UNIQUE INDEX uk_search_indexes_object ON search_indexes(object_type, object_id);
CREATE INDEX idx_search_indexes_object_type ON search_indexes(object_type);
CREATE INDEX idx_search_indexes_author ON search_indexes(author_id);
CREATE INDEX idx_search_indexes_category ON search_indexes(category);
CREATE INDEX idx_search_indexes_status ON search_indexes(status);
CREATE INDEX idx_search_indexes_weight ON search_indexes(weight DESC);
CREATE INDEX idx_search_indexes_publish_time ON search_indexes(publish_time DESC);
CREATE INDEX idx_search_indexes_del_flag ON search_indexes(del_flag);

-- 全文搜索索引（根据数据库类型选择）
-- MySQL: CREATE FULLTEXT INDEX idx_search_indexes_fulltext ON search_indexes(title, content, keywords);
-- PostgreSQL: CREATE INDEX idx_search_indexes_fulltext ON search_indexes USING gin(to_tsvector('english', title || ' ' || content || ' ' || keywords));

-- =============================================
-- 8. 搜索统计表 (search_statistics)
-- =============================================
-- 用于存储搜索相关的统计数据
CREATE TABLE IF NOT EXISTS search_statistics (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type VARCHAR(20) NOT NULL COMMENT '统计类型：DAILY-日统计 WEEKLY-周统计 MONTHLY-月统计',
    total_searches BIGINT DEFAULT 0 COMMENT '总搜索次数',
    unique_users BIGINT DEFAULT 0 COMMENT '搜索用户数',
    avg_results_per_search DECIMAL(10,2) DEFAULT 0 COMMENT '平均每次搜索结果数',
    avg_search_duration DECIMAL(10,2) DEFAULT 0 COMMENT '平均搜索耗时（毫秒）',
    top_keywords JSON COMMENT '热门关键词（JSON数组）',
    search_type_distribution JSON COMMENT '搜索类型分布（JSON对象）',
    zero_result_rate DECIMAL(5,2) DEFAULT 0 COMMENT '零结果搜索率（%）',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 搜索统计表索引
CREATE UNIQUE INDEX uk_search_statistics_date_type ON search_statistics(stat_date, stat_type);
CREATE INDEX idx_search_statistics_stat_date ON search_statistics(stat_date DESC);
CREATE INDEX idx_search_statistics_stat_type ON search_statistics(stat_type);
CREATE INDEX idx_search_statistics_del_flag ON search_statistics(del_flag);

-- =============================================
-- 9. 搜索模板表 (search_templates)
-- =============================================
-- 用于保存用户自定义的搜索模板
CREATE TABLE IF NOT EXISTS search_templates (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '模板ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_description TEXT COMMENT '模板描述',
    search_conditions JSON NOT NULL COMMENT '搜索条件（JSON格式）',
    is_public INTEGER DEFAULT 0 COMMENT '是否公开：0-私有 1-公开',
    use_count BIGINT DEFAULT 0 COMMENT '使用次数',
    last_used_time TIMESTAMP COMMENT '最后使用时间',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 搜索模板表索引
CREATE INDEX idx_search_templates_user_id ON search_templates(user_id);
CREATE INDEX idx_search_templates_is_public ON search_templates(is_public);
CREATE INDEX idx_search_templates_use_count ON search_templates(use_count DESC);
CREATE INDEX idx_search_templates_last_used ON search_templates(last_used_time DESC);
CREATE INDEX idx_search_templates_del_flag ON search_templates(del_flag);

-- =============================================
-- 10. 违禁词表 (forbidden_words)
-- =============================================
-- 用于搜索内容过滤和审核
CREATE TABLE IF NOT EXISTS forbidden_words (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '违禁词ID',
    word VARCHAR(200) NOT NULL COMMENT '违禁词',
    word_type VARCHAR(20) NOT NULL COMMENT '违禁词类型：POLITICAL-政治敏感 PORN-色情低俗 VIOLENCE-暴力恐怖 AD-广告营销 OTHER-其他',
    action_type VARCHAR(20) DEFAULT 'BLOCK' COMMENT '处理方式：BLOCK-直接屏蔽 REVIEW-需审核 REPLACE-替换为*',
    replacement VARCHAR(200) COMMENT '替换词（当action_type为REPLACE时使用）',
    severity INTEGER DEFAULT 1 COMMENT '严重程度：1-轻微 2-中等 3-严重',
    is_regex INTEGER DEFAULT 0 COMMENT '是否正则表达式：0-否 1-是',
    is_active INTEGER DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    hit_count BIGINT DEFAULT 0 COMMENT '命中次数',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 违禁词表索引
CREATE UNIQUE INDEX uk_forbidden_words_word ON forbidden_words(word);
CREATE INDEX idx_forbidden_words_word_type ON forbidden_words(word_type);
CREATE INDEX idx_forbidden_words_action_type ON forbidden_words(action_type);
CREATE INDEX idx_forbidden_words_severity ON forbidden_words(severity);
CREATE INDEX idx_forbidden_words_is_active ON forbidden_words(is_active);
CREATE INDEX idx_forbidden_words_hit_count ON forbidden_words(hit_count DESC);
CREATE INDEX idx_forbidden_words_del_flag ON forbidden_words(del_flag);

-- =============================================
-- 初始化基础数据
-- =============================================

-- 插入默认搜索配置
-- INSERT INTO search_configs (id, config_key, config_value, config_type, config_group, description, is_system, sort_order) VALUES
-- ('1', 'search.max_results_per_page', '20', 'NUMBER', 'PAGINATION', '每页最大搜索结果数', 1, 1),
-- ('2', 'search.max_keyword_length', '200', 'NUMBER', 'VALIDATION', '搜索关键词最大长度', 1, 2),
-- ('3', 'search.enable_fuzzy_search', 'true', 'BOOLEAN', 'FEATURE', '是否启用模糊搜索', 1, 3),
-- ('4', 'search.enable_search_history', 'true', 'BOOLEAN', 'FEATURE', '是否启用搜索历史', 1, 4),
-- ('5', 'search.max_history_count', '50', 'NUMBER', 'HISTORY', '用户最大搜索历史数量', 1, 5),
-- ('6', 'search.trending_keywords_count', '10', 'NUMBER', 'TRENDING', '热门搜索词显示数量', 1, 6),
-- ('7', 'search.suggestion_count', '8', 'NUMBER', 'SUGGESTION', '搜索建议显示数量', 1, 7),
-- ('8', 'search.enable_content_filter', 'true', 'BOOLEAN', 'SECURITY', '是否启用内容过滤', 1, 8),
-- ('9', 'search.cache_expire_minutes', '30', 'NUMBER', 'CACHE', '搜索结果缓存过期时间（分钟）', 1, 9),
-- ('10', 'search.min_keyword_length', '1', 'NUMBER', 'VALIDATION', '搜索关键词最小长度', 1, 10)
-- ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- -- 插入默认违禁词（示例）
-- INSERT INTO forbidden_words (id, word, word_type, action_type, severity, is_active) VALUES
-- ('1', '测试违禁词', 'OTHER', 'BLOCK', 1, 1),
-- ('2', '广告推广', 'AD', 'REVIEW', 2, 1),
-- ('3', '暴力内容', 'VIOLENCE', 'BLOCK', 3, 1)
-- ON DUPLICATE KEY UPDATE word = VALUES(word);

-- COMMIT;

-- -- =============================================
-- -- 搜索模块字典数据
-- -- =============================================

-- -- 搜索类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('search_type_dict', '搜索类型', 'search_type', '搜索功能中的搜索类型分类', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('search_type_content', 'search_type_dict', '内容搜索', 'CONTENT', '搜索内容相关信息', 1, 1, 'admin', NOW()),
-- ('search_type_user', 'search_type_dict', '用户搜索', 'USER', '搜索用户相关信息', 2, 1, 'admin', NOW()),
-- ('search_type_channel', 'search_type_dict', '频道搜索', 'CHANNEL', '搜索频道相关信息', 3, 1, 'admin', NOW()),
-- ('search_type_topic', 'search_type_dict', '话题搜索', 'TOPIC', '搜索话题相关信息', 4, 1, 'admin', NOW()),
-- ('search_type_all', 'search_type_dict', '全部搜索', 'ALL', '搜索所有类型的信息', 5, 1, 'admin', NOW());

-- -- 统计周期类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('period_type_dict', '统计周期类型', 'period_type', '统计分析中的时间周期类型', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('period_type_hour', 'period_type_dict', '小时', 'HOUR', '按小时统计', 1, 1, 'admin', NOW()),
-- ('period_type_day', 'period_type_dict', '天', 'DAY', '按天统计', 2, 1, 'admin', NOW()),
-- ('period_type_week', 'period_type_dict', '周', 'WEEK', '按周统计', 3, 1, 'admin', NOW()),
-- ('period_type_month', 'period_type_dict', '月', 'MONTH', '按月统计', 4, 1, 'admin', NOW());

-- -- 搜索建议类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('suggestion_type_dict', '搜索建议类型', 'suggestion_type', '搜索建议功能中的建议类型分类', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('suggestion_type_content', 'suggestion_type_dict', '内容建议', 'CONTENT', '内容相关的搜索建议', 1, 1, 'admin', NOW()),
-- ('suggestion_type_user', 'suggestion_type_dict', '用户建议', 'USER', '用户相关的搜索建议', 2, 1, 'admin', NOW()),
-- ('suggestion_type_channel', 'suggestion_type_dict', '频道建议', 'CHANNEL', '频道相关的搜索建议', 3, 1, 'admin', NOW()),
-- ('suggestion_type_topic', 'suggestion_type_dict', '话题建议', 'TOPIC', '话题相关的搜索建议', 4, 1, 'admin', NOW());

-- -- 建议来源字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('suggestion_source_dict', '建议来源', 'suggestion_source', '搜索建议的来源类型', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('suggestion_source_system', 'suggestion_source_dict', '系统生成', 'SYSTEM', '系统自动生成的建议', 1, 1, 'admin', NOW()),
-- ('suggestion_source_manual', 'suggestion_source_dict', '人工添加', 'MANUAL', '管理员手动添加的建议', 2, 1, 'admin', NOW()),
-- ('suggestion_source_ai', 'suggestion_source_dict', 'AI生成', 'AI', 'AI算法生成的建议', 3, 1, 'admin', NOW());

-- -- 搜索结果类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('result_type_dict', '搜索结果类型', 'result_type', '搜索结果的类型分类', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('result_type_content', 'result_type_dict', '内容结果', 'CONTENT', '内容类型的搜索结果', 1, 1, 'admin', NOW()),
-- ('result_type_user', 'result_type_dict', '用户结果', 'USER', '用户类型的搜索结果', 2, 1, 'admin', NOW()),
-- ('result_type_channel', 'result_type_dict', '频道结果', 'CHANNEL', '频道类型的搜索结果', 3, 1, 'admin', NOW()),
-- ('result_type_topic', 'result_type_dict', '话题结果', 'TOPIC', '话题类型的搜索结果', 4, 1, 'admin', NOW());

-- -- 配置类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('config_type_dict', '配置类型', 'config_type', '系统配置的数据类型', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('config_type_string', 'config_type_dict', '字符串', 'STRING', '字符串类型的配置值', 1, 1, 'admin', NOW()),
-- ('config_type_number', 'config_type_dict', '数字', 'NUMBER', '数字类型的配置值', 2, 1, 'admin', NOW()),
-- ('config_type_boolean', 'config_type_dict', '布尔值', 'BOOLEAN', '布尔类型的配置值', 3, 1, 'admin', NOW()),
-- ('config_type_json', 'config_type_dict', 'JSON', 'JSON', 'JSON格式的配置值', 4, 1, 'admin', NOW());

-- -- 对象类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('object_type_dict', '对象类型', 'object_type', '搜索索引中的对象类型', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('object_type_content', 'object_type_dict', '内容对象', 'CONTENT', '内容类型的索引对象', 1, 1, 'admin', NOW()),
-- ('object_type_user', 'object_type_dict', '用户对象', 'USER', '用户类型的索引对象', 2, 1, 'admin', NOW()),
-- ('object_type_channel', 'object_type_dict', '频道对象', 'CHANNEL', '频道类型的索引对象', 3, 1, 'admin', NOW()),
-- ('object_type_topic', 'object_type_dict', '话题对象', 'TOPIC', '话题类型的索引对象', 4, 1, 'admin', NOW());

-- -- 统计类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('stat_type_dict', '统计类型', 'stat_type', '统计数据的类型分类', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('stat_type_daily', 'stat_type_dict', '日统计', 'DAILY', '按日进行的统计', 1, 1, 'admin', NOW()),
-- ('stat_type_weekly', 'stat_type_dict', '周统计', 'WEEKLY', '按周进行的统计', 2, 1, 'admin', NOW()),
-- ('stat_type_monthly', 'stat_type_dict', '月统计', 'MONTHLY', '按月进行的统计', 3, 1, 'admin', NOW());

-- -- 违禁词类型字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('forbidden_word_type_dict', '违禁词类型', 'forbidden_word_type', '违禁词的分类类型', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('forbidden_word_type_ad', 'forbidden_word_type_dict', '广告推广', 'AD', '广告推广类违禁词', 1, 1, 'admin', NOW()),
-- ('forbidden_word_type_violence', 'forbidden_word_type_dict', '暴力内容', 'VIOLENCE', '暴力相关违禁词', 2, 1, 'admin', NOW()),
-- ('forbidden_word_type_political', 'forbidden_word_type_dict', '政治敏感', 'POLITICAL', '政治敏感类违禁词', 3, 1, 'admin', NOW()),
-- ('forbidden_word_type_pornography', 'forbidden_word_type_dict', '色情内容', 'PORNOGRAPHY', '色情相关违禁词', 4, 1, 'admin', NOW()),
-- ('forbidden_word_type_other', 'forbidden_word_type_dict', '其他', 'OTHER', '其他类型违禁词', 5, 1, 'admin', NOW());

-- -- 违禁词处理方式字典
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES
-- ('forbidden_action_type_dict', '违禁词处理方式', 'forbidden_action_type', '发现违禁词时的处理方式', 0, 'admin', NOW(), 0);

-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- ('forbidden_action_block', 'forbidden_action_type_dict', '阻止发布', 'BLOCK', '直接阻止内容发布', 1, 1, 'admin', NOW()),
-- ('forbidden_action_review', 'forbidden_action_type_dict', '人工审核', 'REVIEW', '标记为需要人工审核', 2, 1, 'admin', NOW()),
-- ('forbidden_action_replace', 'forbidden_action_type_dict', '自动替换', 'REPLACE', '自动替换为*号', 3, 1, 'admin', NOW()),
-- ('forbidden_action_warn', 'forbidden_action_type_dict', '警告提示', 'WARN', '给用户警告提示', 4, 1, 'admin', NOW());

-- =============================================
-- 表结构说明
-- =============================================
/*
1. 搜索记录表 (search_records)
   - 记录所有搜索行为，支持搜索分析和统计
   - 支持未登录用户搜索记录
   - 记录搜索筛选条件和结果数量
   - 支持搜索性能监控

2. 热门搜索词表 (trending_keywords)
   - 按不同时间周期统计热门关键词
   - 支持排名变化趋势分析
   - 支持关键词分类管理

3. 搜索建议表 (search_suggestions)
   - 提供搜索自动补全功能
   - 支持多种建议来源（系统、人工、AI）
   - 支持权重排序和点击统计

4. 用户搜索历史表 (user_search_history)
   - 记录用户个人搜索历史
   - 支持搜索次数统计和置顶功能
   - 支持搜索条件保存

5. 搜索结果点击记录表 (search_result_clicks)
   - 记录用户点击搜索结果的行为
   - 支持搜索结果质量分析
   - 支持搜索算法优化

6. 搜索配置表 (search_configs)
   - 管理搜索相关的系统配置
   - 支持动态配置调整
   - 支持配置分组管理

7. 搜索索引表 (search_indexes)
   - 存储搜索索引数据
   - 支持全文搜索和权重排序
   - 支持多种对象类型索引

8. 搜索统计表 (search_statistics)
   - 存储搜索相关统计数据
   - 支持多维度统计分析
   - 支持搜索质量监控

9. 搜索模板表 (search_templates)
   - 保存用户自定义搜索模板
   - 支持复杂搜索条件保存
   - 支持模板分享功能

10. 违禁词表 (forbidden_words)
    - 管理搜索内容过滤规则
    - 支持多种处理方式
    - 支持正则表达式匹配

设计特点：
- 统一使用VARCHAR(32)作为主键类型
- 完善的索引设计，优化查询性能
- 支持软删除机制
- 完整的审计字段
- 支持JSON格式存储复杂数据
- 考虑了搜索性能和用户体验
- 支持搜索行为分析和优化
*/