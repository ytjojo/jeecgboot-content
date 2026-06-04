## 1. 路由与页面骨架

- [ ] 1.1 在内容社区模块路由下新增 `/content/profile/edit`、`/content/profile/homepage-settings`、`/content/profile/privacy`、`/content/profile/history` 四个路由配置
- [ ] 1.2 创建编辑资料页骨架组件 `views/profile/edit/index.vue`，包含顶部导航栏、头像区域、表单区域、底部操作栏
- [ ] 1.3 创建主页设置页骨架组件 `views/profile/homepage-settings/index.vue`，包含背景图设置区、主题色设置区、预览区
- [ ] 1.4 创建隐私设置页骨架组件 `views/profile/privacy/index.vue`，按 7 个分组折叠展示 15 个 `*Visibility` 字段 + 2 个 Boolean
- [ ] 1.5 创建历史记录页骨架组件 `views/profile/history/index.vue`，包含 Tabs 切换（昵称/头像）、当前值展示区、历史列表

## 2. API 封装与类型定义

- [ ] 2.1 创建 `api/profile.ts`，封装资料管理接口（`detail`、`update`、`review/handle` 不对接）
  - `update` 接口返回类型：`Result<ContentUserProfileVO>`（需后端改造）
- [ ] 2.2 创建 `api/homepage.ts`，封装主页设置接口（`homepage/update`、`homepage/defaults/restore`、`homepage/modules`）
  - `homepage/update` 返回类型：`Result<ContentUserProfileVO>`（需后端改造）
  - `homepage/defaults/restore` 返回类型：`Result<ContentUserProfileVO>`（需后端改造）
- [ ] 2.3 创建 `api/verification.ts`，封装认证标识接口（`badge/list`、`badge/detail`）
- [ ] 2.4 创建 `api/privacy.ts`，封装隐私设置接口（`privacy/update`）
  - `privacy/update` 返回类型：`Result<String>`（保持现状，无需改造）
- [ ] 2.5 创建 `api/profile-history.ts`，封装历史记录接口（`history/list?historyType=NICKNAME|AVATAR`、`history/restore`）
  - `history/restore` 返回类型：`Result<ContentUserProfileVO>`（需后端改造）
- [ ] 2.6 定义 TypeScript 类型：`ContentUserProfileVO`、`ContentUserProfileUpdateReq`、`ContentUserHomepageUpdateReq`、`ContentUserHomepageModuleVO`、`ContentUserPrivacyUpdateReq`、`ContentUserVerificationBadgeVO`、`ContentUserProfileHistoryVO`
- [ ] 2.7 定义 `PrivacyVisibility` 枚举类型：`PUBLIC | FOLLOWERS_ONLY | MUTUAL_ONLY | PRIVATE`
- [ ] 2.8 定义 `OnlineStatusVisibility` 枚举类型：`PUBLIC | HIDDEN | MUTUAL_ONLY`（用于 `onlineStatusVisibility` 字段）

## 3. 状态管理扩展

- [ ] 3.1 扩展 `useUserStore`，新增 `profileCompletionRate`、`reviewStatus`、`reviewReason`、`lastUpdatedAt` 状态字段
- [ ] 3.2 实现 useUserStore 中资料数据的刷新方法（隐私设置保存后调用 `GET /content/user/profile/detail`）

## 4. 编辑资料页实现

- [ ] 4.1 实现编辑资料页表单，使用 Ant Design Vue Form 组件，包含 nickname、avatar、bio、gender、birthday、region、profession、personalLink 字段
- [ ] 4.2 实现表单校验逻辑：nickname 必填（≤30 字符）、bio 字数统计（≤500）、URL 格式校验（`^https?://.*$`）、birthday 禁用未来日期
- [ ] 4.3 实现保存/取消按钮行为：返回时 isDirty 检查
- [ ] 4.4 实现 OSS 客户端直传：选文件 → 客户端校验（格式 + 大小） → cropperjs 裁剪 → 上传 → 回填 CDN URL → 调用 `POST /profile/update`（后端改造后返回 `ContentUserProfileVO`，直接更新本地状态）
- [ ] 4.5 实现审核状态交互：待审核时黄色 Alert + 字段不可编辑，审核不通过时红色 Alert + 重新编辑按钮（基于 `ContentUserProfileVO.profileReviewStatus`）
- [ ] 4.6 实现加载中骨架屏、网络错误提示、防重复提交

## 5. 头像/背景图裁剪组件

- [ ] 5.1 创建 `AvatarCropper` 组件，集成 `cropperjs`，头像 1:1 比例、背景图 16:9 比例
- [ ] 5.2 实现文件校验：格式（JPG/PNG/WebP）、大小（≤5MB）、非空
- [ ] 5.3 实现裁剪弹窗 UI：图片预览区、缩放控制条、底部操作按钮
- [ ] 5.4 实现 OSS 上传流程：使用 STS 临时凭证，禁止硬编码 AccessKey
- [ ] 5.5 实现移动端适配：全屏展示裁剪区域

## 6. 主页设置页实现

- [ ] 6.1 实现背景图上传，复用 AvatarCropper 组件（16:9 比例），支持更换背景和恢复默认
- [ ] 6.2 实现主题色选择：预设色板（8-12 个颜色）网格排列 + 自定义颜色输入
- [ ] 6.3 实现主题色对比度校验：WCAG AA 标准自动校验，对比度不足时警告
- [ ] 6.4 实时预览区：PC 端右侧固定预览，移动端 Drawer 全屏预览
- [ ] 6.5 实现保存（`POST /content/user/profile/homepage/update`，后端改造后返回 `ContentUserProfileVO`）和恢复默认（`POST /content/user/profile/homepage/defaults/restore`，后端改造后返回 `ContentUserProfileVO`）功能，直接使用返回值更新本地状态

## 7. 主页模块配置实现

- [ ] 7.1 安装 `vuedraggable` 依赖，创建模块排序列表组件
- [ ] 7.2 模块列表数据源：`GET /content/user/profile/homepage/modules?userId=X` 读取 `List<ContentUserHomepageModuleVO>`
- [ ] 7.3 实现模块显隐 Switch 开关
- [ ] 7.4 实现拖拽排序，PC 端拖拽手柄，移动端长按（300ms）触发拖拽模式
- [ ] 7.5 实现保存（提交到 `POST /content/user/profile/update` 的 `moduleOrderJson` 字段）
- [ ] 7.6 实现"至少保留一个模块"校验、恢复默认排序

## 8. 认证标识组件实现

- [ ] 8.1 创建 `VerificationBadge` 组件，Props 接收 `badges: ContentUserVerificationBadgeVO[]`
- [ ] 8.2 维护 `visualStyleKey` 字典：`INDIVIDUAL`（蓝对勾）、`ENTERPRISE`（金徽章）、`CREATOR`（紫星）、`OFFICIAL`（红盾）、`REAL_NAME`（灰盾）、`MOBILE`（绿手机）、`EMAIL`（绿邮箱）
- [ ] 8.3 实现 `DEFAULT` 兜底样式（灰对勾）覆盖未知 key
- [ ] 8.4 实现认证详情弹窗：PC 端 Modal（400px），移动端全屏 Drawer
- [ ] 8.5 实现折叠交互：最多 2 个 + "+N" 徽标，按 OFFICIAL > ENTERPRISE > CREATOR > INDIVIDUAL > REAL_NAME > MOBILE > EMAIL 优先级排序
- [ ] 8.6 列表数据源：`GET /content/user/profile/badge/list?userId=X`；详情：`GET /content/user/profile/badge/detail?badgeId=Y`

## 9. 隐私设置页实现

- [ ] 9.1 覆盖 15 个 `*Visibility` 字段：按 D7 分组（基础资料/扩展/主页/认证/活动/在线状态/布尔开关）
- [ ] 9.2 基础资料组 (5)：`bioVisibility` / `genderVisibility` / `birthdayVisibility` / `regionVisibility` / `professionVisibility`
- [ ] 9.3 扩展资料组 (1)：`personalLinkVisibility`
- [ ] 9.4 主页组 (3)：`homepageBackgroundVisibility` / `themeColorVisibility` / `homepageModuleVisibility`
- [ ] 9.5 认证组 (2)：`certificationVisibility` / `verificationBadgesVisibility`
- [ ] 9.6 活动组 (3)：`profileCompletionVisibility` / `profileReviewStatusVisibility` / `recentActivityVisibility`
- [ ] 9.7 在线状态组 (1)：`onlineStatusVisibility` ← 特殊枚举 `PUBLIC|HIDDEN|MUTUAL_ONLY`
- [ ] 9.8 布尔开关组 (2)：`showMutualFollowersCount` / `showRecentActivityHighlight`（Switch 组件）
- [ ] 9.9 调用 `POST /content/user/profile/privacy/update?userId=X` 提交
- [ ] 9.10 保存成功后主动调用 `GET /content/user/profile/detail?ownerUserId=X&viewerUserId=X` 刷新本地缓存
- [ ] 9.11 移动端 Select 改为 ActionSheet 底部选择器

## 10. 历史记录页实现

- [ ] 10.1 Tabs 组件：昵称历史 / 头像历史
- [ ] 10.2 切换 Tab 时调用 `GET /content/user/profile/history/list?userId=X&historyType=NICKNAME|AVATAR`
- [ ] 10.3 当前值展示区：当前昵称/头像 + 绿色"当前"标签
- [ ] 10.4 历史记录列表：倒序排列，每条显示历史值 + 修改时间 + "恢复"按钮
- [ ] 10.5 恢复操作：确认弹窗 → `POST /content/user/profile/history/restore?userId=X&historyId=Y`（后端改造后返回 `ContentUserProfileVO`） → 成功提示，直接使用返回值更新本地状态
- [ ] 10.6 列表底部说明"最多保留 20 条记录，保留期限 180 天"（由后端裁剪，前端展示说明文案）
- [ ] 10.7 空状态、加载中骨架屏、恢复中 loading 状态

## 11. 响应式适配与通用交互

- [ ] 11.1 实现所有页面的响应式断点适配（xs/sm/md/lg/xl），PC 端居中 640px，移动端全宽
- [ ] 11.2 实现通用交互：表单实时校验（防抖 300ms）、防重复提交、危险操作确认弹窗、成功消息提示
- [ ] 11.3 实现 iPhone Safe Area 适配：底部操作栏 padding-bottom: env(safe-area-inset-bottom)
- [ ] 11.4 实现认证标识弹窗的响应式切换：PC 端 Modal，移动端 Drawer

## 12. 测试与验证

- [ ] 12.1 编写编辑资料页单元测试：表单校验、OSS 上传、审核状态展示
- [ ] 12.2 编写头像裁剪组件单元测试：文件校验、裁剪参数、上传流程
- [ ] 12.3 编写认证标识组件单元测试：排序逻辑、折叠策略、详情弹窗、visualStyleKey 字典
- [ ] 12.4 编写隐私设置页单元测试：15 个 visibility 字段、onlineStatusVisibility 特殊枚举
- [ ] 12.5 编写历史记录页单元测试：historyType 切换、恢复操作
- [ ] 12.6 全量测试运行，确保 100% 通过

## 13. 实施回顾

- [x] 13.1 删除与后端 change 重复的 `specs/profile-management/spec.md`（后端 change 已持有该 spec）
- [x] 13.2 删除前端 change 中假设的、不存在的端点（独立 upload、update-count、隐私 settings update）
- [x] 13.3 调整为对接后端实际 12 个端点（`/content/user/profile/*`）
- [x] 13.4 隐私字段覆盖范围从 5 扩展到 15（含 `onlineStatusVisibility` 特殊枚举）
- [x] 13.5 历史记录从"两个分页接口"改为单接口 + `historyType` 参数
- [x] 13.6 头像/背景图从"复用后端上传"改为"OSS 客户端直传"
- [x] 13.7 确认 4 个 POST 端点需后端改造为返回 VO（`/update`、`/homepage/update`、`/homepage/defaults/restore`、`/history/restore`），`/privacy/update` 保持返回 `Result<String>`
- [x] 13.8 删除不存在的 `ContentUserPrivacySettingVO` 类型引用，隐私设置保存后返回 `Result<String>`
