# EPIC-24 频道数据统计与生命周期管理 -- 前端 PRD 审核报告

> **审核人**: 💻 Amelia (Senior Software Engineer)
> **审核日期**: 2026-06-02
> **审核视角**: 工程实现 -- 可测试性、复杂度、API 契约

## 总体评价

PRD 整体质量较高，页面结构、组件设计、API 契约和数据模型定义较为完整，覆盖了 7 个页面、9 个核心组件、30+ 个 API 接口。TypeScript 类型定义详尽，Store 设计合理，交互流程清晰。但从工程实现角度看，存在若干需要在编码前解决的结构性问题，主要集中在：API 契约缺失错误码定义、状态管理 Store 之间缺少协调机制、图表库选型未锁定导致实现不确定性、以及部分边界场景在前端处理逻辑上不够明确。

## 优点

1. **TypeScript 数据模型定义完整** -- 5.7 节定义了 `ChannelStatsOverview`、`ExportTask`、`ChannelReview`、`LifecycleLog`、`ChannelAppeal` 等完整接口，可直接作为前后端对接契约使用，减少联调歧义。

2. **组件拆分粒度合理** -- `StatsCard`、`StatsTrendChart`、`HotContentTable`、`UserAnalysisPanel`、`LifecycleActionModal` 等组件职责单一，Props 接口清晰，复用性强。

3. **交互流程描述详尽** -- 8.1 节用伪流程图描述了数据看板、导出、审核、高风险操作、申诉 5 条核心交互路径，包含了 loading、成功、失败分支，便于开发和 QA 对照。

4. **操作按钮动态规则表明确** -- 3.6 节的「当前状态 -> 可用操作」矩阵，前端可直接映射为权限/状态判断逻辑，降低了实现歧义。

5. **性能优化策略务实** -- 10.2 节提到的并行加载、图表懒加载、分页、防重复提交等策略都是实际项目中验证过的有效手段。

6. **响应式设计分段明确** -- 9.1-9.3 对桌面端、移动端、平板端的布局差异做了具体描述，移动端 Table 转卡片列表的方案是成熟的响应式模式。

## 问题与建议

### 🔴 高严重度

**1. 图表库选型未锁定，影响实现路径和打包体积**

PRD 7.1 节在图表组件处标注「ECharts / AntV G2」二选一，12.1 节待确认问题也提到需要前端架构确认。这两个库的 API 模式完全不同（ECharts 基于 option 配置，G2 基于声明式语法），封装层、按需引入方式、打包体积差异显著（ECharts 全量 ~800KB，按需引入后 ~200KB；G2 全量 ~1MB）。

**建议**: 编码前必须锁定。如果项目已有 ECharts 依赖则直接复用；否则需评估引入成本。PRD 应在「默认假设」中给出明确默认值而非「二选一」。

**2. Store 之间缺少跨 Store 协调机制**

6.1 节定义了 `channelStats`、`channelExport`、`channelReview`、`channelGovernance` 四个独立 Store，但实际业务中存在跨 Store 依赖：
- 审核通过后需刷新治理列表（`channelReview.approve` 完成后需调用 `channelGovernance.fetchList`）
- 冻结/解冻操作成功后需刷新治理详情页的状态和操作按钮（`channelGovernance.freeze` 完成后需更新 `currentChannel`）
- 申诉处理完成后需刷新治理详情的处罚记录 Tab

PRD 未说明这些跨 Store 联动是通过事件总线、Store 订阅、还是页面组件层协调。如果在组件层处理，治理详情页的逻辑会变得臃肿。

**建议**: 明确跨 Store 协调策略。推荐使用组件层 `onSuccess` 回调串联，或在 Store action 中暴露 `afterHook` 供页面注入。

**3. API 契约缺少错误码和 HTTP 状态码定义**

5.1-5.6 节定义了接口路径和参数，但未定义：
- 错误响应结构（如冻结已冻结频道、关闭已关闭频道时返回什么错误码？）
- 各操作的前置校验失败场景（如频道主对非自己频道发起归档、对已合并频道发起冻结）
- 导出任务失败的具体错误类型（网络超时 vs 数据量超限 vs 服务端异常）

前端需要根据不同的错误码展示不同的提示文案和处理策略（如是否允许重试），缺少错误码定义会导致开发时反复联调。

**建议**: 在 API 对接章节补充错误码枚举表，至少覆盖：参数校验失败（400）、权限不足（403）、状态冲突（409）、频率限制（429）、服务端异常（500）。

### 🟡 中严重度

**4. 数据看板 5 个接口并行加载缺少聚合请求或 loading 状态管理方案**

10.2 节提到「数据看板各模块独立请求，并行加载，互不阻塞」，但 6.1 节的 `ChannelStatsStore` 只有一个 `loading: boolean`。5 个接口并行时：
- 单一 `loading` 无法区分哪些模块已加载、哪些仍在 loading
- 首屏展示时，指标卡片可能已加载但趋势图仍在 loading，需要独立的 skeleton 状态
- 任一接口失败不应阻塞其他模块展示

**建议**: 将 `loading` 改为 `Record<string, boolean>` 或为每个子模块定义独立 loading 状态（如 `overviewLoading`、`trendLoading`）。或者使用 `Promise.allSettled` 并为每个模块维护独立的 error 状态。

**5. `UserAnalysisPanel.departmentStats` 类型定义为 `any[]`**

4.4 节 Props 中 `departmentStats?: any[]` 使用了 `any`，5.7 节数据模型中 `UserAnalysis.departmentStats` 类型为 `{ department: string; count: number }[]`。Props 和数据模型类型不一致，`any` 丢失了类型安全。

**建议**: Props 层直接引用 5.7 节的类型定义，消除 `any`。同时 `channelGovernance.ts` Store 中的 `list: any[]` 和 `currentChannel: any | null` 也应替换为具体类型。

**6. 导出任务轮询机制未定义**

3.3 节提到导出历史列表中「处理中」状态需展示进度指示，5.2 节有查询任务状态接口，但 PRD 未定义：
- 前端轮询间隔（建议 3-5 秒）
- 轮询终止条件（状态变为 completed/failed）
- 用户离开页面时是否取消轮询
- 多个处理中任务时的轮询策略（批量查询 vs 逐个查询）

**建议**: 在交互设计或状态管理章节补充轮询策略定义。推荐：3 秒间隔，`Promise.allSettled` 批量查询所有 processing 状态任务，页面 `onUnmounted` 时取消定时器。

**7. 永久关闭的「输入频道名称确认」缺少校验逻辑细节**

3.6 节提到永久关闭需输入频道名称确认，4.7 节 `LifecycleActionModal` 通过 `requireNameConfirm` 标记，但未说明：
- 输入是否区分大小写
- 是否 trim 空格
- 频道名称含特殊字符时的处理
- 前端是否需要实时校验输入与频道名称是否匹配

**建议**: 明确为「trim 后完全匹配，不区分大小写」，并在 Modal 中实时校验，匹配后才启用确认按钮。

**8. 热门内容排行榜缺少并列排名处理规则**

4.3 节 `HotContentTableProps` 中 `rank: number` 由后端返回，但 PRD 未说明当多个内容的有效互动量相同时排名如何处理（并列跳号 vs 不跳号）。这会影响前端排名展示和后端返回数据的一致性。

**建议**: 补充排名规则定义，推荐使用「并列跳号」（如 1, 2, 2, 4）。

### 🟢 低严重度

**9. `ExportConfig.timeRange` 使用 `[string, string]` 未明确日期格式**

5.7 节 `ExportConfig.timeRange` 定义为 `[string, string]`，未说明日期格式是 ISO 8601（`2026-06-01T00:00:00Z`）还是 `YYYY-MM-DD`。前端 DatePicker 组件通常输出 `YYYY-MM-DD`，但 API 可能期望 ISO 格式。

**建议**: 明确日期格式约定，推荐前端统一使用 `YYYY-MM-DD`，由后端处理时区转换。

**10. StatusTag 颜色映射中 PendingReview 和 Merged 都使用 blue**

4.9 节颜色映射表中 `PendingReview`（blue）和 `Merged`（blue）颜色相同，在治理列表中如果同时出现这两种状态，用户无法通过颜色快速区分。

**建议**: 将 `Merged` 改为 `purple` 或 `cyan` 以区分。

**11. 审计日志的「操作类型」枚举未与 TypeScript 类型关联**

3.7 节审计日志筛选区列出了操作类型选项（冻结/解冻/隐藏/限制推荐/永久关闭/归档/合并/删除），5.7 节 `LifecycleLog.actionType` 定义为 `string`。应定义为联合类型枚举，便于前端渲染 Tag 颜色映射和筛选器选项。

**建议**: 定义 `type LifecycleActionType = 'freeze' | 'unfreeze' | 'hide' | 'restrict' | 'close' | 'archive' | 'merge' | 'delete'`，并在 `LifecycleLog` 和 `LifecycleActionModal` 中统一使用。

**12. 移动端数据看板的导出功能入口描述不够具体**

9.2 节提到「导出按钮：固定在底部操作栏」，但数据看板路由 (`/channel/:id/dashboard`) 和导出路由 (`/channel/:id/export`) 是两个独立页面。移动端固定底部操作栏是点击后跳转到导出页，还是直接触发导出弹窗？

**建议**: 明确移动端导出交互为「点击底部按钮 -> 打开 ExportConfigModal」，与桌面端保持一致，而非跳转独立页面。

**13. `AppealModal` 的附件上传缺少约束定义**

4.8 节 `attachments?: File[]` 未定义：
- 文件类型限制（图片？文档？）
- 单文件大小限制
- 最大附件数量
- 上传方式（同步/异步）

**建议**: 补充附件约束：支持图片和 PDF，单文件 <= 5MB，最多 5 个附件，异步上传。

## 实现建议

### 推荐开发顺序

基于依赖关系和风险优先级：

1. **Phase 1 - 基础设施**（2 天）
   - 确定图表库选型并完成按需引入配置
   - 定义所有 TypeScript 类型（数据模型、Store 接口、API 响应/错误码）
   - 封装统一的生命周期操作 API 调用层（11 个 POST 接口结构相似，可抽象）
   - 实现 `StatusTag` 组件（多处复用）

2. **Phase 2 - 数据看板**（3 天）
   - `StatsCard` + `StatsTrendChart` + `HotContentTable` + `UserAnalysisPanel`
   - Store 层并行请求 + 独立 loading 管理
   - 时间范围联动刷新逻辑
   - 响应式布局适配

3. **Phase 3 - 生命周期管理**（3 天）
   - 审核队列 + `ReviewDetailDrawer`
   - 治理列表 + 治理详情 + `LifecycleActionModal`
   - 状态 -> 操作按钮动态映射逻辑
   - 跨 Store 联动（审核通过 -> 治理刷新）

4. **Phase 4 - 辅助功能**（2 天）
   - 数据导出 + 轮询机制
   - 审计日志
   - 申诉管理

### 架构建议

- **生命周期操作 API 抽象**: 5.4 节的 11 个生命周期操作接口（freeze/unfreeze/hide/restrict/close/archive/merge/restore）请求体结构高度相似（都是 `{ reason }` 或 `{ targetChannelId, reason }`），建议封装为通用函数 `executeLifecycleAction(channelId, actionType, payload)` 而非 11 个独立方法。
- **Store 中 `channelGovernance.list` 和 `currentChannel` 不应使用 `any`**: 这会导致治理详情页中所有字段访问都失去类型提示，增加运行时错误风险。
- **图表封装层**: 无论选 ECharts 还是 G2，建议封装一层 `useChart` hook，统一处理 resize、销毁、主题配置，避免图表逻辑散落在各组件中。

## 总结

PRD 覆盖全面，TypeScript 类型定义和组件拆分质量较高，可作为开发基线。需要在编码前优先解决 3 个高严重度问题：图表库选型锁定、跨 Store 协调机制定义、API 错误码契约补充。中低严重度问题建议在各 Phase 开发时同步处理。整体预估前端开发工时约 10 个工作日（单人），建议 2 人并行可压缩至 6 个工作日。
