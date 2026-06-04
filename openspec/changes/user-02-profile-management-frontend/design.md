## Context

内容社区模块（jeecg-module-content）基于 JeecgBoot Vue3 前端框架，使用 Vue 3 + TypeScript + Ant Design Vue 4 + Vben Admin 架构。本次变更对接后端 `ContentUserProfileController`（`/content/user/profile/*` 前缀，11 个端点，其中 `review/handle` 为后台审核端点前端不对接），实现前端资料管理与主页个性化能力。

> **实施更新（2026-06-04）**: 本次更新以"前端对齐后端实际契约"为目标。原 design 假设的独立上传端点、update-count 接口、5/10 次频控等**不**在当前后端实现中，已剔除相关假设；头像/背景图改由前端 OSS 直传。后端 Controller 实际提供 11 个端点，所有 POST 写入端点仅返回 `Result<String>` 操作结果字符串，前端保存后需重新调用 `GET /detail` 获取最新数据。

现有基础设施：
- 路由系统：基于 vue-router，路由配置在 `src/router/` 下
- 状态管理：Pinia，`src/store/modules/user.ts` 已有 useUserStore
- HTTP 封装：`defHttp` from `src/utils/http/axios`
- 组件库：Ant Design Vue 4（自动导入），Vben Admin 内置组件
- 响应式：项目已有断点体系（xs/sm/md/lg/xl）

## Goals / Non-Goals

**Goals:**
- 对接后端 12 个端点，实现前端资料管理与主页个性化能力
- 头像/背景图走 OSS 客户端直传方案，不引入内容社区上传端点
- 15 个 `*Visibility` 字段全部覆盖前端隐私设置页；`onlineStatusVisibility` 特殊枚举
- 历史记录通过 `historyType` 参数区分类型，前端用 Tabs 切换
- 所有页面适配 PC/平板/移动端

**Non-Goals:**
- 不实现认证申请流程（仅展示后端返回的认证数据）
- 不实现图片压缩和多分辨率生成（OSS 客户端负责）
- 不实现缓存失效逻辑（由后端 Redis 处理）
- 不实现敏感词过滤（由后端处理，前端仅展示错误）
- 不实现服务端素材校验（OSS 客户端保证 ≤5MB、JPG/PNG/WebP）
- 不实现独立的 update-count 接口展示（后端不提供，前端不做）
- 不实现独立支付结算、企业通讯录、生物识别等功能

## Decisions

### D1: 页面路由组织方式

**决策**: 在内容社区模块路由下新增 `/content/profile/` 前缀的子路由，包含 edit、homepage-settings、privacy、history 四个页面。

**理由**: 保持与现有社区模块路由结构一致，避免路由层级过深。

**替代方案**: 放在 `/user/` 路由下 → 社区资料是业务扩展，混入系统路由会造成职责不清。

### D2: 状态管理策略

**决策**: 扩展现有 useUserStore 新增 profile 相关状态字段；页面级表单状态使用组件内部 ref 管理，不提升到全局 Store。

**理由**: `profileCompletionRate`、`reviewStatus` 等需要跨页面共享，适合放 Store；表单数据生命周期仅限于编辑页面，放组件内更简洁。

**替代方案**: 新建独立 useProfileStore → 增加维护成本，且 profile 与 user 数据高度耦合。

### D3: 头像与背景图上传方案

**决策**: 
- **OSS 客户端直传**：用户选择本地图片后，前端使用 OSS SDK 直接上传到对象存储，成功后回填 CDN URL。
- **URL 持久化**：上传完成后把 CDN URL 提交给 `POST /content/user/profile/update` 的 `avatar` 或 `homepage/homepageBackground` 字段。
- **裁剪**：集成 `cropperjs`，头像锁定 1:1、背景图锁定 16:9。
- **客户端校验**：格式（JPG/PNG/WebP）、大小（≤5MB）由前端在文件选择阶段拦截。

**理由**: 避免后端引入 OSS SDK 依赖；让前端直接走 CDN 边缘节点上传，节省一次回源；后端只校验 URL 字符串格式即可。

**替代方案**: 复用 JeecgBoot 现有上传接口 → 路径在 `system` 模块下，与内容社区域不符；且会引入跨域上传链路。

### D4: 模块拖拽排序方案

**决策**: 使用 `vuedraggable`（基于 Sortable.js）实现拖拽排序，移动端通过长按触发拖拽模式。

**理由**: vuedraggable 是 Vue 3 生态最成熟的拖拽排序库，与 Vue 响应式系统集成良好，触摸兼容性好。

### D5: 认证标识组件设计

**决策**: 封装 `VerificationBadge` 组件，Props 接收 `badges: ContentUserVerificationBadgeVO[]`，内部维护 `visualStyleKey → 图标 + 颜色` 字典。

- `visualStyleKey` 字典覆盖以下值：`INDIVIDUAL`、`ENTERPRISE`、`CREATOR`、`OFFICIAL`、`REAL_NAME`、`MOBILE`、`EMAIL`
- 未知 `visualStyleKey` 落入 `DEFAULT` 兜底（灰色对勾）
- 折叠策略：最多显示 2 个 + "+N" 徽标
- 优先级：OFFICIAL > ENTERPRISE > CREATOR > INDIVIDUAL > REAL_NAME > MOBILE > EMAIL

**理由**: `visualStyleKey` 由后端下放，前端维护一份映射表即可统一所有页面的认证展示。

### D6: 隐私设置即时生效方案

**决策**: 隐私设置保存成功后，前端主动调用 `GET /content/user/profile/detail?ownerUserId=X&viewerUserId=Y` 刷新本地缓存的用户资料数据。不实现前端缓存失效逻辑。

**理由**: 后端 Redis TTL 由后端控制，前端只需在用户主动修改后立即刷新本地数据，保证修改者自己看到最新效果。

### D7: 隐私字段全覆盖与特殊枚举处理

**决策**: 隐私设置页必须覆盖 15 个 `*Visibility` 字段（不只 5 个基础字段），按以下分组组织：

- **基础资料** (5): `bioVisibility` / `genderVisibility` / `birthdayVisibility` / `regionVisibility` / `professionVisibility`
- **扩展资料** (1): `personalLinkVisibility`
- **主页** (3): `homepageBackgroundVisibility` / `themeColorVisibility` / `homepageModuleVisibility`
- **认证** (2): `certificationVisibility` / `verificationBadgesVisibility`
- **活动** (3): `profileCompletionVisibility` / `profileReviewStatusVisibility` / `recentActivityVisibility`
- **在线状态** (1): `onlineStatusVisibility` ← 特殊枚举 `PUBLIC|HIDDEN|MUTUAL_ONLY`，表单 Select 选项需动态切换
- **布尔开关** (2): `showMutualFollowersCount` / `showRecentActivityHighlight`

**理由**: 与后端 `ContentUserPrivacyUpdateReq` 15 个 visibility + 2 个 Boolean 字段一一对应。

### D8: 历史记录按类型分 Tab

**决策**: 历史记录页用 Tabs 切换"昵称历史"和"头像历史"，每个 Tab 调用 `GET /content/user/profile/history/list?userId=X&historyType=NICKNAME|AVATAR` 加载。

**理由**: 后端接口使用 `historyType` 路径/查询参数区分类型，前端按 Tab 触发对应请求；恢复操作统一调用 `POST /content/user/profile/history/restore?userId=X&historyId=Y`。

### D9: 频率限制展示

**决策**: **不**展示"今日还可修改 X 次"或"每时还可修改 X 次"提示。后端没有暴露 update-count 接口；保存时如果后端返回业务错误（如敏感词、昵称占用），通过 toast 提示。

**理由**: 不展示可能误导用户的计数；后端频控若实现则走统一错误码。

## API 对接矩阵

> **全局说明**：所有端点均返回 `Result<T>` 包装类型（JeecgBoot 惯例），前端 `defHttp` 自动解包。下表"出参"列展示解包后的实际业务类型。所有 POST 写入端点仅返回操作结果字符串（如"更新成功"），**不**返回更新后的 VO；前端保存后需重新调用 `GET /detail` 获取最新数据。

| 端点 | HTTP | 入参 | 出参（解包后） | 涉及能力 |
|------|------|------|----------------|----------|
| `/content/user/profile/detail` | GET | `ownerUserId`, `viewerUserId`(可选) | `ContentUserProfileVO` | profile-editing / verification-badge / homepage-customization |
| `/content/user/profile/update` | POST | `userId` (query) + `ContentUserProfileUpdateReq` | `String`（"更新成功"） | profile-editing / homepage-customization |
| `/content/user/profile/review/handle` | POST | `ContentUserReviewHandleReq` | `String`（"处理成功"） | 后台审核，前端**不**对接 |
| `/content/user/profile/privacy/update` | POST | `userId` (query) + `ContentUserPrivacyUpdateReq` | `String`（"更新成功"） | privacy-settings |
| `/content/user/profile/homepage/update` | POST | `userId` (query) + `ContentUserHomepageUpdateReq` | `String`（"更新成功"） | homepage-customization |
| `/content/user/profile/homepage/defaults/restore` | POST | `userId` (query) | `String`（"恢复成功"） | homepage-customization |
| `/content/user/profile/homepage/modules` | GET | `userId` (query) | `List<ContentUserHomepageModuleVO>` | homepage-customization |
| `/content/user/profile/badge/list` | GET | `userId` (query) | `List<ContentUserVerificationBadgeVO>` | verification-badge |
| `/content/user/profile/badge/detail` | GET | `badgeId` (query) | `ContentUserVerificationBadgeVO` | verification-badge |
| `/content/user/profile/history/list` | GET | `userId`, `historyType` (NICKNAME\|AVATAR) | `List<ContentUserProfileHistoryVO>` | profile-history |
| `/content/user/profile/history/restore` | POST | `userId`, `historyId` (query) | `String`（"恢复成功"） | profile-history |

> **端点总数**：11 个（其中 `review/handle` 为后台审核端点，前端不对接，实际前端使用 10 个端点）。

## Risks / Trade-offs

**[Risk] vuedraggable 移动端拖拽体验不一致** → 通过长按触发拖拽模式 + 触摸热区 44px + 振动反馈优化体验，必要时降级为上下箭头按钮排序。

**[Risk] 头像裁剪在低端移动设备上性能问题** → cropperjs 使用 Canvas 渲染，大图可能导致卡顿。缓解：上传前客户端压缩图片至 2000px 以内。

**[Risk] OSS 直传泄露 AccessKey** → 必须使用 STS 临时凭证或前端签名 URL，禁止把长期 AccessKey 硬编码到前端 bundle。

**[Risk] 隐私设置 15 个字段页面过长** → 按 D7 分组（基础资料/扩展/主页/认证/活动/在线状态）+ 折叠面板/分类 Tab，控制单屏信息密度。

**[Risk] 认证标识 `visualStyleKey` 字典遗漏** → 维护 `DEFAULT` 兜底样式 + 后端变更时前端同步更新文档。

**[Trade-off] 历史记录不缓存，每次进入重新加载** → 增加了接口调用次数，但保证数据实时性，且历史记录访问频率低，可接受。
