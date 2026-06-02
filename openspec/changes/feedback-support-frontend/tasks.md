## 1. 状态管理与 API 层

- [ ] 1.1 创建 `src/api/support/report.ts` -- 举报 API 封装（创建举报、撤回举报、查询列表、查询详情）
- [ ] 1.2 创建 `src/api/support/appeal.ts` -- 申诉 API 封装（创建申诉、撤回申诉、查询列表、查询详情）
- [ ] 1.3 创建 `src/api/support/help.ts` -- 帮助中心 API 封装（搜索文章、获取分类、获取文章详情、文章反馈）
- [ ] 1.4 创建 `src/api/support/changelog.ts` -- 更新日志 API 封装（获取版本列表）
- [ ] 1.5 创建 `src/api/support/customer-service.ts` -- 客服 API 封装（创建会话、转人工、发送消息、结束会话、提交评分、查询历史）
- [ ] 1.6 创建 `src/store/modules/feedback.ts` -- useFeedbackStore（举报列表、申诉列表、客服会话、消息列表、排队状态、统计数据）
- [ ] 1.7 编写 `src/store/modules/feedback.spec.ts` -- useFeedbackStore 单元测试

## 2. 举报系统

- [ ] 2.1 创建 `src/views/support/report/components/ReportModal.vue` -- 举报表单弹窗组件（类型选择、证据上传、防重复校验）
- [ ] 2.2 编写 `src/views/support/report/components/ReportModal.spec.ts` -- ReportModal 单元测试
- [ ] 2.3 创建 `src/views/support/report/components/ReportDetailDrawer.vue` -- 举报详情抽屉组件
- [ ] 2.4 创建 `src/views/support/report/index.vue` -- 我的举报列表页（表格、筛选、空状态）
- [ ] 2.5 创建 `src/views/support/report/route.ts` -- 举报路由配置

## 3. 申诉系统

- [ ] 3.1 创建 `src/views/support/appeal/components/AppealDetailDrawer.vue` -- 申诉详情抽屉组件
- [ ] 3.2 创建 `src/views/support/appeal/create.vue` -- 申诉提交页（表单、次数限制、确认弹窗）
- [ ] 3.3 编写 `src/views/support/appeal/create.spec.ts` -- 申诉提交页单元测试
- [ ] 3.4 创建 `src/views/support/appeal/index.vue` -- 我的申诉列表页（表格、筛选、空状态）
- [ ] 3.5 创建 `src/views/support/appeal/route.ts` -- 申诉路由配置

## 4. 帮助中心

- [ ] 4.1 创建 `src/views/support/help/components/HelpSearch.vue` -- 帮助中心搜索区组件（防抖、关键词高亮）
- [ ] 4.2 编写 `src/views/support/help/components/HelpSearch.spec.ts` -- HelpSearch 单元测试
- [ ] 4.3 创建 `src/views/support/help/components/ArticleFeedback.vue` -- 文章有用/无用反馈组件
- [ ] 4.4 编写 `src/views/support/help/components/ArticleFeedback.spec.ts` -- ArticleFeedback 单元测试
- [ ] 4.5 创建 `src/views/support/help/index.vue` -- 帮助中心首页（分类卡片、搜索、热门问题）
- [ ] 4.6 创建 `src/views/support/help/article.vue` -- 帮助文章详情页（Markdown 渲染、图片预览、反馈）
- [ ] 4.7 创建 `src/views/support/help/route.ts` -- 帮助中心路由配置

## 5. 更新日志

- [ ] 5.1 创建 `src/views/support/changelog/components/ChangelogTimeline.vue` -- 更新日志时间线组件
- [ ] 5.2 创建 `src/views/support/changelog/components/VersionCard.vue` -- 单个版本卡片组件
- [ ] 5.3 创建 `src/views/support/changelog/index.vue` -- 更新日志页（时间线、搜索、新版本提示）
- [ ] 5.4 创建 `src/views/support/changelog/route.ts` -- 更新日志路由配置

## 6. 客服通道

- [ ] 6.1 创建 `src/views/support/customer-service/components/ChatMessage.vue` -- 单条消息气泡组件（状态图标、重试）
- [ ] 6.2 编写 `src/views/support/customer-service/components/ChatMessage.spec.ts` -- ChatMessage 单元测试
- [ ] 6.3 创建 `src/views/support/customer-service/components/ChatPanel.vue` -- 客服对话面板组件（WebSocket、排队、断连处理）
- [ ] 6.4 编写 `src/views/support/customer-service/components/ChatPanel.spec.ts` -- ChatPanel 单元测试
- [ ] 6.5 创建 `src/views/support/customer-service/components/RatingModal.vue` -- 服务评分弹窗组件
- [ ] 6.6 编写 `src/views/support/customer-service/components/RatingModal.spec.ts` -- RatingModal 单元测试
- [ ] 6.7 创建 `src/views/support/customer-service/index.vue` -- 客服对话页（全屏/面板、移动端适配）
- [ ] 6.8 创建 `src/views/support/customer-service/history.vue` -- 客服历史记录页（表格、查看详情、继续咨询）
- [ ] 6.9 创建 `src/views/support/customer-service/route.ts` -- 客服路由配置

## 7. 路由注册与菜单配置

- [ ] 7.1 在主路由文件中引入所有 support 模块路由
- [ ] 7.2 在个人中心菜单中新增"我的举报"、"我的申诉"、"客服记录"菜单项
- [ ] 7.3 在顶部导航或页脚添加"帮助中心"和"更新日志"入口
- [ ] 7.4 添加客服入口悬浮按钮（所有页面可见）

## 8. 验证

- [ ] 8.1 运行全量单元测试，确保 100% 通过
- [ ] 8.2 验证所有页面路由可正常访问
- [ ] 8.3 验证举报流程：提交举报 -> 查看列表 -> 查看详情 -> 撤回
- [ ] 8.4 验证申诉流程：提交申诉 -> 查看列表 -> 查看详情 -> 撤回
- [ ] 8.5 验证帮助中心：分类浏览 -> 搜索 -> 文章详情 -> 反馈
- [ ] 8.6 验证更新日志：版本列表 -> 搜索 -> 新版本提示
- [ ] 8.7 验证客服通道：智能客服 -> 转人工 -> 实时对话 -> 评分 -> 历史记录
- [ ] 8.8 验证响应式布局（PC/平板/移动端）
- [ ] 8.9 验证性能指标：搜索 <500ms、首屏 <2s
