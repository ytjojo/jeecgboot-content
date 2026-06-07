## Why

内容社区缺少统一的频道管理前端入口，平台管理员、普通用户和组织管理员无法以一致的界面规则创建和管理频道。EPIC-20 后端已定义完整的频道基础设施 API，前端需要配套实现全部用户旅程页面，使频道创建、管理、审核、转让和删除功能端到端可用。

## What Changes

- 新增用户端频道创建页面（支持个人频道和组织频道两种类型，分步向导引导）
- 新增后台端系统频道创建弹窗（管理员直接创建，跳过审核）
- 新增"我的频道"列表页（用户端查看自己所有频道及状态）
- 新增频道管理页（用户端，含概览/编辑信息/设置三个 Tab）
- 新增频道信息编辑功能（区分关键字段需审核、非关键字段立即生效）
- 新增频道转让功能（搜索目标用户、双方确认流程）
- 新增频道删除功能（前置条件校验、7 天冷静期、可撤销）
- 新增后台频道管理页（列表/筛选/详情/批量操作）
- 新增审核队列页（待审核列表、审核详情含修改前后 diff 对比）
- 新增频道相关 API 封装层和 Pinia Store

## Capabilities

### New Capabilities

- `channel-creation`: 频道创建能力，覆盖系统频道创建（后台 Modal）、个人/组织频道创建（用户端分步向导页面），包含表单校验、名称唯一性检查、审核等待页
- `channel-list`: 我的频道列表页，用户端查看自己创建的所有频道，支持筛选、排序、按状态执行不同操作
- `channel-editing`: 频道信息编辑能力，频道管理页 Tab 结构（概览/编辑/设置），Drawer 承载编辑表单，区分关键字段（需审核）与非关键字段（立即生效）
- `channel-transfer`: 频道转让能力，搜索目标用户、二次确认、转让历史记录，支持个人频道和组织频道差异化规则
- `channel-deletion`: 频道删除能力，前置条件校验、输入频道名二次确认、7 天冷静期展示与撤销
- `admin-channel-management`: 后台频道管理页，全量频道列表、多维度筛选、详情查看、系统频道编辑、强制删除
- `review-queue`: 审核队列页，待审核频道列表、审核详情 Drawer（含修改前后 diff 对比）、通过/拒绝/退回修改操作

### Modified Capabilities

（无已有 spec 需要修改）

## Impact

- **新增页面路由**: 用户端 3 个路由（创建、列表、管理），后台端 2 个路由（管理、审核队列）
- **新增 API 调用**: 15 个接口封装（创建、列表、详情、编辑、删除、转让、审核、校验等），全部后端已实现：
  | 接口 | 方法 | 路径 | Controller |
  |------|------|------|-----------|
  | 创建频道 | POST | `/api/v1/content/channels/create` | ChannelController |
  | 创建系统频道 | POST | `/api/v1/content/admin/channels/create-system` | ChannelAdminController |
  | 我的频道列表 | GET | `/api/v1/content/channels/list` | ChannelController |
  | 频道详情 | GET | `/api/v1/content/channels/{id}` | ChannelController |
  | 更新频道 | PUT | `/api/v1/content/channels/{id}` | ChannelController |
  | 删除频道 | DELETE | `/api/v1/content/channels/{id}` | ChannelController |
  | 撤销删除 | POST | `/api/v1/content/channels/{id}/cancel-delete` | ChannelController |
  | 发起转让 | POST | `/api/v1/content/channels/{id}/transfer` | ChannelController |
  | 确认转让 | POST | `/api/v1/content/channels/transfer/{transferId}/confirm` | ChannelController |
  | 拒绝转让 | POST | `/api/v1/content/channels/transfer/{transferId}/reject` | ChannelController |
  | 删除前置校验 | GET | `/api/v1/content/channels/{id}/delete-check` | ChannelController |
  | 转让历史查询 | GET | `/api/v1/content/channels/{id}/transfers` | ChannelController |
  | 待确认转让查询 | GET | `/api/v1/content/channels/{id}/transfer/pending` | ChannelController |
  | 名称唯一性校验 | GET | `/api/v1/content/channels/check-name` | ChannelController |
  | 审核队列列表 | GET | `/api/v1/content/channel/review/list` | ChannelReviewController |
  | 审核操作 | POST | `/api/v1/content/channel/review/action` | ChannelReviewController |
- **新增 Store**: Pinia channel store 管理频道相关状态
- **新增组件**: 10 个共用组件（ChannelForm、ChannelTypeTag、ChannelStatusTag、ReviewDiffViewer 等）
- **依赖**: 复用项目现有 JVxeTable、Form、Modal、Drawer、Upload、useTable、useModal 等基础组件
- **无 Breaking Change**: 纯新增功能，不修改已有页面或接口
