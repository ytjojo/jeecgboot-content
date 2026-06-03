## Why

后端 EPIC-22 频道内容发布与治理能力已设计完成（含发布权限模型、审核流程、内容治理、公告管理等），但前端缺少对应的用户界面。当前频道管理后台仅有基础的频道信息展示，无法进行发布权限配置、投稿审核、内容治理、公告管理等操作。需要实现完整的前端 UI 层，让频道主、管理员和创作者能够通过可视化界面使用后端已定义的全部发布与治理能力。

## What Changes

**频道选择与发布流程**
- From: 发布内容时无频道选择能力
- To: 新建 ChannelSelector 组件，支持搜索、多选、权限状态预览；发布后展示逐频道结果反馈（PublishResult）；支持定时发布设置与"我的定时发布"管理
- Impact: 新增能力，集成到现有内容发布页

**发布权限与限额配置**
- From: 无频道级发布权限配置界面
- To: 新建发布权限配置页（PublishPermissionConfig），支持四种权限模型切换；新建发布限额配置（PublishLimitConfig），支持每小时/每日上限和字数下限
- Impact: 新增能力，集成到频道设置页

**待审区管理**
- From: 无审核流程界面
- To: 新建待审区管理页（ReviewQueue），支持逐条/批量通过/拒绝、拒绝原因填写、超时高亮标识、筛选分页
- Impact: 新增频道管理后台 Tab

**内容治理**
- From: 无频道级内容管理界面
- To: 新建内容治理页（ContentGovernance），支持置顶/精华/删除/移出频道/编辑协助；新建回收站页（RecycleBin）支持恢复操作；新建治理日志页（GovernanceLog）
- Impact: 新增频道管理后台 Tab

**频道公告管理**
- From: 无公告管理界面
- To: 新建公告管理页（AnnouncementEditor），支持 Tinymce 富文本编辑、预览、发布/删除、历史版本恢复
- Impact: 新增频道设置页入口

**已发布内容添加到频道**
- From: 内容只能在发布时选择频道
- To: 新建 AddContentDialog 组件，支持三种场景（系统频道添加、个人/组织频道添加、频道主添加他人作品）
- Impact: 新增能力，集成到内容详情页和频道管理页

## Success Criteria

- 频道选择组件加载时间 P95 <= 500ms
- 发布提交响应 P95 <= 500ms
- 待审区列表加载 P95 <= 500ms
- 所有页面支持 PC + 移动端响应式布局
- 全部 13 个业务组件实现并通过单元测试
- 全部 API 对接完成，使用 defHttp 封装
- 三种 Pinia Store（channelPublish/channelReview/channelGovernance）实现并覆盖核心逻辑

## Non-Goals

- 频道创建与所有权管理 UI（EPIC-20 前端）
- 频道隐私与成员管理 UI（EPIC-21 前端）
- 推荐发现 UI（EPIC-23 前端）
- 频道数据统计 UI（EPIC-24 前端）
- 平台级内容安全审核策略
- 完整创作器能力
- 通知系统 UI（复用 EPIC-06 通知基础设施）

## Capabilities

### New Capabilities

- `channel-publishing-ui`: 频道发布前端能力，包括 ChannelSelector 组件、PublishResult 组件、定时发布管理、发布限额配置、发布权限模型配置页
- `channel-content-moderation-ui`: 内容审核前端能力，包括 ReviewQueueTable 组件、RejectReasonModal 组件、逐条/批量审核操作、超时标识、待审统计 badge
- `channel-content-governance-ui`: 内容治理前端能力，包括 ContentGovernance 页、GovernanceActionMenu、RecycleBinTable、GovernanceLogTable、EditAssistDrawer、MoveChannelDialog
- `channel-announcements-ui`: 频道公告前端能力，包括 AnnouncementEditor 组件（Tinymce + 预览）、公告历史版本管理
- `channel-add-existing-content-ui`: 添加已发布内容前端能力，包括 AddContentDialog 组件、三种场景入口集成

### Modified Capabilities

（无已有 spec 需修改）

## Impact

- **前端模块**: jeecgboot-vue3 新增频道发布、审核、治理、公告相关的页面和组件
- **新增页面**: 待审区管理页、内容治理页、回收站页、治理日志页、公告管理页、发布权限配置页
- **新增组件**: ChannelSelector、PublishResult、ReviewQueueTable、GovernanceActionMenu、RecycleBinTable、GovernanceLogTable、AnnouncementEditor、AddContentDialog、RejectReasonModal、MoveChannelDialog、EditAssistDrawer、PublishPermissionRadio、PublishLimitConfig（共 13 个业务组件）
- **新增 Store**: useChannelPublishStore、useChannelReviewStore、useChannelGovernanceStore
- **API 对接**: 对接后端 EPIC-22 全部 REST API（发布、审核、治理、公告、添加内容共 5 组约 25 个接口）
- **依赖**: 依赖后端 EPIC-22 API、EPIC-20 频道状态查询、EPIC-21 角色/禁言/黑名单状态、Tinymce 富文本编辑器、项目现有 JVxeTable/Table/Form/Modal/Drawer 等基础组件
