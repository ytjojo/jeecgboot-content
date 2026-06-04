## Context

当前内容社区前端（jeecgboot-vue3）已有粉丝、互关、邀请等功能模块，位于 `src/views/content/` 目录下。项目使用 Vue 3 + Ant Design Vue + Vben Admin 框架，表单使用 schema 驱动的 Form 组件，API 通过 `defHttp` 封装。

本变更为内容社区新增"设置"功能分区，包含四个独立页面，覆盖通知偏好、隐私可见性、第三方授权和账户安全。后端接口部分就绪（`/content/user/settings/` 和 `/content/user/auth/`），其中通知设置和第三方授权 API 已完整，但隐私设置缺少 GET 端点、安全设置缺少更新端点，需后端补充。详见 `backend-issues.md`。

## Goals / Non-Goals

**Goals:**
- 实现四个设置页面，覆盖 PRD 定义的全部 P0/P1/P2 功能
- 遵循项目现有组件模式（Page / Form / Table / Modal），保持代码风格一致
- 支持 PC / 平板 / 移动端三档响应式布局
- 表单数据通过 API 实时读写，无前端持久化缓存

**Non-Goals:**
- 不实现数据导出（GDPR 工具）
- 不实现两步验证的具体流程（仅提供入口导航）
- 不实现第三方 OAuth 服务端逻辑（仅管理已授权状态）
- 不实现通知审计日志前端查看（运营后台功能）
- 不新建全局 Pinia Store，全部使用页面级状态

## Decisions

### 1. 目录结构：在 `src/views/content/` 下新增 `settings/` 子目录

**选择**: `src/views/content/settings/` 下按页面分文件夹

```
src/views/content/settings/
  ├── notification/    # 通知设置页
  │   └── index.vue
  ├── privacy/         # 隐私设置页
  │   └── index.vue
  ├── third-party/     # 第三方授权页
  │   └── index.vue
  ├── security/        # 账户安全页
  │   └── index.vue
  └── api.ts           # 统一 API 封装
```

**理由**: 与现有 `content/fan/`、`content/mutual-follow/` 目录结构保持一致，API 集中在一个文件便于维护。

**替代方案**: 每个页面独立 api.ts → 但接口数量不多（9个），集中管理更清晰。

### 2. 表单方案：schema 驱动 Form + 手动布局混合

**选择**: 通知设置页和隐私设置页使用项目已有的 `Form` 组件（schema 驱动），但通知列表的"类型名 + 开关 + 渠道选择"行式布局需要手动模板实现，不完全依赖 schema。

**理由**: PRD 要求的通知列表每行包含图标、名称、Switch、Checkbox 组，这种复合布局超出 schema 驱动 Form 的标准能力。隐私设置页的简单表单项（Select / Radio / Switch）可完全使用 schema。

**替代方案**: 全部手动模板 → 开发效率低，且不利用已有的表单校验和提交逻辑。

### 3. 免打扰临时关闭：前端倒计时 + 后端时间戳校验

**选择**: 前端使用 `setInterval` 实现倒计时展示，页面加载时从 `DndRuleVO.temporaryDisableUntil` 读取截止时间计算剩余时长。

**理由**: 倒计时仅用于 UI 展示，实际免打扰状态由后端控制，前端不承担状态判断逻辑。

**风险**: 用户修改本地时间可能导致倒计时异常 → 后端接口返回的 `temporaryDisableUntil` 为权威时间源，前端仅做展示。

### 4. 路由配置：懒加载 + 路由元信息

**选择**: 四个页面均使用 `() => import(...)` 懒加载，路由 meta 中配置 `title` 用于面包屑和菜单显示。

**理由**: 设置页面为低频访问页，懒加载可减少首屏 bundle 体积。

### 5. API 拦截器 userId 注入

**选择**: 依赖全局请求拦截器自动注入 userId，API 封装中不手动传递。

**理由**: PRD 明确说明后端通过 `@RequestParam("userId")` 接收，前端拦截器已实现自动注入逻辑。需确认拦截器已支持 GET 的 query 参数和 POST 的 params 注入。

### 6. 响应式方案：Ant Design Vue 栅格 + CSS 媒体查询

**选择**: 使用 `a-row` / `a-col` 栅格系统处理卡片布局（账户安全页 2x2 网格），结合 CSS 媒体查询处理通知列表和授权表格的移动端适配。

**理由**: 项目已引入 Ant Design Vue，栅格系统可直接使用；表格转卡片列表需要 CSS 媒体查询配合条件渲染。

## Risks / Trade-offs

- **[隐私设置 GET 端点缺失]** 后端 `ContentUserSettingsController` 中没有 `GET /content/user/settings/privacy` 端点，隐私数据仅通过 profile 接口返回 → 需后端补充独立 GET 端点，或前端改为从 profile 接口提取隐私字段。详见 `backend-issues.md`。
- **[安全设置更新端点缺失]** 后端仅有 `GET /content/user/settings/security`，无 POST 更新端点 → 登录提醒开关无法保存，需后端补充 `POST /content/user/settings/security/update`。详见 `backend-issues.md`。
- **[订阅更新渠道字段缺失]** `ContentNotificationChannelConfigVO` 仅 6 个渠道字段，缺少 `subscriptionChannels` → 需后端补充，否则第 7 类通知（订阅更新）无法配置渠道。
- **[拦截器兼容性]** userId 自动注入拦截器可能未覆盖 POST 请求的 params 场景 → 开发时验证，必要时在 API 封装中手动补充 userId。
- **[免打扰规则数限制]** PRD 假设最多 5 条规则，但未在后端接口文档中明确 → 前端做本地限制（超过 5 条隐藏新增按钮），后端也应做校验。
- **[收藏夹字段名差异]** 后端 `favoriteVisibility` 与前端 `favoritesVisibility` 命名不一致 → 在 API 响应拦截中做字段映射。
- **[移动端表格降级]** 第三方授权 Table 在移动端转为卡片列表，需要额外的条件渲染逻辑 → 增加约 50 行代码，但提升移动端体验。
