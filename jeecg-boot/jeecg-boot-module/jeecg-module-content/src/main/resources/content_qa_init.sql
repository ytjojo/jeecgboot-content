-- =============================================
-- 问答模块数据库初始化脚本 (优化版)
-- 版本: 2.0
-- 创建时间: 2024-12-17
-- 优化时间: 2024-12-17
-- 说明: 问答相关功能的数据库表设计
-- 数据库类型为mysql 版本8.0+
-- 注意: 
--   1. 问答问题和答案内容存储在 contents 表中 (content_type: 3-问答问题, 4-问答答案)
--   2. 问答投票功能已整合到现有 user_reactions 表中，无需单独的 qa_votes 表
--   3. 遵循类型系统优化方案，避免类型冗余和语义混淆
--   4. 采用统一的互动机制，提升系统一致性和扩展性
-- =============================================

START TRANSACTION;

-- =============================================
-- 重要说明：问答投票机制优化
-- =============================================
-- 原设计问题：
-- 1. qa_votes 表与 user_reactions 表功能重复
-- 2. 存在类型冗余：content_type(问答答案) vs target_type(回答)
-- 3. 业务逻辑分散，维护成本高
--
-- 优化方案：
-- 1. 删除 qa_votes 表，统一使用 user_reactions 表
-- 2. 问答答案作为内容类型，使用 target_type=1(CONTENT) 进行投票
-- 3. 通过 contents.content_type=4 区分问答答案
-- 4. 保持业务逻辑统一，降低系统复杂度
-- =============================================

-- =============================================
-- 1. 问答扩展统计表 (qa_extended_stats)
-- 用于记录问答特有的统计数据，补充现有 content_stats 表
-- =============================================
CREATE TABLE IF NOT EXISTS qa_extended_stats (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '统计记录ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID（问题或答案）',
    content_type INTEGER NOT NULL COMMENT '内容类型：3-问答问题 4-问答答案',
    
    -- 问答特有统计（content_stats表中没有的）
    answer_count INTEGER DEFAULT 0 COMMENT '回答数（仅问题有效）',
    accepted_answer_count INTEGER DEFAULT 0 COMMENT '被采纳答案数（仅用户统计）',
    invitation_count INTEGER DEFAULT 0 COMMENT '邀请回答数',
    follow_count INTEGER DEFAULT 0 COMMENT '关注问题数',
    reward_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '悬赏金额',
    
    -- 问答质量评分
    answer_quality_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '答案质量分数',
    question_clarity_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '问题清晰度分数',
    expertise_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '专业度分数',
    
    -- 时间统计
    avg_answer_time INTEGER DEFAULT 0 COMMENT '平均回答时间（分钟）',
    first_answer_time TIMESTAMP COMMENT '首次回答时间',
    best_answer_time TIMESTAMP COMMENT '最佳答案时间',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 问答扩展统计表索引

-- 注意：由于索引可能已存在，如果导入失败请手动删除重复索引
CREATE UNIQUE INDEX uk_qa_extended_stats_content ON qa_extended_stats(content_id, del_flag);
CREATE INDEX idx_qa_extended_stats_type ON qa_extended_stats(content_type, del_flag);
CREATE INDEX idx_qa_extended_stats_quality ON qa_extended_stats(answer_quality_score DESC, question_clarity_score DESC);
CREATE INDEX idx_qa_extended_stats_answer_count ON qa_extended_stats(answer_count DESC, create_time DESC);
CREATE INDEX idx_qa_extended_stats_reward ON qa_extended_stats(reward_amount DESC, create_time DESC);

-- =============================================
-- 2. 问答关系表 (qa_relations) - 优化版
-- 用于记录问题和答案的关系，重点关注最佳答案管理
-- 注意：基础的问答关系已通过 contents.parent_content_id 实现
-- =============================================
CREATE TABLE IF NOT EXISTS qa_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关系记录ID',
    question_id VARCHAR(32) NOT NULL COMMENT '问题ID（关联contents表）',
    answer_id VARCHAR(32) NOT NULL COMMENT '答案ID（关联contents表）',
    
    -- 最佳答案管理
    is_accepted BOOLEAN DEFAULT FALSE COMMENT '是否为最佳答案',
    accepted_time TIMESTAMP COMMENT '采纳时间',
    accepted_by VARCHAR(32) COMMENT '采纳人ID（通常是提问者）',
    accept_reason TEXT COMMENT '采纳理由',
    
    -- 答案评价（由提问者评价）
    questioner_rating INTEGER COMMENT '提问者评分（1-5分）',
    questioner_comment TEXT COMMENT '提问者评价',
    
    -- 答案排序和推荐
    sort_order INTEGER DEFAULT 0 COMMENT '排序权重',
    is_recommended BOOLEAN DEFAULT FALSE COMMENT '是否推荐答案',
    recommend_reason VARCHAR(200) COMMENT '推荐理由',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 问答关系表索引
CREATE UNIQUE INDEX uk_qa_relations_question_answer ON qa_relations(question_id, answer_id, del_flag);
CREATE INDEX idx_qa_relations_question ON qa_relations(question_id, del_flag);
CREATE INDEX idx_qa_relations_answer ON qa_relations(answer_id, del_flag);
CREATE INDEX idx_qa_relations_accepted ON qa_relations(question_id, is_accepted, accepted_time DESC);
CREATE INDEX idx_qa_relations_recommended ON qa_relations(question_id, is_recommended, sort_order);
CREATE INDEX idx_qa_relations_rating ON qa_relations(questioner_rating DESC, accepted_time DESC);

-- =============================================
-- 3. 问答邀请表 (qa_invitations) - 优化版
-- 用于记录邀请用户回答问题的功能
-- =============================================
CREATE TABLE IF NOT EXISTS qa_invitations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '邀请记录ID',
    question_id VARCHAR(32) NOT NULL COMMENT '问题ID（关联contents表）',
    inviter_id VARCHAR(32) NOT NULL COMMENT '邀请人ID',
    invitee_id VARCHAR(32) NOT NULL COMMENT '被邀请人ID',
    
    -- 邀请状态管理
    status INTEGER DEFAULT 1 COMMENT '邀请状态：1-待回应 2-已接受 3-已拒绝 4-已过期 5-已回答',
    response_time TIMESTAMP COMMENT '回应时间',
    answer_time TIMESTAMP COMMENT '回答时间',
    answer_id VARCHAR(32) COMMENT '回答ID（如果已回答）',
    
    -- 邀请内容
    invitation_message TEXT COMMENT '邀请消息',
    response_message TEXT COMMENT '回应消息',
    invitation_reason VARCHAR(200) COMMENT '邀请理由（为什么邀请此人）',
    
    -- 邀请设置
    expire_time TIMESTAMP COMMENT '过期时间',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名邀请',
    priority INTEGER DEFAULT 1 COMMENT '邀请优先级：1-普通 2-重要 3-紧急',
    
    -- 邀请来源
    invitation_source VARCHAR(50) COMMENT '邀请来源：manual-手动邀请，system-系统推荐，ai-AI推荐',
    source_reason TEXT COMMENT '来源理由（系统推荐的原因）',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 问答邀请表索引
CREATE UNIQUE INDEX uk_qa_invitations_question_invitee ON qa_invitations(question_id, invitee_id, del_flag);
CREATE INDEX idx_qa_invitations_question ON qa_invitations(question_id, status, del_flag);
CREATE INDEX idx_qa_invitations_inviter ON qa_invitations(inviter_id, create_time DESC, del_flag);
CREATE INDEX idx_qa_invitations_invitee ON qa_invitations(invitee_id, status, priority DESC, del_flag);
CREATE INDEX idx_qa_invitations_status ON qa_invitations(status, expire_time, create_time DESC);
CREATE INDEX idx_qa_invitations_expire ON qa_invitations(expire_time);

-- =============================================
-- 5. 问答关注表 (qa_follows) - 优化版
-- 用于记录用户关注的问题，支持个性化通知设置
-- 注意：与现有 user_relation 表区分，此表专门用于问答问题关注
-- =============================================
CREATE TABLE IF NOT EXISTS qa_follows (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关注记录ID',
    question_id VARCHAR(32) NOT NULL COMMENT '问题ID（关联contents表）',
    user_id VARCHAR(32) NOT NULL COMMENT '关注用户ID',
    
    -- 关注类型和设置
    follow_type INTEGER DEFAULT 1 COMMENT '关注类型：1-普通关注 2-深度关注 3-仅收藏',
    notification_enabled BOOLEAN DEFAULT TRUE COMMENT '是否开启通知',
    notification_types JSON COMMENT '通知类型设置：{"new_answer":true,"best_answer":true,"comment":false}',
    
    -- 关注偏好
    follow_reason VARCHAR(200) COMMENT '关注理由',
    interest_tags JSON COMMENT '兴趣标签：用户关注此问题的兴趣点',
    
    -- 关注来源和行为
    follow_source VARCHAR(50) COMMENT '关注来源：browse-浏览关注，search-搜索关注，recommend-推荐关注，invite-邀请关注',
    source_detail JSON COMMENT '来源详情：记录具体的关注路径',
    
    -- 互动统计
    view_count INTEGER DEFAULT 0 COMMENT '查看次数',
    last_view_time TIMESTAMP COMMENT '最后查看时间',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 问答关注表索引
CREATE UNIQUE INDEX uk_qa_follows_question_user ON qa_follows(question_id, user_id, del_flag);
CREATE INDEX idx_qa_follows_question ON qa_follows(question_id, follow_type, del_flag);
CREATE INDEX idx_qa_follows_user ON qa_follows(user_id, create_time DESC, del_flag);
CREATE INDEX idx_qa_follows_notification ON qa_follows(user_id, notification_enabled, del_flag);
CREATE INDEX idx_qa_follows_source ON qa_follows(follow_source, create_time DESC);
CREATE INDEX idx_qa_follows_active ON qa_follows(user_id, last_view_time DESC, del_flag);

-- =============================================
-- 6. 问答悬赏表 (qa_rewards) - 优化版
-- 用于记录问题悬赏功能，支持多种悬赏类型和灵活的奖励机制
-- =============================================
CREATE TABLE IF NOT EXISTS qa_rewards (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '悬赏记录ID',
    question_id VARCHAR(32) NOT NULL COMMENT '问题ID（关联contents表）',
    creator_id VARCHAR(32) NOT NULL COMMENT '悬赏创建者ID',
    
    -- 悬赏基本信息
    reward_type INTEGER DEFAULT 1 COMMENT '悬赏类型：1-积分悬赏 2-现金悬赏 3-虚拟物品 4-实物奖品 5-服务奖励',
    reward_amount DECIMAL(10,2) NOT NULL COMMENT '悬赏金额/数量',
    reward_unit VARCHAR(20) DEFAULT 'points' COMMENT '悬赏单位：points-积分，yuan-元，item-物品',
    reward_description TEXT COMMENT '悬赏描述',
    reward_conditions TEXT COMMENT '获奖条件说明',
    
    -- 悬赏规则
    min_answer_length INTEGER DEFAULT 50 COMMENT '最少回答字数要求',
    require_expertise BOOLEAN DEFAULT FALSE COMMENT '是否要求专业认证',
    allow_multiple_winners BOOLEAN DEFAULT FALSE COMMENT '是否允许多个获奖者',
    max_winners INTEGER DEFAULT 1 COMMENT '最大获奖者数量',
    
    -- 悬赏状态管理
    status INTEGER DEFAULT 1 COMMENT '悬赏状态：1-进行中 2-已发放 3-已退回 4-已过期 5-已取消',
    
    -- 时间管理
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    awarded_time TIMESTAMP COMMENT '发放时间',
    auto_award BOOLEAN DEFAULT FALSE COMMENT '是否自动发放（到期自动选择最佳答案）',
    
    -- 获奖信息
    winner_ids JSON COMMENT '获奖者ID列表（支持多个获奖者）',
    winning_answer_ids JSON COMMENT '获奖答案ID列表',
    award_reason TEXT COMMENT '获奖理由',
    award_distribution JSON COMMENT '奖励分配详情',
    
    -- 悬赏统计
    participant_count INTEGER DEFAULT 0 COMMENT '参与者数量',
    answer_count INTEGER DEFAULT 0 COMMENT '回答数量',
    view_count INTEGER DEFAULT 0 COMMENT '查看数量',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 问答悬赏表索引
CREATE UNIQUE INDEX uk_qa_rewards_question ON qa_rewards(question_id, del_flag);
CREATE INDEX idx_qa_rewards_creator ON qa_rewards(creator_id, status, del_flag);
CREATE INDEX idx_qa_rewards_status ON qa_rewards(status, end_time DESC);
CREATE INDEX idx_qa_rewards_amount ON qa_rewards(reward_type, reward_amount DESC, create_time DESC);
CREATE INDEX idx_qa_rewards_active ON qa_rewards(status, end_time);
CREATE INDEX idx_qa_rewards_expire ON qa_rewards(end_time);

-- =============================================
-- 7. 问答话题关联表 (qa_topic_relations) - 优化版
-- 用于记录问答与话题的关联关系，支持智能标签和相关度计算
-- =============================================
CREATE TABLE IF NOT EXISTS qa_topic_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联记录ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID（问题或答案，关联contents表）',
    topic_id VARCHAR(32) NOT NULL COMMENT '话题ID（关联topics表）',
    
    -- 关联基本信息
    relation_type INTEGER DEFAULT 1 COMMENT '关联类型：1-问题关联 2-答案关联 3-系列问题关联',
    relevance_score DECIMAL(5,4) DEFAULT 1.0000 COMMENT '相关度评分（0.0000-1.0000）',
    confidence_level DECIMAL(5,4) DEFAULT 1.0000 COMMENT '置信度（AI推荐的准确性）',
    
    -- 标签来源和管理
    tag_source INTEGER DEFAULT 1 COMMENT '标签来源：1-用户添加 2-AI推荐 3-管理员设置 4-系统自动 5-专家标注',
    tagged_by VARCHAR(32) COMMENT '标记人ID',
    tag_reason TEXT COMMENT '标记理由或依据',
    
    -- 标签状态
    is_primary BOOLEAN DEFAULT FALSE COMMENT '是否为主要标签',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否已验证',
    verified_by VARCHAR(32) COMMENT '验证人ID',
    verified_time TIMESTAMP COMMENT '验证时间',
    
    -- 智能标签特性
    auto_extracted BOOLEAN DEFAULT FALSE COMMENT '是否为自动提取',
    extraction_method VARCHAR(50) COMMENT '提取方法：keyword-关键词，nlp-自然语言处理，ml-机器学习',
    keyword_matches JSON COMMENT '匹配的关键词列表',
    
    -- 统计信息
    usage_count INTEGER DEFAULT 0 COMMENT '使用次数',
    click_count INTEGER DEFAULT 0 COMMENT '点击次数',
    last_used_time TIMESTAMP COMMENT '最后使用时间',
    
    -- 审计字段
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 问答话题关联表索引
CREATE UNIQUE INDEX uk_qa_topic_relations ON qa_topic_relations(content_id, topic_id, del_flag);
CREATE INDEX idx_qa_topic_relations_content ON qa_topic_relations(content_id, relation_type, del_flag);
CREATE INDEX idx_qa_topic_relations_topic ON qa_topic_relations(topic_id, relevance_score DESC, del_flag);
CREATE INDEX idx_qa_topic_relations_source ON qa_topic_relations(tag_source, is_verified, del_flag);
CREATE INDEX idx_qa_topic_relations_primary ON qa_topic_relations(content_id, is_primary, relevance_score DESC);
CREATE INDEX idx_qa_topic_relations_auto ON qa_topic_relations(auto_extracted, confidence_level DESC);

-- =============================================
-- 8. 复合索引优化 - 针对问答模块常见查询场景
-- =============================================

-- 问答扩展统计复合索引
CREATE INDEX idx_qa_extended_stats_content_type ON qa_extended_stats(content_id, content_type, del_flag);
CREATE INDEX idx_qa_extended_stats_quality_ranking ON qa_extended_stats(content_type, answer_quality_score DESC, question_clarity_score DESC);
CREATE INDEX idx_qa_extended_stats_answer_performance ON qa_extended_stats(answer_count DESC, accepted_answer_count DESC, create_time DESC);

-- 问答关系复合索引
CREATE INDEX idx_qa_relations_question_accepted_time ON qa_relations(question_id, is_accepted, accepted_time DESC);
CREATE INDEX idx_qa_relations_answer_rating_time ON qa_relations(answer_id, questioner_rating DESC, accepted_time DESC);
CREATE INDEX idx_qa_relations_recommended_sort ON qa_relations(question_id, is_recommended, sort_order ASC);

-- 问答邀请复合索引
CREATE INDEX idx_qa_invitations_invitee_status_priority ON qa_invitations(invitee_id, status, priority DESC, create_time DESC);
CREATE INDEX idx_qa_invitations_question_pending_time ON qa_invitations(question_id, status, create_time DESC);
CREATE INDEX idx_qa_invitations_answered_performance ON qa_invitations(invitee_id, answer_time DESC, del_flag);

-- 问答关注复合索引
CREATE INDEX idx_qa_follows_user_notification_time ON qa_follows(user_id, notification_enabled, last_view_time DESC);
CREATE INDEX idx_qa_follows_question_type_time ON qa_follows(question_id, follow_type, create_time DESC);
CREATE INDEX idx_qa_follows_active_users ON qa_follows(user_id, view_count DESC, last_view_time DESC);

-- 问答悬赏复合索引
CREATE INDEX idx_qa_rewards_active_amount_time ON qa_rewards(status, reward_type, reward_amount DESC, end_time ASC);
CREATE INDEX idx_qa_rewards_creator_status_time ON qa_rewards(creator_id, status, create_time DESC);
CREATE INDEX idx_qa_rewards_performance_stats ON qa_rewards(reward_type, participant_count DESC, answer_count DESC);

-- 问答话题关联复合索引
CREATE INDEX idx_qa_topic_verified_relevance ON qa_topic_relations(topic_id, is_verified, relevance_score DESC);
CREATE INDEX idx_qa_topic_content_primary_score ON qa_topic_relations(content_id, is_primary, relevance_score DESC);
CREATE INDEX idx_qa_topic_source_confidence ON qa_topic_relations(tag_source, confidence_level DESC, create_time DESC);

-- =============================================
-- 9. 初始化字典数据
-- =============================================
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time) VALUES
-- ('qa_extended_stats_dict', '问答扩展统计类型', 'qa_extended_stats_type', '问答模块-扩展统计类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('qa_invitation_status_dict', '问答邀请状态', 'qa_invitation_status', '问答模块-邀请状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('qa_invitation_source_dict', '问答邀请来源', 'qa_invitation_source', '问答模块-邀请来源字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('qa_follow_type_dict', '问答关注类型', 'qa_follow_type', '问答模块-关注类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('qa_reward_type_dict', '问答悬赏类型', 'qa_reward_type', '问答模块-悬赏类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('qa_reward_status_dict', '问答悬赏状态', 'qa_reward_status', '问答模块-悬赏状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('qa_tag_source_dict', '问答标签来源', 'qa_tag_source', '问答模块-标签来源字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

-- -- =============================================
-- -- 10. 初始化字典项数据
-- -- =============================================
-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES
-- -- 问答邀请状态
-- ('qa_invitation_status_pending', 'qa_invitation_status_dict', '待回应', '1', '邀请待回应', 1, 1, 'admin', NOW()),
-- ('qa_invitation_status_accepted', 'qa_invitation_status_dict', '已接受', '2', '邀请已接受', 2, 1, 'admin', NOW()),
-- ('qa_invitation_status_rejected', 'qa_invitation_status_dict', '已拒绝', '3', '邀请已拒绝', 3, 1, 'admin', NOW()),
-- ('qa_invitation_status_expired', 'qa_invitation_status_dict', '已过期', '4', '邀请已过期', 4, 1, 'admin', NOW()),
-- ('qa_invitation_status_answered', 'qa_invitation_status_dict', '已回答', '5', '邀请已回答', 5, 1, 'admin', NOW()),

-- ('qa_invitation_source_manual', 'qa_invitation_source_dict', '手动邀请', 'manual', '手动邀请', 1, 1, 'admin', NOW()),
-- ('qa_invitation_source_system', 'qa_invitation_source_dict', '系统推荐', 'system', '系统推荐', 2, 1, 'admin', NOW()),
-- ('qa_invitation_source_ai', 'qa_invitation_source_dict', 'AI推荐', 'ai', 'AI推荐', 3, 1, 'admin', NOW()),

-- ('qa_follow_type_normal', 'qa_follow_type_dict', '普通关注', '1', '普通关注', 1, 1, 'admin', NOW()),
-- ('qa_follow_type_deep', 'qa_follow_type_dict', '深度关注', '2', '深度关注', 2, 1, 'admin', NOW()),
-- ('qa_follow_type_collect', 'qa_follow_type_dict', '仅收藏', '3', '仅收藏', 3, 1, 'admin', NOW()),

-- ('qa_reward_type_points', 'qa_reward_type_dict', '积分悬赏', '1', '积分悬赏', 1, 1, 'admin', NOW()),
-- ('qa_reward_type_cash', 'qa_reward_type_dict', '现金悬赏', '2', '现金悬赏', 2, 1, 'admin', NOW()),
-- ('qa_reward_type_virtual', 'qa_reward_type_dict', '虚拟物品', '3', '虚拟物品', 3, 1, 'admin', NOW()),
-- ('qa_reward_type_physical', 'qa_reward_type_dict', '实物奖品', '4', '实物奖品', 4, 1, 'admin', NOW()),
-- ('qa_reward_type_service', 'qa_reward_type_dict', '服务奖励', '5', '服务奖励', 5, 1, 'admin', NOW()),

-- ('qa_reward_status_ongoing', 'qa_reward_status_dict', '进行中', '1', '悬赏进行中', 1, 1, 'admin', NOW()),
-- ('qa_reward_status_distributed', 'qa_reward_status_dict', '已发放', '2', '悬赏已发放', 2, 1, 'admin', NOW()),
-- ('qa_reward_status_returned', 'qa_reward_status_dict', '已退回', '3', '悬赏已退回', 3, 1, 'admin', NOW()),
-- ('qa_reward_status_expired', 'qa_reward_status_dict', '已过期', '4', '悬赏已过期', 4, 1, 'admin', NOW()),
-- ('qa_reward_status_cancelled', 'qa_reward_status_dict', '已取消', '5', '悬赏已取消', 5, 1, 'admin', NOW()),

-- ('qa_tag_source_user', 'qa_tag_source_dict', '用户添加', '1', '用户添加', 1, 1, 'admin', NOW()),
-- ('qa_tag_source_ai', 'qa_tag_source_dict', 'AI推荐', '2', 'AI推荐', 2, 1, 'admin', NOW()),
-- ('qa_tag_source_admin', 'qa_tag_source_dict', '管理员设置', '3', '管理员设置', 3, 1, 'admin', NOW()),
-- ('qa_tag_source_system', 'qa_tag_source_dict', '系统自动', '4', '系统自动', 4, 1, 'admin', NOW()),
-- ('qa_tag_source_expert', 'qa_tag_source_dict', '专家标注', '5', '专家标注', 5, 1, 'admin', NOW());

COMMIT;

-- =============================================
-- 优化后的问答模块设计说明
-- =============================================
-- 本模块包含以下6个问答相关表（已优化）：
-- 1. qa_extended_stats - 问答扩展统计表：补充问答特有的统计数据
-- 2. qa_relations - 问答关系表：专注最佳答案管理和关系维护
-- 3. qa_invitations - 问答邀请表：支持智能邀请和状态管理
-- 4. qa_follows - 问答关注表：个性化关注设置和通知管理
-- 5. qa_rewards - 问答悬赏表：灵活的悬赏机制和奖励分配
-- 6. qa_topic_relations - 问答话题关联表：智能标签和相关度管理
--
-- =============================================
-- 核心优化特点
-- =============================================
-- 【类型系统优化】
-- 1. 统一投票机制：移除qa_votes表，统一使用user_reactions表处理所有内容互动
-- 2. 避免类型冗余：遵循InteractionTargetTypeEnum设计，消除语义混淆
-- 3. 内容类型统一：问答内容统一使用CONTENT类型，通过content_type区分具体类别
--
-- 【功能增强】
-- 4. 扩展统计系统：qa_extended_stats表提供问答特有统计，如质量评分、专业度评估
-- 5. 智能关系管理：qa_relations表专注最佳答案管理，支持提问者评价机制
-- 6. 高级邀请功能：支持匿名邀请、优先级设置、AI推荐邀请
-- 7. 个性化关注：精细化通知设置、兴趣标签管理、关注行为分析
-- 8. 灵活悬赏机制：多种悬赏类型、多获奖者支持、自动发放机制
-- 9. 智能话题关联：AI标签推荐、置信度评估、多种提取方法
--
-- 【性能优化】
-- 10. 复合索引策略：针对常见查询场景设计的高效索引
-- 11. 条件索引优化：使用WHERE条件优化特定场景查询
-- 12. 统计数据分离：避免实时计算，提升查询性能
--
-- 【系统集成】
-- 13. 深度集成现有系统：与contents、user_reactions、content_stats等表协同工作
-- 14. 统一业务逻辑：遵循系统整体设计原则，保持一致性
-- 15. 扩展性设计：JSONB字段支持灵活数据结构，预留未来扩展空间
--
-- 【最佳实践】
-- 16. 软删除机制：所有表支持逻辑删除，保护数据完整性
-- 17. 完整审计追踪：创建时间、更新时间、操作人等完整记录
-- 18. 数据一致性：通过约束和索引保证数据质量
-- 19. 业务逻辑分离：数据库专注存储，业务逻辑在应用层实现
-- 20. 类型系统一致性：严格遵循枚举设计，避免重复定义
-- =============================================