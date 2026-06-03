## 1. API 封装层

- [ ] 1.1 创建 `src/api/content/block.ts`，封装以下接口（使用 `defHttp.post`/`defHttp.get` + 查询参数）：
  - `block(userId, targetUserId)` → `POST /content/user/relation/block`
  - `unblock(userId, targetUserId)` → `POST /content/user/relation/unblock`
  - `getBlacklist(userId, pageNo, pageSize)` → `GET /content/user/relation/blacklist`
  - `checkRelation(userId, targetUserId)` → `GET /content/user/relation/detail`
  - `getBlockMuteHelp()` → `GET /content/user/relation/block-mute/help`
- [ ] 1.2 创建 `src/api/content/mute.ts`，封装以下接口：
  - `mute(userId, targetUserId)` → `POST /content/user/relation/mute`
  - `unmute(userId, targetUserId)` → `POST /content/user/relation/mute/cancel`
  - `getMuteList(userId, pageNo, pageSize)` → `GET /content/user/relation/mute-list`
- [ ] 1.3 创建 `src/api/content/filterRule.ts`，封装以下接口：
  - `addRule(userId, ruleType, value, daysValid?)` → `POST /content/user/filter-rule`
  - `deleteRule(userId, ruleId)` → `POST /content/user/filter-rule/delete`
  - `batchDeleteRules(userId, ruleIds)` → `POST /content/user/filter-rule/batch-delete`
  - `getRuleList(userId, ruleType?)` → `GET /content/user/filter-rule/list`
  - `recordNotInterested(userId, contentId, contentType)` → `POST /content/user/not-interested`
- [ ] 1.4 添加 API 函数类型定义（请求参数、响应类型）

## 2. Pinia Store

- [ ] 2.1 创建 `src/store/modules/blockMute.ts`，定义 BlockMuteState 接口和 Store
- [ ] 2.2 实现 `checkRelation` action，查询并缓存关系状态
- [ ] 2.3 实现 `blockUser`/`unblockUser`/`muteUser`/`unmuteUser` action，操作成功后更新缓存
- [ ] 2.4 实现 `refreshCounts` action，刷新黑名单和屏蔽列表数量
- [ ] 2.5 实现 `clearRelationCache` action，支持登出时清空缓存

## 3. 操作入口组件

- [ ] 3.1 创建 `src/views/content/components/BlockMuteMenu.vue`，基于 a-dropdown 实现拉黑/屏蔽操作菜单
- [ ] 3.2 创建 `src/views/content/components/BlockConfirmModal.vue`，实现拉黑确认弹窗（红色 danger 按钮）
- [ ] 3.3 创建 `src/views/content/components/MuteConfirmModal.vue`，实现屏蔽确认弹窗
- [ ] 3.4 在 BlockMuteMenu 中集成 BlockConfirmModal 和 MuteConfirmModal
- [ ] 3.5 在用户主页组件中嵌入 BlockMuteMenu（更多操作下拉菜单）
- [ ] 3.6 在内容卡片组件中嵌入 BlockMuteMenu（··· 操作菜单）
- [ ] 3.7 在评论区组件中嵌入 BlockMuteMenu（··· 操作菜单）

## 4. 不感兴趣与屏蔽词折叠

- [ ] 4.1 创建 `src/views/content/components/NotInterestedPopover.vue`，实现不感兴趣反馈气泡
- [ ] 4.2 实现气泡选项动态生成逻辑（根据 category 和 topics 字段）
- [ ] 4.3 在内容卡片中集成 NotInterestedPopover，实现乐观更新（立即移除卡片）
- [ ] 4.4 创建 `src/views/content/components/FilteredContentCard.vue`，实现屏蔽词命中折叠卡片
- [ ] 4.5 实现展开/收起交互，标题被命中时显示 "***"

## 5. 被拉黑状态页面

- [ ] 5.1 创建 `src/views/content/components/BlockedUserPage.vue`，实现拉黑发起方看到的占位页（模糊占位图 + 前往黑名单管理按钮）
- [ ] 5.2 实现被拉黑方看到的 404 风格占位页（"该用户不存在"，与真实不存在用户页面一致）
- [ ] 5.3 在用户主页组件中集成 BlockedUserPage，根据 `checkRelation` 结果切换展示

## 6. 隐私设置页面

- [ ] 6.1 创建 `src/views/content/privacy/PrivacySettingsPage.vue`，实现隐私设置聚合页（三个入口卡片 + 帮助说明折叠面板）
- [ ] 6.2 实现入口卡片数量角标展示（黑名单数量、屏蔽列表合计数量）
- [ ] 6.3 创建 `src/views/content/privacy/BlacklistPage.vue`，实现黑名单管理页（搜索、分页列表、解除拉黑）
- [ ] 6.4 实现黑名单列表项展示（头像、昵称、拉黑时间、解除拉黑按钮）和空状态
- [ ] 6.5 创建 `src/views/content/privacy/MuteListPage.vue`，实现屏蔽列表管理页（四个 Tab）
- [ ] 6.6 实现屏蔽用户 Tab（用户列表 + 取消屏蔽）
- [ ] 6.7 实现屏蔽话题 Tab（话题列表 + 取消屏蔽）
- [ ] 6.8 实现屏蔽内容类型 Tab（类型列表 + 取消屏蔽）
- [ ] 6.9 实现临时屏蔽 Tab（倒计时展示 + 提前取消）
- [ ] 6.10 实现批量取消屏蔽功能（全选 + 批量确认弹窗）
- [ ] 6.11 创建 `src/views/content/privacy/KeywordFilterPage.vue`，实现屏蔽词设置页
- [ ] 6.12 实现屏蔽词输入区（关键词/正则添加、格式校验、重复检测、上限提示）
- [ ] 6.13 实现屏蔽词列表展示（类型标签、删除 + 3 秒撤销）

## 7. 响应式适配

- [ ] 7.1 隐私设置聚合页：PC 端两列网格，移动端单列堆叠
- [ ] 7.2 黑名单/屏蔽列表：PC 端表格（JVxeTable），移动端卡片列表
- [ ] 7.3 屏蔽词设置页：PC 端水平排列，移动端垂直堆叠
- [ ] 7.4 确认弹窗：PC 端居中 Modal（420px），移动端底部 Drawer 或全宽 Modal

## 8. 路由注册

- [ ] 8.1 注册隐私设置聚合页路由
- [ ] 8.2 注册黑名单管理页路由
- [ ] 8.3 注册屏蔽列表管理页路由
- [ ] 8.4 注册屏蔽词设置页路由
