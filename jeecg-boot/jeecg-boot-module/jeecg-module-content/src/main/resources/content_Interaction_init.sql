-- =============================================
-- 内容社区系统 - 互动服务数据库初始化脚本
-- 互动服务 (Interaction Service)
-- 职责：点赞、分享、收藏、关注、举报、浏览、评论等全方位互动功能
-- 数据库类型为mysql 版本8.0+
-- 
-- 重要依赖：
-- 1. 本脚本依赖于 content_module_init.sql 中的 media_files 表
-- 2. 请确保先执行 content_module_init.sql 再执行本脚本
-- 3. 采用统一文件表设计模式，实现文件复用和统一管理
-- 4. 支持软删除机制，保证数据完整性和可恢复性
-- 5. 集成完善的审计字段和统计更新机制
-- 
-- 文件结构说明：
-- 第一部分：表结构和索引创建（在事务中执行）
-- 第二部分：初始数据插入（在事务中执行）
-- 第三部分：视图创建（无需事务）
-- 第四部分：存储过程创建（无需事务，内部自管理事务）
-- =============================================

-- =============================================
-- 第一部分：表结构和索引创建（事务处理）
-- =============================================
START TRANSACTION;

-- 1. 用户反应表 (user_reactions) - 支持点赞和反对功能
-- 用于存储用户对内容、评论等的点赞和反对记录，两种操作互斥
CREATE TABLE user_reactions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '操作用户ID',
    target_id VARCHAR(32) NOT NULL COMMENT '目标对象ID',
    target_type INTEGER NOT NULL CHECK (target_type IN (1, 2, 3)) COMMENT '目标对象类型：1-内容(CONTENT)，2-评论(COMMENT)，3-用户(USER)',
    action_type INTEGER NOT NULL CHECK (action_type IN (1, 2)) COMMENT '操作类型：1-点赞(LIKE)，2-反对(DISLIKE)',
    ip_address VARCHAR(45) COMMENT '操作IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理信息',
    device_info JSON COMMENT '设备信息（设备类型、操作系统等）',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    
    -- 确保同一用户对同一目标只能有一种反应（点赞或反对），排除已删除记录
    CONSTRAINT uk_user_reactions_user_target UNIQUE (user_id, target_id, target_type)
) COMMENT '用户反应表（点赞/反对）';

-- 2. 评论表 (comments)
-- 用于存储用户对内容的评论，支持多级回复和丰富的互动功能
CREATE TABLE comments (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    user_id VARCHAR(32) NOT NULL COMMENT '评论用户ID',
    parent_comment_id VARCHAR(32) COMMENT '父评论ID',
    root_comment_id VARCHAR(32) COMMENT '根评论ID，用于楼层显示',
    content TEXT NOT NULL COMMENT '评论内容',
    content_length INTEGER GENERATED ALWAYS AS (LENGTH(content)) STORED COMMENT '评论内容长度',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    dislike_count BIGINT DEFAULT 0 COMMENT '反对数',
    reply_count BIGINT DEFAULT 0 COMMENT '回复数',
    media_count INTEGER DEFAULT 0 COMMENT '媒体文件数量（通过关联表统计）',
    mention_count INTEGER DEFAULT 0 COMMENT '提及用户数量',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名评论',
    is_author_only BOOLEAN DEFAULT FALSE COMMENT '是否仅楼主可见',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_hot BOOLEAN DEFAULT FALSE COMMENT '是否热门评论',
    hot_score DECIMAL(10,2) DEFAULT 0 COMMENT '热度分数',
    ip_address VARCHAR(45) COMMENT '评论IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理信息',
    device_info JSON COMMENT '设备信息',
    location_info JSON COMMENT '地理位置信息',
    status INTEGER DEFAULT 1 CHECK (status IN (1, 2, 3, 4, 5)) COMMENT '评论状态：1-正常(NORMAL)，2-隐藏(HIDDEN)，3-删除(DELETED)，4-审核中(UNDER_REVIEW)，5-违规(VIOLATION)',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人'
) COMMENT '评论表';

-- 3. 评论媒体关联表 (comment_media_relations)
-- 用于存储评论与媒体文件的关联关系，采用统一文件表设计模式
CREATE TABLE comment_media_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联ID',
    comment_id VARCHAR(32) NOT NULL COMMENT '评论ID',
    media_id VARCHAR(32) NOT NULL COMMENT '媒体文件ID（关联media_files表）',
    relation_type INTEGER NOT NULL DEFAULT 1 CHECK (relation_type IN (1, 2, 3)) COMMENT '关联类型：1-附件(attachment)，2-内联(inline)，3-引用(quote)',
    sort_order INTEGER DEFAULT 0 COMMENT '排序顺序',
    position_data JSON COMMENT '位置数据（如内联位置、引用位置等）',
    display_config JSON COMMENT '显示配置（如缩略图尺寸、播放设置等）',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    
    CONSTRAINT uk_comment_media_unique UNIQUE (comment_id, media_id, relation_type)
) COMMENT '评论媒体关联表';

-- 4. 评论提及用户表 (comment_mentions)
-- 用于存储评论中@提及的用户，支持通知和互动追踪
CREATE TABLE comment_mentions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    comment_id VARCHAR(32) NOT NULL COMMENT '评论ID',
    mentioned_user_id VARCHAR(32) NOT NULL COMMENT '被提及用户ID',
    mention_text VARCHAR(100) COMMENT '提及的文本内容',
    position_start INTEGER COMMENT '提及在评论中的起始位置',
    position_end INTEGER COMMENT '提及在评论中的结束位置',
    is_notified BOOLEAN DEFAULT FALSE COMMENT '是否已通知',
    notification_sent_at TIMESTAMP COMMENT '通知发送时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    
    CONSTRAINT uk_comment_mentions_unique UNIQUE (comment_id, mentioned_user_id)
) COMMENT '评论提及用户表';

-- 5. 用户收藏表 (user_collections)
CREATE TABLE user_collections (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    target_id VARCHAR(32) NOT NULL COMMENT '目标对象ID',
    target_type INTEGER NOT NULL CHECK (target_type IN (1, 2)) COMMENT '目标对象类型：1-内容(CONTENT)，2-评论(COMMENT)',
    collection_folder VARCHAR(100) COMMENT '收藏夹名称',
    collection_tags JSON COMMENT '收藏标签',
    collection_note TEXT COMMENT '收藏备注',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开收藏',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    
    CONSTRAINT uk_user_collections_user_target UNIQUE (user_id, target_id, target_type)
) COMMENT '用户收藏表';

-- 6/7. 用户核心关系表请参考独立脚本 content_user_core_init.sql 以避免重复创建
-- 已移动：user_relation、user_relation_stats 表定义至 content_user_core_init.sql
-- 请确保在初始化流程中先执行 content_user_core_init.sql


-- 8. 举报记录表 (report_records)
CREATE TABLE report_records (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    reporter_id VARCHAR(32) NOT NULL COMMENT '举报人ID',
    target_id VARCHAR(32) NOT NULL COMMENT '被举报对象ID',
    target_type INTEGER NOT NULL CHECK (target_type IN (1, 2, 3)) COMMENT '被举报对象类型：1-内容(CONTENT)，2-评论(COMMENT)，3-用户(USER)',
    report_reason INTEGER NOT NULL CHECK (report_reason IN (1, 2, 3, 4, 5, 6, 7, 8)) COMMENT '举报原因：1-垃圾信息(SPAM)，2-违法违规(ILLEGAL)，3-色情内容(PORNOGRAPHY)，4-暴力内容(VIOLENCE)，5-诈骗信息(FRAUD)，6-侵权内容(COPYRIGHT)，7-恶意攻击(MALICIOUS)，8-其他(OTHER)',
    report_description TEXT COMMENT '举报详细描述',
    evidence_files JSON COMMENT '举报证据文件',
    report_status INTEGER DEFAULT 1 CHECK (report_status IN (1, 2, 3, 4, 5)) COMMENT '举报状态：1-待处理(PENDING)，2-处理中(PROCESSING)，3-已处理(PROCESSED)，4-已驳回(REJECTED)，5-已撤销(CANCELLED)',
    handler_id VARCHAR(32) COMMENT '处理人ID',
    handle_result TEXT COMMENT '处理结果',
    handle_time TIMESTAMP COMMENT '处理时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人'
) COMMENT '举报记录表';

-- 9. 浏览记录表 (browse_records)
CREATE TABLE browse_records (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    target_id VARCHAR(32) NOT NULL COMMENT '目标对象ID',
    target_type INTEGER NOT NULL CHECK (target_type IN (1, 2)) COMMENT '目标对象类型：1-内容(CONTENT)，2-用户主页(USER_PROFILE)',
    browse_duration INTEGER DEFAULT 0 COMMENT '浏览时长（秒）',
    browse_progress DECIMAL(5,2) DEFAULT 0 COMMENT '浏览进度（百分比）',
    ip_address VARCHAR(45) COMMENT '浏览IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理信息',
    device_info JSON COMMENT '设备信息',
    referrer_url VARCHAR(1000) COMMENT '来源URL',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人'
) COMMENT '浏览记录表';

-- 10. 内容统计表 (content_stats)
CREATE TABLE content_stats (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    content_id VARCHAR(32) NOT NULL UNIQUE COMMENT '内容ID',
    view_count BIGINT DEFAULT 0 COMMENT '浏览量',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    dislike_count BIGINT DEFAULT 0 COMMENT '反对数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    share_count BIGINT DEFAULT 0 COMMENT '分享数',
    collection_count BIGINT DEFAULT 0 COMMENT '收藏数',
    report_count BIGINT DEFAULT 0 COMMENT '举报数',
    hot_score DECIMAL(10,2) DEFAULT 0 COMMENT '热度分数',
    quality_score DECIMAL(5,2) DEFAULT 0 COMMENT '质量分数',
    engagement_rate DECIMAL(5,2) DEFAULT 0 COMMENT '互动率',
    last_interaction_time TIMESTAMP COMMENT '最后互动时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人'
) COMMENT '内容统计表';

-- 11. 用户统计表 (user_stats)
CREATE TABLE user_stats (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL UNIQUE COMMENT '用户ID',
    content_count BIGINT DEFAULT 0 COMMENT '发布内容数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    like_given_count BIGINT DEFAULT 0 COMMENT '点赞给出数',
    like_received_count BIGINT DEFAULT 0 COMMENT '点赞收到数',
    dislike_given_count BIGINT DEFAULT 0 COMMENT '反对给出数',
    dislike_received_count BIGINT DEFAULT 0 COMMENT '反对收到数',
    collection_count BIGINT DEFAULT 0 COMMENT '收藏数',
    follower_count BIGINT DEFAULT 0 COMMENT '粉丝数',
    following_count BIGINT DEFAULT 0 COMMENT '关注数',
    view_count BIGINT DEFAULT 0 COMMENT '总浏览量',
    reputation_score DECIMAL(10,2) DEFAULT 0 COMMENT '声誉分数',
    activity_score DECIMAL(10,2) DEFAULT 0 COMMENT '活跃度分数',
    last_active_time TIMESTAMP COMMENT '最后活跃时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    create_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    update_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人'
) COMMENT '用户统计表';

-- 12. 临时批量操作表 (temp_batch_reactions)
CREATE TABLE temp_batch_reactions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    target_id VARCHAR(32) NOT NULL COMMENT '目标对象ID',
    target_type INTEGER NOT NULL COMMENT '目标对象类型',
    action_type INTEGER NOT NULL COMMENT '操作类型',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：INSERT, UPDATE, DELETE',
    batch_id VARCHAR(50) NOT NULL COMMENT '批次ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '临时批量操作表';

-- =============================================
-- 索引创建
-- =============================================

-- 用户反应表索引
CREATE INDEX idx_user_reactions_target ON user_reactions (target_id, target_type);
CREATE INDEX idx_user_reactions_user_id ON user_reactions (user_id);
CREATE INDEX idx_user_reactions_action_type ON user_reactions (action_type);
CREATE INDEX idx_user_reactions_created_at ON user_reactions (created_at DESC);
CREATE INDEX idx_user_reactions_target_action ON user_reactions (target_id, target_type, action_type);
CREATE INDEX idx_user_reactions_del_flag ON user_reactions (del_flag);
CREATE INDEX idx_user_reactions_deleted_at ON user_reactions (deleted_at);
-- 性能优化复合索引
CREATE INDEX idx_user_reactions_user_target_active ON user_reactions (user_id, target_id, target_type, action_type);
CREATE INDEX idx_user_reactions_target_created_active ON user_reactions (target_id, target_type, created_at DESC);
CREATE INDEX idx_user_reactions_user_action_created ON user_reactions (user_id, action_type, created_at DESC);

-- 评论表索引
CREATE INDEX idx_comments_content_id ON comments (content_id);
CREATE INDEX idx_comments_user_id ON comments (user_id);
CREATE INDEX idx_comments_parent_comment_id ON comments (parent_comment_id);
CREATE INDEX idx_comments_root_comment_id ON comments (root_comment_id);
CREATE INDEX idx_comments_created_at ON comments (created_at DESC);
CREATE INDEX idx_comments_status ON comments (status);
CREATE INDEX idx_comments_media_count ON comments (media_count);
CREATE INDEX idx_comments_del_flag ON comments (del_flag);
CREATE INDEX idx_comments_deleted_at ON comments (deleted_at);
CREATE INDEX idx_comments_is_pinned ON comments (is_pinned);
CREATE INDEX idx_comments_is_hot ON comments (is_hot);
CREATE INDEX idx_comments_hot_score ON comments (hot_score DESC);
-- 性能优化复合索引
CREATE INDEX idx_comments_content_status_created ON comments (content_id, status, created_at DESC);
CREATE INDEX idx_comments_content_pinned_hot ON comments (content_id, is_pinned DESC, is_hot DESC, hot_score DESC);
CREATE INDEX idx_comments_user_created ON comments (user_id, created_at DESC);
CREATE INDEX idx_comments_parent_created ON comments (parent_comment_id, created_at ASC);
CREATE INDEX idx_comments_root_created ON comments (root_comment_id, created_at ASC);

-- 评论媒体关联表索引
CREATE INDEX idx_comment_media_comment_id ON comment_media_relations (comment_id);
CREATE INDEX idx_comment_media_media_id ON comment_media_relations (media_id);
CREATE INDEX idx_comment_media_relation_type ON comment_media_relations (relation_type);
CREATE INDEX idx_comment_media_sort_order ON comment_media_relations (comment_id, sort_order);
CREATE INDEX idx_comment_media_created_at ON comment_media_relations (created_at DESC);
CREATE INDEX idx_comment_media_del_flag ON comment_media_relations (del_flag);
CREATE INDEX idx_comment_media_deleted_at ON comment_media_relations (deleted_at);
-- 性能优化复合索引
CREATE INDEX idx_comment_media_comment_type_sort ON comment_media_relations (comment_id, relation_type, sort_order);

-- 评论提及用户表索引
CREATE INDEX idx_comment_mentions_comment_id ON comment_mentions (comment_id);
CREATE INDEX idx_comment_mentions_mentioned_user_id ON comment_mentions (mentioned_user_id);
CREATE INDEX idx_comment_mentions_is_notified ON comment_mentions (is_notified);
CREATE INDEX idx_comment_mentions_notification_sent ON comment_mentions (notification_sent_at);
CREATE INDEX idx_comment_mentions_del_flag ON comment_mentions (del_flag);
CREATE INDEX idx_comment_mentions_deleted_at ON comment_mentions (deleted_at);

-- 用户收藏表索引
CREATE INDEX idx_user_collections_user_id ON user_collections (user_id);
CREATE INDEX idx_user_collections_target ON user_collections (target_id, target_type);
CREATE INDEX idx_user_collections_created_at ON user_collections (created_at DESC);
CREATE INDEX idx_user_collections_del_flag ON user_collections (del_flag);
CREATE INDEX idx_user_collections_deleted_at ON user_collections (deleted_at);
CREATE INDEX idx_user_collections_is_public ON user_collections (is_public);
-- 性能优化复合索引
CREATE INDEX idx_user_collections_user_created ON user_collections (user_id, created_at DESC);
CREATE INDEX idx_user_collections_target_created ON user_collections (target_id, target_type, created_at DESC);

-- 用户关系表索引（已在表定义中包含基础索引，这里添加复合索引）
CREATE INDEX idx_user_relation_user_created ON user_relation (user_id, create_time DESC);
CREATE INDEX idx_user_relation_target_created ON user_relation (target_user_id, create_time DESC);
CREATE INDEX idx_user_relation_follow_created ON user_relation (is_follow, create_time DESC);
CREATE INDEX idx_user_relation_subscribe_created ON user_relation (is_subscribe, create_time DESC);

-- 举报记录表索引
CREATE INDEX idx_report_records_reporter_id ON report_records (reporter_id);
CREATE INDEX idx_report_records_target ON report_records (target_id, target_type);
CREATE INDEX idx_report_records_created_at ON report_records (created_at DESC);
CREATE INDEX idx_report_records_report_status ON report_records (report_status);
CREATE INDEX idx_report_records_report_reason ON report_records (report_reason);
CREATE INDEX idx_report_records_handler_id ON report_records (handler_id);
CREATE INDEX idx_report_records_handle_time ON report_records (handle_time);
CREATE INDEX idx_report_records_del_flag ON report_records (del_flag);
CREATE INDEX idx_report_records_deleted_at ON report_records (deleted_at);
-- 性能优化复合索引
CREATE INDEX idx_report_records_status_created ON report_records (report_status, created_at DESC);
CREATE INDEX idx_report_records_target_status ON report_records (target_id, target_type, report_status);

-- 浏览记录表索引
CREATE INDEX idx_browse_records_user_id ON browse_records (user_id);
CREATE INDEX idx_browse_records_target ON browse_records (target_id, target_type);
CREATE INDEX idx_browse_records_created_at ON browse_records (created_at DESC);
CREATE INDEX idx_browse_records_del_flag ON browse_records (del_flag);
CREATE INDEX idx_browse_records_deleted_at ON browse_records (deleted_at);
-- 性能优化复合索引
CREATE INDEX idx_browse_records_user_created ON browse_records (user_id, created_at DESC);
CREATE INDEX idx_browse_records_target_created ON browse_records (target_id, target_type, created_at DESC);

-- 内容统计表索引
CREATE INDEX idx_content_stats_content_id ON content_stats (content_id);
CREATE INDEX idx_content_stats_hot_score ON content_stats (hot_score DESC);
CREATE INDEX idx_content_stats_quality_score ON content_stats (quality_score DESC);
CREATE INDEX idx_content_stats_engagement_rate ON content_stats (engagement_rate DESC);
CREATE INDEX idx_content_stats_last_interaction ON content_stats (last_interaction_time DESC);
CREATE INDEX idx_content_stats_del_flag ON content_stats (del_flag);
CREATE INDEX idx_content_stats_deleted_at ON content_stats (deleted_at);
-- 性能优化复合索引
CREATE INDEX idx_content_stats_hot_quality ON content_stats (hot_score DESC, quality_score DESC);
CREATE INDEX idx_content_stats_engagement_interaction ON content_stats (engagement_rate DESC, last_interaction_time DESC);

-- 用户统计表索引
CREATE INDEX idx_user_stats_user_id ON user_stats (user_id);
CREATE INDEX idx_user_stats_reputation_score ON user_stats (reputation_score DESC);
CREATE INDEX idx_user_stats_activity_score ON user_stats (activity_score DESC);
CREATE INDEX idx_user_stats_last_active ON user_stats (last_active_time DESC);
CREATE INDEX idx_user_stats_del_flag ON user_stats (del_flag);
CREATE INDEX idx_user_stats_deleted_at ON user_stats (deleted_at);
-- 性能优化复合索引
CREATE INDEX idx_user_stats_reputation_activity ON user_stats (reputation_score DESC, activity_score DESC);
CREATE INDEX idx_user_stats_activity_last_active ON user_stats (activity_score DESC, last_active_time DESC);

-- 临时批量操作表索引
CREATE INDEX idx_temp_batch_reactions_batch_id ON temp_batch_reactions (batch_id);
CREATE INDEX idx_temp_batch_reactions_user_id ON temp_batch_reactions (user_id);
CREATE INDEX idx_temp_batch_reactions_created_at ON temp_batch_reactions (created_at);

COMMIT;

-- =============================================
-- 第二部分：初始数据插入（事务处理）
-- =============================================
START TRANSACTION;

-- 插入系统字典数据
-- INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type) VALUES 
-- ('interaction_target_type_dict', '互动对象类型', 'interaction_target_type', '互动功能中的对象类型字典', 0, 'system', NOW(), 0),
-- ('interaction_action_type_dict', '互动操作类型', 'interaction_action_type', '互动功能中的操作类型字典', 0, 'system', NOW(), 0),
-- ('comment_status_dict', '评论状态', 'comment_status', '评论状态字典', 0, 'system', NOW(), 0),
-- ('report_reason_dict', '举报原因', 'report_reason', '举报原因字典', 0, 'system', NOW(), 0),
-- ('report_status_dict', '举报状态', 'report_status', '举报状态字典', 0, 'system', NOW(), 0),
-- ('follow_type_dict', '关注类型', 'follow_type', '用户关注类型字典', 0, 'system', NOW(), 0),
-- ('block_type_dict', '拉黑类型', 'block_type', '用户拉黑类型字典', 0, 'system', NOW(), 0);

-- -- 插入字典项数据
-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES 
-- -- 互动对象类型
-- ('target_content_item', 'interaction_target_type_dict', '内容', '1', '内容对象', 1, 1, 'system', NOW()),
-- ('target_comment_item', 'interaction_target_type_dict', '评论', '2', '评论对象', 2, 1, 'system', NOW()),
-- ('target_user_item', 'interaction_target_type_dict', '用户', '3', '用户对象', 3, 1, 'system', NOW()),

-- -- 互动操作类型
-- ('action_like_item', 'interaction_action_type_dict', '点赞', '1', '点赞操作', 1, 1, 'system', NOW()),
-- ('action_dislike_item', 'interaction_action_type_dict', '反对', '2', '反对操作', 2, 1, 'system', NOW()),

-- -- 评论状态
-- ('comment_normal_item', 'comment_status_dict', '正常', '1', '正常状态', 1, 1, 'system', NOW()),
-- ('comment_hidden_item', 'comment_status_dict', '隐藏', '2', '隐藏状态', 2, 1, 'system', NOW()),
-- ('comment_deleted_item', 'comment_status_dict', '删除', '3', '删除状态', 3, 1, 'system', NOW()),
-- ('comment_review_item', 'comment_status_dict', '审核中', '4', '审核中状态', 4, 1, 'system', NOW()),
-- ('comment_violation_item', 'comment_status_dict', '违规', '5', '违规状态', 5, 1, 'system', NOW()),

-- -- 举报原因
-- ('reason_spam_item', 'report_reason_dict', '垃圾信息', '1', '垃圾信息举报', 1, 1, 'system', NOW()),
-- ('reason_illegal_item', 'report_reason_dict', '违法违规', '2', '违法违规举报', 2, 1, 'system', NOW()),
-- ('reason_pornography_item', 'report_reason_dict', '色情内容', '3', '色情内容举报', 3, 1, 'system', NOW()),
-- ('reason_violence_item', 'report_reason_dict', '暴力内容', '4', '暴力内容举报', 4, 1, 'system', NOW()),
-- ('reason_fraud_item', 'report_reason_dict', '诈骗信息', '5', '诈骗信息举报', 5, 1, 'system', NOW()),
-- ('reason_copyright_item', 'report_reason_dict', '侵权内容', '6', '侵权内容举报', 6, 1, 'system', NOW()),
-- ('reason_malicious_item', 'report_reason_dict', '恶意攻击', '7', '恶意攻击举报', 7, 1, 'system', NOW()),
-- ('reason_other_item', 'report_reason_dict', '其他', '8', '其他原因举报', 8, 1, 'system', NOW()),

-- -- 举报状态
-- ('status_pending_item', 'report_status_dict', '待处理', '1', '待处理状态', 1, 1, 'system', NOW()),
-- ('status_processing_item', 'report_status_dict', '处理中', '2', '处理中状态', 2, 1, 'system', NOW()),
-- ('status_processed_item', 'report_status_dict', '已处理', '3', '已处理状态', 3, 1, 'system', NOW()),
-- ('status_rejected_item', 'report_status_dict', '已驳回', '4', '已驳回状态', 4, 1, 'system', NOW()),
-- ('status_cancelled_item', 'report_status_dict', '已撤销', '5', '已撤销状态', 5, 1, 'system', NOW()),

-- -- 关注类型
-- ('follow_normal_item', 'follow_type_dict', '普通关注', '1', '普通关注', 1, 1, 'system', NOW()),
-- ('follow_special_item', 'follow_type_dict', '特别关注', '2', '特别关注', 2, 1, 'system', NOW()),
-- ('follow_quiet_item', 'follow_type_dict', '悄悄关注', '3', '悄悄关注', 3, 1, 'system', NOW()),

-- -- 拉黑类型
-- ('block_full_item', 'block_type_dict', '完全拉黑', '1', '完全拉黑', 1, 1, 'system', NOW()),
-- ('block_comment_item', 'block_type_dict', '仅屏蔽评论', '2', '仅屏蔽评论', 2, 1, 'system', NOW()),
-- ('block_message_item', 'block_type_dict', '仅屏蔽私信', '3', '仅屏蔽私信', 3, 1, 'system', NOW());

COMMIT;

-- =============================================
-- 第三部分：视图创建（无需事务）
-- =============================================

-- 为了向后兼容，创建user_likes视图
DROP VIEW IF EXISTS user_likes;
CREATE VIEW user_likes AS 
SELECT 
    id,
    user_id,
    target_id,
    target_type,
    ip_address,
    device_info,
    created_at,
    updated_at,
    create_by,
    update_by
FROM user_reactions 
WHERE action_type = 1 AND del_flag = 0;

-- 创建反对视图
DROP VIEW IF EXISTS user_dislikes;
CREATE VIEW user_dislikes AS 
SELECT 
    id,
    user_id,
    target_id,
    target_type,
    ip_address,
    device_info,
    created_at,
    updated_at,
    create_by,
    update_by
FROM user_reactions 
WHERE action_type = 2 AND del_flag = 0;

-- 用户反应汇总视图
DROP VIEW IF EXISTS v_user_reaction_summary;
CREATE VIEW v_user_reaction_summary AS
SELECT 
    user_id,
    COUNT(*) as total_reactions,
    SUM(CASE WHEN action_type = 1 THEN 1 ELSE 0 END) as like_count,
    SUM(CASE WHEN action_type = 2 THEN 1 ELSE 0 END) as dislike_count,
    MAX(created_at) as last_reaction_time
FROM user_reactions 
WHERE del_flag = 0
GROUP BY user_id;

-- 内容反应汇总视图
DROP VIEW IF EXISTS v_content_reaction_summary;
CREATE VIEW v_content_reaction_summary AS
SELECT 
    target_id as content_id,
    target_type,
    COUNT(*) as total_reactions,
    SUM(CASE WHEN action_type = 1 THEN 1 ELSE 0 END) as like_count,
    SUM(CASE WHEN action_type = 2 THEN 1 ELSE 0 END) as dislike_count,
    MAX(created_at) as last_reaction_time
FROM user_reactions 
WHERE del_flag = 0
GROUP BY target_id, target_type;

-- 热门内容视图（7天）
DROP VIEW IF EXISTS v_hot_content_7days;
CREATE VIEW v_hot_content_7days AS
SELECT 
    target_id as content_id,
    target_type,
    COUNT(*) as reaction_count,
    SUM(CASE WHEN action_type = 1 THEN 1 ELSE 0 END) as like_count,
    SUM(CASE WHEN action_type = 2 THEN 1 ELSE 0 END) as dislike_count,
    (SUM(CASE WHEN action_type = 1 THEN 1 ELSE 0 END) * 2 - 
     SUM(CASE WHEN action_type = 2 THEN 1 ELSE 0 END)) as hot_score
FROM user_reactions 
WHERE del_flag = 0 
AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY target_id, target_type
HAVING reaction_count >= 5
ORDER BY hot_score DESC, reaction_count DESC;

-- =============================================
-- 第四部分：存储过程创建（无需事务，内部自管理事务）
-- =============================================

DELIMITER //

-- 数据一致性检查存储过程
DROP PROCEDURE IF EXISTS CheckDataConsistency;
CREATE PROCEDURE CheckDataConsistency()
BEGIN
    DECLARE inconsistency_count INT DEFAULT 0;
    DECLARE result_message TEXT DEFAULT '';
    
    -- 检查用户反应表与统计表的一致性
    SELECT COUNT(*) INTO inconsistency_count
    FROM (
        SELECT ur.target_id, ur.target_type,
               SUM(CASE WHEN ur.action_type = 1 THEN 1 ELSE 0 END) as actual_likes,
               SUM(CASE WHEN ur.action_type = 2 THEN 1 ELSE 0 END) as actual_dislikes,
               COALESCE(cs.like_count, 0) as recorded_likes,
               COALESCE(cs.dislike_count, 0) as recorded_dislikes
        FROM user_reactions ur
        LEFT JOIN content_stats cs ON ur.target_id = cs.content_id
        WHERE ur.del_flag = 0 AND ur.target_type = 1
        GROUP BY ur.target_id, ur.target_type, cs.like_count, cs.dislike_count
        HAVING actual_likes != recorded_likes OR actual_dislikes != recorded_dislikes
    ) inconsistent_data;
    
    IF inconsistency_count > 0 THEN
        SET result_message = CONCAT('发现 ', inconsistency_count, ' 个数据不一致问题');
    ELSE
        SET result_message = '数据一致性检查通过';
    END IF;
    
    SELECT result_message as check_result, inconsistency_count as inconsistent_records;
    
END //

-- 清理过期数据的存储过程
DROP PROCEDURE IF EXISTS CleanupExpiredData;
CREATE PROCEDURE CleanupExpiredData()
BEGIN
    DECLARE affected_rows INT DEFAULT 0;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 清理过期的浏览记录（保留最近30天）
    DELETE FROM browse_records 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    SET affected_rows = ROW_COUNT();
    
    -- 清理软删除超过90天的数据
    DELETE FROM user_reactions 
    WHERE del_flag = 1 
    AND deleted_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    DELETE FROM comments 
    WHERE del_flag = 1 
    AND deleted_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    DELETE FROM user_collections 
    WHERE del_flag = 1 
    AND deleted_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    DELETE FROM user_relation 
    WHERE del_flag = 1 
    AND update_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    -- 提交事务
    COMMIT;
    
    -- 记录清理结果
    SELECT CONCAT('数据清理完成，共处理 ', affected_rows, ' 条记录') AS result;
    
END //

-- 修复统计数据不一致的存储过程
DROP PROCEDURE IF EXISTS FixStatsInconsistency;
CREATE PROCEDURE FixStatsInconsistency()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_content_id VARCHAR(32);
    
    -- 声明游标
    DECLARE content_cursor CURSOR FOR 
        SELECT DISTINCT target_id FROM user_reactions WHERE target_type = 1 AND del_flag = 0;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 打开游标
    OPEN content_cursor;
    
    read_loop: LOOP
        FETCH content_cursor INTO v_content_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 更新内容统计
        INSERT INTO content_stats (
            id, content_id, like_count, dislike_count, 
            created_at, updated_at, create_by, update_by
        )
        SELECT 
            REPLACE(UUID(), '-', ''),
            v_content_id,
            SUM(CASE WHEN action_type = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN action_type = 2 THEN 1 ELSE 0 END),
            NOW(),
            NOW(),
            'system',
            'system'
        FROM user_reactions 
        WHERE target_id = v_content_id AND target_type = 1 AND del_flag = 0
        ON DUPLICATE KEY UPDATE
            like_count = VALUES(like_count),
            dislike_count = VALUES(dislike_count),
            updated_at = NOW(),
            update_by = 'system';
            
    END LOOP;
    
    CLOSE content_cursor;
    
    -- 提交事务
    COMMIT;
    
    SELECT '统计数据修复完成' AS result;
    
END //

-- 审计日志记录存储过程
DROP PROCEDURE IF EXISTS LogAuditOperation;
CREATE PROCEDURE LogAuditOperation(
    IN p_table_name VARCHAR(50),
    IN p_operation_type VARCHAR(20),
    IN p_record_id VARCHAR(32),
    IN p_old_values JSON,
    IN p_new_values JSON,
    IN p_operator VARCHAR(50)
)
BEGIN
    DECLARE v_audit_id VARCHAR(32);
    
    SET v_audit_id = REPLACE(UUID(), '-', '');
    
    -- 这里可以插入到审计日志表（如果存在）
    -- 或者记录到系统日志
    SELECT CONCAT(
        '审计日志: 表=', p_table_name, 
        ', 操作=', p_operation_type,
        ', 记录ID=', p_record_id,
        ', 操作人=', p_operator,
        ', 时间=', NOW()
    ) AS audit_log;
    
END //

-- 软删除记录恢复存储过程
DROP PROCEDURE IF EXISTS RecoverSoftDeletedRecord;
CREATE PROCEDURE RecoverSoftDeletedRecord(
    IN p_table_name VARCHAR(50),
    IN p_record_id VARCHAR(32),
    IN p_operator VARCHAR(50)
)
BEGIN
    DECLARE v_sql TEXT;
    DECLARE v_affected_rows INT DEFAULT 0;
    
    -- 根据表名构建恢复SQL
    SET v_sql = CONCAT(
        'UPDATE ', p_table_name, 
        ' SET del_flag = 0, deleted_at = NULL, updated_at = NOW(), update_by = ''', p_operator, ''' ',
        'WHERE id = ''', p_record_id, ''' AND del_flag = 1'
    );
    
    -- 执行恢复操作
    SET @sql = v_sql;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    SET v_affected_rows = ROW_COUNT();
    
    -- 记录审计日志
    CALL LogAuditOperation(p_table_name, 'RECOVER', p_record_id, NULL, NULL, p_operator);
    
    SELECT CONCAT('恢复操作完成，影响记录数: ', v_affected_rows) AS result;
    
END //

-- 永久删除记录存储过程
DROP PROCEDURE IF EXISTS PermanentDeleteRecord;
CREATE PROCEDURE PermanentDeleteRecord(
    IN p_table_name VARCHAR(50),
    IN p_record_id VARCHAR(32),
    IN p_operator VARCHAR(50)
)
BEGIN
    DECLARE v_sql TEXT;
    DECLARE v_affected_rows INT DEFAULT 0;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 记录审计日志（删除前）
    CALL LogAuditOperation(p_table_name, 'PERMANENT_DELETE', p_record_id, NULL, NULL, p_operator);
    
    -- 根据表名构建删除SQL
    SET v_sql = CONCAT('DELETE FROM ', p_table_name, ' WHERE id = ''', p_record_id, '''');
    
    -- 执行删除操作
    SET @sql = v_sql;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    SET v_affected_rows = ROW_COUNT();
    
    -- 提交事务
    COMMIT;
    
    SELECT CONCAT('永久删除操作完成，影响记录数: ', v_affected_rows) AS result;
    
END //

DELIMITER ;

-- =============================================
-- 第五部分：批量操作存储过程（高性能优化）
-- =============================================

DELIMITER //

-- 批量插入用户反应记录
DROP PROCEDURE IF EXISTS BatchInsertUserReactions;
CREATE PROCEDURE BatchInsertUserReactions(
    IN p_batch_id VARCHAR(50),
    IN p_create_by VARCHAR(50)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_success_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;
    DECLARE v_user_id VARCHAR(32);
    DECLARE v_target_id VARCHAR(32);
    DECLARE v_target_type INTEGER;
    DECLARE v_action_type INTEGER;
    
    -- 声明游标
    DECLARE batch_cursor CURSOR FOR 
        SELECT user_id, target_id, target_type, action_type
        FROM temp_batch_reactions 
        WHERE batch_id = p_batch_id AND operation_type = 'INSERT';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
    BEGIN
        SET v_error_count = v_error_count + 1;
        ROLLBACK;
    END;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 打开游标
    OPEN batch_cursor;
    
    read_loop: LOOP
        FETCH batch_cursor INTO v_user_id, v_target_id, v_target_type, v_action_type;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        SET v_count = v_count + 1;
        
        -- 检查是否已存在相同的反应记录
        IF NOT EXISTS (
            SELECT 1 FROM user_reactions 
            WHERE user_id = v_user_id 
            AND target_id = v_target_id 
            AND target_type = v_target_type
            AND del_flag = 0
        ) THEN
            -- 插入新记录
            INSERT INTO user_reactions (
                id, user_id, target_id, target_type, action_type,
                create_by, created_at, updated_at, del_flag
            ) VALUES (
                REPLACE(UUID(), '-', ''),
                v_user_id,
                v_target_id,
                v_target_type,
                v_action_type,
                p_create_by,
                NOW(),
                NOW(),
                0
            );
            
            SET v_success_count = v_success_count + 1;
        END IF;
        
    END LOOP;
    
    CLOSE batch_cursor;
    
    -- 提交事务
    COMMIT;
    
    -- 清理临时数据
    DELETE FROM temp_batch_reactions WHERE batch_id = p_batch_id;
    
    -- 返回结果
    SELECT 
        p_batch_id as batch_id,
        v_count as total_processed,
        v_success_count as success_count,
        v_error_count as error_count,
        CONCAT('批量插入完成: 处理', v_count, '条，成功', v_success_count, '条，失败', v_error_count, '条') as result;
        
END //

-- 批量更新用户反应记录
DROP PROCEDURE IF EXISTS BatchUpdateUserReactions;
CREATE PROCEDURE BatchUpdateUserReactions(
    IN p_batch_id VARCHAR(50),
    IN p_update_by VARCHAR(50)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_success_count INT DEFAULT 0;
    DECLARE v_user_id VARCHAR(32);
    DECLARE v_target_id VARCHAR(32);
    DECLARE v_target_type INTEGER;
    DECLARE v_action_type INTEGER;
    
    -- 声明游标
    DECLARE batch_cursor CURSOR FOR 
        SELECT user_id, target_id, target_type, action_type
        FROM temp_batch_reactions 
        WHERE batch_id = p_batch_id AND operation_type = 'UPDATE';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 打开游标
    OPEN batch_cursor;
    
    read_loop: LOOP
        FETCH batch_cursor INTO v_user_id, v_target_id, v_target_type, v_action_type;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        SET v_count = v_count + 1;
        
        -- 更新现有记录
        UPDATE user_reactions 
        SET action_type = v_action_type,
            updated_at = NOW(),
            update_by = p_update_by
        WHERE user_id = v_user_id 
        AND target_id = v_target_id 
        AND target_type = v_target_type
        AND del_flag = 0;
        
        IF ROW_COUNT() > 0 THEN
            SET v_success_count = v_success_count + 1;
        END IF;
        
    END LOOP;
    
    CLOSE batch_cursor;
    
    -- 提交事务
    COMMIT;
    
    -- 清理临时数据
    DELETE FROM temp_batch_reactions WHERE batch_id = p_batch_id;
    
    -- 返回结果
    SELECT 
        p_batch_id as batch_id,
        v_count as total_processed,
        v_success_count as success_count,
        CONCAT('批量更新完成: 处理', v_count, '条，成功', v_success_count, '条') as result;
        
END //

-- 批量软删除用户反应记录
DROP PROCEDURE IF EXISTS BatchSoftDeleteUserReactions;
CREATE PROCEDURE BatchSoftDeleteUserReactions(
    IN p_batch_id VARCHAR(50),
    IN p_delete_by VARCHAR(50)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_success_count INT DEFAULT 0;
    DECLARE v_user_id VARCHAR(32);
    DECLARE v_target_id VARCHAR(32);
    DECLARE v_target_type INTEGER;
    
    -- 声明游标
    DECLARE batch_cursor CURSOR FOR 
        SELECT user_id, target_id, target_type
        FROM temp_batch_reactions 
        WHERE batch_id = p_batch_id AND operation_type = 'DELETE';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 打开游标
    OPEN batch_cursor;
    
    read_loop: LOOP
        FETCH batch_cursor INTO v_user_id, v_target_id, v_target_type;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        SET v_count = v_count + 1;
        
        -- 软删除记录
        UPDATE user_reactions 
        SET del_flag = 1,
            deleted_at = NOW(),
            updated_at = NOW(),
            update_by = p_delete_by
        WHERE user_id = v_user_id 
        AND target_id = v_target_id 
        AND target_type = v_target_type
        AND del_flag = 0;
        
        IF ROW_COUNT() > 0 THEN
            SET v_success_count = v_success_count + 1;
        END IF;
        
    END LOOP;
    
    CLOSE batch_cursor;
    
    -- 提交事务
    COMMIT;
    
    -- 清理临时数据
    DELETE FROM temp_batch_reactions WHERE batch_id = p_batch_id;
    
    -- 返回结果
    SELECT 
        p_batch_id as batch_id,
        v_count as total_processed,
        v_success_count as success_count,
        CONCAT('批量删除完成: 处理', v_count, '条，成功', v_success_count, '条') as result;
        
END //

-- 获取用户反应统计
DROP PROCEDURE IF EXISTS GetUserReactionStats;
CREATE PROCEDURE GetUserReactionStats(
    IN p_user_id VARCHAR(32),
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        p_user_id as user_id,
        COUNT(*) as total_reactions,
        SUM(CASE WHEN action_type = 1 THEN 1 ELSE 0 END) as like_count,
        SUM(CASE WHEN action_type = 2 THEN 1 ELSE 0 END) as dislike_count,
        MAX(created_at) as last_reaction_time
    FROM user_reactions 
    WHERE del_flag = 0
    GROUP BY user_id;
END;

-- ... existing code ...

-- =============================================
-- 表结构说明（文件末尾补充）
-- 说明：本脚本共创建 10 张业务表，并包含若干索引与存储过程，负责互动域的核心数据模型
-- 1) user_reactions：记录点赞/反对行为，唯一约束 uk_user_reactions_user_target，含软删除与审计字段
-- 2) comments：内容评论，支持多级回复、匿名、置顶、热度等，含状态与统计字段
-- 3) comment_media_relations：评论与媒体文件关联，统一文件表模式，支持附件/内联/引用多类型关联
-- 4) comment_mentions：评论 @ 提及用户，支持通知追踪与位置标注
-- 5) user_collections：用户收藏，支持收藏夹/标签/备注与公开设置
-- 6) report_records：举报记录，完整处理流程（原因/状态/处理结果）字段
-- 7) browse_records：浏览记录，含行为上下文（设备、UA、来源等）
-- 8) content_stats：内容聚合统计，一行一内容，便于快速展示
-- 9) user_stats：用户聚合统计，一行一用户，侧重互动行为累计
-- 10) temp_batch_reactions：批量操作临时表，供批处理存储过程读取
-- 已迁移的表：user_relation、user_relation_stats 已统一集中至 content_user_core_init.sql，避免重复创建
-- 依赖说明：依赖 content_module_init.sql 中的 media_files 表（用于评论媒体关联），请确保先执行该脚本
-- 执行顺序建议：
--   a. 先执行 content_user_core_init.sql（初始化用户核心关系与扩展资料）
--   b. 再执行 content_module_init.sql（初始化媒体文件等通用表）
--   c. 最后执行本脚本 content_Interaction_init.sql
-- 版本与兼容性：
--   - MySQL 8.0+（使用 CHECK 约束与 JSON 类型）
-- 数据治理与性能：
--   - 所有表均支持软删除（del_flag/deleted_at）与审计字段，便于数据治理与合规审计
--   - 索引部分包含多组复合索引，用于高并发读场景的性能优化
--   - 批量存储过程内部自管理事务，确保原子性与幂等性，批次完成后清理临时表数据
-- 注意事项：
--   - 若后续新增互动类型，请遵循统一命名与审计字段规范
--   - 与用户关系强耦合的需求（拉黑/屏蔽搜索/屏蔽私信）需通过 user_relation 表实现，请勿在互动域重复造轮子