## Why

频道当前缺乏运营数据看板和完整生命周期管理能力，频道主无法用数据驱动运营决策，平台运营无法统一管理频道审核、冻结、归档、合并和违规处置。需要为频道主提供数据统计看板和数据导出能力，为平台运营提供频道全生命周期管理（审核、冻结、归档、合并、违规治理）和审计追溯能力。

## What Changes

- 新增频道数据看板页面，展示核心指标（订阅数、内容数、PV、UV）、趋势图、互动数据、热门内容排行、用户分析
- 新增数据导出功能，支持 Excel/CSV 格式导出，字段筛选，异步处理
- 新增审核队列页面，支持频道创建/修改审核处理
- 新增频道治理后台页面，支持频道列表查看和生命周期状态管理
- 新增频道治理详情页面，支持生命周期操作（冻结、解冻、隐藏、限制推荐、永久关闭、归档、合并）
- 新增审计日志页面，支持生命周期操作审计查询
- 新增申诉管理页面，支持申诉提交和处理
- 引入 ECharts 图表组件用于数据可视化

## Capabilities

### New Capabilities

- `channel-stats-dashboard`: 频道数据统计看板，包含核心指标、趋势图、互动数据、热门内容、用户分析
- `channel-data-export`: 频道数据导出功能，支持 Excel/CSV 格式、字段筛选、异步处理、导出历史管理
- `channel-review-queue`: 频道审核队列管理，支持创建/修改审核、超时标记、审核操作
- `channel-governance`: 频道治理后台，支持频道列表、生命周期状态管理、高风险操作确认
- `channel-audit-log`: 频道审计日志，支持生命周期操作审计查询和筛选
- `channel-appeal`: 频道申诉管理，支持申诉提交、处理、超时提醒

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

**前端代码影响**:
- 新增 7 个页面组件（数据看板、数据导出、审核队列、治理后台、治理详情、审计日志、申诉管理）
- 新增 9 个业务组件（StatsCard、StatsTrendChart、HotContentTable、UserAnalysisPanel、ExportConfigModal、ReviewDetailDrawer、LifecycleActionModal、AppealModal、StatusTag）
- 新增 4 个 Store（channelStats、channelExport、channelReview、channelGovernance）
- 新增 20+ 个 API 接口对接
- 引入 ECharts 图表库依赖

**API 对接**:
- 统计看板 API（5 个接口）
- 数据导出 API（4 个接口）
- 审核管理 API（3 个接口）
- 生命周期管理 API（11 个接口）
- 审计日志 API（2 个接口）
- 申诉管理 API（4 个接口）

**路由配置**:
- 新增 7 个前端路由
