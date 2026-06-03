# 验证报告：complete-profile-management-frontend

## 摘要

| 维度 | 状态 |
|---|---|
| 完整性 | **71/77 任务已实现（92%）**；6 个未勾选归档 `tasks.md`；4/5 能力规格核心需求已满足；2/5 规格显著不完整 |
| 正确性 | **API 契约 100% 匹配后端**；~30/39 规格场景已满足；9 个规格场景未实现（Modal/Drawer/drag/ActionSheet/presets）；测试覆盖 43 个用例 |
| 一致性 | **3 个设计决策违反**（D3 OSS、D4 拖拽、D5 徽章详情 Modal）；项目模式已遵循（Pinia、defHttp、Ant Design Vue 4、/@/ 别名） |

## 上下文

- 归档于 2026-06-03，合并至 `springboot3_content`（commit `38c4813b`）
- 后端变更 `complete-profile-management`（12 个端点）已更早合并
- 已实现 4 个页面 + 3 个组件 + 1 个枚举 + 1 个 API 模块 + 1 个 store 扩展 + 1 个共享验证器
- 43/43 单元测试通过

---

## CRITICAL 问题（重新归档前必须修复）

### C1. 归档 `tasks.md` 复选框未更新（规范性）
- **位置**：`openspec/changes/archive/2026-06-03-complete-profile-management-frontend/tasks.md:1-115`
- **证据**：任务 1-12 的 71 个 `- [ ]` 项未勾选；仅 6 个"实施回顾"项为 `- [x]`
- **现实**：实现实际已完成（通过文件存在 + 43 个通过测试 + 5 个 git 提交验证）
- **建议**：标记所有任务为 `[x]`，在归档后提交修正

### C2. 设计 D3 违反：AvatarCropper 使用 `uploadApi`，非 OSS 直传
- **位置**：`jeecgboot-vue3/src/views/content/profile/components/AvatarCropper.vue:21,63`
- **规格说明**（`design.md:50-58` D3，`proposal.md:14`）："OSS 客户端直传"，使用 STS 临时凭证；profile-editing 规格"使用 STS 临时凭证调用 OSS SDK 上传图片"
- **现实**：从 `/@/api/sys/upload` 导入 `uploadApi`（JeecgBoot 通用 `/sys/common/upload` 端点）并直接调用
- **风险**：D3 说明的理由"避免后端引入 OSS SDK 依赖"已达到，但规格明确要求的 OSS 直传 + STS 未实现
- **建议**：(a) 更新规格/设计以承认 `uploadApi` 作为 OSS 透传使用（成本低，反映实际系统设计），或 (b) 实现实际 OSS SDK + STS 流程

### C3. verification-badge 规格：Modal/Drawer 详情 UI 缺失（8.4）
- **位置**：`jeecgboot-vue3/src/views/content/profile/components/VerificationBadge.vue:1-40`
- **规格要求**（`verification-badge/spec.md:34-49`）：
  - "PC 端 Modal 弹窗（400px 宽）"
  - "移动端全屏 Drawer"
  - "Enterprise badge extra info" / "Influencer badge extra info"
  - Tooltip 从 `GET /content/user/profile/badge/detail?badgeId=Y` 获取详情
- **现实**：仅渲染 `a-tooltip` 的 `primary.tooltip`（静态标签"官方认证"）；无点击处理器，无 Modal/Drawer，无 `getBadgeDetail` 调用
- **API 差距**：`getBadgeDetail`/`restoreHistory` 已在 `src/api/content/profile/index.ts:73-77,87-91` 定义但从未从任何组件调用
- **建议**：在 `.verification-badge` 上添加 `@click`，调用 `getBadgeDetail(badge.badgeId)` 并打开 a-Modal/a-Drawer（通过 768px 断点响应式切换）。渲染 `description`、`verifiedAt`、徽章类型特定额外信息。

### C4. verification-badge 规格：展开 "+N" 交互缺失（8.5）
- **位置**：`VerificationBadge.vue`（仅渲染单个徽章）
- **规格要求**（`verification-badge/spec.md:55-65`）："最多 2 个 + '+N' 徽标"，"OFFICIAL > ENTERPRISE > CREATOR > INDIVIDUAL > REAL_NAME > MOBILE > EMAIL" 优先级，点击展开
- **现实**：`selectPrimaryBadge` 始终返回最高优先级的单个徽章；无 "+N" 指示器；无展开状态
- **已有辅助函数**：`badgeStyle.ts:134-150` 中的 `partitionBadges()` 已实现但从未在组件中使用
- **建议**：连接 `partitionBadges` 渲染 top-2 已知徽章 + "+(N-2)" 药丸，点击展开 popover 显示所有已知徽章

### C5. homepage-customization 规格：拖拽排序缺失（7.4）
- **位置**：`jeecgboot-vue3/src/views/content/profile/homepage-settings/index.vue:50-65,105-118`
- **规格要求**（`homepage-customization/spec.md:50-77` + 设计 D4）：
  - `vuedraggable` 依赖 + 拖拽排序
  - 移动端长按（300ms）触发拖拽模式
  - 键盘方向键重排 + Enter/Escape
- **现实**：使用 `a-button` 上下箭头 + `onMove(index, dir)` 函数；无 `vuedraggable` 导入；无长按处理器；无键盘处理器
- **建议**：添加 `vuedraggable@next` + 用拖拽手柄替换箭头按钮。或更新规格允许箭头按钮作为降级方案（D4 风险段已允许："必要时降级为上下箭头按钮排序"）

### C6. profile-editing 规格：PENDING 审核字段禁用缺失（4.5）
- **位置**：`jeecgboot-vue3/src/views/content/profile/edit/index.vue:35-37,130-145`
- **规格要求**（`profile-editing/spec.md:73-74`）："表单顶部显示黄色 Alert... 保存按钮禁用，字段不可编辑"
- **现实**：黄色 `a-alert` 已显示但所有表单字段仍可编辑；保存按钮在 `reviewStatus === 'PENDING'` 时无禁用绑定
- **建议**：为所有 `a-input`/`a-select`/`a-date-picker`/`a-textarea` 添加 `:disabled="reviewStatus === 'PENDING'"`（或使用 `a-form :disabled`）以及保存按钮

### C7. homepage-customization 规格：主题色预设/对比度缺失（6.2, 6.3）
- **位置**：`homepage-settings/index.vue:23-37`（仅文本输入）
- **规格要求**（`homepage-customization/spec.md:25-49`）：
  - 8-12 个预设色网格 + 选中标记
  - 自定义 `ColorPicker`
  - WCAG AA 对比度（≥4.5:1）检查 + 自动文字色调整
- **现实**：单个 `#RRGGBB` 文本输入；无预设；无对比度检查
- **建议**：添加预设色块网格 + `a-color-picker` 自定义；添加 `checkContrast(themeColor)` 辅助函数计算 WCAG 比率并在 <4.5 时翻转文字色

### C8. homepage-customization 规格：背景图 16:9 裁剪缺失（6.1）
- **位置**：`homepage-settings/index.vue:23-37`
- **规格要求**（`homepage-customization/spec.md:14-23`）："背景图上传 16:9 比例裁剪，复用 AvatarCropper"
- **现实**：`a-input` URL 字段用于 `homepageBackground`；无裁剪器；无 OSS 上传
- **建议**：添加 `BackgroundCropper` AvatarCropper 变体，16:9 宽高比，或更新规格允许仅粘贴 URL

### C9. privacy-settings 规格：移动端 ActionSheet 缺失（9.11）
- **位置**：`jeecgboot-vue3/src/views/content/profile/privacy/index.vue:1-100`（全部使用 `a-select`）
- **规格要求**（`privacy-settings/spec.md:69-77`）："移动端布局... Select 组件改为 ActionSheet 底部选择器"
- **现实**：所有视口使用 `a-select`；仅 `@media (max-width: 768px)` 单列布局
- **建议**：通过 `window.innerWidth < 768` 或响应式钩子为移动端添加 `a-action-sheet`（或 vben 的 `ActionSheet` 组件）

---

## WARNING 问题（应该修复）

### W1. history 规格：恢复确认对话框缺失（10.5）
- **位置**：`history/index.vue:46-49`（`onRestore` 直接调用 `restoreHistory`）
- **规格要求**（`profile-history/spec.md:20-32`）：弹出确认框
- **建议**：用 `Modal.confirm()` 包装 `onRestore`，显示值文本

### W2. history 规格：页脚文本"180 天"未显示（10.6）
- **位置**：`history/index.vue:7-50`（无页脚描述）
- **规格要求**：列表底部说明"最多保留 20 条记录，保留期限 180 天"
- **建议**：在 `a-list` 下方添加说明 div

### W3. profile-editing 规格：未来日期生日验证缺失（4.2）
- **位置**：`edit/index.vue:48-49`（`a-date-picker` 无 `disabled-date` 属性）
- **规格要求**（`profile-editing/spec.md:41-43`）："DatePicker 拒绝该选择并提示 '生日不能为未来日期'"
- **建议**：添加 `:disabled-date="(d) => d && d.isAfter(dayjs())"`

### W4. profile-editing 规格：取消时有修改确认缺失（4.3）
- **位置**：`edit/index.vue:155-159`（`onReset` 仅重置表单；页头 `@back` 直接 `$router.back()`）
- **规格要求**（`profile-editing/spec.md:14-20`）：isDirty 检查 + "您有未保存的修改，确定离开吗？"
- **建议**：添加 `onBack` 处理器，在 `isDirty()` 时使用 `Modal.confirm`

### W5. homepage-customization 规格："至少一个模块"验证缺失（7.6）
- **位置**：`homepage-settings/index.vue` 保存处理器未检查可见模块数
- **规格要求**："保存按钮禁用，提示 '至少需要保留一个模块'"
- **建议**：计算 `visibleCount`，为 0 时禁用模块保存按钮并显示提示文本

### W6. profile-editing 规格：APPROVED 通知未实现（4.5）
- **位置**：`edit/index.vue`（刷新后无通知触发）
- **规格要求**："系统通过 Notification 组件推送 '资料审核已通过'"
- **注**：这是事件驱动的（需要后端推送或轮询）；不在仅加载保存路径范围内
- **建议**：添加轮询或记录为 v1 范围外

### W7. homepage-customization 规格：PC 布局分栏 + 移动端预览 Drawer 缺失（6.4）
- **位置**：`homepage-settings/index.vue`（单列；无预览区域；无 Drawer）
- **规格要求**："PC 端右侧固定预览，移动端 Drawer 全屏预览"
- **建议**：添加预览面板 + 移动端"预览效果"按钮打开 `a-drawer`

### W8. homepage-customization 规格：长按 + 键盘重排缺失（7.4）
- 参见 C5（相关）。即使添加了拖拽，长按和键盘处理器也不在当前箭头按钮实现范围内

---

## SUGGESTION 问题（建议修复）

### S1. 代码模式偏差：单 API 文件 vs 按域分文件
- **位置**：`src/api/content/profile/index.ts`（单个 91 行文件）
- **tasks.md** 建议拆分：`api/profile.ts`、`api/homepage.ts`、`api/verification.ts`、`api/privacy.ts`、`api/profile-history.ts`
- **现实**：所有 11 个端点在一个文件中，为 `defHttp` 便利性选择单文件
- **建议**：更新 `tasks.md` 反映单文件决策，或拆分文件以满足原始任务结构

### S2. Store 集成测试跳过未记录
- **位置**：`tests/profile-store.spec.ts` 已删除
- **原因**：Jest jsdom 无法解析用户 store 的深层导入链
- **建议**：添加 `docs/agent-context/profile-testing.md` 说明集成测试差距和手动验证方法

### S3. 头像上传 accept 列表过宽
- **位置**：`AvatarCropper.vue:48`（`!file.type.startsWith('image/')` 拒绝 BMP 等）
- **规格要求**：显式 `JPG/PNG/WebP` 允许列表 + "仅支持 JPG、PNG、WebP 格式"消息
- **当前**：通用"请选择图片文件"
- **建议**：替换为显式 mime 允许列表 `['image/jpeg', 'image/png', 'image/webp']` 并更新错误消息

### S4. 后缀安全：`birthday` 字段 `value-format` 可能向后端传递无效日期
- **位置**：`edit/index.vue:48`
- **参见 W3**

### S5. 测试未覆盖 4 个页面的规格场景
- **位置**：`tests/profile*` 和 `tests/badgeStyle*`（43 个测试，全部在辅助函数/验证器中）
- **缺失**：无页面级状态机测试（PENDING → REJECTED、isDirty、恢复确认、保存并刷新缓存）
- **建议**：添加基于 vue-test-utils 的测试：隐私 dirty-check + 保存流程、历史恢复错误路径、主页模块重排持久化、编辑页 PENDING 守卫

---

## 最终评估

发现 **8 个 critical 问题 + 8 个 warning + 5 个 suggestion**。

**Critical 问题集中在实现简化的 UI 规格上**：
- VerificationBadge 有数据层（`partitionBadges`、`getBadgeDetail`）但缺少 Modal/Drawer + 展开交互
- 主页自定义有 API 调用但缺少拖拽排序、色板、对比度检查、背景裁剪
- 隐私页面功能完整，但移动端 ActionSheet 缺失
- 个人资料编辑显示审核状态 Alert 但未在 PENDING 期间锁定字段

**8 个 critical 问题全部在 UI/UX 侧，非数据/API 侧。** 后端契约已遵守（所有 12 个端点正确调用）；数据层测试良好（43 个测试）。简化权衡似乎是：快速发布可用基线，推迟打磨。

**建议**：
- (a) **修复 8 个 critical 问题** — 成本高但完全符合规格
- (b) **重新定义规格以承认基线** — 为缺失 UI 开启后续变更，更新归档规格以匹配现实
- (c) **混合方案** — 修复 C3/C4/C6/C9（最用户可见：徽章展开、PENDING 字段锁定、移动端 ActionSheet），为 C5/C7/C8 开启后续变更
