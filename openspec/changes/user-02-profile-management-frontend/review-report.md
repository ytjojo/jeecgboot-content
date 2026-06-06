# Review Report: user-02-profile-management-frontend

> **审核时间**: 2026-06-06
> **审核范围**: proposal.md, design.md, specs/* (5 个), tasks.md, backend-issues.md, verification-review.md
> **前端 PRD**: docs/requirements/prd/frontend/EPIC-02-profile-management-frontend-prd.md
> **配对后端 change**: user-02-profile-management (已 archived)
> **Change 类型**: 前端 change
> **状态**: all_done (79/79 tasks complete)

---

## 一、总览表

| 维度 | 得分 | BLOCK | FLAG | ADVISORY | 说明 |
|------|------|-------|------|----------|------|
| 完整性 (Completeness) | 8/10 | 0 | 2 | 3 | artifacts 齐全，PRD 隐私字段定义与实现脱节 |
| 一致性 (Consistency) | 5/10 | 2 | 3 | 2 | POST 返回类型矛盾未修复、PRD↔design 隐私字段严重分歧 |
| 可实现性 (Feasibility) | 9/10 | 0 | 0 | 1 | 技术栈兼容，架构合理 |
| 可测试性 (Testability) | 8/10 | 0 | 1 | 2 | Scenario 可量化，缺少部分负面路径 |
| 接口契约 (API Contract) | 6/10 | 1 | 2 | 1 | 返回类型不一致、PRD gender 类型矛盾 |
| 边界覆盖 (Boundary) | 5/10 | 0 | 2 | 6 | 缺少 OSS 凭证过期、并发编辑、安全等边界 |

**综合得分: 6.8/10**

---

## 二、量化指标

| 指标 | 数值 | 说明 |
|------|------|------|
| PRD 功能覆盖率 | 9/9 (100%) | F1-F9 全部有对应 spec 或 design 决策覆盖 |
| PRD 隐私字段对齐率 | 0/15 (0%) | 前端 PRD 的隐私字段名与后端/设计完全不一致（详见 BLOCK-1） |
| API 端点覆盖 | 10/10 (100%) | 前端使用的 10 个端点全部有 spec 定义 |
| API 返回类型一致率 | 6/10 (60%) | 4 个 POST 端点返回类型在文档间矛盾 |
| Spec Scenario 数量 | 35 个 | 5 个 spec 共 35 个 Scenario |
| 边界条件覆盖率 | 4/10 (40%) | 覆盖：空值、超长、格式校验、网络错误；缺失：凭证过期、并发、安全等 |
| TDD 配对率 | 5/5 (100%) | 5 个功能模块均有对应单元测试任务 (12.1-12.5) |
| Tasks 完成率 | 79/79 (100%) | 所有任务已标记完成 |

---

## 三、各维度详细审核

### 3.1 完整性 (Completeness) — 8/10

**文档结构完整性**: 所有必需 artifacts 齐全（proposal, design, 5 specs, tasks），额外文件包括 backend-issues.md 和 verification-review.md。

**内容覆盖**:

| PRD 功能 | 对应 Spec/决策 | 覆盖状态 |
|----------|---------------|----------|
| F1 基础资料编辑 | specs/profile-editing | 完整 |
| F2 头像上传与校验 | specs/profile-editing (OSS 直传 Scenario) | 完整 |
| F3 资料审核状态 | specs/profile-editing (review status Scenario) | 完整 |
| F4 主页背景与主题色 | specs/homepage-customization | 完整 |
| F5 主页模块配置 | specs/homepage-customization (module Scenario) | 完整 |
| F6 认证标识展示 | specs/verification-badge | 完整 |
| F7 字段可见性设置 | specs/privacy-settings | 完整 |
| F8 隐私缓存生效 | specs/privacy-settings + design D6 | 完整 |
| F9 昵称/头像历史 | specs/profile-history | 完整 |

**FLAG-1**: 前端 PRD 隐私字段定义（4 组，字段名如 `homepageVisibility`、`dynamicVisibility`）与 design.md D7（7 组，字段名如 `homepageBackgroundVisibility`、`homepageModuleVisibility`）完全不同。PRD 未更新以反映后端实际契约。

**FLAG-2**: 前端 PRD 3.1 节页面结构仍包含"提示条（今日还可修改 X 次）"和"频率限制交互"小节，但 design.md D9 和 proposal.md 已明确决定不展示频率限制。PRD 与设计决策不一致。

**ADVISORY-1**: PRD 3.7 节隐私设置页面结构中的分组（基础资料/认证与绑定/主页与动态/搜索与发现）与 D7 的 7 分组不同，PRD 结构已过时。

**ADVISORY-2**: PRD 5.4.1 节 `ContentUserPrivacyUpdateReq` 字段表包含 `onlineStatusVisible`（Boolean，旧字段）和 `allowSearchEngineIndex`、`allowUserSearch` 等字段，但后端 spec 中这些字段不在 15 个 visibility + 2 个 Boolean 范围内。字段定义存在版本差异。

**ADVISORY-3**: PRD 6.1 节 Store 设计包含 `dailyUpdateRemaining` 字段，但 design.md 和 proposal.md 均已决定不实现此字段（后端不暴露 update-count 接口）。

### 3.2 一致性 (Consistency) — 5/10

**BLOCK-1: POST 端点返回类型矛盾未修复**

verification-review.md 声称"已修复"4 个 POST 端点返回类型，但 design.md API 对接矩阵中仍标注为 `ContentUserProfileVO`（待改造）。实际上：

| 端点 | design.md 矩阵声称 | verification-review.md 声称 | proposal.md 最新描述 |
|------|---------------------|---------------------------|---------------------|
| `POST /profile/update` | `ContentUserProfileVO`（待改造） | 已改造为 `Result<ContentUserProfileVO>` | `Result<String>` |
| `POST /homepage/update` | `ContentUserProfileVO`（待改造） | 已改造为 `Result<ContentUserProfileVO>` | `Result<String>` |
| `POST /homepage/defaults/restore` | `ContentUserProfileVO`（待改造） | 已改造为 `Result<ContentUserProfileVO>` | `Result<String>` |
| `POST /history/restore` | `ContentUserProfileVO`（待改造） | 已改造为 `Result<ContentUserProfileVO>` | `Result<String>` |

三处文档对同一端点的返回类型描述互相矛盾，tasks.md 按"后端改造后返回 VO"编写。**需要明确最终实际返回类型并统一所有文档**。

**BLOCK-2: 前端 PRD 隐私字段与后端/设计完全不匹配**

前端 PRD 定义的隐私字段（14 个 visibility + 2 个 switch）与后端 `ContentUserPrivacyUpdateReq`（15 个 visibility + 2 个 Boolean）字段名完全不同：

| 前端 PRD 字段 | 后端/Design D7 字段 | 匹配 |
|--------------|-------------------|------|
| `birthdayVisibility` | `birthdayVisibility` | 匹配 |
| `genderVisibility` | `genderVisibility` | 匹配 |
| `regionVisibility` | `regionVisibility` | 匹配 |
| `professionVisibility` | `professionVisibility` | 匹配 |
| `personalLinkVisibility` | `personalLinkVisibility` | 匹配 |
| `verificationBadgeVisibility` | `verificationBadgesVisibility` | **不匹配** (单复数) |
| `contactBadgeVisibility` | 不存在 | **PRD 多出** |
| `homepageVisibility` | `homepageBackgroundVisibility` | **不匹配** |
| `dynamicVisibility` | `homepageModuleVisibility` | **不匹配** |
| `browseHistoryVisibility` | `profileCompletionVisibility` | **不匹配** |
| `likeActivityVisibility` | `profileReviewStatusVisibility` | **不匹配** |
| `favoriteVisibility` | `recentActivityVisibility` | **不匹配** |
| `onlineStatusVisibility` | `onlineStatusVisibility` | 匹配 |
| `allowSearchEngineIndex` | `showMutualFollowersCount` | **不匹配** |
| `allowUserSearch` | `showRecentActivityHighlight` | **不匹配** |

后端多出的字段：`bioVisibility`、`certificationVisibility`、`themeColorVisibility`。
前端 PRD 多出的字段：`contactBadgeVisibility`、`allowSearchEngineIndex`、`allowUserSearch`。

**结论**: 前端 PRD 是旧版本，未根据后端实际 `ContentUserPrivacyUpdateReq` 更新。tasks.md 和 specs 按 D7（后端对齐）实现，因此**实现代码是正确的，但 PRD 文档需要同步更新**。

**FLAG-3**: design.md 仍引用 `ContentUserPrivacySettingVO` 类型（后端不存在此类型）。verification-review.md 声称已删除引用，但 design.md 端点表中 `POST /privacy/update` 出参仍标注为 `ContentUserPrivacySettingVO`。

**FLAG-4**: ContentUserProfileUpdateReq 的 gender 字段类型矛盾：
- 前端 PRD: `Integer`（1=男/2=女/3=保密）
- design.md / 后端 spec: `String`（`MALE|FEMALE|OTHER|UNKNOWN`）
- tasks.md 按 String 枚举实现

**ADVISORY-4**: design.md Goals 中写"对接后端 12 个端点"，但 API 矩阵实际列出 11 行，且正文说明"11 个端点（其中 1 个为后台审核，前端不对接）"。端点数量描述不一致。

**ADVISORY-5**: 后端 spec 中 `ContentUserProfileHistoryVO` 字段名使用 `changedAt`，但前端 PRD 使用 `createTime`，design.md 未明确。需要确认后端实际字段名。

### 3.3 可实现性 (Feasibility) — 9/10

**技术栈兼容性**: 全部通过
- Vue 3 + TypeScript + Ant Design Vue 4：项目既有技术栈，无冲突
- cropperjs：成熟裁剪库，Vue 3 兼容
- vuedraggable：Vue 3 版本可用，Sortable.js 底层稳定
- OSS 客户端直传：JeecgBoot 已有 STS 凭证获取通道可复用
- Pinia 扩展 useUserStore：标准做法，无架构风险

**架构规范**:
- 路由组织 `/content/profile/*`：符合项目模块化路由约定
- 状态管理扩展 vs 新建 Store：选择扩展 useUserStore 合理（D2）
- 组件封装：AvatarCropper、VerificationBadge 为纯展示/交互组件，职责清晰

**ADVISORY-6**: OSS STS 临时凭证获取的后端接口未在本文档中明确定义。design.md D3 提到"前端向后端换取 STS 临时凭证"，但未指定具体端点。假设复用 JeecgBoot 既有 `/sys/file/upload` 或类似接口的 STS 能力。

### 3.4 可测试性 (Testability) — 8/10

**Scenario 质量评估**:

| Spec | Scenario 数量 | 正面路径 | 负面路径 | 边界场景 | 可量化 |
|------|-------------|---------|---------|---------|--------|
| profile-editing | 13 | 7 | 4 | 2 | 是 |
| homepage-customization | 12 | 7 | 2 | 3 | 是 |
| privacy-settings | 10 | 6 | 2 | 2 | 是 |
| profile-history | 9 | 5 | 2 | 2 | 是 |
| verification-badge | 8 | 5 | 2 | 1 | 是 |

**TDD 配对**: tasks.md 第 12 节明确为每个功能模块编写单元测试（12.1-12.5），12.6 全量测试运行。

**FLAG-5**: profile-editing spec 缺少"审核中重复提交"的负面 Scenario（后端 spec 中有 `Pending review blocks another profile change`，但前端 spec 未覆盖）。

**ADVISORY-7**: homepage-customization spec 缺少"OSS 上传超时"和"主题色自定义输入非法值"的负面 Scenario。

**ADVISORY-8**: verification-badge spec 缺少"badge 数据加载失败"的错误处理 Scenario。

### 3.5 接口契约 (API Contract) — 6/10

**BLOCK-1（同 3.2）**: 4 个 POST 端点返回类型在 design.md、proposal.md、verification-review.md 之间矛盾。

**端点路径一致性**: 全部 10 个前端使用端点路径与后端 Controller 完全一致（backend-issues.md 已确认无偏差）。

**入参对齐检查**:

| 端点 | 前端声明入参 | 后端声明入参 | 一致 |
|------|-------------|-------------|------|
| GET /detail | ownerUserId, viewerUserId | ownerUserId, viewerUserId | 是 |
| POST /update | userId (query) + body | userId (query) + body | 是 |
| POST /privacy/update | userId (query) + body | userId (query) + body | 是 |
| POST /homepage/update | userId (query) + body | userId (query) + body | 是 |
| POST /homepage/defaults/restore | userId (query) | userId (query) | 是 |
| GET /homepage/modules | userId (query) | userId (query) | 是 |
| GET /badge/list | userId (query) | userId (query) | 是 |
| GET /badge/detail | badgeId (query) | badgeId (query) | 是 |
| GET /history/list | userId, historyType | userId, historyType | 是 |
| POST /history/restore | userId, historyId (query) | userId, historyId (query) | 是 |

**FLAG-6**: 前端 spec `profile-history` 中 "Restore historical value" Scenario 声称后端返回 `Result<String>`（"恢复成功"），但 design.md API 矩阵标注为 `ContentUserProfileVO`（待改造）。同 BLOCK-1 矛盾。

**ADVISORY-9**: `ContentUserProfileUpdateReq` 中 `themeColor` 约束为 `@Size(max=16) @Pattern(regexp="^#[0-9A-Fa-f]{6}$")`（含 # 号共 7 字符，max=16 有余量），但前端 PRD 描述为"max 32"。后端约束更严格，前端应以后端为准。

### 3.6 边界覆盖 (Boundary) — 5/10

**已覆盖的边界条件**:

| 类型 | 覆盖场景 | 来源 |
|------|---------|------|
| 空值/必填 | nickname 必填、avatar 必填 | specs/profile-editing |
| 超长输入 | nickname ≤30、bio ≤500、personalLink ≤256 | specs/profile-editing |
| 格式校验 | URL 格式、生日禁未来日期、文件格式 | specs/profile-editing |
| 网络异常 | 网络错误提示、保留已输入内容 | specs/* 各 spec |

**缺失的边界条件**:

| 类型 | 缺失场景 | 风险等级 |
|------|---------|---------|
| **OSS 凭证过期** | STS Token 过期后上传失败的处理流程 | HIGH |
| **并发编辑** | 多标签页/多设备同时编辑同一资料的冲突处理 | MEDIUM |
| **XSS/CSRF** | 昵称/简介中注入脚本、CSRF Token 校验 | HIGH |
| **浏览器兼容** | cropperjs Canvas 在 Safari/iOS WebView 的兼容性 | MEDIUM |
| **大数据量** | 历史记录接近 20 条时的渲染性能 | LOW |
| **存储限制** | OSS 上传时本地临时文件占用 | LOW |
| **内存泄漏** | cropperjs 实例销毁、vuedraggable 事件解绑 | MEDIUM |
| **弱网环境** | 大图片上传在网络波动时的重试策略 | MEDIUM |
| **无障碍** | 键盘导航、屏幕阅读器兼容（WCAG 2.1 AA） | MEDIUM |
| **i18n** | 枚举标签（PUBLIC 等）的多语言映射 | LOW |

---

## 四、前后端衔接审计

### 4.1 接口清单双向对比

| 对比项 | 结果 |
|--------|------|
| 前端引用端点 vs 后端定义端点 | 10/10 完全匹配，无遗漏、无多余 |
| API 路径一致性 | 100%（backend-issues.md 已确认） |
| HTTP 方法一致性 | 100% |

### 4.2 数据模型一致性

| 对比项 | 前端 | 后端 | 一致 |
|--------|------|------|------|
| `ContentUserProfileVO` 字段 | design.md 列出 | 后端 spec 覆盖 | 是 |
| `ContentUserProfileUpdateReq` gender | PRD: Integer / design: String | String (MALE\|FEMALE\|OTHER\|UNKNOWN) | **design 一致，PRD 不一致** |
| `ContentUserPrivacyUpdateReq` 字段 | PRD: 14 个 + 2 switch | design D7: 15 + 2 Boolean | **PRD 不一致**（详见 BLOCK-2） |
| `ContentUserVerificationBadgeVO` | design.md 定义 | 后端 spec 一致 | 是 |
| `ContentUserHomepageModuleVO` | design.md 定义 | 后端 spec 一致 | 是 |
| `ContentUserProfileHistoryVO` | PRD: createTime | 后端: changedAt | **字段名待确认** |

### 4.3 错误码覆盖检查

| 场景 | 前端处理 | 后端定义 | 覆盖 |
|------|---------|---------|------|
| 敏感词拦截 | toast 提示"昵称包含不当内容" | 后端审核拒绝 | 是 |
| 昵称已被占用 | toast 提示"该昵称已被使用" | 后端唯一性校验 | 是 |
| 频率限制超限 | 被动拦截，toast 提示 | 后端 service 层频控 | 部分（错误码未约定） |
| 未登录 | defHttp 自动处理 510 | Result code=510 | 是 |
| 字段校验失败 | 解析 Result.message 回填 | @NotBlank/@Size/@Pattern | 是 |

**ADVISORY-10**: 频率限制的后端错误码未在文档中明确定义（PRD 提到"如 code=1101"但标注为"待约定"）。建议在 design.md 中明确错误码映射。

### 4.4 认证鉴权一致性

所有端点均需要 `userId` 参数，前端从 `useUserStore.getUserInfo.id` 读取。后端 Controller 通过 `@LoginUser` 或 SecurityContext 获取当前用户。鉴权链路一致。

### 4.5 分页契约检查

| 接口 | 分页方式 | 前端声明 | 后端声明 | 一致 |
|------|---------|---------|---------|------|
| GET /history/list | 无分页，后端限制 20 条 | 前端展示全部 | 后端返回 ≤20 条 | 是 |
| GET /badge/list | 无分页 | 前端展示全部 | 后端返回全部可见 | 是 |
| GET /homepage/modules | 无分页 | 前端展示全部 | 后端返回全部 | 是 |

---

## 五、PRD 追溯矩阵

| PRD 需求 | PRD 章节 | 对应 Spec | 对应 Design 决策 | 对应 Tasks | 覆盖状态 |
|----------|---------|-----------|-----------------|------------|---------|
| 基础资料编辑 | 3.1 F1 | profile-editing | D1/D2/D3 | 1-5, 16-21 | 完整 |
| 头像上传与校验 | 3.2 F2 | profile-editing | D3 | 22-26 | 完整 |
| 资料审核状态 | 3.3 F3 | profile-editing | D2 | 20 | 完整 |
| 主页背景与主题色 | 3.4 F4 | homepage-customization | D3 | 27-31 | 完整 |
| 主页模块配置 | 3.5 F5 | homepage-customization | D4 | 32-37 | 完整 |
| 认证标识展示 | 3.6 F6 | verification-badge | D5 | 38-43 | 完整 |
| 字段可见性设置 | 3.7 F7 | privacy-settings | D7 | 44-54 | 完整（实现对齐后端，PRD 未更新） |
| 隐私缓存生效 | 3.8 F8 | privacy-settings | D6 | 53 | 完整 |
| 昵称/头像历史 | 3.9 F9 | profile-history | D8 | 55-61 | 完整 |

---

## 六、最终结论与问题清单

### 结论

change `user-02-profile-management-frontend` 的设计文档（design.md）和规格文档（specs/*）质量良好，与后端实际契约对齐度高，tasks.md 覆盖全面且已全部完成。**主要问题集中在前端 PRD 文档未同步更新**，导致 PRD 与实际设计/实现之间存在多处矛盾。

### BLOCK 问题（必须修复后方可 apply）

| # | 文件 | 问题 | 建议修复 |
|---|------|------|---------|
| BLOCK-1 | design.md + proposal.md | 4 个 POST 端点返回类型在三处文档间矛盾（design 矩阵标注"待改造 VO"，verification-review 声称"已改造"，proposal 声称"Result\<String\>"） | 统一为后端实际返回类型，更新 design.md API 矩阵、proposal.md、tasks.md 中所有相关描述 |
| BLOCK-2 | 前端 PRD | 隐私字段定义（14 个 visibility + 字段名）与后端 `ContentUserPrivacyUpdateReq`（15 个 visibility + 2 Boolean）完全不匹配 | 更新前端 PRD 3.7 节和 5.4.1 节，对齐 D7 的 15+2 字段定义 |

### FLAG 问题（建议修复）

| # | 文件 | 问题 | 建议修复 |
|---|------|------|---------|
| FLAG-1 | 前端 PRD | 隐私分组结构（4 组 vs D7 的 7 组）过时 | 更新 PRD 隐私设置页面结构 |
| FLAG-2 | 前端 PRD | 仍包含频率限制交互描述和 `dailyUpdateRemaining` Store 字段 | 删除频控相关内容，对齐 D9 决策 |
| FLAG-3 | design.md | `POST /privacy/update` 出参仍标注为 `ContentUserPrivacySettingVO` | 改为 `Result<String>` |
| FLAG-4 | 前端 PRD | gender 字段类型为 Integer（1/2/3），与 design.md String 枚举不一致 | 更新 PRD 为 String 枚举 |
| FLAG-5 | specs/profile-editing | 缺少"审核中重复提交"负面 Scenario | 补充 Scenario |
| FLAG-6 | specs/profile-history | "Restore" Scenario 返回类型描述与 design.md 矛盾 | 统一为后端实际返回类型 |

### ADVISORY 问题（可选改进）

| # | 文件 | 问题 | 建议 |
|---|------|------|------|
| ADV-1 | 前端 PRD | 隐私设置页面结构过时 | 更新对齐 D7 |
| ADV-2 | 前端 PRD | `ContentUserPrivacyUpdateReq` 字段表含旧字段 | 更新对齐后端实际定义 |
| ADV-3 | 前端 PRD | Store 设计含 `dailyUpdateRemaining` | 删除 |
| ADV-4 | design.md | Goals 写"12 个端点"但实际 11 个 | 统一描述 |
| ADV-5 | design.md | history VO 字段名 `createTime` vs 后端 `changedAt` | 确认后端实际字段名 |
| ADV-6 | design.md | OSS STS 凭证获取端点未明确 | 补充说明 |
| ADV-7 | specs/homepage-customization | 缺少 OSS 上传超时 Scenario | 补充 |
| ADV-8 | specs/verification-badge | 缺少 badge 加载失败 Scenario | 补充 |
| ADV-9 | design.md | themeColor 约束 max=16 vs PRD max=32 | 以后端为准 |
| ADV-10 | design.md | 频率限制错误码未约定 | 明确错误码映射 |

### 建议操作

1. **BLOCK-1 优先修复**: 确认 4 个 POST 端点的实际返回类型（是 `Result<ContentUserProfileVO>` 还是 `Result<String>`），统一更新 design.md、proposal.md、verification-review.md 和 tasks.md
2. **BLOCK-2 同步更新**: 将前端 PRD 隐私字段定义对齐后端 `ContentUserPrivacyUpdateReq` 的 15+2 字段
3. **FLAG-3 快速修复**: design.md 中 `POST /privacy/update` 出参改为 `Result<String>`
4. **其余 FLAG/ADVISORY**: 在下次文档迭代中统一修复

---

*审核完成。change 实现质量良好，主要风险在文档层面的一致性问题，不阻塞代码 apply，但建议在 apply 前修复 BLOCK 问题以避免后续文档误导。*
