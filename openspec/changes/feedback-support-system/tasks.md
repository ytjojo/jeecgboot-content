## 1. 举报防重复与提交增强

- [x] 1.1 在 `ContentUserSupportServiceImpl.createReport()` 中新增防重复校验：查询同一 userId + targetType + targetId 是否已存在举报记录
- [x] 1.2 编写举报防重复测试用例：首次举报成功、重复举报被拒绝
- [x] 1.3 在举报提交成功后返回举报编号的确认信息

## 2. 申诉次数限制

- [x] 2.1 在 `ContentUserSupportServiceImpl.createAppeal()` 中新增次数校验：查询同一 userId + targetId 的已申诉次数，超过 3 次拒绝
- [x] 2.2 编写申诉次数限制测试用例：第 1/2/3 次成功、第 4 次被拒绝

## 3. 帮助中心搜索

- [x] 3.1 新增 `ContentHelpSearchResultVO` 搜索结果 VO
- [x] 3.2 在 `IContentUserSupportService` 中新增 `searchHelpArticles(String userId, String keyword)` 方法
- [x] 3.3 在 `ContentUserSupportServiceImpl` 中实现基于 LIKE 的帮助文章搜索
- [x] 3.4 编写帮助搜索测试用例：有结果返回、无结果返回空列表

## 4. 客服会话管理

- [x] 4.1 新增 `ContentCustomerServiceSession` 实体（id, userId, sessionType, status, rating, startTime, endTime）
- [x] 4.2 新增 `ContentCustomerServiceSessionMapper` 及 XML
- [x] 4.3 新增 `ContentServiceSessionVO` 和 `ContentServiceSessionPageVO`
- [x] 4.4 新增 `ContentServiceSessionQueryReq` 会话查询请求
- [x] 4.5 在 `IContentUserSupportService` 中新增 `listServiceSessions()`、`createServiceSession()`、`rateService()` 方法
- [x] 4.6 在 `ContentUserSupportServiceImpl` 中实现会话 CRUD 和评分逻辑
- [x] 4.7 编写客服会话测试用例：创建会话、查看历史、评分、30 天过期提示

## 5. 客服优先级调整

- [x] 5.1 将 `ContentUserSupportServiceImpl.shouldRouteToManualPriority()` 中的 `level >= 5` 改为 `level >= 15`
- [x] 5.2 编写优先级路由测试用例：level>=15 走优先通道、level<15 走普通通道、治理状态走申诉专线

## 6. 更新日志

- [x] 6.1 在 `IContentUserSupportService` 中新增 `getChangelog(String userId)` 方法
- [x] 6.2 在 `ContentUserSupportServiceImpl` 中实现更新日志数据返回（版本号、日期、新增/优化/修复列表）
- [x] 6.3 编写更新日志测试用例：按时间倒序、版本详情完整

## 7. 通知集成

- [x] 7.1 在举报处理完成后调用通知服务发送处理结果通知
- [x] 7.2 在申诉审核完成后调用通知服务发送审核结果通知
- [x] 7.3 编写通知触发测试用例：举报处理通知、申诉通过通知、申诉驳回通知

## 8. 数据库迁移

- [x] 8.1 编写 `content_customer_service_session` 表 DDL 脚本
- [x] 8.2 执行数据库迁移并验证表结构

## 9. 验证

- [x] 9.1 运行全部单元测试确保通过
- [x] 9.2 验证举报防重复、申诉次数限制、帮助搜索、客服会话功能正常
