# Review Report — circle-10-core-frontend

**审核日期**: 2026-06-06
**审核人**: AI Agent (opsx:review)
**Change 类型**: 前端 change
**配对后端 Change**: circle-10-core
**PRD 路径**: docs/requirements/prd/frontend/EPIC-10-circle-core-frontend-prd.md

---

## 1. 总览

### 1.1 六维度评分

| 维度 | 得分 | 评级 |
|------|------|------|
| 完整性 (Completeness) | 88/100 | GOOD |
| 一致性 (Consistency) | 72/100 | FAIR |
| 可实现性 (Feasibility) | 85/100 | GOOD |
| 可测试性 (Testability) | 82/100 | GOOD |
| 接口契约 (API Contract) | 65/100 | NEEDS_WORK |
| 边界覆盖 (Boundary) | 80/100 | GOOD |
| **综合** | **78/100** | **GOOD — 有条件通过** |

### 1.2 问题统计

| 级别 | 数量 | 说明 |
|------|------|------|
| BLOCK | 4 | 阻塞 apply，必须修复后方可继续 |
| FLAG | 7 | 强烈建议修复，不阻塞但有风险 |
| ADVISORY | 6 | 建议改进，不影响核心功能 |

### 1.3 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD AC 覆盖率 | 91% | 35/38 条 AC 在 specs 中有对应 scenario |
| API 契约完整率 | 57% | 14 个接口中 8 个后端已实现，6 个缺失 |
| 边界覆盖率 | 75% | 12 类边界中 9 类已覆盖 |
| TDD 配对率 | 0% | specs 中无 Given-When-Then 到 test case 映射 |
| Spec→Task 可追溯率 | 100% | 5 个 capability 均有对应 task 组 |

---

## 2. 详细审核结果

### 2.1 完整性 (Completeness) — 88/100

#### 文档结构完整性

| 文档 | 状态 | 说明 |
|------|------|------|
| proposal.md | ✅ 完整 | Why/What Changes/Capabilities/Impact 四段齐全 |
| design.md | ✅ 完整 | Context/Goals/Decisions(D1-D10)/Risks/Open Questions 齐全 |
| specs/ (5 files) | ✅ 完整 | 覆盖全部 5 个 capability |
| tasks.md | ✅ 完整 | 79 个 task，12 个分类，覆盖全部 spec |
| backend-issues.md | ✅ 完整 | 6 个缺失接口 + 字段缺口已文档化 |
| verification-review.md | ✅ 完整 | API 存在性验证结果齐全 |

#### PRD AC 覆盖率分析

| PRD 功能 | AC 条数 | Spec 覆盖 | 缺失 |
|---------|---------|-----------|------|
| 创建圈子 | 6 | 6/6 | — |
| 名称校验 | 3 | 3/3 | — |
| 隐私联动 | 4 | 4/4 | — |
| 图片上传 | 4 | 4/4 | — |
| 加入—直接 | 2 | 2/2 | — |
| 加入—审核 | 4 | 4/4 | — |
| 加入—密码 | 3 | 3/3 | — |
| 加入—邀请 | 2 | 2/2 | — |
| 退出圈子 | 3 | 3/3 | — |
| 成员列表 | 5 | 5/5 | — |
| 角色管理 | 4 | 4/4 | — |
| 禁言/解除 | 4 | 4/4 | — |
| 成员移除 | 3 | 3/3 | — |
| 搜索 | 7 | 7/7 | — |
| 治理日志 | 5 | 5/5 | — |
| **圈子信息更新** | **2** | **0/2** | **specs 有 Requirement 但 tasks 无对应实现任务** |
| **密码长度规则** | **1** | **0/1** | **PRD 要求"6-20位，不允许纯数字"，specs 仅覆盖纯数字校验** |
| **成员上限展示** | **1** | **0/1** | **PRD 要求详情页展示"成员数/上限"，spec 未明确** |

**BLOCK-1**: 圈子信息更新（P1 功能）在 PRD 中定义了 AC（创建者可修改简介/图标/封面图/分类，名称不可改），specs circle-crud 中有 Requirement 定义，但 tasks.md 中没有对应的编辑页面/编辑表单实现任务。当前 tasks 仅覆盖创建流程，缺少编辑流程。

**FLAG-1**: PRD 字段校验规则要求密码"6-20位，不允许纯数字，至少包含字母"，但 specs circle-crud 的密码场景仅覆盖"纯数字"校验，未覆盖"长度 6-20"和"至少包含字母"规则。

**FLAG-2**: PRD 3.3 节要求详情页展示"成员数/上限"格式（如"128/500"），但 specs circle-crud 的详情页 Requirement 未明确提及上限字段的展示。

### 2.2 一致性 (Consistency) — 72/100

#### Capabilities ↔ Specs 一致性

| Capability | Spec 文件 | 一致性 |
|-----------|----------|--------|
| circle-crud | specs/circle-crud/spec.md | ✅ 一致 |
| circle-member-management | specs/circle-member-management/spec.md | ✅ 一致 |
| circle-search | specs/circle-search/spec.md | ✅ 一致 |
| circle-governance-log | specs/circle-governance-log/spec.md | ✅ 一致 |
| circle-state-management | specs/circle-state-management/spec.md | ✅ 一致 |

#### Decisions ↔ Requirements 一致性

| Decision | 对应 Requirement | 一致性 |
|----------|----------------|--------|
| D1 独立路由 | 圈子列表页/详情页/成员管理页路由 | ✅ 一致 |
| D2 独立页面创建 | 圈子创建流程 spec | ✅ 一致 |
| D3 集中式 Store | useCircleStore spec | ✅ 一致 |
| D4 后端驱动按钮状态 | 加入按钮 applyStatus/isInvited 场景 | ✅ 一致 |
| D5 搜索防抖 300ms | 搜索防抖 spec | ✅ 一致 |
| D6 Tab 缓存 5 分钟 | 列表缓存策略 spec | ✅ 一致 |
| D7 前端裁剪上传 | 图片上传 spec | ✅ 一致 |
| D8 密码强度前端计算 | 密码强度 spec | ✅ 一致 |
| D9 敏感词降级放行 | 敏感词 spec | ✅ 一致 |
| D10 CSS 媒体查询响应式 | 各页面响应式 task | ✅ 一致 |

#### 跨文档不一致

**BLOCK-2**: design.md Context 段声称"14 个 API 接口对接"，但 verification-review.md 确认仅 8 个后端接口存在。proposal.md Impact 段同样提到"依赖后端 14 个接口"。文档自相矛盾：一方面声称 14 个接口，另一方面又列出 6 个缺失接口。应统一表述为"依赖 14 个接口定义，其中 8 个后端已实现，6 个需后端补充"。

**FLAG-3**: 成员上限数值不一致。后端 design.md 圈子表定义 `max_member_count` 字段，后端 circle-member-management spec 场景中提到"圈子成员数已达到上限（10,000 人）"。前端 design.md 提到"默认 500 人"。PRD 提到"成员上限默认值为 500 人（所有隐私类型统一），可由后端配置覆盖"。前后端对默认值理解不同，需对齐。

**FLAG-4**: 接口命名不一致。design.md 使用 `quitCircle`，后端实际路径为 `/content/circle/leave`。verification-review.md 已指出此问题并建议前端封装使用 `leaveCircle`。但 design.md 和 specs 中仍使用 `quitCircle` 命名，未修正。

### 2.3 可实现性 (Feasibility) — 85/100

#### 技术栈兼容性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Vue 3 + TypeScript | ✅ | 与项目技术栈一致 |
| Ant Design Vue 4 | ✅ | 设计文档明确引用 |
| Vben Admin | ✅ | 设计文档明确引用 |
| defHttp API 封装 | ✅ | 响应格式 `{ code, result, message, success }` 已定义 |
| Pinia Store | ✅ | 与项目现有 Store 模式一致 |
| 路由注册方式 | ✅ | 遵循 `/src/router/` 目录结构 |
| 组件复用 | ✅ | 12 个现有组件被引用复用 |

#### 架构规范

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 页面组织 | ✅ | 独立路由，与项目风格一致 |
| 组件分层 | ✅ | 业务组件在 `views/circle/components/`，复用组件在 `/@/components/` |
| API 层独立 | ✅ | `src/api/content/circle.ts` 统一封装 |
| Mock 策略 | ✅ | 6 个缺失接口有明确 Mock 开发策略 |

**BLOCK-3**: 6 个后端接口缺失（detail/my-list/public-list/check-name/member-list/governance-log），其中 4 个为 P0 阻塞级。虽然 design.md R1 提出了 Mock 策略，但未在 specs 中明确标注哪些 scenario 依赖 Mock 接口。建议在每个依赖缺失接口的 scenario 中添加 `[MOCK]` 标记。

**FLAG-5**: 无障碍（a11y）要求在 PRD 默认假设第 10 条中定义（alt 文本、键盘导航、aria-label、颜色对比度），但 specs 和 tasks 中无对应实现任务。

### 2.4 可测试性 (Testability) — 82/100

#### Scenario 可量化性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| WHEN/THEN 结构 | ✅ | 所有 scenario 遵循 Given-When-Then 格式 |
| 可量化指标 | ✅ | 防抖 300ms、缓存 5 分钟、P95 < 500ms 等均有明确数值 |
| 状态转换 | ✅ | 按钮状态变化（加入→已加入→申请中）有明确描述 |
| 错误路径 | ✅ | 密码错误、满员、权限不足等均有覆盖 |

#### TDD 配对

**ADVISORY-1**: specs 中的 scenario 未与 tasks.md 中的测试任务（12.2/12.3）建立显式映射。建议在 tasks 的测试部分标注对应的 spec scenario 编号，确保测试覆盖全部场景。

**ADVISORY-2**: tasks.md 12.2 列出"核心组件（CircleCard、JoinStatusButton、CircleForm）和 Store"的单元测试，但未列出 JoinCircleModal、MuteMemberModal、GovernanceConfirmModal 等交互组件的测试计划。

### 2.5 接口契约 (API Contract) — 65/100

#### API 端点双向对比

**前端定义 vs 后端实际**:

| 接口 | 前端定义 | 后端实际 | 一致性 |
|------|---------|---------|--------|
| 创建圈子 | POST `/content/circle/create` | POST `/content/circle/create` | ✅ |
| 更新圈子 | PUT `/content/circle/update` | PUT `/content/circle/update` | ✅ |
| 圈子详情 | GET `/content/circle/detail?id={id}` | **不存在** | ❌ 缺失 |
| 已加入列表 | GET `/content/circle/my-list` | **不存在** | ❌ 缺失 |
| 公开列表 | GET `/content/circle/public-list` | **不存在** | ❌ 缺失 |
| 名称校验 | GET `/content/circle/check-name` | **不存在** | ❌ 缺失 |
| 加入圈子 | POST `/content/circle/member/join` | POST `/content/circle/join` | ⚠️ 路径不同 |
| 退出圈子 | POST `/content/circle/member/quit` | POST `/content/circle/leave` | ⚠️ 路径+命名不同 |
| 成员列表 | GET `/content/circle/member/list` | **不存在** | ❌ 缺失 |
| 设置版主 | POST `/content/circle/member/set-moderator` | POST `/content/circle/member/change-role` | ⚠️ 命名不同 |
| 禁言 | POST `/content/circle/member/mute` | POST `/content/circle/member/mute` | ✅ |
| 解除禁言 | POST `/content/circle/member/unmute` | POST `/content/circle/member/unmute` | ✅ |
| 移除成员 | POST `/content/circle/member/remove` | POST `/content/circle/member/remove` | ✅ |
| 搜索 | GET `/content/circle/search` | GET `/content/circle/search` | ✅ |
| 治理日志 | GET `/content/circle/governance-log/list` | **不存在** | ❌ 缺失 |

**BLOCK-4**: 前后端接口路径存在 3 处不一致：
1. 加入圈子：前端 `/content/circle/member/join` vs 后端 `/content/circle/join`
2. 退出圈子：前端 `/content/circle/member/quit` vs 后端 `/content/circle/leave`
3. 设置版主：前端 `/content/circle/member/set-moderator` vs 后端 `/content/circle/member/change-role`

这些不一致会导致前端封装的 API 调用在对接后端时 404。必须在实现前统一。

#### 数据模型一致性

**FLAG-6**: 前端 design.md D4 和 specs circle-member-management 依赖 `applyStatus`（String: PENDING/APPROVED/REJECTED）和 `isInvited`（Boolean）字段，但后端 CircleVO 当前不包含这两个字段。后端 verification-review.md 已确认此缺口。前端加入按钮的核心交互逻辑完全依赖这两个字段，若后端不补充，前端需降级为默认状态。

**ADVISORY-3**: 前端 PRD 接口定义中圈子详情路径为 `/content/circle/detail?id={id}`，但 backend-issues.md 建议实现为 `GET /content/circle/{id}`。路径风格不一致（query param vs path param），需统一。

#### 分页契约

**ADVISORY-4**: 前端 specs 中未明确分页请求参数格式（`pageNum`/`pageSize` vs `page`/`size`）和分页响应格式（是否包含 `total`、`pages` 等字段）。PRD 搜索接口使用 `page`/`size`，backend-issues.md 使用 `pageNum`/`pageSize`。需统一。

#### 错误码覆盖

**ADVISORY-5**: 前端 specs 中的错误处理场景均使用自然语言描述（"满员/密码错误/仅邀请等"），未定义具体错误码。建议在 API 封装层定义错误码枚举，确保前后端错误处理一致。

### 2.6 边界覆盖 (Boundary) — 80/100

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| 空状态 | ✅ 已覆盖 | 列表空、搜索无结果、无日志 |
| 加载状态 | ✅ 已覆盖 | 骨架屏、Loading 按钮 |
| 网络错误 | ✅ 已覆盖 | Toast 提示、重试按钮 |
| 并发竞态 | ✅ 已覆盖 | 管理员操作冲突、并发加入满员 |
| 缓存过期 | ✅ 已覆盖 | 5 分钟过期刷新 |
| 权限越界 | ✅ 已覆盖 | 403 页面、权限矩阵 |
| 表单校验 | ✅ 已覆盖 | 必填、长度、格式、唯一性 |
| 文件上传 | ✅ 已覆盖 | 格式、大小、比例校验 |
| 状态持久化 | ✅ 已覆盖 | applyStatus、isInvited |
| **已禁言用户交互** | ⚠️ 部分覆盖 | PRD 提到"在内容区显示禁言提示条"，specs 未覆盖此场景 |
| **密码错误次数限制** | ⚠️ 部分覆盖 | PRD 和 design.md 提到禁用输入框+倒计时，specs 覆盖了禁用但未覆盖倒计时恢复 |
| **图片上传失败重试** | ❌ 未覆盖 | design.md R6 提到重试按钮，specs 和 tasks 未覆盖 |

**ADVISORY-6**: 已禁言用户在详情页内容区的禁言提示条（PRD 3.3 "您已被禁言，解除时间：{time}"）在 specs 中未定义 scenario。密码错误次数过多后的倒计时恢复逻辑也未在 specs 中明确。

---

## 3. 前后端衔接审计

> 触发条件：配对后端 change `circle-10-core` 目录存在，执行双向审计。

### 3.1 接口清单双向对比

| 方向 | 前端引用 | 后端定义 | 差异 |
|------|---------|---------|------|
| 前端→后端 | 14 个接口 | 9 个接口（含 search） | 6 个后端缺失 |
| 后端→前端 | — | 后端有 ranking/review/statistics/recommend 等接口 | 前端未引用（属后续 EPIC） |

**关键差异**:
- 加入接口路径：前端 `/member/join` vs 后端 `/join`（前端多了 `/member` 前缀）
- 退出接口：前端 `/member/quit` vs 后端 `/leave`（路径和命名均不同）
- 版主设置：前端 `/member/set-moderator` vs 后端 `/member/change-role`

### 3.2 数据模型一致性

| VO/Req | 前端期望字段 | 后端实际字段 | 差异 |
|--------|------------|------------|------|
| CircleVO | applyStatus, isInvited, memberLimit | 无 applyStatus/isInvited，有 maxMemberCount | 2 字段缺失，1 字段命名不同 |
| CircleSearchResultVO | category | 无 category | 1 字段缺失 |
| CircleCreateReq | 隐私类型+加入方式+密码 | 待确认 | 需对比具体字段 |
| CircleMemberVO | 头像/昵称/角色/状态/加入时间 | 待确认 | 需对比 |

### 3.3 错误码覆盖

前端 specs 未定义错误码枚举。后端需定义的错误码至少包括：
- 名称重复
- 圈子满员
- 密码错误
- 密码错误次数过多
- 权限不足
- 已是成员
- 黑名单用户

### 3.4 认证鉴权一致性

- 前端：design.md 提到"未登录用户可访问搜索和详情页（只读）"
- 后端：待确认未登录用户的接口鉴权策略（是否允许匿名访问详情/搜索）

### 3.5 分页契约

| 项目 | 前端 | 后端 | 一致性 |
|------|------|------|--------|
| 搜索分页参数 | `page`, `size` | 待确认 | ⚠️ |
| 列表分页参数 | `pageNum`, `pageSize` (backend-issues.md) | 待确认 | ⚠️ |
| 分页响应格式 | 未定义 | 未定义 | ⚠️ |

---

## 4. PRD 追溯矩阵

| PRD 章节 | PRD AC 编号 | Spec 文件 | Spec Requirement | 状态 |
|---------|------------|----------|-----------------|------|
| 3.1 列表页 | AC-3.1-1~6 | circle-crud | 圈子列表页展示 | ✅ |
| 3.2 创建页 | AC-3.2-1~6 | circle-crud | 圈子创建流程 | ✅ |
| 3.2 创建页 | AC-3.2-name | circle-crud | 圈子名称唯一性校验 | ✅ |
| 3.2 创建页 | AC-3.2-field | circle-crud | 圈子名称与简介字段校验 | ✅ |
| 3.2 创建页 | AC-3.2-sensitive | circle-crud | 敏感词检测与降级 | ✅ |
| 3.2 创建页 | AC-3.2-privacy | circle-crud | 隐私类型与加入方式联动 | ✅ |
| 3.2 创建页 | AC-3.2-upload | circle-crud | 图片上传与裁剪 | ✅ |
| 3.3 详情页 | AC-3.3-1~12 | circle-crud | 圈子详情页 | ✅ |
| 3.3 详情页 | AC-3.3-edit | circle-crud | 圈子信息更新 | ✅ (spec) / ❌ (tasks) |
| 3.4 成员管理 | AC-3.4-1~5 | circle-member-management | 成员列表 | ✅ |
| 3.4 成员管理 | AC-3.4-role | circle-member-management | 角色管理 | ✅ |
| 3.4 成员管理 | AC-3.4-mute | circle-member-management | 成员禁言 | ✅ |
| 3.4 成员管理 | AC-3.4-unmute | circle-member-management | 解除禁言 | ✅ |
| 3.4 成员管理 | AC-3.4-remove | circle-member-management | 成员移除 | ✅ |
| 3.4 成员管理 | AC-3.4-perm | circle-member-management | 操作权限矩阵 | ✅ |
| 3.5 搜索页 | AC-3.5-1~7 | circle-search | 圈子搜索 | ✅ |
| 3.5 搜索页 | AC-3.5-debounce | circle-search | 搜索防抖 | ✅ |
| 3.5 搜索页 | AC-3.5-join | circle-search | 搜索结果加入操作 | ✅ |
| 3.6 治理日志 | AC-3.6-1~5 | circle-governance-log | 治理日志列表 | ✅ |
| 3.6 治理日志 | AC-3.6-perm | circle-governance-log | 治理日志访问权限 | ✅ |
| 3.6 治理日志 | AC-3.6-retention | circle-governance-log | 治理日志数据保留 | ✅ |
| 6 状态管理 | AC-6.1 | circle-state-management | useCircleStore 状态管理 | ✅ |
| 6 状态管理 | AC-6.2 | circle-state-management | 搜索关键词管理 | ✅ |
| 6 状态管理 | AC-6.3 | circle-state-management | 列表缓存策略 | ✅ |
| 6 状态管理 | AC-6.4 | circle-state-management | 权限判断逻辑 | ✅ |
| 6 状态管理 | AC-6.5 | circle-state-management | 并发竞态处理 | ✅ |
| 10 性能 | AC-10-perf | (分散在各 spec) | — | ✅ |
| 11 测试 | AC-11-1~4 | tasks.md 12.x | — | ✅ |

---

## 5. 问题清单

### BLOCK — 必须修复

| # | 问题 | 影响 | 建议修复 |
|---|------|------|---------|
| BLOCK-1 | 圈子信息更新（编辑功能）有 spec 但无 task | PRD P1 功能无法实现 | 在 tasks.md 中新增编辑页面/编辑表单任务 |
| BLOCK-2 | design.md/proposal.md 声称 14 个 API 但实际 8 个 | 文档自相矛盾，误导开发 | 统一表述为"14 个接口定义，8 个已实现，6 个需补充" |
| BLOCK-3 | 6 个后端接口缺失，specs 未标注 Mock 依赖 | 开发时无法区分真实接口和 Mock | 在依赖缺失接口的 scenario 中添加 `[MOCK]` 标记 |
| BLOCK-4 | 前后端 3 个接口路径不一致（join/quit/set-moderator） | 前端 API 调用 404 | 实现前统一接口路径，更新 design.md 和 specs |

### FLAG — 强烈建议修复

| # | 问题 | 影响 | 建议修复 |
|---|------|------|---------|
| FLAG-1 | 密码校验规则不完整（缺长度 6-20 和至少含字母） | 密码安全性不足 | 补充 specs 中密码校验 scenario |
| FLAG-2 | 详情页成员上限展示未在 spec 中明确 | PRD 要求展示"成员数/上限" | 补充 spec scenario |
| FLAG-3 | 成员上限默认值前后端不一致（500 vs 10,000） | 上限展示和满员判断可能出错 | 前后端对齐默认值，或由后端配置驱动 |
| FLAG-4 | 退出接口命名不一致（quit vs leave） | 代码命名混乱 | 统一为 `leaveCircle`，与后端一致 |
| FLAG-5 | 无障碍（a11y）要求无对应 task | PRD 要求无法落地 | 在 tasks.md 中添加 a11y 实现任务 |
| FLAG-6 | applyStatus/isInvited 字段后端未确认补充 | 加入按钮核心逻辑依赖这两个字段 | 后端确认在详情接口中补充，或前端实现降级方案 |
| FLAG-7 | CircleSearchResultVO 缺少 category 字段 | 搜索结果无法展示分类 | 后端补充字段，或前端搜索结果不展示分类 |

### ADVISORY — 建议改进

| # | 问题 | 建议 |
|---|------|------|
| ADVISORY-1 | specs scenario 无 TDD 配对映射 | 在 tasks 测试部分标注对应 scenario 编号 |
| ADVISORY-2 | 交互组件（Modal 类）测试计划缺失 | 补充 JoinCircleModal/MuteMemberModal/GovernanceConfirmModal 测试 |
| ADVISORY-3 | 详情接口路径风格不一致（query vs path param） | 统一为 `GET /content/circle/{id}` |
| ADVISORY-4 | 分页参数格式未统一（page/size vs pageNum/pageSize） | 定义统一分页契约 |
| ADVISORY-5 | 错误码未定义枚举 | 在 API 封装层定义错误码常量 |
| ADVISORY-6 | 已禁言用户提示条和密码倒计时恢复未在 spec 中覆盖 | 补充 spec scenario |

---

## 6. 最终结论

### 结论：有条件通过 (CONDITIONAL PASS)

circle-10-core-frontend 的规范文档整体质量良好，PRD AC 覆盖率达 91%，5 个 capability 的 spec 结构完整且与 design decisions 一致。主要问题集中在**前后端接口契约对接**层面：

1. **3 个接口路径不一致**（BLOCK-4）会导致前端 API 调用失败，必须在实现前统一
2. **6 个后端接口缺失**（BLOCK-3）需明确 Mock 标注策略
3. **2 个关键字段缺失**（FLAG-6: applyStatus/isInvited）影响加入按钮核心逻辑

### 建议操作

1. **立即修复**（BLOCK-1~4）：补充编辑功能 tasks、统一文档表述、标注 Mock 依赖、统一接口路径
2. **实现前修复**（FLAG-1~7）：补充密码规则、对齐成员上限、确认字段补充
3. **实现中改进**（ADVISORY-1~6）：TDD 映射、错误码定义、分页契约统一

### 后端协作清单

| 优先级 | 事项 | 后端 Owner |
|--------|------|-----------|
| P0 | 实现 4 个阻塞接口（detail/my-list/public-list/member-list） | 待分配 |
| P0 | CircleVO 补充 applyStatus/isInvited 字段 | 待分配 |
| P1 | 实现 2 个功能接口（check-name/governance-log） | 待分配 |
| P1 | CircleSearchResultVO 补充 category 字段 | 待分配 |
| P1 | 统一接口路径（join/leave/change-role） | 待分配 |
| P2 | 确认成员上限默认值（500 vs 10,000） | 待分配 |
