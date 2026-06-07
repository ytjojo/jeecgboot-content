## Why

内容社区当前缺少统一的个人资料管理入口，主页无法个性化定制，隐私控制粒度不足，认证标识与资料字段耦合。用户无法有效构建社区身份，资料完善率低，主页访问时长短。需要在前端实现完整的个人资料管理与主页个性化功能，对接后端 `ContentUserProfileController` 的 11 个接口（其中 `review/handle` 为后台审核端点，前端不对接）。

> **实施更新（2026-06-04）**: 本变更对接的后端 `ContentUserProfileController` 实际提供 11 个接口（`/api/v1/content/user/profile/*`），其中：
> - 头像/背景图素材 **不** 通过后端上传端点处理，由前端使用 OSS 客户端直传后回填 URL；
> - 统一资料更新端点 `POST /profile/update` 承载基础资料 + 主页配置字段；
> - 4 个 POST 端点（`/update`、`/homepage/update`、`/homepage/defaults/restore`、`/history/restore`）返回 `Result<ContentUserProfileVO>`，`/privacy/update` 返回 `Result<String>`（"更新成功"）；
> - 隐私接口覆盖 15 个 `*Visibility` 字段，`onlineStatusVisibility` 特殊枚举 `PUBLIC|HIDDEN|MUTUAL_ONLY`；
> - 历史记录通过 `GET /history/list?historyType=NICKNAME|AVATAR` 区分类型；
> - `review/handle` 为后台审核端点，前端不对接。

## What Changes

- 新增编辑资料页面，调用 `POST /api/v1/content/user/profile/update` 提交基础资料字段
- 新增 OSS 客户端直传后回填 URL 的头像/背景图流程（**不**走内容社区上传端点）
- 新增主页设置页面，调用 `POST /api/v1/content/user/profile/homepage/update` 与 `/api/v1/content/user/profile/homepage/defaults/restore`
- 新增主页模块配置，调用 `GET /api/v1/content/user/profile/homepage/modules` 读取、复用 `/update` 写入排序
- 新增认证标识展示组件 `VerificationBadge`，使用 `visualStyleKey` 映射图标/颜色字典，对接 `GET /api/v1/content/user/profile/badge/list` 与 `/badge/detail`
- 新增隐私设置页面，覆盖 15 个 `*Visibility` 字段，调用 `POST /api/v1/content/user/profile/privacy/update`
- 新增历史记录页面，通过 `GET /api/v1/content/user/profile/history/list?historyType=NICKNAME|AVATAR` 与 `POST /api/v1/content/user/profile/history/restore` 恢复
- 扩展 useUserStore，新增资料完善率、审核状态等状态字段（**不**包含每日修改次数字段，后端不暴露）

## Capabilities

### New Capabilities

- `profile-editing`: 基础资料编辑功能，包含表单页面、OSS 直传头像、资料审核状态展示
- `homepage-customization`: 主页个性化功能，包含背景图设置、主题色选择、模块配置与排序
- `verification-badge`: 认证标识展示功能，包含 Badge 组件、认证详情弹窗、`visualStyleKey` 字典
- `privacy-settings`: 隐私设置功能，覆盖 15 个 `*Visibility` 字段，`onlineStatusVisibility` 特殊处理
- `profile-history`: 昵称/头像历史记录功能，通过 `historyType` 参数区分类型

### Modified Capabilities

- （无现有 capability 需要修改）

## Impact

- **前端路由**: 新增 4 个页面路由（编辑资料、主页设置、隐私设置、历史记录）
- **状态管理**: 扩展 useUserStore，新增 `profileCompletionRate`、`reviewStatus`、`reviewReason` 等字段
- **API 对接**: 对接 10 个后端接口（11 个端点中 `review/handle` 为后台审核端点，前端不对接），路径前缀 `/api/v1/content/user/profile/*`（详见 design.md API 对接矩阵）
- **组件依赖**: 复用 Ant Design Vue 4 组件（Form、Modal、Tabs、Select、Switch 等），新增 `VerificationBadge` 自定义组件、`AvatarCropper` 裁剪组件
- **第三方库**: 引入 `cropperjs`（裁剪）、`vuedraggable`（拖拽排序）、OSS 客户端 SDK（图片上传）
- **响应式适配**: 所有新增页面需适配 PC/平板/移动端三端布局
- **频率限制 UI**: 后端未提供独立的 update-count 接口，前端**不**展示"今日还可修改 X 次"提示；保存失败时由后端返回错误码触发提示
