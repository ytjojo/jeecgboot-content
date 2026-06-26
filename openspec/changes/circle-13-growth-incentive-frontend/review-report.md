# 规范审核报告: circle-13-growth-incentive-frontend

> **审核日期**: 2026-06-25（初次审核）→ 2026-06-26（补充澄清）
> **审核工具**: openspec-review-change
> **Change 类型**: 前端
> **业务域**: circle
> **EPIC**: EPIC-13
> **关联 PRD**: docs/requirements/prd/decomposition/circle/EPIC-13-circle-growth-incentive.md
> **关联 Change**: circle-13-growth-incentive（后端配对）
> **审核模式**: 模式 B（后端已完成，后端 change 已有完整 spec 和 design）

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 7/10 | 1 | 1 | 0 |
| 一致性 (Consistency) | 4/10 | 2 | 1 | 0 |
| 可实现性 (Feasibility) | 8/10 | 0 | 3 | 0 |
| 可测试性 (Testability) | 7/10 | 0 | 3 | 0 |
| 接口契约 (API Contract) | 4/10 | 1 | 0 | 0 |
| 边界覆盖 (Boundary) | 6/10 | 1 | 3 | 0 |
| 安全性 (Security) | 7/10 | 0 | 1 | 0 |
| 性能 (Performance) | 9/10 | 0 | 0 | 0 |
| 可维护性 (Maintainability) | 9/10 | 0 | 0 | 0 |
| 文档规范 (Documentation Standards) | 8/10 | 0 | 2 | 0 |
| **综合** | **69/100** | **5** | **14** | **0** |

---

## 量化指标

| 指标 | 分子 | 分母 | 百分比 | 阈值 | 状态 |
|------|------|------|--------|------|------|
| PRD AC 覆盖率 | 21 | 23 | 91% | >=80% | PASS |
| API 契约完整率 | 5 | 8 | 63% | >=90% | FAIL |
| 边界条件覆盖率 | 6 | 10 | 60% | >=60% | PASS |
| TDD 配对率 | N/A | N/A | N/A | >=70% | N/A（前端 tasks 无独立测试任务） |
| Scenario 完整率 | 47 | 17 | 2.8/req | >=3/req | FAIL |
| 后端 API 满足率 | 5 | 8 | 63% | =100% | FAIL |
| 数据库表满足率 | N/A | N/A | N/A | =100% | N/A（前端不直接引用表） |
| 前端组件满足率 | 10 | 10 | 100% | >=90% | PASS |
| 依赖阻塞项数 (P0) | 2 | - | - | =0 | FAIL |

---

## 1. 完整性审核

### 1.1 文档结构完整性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal.md 存在 | PASS | 文件存在，包含 Why/What Changes/Scope Clarification/Capabilities/Impact |
| proposal.md 核心章节完整 | PASS | Why、What Changes、Capabilities、Impact 均存在，Scope Clarification 清晰区分三个成长体系 |
| design.md 存在 | PASS | 文件存在 |
| design.md 核心章节完整 | PASS | Context、Goals/Non-Goals、Decisions(10项)、Risks/Trade-offs、Open Questions、VO 字段映射均存在 |
| specs/ 目录存在且含 spec.md | PASS | 4 个 spec 目录，每个含 spec.md |
| 每个 spec.md 包含 Requirement 和 Scenario | PASS | 所有 spec 均使用规范的 ADDED Requirements 格式 |
| tasks.md 存在且格式正确 | PASS | 64 个任务，8 个分组，`- [ ]` 格式正确 |
| Capabilities 与 specs 一一对应 | PASS | circle-level→circle-level、member-growth→member-growth、badge-system→badge-system、leaderboard→leaderboard |
| design.md 包含路由方案决策 | PASS | D5 明确定义了 3 个子路由和路由结构 |
| design.md 包含状态管理方案决策 | PASS | D2 定义了 circleGrowth Store 和按 circleId 缓存策略 |
| design.md 包含组件拆分决策 | PASS | D1 定义了组件目录组织方式，proposal 列出了 10 个组件 |
| specs 包含页面级交互场景 | PASS | 所有 spec 使用 WHEN/THEN 格式 |
| tasks.md 包含响应式适配任务 | PASS | 任务 7.1-7.3 覆盖三个断点响应式 |
| proposal.md Impact 列出 API 接口依赖清单 | PASS | Impact 节列出了核心接口和可选接口 |

### 1.2 完整性问题清单

#### BLOCK-001: proposal.md 核心 API 路径与 design.md/specs 不一致

- **位置**: `proposal.md` 第 11 行 vs `design.md` Context（第 7-10 行）+ 各 spec 文件
- **描述**: proposal.md 第 11 行列出的 4 个核心接口中，有 3 个路径使用了错误前缀：
  - 成就徽章列表: proposal 写 `GET /api/v1/content/circle/growth/achievement/list`，design/specs 正确为 `GET /api/v1/content/user/growth/achievement/list`
  - 排行榜: proposal 写 `GET /api/v1/content/circle/growth/leaderboard`，design/specs 正确为 `GET /api/v1/content/user/growth/leaderboard`
  - 成员成长: proposal 写 `GET /api/v1/content/circle/growth/info`，design/specs 正确为 `GET /api/v1/content/user/growth/info`
  - 仅圈子等级接口路径正确（`/api/v1/content/circle/growth/level/info`）
- **影响**: 若开发者以 proposal.md 为依据实现，将调用错误的 API 前缀，导致 3 个核心功能（成员成长、徽章、排行榜）接口 404。
- **建议修复**: 修正 proposal.md 第 11 行的 API 路径：
  - `GET /api/v1/content/circle/growth/info` → `GET /api/v1/content/user/growth/info`
  - `GET /api/v1/content/circle/growth/achievement/list` → `GET /api/v1/content/user/growth/achievement/list`
  - `GET /api/v1/content/circle/growth/leaderboard` → `GET /api/v1/content/user/growth/leaderboard`

#### FLAG-001: tasks.md 缺少 DoD 标准收尾项

- **位置**: `tasks.md`
- **描述**: tasks.md 包含 8 个分组共 64 个任务，但缺少 AGENTS.md 要求的 DoD 收尾项：流程确认（subagent+TDD）、Code Review、覆盖率≥90%、模块全量测试、合并+验证+清理 worktree。
- **影响**: 开发完成后可能遗漏必要的质量门禁步骤。
- **建议修复**: 在 tasks.md 末尾补充第 9 分组「9. 质量门禁与收尾」，包含 DoD 要求的 5 项检查任务。

---

## 2. 一致性审核

### 2.1 跨文档一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal Capabilities 与 specs 对应 | PASS | 4 个 Capability 均有对应 spec |
| design.md Decisions 与 specs Requirement 无矛盾 | **FAIL** | API 路径在 proposal 与 design/specs 之间矛盾；等级降级规则前后端矛盾 |
| tasks.md 任务与 specs Requirement 可追溯 | PASS | tasks 分组与 specs 一一对应 |
| design.md VO 字段映射与 specs 引用一致 | PASS | 所有 spec 引用的字段名与 design.md VO 映射表一致 |
| 术语使用一致（徽章 vs Achievement） | PASS | D7 明确定义了 UI 层用「徽章」、代码层用 Achievement 的约定 |

### 2.2 前端特有一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| specs 引用 API 路径与后端 design.md D7 定义一致 | PARTIAL | design.md 和 specs 正确使用了双前缀约定，但 proposal.md 不一致 |
| specs 状态字段名与后端 VO 字段名一致 | PASS | D7 明确不做字段重命名，直接使用后端字段名 |
| design.md 路由路径与 specs 页面跳转路径一致 | PASS | specs 未硬编码路由跳转，无矛盾 |

### 2.3 一致性问题清单

#### BLOCK-002: 等级降级规则与后端定义严重矛盾

- **位置**: `specs/member-growth/spec.md` 第 95-97 行（"等级下降展示" Scenario）vs 后端 `specs/member-experience/spec.md` 第 26-28 行（"Level does not downgrade" Scenario）+ 后端 `design.md` 第 54 行关键区别表
- **描述**: 
  - **后端明确规则**（member-experience spec）: "WHEN 经验值因内容删除/违规被扣减至低于当前等级门槛，THEN 成员等级 SHALL NOT 降级，仅经验值数值减少"
  - **后端 design.md 关键区别表**也明确标注成员等级「不降级（经验值可扣减，等级不变）」，理由是「避免因少量经验值回退导致等级频繁波动」
  - **前端 spec 错误要求**（member-growth spec）: "WHEN 经验值扣减导致低于当前等级门槛，THEN 等级下降一级，页面展示更新后的等级标识"
- **影响**: 前端按此 spec 实现会展示等级下降，但后端实际不降级，导致前后端数据不一致。用户看到等级下降但刷新后等级恢复，造成困惑和 bug 反馈。
- **建议修复**: 删除 `specs/member-growth/spec.md` 中"等级下降展示" Scenario，替换为符合后端规则的 Scenario：
  ```
  #### Scenario: 经验值扣减但等级不下降
  - **WHEN** 经验值因内容删除/撤回/违规被扣减至低于当前等级门槛
  - **THEN** 成员等级保持不变，仅经验值数值和进度条更新，页面提示「经验值已调整」
  ```

#### BLOCK-003: proposal.md API 路径前缀错误导致跨文档不一致（同 BLOCK-001）

- **位置**: `proposal.md` 第 11 行
- **描述**: proposal.md 作为 change 的入口文档，API 路径与 design.md、specs、后端 design.md D7 决策均不一致，将成员成长、徽章、排行榜三个核心接口错误放在 `/circle/growth/` 前缀下。
- **影响**: 开发者或审核者可能被 proposal.md 误导使用错误路径。
- **建议修复**: 同 BLOCK-001，修正 proposal.md 中的 API 路径。

#### FLAG-002: proposal.md 第 11 行成员成长接口路径拼写不完整

- **位置**: `proposal.md` 第 11 行
- **描述**: proposal.md 写 `GET /api/v1/content/circle/growth/info`，即使在错误的 `/circle/growth/` 前缀下，也缺少 `/level/` 段（圈子等级接口是 `/circle/growth/level/info`，成员成长接口应是 `/user/growth/info`）。
- **影响**: 路径完全错误，无法定位到任何 Controller。
- **建议修复**: 修正为 `GET /api/v1/content/user/growth/info?circleId=&userId=`。

---

## 3. 可实现性审核

### 3.1 前端技术兼容性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 组件库与项目兼容 (Ant Design Vue 4) | PASS | 使用 Modal、Card、Tabs、Segmented 等 Ant Design Vue 组件 |
| 状态管理兼容 (Pinia) | PASS | D2 定义 Pinia Store，与项目技术栈一致 |
| API 调用使用 defHttp | PASS | tasks 1.1 定义 API 封装文件，使用 defHttp |
| 路由方案与现有权限模式兼容 | PASS | D5 使用子路由挂载，复用圈子详情页布局容器 |
| 不包含 Non-Goals 功能 | PASS | Non-Goals 明确排除付费、跨圈子合并、自定义徽章、社交分享、虚拟滚动等 |
| WebSocket 复用已有通道 | PASS | D4 复用已有站内通知 WebSocket 基础设施 |

### 3.2 可实现性问题清单

#### FLAG-003: WebSocket 通知消息体格式仅有假设，容错逻辑不明确

- **位置**: `design.md` D4 决策 + Risks 节 + Open Questions Q2
- **描述**: D4 提到"监听已有站内通知 WebSocket 消息，识别等级提升和徽章获得事件"，但 Risks 也承认"WebSocket 通知消息体格式不确定"，Open Questions Q2 标记为"假设包含，前端预留容错"。specs 中仅 badge-system 有 Toast 和刷新的 Scenario，但未定义容错逻辑细节（如字段缺失时如何降级）。
- **影响**: 若消息体格式与假设不符，通知监听可能静默失败，用户收不到等级提升/徽章获得提示。
- **建议修复**: 在 badge-system spec 或 design.md 中补充容错 Scenario："WHEN WebSocket 消息体缺少 circleId 或通知类型字段，THEN 仅展示通用 Toast 不触发数据刷新"。

#### FLAG-004: streakDetail 7天每日明细需后端扩展，缺少降级方案

- **位置**: `design.md` D9 字段对接说明
- **描述**: D9 标注 `streakDetail: boolean[]`（7天每日参与状态）需单独调用 `/participation` 接口，但当前后端该接口仅返回连续天数（`participationDays`），7天每日明细需后端扩展。前端 spec（member-growth）要求展示 7 天时间轴，用实心圆/空心圆/横线标记每日状态，但后端当前不返回每日明细数据。
- **影响**: 连续参与 7 天时间轴无法按 spec 实现（只能展示连续天数，无法展示每日状态）。
- **建议修复**: 二选一：
  1. 在 spec 中明确降级方案：后端扩展前，时间轴仅展示连续天数数字，不渲染 7 天每日圆点；或
  2. 推动后端在 `/participation` 接口中补充 `streakDetail: boolean[]` 字段，并标记为 BLOCK 级依赖。

#### FLAG-005: totalBadges/totalBadgeCount 字段后端暂未提供，缺少降级方案

- **位置**: `design.md` D9 字段对接说明
- **描述**: D9 标注 `totalBadges`/`totalBadgeCount` MemberGrowthVO 暂未提供，待后端补充。badge-system spec 涉及徽章总数展示，但未说明该字段缺失时如何处理。
- **影响**: 徽章总数展示可能为空或显示异常。
- **建议修复**: 在 badge-system spec 中补充降级方案：字段缺失时通过徽章列表长度计算总数，或隐藏徽章总数展示。

---

## 4. 可测试性审核

### 4.1 Scenario 质量

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 关键交互 Scenario 有明确用户操作→系统响应 | PASS | 所有 spec 使用 WHEN/THEN 格式 |
| 错误场景有明确 UI 反馈描述 | PARTIAL | 接口失败有重试按钮，但缺少细节 |
| 异步操作有明确 loading/error/success 状态描述 | PARTIAL | 各 spec 有"加载中状态"和"接口请求失败"，但其他异步场景（如 WebSocket 通知）覆盖不足 |
| 核心用户旅程覆盖完整 | PASS | 从等级展示→成长页→徽章墙→排行榜→通知的主路径覆盖完整 |

### 4.2 可测试性问题清单

#### FLAG-006: 徽章撤销状态缺少具体 UI 展示细节

- **位置**: `specs/badge-system/spec.md` 第 55-69 行（"徽章撤销状态展示" Requirement）
- **描述**: spec 定义撤销徽章"展示为灰色并标注「已撤销」"，但未说明"已撤销"标签的具体样式（位置、颜色、是否可点击、是否显示撤销原因）。"不再计入已获得徽章总数"也未说明总数如何计算。
- **影响**: 开发和测试对撤销徽章的展示方式理解可能不一致。
- **建议修复**: 补充 Scenario 细节："已撤销标签位置在徽章卡片右上角，使用灰色文字带删除线样式，不可点击查看详情"。

#### FLAG-007: 等级提升通知的 WebSocket 场景缺失

- **位置**: `specs/member-growth/spec.md` + `specs/circle-level/spec.md`
- **描述**: badge-system spec 有"获得新徽章 Toast 提示"和"获得新徽章后刷新徽章数据"两个 Scenario，但 member-growth 和 circle-level 中缺少等级提升通知的对应 Scenario。proposal 第 15 行和 tasks 6.1-6.2 提到等级提升也要触发 Toast 和数据刷新，但 specs 未覆盖。
- **影响**: 等级提升通知的 UI 行为无验收标准。
- **建议修复**: 在 member-growth spec（成员等级提升）和 circle-level spec（圈子等级提升）中分别补充 WebSocket 通知 Scenario。

#### FLAG-008: 排行榜数据为小时级快照，无实时刷新提示

- **位置**: `specs/leaderboard/spec.md` + 后端 `specs/circle-leaderboard/spec.md`
- **描述**: 后端明确排行榜每小时刷新一次（快照机制），前端 spec 中未说明需要向用户展示"数据更新时间"或"排行榜每小时更新"提示。用户可能疑惑为什么刚获得经验值排行榜未立即变化。
- **影响**: 用户可能误以为排行榜功能有延迟 bug。
- **建议修复**: 在 leaderboard spec 中补充 Scenario："WHEN 用户查看排行榜，THEN 页面底部展示「榜单每小时更新一次，上次更新时间：{time}」提示"。

---

## 5. 接口契约审核

### 5.1 API 路径与方法对齐

| # | 前端 proposal 引用 | 前端 design/specs 引用 | 后端实际 | 状态 | 问题类型 |
|---|-------------------|----------------------|---------|------|---------|
| 1 | GET /api/v1/content/circle/growth/level/info | GET /api/v1/content/circle/growth/level/info?circleId= | GET /api/v1/content/circle/growth/level/info?circleId= (CircleLevelController) | ✅ OK | 匹配 |
| 2 | GET /api/v1/content/circle/growth/info ❌ | GET /api/v1/content/user/growth/info?circleId=&userId= | GET /api/v1/content/user/growth/info?circleId=&userId= (MemberGrowthController) | **BLOCK** | proposal 路径前缀错误（应为 /user/ 非 /circle/）|
| 3 | GET /api/v1/content/circle/growth/achievement/list ❌ | GET /api/v1/content/user/growth/achievement/list?circleId=&userId= | GET /api/v1/content/user/growth/achievement/list?circleId=&userId= (AchievementController) | **BLOCK** | proposal 路径前缀错误（应为 /user/ 非 /circle/）|
| 4 | GET /api/v1/content/circle/growth/leaderboard ❌ | GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId= | GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId= (LeaderboardController) | **BLOCK** | proposal 路径前缀错误（应为 /user/ 非 /circle/）|
| 5 | GET /api/v1/content/user/growth/participation（可选接口） | GET /api/v1/content/user/growth/participation?circleId=&userId= | GET /api/v1/content/user/growth/participation?circleId=&userId= (MemberGrowthController) | ✅ OK | 匹配（design/specs 正确）|
| 6 | 7 个可选接口（summary/badge/catalog/badge/detail/badge/wear/level/benefit/level/config） | 未在 specs 中对接 | 后端已提供 | ⚠️ FLAG | proposal 列出但未纳入本期实现 |
| 7 | POST 徽章佩戴（badge/wear） | 未引用 | 后端已提供 | ⚠️ FLAG | proposal 列出但本期 Non-Goals 未包含佩戴功能，前后端范围一致但 proposal 列在可选接口易误解 |
| 8 | 查询参数 circleId/userId | specs 中各接口均包含 circleId，member-growth 等包含 userId | 后端接口均需要 circleId（/user/growth/ 还需要 userId） | ✅ OK | 匹配 |

**对齐统计**:
- 核心接口总数（前端消费）: 5 个（4 个核心 + 1 个连续参与）
- 已对齐（design/specs 层面）: 5 个
- proposal 层面路径错误: 3 个
- 可选接口未纳入本期: 7 个（合理，属于 Non-Goals 范围外增强）

### 5.2 接口契约问题清单

#### BLOCK-004: proposal.md 中 3 个核心 API 路径前缀错误（同 BLOCK-001/003）

- **位置**: `proposal.md` 第 11 行
- **描述**: proposal.md 作为 change 的第一入口文档，将成员成长、成就徽章、排行榜三个核心接口错误放在 `/api/v1/content/circle/growth/` 前缀下，违反后端 design.md D7 明确的路径划分规则（`/circle/growth/` 仅用于圈子等级，其他均在 `/user/growth/`）。design.md 和 specs 中路径是正确的，但 proposal.md 与它们不一致。
- **影响**: 若实现者参考 proposal.md（而非通读 design.md 和 specs），将调用错误路径导致 404。
- **建议修复**: 统一修正 proposal.md 第 11 行的三个 API 路径，与 design.md、specs、后端保持一致。

---

## 6. 边界覆盖审核

### 6.1 通用边界覆盖

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| null/空值输入处理 | COVERED | 空状态 Scenario 覆盖徽章/排行榜/连续参与无数据场景 |
| 超长/超大值输入处理 | N/A | 本期前端无表单输入场景 |
| 格式不合法输入处理 | N/A | 本期前端无表单输入场景 |
| 唯一约束冲突处理 | N/A | 无写入操作 |
| 并发/竞态条件处理 | **GAP** | 未覆盖多设备同时打开成长页、WebSocket 通知到达时正在请求接口等场景 |
| 权限不足/未认证处理 | COVERED | circle-level spec 覆盖私有圈子未加入成员的提示 |
| 资源不存在处理 | **GAP** | 未覆盖圈子已解散、成员已退出圈子等场景 |
| 外部服务不可用降级 | PARTIAL | 接口失败有重试按钮，但未覆盖 WebSocket 不可用场景 |
| 网络超时/断网处理 | **GAP** | 仅笼统"接口请求失败"，未定义超时阈值和断网专属 UI |
| 数据不一致/脏数据处理 | **GAP** | 等级降级规则矛盾本身就是数据一致性问题（见 BLOCK-002） |

**边界覆盖率**: 6/10 (60%)

### 6.2 前端特有边界

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| 网络超时/断网 UI 反馈 | **GAP** | 未专门定义超时和断网的差异化提示 |
| Token 过期自动刷新和重试 | **GAP** | 依赖全局机制，spec 未提及 |
| 表单重复提交防护 | N/A | 本期无表单提交 |
| 移动端/平板响应式边界 | COVERED | tasks 7.1-7.3 覆盖三个断点 |
| 空数据状态 UI 展示 | COVERED | 各 spec 覆盖空状态展示 |
| 骨架屏加载态 | COVERED | 各 spec 有"加载中状态"Scenario，tasks 7.4 明确骨架屏集成 |
| 最高等级边界 | COVERED | circle-level 和 member-growth 均有"最高等级展示"Scenario |
| 每日经验上限边界 | COVERED | member-growth 有"达到每日上限"Scenario |
| 排行榜 Top 50 边界 | COVERED | leaderboard 有"不足 50 人"和"Top 50 内/外"Scenario |

### 6.3 边界覆盖问题清单

#### BLOCK-005: 等级降级规则导致的数据一致性边界问题（同 BLOCK-002）

- **位置**: `specs/member-growth/spec.md` 第 95-97 行
- **描述**: 前端要求等级下降但后端不降级，是核心数据一致性边界问题。
- **影响**: 前后端等级展示不一致。
- **建议修复**: 同 BLOCK-002，修正 Scenario 为等级不下降。

#### FLAG-009: 缺少圈子已解散/成员已退出圈子的资源不存在场景

- **位置**: 所有 specs
- **描述**: 用户访问 `/circle/:id/growth` 时圈子可能已解散、用户可能已被移除或退出圈子，但所有 spec 均未覆盖此场景。仅 circle-level spec 覆盖了"私有圈子未加入成员"的权限场景，但未覆盖"资源不存在"场景。
- **影响**: 圈子解散后用户访问成长页可能看到错误数据或空白页。
- **建议修复**: 在各页面 spec 中补充："WHEN 圈子已解散或用户已不在圈子中，THEN 展示「圈子已不存在」提示并提供返回圈子列表入口"。

#### FLAG-010: 缺少网络超时与断网差异化处理

- **位置**: 所有 specs
- **描述**: 所有 spec 的"接口请求失败"场景仅笼统描述"展示错误提示和重试按钮"，未区分网络超时、服务端 500、断网等不同错误类型的用户提示。
- **影响**: 网络异常时用户无法区分是自己网络问题还是服务端问题。
- **建议修复**: 在通用交互规则中补充：网络超时显示"请求超时，请检查网络"，服务端错误显示"服务暂时不可用，请稍后重试"，并确保重试按钮可用。

#### FLAG-011: WebSocket 连接不可用时的降级方案缺失

- **位置**: `design.md` D4 + badge-system spec
- **描述**: D4 依赖已有 WebSocket 通道，但未说明 WebSocket 连接失败或断开时的降级方案（如是否降级为轮询、是否提示用户实时通知不可用）。
- **影响**: WebSocket 不可用时用户无法收到等级提升和徽章通知，但页面无任何提示。
- **建议修复**: 在 design.md Risks 或 specs 中补充降级策略：WebSocket 连接失败时降级为每 60 秒轮询一次成长数据，或在页面顶部显示「实时通知未连接」提示。

---

## 7. 安全性审核

### 7.1 安全检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 私有圈子权限控制 | PASS | circle-level spec 覆盖未加入成员不展示成长区块 |
| 接口鉴权依赖全局机制 | PASS | 项目已有统一权限拦截器，spec 未绕过 |
| XSS 防护 | **GAP** | 未明确徽章名称、描述、用户名等用户生成内容的 XSS 转义 |
| 敏感信息泄露 | PASS | 成长数据非敏感信息，无额外泄露风险 |
| CSRF 防护 | PASS | 依赖项目全局 CSRF 机制（本期无 POST/PUT/DELETE 写入操作）|

### 7.2 安全性问题清单

#### FLAG-012: 未明确 XSS 防护策略

- **位置**: 所有涉及用户生成内容展示的 specs（badge-system、leaderboard）
- **描述**: 徽章名称（`name`）、徽章描述（`description`）、用户名（`username`）等字段由后端返回，前端在渲染时未明确说明需要进行 XSS 转义。虽然 Vue 默认有模板转义，但使用 `v-html` 渲染富文本时可能引入风险。
- **影响**: 若后端存储了恶意脚本内容（如徽章描述被注入），可能导致 XSS 攻击。
- **建议修复**: 在 design.md 中明确："所有后端返回的文本字段使用 Vue 默认模板插值渲染（自动转义），禁止使用 v-html 渲染用户生成内容"。

---

## 8. 性能审核

### 8.1 性能检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| P95 < 1 秒性能目标明确 | PASS | Goals 明确定义页面加载性能 P95 < 1 秒 |
| API 并行请求策略 | PASS | D3 定义个人成长页使用 Promise.all 并行请求 3 个接口 |
| 数据缓存策略 | PASS | D2 定义 Pinia Store 按 circleId 缓存，避免重复请求 |
| 路由懒加载 | PASS | tasks 7.7 明确成长相关页面使用动态 import 按需加载 |
| 排行榜虚拟滚动排除合理 | PASS | Non-Goals 明确不实现虚拟滚动（Top 50 数据量小，不需要）|
| 骨架屏提升感知性能 | PASS | tasks 7.4 明确所有页面首屏使用骨架屏 |

### 8.2 性能问题清单

（无 BLOCK/FLAG/ADVISORY 问题，性能设计良好）

---

## 9. 可维护性审核

### 9.1 可维护性检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 组件目录组织清晰 | PASS | D1 定义所有成长组件放在 `src/components/circle/growth/` 目录下 |
| 状态管理内聚 | PASS | D2 定义独立 `circleGrowth` Pinia Store，不污染其他 store |
| 术语映射明确 | PASS | D7 明确定义 UI 层用「徽章」、代码层用 Achievement 的命名约定 |
| 后端 VO 字段直接复用 | PASS | D7/D9 明确 TypeScript 类型直接使用后端 VO 字段名，不做重命名映射，减少维护成本 |
| TypeScript 类型定义 | PASS | tasks 1.1 明确 API 封装包含对应 TypeScript 类型 |
| Decisions 记录完整 | PASS | 10 项设计决策（D1-D10）均记录了选择、理由和替代方案 |

### 9.2 可维护性问题清单

（无 BLOCK/FLAG/ADVISORY 问题，可维护性设计优秀）

---

## 10. 文档规范审核

### 10.1 文档规范检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal/design/specs/tasks 结构完整 | PASS | 四个核心文档均存在且结构规范 |
| Scenario 使用 WHEN/THEN 格式 | PASS | 所有 Scenario 均遵循 Gherkin 风格的 WHEN/THEN 格式 |
| 术语定义清晰 | PASS | Scope Clarification 用表格清晰区分三个成长体系，避免混淆 |
| Open Questions 状态明确 | PASS | 5 个 Open Questions 中 3 个已确认，2 个明确假设和当前状态 |
| VO 字段映射表完整 | PASS | design.md 提供 4 个 VO 的完整字段映射表，含对接说明 |
| Risks 有缓解措施 | PASS | 5 项风险均有对应的缓解措施说明 |
| 跨文档引用一致 | **FAIL** | proposal.md API 路径与 design/specs 不一致（已在一致性维度记录）|
| DoD 收尾项完整 | **FAIL** | tasks.md 缺少质量门禁收尾任务 |

### 10.2 文档规范问题清单

#### FLAG-013: tasks.md 缺少 DoD 标准收尾项（同 FLAG-001）

- **位置**: `tasks.md`
- **描述**: 缺少 AGENTS.md 要求的流程确认、Code Review、覆盖率、模块测试、合并清理等收尾任务。
- **影响**: 质量门禁步骤可能被遗漏。
- **建议修复**: 补充第 9 分组收尾任务。

---

## 11. 前后端衔接审计

### 11.1 接口清单双向对比

| # | 后端 API | 后端 Controller | 前端 spec 是否引用 | 路径是否匹配 | 状态 |
|---|---------|----------------|-------------------|-------------|------|
| 1 | GET /api/v1/content/circle/growth/level/info?circleId= | CircleLevelController | ✅ circle-level spec | ✅ 匹配 | OK |
| 2 | GET /api/v1/content/user/growth/info?circleId=&userId= | MemberGrowthController | ✅ member-growth spec | ✅ design/specs 匹配，proposal 不匹配 | **BLOCK (proposal)** |
| 3 | GET /api/v1/content/user/growth/achievement/list?circleId=&userId= | AchievementController | ✅ badge-system spec | ✅ design/specs 匹配，proposal 不匹配 | **BLOCK (proposal)** |
| 4 | GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId= | LeaderboardController | ✅ leaderboard spec | ✅ design/specs 匹配，proposal 不匹配 | **BLOCK (proposal)** |
| 5 | GET /api/v1/content/user/growth/participation?circleId=&userId= | MemberGrowthController | ✅ member-growth spec + design.md Context | ✅ 匹配 | OK |
| 6 | GET /api/v1/content/user/growth/summary | - | ❌ 未引用 | N/A | 可选，未纳入本期 |
| 7 | GET /api/v1/content/user/growth/badge/catalog | - | ❌ 未引用 | N/A | 可选，未纳入本期 |
| 8 | GET /api/v1/content/user/growth/badge/detail | - | ❌ 未引用 | N/A | 可选，未纳入本期 |
| 9 | POST /api/v1/content/user/growth/badge/wear | - | ❌ 未引用 | N/A | 可选，徽章佩戴属于后续功能 |
| 10 | GET /api/v1/content/circle/growth/level/benefit | - | ❌ 未引用 | N/A | 可选，未纳入本期 |
| 11 | GET /api/v1/content/circle/growth/level/config | - | ❌ 未引用 | N/A | 可选，未纳入本期 |

**接口对齐统计**:
- 后端核心 API 总数: 5 个
- 前端 design/specs 正确引用: 5 个
- 前端 proposal 路径错误: 3 个
- 可选 API 未纳入本期: 6 个（合理，属于 Non-Goals）
- 前端未引用但后端不存在: 0 个

### 11.2 数据模型一致性

| 数据模型/字段 | 后端定义 | 前端引用 | 状态 |
|-------------|---------|---------|------|
| CircleLevelVO | 后端 design.md File Structure 定义 | design.md 完整字段映射（10个字段） | ✅ MATCH |
| MemberGrowthVO | 后端 design.md 定义（含重复字段排版问题） | design.md 完整字段映射（12个字段） | ✅ MATCH（注意后端 design.md 有字段重复排版错误）|
| AchievementVO | 后端通过 AchievementServiceImpl 实现 | design.md 完整字段映射（10个字段） | ✅ MATCH |
| LeaderboardEntryVO | 后端 design.md 定义 | design.md 完整字段映射（7个字段） | ✅ MATCH |
| 成员等级是否降级 | **不降级**（member-experience spec + design.md） | **要求降级**（member-growth spec "等级下降展示"） | ❌ **BLOCK 矛盾** |
| streakDetail: boolean[] | 后端 participation 接口当前仅返回 participationDays，未返回每日明细数组 | design.md D9 标注需后端扩展 | ⚠️ FLAG 待后端扩展 |
| totalBadges/totalBadgeCount | 后端 MemberGrowthVO 暂未提供此字段 | design.md D9 标注待后端补充 | ⚠️ FLAG 待后端补充 |
| 徽章佩戴 (badge/wear) | 后端已提供接口 | 前端本期不实现徽章佩戴功能 | ✅ 范围一致（Non-Goals 未包含） |
| 等级提升通知 | 后端发送站内通知 | 前端 WebSocket 监听 + Toast | ✅ MATCH |
| 徽章获得通知 | 后端发送站内通知 | 前端 WebSocket 监听 + Toast + 刷新 | ✅ MATCH |

### 11.3 错误码覆盖

| 错误场景 | 后端处理 | 前端 spec 处理 | 状态 |
|----------|---------|---------------|------|
| 接口请求成功 | 返回 Result<T> | 解析 VO 渲染 | ✅ COVERED |
| 接口请求失败（通用） | 返回 Result 错误码 | 展示错误提示 + 重试按钮 | ✅ COVERED |
| 加载中状态 | - | 展示骨架屏 | ✅ COVERED |
| 私有圈子未加入 | 403 或业务错误 | 不展示成长区块，显示「加入圈子后查看」 | ✅ COVERED |
| 空数据（无徽章/无排行/无参与） | 返回空数组 | 展示空状态引导 | ✅ COVERED |
| 网络超时/断网 | - | 未单独定义 | ❌ GAP |
| Token 过期 | 全局拦截器处理（401→刷新/跳转登录）| 依赖全局机制，spec 未提及 | ⚠️ 依赖全局 |
| 圈子已解散/资源不存在 | 404 | 无对应 Scenario | ❌ GAP |
| 成员已退出圈子 | 403/404 | 无对应 Scenario | ❌ GAP |
| WebSocket 消息格式错误 | - | 仅笼统说"预留容错"，无具体 Scenario | ⚠️ GAP |

### 11.4 业务规则一致性

| 业务规则 | 后端定义 | 前端定义 | 状态 |
|----------|---------|---------|------|
| 圈子等级数 | L1-L5 五级（L1新芽圈→L5标杆圈） | L1-L5 五级，颜色映射定义 | ✅ MATCH |
| 圈子等级门槛 | L1:0, L2:100, L3:300, L4:600, L5:850 | 依赖后端返回 nextLevelThreshold，不硬编码 | ✅ MATCH |
| 成员等级数 | L1-L5 五级（L1初来乍到→L5圈中领袖） | L1-L5 五级 | ✅ MATCH |
| 成员等级门槛 | L1:0, L2:100, L3:300, L4:600, L5:1000 | 依赖后端返回 nextLevelThreshold，不硬编码 | ✅ MATCH |
| 成员等级是否降级 | **不降级** | **要求降级** | ❌ **BLOCK 矛盾** |
| 每日经验上限 | 100 点 | 直接使用 dailyExpLimit 字段，不硬编码 | ✅ MATCH |
| 经验值获取规则 | 发帖+10、评论+3、加精+30/+50 | 仅展示，不实现获取逻辑（后端处理）| ✅ MATCH |
| 排行榜维度 | 经验值/贡献值/发帖数 3 维度 | 经验值/贡献值/发帖数 3 维度 | ✅ MATCH |
| 排行榜周期 | 本周/本月/累计 3 周期 | 本周/本月/累计 3 周期 | ✅ MATCH |
| 排行榜 Top N | Top 50 | Top 50 | ✅ MATCH |
| 排行榜刷新频率 | 每小时快照 | 接受延迟 | ✅ MATCH（但缺少"每小时更新"提示）|
| 徽章种类（首期）| ach_001-004 四种（持续创作者/优质贡献者/活跃参与者/圈内新星）| badge-system spec 明确标注"数据库当前仅初始化 4 种" | ✅ MATCH |
| 徽章按圈子隔离 | 是 | 是 | ✅ MATCH |
| 徽章撤销条件 | 内容违规/删除、成员退出圈子 | 内容违规/退出圈子均有 Scenario | ✅ MATCH |
| 连续参与有效行为 | 发帖、评论、点赞、内容被加精 | member-growth spec 提到发帖、评论、点赞 | ✅ MATCH |
| 连续参与统计窗口 | 近 7 天，按圈子隔离 | 近 7 天，按圈子隔离 | ✅ MATCH |

### 11.5 衔接审计问题清单

#### BLOCK-C001: 成员等级降级规则前后端矛盾（同 BLOCK-002）

- **前端 Spec**: `specs/member-growth/spec.md` "等级下降展示" Scenario
- **后端 Spec**: `specs/member-experience/spec.md` "Level does not downgrade" Scenario
- **问题**: 后端明确经验值扣减不降级（防频繁波动），前端要求降级展示
- **影响**: 前后端数据不一致，用户看到等级下降后刷新又恢复
- **建议**: 修正前端 spec 为等级不降级，与后端保持一致

#### BLOCK-C002: proposal.md API 路径前缀错误（同 BLOCK-001/003/004）

- **前端文档**: `proposal.md` 第 11 行
- **后端定义**: design.md D7 明确划分 `/circle/growth/`（圈子等级）和 `/user/growth/`（成员成长/徽章/排行榜）
- **问题**: proposal 错误将 3 个接口放在 `/circle/growth/` 下
- **影响**: 若实现者参考 proposal 会导致 404
- **建议**: 修正 proposal.md 路径

#### FLAG-C001: streakDetail 7天每日明细后端未实现

- **前端 Spec**: `specs/member-growth/spec.md` 连续参与进度 7 天时间轴
- **后端现状**: `/participation` 接口仅返回 `participationDays`（连续天数），无每日明细数组
- **问题**: 前端 spec 要求展示实心圆/空心圆/横线 7 天时间轴，但后端不返回每日数据
- **影响**: 连续参与时间轴无法按 spec 实现
- **建议**: 要么推动后端扩展字段，要么前端降级为仅展示连续天数数字

#### FLAG-C002: totalBadges/totalBadgeCount 字段后端未提供

- **前端 Spec**: 徽章墙/成长页徽章总数展示
- **后端现状**: MemberGrowthVO 暂未提供徽章总数字段
- **问题**: design.md D9 已标注，但 spec 未定义降级方案
- **影响**: 徽章总数可能无法展示
- **建议**: 前端通过徽章列表长度计算，或后端补充字段

---

## 12. PRD 追溯矩阵

### 12.1 PRD 故事列表追溯

| PRD 故事 | 优先级 | 对应前端 spec | 对应 tasks | 状态 | 备注 |
|---------|--------|-------------|-----------|------|------|
| 故事 13.1.1 圈子等级计算与展示 | 中 | circle-level | 2.1-2.4 | ✅ COVERED | 4 个 AC 全部覆盖 |
| 故事 13.2.1 成员经验值系统 | 中 | member-growth | 3.1-3.6 | ⚠️ COVERED（规则矛盾）| 5 个 AC 中等级降级规则与后端矛盾 |
| 故事 13.2.2 连续参与进度 | 中 | member-growth | 3.1-3.6 | ⚠️ COVERED（数据缺口）| 4 个 AC 覆盖，但 streakDetail 数据待后端扩展 |
| 故事 13.3.1 成就徽章系统 | 低 | badge-system | 4.1-4.6 | ✅ COVERED | 5 个 AC 覆盖，徽章总数字段待补充 |
| 故事 13.3.2 圈子内排行榜 | 低 | leaderboard | 5.1-5.5 | ✅ COVERED | 5 个 AC 全部覆盖 |

### 12.2 PRD 验收条件（AC）追溯

| PRD 故事 | AC 编号 | AC 描述 | 前端覆盖 | 状态 |
|---------|---------|---------|---------|------|
| 13.1.1 | AC1 | 圈子等级提升并展示在主页/详情/列表卡片 | circle-level spec "圈子详情页展示等级标识" + "圈子列表卡片展示等级标识" | ✅ COVERED |
| 13.1.1 | AC2 | 等级提升向创建者和活跃成员发送站内通知 | **缺失**，circle-level spec 无等级提升通知 Scenario | ⚠️ GAP（同 FLAG-007）|
| 13.1.1 | AC3 | 详情页展示当前等级/成长分/下一等级条件/进度/权益 | circle-level spec "展示等级进度" + "展示已解锁权益" | ✅ COVERED |
| 13.1.1 | AC4 | 展示距下一等级各维度条件差距 | circle-level spec "展示差距条件" + "点击进度条展开分项指标" | ✅ COVERED |
| 13.2.1 | AC1 | 发帖获得 10 经验值 + 10 贡献值 | 后端处理，前端仅展示，member-growth 覆盖展示 | ✅ COVERED |
| 13.2.1 | AC2 | 评论获得 3 经验值 + 3 贡献值 | 后端处理，前端仅展示 | ✅ COVERED |
| 13.2.1 | AC3 | 加精获得 30 经验值 + 50 贡献值 | 后端处理，前端仅展示 | ✅ COVERED |
| 13.2.1 | AC4 | 个人资料展示经验值/贡献值/等级/进度/排名 | member-growth spec "成长概览卡片展示" + "成员等级进度展示" | ✅ COVERED |
| 13.2.1 | AC5 | 每日经验上限 100 点 | member-growth spec "达到每日上限" | ✅ COVERED |
| 13.2.1 | - | 经验值扣减后等级**不下降** | **错误覆盖**，前端要求等级下降 | ❌ BLOCK（规则矛盾）|
| 13.2.2 | AC1 | 近 7 天至少 3 天参与展示连续进度 | member-growth spec "近 7 天有参与行为" | ✅ COVERED |
| 13.2.2 | AC2 | 当日有效参与标记为已完成 | member-growth spec "展示连续参与进度" | ✅ COVERED |
| 13.2.2 | AC3 | 3/7/14 天里程碑展示对应成就 | member-growth spec "连续参与里程碑达成" | ✅ COVERED |
| 13.2.2 | AC4 | 近 7 天无参与展示空状态 | member-growth spec "近 7 天无参与行为" | ✅ COVERED |
| 13.3.1 | AC1 | 10 篇内容获得持续创作者徽章 | 后端判定，前端展示，badge-system 覆盖展示逻辑 | ✅ COVERED |
| 13.3.1 | AC2 | 5 篇精华获得优质贡献者徽章 | 后端判定，前端展示 | ✅ COVERED |
| 13.3.1 | AC3 | 近 7 天 3 天参与获得活跃参与者徽章 | 后端判定，前端展示 | ✅ COVERED |
| 13.3.1 | AC4 | 获得新徽章发送站内通知 | badge-system spec "获得新徽章 Toast 提示" | ✅ COVERED |
| 13.3.1 | AC5 | 个人资料展示已获得/未获得徽章/条件/进度 | badge-system spec "徽章墙页面展示" + "徽章详情弹窗" | ✅ COVERED |
| 13.3.2 | AC1 | 3 个维度切换（经验/贡献/发帖） | leaderboard spec "排行榜维度切换" | ✅ COVERED |
| 13.3.2 | AC2 | Top 50 展示，不足 50 展示全部 | leaderboard spec "展示 Top 50 列表" + "不足 50 人展示全部" | ✅ COVERED |
| 13.3.2 | AC3 | 未进 Top 50 在底部展示我的排名和差距 | leaderboard spec "当前用户未进入 Top 50" + "距上一名差距展示" | ✅ COVERED |
| 13.3.2 | AC4 | 无排行数据展示空状态和参与入口 | leaderboard spec "无排行数据时展示空状态" | ✅ COVERED |
| 13.3.2 | AC5 | 本周/本月/累计周期切换 | leaderboard spec "排行榜周期切换" | ✅ COVERED |

**PRD AC 覆盖率统计**:
- PRD 核心 AC 总数: 23 个
- 已正确覆盖: 21 个
- 规则矛盾: 1 个（等级降级）
- 缺失 Scenario: 1 个（圈子等级提升通知）
- 覆盖率: 91% (21/23)

### 12.3 UX/UI 需求追溯

| UX/UI 需求 | 覆盖情况 | 状态 |
|-----------|---------|------|
| 圈子主页展示等级标识/名称/进度/下一等级条件 | circle-level spec 覆盖 | ✅ COVERED |
| 个人成长信息展示经验/贡献/等级/连续参与/徽章入口/排名 | member-growth spec 覆盖 | ✅ COVERED |
| 徽章墙区分已获得/未获得/即将达成 3 种状态 | badge-system spec 覆盖 | ✅ COVERED |
| 排行榜 3 维度 + 3 周期切换 | leaderboard spec 覆盖 | ✅ COVERED |
| 排行榜高亮当前用户，Top 50 外展示排名 | leaderboard spec 覆盖 | ✅ COVERED |
| 覆盖加载中/空状态/错误/权限不足/无成员/未加入等状态 | PARTIAL（加载/空/错误/权限覆盖，缺少圈子解散场景）| ⚠️ PARTIAL |
| 移动端 375px 保持可读 | tasks 7.1-7.3 覆盖 | ✅ COVERED |
| 私有圈子成长信息仅对授权用户展示 | circle-level spec 覆盖 | ✅ COVERED |

---

## 13. 审核澄清记录（2026-06-26）

### 13.1 三层成长体系划分澄清

经产品确认，内容社区成长体系划分为三套独立体系，API 前缀语义如下：

| 体系 | 数据库表 | API 前缀 | 前端展示场景 |
|------|---------|---------|-------------|
| **全局内容社区用户成长** | `content_user_*` 系列表 | `/api/v1/content/user/growth/`（跨圈子通用，不在本 change） | 用户个人主页的全局积分/等级/勋章（跨圈子通用），有衰减降级机制（属于 EPIC-03，不在本 change 范围） |
| **圈子等级** | `circle_level` | `/api/v1/content/circle/growth/level/` | 圈子详情页的等级标识、成长进度、权益展示（L1-L5） |
| **圈子内成员成长** | `circle_member_growth` 等 5 张表 | `/api/v1/content/user/growth/`（通过 `circleId` 参数限定单圈子维度） | 个人成长信息页的经验值、贡献值、徽章、排行榜（单圈子维度） |

**澄清结论**: 后端 design.md D7 决策的路径划分正确，`/user/growth/` 前缀通过 `circleId` 查询参数限定为"用户在某圈子内的成长数据"，不存在语义冲突。proposal.md 将圈子内成员成长接口错误放在 `/circle/growth/` 前缀下确实是错误。

### 13.2 BLOCK-001/003/004/C002 API 路径修改方案验证

产品提议的修改方案与前端 design.md Context 章节和后端 design.md D7 决策核对结果：

| 接口 | 产品提议修改后路径 | 与 design.md 核对 | 状态 |
|------|------------------|-----------------|------|
| 成员成长 | `GET /api/v1/content/user/growth/info?circleId=&userId=` | 前端 design.md 第7行一致 | ✅ 正确 |
| 成就徽章 | `GET /api/v1/content/user/growth/achievement/list?circleId=&userId=` | 前端 design.md 第8行一致 | ✅ 正确 |
| 排行榜 | `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | 前端 design.md 第9行一致 | ✅ 正确 |

**补充发现**: proposal.md 第11行还遗漏了一个核心接口：
```
GET /api/v1/content/user/growth/participation?circleId=&userId=  (MemberGrowthController，连续参与接口)
```
该接口在前端 design.md 第10行已列出，用于支撑连续参与 Scenario，修正 proposal.md 时必须补上。

### 13.3 额外发现：排行榜参数名前后不一致

在澄清核对过程中发现新的一致性问题：

- 前端 `design.md` 第9行和产品提议的修改中，排行榜参数名为 `dimension`（维度：experience/contribution/posts）、`period`（周期：week/month/all）
- 但前端 `specs/leaderboard/spec.md` 中使用的参数名是 `type`（experience/contribution）、`range`（week/month/总榜）

**建议**: 根据 D9 决策"代码层直接使用后端 VO 字段名，不做重命名映射"，应以**后端参数名 `dimension`/`period`** 为准，前端 spec 中的 `type`/`range` 参数名需要修正。此问题已升级为 FLAG-014。

### 13.4 澄清后问题状态更新

| 问题 ID | 澄清前状态 | 澄清后状态 | 说明 |
|--------|----------|----------|------|
| BLOCK-001/003/004/C002（API 路径前缀错误）| BLOCK | BLOCK（已确认修复方案）| 产品提议的3个路径修改正确，但需额外补上 participation 接口 |
| BLOCK-002/005/C001（等级降级规则矛盾）| BLOCK | BLOCK（待确认）| 本次澄清未涉及等级降级规则，仍需与产品确认是否采纳后端"不降级"规则 |
| FLAG-014（新）| 未记录 | FLAG | 排行榜参数名 type/range vs dimension/period 不一致，需修正 spec |

---

## 最终结论

### BLOCK 问题汇总（必须修复才能 apply）

| ID | 问题 | 位置 | 影响 |
|----|------|------|------|
| BLOCK-001/003/004/C002 | proposal.md 核心 API 路径前缀错误（成员成长/徽章/排行榜误用 /circle/growth/ 前缀） | proposal.md:11 | 3 个核心接口 404 |
| BLOCK-002/005/C001 | 成员等级降级规则前后端矛盾（前端要求降级，后端明确不降级） | specs/member-growth/spec.md:95-97 | 前后端数据不一致，用户体验 bug |

### FLAG 问题汇总（应该修复）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| FLAG-001/013 | tasks.md 缺少 DoD 标准收尾项 | tasks.md | 补充流程确认/Code Review/覆盖率/测试/合并清理任务 |
| FLAG-002 | proposal.md 成员成长接口路径拼写不完整 | proposal.md:11 | 修正为 /user/growth/info |
| FLAG-003 | WebSocket 消息体容错逻辑不明确 | design.md D4 | 补充字段缺失时的降级 Scenario |
| FLAG-004/C001 | streakDetail 7天每日明细后端未扩展，无降级方案 | design.md D9 + member-growth spec | 明确降级方案（仅展示天数）或推动后端扩展 |
| FLAG-005/C002 | totalBadges 字段后端未提供，无降级方案 | design.md D9 | 通过列表长度计算或隐藏总数 |
| FLAG-006 | 徽章撤销状态 UI 细节不明确 | specs/badge-system/spec.md:55-69 | 补充"已撤销"标签样式细节 |
| FLAG-007 | 等级提升通知 WebSocket Scenario 缺失 | specs/member-growth/ + specs/circle-level/ | 补充等级提升 Toast 和刷新 Scenario |
| FLAG-008 | 排行榜小时级快照无更新时间提示 | specs/leaderboard/spec.md | 补充"榜单每小时更新"提示 |
| FLAG-009 | 缺少圈子已解散/成员已退出的资源不存在场景 | 所有 specs | 补充资源不存在的错误提示 Scenario |
| FLAG-010 | 网络超时与断网缺少差异化处理 | 所有 specs | 区分超时/服务端错误/断网的提示文案 |
| FLAG-011 | WebSocket 不可用时降级方案缺失 | design.md D4 | 补充轮询降级或提示方案 |
| FLAG-012 | 未明确 XSS 防护策略 | design.md | 明确使用 Vue 默认转义，禁止 v-html 渲染用户内容 |
| FLAG-014（澄清新增）| 排行榜参数名前后不一致（type/range vs dimension/period） | specs/leaderboard/spec.md vs design.md:9 | 统一使用后端参数名 dimension/period，修正 spec 中的 type/range |

### ADVISORY 问题汇总（建议改进）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| （无） | - | - | - |

### 门禁判定

```
Step 1 规范审核: BLOCK=2（核心矛盾 2 类，涉及 5 个问题 ID 指向同一根源）, FLAG=12 → REJECTED
Step 2 依赖检查: P0 依赖阻塞=2（等级规则矛盾必须与后端对齐、proposal 路径必须修正）→ NEEDS_FIX

最终判定: REJECTED
```

### 审核结论

- **BLOCK 问题**: 2 类（proposal API 路径错误、等级降级规则矛盾）
- **FLAG 问题**: 12 个
- **ADVISORY 问题**: 0 个
- **依赖阻塞 (P0)**: 2 项
- **PRD AC 覆盖率**: 91% (21/23)
- **核心 API 对齐率（design/specs 层面）**: 100% (5/5)（proposal 层面 40%，需修正）

**结论文本**: 规范审核未通过。design.md 和 specs 整体质量较好，VO 字段映射完整，设计决策记录详实，核心 API 路径在 design/specs 层面与后端对齐。但存在 2 类必须修复的 BLOCK 问题：
1. **proposal.md 中 3 个核心 API 路径前缀错误**：作为入口文档，proposal.md 将成员成长、徽章、排行榜接口错误放在 `/circle/growth/` 前缀下，与 design.md、specs、后端 D7 决策均不一致，必须修正。
2. **成员等级降级规则前后端严重矛盾**：后端明确经验值扣减不降级（防频繁波动），前端 spec 却要求等级下降展示，这是核心业务规则冲突，必须对齐。

修复后重新运行 `/openspec:review-change` 或相关审核命令。

### 修复建议

#### 需要修复的规范文档问题（共 15 项）

**优先级 P0（BLOCK，2 项）**:
- [BLOCK] 修正 `proposal.md` 第 11 行的核心接口清单：
  - `GET /api/v1/content/circle/growth/info` → `GET /api/v1/content/user/growth/info?circleId=&userId=`
  - `GET /api/v1/content/circle/growth/achievement/list` → `GET /api/v1/content/user/growth/achievement/list?circleId=&userId=`
  - `GET /api/v1/content/circle/growth/leaderboard` → `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=`
  - **补充遗漏**：新增 `GET /api/v1/content/user/growth/participation?circleId=&userId=`（MemberGrowthController，连续参与接口）
- [BLOCK] 修正 `specs/member-growth/spec.md` 中的等级降级规则：删除"等级下降展示" Scenario，替换为"经验值扣减但等级不下降" Scenario，与后端 member-experience spec 和 design.md 关键区别表对齐。（待产品最终确认后执行）

**优先级 P1（FLAG，13 项）**:
- 在 `tasks.md` 末尾补充第 9 分组「质量门禁与收尾」，包含 DoD 要求的 5 项任务
- 在 design.md 或 specs 中补充 WebSocket 消息体容错逻辑（字段缺失时仅 Toast 不刷新）
- 明确 streakDetail 7天每日明细的降级方案（后端扩展前仅展示连续天数）
- 明确 totalBadges 字段的降级方案（通过列表长度计算）
- 补充徽章撤销状态的 UI 细节（"已撤销"标签样式/位置）
- 在 member-growth 和 circle-level spec 中补充等级提升通知的 WebSocket Scenario
- 在 leaderboard spec 中补充"榜单每小时更新"提示
- 补充圈子已解散/成员已退出的资源不存在 Scenario
- 补充网络超时与断网的差异化错误提示
- 补充 WebSocket 不可用时的降级策略（轮询或提示）
- 在 design.md 中明确 XSS 防护策略（Vue 默认转义，禁止 v-html 渲染用户内容）
- **[澄清新增 FLAG-014]** 修正 `specs/leaderboard/spec.md` 中排行榜参数名：`type` → `dimension`，`range` → `period`，与后端 VO 字段名和 design.md 保持一致

#### 需要与后端协调的依赖项（共 2 项）

- [P1] 确认后端 `/api/v1/content/user/growth/participation` 接口是否计划在本期补充 `streakDetail: boolean[]` 字段，若否则前端降级实现
- [P1] 确认后端 MemberGrowthVO 是否计划补充 `totalBadges`/`totalBadgeCount` 字段，若否则前端通过徽章列表长度计算

所有 BLOCK 问题修复后，请重新执行审核以确认"可开发"状态。FLAG 问题可在开发过程中同步修复，但建议在 apply 前全部处理完毕以避免实现偏差。
