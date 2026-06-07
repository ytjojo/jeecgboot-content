## 1. API 层与 Store 基础设施

- [ ] 1.1 创建 `src/api/circle/content.ts`：封装置顶/精华切换接口（`togglePin`、`toggleFeatured`）
- [ ] 1.2 创建 `src/api/circle/announcement.ts`：封装公告接口（`createAnnouncement`、`getActiveAnnouncement`、`deleteAnnouncement`）
- [ ] 1.3 创建 `src/api/circle/mention.ts`：封装 @成员查询接口（复用 `GET /api/v1/content/circle/member/list`，方法名 `getMentionableMembers`）
- [ ] 1.4 创建 `src/api/circle/joinRequest.ts`：封装申请审核接口（`getJoinRequestList`（`GET /circle-join-review/list`）、`getPendingJoinRequests`（`GET /circle-join-review/pending/{circleId}`）、`approveJoinRequest`、`rejectJoinRequest`）
- [ ] 1.5 创建 `src/api/circle/report.ts`：封装举报相关接口（`createReport`、`getReportList`、`deleteReportContent`、`ignoreReport`、`muteUser`）
- [ ] 1.6 创建 `src/store/modules/circleInteraction.ts`：实现 `useCircleInteractionStore`（公告状态、@成员列表、审核统计）

## 2. 内容置顶与精华

- [ ] 2.1 创建 `ContentActionMenu` 组件：根据角色权限动态展示置顶/精华/举报菜单项
- [ ] 2.2 在圈子内容列表页集成 `ContentActionMenu`，置顶内容排序逻辑（置顶在前，多条按 pinned_at 倒序）
- [ ] 2.3 在内容列表卡片中添加置顶标识和精华标识（金色徽章）展示
- [ ] 2.4 在内容详情页集成 `ContentActionMenu`，添加置顶/精华标识展示
- [ ] 2.5 实现置顶/精华操作后的即时列表更新（无需刷新页面）

## 3. 圈子公告

- [ ] 3.1 创建 `CircleAnnouncementBar` 组件：展示公告摘要、有效期、展开/收起功能
- [ ] 3.2 创建公告发布弹窗组件：Tinymce 富文本编辑器（`defineAsyncComponent` 按需加载） + 有效期选择器 + 表单校验
- [ ] 3.3 实现公告发布逻辑：已有公告时弹出替换确认框，发布成功后更新公告栏
- [ ] 3.4 实现公告删除逻辑：确认后调用 API，公告栏消失
- [ ] 3.5 在圈子内容列表页顶部集成 `CircleAnnouncementBar`，根据角色展示编辑/删除操作
- [ ] 3.6 实现公告过期自动隐藏（前端定时检查或依赖接口返回）

## 4. @成员功能

- [ ] 4.1 创建 `MentionMemberPicker` 组件：浮层展示成员列表、搜索过滤、键盘导航
- [ ] 4.2 实现 @触发逻辑：输入框监听 `@` 字符输入，弹出浮层
- [ ] 4.3 实现成员搜索：防抖 300ms，调用 `getMentionableMembers`（复用 `GET /api/v1/content/circle/member/list`）接口
- [ ] 4.4 实现提及标记插入：纯文本场景插入 `@{userId:xxx}昵称`，富文本场景插入 `<span class="mention">` 标签
- [ ] 4.5 实现 @提及内容解析渲染：正则匹配提及标记，渲染为可点击链接（跳转用户主页）
- [ ] 4.6 在内容发布框和评论输入框中集成 `MentionMemberPicker`

## 5. 加入申请审核

- [ ] 5.1 创建加入申请审核页面：Table + useTable，支持待处理/已处理/全部标签页切换
- [ ] 5.2 创建 `JoinRequestCard` 组件：展示申请信息、超时警告标识、操作按钮
- [ ] 5.3 实现批准申请逻辑：确认框 + 调用 API + 卡片移出列表 + Toast 反馈
- [ ] 5.4 实现拒绝申请逻辑：拒绝原因输入弹窗（必填） + 调用 API + 卡片移出列表
- [ ] 5.5 实现批量批准功能：勾选多条申请后批量调用批准 API
- [ ] 5.6 实现超时提醒：超过 3 天未处理的申请显示橙色警告标识
- [ ] 5.7 实现管理入口角标：调用 `GET /circle-join-review/pending/{circleId}` 获取待审核列表，角标数字取列表长度
- [ ] 5.8 实现移动端响应式：卡片列表 + 下拉筛选 + 底部固定操作栏

## 6. 内容举报处理

- [ ] 6.1 创建举报提交弹窗组件：举报原因单选 + 补充说明 + 表单校验
- [ ] 6.2 实现举报提交逻辑：调用 `createReport` API，成功后 Toast 反馈
- [ ] 6.3 实现重复举报检测：已举报内容的"举报"选项置灰 + Tooltip 提示
- [ ] 6.4 创建举报处理页面：Table + useTable，支持待处理/已处理/已忽略标签页切换
- [ ] 6.5 创建 `ReportCard` 组件：展示举报信息、操作按钮（查看内容/删除/忽略/禁言）
- [ ] 6.6 实现查看被举报内容详情：Drawer 抽屉展示
- [ ] 6.7 实现删除被举报内容逻辑：确认框 + 调用 API + 卡片状态更新
- [ ] 6.8 实现忽略举报逻辑：确认框 + 调用 API + 卡片状态更新
- [ ] 6.9 实现禁言用户逻辑：禁言时长选择弹窗（1小时/1天/7天/30天/永久） + 调用 API
- [ ] 6.10 实现权限控制：举报处理页入口仅对管理员可见，禁言操作仅创建者可用
- [ ] 6.11 实现移动端响应式：卡片列表 + 操作按钮收进"更多"菜单
