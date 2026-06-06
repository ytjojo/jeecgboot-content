## Context

承接后端 channel-privacy-membership 变更（已设计完成），在前端 Vue 3 + Ant Design Vue 4 项目中实现频道隐私、订阅、成员管理和治理的完整前端能力。项目使用 Vben Admin 框架，组件封装在 `src/components/`，hooks 在 `src/hooks/`，页面在 `src/views/`。

现有频道模块已有基础路由和页面骨架（EPIC-20），本变更需要在这些页面上补充隐私设置、订阅、成员管理等功能，并新增若干子页面。

## Goals / Non-Goals

**Goals:**
- 实现频道隐私设置 UI（公开/私有 Radio.Group，系统频道锁定，变更影响确认弹窗）
- 实现加入方式配置 UI（自由/审核/邀请三种模式，含邀请创建 Drawer）
- 实现频道主页订阅/取消订阅按钮状态机（含乐观更新策略）
- 实现申请加入私有频道表单 Modal
- 实现待审队列页面（单条/批量审核，超时高亮）
- 实现订阅列表管理页面（分组、搜索、提醒控制）
- 实现成员列表与角色分配页面（JVxeTable，批量操作）
- 实现黑名单管理和治理操作日志页面
- 新增 `useChannelContext` composable 按 channelId 隔离管理频道上下文
- 所有页面支持桌面端和移动端响应式布局

**Non-Goals:**
- 频道创建与所有权管理 UI（EPIC-20 已覆盖）
- 内容发布权限详细规则 UI（EPIC-22）
- 频道推荐与发现算法 UI（EPIC-23）
- 完整付费频道闭环
- 治理日志导出功能（P2）

## Decisions

### D1: 使用 useChannelContext composable 而非全局 Pinia Store

**选择**: 新增 `src/composables/useChannelContext.ts`，以 composable 形式管理频道上下文

**理由**:
- 频道页面按 `channelId` 动态切换，Pinia 全局单例会导致数据串台
- Composable 天然按 channelId 隔离上下文，配合 provide/inject 向子组件传递
- 路由守卫中调用 `resetContext()` + `loadContext()` 确保切换时数据正确刷新
- 权限判断（canManageMembers、canPublish）作为 computed 属性自动响应

**替代方案**: Pinia store + channelId key → 需要手动管理多实例，容易遗漏清理

### D2: 乐观更新策略用于订阅和申请操作

**选择**: 点击订阅/申请后立即更新 UI 状态，请求失败时回滚

**理由**:
- 订阅和申请是高频操作，等待接口返回会造成明显卡顿感
- 乐观更新让用户感知操作立即生效，提升体验
- 失败时回滚按钮状态并展示错误提示，用户可重试
- 操作成功后同步更新 useChannelContext 和使相关缓存失效

**替代方案**: 等待接口返回再更新 → 体验差，每次操作需等待网络往返

### D3: 批量操作采用单次批量请求

**选择**: 批量审核、批量移除等操作发送一次批量请求（传数组），后端返回逐条处理结果

**理由**:
- 避免逐条请求的网络开销和并发状态管理复杂度
- 后端允许部分成功，返回 `{ success, failed, details: Array<{ id, success, errorMessage }> }`
- 前端提交后展示 loading Modal，完成后逐条展示处理结果
- 批量拒绝需填写统一拒绝原因

**替代方案**: 逐条请求 → 网络开销大，并发状态管理复杂，部分失败时用户体验差

### D4: 成员列表使用 JVxeTable 组件

**选择**: 使用项目现有的 JVxeTable 组件实现成员列表

**理由**:
- JVxeTable 支持行操作、批量操作、筛选、排序等高级功能
- 项目已有该组件封装，无需额外引入
- 支持 Checkbox 列用于批量选择
- 支持自定义操作列（Dropdown 下拉菜单）

**替代方案**: 基础 Table 组件 → 需要自行实现批量选择、操作列等逻辑

### D5: 响应式策略采用表格转卡片模式

**选择**: 移动端将 Table 转为卡片列表，操作按钮收进卡片底部或长按菜单

**理由**:
- 表格在小屏幕上列过多会溢出，用户体验差
- 卡片列表更适合移动端的信息展示和触控操作
- 筛选区折叠为"筛选"按钮触发 Drawer，节省屏幕空间
- 保持最小触控区域 44x44px

### D6: 路由结构设计

**选择**: 在频道管理路由下新增子路由

**理由**:
- 频道设置页：`/channel/:id/settings`（已有，需扩展）
- 成员管理页：`/channel/:id/members`
- 待审队列：`/channel/:id/members/pending`
- 黑名单：`/channel/:id/blacklist`
- 治理日志：`/channel/:id/governance`
- 订阅列表：`/channel/subscriptions`（个人中心入口）

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 后端 API 未就绪 | 前端开发受阻 | 先用 Mock 数据开发，接口就绪后切换 |
| 乐观更新回滚逻辑复杂 | 状态不一致 | 统一封装乐观更新 hook，失败时自动回滚 |
| 移动端适配工作量大 | 进度延迟 | 优先保证核心操作可用，逐步完善交互 |
| 多频道切换时缓存失效 | 数据不一致 | useChannelContext 统一管理缓存失效策略 |

## File Structure

```
jeecgboot-vue3/src/
├── composables/
│   └── useChannelContext.ts              # 频道上下文 composable
├── api/content/
│   ├── channelSubscription.ts            # 订阅相关 API
│   ├── channelMember.ts                  # 成员相关 API
│   ├── channelBlacklist.ts               # 黑名单 API
│   ├── channelInvite.ts                  # 邀请 API
│   ├── channelPrivacy.ts                 # 隐私设置 API
│   └── channelGovernance.ts              # 治理日志 API
├── views/channel/
│   ├── settings/
│   │   ├── PrivacySettings.vue           # 隐私设置组件
│   │   ├── JoinMethodSettings.vue        # 加入方式配置组件
│   │   └── InviteDrawer.vue              # 邀请创建 Drawer
│   ├── members/
│   │   ├── MemberList.vue                # 成员列表页面
│   │   ├── PendingApplications.vue       # 待审队列页面
│   │   ├── RoleAssignModal.vue           # 角色分配 Modal
│   │   ├── MuteModal.vue                 # 禁言 Modal
│   │   └── RemoveMemberModal.vue         # 移除成员 Modal
│   ├── blacklist/
│   │   └── BlacklistPage.vue             # 黑名单管理页面
│   ├── governance/
│   │   ├── GovernanceLog.vue             # 治理日志页面
│   │   └── GovernanceDetailDrawer.vue    # 治理详情 Drawer
│   ├── subscription/
│   │   ├── SubscriptionList.vue          # 订阅列表页面
│   │   └── SubscriptionCard.vue          # 订阅频道卡片组件
│   └── components/
│       ├── SubscribeButton.vue           # 订阅/取消订阅按钮（状态机）
│       ├── JoinApplyModal.vue            # 申请加入 Modal
│       └── ChannelContextProvider.vue    # 频道上下文 Provider 组件
└── hooks/web/
    └── useChannelOperation.ts            # 频道操作 hook（乐观更新封装）
```

## Data Formats

### 治理详情 beforeState / afterState 对比格式

治理详情 Drawer 中的 beforeState 和 afterState 字段采用 JSON 对象格式，存储操作前后的完整字段快照。前端通过字段级 diff 对比展示变更：

```typescript
// beforeState / afterState 结构
interface GovernanceStateSnapshot {
  [fieldName: string]: unknown;  // 字段名 → 字段值，扁平 key-value 结构
}

// 示例：角色变更操作
// beforeState: { "role": "member", "muted": false }
// afterState:  { "role": "admin",  "muted": false }

// 示例：禁言操作
// beforeState: { "muted": false, "muteEndTime": null }
// afterState:  { "muted": true,  "muteEndTime": "2026-06-07T12:00:00Z" }
```

**前端渲染规则**：
- 对 beforeState 和 afterState 做 key 级别 diff，生成三类变更：新增字段（绿色）、删除字段（红色）、修改字段（黄色）
- 未变更字段默认折叠，点击展开显示完整快照
- 嵌套对象递归 diff，数组按索引逐元素对比
- 时间字段格式化为本地时间展示

## Test Strategy

### 单元与组件测试

| 测试文件 | 测试策略 |
|---------|---------|
| `useChannelContext.test.ts` | 单元测试：composable 状态隔离、loadContext/resetContext、权限 computed |
| `SubscribeButton.test.ts` | 组件测试：状态机切换（6种状态）、乐观更新与回滚、loading 状态 |
| `PrivacySettings.test.ts` | 组件测试：Radio.Group 切换、系统频道锁定、确认弹窗、保存 loading |
| `JoinMethodSettings.test.ts` | 组件测试：三种模式切换、审核配置项展示、邀请列表渲染 |
| `JoinApplyModal.test.ts` | 组件测试：表单验证（10-200字）、提交状态、已有申请禁用 |
| `PendingApplications.test.ts` | 组件测试：列表渲染、超时高亮、单条/批量操作、结果 Modal |
| `MemberList.test.ts` | 组件测试：角色筛选、搜索、批量操作、权限控制 |
| `SubscriptionList.test.ts` | 组件测试：分组切换、搜索过滤、提醒开关、取消订阅 |
| `BlacklistPage.test.ts` | 组件测试：列表渲染、移出确认 |
| `GovernanceLog.test.ts` | 组件测试：筛选、详情 Drawer |
| `channelOperation.test.ts` | 单元测试：乐观更新封装、缓存失效逻辑 |

### 集成测试场景

#### 场景 1: 订阅流程

**前置条件**: 用户已登录，存在公开频道 A（可订阅）和私有频道 B（需申请）。

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入频道 A 主页 | SubscribeButton 显示"订阅"状态 |
| 2 | 点击"订阅" | 按钮立即变为"已订阅"（乐观更新），后台发送订阅请求 |
| 3 | 进入个人订阅列表 | 频道 A 出现在订阅列表中 |
| 4 | 在订阅列表中关闭频道 A 的提醒 | 提醒开关切换成功，接口同步 |
| 5 | 在订阅列表中取消订阅频道 A | 频道 A 从列表移除，频道 A 主页按钮恢复为"订阅" |
| 6 | 进入私有频道 B 主页，点击"订阅" | 显示 JoinApplyModal，填写申请理由并提交 |
| 7 | 提交申请后再次点击"订阅" | 按钮显示"已申请"且不可点击 |

#### 场景 2: 申请加入流程

**前置条件**: 用户 A 已登录为频道管理员，用户 B 已登录并申请加入私有频道。

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 用户 B 提交加入申请 | JoinApplyModal 关闭，提示"申请已提交" |
| 2 | 用户 A 进入待审队列页 | 用户 B 的申请出现在列表中，显示申请理由和提交时间 |
| 3 | 等待超过阈值时间 | 超时申请行高亮显示 |
| 4 | 用户 A 点击单条"通过" | 申请从待审列表移除，用户 B 成为频道成员 |
| 5 | 用户 A 批量选择多条申请，点击"批量通过" | Loading Modal 显示进度，完成后逐条展示结果 |
| 6 | 用户 A 批量选择多条申请，点击"批量拒绝" | 弹出统一拒绝原因输入框，确认后批量拒绝 |

#### 场景 3: 成员管理流程

**前置条件**: 频道已有多个成员（不同角色），当前用户为管理员。

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入成员列表页 | 列表展示所有成员，支持角色筛选和搜索 |
| 2 | 搜索特定成员 | 列表按关键词过滤 |
| 3 | 选择某成员，点击"变更角色" | RoleAssignModal 显示当前角色，可选择新角色 |
| 4 | 确认角色变更 | 成员角色更新，列表刷新 |
| 5 | 选择某成员，点击"禁言" | MuteModal 显示，可设置禁言时长 |
| 6 | 确认禁言 | 成员状态变为"已禁言" |
| 7 | 批量选择成员，点击"移除" | RemoveMemberModal 显示确认，确认后批量移除 |
| 8 | 被移除成员再次访问频道 | 频道主页显示"已被移除"提示 |

#### 场景 4: 治理操作流程

**前置条件**: 频道已产生治理操作记录（角色变更、禁言、移除等）。

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入治理日志页 | 列表展示治理操作记录，含操作类型、操作人、时间 |
| 2 | 按操作类型筛选 | 列表仅显示匹配类型记录 |
| 3 | 点击某条记录的"查看详情" | GovernanceDetailDrawer 打开，显示 beforeState/afterState 字段级 diff |
| 4 | 查看 beforeState/afterState 对比 | JSON diff 高亮显示变更字段，新增/删除/修改一目了然 |
| 5 | 关闭 Drawer 返回列表 | 列表状态保持，筛选条件不丢失 |

### E2E 测试策略

对于以下复杂流程，建议在集成测试基础上补充 E2E 测试覆盖：

- **订阅状态机完整流转**: 从未订阅 → 订阅 → 取消订阅 → 重新订阅 → 申请中 → 已订阅的全链路验证，确保状态切换无死锁或中间态残留
- **批量审核流程**: 大量申请（50+ 条）的批量审核操作，验证分页加载、批量提交、部分失败处理的端到端体验
- **频道隐私切换影响链**: 公开 → 私有切换后，验证订阅按钮、加入方式、成员列表等关联 UI 的同步更新

E2E 测试建议使用 Playwright，优先覆盖桌面端核心路径，移动端作为 P2 补充。

## API 路径映射

前端 API 模块路径与后端 Controller 路径对应关系：

| 前端 API 文件 | 前端函数路径前缀 | 后端 Controller | 后端路径前缀 |
|--------------|----------------|----------------|-------------|
| `api/content/channelSubscription.ts` | `/channel/subscription/*` | ChannelSubscriptionController | `/channel/subscription` |
| `api/content/channelMember.ts` | `/channel/member/*` | ChannelMemberController | `/channel/member` |
| `api/content/channelInvite.ts` | `/channel/invite/*` | ChannelInviteController | `/channel/invite` |
| `api/content/channelGovernance.ts` | `/channel/governance/*` | ChannelGovernanceController | `/channel/governance` |
| `api/content/channelPrivacy.ts` | `/api/v1/channels/privacy`, `/api/v1/channels/join-method` | ChannelController | `/api/v1/channels` |
| `api/content/channelBlacklist.ts` | `/channel/governance/blacklist/*` | ChannelGovernanceController | `/channel/governance` |

前端 API 文件统一通过 `defHttp` 发起请求，路径与后端一一对应，无需额外转换层。隐私设置和加入方式接口挂在 ChannelController 下（`PUT /api/v1/channels/privacy`、`PUT /api/v1/channels/join-method`），黑名单接口挂在 ChannelGovernanceController 下（`/channel/governance/blacklist/*`）。

## Migration Plan

N/A — 本 change 不涉及部署变更，纯前端功能新增。后端 API 由 channel-privacy-membership 变更提供。

1. **开发阶段**: 先实现 useChannelContext composable，再按页面逐个实现
2. **Mock 阶段**: 后端 API 未就绪时使用 Mock 数据开发
3. **联调阶段**: API 就绪后切换真实接口，验证完整流程
4. **响应式适配**: 桌面端完成后统一处理移动端适配

## Open Questions

1. 频道主页的具体入口和路由？（假设已有 `/channel/:id` 路由）
2. 订阅列表页面的入口位置？（假设在个人中心或频道模块导航中有"我的订阅"入口）
3. 成员管理页、黑名单页、治理日志页是否为同一页面的不同标签页？（假设为频道管理下的子页面）
4. 通知系统的具体接口？（假设由后端异步处理，前端仅展示操作结果）
5. 邀请链接的格式和域名？（假设由后端生成完整链接，前端仅展示和复制）
