## Context

JeecgBoot 内容社区前端基于 Vue 3 + TypeScript + Vite 6 + Ant Design Vue 4 + Pinia 技术栈。现有项目使用 `defHttp` 封装 HTTP 请求，响应格式统一为 `{ code, result, message, success }`。前端已有 `useUserStore`（用户信息）、`usePermissionStore`（菜单权限）等 Store，以及 Table、Form、Modal、Drawer、Upload 等通用组件。

当前社区缺少举报、申诉、帮助中心、更新日志和客服支持功能。用户无法举报违规内容，被处罚后无法申诉，遇到问题只能联系人工客服。本变更在现有架构上新增这些能力，遵循项目既有模式。

## Goals / Non-Goals

**Goals:**
- 新增举报系统：举报表单弹窗、举报列表页、举报详情、防重复校验、撤回
- 新增申诉系统：申诉提交页、申诉列表页、申诉详情、次数限制（最多3次）、撤回
- 新增帮助中心：分类浏览、全文搜索（<500ms）、文章详情、有用/无用反馈
- 新增更新日志：版本列表、时间线展示、搜索、新版本首次登录提示
- 新增客服通道：智能客服机器人、转人工排队、实时对话（WebSocket）、服务评分、会话历史
- 高等级用户（LV.15+）客服优先排队
- 所有页面支持响应式布局（PC/平板/移动端）
- 所有页面首屏加载 <2秒

**Non-Goals:**
- 独立工单系统
- 电话客服
- 智能客服 NLP 引擎训练
- 帮助中心 CMS 后端
- 申诉审计日志前端生成

## Decisions

### D1: 页面组件组织方式

**决策**: 按功能域在 `src/views/support/` 下创建子目录，每个功能域独立目录。

**理由**: 举报、申诉、帮助中心、更新日志、客服五个功能域相对独立，按域组织便于维护和后续拆分。

**目录结构**:
```
src/views/support/
  report/          # 举报相关页面
  appeal/          # 申诉相关页面
  help/            # 帮助中心
  changelog/       # 更新日志
  customer-service/ # 客服对话
```

**替代方案**: 全部放在 `src/views/user/` 下 -- 会与现有用户中心页面混在一起，不利于模块化。

### D2: 客服实时通信方案

**决策**: 使用 WebSocket 实现客服实时对话，复用项目现有 WebSocket 基础设施。

**理由**: PRD 明确要求 WebSocket 优先，消息延迟 <500ms。HTTP 轮询无法满足实时性要求。

**关键设计**:
- WebSocket 连接由 `ChatPanel` 组件管理，进入页面时建立，离开时断开
- 消息状态机：sending -> sent -> failed
- 断连重连：自动重连，30秒超时提示刷新
- 排队状态持久化到 sessionStorage

**替代方案**: HTTP 长轮询 -- 延迟高，服务端压力大，不符合 PRD 要求。

### D3: 状态管理策略

**决策**: 新增 `useFeedbackStore`（Pinia），集中管理举报、申诉、客服会话状态。

**理由**: 举报列表、申诉列表、客服消息等状态需要跨页面共享（如个人中心菜单角标显示待处理数量）。单一 Store 简化状态同步。

**Store 职责划分**:
- `useFeedbackStore`: 举报/申诉列表、客服会话、消息列表、排队状态、统计数据
- `useUserStore`: 用户等级判断（LV.15+ 优先排队）

**替代方案**: 每个功能域独立 Store -- 增加复杂度，且统计数据需要跨域汇总。

### D4: 路由与菜单注册

**决策**: 路由配置放在各功能域目录下的 `route.ts` 文件中，统一在主路由文件中引入。

**理由**: 遵循项目现有路由组织模式，每个模块自包含路由定义。

**路由清单**:
- `/user/reports` -- 我的举报列表
- `/user/appeals` -- 我的申诉列表
- `/user/appeals/create` -- 申诉提交页
- `/help` -- 帮助中心
- `/help/article/:id` -- 帮助文章详情
- `/changelog` -- 更新日志
- `/customer-service` -- 客服对话
- `/user/service-history` -- 客服历史记录

### D5: 组件复用策略

**决策**: 优先复用项目现有通用组件（Table、Form、Modal、Drawer、Upload、useTable、useForm、useModal、useDrawer），仅新建业务组件。

**理由**: 减少代码重复，保持 UI 一致性，降低维护成本。

**新建业务组件**:
- `ReportModal` -- 举报表单弹窗
- `AppealForm` -- 申诉提交表单
- `ChatPanel` -- 客服对话面板
- `ChatMessage` -- 单条消息气泡
- `RatingModal` -- 服务评分弹窗
- `HelpSearch` -- 帮助中心搜索区
- `ChangelogTimeline` -- 更新日志时间线
- `VersionCard` -- 单个版本卡片
- `ArticleFeedback` -- 文章有用/无用反馈

### D6: 响应式布局策略

**决策**: 使用 CSS 媒体查询 + Ant Design Vue 的 Grid 系统实现响应式。列表页在移动端转为卡片布局。

**断点**:
- PC 端: >= 1200px
- 平板端: 768px - 1199px
- 移动端: < 768px

**关键适配**:
- 列表页：PC 用 Table，移动端用卡片列表
- 弹窗：PC 用 Modal 520px，移动端全屏
- 客服对话：PC 用固定高度面板，移动端全屏

## Risks / Trade-offs

### R1: WebSocket 连接稳定性
**风险**: 弱网环境下 WebSocket 频繁断连，影响客服体验。
**缓解**: 实现自动重连机制，断连时显示状态横幅，30秒超时提示刷新，消息发送失败支持重试。

### R2: 客服排队状态丢失
**风险**: 用户刷新页面后排队状态丢失。
**缓解**: 排队状态持久化到 sessionStorage，刷新后自动恢复并重新连接 WebSocket。

### R3: 文件上传大文件处理
**风险**: 10MB 视频文件上传耗时长，用户可能误以为卡住。
**缓解**: 显示上传进度条，上传期间禁用提交按钮，支持失败重试。

### R4: 帮助中心搜索性能
**风险**: 搜索响应时间超过 500ms 目标。
**缓解**: 前端 300ms 防抖，搜索结果本地缓存，首次加载后缓存分类列表。

### R5: 申诉次数限制的前端校验
**风险**: 前端次数限制可能被绕过（如并发请求）。
**缓解**: 前端校验仅用于 UX 优化，真正的次数限制由后端 API 校验，前端根据后端返回的错误码提示。

## File Structure

```
jeecgboot-vue3/src/
  views/support/
    report/
      index.vue              # 我的举报列表页
      components/
        ReportModal.vue       # 举报表单弹窗
        ReportDetailDrawer.vue # 举报详情抽屉
    appeal/
      index.vue              # 我的申诉列表页
      create.vue             # 申诉提交页
      components/
        AppealDetailDrawer.vue # 申诉详情抽屉
    help/
      index.vue              # 帮助中心首页
      article.vue            # 帮助文章详情页
      components/
        HelpSearch.vue        # 帮助中心搜索区
        ArticleFeedback.vue   # 文章有用/无用反馈
    changelog/
      index.vue              # 更新日志页
      components/
        ChangelogTimeline.vue # 更新日志时间线
        VersionCard.vue       # 单个版本卡片
    customer-service/
      index.vue              # 客服对话页
      history.vue            # 客服历史记录页
      components/
        ChatPanel.vue         # 客服对话面板
        ChatMessage.vue       # 单条消息气泡
        RatingModal.vue       # 服务评分弹窗
  store/modules/
    feedback.ts              # useFeedbackStore
  api/support/
    report.ts                # 举报 API
    appeal.ts                # 申诉 API
    help.ts                  # 帮助中心 API
    changelog.ts             # 更新日志 API
    customer-service.ts      # 客服 API
```

## Test Strategy

### 单元测试 (Vitest + Vue Test Utils)

| 测试文件 | 测试内容 |
|---------|---------|
| `ReportModal.spec.ts` | 举报表单校验、防重复检查、证据上传状态机、提交成功/失败 |
| `AppealForm.spec.ts` | 申诉表单校验、次数限制逻辑、第3次确认弹窗 |
| `ChatPanel.spec.ts` | 消息发送/接收、断连重连、排队状态管理、消息状态流转 |
| `ChatMessage.spec.ts` | 消息气泡渲染、发送失败重试、状态图标显示 |
| `RatingModal.spec.ts` | 评分提交、重复评分拦截 |
| `HelpSearch.spec.ts` | 搜索防抖、空结果展示、关键词高亮 |
| `ArticleFeedback.spec.ts` | 有用/无用反馈、重复反馈拦截 |
| `useFeedbackStore.spec.ts` | 状态流转、列表分页、统计数据更新 |

### 测试策略

- **TDD 驱动**: 先写测试再实现，确保每个组件的核心交互逻辑有测试覆盖
- **重点测试**: 表单校验、状态流转、防重复提交、WebSocket 断连处理、响应式布局
- **Mock 策略**: Mock `defHttp` 和 WebSocket 连接，隔离前端逻辑

## Migration Plan

N/A -- 本变更为纯前端新增功能，不涉及部署变更。新增页面和组件不影响现有功能。

部署步骤：
1. 合并代码到 `springboot3_content` 分支
2. 前端构建部署
3. 后端 API 需同步上线（举报、申诉、帮助中心、客服相关接口）

## Open Questions

| 编号 | 问题 | 影响 | 当前假设 | 验证状态 |
|------|------|------|---------|---------|
| Q1 | 客服实时对话是否复用现有 IM 系统？ | ChatPanel 组件实现 | 假设复用现有 WebSocket IM 系统 | 待确认 -- 后端仅有 `getCustomerServiceEntry` 返回路由信息（routeType/title/description），无 WebSocket 端点 |
| Q2 | 帮助文章数据来源？ | 帮助中心 API | 假设由运营通过 CMS 后台管理 | 部分验证 -- 后端 `getHelpCenter` 返回静态数据（faqCategories/guideEntries/releaseNotes），无文章详情和搜索端点 |
| Q3 | 智能客服 NLP 引擎是否已就绪？ | 智能客服对话 | 假设使用现有引擎 | 待确认 -- 后端仅有 `createServiceSession` 服务方法，未暴露 HTTP 端点 |
| Q4 | 举报/申诉的通知方式？ | 通知对接 | 假设使用现有站内通知系统 | 待确认 |
| Q5 | 文件上传是否支持断点续传？ | 证据上传 | 假设不支持 | 未验证 |
| Q6 | 客服优先级阈值：LV.15+ 还是 LV.5+？ | 排队逻辑 | 按 PRD 假设 LV.15+ | 已修正 -- 后端实际逻辑是 `level>=15` 或 `growthValue>=400`，即 LV.15+ 或成长值 >=400 均可获得优先 |
| Q7 | 举报/申诉撤回 API 是否已就绪？ | 撤回功能 | 假设后端支持 | 已验证 -- 后端完全缺失撤回功能（举报和申诉均无撤回端点和服务方法） |
