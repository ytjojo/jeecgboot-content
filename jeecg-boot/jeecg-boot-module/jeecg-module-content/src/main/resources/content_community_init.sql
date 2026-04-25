-- =============================================
-- 内容社区系统 - 社群模块数据库初始化脚本
-- =============================================
-- 说明：本文件包含社群相关的所有数据库表定义
-- 依赖：无（已包含所有必要的表定义）
-- 数据库类型为mysql 版本8.0+
-- 版本：1.1
-- 创建时间：2024-12-16
-- 更新时间：2024-12-16
-- =============================================

START TRANSACTION;

-- =============================================
-- 1. 社区表 (community)
-- =============================================
CREATE TABLE IF NOT EXISTS communities (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '社区名称',
    description TEXT COMMENT '社区描述',
    avatar VARCHAR(500) COMMENT '社区头像',
    cover_image VARCHAR(500) COMMENT '社区封面',
    type INTEGER DEFAULT 1 COMMENT '社区类型：1-公开 2-私密 3-付费',
    join_type INTEGER DEFAULT 1 COMMENT '加入方式：1-自由加入 2-申请加入 3-邀请加入',
    post_permission INTEGER DEFAULT 1 COMMENT '发帖权限：1-所有成员 2-管理员 3-指定成员',
    member_count BIGINT DEFAULT 0 COMMENT '成员数量',
    max_member_count BIGINT DEFAULT 0 COMMENT '最大成员数量',
    content_count BIGINT DEFAULT 0 COMMENT '内容数量',
    status INTEGER DEFAULT 1 COMMENT '状态: 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝',
    rules TEXT COMMENT '社区规则',
    announcement TEXT COMMENT '社区公告',
    invite_code VARCHAR(32) COMMENT '邀请码',
    tags JSON COMMENT '社区标签',
    ext_data JSON COMMENT '扩展数据',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社区表索引
CREATE INDEX idx_community_create_by ON communities(create_by);
CREATE INDEX idx_community_type ON communities(type);
CREATE INDEX idx_community_status ON communities(status);
CREATE INDEX idx_community_member_count ON communities(member_count DESC);
CREATE INDEX idx_community_del_flag ON communities(del_flag);

-- 2. 用户资料扩展表已迁移至独立脚本 content_user_core_init.sql，避免重复创建
-- 请确保在初始化流程中先执行 content_user_core_init.sql 以创建 user_profile_extension 并建立相关索引

-- =============================================
-- 3. 社群成员表 (community_members)
-- =============================================
CREATE TABLE IF NOT EXISTS community_members (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    community_id VARCHAR(32) NOT NULL COMMENT '社群ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role INTEGER DEFAULT 1 COMMENT '成员角色：0-访客 1-普通成员 2-版主 3-管理员 4-创建者',
    join_type INTEGER DEFAULT 1 COMMENT '加入方式：1-自由加入 2-申请通过 3-邀请加入',
    join_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_active_time TIMESTAMP COMMENT '最后活跃时间',
    post_count BIGINT DEFAULT 0 COMMENT '发帖数量',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数量',
    like_count BIGINT DEFAULT 0 COMMENT '获赞数量',
    status INTEGER DEFAULT 1 COMMENT '状态：1-正常 2-禁言 3-已退出',
    mute_until TIMESTAMP COMMENT '禁言到期时间',
    mute_reason TEXT COMMENT '禁言原因',
    inviter_id VARCHAR(32) COMMENT '邀请人ID（如果是邀请加入）',
    inviter_at TIMESTAMP COMMENT '邀请时间',
    approved_at TIMESTAMP COMMENT '审核时间',
    approved_by VARCHAR(32) COMMENT '审核人',
    ext_data JSON COMMENT '扩展数据',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群成员表索引
CREATE UNIQUE INDEX idx_community_members_unique ON community_members(community_id, user_id);
CREATE INDEX idx_community_members_community_id ON community_members(community_id);
CREATE INDEX idx_community_members_user_id ON community_members(user_id);
CREATE INDEX idx_community_members_role ON community_members(role);
CREATE INDEX idx_community_members_join_at ON community_members(join_at DESC);
CREATE INDEX idx_community_members_status ON community_members(status);
CREATE INDEX idx_community_members_del_flag ON community_members(del_flag);

-- =============================================
-- 4. 社群申请表 (community_join_requests)
-- =============================================
CREATE TABLE IF NOT EXISTS community_join_requests (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    community_id VARCHAR(32) NOT NULL COMMENT '社群ID',
    user_id VARCHAR(32) NOT NULL COMMENT '申请用户ID',
    request_message TEXT COMMENT '申请理由',
    status INTEGER DEFAULT 0 COMMENT '申请状态：0-待审核 1-已通过 2-已拒绝 3-已取消',
    reviewer_id VARCHAR(32) COMMENT '审核人ID',
    source INTEGER DEFAULT 1 COMMENT '申请来源：1-直接申请 2-邀请链接 3-搜索发现 4-推荐',
    review_note TEXT COMMENT '审核意见',
    review_time TIMESTAMP COMMENT '审核时间',
    expire_time TIMESTAMP COMMENT '申请过期时间',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群申请表索引
CREATE INDEX idx_community_join_requests_community_id ON community_join_requests(community_id);
CREATE INDEX idx_community_join_requests_user_id ON community_join_requests(user_id);
CREATE INDEX idx_community_join_requests_status ON community_join_requests(status);
CREATE INDEX idx_community_join_requests_create_time ON community_join_requests(create_time DESC);
CREATE INDEX idx_community_join_requests_del_flag ON community_join_requests(del_flag);

-- =============================================
-- 5. 社群公告表 (community_announcements)
-- =============================================
CREATE TABLE IF NOT EXISTS community_announcements (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    community_id VARCHAR(32) NOT NULL COMMENT '社群ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    is_pinned INTEGER DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    sort_order INTEGER DEFAULT 0 COMMENT '排序号',
    type INTEGER DEFAULT 1 COMMENT '公告类型：1-普通公告 2-重要公告 3-紧急公告',
    read_count BIGINT DEFAULT 0 COMMENT '阅读数量',
    status INTEGER DEFAULT 1 COMMENT '状态：1-正常 2-已下线',
    publish_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    expire_time TIMESTAMP COMMENT '过期时间',
    publisher_id VARCHAR(32) NOT NULL COMMENT '发布人ID',
    ext_data JSON COMMENT '扩展数据',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群公告表索引
CREATE INDEX idx_community_announcements_community_id ON community_announcements(community_id);
CREATE INDEX idx_community_announcements_publisher_id ON community_announcements(publisher_id);
CREATE INDEX idx_community_announcements_status ON community_announcements(status);
CREATE INDEX idx_community_announcements_is_pinned ON community_announcements(is_pinned);
CREATE INDEX idx_community_announcements_publish_time ON community_announcements(publish_time DESC);
CREATE INDEX idx_community_announcements_del_flag ON community_announcements(del_flag);

-- =============================================
-- 6. 社群公告阅读记录表 (community_announcement_reads)
-- =============================================
CREATE TABLE IF NOT EXISTS community_announcement_reads (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    announcement_id VARCHAR(32) NOT NULL COMMENT '公告ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    read_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(32) COMMENT '创建人',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群公告阅读记录表索引
CREATE UNIQUE INDEX idx_announcement_reads_unique ON community_announcement_reads(announcement_id, user_id);
CREATE INDEX idx_announcement_reads_announcement_id ON community_announcement_reads(announcement_id);
CREATE INDEX idx_announcement_reads_user_id ON community_announcement_reads(user_id);
CREATE INDEX idx_announcement_reads_read_time ON community_announcement_reads(read_time DESC);

-- =============================================
-- 7. 社群邀请表 (community_invitations)
-- =============================================
CREATE TABLE IF NOT EXISTS community_invitations (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    community_id VARCHAR(32) NOT NULL COMMENT '社群ID',
    inviter_id VARCHAR(32) NOT NULL COMMENT '邀请人ID',
    invitee_id VARCHAR(32) COMMENT '被邀请人ID（如果已注册）',
    invitee_phone VARCHAR(20) COMMENT '被邀请人手机号（如果未注册）',
    invitation_channel INTEGER DEFAULT 1 COMMENT '邀请渠道：1-系统内 2-邮件 3-短信 4-微信 5-QQ 6-其他',
    invitation_code VARCHAR(50) NOT NULL COMMENT '邀请码',
    invitation_message TEXT COMMENT '邀请消息',
    status INTEGER DEFAULT 0 COMMENT '邀请状态：0-待接受 1-已接受 2-已拒绝 3-已过期',
    accept_time TIMESTAMP COMMENT '接受时间',
    expire_time TIMESTAMP NOT NULL COMMENT '过期时间',
    rejected_at TIMESTAMP COMMENT '拒绝时间',
    reject_reason TEXT COMMENT '拒绝原因',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群邀请表索引
CREATE UNIQUE INDEX idx_community_invitations_code ON community_invitations(invitation_code);
CREATE INDEX idx_community_invitations_community_id ON community_invitations(community_id);
CREATE INDEX idx_community_invitations_inviter_id ON community_invitations(inviter_id);
CREATE INDEX idx_community_invitations_invitee_id ON community_invitations(invitee_id);
CREATE INDEX idx_community_invitations_status ON community_invitations(status);
CREATE INDEX idx_community_invitations_expire_time ON community_invitations(expire_time);
CREATE INDEX idx_community_invitations_del_flag ON community_invitations(del_flag);

-- =============================================
-- 8. 社群规则表 (community_rules)
-- =============================================
CREATE TABLE IF NOT EXISTS community_rules (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    community_id VARCHAR(32) NOT NULL COMMENT '社群ID',
    title VARCHAR(200) NOT NULL COMMENT '规则标题',
    content TEXT NOT NULL COMMENT '规则内容',
    rule_type INTEGER DEFAULT 1 COMMENT '规则类型：1-社群规则 2-发帖规则 3-评论规则 4-行为规范 5-其他',
    rule_level INTEGER DEFAULT 1 COMMENT '规则级别：1-建议 2-警告 3-强制 4-禁止',
    punishment INTEGER DEFAULT 1 COMMENT '违规处罚：1-警告 2-禁言 3-踢出 4-封禁 5-其他',
    punishment_duration INTEGER DEFAULT 0 COMMENT '处罚时长（分钟，0表示永久）',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
    is_required INTEGER DEFAULT 0 COMMENT '是否必读：0-否 1-是',
    show_on_join INTEGER DEFAULT 0 COMMENT '是否在加入时显示：0-否 1-是',
    effective_at TIMESTAMP NULL COMMENT '生效时间',
    expires_at TIMESTAMP NULL COMMENT '失效时间',
    version VARCHAR(50) DEFAULT '1.0' COMMENT '规则版本',
    parent_id VARCHAR(32) NULL COMMENT '父规则ID（用于规则层级）',
    rule_path VARCHAR(500) NULL COMMENT '规则路径（层级路径）',
    ext_data TEXT NULL COMMENT '扩展数据（JSON格式）',
    status INTEGER DEFAULT 1 COMMENT '状态：0-草稿 1-启用 2-禁用',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群规则表索引
CREATE INDEX idx_community_rules_community_id ON community_rules(community_id);
CREATE INDEX idx_community_rules_rule_type ON community_rules(rule_type);
CREATE INDEX idx_community_rules_rule_level ON community_rules(rule_level);
CREATE INDEX idx_community_rules_punishment ON community_rules(punishment);
CREATE INDEX idx_community_rules_sort_order ON community_rules(sort_order);
CREATE INDEX idx_community_rules_status ON community_rules(status);
CREATE INDEX idx_community_rules_del_flag ON community_rules(del_flag);
CREATE INDEX idx_community_rules_parent_id ON community_rules(parent_id);
CREATE INDEX idx_community_rules_effective_at ON community_rules(effective_at);
CREATE INDEX idx_community_rules_expires_at ON community_rules(expires_at);
CREATE INDEX idx_community_rules_version ON community_rules(version);
-- 复合索引
CREATE INDEX idx_community_rules_community_status ON community_rules(community_id, status);
CREATE INDEX idx_community_rules_community_type ON community_rules(community_id, rule_type);
CREATE INDEX idx_community_rules_parent_sort ON community_rules(parent_id, sort_order);
-- =============================================
-- 9 社群统计表 (community_statistics)
-- =============================================
CREATE TABLE IF NOT EXISTS community_statistics (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    community_id VARCHAR(32) NOT NULL COMMENT '社群ID',
    statistics_date DATE NOT NULL COMMENT '统计日期',
    total_members INTEGER DEFAULT 0 COMMENT '总成员数',
    active_members INTEGER DEFAULT 0 COMMENT '活跃成员数',
    new_members INTEGER DEFAULT 0 COMMENT '新增成员数',
    left_members INTEGER DEFAULT 0 COMMENT '退出成员数',
    total_posts INTEGER DEFAULT 0 COMMENT '总帖子数',
    new_posts INTEGER DEFAULT 0 COMMENT '新增帖子数',
    total_comments INTEGER DEFAULT 0 COMMENT '总评论数',
    new_comments INTEGER DEFAULT 0 COMMENT '新增评论数',
    total_likes INTEGER DEFAULT 0 COMMENT '总点赞数',
    new_likes INTEGER DEFAULT 0 COMMENT '新增点赞数',
    total_views INTEGER DEFAULT 0 COMMENT '总浏览量',
    new_views INTEGER DEFAULT 0 COMMENT '新增浏览量',
    join_requests INTEGER DEFAULT 0 COMMENT '申请加入数',
    invitations INTEGER DEFAULT 0 COMMENT '邀请数',
    activity_score DECIMAL(10,2) DEFAULT 0.00 COMMENT '活跃度评分',
    health_score DECIMAL(10,2) DEFAULT 0.00 COMMENT '健康度评分',
    growth_index DECIMAL(10,2) DEFAULT 0.00 COMMENT '成长指数',
    interaction_index DECIMAL(10,2) DEFAULT 0.00 COMMENT '互动指数',
    content_quality_index DECIMAL(10,2) DEFAULT 0.00 COMMENT '内容质量指数',
    retention_rate DECIMAL(10,2) DEFAULT 0.00 COMMENT '用户留存率',
    daily_active_users INTEGER DEFAULT 0 COMMENT '日活跃用户数',
    weekly_active_users INTEGER DEFAULT 0 COMMENT '周活跃用户数',
    monthly_active_users INTEGER DEFAULT 0 COMMENT '月活跃用户数',
    avg_online_time INTEGER DEFAULT 0 COMMENT '平均在线时长（分钟）',
    del_flag INTEGER DEFAULT 0 COMMENT '逻辑删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 社群统计表索引
CREATE INDEX idx_community_statistics_community_id ON community_statistics(community_id);
CREATE INDEX idx_community_statistics_date ON community_statistics(statistics_date DESC);
CREATE UNIQUE INDEX idx_community_statistics_unique ON community_statistics(community_id, statistics_date);
CREATE INDEX idx_community_statistics_activity_score ON community_statistics(activity_score DESC);
CREATE INDEX idx_community_statistics_health_score ON community_statistics(health_score DESC);
CREATE INDEX idx_community_statistics_del_flag ON community_statistics(del_flag);

-- =============================================
-- 社群统计表说明文档
-- =============================================
-- 表名：community_statistics
-- 用途：记录社群的各项统计数据，支持按日期统计
-- 特点：
-- 1. 按社群和日期进行统计，支持历史数据查询
-- 2. 包含成员、内容、互动等多维度统计指标
-- 3. 支持活跃度、健康度等评分指标
-- 4. 提供用户活跃度分析（日活、周活、月活）
-- 5. 支持数据趋势分析和对比
-- 6. 采用逻辑删除，保护历史统计数据
-- 7. 唯一索引确保同一社群同一日期只有一条记录
-- 8. 多个索引优化查询性能，支持排行榜等功能
-- =============================================

-- =============================================
-- 10. 触发器 - 更新社群成员数量
-- =============================================

-- 插入成员时增加计数
DROP TRIGGER IF EXISTS trigger_community_member_insert;
DELIMITER $$
CREATE TRIGGER trigger_community_member_insert
    AFTER INSERT ON community_members
    FOR EACH ROW
BEGIN
    IF NEW.status = 1 AND NEW.del_flag = 0 THEN
        UPDATE communities 
        SET member_count = member_count + 1,
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.community_id;
    END IF;
END$$
DELIMITER ;

-- 删除成员时减少计数
DROP TRIGGER IF EXISTS trigger_community_member_delete;
DELIMITER $$
CREATE TRIGGER trigger_community_member_delete
    AFTER DELETE ON community_members
    FOR EACH ROW
BEGIN
    IF OLD.status = 1 AND OLD.del_flag = 0 THEN
        UPDATE communities 
        SET member_count = GREATEST(member_count - 1, 0),
            update_time = CURRENT_TIMESTAMP
        WHERE id = OLD.community_id;
    END IF;
END$$
DELIMITER ;

-- 更新成员状态时调整计数
DROP TRIGGER IF EXISTS trigger_community_member_update;
DELIMITER $$
CREATE TRIGGER trigger_community_member_update
    AFTER UPDATE ON community_members
    FOR EACH ROW
BEGIN
    -- 从正常状态变为非正常状态
    IF OLD.status = 1 AND OLD.del_flag = 0 AND (NEW.status != 1 OR NEW.del_flag = 1) THEN
        UPDATE communities 
        SET member_count = GREATEST(member_count - 1, 0),
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.community_id;
    -- 从非正常状态变为正常状态
    ELSEIF (OLD.status != 1 OR OLD.del_flag = 1) AND NEW.status = 1 AND NEW.del_flag = 0 THEN
        UPDATE communities 
        SET member_count = member_count + 1,
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.community_id;
    END IF;
END$$
DELIMITER ;

-- =============================================
-- 11. 触发器 - 更新公告阅读数量
-- =============================================

-- 插入阅读记录时增加计数
DROP TRIGGER IF EXISTS trigger_announcement_read_insert;
DELIMITER $$
CREATE TRIGGER trigger_announcement_read_insert
    AFTER INSERT ON community_announcement_reads
    FOR EACH ROW
BEGIN
    UPDATE community_announcements 
    SET read_count = read_count + 1,
        update_time = CURRENT_TIMESTAMP
    WHERE id = NEW.announcement_id;
END$$
DELIMITER ;

-- 删除阅读记录时减少计数
DROP TRIGGER IF EXISTS trigger_announcement_read_delete;
DELIMITER $$
CREATE TRIGGER trigger_announcement_read_delete
    AFTER DELETE ON community_announcement_reads
    FOR EACH ROW
BEGIN
    UPDATE community_announcements 
    SET read_count = GREATEST(read_count - 1, 0),
        update_time = CURRENT_TIMESTAMP
    WHERE id = OLD.announcement_id;
END$$
DELIMITER ;

-- =============================================
-- 11. 数据字典
-- =============================================
-- INSERT IGNORE INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time) VALUES
-- ('community_member_role_dict', '社群成员角色', 'community_member_role', '社群成员角色字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_join_type_dict', '社群加入方式', 'community_join_type', '社群加入方式字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_member_status_dict', '社群成员状态', 'community_member_status', '社群成员状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_request_status_dict', '社群申请状态', 'community_request_status', '社群申请状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_invitation_status_dict', '社群邀请状态', 'community_invitation_status', '社群邀请状态字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
-- ('community_rule_type_dict', '社群规则类型', 'community_rule_type', '社群规则类型字典', 0, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

-- -- =============================================
-- -- 12. 数据字典项
-- -- =============================================
-- INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) VALUES 
-- -- 社群成员角色
-- ('comm_member_normal', 'community_member_role_dict', '普通成员', '1', '普通成员', 1, 1, 'admin', NOW()),
-- ('comm_member_moderator', 'community_member_role_dict', '版主', '2', '版主', 2, 1, 'admin', NOW()),
-- ('comm_member_admin', 'community_member_role_dict', '管理员', '3', '管理员', 3, 1, 'admin', NOW()),
-- ('comm_member_creator', 'community_member_role_dict', '创建者', '4', '创建者', 4, 1, 'admin', NOW()),
-- -- 社群加入方式
-- ('comm_join_free', 'community_join_type_dict', '自由加入', '1', '自由加入', 1, 1, 'admin', NOW()),
-- ('comm_join_apply', 'community_join_type_dict', '申请通过', '2', '申请通过', 2, 1, 'admin', NOW()),
-- ('comm_join_invite', 'community_join_type_dict', '邀请加入', '3', '邀请加入', 3, 1, 'admin', NOW()),
-- -- 社群成员状态
-- ('comm_status_normal', 'community_member_status_dict', '正常', '1', '正常状态', 1, 1, 'admin', NOW()),
-- ('comm_status_muted', 'community_member_status_dict', '禁言', '2', '禁言状态', 2, 1, 'admin', NOW()),
-- ('comm_status_left', 'community_member_status_dict', '已退出', '3', '已退出', 3, 1, 'admin', NOW()),
-- -- 社群申请状态
-- ('comm_req_pending', 'community_request_status_dict', '待审核', '0', '待审核', 1, 1, 'admin', NOW()),
-- ('comm_req_approved', 'community_request_status_dict', '已通过', '1', '已通过', 2, 1, 'admin', NOW()),
-- ('comm_req_rejected', 'community_request_status_dict', '已拒绝', '2', '已拒绝', 3, 1, 'admin', NOW()),
-- ('comm_req_cancelled', 'community_request_status_dict', '已取消', '3', '已取消', 4, 1, 'admin', NOW()),
-- -- 社群邀请状态
-- ('comm_inv_pending', 'community_invitation_status_dict', '待接受', '0', '待接受', 1, 1, 'admin', NOW()),
-- ('comm_inv_accepted', 'community_invitation_status_dict', '已接受', '1', '已接受', 2, 1, 'admin', NOW()),
-- ('comm_inv_rejected', 'community_invitation_status_dict', '已拒绝', '2', '已拒绝', 3, 1, 'admin', NOW()),
-- ('comm_inv_expired', 'community_invitation_status_dict', '已过期', '3', '已过期', 4, 1, 'admin', NOW()),
-- -- 社群规则类型
-- ('comm_rule_general', 'community_rule_type_dict', '社群规则', '1', '社群规则', 1, 1, 'admin', NOW()),
-- ('comm_rule_post', 'community_rule_type_dict', '发帖规则', '2', '发帖规则', 2, 1, 'admin', NOW()),
-- ('comm_rule_comment', 'community_rule_type_dict', '评论规则', '3', '评论规则', 3, 1, 'admin', NOW());

COMMIT;

-- =============================================
-- 表结构说明
-- =============================================
-- 本脚本包含以下8个核心表（本文件定义）：
-- 1. community - 社区表：存储社区基本信息、类型、权限设置等
-- 2. community_members - 社群成员表：管理社群成员关系、角色、权限等
-- 3. community_join_requests - 社群申请表：处理用户加入社群的申请流程
-- 4. community_announcements - 社群公告表：发布和管理社群公告信息
-- 5. community_announcement_reads - 社群公告阅读记录表：跟踪公告阅读状态
-- 6. community_invitations - 社群邀请表：管理社群邀请机制
-- 7. community_rules - 社群规则表：定义和管理社群规则
-- 8. community_statistics - 社群统计表：按社群和日期进行统计，支持历史数据查询，包含成员、内容、互动等多维度统计指标
--
-- 说明：
-- - 用户资料扩展表 user_profile_extension 已迁移至独立脚本 content_user_core_init.sql，由内容用户核心初始化脚本统一创建，请确保先执行该脚本。
--
-- =============================================
-- 设计特点
-- =============================================
-- 1. 统一主键类型：所有表使用 VARCHAR(32) 作为主键，保持一致性
-- 2. 完善的索引设计：为查询频繁的字段建立索引，提升查询性能
-- 3. 软删除机制：使用 del_flag 字段实现逻辑删除，保护数据完整性
-- 4. 审计字段：包含create_by、create_time、update_by、update_time等审计信息
-- 5. JSONB支持：使用JSONB字段存储扩展数据，提供灵活的数据结构
-- 6. 层级结构：支持社区-成员的层级关系管理
-- 7. 权限管理：细粒度的角色权限控制（普通成员、版主、管理员、创建者）
-- 8. 状态管理：多种状态控制（正常、禁用、审核中、禁言等）
-- 9. 邀请机制：支持邀请码、邮箱、手机号等多种邀请方式
-- 10. 公告系统：支持置顶、重要标记、目标角色等功能
-- 11. 申请流程：完整的加入申请、审核、通过/拒绝流程
-- 12. 触发器优化：自动维护统计数据（成员数量、阅读数量等）
-- 13. 数据字典：标准化的状态值和类型定义
-- 14. 复合索引：针对多字段查询场景优化的复合索引
-- 15. 约束保护：唯一性约束防止重复数据
-- =============================================
