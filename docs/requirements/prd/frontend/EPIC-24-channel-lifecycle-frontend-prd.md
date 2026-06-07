# EPIC-24 频道数据统计与生命周期管理 -- 前端 PRD

> **史诗ID**: EPIC-24
> **域**: channel（频道域）
> **变更ID**: channel-24-lifecycle-frontend
> **版本**: 1.0
> **前置依赖**: EPIC-20
> **日期**: 2026-06-02

---

## 1. 概述

### 1.1 需求目标

- **需求名称**: 频道数据统计与生命周期管理（channel-lifecycle-stats）
- **一句话概述**: 为频道主提供数据统计看板和数据导出能力，为平台运营提供频道全生命周期管理（审核、冻结、归档、合并、违规治理）和审计追溯能力。
- **要解决的问题**: 频道当前缺乏运营数据看板和完整生命周期管理能力，频道主无法用数据驱动运营决策，平台运营无法统一管理频道审核、冻结、归档、合并和违规处置。
- **期望达成的结果**: 频道从创建后的运营、增长、治理到归档都具备可度量、可审计、可追踪的产品闭环。

### 1.2 目标用户与使用场景

| 角色 | 使用场景 | 入口 |
|------|----------|------|
| 频道主 | 查看频道数据看板、导出数据、发起归档/合并申请 | 频道管理后台 |
| 频道管理员 | 查看频道数据看板 | 频道管理后台 |
| 组织最高管理员 | 审批组织频道合并、查看组织频道数据 | 组织管理后台 |
| 平台运营 | 审核频道、执行冻结/隐藏/关闭/归档/合并、查看审计日志、处理申诉 | 平台运营后台 |
| 系统管理员 | 查看系统频道数据、审计日志 | 平台运营后台 |

**使用场景**: 桌面端（平台运营后台）+ 移动端（频道主数据看板）

### 1.3 范围定义

**本期范围**:
- 频道核心指标看板（订阅数、内容数、PV、UV、趋势图）
- 互动数据与热门内容排行（点赞、评论、收藏、分享、有效访问）
- 用户分析（订阅增量/流失、成员活跃度、贡献排行）
- Excel/CSV 数据导出（字段筛选、异步处理、权限控制）
- 频道审核队列（创建审核、关键字段修改审核、超时标记）
- 频道冻结与解冻（只读治理状态）
- 频道归档（自动归档、手动归档、归档申请审核）
- 频道合并（合并申请、影响范围预览、审核流程）
- 违规处理（限制推荐、强制隐藏、永久关闭）
- 治理审计日志与申诉入口

**非本期范围**:
- 频道创建基础流程（EPIC-20）
- 成员和加入规则（EPIC-21）
- 内容发布与内容级审核（EPIC-22）
- 推荐发现与搜索排序（EPIC-23）
- 底层数据采集方案
- 审核系统和通知系统基础设施建设

---

## 2. 用户故事

### 2.1 数据统计看板

| 故事ID | 用户故事 | 优先级 |
|--------|----------|--------|
| US-01 | 作为频道主，我希望在数据看板查看频道核心指标（订阅数、内容数、PV、UV），以便了解频道整体运营状况 | P0 |
| US-02 | 作为频道主，我希望切换时间范围（日/周/月/自定义）查看指标变化趋势 | P0 |
| US-03 | 作为频道主，我希望查看互动数据（点赞、评论、收藏、分享、有效访问）和热门内容排行 | P1 |
| US-04 | 作为频道主，我希望查看用户分析数据（订阅增量/流失、成员活跃度、贡献排行） | P1 |
| US-05 | 作为频道主，我希望将看板数据导出为 Excel/CSV 格式，支持字段筛选 | P1 |
| US-06 | 作为平台运营，我希望在运营后台查看系统频道的数据看板 | P1 |

### 2.2 生命周期管理

| 故事ID | 用户故事 | 优先级 |
|--------|----------|--------|
| US-07 | 作为平台运营，我希望在审核队列中查看和处理频道创建/修改申请 | P0 |
| US-08 | 作为平台运营，我希望对违规频道执行冻结操作并填写原因 | P0 |
| US-09 | 作为平台运营，我希望对已冻结频道执行解冻操作 | P0 |
| US-10 | 作为平台运营或频道主，我希望将不活跃频道归档 | P1 |
| US-11 | 作为频道主，我希望发起频道合并申请并预览影响范围 | P2 |
| US-12 | 作为平台运营，我希望对违规频道执行限制推荐、强制隐藏或永久关闭 | P0 |
| US-13 | 作为频道主，我希望在处罚通知中查看申诉入口并提交申诉 | P1 |
| US-14 | 作为平台运营，我希望查看治理审计日志并按频道/操作人/时间筛选 | P1 |

---

## 3. 页面设计

### 3.1 页面清单

| 页面 | 路由 | 说明 | 适用角色 |
|------|------|------|----------|
| 频道数据看板 | `/channel/:id/dashboard` | 核心指标、互动数据、热门内容、用户分析 | 频道主、管理员 |
| 数据导出 | `/channel/:id/export` | 导出配置、历史导出记录 | 频道主 |
| 审核队列 | `/admin/channel/review` | 频道创建/修改审核列表与处理 | 平台运营 |
| 频道治理后台 | `/admin/channel/governance` | 频道列表、生命周期状态管理 | 平台运营 |
| 频道治理详情 | `/admin/channel/governance/:id` | 频道详情、生命周期操作、处罚记录 | 平台运营 |
| 审计日志 | `/admin/channel/audit-log` | 生命周期操作审计日志查询 | 平台运营 |
| 申诉管理 | `/admin/channel/appeal` | 申诉列表与处理 | 平台运营 |

### 3.2 频道数据看板页面

**页面结构**:

```
Page
  +-- 顶部信息栏：频道名称 + 数据更新时间 + 统计周期说明
  +-- 筛选区：时间范围选择器（日/周/月/自定义日期区间）+ 导出按钮
  +-- 核心指标卡片区：订阅数 | 内容数 | PV | UV（4 列等宽卡片）
  +-- 趋势图区：折线图展示订阅数/内容数/PV/UV 趋势
  +-- 互动数据区：
  |     +-- 互动指标卡片区：点赞 | 评论 | 收藏 | 分享 | 有效访问
  |     +-- 新增内容统计：数量 + 类型分布
  +-- 热门内容区：
  |     +-- 周期切换 Tabs（近 7 天 / 近 30 天 / 近 90 天）
  |     +-- 热门内容列表（Table：排名、标题、类型、发布时间、有效互动量）
  +-- 用户分析区：
        +-- 订阅增量/流失趋势图
        +-- 成员活跃度占比（饼图或环形图）
        +-- 贡献排行列表（Table：排名、用户、贡献值）
        +-- [组织频道] 按部门/职务统计（权限控制展示）
```

**信息层级**:
- 第一眼：核心指标卡片区（4 个数字 + 更新时间）
- 第二眼：趋势图（快速判断增减趋势）
- 第三眼：互动数据和热门内容（内容表现）
- 第四眼：用户分析（深度运营洞察）

**交互规则**:
- 时间范围选择器变更后，全页面数据联动刷新
- 趋势图支持 hover 查看具体数值
- 热门内容列表点击标题可跳转到内容详情
- 用户分析的部门/职务维度仅在操作者有权限时展示，无权限时展示"权限不足"提示

### 3.3 数据导出页面

**页面结构**:

```
Page
  +-- 导出配置区：
  |     +-- 时间范围选择器
  |     +-- 字段多选（Checkbox Group）
  |     +-- 格式选择（Excel / CSV Radio）
  |     +-- 预计行数展示
  |     +-- 导出按钮
  +-- 导出历史列表区：
        +-- Table：导出时间、导出范围、格式、行数、状态（处理中/已完成/失败）、操作（下载/重试）
```

**交互规则**:
- 选择字段和时间范围后，实时展示预计行数
- 预计行数超过 10,000 时展示提示："数据量较大，导出将在后台处理，完成后可在此页面下载"
- 导出按钮点击后进入 loading 状态，防止重复提交
- 导出历史列表按导出时间倒序
- "处理中"状态行展示进度指示，"已完成"行展示下载按钮，"失败"行展示重试按钮和失败原因
- 下载有效期默认 7 天，过期后下载按钮置灰并提示"文件已过期"
- 导出任务轮询策略：页面进入时启动 3 秒间隔轮询，批量查询所有 `processing` 状态任务状态；全部变为 `completed`/`failed` 后停止；页面离开时取消轮询（详见 4.5 节）

### 3.4 审核队列页面

**页面结构**:

```
Page
  +-- 筛选区：
  |     +-- 频道类型（个人/组织/系统 Select）
  |     +-- 申请类型（创建/修改 Select）
  |     +-- 状态（待审核/已通过/已拒绝 Select）
  |     +-- 提交时间范围（DatePicker Range）
  |     +-- 搜索按钮 + 重置按钮
  +-- 审核列表区：
        +-- Table：
              列：申请编号、频道名称、频道类型、申请人、申请类型、提交时间、状态、超时标记、操作
              操作列：查看详情、审核（通过/拒绝/退回修改）
```

**交互规则**:
- 超过 24 小时未处理的申请在"状态"列旁展示红色超时标记
- 点击"查看详情"打开 Drawer 或 Modal，展示频道名称、简介、图标、封面、分类、申请人信息、历史审核记录
- 点击"审核"弹出审核 Modal：选择通过/拒绝/退回修改 + 原因输入框（拒绝和退回修改时必填）
- 审核完成后列表自动刷新，该申请状态更新
- 空状态：无审核申请时展示空状态插图和文案"暂无待审核申请"

### 3.5 频道治理后台页面

**页面结构**:

```
Page
  +-- 筛选区：
  |     +-- 频道名称搜索
  |     +-- 频道类型 Select
  |     +-- 生命周期状态 Select（Active/ReadonlyFrozen/Hidden/Archived/Merged/Closed）
  |     +-- 搜索 + 重置按钮
  +-- 频道列表区：
        +-- Table：
              列：频道名称、频道类型、当前状态（Tag 标签）、订阅数、最后活跃时间、创建时间、操作
              操作列：查看详情
```

**交互规则**:
- 生命周期状态使用不同颜色 Tag 区分：Active（绿色）、ReadonlyFrozen（橙色）、Hidden（红色）、Archived（灰色）、Merged（蓝色）、Closed（黑色）
- 点击"查看详情"跳转到频道治理详情页

### 3.6 频道治理详情页面

**页面结构**:

```
Page
  +-- 频道基本信息区（Description 组件）：
  |     频道名称、频道类型、当前状态、创建时间、订阅数、内容数、最后活跃时间
  +-- 操作按钮区：
  |     根据当前状态动态展示可用操作按钮
  +-- Tab 切换区：
        Tab 1 - 近期内容：最近发布的内容列表
        Tab 2 - 互动数据：近期互动趋势
        Tab 3 - 历史处罚：处罚记录列表
        Tab 4 - 审计日志：该频道的操作日志
        Tab 5 - 申诉记录：该频道的申诉记录
```

**操作按钮动态规则**:

| 当前状态 | 可用操作 |
|----------|----------|
| Active | 冻结、限制推荐、强制隐藏、归档、合并、永久关闭 |
| ReadonlyFrozen | 解冻、限制推荐、强制隐藏、永久关闭 |
| Hidden | 恢复可见、永久关闭 |
| Archived | 恢复运营（需确认） |
| Merged | 无操作（展示目标频道入口） |
| Closed | 无操作（不可恢复） |

**交互规则**:
- 所有高风险操作（冻结、解冻、隐藏、限制推荐、永久关闭、归档、合并）点击后弹出二次确认 Modal
- 确认 Modal 内容：操作名称、操作原因输入框（必填）、影响范围说明、确认/取消按钮
- 永久关闭操作需额外输入频道名称进行确认
- 操作成功后页面刷新，状态更新，展示 Toast 提示

### 3.7 审计日志页面

**页面结构**:

```
Page
  +-- 筛选区：
  |     +-- 频道名称搜索
  |     +-- 操作人搜索
  |     +-- 操作类型 Select（冻结/解冻/隐藏/限制推荐/永久关闭/归档/合并/删除）
  |     +-- 操作时间范围（DatePicker Range）
  |     +-- 搜索 + 重置按钮
  +-- 日志列表区：
        +-- Table：
              列：操作时间、频道名称、操作人、操作类型（Tag）、前后状态、原因、影响范围
```

**交互规则**:
- 操作类型使用不同颜色 Tag 区分
- 点击行可展开详情（或跳转详情），展示完整审计信息：操作人、操作对象、前后状态、原因、时间、影响范围、通知结果
- 日志列表按操作时间倒序
- 支持分页，默认每页 20 条

### 3.8 申诉管理页面

**页面结构**:

```
Page
  +-- 筛选区：
  |     +-- 申诉状态 Select（待处理/已处理/已驳回）
  |     +-- 频道名称搜索
  |     +-- 提交时间范围
  |     +-- 搜索 + 重置按钮
  +-- 申诉列表区：
        +-- Table：
              列：申诉编号、频道名称、处罚类型、申诉人、提交时间、状态、操作
              操作列：查看详情、处理
```

**交互规则**:
- 点击"处理"打开处理 Modal：处理结果（恢复状态/维持原处理 Radio）+ 处理说明输入框
- 详情展示：处罚信息、申诉说明、补充材料、历史处理记录
- 申诉首次响应 SLA <= 3 个工作日，超时展示提醒标记

---

## 4. 组件设计

### 4.1 StatsCard - 指标卡片

**用途**: 数据看板核心指标展示

**Props**:
```typescript
interface StatsCardProps {
  title: string;          // 指标名称：订阅数、内容数、PV、UV
  value: number;          // 当前值
  trend?: number;         // 趋势变化值（正数增长，负数下降）
  trendPeriod?: string;   // 趋势周期说明
  loading?: boolean;      // 加载状态
  updatedAt?: string;     // 数据更新时间
}
```

**展示规则**:
- 数值使用 CountTo 数字动画组件
- 趋势值展示上升/下降箭头和百分比
- 加载时展示骨架屏

### 4.2 StatsTrendChart - 趋势图

**用途**: 核心指标趋势折线图

**Props**:
```typescript
interface StatsTrendChartProps {
  data: {
    date: string;
    subscribeCount: number;
    contentCount: number;
    pv: number;
    uv: number;
  }[];
  timeRange: 'day' | 'week' | 'month' | 'custom';
  loading?: boolean;
}
```

**展示规则**:
- 折线图支持多指标叠加展示
- 支持 hover 查看具体数值 tooltip
- 支持点击图例切换显示/隐藏某指标

### 4.3 HotContentTable - 热门内容表格

**用途**: 热门内容排行展示

**Props**:
```typescript
interface HotContentTableProps {
  data: {
    rank: number;
    contentId: string;
    title: string;
    contentType: string;
    publishTime: string;
    effectiveInteraction: number;
  }[];
  period: '7d' | '30d' | '90d';
  loading?: boolean;
  onPeriodChange: (period: string) => void;
}
```

**排名规则**:
- 排名由后端根据有效互动量（`effectiveInteraction`）降序计算后返回
- 并列排名采用「并列跳号」规则：互动量相同的内容排名相同，后续排名跳过并列位次（如 1, 2, 2, 4, 5）
- 前端直接使用后端返回的 `rank` 值展示，不做前端重排序

### 4.4 UserAnalysisPanel - 用户分析面板

**用途**: 订阅增量/流失、成员活跃度、贡献排行展示

**Props**:
```typescript
interface UserAnalysisPanelProps {
  subscribeTrend: { date: string; increase: number; churn: number }[];
  activeDistribution: { label: string; value: number }[];
  contributionRank: { rank: number; userId: string; nickname: string; value: number }[];
  hasOrgPermission?: boolean;  // 是否有组织数据查看权限
  departmentStats?: any[];     // 部门统计（仅组织频道且有权限时展示）
}
```

### 4.5 ExportConfigModal - 导出配置弹窗

**用途**: 数据导出配置

**Props**:
```typescript
interface ExportConfigModalProps {
  visible: boolean;
  channelId: string;
  availableFields: { key: string; label: string }[];
  onSubmit: (config: ExportConfig) => Promise<void>;
  onCancel: () => void;
}

interface ExportConfig {
  timeRange: [string, string];
  fields: string[];
  format: 'excel' | 'csv';
}
```

**交互规则**:
- 字段选择使用 Checkbox Group，默认全选
- 格式选择使用 Radio
- 选择后实时展示预计行数
- 提交按钮 loading 防重复

**导出任务轮询策略**:
- 轮询间隔：3 秒
- 轮询目标：查询导出历史列表中所有 `processing` 状态的任务，使用批量查询接口（`fetchHistory`），非逐个查询
- 终止条件：所有 `processing` 状态任务变为 `completed` 或 `failed` 后停止轮询
- 页面离开时取消：`onUnmounted` 时清除轮询定时器，避免无效请求
- 多任务并行：同一页面存在多个 `processing` 任务时，一次批量查询获取所有任务最新状态，非独立轮询

### 4.6 ReviewDetailDrawer - 审核详情抽屉

**用途**: 审核申请详情展示

**Props**:
```typescript
interface ReviewDetailDrawerProps {
  visible: boolean;
  reviewId: string;
  onApprove: (reviewId: string) => Promise<void>;
  onReject: (reviewId: string, reason: string) => Promise<void>;
  onReturn: (reviewId: string, reason: string) => Promise<void>;
  onClose: () => void;
}
```

### 4.7 LifecycleActionModal - 生命周期操作确认弹窗

**用途**: 冻结、解冻、隐藏、限制推荐、永久关闭、归档、合并等高风险操作确认

**Props**:
```typescript
interface LifecycleActionModalProps {
  visible: boolean;
  actionType: 'freeze' | 'unfreeze' | 'hide' | 'restrict' | 'close' | 'archive' | 'merge';
  channelName: string;
  channelId: string;
  impactDescription: string;  // 影响范围说明
  requireNameConfirm?: boolean; // 永久关闭时需输入频道名称确认
  onSubmit: (reason: string) => Promise<void>;
  onCancel: () => void;
}
```

**交互规则**:
- 操作类型为"永久关闭"时，需额外输入频道名称进行确认
- 原因输入框必填，最少 10 个字符
- 提交按钮在请求期间 loading 并禁用

### 4.8 AppealModal - 申诉弹窗

**用途**: 频道主提交申诉

**Props**:
```typescript
interface AppealModalProps {
  visible: boolean;
  channelId: string;
  punishmentType: string;
  punishmentReason: string;
  appealDeadline: string;
  onSubmit: (data: { description: string; attachments?: File[] }) => Promise<void>;
  onCancel: () => void;
}
```

### 4.9 StatusTag - 生命周期状态标签

**用途**: 统一展示频道生命周期状态

**Props**:
```typescript
interface StatusTagProps {
  status: 'PendingReview' | 'Active' | 'ReadonlyFrozen' | 'Hidden' | 'Archived' | 'Merged' | 'Closed' | 'Deleted';
}
```

**颜色映射**:

| 状态 | 颜色 | 文案 |
|------|------|------|
| PendingReview | blue | 待审核 |
| Active | green | 正常运营 |
| ReadonlyFrozen | orange | 只读冻结 |
| Hidden | red | 强制隐藏 |
| Archived | default | 已归档 |
| Merged | blue | 已合并 |
| Closed | black | 永久关闭 |
| Deleted | default | 已删除 |

---

## 5. API 对接

### 5.1 统计看板 API

**基础路径**: `/api/v1/content/channel/stats`（ChannelStatsController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取核心指标 | GET | `/api/v1/content/channel/stats/core` | 参数：`channelId`, `timeRange`, `startDate`, `endDate` |
| 获取趋势数据 | GET | `/api/v1/content/channel/stats/trend` | 参数：`channelId`, `timeRange`, `startDate`, `endDate` |
| 获取互动数据 | GET | `/api/v1/content/channel/stats/interaction` | 参数：`channelId`, `timeRange`（待实现） |
| 获取热门内容 | GET | `/api/v1/content/channel/stats/hot-content` | 参数：`channelId`, `period`（7d/30d/90d）, `limit` |
| 获取用户分析 | GET | `/api/v1/content/channel/stats/user-analysis` | 参数：`channelId`, `timeRange`, `dimension` |

### 5.2 数据导出 API

**基础路径**: `/api/v1/content/channel/export`（ChannelExportController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发起导出 | POST | `/api/v1/content/channel/export/create` | 请求体：`{ channelId, timeRange, fields[], format }` |
| 查询导出任务状态 | GET | `/api/v1/content/channel/export/status` | 参数：`taskId`，返回：`{ status, progress, downloadUrl }` |
| 导出历史列表 | GET | `/api/v1/content/channel/export/history` | 参数：`channelId`，分页参数（待实现） |
| 下载导出文件 | GET | `/api/v1/content/channel/export/download` | 参数：`taskId`，文件下载 |

### 5.3 审核管理 API

**基础路径**: `/api/v1/content/channel/review`（ChannelReviewController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 审核队列列表 | GET | `/api/v1/content/channel/review/list` | 参数：`channelType`, `applyType`, `status`, `dateRange`, 分页 |
| 审核详情 | GET | `/api/v1/content/channel/review/detail/{id}` | 返回审核申请详情（待实现） |
| 执行审核操作 | POST | `/api/v1/content/channel/review/action` | 请求体：`{ reviewId, action: approve/reject/return, reason }` |

### 5.4 生命周期管理 API

**基础路径**: `/api/v1/content/channel/lifecycle`（ChannelLifecycleController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 冻结频道 | POST | `/api/v1/content/channel/lifecycle/freeze` | 请求体：`{ channelId, reason }` |
| 解冻频道 | POST | `/api/v1/content/channel/lifecycle/unfreeze` | 请求体：`{ channelId, reason }` |
| 限制推荐 | POST | `/api/v1/content/channel/lifecycle/restrict-recommend` | 请求体：`{ channelId, reason }` |
| 强制隐藏 | POST | `/api/v1/content/channel/lifecycle/hide` | 请求体：`{ channelId, reason }` |
| 恢复可见 | POST | `/api/v1/content/channel/lifecycle/restore-visibility` | 请求体：`{ channelId, reason }`（待实现） |
| 永久关闭 | POST | `/api/v1/content/channel/lifecycle/close` | 请求体：`{ channelId, reason }` |
| 归档 | POST | `/api/v1/content/channel/lifecycle/archive` | 请求体：`{ channelId, reason }` |
| 审计日志 | GET | `/api/v1/content/channel/lifecycle/logs` | 参数：`channelId`, 分页 |

**基础路径**: `/api/v1/content/channel/governance`（ChannelGovernanceController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 治理频道列表 | GET | `/api/v1/content/channel/governance/list`（注：当前 Controller 无 list 端点，需确认） | 参数：`keyword`, `channelType`, `status`, 分页 |

**基础路径**: `/api/v1/content/channel/merge`（ChannelMergeController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 合并校验 | POST | `/api/v1/content/channel/merge/validate` | 请求体：`{ sourceChannelId, targetChannelId }` |
| 执行合并 | POST | `/api/v1/content/channel/merge/execute` | 请求体：`{ sourceChannelId, targetChannelId, reason }` |

### 5.5 审计日志 API

**基础路径**: `/api/v1/content/channel/lifecycle`（ChannelLifecycleController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 审计日志列表 | GET | `/api/v1/content/channel/lifecycle/logs` | 参数：`channelId`, `operatorKeyword`, `actionType`, `dateRange`, 分页 |

### 5.6 申诉管理 API

**基础路径**: `/api/v1/content/channel/lifecycle/appeal`（ChannelLifecycleController）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 申诉列表 | GET | `/api/v1/content/channel/lifecycle/appeal/list` | 参数：`channelId`, `status`, `dateRange`, 分页 |
| 申诉详情 | GET | `/api/v1/content/channel/lifecycle/appeal/detail/{id}` | 返回申诉详情（待实现） |
| 处理申诉 | POST | `/api/v1/content/channel/lifecycle/appeal/handle` | 请求体：`{ appealId, result: restore/maintain, description }` |
| 提交申诉 | POST | `/api/v1/content/channel/lifecycle/appeal/submit` | 请求体：`{ channelId, description, attachments }` |

### 5.7 前端数据模型

```typescript
// 核心指标
interface ChannelStatsOverview {
  subscribeCount: number;
  contentCount: number;
  pv: number;
  uv: number;
  updatedAt: string;
}

// 趋势数据
interface ChannelTrendItem {
  date: string;
  subscribeCount: number;
  contentCount: number;
  pv: number;
  uv: number;
}

// 互动数据
interface ChannelInteraction {
  likeCount: number;
  commentCount: number;
  favoriteCount: number;
  shareCount: number;
  effectiveVisitCount: number;
}

// 热门内容
interface HotContentItem {
  rank: number;
  contentId: string;
  title: string;
  contentType: string;
  publishTime: string;
  effectiveInteraction: number;
}

// 用户分析
interface UserAnalysis {
  subscribeTrend: { date: string; increase: number; churn: number }[];
  activeDistribution: { label: string; value: number }[];
  contributionRank: { rank: number; userId: string; nickname: string; value: number }[];
  departmentStats?: { department: string; count: number }[];
}

// 导出任务
interface ExportTask {
  taskId: string;
  channelId: string;
  timeRange: [string, string];
  fields: string[];
  format: 'excel' | 'csv';
  status: 'processing' | 'completed' | 'failed';
  progress?: number;
  downloadUrl?: string;
  failReason?: string;
  createdAt: string;
  expiresAt?: string;
}

// 审核记录
interface ChannelReview {
  reviewId: string;
  channelId: string;
  channelName: string;
  channelType: 'personal' | 'organization' | 'system';
  applyType: 'create' | 'modify';
  applicantId: string;
  applicantName: string;
  status: 'pending' | 'approved' | 'rejected';
  submitTime: string;
  isTimeout: boolean;
  detail: Record<string, any>;
}

// 生命周期日志
interface LifecycleLog {
  logId: string;
  channelId: string;
  channelName: string;
  operatorId: string;
  operatorName: string;
  actionType: string;
  beforeStatus: string;
  afterStatus: string;
  reason: string;
  impactScope: string;
  createdAt: string;
}

// 申诉记录
interface ChannelAppeal {
  appealId: string;
  channelId: string;
  channelName: string;
  punishmentType: string;
  punishmentReason: string;
  appellantId: string;
  appellantName: string;
  description: string;
  attachments: string[];
  status: 'pending' | 'resolved' | 'rejected';
  result?: string;
  resultDescription?: string;
  createdAt: string;
  handledAt?: string;
}
```

### 5.8 API 错误码定义

所有 API 统一返回格式：`{ code: number, result: T, message: string, success: boolean }`。前端根据 `code` 字段判断错误类型并展示对应提示。

**通用错误码**:

| HTTP 状态码 | 业务错误码 | 含义 | 前端处理策略 |
|-------------|-----------|------|-------------|
| 400 | 40001 | 参数校验失败 | Toast 展示具体字段错误信息，保留用户输入 |
| 401 | 40101 | 未登录或 Token 过期 | 跳转登录页 |
| 403 | 40301 | 权限不足 | Toast "权限不足"，隐藏无权限操作按钮 |
| 404 | 40401 | 资源不存在 | Toast "资源不存在或已删除"，返回列表页 |
| 409 | 40901 | 状态冲突（如冻结已冻结频道） | Toast 展示冲突说明，刷新页面状态 |
| 429 | 42901 | 操作频率限制 | Toast "操作过于频繁，请稍后重试"，按钮延迟恢复 |
| 500 | 50001 | 服务端异常 | Toast "服务器异常，请稍后重试"，保留用户输入 |

**生命周期操作专用错误码**:

| 业务错误码 | 场景 | 前端提示 |
|-----------|------|----------|
| 40910 | 冻结已冻结的频道 | "该频道已处于冻结状态" |
| 40911 | 解冻未冻结的频道 | "该频道当前未处于冻结状态" |
| 40912 | 关闭已关闭的频道 | "该频道已永久关闭" |
| 40913 | 对已合并频道发起操作 | "该频道已合并至其他频道，无法操作" |
| 40914 | 对非自己频道发起归档（频道主） | "只能归档自己创建的频道" |
| 40915 | 频道状态不允许当前操作 | "当前频道状态不允许执行此操作" |
| 40310 | 频道主尝试执行运营专属操作 | "该操作仅限平台运营执行" |

**导出专用错误码**:

| 业务错误码 | 场景 | 前端提示 |
|-----------|------|----------|
| 40020 | 导出字段为空 | "请至少选择一个导出字段" |
| 40021 | 时间范围无效 | "请选择有效的时间范围" |
| 40920 | 存在进行中的导出任务 | "已有导出任务正在处理中，请等待完成后再试" |
| 42920 | 导出频率限制 | "导出操作过于频繁，请 5 分钟后再试" |
| 50020 | 导出任务服务端失败 | "导出任务失败，请重试" |
| 50021 | 数据量超限 | "导出数据量超过上限（100,000 行），请缩小时间范围" |

**前端统一错误处理封装**:

```typescript
// utils/errorHandler.ts
function handleApiError(error: ApiError): void {
  switch (error.code) {
    case 40101:
      router.push('/login');
      break;
    case 40910: case 40911: case 40912: case 40913: case 40914: case 40915:
      message.error(error.message);
      // 刷新当前页面数据以同步最新状态
      break;
    case 42901: case 42920:
      message.warning(error.message);
      break;
    case 50021:
      message.error('导出数据量超过上限（100,000 行），请缩小时间范围');
      break;
    default:
      message.error(error.message || '操作失败，请稍后重试');
  }
}
```

---

## 6. 状态管理

### 6.1 Store 设计

```typescript
// stores/channelStats.ts - 频道统计数据
interface ChannelStatsStore {
  overview: ChannelStatsOverview | null;
  trend: ChannelTrendItem[];
  interaction: ChannelInteraction | null;
  hotContent: HotContentItem[];
  userAnalysis: UserAnalysis | null;
  timeRange: string;
  // 每个子模块独立 loading 状态，支持并行加载互不阻塞
  loading: {
    overview: boolean;
    trend: boolean;
    interaction: boolean;
    hotContent: boolean;
    userAnalysis: boolean;
  };
  // 每个子模块独立 error 状态，任一接口失败不阻塞其他模块展示
  errors: {
    overview: string | null;
    trend: string | null;
    interaction: string | null;
    hotContent: string | null;
    userAnalysis: string | null;
  };

  fetchOverview(channelId: string, timeRange: string): Promise<void>;
  fetchTrend(channelId: string, timeRange: string): Promise<void>;
  fetchInteraction(channelId: string, timeRange: string): Promise<void>;
  fetchHotContent(channelId: string, period: string): Promise<void>;
  fetchUserAnalysis(channelId: string, timeRange: string): Promise<void>;
  /** 并行加载所有看板数据，使用 Promise.allSettled 确保任一失败不阻塞其他 */
  fetchAll(channelId: string, timeRange: string): Promise<void>;
  setTimeRange(range: string): void;
  reset(): void;
}

// stores/channelExport.ts - 导出任务
interface ChannelExportStore {
  tasks: ExportTask[];
  loading: boolean;
  submitting: boolean;

  submitExport(channelId: string, config: ExportConfig): Promise<void>;
  fetchHistory(channelId: string): Promise<void>;
  refreshTaskStatus(taskId: string): Promise<void>;
}

// stores/channelReview.ts - 审核管理
interface ChannelReviewStore {
  list: ChannelReview[];
  total: number;
  loading: boolean;
  currentReview: ChannelReview | null;

  fetchList(params: ReviewQueryParams): Promise<void>;
  fetchDetail(reviewId: string): Promise<void>;
  approve(reviewId: string): Promise<void>;
  reject(reviewId: string, reason: string): Promise<void>;
  returnForRevision(reviewId: string, reason: string): Promise<void>;
}

// stores/channelGovernance.ts - 频道治理
interface ChannelGovernanceStore {
  list: GovernanceChannelItem[];
  total: number;
  loading: boolean;
  currentChannel: GovernanceChannelDetail | null;

  fetchList(params: GovernanceQueryParams): Promise<void>;
  fetchDetail(channelId: string): Promise<void>;
  freeze(channelId: string, reason: string): Promise<void>;
  unfreeze(channelId: string, reason: string): Promise<void>;
  hide(channelId: string, reason: string): Promise<void>;
  restrict(channelId: string, reason: string): Promise<void>;
  close(channelId: string, reason: string): Promise<void>;
  archive(channelId: string, reason: string): Promise<void>;
  merge(channelId: string, targetId: string, reason: string): Promise<void>;
}

// 治理频道列表项
interface GovernanceChannelItem {
  channelId: string;
  channelName: string;
  channelType: 'personal' | 'organization' | 'system';
  status: LifecycleStatus;
  subscribeCount: number;
  lastActiveTime: string;
  createdAt: string;
}

// 治理频道详情
interface GovernanceChannelDetail extends GovernanceChannelItem {
  contentCount: number;
  punishments: PunishmentRecord[];
  appeals: ChannelAppeal[];
  auditLogs: LifecycleLog[];
}

// 处罚记录
interface PunishmentRecord {
  punishmentId: string;
  actionType: LifecycleActionType;
  reason: string;
  operatorName: string;
  createdAt: string;
}

// 生命周期状态联合类型
type LifecycleStatus = 'PendingReview' | 'Active' | 'ReadonlyFrozen' | 'Hidden' | 'Archived' | 'Merged' | 'Closed' | 'Deleted';

// 生命周期操作类型联合类型
type LifecycleActionType = 'freeze' | 'unfreeze' | 'hide' | 'restrict' | 'close' | 'archive' | 'merge' | 'restore' | 'delete';
```

### 6.2 跨 Store 协调机制

Store 之间存在以下业务联动关系，采用**组件层 `onSuccess` 回调串联**策略：

| 场景 | 源操作 | 联动目标 | 实现方式 |
|------|--------|----------|----------|
| 审核通过/拒绝 | `channelReview.approve()` / `reject()` | 刷新治理列表 | 页面组件 `onSuccess` 回调中调用 `channelGovernance.fetchList()` |
| 冻结/解冻/隐藏等治理操作 | `channelGovernance.freeze()` 等 | 刷新治理详情页状态和操作按钮 | `LifecycleActionModal` 的 `onSubmit` 成功后回调 `channelGovernance.fetchDetail(channelId)` |
| 申诉处理完成 | 申诉处理 API 成功 | 刷新治理详情页的处罚记录 Tab 和申诉记录 Tab | `AppealModal` 处理成功后回调 `channelGovernance.fetchDetail(channelId)` |

**协调原则**:
- Store 自身不引用其他 Store，保持单一职责
- 跨 Store 联动统一在页面组件层通过 `onSuccess` / `afterHook` 回调实现
- 每个 Store action 返回 `Promise`，便于页面组件串联调用
- 操作成功后的全局刷新逻辑封装为 `useChannelActionSync()` composable，避免各页面重复编写

### 6.3 缓存策略

- 统计数据：页面级缓存，切换时间范围时重新请求
- 导出历史：每次进入页面重新加载
- 审核队列：每次进入页面重新加载，操作后立即刷新
- 治理频道列表：每次进入页面重新加载
- 审计日志：每次进入页面重新加载，支持分页

---

## 7. 组件选型

基于 `frontend-standards.md` 规范，优先复用现有组件：

| 需求场景 | 推荐组件 | 路径 | 说明 |
|----------|----------|------|------|
| 数据表格 | JVxeTable | `src/components/jeecg/JVxeTable/` | 审核队列、治理列表、审计日志、申诉列表、热门内容、贡献排行 |
| 表单配置 | Form | `src/components/Form/` | 导出配置、筛选条件、审核操作表单 |
| 弹窗确认 | Modal | `src/components/Modal/` | 生命周期操作确认、审核操作、导出配置 |
| 详情抽屉 | Drawer | `src/components/Drawer/` | 审核详情、治理详情 |
| 描述信息 | Description | `src/components/Description/` | 频道基本信息展示 |
| 按钮 | Button | `src/components/Button/` | 操作按钮，支持权限控制 |
| 页面容器 | Page | `src/components/Page/` | 所有页面的根容器 |
| 数字动画 | CountTo | `src/components/CountTo/` | 核心指标数字展示 |
| 加载状态 | Loading | `src/components/Loading/` | 数据加载中状态 |
| API 封装 | defHttp | `/@/utils/http/axios` | 所有 API 请求 |
| 表格逻辑 | useTable | `src/hooks/component/` | 表格分页、排序、筛选 |
| 表单逻辑 | useForm | `src/hooks/component/` | 表单校验、提交 |
| 弹窗逻辑 | useModal | `src/hooks/component/` | 弹窗开关、确认 |
| 抽屉逻辑 | useDrawer | `src/hooks/component/` | 抽屉开关 |
| 消息提示 | useMessage | `src/hooks/web/` | Toast 提示 |
| 权限判断 | usePermission | `src/hooks/web/` | 操作权限校验 |

**图表组件**（需引入，已锁定 ECharts）:

| 需求场景 | 推荐方案 | 说明 |
|----------|----------|------|
| 趋势折线图 | ECharts（按需引入） | 核心指标趋势、订阅增量/流失趋势 |
| 饼图/环形图 | ECharts（按需引入） | 成员活跃度占比 |
| 柱状图 | ECharts（按需引入） | 新增内容类型分布 |

**图表库选型决策**: 锁定 ECharts，理由：项目已有 ECharts 生态经验，社区成熟度高，按需引入后打包体积约 200KB。按需引入配置仅包含 `LineChart`、`PieChart`、`BarChart`、`TooltipComponent`、`LegendComponent`、`GridComponent`。封装统一 `useChart` hook，处理 resize、销毁、主题配置，避免图表逻辑散落在各组件中。

---

## 8. 交互设计

### 8.1 核心交互流程

**数据看板查看流程**:
```
频道主进入频道管理后台
  → 点击"数据看板"菜单
  → 页面加载，展示骨架屏
  → 数据返回，展示核心指标卡片（数字动画）+ 更新时间
  → 趋势图、互动数据、热门内容、用户分析依次加载
  → 频道主切换时间范围 → 全页面数据联动刷新
  → 频道主点击"导出数据" → 打开导出配置弹窗
```

**数据导出流程**:
```
频道主点击"导出数据"
  → 打开导出配置 Modal
  → 选择时间范围、字段、格式
  → 展示预计行数
  → 点击"导出"
  → 预计行数 <= 10,000：直接下载文件
  → 预计行数 > 10,000：提交异步任务 → Toast "导出任务已提交，请在导出历史中查看"
  → 频道主查看导出历史列表 → 状态更新为"已完成" → 点击下载
```

**审核处理流程**:
```
平台运营进入审核队列
  → 筛选条件 → 列表展示
  → 点击"查看详情" → 打开 Drawer 展示申请详情
  → 点击"审核" → 弹出审核 Modal
  → 选择通过/拒绝/退回修改
  → 拒绝/退回修改时填写原因（必填）
  → 点击"确认" → API 调用 → 成功 Toast → 列表刷新
```

**高风险操作流程**:
```
平台运营在治理详情页点击操作按钮（如"冻结"）
  → 弹出 LifecycleActionModal
  → Modal 展示：操作名称、影响范围说明、原因输入框
  → 输入原因（必填，>= 10 字符）
  → 点击"确认操作"
  → 按钮 loading + 禁用
  → API 成功 → Modal 关闭 → 页面刷新 → Toast "操作成功"
  → API 失败 → Modal 保持打开 → 展示错误提示
```

**申诉流程**:
```
频道主收到处罚通知
  → 点击"申诉"入口
  → 打开 AppealModal
  → 填写申诉说明 + 上传补充材料
  → 点击"提交申诉"
  → API 成功 → Modal 关闭 → Toast "申诉已提交"
  → 平台运营在申诉管理页面查看并处理
```

### 8.2 反馈规则

| 场景 | 反馈方式 |
|------|----------|
| 数据加载中 | 骨架屏（Skeleton） |
| 操作成功 | 全局 Toast 提示 |
| 操作失败 | 全局 Toast 错误提示 + 保留用户已输入内容 |
| 危险操作确认 | Modal 弹窗 + 原因输入 + 二次确认 |
| 空数据 | 空状态插图 + 引导文案 + 下一步入口 |
| 权限不足 | 提示文案"权限不足" + 隐藏无权限维度 |
| 导出处理中 | 列表行展示进度指示 |
| 导出失败 | 列表行展示失败原因 + 重试按钮 |
| 审核超时 | 红色超时标记 |
| 防重复提交 | 提交按钮 loading + 禁用 |

---

## 9. 响应式设计

### 9.1 桌面端（> 1024px）

- 数据看板：4 列指标卡片 + 全宽趋势图 + 左右分栏（互动数据 + 热门内容）+ 用户分析全宽
- 审核队列/治理列表/审计日志：高密度 Table + 顶部筛选区
- 弹窗：居中 Modal，宽度 520px-640px
- 抽屉：右侧 Drawer，宽度 480px-640px

### 9.2 移动端（< 768px）

**数据看板**:
- 指标卡片：2 列网格，优先展示核心指标
- 趋势图：全宽，高度缩减
- 筛选条件：收纳到顶部折叠面板
- 互动数据/热门内容/用户分析：垂直堆叠，各占全宽
- 导出按钮：固定在底部操作栏

**审核队列/治理列表**:
- Table 转为卡片列表布局
- 每张卡片保留：频道名称、状态 Tag、申请人、操作按钮
- 筛选条件收纳到顶部折叠面板
- 详情使用全屏 Modal 或新页面展示

**审计日志**:
- Table 转为卡片列表
- 每张卡片保留：操作时间、频道名称、操作类型 Tag、操作人

**弹窗**:
- Modal 转为底部弹出式 ActionSheet 或全屏 Modal
- 高风险操作确认弹窗保持居中 Modal（避免误触）

### 9.3 平板端（768px - 1024px）

- 数据看板：3 列指标卡片，其余同桌面端
- 列表页：保持 Table 布局，减少列数，次要信息折叠
- 弹窗：居中 Modal，宽度适配屏幕

---

## 10. 性能要求

### 10.1 响应时间

| 操作 | 目标响应时间 |
|------|-------------|
| 数据看板首屏加载 | < 2s |
| 看板数据切换时间范围 | < 1s |
| 常规看板查询（90 天内） | P95 < 1s |
| 热门内容列表加载 | < 1s |
| 用户分析加载 | < 1s |
| 审核队列列表加载 | < 1s |
| 审核操作（通过/拒绝） | < 500ms |
| 生命周期操作（冻结/解冻等） | < 1s |
| 导出任务提交 | < 500ms |
| 审计日志列表加载 | < 1s |

### 10.2 优化策略

- 数据看板各模块独立请求，并行加载，互不阻塞
- 趋势图和饼图使用懒加载（lazy load），首屏仅加载指标卡片
- 列表页使用分页加载，默认每页 20 条
- 导出按钮防重复提交（loading + 禁用）
- 生命周期操作防重复提交
- 审核队列和治理列表支持下拉刷新
- 图表组件按需引入，避免打包体积过大

### 10.3 数据新鲜度

- 看板核心统计数据可见延迟 P99 <= 5 分钟
- 页面每次进入重新请求最新数据
- 不使用前端缓存统计数据

---

## 11. 测试要点

### 11.1 功能测试

**数据看板**:
- [ ] 核心指标卡片正确展示订阅数、内容数、PV、UV
- [ ] 数据更新时间正确展示
- [ ] 切换时间范围后全页面数据联动刷新
- [ ] 趋势图正确展示多指标折线
- [ ] 趋势图 hover tooltip 展示具体数值
- [ ] 互动数据 5 项指标正确展示
- [ ] 热门内容默认展示近 7 天 Top20
- [ ] 切换热门内容周期后列表刷新
- [ ] 违规/删除内容不进入热门排行
- [ ] 用户分析正确展示订阅增量/流失/活跃度/贡献排行
- [ ] 组织频道按部门统计维度权限控制
- [ ] 无权限用户无法查看频道看板

**数据导出**:
- [ ] 选择字段和时间范围后预计行数正确展示
- [ ] Excel/CSV 格式导出内容完整
- [ ] 导出文件包含字段标题、时间范围、筛选条件、导出时间
- [ ] 大数据量（> 10,000 行）异步导出流程正确
- [ ] 导出历史列表正确展示状态和操作
- [ ] 处理中状态展示进度
- [ ] 已完成状态可下载
- [ ] 失败状态展示原因和重试按钮
- [ ] 无权限用户导出被拒

**审核队列**:
- [ ] 审核列表按筛选条件正确过滤
- [ ] 超过 24 小时未处理的申请展示超时标记
- [ ] 审核详情展示完整信息
- [ ] 审核通过后频道状态变更为 Active
- [ ] 审核拒绝必须填写原因
- [ ] 退回修改必须填写原因
- [ ] 系统频道免审核

**生命周期管理**:
- [ ] 冻结操作后频道进入 ReadonlyFrozen 状态
- [ ] 解冻操作后频道恢复原状态
- [ ] 限制推荐后频道内容不进入公共推荐流
- [ ] 强制隐藏后频道对外不可见
- [ ] 永久关闭需二次确认（输入频道名称）
- [ ] 归档操作后频道从发现入口消失
- [ ] 合并操作展示影响范围预览
- [ ] 操作按钮根据当前状态动态展示
- [ ] 所有高风险操作记录审计日志

**审计日志**:
- [ ] 日志列表按筛选条件正确过滤
- [ ] 日志详情展示完整审计信息
- [ ] 日志保留期不少于 180 天

**申诉管理**:
- [ ] 申诉列表按状态正确过滤
- [ ] 提交申诉流程完整
- [ ] 处理申诉（恢复/维持）流程完整
- [ ] 申诉超时提醒标记

### 11.2 边界测试

- [ ] 无统计数据时看板空状态展示
- [ ] 无热门内容时列表空状态展示
- [ ] 无审核申请时列表空状态展示
- [ ] 无审计日志时列表空状态展示
- [ ] 导出预计行数为 0 时的处理
- [ ] 网络异常时操作失败处理和重试
- [ ] 重复提交操作的防抖处理
- [ ] 导出文件下载过期后的处理
- [ ] 长频道名称的文本截断
- [ ] 大数值指标的格式化展示（如 1,234,567）

### 11.3 权限测试

- [ ] 频道主只能查看自己频道的数据看板
- [ ] 无权限用户无法访问数据看板
- [ ] 导出数据不包含超出权限的字段
- [ ] 平台运营可查看所有频道的治理详情
- [ ] 组织频道部门统计维度的权限控制
- [ ] 各角色的操作按钮权限正确

### 11.4 响应式测试

- [ ] 移动端数据看板布局正确（2 列卡片、全宽图表）
- [ ] 移动端筛选条件收纳到折叠面板
- [ ] 移动端 Table 转为卡片列表
- [ ] 移动端弹窗转为底部弹出或全屏
- [ ] 平板端布局适配
- [ ] 桌面端高密度列表展示

### 11.5 性能测试

- [ ] 数据看板首屏加载时间 < 2s
- [ ] 常规看板查询 P95 < 1s
- [ ] 列表页大数据量（1000+）渲染性能
- [ ] 图表组件懒加载正常
- [ ] 频繁操作防抖处理

---

## 12. 待确认问题 / 默认假设

### 待确认问题

| 问题 | 影响范围 | 处理建议 |
|------|----------|----------|
| 归档频道是否允许频道主申请恢复，恢复后是否重新审核？ | 归档流程、用户体验 | 需产品/运营确认恢复策略和审核要求 |
| 数据导出文件的下载有效期和历史导出记录保留期限是多少？ | 数据安全、存储、用户体验 | 需产品/安全/数据确认保留策略，默认假设 7 天 |
| 频道合并后历史统计是否合并到目标频道，还是仅从合并完成后重新计算？ | 数据看板、运营复盘 | 需产品/数据确认统计口径 |
| 永久关闭是否存在法务或平台管理员特权恢复流程？ | 合规治理、申诉 | 需产品/法务/运营确认例外恢复权限 |
| ~~图表组件选型~~ | ~~打包体积、图表能力~~ | 已确认：锁定 ECharts，详见第 7 节「图表组件」部分 |

### 默认假设

| 假设 | 说明 |
|------|------|
| 统计数据 API 返回格式统一 | 假设所有统计 API 返回 `{ code: 200, result: T, message: string, success: boolean }` |
| 导出文件下载有效期 7 天 | 默认假设，需产品确认 |
| 审核超时阈值 24 小时 | 基于 EPIC-24 PRD 定义 |
| 不活跃频道识别周期 6 个月 | 基于 EPIC-24 PRD 定义 |
| 申诉首次响应 SLA 3 个工作日 | 基于 EPIC-24 PRD 定义 |
| 图表使用 ECharts | 已确认锁定 ECharts，按需引入 LineChart/PieChart/BarChart，详见第 7 节 |
| 数据看板支持日/周/月/自定义时间范围 | 自定义时间范围使用 DatePicker Range |
| 热门内容默认展示近 7 天 Top20 | 基于 spec 定义 |
| 导出单次上限 100,000 行 | 基于 EPIC-24 非功能需求 |
