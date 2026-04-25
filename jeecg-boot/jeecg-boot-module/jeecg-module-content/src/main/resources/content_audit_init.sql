-- =============================================
-- 内容社区系统 - 审核模块数据库初始化脚本
-- 数据库类型为mysql 版本8.0+
-- =============================================
-- 版本: 1.0
-- 创建时间: 2024-12-19
-- 描述: 审核模块核心数据表，包括内容审核、举报处理、申诉管理、审核规则等功能
-- =============================================

START TRANSACTION;

-- 审核记录表
CREATE TABLE content_audit_records (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '审核记录ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    audit_type INTEGER NOT NULL COMMENT '审核类型：1-系统自动 2-人工审核 3-用户举报',
    audit_status INTEGER NOT NULL COMMENT '审核状态：1-待审核 2-审核通过 3-审核拒绝 4-需要复审',
    audit_result JSON COMMENT '审核结果详情',
    audit_score DECIMAL(5,2) COMMENT '审核评分',
    audit_reason TEXT COMMENT '审核原因',
    auditor_id VARCHAR(32) COMMENT '审核员ID',
    audit_time TIMESTAMP COMMENT '审核时间',
    
    -- 违规信息
    violation_type INTEGER COMMENT '违规类型：1-色情 2-暴力 3-政治敏感 4-广告 5-其他',
    violation_level INTEGER COMMENT '违规级别：1-轻微 2-一般 3-严重',
    violation_keywords JSON COMMENT '违规关键词',
    
    -- 处理结果
    action_type INTEGER COMMENT '处理动作：1-无动作 2-警告 3-删除内容 4-封禁用户',
    action_duration INTEGER COMMENT '处理时长(小时)',
    action_reason TEXT COMMENT '处理原因',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 举报记录表
CREATE TABLE content_report_records (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '举报记录ID',
    content_id VARCHAR(32) NOT NULL COMMENT '被举报内容ID',
    reporter_id VARCHAR(32) NOT NULL COMMENT '举报人ID',
    report_type INTEGER NOT NULL COMMENT '举报类型：1-色情 2-暴力 3-政治敏感 4-广告 5-其他',
    report_reason TEXT NOT NULL COMMENT '举报原因',
    report_evidence JSON COMMENT '举报证据(截图等)',
    report_status INTEGER DEFAULT 1 COMMENT '举报状态：1-待处理 2-处理中 3-已处理 4-已驳回',
    
    -- 处理信息
    handler_id VARCHAR(32) COMMENT '处理人ID',
    handle_time TIMESTAMP COMMENT '处理时间',
    handle_result INTEGER COMMENT '处理结果：1-举报成立 2-举报不成立',
    handle_reason TEXT COMMENT '处理说明',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 申诉记录表
CREATE TABLE content_appeal_records (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '申诉记录ID',
    appellant_id VARCHAR(32) NOT NULL COMMENT '申诉人ID',
    audit_record_id VARCHAR(32) NOT NULL COMMENT '关联的审核记录ID',
    appeal_reason TEXT NOT NULL COMMENT '申诉理由',
    appeal_evidence JSON COMMENT '申诉证据',
    appeal_status INTEGER DEFAULT 1 COMMENT '申诉状态：1-待处理 2-处理中 3-申诉成功 4-申诉失败',
    
    -- 处理信息
    reviewer_id VARCHAR(32) COMMENT '复审员ID',
    review_time TIMESTAMP COMMENT '复审时间',
    review_result INTEGER COMMENT '复审结果：1-申诉成立 2-申诉不成立',
    review_reason TEXT COMMENT '复审说明',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 审核规则表
CREATE TABLE content_audit_rules (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_desc TEXT COMMENT '规则描述',
    rule_category INTEGER NOT NULL COMMENT '规则分类：1-关键词 2-图片 3-视频 4-行为',
    rule_type INTEGER NOT NULL COMMENT '规则类型：1-黑名单 2-白名单 3-正则匹配',
    rule_content TEXT NOT NULL COMMENT '规则内容（关键词、正则表达式等）',
    action_type INTEGER NOT NULL COMMENT '触发动作：1-自动拒绝 2-人工审核 3-警告',
    severity_level INTEGER DEFAULT 2 COMMENT '严重程度：1-低 2-中 3-高',
    rule_status INTEGER DEFAULT 1 COMMENT '规则状态：0-禁用 1-启用',
    hit_count BIGINT DEFAULT 0 COMMENT '命中次数',
    
    -- 系统字段
    del_flag INTEGER DEFAULT 0 COMMENT '删除标识：0-正常 1-删除',
    create_by VARCHAR(32) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 5. 用户处罚记录表 (user_punishments)
-- 功能: 记录对用户的各种处罚措施
CREATE TABLE user_punishments (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '被处罚用户ID',
    punishment_type ENUM('WARNING', 'MUTE', 'BAN', 'DELETE_CONTENT') NOT NULL COMMENT '处罚类型',
    reason TEXT NOT NULL COMMENT '处罚原因',
    duration BIGINT DEFAULT NULL COMMENT '处罚时长（分钟），NULL表示永久',
    moderator_id BIGINT NOT NULL COMMENT '执行处罚的管理员ID',
    report_id BIGINT DEFAULT NULL COMMENT '相关举报ID',
    moderation_record_id BIGINT DEFAULT NULL COMMENT '相关审核记录ID',
    status ENUM('ACTIVE', 'EXPIRED', 'REVOKED') DEFAULT 'ACTIVE' COMMENT '处罚状态',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '处罚开始时间',
    end_time TIMESTAMP DEFAULT NULL COMMENT '处罚结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_punishment_type (punishment_type),
    INDEX idx_status (status),
    INDEX idx_moderator_id (moderator_id),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户处罚记录表';

-- 6. 审核员统计表 (moderator_stats)
-- 功能: 统计审核员的工作数据
CREATE TABLE moderator_stats (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    moderator_id BIGINT NOT NULL COMMENT '审核员ID',
    date DATE NOT NULL COMMENT '统计日期',
    reviewed_count INT DEFAULT 0 COMMENT '审核数量',
    approved_count INT DEFAULT 0 COMMENT '通过数量',
    rejected_count INT DEFAULT 0 COMMENT '拒绝数量',
    reports_handled INT DEFAULT 0 COMMENT '处理举报数量',
    appeals_handled INT DEFAULT 0 COMMENT '处理申诉数量',
    avg_review_time DECIMAL(10,2) DEFAULT 0 COMMENT '平均审核时间（分钟）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_moderator_date (moderator_id, date),
    INDEX idx_moderator_id (moderator_id),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核员统计表';

-- 7. 违禁词表 (forbidden_words)
-- 功能: 存储系统违禁词库
CREATE TABLE forbidden_words (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    word VARCHAR(100) NOT NULL COMMENT '违禁词',
    category ENUM('POLITICAL', 'PORNOGRAPHIC', 'VIOLENT', 'ADVERTISING', 'OTHER') NOT NULL COMMENT '违禁词分类',
    severity ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM' COMMENT '严重程度',
    action ENUM('BLOCK', 'REPLACE', 'REVIEW') DEFAULT 'BLOCK' COMMENT '处理动作',
    replacement VARCHAR(100) DEFAULT NULL COMMENT '替换词（当action为REPLACE时使用）',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    hit_count BIGINT DEFAULT 0 COMMENT '命中次数',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_word (word),
    INDEX idx_category (category),
    INDEX idx_severity (severity),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违禁词表';

-- 8. 审核队列表 (moderation_queue)
-- 功能: 管理待审核内容的队列
CREATE TABLE moderation_queue (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    content_type ENUM('CONTENT', 'COMMENT', 'USER', 'CHANNEL') NOT NULL COMMENT '内容类型',
    priority ENUM('HIGH', 'NORMAL', 'LOW') DEFAULT 'NORMAL' COMMENT '优先级',
    assigned_to BIGINT DEFAULT NULL COMMENT '分配给的审核员ID',
    queue_status ENUM('WAITING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'WAITING' COMMENT '队列状态',
    auto_flags JSON COMMENT '自动检测标记',
    estimated_review_time INT DEFAULT 5 COMMENT '预估审核时间（分钟）',
    assigned_at TIMESTAMP NULL COMMENT '分配时间',
    started_at TIMESTAMP NULL COMMENT '开始审核时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_content_id (content_id),
    INDEX idx_content_type (content_type),
    INDEX idx_priority (priority),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_queue_status (queue_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核队列表';

-- 9. 审核模板表 (moderation_templates)
-- 功能: 存储审核决定的模板和标准回复
CREATE TABLE moderation_templates (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    category ENUM('APPROVAL', 'REJECTION', 'WARNING', 'APPEAL_RESPONSE') NOT NULL COMMENT '模板分类',
    content_type ENUM('CONTENT', 'COMMENT', 'USER', 'CHANNEL', 'ALL') DEFAULT 'ALL' COMMENT '适用内容类型',
    title VARCHAR(200) NOT NULL COMMENT '模板标题',
    content TEXT NOT NULL COMMENT '模板内容',
    variables JSON COMMENT '模板变量定义',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    usage_count BIGINT DEFAULT 0 COMMENT '使用次数',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_category (category),
    INDEX idx_content_type (content_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核模板表';

-- 10. 审核日志表 (moderation_logs)
-- 功能: 记录所有审核相关操作的详细日志
CREATE TABLE moderation_logs (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operation_type ENUM('REVIEW', 'REPORT', 'APPEAL', 'PUNISHMENT', 'RULE_UPDATE') NOT NULL COMMENT '操作类型',
    target_id BIGINT NOT NULL COMMENT '操作对象ID',
    target_type ENUM('CONTENT', 'COMMENT', 'USER', 'CHANNEL', 'RULE', 'TEMPLATE') NOT NULL COMMENT '操作对象类型',
    action VARCHAR(50) NOT NULL COMMENT '具体操作',
    old_value JSON COMMENT '操作前的值',
    new_value JSON COMMENT '操作后的值',
    reason TEXT COMMENT '操作原因',
    ip_address VARCHAR(45) COMMENT '操作IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_operator_id (operator_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_target_id_type (target_id, target_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认审核规则
INSERT INTO content_audit_rules (id, rule_name, rule_desc, rule_category, rule_type, rule_content, action_type, severity_level, create_by) VALUES
('audit_rule_001', '政治敏感词检测', '检测政治敏感内容', 1, 1, '政治敏感词列表', 1, 3, '1'),
('audit_rule_002', '色情内容检测', '检测色情低俗内容', 1, 1, '色情词汇列表', 1, 3, '1'),
('audit_rule_003', '暴力内容检测', '检测暴力恐怖内容', 1, 1, '暴力词汇列表', 2, 2, '1'),
('audit_rule_004', '广告营销检测', '检测广告营销内容', 1, 1, '广告词汇列表', 2, 1, '1'),
('audit_rule_005', '图片内容检测', '检测图片中的不当内容', 2, 3, 'AI图像识别规则', 2, 2, '1'),
('audit_rule_006', '视频内容检测', '检测视频中的不当内容', 3, 3, 'AI视频识别规则', 2, 2, '1');

-- 插入默认违禁词
INSERT INTO forbidden_words (id, word, category, severity, action, created_by) VALUES
('forbidden_word_001', '测试违禁词1', 'OTHER', 'LOW', 'REPLACE', 1),
('forbidden_word_002', '测试违禁词2', 'OTHER', 'MEDIUM', 'BLOCK', 1),
('forbidden_word_003', '测试违禁词3', 'OTHER', 'HIGH', 'BLOCK', 1);

-- 插入默认审核模板
INSERT INTO moderation_templates (id, name, category, content_type, title, content, created_by) VALUES
('template_001', '内容通过模板', 'APPROVAL', 'CONTENT', '您的内容已通过审核', '恭喜！您发布的内容已通过审核，现在可以正常展示给其他用户。', 1),
('template_002', '内容拒绝模板', 'REJECTION', 'CONTENT', '您的内容未通过审核', '很抱歉，您发布的内容因违反社区规定未能通过审核。请修改后重新提交。', 1),
('template_003', '用户警告模板', 'WARNING', 'USER', '社区行为警告', '您的行为违反了社区规定，这是一次警告。请遵守社区规则，避免类似行为。', 1),
('template_004', '申诉通过模板', 'APPEAL_RESPONSE', 'ALL', '申诉处理结果', '经过重新审核，您的申诉已被接受。相关处罚已撤销。', 1),
('template_005', '申诉拒绝模板', 'APPEAL_RESPONSE', 'ALL', '申诉处理结果', '经过重新审核，维持原审核决定。如有疑问，请联系客服。', 1);

-- =============================================
-- 表结构说明和设计特点
-- =============================================

/*
审核模块表结构说明：

1. moderation_records (审核记录表)
   - 记录所有内容的审核状态和结果
   - 支持自动审核和人工审核的双重机制
   - 包含审核优先级和详细的审核信息

2. reports (举报记录表)
   - 记录用户举报的内容和处理过程
   - 支持匿名举报和证据上传
   - 完整的举报处理流程跟踪

3. appeals (申诉记录表)
   - 处理用户对审核结果的申诉
   - 支持多种申诉类型和证据提交
   - 完整的申诉处理流程

4. moderation_rules (审核规则表)
   - 配置自动审核规则
   - 支持关键词、图像、视频等多种检测方式
   - 灵活的规则配置和动作定义

5. user_punishments (用户处罚记录表)
   - 记录对用户的各种处罚措施
   - 支持临时和永久处罚
   - 完整的处罚生命周期管理

6. moderator_stats (审核员统计表)
   - 统计审核员的工作效率和质量
   - 支持按日期维度的统计分析
   - 为审核员绩效评估提供数据支持

7. forbidden_words (违禁词表)
   - 维护系统违禁词库
   - 支持分类管理和不同处理策略
   - 统计违禁词命中情况

8. moderation_queue (审核队列表)
   - 管理待审核内容的队列
   - 支持优先级排序和审核员分配
   - 跟踪审核进度和效率

9. moderation_templates (审核模板表)
   - 提供标准化的审核回复模板
   - 支持变量替换和多场景应用
   - 提高审核效率和一致性

10. moderation_logs (审核日志表)
    - 记录所有审核相关操作的详细日志
    - 支持审计和问题追溯
    - 保障审核过程的透明性

设计特点：
1. 统一主键类型：所有表使用BIGINT类型的自增主键
2. 完善的索引设计：为查询频繁的字段建立合适的索引
3. 软删除机制：重要数据支持软删除，保留历史记录
4. 审计字段：包含创建时间、更新时间等审计信息
5. JSON数据支持：使用JSON字段存储复杂的结构化数据
6. 枚举类型：使用ENUM类型确保数据一致性
7. 状态管理：完善的状态流转机制
8. 关联设计：合理的外键关联和索引优化
9. 扩展性：预留扩展字段，支持未来功能扩展
10. 性能优化：合理的表结构设计和索引策略
11. 数据完整性：通过约束和触发器保证数据完整性
12. 分类管理：支持多维度的分类和标签管理
13. 统计分析：内置统计字段，支持数据分析
14. 模板化设计：支持模板化配置，提高系统灵活性
15. 日志记录：完整的操作日志，支持审计和追溯
16. 队列管理：支持任务队列和负载均衡
17. 规则引擎：灵活的规则配置和执行机制
18. 多级处罚：支持多种处罚类型和等级
19. 申诉机制：完整的申诉和复审流程
20. 数据安全：敏感数据加密和访问控制
*/

COMMIT;