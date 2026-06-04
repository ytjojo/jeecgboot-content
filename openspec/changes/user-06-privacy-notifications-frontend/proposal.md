## Why

当前内容社区缺少细粒度的隐私控制与通知管理能力，用户无法独立控制各类通知的开关与渠道，也无法管理个人动态可见性、在线状态和第三方授权。这导致用户隐私边界模糊、通知打扰过多，直接影响用户体验和平台信任度。需要在前端实现四个设置页面（通知设置、隐私设置、第三方授权、账户安全），为用户提供完整的隐私与通知管理入口。

## What Changes

- 新增通知设置页（NotificationSettings.vue）：七类通知独立开关 + 四渠道配置 + 免打扰多时段规则 + 临时关闭功能
- 新增隐私设置页（PrivacySettings.vue）：动态可见性四级控制 + 在线状态三级控制 + 搜索引擎索引开关
- 新增第三方授权管理页（ThirdPartyAuth.vue）：授权列表展示 + 详情查看 + 撤销授权
- 新增账户安全设置页（AccountSecurity.vue）：四个安全功能入口卡片 + 登录提醒开关
- 新增通知设置 API 封装（getNotificationSettings / updateNotificationSettings / updateDndRules）
- 新增隐私设置 API 封装（getPrivacySettings / updatePrivacySettings）
- 新增安全设置 API 封装（getSecuritySettings）
- 新增第三方授权 API 封装（getThirdPartyAuthList / getThirdPartyAuthDetail / revokeThirdPartyAuth）
- 新增左侧菜单路由配置，添加"设置"分区下的四个入口

## Capabilities

### New Capabilities
- `notification-settings`: 通知偏好设置能力，包括七类通知开关、四渠道配置、免打扰多时段规则管理
- `privacy-settings`: 隐私可见性设置能力，包括动态可见性四级控制、在线状态控制、搜索引擎索引
- `third-party-auth`: 第三方授权管理能力，包括授权列表、详情查看、撤销授权
- `account-security`: 账户安全设置能力，包括安全功能入口展示、登录提醒开关

### Modified Capabilities
<!-- 无现有 spec 需要修改 -->

## Impact

- **前端代码**: 新增 4 个页面组件 + 1 个 API 模块 + 路由配置变更，位于 `jeecgboot-vue3/src/views/content/` 下新增 `settings/` 目录
- **API 依赖**: 依赖后端 9 个接口（通知设置 3 个、隐私设置 2 个、安全设置 1 个、第三方授权 3 个），接口路径前缀 `/content/user/`。其中隐私设置 GET 端点和安全设置更新端点尚未实现，需后端补充（详见 `backend-issues.md`）
- **组件依赖**: 使用 Ant Design Vue 基础组件（Switch / Checkbox / TimePicker / Select / Radio / Table / Modal）+ 项目已有 Page / Form / Table / Modal 封装组件
- **路由变更**: 需要在内容社区模块的路由配置中新增 4 个子路由
- **无破坏性变更**: 纯新增功能，不影响已有页面和接口
