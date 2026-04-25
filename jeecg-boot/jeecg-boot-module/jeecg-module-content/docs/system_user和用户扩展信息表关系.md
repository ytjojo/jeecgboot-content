## 项目用户表结构梳理完成

经过详细分析，我已经完全梳理清楚了项目中的用户表结构：

### 1. 核心用户表 (sys_user)
- **表名**: `sys_user`
- **位置**: 系统核心表，在 <mcfile name="jeecgboot-mysql-5.7.sql" path="/Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/db/jeecgboot-mysql-5.7.sql"></mcfile> 中定义
- **字段**: 包含用户基础信息如 `id`、`username`、`realname`、`password`、`email`、`phone` 等
- **实体类**: <mcfile name="SysUser.java" path="/Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/entity/SysUser.java"></mcfile>

### 2. 用户扩展信息表 (user_profile_extension)
- **表名**: `user_profile_extension`
- **位置**: 内容模块表，在 <mcfile name="content_community_init.sql" path="/Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/content_community_init.sql"></mcfile> 中定义
- **关联关系**: 通过 `user_id` 字段关联 `sys_user` 表的 `id` 字段
- **实体类**: <mcfile name="UserProfileExtension.java" path="/Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/entity/UserProfileExtension.java"></mcfile>
- **扩展字段**: 包含社区功能相关的扩展信息，如昵称、个人简介、关注数、粉丝数、积分、等级、认证状态、VIP状态等

### 3. 用户关联表
项目还包含多个用户关联表，用于存储用户的角色、部门、职位等信息：
- `sys_user_role` - 用户角色关联表
- `sys_user_depart` - 用户部门关联表  
- `sys_user_position` - 用户职位关联表
- `sys_user_tenant` - 用户租户关联表
- `sys_user_agent` - 用户代理关联表

### 4. 表关系总结
- **sys_user**: 系统核心用户表，存储用户基础信息和认证信息
- **user_profile_extension**: 用户资料扩展表，存储社区功能相关的用户扩展信息
- **关联方式**: 一对一关系，通过 `user_id` 字段关联
- **业务场景**: `sys_user` 用于系统认证和基础管理，`user_profile_extension` 用于社区功能和用户画像

这种设计符合单一职责原则，将系统核心用户信息与业务扩展信息分离，便于维护和扩展。
        