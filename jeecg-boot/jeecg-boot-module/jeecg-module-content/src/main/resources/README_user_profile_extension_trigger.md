# 用户档案扩展表自动同步触发器

## 功能说明

当向 `sys_user` 表插入新用户记录时，自动在 `user_profile_extension` 表中创建对应的用户档案扩展记录。

## 文件说明

- `user_profile_extension_trigger_mysql.sql` - MySQL版本的触发器和存储过程
- `user_profile_extension_trigger_postgresql.sql` - PostgreSQL版本的触发器和存储过程

## 安装步骤

### MySQL版本

1. 确保 `user_profile_extension` 表已存在
2. 执行 `user_profile_extension_trigger_mysql.sql` 脚本
3. 验证触发器创建成功：
   ```sql
   SHOW TRIGGERS LIKE 'sys_user';
   ```

### PostgreSQL版本

1. 确保 `user_profile_extension` 表已存在
2. 启用UUID扩展（如果需要）：
   ```sql
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   ```
3. 执行 `user_profile_extension_trigger_postgresql.sql` 脚本
4. 验证触发器创建成功：
   ```sql
   SELECT * FROM information_schema.triggers WHERE trigger_name = 'tr_sys_user_after_insert';
   ```

## 工作原理

1. **触发器监听**：当向 `sys_user` 表执行 INSERT 操作时，触发器自动执行
2. **存储过程调用**：触发器调用存储过程 `sp_insert_user_profile_extension`
3. **数据同步**：存储过程在 `user_profile_extension` 表中创建对应记录
4. **重复检查**：存储过程会检查是否已存在相同 `id` 的记录，避免重复插入（注意：现在检查的是 `id` 字段，不再是 `user_id`）
5. **日志记录**：操作会记录到系统日志表中

## 默认字段值

新创建的用户档案扩展记录包含以下默认值：

- `id`：直接使用 `sys_user.id` 作为主键（一对一关系）
- `nickname`：优先使用 `realname`，否则使用 `username`
- `avatar`：继承自 `sys_user.avatar`
- `bio`：空字符串（个人简介）
- `website`：空字符串（个人网站）
- `tags`：空JSON数组 `[]`（用户标签）
- `interests`：空JSON数组 `[]`（兴趣爱好）
- `social_links`：空JSON对象 `{}`（社交链接）
- `privacy_settings`：空JSON对象 `{}`（隐私设置）
- `notification_settings`：空JSON对象 `{}`（通知设置）
- `ext_data`：空JSON对象 `{}`（扩展数据）
- `del_flag`：0（正常状态）
- `last_active_time`：当前时间（最后活跃时间）

## 测试方法

可以通过向 `sys_user` 表插入测试数据来验证触发器功能：

```sql
-- MySQL测试
INSERT INTO sys_user (
    id, username, realname, password, salt, avatar, birthday, sex, email, phone, 
    org_code, status, del_flag, third_id, third_type, activiti_sync, work_no, 
    post, telephone, create_by, create_time, update_by, update_time, user_identity, 
    depart_ids, rel_tenant_ids, client_id
) VALUES (
    REPLACE(UUID(), '-', ''),
    'test_trigger_user',
    '触发器测试用户',
    'e10adc3949ba59abbe56e057f20f883e',
    'RCGTeGiH',
    '',
    NULL,
    1,
    'test@example.com',
    '13800138000',
    'A01',
    1,
    0,
    NULL,
    NULL,
    1,
    'TEST001',
    '',
    '',
    'admin',
    NOW(),
    'admin',
    NOW(),
    1,
    '',
    '',
    ''
);
```

插入后检查 `user_profile_extension` 表是否自动创建了对应记录。

## 卸载方法

### MySQL版本
```sql
-- 删除触发器
DROP TRIGGER IF EXISTS tr_sys_user_after_insert;

-- 删除存储过程
DROP PROCEDURE IF EXISTS sp_insert_user_profile_extension;
```

### PostgreSQL版本
```sql
-- 删除触发器
DROP TRIGGER IF EXISTS tr_sys_user_after_insert ON sys_user;

-- 删除触发器函数
DROP FUNCTION IF EXISTS fn_sys_user_after_insert();

-- 删除存储过程
DROP FUNCTION IF EXISTS sp_insert_user_profile_extension(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP);
```

## 注意事项

1. **表结构变更**：`user_profile_extension` 表的主键 `id` 直接对应 `sys_user.id`，不再使用单独的 `user_id` 字段
2. **一对一关系**：每个系统用户对应唯一的档案扩展记录，通过主键建立直接关联
3. **数据一致性**：触发器在事务中执行，确保数据一致性
4. **性能影响**：触发器会增加插入操作的执行时间，但影响很小
5. **错误处理**：存储过程包含异常处理，不会影响主表的插入操作
6. **日志记录**：所有操作都会记录到系统日志中，便于追踪（日志类型为操作日志）
7. **重复插入**：存储过程会检查记录是否已存在，避免重复插入
8. **JSON字段支持**：需要MySQL 5.7.8+或PostgreSQL 9.4+版本支持JSON数据类型

## 维护建议

1. 定期检查触发器是否正常工作
2. 监控系统日志中的相关记录
3. 在数据库升级前备份触发器和存储过程
4. 根据业务需求调整默认字段值
5. 定期检查 `user_profile_extension` 表与 `sys_user` 表的数据一致性
6. 监控JSON字段的存储大小，避免过大的JSON数据影响性能

## 版本兼容性

- **MySQL**：需要 5.7.8 或更高版本（支持JSON数据类型和相关函数）
- **PostgreSQL**：需要 9.4 或更高版本（支持JSONB数据类型）

## 数据迁移说明

如果从旧版本的表结构升级，需要执行以下迁移步骤：

1. 备份现有数据
2. 更新表结构（移除 `user_id` 字段，调整主键）
3. 重新创建触发器和存储过程
4. 验证数据完整性