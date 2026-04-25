-- =============================================
-- 内容社区系统数据库初始化脚本 (修正版)
-- 数据库类型为mysql 版本8.0+
-- 基于需求文档进行全面修正和补充
-- =============================================

START TRANSACTION;

-- =============================================
-- 1. 核心内容表 (contents) - 修正版
-- =============================================
CREATE TABLE IF NOT EXISTS contents (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '内容ID',
    title VARCHAR(200) COMMENT '标题',
    slug VARCHAR(255) COMMENT 'URL友好的唯一标识符',
    content JSON COMMENT '内容正文(JSON格式存储富文本)',
    content_type INTEGER NOT NULL COMMENT '内 容类型：1-文章 2-帖子POST 3-问答问题 4-问答答案 5-视频 6-笔记',
    author_id VARCHAR(32) NOT NULL COMMENT '作者ID（关联sys_user表）',
    community_id VARCHAR(32) COMMENT '所属社区ID',
    channel_id VARCHAR(32) COMMENT '所属频道ID 与channels表关联',
    rendered_content LONGTEXT COMMENT '渲染后的html内容',
    plain_content TEXT COMMENT '原始内容正文（用于搜索和索引）',
    
    -- 内容属性
    cover_image JSON COMMENT '图片信息 冗余存储 id 地址 文件类型 宽高 ,双写入表content_media_relations',
    attachments JSON COMMENT '附件列表信息 冗余存储 id 地址 文件类型 宽高 时长 文件大小,每个元素都是media_files表中信息,采用冗余存储 ,双写入表content_media_relations',
    summary TEXT COMMENT '内容摘要',
    content_version BIGINT DEFAULT 1 COMMENT '内容版本号',
    word_count INTEGER DEFAULT 0 COMMENT '字数统计',
    reading_time INTEGER DEFAULT 0 COMMENT '预计阅读时间(分钟)',
    
    -- 付费与预览策略（支付墙）
    paywall_enabled INTEGER DEFAULT 0 COMMENT '是否开启付费阅读：0-否 1-是（开启后未购买仅展示预览）',
    pay_type INTEGER DEFAULT 1 COMMENT '付费类型：1-免费 2-单篇付费 3-专栏/订阅 4-会员专享',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '单篇付费价格（单位：元，人民币）',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '币种（默认CNY，预留扩展如USD, EUR）',
    preview_strategy INTEGER DEFAULT 1 COMMENT '预览策略：1-按字数 2-按百分比 3-使用摘要summary 4-自定义预览内容',
    preview_length INTEGER DEFAULT 200 COMMENT '预览字数（当策略为1时生效，建议200-500）',
    preview_percent INTEGER DEFAULT 20 COMMENT '预览百分比（当策略为2时生效，取值1-90）',
    preview_content LONGTEXT COMMENT '自定义预览内容（当策略为4时生效，用于展示未购买时的内容片段）',
    preview_hint TEXT COMMENT '预览提示文案（例如：购买后可阅读全文）',
    
    -- 统计字段
    view_count BIGINT DEFAULT 0 COMMENT '浏览数',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    dislike_count BIGINT DEFAULT 0 COMMENT '反对数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    favorite_count BIGINT DEFAULT 0 COMMENT '收藏数',
    share_count BIGINT DEFAULT 0 COMMENT '分享数',
    repost_count BIGINT DEFAULT 0 COMMENT '转发数',
    
    -- 状态字段
    status INTEGER DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已删除 3-审核中 4-审核不通过 5 下架',
    visibility INTEGER DEFAULT 1 COMMENT '可见性：1-公开 2-仅关注者 3-私密',
    reply_type INTEGER DEFAULT 1 COMMENT '回复类型：1 EVERYONE: 所有人都可以回复。2 FOLLOW: 只有作者关注的人可以回复。3 MENTION: 只有在内容中被@到的人可以回复。4 FOLLOWERS_ONLY: 只有关注我的粉丝可以评论。5 FOLLOWERS_3DAYS: 关注我3天及以上粉丝才可以评论。6 DISABLED: 禁止所有人评论。',
    is_top INTEGER DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    is_hot INTEGER DEFAULT 0 COMMENT '是否热门：0-否 1-是',
    is_essence INTEGER DEFAULT 0 COMMENT '是否精华：0-否 1-是',
    is_recommend INTEGER DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    
    -- 转发相关
    source_type INTEGER DEFAULT 1 COMMENT '来源类型：1-原创 2-转发',
    original_content_id VARCHAR(32) COMMENT '原始内容ID（转发时使用）',
    
    -- 问答相关
    parent_content_id VARCHAR(32) COMMENT '父内容ID（答案关联问题）',
    is_accepted INTEGER DEFAULT 0 COMMENT '是否被采纳：0-否 1-是（仅答案使用）',
    accepted_at TIMESTAMP COMMENT '采纳时间',
    
    -- 地理位置
    location_latitude DECIMAL(10, 8) COMMENT '纬度',
    location_longitude DECIMAL(11, 8) COMMENT '经度',
    location_address VARCHAR(200) COMMENT '地址描述',
    
    -- 排序字段
    priority INTEGER DEFAULT 0 COMMENT '优先级（用于排序和权限控制）',

    -- 时间字段
    publish_at TIMESTAMP COMMENT '发布时间',
    last_edit_at TIMESTAMP COMMENT '最后编辑时间',
    scheduled_date TIMESTAMP COMMENT '计划发布时间',
    
    -- 扩展字段
    ext_data JSON COMMENT '扩展数据',
    tags JSON COMMENT '标签数组',
    topics JSON COMMENT '主题列表',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 内容表索引 
CREATE UNIQUE INDEX uk_contents_slug ON contents(slug);
CREATE INDEX idx_contents_author_id ON contents(author_id);
CREATE INDEX idx_contents_community_id ON contents(community_id);
CREATE INDEX idx_contents_type_status ON contents(content_type, status);
CREATE INDEX idx_contents_publish_at ON contents(publish_at DESC);
CREATE INDEX idx_contents_hot_recommend ON contents(is_hot, is_recommend);
CREATE INDEX idx_contents_view_count ON contents(view_count DESC);
CREATE INDEX idx_contents_like_count ON contents(like_count DESC);
CREATE INDEX idx_contents_original_id ON contents(original_content_id);
CREATE INDEX idx_contents_parent_id ON contents(parent_content_id);
CREATE INDEX idx_contents_del_flag ON contents(del_flag);
-- 支付墙相关索引
CREATE INDEX idx_contents_paywall_enabled ON contents(paywall_enabled);
CREATE INDEX idx_contents_pay_type ON contents(pay_type);
CREATE INDEX idx_contents_price ON contents(price);

-- =============================================
-- 3. 媒体文件表 (media_files) - 修正版
-- =============================================
CREATE TABLE IF NOT EXISTS media_files (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '媒体文件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) COMMENT '原始文件名',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    thumbnail_url VARCHAR(500) COMMENT '缩略图URL',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型：image/video/audio/document',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    
    -- 媒体属性
    width INTEGER COMMENT '宽度(像素)',
    height INTEGER COMMENT '高度(像素)',
    duration BIGINT COMMENT '时长(毫秒)',
    resolution VARCHAR(20) COMMENT '分辨率',
    bitrate INTEGER COMMENT '比特率',
    
    -- 存储相关
    storage_type VARCHAR(20) DEFAULT 'oss' COMMENT '存储类型：local/oss/cdn',
    storage_path VARCHAR(500) COMMENT '存储路径',
    cdn_urls JSON COMMENT 'CDN地址列表',
    md5 VARCHAR(32) COMMENT '文件MD5校验值',
    
    -- 处理状态
    status INTEGER DEFAULT 1 COMMENT '状态：1-正常 2-处理中 3-失败',
    process_status VARCHAR(20) DEFAULT 'completed' COMMENT '处理状态：pending/processing/completed/failed',
    
    -- 元数据
    metadata JSON COMMENT '文件元数据',
    
    upload_by VARCHAR(32) COMMENT '上传者ID',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 媒体文件表索引
CREATE INDEX idx_media_upload_by ON media_files(upload_by);
CREATE INDEX idx_media_file_type ON media_files(file_type);
CREATE INDEX idx_media_upload_time ON media_files(upload_time DESC);
CREATE INDEX idx_media_status ON media_files(status);
CREATE INDEX idx_media_del_flag ON media_files(del_flag);

-- =============================================
-- 4. 内容媒体关联表 (contentattachments_media_relations) 多对多关联- 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_media_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    media_id VARCHAR(32) NOT NULL COMMENT '媒体文件ID',
    relation_type INTEGER NOT NULL COMMENT '关联类型：1-封面/2-内联/3-附件',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
    position_data JSON COMMENT '位置数据',
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 内容媒体关联表索引
CREATE UNIQUE INDEX uk_content_media ON content_media_relations(content_id, media_id, relation_type);
CREATE INDEX idx_relations_content_id ON content_media_relations(content_id);
CREATE INDEX idx_relations_media_id ON content_media_relations(media_id);

-- =============================================
-- 5. 内联实体表 (content_inline_entities) - 修正版
-- =============================================
CREATE TABLE IF NOT EXISTS content_inline_entities (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '实体ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    entity_type VARCHAR(20) NOT NULL COMMENT '实体类型：mention/hashtag/stock/link/topic',
    entity_id VARCHAR(100) NOT NULL COMMENT '实体标识',
    entity_text VARCHAR(200) NOT NULL COMMENT '实体文本',
    start_pos INTEGER NOT NULL COMMENT '开始位置',
    end_pos INTEGER NOT NULL COMMENT '结束位置',
    entity_data JSON COMMENT '实体扩展数据',
    
    create_by VARCHAR(32) COMMENT '创建人',
    update_by VARCHAR(32) COMMENT '更新人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 内联实体表索引
CREATE INDEX idx_inline_content_id ON content_inline_entities(content_id);
CREATE INDEX idx_inline_entity_type ON content_inline_entities(entity_type, entity_id);
CREATE INDEX idx_inline_position ON content_inline_entities(content_id, start_pos);

-- =============================================
-- 6. 内容标签表 (content_tags) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_tags (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    slug VARCHAR(255) COMMENT 'URL友好的唯一标识符',
    tag_color VARCHAR(7) COMMENT '标签颜色',
    tag_icon VARCHAR(100) COMMENT '标签图标',
    description TEXT COMMENT '标签描述',
    use_count BIGINT DEFAULT 0 COMMENT '使用次数',
    
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建者ID',
    update_by VARCHAR(32) COMMENT '更新者ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 标签表索引
CREATE UNIQUE INDEX uk_tag_name ON content_tags(tag_name);
CREATE UNIQUE INDEX uk_tags_slug ON content_tags(slug);
CREATE INDEX idx_tags_use_count ON content_tags(use_count DESC);

-- =============================================
-- 7. 内容话题表 (content_topics) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_topics (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '话题ID',
    topic_name VARCHAR(100) NOT NULL COMMENT '话题名称',
    slug VARCHAR(255) COMMENT 'URL友好的唯一标识符',
    topic_desc TEXT COMMENT '话题描述',
    cover_image VARCHAR(500) COMMENT '话题封面',
    participant_count BIGINT DEFAULT 0 COMMENT '参与人数',
    content_count BIGINT DEFAULT 0 COMMENT '内容数量',
    
    is_hot INTEGER DEFAULT 0 COMMENT '是否热门：0-否 1-是',
    status INTEGER DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-审核中 3-审核拒绝',
    
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    creator_id VARCHAR(32) COMMENT '创建者ID',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 话题表索引
CREATE UNIQUE INDEX uk_topic_name ON content_topics(topic_name);
CREATE UNIQUE INDEX uk_topics_slug ON content_topics(slug);
CREATE INDEX idx_topics_hot ON content_topics(is_hot, participant_count DESC);
CREATE INDEX idx_topics_content_count ON content_topics(content_count DESC);

-- =============================================
-- 7.1. 内容标签关联表 (content_tag_relations) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_tag_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    tag_id VARCHAR(32) NOT NULL COMMENT '标签ID',
    
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 内容标签关联表索引
CREATE UNIQUE INDEX uk_content_tag ON content_tag_relations(content_id, tag_id);
CREATE INDEX idx_tag_relations_content_id ON content_tag_relations(content_id);
CREATE INDEX idx_tag_relations_tag_id ON content_tag_relations(tag_id);

-- =============================================
-- 7.2. 内容话题关联表 (content_topic_relations) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_topic_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    topic_id VARCHAR(32) NOT NULL COMMENT '话题ID',
    
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 内容话题关联表索引
CREATE UNIQUE INDEX uk_content_topic ON content_topic_relations(content_id, topic_id);
CREATE INDEX idx_topic_relations_content_id ON content_topic_relations(content_id);
CREATE INDEX idx_topic_relations_topic_id ON content_topic_relations(topic_id);

-- =============================================
-- 用户关注话题表 (user_topic_follows)
-- =============================================
CREATE TABLE IF NOT EXISTS user_topic_follows (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID（关联sys_user表）',
    topic_id VARCHAR(32) NOT NULL COMMENT '话题ID（关联content_topics表）',
    follow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    notification_enabled INTEGER DEFAULT 1 COMMENT '是否开启通知：0-否 1-是',
    interest_level INTEGER DEFAULT 1 COMMENT '兴趣等级：1-一般 2-感兴趣 3-非常感兴趣',
    follow_source VARCHAR(50) DEFAULT 'manual' COMMENT '关注来源：manual-手动关注 recommend-推荐关注 search-搜索关注',
    
    -- 审计字段
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常 1-删除',
    
    -- 约束
    CONSTRAINT uk_user_topic_follows UNIQUE (user_id, topic_id, del_flag)
) COMMENT '用户关注话题表';

-- 用户关注话题表索引
CREATE INDEX idx_user_topic_follows_user_id ON user_topic_follows (user_id, del_flag);
CREATE INDEX idx_user_topic_follows_topic_id ON user_topic_follows (topic_id, del_flag);
CREATE INDEX idx_user_topic_follows_follow_time ON user_topic_follows (follow_time DESC, del_flag);
CREATE INDEX idx_user_topic_follows_notification ON user_topic_follows (user_id, notification_enabled, del_flag);
CREATE INDEX idx_user_topic_follows_interest ON user_topic_follows (user_id, interest_level DESC, del_flag);

-- =============================================
-- 用户关注标签表 (user_tag_follows)
-- =============================================
CREATE TABLE IF NOT EXISTS user_tag_follows (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID（关联sys_user表）',
    tag_id VARCHAR(32) NOT NULL COMMENT '标签ID（关联content_tags表）',
    follow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    notification_enabled INTEGER DEFAULT 1 COMMENT '是否开启通知：0-否 1-是',
    interest_level INTEGER DEFAULT 1 COMMENT '兴趣等级：1-一般 2-感兴趣 3-非常感兴趣',
    follow_source VARCHAR(50) DEFAULT 'manual' COMMENT '关注来源：manual-手动关注 recommend-推荐关注 search-搜索关注',
    
    -- 审计字段
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志：0-正常 1-删除',
    
    -- 约束
    CONSTRAINT uk_user_tag_follows UNIQUE (user_id, tag_id, del_flag)
) COMMENT '用户关注标签表';

-- 用户关注标签表索引
CREATE INDEX idx_user_tag_follows_user_id ON user_tag_follows (user_id, del_flag);
CREATE INDEX idx_user_tag_follows_tag_id ON user_tag_follows (tag_id, del_flag);
CREATE INDEX idx_user_tag_follows_follow_time ON user_tag_follows (follow_time DESC, del_flag);
CREATE INDEX idx_user_tag_follows_notification ON user_tag_follows (user_id, notification_enabled, del_flag);
CREATE INDEX idx_user_tag_follows_interest ON user_tag_follows (user_id, interest_level DESC, del_flag);
-- =============================================
-- 8. 内容股票关联表 (content_stock_relations) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_stock_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    stock_code VARCHAR(20) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(100) COMMENT '股票名称',
    market VARCHAR(10) COMMENT '市场：SH/SZ/HK/US',
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 股票关联表索引
CREATE INDEX idx_stock_content_id ON content_stock_relations(content_id);
CREATE INDEX idx_stock_code ON content_stock_relations(stock_code);

-- =============================================
-- 9. 内容提及表 (content_mentions) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_mentions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '提及ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    mentioned_user_id VARCHAR(32) NOT NULL COMMENT '被提及用户ID',
    mention_text VARCHAR(100) NOT NULL COMMENT '提及文本',
    position_start INTEGER COMMENT '开始位置',
    position_end INTEGER COMMENT '结束位置',
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 提及表索引
CREATE INDEX idx_mentions_content_id ON content_mentions(content_id);
CREATE INDEX idx_mentions_user_id ON content_mentions(mentioned_user_id);

-- =============================================
-- 10. 投票表 (polls) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS polls (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '投票ID',
    content_id VARCHAR(32) NOT NULL COMMENT '关联内容ID',
    title VARCHAR(200) NOT NULL COMMENT '投票标题',
    description TEXT COMMENT '投票描述',
    
    poll_type INTEGER DEFAULT 1 COMMENT '投票类型：1-单选 2-多选',
    max_choices INTEGER DEFAULT 1 COMMENT '最大选择数',
    
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    
    total_votes BIGINT DEFAULT 0 COMMENT '总投票数',
    status INTEGER DEFAULT 1 COMMENT '状态：1-进行中 2-已结束 3-已取消',
    
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',

    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 投票表索引
CREATE INDEX idx_polls_content_id ON polls(content_id);
CREATE INDEX idx_polls_status_time ON polls(status, end_time);

-- =============================================
-- 11. 投票选项表 (poll_options) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS poll_options (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '选项ID',
    poll_id VARCHAR(32) NOT NULL COMMENT '投票ID',
    option_text VARCHAR(200) NOT NULL COMMENT '选项文本',
    option_image VARCHAR(500) COMMENT '选项图片',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
    vote_count BIGINT DEFAULT 0 COMMENT '得票数',
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 投票选项表索引
CREATE INDEX idx_poll_options_poll_id ON poll_options(poll_id);
CREATE INDEX idx_poll_options_sort ON poll_options(poll_id, sort_order);

-- =============================================
-- 12. 用户投票记录表 (user_poll_votes) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS user_poll_votes (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '投票记录ID',
    poll_id VARCHAR(32) NOT NULL COMMENT '投票ID',
    option_id VARCHAR(32) NOT NULL COMMENT '选项ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    
    vote_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间'
);

-- 用户投票记录表索引
CREATE UNIQUE INDEX uk_user_poll_option ON user_poll_votes(user_id, poll_id, option_id);
CREATE INDEX idx_votes_poll_id ON user_poll_votes(poll_id);
CREATE INDEX idx_votes_user_id ON user_poll_votes(user_id);

-- =============================================
-- 13. 内容广告表 (content_ads) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_ads (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '广告ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    ad_type VARCHAR(20) NOT NULL COMMENT '广告类型：banner/native/video',
    ad_title VARCHAR(200) COMMENT '广告标题',
    ad_content TEXT COMMENT '广告内容',
    ad_image VARCHAR(500) COMMENT '广告图片',
    ad_url VARCHAR(500) COMMENT '广告链接',
    
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    start_time TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    status INTEGER DEFAULT 1 COMMENT '状态：1-正常 2-暂停 3-结束',
    
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 广告表索引
CREATE INDEX idx_ads_content_id ON content_ads(content_id);
CREATE INDEX idx_ads_status_time ON content_ads(status, start_time, end_time);

-- =============================================
-- 14. 内容草稿表 (content_drafts) - 新增
-- =============================================

CREATE TABLE content_drafts (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '草稿ID',
    
    -- 基础信息（与contents表保持一致）
    title VARCHAR(200) COMMENT '标题',
    content JSON COMMENT '内容正文(JSON格式存储富文本)',
    rendered_content LONGTEXT COMMENT '渲染后的内容',
    content_type INTEGER NOT NULL COMMENT '内容类型：1-文章 2-帖子POST 3-问答问题 4-问答答案 5-视频 6-图片',
    author_id VARCHAR(32) NOT NULL COMMENT '作者ID（关联sys_user表）',
    community_id VARCHAR(32) COMMENT '所属社区ID',
    channel_id VARCHAR(32) COMMENT '所属频道ID',
    
    -- 内容属性（与contents表保持一致）
    cover_image JSON COMMENT '封面图片信息',
    attachments JSON COMMENT '附件信息',
    summary TEXT COMMENT '内容摘要',
    word_count INTEGER DEFAULT 0 COMMENT '字数统计',
    reading_time INTEGER DEFAULT 0 COMMENT '预计阅读时间(分钟)',
    
    -- 发布设置
    visibility INTEGER DEFAULT 1 COMMENT '可见性：1-公开 2-仅关注者 3-私密',
    reply_type INTEGER DEFAULT 1 COMMENT '回复类型：1 EVERYONE: 所有人都可以回复。2 FOLLOW: 只有作者关注的人可以回复。3 MENTION: 只有在内容中被@到的人可以回复。4 FOLLOWERS_ONLY: 只有关注我的粉丝可以评论。5 FOLLOWERS_3DAYS: 关注我3天及以上粉丝才可以评论。6 DISABLED: 禁止所有人评论。',
    scheduled_date TIMESTAMP COMMENT '计划发布时间',
    
    -- 转发相关
    source_type INTEGER DEFAULT 1 COMMENT '来源类型：1-原创 2-转发',
    original_content_id VARCHAR(32) COMMENT '原始内容ID（转发时使用）',
    
    -- 问答相关
    parent_content_id VARCHAR(32) COMMENT '父内容ID（答案关联问题）',
    
    -- 地理位置
    location_latitude DECIMAL(10, 8) COMMENT '纬度',
    location_longitude DECIMAL(11, 8) COMMENT '经度',
    location_address VARCHAR(200) COMMENT '地址描述',
    
    -- 扩展字段
    tags JSON COMMENT '标签数组',
    ext_data JSON COMMENT '扩展数据',
    topics JSON COMMENT '话题列表',
    
    -- 草稿特有字段
    draft_status INTEGER DEFAULT 1 COMMENT '草稿状态：1-编辑中 2-待发布 3-定时发布 4-已发布 5-发布失败',
    auto_save INTEGER DEFAULT 1 COMMENT '是否自动保存：0-否 1-是',
    save_count INTEGER DEFAULT 0 COMMENT '保存次数',
    last_save_time TIMESTAMP COMMENT '最后保存时间',
    
    -- 发布相关
    published_content_id VARCHAR(32) COMMENT '发布后的内容ID',
    publish_at TIMESTAMP COMMENT '发布时间',
    publish_error TEXT COMMENT '发布失败原因',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 草稿表索引
CREATE INDEX idx_drafts_author_id ON content_drafts(author_id);
CREATE INDEX idx_drafts_status ON content_drafts(draft_status);
CREATE INDEX idx_drafts_update_time ON content_drafts(update_time DESC);
CREATE INDEX idx_drafts_scheduled ON content_drafts(scheduled_date);
CREATE INDEX idx_drafts_published ON content_drafts(published_content_id);
CREATE INDEX idx_drafts_community_author ON content_drafts(community_id, author_id);
CREATE INDEX idx_drafts_type_status ON content_drafts(content_type, draft_status);
CREATE INDEX idx_drafts_del_flag ON content_drafts(del_flag);

-- =============================================
-- 15. 内容版本表 (content_versions) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS content_versions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '版本ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    version_number BIGINT NOT NULL COMMENT '版本号',
    base_version_number BIGINT NOT NULL COMMENT '基础版本号',
    
    parent_snapshot_id VARCHAR(32) COMMENT '父快照引用',
    is_base_snapshot TINYINT(1) DEFAULT 0 COMMENT '是否基础快照',
    is_head TINYINT(1) DEFAULT 0 COMMENT '是否最新版本',
    is_active TINYINT(1) DEFAULT 0 COMMENT '是否激活版本',
    content_patch LONGTEXT COMMENT '原始内容/差异',
    rendered_patch LONGTEXT COMMENT '渲染内容/差异',
    owner VARCHAR(32) NOT NULL COMMENT '修改者',

    version_type VARCHAR(20) DEFAULT 'DRAFT' COMMENT '版本类型：CONTENT-内容版本, DRAFT-草稿版本',
    save_type INTEGER DEFAULT 1 COMMENT '保存类型：1-自动保存 2-手动保存',
    change_type VARCHAR(20) COMMENT '变更类型：create/update/delete',
    change_reason TEXT COMMENT '变更原因',
    
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 唯一性约束
    UNIQUE KEY uk_content_head (content_id, is_head),
    UNIQUE KEY uk_content_version_number (content_id, version_number),
    UNIQUE KEY uk_content_active (content_id, is_active)
);

-- 版本表索引
-- 针对CONTENT类型的优化索引（高频查询）
CREATE INDEX idx_versions_content_type_number ON content_versions(content_id, version_type, version_number DESC);
CREATE INDEX idx_versions_create_time ON content_versions(create_time DESC);
CREATE INDEX idx_versions_creator ON content_versions(create_by, create_time DESC);

-- 针对DRAFT类型的轻量级索引（低频查询）
CREATE INDEX idx_versions_draft_simple ON content_versions(content_id, create_time DESC);
CREATE INDEX idx_versions_content_id ON content_versions(content_id);
CREATE INDEX idx_versions_type ON content_versions(version_type);
CREATE INDEX idx_versions_number ON content_versions(content_id, version_number DESC);
-- 复合索引：按内容ID和版本类型查询（常用查询场景）
CREATE INDEX idx_versions_content_type ON content_versions(content_id, version_type);
-- 复合索引：按版本类型和创建时间查询（用于按类型分页查询）
CREATE INDEX idx_versions_type_time ON content_versions(version_type, create_time DESC);
-- 复合索引：按内容ID、版本类型和状态查询（用于查找特定状态的版本）
CREATE INDEX idx_versions_content_type_status ON content_versions(content_id, version_type, is_active);
-- 复合索引：按创建者和版本类型查询（用于查询用户的特定类型版本）
CREATE INDEX idx_versions_owner_type ON content_versions(owner, version_type, create_time DESC);

-- =============================================
-- 15.1 内容访问授权表 (content_access_rights) - 新增
-- 说明：记录用户对内容的访问授权（购买、订阅、会员解锁等），用于详情页判断是否可阅读全文
-- =============================================
CREATE TABLE IF NOT EXISTS content_access_rights (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '访问授权记录ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID（关联contents表）',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID（关联sys_user表）',
    access_type INTEGER NOT NULL DEFAULT 2 COMMENT '授权类型：1-免费 2-购买 3-订阅 4-会员',
    status INTEGER NOT NULL DEFAULT 1 COMMENT '授权状态：1-有效 2-过期 3-撤销',
    granted_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权生效时间',
    expired_time TIMESTAMP COMMENT '授权过期时间（可为空，单次购买通常为空）',
    source_order_id VARCHAR(64) COMMENT '来源订单号/账单号（用于幂等与审计）',
    
    -- 并发与审计
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号（并发安全）',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一约束：同一用户同一内容同一授权类型仅一条有效记录
    CONSTRAINT uk_user_content_access UNIQUE (user_id, content_id, access_type, del_flag)
) COMMENT '内容访问授权表：记录用户对内容的购买/订阅/会员解锁状态';

-- 内容访问授权索引
CREATE INDEX idx_access_user_content ON content_access_rights(user_id, content_id);
CREATE INDEX idx_access_content ON content_access_rights(content_id, status);
CREATE INDEX idx_access_user_type_status ON content_access_rights(user_id, access_type, status);
CREATE INDEX idx_access_granted_time ON content_access_rights(granted_time DESC);

-- =============================================
-- 16. 复合索引优化
-- =============================================

-- 内容查询优化索引
CREATE INDEX idx_contents_community_status_time ON contents(community_id, status, publish_at DESC);
CREATE INDEX idx_contents_author_type_time ON contents(author_id, content_type, create_time DESC);
CREATE INDEX idx_contents_hot_view_time ON contents(is_hot, view_count DESC, publish_at DESC);
CREATE INDEX idx_contents_stats_time ON contents(status, create_time DESC, view_count, like_count);

-- 全文搜索索引（PostgreSQL）
-- CREATE INDEX idx_contents_fulltext ON contents USING gin(to_tsvector('chinese', title || ' ' || COALESCE(content, '')));

-- =============================================
-- 17. 外键约束（可选）
-- =============================================
-- 注释：根据实际需要添加外键约束
-- ALTER TABLE contents ADD CONSTRAINT fk_contents_author FOREIGN KEY (author_id) REFERENCES sys_user(id);
-- ALTER TABLE contents ADD CONSTRAINT fk_contents_community FOREIGN KEY (community_id) REFERENCES community(id);
-- ALTER TABLE content_attachments ADD CONSTRAINT fk_attachments_content FOREIGN KEY (content_id) REFERENCES contents(id);
-- ALTER TABLE content_media_relations ADD CONSTRAINT fk_media_relations_content FOREIGN KEY (content_id) REFERENCES contents(id);
-- ALTER TABLE content_media_relations ADD CONSTRAINT fk_media_relations_media FOREIGN KEY (media_id) REFERENCES media_files(id);
-- ALTER TABLE content_inline_entities ADD CONSTRAINT fk_inline_entities_content FOREIGN KEY (content_id) REFERENCES contents(id);
-- ALTER TABLE content_mentions ADD CONSTRAINT fk_mentions_content FOREIGN KEY (content_id) REFERENCES contents(id);
-- ALTER TABLE content_mentions ADD CONSTRAINT fk_mentions_user FOREIGN KEY (mentioned_user_id) REFERENCES sys_user(id);
-- ALTER TABLE polls ADD CONSTRAINT fk_polls_content FOREIGN KEY (content_id) REFERENCES contents(id);
-- ALTER TABLE poll_options ADD CONSTRAINT fk_poll_options_poll FOREIGN KEY (poll_id) REFERENCES polls(id);
-- ALTER TABLE user_poll_votes ADD CONSTRAINT fk_user_votes_poll FOREIGN KEY (poll_id) REFERENCES polls(id);
-- ALTER TABLE user_poll_votes ADD CONSTRAINT fk_user_votes_option FOREIGN KEY (option_id) REFERENCES poll_options(id);
-- ALTER TABLE user_poll_votes ADD CONSTRAINT fk_user_votes_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
-- ALTER TABLE content_drafts ADD CONSTRAINT fk_drafts_author FOREIGN KEY (author_id) REFERENCES sys_user(id);
-- ALTER TABLE content_versions ADD CONSTRAINT fk_versions_content FOREIGN KEY (content_id) REFERENCES contents(id);
-- ALTER TABLE community ADD CONSTRAINT fk_community_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id);
-- ALTER TABLE user_profile_extension ADD CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES sys_user(id);

-- -- =============================================
-- -- 20. 初始化字典数据
-- -- =============================================
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time) VALUES
-- ('content_type_dict', '内容类型', 'content_type', '内容社区系统-内容类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('content_status_dict', '内容状态', 'content_status', '内容社区系统-内容状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('file_type_dict', '文件类型', 'file_type', '内容社区系统-文件类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('storage_type_dict', '存储类型', 'storage_type', '内容社区系统-存储类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('inline_entity_type_dict', '内联实体类型', 'inline_entity_type', '内容社区系统-内联实体类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_type_dict', '社区类型', 'community_type', '内容社区系统-社区类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_status_dict', '社区状态', 'community_status', '内容社区系统-社区状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('join_type_dict', '加入方式', 'join_type', '内容社区系统-加入方式字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('post_permission_dict', '发帖权限', 'post_permission', '内容社区系统-发帖权限字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('verify_status_dict', '认证状态', 'verify_status', '内容社区系统-认证状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('poll_type_dict', '投票类型', 'poll_type', '内容社区系统-投票类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('visibility_dict', '可见性', 'visibility', '内容社区系统-可见性字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('draft_status_dict', '草稿状态', 'draft_status', '内容社区系统-草稿状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('media_publish_status_dict', '媒体发布状态', 'media_publish_status', '内容社区系统-媒体发布状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('draft_operation_type_dict', '草稿操作类型', 'draft_operation_type', '内容社区系统-草稿操作类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

-- -- =============================================
-- -- 21. 初始化字典项数据
-- -- =============================================
-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES 
-- -- 内容类型
-- ('content_article_item', 'content_type_dict', '文章', '1', '长篇内容', 1, 1, 'admin', NOW()),
-- ('content_dynamic_item', 'content_type_dict', '动态', '2', '短篇动态', 2, 1, 'admin', NOW()),
-- ('content_qa_item', 'content_type_dict', '问答', '3', '问答内容', 3, 1, 'admin', NOW()),
-- ('content_qa_question_item', 'content_type_dict', '问答问题', '3', '问答问题', 3, 1, 'admin', NOW()),
-- ('content_qa_answer_item', 'content_type_dict', '问答答案', '4', '问答答案', 4, 1, 'admin', NOW()),
-- ('content_video_item', 'content_type_dict', '视频', '5', '视频内容', 5, 1, 'admin', NOW()),
-- ('content_image_item', 'content_type_dict', '图片', '6', '图片内容', 6, 1, 'admin', NOW()),
-- -- 内容状态
-- ('content_draft_item', 'content_status_dict', '草稿', '0', '草稿状态', 1, 1, 'admin', NOW()),
-- ('content_published_item', 'content_status_dict', '已发布', '1', '已发布状态', 2, 1, 'admin', NOW()),
-- ('content_deleted_item', 'content_status_dict', '已删除', '2', '已删除状态', 3, 1, 'admin', NOW()),
-- ('content_reviewing_item', 'content_status_dict', '审核中', '3', '审核中状态', 4, 1, 'admin', NOW()),
-- ('content_rejected_item', 'content_status_dict', '审核不通过', '4', '审核不通过状态', 5, 1, 'admin', NOW()),
-- -- 可见性
-- ('visibility_public_item', 'visibility_dict', '公开', '1', '所有人可见', 1, 1, 'admin', NOW()),
-- ('visibility_followers_item', 'visibility_dict', '仅关注者', '2', '仅关注者可见', 2, 1, 'admin', NOW()),
-- ('visibility_private_item', 'visibility_dict', '私密', '3', '仅自己可见', 3, 1, 'admin', NOW()),
-- -- 投票类型
-- ('poll_single_item', 'poll_type_dict', '单选', '1', '单选投票', 1, 1, 'admin', NOW()),
-- ('poll_multiple_item', 'poll_type_dict', '多选', '2', '多选投票', 2, 1, 'admin', NOW()),
-- -- 草稿状态
-- ('draft_editing_item', 'draft_status_dict', '编辑中', '1', '草稿编辑中', 1, 1, 'admin', NOW()),
-- ('draft_pending_item', 'draft_status_dict', '待发布', '2', '草稿待发布', 2, 1, 'admin', NOW()),
-- ('draft_scheduled_item', 'draft_status_dict', '定时发布', '3', '草稿定时发布', 3, 1, 'admin', NOW()),
-- ('draft_published_item', 'draft_status_dict', '已发布', '4', '草稿已发布', 4, 1, 'admin', NOW()),
-- ('draft_failed_item', 'draft_status_dict', '发布失败', '5', '草稿发布失败', 5, 1, 'admin', NOW()),
-- -- 媒体发布状态
-- ('media_unpublished_item', 'media_publish_status_dict', '未发布', '1', '媒体文件未发布', 1, 1, 'admin', NOW()),
-- ('media_published_item', 'media_publish_status_dict', '已发布', '2', '媒体文件已发布', 2, 1, 'admin', NOW()),
-- ('media_abandoned_item', 'media_publish_status_dict', '已废弃', '3', '媒体文件已废弃', 3, 1, 'admin', NOW());

COMMIT;

-- =============================================
-- 表结构说明
-- =============================================
-- 本模块包含以下17个核心表：
-- 1. contents - 核心内容表：存储文章、帖子、问答、视频等各类内容
-- 2. media_files - 媒体文件表：管理图片、视频、音频等媒体资源
-- 3. content_media_relations - 内容媒体关联表：建立内容与媒体文件的关联关系
-- 4. content_inline_entities - 内联实体表：存储@用户、#话题、$股票等内联实体
-- 5. content_tags - 内容标签表：管理内容标签系统
-- 6. content_topics - 内容话题表：管理话题系统
-- 7. content_tag_relations - 内容标签关联表：建立内容与标签的多对多关系
-- 8. content_topic_relations - 内容话题关联表：建立内容与话题的多对多关系
-- 9. content_stock_relations - 内容股票关联表：关联内容与股票信息
-- 10. content_mentions - 内容提及表：管理用户提及功能
-- 11. polls - 投票表：支持单选、多选投票功能
-- 12. poll_options - 投票选项表：存储投票选项信息
-- 13. user_poll_votes - 用户投票记录表：记录用户投票行为
-- 14. content_ads - 内容广告表：管理内容中的广告信息
-- 15. content_drafts - 内容草稿表：支持草稿保存功能
-- 16. content_versions - 内容版本表：记录内容变更历史
-- 17. user_topic_follows - 用户关注话题表：管理用户对话题的关注关系
-- 18. user_tag_follows - 用户关注标签表：管理用户对标签的关注关系
--
-- =============================================
-- 设计特点
-- =============================================
-- 1. 统一主键类型：所有表使用VARCHAR(32)作为主键，保持一致性
-- 2. 完善的索引设计：为查询频繁的字段建立索引，包括复合索引优化
-- 3. 软删除机制：使用del_flag字段实现逻辑删除，保护数据完整性
-- 4. 审计字段：包含create_by、create_time、update_by、update_time等审计信息
-- 5. JSON支持：使用JSON字段存储富文本内容、标签、附件等复杂数据结构
-- 6. 多媒体支持：完整的媒体文件管理体系，支持图片、视频、音频等
-- 7. 内容类型多样化：支持文章、帖子、问答、视频等多种内容类型
-- 8. 统计数据实时性：内置浏览数、点赞数、评论数等统计字段
-- 9. 状态管理：多种状态控制（草稿、已发布、审核中等）
-- 10. 可见性控制：支持公开、仅关注者、私密等可见性设置
-- 11. 转发机制：支持内容转发和原创内容追溯
-- 12. 问答系统：支持问题-答案关联和答案采纳机制
-- 13. 地理位置：支持内容地理位置标记
-- 14. 投票功能：完整的投票系统，支持单选、多选
-- 15. 内联实体：支持@用户、#话题、$股票等富文本实体
-- 16. 标签系统：灵活的标签管理和使用统计
-- 17. 话题系统：支持热门话题和参与统计
-- 18. 关注系统：支持用户关注话题和标签，提供个性化推荐
-- 19. 草稿功能：支持自动保存和手动保存草稿
-- 20. 版本控制：记录内容变更历史和版本管理
-- 21. 广告集成：支持多种类型的广告内容
-- 22. 媒体关联：灵活的内容与媒体文件关联机制
-- 23. 性能优化：针对查询场景优化的复合索引设计
-- 24. 扩展性：ext_data字段支持业务扩展
-- 25. 数据完整性：合理的约束设计保证数据一致性
-- 26. 个性化推荐：基于用户关注的话题和标签提供内容推荐
-- =============================================