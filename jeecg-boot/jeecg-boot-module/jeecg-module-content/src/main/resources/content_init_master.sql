-- =============================================
-- 内容社区系统主初始化脚本（简化版）
-- 统一管理所有内容模块的数据库初始化
-- 作者: JeecgBoot团队
-- 创建时间: 2024-01-15
-- 版本: 1.0.0
-- =============================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 开始事务
START TRANSACTION;

-- =============================================
-- 按依赖顺序执行各个初始化脚本
-- =============================================

-- 1. 执行核心内容模块初始化（基础表，其他模块依赖）
SOURCE content_module_init.sql;

-- 2. 执行社区模块初始化（依赖核心内容表）
SOURCE content_community_init.sql;

-- 3. 执行问答模块初始化（依赖核心内容表和社区表）
SOURCE content_qa_init.sql;

-- 4. 执行互动模块初始化（依赖核心内容表和媒体文件表）
SOURCE content_Interaction_init.sql;

-- 5. 执行频道模块初始化（独立模块，可选）
SOURCE content_channel_init.sql;

-- 6. 执行搜索模块初始化（依赖contents、user_profile_extension、communities表）
SOURCE content_search_init.sql;

-- 提交事务
COMMIT;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 初始化完成提示
-- =============================================
SELECT '内容社区系统数据库初始化完成！' as '执行结果';

-- =============================================
-- 脚本说明
-- =============================================
/*
使用说明：
1. 确保MySQL版本 >= 5.7
2. 确保有足够的数据库权限
3. 建议在测试环境先执行验证
4. 执行前请备份现有数据

执行顺序：
1. content_module_init.sql - 核心内容模块（基础表）
2. content_community_init.sql - 社区模块
3. content_qa_init.sql - 问答模块  
4. content_Interaction_init.sql - 互动模块
5. content_channel_init.sql - 频道模块
6. content_search_init.sql - 搜索模块

依赖关系：
- 所有模块都依赖 content_module_init.sql 中的基础表
- content_search_init.sql 依赖 contents、user_profile_extension、communities 表
- content_Interaction_init.sql 依赖 media_files 表

注意事项：
- 如果某个脚本执行失败，整个事务会回滚
- 执行前确保所有子脚本文件存在于同一目录
*/