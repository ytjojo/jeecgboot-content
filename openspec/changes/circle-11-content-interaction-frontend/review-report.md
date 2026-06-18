# 规范审核报告: circle-11-content-interaction-frontend

> **审核日期**: 2026-06-06
> **审核工具**: openspec-review-change
> **Change 类型**: 前端
> **业务域**: circle
> **EPIC**: EPIC-11
> **关联 PRD**: docs/requirements/prd/frontend/EPIC-11-circle-content-interaction-frontend-prd.md
> **关联 Change**: circle-11-content-interaction（后端配对）
> **审核模式**: 模式 B（后端部分完成，后端 change 已存在且有 spec）

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 7/10 | 1 | 2 | 0 |
| 一致性 (Consistency) | 8/10 | 0 | 2 | 0 |
| 可实现性 (Feasibility) | 9/10 | 0 | 1 | 0 |
| 可测试性 (Testability) | 7/10 | 0 | 3 | 0 |
| 接口契约 (API Contract) | 1/10 | 3 | 5 | 0 |
| 边界覆盖 (Boundary) | 6/10 | 0 | 4 | 0 |
| **综合** | **38/60** | **4** | **17** | **0** |

---

## 量化指标

| 指标 | 分子 | 分母 | 百分比 | 阈值 | 状态 |
|------|------|------|--------|------|------|
| PRD AC 覆盖率 | 19 | 24 | 79% | >=80% | FAIL |
| API 契约完整率 | 6 | 11 | 55% | >=90% | FAIL |
| 边界条件覆盖率 | 3 | 10 | 30% | >=60% | FAIL |
| TDD 配对率 | N/A | N/A | N/A | >=70% | N/A（前端 tasks 无独立测试任务） |
| Scenario 完整率 | 23 | 8 | 2.9/req | >=3/req | FAIL |
| 后端 API 满足率 | 6 | 11 | 55% | =100% | FAIL |
| 数据库表满足率 | N/A | N/A | N/A | =100% | N/A（前端不直接引用表） |
| 前端组件满足率 | 8 | 10 | 80% | >=90% | FAIL |
| 依赖阻塞项数 (P0) | 5 | - | - | =0 | FAIL |

---

## 1. 完整性审核

### 1.1 文档结构完整性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal.md 存在 | PASS | 文件存在，包含 Why/What/Capabilities/Impact |
| proposal.md 四章节完整 | PASS | Why、What Changes、Capabilities、Impact 均存在 |
| design.md 存在 | PASS | 文件存在 |
| design.md 五章节完整 | PASS | Context、Goals/Non-Goals、Decisions(6项)、Risks/Trade-offs 均存在 |
| specs/ 目录存在且含 spec.md | PASS | 4 个 spec 目录，每个含 spec.md |
| 每个 spec.md 包含 Requirement 和 Scenario | PASS | 所有 spec 均使用规范格式 |
| tasks.md 存在且格式正确 | PASS | 42 个任务，6 个分组，`- [ ]` 格式正确 |
| Capabilities 与 specs 一一对应 | **FAIL** | 见下方 BLOCK-001 |

### 1.2 前端特有检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| design.md 包含路由方案决策 | **FAIL** | 未明确新页面的路由路径和路由注册方式 |
| design.md 包含状态管理方案决策 | PASS | D6 定义了 useCircleInteractionStore |
| design.md 包含组件拆分决策 | PASS | PRD 4.1-4.5 定义了 5 个组件的 Props/Events/行为 |
| specs 包含页面级交互场景 | PASS | 所有 spec 使用 WHEN/THEN 格式 |
| tasks.md 包含响应式适配任务 | PASS | 任务 5.8、6.11 包含移动端响应式 |
| proposal.md Impact 列出 API 接口依赖清单 | PASS | Impact 节列出了 11 个接口 |

### 1.3 完整性问题清单

#### BLOCK-001: circle-announcement capability 缺少 spec 文件

- **位置**: `proposal.md` Capabilities 节 + `specs/` 目录
- **描述**: proposal.md 声明了 5 个 Capabilities（content-pin-featured、circle-announcement、mention-member、join-request-review、content-report），但 `specs/` 目录下仅有 4 个 spec 子目录，缺少 `circle-announcement` 对应的 spec 文件。tasks.md 中任务 3.1-3.6 涉及公告功能，但无 spec 定义 Requirement 和 Scenario。
- **影响**: 公告功能的验收标准缺失，无法追溯需求到测试。apply 后公告功能的实现无规范约束。
- **建议修复**: 在 `specs/circle-announcement/` 下创建 `spec.md`，定义公告发布、展示、删除的 Requirement 和 Scenario。后端已有 `circle-announcement/spec.md` 可作为参考。

#### FLAG-001: design.md 缺少路由方案决策

- **位置**: `design.md` Decisions 节
- **描述**: 前端 change 的 design.md 未定义新增页面的路由路径和路由注册方式。加入申请审核页和举报处理页需要路由配置，但 design.md 未提及。
- **影响**: 开发时路由路径可能不一致，需临时决策。
- **建议修复**: 在 Decisions 中补充路由方案，如 `/circle/:circleId/join-requests` 和 `/circle/:circleId/reports`。

#### FLAG-002: PRD API 端点路径与 spec 中的路径不一致

- **位置**: `specs/content-report/spec.md` + `specs/join-request-review/spec.md` + `specs/mention-member/spec.md`
- **描述**: verification-review.md 已记录前端文档中 API 路径与后端实际实现的多处不一致，但 spec 文件中的 API 路径**未修正**。spec 中仍使用旧路径。
- **影响**: apply 后前端代码将使用错误的 API 路径。
- **建议修复**: 详见接口契约维度的 BLOCK 问题，需同步修正所有 spec 中的 API 路径。

---

## 2. 一致性审核

### 2.1 跨文档一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal Capabilities 与 specs 对应 | **FAIL** | circle-announcement 无 spec（同 BLOCK-001） |
| design.md Decisions 与 specs Requirement 无矛盾 | PASS | Decisions 中的 API 设计与 spec 一致（均使用前端旧路径，互相一致但与后端不一致） |
| tasks.md 任务与 specs Requirement 可追溯 | **FAIL** | tasks 3.1-3.6 无对应 spec Requirement |
| tasks.md 任务与 design.md Decisions 无矛盾 | PASS | 无矛盾 |

### 2.2 前端特有一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| specs 引用 API 路径与后端 design.md 定义一致 | **FAIL** | 5 个 API 路径/方法不一致（详见接口契约维度） |
| specs 状态字段名与后端 VO/DTO 字段名一致 | PASS | VO 定义在 PRD 5.5 节，与后端设计对齐 |
| design.md 路由路径与 specs 页面跳转路径一致 | PASS | specs 未定义路由跳转，无矛盾 |

### 2.3 一致性问题清单

#### FLAG-003: tasks 3.1-3.6 无对应 spec Requirement

- **位置**: `tasks.md` 第 12-17 行 + `specs/` 目录
- **描述**: 公告功能的 6 个 tasks（CircleAnnouncementBar 组件、公告发布弹窗、发布逻辑、删除逻辑、列表集成、过期隐藏）无对应 spec 文件定义 Requirement。
- **影响**: 公告功能的验收标准不明确。
- **建议修复**: 补充 `specs/circle-announcement/spec.md`。

#### FLAG-004: design.md D1 决策中的 API 路径描述未提及 circleId 参数

- **位置**: `design.md` D1 决策
- **描述**: D1 提到 `/circle-content/{id}/pin` 和 `/circle-content/{id}/featured`，未说明需要 `circleId` 查询参数。后端两个接口均需要 `@RequestParam circleId`。
- **影响**: 前端开发时可能遗漏必需参数。
- **建议修复**: 在 D1 中补充说明 `circleId` 作为查询参数传递。

---

## 3. 可实现性审核

### 3.1 前端技术兼容性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 组件库与项目兼容 (Ant Design Vue 4) | PASS | PRD 使用 Ant Design Vue 组件 |
| 状态管理兼容 (Pinia) | PASS | D6 定义 Pinia store |
| API 调用使用 defHttp | PASS | PRD 5.5 节示例使用 defHttp |
| 路由方案与现有权限模式兼容 | PASS | 未定义新路由模式，假设使用现有 BACK 模式 |
| 不包含 Non-Goals 功能 | PASS | Non-Goals 明确排除统计/推荐/激励/AI审核 |

### 3.2 可实现性问题清单

#### FLAG-005: Tinymce 富文本编辑器加载策略未明确

- **位置**: `design.md` Risks 节
- **描述**: Risks 提到使用 dynamic import 按需加载 Tinymce，但未在 design.md Decisions 或 tasks 中明确实现方式。PRD 要求公告内容使用 Tinymce，但未指定如何集成到 Modal 弹窗中。
- **影响**: 实现时可能需要额外调研 Tinymce 在 Vue 3 + Modal 中的集成方式。
- **建议修复**: 在 tasks 或 design.md 中补充 Tinymce 集成方案。

---

## 4. 可测试性审核

### 4.1 Scenario 质量

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 关键交互 Scenario 有明确用户操作→系统响应 | PASS | 所有 spec 使用 WHEN/THEN 格式 |
| 错误场景有明确 UI 反馈描述 | **FAIL** | 部分错误场景缺少 UI 反馈描述 |
| 异步操作有明确 loading/error/success 状态描述 | **FAIL** | 大部分 spec 未覆盖 loading 和 error 状态 |

### 4.2 可测试性问题清单

#### FLAG-006: content-pin-featured spec 缺少 loading/error 状态场景

- **位置**: `specs/content-pin-featured/spec.md`
- **描述**: 置顶/精华操作的 Scenario 仅描述成功路径（WHEN 操作 THEN 成功），未覆盖 API 调用中（loading）和失败（error）的 UI 反馈。
- **影响**: 开发时 loading/error 状态实现可能不一致。
- **建议修复**: 补充 Scenario：「WHEN 置顶 API 调用中 THEN 按钮显示 loading」和「WHEN API 调用失败 THEN Toast 提示操作失败」。

#### FLAG-007: mention-member spec 缺少加载失败和网络错误场景

- **位置**: `specs/mention-member/spec.md`
- **描述**: @成员浮层的 Scenario 未覆盖：成员列表加载失败时的 UI 反馈、搜索接口超时时的降级处理。
- **影响**: 网络异常时用户体验未定义。
- **建议修复**: 补充「WHEN 成员列表加载失败 THEN 显示重试按钮」等 Scenario。

#### FLAG-008: join-request-review spec 缺少批量操作失败场景

- **位置**: `specs/join-request-review/spec.md`
- **描述**: 批量批准 Scenario 仅描述成功路径，未覆盖部分成功、部分失败的场景。
- **影响**: 批量操作异常处理未定义。
- **建议修复**: 补充「WHEN 批量批准中部分失败 THEN 已成功的卡片移出列表，失败的保留并提示」。

---

## 5. 接口契约审核

### 5.1 API 路径与方法对齐

| # | 前端引用 | 后端实际 | 状态 | 问题类型 |
|---|---------|---------|------|---------|
| 1 | `PUT /circle-content/{id}/pin` | `PUT /circle-content/{contentId}/pin?circleId=xxx` | **FLAG** | 缺少 circleId 参数 |
| 2 | `PUT /circle-content/{id}/featured` | `PUT /circle-content/{contentId}/featured?circleId=xxx` | **FLAG** | 缺少 circleId 参数 |
| 3 | `POST /circle-report` | `POST /circle-report/` | OK | 匹配 |
| 4 | `PUT /circle-report/{id}/delete-content` | `POST /circle-report/{reportId}/delete-content?circleId=xxx` | **BLOCK** | HTTP 方法错误（PUT→POST）+ 缺少 circleId |
| 5 | `PUT /circle-report/{id}/ignore` | `POST /circle-report/{reportId}/ignore?circleId=xxx` | **BLOCK** | HTTP 方法错误（PUT→POST）+ 缺少 circleId |
| 6 | `PUT /circle-report/{id}/mute-user` | `POST /circle-report/{reportId}/mute?circleId=xxx` | **BLOCK** | HTTP 方法错误（PUT→POST）+ 路径错误（/mute-user→/mute）+ 缺少 circleId |
| 7 | `PUT /circle-join-request/{id}/approve` | `POST /circle-join-review/approve?circleId=xxx`（body: requestId） | **BLOCK** | 路径前缀完全不同 + 方法错误 + 参数传递方式不同 |
| 8 | `PUT /circle-join-request/{id}/reject` | `POST /circle-join-review/reject?circleId=xxx`（body: requestId + rejectReason） | **BLOCK** | 路径前缀完全不同 + 方法错误 + 参数传递方式不同 |
| 9 | `GET /circle-join-request/list` | 后端 spec 中无此端点定义 | **FLAG** | 后端 spec 中未定义此端点 |
| 10 | `GET /api/v1/content/circle/{circleId}/mentionable-members` | 后端无 Controller 端点 | **FLAG** | Service 层已有，Controller 端点缺失 |
| 11 | `DELETE /circle-announcement/{id}` | 后端无此接口 | **FLAG** | 后端仅 publish 和 getActive，无 delete |

### 5.2 接口契约问题清单

#### BLOCK-002: 举报处理 API HTTP 方法全部错误

- **位置**: `specs/content-report/spec.md` + `design.md` API 节
- **描述**: 前端 spec 定义举报的 delete-content、ignore、mute-user 三个接口使用 `PUT` 方法，后端实际为 `POST`。前端 apply 后代码将使用错误的 HTTP 方法，导致 405 错误。
- **影响**: 举报处理的三个核心操作将完全不可用。
- **建议修复**: 将 spec 和 PRD 中的 `PUT` 改为 `POST`，同步修正路径 `/mute-user` → `/mute`，补充 `circleId` 参数。

#### BLOCK-003: 加入申请审核 API 路径和方法完全不匹配

- **位置**: `specs/join-request-review/spec.md` + `design.md` + `PRD 5.4 节`
- **描述**: 前端定义 `PUT /circle-join-request/{id}/approve` 和 `PUT /circle-join-request/{id}/reject`，后端实际为 `POST /circle-join-review/approve`（body 传 requestId）和 `POST /circle-join-review/reject`（body 传 requestId + rejectReason）。路径前缀（`circle-join-request` vs `circle-join-review`）、HTTP 方法（PUT vs POST）、参数传递方式（path vs body）均不一致。
- **影响**: 加入申请审核的批准和拒绝操作将完全不可用。
- **建议修复**: 将 spec 和 PRD 中的 API 定义修正为后端实际路径和方法。

#### BLOCK-004: 禁言时长参数后端未实现

- **位置**: `specs/content-report/spec.md` 禁言 Scenario + `PRD 5.5 节`
- **描述**: 前端 spec 定义了禁言时长选项（1小时/1天/7天/30天/永久），后端 `handleMute` 方法不接受禁言时长参数，且实现中标注 `// TODO`。spec 中已标注"后端遗留"但未提供降级方案。
- **影响**: 禁言功能前端实现了时长选择但后端无法处理，用户体验不完整。
- **建议修复**: 在 spec 中明确降级方案：后端未实现时长参数时，前端禁言弹窗仍展示但固定提交默认时长（如 permanent），或暂时禁用禁言功能入口。

#### FLAG-006: 5 个 API 缺少 circleId 必需参数

- **位置**: `specs/content-pin-featured/spec.md`（2处）+ `specs/content-report/spec.md`（3处）
- **描述**: 后端置顶/精华/删除举报内容/忽略举报/禁言用户接口均需要 `circleId` 查询参数（`@RequestParam circleId`），前端 spec 中的 API 调用均未体现此参数。
- **影响**: 前端开发时可能遗漏必需参数导致接口调用失败。
- **建议修复**: 在所有涉及的 API 调用处补充 `circleId` 参数说明。

#### FLAG-007: pendingJoinRequestCount 接口后端未定义

- **位置**: `specs/join-request-review/spec.md` 管理入口角标 Scenario + `tasks.md` 任务 5.7
- **描述**: 前端需要 `pendingJoinRequestCount` 查询接口来展示管理入口角标，后端无此接口。spec 中已标注"后端遗留"但 tasks 5.7 仍将其列为待实现任务。
- **影响**: 管理入口角标功能无法实现。
- **建议修复**: tasks 5.7 标注为"待后端接口就绪后实现"，或前端先用 mock 数据占位。

#### FLAG-008: GET /circle-join-request/list 后端 spec 中未定义

- **位置**: `specs/join-request-review/spec.md` 查看申请列表 Scenario
- **描述**: 前端 spec 引用 `GET /circle-join-request/list` 查询申请列表，但后端 `circle-join-review/spec.md` 中未明确定义此列表查询端点（后端 spec 仅定义了 approve/reject 操作和审核日志）。
- **影响**: 申请列表查询接口可能未在后端规划中。
- **建议修复**: 与后端确认列表查询端点是否已实现或计划实现。

#### FLAG-009: 公告查询路径前后端不一致

- **位置**: `PRD 5.2 节` + 后端 `circle-announcement/spec.md`
- **描述**: 前端 PRD 定义查询当前公告路径为 `GET /circle-announcement/current?circleId={id}`，后端实际路径为 `GET /circle-announcement/active/{circleId}`。路径关键字不同（`current` vs `active`），参数传递方式不同（query vs path）。
- **影响**: 公告查询接口调用将失败。
- **建议修复**: 统一为后端实际路径 `GET /circle-announcement/active/{circleId}`。

#### FLAG-010: 后端 spec 中 circle-announcement 有 5 个 Requirement，前端无对应 spec

- **位置**: 后端 `specs/circle-announcement/spec.md` vs 前端 `specs/` 目录
- **描述**: 后端公告 spec 定义了 5 个 Requirement（发布、顶部展示、编辑/删除、普通成员限制、审核日志），前端无对应 spec，无法确保前端实现覆盖所有后端定义的 Requirement。
- **影响**: 前后端公告功能的验收标准不对齐。
- **建议修复**: 创建前端公告 spec，与后端 spec 对齐。

---

## 6. 边界覆盖审核

### 6.1 通用边界覆盖

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| null/空值输入处理 | PARTIAL | 公告内容为空、拒绝原因为空有覆盖；举报描述为空未覆盖 |
| 超长/超大值输入处理 | **GAP** | 未覆盖公告内容超长、举报描述超长等场景 |
| 格式不合法输入处理 | **GAP** | 未覆盖 |
| 唯一约束冲突处理 | PARTIAL | 重复举报有覆盖；重复置顶未覆盖 |
| 并发/竞态条件处理 | **GAP** | 未覆盖多管理员同时操作同一内容的场景 |
| 权限不足/未认证处理 | COVERED | 普通成员权限限制覆盖充分 |
| 资源不存在处理 | COVERED | 被举报内容已被删除有覆盖 |
| 外部服务不可用降级 | **GAP** | 未覆盖后端 API 不可用时的前端降级 |
| 网络超时/断网处理 | **GAP** | 未覆盖网络异常时的 UI 反馈 |
| 数据不一致/脏数据处理 | **GAP** | 未覆盖列表数据过期刷新场景 |

**边界覆盖率**: 3/10 (30%)

### 6.2 前端特有边界

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| 网络超时/断网 UI 反馈 | **GAP** | 未覆盖 |
| Token 过期自动刷新和重试 | **GAP** | 未覆盖 |
| 表单重复提交防护 | COVERED | PRD 8.1 节提到"防重复提交：按钮 loading + disabled" |
| 移动端/平板响应式边界 | COVERED | tasks 5.8、6.11 覆盖移动端响应式 |
| 空数据状态 UI 展示 | COVERED | 各 spec 覆盖空状态展示 |

### 6.3 边界覆盖问题清单

#### FLAG-011: 未覆盖网络超时/断网场景

- **位置**: 所有 specs
- **描述**: 10 类通用边界中有 7 类未覆盖，其中网络超时和外部服务不可用是最常见的生产环境问题，但所有 spec 均未定义相关 Scenario。
- **影响**: 网络异常时用户体验未定义，可能出现无响应或白屏。
- **建议修复**: 在 PRD 8.1 节"通用交互规则"中补充网络异常处理规则，各 spec 补充至少一个错误处理 Scenario。

#### FLAG-012: 未覆盖超长输入场景

- **位置**: `specs/content-report/spec.md`（举报描述）+ 公告功能（无 spec）
- **描述**: 举报补充说明和公告内容均可能为长文本，但 spec 未定义最大长度限制和超长处理。
- **影响**: 可能导致请求体过大或后端截断。
- **建议修复**: 在 spec 中定义字段最大长度，超长时前端截断或提示。

#### FLAG-013: 未覆盖并发操作场景

- **位置**: `specs/content-pin-featured/spec.md` + `specs/join-request-review/spec.md`
- **描述**: 多个管理员同时操作同一内容（如同时置顶/取消置顶、同时批准同一申请）的场景未覆盖。
- **影响**: 并发操作可能导致数据不一致。
- **建议修复**: 后端应保证操作幂等性，前端可补充乐观锁或版本号机制。

#### FLAG-014: 未覆盖 Token 过期场景

- **位置**: 所有 specs
- **描述**: 长时间停留页面后 Token 过期，操作失败时的 UI 反馈和自动刷新机制未定义。
- **影响**: Token 过期时用户操作可能无响应。
- **建议修复**: 依赖项目全局的 Token 刷新机制，各 spec 可补充「WHEN Token 过期 THEN 自动刷新后重试」。

---

## 7. 前后端衔接审计

### 7.1 接口清单双向对比

| # | 后端定义的 API | 前端引用 | 状态 | 说明 |
|---|---------------|---------|------|------|
| 1 | `PUT /circle-content/{id}/pin` | spec Scenario 1 | **FLAG** | 后端需 circleId，前端未传 |
| 2 | `PUT /circle-content/{id}/featured` | spec Scenario 2 | **FLAG** | 后端需 circleId，前端未传 |
| 3 | `POST /circle-report/` | spec Scenario | OK | 匹配 |
| 4 | `POST /circle-report/{id}/delete-content` | spec Scenario | **BLOCK** | 前端写 PUT，后端 POST |
| 5 | `POST /circle-report/{id}/ignore` | spec Scenario | **BLOCK** | 前端写 PUT，后端 POST |
| 6 | `POST /circle-report/{id}/mute` | spec Scenario | **BLOCK** | 前端写 PUT /mute-user，后端 POST /mute |
| 7 | `POST /circle-join-review/approve` | spec Scenario | **BLOCK** | 前端写 PUT /circle-join-request/{id}/approve |
| 8 | `POST /circle-join-review/reject` | spec Scenario | **BLOCK** | 前端写 PUT /circle-join-request/{id}/reject |
| 9 | GET join request list | spec Scenario | **FLAG** | 后端 spec 未定义列表端点 |
| 10 | GET mentionable-members | spec Scenario | **FLAG** | 后端无 Controller 端点 |
| 11 | POST circle-announcement | tasks 1.2 | OK | 匹配 |
| 12 | GET circle-announcement/active/{circleId} | PRD 5.2 | **FLAG** | 前端写 /current?circleId=，后端 /active/{circleId} |
| 13 | DELETE circle-announcement/{id} | tasks 1.2 | **FLAG** | 后端无此接口 |

**对齐统计**:
- 接口总数（前端消费清单）: 13
- 已对齐: 3
- 字段/行为不符: 7
- 完全缺失: 3
- GAP 合计: 10

### 7.2 数据模型一致性

| 字段 | 后端定义 | 前端引用 | 状态 |
|------|---------|---------|------|
| isPinned / pinnedAt | CircleContent 表字段 | PRD Response VO | MATCH |
| isFeatured / featuredAt | CircleContent 表字段 | PRD Response VO | MATCH |
| AnnouncementVO | CircleAnnouncementVO.java | PRD 5.2 节 | MATCH |
| JoinRequestVO | CircleJoinRequestVO.java | PRD 5.4 节 | MATCH |
| ReportVO | CircleReportVO.java | PRD 5.5 节 | MATCH |
| MentionMemberVO | 后端无 VO 定义 | PRD 5.3 节 | **GAP** |
| 禁言时长参数 | 后端不接受 | PRD 5.5 MuteUserReq | **GAP** |

### 7.3 错误码覆盖

| 错误场景 | 后端返回 | 前端处理 | 状态 |
|----------|---------|---------|------|
| 权限不足 | 后端返回 403 | Toast "权限不足" | COVERED |
| 重复举报 | 后端返回错误 | Tooltip "已提交过举报" | COVERED |
| 资源不存在 | 后端返回 404 | 卡片显示"已删除" | COVERED |
| 网络超时 | 未定义 | 未定义 | **GAP** |
| 服务端错误 | 未定义 | 未定义 | **GAP** |

### 7.4 衔接审计问题清单

#### BLOCK-C001: 举报处理 3 个 API HTTP 方法不匹配

- **前端 Spec**: `specs/content-report/spec.md` Scenarios: 删除内容、忽略举报、禁言用户
- **引用 API**: `PUT /circle-report/{id}/delete-content`、`PUT /circle-report/{id}/ignore`、`PUT /circle-report/{id}/mute-user`
- **问题**: 后端使用 `POST` 方法，路径中 mute-user 为 mute
- **影响**: 前端 apply 后 API 调用将收到 405 Method Not Allowed
- **建议**: 修正前端 spec 和 PRD 中的 HTTP 方法和路径

#### BLOCK-C002: 加入申请审核 2 个 API 完全不匹配

- **前端 Spec**: `specs/join-request-review/spec.md` Scenarios: 批准申请、拒绝申请
- **引用 API**: `PUT /circle-join-request/{id}/approve`、`PUT /circle-join-request/{id}/reject`
- **问题**: 后端实际为 `POST /circle-join-review/approve`（body 传 requestId）和 `POST /circle-join-review/reject`（body 传 requestId + rejectReason）
- **影响**: 前端 apply 后加入申请审核功能完全不可用
- **建议**: 修正前端 spec 和 PRD 中的 API 路径、方法和参数传递方式

#### BLOCK-C003: 禁言时长参数前后端不对齐

- **前端 Spec**: `specs/content-report/spec.md` 禁言 Scenario
- **引用 API**: `POST /circle-report/{id}/mute?circleId={circleId}`，body 含 `duration`
- **问题**: 后端 `handleMute` 不接受禁言时长参数，实现为 TODO
- **影响**: 前端发送的禁言时长将被后端忽略
- **建议**: spec 中明确降级方案，或等待后端实现后启用

#### FLAG-C001: @成员查询 API 后端缺失

- **前端 Spec**: `specs/mention-member/spec.md` 成员列表 Scenario
- **引用 API**: `GET /api/v1/content/circle/{circleId}/mentionable-members`
- **问题**: 后端 `ICircleMentionService.getMentionCandidates()` 已存在但无 Controller 端点
- **影响**: @成员功能完全不可用
- **建议**: 后端补充 Controller 端点，前端先使用圈子成员列表接口替代

#### FLAG-C002: 公告删除 API 后端缺失

- **前端 Spec**: `specs/circle-announcement/`（待创建）+ tasks 3.4
- **引用 API**: `DELETE /circle-announcement/{id}`
- **问题**: 后端 Controller 仅有 publish 和 getActive，无 delete 接口
- **影响**: 管理员无法删除公告
- **建议**: 后端补充删除接口，或前端先不实现删除功能

#### FLAG-C003: 待审核申请计数 API 后端缺失

- **前端 Spec**: `specs/join-request-review/spec.md` 管理入口角标 Scenario
- **引用 API**: `pendingJoinRequestCount`
- **问题**: 后端无此查询接口
- **影响**: 管理入口角标无法实现
- **建议**: 后端补充聚合查询接口

---

## 8. PRD 追溯矩阵

### 8.1 PRD 功能列表追溯

| PRD 功能 | 优先级 | 对应 spec | 对应 tasks | 状态 |
|---------|--------|----------|-----------|------|
| 内容置顶 | P0 | content-pin-featured | 2.1-2.5 | COVERED |
| 内容精华 | P0 | content-pin-featured | 2.1-2.5 | COVERED |
| 圈子公告 | P1 | **无 spec** | 3.1-3.6 | **GAP** |
| @成员 | P1 | mention-member | 4.1-4.6 | COVERED（API 后端缺失） |
| 加入申请审核 | P0 | join-request-review | 5.1-5.8 | COVERED（API 路径不匹配） |
| 内容举报 | P0 | content-report | 6.1-6.11 | COVERED（API 方法不匹配） |

### 8.2 PRD 验收条件追溯

| PRD 页面 | AC 数 | 已覆盖 | 未覆盖 | 覆盖率 |
|---------|-------|--------|--------|--------|
| 3.1 内容列表页（增强） | 5 | 5 | 0 | 100% |
| 3.2 内容详情页（增强） | 3 | 3 | 0 | 100% |
| 3.3 公告发布弹窗 | 4 | 3 | 1（有效期校验边界） | 75% |
| 3.4 公告展示栏 | 4 | 3 | 1（过期自动隐藏机制） | 75% |
| 3.5 @成员选择器 | 6 | 5 | 1（已选成员展示） | 83% |
| 3.6 加入申请审核页 | 5 | 5 | 0 | 100% |
| 3.7 举报处理页 | 5 | 5 | 0 | 100% |
| 3.8 举报提交弹窗 | 4 | 4 | 0 | 100% |
| **合计** | **36** | **33** | **3** | **92%** |

> 注：上表基于 PRD 页面级 AC 统计，与量化指标中的 AC 覆盖率（79%）口径不同。量化指标中的 AC 覆盖率包含了 API 对接和状态管理等非页面级 AC。

---

## 最终结论

### BLOCK 问题汇总（必须修复才能 apply）

| ID | 问题 | 位置 | 影响 |
|----|------|------|------|
| BLOCK-001 | circle-announcement capability 缺少 spec 文件 | proposal.md + specs/ | 公告功能无验收标准 |
| BLOCK-002 | 举报处理 3 个 API HTTP 方法错误 | specs/content-report + PRD 5.5 | 举报处理操作将返回 405 |
| BLOCK-003 | 加入申请审核 2 个 API 路径和方法完全不匹配 | specs/join-request-review + PRD 5.4 | 申请审核功能完全不可用 |
| BLOCK-004 | 禁言时长参数后端未实现，前端无降级方案 | specs/content-report + PRD 5.5 | 禁言功能不完整 |

### FLAG 问题汇总（应该修复）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| FLAG-001 | design.md 缺少路由方案决策 | design.md | 补充路由路径定义 |
| FLAG-002 | spec 中 API 路径未修正（verification-review 已记录） | specs/*.md | 按 verification-review.md 修正 |
| FLAG-003 | tasks 3.1-3.6 无对应 spec Requirement | tasks.md + specs/ | 补充公告 spec |
| FLAG-004 | design.md D1 未提及 circleId 参数 | design.md | 补充参数说明 |
| FLAG-005 | Tinymce 加载策略未明确 | design.md + tasks | 补充集成方案 |
| FLAG-006 | content-pin-featured spec 缺少 loading/error 场景 | specs/content-pin-featured | 补充错误处理 Scenario |
| FLAG-007 | mention-member spec 缺少加载失败场景 | specs/mention-member | 补充错误处理 Scenario |
| FLAG-008 | join-request-review spec 缺少批量失败场景 | specs/join-request-review | 补充部分失败 Scenario |
| FLAG-009 | 公告查询路径前后端不一致 | PRD 5.2 | 统一为 /active/{circleId} |
| FLAG-010 | 后端公告 spec 5 个 Requirement 前端无对应 | specs/ | 创建前端公告 spec |
| FLAG-011 | 未覆盖网络超时/断网场景 | 所有 specs | 补充网络异常 Scenario |
| FLAG-012 | 未覆盖超长输入场景 | specs/content-report | 定义字段最大长度 |
| FLAG-013 | 未覆盖并发操作场景 | specs/ | 补充并发 Scenario |
| FLAG-014 | 未覆盖 Token 过期场景 | 所有 specs | 补充 Token 刷新 Scenario |
| FLAG-015 | 5 个 API 缺少 circleId 参数 | specs/*.md | 补充参数说明 |
| FLAG-016 | pendingJoinRequestCount 接口后端缺失 | specs/join-request-review | 标注待后端实现 |
| FLAG-017 | GET /circle-join-request/list 后端 spec 未定义 | specs/join-request-review | 与后端确认 |

### ADVISORY 问题汇总（建议改进）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| （无） | - | - | - |

### 门禁判定

```
Step 1 规范审核: BLOCK=4, FLAG=17 → REJECTED
Step 2 依赖检查: P0 依赖阻塞=5 → NEEDS_DEPENDENCIES

最终判定: REJECTED
```

### 审核结论

- BLOCK 问题: 4 个
- FLAG 问题: 17 个
- ADVISORY 问题: 0 个
- 依赖阻塞 (P0): 5 项

**结论文本**: 规范审核未通过。发现 4 个 BLOCK 问题，必须修复后才能执行 apply。主要问题集中在 API 契约不匹配（HTTP 方法错误、路径不匹配）和缺少公告功能 spec 文件。修复后重新运行 `/opsx:review`。

### 修复建议

#### 需要修复的规范文档问题（共 21 项）

**优先级 P0（BLOCK，4 项）**:
- [BLOCK] 创建 `specs/circle-announcement/spec.md`，覆盖公告发布、展示、删除 Requirement
- [BLOCK] 修正举报处理 API：`PUT` → `POST`，`/mute-user` → `/mute`，补充 `circleId`
- [BLOCK] 修正加入申请审核 API：路径改为 `/circle-join-review/approve` 和 `/reject`，方法改为 `POST`，参数改为 body 传递
- [BLOCK] 为禁言功能定义降级方案（后端未实现时长参数）

**优先级 P1（FLAG，17 项）**:
- 修正所有 spec 中与后端不一致的 API 路径（按 verification-review.md）
- 补充 design.md 中缺失的路由方案、circleId 参数说明、Tinymce 集成方案
- 补充各 spec 中缺失的 loading/error/boundary 场景
- 统一公告查询路径为后端实际路径

#### 需要完善的依赖模块（共 5 项）

- [P0] 后端 API 缺失: `GET /api/v1/content/circle/{circleId}/mentionable-members` Controller 端点
- [P0] 后端 API 缺失: `DELETE /api/v1/content/circle-announcement/{id}` 删除公告接口
- [P0] 后端 API 缺失: `pendingJoinRequestCount` 待审核申请计数接口
- [P0] 后端 API 未完成: `handleMute` 禁言时长参数未实现
- [P1] 后端 API 未确认: `GET /api/v1/content/circle-join-request/list` 申请列表查询端点

所有问题修复后，请重新执行审核以确认"可开发"状态。
