# MySQL 8.0+ 数据库设计规范文档

## 目录
1. [概述](#概述)
2. [命名规范](#命名规范)
3. [主键设计](#主键设计)
4. [数据类型规范](#数据类型规范)
5. [索引设计](#索引设计)
6. [外键约束](#外键约束)
7. [字符集和排序规则](#字符集和排序规则)
8. [JSON字段设计](#json字段设计)
9. [表结构设计](#表结构设计)
10. [性能优化](#性能优化)
11. [安全规范](#安全规范)
12. [最佳实践](#最佳实践)

---

## 概述

本文档基于MySQL 8.0+版本，结合JeecgBoot框架特点，制定了全面的数据库设计规范。旨在提高数据库性能、可维护性和安全性。

### 适用范围
- MySQL 8.0及以上版本
- JeecgBoot框架项目
- 企业级应用开发

---

## 命名规范

### 表命名规范
```sql
-- ✅ 推荐：使用小写复数，下划线分隔
CREATE TABLE user_profiles (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户名'
);

-- ❌ 不推荐：驼峰命名、单数形式
CREATE TABLE UserProfile (
    ID int,
    UserName varchar(50)
);
```

**规则：**
- 表名使用**小写复数**形式
- 单词间用下划线分隔（snake_case）
- 避免使用MySQL保留字
- 表名长度不超过64个字符
- 使用有意义的英文单词，避免拼音

### 列命名规范
```sql
-- ✅ 推荐：小写单数，下划线分隔
CREATE TABLE orders (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '订单ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    order_status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1-待支付，2-已支付',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
```

**规则：**
- 列名使用**小写单数**形式
- 单词间用下划线分隔
- 布尔类型字段使用is_前缀：`is_active`, `is_deleted`
- 时间字段使用统一后缀：`created_time`, `updated_time`
- 外键字段使用`表名_id`格式：`user_id`, `order_id`

### 索引命名规范
```sql
-- 主键索引（自动创建）
PRIMARY KEY (`id`)

-- 唯一索引
UNIQUE KEY `uk_users_email` (`email`)
UNIQUE KEY `uk_users_phone` (`phone`)

-- 普通索引
KEY `idx_users_status` (`status`)
KEY `idx_users_created_time` (`created_time`)

-- 复合索引
KEY `idx_orders_user_status` (`user_id`, `status`)
KEY `idx_orders_created_time_status` (`created_time`, `status`)

-- 外键索引
KEY `fk_orders_user_id` (`user_id`)
```

**规则：**
- 主键索引：`PRIMARY KEY`
- 唯一索引：`uk_表名_列名`
- 普通索引：`idx_表名_列名`
- 复合索引：`idx_表名_列名1_列名2`
- 外键索引：`fk_表名_列名`

---

## 主键设计

### JeecgBoot框架主键规范
```sql
-- ✅ 推荐：JeecgBoot标准主键（雪花算法生成）
CREATE TABLE sys_users (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    -- 其他字段...
    created_by VARCHAR(32) COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(32) COMMENT '更新人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
```

### 主键类型对比

| 主键类型 | 优点 | 缺点 | 适用场景 |
|---------|------|------|----------|
| VARCHAR(32) | 分布式友好、全局唯一、安全性高 | 存储空间大、性能略低 | **JeecgBoot推荐**、分布式系统 |
| BIGINT AUTO_INCREMENT | 性能高、存储空间小 | 分布式困难、可预测 | 单机系统、高性能要求 |
| UUID() | 全局唯一、分布式友好 | 无序、存储空间大 | 需要全局唯一性 |

### 主键设计原则
```sql
-- ✅ 推荐：JeecgBoot标准主键
id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID'

-- ✅ 可选：自增主键（单机系统）
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID'

-- ❌ 不推荐：复合主键
PRIMARY KEY (user_id, order_id)  -- 避免使用

-- ❌ 不推荐：业务字段作主键
PRIMARY KEY (email)  -- 业务字段可能变更
```

---

## 数据类型规范

### 整数类型
```sql
-- 根据数值范围选择合适的整数类型
CREATE TABLE examples (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    
    -- 状态字段：使用TINYINT
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    
    -- 年龄、数量等小范围数值
    age TINYINT UNSIGNED COMMENT '年龄（0-255）',
    quantity SMALLINT UNSIGNED COMMENT '数量（0-65535）',
    
    -- 金额相关：使用DECIMAL
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    
    -- 计数器、ID等
    view_count INT UNSIGNED DEFAULT 0 COMMENT '浏览次数',
    sort_order INT DEFAULT 0 COMMENT '排序号'
);
```

### 字符串类型
```sql
CREATE TABLE string_examples (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    
    -- 固定长度字符串
    country_code CHAR(2) COMMENT '国家代码',
    phone_code CHAR(4) COMMENT '区号',
    
    -- 变长字符串（推荐）
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    mobile VARCHAR(20) COMMENT '手机号',
    
    -- 长文本
    description TEXT COMMENT '描述信息',
    content LONGTEXT COMMENT '内容详情',
    
    -- JSON数据
    extra_data JSON COMMENT '扩展数据'
);
```

### 时间类型
```sql
CREATE TABLE time_examples (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    
    -- ✅ 推荐：DATETIME（不受时区影响）
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- ✅ 推荐：TIMESTAMP（自动时区转换）
    login_time TIMESTAMP NULL COMMENT '登录时间',
    
    -- 日期类型
    birth_date DATE COMMENT '出生日期',
    
    -- 时间类型
    work_time TIME COMMENT '工作时间'
);
```

### 数据类型选择指南

| 数据类型 | 存储范围 | 存储空间 | 使用场景 |
|---------|----------|----------|----------|
| TINYINT | -128~127 | 1字节 | 状态、标志位 |
| SMALLINT | -32768~32767 | 2字节 | 年龄、数量 |
| INT | -2^31~2^31-1 | 4字节 | 计数器、排序 |
| BIGINT | -2^63~2^63-1 | 8字节 | 大数值 |
| DECIMAL(M,D) | 精确数值 | M+2字节 | 金额、价格 |
| VARCHAR(N) | 0~65535字符 | 实际长度+1~2字节 | 变长字符串 |
| TEXT | 0~65535字符 | 实际长度+2字节 | 长文本 |
| JSON | JSON文档 | 实际长度+元数据 | 结构化数据 |

### 枚举类型规范

枚举字段应使用**INTEGER类型**存储，配合**CHECK约束**和详细的**COMMENT注释**来确保数据完整性和可读性。

#### 枚举字段设计原则
```sql
-- ✅ 推荐：使用INTEGER + CHECK约束 + 详细注释
CREATE TABLE comments (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    content TEXT NOT NULL COMMENT '评论内容',
    
    -- 枚举字段标准格式
    status INTEGER DEFAULT 1 CHECK (status IN (1, 2, 3, 4, 5)) 
        COMMENT '评论状态：1-正常(NORMAL)，2-隐藏(HIDDEN)，3-删除(DELETED)，4-审核中(UNDER_REVIEW)，5-违规(VIOLATION)',
    
    priority INTEGER DEFAULT 1 CHECK (priority IN (1, 2, 3)) 
        COMMENT '优先级：1-低(LOW)，2-中(MEDIUM)，3-高(HIGH)',
    
    type INTEGER DEFAULT 1 CHECK (type IN (1, 2, 3, 4)) 
        COMMENT '评论类型：1-文章评论(ARTICLE)，2-回复评论(REPLY)，3-系统消息(SYSTEM)，4-举报(REPORT)'
);
```

#### 枚举字段命名规范
```sql
-- ✅ 推荐的枚举字段命名
status INTEGER DEFAULT 1 CHECK (status IN (1, 2, 3))     -- 状态类
type INTEGER DEFAULT 1 CHECK (type IN (1, 2, 3))         -- 类型类
level INTEGER DEFAULT 1 CHECK (level IN (1, 2, 3))       -- 级别类
priority INTEGER DEFAULT 1 CHECK (priority IN (1, 2, 3)) -- 优先级类
category INTEGER DEFAULT 1 CHECK (category IN (1, 2, 3)) -- 分类类

-- ❌ 避免使用的命名
state INTEGER DEFAULT 1 CHECK (state IN (1, 2, 3))       -- 与status重复
kind INTEGER DEFAULT 1 CHECK (kind IN (1, 2, 3))         -- 与type重复
```

#### 枚举值设计规范

**数值分配原则：**
- 从1开始分配（避免0值的歧义）
- 连续递增分配
- 预留扩展空间
- 删除的枚举值不要重复使用

```sql
-- ✅ 推荐：标准枚举值设计
CREATE TABLE articles (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    
    -- 发布状态（预留扩展空间）
    publish_status INTEGER DEFAULT 1 CHECK (publish_status IN (1, 2, 3, 4, 5)) 
        COMMENT '发布状态：1-草稿(DRAFT)，2-待审核(PENDING)，3-已发布(PUBLISHED)，4-已下线(OFFLINE)，5-已删除(DELETED)',
    
    -- 文章类型
    article_type INTEGER DEFAULT 1 CHECK (article_type IN (1, 2, 3, 4)) 
        COMMENT '文章类型：1-原创(ORIGINAL)，2-转载(REPRINT)，3-翻译(TRANSLATION)，4-合集(COLLECTION)',
    
    -- 可见性级别
    visibility INTEGER DEFAULT 1 CHECK (visibility IN (1, 2, 3)) 
        COMMENT '可见性：1-公开(PUBLIC)，2-仅好友(FRIENDS)，3-私密(PRIVATE)'
);
```

#### 枚举注释格式规范

**标准格式：**
```
COMMENT '字段描述：数值1-中文名称(英文常量)，数值2-中文名称(英文常量)，...'
```

**示例：**
```sql
-- ✅ 标准格式
status INTEGER DEFAULT 1 CHECK (status IN (1, 2, 3, 4, 5)) 
    COMMENT '评论状态：1-正常(NORMAL)，2-隐藏(HIDDEN)，3-删除(DELETED)，4-审核中(UNDER_REVIEW)，5-违规(VIOLATION)'

-- ✅ 简化格式（当英文常量较长时）
user_role INTEGER DEFAULT 1 CHECK (user_role IN (1, 2, 3, 4)) 
    COMMENT '用户角色：1-普通用户，2-VIP用户，3-管理员，4-超级管理员'
```

#### 枚举字段最佳实践

```sql
-- ✅ 完整的枚举字段示例
CREATE TABLE orders (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    
    -- 订单状态（业务核心枚举）
    order_status INTEGER DEFAULT 1 CHECK (order_status IN (1, 2, 3, 4, 5, 6, 7)) 
        COMMENT '订单状态：1-待支付(PENDING_PAYMENT)，2-已支付(PAID)，3-已发货(SHIPPED)，4-已收货(DELIVERED)，5-已完成(COMPLETED)，6-已取消(CANCELLED)，7-已退款(REFUNDED)',
    
    -- 支付方式
    payment_method INTEGER DEFAULT 1 CHECK (payment_method IN (1, 2, 3, 4, 5)) 
        COMMENT '支付方式：1-微信支付(WECHAT)，2-支付宝(ALIPAY)，3-银行卡(BANK_CARD)，4-余额支付(BALANCE)，5-积分支付(POINTS)',
    
    -- 配送方式
    delivery_type INTEGER DEFAULT 1 CHECK (delivery_type IN (1, 2, 3)) 
        COMMENT '配送方式：1-快递配送(EXPRESS)，2-自提(PICKUP)，3-同城配送(LOCAL_DELIVERY)',
    
    -- 订单优先级
    priority INTEGER DEFAULT 2 CHECK (priority IN (1, 2, 3)) 
        COMMENT '订单优先级：1-低(LOW)，2-普通(NORMAL)，3-高(HIGH)',
    
    -- 是否需要发票（布尔枚举）
    need_invoice INTEGER DEFAULT 0 CHECK (need_invoice IN (0, 1)) 
        COMMENT '是否需要发票：0-不需要(NO)，1-需要(YES)'
);
```

#### 与Java枚举类的对应关系

```java
// Java端对应的枚举类
public enum OrderStatus {
    PENDING_PAYMENT(1, "待支付"),
    PAID(2, "已支付"),
    SHIPPED(3, "已发货"),
    DELIVERED(4, "已收货"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    REFUNDED(7, "已退款");
    
    private final Integer code;
    private final String desc;
    
    // 构造函数和getter方法...
}
```

---

## 索引设计

### 索引类型和使用场景
```sql
CREATE TABLE index_examples (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status TINYINT NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 唯一索引：确保数据唯一性
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    
    -- 普通索引：提高查询性能
    KEY idx_status (status),
    KEY idx_created_time (created_time),
    
    -- 复合索引：多字段查询优化
    KEY idx_status_created_time (status, created_time),
    
    -- 前缀索引：节省存储空间
    KEY idx_phone_prefix (phone(11))
);
```

### 索引设计原则

#### 1. 创建索引的场景
```sql
-- ✅ WHERE条件中频繁使用的列
SELECT * FROM users WHERE status = 1;  -- 需要idx_status

-- ✅ ORDER BY排序的列
SELECT * FROM users ORDER BY created_time DESC;  -- 需要idx_created_time

-- ✅ GROUP BY分组的列
SELECT status, COUNT(*) FROM users GROUP BY status;  -- 需要idx_status

-- ✅ JOIN连接的列
SELECT u.*, p.* FROM users u JOIN profiles p ON u.id = p.user_id;  -- 需要idx_user_id
```

#### 2. 复合索引设计
```sql
-- 复合索引遵循最左前缀原则
KEY idx_status_created_time_updated_time (status, created_time, updated_time)

-- 可以使用的查询：
-- ✅ WHERE status = 1
-- ✅ WHERE status = 1 AND created_time > '2024-01-01'
-- ✅ WHERE status = 1 AND created_time > '2024-01-01' AND updated_time < '2024-12-31'

-- 无法使用的查询：
-- ❌ WHERE created_time > '2024-01-01'  -- 跳过了status
-- ❌ WHERE updated_time < '2024-12-31'  -- 跳过了status和created_time
```

#### 3. 索引优化建议
```sql
-- ✅ 推荐：选择性高的列放在前面
KEY idx_user_status_type (user_id, status, type)  -- user_id选择性最高

-- ✅ 推荐：覆盖索引减少回表
KEY idx_user_status_created_time (user_id, status, created_time)
-- 查询：SELECT user_id, status, created_time FROM users WHERE user_id = 'xxx'

-- ❌ 避免：过多索引影响写入性能
-- 单表索引数量建议不超过5个

-- ❌ 避免：重复索引
KEY idx_status (status)
KEY idx_status_duplicate (status)  -- 重复索引
```

---

## 外键约束

### 外键设计规范
```sql
-- 主表
CREATE TABLE users (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 从表
CREATE TABLE user_profiles (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '档案ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    
    -- 外键约束
    CONSTRAINT fk_user_profiles_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- 外键索引
    KEY fk_user_profiles_user_id (user_id)
);
```

### 外键约束选项

| 约束选项 | 说明 | 使用场景 |
|---------|------|----------|
| CASCADE | 级联操作 | 主表删除时从表也删除 |
| SET NULL | 设置为NULL | 主表删除时从表外键置空 |
| RESTRICT | 限制操作 | 有从表数据时不允许删除主表 |
| NO ACTION | 无操作 | 同RESTRICT |

### 外键使用建议
```sql
-- ✅ 推荐：重要业务关系使用外键
CREATE TABLE orders (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    
    CONSTRAINT fk_orders_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT,  -- 防止误删用户
    
    KEY fk_orders_user_id (user_id)
);

-- ⚠️ 注意：高并发场景可考虑不使用外键
-- 在应用层保证数据一致性，避免数据库锁竞争
```

---

## 字符集和排序规则

### 字符集配置
```sql
-- ✅ 推荐：数据库级别配置
CREATE DATABASE jeecg_boot 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- ✅ 推荐：表级别配置
CREATE TABLE users (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    nickname VARCHAR(100)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户表';

-- ✅ 推荐：字段级别配置（特殊需求）
CREATE TABLE articles (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    title VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);
```

### 字符集选择指南

| 字符集 | 说明 | 存储空间 | 使用场景 |
|--------|------|----------|----------|
| utf8mb4 | **推荐**，支持完整UTF-8 | 1-4字节/字符 | 国际化应用、表情符号 |
| utf8 | 仅支持3字节UTF-8 | 1-3字节/字符 | 不推荐使用 |
| latin1 | ASCII字符集 | 1字节/字符 | 纯英文系统 |

### 排序规则选择

| 排序规则 | 说明 | 特点 |
|---------|------|------|
| utf8mb4_unicode_ci | **推荐**，Unicode标准 | 准确排序，不区分大小写 |
| utf8mb4_general_ci | 通用排序 | 性能较好，排序略有差异 |
| utf8mb4_bin | 二进制排序 | 区分大小写，精确匹配 |

---

## JSON字段设计

### JSON字段使用场景
```sql
CREATE TABLE user_settings (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    
    -- ✅ 推荐：配置信息
    preferences JSON COMMENT '用户偏好设置',
    
    -- ✅ 推荐：扩展属性
    extra_data JSON COMMENT '扩展数据',
    
    -- ✅ 推荐：动态表单数据
    form_data JSON COMMENT '表单数据',
    
    KEY idx_user_id (user_id)
);
```

### JSON操作示例
```sql
-- 插入JSON数据
INSERT INTO user_settings (id, user_id, preferences) VALUES 
('1', 'user001', JSON_OBJECT(
    'theme', 'dark',
    'language', 'zh-CN',
    'notifications', JSON_OBJECT(
        'email', true,
        'sms', false
    )
));

-- 查询JSON数据
SELECT 
    id,
    user_id,
    JSON_EXTRACT(preferences, '$.theme') as theme,
    JSON_EXTRACT(preferences, '$.language') as language,
    JSON_EXTRACT(preferences, '$.notifications.email') as email_notify
FROM user_settings 
WHERE user_id = 'user001';

-- 使用->操作符（MySQL 5.7+）
SELECT 
    id,
    preferences->'$.theme' as theme,
    preferences->'$.notifications.email' as email_notify
FROM user_settings;

-- 更新JSON数据
UPDATE user_settings 
SET preferences = JSON_SET(preferences, '$.theme', 'light')
WHERE user_id = 'user001';

-- JSON条件查询
SELECT * FROM user_settings 
WHERE JSON_EXTRACT(preferences, '$.theme') = 'dark';

-- 创建JSON字段的虚拟列索引
ALTER TABLE user_settings 
ADD COLUMN theme_virtual VARCHAR(20) 
GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(preferences, '$.theme'))) VIRTUAL,
ADD INDEX idx_theme_virtual (theme_virtual);
```

### JSON字段最佳实践
```sql
-- ✅ 推荐：JSON字段验证
CREATE TABLE products (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    
    -- JSON字段添加约束
    attributes JSON COMMENT '产品属性',
    
    -- 添加JSON格式检查
    CONSTRAINT chk_attributes_format 
        CHECK (JSON_VALID(attributes))
);

-- ✅ 推荐：为常用JSON路径创建虚拟列
ALTER TABLE products
ADD COLUMN category_virtual VARCHAR(50) 
    GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(attributes, '$.category'))) VIRTUAL,
ADD INDEX idx_category_virtual (category_virtual);

-- ❌ 避免：在JSON字段中存储大量数据
-- ❌ 避免：频繁更新JSON字段的部分内容
-- ❌ 避免：在JSON字段上直接创建索引
```

---

## 表结构设计

### 基础表结构模板
```sql
-- JeecgBoot标准表结构
CREATE TABLE sys_example (
    -- 主键（必须）
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    
    -- 业务字段
    name VARCHAR(100) NOT NULL COMMENT '名称',
    code VARCHAR(50) NOT NULL COMMENT '编码',
    description TEXT COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    
    -- 扩展字段
    extra_data JSON COMMENT '扩展数据',
    
    -- 系统字段（继承JeecgEntity）
    created_by VARCHAR(32) COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(32) COMMENT '更新人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 逻辑删除（可选）
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：1-已删除，0-未删除',
    
    -- 索引
    UNIQUE KEY uk_code (code),
    KEY idx_status (status),
    KEY idx_created_time (created_time),
    KEY idx_is_deleted (is_deleted)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='示例表';
```

### 关联表设计
```sql
-- 一对多关系：用户-订单
CREATE TABLE users (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) COMMENT='用户表';

CREATE TABLE orders (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '订单ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    
    CONSTRAINT fk_orders_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT
) COMMENT='订单表';

-- 多对多关系：用户-角色
CREATE TABLE sys_users (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名'
) COMMENT='用户表';

CREATE TABLE sys_roles (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称'
) COMMENT='角色表';

CREATE TABLE sys_user_roles (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    
    CONSTRAINT fk_user_roles_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES sys_users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role_id 
        FOREIGN KEY (role_id) 
        REFERENCES sys_roles(id) 
        ON DELETE CASCADE
) COMMENT='用户角色关联表';
```

---

## 性能优化

### 查询优化
```sql
-- ✅ 推荐：使用LIMIT限制结果集
SELECT * FROM users WHERE status = 1 LIMIT 10;

-- ✅ 推荐：避免SELECT *，明确指定字段
SELECT id, username, email FROM users WHERE status = 1;

-- ✅ 推荐：使用索引覆盖
-- 索引：KEY idx_status_username (status, username)
SELECT username FROM users WHERE status = 1;  -- 无需回表

-- ✅ 推荐：合理使用JOIN
SELECT u.username, p.real_name 
FROM users u 
INNER JOIN user_profiles p ON u.id = p.user_id 
WHERE u.status = 1;

-- ❌ 避免：在WHERE条件中使用函数
-- 不推荐
SELECT * FROM users WHERE YEAR(created_time) = 2024;
-- 推荐
SELECT * FROM users WHERE created_time >= '2024-01-01' AND created_time < '2025-01-01';
```

### 分页优化
```sql
-- ❌ 传统分页（深分页性能差）
SELECT * FROM users ORDER BY created_time DESC LIMIT 10000, 10;

-- ✅ 推荐：游标分页
SELECT * FROM users 
WHERE created_time < '2024-01-01 10:00:00' 
ORDER BY created_time DESC 
LIMIT 10;

-- ✅ 推荐：延迟关联分页
SELECT u.* FROM users u
INNER JOIN (
    SELECT id FROM users ORDER BY created_time DESC LIMIT 10000, 10
) t ON u.id = t.id;
```

### 批量操作优化
```sql
-- ✅ 推荐：批量插入
INSERT INTO users (id, username, email) VALUES 
('1', 'user1', 'user1@example.com'),
('2', 'user2', 'user2@example.com'),
('3', 'user3', 'user3@example.com');

-- ✅ 推荐：批量更新
UPDATE users 
SET status = CASE 
    WHEN id = '1' THEN 1
    WHEN id = '2' THEN 0
    ELSE status 
END
WHERE id IN ('1', '2');

-- ✅ 推荐：使用ON DUPLICATE KEY UPDATE
INSERT INTO user_stats (user_id, login_count) VALUES ('1', 1)
ON DUPLICATE KEY UPDATE login_count = login_count + 1;
```

---

## 安全规范

### 数据安全
```sql
-- ✅ 推荐：敏感数据加密存储
CREATE TABLE user_secrets (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    
    -- 密码字段（应用层加密）
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    
    -- 敏感信息（数据库层加密）
    id_card_encrypted VARBINARY(255) COMMENT '身份证号（加密）',
    
    KEY idx_user_id (user_id)
);

-- ✅ 推荐：逻辑删除代替物理删除
ALTER TABLE users 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：1-已删除，0-未删除',
ADD INDEX idx_is_deleted (is_deleted);

-- 查询时过滤已删除数据
SELECT * FROM users WHERE is_deleted = 0;
```

### 权限控制
```sql
-- 创建专用数据库用户
CREATE USER 'jeecg_app'@'%' IDENTIFIED BY 'strong_password';

-- 授予必要权限
GRANT SELECT, INSERT, UPDATE, DELETE ON jeecg_boot.* TO 'jeecg_app'@'%';

-- 禁止DROP、ALTER等危险操作
-- REVOKE DROP, ALTER ON jeecg_boot.* FROM 'jeecg_app'@'%';
```

---

## 最佳实践

### 表设计检查清单
- [ ] 表名使用小写复数，下划线分隔
- [ ] 每个表都有主键（推荐VARCHAR(32)）
- [ ] 包含created_time和updated_time字段
- [ ] 重要字段添加NOT NULL约束
- [ ] 为外键字段创建索引
- [ ] 添加适当的业务索引
- [ ] 字符集使用utf8mb4
- [ ] 添加表和字段注释

### 索引设计检查清单
- [ ] WHERE条件字段有索引
- [ ] ORDER BY字段有索引
- [ ] JOIN连接字段有索引
- [ ] 复合索引遵循最左前缀原则
- [ ] 避免重复索引
- [ ] 单表索引数量控制在5个以内

### 性能优化检查清单
- [ ] 避免SELECT *
- [ ] 使用LIMIT限制结果集
- [ ] 合理使用批量操作
- [ ] 避免在WHERE条件中使用函数
- [ ] 大表考虑分区策略
- [ ] 定期分析表统计信息

### 安全规范检查清单
- [ ] 敏感数据加密存储
- [ ] 使用逻辑删除
- [ ] 数据库用户权限最小化
- [ ] 定期备份数据
- [ ] 监控异常访问

---

## 示例：完整表设计

```sql
-- 内容管理系统示例
CREATE TABLE cms_articles (
    -- 主键
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '文章ID',
    
    -- 基础信息
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    slug VARCHAR(200) NOT NULL COMMENT 'URL别名',
    summary TEXT COMMENT '文章摘要',
    content LONGTEXT NOT NULL COMMENT '文章内容',
    
    -- 分类和标签
    category_id VARCHAR(32) NOT NULL COMMENT '分类ID',
    tags JSON COMMENT '标签列表',
    
    -- 状态和属性
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-草稿，2-发布，3-下线',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    is_recommend TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐：1-是，0-否',
    
    -- 统计信息
    view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞次数',
    comment_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论次数',
    
    -- SEO信息
    seo_title VARCHAR(200) COMMENT 'SEO标题',
    seo_keywords VARCHAR(500) COMMENT 'SEO关键词',
    seo_description TEXT COMMENT 'SEO描述',
    
    -- 发布信息
    author_id VARCHAR(32) NOT NULL COMMENT '作者ID',
    published_time DATETIME COMMENT '发布时间',
    
    -- 扩展数据
    extra_data JSON COMMENT '扩展数据',
    
    -- 系统字段
    created_by VARCHAR(32) COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(32) COMMENT '更新人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：1-已删除，0-未删除',
    
    -- 索引
    UNIQUE KEY uk_slug (slug),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_author_id (author_id),
    KEY idx_published_time (published_time),
    KEY idx_is_top_status (is_top, status),
    KEY idx_is_recommend_status (is_recommend, status),
    KEY idx_created_time (created_time),
    KEY idx_is_deleted (is_deleted),
    
    -- 外键约束
    CONSTRAINT fk_articles_category_id 
        FOREIGN KEY (category_id) 
        REFERENCES cms_categories(id) 
        ON DELETE RESTRICT,
    CONSTRAINT fk_articles_author_id 
        FOREIGN KEY (author_id) 
        REFERENCES sys_users(id) 
        ON DELETE RESTRICT
        
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='文章表';

-- 为JSON字段创建虚拟列索引
ALTER TABLE cms_articles
ADD COLUMN tags_virtual TEXT 
    GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(tags, '$'))) VIRTUAL,
ADD FULLTEXT INDEX ft_tags_virtual (tags_virtual);
```

---

## 总结

本规范文档涵盖了MySQL 8.0+数据库设计的各个方面，结合JeecgBoot框架特点，提供了全面的设计指导。遵循这些规范可以：

1. **提高性能**：合理的索引设计和查询优化
2. **保证安全**：数据加密和权限控制
3. **便于维护**：统一的命名规范和结构设计
4. **支持扩展**：JSON字段和灵活的表结构
5. **确保一致性**：标准化的设计模式

建议在项目开发过程中严格遵循本规范，并根据具体业务需求进行适当调整。