# 审核报告: user-06-privacy-notifications-frontend

**审核时间**: 2026-06-05
**审核范围**: 前端 change 全部文档 + 前后端接口对齐
**门禁判定**: **CONDITIONAL** — 4 个 BLOCK 问题需在 apply 前解决

---

## 审核摘要

| 维度 | 评分 | 说明 |
|------|------|------|
| 1. 完整性 | **PARTIAL** | 文档结构齐全，tasks.md 存在编号重复 |
| 2. 一致性 | **PARTIAL** | 后端 design.md 和 specs 路径与实际实现不一致 |
| 3. 可实现性 | **PASS** | 技术栈兼容，组件选型合理 |
| 4. 可测试性 | **PARTIAL** | PRD F/E 用例全覆盖，缺少分页和网络断开独立 Scenario |
| 5. 接口契约 | **PARTIAL** | 9 个 API 列出完整，2 个后端缺失已记录，PRD 遗漏安全更新端点 |
| 6. 边界覆盖 | **PARTIAL** | null/空状态覆盖好，缺少并发重试和超大列表场景 |
| 7. API 命名规范 | **PASS** | RESTful 语义正确 |
| 8. 存量 API 兼容 | **PASS** | 纯新增，无破坏性变更 |
| 9. 跨端一致性 | **FAIL** | 可见性级别 4 vs 3、通知类型 7 vs 6、渠道字段 7 vs 6 |
| 10. 依赖分析 | **PARTIAL** | 4 个后端 API/字段缺失，EPIC-01 依赖已确认 |

---

## 量化指标

| 指标 | 数值 |
|------|------|
| 总问题数 | **15** |
| BLOCK | **4** |
| FLAG | **6** |
| ADVISORY | **5** |

---

## BLOCK 问题（阻塞 apply）

### B-1: 隐私可见性级别数不一致（前端 4 级 vs 后端 spec 3 级）

- **位置**: 前端 `specs/privacy-settings/spec.md` vs 后端 `specs/privacy-visibility/spec.md`
- **描述**: 前端定义四级可见性（PUBLIC / FOLLOWERS_ONLY / MUTUAL_ONLY / PRIVATE），后端 spec 仅定义三级，缺少 `MUTUAL_ONLY`
- **实际**: 后端 `ContentUserPrivacyUpdateReq` 的 `@Pattern` 正则已包含 MUTUAL_ONLY，属后端 spec 文档错误
- **责任方**: 后端
- **修复**: 后端 spec 补充四级可见性描述

### B-2: 后端 VO 缺少订阅更新通知的开关和渠道字段

- **位置**: 后端 `ContentNotificationChannelConfigVO`（缺 `subscriptionChannels`）、`ContentUserNotificationSettingVO`（缺 `subscriptionNoticeEnabled`）
- **描述**: 前端定义 7 类通知，后端 VO 仅 6 个渠道字段和 6 个开关字段，"订阅更新"无对应后端字段
- **责任方**: 后端
- **修复**: 后端 VO 补充 `subscriptionNoticeEnabled` 和 `subscriptionChannels` 字段，service 层 JSON 序列化同步更新
- **注意**: 后端实体 `ContentUserNotificationSetting` 已有 `subscriptionNoticeEnabled` 字段，但 VO 未暴露

### B-3: 隐私设置 GET 端点缺失

- **位置**: `ContentUserSettingsController`
- **描述**: 后端仅有 `POST /privacy/update`，无 `GET /privacy` 查询端点
- **责任方**: 后端
- **修复**: 补充 `@GetMapping("/privacy")`，复用 profileService 查询
- **已在 backend-issues.md 记录**: 是（问题 1，P0）

### B-4: 安全设置更新端点缺失

- **位置**: `ContentUserSettingsController`
- **描述**: 后端仅有 `GET /security`，无 `POST /security/update` 更新端点
- **责任方**: 后端
- **修复**: 补充 `@PostMapping("/security/update")`，需新建 `ContentUserSecurityUpdateReq`
- **已在 backend-issues.md 记录**: 是（问题 2，P0）

---

## FLAG 问题（建议修复）

### F-1: tasks.md 子任务 4.4 重复编号

- **位置**: `tasks.md` 第 36 行和第 38 行
- **描述**: "实现查看详情 Modal"和"实现撤销授权流程"均编号为 4.4
- **修复**: 将第二个 4.4 改为 4.5，后续顺延

### F-2: 后端 design.md 和 specs API 路径前缀错误

- **位置**: 后端 `design.md` 第 52/54 行、`specs/account-security-entry/spec.md`、`specs/third-party-authorization/spec.md`
- **描述**: 文档使用 `/api/v1/` 前缀，实际 controller 使用 `/content/user/` 前缀
- **责任方**: 后端
- **修复**: 统一修正为 `/content/user/` 前缀

### F-3: 后端通知偏好 spec 标题写"六类通知"

- **位置**: 后端 `specs/notification-preferences/spec.md` 第 4 行
- **描述**: PRD 和前端 spec 均为"七类通知"（含订阅更新），后端 spec 未更新
- **责任方**: 后端
- **修复**: 改为"七类通知"，补充订阅更新 Scenario

### F-4: PRD 接口清单遗漏安全设置更新端点

- **位置**: PRD 第 5 节"接口清单"
- **描述**: 列出 9 个 API，但 account-security spec 要求 `POST /security/update`，未在清单中
- **修复**: 在 PRD 接口清单补充该端点，标记"后端待实现"

### F-5: 收藏夹字段名不一致

- **位置**: 前端 `specs/privacy-settings/spec.md` "收藏夹字段名映射" Scenario
- **描述**: 后端 `favoriteVisibility`（无 s）vs 前端 `favoritesVisibility`（有 s）
- **修复**: 前端 API 层做字段映射（spec 已识别，需确保 tasks.md 覆盖）

### F-6: userId 注入拦截器验证未完成

- **位置**: `design.md` Decision 5
- **描述**: 标注"需确认拦截器已支持 GET query 和 POST params 注入"，但留到 Task 6.1 才验证
- **修复**: 建议在 Task 1.1 创建 API 封装时立即验证

---

## ADVISORY 问题（可选改进）

### A-1: 第三方授权列表缺少分页测试场景
- PRD 提到分页加载，spec 中无相关 Scenario

### A-2: 网络断开时保存缺少独立 Scenario
- E-10 测试场景在 PRD 中列出，spec 中无对应 Scenario

### A-3: 免打扰规则数限制未提升为 Requirement
- "最多 5 条"仅在 Scenario 中，建议提升为独立 Requirement

### A-4: 第三方授权列表排序未定义
- spec 未定义排序方式（建议按授权时间降序）

### A-5: 免打扰后端 spec 文档可见性级别描述不准确
- 后端 `privacy-visibility/spec.md` 写三级，实际代码支持四级

---

## 接口对齐矩阵

| # | 接口 | 方法 | 前端 | 后端(代码) | 对齐状态 |
|---|------|------|------|-----------|---------|
| 1 | /content/user/settings/notification | GET | 有 | 有 | ALIGNED |
| 2 | /content/user/settings/notification/update | POST | 有 | 有 | ALIGNED |
| 3 | /content/user/settings/notification/dnd/update | POST | 有 | 有 | ALIGNED |
| 4 | /content/user/settings/privacy | GET | 有 | **无** | MISSING_BACKEND |
| 5 | /content/user/settings/privacy/update | POST | 有 | 有 | ALIGNED |
| 6 | /content/user/settings/security | GET | 有 | 有 | ALIGNED |
| 7 | /content/user/settings/security/update | POST | 有 | **无** | MISSING_BACKEND |
| 8 | /content/user/auth/third-party | GET | 有 | 有 | ALIGNED |
| 9 | /content/user/auth/third-party/{authId} | GET | 有 | 有 | ALIGNED |
| 10 | /content/user/auth/third-party/{authId} | DELETE | 有 | 有 | ALIGNED |

---

## 修复优先级

1. **后端** 补充 `GET /content/user/settings/privacy` 端点（B-3）
2. **后端** 补充 `POST /content/user/settings/security/update` 端点（B-4）
3. **后端** 补充 `subscriptionNoticeEnabled` + `subscriptionChannels` 字段（B-2）
4. **后端** 修正 spec 文档可见性级别和 API 路径（B-1, F-2, F-3, A-5）
5. **前端** 修正 tasks.md 4.4 重复编号（F-1）
6. **前端** 在 PRD 接口清单补充安全更新端点（F-4）
