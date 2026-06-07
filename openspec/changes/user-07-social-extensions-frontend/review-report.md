# OpenSpec Review Report: user-07-social-extensions-frontend

**审核日期**: 2026-06-06
**Change**: user-07-social-extensions-frontend
**类型**: 前端 change (EPIC-07 社交关系扩展)
**配对后端 change**: user-07-social-extensions
**PRD**: `docs/requirements/prd/frontend/EPIC-07-social-extensions-frontend-prd.md`

---

## 1. 总览表

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 8/10 | 0 | 1 | 1 |
| 一致性 (Consistency) | 5/10 | 0 | 3 | 0 |
| 可实现性 (Feasibility) | 9/10 | 0 | 0 | 1 |
| 可测试性 (Testability) | 6/10 | 0 | 2 | 0 |
| 接口契约 (API Contract) | 4/10 | 1 | 1 | 0 |
| 边界覆盖 (Boundary) | 7/10 | 0 | 1 | 0 |
| **总计** | **6.5/10** | **1** | **8** | **2** |

---

## 2. 量化指标

| 指标 | 数值 | 说明 |
|------|------|------|
| PRD AC 覆盖率 | 85% | 7 个 capability 全部有 spec，但粉丝画像降级未在 specs 中明确标注 |
| API 契约完整率 | 62% | 8 个后端已实现端点中 specs 正确引用 5 个，3 个后端缺失端点 |
| 边界覆盖率 | 70% | 10 类边界中覆盖 7 类，缺少并发操作、输入长度限制、状态竞态 |
| TDD 配对率 | 0% | 前端无自动化测试策略（后端有明确测试策略） |
| Spec 路径正确率 | 73% | 11 个 API 路径引用中 8 个已修正为正确后端路径 |
| PRD-Spec 路径一致率 | 27% | PRD 中 API 路径仍使用旧路径，与已修正的 specs 严重不一致 |

---

## 3. 各维度详细审核结果

### 3.1 完整性 (Completeness) — 8/10

**文档结构完整性**:
- [PASS] proposal.md: Why/What Changes/Capabilities/Impact 四要素齐全
- [PASS] design.md: Context/Goals-Non-Goals/Decisions/Risks 四要素齐全
- [PASS] specs/: 7 个 spec 文件，与 proposal 中 7 个 Capabilities 一一对应
- [PASS] tasks.md: 53 个任务，全部标记完成
- [PASS] backend-issues.md: 记录 3 个后端缺失端点
- [PASS] verification-review.md: 记录 API 路径验证结果

**内容覆盖**:
- [PASS] 互关标识 + 互关列表: 完整覆盖
- [PASS] 私密内容: 完整覆盖发布/展示/搜索/交互限制/取关后处理
- [PASS] 粉丝管理: 列表 + 趋势覆盖
- [PASS] 邀请系统: 生成/分享/记录/统计/落地页覆盖
- [PASS] 社区角色: 标签展示 + 角色说明覆盖
- [PASS] 治理操作: 删除评论 + 警告用户 + 权限控制覆盖
- [PASS] 审计日志: 页面 + 筛选 + 权限覆盖
- [PASS] 埋点: 16 个事件完整定义

| # | 级别 | 问题 | 位置 |
|---|------|------|------|
| C-1 | FLAG | 粉丝画像 Tab 3 降级至二期，但 design.md 文件结构中仍列出 `FanProfile.vue`，且 PRD 测试表中仍包含粉丝画像测试用例（画像正常/不足/导出），可能误导开发 | design.md L163, PRD L792-794 |
| C-2 | ADVISORY | PRD 3.3 节邀请分享页提到"二维码展示（可选）"，但无 spec 场景覆盖二维码生成逻辑，组件选型表中列出 `Qrcode` 组件但无具体实现规格 | PRD L158 |

### 3.2 一致性 (Consistency) — 5/10

**Proposal ↔ Specs 一致性**:
- [PASS] 7 个 Capabilities 与 7 个 spec 目录完全对应
- [PASS] 每个 capability 的核心功能在 spec 中有对应 Requirement

**Decisions ↔ Requirements 一致性**:
- [PASS] D1（互关缓存策略）→ mutual-follow spec 的 "Comment list with built-in mutual follow field" 场景
- [PASS] D2（ECharts 按需引入）→ fan-analytics spec 的 "ECharts bundle optimization" 场景
- [PASS] D3（私密内容后端过滤）→ private-content spec 的 "Non-mutual follow user does not see private content" 场景
- [PASS] D5（角色标签数据来源）→ community-roles spec 的 "Role data from comment API" Requirement

**PRD ↔ Specs 一致性 (严重问题)**:
- [FAIL] PRD 第 5 节 API 对接表中的路径与 specs 中已修正的路径完全不一致

| # | 级别 | 问题 | 位置 |
|---|------|------|------|
| S-1 | FLAG | **PRD API 路径与 Specs 严重不一致** — PRD 使用旧路径模式 `content/{feature}`，specs 已修正为实际后端路径 `content/user/{feature}`。共 11 处不一致。此问题会导致开发者困惑：以 PRD 为准还是以 spec 为准？ | PRD Section 5 全部 API 表 |
| S-2 | FLAG | **PRD 邀请码生成方法错误** — PRD 写 `GET /content/invite/code`（获取/生成），spec 已修正为 `POST /api/v1/content/user/invite/generate`（显式生成）。PRD 语义暗示"首次自动生成后续复用"，但实际是 POST 显式生成 | PRD L464 vs invite-system spec L8 |
| S-3 | FLAG | **design.md 文件结构与 tasks.md 不一致** — design.md 列出 `MutualFollowList.vue`、`FanList.vue`、`FanTrend.vue`、`InviteShare.vue` 等文件名，但 tasks.md 使用 `index.vue` 命名模式（如 `mutual-follow/index.vue`、`fan/index.vue`）。实际代码使用 `index.vue` 模式 | design.md L158-169 vs tasks.md |

### 3.3 可实现性 (Feasibility) — 9/10

- [PASS] 技术栈完全兼容: Vue 3 + Vite + Ant Design Vue + TypeScript + defHttp + Pinia
- [PASS] ECharts 按需引入策略合理，控制包体积在 ~200KB
- [PASS] 组件设计复用现有 Ant Design Vue 组件（Tag/Popover/Modal/Form/Table/Tabs）
- [PASS] Store 设计合理: useMutualFollowStore 缓存 Map、useInviteStore 缓存邀请码
- [PASS] Hooks 使用符合项目规范: useTable/useForm/useModal/useMessage/usePermission
- [PASS] 路由设计合理，独立路由 `/invite/:inviteCode` 面向未注册用户
- [PASS] 权限控制采用前端显隐 + 后端校验双重保障

| # | 级别 | 问题 | 位置 |
|---|------|------|------|
| F-1 | ADVISORY | 粉丝画像 Tab 3 降级至二期，但 design.md 仍将其列入文件结构，建议清理避免开发者误建文件 | design.md L163 |

### 3.4 可测试性 (Testability) — 6/10

**Scenario 质量**:
- [PASS] 每个 spec 的 Scenario 具有可验证的 WHEN/THEN 结构
- [PASS] 边界场景覆盖较好（空状态、无权限、失效码等）
- [PASS] PRD 第 11 节包含 40+ 个功能测试用例

**TDD 配对**:
- [FAIL] **无前端自动化测试策略** — 后端 design.md 有明确的测试文件和测试范围表，前端 design.md 完全没有提及测试。PRD 测试部分仅有手工测试用例表，无 Vitest 单元测试或 Playwright E2E 测试规划
- [FAIL] **tasks.md 无测试任务** — 53 个任务全部是实现任务，无一测试编写任务

| # | 级别 | 问题 | 位置 |
|---|------|------|------|
| T-1 | FLAG | **前端无自动化测试计划** — 后端有 `ContentUserRelationServiceMutualTest` 等 5 个测试类规划，前端 0 个测试文件规划。按照 AGENTS.md 的 TDD 要求，应在 tasks 中包含测试编写任务 | design.md, tasks.md |
| T-2 | FLAG | **Scenario 缺少量化验收标准** — 如"粉丝列表加载 < 1s"仅在 PRD 性能要求中出现，spec Scenario 中无性能断言；"300ms 防抖"仅在 tasks 中提及，spec 中未量化 | fan-analytics spec, mutual-follow spec |

### 3.5 接口契约 (API Contract) — 4/10

**API 端点覆盖**:
- Specs 中定义了 12 个 API 端点引用
- 后端已实现 8 个端点
- 后端缺失 3 个端点（互关状态批量查询、邀请码校验、审计日志查询）
- 1 个端点 specs 已修正路径但 PRD 未同步

**请求/响应格式**:
- [FAIL] **specs 无请求参数和响应格式定义** — 7 个 spec 文件均未定义请求参数类型、响应 VO 结构。仅 PRD Section 5 有部分格式定义，但路径已过时
- [PASS] PRD 定义了主要端点的请求参数和响应格式（但路径需修正）

| # | 级别 | 问题 | 位置 |
|---|------|------|------|
| A-1 | BLOCK | **3 个后端端点缺失，阻塞对应前端功能** — (1) `GET /api/v1/content/user/relation/mutual-status` 阻塞增量评论互关标识；(2) `GET /api/v1/content/user/invite/info/{inviteCode}` 阻塞邀请落地页全部功能（6.1-6.6）；(3) `GET /api/v1/content/user/governance/audit-log` 阻塞审计日志页全部功能（9.1-9.4）。tasks.md 将这些任务标记为"完成"，但后端接口不存在 | backend-issues.md |
| A-2 | FLAG | **specs 缺少请求/响应 VO 定义** — 7 个 spec 文件均无请求参数类型（Req）和响应结构（VO）定义。开发者需同时阅读 PRD 和 spec 才能了解完整接口契约，增加认知负担 | 所有 spec 文件 |

### 3.6 边界覆盖 (Boundary) — 7/10

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| 空状态 | [PASS] | 互关列表空、粉丝空、邀请记录空、搜索无结果 |
| 分页边界 | [PASS] | 粉丝列表分页、邀请记录分页、审计日志分页 |
| 搜索边界 | [PASS] | 300ms 防抖、清空恢复、无结果提示 |
| 权限边界 | [PASS] | 非管理员 403、普通用户隐藏管理按钮 |
| 时间范围 | [PASS] | 审计日志时间筛选、邀请码过期处理 |
| 状态转换 | [PASS] | 取关后私密内容不可见、邀请码过期/名额满 |
| 网络错误 | [PASS] | 互关列表网络错误+重试、管理操作失败保留弹窗 |
| 并发操作 | [FAIL] | 未覆盖：快速连续取关/关注、同时打开多个评论详情页修改互关状态 |
| 输入验证 | [FLAG] | 管理操作原因输入无长度限制说明、搜索关键词无长度限制 |
| 状态竞态 | [FAIL] | 未覆盖：取关操作进行中用户刷新页面、邀请码生成请求重复提交 |

| # | 级别 | 问题 | 位置 |
|---|------|------|------|
| B-1 | FLAG | **输入长度限制未定义** — 管理操作 Modal 的原因输入框（删除原因/警告原因）和搜索输入框均无最大长度限制说明，可能导致超长输入或后端截断 | moderation spec, PRD L296-298 |

---

## 4. 前后端衔接审计

### 4.1 接口清单双向对比

**后端已实现端点 vs 前端 Spec 引用**:

| 功能 | 后端实际路径 | 前端 Spec 路径 | 状态 |
|------|------------|--------------|------|
| 互关好友列表 | `GET /api/v1/content/user/relation/mutual-follow-list` | `GET /api/v1/content/user/relation/mutual-follow-list` | [MATCH] |
| 互关状态查询 | **不存在** | `GET /api/v1/content/user/relation/mutual-status` | [MISSING] |
| 粉丝列表 | `GET /api/v1/content/user/fan/list` | `GET /api/v1/content/user/fan/list` | [MATCH] |
| 粉丝趋势 | `GET /api/v1/content/user/fan/trend` | `GET /api/v1/content/user/fan/trend` | [MATCH] |
| 邀请码生成 | `POST /api/v1/content/user/invite/generate` | `POST /api/v1/content/user/invite/generate` | [MATCH] |
| 邀请码校验 | **不存在** | (specs 中 NOTE 标注需后端补充) | [MISSING] |
| 邀请记录 | `GET /api/v1/content/user/invite/records` | `GET /api/v1/content/user/invite/records` | [MATCH] |
| 邀请统计 | `GET /api/v1/content/user/invite/stats` | `GET /api/v1/content/user/invite/stats` | [MATCH] |
| 删除评论 | `POST /api/v1/content/user/governance/moderator/comment/delete` | `POST /api/v1/content/user/governance/moderator/comment/delete` | [MATCH] |
| 警告用户 | `POST /api/v1/content/user/governance/moderator/user/warn` | `POST /api/v1/content/user/governance/moderator/user/warn` | [MATCH] |
| 审计日志 | **不存在** | (specs 中 NOTE 标注需后端补充) | [MISSING] |

**匹配率**: 8/11 = 73%（已实现端点全部匹配，3 个端点后端缺失）

### 4.2 数据模型一致性

| 对比项 | 状态 | 说明 |
|--------|------|------|
| 互关列表响应字段 | [OK] | PRD 定义 `userId/nickname/avatar/mutualFollowTime`，与后端 VO 设计一致 |
| 粉丝列表响应字段 | [OK] | PRD 定义 `avatar/nickname/followTime/interactionCount` |
| 邀请码响应字段 | [OK] | PRD 定义 `inviteCode/inviteLink` |
| 邀请统计响应字段 | [OK] | PRD 定义 `totalInviteCount/successRegisterCount/totalRewardPoints` |
| 社区角色枚举值 | [OK] | 前后端统一使用 `NORMAL/CREATOR/MODERATOR/ADMIN` |
| 管理操作请求体 | [FLAG] | Spec 定义 `{ commentId, reason }` 和 `{ userId, reason }`，需确认后端 controller 参数名完全一致 |

### 4.3 错误码覆盖检查

| 错误场景 | PRD 定义 | Spec 覆盖 | 状态 |
|---------|---------|----------|------|
| 邀请码无效 | `INVALID_INVITE_CODE` | invite-system spec "Invalid invite code" | [OK] |
| 邀请码过期 | `EXPIRED` | invite-system spec "Expired invite code" | [OK] |
| 邀请名额已满 | `MAX_REACHED` | invite-system spec "Max reached invite code" | [OK] |
| 粉丝不足 | `INSUFFICIENT_FANS` | PRD 定义 | [OK - 但降级至二期] |
| 非管理员访问 | HTTP 403 | audit-log spec "Non-admin access denied" | [OK] |
| 操作失败 | 未定义通用错误码 | moderation spec "Delete comment failure" | [FLAG] 缺少具体错误码 |

### 4.4 认证鉴权一致性

- [OK] 管理操作使用 `usePermission` Hook + 后端 403 双重保障
- [OK] 审计日志页仅管理员可访问
- [OK] 邀请落地页已登录用户重定向
- [OK] 私密内容后端过滤（非前端过滤）

### 4.5 分页契约检查

| 接口 | 分页参数 | 状态 |
|------|---------|------|
| 互关列表 | page, pageSize, keyword | [OK] 与 PRD 定义一致 |
| 粉丝列表 | page, pageSize, keyword | [OK] |
| 邀请记录 | page, pageSize (推断) | [FLAG] PRD 未明确分页参数名 |
| 审计日志 | page, pageSize, operatorName, operationType, startTime, endTime | [OK - 但后端接口缺失] |

---

## 5. PRD 追溯矩阵

| PRD 用户故事 | 对应 Spec | 对应 Tasks | 覆盖状态 |
|-------------|----------|-----------|---------|
| US-7.1.1 互关标识 | mutual-follow/spec.md | 2.1, 2.5, 3.1-3.6 | [PASS] |
| US-7.1.2 私密内容 | private-content/spec.md | 7.1-7.6 | [PASS] |
| US-7.2.1 粉丝列表+趋势 | fan-analytics/spec.md | 4.1-4.6 | [PASS] |
| US-7.2.2 粉丝画像 | (降级至二期) | (无) | [N/A] |
| US-7.3.1 邀请系统 | invite-system/spec.md | 5.1-5.6, 6.1-6.6 | [PASS - 但落地页依赖缺失接口] |
| US-7.4.1 角色标签 | community-roles/spec.md | 8.1, 8.4, 2.2 | [PASS] |
| US-7.4.2 管理操作 | moderation/spec.md | 8.2-8.4, 2.3 | [PASS] |
| (PRD 3.6) 审计日志 | audit-log/spec.md | 9.1-9.4 | [PASS - 但依赖缺失接口] |
| (PRD 12) 埋点 | (无独立 spec) | 10.1-10.6 | [FLAG] 埋点事件仅在 PRD 定义，无 spec 约束 |

---

## 6. 最终结论

### 评估结论: CONDITIONAL PASS — 需修复 1 个 BLOCK 后可 Apply

**核心发现**:

1. **BLOCK — 3 个后端端点缺失**: 互关状态批量查询、邀请码校验、审计日志查询端点后端尚未实现。其中邀请码校验缺失直接阻塞邀请落地页全部功能（6 个任务），审计日志缺失阻塞审计日志页全部功能（4 个任务）。tasks.md 将这些任务标记为"已完成"，但实际后端接口不存在。

2. **PRD 与 Specs 路径严重分裂**: PRD 使用旧路径模式（如 `/content/invite/code`），specs 已修正为实际后端路径（如 `/api/v1/content/user/invite/generate`）。开发者若以 PRD 为准将对接错误接口。

3. **前端无自动化测试**: 53 个实现任务无一测试任务，违反 AGENTS.md 的 TDD 要求。

### 建议操作

| 优先级 | 操作 | 负责方 |
|--------|------|--------|
| P0 | 后端补充 3 个缺失端点（互关状态查询、邀请码校验、审计日志查询） | 后端 |
| P0 | 修正 PRD Section 5 API 路径表，与 specs 和实际后端保持一致 | 前端 |
| P1 | 为 specs 补充请求参数（Req）和响应结构（VO）定义 | 前端 |
| P1 | 在 tasks.md 中补充前端自动化测试任务（Vitest 组件测试 + API mock） | 前端 |
| P2 | 清理 design.md 中粉丝画像相关文件（FanProfile.vue），避免误导 | 前端 |
| P2 | 统一 design.md 文件命名与 tasks.md 的 `index.vue` 模式 | 前端 |
| P3 | 补充管理操作 Modal 输入框最大长度限制 | 前端 |
| P3 | 明确邀请记录列表分页参数名 | 前端 |

---

## 7. 问题清单

| # | 级别 | 维度 | 问题摘要 | 影响范围 |
|---|------|------|---------|---------|
| A-1 | BLOCK | API Contract | 3 个后端端点缺失（互关状态查询、邀请码校验、审计日志查询） | 邀请落地页 6 个任务 + 审计日志页 4 个任务 + 评论增量互关标识 |
| S-1 | FLAG | Consistency | PRD API 路径与 specs 严重不一致（11 处） | 所有 API 对接任务 |
| S-2 | FLAG | Consistency | 邀请码生成方法 PRD(GEN) vs spec(POST) 不一致 | 邀请码生成逻辑 |
| S-3 | FLAG | Consistency | design.md 文件名与 tasks.md 实际命名不一致 | 文件创建任务 |
| T-1 | FLAG | Testability | 前端无自动化测试计划（后端有 5 个测试类规划） | 全部 53 个任务的质量保障 |
| T-2 | FLAG | Testability | Spec Scenario 缺少量化验收标准（性能、防抖等） | 性能相关场景 |
| A-2 | FLAG | API Contract | 7 个 spec 文件缺少请求/响应 VO 定义 | 所有 API 对接任务 |
| B-1 | FLAG | Boundary | 管理操作输入框无最大长度限制 | 管理操作 Modal |
| C-1 | FLAG | Completeness | 粉丝画像降级至二期但 design.md 和 PRD 测试表仍包含 | 开发者认知 |
| C-2 | ADVISORY | Completeness | 二维码生成无 spec 场景覆盖 | 邀请分享页 |
| F-1 | ADVISORY | Feasibility | design.md 文件结构包含已降级的 FanProfile.vue | 文件创建 |

---

## 8. 修复记录

**修复日期**: 2026-06-07

### 已修复问题

| # | 级别 | 修复内容 | 关联文件 |
|---|------|---------|---------|
| S-1 | FLAG | **已验证无需修复** — PRD Section 5 当前已使用正确路径 `/api/v1/content/user/...`，与 specs 一致。审核时基于旧版 PRD | PRD |
| S-2 | FLAG | **已验证无需修复** — PRD 当前已使用 `POST /api/v1/content/user/invite/generate`，与 spec 一致 | PRD |
| S-3 | FLAG | **已验证无需修复** — design.md 当前版本（93 行）无文件结构章节，不存在命名不一致问题 | design.md |
| A-2 | FLAG | **已修复** — 为 7 个 spec 文件补充 `## API 封装` 章节，包含端点路径、方法、参数注解、响应关键字段和后端实现状态 | 所有 spec 文件 |
| B-1 | FLAG | **已修复** — moderation spec 中删除原因和警告原因 textarea 增加 `maxLength: 200` 约束 | specs/moderation/spec.md |
| T-1 | FLAG | **已修复** — tasks.md 新增第 11 节"前端自动化测试"，包含 8 个 Vitest 测试任务（3 组件 + 2 Store + 3 API） | tasks.md |
| T-2 | FLAG | **已修复** — mutual-follow spec 搜索场景增加防抖 300ms ± 50ms 验收标准；fan-analytics spec 列表加载 < 1s、图表渲染 < 2s 验收标准 | specs/mutual-follow/spec.md, specs/fan-analytics/spec.md |
| C-1 | FLAG | **已验证无需修复** — design.md 当前版本无文件结构章节，粉丝画像已在 Non-Goals 中标注降级 | design.md |
| F-1 | ADVISORY | **已验证无需修复** — 同 C-1 | design.md |
| invite-stats 路径 | — | **已修复** — invite-system spec 中 `GET /content/invite/stats` 修正为 `GET /api/v1/content/user/invite/stats` | specs/invite-system/spec.md |

### 仍未修复（后端依赖）

| # | 级别 | 问题 | 说明 |
|---|------|------|------|
| A-1 | BLOCK | 3 个后端端点缺失 | 互关状态查询、邀请码校验、审计日志查询 — 需后端补充，详见 backend-issues.md |
| C-2 | ADVISORY | 二维码生成无 spec 覆盖 | 组件选型表已列出 Qrcode 组件，实现时按需集成即可 |

### 更新后评估结论

**CONDITIONAL PASS** → A-1 BLOCK 仍需后端补充 3 个端点。前端侧所有 FLAG 问题已修复。待后端端点就绪后可 Apply。
