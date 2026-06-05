## 1. 基础设施与 API 封装

- [x] 1.1 创建 `src/views/content/settings/api.ts`，封装全部 10 个 API 接口（getNotificationSettings / updateNotificationSettings / updateDndRules / getPrivacySettings / updatePrivacySettings / getSecuritySettings / updateSecuritySettings / getThirdPartyAuthList / getThirdPartyAuthDetail / revokeThirdPartyAuth）。同时验证 userId 注入拦截器支持 GET query 和 POST params
- [x] 1.2 创建 `src/views/content/settings/` 目录结构（notification / privacy / third-party / security 四个子目录）
- [x] 1.3 在内容社区路由配置中新增四个子路由（通知设置、隐私设置、第三方授权、账户安全），使用懒加载

## 2. 通知设置页

- [x] 2.1 创建 `NotificationSettings.vue` 页面骨架，包含 Page 容器 + 骨架屏加载状态
- [x] 2.2 实现通知类型列表：七行卡片式布局（图标 + 名称 + Switch 开关 + Checkbox 渠道组），支持主开关关闭时渠道置灰
- [x] 2.3 实现安全类通知行的"始终开启"标签和锁图标（disabled 状态）
- [x] 2.4 实现渠道配置 null 值默认逻辑（默认选中 App内 + 推送）
- [x] 2.5 实现免打扰规则列表：可增删的规则卡片（启用 Switch + 时间选择器 + 日期类型 Select + 摘要模式 Switch）
- [x] 2.6 实现免打扰新增时段按钮（默认 22:00-07:00），最多 5 条规则限制
- [x] 2.7 实现免打扰临时关闭按钮（倒计时逻辑 + temporaryDisableUntil 状态恢复）
- [x] 2.8 实现安全通知提示文案（免打扰区域底部）
- [x] 2.9 实现页面底部保存按钮：并发发送通知开关和免打扰规则请求（Promise.all），处理成功/失败/部分失败状态
- [x] 2.10 实现移动端响应式：通知类型列表从表格行转为卡片堆叠

## 3. 隐私设置页

- [x] 3.1 创建 `PrivacySettings.vue` 页面骨架，包含 Page 容器 + 骨架屏加载状态
- [x] 3.2 实现动态可见性设置：三项（浏览记录、点赞动态、收藏夹）各一个 Select 下拉，四级选项（公开/仅关注者/仅互关/仅自己）
- [x] 3.3 实现"仅自己可见"时的提示文案显示
- [x] 3.4 实现收藏夹字段名映射（后端 favoriteVisibility ↔ 前端 favoritesVisibility）
- [x] 3.5 实现在线状态可见性：Radio Group 横向三选项 + 选择时的提示文案
- [x] 3.6 实现搜索引擎索引控制：Switch + 说明文案 + 认证用户推荐标签
- [x] 3.7 实现 null 值默认逻辑（可见性默认 PUBLIC，搜索引擎默认关闭）
- [x] 3.8 实现保存按钮：调用 `POST /content/user/settings/privacy/update`，成功/失败提示
- [x] 3.9 实现移动端响应式：Radio 组纵向排列，Card 全宽堆叠

## 4. 第三方授权管理页

- [x] 4.1 创建 `ThirdPartyAuth.vue` 页面骨架，包含 Page 容器 + 骨架屏加载状态
- [x] 4.2 实现授权列表 Table：四列（应用名称、授权时间、授权范围 Tag、操作按钮）
- [x] 4.3 实现应用名称为 null 时显示"未知应用"，授权范围为空时显示"未知权限"红色 Tag
- [x] 4.4 实现查看详情 Modal：点击按钮调用详情接口，弹窗展示数据范围清单
- [x] 4.5 实现撤销授权流程：确认弹窗 → 调用 DELETE 接口 → 成功移除行 / 失败提示
- [x] 4.6 实现异常处理：授权不存在提示、越权 403 提示
- [x] 4.7 实现 authId 类型转换（路由参数 string → API 调用 number）
- [x] 4.8 实现空状态展示（无授权记录时的插图 + 文案）
- [x] 4.9 实现移动端响应式：Table 转为卡片列表，授权范围折叠

## 5. 账户安全设置页

- [x] 5.1 创建 `AccountSecurity.vue` 页面骨架，包含 Page 容器 + 骨架屏加载状态
- [x] 5.2 实现 2x2 网格卡片布局（设备管理、密码修改、两步验证、登录提醒）
- [x] 5.3 实现安全功能状态加载：从 GET /content/user/settings/security 获取 Boolean 状态，null 默认"已启用"
- [x] 5.4 实现设备管理/密码修改/两步验证卡片的点击跳转（链接到 EPIC-01 已有页面）
- [x] 5.5 实现登录提醒 Switch 开关：切换调用接口更新，失败时恢复原状态
- [x] 5.6 实现移动端响应式：卡片从 2x2 网格变为单列堆叠（1x4）

## 6. 集成与验证

- [x] 6.1 验证全局请求拦截器 userId 注入是否支持 GET query 和 POST params 场景，必要时补充
- [x] 6.2 全量功能自测：按 PRD 测试要点 F-01 至 F-12 逐项验证
- [x] 6.3 异常场景自测：按 PRD 异常测试 E-01 至 E-10 逐项验证
- [x] 6.4 响应式自测：PC（>=1024px）、平板（768-1023px）、移动端（<768px）三档布局验证
