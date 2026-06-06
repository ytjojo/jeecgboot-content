# 审核报告: user-06-privacy-notifications-frontend

**审核时间**: 2026-06-06
**审核范围**: 前端 change 全部文档 + 前后端接口对齐 + 配对后端 change 衔接审计
**门禁判定**: **CONDITIONAL** — 4 个 BLOCK 问题需在 apply 前解决

---

## 审核摘要

| 维度 | 评分 | 说明 |
|------|------|------|
| 1. 完整性 (Completeness) | **PASS** | 文档结构齐全，proposal/design/specs/tasks/backend-issues 五件套完整，tasks.md 存在编号重复小瑕疵 |
| 2. 一致性 (Consistency) | **PARTIAL** | 后端 spec 与代码实现存在 3 处不一致（可见性级别数、通知类型数、API 路径前缀） |
| 3. 可实现性 (Feasibility) | **PASS** | 技术栈兼容（Vue 3 + Ant Design Vue + Vben Admin），组件选型合理，2 个后端缺失端点已在 backend-issues.md 记录 |
| 4. 可测试性 (Testability) | **PARTIAL** | PRD F-01~F-12 / E-01~E-10 覆盖率 85%，缺少分页、网络断开、并发保存独立 Scenario |
| 5. 接口契约 (API Contract) | **PARTIAL** | 10 个 API 端点定义完整，8/10 后端已实现，2 个后端缺失（隐私 GET、安全更新 POST） |
| 6. 边界覆盖 (Boundary) | **PARTIAL** | null/空状态覆盖良好（8/8），缺少并发重试和超大列表场景 |

---

## 量化指标

| 指标 | 数值 |
|------|------|
| PRD 功能需求覆盖率 | **10/10 (100%)** |
| PRD 测试要点覆盖率 | **22/26 (85%)** |
| API 契约完整率 | **8/10 (80%)** — 2 个后端缺失端点 |
| 边界条件覆盖率 | **14/18 (78%)** — 缺并发/超时/大列表/慢网络 |
| TDD 配对率 | **N/A** — 前端 change 无后端测试要求 |
| 总问题数 | **15** |
| BLOCK | **4** |
| FLAG | **6** |
| ADVISORY | **5** |

---

## 维度 1: 完整性 (Completeness) — PASS

### 文档结构检查

| 文件 | 状态 | 说明 |
|------|------|------|
| `proposal.md` | ✅ 完整 | Why / What / Capabilities / Impact 四节齐全 |
| `design.md` | ✅ 完整 | Context / Goals-NonGoals / 6 Decisions / Risks 齐全 |
| `specs/notification-settings/spec.md` | ✅ 完整 | 5 Requirements + 14 Scenarios |
| `specs/privacy-settings/spec.md` | ✅ 完整 | 5 Requirements + 11 Scenarios |
| `specs/third-party-auth/spec.md` | ✅ 完整 | 4 Requirements + 10 Scenarios |
| `specs/account-security/spec.md` | ✅ 完整 | 3 Requirements + 7 Scenarios |
| `tasks.md` | ✅ 完整 | 6 模块 41 子任务，全部 marked done |
| `backend-issues.md` | ✅ 完整 | 5 个后端遗留问题，含优先级排序 |

### PRD 需求 → Spec 覆盖追溯

| PRD 功能 | 对应 Spec | 覆盖状态 |
|----------|-----------|----------|
| 通知类型独立开关 (P0) | notification-settings/Requirement 1 | ✅ |
| 通知渠道配置 (P0) | notification-settings/Requirement 2 | ✅ |
| 免打扰多时段配置 (P1) | notification-settings/Requirement 4 | ✅ |
| 动态可见性四级控制 (P0) | privacy-settings/Requirement 1 | ✅ |
| 在线状态可见性 (P1) | privacy-settings/Requirement 2 | ✅ |
| 搜索引擎索引控制 (P2) | privacy-settings/Requirement 3 | ✅ |
| 第三方授权列表 (P1) | third-party-auth/Requirement 1 | ✅ |
| 撤销第三方授权 (P1) | third-party-auth/Requirement 3 | ✅ |
| 账户安全入口 (P1) | account-security/Requirement 1 | ✅ |
| 登录提醒开关 (P1) | account-security/Requirement 3 | ✅ |

### 问题

**F-1: tasks.md 子任务编号重复**
- 位置: tasks.md 第 37 行和第 39 行
- 描述: "实现查看详情 Modal"和"实现撤销授权流程"均编号为 4.4
- 修复: 将第二个 4.4 改为 4.5，后续顺延（4.5→4.6, 4.6→4.7, 4.7→4.8, 4.8→4.9）

---

## 维度 2: 一致性 (Consistency) — PARTIAL

### Capabilities ↔ Specs 映射

| Capability | Spec 文件 | 一致性 |
|-----------|-----------|--------|
| `notification-settings` | `specs/notification-settings/spec.md` | ✅ |
| `privacy-settings` | `specs/privacy-settings/spec.md` | ✅ |
| `third-party-auth` | `specs/third-party-auth/spec.md` | ✅ |
| `account-security` | `specs/account-security/spec.md` | ✅ |

### Decisions ↔ Requirements 一致性

| design.md Decision | 对应 Spec Requirement | 一致性 |
|-------------------|----------------------|--------|
| Decision 2: 在线状态三级控制 | privacy-settings/Requirement 2 (三级) | ✅ |
| Decision 3: 第三方授权独立表 | third-party-auth 全部 Requirements | ✅ |
| Decision 5: userId 拦截器注入 | 所有 API 调用 | ✅ |
| Decision 6: 响应式方案 | 各 spec 移动端 Scenario | ✅ |

### 问题

**B-1: 隐私可见性级别数不一致（前端 4 级 vs 后端 spec 3 级）**
- 位置: 前端 `specs/privacy-settings/spec.md` vs 后端 `specs/privacy-visibility/spec.md`
- 描述: 前端定义四级可见性（PUBLIC / FOLLOWERS_ONLY / MUTUAL_ONLY / PRIVATE），后端 spec 仅定义三级（PUBLIC / HIDDEN / MUTUAL_ONLY），缺少 `FOLLOWERS_ONLY` 和 `PRIVATE`，多了一个 `HIDDEN`（这是在线状态的值，非动态可见性）
- 实际: 后端 `ContentUserPrivacyUpdateReq` 的 `@Pattern` 正则已包含全部四级，属后端 spec 文档错误
- 责任方: 后端
- 修复: 后端 spec 补充四级可见性描述，修正 `HIDDEN` 为在线状态专属值

**F-2: 后端 spec API 路径前缀错误**
- 位置: 后端 `specs/account-security-entry/spec.md`、`specs/third-party-authorization/spec.md`
- 描述: 文档使用 `/api/v1/` 前缀，实际 controller 使用 `/content/user/` 前缀
- 责任方: 后端
- 修复: 统一修正为 `/content/user/` 前缀

**F-3: 后端通知偏好 spec 标题写"六类通知"，内容缺少订阅更新 Scenario**
- 位置: 后端 `specs/notification-preferences/spec.md` 第 1 行
- 描述: PRD 和前端 spec 均为"七类通知"（含订阅更新），后端 spec 仅列出六类 Scenario
- 责任方: 后端
- 修复: 标题改为"七类通知"，补充订阅更新 Scenario

---

## 维度 3: 可实现性 (Feasibility) — PASS

### 技术栈兼容性

| 项目 | 状态 | 说明 |
|------|------|------|
| Vue 3 | ✅ | 项目已使用 Vue 3 Composition API |
| Ant Design Vue | ✅ | Switch/Checkbox/TimePicker/Select/Radio/Table/Modal 均可用 |
| Vben Admin | ✅ | Page/Form/Table/Modal 封装组件可复用 |
| 路由懒加载 | ✅ | 项目已有 `() => import(...)` 模式 |
| defHttp | ✅ | API 封装使用项目已有 HTTP 工具 |

### 架构规范兼容性

| 项目 | 状态 | 说明 |
|------|------|------|
| 目录结构 | ✅ | `src/views/content/settings/` 与 `fan/`、`mutual-follow/` 一致 |
| API 封装集中管理 | ✅ | 单一 `api.ts` 文件，9 个接口 |
| 页面级状态管理 | ✅ | 不新建全局 Store，符合项目约定 |
| 响应式方案 | ✅ | Ant Design Vue 栅格 + CSS 媒查，项目标准做法 |

### 后端依赖风险

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| 隐私 GET 端点缺失 | 高 | backend-issues.md 已记录（P0），或临时用 profile 接口 |
| 安全更新 POST 端点缺失 | 高 | backend-issues.md 已记录（P0） |
| 订阅更新渠道字段缺失 | 中 | backend-issues.md 已记录（P0） |
| userId 拦截器兼容性 | 低 | Task 6.1 验证，必要时 API 封装手动补充 |

---

## 维度 4: 可测试性 (Testability) — PARTIAL

### PRD 测试要点 → Spec Scenario 覆盖

| PRD 编号 | 测试场景 | Spec Scenario | 覆盖 |
|----------|----------|---------------|------|
| F-01 | 关闭点赞通知开关 | notification-settings/S-1.1 | ✅ |
| F-01a | 关闭订阅更新通知开关 | notification-settings/S-1.1 (隐含) | ⚠️ 无独立 Scenario |
| F-02 | 仅保留 App 内渠道 | notification-settings/S-2.1 | ✅ |
| F-03 | 设置免打扰 23:00-07:00 | notification-settings/S-4.1 | ✅ |
| F-04 | 摘要模式开启 | notification-settings/S-4.2 | ✅ |
| F-05 | 暂时关闭免打扰 | notification-settings/S-5.1 | ✅ |
| F-05a | temporaryDisableUntil 状态恢复 | notification-settings/S-5.2 | ✅ |
| F-06 | 浏览记录仅自己可见 | privacy-settings/S-1.1 | ✅ |
| F-06a | 仅互关可见（MUTUAL_ONLY） | privacy-settings/S-1.1 (隐含) | ⚠️ 无独立 Scenario |
| F-07 | 在线状态隐藏 | privacy-settings/S-2.1 | ✅ |
| F-08 | 禁止搜索引擎索引 | privacy-settings/S-3.1 | ✅ |
| F-09 | 查看第三方授权列表 | third-party-auth/S-1.1 | ✅ |
| F-10 | 撤销第三方授权 | third-party-auth/S-3.1 | ✅ |
| F-11 | 进入账户安全页 | account-security/S-1.1 | ✅ |
| F-12 | 开启/关闭登录提醒 | account-security/S-3.1 | ✅ |

### PRD 异常测试 → Spec Scenario 覆盖

| PRD 编号 | 测试场景 | Spec Scenario | 覆盖 |
|----------|----------|---------------|------|
| E-01 | 通知开关 null | notification-settings/S-1.3 | ✅ |
| E-02 | 可见性 null | privacy-settings/S-1.2 | ✅ |
| E-03 | 搜索引擎 null | privacy-settings/S-3.3 | ✅ |
| E-04 | 在线状态 null | privacy-settings/S-2.3 | ✅ |
| E-05 | 应用名 null | third-party-auth/S-1.2 | ✅ |
| E-06 | 授权范围 null | third-party-auth/S-1.3 | ✅ |
| E-07 | 免打扰启用未填时间 | notification-settings/S-4.4 | ✅ |
| E-08 | 撤销不存在授权 | third-party-auth/S-3.4 | ✅ |
| E-09 | 未登录访问 | privacy-settings/S-5.2 | ✅ |
| E-10 | 网络断开保存 | **无** | ❌ |

### 缺失 Scenario

**A-1: 第三方授权列表分页测试场景**
- PRD 第 9 节提到"第三方授权列表分页加载（如有大量授权记录）"
- spec 中无分页相关 Scenario
- 建议: 补充分页加载 Scenario 或明确标注"单页加载全部"假设

**A-2: 网络断开时保存缺少独立 Scenario**
- PRD E-10 已列出，但 spec 中无对应 Scenario
- 建议: 在各 spec 的保存 Requirement 中补充网络异常 Scenario

**A-3: 并发保存/重复提交缺少 Scenario**
- 用户快速双击保存按钮的场景
- 建议: 补充"保存中按钮禁用"的 Scenario（PRD 交互规则有提及但 spec 未覆盖）

---

## 维度 5: 接口契约 (API Contract) — PARTIAL

### 接口对齐矩阵

| # | 接口 | 方法 | 前端定义 | 后端(代码) | 对齐状态 |
|---|------|------|----------|-----------|---------|
| 1 | /content/user/settings/notification | GET | ✅ | ✅ | ALIGNED |
| 2 | /content/user/settings/notification/update | POST | ✅ | ✅ | ALIGNED |
| 3 | /content/user/settings/notification/dnd/update | POST | ✅ | ✅ | ALIGNED |
| 4 | /content/user/settings/privacy | GET | ✅ | ❌ | **MISSING_BACKEND** |
| 5 | /content/user/settings/privacy/update | POST | ✅ | ✅ | ALIGNED |
| 6 | /content/user/settings/security | GET | ✅ | ✅ | ALIGNED |
| 7 | /content/user/settings/security/update | POST | ✅ | ❌ | **MISSING_BACKEND** |
| 8 | /content/user/auth/third-party | GET | ✅ | ✅ | ALIGNED |
| 9 | /content/user/auth/third-party/{authId} | GET | ✅ | ✅ | ALIGNED |
| 10 | /content/user/auth/third-party/{authId} | DELETE | ✅ | ✅ | ALIGNED |

### 请求/响应字段映射检查

**通知设置** — 字段映射完整 ✅
- 7 个 Boolean 开关字段: likeNoticeEnabled / commentNoticeEnabled / followNoticeEnabled / favoriteNoticeEnabled / mentionNoticeEnabled / messageNoticeEnabled / subscriptionNoticeEnabled
- 7 个渠道字段: likeChannels / commentChannels / followChannels / favoriteChannels / mentionChannels / messageChannels / subscriptionChannels
- 问题: 后端 VO 仅暴露 6 个开关和 6 个渠道字段（B-2）

**隐私设置** — 字段映射完整 ✅
- 5 个字段映射表已定义（含 favoriteVisibility → favoritesVisibility 映射说明）

**安全设置** — 字段映射完整 ✅
- 4 个 Boolean 字段映射表已定义

**第三方授权** — 字段映射隐含在 Scenario 中 ⚠️
- 缺少明确的 VO/Req 字段映射表

### 问题

**B-2: 后端 VO 缺少订阅更新通知的开关和渠道字段**
- 位置: 后端 `ContentNotificationChannelConfigVO`（缺 `subscriptionChannels`）、`ContentUserNotificationSettingVO`（缺 `subscriptionNoticeEnabled`）
- 描述: 前端定义 7 类通知，后端 VO 仅 6 个渠道字段和 6 个开关字段
- 责任方: 后端
- 修复: 后端 VO 补充字段，service 层 JSON 序列化同步更新
- 注意: 后端实体 `ContentUserNotificationSetting` 已有 `subscriptionNoticeEnabled` 字段，但 VO 未暴露

**B-3: 隐私设置 GET 端点缺失**
- 位置: `ContentUserSettingsController`
- 描述: 后端仅有 `POST /privacy/update`，无 `GET /privacy` 查询端点
- 责任方: 后端
- 修复: 补充 `@GetMapping("/privacy")`
- 已在 backend-issues.md 记录: 是（问题 1，P0）

**B-4: 安全设置更新端点缺失**
- 位置: `ContentUserSettingsController`
- 描述: 后端仅有 `GET /security`，无 `POST /security/update` 更新端点
- 责任方: 后端
- 修复: 补充 `@PostMapping("/security/update")`
- 已在 backend-issues.md 记录: 是（问题 2，P0）

**F-4: PRD 接口清单遗漏安全设置更新端点**
- 位置: PRD 第 5 节"接口清单"
- 描述: 列出 9 个 API，但 account-security spec 要求 `POST /security/update`，未在清单中
- 修复: 在 PRD 接口清单补充该端点，标记"后端待实现"

**F-5: 收藏夹字段名不一致**
- 位置: 前端 `specs/privacy-settings/spec.md` "收藏夹字段名映射" Scenario
- 描述: 后端 `favoriteVisibility`（无 s）vs 前端 `favoritesVisibility`（有 s）
- 修复: 前端 API 层做字段映射（spec 已识别，需确保 tasks.md 覆盖 — task 3.4 已覆盖 ✅）

---

## 维度 6: 边界覆盖 (Boundary) — PARTIAL

### 10 类边界条件覆盖检查

| 边界类型 | 覆盖状态 | 对应 Scenario |
|----------|----------|---------------|
| 1. null 值处理 | ✅ 全覆盖 | 通知开关默认 true, 可见性默认 PUBLIC, 搜索引擎默认关闭, 在线状态默认 PUBLIC, 安全功能默认已启用, 应用名→"未知应用", 授权范围→"未知权限" |
| 2. 空状态 | ✅ | 授权列表为空 → 空状态插图 + 文案 |
| 3. 最大值限制 | ✅ | 免打扰最多 5 条 → 按钮隐藏/禁用 |
| 4. 最小值/校验 | ✅ | 启用但未填时间 → 校验失败; 开始=结束 → 全天免打扰 |
| 5. 异常响应码 | ✅ | 授权不存在 404, 越权 403, 未登录路由守卫拦截 |
| 6. 并发/重复提交 | ❌ 缺失 | 无快速双击保存按钮 Scenario |
| 7. 超大列表 | ❌ 缺失 | 第三方授权列表无分页/虚拟滚动 Scenario |
| 8. 超时/慢网络 | ❌ 缺失 | 无请求超时 Scenario |
| 9. 跨午夜免打扰 | ✅ | PRD 提到"支持跨午夜" |
| 10. 临时关闭状态恢复 | ✅ | temporaryDisableUntil > 当前时间 → 自动倒计时 |

### 问题

**A-3: 免打扰规则数限制未提升为 Requirement**
- "最多 5 条"仅在 Scenario 中，建议提升为独立 Requirement

**A-4: 第三方授权列表排序未定义**
- spec 未定义排序方式（建议按授权时间降序）

---

## 前后端衔接审计

### 接口清单双向对比

| 方向 | 接口数 | 对齐 | 缺失 |
|------|--------|------|------|
| 前端引用 → 后端定义 | 10 | 8 | 2 (隐私 GET, 安全更新 POST) |
| 后端定义 → 前端引用 | 8 | 8 | 0 (后端已实现的接口前端全部引用) |

### 数据模型一致性

| 数据模型 | 前端定义 | 后端定义 | 一致性 |
|----------|----------|----------|--------|
| 通知开关字段 | 7 个 Boolean | 6 个 Boolean (VO 缺 subscriptionNoticeEnabled) | ❌ B-2 |
| 通知渠道字段 | 7 个 string[] | 6 个 string[] (VO 缺 subscriptionChannels) | ❌ B-2 |
| 隐私可见性 | 4 级枚举 | 4 级枚举 (spec 写 3 级但代码支持 4 级) | ⚠️ 文档不一致 |
| 在线状态 | 3 级枚举 | 3 级枚举 | ✅ |
| 安全功能状态 | 4 个 Boolean | 4 个 Boolean | ✅ |
| 授权记录 | 应用名/时间/范围 | 应用名/时间/范围 | ✅ |

### 错误码覆盖检查

| 错误码 | 前端处理 | 后端返回 | 一致性 |
|--------|----------|----------|--------|
| 401 未登录 | 路由守卫拦截 | 401 返回 | ✅ |
| 403 越权 | 提示"权限不足" | 403 返回 | ✅ |
| 404 不存在 | 提示"授权记录不存在" | 404 返回 | ✅ |
| 500 服务器错误 | 全局错误提示 | 500 返回 | ✅ |

### 认证鉴权一致性

| 项目 | 前端 | 后端 | 一致性 |
|------|------|------|--------|
| userId 传递 | 全局拦截器自动注入 | @RequestParam("userId") | ✅ |
| Token 验证 | 登录态 Token | 全局认证过滤器 | ✅ |

### 分页契约检查

| 接口 | 分页 | 说明 |
|------|------|------|
| 授权列表 | 未定义 | PRD 假设 <100 条一次加载，spec 无分页 Scenario |

---

## PRD 追溯矩阵

| PRD 需求 | PRD 优先级 | Spec 文件 | Spec Requirement | tasks.md | 覆盖 |
|----------|-----------|-----------|-----------------|----------|------|
| 通知类型独立开关 | P0 | notification-settings | Requirement 1 | 2.2, 2.3 | ✅ |
| 通知渠道配置 | P0 | notification-settings | Requirement 2 | 2.2, 2.4 | ✅ |
| 免打扰多时段配置 | P1 | notification-settings | Requirement 4 | 2.5, 2.6, 2.8 | ✅ |
| 免打扰临时关闭 | P1 | notification-settings | Requirement 5 | 2.7 | ✅ |
| 通知保存 | P0 | notification-settings | Requirement 3 | 2.9 | ✅ |
| 动态可见性四级控制 | P0 | privacy-settings | Requirement 1 | 3.2, 3.3, 3.4 | ✅ |
| 在线状态可见性 | P1 | privacy-settings | Requirement 2 | 3.5 | ✅ |
| 搜索引擎索引控制 | P2 | privacy-settings | Requirement 3 | 3.6 | ✅ |
| 隐私设置保存 | P0 | privacy-settings | Requirement 4 | 3.8 | ✅ |
| 第三方授权列表 | P1 | third-party-auth | Requirement 1 | 4.1-4.3, 4.8 | ✅ |
| 查看授权详情 | P1 | third-party-auth | Requirement 2 | 4.4 | ✅ |
| 撤销第三方授权 | P1 | third-party-auth | Requirement 3 | 4.5, 4.6 | ✅ |
| 账户安全入口 | P1 | account-security | Requirement 1 | 5.1-5.4 | ✅ |
| 登录提醒开关 | P1 | account-security | Requirement 3 | 5.5 | ✅ |

---

## 最终结论

### 门禁判定: CONDITIONAL

**理由**: 4 个 BLOCK 问题均为后端侧缺失，前端文档本身质量良好。前端 change 可以在后端 BLOCK 问题解决后直接 apply。

### BLOCK 问题清单（阻塞 apply）

| 编号 | 问题 | 责任方 | 优先级 |
|------|------|--------|--------|
| B-1 | 隐私可见性级别数不一致（前端 4 级 vs 后端 spec 3 级） | 后端 | P0 |
| B-2 | 后端 VO 缺少订阅更新通知的开关和渠道字段 | 后端 | P0 |
| B-3 | 隐私设置 GET 端点缺失 | 后端 | P0 |
| B-4 | 安全设置更新端点缺失 | 后端 | P0 |

### FLAG 问题清单（建议修复）

| 编号 | 问题 | 责任方 | 优先级 |
|------|------|--------|--------|
| F-1 | tasks.md 子任务 4.4 重复编号 | 前端 | P1 |
| F-2 | 后端 spec API 路径前缀错误 (/api/v1/ → /content/user/) | 后端 | P1 |
| F-3 | 后端通知偏好 spec 标题"六类通知"应为"七类" | 后端 | P1 |
| F-4 | PRD 接口清单遗漏安全设置更新端点 | 前端 | P1 |
| F-5 | 收藏夹字段名不一致（已识别，tasks 覆盖） | 前端 | P2 |
| F-6 | userId 注入拦截器验证延后到 Task 6.1 | 前端 | P2 |

### ADVISORY 问题清单（可选改进）

| 编号 | 问题 | 建议 |
|------|------|------|
| A-1 | 第三方授权列表缺少分页 Scenario | 补充或明确标注"一次加载全部"假设 |
| A-2 | 网络断开保存缺少独立 Scenario | 在各保存 Requirement 中补充 |
| A-3 | 免打扰规则数限制未提升为 Requirement | 从 Scenario 提升为独立 Requirement |
| A-4 | 第三方授权列表排序未定义 | 建议按授权时间降序 |
| A-5 | 并发保存/重复提交缺少 Scenario | 补充"保存中按钮禁用" Scenario |

### 修复优先级

1. **后端** 补充 `GET /content/user/settings/privacy` 端点（B-3）
2. **后端** 补充 `POST /content/user/settings/security/update` 端点（B-4）
3. **后端** 补充 `subscriptionNoticeEnabled` + `subscriptionChannels` 字段到 VO（B-2）
4. **后端** 修正 spec 文档可见性级别和 API 路径（B-1, F-2, F-3）
5. **前端** 修正 tasks.md 4.4 重复编号（F-1）
6. **前端** 在 PRD 接口清单补充安全更新端点（F-4）

### 建议操作

- **后端 BLOCK 问题**: 4 个 P0 问题均为后端侧，建议先解决后端 change（`user-06-privacy-notifications`）中的对应问题，或由后端 agent 创建修复 change
- **前端可先行**: 在后端 BLOCK 解决前，前端可先处理 F-1（修正 tasks.md 编号）和 F-4（补充 PRD 接口清单）
- **apply 条件**: 4 个 BLOCK 全部解决后，本 change 可直接 apply
