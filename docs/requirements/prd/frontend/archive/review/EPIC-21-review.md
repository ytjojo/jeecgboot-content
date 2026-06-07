# EPIC-21 频道隐私、订阅与成员管理 — 前端 PRD 审核报告

> **审核人**: 🏗️ Winston (System Architect)
> **审核日期**: 2026-06-02
> **审核视角**: 系统架构 — 技术可行性、可扩展性、组件设计

## 总体评价

PRD 质量较高，功能拆分清晰，状态机定义完整，API 设计合理。组件选型基本符合项目 `frontend-standards.md` 的约束。但在状态管理设计、Store 扩展策略、多页面状态同步、以及部分 API 设计细节上存在需要讨论的架构问题。整体可落地，但建议在开发前解决以下中高优先级问题。

## 优点

1. **状态机定义清晰**：7.2 节频道主页操作按钮状态机覆盖了公开/私有、订阅/未订阅、申请/审核/拒绝/冷却期/黑名单等所有状态组合，开发者可直接据此实现状态判断逻辑。

2. **组件选型务实**：严格复用项目现有 `Form`、`Table`、`Modal`、`Drawer`、`JVxeTable` 等组件，未引入新依赖，符合 `frontend-standards.md` 的硬规则。

3. **API 设计 RESTful 且完整**：5.1-5.5 共覆盖 5 大类 25+ 接口，URL 命名规范，请求/响应参数明确，前后端对接歧义小。

4. **响应式策略具体**：8.2 节逐页面定义了桌面端到移动端的适配策略（Table 转卡片列表、筛选区折叠为 Drawer 等），可执行性强。

5. **边界状态覆盖全面**：每个功能模块都列出了空状态、加载中、权限不足、操作失败等边界场景，减少了开发时的遗漏。

## 问题与建议

### 🟡 1. Channel Store 设计存在单实例陷阱

**问题**：6.2 节建议新增 `src/store/modules/channel.ts`，`ChannelState` 接口包含 `currentChannel`、`userRelation`、`privacyType` 等字段。Pinia store 默认是全局单例，但频道页面是按 `channelId` 动态切换的。

**风险**：
- 用户在频道 A 的设置页操作后，通过标签页切到频道 B，Store 中残留频道 A 的数据，导致权限判断错误或数据串台。
- `currentChannel` 如果在路由切换时未正确重置，`canManageMembers` 等 computed 属性会基于错误数据计算。

**建议**：
- 方案一（推荐）：将 `channelStore` 设计为按 `channelId` 缓存的 Map 结构，路由守卫或 `onActivated` 时切换当前激活的 channelId。
- 方案二：不使用全局 Store，改为 composable（`useChannelContext(channelId)`），在频道相关页面的根组件中 provide，子组件 inject 使用。这样天然隔离不同频道的上下文。

### 🟡 2. 缺少乐观更新与缓存失效策略

**问题**：订阅/取消订阅（3.3 节）的交互要求"按钮变为 loading -> 成功后变为已订阅"，但未说明：
- 订阅操作成功后，订阅列表页、信息流等其他页面的数据如何同步。
- 频道 Store 中的 `isSubscribed` 字段何时刷新。

**风险**：用户在频道主页订阅后，切换到订阅列表页看不到刚订阅的频道，或信息流未即时反映订阅关系变化。

**建议**：
- 核心操作（订阅/取消订阅/申请加入/审核）采用乐观更新：先更新本地 Store 状态，请求失败时回滚。
- 定义缓存失效规则：订阅操作成功后，invalidate `/api/channel/subscription/list` 和 `/api/channel/subscription/status/{channelId}` 的缓存。
- 在 PRD 中明确"操作成功后需要同步更新的页面/数据列表"。

### 🟡 3. API 设计 — 缺少 channelId 参数一致性

**问题**：5.2 节成员相关接口中，部分接口缺少 `channelId` 参数：
- `POST /api/channel/member/application/approve` 只有 `applicationIds[]`，但前端审批场景中需要知道当前频道上下文。
- `POST /api/channel/member/remove` 只有 `memberIds[]` 和 `reason`，但 `memberId` 的全局唯一性需要确认（同一用户在不同频道可能有不同的 member 记录）。
- `POST /api/channel/blacklist/add` 只有 `userId` 和 `reason`，缺少 `channelId`，黑名单是频道级还是平台级？

**建议**：
- 所有成员/治理相关接口统一加上 `channelId` 参数，即使 `memberId` 本身已隐含频道信息——前端通常只知道当前频道上下文，不应要求前端额外查询 memberId。
- 明确 `memberId` 的语义：是频道内的成员记录 ID，还是用户 ID？如果是成员记录 ID，前端需要从成员列表接口获取，增加了一次请求依赖。

### 🟡 4. 批量操作缺乏并发控制和进度反馈

**问题**：3.5 节待审队列和 3.8 节成员管理都支持批量操作（批量批准、批量移除、批量禁言），但：
- 未定义批量操作的并发策略（是一次请求传数组，还是逐条请求？）。
- 9.1 节写"异步处理，展示进度或结果 Modal"，但未定义进度展示的具体交互。

**风险**：
- 如果是一次请求传数组，后端需要支持事务性批量操作（部分失败时的回滚策略）。
- 如果是逐条请求，前端需要管理并发数、进度百分比、失败重试，复杂度显著上升。

**建议**：
- 与后端协商确认：批量接口是否为原子操作（全部成功或全部失败），还是允许部分成功。
- PRD 中明确批量操作的接口契约：`POST /api/channel/member/application/approve` 的响应中应包含每条处理结果（成功/失败+原因），而非仅返回总体状态。
- 前端实现建议：采用单次批量请求 + 结果 Modal 逐条展示，避免逐条请求带来的网络开销和状态管理复杂度。

### 🟢 5. 频道设置页表单拆分不明确

**问题**：3.1 节隐私设置和 3.2 节加入方式配置都在"频道设置页"，但未说明：
- 两者是否属于同一个表单（共享保存按钮），还是独立保存。
- 隐私从"公开"切到"私有"后，加入方式的配置项是否需要联动变化（例如：公开频道不应出现"邀请加入"选项？）。

**建议**：
- 明确隐私设置和加入方式是独立保存（各自有保存按钮），降低表单复杂度和数据冲突风险。
- 明确联动规则：隐私状态变更时，加入方式选项是否需要动态显示/隐藏/禁用。

### 🟢 6. 订阅列表分组功能缺少后端接口支撑

**问题**：3.7 节订阅列表支持分组功能（新建分组、移动频道到分组、分组筛选），5.1 节列出了分组 CRUD 接口，但：
- 未说明默认分组的逻辑（"默认分组"是用户创建的第一个分组，还是系统自动创建的？）。
- 未说明频道在分组中的归属关系是否唯一（一个频道能否同时属于多个分组？）。

**建议**：
- 明确分组归属模型：一对多（一个频道只属于一个分组）还是多对多。
- 明确默认分组的初始化逻辑：新用户的订阅是否自动进入"默认分组"。

### 🟢 7. 治理操作日志缺少实时性要求

**问题**：3.10 节治理日志支持按操作类型、时间范围、操作者筛选，但未说明：
- 日志是否需要实时推送（WebSocket），还是仅支持主动刷新/轮询。
- 日志数据量预估和分页策略。

**建议**：
- 本期治理日志采用分页查询即可（当前 PRD 的 P1 优先级定位合理），无需实时推送。
- 如果后续需要实时性，可复用项目已有的 WebSocket 通道推送治理事件。

### 🟢 8. 移动端响应式策略实现成本偏高

**问题**：8.2 节成员管理页的移动端适配要求"表格转为卡片列表，操作改为长按/滑动菜单"。长按/滑动菜单在 Ant Design Vue 中没有原生支持，需要自定义实现或引入第三方库。

**建议**：
- 移动端操作统一采用"点击操作按钮触发 ActionSheet/底部菜单"的模式，避免自定义手势交互的开发和测试成本。
- 与 UX 设计师确认移动端交互方案的可行性。

## 架构建议

### 1. 页面组件架构建议

```
src/views/api/v1/content/channel/
├── settings/                    # 频道设置页
│   ├── index.vue               # 容器组件
│   ├── PrivacySettings.vue     # 隐私设置子组件
│   └── JoinMethodSettings.vue  # 加入方式子组件
├── members/                     # 成员管理页
│   ├── index.vue               # 容器组件 + Tab 切换
│   ├── MemberList.vue          # 成员列表
│   ├── PendingApplications.vue # 待审队列
│   ├── Blacklist.vue           # 黑名单
│   └── GovernanceLog.vue       # 治理日志
├── subscription/                # 订阅列表页
│   ├── index.vue
│   └── SubscriptionCard.vue    # 单个订阅卡片
└── components/                  # 频道模块共享组件
    ├── ChannelSubscribeButton.vue  # 订阅状态按钮（含状态机）
    ├── RoleTag.vue                  # 角色标签
    └── GovernanceActionModal.vue    # 治理操作确认弹窗
```

### 2. Channel Store 建议设计

```typescript
// src/composables/useChannelContext.ts
export function useChannelContext(channelId: Ref<string>) {
  // 响应式频道上下文，天然随 channelId 变化
  const channelInfo = ref<ChannelInfo | null>(null);
  const userRelation = ref<UserChannelRelation | null>(null);

  // 权限判断 computed
  const canManageMembers = computed(() => {
    const role = userRelation.value?.role;
    return role === 'OWNER' || role === 'ADMIN';
  });

  // 数据加载
  async function loadContext() {
    const [info, relation] = await Promise.all([
      getChannelInfo(channelId.value),
      getUserChannelRelation(channelId.value),
    ]);
    channelInfo.value = info;
    userRelation.value = relation;
  }

  return { channelInfo, userRelation, canManageMembers, loadContext };
}
```

### 3. API 模块组织建议

```
src/api/content/channel/
├── subscription.ts    # 订阅相关接口
├── member.ts          # 成员与加入相关接口
├── blacklist.ts       # 黑名单相关接口
├── invite.ts          # 邀请相关接口
└── privacy.ts         # 隐私与设置相关接口
```

### 4. 权限控制架构

建议采用两层权限控制：
- **路由层**：通过 `usePermissionStore` 控制页面访问权限（频道设置页仅频道主/管理员可访问）。
- **组件层**：通过 `useChannelContext` 的 `canManageMembers` 等 computed 控制按钮/操作的可见性。

避免在每个组件中独立判断权限，统一在 `useChannelContext` 中暴露权限标志。

## 总结

PRD 的功能定义完整、边界场景覆盖充分，是一份高质量的前端需求文档。主要的架构风险集中在 **Channel Store 的多频道上下文隔离** 和 **批量操作的接口契约定义** 两个方面。建议在开发启动前优先确认：

1. Channel 上下文的管理策略（Store vs Composable）。
2. 批量操作接口的原子性约定。
3. 成员相关接口中 `channelId` 参数的一致性。

其余问题为中低优先级，可在开发过程中逐步完善。
