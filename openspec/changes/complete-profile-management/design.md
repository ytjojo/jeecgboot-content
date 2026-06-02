## Context

内容社区模块（jeecg-module-content）基于 JeecgBoot Vue3 前端框架，使用 Vue 3 + TypeScript + Ant Design Vue 4 + Vben Admin 架构。当前社区功能缺少个人资料管理、主页个性化、隐私控制等能力。本次变更需要在前端实现完整的个人资料与主页个性化功能，对接后端 RESTful API。

现有基础设施：
- 路由系统：基于 vue-router，路由配置在 `src/router/` 下
- 状态管理：Pinia，`src/store/modules/user.ts` 已有 useUserStore
- HTTP 封装：`defHttp` from `src/utils/http/axios`
- 组件库：Ant Design Vue 4（自动导入），Vben Admin 内置组件
- 响应式：项目已有断点体系（xs/sm/md/lg/xl）

## Goals / Non-Goals

**Goals:**
- 实现完整的个人资料编辑流程（表单 + 校验 + 频率限制 + 审核状态）
- 实现头像上传裁剪组件，支持跨端复用
- 实现主页个性化设置（背景图、主题色、模块配置）
- 实现认证标识展示组件，支持多认证折叠
- 实现隐私设置页面，支持字段级可见性控制和批量操作
- 实现昵称/头像历史记录查看与恢复
- 所有页面适配 PC/平板/移动端

**Non-Goals:**
- 不实现认证申请流程（仅展示后端返回的认证数据）
- 不实现图片压缩和多分辨率生成（由后端/CDN 处理）
- 不实现缓存失效逻辑（由后端 Redis 处理）
- 不实现敏感词过滤（由后端处理，前端仅展示错误）
- 不实现独立支付结算、企业通讯录、生物识别等功能

## Decisions

### D1: 页面路由组织方式

**决策**: 在内容社区模块路由下新增 `/content/profile/` 前缀的子路由，包含 edit、homepage-settings、privacy、history 四个页面。

**理由**: 保持与现有社区模块路由结构一致，避免路由层级过深。个人资料属于用户域，但入口在社区模块内，放在 content 路由下更符合用户心智模型。

**替代方案**: 放在 `/user/` 路由下 → 但用户模块是 JeecgBoot 系统级模块，社区资料是业务扩展，混入系统路由会造成职责不清。

### D2: 状态管理策略

**决策**: 扩展现有 useUserStore 新增 profile 相关状态字段；页面级表单状态（formData、isDirty）使用组件内部 ref 管理，不提升到全局 Store。

**理由**: profileCompletionRate、reviewStatus 等需要跨页面共享（个人中心和编辑页都要用），适合放 Store。表单数据生命周期仅限于编辑页面，放组件内更简洁，避免 Store 膨胀。

**替代方案**: 新建独立 useProfileStore → 增加 Store 数量和维护成本，且 profile 数据与 user 数据高度耦合，拆分后需要频繁跨 Store 同步。

### D3: 头像裁剪方案

**决策**: 封装独立的 `AvatarCropper` 组件，内部使用 `cropperjs` 库实现裁剪，通过 Modal 弹窗承载。

**理由**: cropperjs 是成熟稳定的裁剪库，支持触摸手势、缩放、旋转，移动端体验好。封装为组件后可在头像上传和背景图上传（16:9 比例）场景复用。

**替代方案**: 使用 Ant Design Vue Upload 的裁剪功能 → 不支持自定义裁剪比例和预览，功能不足。

### D4: 模块拖拽排序方案

**决策**: 使用 `vuedraggable`（基于 Sortable.js）实现拖拽排序，移动端通过长按触发拖拽模式。

**理由**: vuedraggable 是 Vue 3 生态最成熟的拖拽排序库，API 简洁，与 Vue 响应式系统集成良好。Sortable.js 底层支持触摸事件，移动端兼容性好。

**替代方案**: 纯 CSS + JS 手动实现 → 开发成本高，触摸兼容性难以保证。`@dnd-kit/vue` → 生态尚不成熟，Vue 3 支持不如 vuedraggable。

### D5: 认证标识组件设计

**决策**: 封装 `VerificationBadge` 组件，Props 接收 `badges: BadgeItem[]` 数组，内部处理排序、折叠（最多显示 2 个 + "+N" 徽标）、Tooltip 和详情弹窗。

**理由**: 认证标识需要在多个位置复用（个人主页、评论区、消息列表），封装为组件可统一交互逻辑和样式。折叠策略内聚在组件内部，调用方无需关心展示逻辑。

### D6: 隐私设置即时生效方案

**决策**: 隐私设置保存成功后，前端主动调用 `/content/user/profile/current` 刷新本地缓存的用户资料数据。不实现前端缓存失效逻辑。

**理由**: 缓存一致性由后端 Redis TTL 控制（5 分钟），前端只需在用户主动修改后立即刷新本地数据，保证修改者自己看到最新效果。其他用户的缓存延迟是可接受的。

### D7: 响应式弹窗策略

**决策**: PC 端使用 Modal 弹窗，移动端使用 Drawer 底部抽屉。通过 `useBreakpoint` hook 检测屏幕尺寸动态切换。

**理由**: 移动端 Modal 弹窗体验差（遮挡过多屏幕空间），Drawer 从底部弹出更符合移动端交互习惯。复用项目已有的 `useBreakpoint` hook 可保持一致性。

## Risks / Trade-offs

**[Risk] vuedraggable 移动端拖拽体验不一致** → 通过长按触发拖拽模式 + 触摸热区 44px + 振动反馈优化体验，必要时降级为上下箭头按钮排序。

**[Risk] 头像裁剪在低端移动设备上性能问题** → cropperjs 使用 Canvas 渲染，大图可能导致卡顿。缓解：上传前客户端压缩图片至 2000px 以内。

**[Risk] 隐私设置批量操作可能导致误操作** → 通过确认弹窗 + 5 秒撤销窗口双重保护，降低误操作风险。

**[Risk] 认证标识过多导致昵称区域拥挤** → 通过折叠策略（最多显示 2 个 + "+N"）控制视觉密度，平衡信息展示和界面整洁。

**[Trade-off] 主题色对比度校验使用 Web Worker** → 增加了 Worker 文件维护成本，但避免了色板选择时的主线程阻塞，保证交互流畅性。

**[Trade-off] 历史记录不缓存，每次进入重新加载** → 增加了接口调用次数，但保证数据实时性，且历史记录访问频率低，可接受。
