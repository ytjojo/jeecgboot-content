# OpenSpec Review Report: user-04-social-subscription-frontend

> **Change**: user-04-social-subscription-frontend
> **Type**: Frontend change
> **Domain**: user | **Epic**: EPIC-04
> **Backend pairing**: user-04-social-subscription
> **Review date**: 2026-06-06
> **Schema**: use-tdd-plan

---

## 1. 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 8.5/10 | 0 | 2 | 1 |
| 一致性 (Consistency) | 7.5/10 | 1 | 2 | 2 |
| 可实现性 (Feasibility) | 9.0/10 | 0 | 1 | 1 |
| 可测试性 (Testability) | 8.0/10 | 0 | 1 | 2 |
| 接口契约 (API Contract) | 6.5/10 | 1 | 3 | 2 |
| 边界覆盖 (Boundary) | 8.0/10 | 0 | 2 | 2 |

**总计**: BLOCK=2, FLAG=11, ADVISORY=10

---

## 2. 量化指标

| 指标 | 数值 | 说明 |
|------|------|------|
| PRD AC 覆盖率 | 100% (14/14) | 所有功能列表项均有 spec 对应 |
| API 契约完整率 | ~58% (18/31) | 前端 design.md 定义 31 端点，backend spec 明确定义约 18 个的输入输出约束 |
| 边界覆盖率 | ~70% | 10 类边界中覆盖 7 类 |
| TDD 配对率 | 100% | 所有 spec requirements 均有 scenarios 配对 |
| Tasks 完成率 | 95.9% (93/97) | 4 项验收任务待完成 |

---

## 3. 各维度详细审核结果

### 3.1 完整性 (Completeness) -- 8.5/10

#### PASS
- **文档结构完整**: proposal.md (Why/What/Success/Non-Goals/Capabilities/Impact) + design.md (Context/Goals/Decisions/Risks/API/File Structure/Test Strategy/Migration) + 3 个 spec 文件 + tasks.md + plan.md，全部具备
- **Spec 覆盖全部 PRD 功能点**: 3 个 capability spec 覆盖 14 个 PRD 功能
  - `user-follow-system` spec: 关注/取消关注、特别关注、分组管理、关注列表、特别关注列表、关注推荐、批量管理、关注数上限
  - `content-subscription` spec: 订阅、暂停/恢复、通知配置、订阅广场、订阅源详情、订阅统一管理
  - `social-feed` spec: 关注流、订阅流、动态卡片、关注按钮、特别关注按钮、订阅按钮、用户卡片、批量操作栏、信息流状态管理
- **Proposal 的 Success Criteria 可量化**: 有明确数值目标（关注流<2s、操作<500ms 等）
- **Design 的 Test Strategy 覆盖单元/集成/E2E/性能四层测试**

#### FLAG
- **[F1] 缺少 gap-analysis.md**: change 目录中未发现 gap-analysis.md 文档。对于前后端配对的 change，建议记录后端 API 尚未就绪时的 mock 策略和前端降级方案
- **[F2] Plan.md 体积过大 (91KB)**: plan.md 文件 91000 字节，97 个任务点。单文件过大不利于增量更新和 subagent 分发，建议按模块拆分

#### ADVISORY
- **[A1] Verification tasks 未完成**: tasks.md 中 15.3（部署文档）、15.4（功能验收）、15.5（性能验收）、15.6（兼容性验收）4 项任务标记为未完成 `[ ]`，完成率 95.9%

---

### 3.2 一致性 (Consistency) -- 7.5/10

#### PASS
- **Proposal ↔ Design Goals/Non-Goals 完全对齐**: 两处 Non-Goals 列表一致（付费订阅、ML 推荐、拉黑语义、路径迁移、国际化）
- **Design Decisions ↔ Spec Requirements 一致**: 读扩散模式在 design.md Decision #2 和 social-feed spec 中均有描述且逻辑一致
- **PRD 功能列表 ↔ Spec Requirements 一一对应**: 14 个功能均有对应 spec scenario

#### BLOCK
- **[B1] API 路径前缀不一致**: 前端 PRD 使用 `/api/v1/` 前缀（如 `/api/v1/feed/following`、`/api/v1/user/recommend`），但后端 design.md 使用 `/content/user/relation` 和 `/content/user/subscription` 前缀。前后端对接时将产生路径不匹配。**必须在 apply 前明确统一的 API 路径前缀**

#### FLAG
- **[F3] 推荐接口路径不一致**: 前端 PRD 3.7 节定义推荐接口为 `GET /api/v1/user/recommend`，后端 design.md API 表定义为 `GET /content/user/relation/recommendations`。路径和命名风格均不同
- **[F4] 关注流接口路径不一致**: 前端 PRD 3.6 节定义为 `GET /api/v1/feed/following`，后端 design.md 定义为 `GET /content/user/relation/feed`

#### ADVISORY
- **[A2] 分页策略表述差异**: 前端 PRD 3.7 节推荐接口使用"基于游标分页，返回 `hasMore` + `nextCursor`"，后端 spec 使用标准 page/size 分页。需确认最终采用哪种分页策略
- **[A3] 动态类型枚举值未统一定义**: 前端 PRD 使用"发布/点赞/收藏"中文描述，后端 spec 使用"publish/like/favorite"英文枚举。建议在 spec 中显式定义枚举值列表

---

### 3.3 可实现性 (Feasibility) -- 9.0/10

#### PASS
- **技术栈明确**: Vue 3 + TypeScript + Ant Design Vue 4 + Pinia + Vue Router + Vite + Axios，全部为项目已集成技术
- **现有组件复用**: design.md 明确列出可复用组件（Table、Modal、Form、Description、Upload、Page、CardList、Button、Icon、Loading、Dropdown）和 Hooks（useTable、useForm、useModal、useMessage、usePermission）
- **架构决策合理**: 读扩散关注流、Pinia store 分离、后端排序特别关注置顶、断点系统响应式适配，均有合理的替代方案分析
- **文件结构清晰**: design.md File Structure 部分完整定义了 views/components/stores/api/hooks/styles 目录结构

#### FLAG
- **[F5] 订阅源目录表前置依赖未解决**: design.md Open Question #2 指出"订阅广场的分类浏览和搜索功能依赖统一的订阅源目录表数据模型"，且要求"在本 PRD 落地前，必须先完成后端数据模型设计评审"。后端 design.md Decision #4 提到新增 `content_subscription_source` 表，但未确认该表是否已创建。若未创建，订阅广场功能无法对接

#### ADVISORY
- **[A4] 虚拟滚动实现方案未指定**: design.md 提到"实现虚拟滚动（长列表优化）"作为性能优化任务，但未指定具体实现方案（如使用 vue-virtual-scroller 还是自研）。建议在 design 中明确选型

---

### 3.4 可测试性 (Testability) -- 8.0/10

#### PASS
- **Spec scenarios 可量化可验证**: 所有 spec requirements 均配有 WHEN/THEN 格式的 scenario，可直接映射为测试用例
- **Design Test Strategy 覆盖四层**: 单元测试（组件 + store）、集成测试（页面）、E2E 测试（完整流程）、性能测试（加载时间、操作响应、长列表、批量操作）
- **PRD 验收标准可执行**: 5.1-5.10 共 10 类验收标准，均为可检查的 checkbox 项
- **TDD 配对率 100%**: 每个 spec requirement 至少有 2 个 scenarios，平均 4-5 个

#### FLAG
- **[F6] 性能测试阈值来源不一致**: design.md Test Strategy 中关注流首次加载目标 <2s，但 PRD 5.10 也标注 <2s。而 design.md Decision #2 提到"单次聚合查询时间预算 2s"，这意味着前端 2s 可能包含网络延迟 + 后端 2s，实际前端加载时间可能超过 2s。建议明确"前端首次加载 <2s"是否包含后端聚合时间

#### ADVISORY
- **[A5] E2E 测试方案未明确**: design.md 列出 E2E 测试场景但未指定框架（Playwright/Cypress/其他）。建议在 design 中明确
- **[A6] Mock 策略缺失**: 未说明在后端 API 未就绪时如何进行前端测试（MSW/手动 mock/其他）

---

### 3.5 接口契约 (API Contract) -- 6.5/10

#### PASS
- **Design.md API Endpoints 表格完整**: 列出 31 个端点（17 个关系 + 14 个订阅），含方法、说明、对应 Spec 引用
- **请求/响应格式定义**: 明确 POST 使用 application/json、@RequestBody 传递参数、userId 通过 @RequestParam 传递、响应使用 Result<T> 包装
- **Backend spec 输入校验充分**: 每个 scenario 都有 invalid values 场景（null/empty/over-length/duplicate 等）

#### BLOCK
- **[B2] API 路径前缀未统一**: 见 [B1]。前端 PRD 使用 `/api/v1/` 路径，后端 design 使用 `/content/user/` 路径。这是阻塞性问题，前后端无法对接

#### FLAG
- **[F7] 全局通知默认配置接口路径不一致**: 前端 PRD 3.10 节引用 `GET /api/v1/subscribe/notification/global-default`，但后端 design.md API 表中无此端点。后端仅列出 `/content/user/subscription/notification/preference` (GET)。全局默认配置的获取路径需要确认
- **[F8] VO/Req 字段定义缺失**: design.md API 表仅列出端点和说明，未定义具体的 Req body 字段和 VO 返回字段。前端开发需要明确的字段定义才能对接
- **[F9] 批量操作上限未在后端 spec 明确**: 前端 PRD 假设单次批量操作最多 100 条，但后端 spec 仅提到"over-limit target count"会被拒绝，未给出具体上限数值

#### ADVISORY
- **[A7] 错误码体系未定义**: 前端 PRD 提到多种错误场景（网络超时、服务端 500、目标用户已注销、已被拉黑等），但未定义统一的错误码体系。建议在 API 契约中定义错误码枚举
- **[A8] 关注数上限 5000 的接口返回**: 前端 spec user-follow-system 提到"关注数已达上限（5000）"的 Toast 提示，但未说明后端如何返回此状态（错误码？特定 HTTP 状态码？Result 中的字段？）

---

### 3.6 边界覆盖 (Boundary) -- 8.0/10

#### 10 类边界条件覆盖情况

| 边界类型 | 覆盖状态 | 说明 |
|----------|----------|------|
| 空值/null 输入 | PASS | 后端 spec 每个 scenario 都有 null/empty 校验场景 |
| 超长输入 | PASS | 后端 spec 有 over-length 校验场景 |
| 重复操作 | PASS | 前端 spec 有重复订阅、防重复提交场景 |
| 并发操作 | FLAG | 未覆盖并发关注/取消关注同一用户的场景 |
| 权限边界 | PASS | 禁止自关注、拉黑互斥、订阅所有权校验 |
| 数量上限 | PASS | 关注数 5000 上限、推荐累计 200 条上限、分组 20 个上限 |
| 网络异常 | PASS | 前端 spec 有乐观更新失败回滚、Toast 提示 |
| 空状态 | PASS | 所有列表页面均有空状态 scenario |
| 分页边界 | FLAG | 未覆盖最后一页不足 pageSize 的场景、负数页码场景 |
| 跨端兼容 | PASS | 前端 spec 有 PC/平板/手机三种设备的 layout scenario |

#### FLAG
- **[F10] 并发操作边界缺失**: 未覆盖用户在多标签页同时操作关注/取消关注同一用户时的数据一致性场景
- **[F11] 分页极端场景缺失**: 未覆盖 pageSize=0、page 超过总页数、数据变更导致 page 失效等场景

#### ADVISORY
- **[A9] 移动端网络环境边界**: 未覆盖弱网（2G/3G）环境下的操作体验和超时处理
- **[A10] 浏览器兼容性边界**: PRD 5.9 提到 Chrome/Firefox/Safari/Edge 兼容，但 spec 中无对应 scenario

---

## 4. 前后端衔接审计

> 触发条件: change-prd-mapping.yaml 中存在配对后端 change `user-04-social-subscription`，且该 change 目录存在。

### 4.1 接口清单双向对比

#### 前端 design.md 引用但后端 spec 未明确定义的端点

| 前端端点 | 后端状态 | 风险 |
|----------|----------|------|
| `GET /content/user/relation/mutual-follow-list` | 后端 spec 未提及互关好友列表 | FLAG |
| `GET /content/user/relation/detail` | 后端 spec 提及 relationship detail 可查询 | PASS |
| `POST /content/user/relation/block` | 后端 spec 未定义拉黑接口（属 EPIC-05） | ADVISORY |
| `POST /content/user/relation/unblock` | 同上 | ADVISORY |
| `POST /content/user/relation/mute` | 同上 | ADVISORY |
| `POST /content/user/relation/mute/cancel` | 同上 | ADVISORY |
| `GET /content/user/relation/blacklist` | 同上 | ADVISORY |
| `GET /content/user/relation/block-mute/help` | 同上 | ADVISORY |
| `POST /content/user/subscription/source/save` | 后端 spec 未提及订阅源写入接口 | FLAG |
| `GET /content/user/subscription/notification/decision` | 后端 spec 未提及通知决策计算接口 | FLAG |

#### 后端 spec 定义但前端 design.md 未引用的端点

无。后端 spec 中所有核心能力均在前端 design.md API 表中有对应端点。

### 4.2 数据模型一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| VO 返回字段 | FLAG | 前端 design.md 未定义具体 VO 字段结构，仅列出端点。后端 spec 描述了返回数据语义但未给出具体字段名 |
| Req 请求字段 | FLAG | 前端 PRD 有部分请求示例（如 follow 请求的 targetUserId + relationGroupId），但多数接口未定义 |
| 分页契约 | FLAG | 推荐接口前端用游标分页，后端用 page/size 分页，需统一 |
| 枚举值 | ADVISORY | 动态类型（publish/like/favorite）、订阅源类型（topic/tag/collection/special/column/channel）、推荐理由类型（similar/popular/mutual_follow/interest_tag）需在契约中统一定义 |

### 4.3 错误码覆盖检查

| 前端预期错误场景 | 后端 spec 覆盖 | 状态 |
|------------------|----------------|------|
| 目标用户已注销/被封禁 | "inactive users" excluded from recommendation | PASS |
| 已被对方拉黑 | "blocked target" rejected | PASS |
| 关注数达到上限 5000 | 后端 spec 未明确上限值 | FLAG |
| 网络超时 | 后端 spec 未涉及前端网络层 | N/A |
| 服务端 500 | 后端 spec 未涉及通用错误处理 | ADVISORY |
| 来源不存在 | "unknown source" rejected | PASS |
| 重复订阅 | "duplicate active subscription" handled | PASS |

### 4.4 认证鉴权一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 用户身份获取 | ADVISORY | 前端 PRD 明确"继续使用显式 userId 参数"，后端 design 也提到"显式 userId 参数"。一致但建议后续迁移到认证上下文 |
| 权限校验 | PASS | 后端 spec 有 subscription ownership enforcement、self-follow rejection |

### 4.5 分页契约检查

| 接口 | 前端分页方式 | 后端分页方式 | 状态 |
|------|-------------|-------------|------|
| 关注列表 | page/size | page/size | PASS |
| 特别关注列表 | page/size | page/size | PASS |
| 关注推荐 | 游标分页 (cursor) | page/size | FLAG |
| 关注流 | page/size + cursor | page/size | ADVISORY |
| 订阅列表 | page/size | page/size | PASS |
| 订阅流 | page/size | page/size | PASS |
| 订阅广场 | page/size | page/size | PASS |

---

## 5. PRD 追溯矩阵

| PRD 功能 | Spec 文件 | Spec Requirement | Scenarios 数 | 覆盖状态 |
|----------|-----------|------------------|-------------|----------|
| 关注与取消关注 | user-follow-system | 关注与取消关注功能 | 5 | PASS |
| 特别关注与强提醒 | user-follow-system | 特别关注与强提醒功能 | 4 | PASS |
| 关注分组管理 | user-follow-system | 关注分组管理功能 | 5 | PASS |
| 关注列表 | user-follow-system | 关注列表功能 | 4 | PASS |
| 特别关注列表 | user-follow-system | 特别关注列表功能 | 3 | PASS |
| 关注流 | social-feed | 关注流功能 | 7 | PASS |
| 关注推荐 | user-follow-system | 关注推荐功能 | 4 | PASS |
| 批量管理关注 | user-follow-system | 批量管理关注功能 | 5 | PASS |
| 订阅内容源 | content-subscription | 订阅内容源功能 | 4 | PASS |
| 暂停/恢复订阅 | content-subscription | 暂停/恢复订阅功能 | 2 | PASS |
| 订阅通知配置 | content-subscription | 订阅通知配置功能 | 6 | PASS |
| 订阅广场 | content-subscription | 订阅广场功能 | 7 | PASS |
| 订阅流 | social-feed | 订阅流功能 | 5 | PASS |
| 订阅统一管理 | content-subscription | 订阅统一管理功能 | 7 | PASS |

**PRD AC 覆盖率: 14/14 = 100%**

---

## 6. 最终结论

### 总体评价

change 文档质量**中等偏上**，结构完整、PRD 功能覆盖率 100%、TDD 配对率 100%。主要问题集中在**前后端 API 契约层面**：路径前缀不一致、VO/Req 字段定义缺失、分页策略差异。这些问题若不在 apply 前解决，将导致前后端对接失败。

### 必须解决 (BLOCK) -- 2 项

1. **[B1] API 路径前缀不一致**: 前端 `/api/v1/` vs 后端 `/content/user/`，必须统一
2. **[B2] API 契约字段定义缺失**: 31 个端点均无 Req/VO 字段定义，前端无法开发

### 建议解决 (FLAG) -- 11 项

- [F1] 补充 gap-analysis.md（mock 策略）
- [F2] Plan.md 拆分为模块级文件
- [F3] 推荐接口路径统一
- [F4] 关注流接口路径统一
- [F5] 确认订阅源目录表是否已创建
- [F6] 明确性能阈值是否包含后端时间
- [F7] 全局通知默认配置接口路径确认
- [F8] 补充 VO/Req 字段定义
- [F9] 后端 spec 明确批量操作上限
- [F10] 补充并发操作边界 scenario
- [F11] 补充分页极端场景

### 可选优化 (ADVISORY) -- 10 项

- [A1] 完成 4 项验收任务
- [A2] 统一分页策略
- [A3] 定义枚举值列表
- [A4] 明确虚拟滚动实现方案
- [A5] 明确 E2E 测试框架
- [A6] 补充 Mock 策略
- [A7] 定义错误码体系
- [A8] 明确关注数上限接口返回方式
- [A9] 补充弱网边界场景
- [A10] 补充浏览器兼容性场景

### 建议操作

1. **立即处理 BLOCK 项**: 统一 API 路径前缀，补充核心接口的 Req/VO 字段定义
2. **Apply 前处理 FLAG 项**: 至少解决 F3/F4/F5/F7/F8 五项关键一致性问题
3. **Apply 后补全**: 完成验收任务（A1）、补充边界场景（F10/F11）
