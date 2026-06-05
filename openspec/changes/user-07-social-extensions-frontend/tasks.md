## 1. 基础设施与依赖

- [x] 1.1 安装 ECharts 依赖（`echarts`），配置 `echarts/core` 按需引入工具函数
- [x] 1.2 创建 API 封装文件 `src/api/content/invite.ts`（邀请相关接口）
- [x] 1.3 创建 API 封装文件 `src/api/content/fan-analytics.ts`（粉丝分析接口）
- [x] 1.4 创建 API 封装文件 `src/api/content/governance.ts`（治理操作接口）
- [x] 1.5 创建 Pinia Store `src/store/modules/mutualFollow.ts`（互关状态缓存）
- [x] 1.6 创建 Pinia Store `src/store/modules/invite.ts`（邀请信息缓存）

## 2. 公共组件

- [x] 2.1 创建 `MutualFollowBadge.vue` 组件（互关标识，支持 small/default 尺寸和评论区内嵌模式）
- [x] 2.2 创建 `CommunityRoleBadge.vue` 组件（社区角色标签，支持 NORMAL/CREATOR/MODERATOR/ADMIN 角色，含 Popover 说明）
- [x] 2.3 创建 `ModeratorActionModal.vue` 组件（管理操作弹窗，支持 deleteComment/warnUser 两种操作）

## 3. 互关好友列表页

- [x] 3.1 创建互关好友列表页 `src/views/content/mutual-follow/index.vue`（页面结构、CardList 布局）
- [x] 3.2 实现互关好友列表分页加载（调用 mutual-follow-list API）
- [x] 3.3 实现搜索功能（300ms 防抖，按昵称/用户名搜索）
- [x] 3.4 实现取消互关操作（二次确认弹窗、调用 API、列表移除、提示消息）
- [x] 3.5 实现空状态、加载中骨架屏、搜索无结果、网络错误等边界状态
- [x] 3.6 配置互关好友列表路由 `/content/mutual-follow`

## 4. 粉丝管理页

- [x] 4.1 创建粉丝管理页 `src/views/content/fan/index.vue`（Tab 切换结构：列表/趋势）
- [x] 4.2 实现粉丝列表 Tab（统计概览 + 搜索 + Table 分页列表）
- [x] 4.3 实现粉丝趋势 Tab（ECharts 折线图，日/周/月维度切换）
- [x] 4.4 实现趋势数据点点击弹出 Modal（显示当日新增粉丝列表）
- [x] 4.5 实现空状态、图表加载中等边界状态
- [x] 4.6 配置粉丝管理页路由 `/content/fan`

## 5. 邀请分享页

- [x] 5.1 创建邀请分享页 `src/views/content/invite/index.vue`（邀请码展示区 + 统计概览 + 记录列表）
- [x] 5.2 实现邀请码获取/生成逻辑（首次自动生成，后续复用，调用 invite/code API）
- [x] 5.3 实现复制链接功能（剪贴板复制 + "已复制"状态切换 + 失败降级）
- [x] 5.4 实现邀请统计展示（CountTo 数字动画，调用 invite/stats API）
- [x] 5.5 实现邀请记录列表（Table 分页，调用 invite/records API）
- [x] 5.6 配置邀请分享页路由 `/content/invite`

## 6. 邀请落地页

- [x] 6.1 创建邀请落地页 `src/views/content/invite/LandingPage.vue`（独立布局，Banner + 价值展示 + 注册引导）
- [x] 6.2 实现邀请码校验逻辑（有效/无效/过期/名额已满的差异化展示）
- [x] 6.3 实现注册按钮跳转（携带 inviteCode 参数）
- [x] 6.4 实现已登录用户访问时重定向到首页
- [x] 6.5 实现移动端响应式布局（注册按钮固定底部）
- [x] 6.6 配置邀请落地页路由 `/invite/:inviteCode`

## 7. 私密内容发布与展示

- [x] 7.1 在内容发布表单中新增"可见性"下拉选项（公开/仅互关可见）
- [x] 7.2 实现 Feed 流中私密内容的可见性控制（互关可见 + "仅互关可见"标识）
- [x] 7.3 实现用户主页私密内容展示逻辑（互关用户包含私密内容，非互关用户完全隐藏）
- [x] 7.4 实现搜索结果中私密内容的可见性过滤
- [x] 7.5 实现私密内容交互限制（隐藏转发按钮、收藏后取关显示"内容已不可见"）
- [x] 7.6 实现取关后私密内容访问权限提示

## 8. 评论区角色标签与管理操作

- [x] 8.1 在评论区组件中集成 `CommunityRoleBadge` 组件（读取 comment.communityRole 字段）
- [x] 8.2 在评论区操作菜单中集成版主/管理员管理操作（删除评论、警告用户）
- [x] 8.3 实现管理员"前往用户管理"快捷跳转（跳转 EPIC-09 用户详情页）
- [x] 8.4 实现管理操作按钮的权限控制（usePermission Hook 控制显隐）

## 9. 审计日志页

- [x] 9.1 创建审计日志页 `src/views/system/audit-log/index.vue`（查询筛选区 + JVxeTable 日志列表）
- [x] 9.2 实现筛选功能（操作人搜索、操作类型下拉、时间范围选择、查询/重置）
- [x] 9.3 实现权限控制（仅管理员可访问，非管理员返回 403）
- [x] 9.4 配置审计日志页路由 `/system/audit-log`（归入系统管理菜单）

## 10. 埋点集成

- [x] 10.1 集成互关相关埋点事件（mutual_follow_badge_show/click、mutual_follow_cancel）
- [x] 10.2 集成粉丝管理埋点事件（fan_list_view、fan_trend_view、fan_trend_point_click）
- [x] 10.3 集成邀请相关埋点事件（invite_code_copy、invite_landing_page_view、invite_register_click/complete、invite_reward_trigger）
- [x] 10.4 集成角色标签埋点事件（community_role_badge_show/click）
- [x] 10.5 集成管理操作埋点事件（moderator_action_execute）
- [x] 10.6 集成私密内容埋点事件（private_content_publish、private_content_access_denied）
