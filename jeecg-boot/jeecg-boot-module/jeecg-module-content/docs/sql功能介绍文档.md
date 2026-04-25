


          
# SQL功能介绍文档

## 概述

本文档详细介绍SQL中的常用功能，包括触发器、视图、存储过程、函数、索引、约束等核心特性。这些功能在企业级应用开发中具有重要作用，特别是在JeecgBoot这样的企业级平台中，合理使用这些功能可以显著提升系统的性能和可维护性。

## 1. 触发器 (Triggers)

### 1.1 定义与作用
触发器是一种特殊的存储过程，它会在特定的数据库事件发生时自动执行，无需显式调用。

**主要作用：**
- **数据完整性约束**：自动维护数据的一致性
- **审计日志**：记录数据变更历史
- **业务规则执行**：自动执行复杂的业务逻辑
- **数据同步**：在相关表之间同步数据

### 1.2 MySQL触发器示例

#### 审计日志触发器
```sql
-- 创建审计日志表
CREATE TABLE user_audit_log (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32),
    operation_type VARCHAR(10),
    old_data JSON,
    new_data JSON,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户更新触发器
DELIMITER $$
CREATE TRIGGER tr_users_after_update
    AFTER UPDATE ON users
    FOR EACH ROW
BEGIN
    INSERT INTO user_audit_log (
        id, user_id, operation_type, old_data, new_data
    ) VALUES (
        UUID(),
        NEW.id,
        'UPDATE',
        JSON_OBJECT('username', OLD.username, 'email', OLD.email),
        JSON_OBJECT('username', NEW.username, 'email', NEW.email)
    );
END$$
DELIMITER ;
```

#### 业务规则触发器
```sql
-- 创建订单状态变更触发器
DELIMITER $$
CREATE TRIGGER tr_orders_status_change
    AFTER UPDATE ON orders
    FOR EACH ROW
BEGIN
    -- 当订单状态变为已支付时，更新库存
    IF NEW.status = 2 AND OLD.status = 1 THEN
        UPDATE products 
        SET stock = stock - NEW.quantity 
        WHERE id = NEW.product_id;
    END IF;
END$$
DELIMITER ;
```

## 2. 视图 (Views)

### 2.1 定义与作用
视图是基于SQL查询结果的虚拟表，它不存储实际数据，而是动态生成查询结果。

**主要作用：**
- **简化复杂查询**：将复杂的JOIN操作封装成简单的表
- **数据安全**：隐藏敏感字段，只暴露必要信息
- **数据抽象**：为不同用户提供不同的数据视角
- **向后兼容**：在表结构变更时保持接口稳定

### 2.2 MySQL视图示例

#### 数据安全视图
```sql
-- 创建用户信息视图（隐藏敏感信息）
CREATE VIEW v_user_info AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.status,
    p.real_name,
    p.phone,
    u.created_time
FROM users u
LEFT JOIN user_profiles p ON u.id = p.user_id
WHERE u.is_deleted = 0;
```

#### 统计分析视图
```sql
-- 创建订单统计视图
CREATE VIEW v_order_statistics AS
SELECT 
    u.id as user_id,
    u.username,
    COUNT(o.id) as order_count,
    COALESCE(SUM(o.amount), 0) as total_amount,
    MAX(o.created_time) as last_order_time
FROM users u
LEFT JOIN orders o ON u.id = o.user_id AND o.status = 2
WHERE u.is_deleted = 0
GROUP BY u.id, u.username;
```

#### 可更新视图
```sql
-- 可更新视图示例
CREATE VIEW v_active_users AS
SELECT id, username, email, status
FROM users 
WHERE status = 1 AND is_deleted = 0
WITH CHECK OPTION;  -- 确保更新后的数据仍满足视图条件

-- 通过视图更新数据
UPDATE v_active_users SET email = 'new@example.com' WHERE id = '123';
```

## 3. 存储过程 (Stored Procedures)

### 3.1 定义与特点
存储过程是预编译的SQL语句集合，可以接受参数并返回结果，支持复杂的业务逻辑处理。

### 3.2 存储过程示例

#### 用户注册存储过程
```sql
DELIMITER $$
CREATE PROCEDURE sp_user_register(
    IN p_username VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255),
    OUT p_user_id VARCHAR(32),
    OUT p_result_code INT,
    OUT p_result_msg VARCHAR(255)
)
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = -1;
        SET p_result_msg = '注册失败，系统异常';
    END;
    
    START TRANSACTION;
    
    -- 检查用户名是否存在
    SELECT COUNT(*) INTO v_count FROM users WHERE username = p_username;
    IF v_count > 0 THEN
        SET p_result_code = 1001;
        SET p_result_msg = '用户名已存在';
        ROLLBACK;
    ELSE
        -- 生成用户ID
        SET p_user_id = REPLACE(UUID(), '-', '');
        
        -- 插入用户数据
        INSERT INTO users (id, username, email, password_hash, created_time)
        VALUES (p_user_id, p_username, p_email, p_password, NOW());
        
        SET p_result_code = 0;
        SET p_result_msg = '注册成功';
        COMMIT;
    END IF;
END$$
DELIMITER ;

-- 调用存储过程
CALL sp_user_register('testuser', 'test@example.com', 'hashedpassword', @user_id, @code, @msg);
SELECT @user_id, @code, @msg;
```

## 4. 函数 (Functions)

### 4.1 用户自定义函数
函数用于封装可重用的计算逻辑，返回单一值。

#### 计算函数示例
```sql
-- 创建计算年龄的函数
DELIMITER $$
CREATE FUNCTION fn_calculate_age(birth_date DATE)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE age INT;
    SET age = TIMESTAMPDIFF(YEAR, birth_date, CURDATE());
    RETURN age;
END$$
DELIMITER ;

-- 使用函数
SELECT username, birth_date, fn_calculate_age(birth_date) as age 
FROM user_profiles;
```

#### 格式化函数示例
```sql
-- 创建格式化金额的函数
DELIMITER $$
CREATE FUNCTION fn_format_amount(amount DECIMAL(15,2))
RETURNS VARCHAR(50)
READS SQL DATA
DETERMINISTIC
BEGIN
    RETURN CONCAT('¥', FORMAT(amount, 2));
END$$
DELIMITER ;

-- 使用函数
SELECT order_no, fn_format_amount(amount) as formatted_amount FROM orders;
```

## 5. 索引 (Indexes)

### 5.1 索引类型与应用

#### 基础索引类型
```sql
-- 普通索引
CREATE INDEX idx_users_email ON users(email);

-- 唯一索引
CREATE UNIQUE INDEX uk_users_username ON users(username);

-- 复合索引
CREATE INDEX idx_orders_user_status_time ON orders(user_id, status, created_time);

-- 前缀索引（适用于长字符串）
CREATE INDEX idx_articles_title_prefix ON articles(title(20));
```

#### 特殊索引类型
```sql
-- 全文索引（MySQL 5.6+）
CREATE FULLTEXT INDEX ft_articles_content ON articles(title, content);

-- 使用全文索引搜索
SELECT * FROM articles 
WHERE MATCH(title, content) AGAINST('MySQL 数据库' IN NATURAL LANGUAGE MODE);

-- 空间索引（用于地理位置数据）
CREATE SPATIAL INDEX sp_locations_point ON locations(coordinates);
```

## 6. 约束 (Constraints)

### 6.1 约束类型

#### 主键约束
```sql
-- 单字段主键
CREATE TABLE users (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(50) NOT NULL
);

-- 复合主键
CREATE TABLE user_roles (
    user_id VARCHAR(32),
    role_id VARCHAR(32),
    PRIMARY KEY (user_id, role_id)
);
```

#### 外键约束
```sql
CREATE TABLE orders (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32),
    FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);
```

#### 其他约束
```sql
-- 唯一约束
CREATE TABLE users (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE
);

-- 检查约束
CREATE TABLE products (
    id VARCHAR(32) PRIMARY KEY,
    price DECIMAL(10,2) CHECK (price > 0),
    status ENUM('active', 'inactive') CHECK (status IN ('active', 'inactive'))
);
```

## 7. 分区 (Partitioning)

### 7.1 表分区策略

#### 范围分区
```sql
-- 按日期范围分区
CREATE TABLE order_history (
    id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2),
    created_time DATETIME NOT NULL,
    PRIMARY KEY (id, created_time)
) PARTITION BY RANGE (YEAR(created_time)) (
    PARTITION p2022 VALUES LESS THAN (2023),
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

#### 哈希分区
```sql
-- 按哈希分区
CREATE TABLE user_logs (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    action VARCHAR(50),
    created_time DATETIME
) PARTITION BY HASH(CRC32(user_id)) PARTITIONS 4;
```

## 8. 事务控制

### 8.1 事务处理示例
```sql
-- 转账事务示例
START TRANSACTION;

-- 检查账户余额
SELECT balance INTO @from_balance FROM accounts WHERE user_id = 'user1' FOR UPDATE;
SELECT balance INTO @to_balance FROM accounts WHERE user_id = 'user2' FOR UPDATE;

-- 验证余额是否足够
IF @from_balance >= 100 THEN
    -- 扣款
    UPDATE accounts SET balance = balance - 100 WHERE user_id = 'user1';
    -- 入账
    UPDATE accounts SET balance = balance + 100 WHERE user_id = 'user2';
    
    -- 记录转账日志
    INSERT INTO transfer_logs (id, from_user, to_user, amount, created_time)
    VALUES (UUID(), 'user1', 'user2', 100, NOW());
    
    COMMIT;
    SELECT '转账成功' as result;
ELSE
    ROLLBACK;
    SELECT '余额不足' as result;
END IF;
```

## 9. 窗口函数 (Window Functions) - MySQL 8.0+

### 9.1 排名函数
```sql
-- 排名函数
SELECT 
    username,
    score,
    ROW_NUMBER() OVER (ORDER BY score DESC) as row_num,
    RANK() OVER (ORDER BY score DESC) as rank_num,
    DENSE_RANK() OVER (ORDER BY score DESC) as dense_rank_num
FROM user_scores;

-- 分组排名
SELECT 
    department,
    username,
    salary,
    ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) as dept_rank
FROM employees;
```

### 9.2 聚合函数
```sql
-- 累计统计
SELECT 
    order_date,
    daily_amount,
    SUM(daily_amount) OVER (ORDER BY order_date) as cumulative_amount,
    AVG(daily_amount) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) as moving_avg_7days
FROM daily_sales;

-- 前后行比较
SELECT 
    order_date,
    daily_amount,
    LAG(daily_amount, 1) OVER (ORDER BY order_date) as prev_day_amount,
    LEAD(daily_amount, 1) OVER (ORDER BY order_date) as next_day_amount
FROM daily_sales;
```

## 10. 公用表表达式 (CTE) - MySQL 8.0+

### 10.1 递归CTE
```sql
-- 递归CTE：组织架构查询
WITH RECURSIVE org_hierarchy AS (
    -- 锚点：顶级部门
    SELECT id, name, parent_id, 0 as level, name as path
    FROM departments 
    WHERE parent_id IS NULL
    
    UNION ALL
    
    -- 递归部分
    SELECT d.id, d.name, d.parent_id, oh.level + 1, 
           CONCAT(oh.path, ' -> ', d.name) as path
    FROM departments d
    INNER JOIN org_hierarchy oh ON d.parent_id = oh.id
)
SELECT * FROM org_hierarchy ORDER BY level, name;
```

### 10.2 非递归CTE
```sql
-- 非递归CTE：复杂查询简化
WITH user_stats AS (
    SELECT 
        user_id,
        COUNT(*) as order_count,
        SUM(amount) as total_amount
    FROM orders 
    WHERE status = 2 
    GROUP BY user_id
),
user_categories AS (
    SELECT 
        user_id,
        CASE 
            WHEN total_amount >= 10000 THEN 'VIP'
            WHEN total_amount >= 5000 THEN 'Gold'
            WHEN total_amount >= 1000 THEN 'Silver'
            ELSE 'Bronze'
        END as category
    FROM user_stats
)
SELECT 
    u.username,
    us.order_count,
    us.total_amount,
    uc.category
FROM users u
JOIN user_stats us ON u.id = us.user_id
JOIN user_categories uc ON u.id = uc.user_id;
```

## 11. JSON函数 (MySQL 5.7+)

### 11.1 JSON数据操作
```sql
-- JSON数据插入
INSERT INTO user_settings (user_id, preferences) VALUES 
('user1', JSON_OBJECT(
    'theme', 'dark',
    'language', 'zh-CN',
    'notifications', JSON_OBJECT('email', true, 'sms', false)
));

-- JSON查询和更新
SELECT 
    user_id,
    JSON_EXTRACT(preferences, '$.theme') as theme,
    JSON_EXTRACT(preferences, '$.notifications.email') as email_notify
FROM user_settings;

-- 使用JSON路径更新
UPDATE user_settings 
SET preferences = JSON_SET(preferences, '$.theme', 'light', '$.language', 'en-US')
WHERE user_id = 'user1';

-- JSON数组操作
UPDATE user_settings 
SET preferences = JSON_ARRAY_APPEND(preferences, '$.tags', 'new_tag')
WHERE user_id = 'user1';

-- JSON条件查询
SELECT * FROM products 
WHERE JSON_EXTRACT(attributes, '$.category') = 'electronics'
AND JSON_EXTRACT(attributes, '$.price') > 1000;
```

## 12. 高级功能

### 12.1 序列 (Sequences)
```sql
-- MySQL 8.0+ 不直接支持序列，但可以用AUTO_INCREMENT
-- PostgreSQL 支持序列
CREATE SEQUENCE user_seq START 1000 INCREMENT 1;

-- 使用序列
INSERT INTO users (id, username) VALUES (NEXTVAL('user_seq'), 'john');
```

### 12.2 临时表 (Temporary Tables)
```sql
-- 会话级临时表
CREATE TEMPORARY TABLE temp_sales (
    product_id VARCHAR(32),
    sales_amount DECIMAL(10,2),
    sale_date DATE
);

-- 使用临时表进行复杂计算
INSERT INTO temp_sales 
SELECT product_id, SUM(amount), CURDATE() 
FROM orders 
WHERE order_date = CURDATE()
GROUP BY product_id;
```

### 12.3 游标 (Cursors)
```sql
DELIMITER //
CREATE PROCEDURE process_users()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE user_id VARCHAR(32);
    DECLARE user_cursor CURSOR FOR 
        SELECT id FROM users WHERE status = 'active';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN user_cursor;
    
    read_loop: LOOP
        FETCH user_cursor INTO user_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 处理每个用户
        UPDATE users SET last_processed = NOW() WHERE id = user_id;
    END LOOP;
    
    CLOSE user_cursor;
END //
DELIMITER ;
```

### 12.4 数据类型转换
```sql
-- 数据类型转换
SELECT 
    CAST('123' AS SIGNED) as int_value,
    CONVERT('2023-12-01', DATE) as date_value,
    CAST(price AS CHAR) as price_string
FROM products;

-- 条件转换
SELECT 
    id,
    CASE 
        WHEN status = 1 THEN '激活'
        WHEN status = 0 THEN '禁用'
        ELSE '未知'
    END as status_text
FROM users;
```

### 12.5 批量操作
```sql
-- 批量插入
INSERT INTO users (id, username, email) VALUES
    ('1', 'user1', 'user1@example.com'),
    ('2', 'user2', 'user2@example.com'),
    ('3', 'user3', 'user3@example.com');

-- 从查询结果批量插入
INSERT INTO user_backup (id, username, email)
SELECT id, username, email FROM users WHERE status = 'active';

-- 使用CASE WHEN批量更新
UPDATE users 
SET status = CASE 
    WHEN id IN ('1', '2', '3') THEN 'active'
    WHEN id IN ('4', '5', '6') THEN 'inactive'
    ELSE status
END
WHERE id IN ('1', '2', '3', '4', '5', '6');
```

## 13. 数据库元数据查询

### 13.1 表结构查询
```sql
-- 查询表信息
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    ENGINE,
    TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'jeecg-boot';

-- 查询字段信息
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'jeecg-boot' AND TABLE_NAME = 'sys_user';
```

## 14. 性能优化

### 14.1 执行计划分析
```sql
-- 查看执行计划
EXPLAIN SELECT * FROM users WHERE username = 'admin';

-- 详细执行计划
EXPLAIN FORMAT=JSON SELECT * FROM users u 
JOIN user_roles ur ON u.id = ur.user_id;
```

### 14.2 查询优化提示
```sql
-- 强制使用索引
SELECT * FROM users USE INDEX (idx_username) WHERE username = 'admin';

-- 忽略索引
SELECT * FROM users IGNORE INDEX (idx_username) WHERE username = 'admin';
```

## 15. 在JeecgBoot项目中的应用建议

### 15.1 约束使用建议
- **主键约束**：所有表都应该有主键，推荐使用VARCHAR(32)存储雪花算法ID
- **外键约束**：在开发环境使用，生产环境根据性能需求决定
- **唯一约束**：用于业务唯一性保证，如用户名、邮箱等

### 15.2 临时表使用场景
- 复杂报表数据预处理
- 批量数据导入时的中间处理
- 大数据量统计分析的中间结果存储

### 15.3 递归查询应用
- 组织架构树形结构查询
- 菜单权限树形结构查询
- 分类目录层级查询

### 15.4 批量操作优化
- 数据导入时使用批量插入提高性能
- 状态批量更新时使用CASE WHEN语句
- 避免在循环中执行单条SQL

### 15.5 性能优化建议
- 合理使用索引，避免过度索引
- 对于大表考虑分区策略
- 使用视图简化复杂查询
- 在适当场景使用存储过程提升性能

## 16. 总结

这些SQL功能各有其特定的使用场景：

- **触发器**：适用于数据完整性维护、审计日志、自动化业务规则
- **视图**：适用于数据安全、查询简化、向后兼容
- **存储过程**：适用于复杂业务逻辑、事务处理、性能优化
- **函数**：适用于数据转换、计算、格式化
- **索引**：适用于查询性能优化
- **约束**：适用于数据完整性保证
- **分区**：适用于大表性能优化、数据管理
- **窗口函数**：适用于分析查询、排名统计
- **CTE**：适用于复杂查询简化、递归查询
- **JSON函数**：适用于半结构化数据处理

在JeecgBoot项目中，建议根据具体业务需求合理使用这些功能，特别是要注意性能影响和维护复杂度。合理的SQL功能使用可以显著提升系统的性能、可维护性和数据安全性。
        