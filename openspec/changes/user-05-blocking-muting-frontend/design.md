## Context

后端 EPIC-05 拉黑/屏蔽能力已全部实现（change: user-05-blocking-muting），包含拉黑、屏蔽、屏蔽规则、不感兴趣反馈的 API 和数据模型。前端需要补齐 UI 层。

现有前端项目使用 Vue 3 + TypeScript + Vite + Ant Design Vue 4 + Pinia。内容社区模块位于 `src/views/content/`，API 层在 `src/api/content/`，Store 在 `src/store/modules/`。

现有模式：
- API：`enum Api` 定义 URL 常量 + 独立函数包装 `defHttp.get/post/delete`
- Store：`defineStore({ id, state, getters, actions })`，state 为返回默认值的函数
- 确认弹窗：`Modal.confirm`（简单场景）或声明式 `<a-modal v-model:open>`（复杂场景）
- 组件：`<script setup lang="ts">` + `<style scoped>`，使用 `reactive`/`ref`/`onMounted`

## Goals / Non-Goals

**Goals:**

- 在用户主页、内容卡片、评论区提供拉黑/屏蔽操作入口
- 提供清晰的确认弹窗，传达"拉黑是双向切断、屏蔽是单向降噪"的行为边界
- 实现黑名单、屏蔽列表、屏蔽词管理页面
- 实现不感兴趣反馈及后续内容类型/话题屏蔽
- 实现被拉黑状态占位页和屏蔽词命中折叠展示
- 使用 Pinia Store 缓存关系状态，减少重复请求
- 遵循项目既有 API/Store/组件模式，不引入新依赖

**Non-Goals:**

- 不实现内容审核系统或自动风控
- 不重构推荐算法，仅对接后端已有的过滤接口
- 不实现埋点（后续迭代补充）
- 不实现私信模块，仅对接拉黑拦截逻辑
- 不修改后端 API

## Decisions

### 1. 确认弹窗使用声明式 `<a-modal>`

PRD 要求三种确认弹窗（拉黑、屏蔽、解除拉黑），每种文案和按钮样式不同。采用声明式 `<a-modal v-model:open>` + `:confirm-loading` 模式，与项目 `cancellation/index.vue` 一致。

理由：声明式弹窗模板可见、状态清晰，适合需要自定义文案和按钮样式的场景。`useModal` 适合跨组件复用的全局弹窗，但拉黑/屏蔽弹窗仅在 BlockMuteMenu 内使用，不需要全局注册。

替代方案：`Modal.confirm` 命令式调用。放弃原因是难以自定义按钮样式（danger 红色按钮）。

### 2. 操作入口采用 Dropdown 菜单组件

用户主页、内容卡片和评论区的拉黑/屏蔽入口统一使用 `BlockMuteMenu` 组件，基于 Ant Design Vue `a-dropdown` 实现。组件接收 `targetUserId` 和 `placement` props，根据场景渲染不同菜单项。

理由：三个入口的菜单项高度相似（拉黑、屏蔽），统一组件减少重复代码。`a-dropdown` 是项目标准下拉菜单组件。

替代方案：每个入口独立实现菜单。放弃原因是文案和交互逻辑重复，维护成本高。

### 3. API 封装按领域拆分三个文件

- `src/api/content/block.ts`：拉黑相关
  - `POST /api/v1/content/user/relation/block` — 拉黑
  - `POST /api/v1/content/user/relation/unblock` — 解除拉黑
  - `GET /api/v1/content/user/relation/blacklist` — 黑名单分页
  - `GET /api/v1/content/user/relation/detail` — 查询关系状态
  - `GET /api/v1/content/user/relation/block-mute/help` — 帮助说明
- `src/api/content/mute.ts`：屏蔽相关
  - `POST /api/v1/content/user/relation/mute` — 屏蔽
  - `POST /api/v1/content/user/relation/mute/cancel` — 解除屏蔽
  - `GET /api/v1/content/user/relation/mute-list` — 屏蔽列表分页
- `src/api/content/filterRule.ts`：屏蔽规则相关
  - `POST /api/v1/content/user/filter-rule` — 添加规则
  - `POST /api/v1/content/user/filter-rule/delete` — 删除规则
  - `POST /api/v1/content/user/filter-rule/batch-delete` — 批量删除
  - `GET /api/v1/content/user/filter-rule/list` — 规则列表
  - `POST /api/v1/content/user/not-interested` — 不感兴趣反馈

参数风格：所有写操作使用 POST，参数通过 @RequestParam（查询参数）传递，与后端保持一致。

理由：与后端 API 领域对齐，单文件职责清晰。与项目 `settings.ts` 模式一致：`enum Api` + 独立函数。

替代方案：单文件 `blockMute.ts` 包含所有接口。放弃原因是函数过多（12+），不利于维护。

### 4. Pinia Store 仅缓存关系状态和计数

`blockMute.ts` Store 仅存储：
- `relationCache`: Record<string, { isBlocked, isMuted, isBlockedBy }> — 用户关系状态缓存
- `blacklistCount` / `muteListCount` — 隐私设置页角标

理由：列表数据（黑名单、屏蔽列表）在管理页面中通过 API 分页查询，无需全局缓存。关系状态在用户主页、内容卡片等多处使用，缓存可减少重复请求。

替代方案：缓存完整列表数据。放弃原因是列表数据量大、分页复杂，缓存一致性难维护。

### 5. 不感兴趣气泡使用 Popover 组件

`NotInterestedPopover` 使用 `a-popover` 实现，在内容卡片原位置底部弹出。气泡选项根据内容数据动态生成（category、topics 字段）。

理由：Popover 比 Dropdown 更适合展示多选项+说明文案的场景。项目已使用 Ant Design Vue 的 Popover。

替代方案：使用自定义浮层。放弃原因是增加实现复杂度，Popover 已满足需求。

### 6. 被拉黑状态页区分两种展示

- 拉黑发起方访问被拉黑用户主页：显示模糊占位图 + "您已拉黑该用户" + 前往黑名单管理按钮
- 被拉黑方访问拉黑发起方主页：显示标准 404 风格占位页 + "该用户不存在"

理由：PRD 明确要求被拉黑方不能通过页面差异推断出被拉黑状态。两种展示通过 `checkRelation` 接口的返回值区分。

### 7. 响应式设计使用 CSS 媒体查询

PC 端和移动端的布局差异（表格 vs 卡片列表、弹窗 vs 底部抽屉）通过 CSS 媒体查询 + 条件渲染实现。断点：< 768px 移动端，768-1024px 平板，> 1024px 桌面端。

理由：项目已有响应式设计基础，使用 CSS 媒体查询是标准做法。Ant Design Vue 的组件本身支持响应式。

## Risks / Trade-offs

- [mute-list 端点已补充] → `GET /api/v1/content/user/relation/mute-list` 已在 `ContentUserRelationController` 中实现，返回 `ContentUserMuteListPageVO`。
- [关系状态缓存不一致] → 拉黑/屏蔽操作成功后立即更新本地缓存，页面切换时通过 `checkRelation` 验证。用户登出时清空缓存。
- [屏蔽词正则前端校验] → 前端仅做基础格式校验（`/` 开头结尾），复杂校验依赖后端。无效正则时前端提示格式错误。
- [临时屏蔽倒计时精度] → 使用 Ant Design Vue `Statistic` 组件的 `countdown` 模式，精度到秒。到期后通过轮询或页面刷新更新列表。
- [移动端底部弹窗兼容性] → 确认弹窗在移动端使用 `a-drawer` 替代 `a-modal`，通过 `useBreakpoint` hook 判断。
- [列表批量操作性能] → 批量取消屏蔽逐个调用接口（后端无批量接口时）或使用 `batch-delete` 接口。超过 50 条时显示进度。

## Migration Plan

1. 新增 API 封装文件（block.ts、mute.ts、filterRule.ts）
2. 新增 Pinia Store（blockMute.ts）
3. 新增操作入口组件（BlockMuteMenu、BlockConfirmModal、MuteConfirmModal）
4. 新增不感兴趣气泡（NotInterestedPopover）和屏蔽词折叠卡片（FilteredContentCard）
5. 新增被拉黑状态页（BlockedUserPage）
6. 新增隐私设置聚合页和子管理页面
7. 修改现有内容卡片、用户主页、评论区组件，嵌入 BlockMuteMenu（**依赖**: 目标组件需由其他 change 提供，当前 5 个 task 因目标组件不存在而阻塞，详见 tasks.md）
   - **用户主页** (`FanProfile.vue`): 已存在，可嵌入 BlockMuteMenu
   - **内容卡片**: 尚未创建，需等待 `circle-10-core-frontend` 或 `circle-11-content-interaction-frontend` 提供
   - **评论区** (`CommentActions.vue`): 已存在，可嵌入 BlockMuteMenu
8. 路由注册隐私设置相关页面

**Rollback strategy:**

- 移除新增的 API、Store、组件文件
- 移除对现有组件的修改（恢复原始菜单项）
- 移除路由配置
- 后端 API 保持不变，不影响已有功能

## Open Questions

- 私信模块的拉黑拦截：前端是否已有私信页面需要嵌入拦截逻辑？
- 信息流中屏蔽用户内容的过滤：是前端过滤还是后端在接口中已过滤？（假设后端已过滤，前端仅处理不感兴趣后的乐观更新）
- 临时屏蔽的可选时长：PRD 提到"1/3/7/30 天"，需确认前端是否需要自定义时长输入
