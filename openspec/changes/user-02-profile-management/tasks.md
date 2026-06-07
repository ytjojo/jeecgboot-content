## 1. 数据结构与迁移

- [x] 1.1 编写资料审核、主页模块、认证标识、资料历史表 Flyway migration
- [x] 1.2 添加 migration 表、索引、默认值、回滚脚本校验测试
- [x] 1.3 扩展 `content_user_profile` 和 `content_user_privacy_setting` 必要字段
- [x] 1.4 添加现有表新增字段兼容性和默认值测试
- [x] 1.5 创建新增表对应 entity、mapper、service 基础类
- [x] 1.6 添加 mapper 启动加载和基础 CRUD 测试
- [x] 1.7 编写已有 profile 字段到认证标识和模块配置的兼容初始化逻辑
- [x] 1.8 添加兼容初始化幂等性测试

## 2. 资料更新与审核（统一端点 `/profile/update`）

- [x] 2.1 定义资料素材处理、敏感词审核、AI 审核适配接口（`ContentUserProfileAuditAdapter`）
- [x] 2.2 添加适配接口 mock 和失败降级测试
- [x] 2.3 实现 `ContentUserProfileUpdateReq` 入参规范化和字段校验（`@NotBlank`/`@Size`/`@Pattern`）
- [x] 2.4 添加昵称、头像、简介、生日、地区、职业、链接的 null、空值、越界测试
- [x] 2.5 实施更新（2026-06-03）：移除独立上传端点，改用前端 OSS 客户端直传 + URL 持久化
- [x] 2.6 实现疑似违规资料进入待审核流程
- [x] 2.7 添加敏感词命中、待审核阻断再次修改测试
- [x] 2.8 实现审核通过发布和审核拒绝恢复旧值（`/profile/review/handle`）
- [x] 2.9 添加审核通过、拒绝原因、旧值恢复测试

## 3. 主页个性化

- [x] 3.1 实现主页背景图、主题色持久化（URL + 十六进制颜色）
- [x] 3.2 添加主页字段空值、非法十六进制、长度越界测试
- [x] 3.3 实现 `ContentUserHomepageUpdateReq` 与 `/profile/homepage/update` 端点
- [x] 3.4 实现 `/profile/homepage/defaults/restore` 恢复默认
- [x] 3.5 实现 `ContentUserHomepageModuleVO` 模块聚合与 `/profile/homepage/modules` 端点
- [x] 3.6 添加主页模块可见性裁剪、空列表、未知模块键测试

## 4. 认证标识展示

- [x] 4.1 实现 `content_user_verification_badge` 表、查询服务和 VO 聚合
- [x] 4.2 添加个人、企业、达人、官方、实名 badge 聚合测试
- [x] 4.3 实现 `/profile/badge/list` 与 `/profile/badge/detail` 端点
- [x] 4.4 添加认证详情可见、失效、过期、缺少元数据测试
- [x] 4.5 定义手机号和邮箱绑定状态读取适配接口
- [x] 4.6 添加绑定状态公开、隐藏、账号层不可用降级测试
- [x] 4.7 从 profile 响应中移除对旧认证字段的直接依赖，以 `verificationBadges` 列表为准
- [x] 4.8 添加 `visualStyleKey` 字段映射前端图标/颜色测试

## 5. 隐私与缓存

- [x] 5.1 扩展 `ContentUserPrivacyUpdateReq` 与隐私实体支持 15 个 `*Visibility` 字段
- [x] 5.2 添加隐私字段 null、空值、非法枚举、越界长度测试（含 `onlineStatusVisibility` 特殊枚举 `PUBLIC|HIDDEN|MUTUAL_ONLY`）
- [x] 5.3 重构 `ContentUserProfileVO.from(...)` 为统一字段裁剪流程
- [x] 5.4 添加生日、性别、地区、职业、链接、认证标识、手机/邮箱绑定四种可见性测试
- [x] 5.5 实施更新（2026-06-03）：隐私更新频控（每小时 10 次）本期不在 controller 端点中实现，需在 service 层补齐
- [x] 5.6 实现资料缓存和隐私缓存 key 管理
- [x] 5.7 添加缓存 key 空 userId、空 viewerScope、越界标识测试
- [x] 5.8 实现隐私变更后的公共缓存删除（`invalidateProfileCache(userId)`）
- [x] 5.9 添加公开转私密后新请求不泄露旧字段测试

## 6. 历史记录与恢复

- [x] 6.1 实现昵称和头像生效变更后的历史记录写入（`ContentUserProfileHistoryVO`）
- [x] 6.2 添加昵称变更、头像变更、未生效审核不记录测试
- [x] 6.3 实现 `/profile/history/list?historyType=NICKNAME|AVATAR` 倒序查询和每类最多 20 条限制
- [x] 6.4 添加倒序、超过 20 条 FIFO 清理测试
- [x] 6.5 实现 180 天历史清理任务（`expires_at` 字段 TTL）
- [x] 6.6 添加未过期保留、过期清理、重复执行幂等测试
- [x] 6.7 实现 `/profile/history/restore` 复用资料更新流程
- [x] 6.8 添加历史 id 空值、未知、过期、昵称占用测试

## 7. API 与验收

- [x] 7.1 补齐 12 个 profile/privacy/homepage/badge/history controller 接口
- [x] 7.2 添加 Controller WebMvc 成功响应和参数校验测试
- [x] 7.3 补齐 Knife4j 注解、中文字段说明、错误码常量
- [x] 7.4 添加错误码和接口文档关键字段覆盖测试
- [x] 7.5 运行内容社区用户资料相关单元测试和 WebMvc 测试
- [x] 7.6 修复测试失败并复跑到通过
- [x] 7.7 实施回顾（2026-06-03）：为前端 change `complete-profile-management-frontend` 提供接口契约对齐

## 8. 实施回顾遗留项（与原 design.md 偏差）

- [ ] 8.1 在 service 层补齐资料更新与隐私更新频控（每日 5 次 / 每小时 10 次）
- [ ] 8.2 评估是否新增 `POST /api/v1/content/user/profile/asset/validate` 服务端素材校验端点
- [ ] 8.3 与前端对齐 `onlineStatusVisibility` 特殊枚举的前端表单实现
