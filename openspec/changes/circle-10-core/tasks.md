## 1. 数据库与 Entity 层

- [x] 1.1 创建 Flyway SQL 迁移脚本，包含 circle、circle_member、circle_governance_log 三张表及索引
- [x] 1.2 创建 Circle Entity，包含枚举字段（privacy_type、join_type、status）
- [x] 1.3 创建 CircleMember Entity，包含枚举字段（role、status）
- [x] 1.4 创建 CircleGovernanceLog Entity
- [x] 1.5 创建 CircleMapper 接口及 XML
- [x] 1.6 创建 CircleMemberMapper 接口及 XML
- [x] 1.7 创建 CircleGovernanceLogMapper 接口及 XML
- [x] 1.8 编写 Entity 单元测试（枚举值验证）

## 2. Service 层

- [x] 2.1 创建 CircleService 接口及实现，包含圈子 CRUD 逻辑
- [x] 2.2 创建 CircleMemberService 接口及实现，包含成员状态管理、禁言到期检查
- [x] 2.3 创建 CircleGovernanceLogService 接口及实现，包含日志记录和查询
- [x] 2.4 编写 CircleService 单元测试
- [x] 2.5 编写 CircleMemberService 单元测试
- [x] 2.6 编写 CircleGovernanceLogService 单元测试

## 3. Biz 层（圈子创建编排）

- [x] 3.1 创建 CircleBiz，编排圈子创建流程（名称唯一性校验 + 敏感词检测 + 创建）
- [x] 3.2 创建 CircleMemberBiz，编排成员加入流程（密码验证 + 满员检查 + 黑名单检查 + 加入）
- [x] 3.3 编写 CircleBiz 单元测试
- [x] 3.4 编写 CircleMemberBiz 单元测试

## 4. Req/VO/DTO 层

- [x] 4.1 创建 CircleCreateReq、CircleUpdateReq、CircleSearchReq 请求对象
- [x] 4.2 创建 CircleJoinReq、CircleMemberUpdateReq 请求对象
- [x] 4.3 创建 CircleVO、CircleMemberVO、CircleSearchResultVO 响应对象
- [x] 4.4 创建 CircleDTO、CircleMemberDTO 传输对象

## 5. Controller 层

- [x] 5.1 创建 CircleController，实现圈子 CRUD API
- [x] 5.2 创建 CircleMemberController，实现成员管理 API（加入/退出/角色变更/禁言/移除）
- [x] 5.3 创建 CircleSearchController，实现搜索 API
- [x] 5.4 编写 CircleController WebMvcTest
- [x] 5.5 编写 CircleMemberController WebMvcTest
- [x] 5.6 编写 CircleSearchController WebMvcTest

## 6. 验证

- [x] 6.1 运行所有单元测试，确保通过
- [x] 6.2 运行 Flyway 迁移，验证数据库表创建正确
- [x] 6.3 验证圈子创建完整流程（创建 → 设置隐私 → 设置加入方式）
- [x] 6.4 验证成员管理完整流程（加入 → 角色变更 → 禁言 → 解禁 → 移除）
- [x] 6.5 验证搜索功能（关键词搜索 → 结果展示 → 降级处理）

## 7. 审核后补充 (2026-06-08 review follow-up)

> 来源: `/opsx:review` → `review-report.md`
> 目的: 修复规范审核发现的 FLAG 问题，补齐边界校验和测试缺口

### 7.1 输入校验加固

- [x] 7.1.1 CircleCreateReq 添加隐私类型/加入方式的枚举校验（`@Pattern` 或自定义 validator），非法值返回 400 而非 500
- [x] 7.1.2 CircleSearchReq 添加 `@Min(1) pageNum` 和 `@Min(1) @Max(100) pageSize` 分页参数校验
- [x] 7.1.3 CircleMemberController `/list` 的 pageNum/pageSize 添加 `@Min/@Max` 校验

### 7.2 测试缺口补充

- [x] 7.2.1 CircleGovernanceLog Entity 单元测试 — 验证 Action 枚举 (MUTE/UNMUTE/REMOVE/ROLE_CHANGE)
- [ ] 7.2.2 CircleBizTest 补充敏感词检测失败场景测试 — **延期**: 敏感词服务未在 MVP 实现中集成，待 EPIC-11 补充
- [x] 7.2.3 修复既存 WebMvcTest 路径问题 — 测试路径从 `/content/circle/...` 改为 `/api/v1/content/circle/...`
- [ ] 7.2.4 CircleControllerWebMvcTest 补充 my-list/public-list/{id} 端点测试 — **延期**: 需重构测试以 mock ICircleService/ICircleMemberService

### 7.3 错误处理与降级

- [x] 7.3.1 CircleSearchController 搜索异常时返回明确错误消息"搜索暂时不可用"而非静默空列表
- [x] 7.3.2 CircleController/GlobalExceptionHandler 数据库超时/连接异常时返回友好错误提示 — 已由 JeecgBootExceptionHandler 全局覆盖

### 7.4 配置与数据模型对齐

- [x] 7.4.1 maxMemberCount 默认值配置化 — 从 `application.yml` 读取 `circle.max-member-count`，默认值对齐前端 PRD（500 人）
- [x] 7.4.2 前后端分页参数命名对齐 — 确认前端使用 `pageNum`/`pageSize`，member list 已改为 `pageNum`/`pageSize`

### 7.5 规范文档同步

- [x] 7.5.1 design.md 新增 API Endpoints 章节（15 个端点 + 认证矩阵 + 错误消息表）
- [x] 7.5.2 design.md 关闭 3 个 Open Questions
- [x] 7.5.3 circle-creation/spec.md 补充非法枚举值 + 敏感词降级边界场景
- [x] 7.5.4 circle-member-management/spec.md 补充成员计数一致性 Requirement
- [x] 7.5.5 circle-search/spec.md 补充分页边界处理 Requirement
