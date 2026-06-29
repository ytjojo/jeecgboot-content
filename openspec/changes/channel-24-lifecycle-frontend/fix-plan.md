# channel-24-lifecycle-frontend Fix Plan

**生成时间**: 2026-06-30
**基于报告**: drift-report-20260627-084036.md, review-report-20260627-084036.md, verify-report-20260627-084036.md, backend-issues.md, verification-review.md

---

## FixItem 列表

### FE-001 - Store命名冲突：channelReview与channel-22-content-governance-frontend重名
**来源**: drift-report CRITICAL D-01/ARCH-STORE-01, review-report BLOCK-01/B-01, verify-report CRITICAL C-01
**位置**: store/modules/channelReview.ts, 所有引用该Store的组件
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端（重构）
**修复步骤**:
1. 将channel-24的审核Store重命名：
   - 文件名：store/modules/channelReview.ts → store/modules/channelLifecycleReview.ts
   - Store函数名：useChannelReviewStore → useChannelLifecycleReviewStore
   - Store id：`app-channel-review` → `app-channel-lifecycle-review`
2. 更新所有引用该Store的文件中的import和使用：
   - views/content/channel/review/ 下所有组件
   - 治理详情页（views/content/channel/governance/detail/）
   - 其他任何引入useChannelReviewStore的文件
3. 注意区分：
   - channel-22的useChannelReviewStore：用于内容级审核（待审内容审核）
   - channel-24的useChannelLifecycleReviewStore：用于频道创建/修改审核
4. 后续新Store命名建议添加更具体的业务前缀，避免命名空间冲突
**验证方式**:
- Pinia初始化无Store id冲突错误
- 审核队列页面功能正常
- 两个不同的Review Store互不干扰
- 编译无错误，所有import路径更新正确
- 单元测试更新后通过
**状态**: pending

---

### FE-002 - 目录双轨制：views/channel/与views/content/channel/两个并行根目录
**来源**: drift-report CRITICAL D-02/ARCH-COMP-01, review-report BLOCK-02/B-02, verify-report CRITICAL C-02/C-04
**位置**: views/ 目录整体
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端（重构）
**修复步骤**:
1. 立即停止新增文件到views/channel/目录，禁止双轨制继续扩大
2. 制定统一目录计划：
   - 统一根目录：以API目录api/content/channel/为基准，统一使用views/content/channel/
   - views/content/channel/作为唯一的channel相关页面根目录
3. 迁移现有文件（建议作为单独的重构任务，在所有channel功能合并后统一进行）：
   - 将views/channel/components/下所有组件迁移到views/content/channel/components/
   - 将views/channel/governance/等页面迁移到views/content/channel/governance/
   - 更新所有import路径
   - 检查并更新路由配置
4. 本change（channel-24）的文件已正确放在views/content/channel/下，无需移动
5. 在文档中明确记录目录规范，后续所有channel相关change必须使用views/content/channel/
**验证方式**:
- 本change不新增文件到旧目录
- 制定明确的目录统一时间表和责任人
- 迁移后所有import路径正确
- 编译无错误，路由正常工作
- 新人可快速找到channel相关代码
**状态**: pending

---

### FE-003 - 后端API路径重构后前端API调用路径需同步更新
**来源**: 后端BE-004（API路径重构）, backend-issues.md, verification-review.md
**位置**: src/api/content/channel/ 所有API文件
**优先级**: BLOCK
**依赖**: BE-004（后端API路径重构完成）
**类型**: 代码修复-前端（API同步）
**修复步骤**:
1. 待后端完成API路径重构（BE-004）后，同步更新所有前端API调用路径：
   - 旧路径：`/api/v1/content/channel/stats/*` → 新路径：`/api/v1/content/channels/{channelId}/stats/*`
   - 旧路径：`/api/v1/content/channel/export/*` → 新路径：`/api/v1/content/channels/{channelId}/exports/*`
   - 旧路径：`/api/v1/content/channel/review/*` → 新路径：`/api/v1/content/channels/reviews/*`（运营视角）或 `/channels/{channelId}/reviews/*`
   - 旧路径：`/api/v1/content/channel/lifecycle/*` → 新路径：`/api/v1/content/channels/{channelId}/*`（freeze/unfreeze/hide等直接作为子路径）
   - 旧路径：`/api/v1/content/channel/merge/*` → 新路径：`/api/v1/content/channels/merge/*`
2. 所有路径参数从query参数改为path variable传递：
   - channelId从params放入URL路径中
   - taskId从params放入URL路径中（如`/exports/{taskId}`）
   - reviewId从params放入URL路径中（如`/reviews/{reviewId}`）
   - appealId从params放入URL路径中（如`/appeals/{appealId}`）
3. 补充6个缺失的API接口（backend-issues.md中的P0接口）：
   - getInteractionStats：获取互动数据
   - getExportHistory：获取导出历史列表
   - getReviewDetail：获取审核详情
   - restoreVisibility：恢复可见
   - getLifecycleLogsByChannel：按频道查询审计日志（扩展现有接口）
   - getAppealDetail：获取申诉详情
4. 注意：前端API封装不需要写`/api/v1/`前缀（由baseURL统一处理），实际路径如`/content/channels/{channelId}/...`
5. 检查并更新HTTP方法：查询接口用GET，写操作用POST
**验证方式**:
- 所有API路径使用复数资源风格，与后端重构后一致
- channelId/taskId/reviewId/appealId正确传递到URL路径中
- 6个缺失API补充完整
- 联调时无404错误
- 所有组件单元测试更新后通过
**状态**: pending

---

### FE-004 - API文件需按领域拆分，确认lifecycle.ts存在
**来源**: drift-report WARNING D-03/ARCH-API-01, verify-report WARNING W-01
**位置**: src/api/content/channel/ 目录
**优先级**: P1
**依赖**: FE-003（API路径更新）
**类型**: 代码修复-前端（API组织）
**修复步骤**:
1. 确认api/content/channel/目录下文件按领域拆分（参考其他channel模块）：
   - stats.ts：统计看板API（已存在）
   - export.ts：数据导出API（已存在）
   - lifecycleReview.ts：频道创建/修改审核API（原review相关，重命名避免与内容审核冲突）
   - lifecycle.ts：生命周期操作API（冻结/解冻/隐藏/归档/关闭/合并等，需确认存在或创建）
   - auditLog.ts：审计日志API（已存在）
   - appeal.ts：申诉管理API（已存在）
   - governance.ts：治理后台列表/详情API（需确认）
   - merge.ts：合并相关API（可选，可放在lifecycle.ts中）
2. 每个文件独立定义Api enum和导出API方法，保持一个领域一个文件
3. 每个文件不超过300行，职责清晰
4. 创建index.ts统一导出（可选）
5. 参考EPIC-22/23的api/content/channel/目录结构保持一致
**验证方式**:
- 所有lifecycle相关API在独立的lifecycle.ts中
- 单文件不超过300行，职责清晰
- 所有import路径更新正确
- 编译无错误
- 功能与拆分前一致
- 单元测试更新后通过
**状态**: pending

---

### FE-005 - 导出轮询缺少超时机制，可能无限轮询
**来源**: drift-report WARNING D-04, review-report FLAG-04/F-04, verify-report WARNING W-03
**位置**: 数据导出页面（views/content/channel/export/index.vue）或channelExport Store
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-前端（体验优化）
**修复步骤**:
1. 在导出轮询逻辑中添加超时机制：
   - 最大轮询时长：90秒（或最多30次，3秒间隔）
   - 超时后停止轮询
   - 提示用户："导出任务处理中，请稍后在导出历史中查看"
2. 轮询策略优化（与PRD一致）：
   - 页面进入时启动3秒间隔轮询
   - 使用批量查询接口（fetchHistory）一次获取所有processing状态任务，而非逐个查询
   - 所有processing任务变为completed/failed后自动停止轮询
   - 页面onUnmounted时清除轮询定时器，避免无效请求和内存泄漏
3. 添加轮询状态指示：
   - 处理中：展示进度指示器
   - 超时：提示用户稍后查看
   - 失败：展示失败原因和重试按钮
4. 测试场景：
   - 任务快速完成：轮询自动停止
   - 任务长时间处理：超时提示
   - 页面切换/关闭：定时器清理
**验证方式**:
- 轮询最多90秒后自动停止
- 页面离开时轮询停止
- 超时提示清晰友好
- 批量查询减少请求次数
- 无内存泄漏警告
**状态**: pending

---

### FE-006 - 复用现有useECharts hook，避免重复封装
**来源**: drift-report WARNING D-05, review-report ADVISORY-01/A-01
**位置**: 图表相关组件（StatsTrendChart等）
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-前端（复用优化）
**修复步骤**:
1. 检查项目现有hooks/web/useECharts.ts的功能：
   - 是否支持resize自动处理
   - 是否支持主题配置
   - 是否支持组件销毁时自动清理
   - 是否支持按需引入ECharts组件
2. 如果现有useECharts满足需求，直接复用，删除重复封装的useChart hook
3. 如果现有hook缺少必要功能（如按需引入配置），扩展现有useECharts而非重新写一个
4. 图表组件（StatsTrendChart等）使用统一的useECharts hook
5. 确保ECharts按需引入：
   - 只引入LineChart、PieChart、BarChart
   - 只引入TooltipComponent、LegendComponent、GridComponent
   - 打包体积控制在合理范围（约200KB）
**验证方式**:
- 无重复的chart hook
- 图表正常渲染，resize正常
- 打包体积无异常增长
- 首屏加载性能达标（<2s）
**状态**: pending

---

### FE-007 - Store异步action缺少错误处理和loading状态
**来源**: drift-report WARNING ARCH-STORE-02, verify-report WARNING W-06
**位置**: src/store/modules/ 所有channel相关Store
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-前端（Store健壮性）
**修复步骤**:
1. 参考channel-23 discoveryStore的错误处理模式，为所有Store异步action添加：
   - try/catch错误捕获
   - catch中调用message.error展示友好错误提示
   - 错误发生时重置loading状态
   - 不要静默失败
2. 为每个异步操作添加独立的loading状态：
   - 列表加载loading
   - 提交操作submitting（按钮禁用防重复）
   - 避免全Store共用一个loading导致多个按钮同时loading
3. 为所有Store添加useXxxStoreWithOut函数（与项目其他Store保持一致）
4. 检查Store风格：
   - 建议统一使用Options API风格，与现有channel-20/22保持一致
   - 如果使用Composition API，保持整个模块风格统一
5. 错误处理覆盖场景：
   - 网络错误
   - 权限不足（403）
   - 状态冲突（409）
   - 参数错误（400）
   - 服务器错误（500）
6. 防重复提交：
   - 所有写操作（冻结/解冻/审核/导出等）按钮点击后设置submitting=true并禁用
   - 请求完成（成功/失败）后重置submitting
**验证方式**:
- 所有异步action有try/catch
- 网络错误时用户能看到友好提示而非静默失败
- 按钮在请求期间禁用，防止重复提交
- 所有Store有WithOut函数
- Store风格统一
- 单元测试覆盖错误场景
**状态**: pending

---

### FE-008 - 合并操作影响范围预览字段不明确
**来源**: review-report FLAG-05/F-05
**位置**: LifecycleActionModal组件、合并相关页面
**优先级**: P2
**依赖**: BE-008（后端合并校验/预览API）
**类型**: 文档+代码修复-前端
**修复步骤**:
1. 在spec或组件Props中明确合并预览必须展示的字段：
   - 源频道名称、类型、订阅数、内容数
   - 目标频道名称、类型、订阅数、内容数
   - 合并后预计影响：
     - 内容迁移数量
     - 订阅者迁移数量（去重后）
     - 历史链接处理方式
     - 源频道状态将变为Merged
     - 合并后不可撤销的提示
2. 合并校验API（/merge/validate）返回预览数据
3. 合并确认Modal展示以上所有信息，让用户充分知晓影响
4. 组织频道合并提示需要组织最高管理员审批
**验证方式**:
- 合并申请前展示完整影响范围
- 用户可清晰看到合并后果
- 预览数据与后端返回一致
**状态**: pending

---

### FE-009 - 高风险操作建议都添加操作原因输入框
**来源**: review-report ADVISORY-02/A-02
**位置**: LifecycleActionModal组件
**优先级**: P2
**依赖**: 无
**类型**: 体验优化-前端
**修复步骤**:
1. 目前永久关闭有原因输入，其他高风险操作建议统一添加：
   - 冻结/解冻：原因必填
   - 强制隐藏/恢复可见：原因必填
   - 限制推荐：原因必填
   - 归档：原因必填
   - 合并：原因必填
   - 永久关闭：原因必填+输入频道名称确认
2. 原因输入框校验：最少10个字符
3. 原因会传递给后端记录审计日志
**验证方式**:
- 所有高风险操作都有原因输入框
- 原因为必填且有长度校验
- 原因正确传递给API
**状态**: pending

---

### FE-010 - 检查API函数参数是否正确接收channelId
**来源**: verify-report WARNING W-05
**位置**: src/api/content/channel/ 所有API文件，特别是stats.ts
**优先级**: P1
**依赖**: FE-003（API路径更新）
**类型**: 代码修复-前端（参数修正）
**修复步骤**:
1. 检查所有统计API函数（getCoreStats、getTrend、getInteraction、getHotContent、getUserAnalysis）是否接收channelId作为第一个参数
2. 检查其他API函数是否正确接收路径参数：
   - export相关：channelId、taskId
   - review相关：reviewId
   - lifecycle相关：channelId
   - appeal相关：appealId
3. 确保参数正确传递到URL路径或query中（根据FE-003的重构结果）
4. TypeScript类型定义中channelId等参数为必填
**验证方式**:
- 所有需要channelId的API函数都接收该参数
- 编译无TypeScript错误
- 调用时不遗漏必填参数
**状态**: pending

---

## 修复优先级总览

| 优先级 | 数量 | 项 |
|--------|------|-----|
| BLOCK | 3 | FE-001, FE-002, FE-003 |
| P1 | 5 | FE-004, FE-005, FE-006, FE-007, FE-010 |
| P2 | 2 | FE-008, FE-009 |
| **总计** | **10** | |
