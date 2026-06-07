## Context

JeecgBoot_sass 项目前端基于 Vue 3 + Vben Admin 框架，使用 Ant Design Vue 4 组件库。项目已有成熟的表单（Form）、表格（JVxeTable）、弹窗（Modal/Drawer）、上传（Upload）等基础组件，以及 useTable、useModal、useDrawer、useMessage 等 hooks。

EPIC-20 后端 API 已定义 15 个频道相关接口，前端需要从零构建频道管理的全部页面和交互。本次变更纯新增，不涉及已有页面改造。

## Goals / Non-Goals

**Goals:**

- 实现用户端频道创建、列表、管理（编辑/转让/删除）完整页面
- 实现后台端频道管理、审核队列页面
- 复用项目现有组件体系，保持风格一致
- 支持桌面端和移动端响应式适配
- API 封装遵循项目 defHttp 规范

**Non-Goals:**

- 频道隐私与加入规则（EPIC-21）
- 频道内容发布与治理（EPIC-22）
- 频道推荐与发现（EPIC-23）
- 频道数据统计与生命周期管理（EPIC-24）
- 付费订阅能力

## Decisions

### 1. 路由与页面结构

**决策**: 用户端和后台端分别注册路由，用户端 3 个页面（创建、列表、管理），后台端 2 个页面（管理、审核队列）。

**理由**: 项目已有用户端/后台端分离的路由体系，沿用现有模式。频道管理页使用动态路由 `:id` 参数区分不同频道。

**替代方案**: 将所有频道功能放在一个页面用 Tab 切换 -- 放弃，因为用户端和后台端入口不同，合并会增加路由和权限复杂度。

### 2. 频道创建页复用策略

**决策**: 个人频道和组织频道创建共用同一页面 `/content/channel/create`，通过 Steps 组件实现分步向导（Step 1 选类型，Step 2 填表单），根据类型动态渲染字段。系统频道创建使用后台 Modal 弹窗。

**理由**: PRD 明确要求个人/组织频道共用页面，分步向导符合用户认知。系统频道在后台操作，用 Modal 更轻量。

### 3. 频道管理页 Tab 结构

**决策**: 频道管理页 `/content/channel/manage/:id` 使用 Ant Design Tabs 组件，三个 Tab：概览、编辑信息、设置。

**理由**: PRD 要求将危险操作（转让/删除）放在深层级（设置 Tab），编辑信息独立 Tab，概览提供快速了解。Tab 结构清晰且符合项目现有页面模式。

### 4. 表单承载方式

**决策**: 创建表单使用独立页面，编辑表单使用 Drawer（抽屉），系统频道创建使用 Modal。

**理由**: 创建流程包含分步向导，需要独立页面承载。编辑是管理页内的子操作，Drawer 从右侧滑出不打断上下文。系统频道创建是轻量操作，Modal 足够。

### 5. 关键字段审核标识

**决策**: 关键字段区域使用浅黄色背景 + 橙色 Tag 标签"修改需审核"双重标识，用户首次修改时弹出一次性提示。

**理由**: PRD 要求不仅依赖 Tooltip，需要始终可见的视觉标识。双重标识（背景色 + Tag）确保用户注意到审核机制。

### 6. 审核 diff 对比

**决策**: 文本字段使用左右并排对比（删除红色删除线，新增绿色背景），图片字段左右并排展示，未修改字段折叠显示。

**理由**: PRD 明确要求的展示格式。文本 diff 可基于字符串对比实现，图片直接展示新旧两图。

### 7. API 封装与文件组织

**决策**: 所有频道 API 封装在 `src/api/content/channel/` 目录下，`index.ts` 统一导出，`model/channelModel.ts` 定义类型。使用项目标准 defHttp。

**理由**: 遵循项目现有 API 组织规范。

### 8. 状态管理

**决策**: 新建 `src/store/modules/channel.ts` Pinia store，管理当前频道详情、列表缓存、类型/状态选项缓存。

**理由**: 频道相关状态跨页面共享（如频道详情在管理页、编辑、转让等多处使用），Store 避免重复请求。

### 9. API 路径对照表

**决策**: 前端封装时需注意后端 API 路径前缀差异，统一使用以下路径映射。

| 功能模块 | Controller | API 前缀 | 前端封装路径 |
|----------|------------|----------|--------------|
| 用户端频道 CRUD | ChannelController | `/api/v1/channels` | `/api/v1/channels/*` |
| 后台频道管理 | ChannelAdminController | `/api/v1/admin/channels` | `/api/v1/admin/channels/*` |
| 审核队列 | ChannelReviewController | `/jeecg-boot/api/v1/content/channel/review` | `/jeecg-boot/api/v1/content/channel/review/*`（绝对路径，不经过 defHttp 前缀拼接） |
| 生命周期管理 | ChannelLifecycleController | `/jeecg-boot/api/v1/content/channel/lifecycle` | `/jeecg-boot/api/v1/content/channel/lifecycle/*` |
| 频道治理 | ChannelGovernanceController | `/channel/governance` | `/channel/governance/*` |
| 内容发布 | ChannelPublishController | `/content/channel/publish` | `/content/channel/publish` |

**理由**: 后端 API 存在多个 Controller，路径前缀不一致，前端需统一管理避免混淆。

**后端已实现的 API（ChannelController）**:
- `POST /api/v1/channels/create` — 创建频道
- `GET /api/v1/channels/list` — 我的频道列表
- `GET /api/v1/channels/{id}` — 频道详情
- `PUT /api/v1/channels/{id}` — 更新频道
- `DELETE /api/v1/channels/{id}` — 删除频道
- `POST /api/v1/channels/{id}/cancel-delete` — 撤销删除
- `POST /api/v1/channels/{id}/transfer` — 发起转让
- `POST /api/v1/channels/transfer/{transferId}/confirm` — 确认转让
- `POST /api/v1/channels/transfer/{transferId}/reject` — 拒绝转让
- `GET /api/v1/channels/{id}/delete-check` — 删除前置校验
- `GET /api/v1/channels/{id}/transfers` — 转让历史查询
- `GET /api/v1/channels/{id}/transfer/pending` — 待确认转让查询
- `GET /api/v1/channels/check-name` — 名称唯一性校验
- `PUT /api/v1/channels/privacy` — 更新隐私设置
- `PUT /api/v1/channels/join-method` — 更新加入方式

**后端已实现的 API（ChannelReviewController）**:
- `GET /jeecg-boot/api/v1/content/channel/review/list` — 审核队列列表
- `GET /jeecg-boot/api/v1/content/channel/review/detail/{id}` — 审核详情
- `POST /jeecg-boot/api/v1/content/channel/review/action` — 审核操作

**后端已实现的 API（ChannelAdminController）**:
- `POST /api/v1/admin/channels/create-system` — 创建系统频道
- `POST /api/v1/admin/channels/{id}/review` — 审核频道

## Risks / Trade-offs

- **[风险] 审核 diff 对比复杂度** → 文本 diff 使用简单字符串对比即可满足需求，不引入第三方 diff 库。如果后续需要更精确的 diff（如富文本），再评估引入。
- **[风险] 移动端适配工作量** → 优先实现桌面端核心功能，移动端使用响应式断点 + 组件切换（表格转卡片、弹窗转全屏）渐进适配。
- **[风险] 名称唯一性校验并发** → 使用 300ms 防抖 + 失焦触发策略，减少接口调用频率。后端做最终兜底校验。
- **[权衡] 频道管理页 vs 独立子页面** → 选择 Tab 结构而非独立子页面，牺牲了一定的 URL 可分享性，但获得了更好的导航体验和上下文保持。
- **[权衡] Store 缓存策略** → 选择在 Store 中缓存列表数据，页面切换时可能有短暂的数据陈旧，但减少了重复请求。通过关键操作后主动刷新解决。

### 10. 错误码处理体系

**决策**: 前端依赖项目全局 HTTP 拦截器统一处理后端错误码，频道模块不单独实现错误处理逻辑。

**后端响应格式**: `{ code: number, result: any, message: string, success: boolean }`

**前端处理策略**（由 `src/utils/http/axios/index.ts` 中 `transformRequestHook` 和 `checkStatus()` 统一处理）:

| 场景 | 后端返回 | 前端处理 |
|------|---------|---------|
| 成功 | `code: 200` | 返回 `result` 字段 |
| Token 过期 | `code: 401` 或 HTTP 401 | 清除 Token，跳转登录页（`userStore.logout(true)`） |
| 权限不足 | HTTP 403 | 显示"暂无权限"提示 |
| 资源不存在 | HTTP 404 | 显示"资源不存在"提示 |
| 网络超时 | `ECONNABORTED` | 显示"请求超时"提示 |
| 网络断开 | `Network Error` | 显示"网络异常"提示 |
| 业务错误 | `code: 非200` | 直接显示后端 `message` 字段内容（toast/modal） |
| 用户状态异常 | `message` 含状态标识 | 解析 message 中的状态码，显示对应提示 |

**频道模块特殊错误场景**: 频道模块的业务错误（名称冲突、数量上限、前置条件不满足等）由后端通过 `code + message` 返回，前端直接展示后端 message，无需前端硬编码错误码映射。

**理由**: 项目已有成熟的全局错误处理机制（`transformRequestHook` + `checkStatus.ts`），频道模块复用该机制即可，无需额外封装。

### 11. Token 过期处理策略

**决策**: 频道模块依赖项目全局 Token 过期处理机制，不做额外的 Token 刷新逻辑。

**当前项目行为**（`src/utils/http/axios/index.ts`）:
1. 响应拦截器检测 `code: 401`（`ResultEnum.TIMEOUT`）→ 清除 Token → `userStore.logout(true)` → 跳转登录页
2. HTTP 状态码 401 → `checkStatus(401)` → 清除 Token → 跳转登录页或显示登录覆盖层（取决于 `sessionTimeoutProcessing` 配置）

**注意**: 项目定义了 `refreshToken` API（`src/api/content/auth/index.ts`）但未在拦截器中接入自动刷新。Token 过期后用户需重新登录。如后续需要无感刷新，需在全局拦截器层面改造，不属于本 change 范围。

**理由**: 保持与项目现有行为一致，不在频道模块引入特殊逻辑。
