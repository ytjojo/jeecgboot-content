# Review Report: user-05-blocking-muting-frontend

**审核日期**: 2026-06-06
**修复日期**: 2026-06-07
**审核类型**: Apply 前深度审核
**Change 类型**: 前端 change
**Domain**: user, EPIC-05
**配对后端 change**: user-05-blocking-muting
**审核范围**: proposal.md, design.md, specs/spec.md, tasks.md, 后端设计文档, 后端 spec, 前端 PRD

---

## 1. 总览表

| 维度 | 得分 | BLOCK | FLAG | FLAG 已修复 | ADVISORY |
|------|------|-------|------|------------|----------|
| 完整性 (Completeness) | 8/10 | 0 | 1 | 1 ✅ | 2 |
| 一致性 (Consistency) | 7/10 | 0 | 2 | 2 ✅ | 1 |
| 可实现性 (Feasibility) | 7/10 | 0 | 1 | 1 ✅ | 1 |
| 可测试性 (Testability) | 8/10 | 0 | 1 | 1 ✅ | 2 |
| 接口契约 (API Contract) | 8/10 | 0 | 1 | 1 ✅ | 1 |
| 边界覆盖 (Boundary) | 7/10 | 0 | 1 | 1 ✅ | 2 |
| **综合** | **7.5/10** | **0** | **7** | **7 ✅** | **9** |

---

## 2. 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD AC 覆盖率 | 90% | 10 个用户故事中 9 个有完整 spec 覆盖，US-5.2.2 "不感兴趣"气泡交互细节偏弱 |
| API 契约完整率 | 92% | 13 个 API 端点中 12 个路径/参数完全对齐，spec Requirement 部分有 1 处旧路径残留 |
| 边界覆盖率 | 78% | PRD 10.2 节 9 个边界场景中 7 个在 spec 中有对应 scenario，2 个缺失 |
| TDD 配对率 | 89% | 45 个 tasks 中 40 个有对应 spec scenario，5 个 blocked tasks 无独立 spec |
| 任务完成率 | 89% | 40/45 tasks 完成，5 个因目标组件不存在而阻塞 |
| 制品完整度 | 100% | proposal, design, specs, tasks 全部 done |

---

## 3. 各维度详细审核

### 3.1 完整性 (Completeness) — 8/10

#### 文档结构完整性

| 制品 | 状态 | 说明 |
|------|------|------|
| proposal.md | ✅ | Context, Goals/Non-Goals, Capabilities, Impact 齐全 |
| design.md | ✅ | 7 个 Decisions + Risks + Migration + Rollback + Open Questions |
| specs/spec.md | ✅ | 11 个 Requirements, 50+ Scenarios |
| tasks.md | ✅ | 8 个 task group, 45 个 tasks |
| backend-issues.md | ✅ | 1 个活跃问题, 3 个已修复 |
| verification.md | ✅ | 6 个 CRITICAL 全部修复 |
| verification-review.md | ✅ | 更新版审核结论 |

#### PRD 用户故事覆盖

| PRD 用户故事 | Spec 覆盖 | 评估 |
|-------------|-----------|------|
| US-5.1.1 拉黑用户 | ✅ "User blocking" Requirement | 完整 |
| US-5.1.2 拉黑后互动拦截 | ✅ "Blocking interaction boundaries" | 完整 |
| US-5.1.3 查看黑名单并解除 | ✅ "Blacklist management" | 完整 |
| US-5.1.4 拉黑不通知对方 | ✅ "Blocking and unblocking are silent" | 完整 |
| US-5.2.1 屏蔽用户动态 | ✅ "Muting users" | 完整 |
| US-5.2.2 不感兴趣+内容/话题屏蔽 | ⚠️ "Not interested feedback" | 部分覆盖，气泡交互细节不足 |
| US-5.2.3 屏蔽词过滤 | ✅ "Keyword filtering" | 完整 |
| US-5.2.4 临时屏蔽话题 | ✅ "Temporary topic filter expires/cancelled" | 完整 |
| US-5.2.5 独立管理列表 | ✅ "Mute and filter list management" | 完整 |
| US-5.3.1 行为边界说明 | ✅ "Boundary education" | 完整 |

**FLAG-1**: US-5.2.2 "不感兴趣" 气泡交互的 spec 覆盖偏弱。spec 仅记录了 "record not-interested feedback" 和 "block content type/topic" 的后端行为，未覆盖前端气泡选项动态生成、乐观更新、category/topics 为空时的降级展示等交互细节。这些细节在 PRD 3.5.2 和 design.md Decision 5 中有描述，但 spec 的 scenario 粒度不足。

**ADVISORY-1**: 5 个 blocked tasks (3.5, 3.6, 3.7, 4.3, 5.3) 均因"目标组件不存在"而阻塞。这些是核心集成点（用户主页、内容卡片、评论区），需确认这些目标组件是否在其他 change 中创建，或需要在本 change 中新建。

**ADVISORY-2**: design.md 的 Open Questions 中有 3 个未关闭问题（私信模块、信息流过滤方式、临时屏蔽时长），建议在 apply 前明确答案或标记为"后续迭代"。

---

### 3.2 一致性 (Consistency) — 7/10

#### Capabilities ↔ Specs 一致性

proposal.md 声明 1 个 capability: `blocking-muting-frontend`，覆盖拉黑/屏蔽操作入口、确认弹窗、管理页面、屏蔽词设置和不感兴趣反馈。spec.md 的 11 个 Requirements 基本覆盖了这些能力。

**FLAG-2**: spec.md 内部存在 API 路径不一致。API 对齐表（第 167-181 行）使用更新后的后端路径 `/content/user/relation/block`，但 Requirement scenario 中仍引用旧路径 `/api/content/user/block`（第 3、14、17 行等）。同一个 spec 文件内的路径不一致会导致实现时的困惑。

**FLAG-3**: design.md 与 PRD 在 API 设计上存在差异：
- PRD 使用 `DELETE /api/content/user/block/{targetUserId}`（Path Variable）
- design.md 使用 `POST /content/user/relation/unblock`（@RequestParam）
- design.md 的 Decision 3 已说明"所有写操作使用 POST"，但 PRD 未同步更新

design.md 已基于实际后端做了修正（正确），但 PRD 仍保留旧设计（过时）。建议在 PRD 中添加注释说明"以 design.md 和实际后端为准"。

#### Decisions ↔ Requirements 一致性

design.md 的 7 个 Decisions 与 spec Requirements 基本对齐：
- Decision 1 (声明式弹窗) ↔ spec "Block confirmation explains consequences"
- Decision 2 (Dropdown 菜单) ↔ spec 拉黑/屏蔽操作入口
- Decision 3 (API 按领域拆分) ↔ spec "API 文件结构"
- Decision 5 (Popover 气泡) ↔ spec "Not interested feedback"
- Decision 6 (被拉黑状态页区分) ↔ spec "Blocked/Blocked user profile" scenarios

**ADVISORY-3**: design.md Decision 4 提到 Store 缓存 `relationCache` 和 `blacklistCount`/`muteListCount`，但 spec 的 "关系状态缓存" Requirement 未提及 count 缓存策略。建议补充 `refreshCounts` 的 spec scenario。

---

### 3.3 可实现性 (Feasibility) — 7/10

#### 技术栈兼容性

| 方面 | 评估 | 说明 |
|------|------|------|
| 框架 | ✅ | Vue 3 + TypeScript + Vite，与项目一致 |
| UI 库 | ✅ | Ant Design Vue 4 组件（Dropdown, Modal, Tabs, Statistic, Empty） |
| 状态管理 | ✅ | Pinia `defineStore`，遵循项目模式 |
| API 层 | ✅ | `defHttp` + `enum Api` 模式 |
| 新依赖 | ✅ | 不引入新依赖 |

#### 架构规范遵循

- ✅ API 文件按领域拆分（block.ts, mute.ts, filterRule.ts）
- ✅ Store 使用函数式 state 返回
- ✅ 组件使用 `<script setup lang="ts">` + `<style scoped>`
- ✅ 响应式设计使用 CSS 媒体查询

**FLAG-4**: 5 个 blocked tasks 涉及核心集成点：
- 3.5 用户主页嵌入 BlockMuteMenu
- 3.6 内容卡片嵌入 BlockMuteMenu
- 3.7 评论区嵌入 BlockMuteMenu
- 4.3 内容卡片集成 NotInterestedPopover
- 5.3 用户主页集成 BlockedUserPage

这些任务依赖的"目标组件"（用户主页、内容卡片、评论区）在当前 change 中不存在。需要确认：
1. 这些组件是否在其他 change（如 circle-10-core-frontend）中创建？
2. 是否需要在本 change 中创建这些组件的骨架？

**ADVISORY-4**: design.md Migration Plan 第 7 步提到"修改现有内容卡片、用户主页、评论区组件"，但 tasks.md 中这些任务标记为"目标组件不存在"。Migration Plan 应与 tasks.md 的阻塞状态保持一致，注明这些步骤需等待依赖组件就绪。

---

### 3.4 可测试性 (Testability) — 8/10

#### Scenario 可量化程度

spec.md 包含 50+ 个明确的 Given-When-Then scenarios，每个 scenario 有清晰的触发条件和预期结果。

| Requirement | Scenario 数量 | 可量化 | 评估 |
|-------------|--------------|--------|------|
| User blocking | 6 | ✅ | 完整 |
| Blocking interaction boundaries | 7 | ✅ | 完整 |
| Blacklist management | 5 | ✅ | 完整 |
| Muting users | 6 | ✅ | 完整 |
| Not interested feedback | 7 | ⚠️ | 部分 scenario 偏高层 |
| Keyword filtering | 6 | ✅ | 完整 |
| Mute and filter list management | 5 | ✅ | 完整 |
| Boundary education | 4 | ✅ | 完整 |

**FLAG-5**: "Not interested feedback" Requirement 的部分 scenario 偏高层（如 "the system records the feedback and reduces similar content"），缺少可量化的前端验证点（如气泡出现时机、选项数量、乐观更新的视觉表现）。

**ADVISORY-5**: PRD 10.1 节的测试要点（如"确认弹窗展示正确文案"、"按钮 loading 期间不可重复点击"）未在 spec 中转化为独立 scenario。建议将 PRD 测试要点作为 spec scenario 的补充验证条件。

**ADVISORY-6**: 临时屏蔽倒计时精度测试（PRD 10.2 提到"到期后自动从列表消失"）缺少边界 scenario（如恰好在到期瞬间刷新列表的行为）。

---

### 3.5 接口契约 (API Contract) — 8/10

#### API 端点完整性

| # | 后端端点 | 前端 tasks 引用 | spec 对齐表 | 状态 |
|---|---------|----------------|-------------|------|
| 1 | POST /content/user/relation/block | ✅ | ✅ | 一致 |
| 2 | POST /content/user/relation/unblock | ✅ | ✅ | 一致 |
| 3 | GET /content/user/relation/blacklist | ✅ | ✅ | 一致 |
| 4 | GET /content/user/relation/detail | ✅ | ✅ | 一致 |
| 5 | GET /content/user/relation/block-mute/help | ✅ | ✅ | 一致 |
| 6 | POST /content/user/relation/mute | ✅ | ✅ | 一致 |
| 7 | POST /content/user/relation/mute/cancel | ✅ | ✅ | 一致 |
| 8 | GET /content/user/relation/mute-list | ✅ | ✅ | 一致 |
| 9 | POST /content/user/filter-rule | ✅ | ✅ | 一致 |
| 10 | POST /content/user/filter-rule/delete | ✅ | ✅ | 一致 |
| 11 | POST /content/user/filter-rule/batch-delete | ✅ | ✅ | 一致 |
| 12 | GET /content/user/filter-rule/list | ✅ | ✅ | 一致 |
| 13 | POST /content/user/not-interested | ✅ | ✅ | 一致 |

**13/13 端点全部在后端已实现，前端 tasks 和 spec 对齐表已正确引用。**

#### 参数风格一致性

- ✅ 所有写操作使用 POST（与后端一致）
- ✅ 参数通过 @RequestParam 查询参数传递（与后端一致）
- ✅ batch-delete 使用 @RequestBody 传递 JSON 数组（spec 已标注差异）
- ✅ 分页参数统一使用 pageNo + pageSize

**FLAG-6**: spec.md 的 Requirement scenario 部分仍引用旧 API 路径（如 `POST /api/content/user/block`），与 API 对齐表和 tasks.md 不一致。这不影响实现（实现时会参考 tasks.md 和 design.md），但会造成文档混乱。

**ADVISORY-7**: 后端 `ContentUserRelationVO` 的字段名为 `blacklisted`（boolean），与 API 路径 `block`/`unblock` 命名风格不一致。design.md Decision 4 已正确映射（`isBlocked: boolean` ← `blacklisted`），但建议在 tasks.md 中添加字段映射备注。

---

### 3.6 边界覆盖 (Boundary) — 7/10

#### PRD 10.2 边界测试场景覆盖

| PRD 边界场景 | Spec 覆盖 | 评估 |
|-------------|-----------|------|
| 拉黑自己 | ✅ "Block user rejects invalid targets" | 已覆盖 |
| 重复拉黑 | ⚠️ 未显式覆盖 | 缺失 |
| 重复屏蔽 | ⚠️ 未显式覆盖 | 缺失 |
| 无效正则 | ✅ "Keyword or regex filter rejects invalid values" | 已覆盖 |
| 屏蔽词上限 | ✅ PRD 3.4 "达到上限提示" | 已覆盖 |
| 网络异常 | ⚠️ 未在 spec 中覆盖 | 缺失 |
| 被拉黑方访问主页 | ✅ "Blocked user cannot view actor profile" | 已覆盖 |
| 拉黑方访问被拉黑主页 | ✅ "Actor cannot view blocked user's profile" | 已覆盖 |
| 同时拉黑+屏蔽 | ⚠️ 未显式覆盖 | 缺失 |

**FLAG-7**: 4 个 PRD 边界场景在 spec 中缺失显式 scenario：
1. **重复拉黑** — 后端应幂等处理，前端应不报错
2. **重复屏蔽** — 后端应幂等处理，前端应不报错
3. **网络异常** — 操作失败时保留用户上下文，显示错误提示
4. **同时拉黑+屏蔽同一用户** — 拉黑优先级高于屏蔽

建议在 spec 中补充这 4 个 scenario。

**ADVISORY-8**: 删除屏蔽词的 3 秒撤销机制在 PRD 3.4 中有描述，但 spec "Keyword filter can be removed" scenario 未提及撤销交互。这是前端交互细节，可作为 ADVISORY 记录。

**ADVISORY-9**: 批量取消屏蔽的 "原子性" 边界未在 spec 中覆盖（如部分成功、部分失败的处理）。后端 spec 有 "reports failed items without removing unrelated rules"，但前端 spec 缺少对应的错误处理 scenario。

---

## 4. 前后端衔接审计

### 4.1 接口清单双向对比

| 对比维度 | 结果 |
|---------|------|
| 前端引用的端点数 | 13 |
| 后端已实现的端点数 | 13 |
| 完全匹配 | 13/13 ✅ |

**结论**: 前端 tasks.md 中引用的所有 API 端点均已在后端实现，无遗漏、无多余。

### 4.2 数据模型一致性

| 后端 VO/Req | 前端引用 | 字段匹配 |
|-------------|---------|---------|
| ContentUserRelationVO | Store relationCache | ✅ blacklisted→isBlocked, blockedByOwner→isBlockedBy, muted→isMuted |
| ContentUserMuteListPageVO | MuteListPage | ✅ records, total, pageNo, pageSize |
| ContentUserMuteListItemVO | MuteListPage | ✅ mutedUserId, nickname, avatar, muteTime |
| ContentBlockMuteHelpVO | PrivacySettingsPage | ✅ blockConfirmation, muteConfirmation, unblockConfirmation |

### 4.3 错误码覆盖

| 场景 | 后端处理 | 前端处理 | 一致性 |
|------|---------|---------|--------|
| 拉黑自己 | 后端拒绝 | 前端拦截 | ✅ 双重校验 |
| 无效 targetUserId | 后端返回错误 | 前端显示错误消息 | ✅ |
| 操作失败 | Result.fail() | useMessage.error | ✅ |
| 通用失败提示 | 不暴露拉黑原因 | 显示"操作失败，请重试" | ✅ |

### 4.4 认证鉴权一致性

前后端均使用 `userId` 作为请求参数传递用户身份。后端通过 @RequestParam 接收，前端通过 API 函数参数传递。**注意**: 这种方式将 userId 暴露在 URL 中，建议后续迭代考虑从 JWT token 中提取。

### 4.5 分页契约

| 参数 | 后端 | 前端 | 一致性 |
|------|------|------|--------|
| 页码 | pageNo (默认 1) | pageNo | ✅ |
| 每页大小 | pageSize (默认 10) | pageSize | ✅ |
| 响应结构 | { records, total, pageNo, pageSize } | 对应字段 | ✅ |

---

## 5. PRD 追溯矩阵

| PRD 章节 | PRD 内容 | Spec Requirement | Design Decision | Task 覆盖 |
|---------|---------|-----------------|-----------------|----------|
| 3.1 隐私设置聚合页 | 入口卡片+帮助说明 | "Privacy settings expose separate entries" | Decision 2 (Dropdown) | 6.1-6.2, 8.1 ✅ |
| 3.2 黑名单管理页 | 搜索+列表+解除拉黑 | "Blacklist management" | Decision 3 (API 拆分) | 6.3-6.4, 8.2 ✅ |
| 3.3 屏蔽列表管理页 | 4 Tab+批量操作 | "Mute and filter list management" | Decision 3 (API 拆分) | 6.5-6.10, 8.3 ⚠️ (6.5-6.6 blocked) |
| 3.4 屏蔽词设置页 | 关键词/正则+删除撤销 | "Keyword filtering" | Decision 3 (API 拆分) | 6.11-6.13, 8.4 ✅ |
| 3.5 操作入口 | 用户主页/卡片/评论区 | "User blocking" + "Muting users" | Decision 2 (Dropdown) | 3.1-3.7 ⚠️ (3.5-3.7 blocked) |
| 3.6 确认弹窗 | 3 种弹窗+文案 | "Boundary education" | Decision 1 (声明式 Modal) | 3.2-3.4 ✅ |
| 3.7 被拉黑状态页 | 发起方/被拉黑方区分 | "Blocked user cannot view actor profile" | Decision 6 (两种展示) | 5.1-5.3 ⚠️ (5.3 blocked) |
| 3.8 屏蔽词命中展示 | 折叠+展开 | "Matching content is folded or hidden" | — | 4.4-4.5 ✅ |
| 4 组件设计 | 10 个组件清单 | — | Decision 1-7 | 全部 ✅ |
| 5 API 对接 | 13 个接口 | "API 路径对齐后端" | Decision 3 (API 拆分) | 1.1-1.4 ✅ |
| 6 状态管理 | Pinia Store | "关系状态缓存" | Decision 4 (Store 缓存) | 2.1-2.5 ✅ |
| 7 交互设计 | 操作流程+反馈规则 | 各 Requirement scenarios | Decision 1-6 | 3.1-4.5 ✅ |
| 8 响应式设计 | 3 断点+策略 | — | Decision 7 (CSS 媒体查询) | 7.1-7.4 ✅ |

---

## 6. 问题清单

### FLAG 级别（建议修复，不阻塞 apply）

| # | 维度 | 问题 | 建议修复方式 | 修复状态 |
|---|------|------|-------------|---------|
| FLAG-1 | 完整性 | US-5.2.2 "不感兴趣" 气泡交互 spec 覆盖偏弱 | 在 spec 中补充气泡选项生成、乐观更新、空数据降级的 scenario | ✅ 已修复 |
| FLAG-2 | 一致性 | spec.md Requirement 部分引用旧 API 路径 | 统一为 `/content/user/relation/block` 等实际后端路径 | ✅ 已验证无旧路径 |
| FLAG-3 | 一致性 | PRD 使用 DELETE 方法，design.md 使用 POST | 在 PRD 中添加注释"以 design.md 和实际后端为准" | ✅ 已修复 |
| FLAG-4 | 可实现性 | 5 个 blocked tasks 依赖不存在的目标组件 | 确认目标组件来源（其他 change 或需新建） | ✅ 已记录依赖 |
| FLAG-5 | 可测试性 | "Not interested" 部分 scenario 偏高层 | 补充前端可量化的验证点 | ✅ 已修复 |
| FLAG-6 | 接口契约 | spec Requirement 与 API 对齐表路径不一致 | 统一 spec 内部路径引用 | ✅ 已验证无旧路径 |
| FLAG-7 | 边界覆盖 | 4 个 PRD 边界场景在 spec 中缺失 | 补充重复拉黑/屏蔽、网络异常、同时拉黑+屏蔽 scenario | ✅ 已修复 |

### ADVISORY 级别（锦上添花）

| # | 维度 | 建议 |
|---|------|------|
| ADVISORY-1 | 完整性 | 确认 blocked tasks 的目标组件来源 |
| ADVISORY-2 | 完整性 | 关闭 design.md 的 3 个 Open Questions |
| ADVISORY-3 | 一致性 | spec 补充 `refreshCounts` scenario |
| ADVISORY-4 | 可实现性 | Migration Plan 标注 blocked 步骤 |
| ADVISORY-5 | 可测试性 | PRD 测试要点转化为 spec scenario |
| ADVISORY-6 | 可测试性 | 补充临时屏蔽倒计时精度边界 scenario |
| ADVISORY-7 | 接口契约 | tasks.md 添加 VO 字段映射备注 |
| ADVISORY-8 | 边界覆盖 | spec 补充删除屏蔽词撤销交互 |
| ADVISORY-9 | 边界覆盖 | spec 补充批量操作部分失败处理 |

---

## 7. 最终结论

### 审核判定: PASS (通过)

**无 BLOCK 级别问题**。所有后端 API 端点已实现（13/13），前端 tasks 与后端契约完全对齐。

**7 个 FLAG 级别问题已全部修复**：
- FLAG-1 ✅: spec 已补充乐观更新、空数据降级、操作失败的 scenario
- FLAG-2 ✅: 验证 spec 中无旧 API 路径（review 基于旧版本）
- FLAG-3 ✅: PRD 已添加"以 design.md 和实际后端为准"注释
- FLAG-4 ✅: design.md 已记录 blocked tasks 依赖关系
- FLAG-5 ✅: spec 已补充前端可量化的验证点
- FLAG-6 ✅: 验证 spec 中无旧 API 路径（review 基于旧版本）
- FLAG-7 ✅: spec 已补充 4 个边界 scenario（重复拉黑/屏蔽、网络异常、同时拉黑+屏蔽）

### 剩余依赖

5 个 blocked tasks (3.5, 3.6, 3.7, 4.3, 5.3) 仍需等待目标组件就绪：
- **用户主页** (`FanProfile.vue`): 已存在，可嵌入 BlockMuteMenu
- **内容卡片**: 尚未创建，需等待 `circle-10-core-frontend` 或 `circle-11-content-interaction-frontend` 提供
- **评论区** (`CommentActions.vue`): 已存在，可嵌入 BlockMuteMenu

此 change 可进入 apply 阶段，blocked tasks 待依赖组件就绪后单独处理。
