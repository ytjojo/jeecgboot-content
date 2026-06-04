## Context

后端 EPIC-22 频道内容发布与治理能力已部分实现。实际后端有 8 个端点（2 个发布 + 1 个内容审核 + 1 个频道审核 + 1 个治理 + 3 个公告），spec 中引用的 13 个端点尚未实现。治理和审核操作使用统一入口 + action 字段模式（非 spec 设计的独立端点）。详见 `backend-issues.md`。

现有前端基础设施：
- 项目基于 Vue3 + TypeScript + Ant Design Vue 4
- 组件库：JVxeTable（表格+批量操作）、Table、Form（schema 驱动）、Modal、Drawer、Tinymce（富文本编辑）
- 状态管理：Pinia（现有 store 模块位于 `src/store/modules/`）
- API 封装：`defHttp` from `src/utils/http/axios`
- 权限控制：`usePermission` hook
- 消息反馈：`useMessage` hook
- 路由：`src/router/` 目录下配置

当前无 `src/views/channel/` 目录，需从零创建频道管理相关页面。内容发布页已有基础能力，需集成频道选择组件。

**约束**:
- 遵循项目现有组件选型（JVxeTable 用于复杂表格、Form schema 驱动配置式表单）
- API 调用统一使用 `defHttp` 封装
- 新增 Store 遵循 Pinia 模块化规范
- 移动端需支持响应式布局（表格 < md 断点切换为卡片列表）
- 埋点使用项目现有工具（`src/utils/track/`）

## Goals / Non-Goals

**Goals:**
- 实现频道选择组件（ChannelSelector），支持搜索、多选、权限状态预览
- 实现发布权限配置和发布限额配置页面
- 实现待审区管理页面，支持逐条/批量审核
- 实现内容治理页面（置顶、精华、删除、移出、编辑协助）
- 实现回收站和治理日志页面
- 实现频道公告管理页面（Tinymce 富文本 + 预览）
- 实现已发布内容添加到频道功能
- 全部页面支持 PC + 移动端响应式

**Non-Goals:**
- 不涉及频道创建和所有权管理 UI
- 不涉及频道隐私和成员管理 UI
- 不涉及推荐发现和数据统计 UI
- 不实现通知系统 UI（复用已有通知基础设施）
- 不实现平台级内容安全审核 UI

## Decisions

### Decision 1: 页面组织结构

**选择**: 在 `src/views/channel/` 下按功能模块创建子目录，频道管理后台使用 Tab 切换。

**理由**: 频道管理后台的待审区、内容管理、回收站、治理日志、公告管理属于同一业务域的不同功能面，使用 Tab 切换可以减少路由配置复杂度，用户在同一页面内切换更流畅。

**目录结构**:
```
src/views/channel/
├── publish/                    # 发布相关
│   ├── ChannelSelector.vue     # 频道选择组件
│   ├── PublishResult.vue       # 发布结果反馈
│   └── ScheduledPublish.vue    # 定时发布管理
├── settings/                   # 频道设置
│   ├── PublishPermission.vue   # 发布权限配置
│   └── Announcement.vue        # 公告管理
├── governance/                 # 治理后台（Tab 页）
│   ├── index.vue               # Tab 容器
│   ├── ReviewQueue.vue         # 待审区
│   ├── ContentManage.vue       # 内容管理
│   ├── RecycleBin.vue          # 回收站
│   ├── GovernanceLog.vue       # 治理日志
│   └── AnnouncementManage.vue  # 公告管理 Tab
└── components/                 # 共享业务组件
    ├── RejectReasonModal.vue
    ├── MoveChannelDialog.vue
    ├── EditAssistDrawer.vue
    ├── AddContentDialog.vue
    └── GovernanceActionMenu.vue
```

**备选方案**: 每个功能独立路由页面 → 路由配置过多，用户在治理功能间切换需频繁导航。

### Decision 2: 状态管理策略

**选择**: 新建 3 个 Pinia Store，按业务域划分。

**理由**: 发布、审核、治理是三个独立的业务域，各自有独立的列表数据、筛选条件和操作状态。按域划分 Store 职责清晰，避免单一 Store 过大。

**Store 设计**:
- `useChannelPublishStore`: 已选频道、发布结果、定时发布任务
- `useChannelReviewStore`: 待审列表、筛选条件、批量选中、统计
- `useChannelGovernanceStore`: 内容列表、回收站、治理日志、筛选条件

**备选方案**: 单一 `useChannelStore` → 职责过多，状态耦合，不利于按需加载。

### Decision 3: 频道选择组件实现方式

**选择**: 使用 Modal + 自定义列表实现，频道按角色分组展示（推荐/我管理的/我加入的），支持搜索防抖和虚拟滚动。

**理由**: 频道选择器需要展示权限状态（可发布/待审/不可发布+原因），标准 Tree 或 Select 组件无法满足。Modal 弹窗形式在 PC 和移动端都有良好体验。

**备选方案**: 使用 Tree 组件 → 频道不是树形结构，分组是逻辑分组而非层级关系。

### Decision 4: 表格组件选型

**选择**: 待审区和内容治理列表使用 JVxeTable（支持行编辑、批量操作、分页）；回收站和治理日志使用基础 Table 组件。

**理由**: 待审区和内容治理需要复杂的批量操作（全选、批量通过/拒绝、批量删除/置顶/精华），JVxeTable 内置这些能力。回收站和治理日志是只读列表，基础 Table 更轻量。

### Decision 5: 移动端适配策略

**选择**: 使用 Ant Design Vue 4 的响应式断点，表格在 `< md` 断点时切换为卡片列表模式，弹窗在移动端使用全屏弹窗，筛选区折叠为抽屉。

**理由**: 项目已使用 Ant Design Vue 4，其断点系统（xs/sm/md/lg/xl）与组件库原生集成。卡片列表模式在移动端提供更好的触控体验。

### Decision 6: 公告编辑器方案

**选择**: 复用项目已有的 Tinymce 组件（`src/components/Tinymce/`），公告预览使用 HTML 渲染。

**理由**: 项目已集成 Tinymce，无需引入新的富文本编辑器。预览直接渲染 HTML 即可，通过后端 preview 接口做安全过滤。

## Risks / Trade-offs

**[Risk] 频道选择器性能** → 频道数超过 50 时启用虚拟滚动；搜索使用 300ms 防抖减少请求频率。

**[Risk] 多频道发布部分失败的 UX** → PublishResult 组件逐频道展示结果，失败项展示具体原因和重试按钮，用户可选择性重试。

**[Risk] 待审区超时提醒的实时性** → 前端每 60 秒轮询 `/api/channel/review/stats` 更新 badge；超时内容在列表中高亮标识。

**[Risk] 公告并发编辑冲突** → 保存时后端校验版本号（乐观锁），前端捕获冲突后提示用户刷新。

**[Risk] 移动端表格体验** → 表格在 < md 断点切换为卡片列表，批量操作收纳到顶部菜单，操作按钮固定在卡片底部。

## File Structure

```
jeecgboot-vue3/
├── src/api/content/channel/
│   ├── publish.ts              # 发布相关 API
│   ├── review.ts               # 审核相关 API
│   ├── governance.ts           # 治理相关 API
│   ├── announcement.ts         # 公告相关 API
│   └── addContent.ts           # 添加内容 API
├── src/store/modules/
│   ├── channelPublish.ts       # 发布 Store
│   ├── channelReview.ts        # 审核 Store
│   └── channelGovernance.ts    # 治理 Store
├── src/views/channel/
│   ├── publish/
│   │   ├── ChannelSelector.vue
│   │   ├── PublishResult.vue
│   │   └── ScheduledPublish.vue
│   ├── settings/
│   │   ├── PublishPermission.vue
│   │   └── Announcement.vue
│   ├── governance/
│   │   ├── index.vue
│   │   ├── ReviewQueue.vue
│   │   ├── ContentManage.vue
│   │   ├── RecycleBin.vue
│   │   ├── GovernanceLog.vue
│   │   └── AnnouncementManage.vue
│   └── components/
│       ├── RejectReasonModal.vue
│       ├── MoveChannelDialog.vue
│       ├── EditAssistDrawer.vue
│       ├── AddContentDialog.vue
│       └── GovernanceActionMenu.vue
├── src/views/channel/__tests__/  # 测试文件
│   ├── ChannelSelector.test.ts
│   ├── PublishResult.test.ts
│   ├── ReviewQueue.test.ts
│   ├── ContentManage.test.ts
│   ├── RecycleBin.test.ts
│   ├── GovernanceLog.test.ts
│   ├── AnnouncementManage.test.ts
│   ├── AddContentDialog.test.ts
│   ├── RejectReasonModal.test.ts
│   ├── MoveChannelDialog.test.ts
│   ├── EditAssistDrawer.test.ts
│   ├── PublishPermission.test.ts
│   ├── GovernanceActionMenu.test.ts
│   ├── channelPublishStore.test.ts
│   ├── channelReviewStore.test.ts
│   └── channelGovernanceStore.test.ts
```

## Test Strategy

每个组件和 Store 对应一个测试文件，采用 TDD 方式开发：

**组件测试策略**:
- `ChannelSelector.test.ts`: 测试频道列表加载、搜索过滤、多选上限拦截、不可选频道展示原因、已选频道移除
- `PublishResult.test.ts`: 测试逐频道结果展示、失败项重试、定时发布状态展示
- `ReviewQueue.test.ts`: 测试列表加载与筛选、单条通过/拒绝、批量操作、拒绝原因必填校验、超时标识
- `ContentManage.test.ts`: 测试列表加载与筛选排序、置顶/精华操作、删除确认、移出频道、编辑协助
- `RecycleBin.test.ts`: 测试列表加载、单条/批量恢复、超期不可恢复状态
- `GovernanceLog.test.ts`: 测试列表加载与筛选、操作类型筛选
- `AnnouncementManage.test.ts`: 测试公告编辑、预览、发布确认、删除确认、历史版本恢复
- `AddContentDialog.test.ts`: 测试内容搜索选择、目标频道选择、三种场景入口差异
- `RejectReasonModal.test.ts`: 测试拒绝原因必填校验（最少10字）、预设原因选择
- `MoveChannelDialog.test.ts`: 测试目标频道选择、预期结果展示
- `EditAssistDrawer.test.ts`: 测试可编辑字段、修改原因必填、修订历史展示
- `GovernanceActionMenu.test.ts`: 测试菜单项展示、权限控制
- `PublishPermission.test.ts`: 测试四种权限模型切换、限额配置、保存确认

**Store 测试策略**:
- `channelPublishStore.test.ts`: 测试频道选择状态管理、发布结果管理、定时发布任务 CRUD
- `channelReviewStore.test.ts`: 测试待审列表获取、筛选条件、批量操作、统计更新
- `channelGovernanceStore.test.ts`: 测试内容列表获取、治理操作（置顶/精华/删除/移出/恢复）、回收站和日志管理

## Migration Plan

N/A — 本 change 不涉及部署变更。纯前端新增页面和组件，不修改已有功能。新增路由需在前端路由配置中注册，新增 Store 需在 Pinia 初始化时注册。

## Open Questions

1. 单篇内容最多可同步到多少个频道 N？PRD 假设为 5，需确认是否按用户等级区分
2. 定时发布使用哪个调度框架？前端轮询间隔需与后端对齐
3. 编辑协助是否需要作者确认后才生效？PRD 假设即时生效
4. 作者是否可以拒绝他人将自己的作品添加到频道？PRD 未明确
5. 频道选择器是否需要展示频道头像/封面？影响 ChannelSelector 视觉设计
