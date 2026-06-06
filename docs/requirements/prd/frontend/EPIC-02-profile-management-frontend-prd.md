# EPIC-02 个人资料与主页个性化 — 前端 PRD

> **史诗ID**: EPIC-02
> **域**: user（用户域）
> **变更ID**: user-02-profile-management-frontend
> **版本**: 1.0
> **前置依赖**: 无
> **日期**: 2026-06-02

---

## 1. 需求说明

### 需求目标

- **需求名称**: 个人资料与主页个性化
- **一句话概述**: 为内容社区用户提供完整的个人资料管理、主页个性化定制、认证标识展示、隐私可见性控制和历史记录能力
- **要解决的问题**: 当前社区缺少统一的资料管理入口，主页无法个性化，隐私控制粒度不足，认证标识与资料字段耦合，无法满足用户构建社区身份的需求
- **期望达成的结果**: 资料完善率 >80%，主页访问时长提升 30%，隐私设置使用率 >60%，认证用户信任度评分提升 25%

### 目标用户与使用场景

| 角色 | 说明 | 使用场景 |
|------|------|----------|
| 注册用户 | 所有已完成注册登录的社区用户 | 编辑个人资料、设置隐私、查看历史记录 |
| 认证用户 | 已通过个人/企业/达人/官方认证的用户 | 查看认证标识、展示认证详情 |
| 访客用户 | 未登录或访问他人主页的用户 | 查看他人的公开资料和认证标识 |
| 管理员 | 平台运营管理人员 | 审核资料变更、管理认证标识 |

- **使用场景**: 桌面端 + 移动端跨端适配
- **入口路径**:
  - 个人中心 → 编辑资料
  - 个人主页 → 主页设置
  - 设置 → 隐私设置
  - 个人中心 → 历史记录

### 范围定义

#### 本期范围
- 基础资料编辑（昵称、头像、简介、性别、生日、地区、职业、个人链接）
- 头像上传与素材校验（JPG/PNG/WebP，最大 5MB）
- 资料修改频率限制提示（每日 5 次）
- 资料审核状态展示与提示
- 主页背景图与主题色设置
- 主页模块显隐与拖拽排序
- 认证标识展示（个人/企业/达人/官方/实名/手机号/邮箱验证）
- 认证详情弹窗
- 字段可见性设置（公开/仅关注者/互关可见/仅自己）
- 隐私设置即时生效反馈
- 隐私修改频率限制提示（每小时 10 次）
- 曾用昵称/头像历史记录查看
- 历史记录恢复操作

#### 非本期范围
- 独立支付结算
- 企业组织通讯录
- 认证申请材料提交与审批后台
- 生物识别与两步验证

---

## 2. 功能列表

| 功能名称 | 功能概述 | 适用角色 | 优先级 | 依赖 |
|----------|----------|----------|--------|------|
| F1 基础资料编辑 | 编辑昵称、头像、简介等基础信息 | 注册用户 | P0 | EPIC-01 登录 |
| F2 头像上传与校验 | 上传头像并校验格式、大小、分辨率 | 注册用户 | P0 | F1 |
| F3 资料审核状态 | 展示资料审核中的状态和结果通知 | 注册用户 | P0 | F1 |
| F4 主页背景与主题色 | 设置个人主页背景图和主题色 | 注册用户 | P1 | F1 |
| F5 主页模块配置 | 自定义主页模块显隐与排序 | 注册用户 | P1 | F1 |
| F6 认证标识展示 | 在昵称旁展示认证标识并查看详情 | 认证用户/访客 | P0 | EPIC-01 |
| F7 字段可见性设置 | 为每个资料字段设置可见性级别 | 注册用户 | P0 | F1 |
| F8 隐私缓存生效 | 隐私设置变更后即时生效反馈 | 注册用户 | P0 | F7 |
| F9 昵称/头像历史 | 查看和恢复曾用昵称与头像 | 注册用户 | P2 | F1 |

---

## 3. 功能详细说明

### 3.1 F1 基础资料编辑

#### 页面: 编辑资料页

**入口**: 个人中心 → 头像/昵称区域 → "编辑资料"按钮

**数据来源**: 页面初始化时调用 `GET /content/user/profile/detail?ownerUserId={currentUid}` 获取当前用户资料。

**页面结构**:
```
顶部导航栏（返回箭头 + 页面标题"编辑资料" + 保存按钮）
├── 头像区域（居中大头像 + 相机图标触发上传）
├── 审核状态条（仅在 profileReviewStatus === 'PENDING' 时展示）
├── 提示条（"今日还可修改 X 次"）
├── 表单区域
│   ├── 昵称（必填，输入框，最大 20 字符）
│   ├── 简介（选填，文本域，最大 500 字符，显示字数统计）
│   ├── 性别（选填，单选：男/女/保密）
│   ├── 生日（选填，日期选择器，不可选未来日期）
│   ├── 地区（选填，级联选择省/市/区）
│   ├── 职业（选填，输入框，最大 64 字符）  ← 后端 max=64（非 30）
│   └── 个人链接（选填，URL 输入框，带格式校验）
└── 底部操作栏（取消 + 保存按钮）
```

**保存/取消按钮行为规范**:
- **PC 端**: 顶部导航栏的保存按钮作为视觉引导，底部操作栏为主要操作区域，两处按钮功能一致（同一事件源）
- **移动端**: 底部操作栏固定在屏幕底部（适配 iPhone Safe Area），为主要操作区域；顶部保存按钮仅作为视觉引导，点击后滚动至底部操作栏或直接触发保存
- **返回按钮行为**: 点击顶部返回箭头时，若表单有未修改（isDirty=false），直接返回；若有未修改，弹出确认框"您有未保存的修改，确定离开吗？"
- **审核中状态返回**: 当表单处于审核中状态（字段不可编辑，isDirty 始终为 false）时，点击返回直接返回，不弹出确认框

**交互要求**:
- 头像区域点击触发文件选择，选择后进入裁剪弹窗（复用 `Cropper` 组件，详见 3.2）
- **提交方式**: 点击保存 → 合并 formData 为 `ContentUserProfileUpdateReq` → 调用 `POST /content/user/profile/update?userId={currentUid}` 一次性提交
- **必填约束**（前后端共用）: `nickname`、`avatar` 字段在 `@NotBlank` 校验下必须非空，提交前前端表单层必须保证
- 昵称输入框实时校验：为空时提示"请输入昵称"，含敏感词时提示"昵称包含不当内容，请修改"
- 简介文本域右下角显示已输入字数/最大字数（如 123/500）
- 性别使用 Radio.Group 组件（提交时转为 String 枚举：`MALE` / `FEMALE` / `OTHER` / `UNKNOWN`）
- 生日使用 DatePicker 组件，`disabledDate` 禁用未来日期，提交时序列化为 ISO 8601 字符串
- 地区使用 Cascader 组件加载省市区数据，最终合并为字符串（max 64）
- 个人链接输入框失去焦点时校验 URL 格式（regex `^(https?://|/).*$`）
- 保存按钮点击后进入 loading 状态，防止重复提交
- 保存成功后显示全局消息"资料已更新"并刷新 `useUserStore` 中的资料缓存，返回个人中心
- 保存失败时保留已输入内容，显示错误提示

**频率限制交互**（前端 UX，依赖后端错误码）:
- **不预查**: 后端无独立 `/profile/update-count` 接口，前端**不预查**当日剩余次数
- **被动拦截**: 当后端返回频率限制错误码（待与后端约定，如 `code = 1101` 或 `message` 包含"今日修改次数已达上限"）时：
  - 表单顶部显示红色 Alert "今日修改次数已达上限，请明天再试"
  - 保存按钮置灰，hover 提示同上
  - 直至用户离开页面或收到后端"重置"信号
- **本地乐观计数**: 前端可在本地维护 `dailyUpdateCount`，每次提交成功 +1，达到 5 次后主动置灰（仅作 UX 提示，不替代后端判定）

**审核状态交互**:
- **数据来源**: `ContentUserProfileVO.profileReviewStatus` 字段（取值：`NONE` / `PENDING` / `APPROVED` / `REJECTED`）
- 当 `profileReviewStatus === 'PENDING'`：表单顶部显示黄色提示条"您的资料正在审核中，预计 24 小时内完成"
- 审核期间保存按钮禁用
- 审核结果：通过 WebSocket/SSE 推送（详见 3.3 节），前端用 Notification 组件弹出

**状态与边界情况**:
- **加载中**: 页面显示骨架屏
- **首次编辑**: 必填字段（昵称、头像）标记红色星号，引导用户完善
- **审核中**: 表单字段显示当前生效值（旧值），不可编辑
- **网络错误**: 显示"网络异常，请重试"提示，保留已输入内容
- **字段超长**: 输入框下方显示红色错误提示，阻止提交
- **后端校验失败**（如 `@NotBlank nickname`）: 解析后端 `Result.message`，回填到对应字段下方

---

### 3.2 F2 头像上传与校验

#### 组件: 头像裁剪弹窗

**触发**: 编辑资料页点击头像区域

> **架构调整说明**: 后端 `ContentUserProfileController` **不提供**独立的 `/avatar/upload` 接口。头像上传改为**前端 OSS 客户端直传**模式，流程为：裁剪 → 客户端直传 OSS → 获得 CDN URL → 暂存于 formData.avatar → 用户点击"保存"时由 `/profile/update` 统一提交。

**弹窗结构**:
```
Modal 弹窗
├── 图片预览区（Cropper 裁剪框，1:1 比例锁定）
├── 缩放控制条（滑块控制裁剪区域大小）
├── 裁剪效果预览区
│   ├── 圆形预览（模拟个人主页/评论区头像显示，48px）
│   ├── 小方形预览（模拟消息列表头像，32px）
│   └── 当前裁剪区域坐标显示（可选，开发调试用）
├── 底部操作
│   ├── "重新选择" 按钮
│   ├── "取消" 按钮
│   └── "确定" 按钮
```

**校验规则**（前端 + OSS 服务端双重校验）:
| 校验项 | 规则 | 错误提示 |
|--------|------|----------|
| 文件格式 | 仅 JPG、PNG、WebP | "仅支持 JPG、PNG、WebP 格式" |
| 文件大小 | ≤ 5MB（OSS 限制） | "图片大小必须小于 5MB" |
| 文件内容 | 非空文件 | "请选择有效的图片文件" |
| CDN URL 长度 | max 500 | 后端 `@Size(max=500)` 校验 |

**交互要求**:
- 文件选择后立即校验格式和大小，不通过时在文件选择阶段就拦截并提示
- 校验通过后打开裁剪弹窗，裁剪框默认居中，比例 1:1
- 支持滚轮缩放和滑块缩放
- 点击"确定"后调用 OSS 客户端直传通道（复用 JeecgBoot 既有上传组件），弹窗显示上传进度
- **上传成功**: 获得 CDN URL → 暂存于 formData.avatar → 关闭弹窗 → 编辑页头像区域更新为新头像预览（不直接提交到后端）
- **上传失败**: 显示"上传失败，请重试"，弹窗不关闭，允许重试
- 用户最终点击编辑页底部"保存"时，统一走 `/profile/update` 将 avatar URL 与其他字段一并提交

**OSS 直传组件复用**:
- 优先复用 `src/components/Upload/` 下的既有 OSS 上传组件
- 若现有组件不支持纯客户端签名，需扩展：前端向后端换取 STS 临时凭证 → 前端携带凭证直传 OSS
- 上传后的 CDN URL 通过 `useUserStore` 暂存，不立即触发 `useUserStore` 全量更新

---

### 3.3 F3 资料审核状态

#### 展示位置: 编辑资料页顶部 + 个人中心页

**审核状态流转**:
```
正常 → 提交修改 → 待审核 → 审核通过 → 生效
                                     → 审核不通过 → 恢复原值 + 通知
```

**编辑资料页审核提示**:
- 待审核状态：黄色 Alert 组件，文案"您的资料正在审核中，预计 24 小时内完成"
- 审核不通过：红色 Alert 组件，文案"资料审核未通过，原因：{reason}"，提供"重新编辑"按钮
- 审核通过：绿色 Notification 推送"资料审核已通过"

**个人中心页审核标识**:
- 头像右下角显示黄色时钟图标，hover 提示"资料审核中"

---

### 3.4 F4 主页背景与主题色

#### 页面: 主页设置页

**入口**: 个人主页 → 右上角齿轮图标 → "主页设置"

**页面结构**:
```
顶部导航栏（返回箭头 + "主页设置" 标题 + 保存按钮）
├── 背景图设置区
│   ├── 当前背景图预览（宽幅缩略图）
│   ├── "更换背景" 按钮（触发上传）
│   └── "恢复默认" 文字按钮
├── 主题色设置区
│   ├── 预设色板（网格排列 8-12 个预设颜色）
│   └── 自定义颜色输入（可选，ColorPicker）
├── 实时预览区
│   └── 缩小版主页预览（展示背景+主题色效果）
└── 底部操作栏（恢复全部默认 + 保存按钮）
```

**交互要求**:
- **背景图上传**: 复用 3.2 节的 OSS 客户端直传流程（JPG/PNG/WebP，≤5MB），裁剪比例 16:9；上传后获得 CDN URL 暂存于 formData.homepageBackground
- **保存方式**: 主页设置页提供两种保存路径
  - **路径 A**（推荐，仅修改主页）: `POST /content/user/profile/homepage/update?userId={uid}` 提交 `ContentUserHomepageUpdateReq`（含 background/themeColor/modules）
  - **路径 B**（与基础资料合并保存）: 暂存为 formData，用户返回编辑资料页统一走 `/profile/update`
- **模块配置**: 通过 `GET /content/user/profile/homepage/modules?userId={uid}` 加载可选模块清单
- **恢复默认**: 调用 `POST /content/user/profile/homepage/defaults/restore?userId={uid}` 清除自定义配置
- 选择主题色后实时更新预览区效果（不触发接口，CSS 变量切换）
- 预设色板选中项显示对勾图标
- 保存成功提示"主页设置已更新"，刷新当前用户主页资料缓存

**无障碍对比度校验**:
- **自动校验**: 选择主题色后自动计算与白色/黑色文字的对比度（WCAG AA 标准要求对比度 ≥ 4.5:1）
- **警告提示**: 对比度不满足标准时，在主题色选择区下方显示黄色警告"当前主题色与文字对比度不足，可能影响可读性"
- **自动调整**: 当对比度不足时，系统自动选择对比度更高的文字颜色（深色主题色用白色文字，浅色主题色用黑色文字），并在预览区实时展示调整效果
- **手动覆盖**: 用户可在"高级设置"中手动选择文字颜色，覆盖自动调整结果
- **对比度计算公式**: 使用相对亮度公式 `contrast = (L1 + 0.05) / (L2 + 0.05)`，其中 L1 为较亮颜色的相对亮度

**状态与边界情况**:
- **无自定义背景**: 预览区显示平台默认渐变背景
- **背景图加载失败**: 显示占位背景 + "背景图加载失败" 提示
- **主题色为空**: 使用平台默认主色调

---

### 3.5 F5 主页模块配置

#### 页面: 主页设置页（续）/ 模块管理页

**入口**: 主页设置 → "模块管理" 卡片

**页面结构**:
```
模块管理区域
├── 已启用模块列表（可拖拽排序）
│   ├── 每项包含：拖拽手柄 + 模块名称 + 显隐开关
│   └── 模块：动态、收藏、成就、关注、粉丝、相册、音乐...
├── "恢复默认排序" 文字按钮
└── 保存按钮
```

**交互要求**:
- 使用拖拽排序组件（如 vuedraggable）实现模块排序
- 显隐开关为 Switch 组件，关闭时该模块从主页隐藏
- 当所有模块开关都关闭时，保存按钮禁用，提示"至少需要保留一个模块"
- 拖拽排序后自动标记为"未保存"状态，显示"保存"按钮
- "恢复默认排序" 复用 `POST /content/user/profile/homepage/defaults/restore?userId={uid}` 端点
- **保存方式**: 页面 mount 时调用 `GET /content/user/profile/homepage/modules?userId={uid}` 加载模块清单（含 `moduleKey`、`moduleName`、`visible`、`sortOrder`）；用户调整后提交到 `POST /content/user/profile/homepage/update`，body 中 `modules` 字段传入 `List<ContentUserHomepageModuleReq>`（含 `moduleKey`/`visible`/`sortOrder` 必填）
- 保存成功提示"主页模块配置已更新"

**移动端拖拽交互规范**:
- **触发方式**: 长按（300ms）模块列表项进入拖拽模式，拖拽手柄图标作为辅助视觉提示
- **拖拽反馈**: 拖拽过程中列表项增加阴影效果（box-shadow: 0 4px 12px rgba(0,0,0,0.15)），提升拖拽状态感知
- **触摸热区**: 拖拽手柄最小触摸热区为 44px x 44px（符合 WCAG 2.5.5 Target Size 标准）
- **位置指示**: 拖拽时在目标位置显示蓝色插入线，明确放置位置
- **键盘无障碍**: 支持键盘操作，通过上下箭头键调整模块顺序，Enter 键确认，Escape 键取消
- **振动反馈**: 拖拽开始时触发轻微振动反馈（navigator.vibrate(50)），增强操作确认感

**状态与边界情况**:
- **全部隐藏**: 保存时校验拦截，提示"至少需要保留一个模块"
- **未保存离开**: 弹出确认框"您有未保存的修改，确定离开吗？"

---

### 3.6 F6 认证标识展示

#### 组件: 认证标识 Badge

**展示位置**: 昵称右侧（所有出现昵称的地方）

> **数据来源调整**: 认证标识列表已内嵌在 `ContentUserProfileVO.verificationBadges` 字段中，**编辑资料页和个人主页**复用 `/profile/detail` 响应即可，无需额外请求。**认证详情弹窗**才需调用 `GET /content/user/profile/badge/detail?badgeId={id}`。
> 
> 字段映射调整（对齐 `ContentUserVerificationBadgeVO`）:
> - `type` → `badgeType`
> - `label` → `badgeLabel`
> - 视觉样式不再硬编码，改由 `visualStyleKey` 映射图标/颜色（前端维护 `visualStyleKey → 图标 + 颜色` 字典）

**标识类型与样式**（前端 `visualStyleKey` 映射）:
| 视觉样式 Key | 视觉含义 | 颜色 | 图标 |
|-------------|---------|------|------|
| `INDIVIDUAL` | 个人认证 | #1890ff | CheckCircleFilled |
| `ENTERPRISE` | 企业认证 | #faad14 | SafetyCertificateFilled |
| `INFLUENCER` | 大V/达人 | #722ed1 | StarFilled |
| `OFFICIAL` | 官方认证 | #f5222d | ShieldFilled |
| `REAL_NAME` | 实名认证 | #8c8c8c | IdcardFilled |
| `PHONE_VERIFIED` | 手机已验证 | #52c41a | MobileFilled |
| `EMAIL_VERIFIED` | 邮箱已验证 | #52c41a | MailFilled |
| `DEFAULT` | 兜底样式 | #8c8c8c | SafetyCertificateFilled |

> **未识别 visualStyleKey 处理**: 落入 `DEFAULT` 兜底样式，避免显示空白。后续如新增样式仅需扩展映射字典。

**认证详情弹窗**:
```
Modal 弹窗
├── 认证图标（大尺寸，按 visualStyleKey 映射）
├── 认证文案（badgeLabel）
├── 认证时间（verifiedAt，格式 YYYY-MM-DD HH:mm）
├── 认证说明/描述（description，可选）
├── 企业认证额外显示：企业名称（从 description 或独立字段解析）
├── 达人认证额外显示：认证领域（同上）
└── 关闭按钮
```

**交互要求**:
- 认证标识紧跟昵称右侧显示，与昵称同行
- 鼠标 hover 标识时显示 Tooltip，简要说明认证类型（取 `badgeLabel`）
- 点击标识打开认证详情弹窗，调用 `GET /content/user/profile/badge/detail?badgeId={id}` 拉取详情
- 多个认证同时存在时，按优先级排列（官方 > 企业 > 达人 > 个人 > 实名 > 手机 > 邮箱），由前端定义的 `BADGE_PRIORITY` 常量控制
- 无认证时不显示任何标识
- 认证标识不随昵称审核状态变化，独立展示

**昵称过长截断策略**:
- **PC 端**: 昵称最大显示宽度为 200px，超出部分显示省略号（...），hover 时 Tooltip 显示完整昵称
- **移动端**: 昵称最大显示宽度为 120px，超出部分显示省略号，长按显示完整昵称
- **截断后标识布局**: 昵称截断后认证标识仍紧跟显示，不因昵称截断而换行

**认证标识过多折叠方案**:
- **显示规则**: 最多同时显示 2 个认证标识（按优先级排序），超出部分显示 "+N" 徽标
- **折叠交互**: 点击 "+N" 徽标展开显示所有认证标识，点击空白处或选择标识后收起
- **徽标样式**: "+N" 使用灰色背景圆角徽标，与认证标识风格统一

**组件封装**:
- 封装 `VerificationBadge` 组件，Props 包含 `badges: ContentUserVerificationBadgeVO[]`（直接使用后端 VO 类型）
- 组件内部根据 `visualStyleKey` 映射图标/颜色，按 `BADGE_PRIORITY` 排序，应用折叠策略
- 组件无状态，仅做展示

---

### 3.7 F7 字段可见性设置

#### 页面: 隐私设置页

**入口**: 设置 → 隐私设置 / 个人中心 → 隐私设置

> **数据加载调整**: 后端无独立 `/privacy/settings` 接口。隐私设置页 mount 时调用 `GET /content/user/profile/detail?ownerUserId={currentUid}`，从 `ContentUserProfileVO` 中推导初始可见性：
> - 字段值为 `null`（后端隐私裁剪）→ 默认推断为 `PRIVATE`（前端 UX 选择）
> - 字段有值 → 默认推断为 `PUBLIC`
> - **精确模式**: 后续可增加 `/privacy/settings` 独立接口返回 16 个字段精确配置（待与后端确认）

**页面结构**（对齐 D7 的 7 组 15+2 字段）:
```
顶部导航栏（返回箭头 + "隐私设置" 标题）
├── 说明区域（简要说明隐私设置的作用）
├── 批量操作区域
│   └── "一键全部设为" 快捷操作（Select 组件，批量设置所有可修改字段的可见性）
├── 字段可见性列表（按 7 个分组折叠展示）
│   ├── 📋 基础资料组 (5)
│   │   ├── 简介 → 下拉选择（bioVisibility）
│   │   ├── 性别 → 下拉选择（genderVisibility）
│   │   ├── 生日 → 下拉选择（birthdayVisibility）
│   │   ├── 地区 → 下拉选择（regionVisibility）
│   │   └── 职业 → 下拉选择（professionVisibility）
│   ├── 🔗 扩展资料组 (1)
│   │   └── 个人链接 → 下拉选择（personalLinkVisibility）
│   ├── 🏠 主页组 (3)
│   │   ├── 主页背景 → 下拉选择（homepageBackgroundVisibility）
│   │   ├── 主题色 → 下拉选择（themeColorVisibility）
│   │   └── 主页模块 → 下拉选择（homepageModuleVisibility）
│   ├── 🏆 认证组 (2)
│   │   ├── 认证标识 → 下拉选择（verificationBadgesVisibility）
│   │   └── 认证信息 → 下拉选择（certificationVisibility）
│   ├── 📊 活动组 (3)
│   │   ├── 资料完善度 → 下拉选择（profileCompletionVisibility）
│   │   ├── 审核状态 → 下拉选择（profileReviewStatusVisibility）
│   │   └── 最近活动 → 下拉选择（recentActivityVisibility）
│   ├── 🟢 在线状态组 (1)
│   │   └── 在线状态 → 下拉选择（onlineStatusVisibility，特殊枚举，见下文）
│   └── ⚙️ 显示开关组 (2)
│       ├── 显示互关好友数 → Switch（showMutualFollowersCount）
│       └── 显示最近活动高亮 → Switch（showRecentActivityHighlight）
└── 保存按钮
```

> **注意**: 昵称和头像始终公开，不参与隐私控制（后端接口不提供对应字段）。

**批量操作交互规范**:
- **默认可见性** (本版移除): 后端无 `defaultVisibility` 字段，移除该 UX 控件
- **一键全部设为**: 选择可见性级别后弹出确认框"确定将所有可修改字段设为{级别}吗？此操作会覆盖当前各字段的单独设置"，确认后批量更新所有 `*Visibility` 字段（昵称、头像、在线状态除外；Switch 类型字段不参与）
- **操作反馈**: 批量操作成功后显示"已将 X 个字段设为{级别}"，并在字段列表中高亮变化的字段（2 秒后恢复）
- **撤销能力**: 批量操作后提供"撤销"按钮（5 秒内有效），点击后恢复批量操作前的状态

**交互要求**:
- 每个字段右侧使用 Select 组件展示当前可见性级别（Switch 字段除外）
- 通用可见性选项图标说明：
  - 🌐 公开（`PUBLIC`）：所有人可见
  - 👥 仅关注者（`FOLLOWERS_ONLY`）：关注你的人可见
  - 🤝 互关可见（`MUTUAL_ONLY`）：互相关注的人可见
  - 🔒 仅自己（`PRIVATE`）：仅自己可见
- **在线状态特殊枚举**（`onlineStatusVisibility`）:
  - 🌐 公开（`PUBLIC`）
  - 🚫 隐藏（`HIDDEN`）
  - 🤝 互关可见（`MUTUAL_ONLY`）
  - ⚠️ 不支持 `PRIVATE`，后端 `@Pattern` 拒绝
- 昵称和头像默认公开且不可修改（**不参与隐私控制**——后端接口不提供对应字段，置灰 + Tooltip 说明"昵称和头像始终公开"）
- 修改可见性后保存按钮激活
- 保存成功提示"隐私设置已更新"，调用 `POST /content/user/profile/privacy/update?userId={uid}` 提交，body 为合并后的 `ContentUserPrivacyUpdateReq`
- **频率限制**: 后端无独立 `/privacy/update-count` 接口，采用**被动拦截**策略：当后端返回"操作过于频繁"错误时，黄色提示条 + 保存按钮禁用 5 分钟（前端 UX 兜底）

**前端提交字段映射**（对齐 D7 的 15+2 字段）:
```typescript
const visibilityMap: Record<string, string> = {
  PUBLIC: '公开',
  FOLLOWERS_ONLY: '仅关注者',
  MUTUAL_ONLY: '互关可见',
  PRIVATE: '仅自己'
};

const req: ContentUserPrivacyUpdateReq = {
  // 基础资料 (5)
  bioVisibility: privacyMap.bio,
  genderVisibility: privacyMap.gender,
  birthdayVisibility: privacyMap.birthday,
  regionVisibility: privacyMap.region,
  professionVisibility: privacyMap.profession,
  // 扩展资料 (1)
  personalLinkVisibility: privacyMap.personalLink,
  // 主页 (3)
  homepageBackgroundVisibility: privacyMap.homepageBackground,
  themeColorVisibility: privacyMap.themeColor,
  homepageModuleVisibility: privacyMap.homepageModule,
  // 认证 (2)
  certificationVisibility: privacyMap.certification,
  verificationBadgesVisibility: privacyMap.verificationBadges,
  // 活动 (3)
  profileCompletionVisibility: privacyMap.profileCompletion,
  profileReviewStatusVisibility: privacyMap.profileReviewStatus,
  recentActivityVisibility: privacyMap.recentActivity,
  // 在线状态 (1) — 特殊枚举：PUBLIC/HIDDEN/MUTUAL_ONLY，不含 PRIVATE
  onlineStatusVisibility: privacyMap.onlineStatus,
  // 布尔开关 (2)
  showMutualFollowersCount: switches.showMutualFollowersCount,
  showRecentActivityHighlight: switches.showRecentActivityHighlight
};
```
- 修改可见性后保存按钮激活
- 保存成功提示"隐私设置已更新"
- **频率限制**: 后端无 `/privacy/update-count` 预查接口，采用**被动拦截**——后端返回"操作过于频繁"错误时，前端展示黄色提示条 + 保存按钮禁用 5 分钟（前端 UX 兜底值，待与后端对齐具体阈值）

**状态与边界情况**:
- **加载中**: 列表显示骨架屏
- **保存中**: 保存按钮显示 loading
- **频率受限**: 黄色提示条 + 保存按钮禁用
- **网络错误**: 保留已修改的选择，显示错误提示

---

### 3.8 F8 隐私缓存生效

#### 展示策略

- 隐私设置保存成功后，前端立即刷新本地缓存的用户资料数据
- 访问他人主页时，如果对方刚修改了隐私设置，前端展示的字段可能有 5 分钟缓存延迟
- 前端不主动实现缓存失效逻辑（由后端处理），但需处理以下场景：
  - 用户修改隐私后返回个人主页，应立即看到最新效果
  - 前端在隐私设置保存成功后，主动调用资料刷新接口更新本地缓存

**交互反馈**:
- 保存成功后显示提示"隐私设置已更新，新设置将立即对新访问者生效"
- 如果因缓存导致短暂数据不一致，不向用户展示技术细节，静默处理

---

### 3.9 F9 昵称/头像历史记录

#### 页面: 历史记录页

**入口**: 个人中心 → 编辑资料 → "历史记录" 文字链接

> **API 调整说明**: 后端无独立的 `/history/nicknames` 和 `/history/avatars` 接口，改为通过 `historyType` 区分的**统一接口**：
> - 昵称历史: `GET /content/user/profile/history/list?userId={uid}&historyType=NICKNAME`
> - 头像历史: `GET /content/user/profile/history/list?userId={uid}&historyType=AVATAR`
> 
> 响应字段 `ContentUserProfileHistoryVO`: `id`、`historyType`、`historyValue`（昵称文本或头像 URL）、`changedAt`、`expiresAt`

**页面结构**:
```
顶部导航栏（返回箭头 + "历史记录" 标题）
├── 标签页切换
│   ├── 昵称历史 Tab（默认激活）
│   └── 头像历史 Tab
├── 当前值展示区
│   ├── 昵称历史 Tab：当前昵称 + "当前" 标签
│   └── 头像历史 Tab：当前头像缩略图 + "当前" 标签
├── 历史记录列表（倒序排列）
│   ├── 每条记录
│   │   ├── 昵称历史项：`historyValue` 文本 + `changedAt` + "恢复" 按钮
│   │   └── 头像历史项：`historyValue`（URL）缩略图 + `changedAt` + "恢复" 按钮
│   └── 列表底部说明："最多保留 20 条记录，保留期限 180 天（按 expiresAt 字段判定）"
└── 空状态
```

**当前值标识规范**:
- **昵称历史**: 当前昵称在列表顶部独立展示，右侧显示绿色"当前"标签，与历史记录有明显分隔线
- **头像历史**: 当前头像在列表顶部独立展示，右侧显示绿色"当前"标签，缩略图边框高亮（蓝色 2px solid）
- **视觉对比**: 历史记录项的昵称/头像与当前值在视觉上形成对比，帮助用户判断恢复价值

**交互要求**:
- 使用 Tabs 组件切换昵称/头像历史；切换时**重新请求**对应 historyType
- 列表按 `changedAt` 倒序排列，最新记录在最上方
- 每条记录显示：`historyValue` + `changedAt`（格式"YYYY-MM-DD HH:mm"）
- "恢复" 按钮点击后弹出确认框"确定恢复为 {historyValue} 吗？"
- 确认恢复后调用 `POST /content/user/profile/history/restore?userId={uid}&historyId={id}`，等同于一次新的资料修改，需遵守频率限制（后端错误码拦截）
- 恢复成功提示"{昵称/头像}已恢复"，刷新 `useUserStore` 资料缓存
- 恢复失败（如昵称已被占用）显示对应错误提示
- 最多显示 20 条记录（后端控制），超出部分不返回

**状态与边界情况**:
- **无历史记录**: 空状态插图 + 文案"暂无历史记录"
- **加载中**: 列表显示骨架屏
- **恢复中**: 对应记录的"恢复"按钮显示 loading
- **已达修改上限**: "恢复"按钮置灰，Tooltip 提示"今日修改次数已达上限"

---

## 4. 原型生成要求

### 4.1 组件库

**主组件库**: Ant Design Vue 4（项目已集成，自动导入，无需手动 import）

**复用现有组件**:
| 需求场景 | 推荐组件 | 路径 |
|----------|----------|------|
| 资料编辑表单 | Form（配置式表单） | `src/components/Form/` |
| 头像裁剪弹窗 | Modal + Cropper | `src/components/Modal/` + `src/components/Cropper/` |
| 主页设置页面 | Page 容器 | `src/components/Page/` |
| 模块排序列表 | 自定义拖拽 + Switch | vuedraggable + Ant Design Vue Switch |
| 认证标识 | Badge / 自定义组件 | Ant Design Vue Badge / Tooltip |
| 认证详情弹窗 | Modal | `src/components/Modal/` |
| 隐私设置列表 | Form + Select | `src/components/Form/` |
| 历史记录列表 | Tabs + List | Ant Design Vue Tabs / List |
| 文件上传 | Upload | `src/components/Upload/` |
| 消息提示 | useMessage | `src/hooks/web/` |
| 权限判断 | usePermission | `src/hooks/web/` |

### 4.2 主题色

- **主色调**: `--j-global-primary-color`（动态设置，预设色板含 `#0960bd`, `#1890ff`, `#009688`, `#536dfe` 等）
- 认证标识颜色见 3.6 节表格
- 主页主题色由用户自定义，通过 CSS 变量动态切换

### 4.3 响应式适配

#### 编辑资料页

| 屏幕尺寸 | 布局变化 |
|----------|----------|
| PC 端（≥1200px） | 表单居中，最大宽度 640px，两列布局（左标签右输入） |
| 平板端（768-1199px） | 表单居中，最大宽度 480px，两列布局 |
| 移动端（<768px） | 表单全宽，单列布局（标签在输入框上方），头像居中缩小 |

#### 主页设置页

| 屏幕尺寸 | 布局变化 |
|----------|----------|
| PC 端 | 背景图设置和主题色设置左右分栏，右侧实时预览 |
| 移动端 | 上下堆叠，预览区折叠，点击"预览"展开全屏预览 |

**移动端预览入口规范**:
- **预览按钮位置**: 位于主题色设置区域下方，与保存按钮同级，作为主要操作之一
- **按钮样式**: 带图标的文字按钮（图标使用 EyeOutlined），文案"预览主页效果"，使用默认按钮样式（非主色）
- **展开交互**: 点击后以全屏 Drawer 形式从底部展开预览，预览内容为缩小版主页（按屏幕宽度 85% 缩放）
- **关闭方式**: 点击右上角关闭按钮、下滑手势、或点击背景蒙层关闭
- **实时同步**: 预览打开后，背景图和主题色的修改实时同步到预览中

#### 隐私设置页

| 屏幕尺寸 | 布局变化 |
|----------|----------|
| PC 端 | 列表最大宽度 640px 居中，每行字段名 + 可见性选择左右排列 |
| 移动端 | 全宽单列，字段名在上，可见性选择在下 |

#### 历史记录页

| 屏屏尺寸 | 布局变化 |
|----------|----------|
| PC 端 | 列表最大宽度 640px 居中，昵称/头像内容与时间左右排列 |
| 移动端 | 全宽，内容与时间上下排列 |

#### 认证详情弹窗

| 屏幕尺寸 | 布局变化 |
|----------|----------|
| PC 端 | Modal 弹窗，宽度 400px |
| 移动端 | 全屏 Drawer 抽屉 |

### 4.4 通用交互要求

以下交互行为适用于所有页面：
- **加载状态**: 数据请求期间显示骨架屏（Skeleton）
- **空状态**: 无数据时显示空状态插图和引导文案（Empty 组件）
- **错误反馈**: 操作失败时显示明确的错误提示，保留用户已输入内容
- **表单校验**: 必填字段实时校验（change 事件触发），提交时触发全量校验，错误提示显示在对应字段下方
- **防重复提交**: 提交类按钮在请求期间显示 loading 并禁用
- **危险操作确认**: 恢复历史记录、恢复默认主页设置等操作需二次确认弹窗
- **成功反馈**: 操作成功后显示全局消息提示（message.success）
- **返回行为**: 有未保存修改时弹出确认框，无修改时直接返回

---

## 5. API 对接

> **更新说明（2026-06-06）**: 本节已根据后端 `ContentUserProfileController` 实际实现对齐。所有接口以 `ContentUserProfileController` 的 11 个端点为准（路径前缀 `/content/user/profile`），其中 `review/handle` 为后台审核端点前端不对接，实际前端使用 10 个端点。4 个 POST 端点（`/update`、`/homepage/update`、`/homepage/defaults/restore`、`/history/restore`）返回 `ContentUserProfileVO`，`/privacy/update` 返回 `String`（"更新成功"）。后端未提供独立的「上传头像/上传背景图/查询修改次数/查询审核状态/查询隐私修改次数」等接口，对应能力由前端组合现有接口或后端内嵌实现。

### 5.1 资料管理接口（统一端点）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取用户资料 | GET | `/content/user/profile/detail?ownerUserId={uid}&viewerUserId={vid?}` | `ownerUserId` 必填（资料拥有者），`viewerUserId` 选填（当前访问者）。返回完整 `ContentUserProfileVO`，含 `verificationBadges`、`homepageModules`、`profileCompletionState`、`profileReviewStatus` |
| 更新资料 | POST | `/content/user/profile/update?userId={uid}` | **统一端点**，Body 提交 `ContentUserProfileUpdateReq`。可同时更新基础资料 + 主页背景/主题色 + 模块排序 + 认证展示文案；`nickname`、`avatar` 为 `@NotBlank` 必填。返回 `ContentUserProfileVO` |
| 处理资料审核 | POST | `/content/user/profile/review/handle` | 管理员处理资料审核（APPROVED/REJECTED），前端不直接调用 |

#### 5.1.1 `ContentUserProfileUpdateReq` 字段

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `nickname` | String | ✅ | max 20 | 昵称 |
| `avatar` | String | ✅ | max 500 | 头像 URL（由前端 OSS 直传后获得，非 multipart 上传） |
| `bio` | String | ❌ | max 500 | 个人简介 |
| `gender` | String | ❌ | — | 性别（枚举：`MALE` / `FEMALE` / `OTHER` / `UNKNOWN`） |
| `birthday` | Date | ❌ | — | 生日（ISO 8601 字符串） |
| `region` | String | ❌ | max 64 | 地区 |
| `profession` | String | ❌ | max 64 | 职业 |
| `personalLink` | String | ❌ | max 255，regex `^(https?://\|/).*$` | 个人链接 |
| `homepageBackground` | String | ❌ | max 500 | 主页背景图 URL |
| `themeColor` | String | ❌ | max 16 | 主题色（HEX，`#[0-9A-Fa-f]{6}`） |
| `moduleOrderJson` | String | ❌ | max 2000 | 主页模块排序 JSON 字符串 |
| `certificationType` | String | ❌ | max 64 | 认证类型 |
| `certificationLabel` | String | ❌ | max 64 | 认证展示文案 |

#### 5.1.2 `ContentUserProfileVO` 关键字段

`userId`、`nickname`、`avatar`、`bio`、`gender`、`birthday`、`region`、`profession`、`personalLink`、`homepageBackground`、`themeColor`、`moduleOrderJson`、`certificationType`、`certificationLabel`、`verificationBadges`、`homepageModules`、`profileCompletionState`、`profileReviewStatus`、`status`、`level`、`communityRole`

> 注意：后端基于 `viewerUserId` 自动裁剪隐私字段。生日/性别/地区/职业/个人链接不可见时返回 `null`。

#### 5.1.3 头像/背景图上传策略

后端无独立上传接口，前端采用 **OSS 客户端直传** 模式：
1. 前端调用统一的文件上传组件（复用 JeecgBoot 既有 OSS 上传通道）
2. 上传成功后获得 CDN URL
3. URL 作为 `avatar` 或 `homepageBackground` 字段提交 `/content/user/profile/update`
4. 头像裁剪仍由前端完成（复用 `Cropper` 组件，1:1 锁定）

### 5.2 主页设置接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 更新主页配置 | POST | `/content/user/profile/homepage/update?userId={uid}` | Body 提交 `ContentUserHomepageUpdateReq`（含背景、主题色、模块列表）。返回 `ContentUserProfileVO` |
| 恢复主页默认 | POST | `/content/user/profile/homepage/defaults/restore?userId={uid}` | 恢复默认背景、主题色、模块配置。返回 `ContentUserProfileVO` |
| 查询主页模块 | GET | `/content/user/profile/homepage/modules?userId={uid}` | 返回 `List<ContentUserHomepageModuleVO>`（含 `moduleKey`、`moduleName`、`visible`、`sortOrder`） |

> **简化说明**: 主页配置不再独立于资料更新——`ContentUserProfileUpdateReq` 内的 `homepageBackground`、`themeColor`、`moduleOrderJson` 三个字段已能覆盖主页设置的写入需求。`/profile/homepage/update` 端点用于**单独**提交主页配置（不修改昵称/简介等基础资料）的场景。

#### 5.2.1 `ContentUserHomepageUpdateReq` 字段

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| `homepageBackground` | String | ❌ | max 500 |
| `themeColor` | String | ❌ | max 32 |
| `modules` | `List<ContentUserHomepageModuleReq>` | ❌ | 可空 |

`ContentUserHomepageModuleReq`：`moduleKey` (必填), `visible` (Boolean 必填), `sortOrder` (Integer 必填)

### 5.3 认证标识接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询认证标识列表 | GET | `/content/user/profile/badge/list?userId={uid}` | 返回 `List<ContentUserVerificationBadgeVO>`，已按隐私裁剪 |
| 查询认证详情 | GET | `/content/user/profile/badge/detail?badgeId={id}` | 返回 `ContentUserVerificationBadgeVO` |

> **简化说明**: `ContentUserProfileVO.verificationBadges` 字段已内嵌认证列表，**编辑资料页无需额外请求**。仅"认证详情弹窗"需独立请求 `/badge/detail`。

#### 5.3.1 `ContentUserVerificationBadgeVO` 字段

`id`、`badgeType`（注意：不是 `type`）、`badgeLabel`（不是 `label`）、`visualStyleKey`（视觉样式编码：用于映射图标/颜色）、`description`、`verifiedAt`

### 5.4 隐私设置接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 更新隐私配置 | POST | `/content/user/profile/privacy/update?userId={uid}` | Body 提交 `ContentUserPrivacyUpdateReq`，按字段逐项更新。返回 `String`（"更新成功"） |

> **简化说明**: 后端未提供「获取隐私设置」独立接口。隐私字段通过 `/profile/detail` 的响应隐式返回（不可见字段值为 `null`）；前端在打开隐私设置页时从当前 `UserProfileVO` 推导，并维护一份本地 `PrivacyMap` 用于在 UI 上展示所有可选级别。
> 
> 「一键全部设为」「默认可见性」属于前端 UX 概念，前端在批量提交前合并本地状态为 `ContentUserPrivacyUpdateReq` 一次性提交。

#### 5.4.1 `ContentUserPrivacyUpdateReq` 字段（15 visibility + 2 Boolean）

对齐后端 `ContentUserPrivacyUpdateReq` 实际字段，按 D7 分组：

| 分组 | 字段 | 可见性枚举 | 说明 |
|------|------|-----------|------|
| **基础资料** | `bioVisibility` | `PUBLIC` / `FOLLOWERS_ONLY` / `MUTUAL_ONLY` / `PRIVATE` | 简介可见性 |
| | `genderVisibility` | 同上 | 性别可见性 |
| | `birthdayVisibility` | 同上 | 生日可见性 |
| | `regionVisibility` | 同上 | 地区可见性 |
| | `professionVisibility` | 同上 | 职业可见性 |
| **扩展资料** | `personalLinkVisibility` | 同上 | 个人链接可见性 |
| **主页** | `homepageBackgroundVisibility` | 同上 | 主页背景可见性 |
| | `themeColorVisibility` | 同上 | 主题色可见性 |
| | `homepageModuleVisibility` | 同上 | 主页模块可见性 |
| **认证** | `certificationVisibility` | 同上 | 认证信息可见性 |
| | `verificationBadgesVisibility` | 同上 | 认证标识可见性 |
| **活动** | `profileCompletionVisibility` | 同上 | 资料完善度可见性 |
| | `profileReviewStatusVisibility` | 同上 | 审核状态可见性 |
| | `recentActivityVisibility` | 同上 | 最近活动可见性 |
| **在线状态** | `onlineStatusVisibility` | `PUBLIC` / `HIDDEN` / `MUTUAL_ONLY` | 在线状态可见性（**特殊枚举，不含 PRIVATE**） |
| **布尔开关** | `showMutualFollowersCount` | Boolean | 是否显示互关好友数 |
| | `showRecentActivityHighlight` | Boolean | 是否显示最近活动高亮 |

> **关键约束**:
> 1. 所有 `*Visibility` 字段受 `@Pattern(regexp = "^(PUBLIC\|FOLLOWERS_ONLY\|MUTUAL_ONLY\|PRIVATE)$")` 约束，**前端必须在提交前转换中文标签为枚举值**。
> 2. `onlineStatusVisibility` 特殊：不接受 `PRIVATE`，仅 `PUBLIC`/`HIDDEN`/`MUTUAL_ONLY`。
> 3. 昵称和头像**不参与**隐私控制（始终公开），本接口不提供对应字段。
> 4. 字段总数：15 个 visibility + 2 个 Boolean = 17 个字段。

### 5.5 历史记录接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询历史 | GET | `/content/user/profile/history/list?userId={uid}&historyType={NICKNAME\|AVATAR}` | 返回 `List<ContentUserProfileHistoryVO>`，按 `historyType` 区分 |
| 恢复历史 | POST | `/content/user/profile/history/restore?userId={uid}&historyId={id}` | 恢复指定历史值为当前值（仍走资料修改频率限制）。返回 `ContentUserProfileVO` |

#### 5.5.1 `ContentUserProfileHistoryVO` 字段

`id`、`historyType`（`NICKNAME` 或 `AVATAR`）、`historyValue`（昵称文本或头像 URL）、`changedAt`、`expiresAt`（后端按 180 天 TTL 清理）

### 5.6 API 封装规范

```typescript
// 使用项目统一封装
import { defHttp } from '/@/utils/http/axios';

// 响应格式（项目 Result<T>）
interface ApiResponse<T> {
  code: number;      // 200 成功，500 业务错误，510 未登录
  result: T;
  message: string;
  success: boolean;
}

// 示例：获取用户资料
defHttp.get<Result<ContentUserProfileVO>>({
  url: '/content/user/profile/detail',
  params: { ownerUserId: 'xxx', viewerUserId: 'yyy' }
});

// 示例：更新资料（统一端点）
defHttp.post<Result<ContentUserProfileVO>>({
  url: '/content/user/profile/update',
  params: { userId: 'xxx' },
  data: profileUpdateReq
});

// 示例：更新隐私
defHttp.post<Result<string>>({
  url: '/content/user/profile/privacy/update',
  params: { userId: 'xxx' },
  data: privacyUpdateReq
});

// 示例：查询历史
defHttp.get<Result<ContentUserProfileHistoryVO[]>>({
  url: '/content/user/profile/history/list',
  params: { userId: 'xxx', historyType: 'NICKNAME' }
});
```

> **前端 userId 来源**: 从 `useUserStore.getUserInfo.id` 读取（已登录用户），无需前端硬编码。

---

## 6. 状态管理

### 6.1 Store 设计

复用现有 `useUserStore`（`src/store/modules/user.ts`），扩展以下状态：

```typescript
// 在 useUserStore 中扩展
interface UserStoreState {
  // 现有状态...
  
  // EPIC-02 新增
  profileCompletionRate: number;        // 资料完善率（百分比）
  dailyUpdateRemaining: number;         // 当日剩余修改次数
  reviewStatus: 'none' | 'pending' | 'approved' | 'rejected'; // 审核状态
  reviewReason?: string;                // 审核不通过原因
}
```

### 6.2 页面级状态

各页面使用组件内部 state 管理页面状态，不提升到全局 Store：

| 页面 | 状态 | 说明 |
|------|------|------|
| 编辑资料页 | formData, isDirty, isSubmitting | 表单数据、是否修改、提交中 |
| 主页设置页 | config, previewTheme, isDirty | 配置数据、预览主题、是否修改 |
| 隐私设置页 | privacyMap, isDirty | 字段可见性映射、是否修改 |
| 历史记录页 | activeTab, historyList, restoringId | 当前 Tab、历史列表、恢复中的记录 ID |

### 6.3 缓存策略

- 用户资料数据缓存在 `useUserStore` 中，隐私设置保存后主动刷新
- 认证标识数据请求后缓存在组件内部，切换用户时重新加载
- 历史记录不缓存，每次进入页面重新加载

---

## 7. 交互设计

### 7.1 核心交互流程

#### 流程 1: 编辑资料
```
进入编辑页 → 加载当前资料 → 用户修改字段 → 实时校验
→ 点击保存 → 检查频率限制 → 提交请求
→ 成功：显示"资料已更新"，返回个人中心
→ 失败（审核中）：提示"资料正在审核中"
→ 失败（频率限制）：提示"今日修改次数已达上限"
→ 失败（敏感词）：提示"昵称包含不当内容"
→ 失败（网络错误）：提示"网络异常，请重试"
```

#### 流程 2: 上传头像
```
点击头像区 → 触发文件选择 → 校验格式/大小
→ 不通过：提示错误信息
→ 通过：打开裁剪弹窗 → 裁剪 → 点击确定
→ 上传中：显示进度 → 成功：更新头像预览
→ 失败：提示"上传失败，请重试"
```

#### 流程 3: 设置隐私
```
进入隐私设置 → 加载当前设置 → 修改可见性
→ 点击保存 → 检查频率限制 → 提交请求
→ 成功：提示"隐私设置已更新，新设置将立即对新访问者生效"，刷新本地缓存
→ 失败（频率限制）：提示"操作过于频繁"
→ 失败（网络错误）：保留修改，提示重试
```

#### 流程 4: 恢复历史记录
```
进入历史记录 → 选择 Tab（昵称/头像） → 浏览列表
→ 点击"恢复" → 确认弹窗"确定恢复为 xxx 吗？"
→ 确认 → 提交恢复请求（等同于新修改，受频率限制）
→ 成功：提示"已恢复"，更新当前资料
→ 失败（昵称已占用）：提示"该昵称已被使用"
→ 失败（频率限制）：提示"今日修改次数已达上限"
```

### 7.2 状态反馈设计

| 场景 | 反馈方式 | 组件 |
|------|----------|------|
| 操作成功 | 页面顶部全局消息 | message.success |
| 操作失败 | 页面顶部全局消息 | message.error |
| 审核状态变更 | 右上角通知 | Notification |
| 字段校验失败 | 字段下方红色文字 | Form.Item validateState |
| 频率限制 | 表单顶部黄色提示条 | Alert |
| 数据加载中 | 内容区骨架屏 | Skeleton |
| 按钮操作中 | 按钮内 loading 图标 | Button loading |
| 危险操作确认 | 居中弹窗 | Modal.confirm |

---

## 8. 响应式设计

### 8.1 断点定义

| 断点 | 宽度范围 | 设备 |
|------|----------|------|
| xs | < 576px | 手机竖屏 |
| sm | 576-767px | 手机横屏 |
| md | 768-991px | 平板 |
| lg | 992-1199px | 小桌面 |
| xl | ≥ 1200px | 标准桌面 |

### 8.2 通用响应式规则

- 所有表单页面：PC 端居中最大宽度 640px，移动端全宽
- 弹窗：PC 端 Modal，移动端 Drawer（底部弹出）
- 操作按钮：PC 端在表单底部，移动端固定在屏幕底部
- 认证标识：PC 端 inline 显示，移动端保持 inline 但缩小图标尺寸
- 历史记录列表：PC 端左右排列，移动端上下堆叠

### 8.3 移动端特殊处理

- 头像裁剪弹窗：移动端全屏展示裁剪区域，裁剪预览区改为底部横向排列
- 主题色选择：移动端色板改为两列网格
- 模块排序：移动端采用长按（300ms）触发拖拽模式，拖拽手柄最小触摸热区 44px x 44px，拖拽过程中列表项增加阴影效果，支持键盘无障碍操作
- 隐私设置：移动端 Select 组件改为 ActionSheet 底部选择器
- 底部操作栏：所有固定在屏幕底部的操作按钮需适配 iPhone Safe Area，使用 `padding-bottom: env(safe-area-inset-bottom)` 避免被 Home Indicator 遮挡
- 主页预览：移动端预览按钮使用带图标的文字按钮，点击后以全屏 Drawer 形式展开预览

---

## 9. 性能要求

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 页面首屏加载 | < 2s | 编辑资料页、主页设置页、隐私设置页 |
| 头像上传 | < 5s | 包含裁剪和上传，5MB 图片 |
| 背景图上传 | < 8s | 包含裁剪和上传，5MB 图片 |
| 资料保存响应 | < 1s | 接口响应时间 |
| 历史记录加载 | < 1s | 最多 20 条数据 |
| 认证标识渲染 | 无延迟感 | 随页面一起渲染，不阻塞主要内容 |
| 主题色切换 | 即时 | CSS 变量切换，无网络请求 |

**优化策略**:
- 头像和背景图上传前进行客户端压缩，减少上传体积
- 使用骨架屏提升加载感知体验
- 认证标识数据随用户资料接口一起返回，避免额外请求
- 历史记录列表使用虚拟滚动（如果数据量较大）
- 表单校验使用防抖（300ms）避免频繁触发
- 主题色对比度校验使用 Web Worker 计算，避免阻塞主线程
- 认证标识折叠状态使用 CSS transition，避免频繁 DOM 操作

---

## 10. 测试要点

### 10.1 功能测试

| 测试项 | 测试场景 | 预期结果 |
|--------|----------|----------|
| 资料编辑 | 填写所有字段并保存 | 资料保存成功，显示"资料已更新" |
| 必填校验 | 不填昵称直接保存 | 提示"请输入昵称"，阻止提交 |
| 敏感词校验 | 输入含敏感词的昵称 | 提示"昵称包含不当内容" |
| 头像格式校验 | 上传 BMP 格式图片 | 提示"仅支持 JPG、PNG、WebP 格式" |
| 头像大小校验 | 上传超过 5MB 图片 | 提示"图片大小必须小于 5MB" |
| 频率限制 | 连续修改 6 次资料 | 第 6 次提示"今日修改次数已达上限" |
| 审核状态 | 提交含敏感词资料后查看 | 显示"资料正在审核中"提示 |
| 背景图上传 | 上传有效背景图并保存 | 主页背景更新 |
| 主题色切换 | 选择预设颜色 | 预览区实时更新主题色效果 |
| 主题色对比度校验 | 选择浅色主题色（如 #fffffe） | 显示对比度不足警告，文字颜色自动调整为黑色 |
| 模块排序 | 拖拽调整模块顺序 | 保存后主页按新顺序展示 |
| 模块全隐藏 | 关闭所有模块开关 | 保存按钮禁用，提示"至少保留一个模块" |
| 移动端模块拖拽 | 长按模块列表项 | 进入拖拽模式，列表项增加阴影，支持拖拽排序 |
| 恢复默认 | 点击恢复默认主页 | 清除自定义背景、主题色、模块配置 |
| 认证标识 | 已认证用户查看主页 | 昵称旁显示对应认证标识 |
| 认证详情 | 点击认证标识 | 弹窗显示认证类型、时间、说明 |
| 认证标识折叠 | 用户有 3 个以上认证 | 显示前 2 个标识 + "+1" 徽标 |
| 长昵称截断 | 昵称接近 20 字符 + 多个认证 | 昵称显示省略号，认证标识正常显示 |
| 隐私设置 | 将生日设为"仅自己" | 其他用户无法看到生日 |
| 隐私批量操作 | 一键全部设为"仅自己" | 所有可修改字段批量更新，显示撤销按钮 |
| 隐私频率限制 | 1 小时内修改 11 次 | 第 11 次提示"操作过于频繁" |
| 头像裁剪预览 | 上传头像后查看裁剪弹窗 | 显示圆形和方形预览效果 |
| 昵称历史 | 修改昵称后查看历史 | 历史列表包含旧昵称，当前昵称显示"当前"标签 |
| 头像历史 | 修改头像后查看历史 | 历史列表包含旧头像，当前头像显示"当前"标签 |
| 恢复历史昵称 | 从历史恢复曾用昵称 | 当前昵称更新为历史值 |
| 昵称冲突恢复 | 恢复已被占用的昵称 | 提示"该昵称已被使用" |

### 10.2 交互测试

| 测试项 | 测试场景 | 预期结果 |
|--------|----------|----------|
| 防重复提交 | 快速点击保存按钮多次 | 只发送一次请求，按钮显示 loading |
| 未保存离开 | 修改资料后点击返回 | 弹出确认框"确定离开吗？" |
| 审核中返回 | 资料审核中点击返回 | 直接返回，不弹出确认框 |
| 表单校验 | 输入超长昵称 | 字段下方显示红色错误提示 |
| 加载状态 | 进入页面等待数据 | 显示骨架屏 |
| 空状态 | 无历史记录时进入历史页 | 显示空状态插图和引导文案 |
| 成功反馈 | 保存资料成功 | 页面顶部显示绿色成功消息 |
| 移动端拖拽 | 长按模块列表项 300ms | 进入拖拽模式，显示蓝色插入线 |
| 批量操作确认 | 点击"一键全部设为" | 弹出确认框，确认后批量更新 |
| 批量操作撤销 | 批量操作后点击撤销 | 恢复操作前状态，5 秒后撤销按钮消失 |
| 预览入口 | 移动端点击"预览主页效果" | 全屏 Drawer 展开主页预览 |
| Safe Area 适配 | iPhone 上查看底部操作栏 | 按钮不被 Home Indicator 遮挡 |

### 10.3 响应式测试

| 测试项 | 测试场景 | 预期结果 |
|--------|----------|----------|
| 移动端编辑 | 手机上编辑资料 | 表单单列布局，操作按钮固定底部，适配 Safe Area |
| 移动端裁剪 | 手机上裁剪头像 | 全屏裁剪弹窗，裁剪预览区底部横向排列 |
| 移动端隐私 | 手机上设置隐私 | 底部弹出选择器 |
| 移动端模块拖拽 | 手机上长按模块列表项 | 进入拖拽模式，触摸热区 44px，拖拽有阴影反馈 |
| 移动端主页预览 | 手机上点击预览按钮 | 全屏 Drawer 展开预览，支持下滑关闭 |
| 移动端认证标识 | 手机上查看长昵称+多认证 | 昵称省略号截断，认证标识最多显示 2 个+"+N" |
| 平板端主页设置 | 平板上设置主页 | 背景和主题色左右分栏 |

### 10.4 边界测试

| 测试项 | 测试场景 | 预期结果 |
|--------|----------|----------|
| 极长输入 | 昵称输入 20 个字符 | 正常保存 |
| 超长输入 | 昵称输入 21 个字符 | 阻止输入/提示超长 |
| 特殊字符 | 昵称含 emoji、特殊符号 | 按业务规则处理 |
| 网络断开 | 保存时网络断开 | 提示"网络异常，请重试"，保留输入 |
| 并发修改 | 多标签页同时编辑资料 | 后保存的覆盖先保存的，或提示冲突 |
| 历史记录上限 | 累积超过 20 条历史 | 只显示最新 20 条，FIFO 删除最旧 |

---

## 11. 待确认问题 / 默认假设

### 待确认问题

1. **昵称唯一性**: 昵称是否全站唯一？当前假设恢复历史昵称时需校验唯一性，但日常修改是否也需要？
2. **头像上传方式**: 头像上传是内容社区模块自行提供接口，还是复用 JeecgBoot 现有文件上传能力后仅保存 URL？
3. **手机号/邮箱验证标识默认可见性**: 默认公开还是默认仅自己可见后由用户选择？
4. **认证标识申请入口**: 本期是否需要前端提供认证申请入口，还是仅展示后端已有的认证数据？
5. **主页模块列表**: 具体有哪些可配置模块（动态、收藏、成就、关注、粉丝、相册、音乐...），需要后端接口返回还是前端硬编码？
6. **地区数据源**: 省市区级联数据从哪里获取？是否复用现有字典或接口？

### 默认假设

1. **认证标识数据**: 假设认证标识通过后端接口返回，前端只负责展示，不实现认证申请流程
2. **审核机制**: 假设资料审核由后端自动处理（AI + 人工），前端只展示审核状态和结果
3. **敏感词过滤**: 假设敏感词校验在后端执行，前端只展示错误提示
4. **图片处理**: 假设图片压缩、多分辨率生成由后端/CDN 处理，前端只负责上传和裁剪
5. **缓存策略**: 假设缓存由后端 Redis 管理，前端在隐私设置保存后主动刷新本地数据
6. **默认主页模块**: 假设默认模块列表为：动态、收藏、成就、关注、粉丝
7. **主题色预设**: 假设主题色预设为 8 个常用颜色，具体色值需与设计确认
8. **历史记录清理**: 假设 180 天过期清理由后端定时任务处理，前端不做过期判断
