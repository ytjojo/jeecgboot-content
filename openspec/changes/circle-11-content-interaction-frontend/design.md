## Context

circle-core MVP 已实现圈子基础功能（圈子列表、内容列表、成员管理、加入流程），但缺乏内容质量管控、成员互动和违规治理能力。本变更在已有页面和组件基础上，新增置顶/精华、公告、@成员、申请审核、举报处理五大功能模块。

前端技术栈：Vue 3 + Vben Admin + Ant Design Vue，使用 `defHttp` 封装 API，`useTable` 管理列表状态，Pinia 管理全局状态。

## Goals / Non-Goals

**Goals:**
- 在圈子内容列表页和详情页增加置顶排序和精华标识
- 实现圈子公告的发布、展示、编辑和删除
- 实现圈子内 @成员选择和提及标记解析
- 实现加入申请的审核流程（批准/拒绝/超时提醒/批量操作）
- 实现内容举报的提交和处理流程（删除/忽略/禁言）

**Non-Goals:**
- 不涉及数据统计与分析面板
- 不涉及推荐算法
- 不涉及成长激励体系
- 不涉及 AI 自动内容审核
- 不涉及平台管理员操作（后续 PRD 定义）

## Decisions

### D0: 路由方案

新增页面使用 BACK 权限模式注册路由，路径与已有圈子管理路由保持一致风格：

| 页面 | 路由路径 | 权限 |
|------|---------|------|
| 加入申请审核页 | `/circle/:circleId/join-requests` | BACK（管理员） |
| 举报处理页 | `/circle/:circleId/reports` | BACK（管理员） |

公告发布弹窗、@成员浮层、举报提交弹窗为组件级交互，不涉及独立路由。

### D1: 置顶/精华为独立 API 而非批量更新

选择为每个内容提供独立的 PUT 切换接口（`PUT /circle-content/{contentId}/pin?circleId=xxx`、`PUT /circle-content/{contentId}/featured?circleId=xxx`），而非批量更新接口。两个接口均需要 `circleId` 查询参数（`@RequestParam circleId`），前端调用时必须传递。

理由：置顶/精华操作频率低，单条操作更直观，且切换语义清晰（toggle）。批量接口增加复杂度但收益有限。

### D1.5: Tinymce 富文本编辑器集成方案

公告发布弹窗使用 Tinymce 富文本编辑器。集成方式：
- 使用 `defineAsyncComponent` + dynamic import 按需加载 Tinymce 组件
- 编辑器配置：精简工具栏（加粗/斜体/列表/链接/图片），高度 300px
- 弹窗打开时异步加载编辑器，展示 loading 骨架屏
- 弹窗关闭时销毁编辑器实例，释放内存

### D2: 公告采用"单条生效"模型

每个圈子同时只有一条生效公告，新公告自动替换旧公告。

理由：简化前端展示逻辑（无需管理多条公告的优先级和轮播），符合大多数社区产品的公告模式。如需历史公告，可由后端记录。

### D3: @提及标记使用自定义格式

纯文本场景使用 `@{userId:xxx}昵称` 格式，富文本场景使用 `<span class="mention" data-user-id="xxx">@昵称</span>` 格式。

理由：自定义格式可精确控制解析和渲染，避免与普通文本冲突。后端统一存储这两种格式，前端负责解析为可点击链接。

### D4: 审核列表使用 Table + useTable 而非 JVxeTable

加入申请和举报列表使用标准 Table 组件配合 useTable hook。

理由：列表结构简单，无需内联编辑等高级功能，Table + useTable 更轻量且满足需求。

### D5: 举报处理页使用 Drawer 展示被举报内容详情

被举报内容详情以侧边抽屉形式展示，而非跳转到独立页面。

理由：管理员需要在处理举报的同时保持列表上下文，Drawer 支持快速查看和返回。Modal 空间有限，不适合展示富文本内容。

### D6: 新增独立 Store 管理互动状态

创建 `useCircleInteractionStore` 管理公告状态、@成员列表和审核统计。

理由：这些状态跨页面共享（如公告在内容列表页展示，审核统计在管理入口角标展示），提升到全局 Store 避免重复请求。内容列表的置顶/精华状态通过 API 实时查询，不缓存到 Store。

## Risks / Trade-offs

- **[风险] circle-core 前端页面未完成** → 本 Epic 依赖 circle-core 已有页面（内容列表、圈子详情）。若 circle-core 前端未就绪，需先完成基础页面。缓解：并行开发，本 Epic 的组件设计为可插拔模式。
- **[风险] 通知系统前端未定义** → @成员通知和审核结果通知需要前端展示入口。缓解：本 Epic 仅负责触发 API 调用，通知展示由通知系统模块负责。
- **[风险] Tinymce 富文本编辑器加载性能** → 公告发布使用 Tinymce，可能影响弹窗打开速度。缓解：使用 dynamic import 按需加载。
- **[风险] @成员查询无独立接口** → 后端无 `mentionable-members` Controller 端点，前端复用 `GET /content/circle/member/list` 获取成员列表。缓解：接口已存在，功能可用，后续可按需添加独立端点。
- **[风险] 禁言时长参数不对齐** → `POST /circle-report/{reportId}/mute` 接口不接受禁言时长参数。后端 `CircleMemberUpdateReq` 支持 `muteDuration`（1h/24h/7d/PERMANENT），但举报禁言接口未透传。降级方案：前端禁言弹窗展示时长选择 UI，但提交时不传 duration 参数（后端忽略），待后端对齐后再对接。
