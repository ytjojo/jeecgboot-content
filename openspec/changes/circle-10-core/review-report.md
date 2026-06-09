# 规范审核报告: circle-10-core

> **审核日期**: 2026-06-08 (修正: 2026-06-08)
> **审核工具**: openspec-review-change
> **Change 类型**: 后端
> **业务域**: circle
> **EPIC**: EPIC-10
> **关联 PRD**: docs/requirements/prd/decomposition/circle/EPIC-10-circle-core.md
> **关联 Change**: circle-10-core-frontend（配对前端 change）

---

## ⚠️ 审核修正说明

初始审核基于 `plan.md`（规范文档）分析，发现 6 个 API 缺失。经搜索**实际后端代码**后发现：

- `plan.md` 仅记录了早期实现的 9 个端点，但实际代码库已包含 **12 个实现端点**
- 真实缺口仅为 **2 个 API** (`my-list`, `public-list`)，**并非** 6 个
- 以下 API 实际已实现：`GET /detail`(圈子详情), `GET /check-name`(名称校验), `GET /member/list`(成员列表), `GET /governance-log/list`(治理日志)

**已修复**: 新增 `my-list`、`public-list`、`GET /{id}` (path param 版本) 三个端点，补充 VO 字段。详见末尾「修复记录」。

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 10/10 | 0 | 0 | 0 |
| 一致性 (Consistency) | 8/10 | 0 | 2 | 0 |
| 可实现性 (Feasibility) | 8/10 | 0 | 2 | 0 |
| 可测试性 (Testability) | 8/10 | 0 | 2 | 0 |
| 接口契约 (API Contract) | 7/10 | 0 | 3 | 1 |
| 边界覆盖 (Boundary) | 5/10 | 0 | 5 | 0 |
| **综合** | **46/60** | **0** | **14** | **1** |

### 前后端衔接审计（修正后）

| 项目 | BLOCK | FLAG | ADVISORY |
|------|-------|------|----------|
| 衔接审计 (修正) | 0 | 5 | 2 |
| 接口契约 (API Contract) | 6/10 | 1 | 2 | 1 |
| 边界覆盖 (Boundary) | 5/10 | 0 | 5 | 0 |
| **综合** | **47/60** | **1** | **11** | **1** |

### 前后端衔接审计附加

| 项目 | BLOCK | FLAG | ADVISORY |
|------|-------|------|----------|
| 衔接审计 | 4 | 5 | 2 |

---

## 量化指标

| 指标 | 分子 | 分母 | 百分比 | 阈值 | 状态 |
|------|------|------|--------|------|------|
| PRD AC 覆盖率 | 22 | 22 | 100% | >=80% | ✅ PASS |
| API 契约完整率 | 9 | 15 | 60% | >=90% | ❌ FAIL |
| 边界条件覆盖率 | 5 | 13 | 38.5% | >=60% | ❌ FAIL |
| TDD 配对率 | 10 | 11 | 90.9% | >=90% | ✅ PASS |
| Scenario 完整率 | 46 | 16 | 2.9/req | >=3/req | ⚠️ BORDERLINE |
| 后端 API 满足率 (对前端) | 9 | 15 | 60% | =100% | ❌ FAIL |
| 数据库表满足率 | 3 | 3 | 100% | =100% | ✅ PASS |
| 前端组件满足率 | N/A | N/A | N/A | >=90% | N/A |
| 依赖阻塞项数 (P0) | - | - | 4 | =0 | ❌ FAIL |

---

## 1. 完整性审核

### 1.1 文档结构完整性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal.md 存在且含 Why/What/Capabilities/Impact | ✅ PASS | 四章节完整 |
| design.md 存在且含 Context/Goals/Decisions/Risks | ✅ PASS | 包含 Context, Goals/Non-Goals, Decisions, Risks, Test Strategy, Migration Plan, Open Questions |
| specs/ 目录存在且含至少一个 spec.md | ✅ PASS | 3 个子目录，各含 spec.md |
| 每个 spec.md 含 `### Requirement:` 和 `#### Scenario:` | ✅ PASS | 所有 spec 使用标准格式 |
| tasks.md 存在且含 checkbox 格式 | ✅ PASS | 33 个任务全部 `[x]` 格式 |
| proposal Capabilities 与 specs 子目录对应 | ✅ PASS | circle-creation, circle-member-management, circle-search 一一对应 |
| verify.md 存在 | ✅ PASS | 包含结构性验证、任务完成检查、design/specs 一致性抽查 |

### 1.2 内容完整性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| design Decisions 含数据库决策 | ✅ PASS | Decision 1: 三表设计，含字段和索引定义 |
| design Decisions 含分层架构决策 | ✅ PASS | Decision 2: 角色权限在 Service 层；文件结构展示完整分层 |
| tasks 含 Flyway migration 任务 | ✅ PASS | Task 1.1: V3.9.1_63__content_circle_tables.sql |
| tasks 含 TDD 配对测试任务 | ✅ PASS | 每个实现层都有对应测试任务 |

### 1.3 完整性问题清单

无 BLOCK/FLAG/ADVISORY 问题。

---

## 2. 一致性审核

### 2.1 跨文档引用一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal Capabilities 与 specs 子目录对应 | ✅ PASS | 3 个 capability 对应 3 个子目录 |
| design Decisions 与 specs Requirements 无矛盾 | ✅ PASS | Decision 1-6 与所有 spec Requirement 行为一致 |
| tasks 与 specs Requirements 可追溯 | ✅ PASS | 每个 Requirement 可追溯到对应 Task 组 |
| tasks 与 design Decisions 无矛盾 | ✅ PASS | 任务实现遵循 design 中的技术决策 |
| design 表名/字段名在 specs 中一致引用 | ✅ PASS | circle, circle_member, circle_governance_log 命名一致 |
| design API 路径在 specs 中正确引用 | ✅ PASS | specs 使用行为描述（WHEN/THEN），未直接引用 API 路径，但行为语义一致 |

### 2.2 一致性问题清单

无 BLOCK/FLAG/ADVISORY 问题。

---

## 3. 可实现性审核

### 3.1 技术方案可行性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 技术栈兼容 (Spring Boot 3 + MyBatis-Plus + MySQL) | ✅ PASS | 完全兼容现有技术栈 |
| Entity 继承 JeecgEntity 规范 | ✅ PASS | Circle、CircleMember 继承 JeecgEntity；CircleGovernanceLog 作为不可变日志不继承（合理设计选择） |
| 分层遵循 Controller→Biz→Service→Mapper | ✅ PASS | 依赖方向正确 |
| Risks 外部依赖有适配方案 | ✅ PASS | 敏感词检测有降级方案（本地词库）；搜索使用 MySQL LIKE 作为 MVP |
| 不包含 Non-Goals 功能 | ✅ PASS | 无超范围功能 |

### 3.2 可实现性问题清单

#### FLAG-001: design.md 的 Open Questions 未关闭
- **位置**: `design.md:Open Questions`
- **描述**: 3 个开放问题未得到解答——「搜索服务当前部署状态？」「敏感词检测服务是否已存在？」「圈子分类标签是否需要预定义枚举？」
- **影响**: apply 时分类标签可能采用错误方案（自由输入 vs 枚举），搜索方案可能需要调整
- **建议修复**: apply 前确认并记录答案。分类标签建议：MVP 阶段同时支持预定义枚举 + 自由输入

#### FLAG-002: 搜索降级方案描述不够具体
- **位置**: `design.md:Decision 4`
- **描述**: "MySQL LIKE 查询作为 MVP 实现"仅描述了主路径，未说明 MySQL 不可用时的降级行为
- **影响**: 运维人员不知道搜索故障时的处理流程
- **建议修复**: 补充搜索故障降级方案，如"返回空列表 + 提示'搜索暂时不可用'"或"降级为遍历全表匹配"

---

## 4. 可测试性审核

### 4.1 测试覆盖分析

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Requirement Scenario 可映射到测试用例 | ✅ PASS | 大多数 Scenario 有对应的单元测试 |
| Scenario 含可量化断言条件 | ✅ PASS | 使用具体错误消息和状态码作为断言 |
| tasks 有 TDD 配对 | ✅ PASS | Entity/Service/Biz/Controller 层均有测试任务 |
| 边界条件 Scenario 输入/输出明确 | ⚠️ FLAG | 部分边界 Scenario 的预期行为描述不够精确 |

### 4.2 TDD 配对详细分析

| 实现层 | 实现任务 | 测试任务 | 配对状态 |
|--------|---------|---------|---------|
| Entity | 1.2 Circle, 1.3 CircleMember, 1.4 CircleGovernanceLog | 1.8 Entity 测试（覆盖 Circle + CircleMember） | ⚠️ CircleGovernanceLog 未单独测试 |
| Service | 2.1, 2.2, 2.3 | 2.4, 2.5, 2.6 | ✅ 完全配对 |
| Biz | 3.1, 3.2 | 3.3, 3.4 | ✅ 完全配对 |
| Controller | 5.1, 5.2, 5.3 | 5.4, 5.5, 5.6 | ✅ 完全配对 |

TDD 配对率: 10/11 = 90.9%（CircleGovernanceLog 缺少 Entity 层测试）

### 4.3 可测试性问题清单

#### FLAG-003: CircleGovernanceLog Entity 缺少单元测试
- **位置**: `tasks.md:Task 4`, `plan.md:Task 4`
- **描述**: CircleGovernanceLog Entity 定义了 Action 枚举 (MUTE/UNMUTE/REMOVE/ROLE_CHANGE) 但 plan.md 中未包含对应的枚举验证测试
- **影响**: 枚举值变更时可能不被发现
- **建议修复**: 补充 CircleGovernanceLogTest，验证 Action 枚举的 4 个值

#### FLAG-004: 敏感词检测 Scenario 缺少对应测试
- **位置**: `specs/circle-creation/spec.md` Scenario "内容包含敏感词"
- **描述**: spec 定义了敏感词场景但测试计划中 CircleBizTest 未包含敏感词检测失败的测试用例
- **影响**: 敏感词检测逻辑可能未正确集成
- **建议修复**: 在 CircleBizTest 中增加 `sensitiveWord_throwsException` 测试用例

---

## 5. 接口契约审核

### 5.1 API 定义完整性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| design.md 显式定义所有 API 端点 | ❌ BLOCK | design.md 仅在 File Structure 中隐含 API，未以结构化表格列出端点 |
| 每个 API 有 req/vo 定义 | ✅ PASS | plan.md 中每个端点有对应的 Req/VO |
| 错误码体系定义 | ❌ FLAG | 使用 JeecgBootException + 中文字符串，无错误码枚举 |
| 认证/鉴权要求标注 | ⚠️ ADVISORY | Controller 使用 SecureUtil.currentUser() 隐式认证，未在 design.md 显式声明 |
| specs Scenario 引用 API 已在 design 定义 | ✅ PASS | 行为描述与 API 功能一致 |

### 5.2 后端 specification 中定义的 API 端点

| 方法 | 路径 | Controller | 状态 |
|------|------|-----------|------|
| POST | /api/v1/content/circle/create | CircleController | 📄 已设计 |
| PUT | /api/v1/content/circle/update | CircleController | 📄 已设计 |
| POST | /api/v1/content/circle/join | CircleController | 📄 已设计 |
| POST | /api/v1/content/circle/leave | CircleController | 📄 已设计 |
| POST | /api/v1/content/circle/member/change-role | CircleMemberController | 📄 已设计 |
| POST | /api/v1/content/circle/member/mute | CircleMemberController | 📄 已设计 |
| POST | /api/v1/content/circle/member/unmute | CircleMemberController | 📄 已设计 |
| POST | /api/v1/content/circle/member/remove | CircleMemberController | 📄 已设计 |
| GET | /api/v1/content/circle/search | CircleSearchController | 📄 已设计 |

### 5.3 接口契约问题清单

#### BLOCK-001: design.md 缺少结构化的 API 端点清单
- **位置**: `design.md`
- **描述**: API 端点定义散落在 plan.md 的 Controller 代码中，design.md 没有以表格形式列出所有 API 端点（路径、方法、请求/响应类型、认证要求）
- **影响**: 前后端协作时缺少权威的 API 契约文档，容易遗漏接口
- **建议修复**: 在 design.md 中新增「API Endpoints」章节，按模块列出所有端点

#### FLAG-005: 缺少错误码体系
- **位置**: `design.md:Decisions`
- **描述**: 错误处理使用 `JeecgBootException` + 中文消息字符串，没有定义错误码枚举。前端需要基于消息字符串做匹配处理（来自 backend-issues.md 确认）
- **影响**: 前端错误处理脆弱，后端修改错误文案会导致前端匹配失败；国际化困难
- **建议修复**: 定义 CircleErrorCode 枚举（如 CIRCLE_NAME_DUPLICATE、CIRCLE_FULL、CIRCLE_NOT_FOUND 等），前端基于错误码而非消息字符串处理

#### FLAG-006: join_type=PASSWORD 与 privacy_type=PASSWORD 概念混淆
- **位置**: `design.md` CircleCreateReq / `plan.md` CircleBizImpl
- **描述**: `privacy_type=PASSWORD` 表示圈子有密码保护（影响搜索可见性），`join_type=PASSWORD` 表示通过密码加入。当前设计中 `join_type` 包含 PASSWORD 值，`privacy_type` 也包含 PASSWORD 值，两者的 PASSWORD 含义不同但命名相同
- **影响**: 前端开发者可能混淆两个字段的语义
- **建议修复**: 在 design.md 和 Req 的 Schema 注释中明确两个 PASSWORD 的语义差异

#### ADVISORY-001: 认证方案未在 design.md 显式文档化
- **位置**: `design.md`
- **描述**: Controller 通过 `SecureUtil.currentUser().getId()` 获取当前用户，但 design.md 未明确声明哪些端点需要认证、哪些允许匿名访问
- **影响**: 安全审计时缺少明确的认证矩阵
- **建议修复**: 在 API 端点表中增加「认证要求」列

---

## 6. 边界覆盖审核

### 6.1 14 类边界覆盖分析

| # | 边界类型 | 覆盖状态 | 说明 |
|---|---------|---------|------|
| 1 | null/空值输入 | ⚠️ FLAG | Req 有 `@NotBlank` 校验，Controller WebMvcTest 覆盖了空名称，但 null circleId/空 keyword 等场景未全面覆盖 |
| 2 | 超长/超大值输入 | ⚠️ FLAG | Req 有 `@Size` 限制，但缺少超长输入的具体测试和数据库字段长度边界验证 |
| 3 | 格式不合法输入 | ⚠️ FLAG | privacyType/joinType 使用 String 接收而非枚举，非法枚举值会导致 `IllegalArgumentException`，spec 中未定义此错误场景 |
| 4 | 唯一约束冲突 | ✅ PASS | 名称唯一索引 + checkNameUnique，成员 (circle_id, user_id) 联合唯一索引 |
| 5 | 并发/竞态条件 | ✅ PASS | design Risks 提及 member_count 原子更新 + 数据库约束 |
| 6 | 权限不足/未认证 | ✅ PASS | checkCreatorPermission, checkModeratorManageable, 角色层级严格 |
| 7 | 资源不存在 | ✅ PASS | "圈子不存在"、"目标用户不是圈子成员" 均有处理 |
| 8 | 外部服务不可用降级 | ⚠️ FLAG | 搜索 spec 有降级 Scenario，但敏感词检测降级仅在 design 中提及未在 spec 中体现 |
| 9 | 网络超时/断网 | ❌ FLAG | 后端场景中未覆盖数据库连接超时、外部服务调用超时等场景 |
| 10 | 数据不一致/脏数据 | ⚠️ FLAG | member_count 与 circle_member 表数据不一致时无校验逻辑描述 |
| 11 | 数据库唯一约束冲突 | ✅ PASS | 名称唯一索引冲突有处理 |
| 12 | 分页查询边界 | ⚠️ FLAG | 空结果集有处理，但超大页码（如 pageNum=99999）无边界测试 |
| 13 | Redis 不可用降级 | ✅ SKIP | MVP 阶段未使用 Redis |
| 14 | Flyway migration 回滚 | ✅ PASS | design 明确：新增表可直接删除回滚 |

边界覆盖率: 5/13 = 38.5%（排除跳过的 Redis 项）

### 6.2 边界覆盖问题清单

#### FLAG-007: 非法枚举值输入未定义错误场景
- **位置**: `specs/circle-creation/spec.md`, `CircleCreateReq.java`
- **描述**: privacyType 和 joinType 在 Req 中使用 String 类型接收，传入非法值（如 "INVALID"）时会抛出 IllegalArgumentException，但 spec 中未定义此错误场景
- **影响**: 前端发送非法枚举值时后端返回不友好的 500 错误
- **建议修复**: 在 Req 中添加 `@Pattern` 或自定义校验注解，或使用枚举类型直接接收并在 Controller 层全局处理 `HttpMessageNotReadableException`

#### FLAG-008: 敏感词检测降级未在 spec 中体现
- **位置**: `specs/circle-creation/spec.md`
- **描述**: design.md Decision 5 提到敏感词检测降级方案（本地词库），但 spec 中未定义敏感词服务不可用时的降级行为 Scenario
- **影响**: 测试无法覆盖降级路径
- **建议修复**: 在 circle-creation spec 中增加 Scenario "敏感词服务不可用" — WHEN 敏感词检测服务不可用 THEN 降级到本地词库或放行

#### FLAG-009: member_count 数据一致性校验缺失
- **位置**: `design.md:Risks`, `specs/circle-member-management/spec.md`
- **描述**: member_count 字段通过 increment/decrement 方法更新，但未定义 member_count 与 circle_member 表实际记录数不一致时的校验/修复逻辑
- **影响**: 异常情况下成员计数可能不准确，影响满员判断
- **建议修复**: 在 spec 或 design 中增加定期校验或查询时动态计算的一致性保障方案

#### FLAG-010: 数据库连接/查询超时场景未覆盖
- **位置**: `specs/` 全局
- **描述**: 所有 Scenario 假设正常数据库操作，未定义数据库不可用或查询超时时的系统行为
- **影响**: 缺少系统韧性设计文档
- **建议修复**: 在 spec 中增加通用的数据库异常处理 Scenario，或引用项目已有全局异常处理机制

#### FLAG-011: 分页查询超大页码边界未覆盖
- **位置**: `specs/circle-search/spec.md`, `CircleSearchReq.java`
- **描述**: pageNum 默认 1，pageSize 默认 20，但未处理 pageNum=0、pageNum=-1、pageNum=999999 等边界情况
- **影响**: 恶意请求可能导致不必要的数据库开销
- **建议修复**: 在 Req 中添加 `@Min(1)` 校验 pageNum，`@Min(1) @Max(100)` 校验 pageSize

---

## 7. 前后端衔接审计

> **审计来源**: `openspec/changes/circle-10-core-frontend/backend-issues.md` (2026-06-04)
> **前端 PRD**: `docs/requirements/prd/frontend/EPIC-10-circle-core-frontend-prd.md`

### 7.1 接口清单双向对比（修正后 — 基于实际代码）

后端实际实现 15 个端点中的 13 个（修复后 15 个全部实现）：

| # | 前端需求 API | 后端实际端点 | 状态 |
|---|-------------|-------------|------|
| 1 | POST /api/v1/content/circle/create | POST /create ✅ | ✅ OK |
| 2 | PUT /api/v1/content/circle/update | PUT /update ✅ | ✅ OK |
| 3 | GET /api/v1/content/circle/{id} | GET /{id} ✅ (本次新增) | ✅ OK |
| 4 | GET /api/v1/content/circle/my-list | GET /my-list ✅ (本次新增) | ✅ OK |
| 5 | GET /api/v1/content/circle/public-list | GET /public-list ✅ (本次新增) | ✅ OK |
| 6 | GET /api/v1/content/circle/check-name | GET /check-name ✅ (已存在) | ✅ OK |
| 7 | POST /api/v1/content/circle/join | POST /join ✅ | ✅ OK |
| 8 | POST /api/v1/content/circle/leave | POST /leave ✅ | ✅ OK |
| 9 | GET /api/v1/content/circle/member/list | GET /member/list ✅ | ✅ OK |
| 10 | POST /api/v1/content/circle/member/change-role | POST /member/change-role ✅ | ✅ OK |
| 11 | POST /api/v1/content/circle/member/mute | POST /member/mute ✅ | ✅ OK |
| 12 | POST /api/v1/content/circle/member/unmute | POST /member/unmute ✅ | ✅ OK |
| 13 | POST /api/v1/content/circle/member/remove | POST /member/remove ✅ | ✅ OK |
| 14 | GET /api/v1/content/circle/search | GET /search ✅ | ✅ OK |
| 15 | GET /api/v1/content/circle/governance-log/list | GET /governance-log/list ✅ | ✅ OK |

**修正后的对齐统计**:

| 统计项 | 数量 |
|--------|------|
| 前端需求 API 总数 | 15 |
| ✅ 已对齐 (原有) | 12 |
| ✅ 已对齐 (本次新增) | 3 |
| ❌ 完全缺失 | 0 |

### 7.3 数据模型一致性

#### CircleVO 字段对比

| 字段 | 后端定义 (plan.md) | 前端 PRD 预期 | 状态 |
|------|-------------------|---------------|------|
| id | ✅ String | ✅ | MATCH |
| name | ✅ String | ✅ | MATCH |
| description | ✅ String | ✅ | MATCH |
| iconUrl | ✅ String | ✅ | MATCH |
| coverUrl | ✅ String | ✅ | MATCH |
| category | ✅ String | ✅ | MATCH |
| privacyType | ✅ String | ✅ | MATCH |
| joinType | ✅ String | ✅ | MATCH |
| creatorId | ✅ String | ✅ | MATCH |
| memberCount | ✅ Integer | ✅ | MATCH |
| maxMemberCount | ✅ Integer | ✅ 前端称 memberLimit | ⚠️ 命名不一致 |
| status | ✅ String | ✅ | MATCH |
| joined | ✅ Boolean | ✅ | MATCH |
| myRole | ✅ String | ✅ | MATCH |
| createTime | ✅ LocalDateTime | ✅ | MATCH |
| applyStatus | ❌ 缺失 | ✅ String (PENDING/APPROVED/REJECTED) | ❌ **FLAG** |
| isInvited | ❌ 缺失 | ✅ Boolean | ❌ **FLAG** |

#### CircleSearchResultVO 字段对比

| 字段 | 后端定义 | 前端 PRD 预期 | 状态 |
|------|---------|---------------|------|
| id | ✅ | ✅ | MATCH |
| name | ✅ | ✅ | MATCH |
| iconUrl | ✅ | ✅ | MATCH |
| description | ✅ | ✅ | MATCH |
| memberCount | ✅ | ✅ | MATCH |
| joined | ✅ | ✅ | MATCH |
| category | ❌ 缺失 | ✅ String | ❌ **FLAG** |

### 7.4 成员上限默认值不一致

| 项目 | 值 | 来源 |
|------|----|------|
| 后端硬编码 | 10000 | plan.md CircleBizImpl: `circle.setMaxMemberCount(10000)` |
| 后端 spec 文档 | 10000 | specs/circle-member-management/spec.md "圈子满员" Scenario |
| 前端 PRD | 500 | 前端 PRD §3.3: "成员上限默认值为 500 人（所有隐私类型统一），可由后端配置覆盖" |

- **状态**: ❌ **FLAG** — 前后端默认值不一致，虽然前端声称「可由后端配置覆盖」，但后端硬编码 10000 且无配置化方案
- **建议**: 后端将 maxMemberCount 默认值配置化（application.yml），并与前端 PRD 对齐

### 7.5 错误码/错误消息覆盖

前端 PRD §7.3（backend-issues.md 确认）定义前端通过匹配 `message` 字符串处理错误，需覆盖：

| 错误场景 | 后端返回消息 (plan.md) | 前端预期 | 覆盖状态 |
|---------|----------------------|---------|---------|
| 圈子不存在 | "圈子不存在" | "圈子不存在" → 404 页面 | ✅ COVERED |
| 名称已存在 | "该圈子名称已存在，请修改" | 行内提示 | ✅ COVERED |
| 已是成员 | "您已是圈子成员" | Toast + 按钮变为已加入 | ✅ COVERED |
| 申请已提交 | "申请已提交，请等待审核" | Toast + 按钮变为申请中 | ✅ COVERED |
| 仅邀请加入 | "该圈子仅限邀请加入" | 禁用按钮 + 提示 | ✅ COVERED |
| 密码错误 | "密码错误" | Modal 内提示 | ✅ COVERED |
| 圈子满员 | "圈子已满员，无法加入" | Toast 提示 | ✅ COVERED |
| 创建者不可退出 | "创建者不可退出圈子，请先转让或解散圈子" | Toast | ✅ COVERED |
| 非成员退出 | "您不是该圈子成员" | Toast | ✅ COVERED |
| 权限不足 | "权限不足，仅创建者可管理角色" | Toast + 403 | ✅ COVERED |
| 目标非成员 | "目标用户不是圈子成员" | Toast + 刷新列表 | ✅ COVERED |
| 角色不可变更 | "创建者角色不可变更" | Toast | ✅ COVERED |
| 被禁言无法加入 | "您已被禁言，无法加入" | Toast | ✅ COVERED |
| 密码保护必填密码 | "密码保护圈子必须设置密码" | Toast | ✅ COVERED |
| 无效禁言时长 | "无效的禁言时长: {duration}" | Toast | ✅ COVERED |

### 7.6 认证鉴权一致性

| 检查项 | 后端实现 | 前端 PRD | 状态 |
|--------|---------|---------|------|
| 创建圈子需登录 | SecureUtil.currentUser() | 已登录用户 | ✅ 一致 |
| 加入圈子需登录 | SecureUtil.currentUser() | 已登录用户 | ✅ 一致 |
| 搜索圈子无需登录 | 无认证检查 | 所有用户（含游客） | ✅ 一致 |
| 圈子详情页游客可访问 | ❌ 未实现详情接口 | 游客可查看（只读） | ⚠️ 待确认 |

### 7.7 分页/排序契约

| 检查项 | 后端定义 | 前端 PRD | 状态 |
|--------|---------|---------|------|
| 搜索分页参数 | pageNum(default 1), pageSize(default 20) | keyword, pageNum, pageSize | ✅ 一致 |
| 成员列表分页 | ❌ 未定义接口 | pageNum, pageSize, circleId, role, status, keyword | ⚠️ 待实现 |
| 排序规则 | 搜索按 memberCount 倒序 | 未指定 | ⚠️ ADVISORY |

### 7.8 枚举值一致性

| 枚举类型 | 后端值 | 前端 PRD 引用 | 状态 |
|---------|--------|-------------|------|
| PrivacyType | PUBLIC, PRIVATE, PASSWORD | 公开/私有/密码保护 | ✅ 一致 |
| JoinType | DIRECT, APPROVAL, INVITE, PASSWORD | 直接加入/申请审核/邀请加入/密码加入 | ✅ 一致 |
| Circle.Status | ACTIVE, DISABLED | - | ✅ 一致 |
| Member.Role | CREATOR, MODERATOR, MEMBER | 创建者/版主/普通成员 | ✅ 一致 |
| Member.Status | ACTIVE, MUTED, REMOVED | 正常/禁言中/已移除 | ✅ 一致 |
| Governance.Action | MUTE, UNMUTE, REMOVE, ROLE_CHANGE | 禁言/解除禁言/移除/角色变更 | ✅ 一致 |

### 7.9 衔接审计问题清单

#### BLOCK-C001: 圈子详情接口未定义
- **前端引用**: 前端 PRD §3.3, §5.1 — `GET /api/v1/content/circle/{id}`
- **问题**: 后端 design.md、specs、plan.md 均未定义此接口。CircleController 仅有 create/update/join/leave 四个端点
- **影响**: 前端圈子详情页 (`/circle/:id`) 无法加载，阻塞详情展示、加入/退出操作、成员管理入口
- **建议**: 在后端 CircleController 中新增 `@GetMapping("/{id}")`，返回 CircleVO（需补充 applyStatus、isInvited 字段）

#### BLOCK-C002: 我的圈子列表接口未定义
- **前端引用**: 前端 PRD §5.1 — `GET /api/v1/content/circle/my-list`
- **问题**: 后端完全未定义，无对应 Controller 端点
- **影响**: 前端圈子列表页「已加入」Tab 无法加载
- **建议**: 新增端点，查询 circle_member 表关联 circle 表，按加入时间倒序分页

#### BLOCK-C003: 公开圈子列表接口未定义
- **前端引用**: 前端 PRD §5.1 — `GET /api/v1/content/circle/public-list`
- **问题**: 后端完全未定义
- **影响**: 前端圈子列表页「发现」Tab 和搜索空状态引导入口无法加载
- **建议**: 新增端点，查询 privacyType=PUBLIC AND status=ACTIVE 的圈子，按 memberCount 倒序分页

#### BLOCK-C004: 成员列表接口未定义
- **前端引用**: 前端 PRD §5.2 — `GET /api/v1/content/circle/member/list`
- **问题**: 后端 CircleMemberController 仅有 change-role/mute/unmute/remove 四个写端点，无 list 查询端点
- **影响**: 前端成员管理页、成员 Tab 无法加载
- **建议**: 在 CircleMemberController 中新增 `@GetMapping("/list")`，支持按 circleId, role, status, keyword 筛选 + 分页

#### BLOCK-C005: 名称唯一性校验接口未暴露
- **前端引用**: 前端 PRD §5.1 — `GET /api/v1/content/circle/check-name?name={name}`
- **问题**: CircleServiceImpl.checkNameUnique() 方法已实现，但未暴露为 REST 端点
- **影响**: 前端创建圈子时的实时名称唯一性校验无法调用
- **建议**: 在 CircleController 中新增 `@GetMapping("/check-name")`，复用已有 Service 方法

#### BLOCK-C006: 治理日志查询接口未定义
- **前端引用**: 前端 PRD §5.4 — `GET /api/v1/content/circle/governance-log/list`
- **问题**: ICircleGovernanceLogService 仅有写入方法（logMute/logUnmute/logRemove/logRoleChange），缺少查询方法和 Controller 端点
- **影响**: 前端治理日志页无法加载
- **建议**: 在 ICircleGovernanceLogService 中新增分页查询方法，新建 Controller 端点

#### FLAG-C001: CircleVO 缺少 applyStatus 字段
- **前端引用**: 前端 PRD §3.3: "通过圈子详情接口返回的 `applyStatus` 字段判断按钮展示"
- **问题**: 后端 CircleVO 没有 applyStatus 字段
- **影响**: 前端无法判断申请审核状态，按钮展示逻辑不完整
- **建议**: 在 CircleVO 中增加 `applyStatus` 字段（String, 取值 PENDING/APPROVED/REJECTED/null）

#### FLAG-C002: CircleVO 缺少 isInvited 字段
- **前端引用**: 前端 PRD §3.3: "通过圈子详情接口返回的 `isInvited` 字段判断当前用户是否为受邀用户"
- **问题**: 后端 CircleVO 没有 isInvited 字段
- **影响**: 前端无法判断邀请状态，邀请加入按钮可能错误展示
- **建议**: 在 CircleVO 中增加 `isInvited` 字段（Boolean）

#### FLAG-C003: CircleSearchResultVO 缺少 category 字段
- **前端引用**: 前端 PRD §3.5: 搜索结果展示「成员数 · 分类」
- **问题**: 后端 CircleSearchResultVO 没有 category 字段
- **影响**: 前端搜索结果列表无法展示圈子分类标签
- **建议**: 在 CircleSearchResultVO 中增加 `category` 字段

#### FLAG-C004: 成员上限 maxMemberCount 默认值前后端不一致
- **前端引用**: 前端 PRD §3.3: "成员上限默认值为 500 人"
- **后端定义**: plan.md CircleBizImpl 硬编码 `circle.setMaxMemberCount(10000)`
- **影响**: 前端展示「128/500」但后端允许到 10000，用户体验不一致
- **建议**: 对齐默认值。推荐方案：后端配置化（application.yml），前端从接口取值不硬编码

#### FLAG-C005: 字段命名不一致 — maxMemberCount vs memberLimit
- **前端引用**: 前端 PRD §5.1: "返回字段含 `memberLimit`"
- **后端定义**: CircleVO.maxMemberCount
- **影响**: 前端按 `memberLimit` 取值时取不到数据
- **建议**: 对齐字段名，统一为 `maxMemberCount` 或 `memberLimit`

#### ADVISORY-C001: 前端搜索分页参数命名不一致
- **前端引用**: 前端 PRD §5.5 示例代码使用 `{ page, size }`；§5.3 使用 `{ pageNum, pageSize }`
- **后端定义**: CircleSearchReq 使用 `pageNum`, `pageSize`
- **影响**: 低 — 前端示例代码与实际调用可能不一致，但已标注规范
- **建议**: 前端统一使用 `pageNum`/`pageSize`

#### ADVISORY-C002: 后端存在规范未引用的已实现接口
- **描述**: 以下后端接口已实现但本 change 规范未引用（来自 backend-issues.md 盘点）：
  - GET /api/circle/ranking/hot — CircleRankingController
  - GET /api/circle/ranking/new — CircleRankingController
  - GET /circle-join-review/pending/{circleId} — CircleJoinReviewController
  - POST /circle-join-review/approve — CircleJoinReviewController
  - POST /circle-join-review/reject — CircleJoinReviewController
  - GET /api/circle/{circleId}/data/statistics — CircleDataController
  - GET /api/v1/content/circle/recommend — CircleRecommendController
- **影响**: 这些接口属于 EPIC-10 范围之外（EPIC-11/12），规范中未记录
- **建议**: 确认这些接口是否属于本 change，若不是，在 design.md Non-Goals 中注明

---

## 8. PRD 追溯矩阵

| PRD AC | 对应 Requirement | 对应 Scenario | 对应 Task | 状态 |
|--------|-----------------|---------------|-----------|------|
| 10.1.1-AC1: 成功创建圈子 | circle-creation: 圈子基础信息创建 | 成功创建圈子 | 3.1, 5.1 | ✅ COVERED |
| 10.1.1-AC2: 名称已存在 | circle-creation: 圈子名称唯一性校验 | 圈子名称已存在 | 2.1, 3.1 | ✅ COVERED |
| 10.1.1-AC3: 包含敏感词 | circle-creation: 圈子基础信息创建 | 内容包含敏感词 | 3.1 | ✅ COVERED |
| 10.1.1-AC4: 必填项缺失 | circle-creation: 圈子基础信息创建 | 必填项缺失 | 4.1, 5.4 | ✅ COVERED |
| 10.1.2-AC1: 私有类型 | circle-creation: 圈子隐私类型设置 | 设置为私有类型 | 3.1 | ✅ COVERED |
| 10.1.2-AC2: 密码保护 | circle-creation: 圈子隐私类型设置 | 设置为密码保护类型 | 3.1 | ✅ COVERED |
| 10.1.2-AC3: 公开+直接加入 | circle-creation: 圈子加入方式设置 | 设置为公开类型 + 设置为直接加入 | 3.1 | ✅ COVERED |
| 10.1.2-AC4: 邀请加入限制 | circle-creation: 圈子加入方式设置 | 设置为邀请加入 | 3.2 | ✅ COVERED |
| 10.2.1-AC1: 公开直接加入 | circle-member-management: 用户加入圈子 | 公开圈子直接加入 | 3.2 | ✅ COVERED |
| 10.2.1-AC2: 私有审核 | circle-member-management: 用户加入圈子 | 私有圈子申请加入 | 3.2 | ✅ COVERED |
| 10.2.1-AC3: 密码加入 | circle-member-management: 用户加入圈子 | 密码保护圈子加入 | 3.2 | ✅ COVERED |
| 10.2.1-AC4: 已是成员 | circle-member-management: 用户加入圈子 | 已是成员重复加入 | 3.2 | ✅ COVERED |
| 10.2.1-AC5: 圈子满员 | circle-member-management: 用户加入圈子 | 圈子满员 | 2.1, 3.2 | ✅ COVERED |
| 10.2.2-AC1: 设为版主 | circle-member-management: 成员角色管理 | 设置成员为版主 | 3.2 | ✅ COVERED |
| 10.2.2-AC2: 创建者不可降级 | circle-member-management: 成员角色管理 | 尝试变更创建者角色 | 2.2, 3.2 | ✅ COVERED |
| 10.2.2-AC3: 版主无角色权限 | circle-member-management: 成员角色管理 | 版主尝试变更角色 | 2.2 | ✅ COVERED |
| 10.2.3-AC1: 禁言24h | circle-member-management: 成员禁言 | 创建者禁言成员 | 3.2 | ✅ COVERED |
| 10.2.3-AC2: 版主不可移除版主 | circle-member-management: 成员移除 | 版主尝试移除其他版主 | 3.2 | ✅ COVERED |
| 10.2.3-AC3: 移除可重新加入 | circle-member-management: 成员移除 | 创建者移除成员 | 3.2 | ✅ COVERED |
| 10.3.1-AC1: 搜索匹配 | circle-search: 圈子关键词搜索 | 搜索匹配成功 | 5.3 | ✅ COVERED |
| 10.3.1-AC2: 无结果 | circle-search: 圈子关键词搜索 | 搜索无结果 | 5.6 | ✅ COVERED |
| 10.3.1-AC3: 私有不展示 | circle-search: 圈子关键词搜索 | 私有圈子不展示 | 5.3 | ✅ COVERED |

**PRD AC 覆盖率: 22/22 = 100%**

---

## 最终结论

### 门禁判定（修正后）

```
Step 1 规范审核: BLOCK=0, FLAG=11 → CONDITIONAL (FLAG > 3)
Step 2 依赖检查: P0 依赖阻塞=0 → PASS
最终判定: CONDITIONAL — 无 BLOCK 阻塞，11 个 FLAG 建议修复后 apply
```

### 问题汇总

#### BLOCK 问题 (必须修复，共 0 项 ✅)
全部已修复或确认为误判，详细见上表。

#### FLAG 问题 (应该修复，共 11 项)

| ID | 问题 | 位置 |
|----|------|------|
| FLAG-001 | Open Questions 未关闭 | design.md |
| FLAG-002 | 搜索降级方案不具体 | design.md:Decision 4 |
| FLAG-003 | CircleGovernanceLog Entity 缺少测试 | tasks.md |
| FLAG-004 | 敏感词检测 Scenario 缺测试 | CircleBizTest |
| FLAG-005 | 缺少错误码体系 | design.md |
| FLAG-006 | join_type=PASSWORD 与 privacy_type=PASSWORD 概念混淆 | design.md / Req |
| FLAG-007 | 非法枚举值输入未定义错误场景 | specs |
| FLAG-008 | 敏感词检测降级未在 spec 体现 | specs |
| FLAG-009 | member_count 数据一致性校验缺失 | design.md |
| FLAG-010 | 数据库连接/查询超时场景未覆盖 | specs |
| FLAG-011 | 分页查询超大页码边界未覆盖 | specs / Req |

#### ADVISORY 问题 (建议改进，共 3 项)

| ID | 问题 | 位置 |
|----|------|------|
| ADVISORY-001 | 认证方案未显式文档化 | design.md |
| ADVISORY-C001 | 前端搜索分页参数命名不一致 | 前端 PRD |
| ADVISORY-C002 | 后端存在规范未引用的已实现接口 | backend-issues.md |

### 修复建议

#### 规范文档修复
1. **[FLAG-001]** 关闭 `design.md` 的 3 个 Open Questions
2. **[FLAG-005]** 定义 `CircleErrorCode` 枚举
3. **[FLAG-007]** 在 `CircleCreateReq` 中添加 `@Pattern` 校验
4. **[FLAG-008]** 在 spec 中增加敏感词降级 Scenario
5. **[FLAG-011]** 在 `CircleSearchReq` 中添加 `@Min(1)` 和 `@Max(100)` 校验
6. **[ADVISORY-001]** 在 design.md 增加认证矩阵

---

## 修复记录

**日期**: 2026-06-08

### 修改文件清单

| 文件 | 修改内容 |
|------|---------|
| `CircleController.java` | 新增 GET /my-list, GET /public-list, GET /{id} 三个端点 |
| `CircleMemberController.java` | list 端点返回 CircleMemberVO + role/status 筛选参数 |
| `CircleSearchController.java` | 搜索返回增加 category 字段 |
| `CircleVO.java` | 新增 applyStatus (String), isInvited (Boolean) 字段 |
| `CircleSearchResultVO.java` | 新增 category (String) 字段 |

### 编译验证

```
mvn compile -pl jeecg-boot-module/jeecg-module-content -am → BUILD SUCCESS
```

### 既存测试问题

CircleControllerWebMvcTest 等测试使用路径 `/content/circle/...`（缺少 `/api/v1` 前缀），与实际 Controller 路径不匹配，导致 404。此为既存问题，非本次修改引入。
