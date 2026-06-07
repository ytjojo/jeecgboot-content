## Context

JeecgBoot 内容社区模块（jeecg-module-content）当前不具备圈子能力。前端基于 Vue 3 + TypeScript + Ant Design Vue 4 + Vben Admin 技术栈，使用 `defHttp` 统一封装 API 请求，组件体系包含自定义 Form（schema 驱动）、Table、Modal、Drawer、Upload、Cropper 等。

本次变更需要在现有前端架构下新增完整的圈子模块，包含 6 个页面、8 个业务组件、1 个 Pinia Store、14 个 API 接口定义。后端已实现 8 个接口（create/update/join/leave/change-role/mute/unmute/remove），search 已实现但结果 VO 缺少 category 字段；6 个接口尚未实现（detail/my-list/public-list/check-name/member-list/governance-log），前端需对缺失接口采用 Mock 开发策略。

**约束**:
- 技术栈：Vue 3 + TypeScript + Ant Design Vue 4 + Vben Admin
- API 封装：统一使用 `defHttp`，响应格式 `{ code: 200, result, message, success }`
- 路由：遵循现有 `/src/router/` 目录结构和动态路由注册方式
- 组件：优先复用 `/@/components/` 下现有组件，新建业务组件放在 `/src/views/circle/components/`
- 状态管理：使用 Pinia，Store 放在 `/src/store/modules/`
- 国际化：文案硬编码中文，但统一管理在常量文件 `src/locales/lang/zh-CN/circle.ts`
- 响应式：支持桌面端（>= 1200px）、平板端（768-1199px）、移动端（< 768px）

## Goals / Non-Goals

**Goals:**
- 实现圈子 MVP 全部前端功能：创建、详情、加入/退出、成员管理、搜索、治理日志
- 复用现有组件体系（Form/Table/Modal/Upload 等），减少重复开发
- 实现响应式三端适配（PC/平板/移动端）
- 搜索性能 P95 < 500ms，核心操作 P95 < 800ms
- 文案统一管理，预留国际化接口
- 关键操作节点埋点上报

**Non-Goals:**
- 不实现内容管理（置顶/精华/公告/@成员）— 属于 EPIC-11
- 不实现审核机制（人工审核流程）— 后端标记待复核，前端仅展示状态
- 不实现推荐算法与成长激励 — 属于 EPIC-12/13
- 不实现圈子解散功能
- 不实现密码修改功能
- 不实现邀请功能的具体流程（仅做入口限制，通过 `isInvited` 字段判断）
- 不实现治理日志导出 CSV

## Decisions

### D1: 页面组织方式 — 嵌套路由 vs 独立路由

**选择**: 独立路由，每个页面注册为独立路由项。

**理由**: 圈子各页面（列表、创建、详情、成员管理、搜索、治理日志）之间没有嵌套关系，详情页的「动态/成员」Tab 内容通过组件切换实现而非路由嵌套。独立路由更简单，与项目现有页面组织方式一致。

**替代方案**: 嵌套路由（详情页包含成员管理子路由）— 增加路由复杂度，且成员管理页需要独立的权限判断。

### D2: 圈子创建方式 — 独立页面 vs Modal 弹窗

**选择**: 独立页面 `/circle/create`，采用步骤条（Steps）组件引导。

**理由**: PRD 定义了两步表单（基础信息 + 隐私设置）+ 成功页的流程，独立页面有更好的空间展示表单字段和图片上传裁剪，移动端体验更佳。

**替代方案**: Modal 弹窗 — 空间有限，图片上传裁剪体验差，移动端表单字段拥挤。

### D3: 状态管理方案 — 集中式 Store vs 分散式 composables

**选择**: 集中式 `useCircleStore`（Pinia）管理圈子详情、用户角色、成员状态；列表数据通过 `useTable` hook 分散管理。

**理由**: 圈子详情（currentCircle）、用户角色（currentRole）、成员状态（currentMemberStatus）需要跨页面共享（详情页 → 成员管理页），适合集中管理。列表数据（成员列表、治理日志）仅在各自页面内使用，用 `useTable` hook 管理更简洁。

**替代方案**: 全部用 composables — 跨页面共享数据需要 provide/inject 或全局变量，增加复杂度。

### D4: 加入按钮状态管理 — 前端判断 vs 后端驱动

**选择**: 后端驱动，前端根据详情接口返回的 `applyStatus`、`isInvited`、`memberCount`、`memberLimit` 字段动态渲染按钮状态。

**理由**: 申请状态（PENDING/APPROVED/REJECTED）、邀请状态、成员数都是后端权威数据，前端仅做展示判断，避免状态不一致。

**替代方案**: 前端维护加入状态 — 需要额外的本地持久化，且跨设备/浏览器状态不同步。

### D5: 搜索防抖策略

**选择**: 输入防抖 300ms + Enter 键立即触发。

**理由**: 300ms 防抖平衡了用户体验（不会太慢）和请求频率（减少无效请求）。Enter 键立即触发满足快速搜索需求。

### D6: 列表缓存策略

**选择**: 「已加入」Tab 和「发现」Tab 独立缓存，Tab 切换时保留数据（keep-alive），5 分钟过期自动刷新。加入/退出操作后同时刷新两个 Tab。

**理由**: 两个 Tab 数据来源不同（`my-list` vs `public-list`），独立缓存避免数据混淆。5 分钟过期保证数据新鲜度。操作后双 Tab 刷新保证一致性。

### D7: 图片上传裁剪方案

**选择**: 前端裁剪后上传，使用现有 Cropper 组件。图标 1:1、封面图 16:9。

**理由**: 复用现有 Cropper 组件，减少开发量。前端裁剪确保上传的图片比例一致，后端无需处理裁剪逻辑。

### D8: 密码强度指示器实现

**选择**: 前端实时计算密码强度（纯数字 = 弱；字母+数字 = 中；字母+数字+特殊字符或长度 >= 12 = 强），以进度条+文字形式展示。

**理由**: 纯前端计算无网络延迟，用户体验好。规则简单明确，不需要后端参与。

### D9: 敏感词降级策略

**选择**: 敏感词服务不可用时不阻断创建，采用「降级放行 + 后端标记待复核」策略。前端无感知，创建流程正常完成。

**理由**: 保证核心流程（创建圈子）的可用性，不因子服务故障阻断用户操作。后端标记待复核状态，运营在管理后台审核。

### D10: 响应式布局方案

**选择**: CSS 媒体查询 + 组件内条件渲染。桌面端双列网格，移动端单列列表。Table 在移动端转为卡片列表。

**理由**: 与项目现有响应式方案一致，不引入额外的响应式框架。组件内通过 `useBreakpoint` 或媒体查询切换布局。

### D11: 前端 API 路径与后端对齐

**选择**: 前端 API 封装层 (`src/api/content/circle.ts`) 直接使用后端实际路径，不做路径转换。

**后端实际路径**（来自 CircleController / CircleMemberController / CircleSearchController）:

| 功能 | 后端实际路径 | 前端封装名称 |
|------|------------|------------|
| 创建圈子 | POST `/content/circle/create` | `createCircle` |
| 更新圈子 | PUT `/content/circle/update` | `updateCircle` |
| 加入圈子 | POST `/content/circle/join` | `joinCircle` |
| 退出圈子 | POST `/content/circle/leave` | `leaveCircle` |
| 变更角色 | POST `/content/circle/member/change-role` | `changeRole` |
| 禁言 | POST `/content/circle/member/mute` | `muteMember` |
| 解除禁言 | POST `/content/circle/member/unmute` | `unmuteMember` |
| 移除成员 | POST `/content/circle/member/remove` | `removeMember` |
| 搜索 | GET `/content/circle/search` | `searchCircle` |

**理由**: 直接对齐后端路径避免中间转换层的维护成本和 404 风险。join 和 leave 在 CircleController 中（`/content/circle/` 前缀），不在 CircleMemberController 中（`/content/circle/member/` 前缀），因为加入/退出是圈子级操作而非纯成员管理操作。

**注意点**:
- 加入接口路径：`/content/circle/join`（非 `/content/circle/member/join`）
- 退出接口：`/content/circle/leave`（非 `/content/circle/member/quit`），封装名称用 `leaveCircle` 而非 `quitCircle`
- 角色变更：`/content/circle/member/change-role`（非 `/content/circle/member/set-moderator`）

### D12: 分页参数与响应格式

**选择**: 统一使用 `pageNum`/`pageSize` 作为分页请求参数，与后端 `CircleSearchReq` 保持一致。

**分页请求参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `pageNum` | `number` | 1 | 页码，从 1 开始 |
| `pageSize` | `number` | 20 | 每页条数 |

**分页响应格式**（与 JeecgBoot 标准一致）:

```typescript
interface PageResult<T> {
  records: T[];    // 数据列表
  total: number;   // 总条数
  pages: number;   // 总页数
  pageNum: number; // 当前页码
  pageSize: number; // 每页条数
}
```

**理由**: 后端 `CircleSearchReq` 已使用 `pageNum`/`pageSize`，前端统一避免参数映射。PRD 搜索接口中使用的 `page`/`size` 已在 D11 对齐时统一为 `pageNum`/`pageSize`。

## Risks / Trade-offs

**[R1] 后端接口未就绪** → 前端先基于接口文档定义 TypeScript 类型和 Mock 数据开发，接口就绪后切换真实调用。API 层独立封装，切换成本低。当前后端已实现 8/14 个接口，缺失的 6 个接口（detail/my-list/public-list/check-name/member-list/governance-log）需后端优先补充 P0 级接口（detail/my-list/public-list）。

**[R8] 后端 CircleVO 缺少前端依赖字段** → `applyStatus`（申请状态）和 `isInvited`（邀请状态）字段在当前 CircleVO 中不存在，但前端加入按钮状态逻辑完全依赖这两个字段。需后端在详情接口中补充，或前端采用降级方案（默认非申请中、非受邀）。

**[R2] 敏感词服务可用性** → 降级放行策略已覆盖，前端无需特殊处理。后端负责标记待复核状态。

**[R3] 并发操作竞态** → 前端通过 Toast 提示「操作失败，该成员状态已变更」并自动刷新列表。后端保证接口幂等性。

**[R4] 移动端 Table 体验** → 成员列表和治理日志在移动端转为卡片列表/时间线，增加前端开发量。可通过响应式组件封装复用逻辑。

**[R5] 虚拟滚动性能** → 圈子列表超过 100 条时启用虚拟滚动，需要处理卡片高度自适应和滚动位置恢复。

**[R6] 图片上传失败** → 前端展示重试按钮，支持断点续传（如有）。上传进度条展示上传状态。

**[R7] 密码错误次数限制** → 前端在收到「密码错误次数过多」错误码时禁用密码输入框，需处理倒计时恢复（如后端返回重试时间）。

## Open Questions

1. **圈子分类标签枚举**: 后端是否提供分类标签枚举接口？还是前端硬编码预定义列表？
2. **成员上限配置**: PRD 提到默认 500 人可由后端配置覆盖，前端是否需要展示配置入口？
3. **治理日志分页**: 后端是否支持游标分页？PRD 提到游标分页避免偏移量不一致。
4. **图片上传接口**: 是否使用统一的文件上传接口？还是圈子模块有独立的上传接口？
5. **埋点方案**: 具体埋点 SDK 和上报接口是什么？需要在开发阶段确定。
6. **CircleVO 字段确认**: 后端详情接口是否会在 CircleVO 中补充 `applyStatus`（PENDING/APPROVED/REJECTED）和 `isInvited`（Boolean）字段？`maxMemberCount` 是否等同于 PRD 中的 `memberLimit`？
7. **缺失接口排期**: 6 个缺失后端接口（detail/my-list/public-list/check-name/member-list/governance-log）的开发排期？前端将基于 Mock 数据先行开发。
