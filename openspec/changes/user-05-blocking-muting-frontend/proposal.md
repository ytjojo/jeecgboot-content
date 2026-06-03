## Why

后端 EPIC-05 拉黑/屏蔽能力已全部实现，但前端缺少对应的操作入口、管理页面和交互反馈。用户无法在内容社区中执行拉黑、屏蔽、不感兴趣等降噪操作，也无法管理黑名单、屏蔽列表和屏蔽词。需要补齐前端 UI 层，让后端已有的拉黑/屏蔽能力完整可用。

## What Changes

- 新增拉黑/屏蔽操作入口组件（BlockMuteMenu），嵌入用户主页、内容卡片和评论区
- 新增拉黑确认弹窗、屏蔽确认弹窗和解除拉黑确认弹窗，明确行为边界
- 新增不感兴趣反馈气泡组件（NotInterestedPopover），支持按内容类型/话题屏蔽
- 新增被拉黑状态占位页（BlockedUserPage），区分拉黑发起方和被拉黑方的展示
- 新增屏蔽词命中折叠卡片组件（FilteredContentCard），支持展开/收起
- 新增隐私设置聚合页（PrivacySettingsPage），统一管理黑名单、屏蔽列表和屏蔽词入口
- 新增黑名单管理页（BlacklistPage），支持搜索、分页和解除拉黑
- 新增屏蔽列表管理页（MuteListPage），含屏蔽用户/话题/内容类型/临时屏蔽四个 Tab
- 新增屏蔽词设置页（KeywordFilterPage），支持关键词和正则表达式
- 新增 Pinia Store（blockMute），管理关系状态缓存和列表数量
- 新增 API 封装层（block.ts、mute.ts、filterRule.ts），对接 12 个后端接口

## Capabilities

### New Capabilities

- `blocking-muting-frontend`: 内容社区降噪与内容保护前端能力，覆盖拉黑/屏蔽操作入口、确认弹窗、管理页面、屏蔽词设置和不感兴趣反馈交互。

### Modified Capabilities

- 无。

## Impact

- 影响前端项目 `jeecgboot-vue3/`，新增约 10 个 Vue 组件和 1 个 Pinia Store
- 新增 API 封装文件：`src/api/content/block.ts`、`src/api/content/mute.ts`、`src/api/content/filterRule.ts`
- 新增组件路径：`src/views/content/components/` 和 `src/views/content/privacy/`
- 新增 Store：`src/store/modules/blockMute.ts`
- 需要修改现有内容卡片、用户主页和评论区组件以嵌入拉黑/屏蔽操作入口
- 依赖后端 `user-05-blocking-muting` 变更（已完成）
- 依赖 Ant Design Vue 4 的 Dropdown、Modal、Tabs、Statistic、Empty 等组件
