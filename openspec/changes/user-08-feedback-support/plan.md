# Execution Plan

> **TDD 驱动**: 每个 Step 遵循 RED → GREEN → REFACTOR 循环

## Step 1: RED — 举报防重复校验

- Test file: `src/test/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImplTest.java`
- Assertion: 对同一 targetType+targetId 重复调用 createReport() 应抛出 JeecgBootException
- Expected failure: 当前无防重复逻辑，第二次调用会成功
- Verify: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceImplTest`

## Step 2: GREEN — 实现举报防重复

- Pass test from: Step 1
- Minimal code: 在 `createReport()` 开头查询 `reportMapper.selectOne()` 检查是否已存在相同 userId+targetType+targetId 的记录
- Verify: 同上测试命令

## Step 3: RED — 申诉次数限制

- Test file: 同上
- Assertion: 第 4 次对同一 targetId 调用 createAppeal() 应抛出 JeecgBootException
- Expected failure: 当前无次数限制逻辑
- Verify: 同上

## Step 4: GREEN — 实现申诉次数限制

- Pass test from: Step 3
- Minimal code: 在 `createAppeal()` 开头查询同一 userId+targetId 的已申诉次数，>=3 时拒绝
- Verify: 同上

## Step 5: RED — 帮助搜索

- Test file: 同上
- Assertion: searchHelpArticles("user1", "账号") 应返回包含关键词的文章列表
- Expected failure: 方法不存在
- Verify: 同上

## Step 6: GREEN — 实现帮助搜索

- Pass test from: Step 5
- Minimal code: 新增 `searchHelpArticles()` 方法，使用 LIKE 查询帮助文章数据源
- Verify: 同上

## Step 7: RED — 客服优先级路由（LV.15+）

- Test file: 同上
- Assertion: level=15 的用户应路由到 MANUAL_PRIORITY，level=14 应路由到 SMART_FIRST
- Expected failure: 当前阈值是 level>=5，level=14 会错误路由到 MANUAL_PRIORITY
- Verify: 同上

## Step 8: GREEN — 调整优先级阈值

- Pass test from: Step 7
- Minimal code: 将 `shouldRouteToManualPriority()` 中 `level >= 5` 改为 `level >= 15`
- Verify: 同上

## Step 9: RED — 客服会话 CRUD

- Test file: 同上
- Assertion: createServiceSession() 创建会话、listServiceSessions() 查询历史、rateService() 评分
- Expected failure: 方法和实体不存在
- Verify: 同上

## Step 10: GREEN — 实现客服会话管理

- Pass test from: Step 9
- Minimal code: 新增 `ContentCustomerServiceSession` 实体、Mapper、Service 方法
- Verify: 同上

## Step 11: RED — 更新日志

- Test file: 同上
- Assertion: getChangelog() 返回版本列表，包含版本号、日期、变更内容
- Expected failure: 方法不存在
- Verify: 同上

## Step 12: GREEN — 实现更新日志

- Pass test from: Step 11
- Minimal code: 新增 `getChangelog()` 方法返回静态/配置化的更新日志数据
- Verify: 同上

## Step 13: RED — 通知集成

- Test file: 同上
- Assertion: 举报处理/申诉审核完成后应触发通知
- Expected failure: 当前无通知调用
- Verify: 同上

## Step 14: GREEN — 实现通知触发

- Pass test from: Step 13
- Minimal code: 在 `handleReport()` 和 `handleAppeal()` 中调用通知服务
- Verify: 同上

## Step 15: 数据库迁移

- 执行 `content_customer_service_session` 表 DDL
- 验证表结构正确

## Step 16: 集成验证

- 运行全部单元测试
- 验证所有功能端到端正常
