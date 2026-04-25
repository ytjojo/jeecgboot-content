-- =============================================
-- 内容社区系统频道模块数据库初始化脚本
-- 数据库类型为mysql 版本8.0+
-- 基于频道管理需求文档进行设计
-- =============================================

START TRANSACTION;

-- =============================================
-- 1. 频道表 (channels) - 核心频道信息
-- =============================================
CREATE TABLE IF NOT EXISTS channels (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '频道ID',
    channel_name VARCHAR(100) NOT NULL COMMENT '频道名称',
    description TEXT COMMENT '频道描述',
    channel_avatar VARCHAR(500) COMMENT '频道头像',
    channel_cover_image VARCHAR(500) COMMENT '频道封面图',
    
    -- 频道属性
    category VARCHAR(50) COMMENT '频道分类',
    category_id VARCHAR(32) COMMENT '频道分类ID，关联channel_categories表',
    parent_channel_id VARCHAR(32) COMMENT '父频道ID（支持子频道）',
    owner_id VARCHAR(32) NOT NULL COMMENT '频道创建者ID',
    
    -- 权限设置
    is_public INTEGER DEFAULT 1 COMMENT '是否公开频道：0-私有 1-公开',
    join_type INTEGER DEFAULT 1 COMMENT '加入方式：1-自由加入 2-申请加入 3-邀请加入',
    post_permission INTEGER DEFAULT 1 COMMENT '发帖权限：0-任何人 1-所有成员 2-版主以上 3-管理员 4-指定成员',
    allowed_content_types JSON COMMENT '允许的内容类型（JSON数组）',
    view_permission INTEGER DEFAULT 0 COMMENT '查看权限：0-任何人 1-所有成员 2-管理员 3-指定成员',

    -- 统计字段
    member_count BIGINT DEFAULT 0 COMMENT '成员数量',
    content_count BIGINT DEFAULT 0 COMMENT '内容数量',
    max_members INTEGER COMMENT '最大成员数限制',
    
    -- 状态字段
    status INTEGER DEFAULT 1 COMMENT '频道状态: 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝',
    sort_order INTEGER DEFAULT 0 COMMENT '排序权重',
    is_recommend INTEGER DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    
    -- 频道规则
    rules TEXT COMMENT '频道规则',
    announcement TEXT COMMENT '频道公告',
    
    -- 扩展字段
    tags JSON COMMENT '频道标签（JSON数组）',
    ext_data JSON COMMENT '扩展数据（JSON格式）',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道表索引
CREATE INDEX idx_channels_owner_id ON channels(owner_id);
CREATE INDEX idx_channels_category ON channels(category);
CREATE INDEX idx_channels_category_id ON channels(category_id);
CREATE INDEX idx_channels_is_public ON channels(is_public);
CREATE INDEX idx_channels_status ON channels(status);
CREATE INDEX idx_channels_parent_id ON channels(parent_channel_id);
CREATE INDEX idx_channels_member_count ON channels(member_count DESC);
CREATE INDEX idx_channels_content_count ON channels(content_count DESC);
CREATE INDEX idx_channels_recommend ON channels(is_recommend, sort_order);
CREATE INDEX idx_channels_del_flag ON channels(del_flag);
CREATE INDEX idx_channels_create_time ON channels(create_time DESC);

-- =============================================
-- 2. 频道成员表 (channel_members) - 频道成员关系
-- =============================================
CREATE TABLE IF NOT EXISTS channel_members (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '成员关系ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    
    -- 成员角色
    role INTEGER DEFAULT 0 COMMENT '成员角色：0-访客 1-普通成员 2-版主 3-管理员 4-创建者',
    status INTEGER DEFAULT 1 COMMENT '成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝 5-邀请中',
    
    join_type INTEGER DEFAULT 1 COMMENT '加入方式：1-自由加入 2-申请通过 3-邀请加入',
    -- 权限设置
    permissions JSON COMMENT '特殊权限设置（JSON格式）',

    -- 禁言设置
    mute_type INTEGER DEFAULT 0 COMMENT '禁言类型：0-未禁言 1-发言禁言 2-评论禁言 3-全局禁言',
    mute_reason TEXT COMMENT '禁言原因',
    mute_operator_id VARCHAR(32) COMMENT '禁言操作人ID',
    mute_start_time TIMESTAMP COMMENT '禁言开始时间',
    mute_end_time TIMESTAMP COMMENT '禁言结束时间',
    is_permanent_mute INTEGER DEFAULT 0 COMMENT '是否永久禁言：0-否 1-是',
    
    -- 邀请关系
    inviter_id VARCHAR(32) COMMENT '邀请人ID',
    invited_at TIMESTAMP COMMENT '邀请时间',
    -- 申请处理
    approved_at TIMESTAMP COMMENT '审核时间',
    approved_by VARCHAR(32) COMMENT '审核人',



    is_subscribed INTEGER DEFAULT 0 COMMENT '是否订阅：0-否 1-是',
  
    
    -- 排序权重
    sort_order INTEGER DEFAULT 0 COMMENT '排序权重',
    
    -- 时间字段
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_active_time TIMESTAMP COMMENT '最后活跃时间',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道成员表索引
CREATE UNIQUE INDEX uk_channel_members ON channel_members(channel_id, user_id);
CREATE INDEX idx_channel_members_channel_id ON channel_members(channel_id);
CREATE INDEX idx_channel_members_user_id ON channel_members(user_id);
CREATE INDEX idx_channel_members_role ON channel_members(role);
CREATE INDEX idx_channel_members_status ON channel_members(status);
CREATE INDEX idx_channel_members_joined_at ON channel_members(joined_at DESC);

-- =============================================
-- 3. 频道权限表 (channel_permissions) - 角色权限配置
-- =============================================
CREATE TABLE IF NOT EXISTS channel_permissions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '权限ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    role INTEGER NOT NULL COMMENT '角色：1-创建者 2-管理员 3-版主 4-普通成员',
    permission VARCHAR(50) NOT NULL COMMENT '权限类型：CREATE_CONTENT/DELETE_CONTENT/PIN_CONTENT/MANAGE_MEMBERS/INVITE_MEMBERS/MODERATE_COMMENTS/CHANGE_SETTINGS',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道权限表索引
CREATE UNIQUE INDEX uk_channel_role_permission ON channel_permissions(channel_id, role, permission);
CREATE INDEX idx_channel_permissions_channel_id ON channel_permissions(channel_id);
CREATE INDEX idx_channel_permissions_role ON channel_permissions(role);

-- =============================================
-- 4. 频道内容关联表 (channel_content_relations) - 内容与频道关联
-- =============================================
CREATE TABLE IF NOT EXISTS channel_content_relations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '关联ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    
    -- 内容状态
    is_pinned INTEGER DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    pinned_order INTEGER DEFAULT 0 COMMENT '置顶排序',
    is_featured INTEGER DEFAULT 0 COMMENT '是否精华：0-否 1-是',
    is_hot INTEGER DEFAULT 0 COMMENT '是否热门：0-否 1-是',



    
    -- 操作信息
    pinned_by VARCHAR(32) COMMENT '置顶操作者ID',
    pinned_at TIMESTAMP COMMENT '置顶时间',
    featured_by VARCHAR(32) COMMENT '精华操作者ID',
    featured_at TIMESTAMP COMMENT '精华时间',
    
    -- 系统字段
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道内容关联表索引
CREATE UNIQUE INDEX uk_channel_content ON channel_content_relations(channel_id, content_id);
CREATE INDEX idx_channel_content_channel_id ON channel_content_relations(channel_id);
CREATE INDEX idx_channel_content_content_id ON channel_content_relations(content_id);
CREATE INDEX idx_channel_content_pinned ON channel_content_relations(is_pinned, pinned_order);
CREATE INDEX idx_channel_content_featured ON channel_content_relations(is_featured, featured_at DESC);

-- =============================================
-- 5. 频道邀请表 (channel_invitations) - 频道邀请管理
-- =============================================
CREATE TABLE IF NOT EXISTS channel_invitations (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '邀请ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    inviter_id VARCHAR(32) NOT NULL COMMENT '邀请者ID',
    invitee_id VARCHAR(32) NOT NULL COMMENT '被邀请者ID',
    
    -- 邀请信息
    message TEXT COMMENT '邀请消息',
    status INTEGER DEFAULT 1 COMMENT '邀请状态：1-待处理 2-已接受 3-已拒绝 4-已过期',
    expires_at TIMESTAMP COMMENT '过期时间',
    
    -- 处理信息
    processed_at TIMESTAMP COMMENT '处理时间',
    
    -- 系统字段
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道邀请表索引
CREATE UNIQUE INDEX uk_channel_invitee_pending ON channel_invitations(channel_id, invitee_id, status);
CREATE INDEX idx_channel_invitations_channel_id ON channel_invitations(channel_id);
CREATE INDEX idx_channel_invitations_inviter_id ON channel_invitations(inviter_id);
CREATE INDEX idx_channel_invitations_invitee_id ON channel_invitations(invitee_id);
CREATE INDEX idx_channel_invitations_status ON channel_invitations(status);
CREATE INDEX idx_channel_invitations_expires_at ON channel_invitations(expires_at);

-- =============================================
-- 6. 频道活动日志表 (channel_activity_logs) - 频道操作记录
-- =============================================
CREATE TABLE IF NOT EXISTS channel_activity_logs (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '日志ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '操作用户ID',
    
    -- 操作信息
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型：JOIN/LEAVE/ROLE_CHANGE/CONTENT_PIN/CONTENT_UNPIN/CONTENT_FEATURE/CONTENT_REMOVE/MEMBER_INVITE/MEMBER_REMOVE/SETTINGS_CHANGE',
    target_type VARCHAR(20) COMMENT '操作目标类型：CHANNEL/MEMBER/CONTENT',
    target_id VARCHAR(32) COMMENT '操作目标ID',
    
    -- 操作详情
    details JSON COMMENT '操作详情（JSON格式）',
    description TEXT COMMENT '操作描述',
    
    -- 系统字段
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 频道活动日志表索引
CREATE INDEX idx_channel_activity_channel_id ON channel_activity_logs(channel_id);
CREATE INDEX idx_channel_activity_user_id ON channel_activity_logs(user_id);
CREATE INDEX idx_channel_activity_action_type ON channel_activity_logs(action_type);
CREATE INDEX idx_channel_activity_create_time ON channel_activity_logs(create_time DESC);
CREATE INDEX idx_channel_activity_target ON channel_activity_logs(target_type, target_id);

-- =============================================
-- 7. 频道分类表 (channel_categories) - 频道分类管理
-- =============================================
CREATE TABLE IF NOT EXISTS channel_categories (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    icon VARCHAR(100) COMMENT '分类图标',
    color VARCHAR(7) COMMENT '分类颜色',
    
    -- 分类属性
    parent_id VARCHAR(32) COMMENT '父分类ID',
    level INTEGER DEFAULT 1 COMMENT '分类层级',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
    
    -- 统计字段
    channel_count BIGINT DEFAULT 0 COMMENT '频道数量',
    
    -- 状态字段
    status INTEGER DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道分类表索引
CREATE INDEX idx_channel_categories_parent_id ON channel_categories(parent_id);
CREATE INDEX idx_channel_categories_level ON channel_categories(level);
CREATE INDEX idx_channel_categories_sort ON channel_categories(sort_order);
CREATE INDEX idx_channel_categories_status ON channel_categories(status);
CREATE INDEX idx_channel_categories_del_flag ON channel_categories(del_flag);

-- =============================================
-- 8. 频道订阅表 (channel_subscriptions) - 用户订阅频道关系
-- =============================================
CREATE TABLE IF NOT EXISTS channel_subscriptions (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '订阅ID',
    channel_id VARCHAR(32) NOT NULL COMMENT '频道ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    
    -- 订阅设置
    notification_enabled INTEGER DEFAULT 1 COMMENT '是否开启通知：0-否 1-是',
    subscription_type INTEGER DEFAULT 1 COMMENT '订阅类型：1-普通订阅 2-特别关注',
    
    -- 时间字段
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    
    -- 系统字段
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 频道订阅表索引
CREATE UNIQUE INDEX uk_channel_subscriptions ON channel_subscriptions(channel_id, user_id);
CREATE INDEX idx_channel_subscriptions_channel_id ON channel_subscriptions(channel_id);
CREATE INDEX idx_channel_subscriptions_user_id ON channel_subscriptions(user_id);
CREATE INDEX idx_channel_subscriptions_type ON channel_subscriptions(subscription_type);
CREATE INDEX idx_channel_subscriptions_time ON channel_subscriptions(subscribed_at DESC);

-- =============================================
-- 9. 复合索引优化
-- =============================================

-- 频道查询优化索引
CREATE INDEX idx_channels_public_status_sort ON channels(is_public, status, sort_order);
CREATE INDEX idx_channels_category_status_member ON channels(category, status, member_count DESC);
CREATE INDEX idx_channels_recommend_status_time ON channels(is_recommend, status, create_time DESC);

-- 频道成员查询优化索引
CREATE INDEX idx_channel_members_channel_role_status ON channel_members(channel_id, role, status);
CREATE INDEX idx_channel_members_user_status_time ON channel_members(user_id, status, joined_at DESC);

-- 频道内容关联查询优化索引
CREATE INDEX idx_channel_content_channel_pinned_time ON channel_content_relations(channel_id, is_pinned DESC, create_time DESC);
CREATE INDEX idx_channel_content_channel_featured_time ON channel_content_relations(channel_id, is_featured DESC, featured_at DESC);

-- =============================================
-- 10. 初始化字典数据
-- =============================================

-- -- 频道状态字典
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type, tenant_id) VALUES
-- ('channel_status_dict', '频道状态', 'channel_status', '频道模块-频道状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_join_type_dict', '频道加入方式', 'channel_join_type', '频道模块-加入方式字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_post_permission_dict', '频道发帖权限', 'channel_post_permission', '频道模块-发帖权限字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_member_role_dict', '频道成员角色', 'channel_member_role', '频道模块-成员角色字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_member_status_dict', '频道成员状态', 'channel_member_status', '频道模块-成员状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_invitation_status_dict', '频道邀请状态', 'channel_invitation_status', '频道模块-邀请状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_activity_action_dict', '频道活动操作类型', 'channel_activity_action', '频道模块-活动操作类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0),
-- ('channel_subscription_type_dict', '频道订阅类型', 'channel_subscription_type', '频道模块-订阅类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0),
-- ('channel_category_status_dict', '频道分类状态', 'channel_category_status', '频道模块-分类状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 1, 0);

-- -- =============================================
-- -- 11. 字典项数据
-- -- =============================================

-- -- 频道状态字典项
-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES 
-- -- 频道状态
-- ('channel_status_disabled', 'channel_status_dict', '禁用', '0', '频道已禁用', 1, 1, 'admin', NOW()),
-- ('channel_status_normal', 'channel_status_dict', '正常', '1', '频道正常运行', 2, 1, 'admin', NOW()),
-- ('channel_status_reviewing', 'channel_status_dict', '审核中', '2', '频道审核中', 3, 1, 'admin', NOW()),
-- ('channel_status_closed', 'channel_status_dict', '已关闭', '3', '频道已关闭', 4, 1, 'admin', NOW()),

-- -- 频道加入方式字典项
-- ('channel_join_free', 'channel_join_type_dict', '自由加入', '1', '用户可自由加入频道', 1, 1, 'admin', NOW()),
-- ('channel_join_apply', 'channel_join_type_dict', '申请加入', '2', '需要申请审核后加入', 2, 1, 'admin', NOW()),
-- ('channel_join_invite', 'channel_join_type_dict', '邀请加入', '3', '仅通过邀请加入', 3, 1, 'admin', NOW()),

-- -- 频道发帖权限字典项
-- ('channel_post_all_members', 'channel_post_permission_dict', '所有成员', '1', '所有成员都可以发帖', 1, 1, 'admin', NOW()),
-- ('channel_post_admin_only', 'channel_post_permission_dict', '管理员', '2', '仅管理员可以发帖', 2, 1, 'admin', NOW()),
-- ('channel_post_specified', 'channel_post_permission_dict', '指定成员', '3', '仅指定成员可以发帖', 3, 1, 'admin', NOW()),

-- -- 频道成员角色字典项（与MemberRoleEnum枚举保持一致）
-- ('channel_member_guest', 'channel_member_role_dict', '访客', '0', '访客，只有查看权限', 0, 1, 'admin', NOW()),
-- ('channel_member_normal', 'channel_member_role_dict', '普通成员', '1', '普通成员，拥有基础权限', 1, 1, 'admin', NOW()),
-- ('channel_member_moderator', 'channel_member_role_dict', '版主', '2', '频道版主，拥有内容管理权限', 2, 1, 'admin', NOW()),
-- ('channel_member_admin', 'channel_member_role_dict', '管理员', '3', '频道管理员，拥有管理权限', 3, 1, 'admin', NOW()),
-- ('channel_member_owner', 'channel_member_role_dict', '创建者', '4', '频道创建者，拥有所有权限', 4, 1, 'admin', NOW()),

-- -- 频道成员状态字典项
-- ('channel_member_status_left', 'channel_member_status_dict', '已退出', '0', '成员已退出频道', 1, 1, 'admin', NOW()),
-- ('channel_member_status_normal', 'channel_member_status_dict', '正常', '1', '成员状态正常', 2, 1, 'admin', NOW()),
-- ('channel_member_status_muted', 'channel_member_status_dict', '被禁言', '2', '成员被禁言', 3, 1, 'admin', NOW()),
-- ('channel_member_status_pending', 'channel_member_status_dict', '待审核', '3', '成员待审核', 4, 1, 'admin', NOW()),

-- -- 频道邀请状态字典项
-- ('channel_invitation_pending', 'channel_invitation_status_dict', '待处理', '1', '邀请待处理', 1, 1, 'admin', NOW()),
-- ('channel_invitation_accepted', 'channel_invitation_status_dict', '已接受', '2', '邀请已接受', 2, 1, 'admin', NOW()),
-- ('channel_invitation_rejected', 'channel_invitation_status_dict', '已拒绝', '3', '邀请已拒绝', 3, 1, 'admin', NOW()),
-- ('channel_invitation_expired', 'channel_invitation_status_dict', '已过期', '4', '邀请已过期', 4, 1, 'admin', NOW()),

-- -- 频道活动操作类型字典项
-- ('channel_activity_join', 'channel_activity_action_dict', '加入频道', 'JOIN', '用户加入频道', 1, 1, 'admin', NOW()),
-- ('channel_activity_leave', 'channel_activity_action_dict', '离开频道', 'LEAVE', '用户离开频道', 2, 1, 'admin', NOW()),
-- ('channel_activity_role_change', 'channel_activity_action_dict', '角色变更', 'ROLE_CHANGE', '成员角色变更', 3, 1, 'admin', NOW()),
-- ('channel_activity_content_pin', 'channel_activity_action_dict', '内容置顶', 'CONTENT_PIN', '内容置顶操作', 4, 1, 'admin', NOW()),
-- ('channel_activity_content_unpin', 'channel_activity_action_dict', '取消置顶', 'CONTENT_UNPIN', '取消内容置顶', 5, 1, 'admin', NOW()),
-- ('channel_activity_content_feature', 'channel_activity_action_dict', '设为精华', 'CONTENT_FEATURE', '设置内容为精华', 6, 1, 'admin', NOW()),
-- ('channel_activity_content_remove', 'channel_activity_action_dict', '移除内容', 'CONTENT_REMOVE', '移除频道内容', 7, 1, 'admin', NOW()),
-- ('channel_activity_member_invite', 'channel_activity_action_dict', '邀请成员', 'MEMBER_INVITE', '邀请新成员', 8, 1, 'admin', NOW()),
-- ('channel_activity_member_remove', 'channel_activity_action_dict', '移除成员', 'MEMBER_REMOVE', '移除频道成员', 9, 1, 'admin', NOW()),
-- ('channel_activity_settings_change', 'channel_activity_action_dict', '设置变更', 'SETTINGS_CHANGE', '频道设置变更', 10, 1, 'admin', NOW()),

-- -- 频道订阅类型字典项
-- ('channel_subscription_normal', 'channel_subscription_type_dict', '普通订阅', '1', '普通订阅频道', 1, 1, 'admin', NOW()),
-- ('channel_subscription_special', 'channel_subscription_type_dict', '特别关注', '2', '特别关注频道', 2, 1, 'admin', NOW()),

-- -- 频道分类状态字典项
-- ('channel_category_status_disabled', 'channel_category_status_dict', '禁用', '0', '分类已禁用', 1, 1, 'admin', NOW()),
-- ('channel_category_status_enabled', 'channel_category_status_dict', '启用', '1', '分类已启用', 2, 1, 'admin', NOW());

COMMIT;

-- =============================================
-- 表结构说明
-- =============================================
/*
1. 频道表 (channels)
   - 频道核心信息管理，支持层级结构（父子频道）
   - 支持多种加入方式和发帖权限控制
   - 包含完整的统计字段和状态管理
   - 支持频道分类、推荐和排序功能

2. 频道成员表 (channel_members)
   - 管理频道成员关系和角色权限
   - 支持多级角色体系（创建者、管理员、版主、普通成员）
   - 记录成员状态和活跃时间
   - 支持特殊权限设置

3. 频道权限表 (channel_permissions)
   - 角色权限配置管理，支持细粒度权限控制
   - 权限类型包括内容管理、成员管理、设置管理等
   - 支持权限的动态授予和撤销

4. 频道内容关联表 (channel_content_relations)
   - 管理频道与内容的关联关系
   - 支持内容置顶和精华功能
   - 记录操作者和操作时间
   - 支持置顶排序和精华时间排序

5. 频道邀请表 (channel_invitations)
   - 管理频道邀请机制
   - 支持邀请状态跟踪和过期处理
   - 记录邀请消息和处理结果

6. 频道活动日志表 (channel_activity_logs)
   - 记录频道内所有重要操作
   - 支持多种操作类型和目标类型
   - 提供详细的操作记录和审计功能

7. 频道分类表 (channel_categories)
   - 频道分类管理，支持多级分类
   - 包含分类统计和排序功能
   - 支持分类图标和颜色设置

8. 频道订阅表 (channel_subscriptions)
   - 用户订阅频道关系管理
   - 支持通知设置和订阅类型
   - 记录订阅时间和更新时间

设计特点：
- 统一使用VARCHAR(32)作为主键类型，符合JeecgBoot规范
- 完善的索引设计，包括单列索引和复合索引，优化查询性能
- 支持软删除机制，保证数据安全
- 完整的审计字段（create_by, create_time, update_by, update_time）
- 支持JSONB格式存储复杂数据（权限设置、扩展数据等）
- 层级结构设计，支持父子频道关系
- 完善的权限管理体系，角色权限分离
- 支持频道内容的置顶和精华功能
- 完整的邀请和订阅机制
- 详细的操作日志记录，支持审计和分析
- 灵活的分类管理系统
- 考虑了频道管理的各种业务场景
- 支持频道推荐和排序功能
- 优化的复合索引策略，提升复杂查询性能
*/