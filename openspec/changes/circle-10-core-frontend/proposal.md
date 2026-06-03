## Why

内容社区缺乏基于兴趣聚合的垂直社区能力，用户无法围绕特定主题进行深度交流。需要构建圈子 MVP 基础设施，支持圈子创建与设置、成员加入/退出/角色管理/禁言移除、圈子搜索三大核心能力，为后续 EPIC-11（内容管理）和 EPIC-12/13（推荐与激励）奠定基础。

## What Changes

- 新增圈子列表页（`/circle/list`），展示已加入圈子和发现公开圈子，支持搜索入口和创建入口
- 新增圈子创建流程（`/circle/create`），包含步骤条引导：基础信息填写（名称、简介、图标、封面图、分类）→ 隐私与加入方式设置 → 创建成功
- 新增圈子名称唯一性实时校验与敏感词检测（含降级放行策略）
- 新增圈子详情页（`/circle/:id`），展示圈子完整信息，支持加入/退出操作，操作按钮根据隐私类型、加入方式、申请状态（`applyStatus`）、邀请状态（`isInvited`）动态变化
- 新增加入圈子流程，支持四种方式：直接加入、申请审核、密码加入、邀请限制
- 新增退出圈子流程（创建者不可退出）
- 新增成员管理页（`/circle/:id/members`），支持按角色/状态筛选、角色变更（设置/取消版主）、禁言（1h/24h/7d/永久）、解除禁言、移除成员
- 新增圈子搜索结果页（`/circle/search?q={keyword}`），关键词模糊匹配公开圈子，私有/密码保护圈子不展示
- 新增治理日志页（`/circle/:id/governance-log`），展示治理操作记录（禁言/解除禁言/移除/角色变更），支持筛选
- 新增圈子信息更新能力（简介、图标、封面图、分类，名称不可改）
- 新增 `useCircleStore` 状态管理，管理当前圈子详情、用户角色、成员状态、搜索关键词
- 新增 8 个自定义业务组件：CircleCard、CircleForm、JoinCircleModal、MuteMemberModal、GovernanceConfirmModal、MemberAvatar、PrivacyBadge、JoinStatusButton
- 前端埋点：创建成功、加入成功、退出、搜索点击等关键操作节点上报

## Capabilities

### New Capabilities

- `circle-crud`: 圈子创建、更新、详情查看、列表展示（已加入/公开）、名称唯一性校验，包含创建表单步骤流程、隐私与加入方式联动、图片上传裁剪
- `circle-member-management`: 成员加入（四种方式）、退出、角色管理（设置/取消版主）、禁言/解除禁言、移除，包含权限判断逻辑和操作确认弹窗
- `circle-search`: 圈子搜索能力，关键词模糊匹配公开圈子，搜索结果展示与加入操作，私有/密码保护圈子过滤
- `circle-governance-log`: 治理操作日志查看，支持按操作类型、操作对象、日期范围筛选
- `circle-state-management`: 圈子模块状态管理，包括 useCircleStore、权限判断逻辑、列表缓存策略、数据生命周期管理

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

- **前端路由**: 新增 `/circle/list`、`/circle/create`、`/circle/:id`、`/circle/:id/members`、`/circle/search`、`/circle/:id/governance-log` 六个页面路由
- **API 对接**: 依赖后端 14 个接口（圈子 CRUD 6 个、成员管理 7 个、搜索 1 个、治理日志 1 个），统一使用 `defHttp` 封装
- **状态管理**: 新增 `useCircleStore`（Pinia），管理圈子详情、用户角色、成员状态
- **组件库**: 新增 8 个业务组件，复用现有 Form/Table/Modal/Drawer/Upload/Cropper 等基础组件
- **国际化**: 当前版本中文硬编码，但文案统一管理在 `src/locales/lang/zh-CN/circle.ts` 常量文件中，预留 `t()` 接口
- **性能**: 搜索 P95 < 500ms，核心操作 P95 < 800ms，列表首屏 < 2s，需虚拟滚动、防抖、图片懒加载等优化
- **响应式**: 支持桌面端（>= 1200px）、平板端（768-1199px）、移动端（< 768px）三档适配
- **依赖**: 依赖用户账号系统（登录态判断）、后端敏感词服务（含降级方案）、图片上传服务
