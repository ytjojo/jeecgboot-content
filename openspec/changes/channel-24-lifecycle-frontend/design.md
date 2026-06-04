## Context

基于 JeecgBoot Vue3 前端项目，实现频道数据统计与生命周期管理功能。项目已有一套成熟的组件库（JVxeTable、Form、Modal、Drawer 等）和 hooks（useTable、useForm、useModal 等），需要在此基础上新增频道运营相关页面和组件。

**当前状态**:
- 项目使用 Vue 3 + TypeScript + Vite
- 已有组件库：JVxeTable、Form、Modal、Drawer、Description、Button、Page、CountTo、Loading
- 已有 hooks：useTable、useForm、useModal、useDrawer、useMessage、usePermission
- 已有 HTTP 封装：defHttp（基于 axios）
- 图表组件：需引入 ECharts

**约束条件**:
- 遵循项目现有代码规范和目录结构
- 优先复用现有组件，避免重复造轮子
- 响应式设计支持桌面端、平板端、移动端

## Goals / Non-Goals

**Goals:**
- 实现频道数据统计看板，支持多维度数据展示和时间范围筛选
- 实现数据导出功能，支持 Excel/CSV 格式和异步处理
- 实现频道审核队列，支持创建/修改审核处理
- 实现频道治理后台，支持生命周期状态管理和高风险操作
- 实现审计日志和申诉管理功能
- 保证首屏加载时间 < 2s，常规查询 P95 < 1s

**Non-Goals:**
- 不实现底层数据采集方案
- 不实现审核系统和通知系统基础设施
- 不实现频道创建基础流程（EPIC-20）
- 不实现成员和加入规则（EPIC-21）
- 不实现内容发布与内容级审核（EPIC-22）
- 不实现推荐发现与搜索排序（EPIC-23）

## Decisions

### 1. 图表组件选型：ECharts

**决策**: 使用 ECharts 作为图表组件库

**理由**:
- 项目已有 ECharts 生态经验
- 社区成熟度高，文档完善
- 按需引入后打包体积约 200KB，可接受
- 支持折线图、饼图、柱状图等所有需要的图表类型

**替代方案**:
- Chart.js：轻量但功能相对较少，定制性不如 ECharts
- AntV G2：功能强大但学习成本较高，社区生态不如 ECharts

**实现方案**:
- 按需引入：LineChart、PieChart、BarChart、TooltipComponent、LegendComponent、GridComponent
- 封装统一 `useChart` hook，处理 resize、销毁、主题配置

### 2. 状态管理：Pinia Store 分模块设计

**决策**: 按业务模块拆分 Store，每个模块独立管理状态

**理由**:
- 职责清晰，便于维护和测试
- 避免单一 Store 过于臃肿
- 支持并行加载，互不阻塞

**Store 模块划分**:
- `channelStats`: 频道统计数据（overview、trend、interaction、hotContent、userAnalysis）
- `channelExport`: 导出任务管理
- `channelReview`: 审核队列管理
- `channelGovernance`: 频道治理管理

**跨 Store 协调**:
- Store 自身不引用其他 Store，保持单一职责
- 跨 Store 联动在页面组件层通过 `onSuccess` 回调实现
- 封装 `useChannelActionSync()` composable 避免重复编写

### 3. 数据加载策略：并行加载 + 独立 loading

**决策**: 数据看板各模块独立请求，并行加载，每个模块独立 loading 状态

**理由**:
- 提升用户体验，避免单个接口失败阻塞整个页面
- 支持渐进式加载，核心指标优先展示
- 便于错误处理和重试

**实现方案**:
- 使用 `Promise.allSettled` 并行请求所有看板数据
- 每个模块独立 loading 和 error 状态
- 首屏仅加载指标卡片，图表和列表懒加载

### 4. 导出任务轮询策略

**决策**: 页面进入时启动 3 秒间隔轮询，批量查询所有 processing 状态任务

**理由**:
- 避免逐个查询，减少请求次数
- 所有任务完成后停止轮询，节省资源
- 页面离开时取消轮询，避免无效请求

**实现方案**:
- 轮询间隔：3 秒
- 轮询目标：批量查询导出历史列表中所有 processing 状态任务
- 终止条件：所有 processing 状态任务变为 completed 或 failed
- 页面离开时：onUnmounted 清除轮询定时器

### 5. 响应式设计策略

**决策**: 移动端 Table 转为卡片列表，弹窗转为底部弹出或全屏

**理由**:
- 移动端屏幕空间有限，Table 不适合展示
- 卡片列表更符合移动端交互习惯
- 高风险操作确认弹窗保持居中 Modal，避免误触

**断点定义**:
- 桌面端：> 1024px
- 平板端：768px - 1024px
- 移动端：< 768px

### 6. 错误处理策略

**决策**: 统一错误处理封装，按错误码分类处理

**理由**:
- 避免各页面重复编写错误处理逻辑
- 保证错误提示一致性
- 便于维护和扩展

**错误码分类**:
- 通用错误码：400、401、403、404、409、429、500
- 生命周期操作专用错误码：40910-40915、40310
- 导出专用错误码：40020-40021、40920、42920、50020-50021

**处理策略**:
- 401：跳转登录页
- 409xxx：展示冲突说明，刷新页面状态
- 429xxx：展示频率限制提示，按钮延迟恢复
- 其他：展示错误提示，保留用户输入

## Risks / Trade-offs

### 风险 1: ECharts 打包体积

**风险**: ECharts 按需引入后仍有约 200KB，可能影响首屏加载时间

**缓解措施**:
- 图表组件懒加载，首屏仅加载指标卡片
- 使用动态 import 按需加载图表组件
- 生产环境开启 gzip 压缩

### 风险 2: 数据看板性能

**风险**: 数据看板需要并行请求多个接口，可能导致接口并发过高

**缓解措施**:
- 后端接口做好缓存和性能优化
- 前端使用 Promise.allSettled 并行请求，单个失败不影响其他
- 设置合理的请求超时时间

### 风险 3: 导出任务轮询资源消耗

**风险**: 导出任务轮询可能产生大量无效请求

**缓解措施**:
- 批量查询而非逐个查询
- 所有任务完成后停止轮询
- 页面离开时取消轮询
- 设置合理的轮询间隔（3 秒）

### 风险 4: 移动端适配工作量

**风险**: 移动端需要将 Table 转为卡片列表，工作量较大

**缓解措施**:
- 封装通用的响应式列表组件，支持 Table/卡片列表切换
- 优先实现核心页面的移动端适配
- 使用 CSS 媒体查询实现断点切换

### 风险 5: 高风险操作误操作

**风险**: 冻结、永久关闭等高风险操作可能被误触发

**缓解措施**:
- 所有高风险操作都需要二次确认
- 永久关闭需要输入频道名称确认
- 原因输入框必填，最少 10 个字符
- 操作按钮使用危险样式，视觉上区分

## Migration Plan

**部署步骤**:
1. 前端代码合并到 springboot3_content 分支
2. 安装 ECharts 依赖
3. 配置前端路由
4. 构建并部署前端资源
5. 验证各页面功能正常

**回滚策略**:
- 保留旧版本前端资源
- 如有问题，快速回滚到旧版本
- 数据库无变更，无需数据迁移

## Open Questions

| 问题 | 影响范围 | 处理建议 |
|------|----------|----------|
| 归档频道是否允许频道主申请恢复，恢复后是否重新审核？ | 归档流程、用户体验 | 需产品/运营确认恢复策略和审核要求 |
| 数据导出文件的下载有效期和历史导出记录保留期限是多少？ | 数据安全、存储、用户体验 | 默认假设 7 天，需产品确认 |
| 频道合并后历史统计是否合并到目标频道？ | 数据看板、运营复盘 | 需产品/数据确认统计口径 |
| 永久关闭是否存在法务或平台管理员特权恢复流程？ | 合规治理、申诉 | 需产品/法务/运营确认例外恢复权限 |

## API 实现状态

> **注意**: 以下 API 接口状态基于 2026-06-04 的代码库验证。

### 统计看板 API (ChannelStatsController)

**基础路径**: `/jeecg-boot/api/v1/content/channel/stats`

| API | 功能 | 状态 | 备注 |
|-----|------|------|------|
| `GET /core` | 核心指标 | ✅ 已实现 | - |
| `GET /trend` | 趋势数据 | ✅ 已实现 | - |
| `GET /hot-content` | 热门内容 | ✅ 已实现 | - |
| `GET /user-analysis` | 用户分析 | ✅ 已实现 | - |
| `GET /interaction` | 互动数据 | ❌ **待实现** | 点赞、评论、收藏、分享、有效访问 |

### 数据导出 API (ChannelExportController)

**基础路径**: `/jeecg-boot/api/v1/content/channel/export`

| API | 功能 | 状态 | 备注 |
|-----|------|------|------|
| `POST /create` | 创建导出任务 | ✅ 已实现 | - |
| `GET /status` | 查询任务状态 | ✅ 已实现 | - |
| `GET /download` | 下载文件 | ✅ 已实现 | - |
| `GET /history` | 导出历史列表 | ❌ **待实现** | 返回历史导出记录 |

### 审核管理 API (ChannelReviewController)

**基础路径**: `/jeecg-boot/api/v1/content/channel/review`

| API | 功能 | 状态 | 备注 |
|-----|------|------|------|
| `GET /list` | 审核列表 | ✅ 已实现 | - |
| `POST /action` | 审核操作 | ✅ 已实现 | 通过/拒绝/退回 |
| `GET /detail/{id}` | 审核详情 | ❌ **待实现** | 返回审核申请详情 |

### 生命周期管理 API (ChannelLifecycleController)

**基础路径**: `/jeecg-boot/api/v1/content/channel/lifecycle`

| API | 功能 | 状态 | 备注 |
|-----|------|------|------|
| `POST /freeze` | 冻结 | ✅ 已实现 | - |
| `POST /unfreeze` | 解冻 | ✅ 已实现 | - |
| `POST /hide` | 强制隐藏 | ✅ 已实现 | - |
| `POST /close` | 永久关闭 | ✅ 已实现 | - |
| `POST /archive` | 归档 | ✅ 已实现 | - |
| `POST /restrict-recommend` | 限制推荐 | ✅ 已实现 | - |
| `POST /restore-visibility` | 恢复可见 | ❌ **待实现** | Hidden → Active |
| `GET /logs` | 审计日志 | ✅ 已实现 | - |

### 申诉管理 API (ChannelLifecycleController)

**基础路径**: `/jeecg-boot/api/v1/content/channel/lifecycle/appeal`

| API | 功能 | 状态 | 备注 |
|-----|------|------|------|
| `POST /submit` | 提交申诉 | ✅ 已实现 | - |
| `POST /handle` | 处理申诉 | ✅ 已实现 | - |
| `GET /list` | 申诉列表 | ✅ 已实现 | - |
| `GET /detail/{id}` | 申诉详情 | ❌ **待实现** | 返回申诉详情 |

### 待实现 API 汇总

| # | API | 所属模块 | 优先级 |
|---|-----|----------|--------|
| 1 | `GET /stats/interaction` | 统计看板 | P0 |
| 2 | `GET /export/history` | 数据导出 | P0 |
| 3 | `GET /review/detail/{id}` | 审核管理 | P0 |
| 4 | `POST /lifecycle/restore-visibility` | 生命周期 | P0 |
| 5 | `GET /lifecycle/logs?channelId=xxx` | 审计日志 | P0 |
| 6 | `GET /appeal/detail/{id}` | 申诉管理 | P0 |
