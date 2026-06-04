# circle-11-content-interaction-frontend 验证审核报告

**验证时间**: 2026-06-04
**验证范围**: 前端 change 文档（design.md、proposal.md、specs/*.md）与后端实际代码的一致性

---

## 验证结果摘要

| 类别 | 状态 | 问题数 |
|------|------|--------|
| 后端 API 路径一致性 | 有问题 | 5 |
| 后端 API 方法一致性 | 有问题 | 3 |
| 缺失后端 API | 有问题 | 3 |
| 文档完整性 | 有问题 | 2 |
| 前端文档内部一致性 | 通过 | 0 |

**总体结论**: 存在多处前后端接口不一致问题，需修复前端文档以匹配后端实际实现，并记录后端缺失的 API。

---

## 一、后端 API 验证详情

### 1.1 置顶/精华（content-pin-featured） - 基本匹配

| 前端 Spec 引用 | 后端实际 | 状态 |
|---------------|---------|------|
| `PUT /circle-content/{id}/pin` | `PUT /circle-content/{contentId}/pin?circleId=xxx` | 路径匹配，但前端文档未提及 `circleId` 必需参数 |
| `PUT /circle-content/{id}/featured` | `PUT /circle-content/{contentId}/featured?circleId=xxx` | 路径匹配，但前端文档未提及 `circleId` 必需参数 |

**问题**: 两个接口均需要 `@RequestParam circleId`，前端 spec 中未体现此参数。

### 1.2 举报（content-report） - 多处不匹配

| 前端 Spec 引用 | 后端实际 | 状态 |
|---------------|---------|------|
| `POST /circle-report` | `POST /circle-report/` | 匹配 |
| `PUT /circle-report/{id}/delete-content` | `POST /circle-report/{reportId}/delete-content?circleId=xxx` | **HTTP 方法错误**：前端写 PUT，后端为 POST；缺少 circleId 参数 |
| `PUT /circle-report/{id}/ignore` | `POST /circle-report/{reportId}/ignore?circleId=xxx` | **HTTP 方法错误**：前端写 PUT，后端为 POST；缺少 circleId 参数 |
| `PUT /circle-report/{id}/mute-user` | `POST /circle-report/{reportId}/mute?circleId=xxx` | **路径和方法均错误**：前端写 `PUT /mute-user`，后端为 `POST /mute`；缺少 circleId 参数 |

**额外问题**: 前端 spec 定义了禁言时长选项（1小时/1天/7天/30天/永久），但后端 `handleMute` 方法**不接受禁言时长参数**，且实现中标注了 `// TODO: 调用禁言服务对被举报用户执行禁言`。

### 1.3 加入申请审核（join-request-review） - 路径和方法均不匹配

| 前端 Spec 引用 | 后端实际 | 状态 |
|---------------|---------|------|
| `PUT /circle-join-request/{id}/approve` | `POST /circle-join-review/approve?circleId=xxx`（body 含 requestId） | **完全不匹配**：路径前缀不同（`circle-join-request` vs `circle-join-review`），方法不同（PUT vs POST），参数传递方式不同（path vs body） |
| `PUT /circle-join-request/{id}/reject` | `POST /circle-join-review/reject?circleId=xxx`（body 含 requestId + rejectReason） | **完全不匹配**：同上 |

**额外问题**: 前端 spec 提到 `pendingJoinRequestCount` 角标，后端**无此接口**。

### 1.4 @成员（mention-member） - 缺失后端 API

| 前端 Spec 引用 | 后端实际 | 状态 |
|---------------|---------|------|
| `GET /circle/{circleId}/mentionable-members` | **不存在** | **缺失**：后端有 `ICircleMentionService.getMentionCandidates()` 方法，但**无对应 Controller 端点** |

### 1.5 公告（circle-announcement） - 缺失 spec 和 API

| 前端 tasks.md 引用 | 后端实际 | 状态 |
|-------------------|---------|------|
| `createAnnouncement` → `POST /circle-announcement` | `POST /circle-announcement/` | 匹配 |
| `getCurrentAnnouncement` → `GET /circle-announcement/active/{circleId}` | `GET /circle-announcement/active/{circleId}` | 匹配 |
| `deleteAnnouncement` → `DELETE /circle-announcement/{id}` | **不存在** | **缺失**：后端 Controller 仅有 publish 和 getActive，无 delete 接口 |

**额外问题**: proposal.md 将 `circle-announcement` 列为 Capabilities，但 **specs/ 目录下无 circle-announcement 对应 spec 文件**。

---

## 二、前端文档问题列表

### 2.1 design.md 问题

1. **D1 决策中的 API 路径描述不准确**: 提到 `/circle-content/{id}/pin` 和 `/circle-content/{id}/featured`，未说明需要 `circleId` 查询参数
2. **Risks 中未提及与后端 API 不匹配的风险**: 前端依赖的 API 路径与后端实际路径存在差异

### 2.2 specs/content-pin-featured/spec.md 问题

1. 场景中调用 `PUT /circle-content/{id}/pin` 时未说明需要传递 `circleId` 参数
2. 场景中调用 `PUT /circle-content/{id}/featured` 时未说明需要传递 `circleId` 参数

### 2.3 specs/content-report/spec.md 问题

1. `PUT /circle-report/{id}/delete-content` 应为 `POST /circle-report/{id}/delete-content?circleId=xxx`
2. `PUT /circle-report/{id}/ignore` 应为 `POST /circle-report/{id}/ignore?circleId=xxx`
3. `PUT /circle-report/{id}/mute-user` 应为 `POST /circle-report/{id}/mute?circleId=xxx`
4. 禁言时长选项（1小时/1天/7天/30天/永久）在后端未实现，需标注为后端遗留

### 2.4 specs/join-request-review/spec.md 问题

1. `PUT /circle-join-request/{id}/approve` 应为 `POST /circle-join-review/approve`（body 传 requestId，query 传 circleId）
2. `PUT /circle-join-request/{id}/reject` 应为 `POST /circle-join-review/reject`（body 传 requestId + rejectReason，query 传 circleId）
3. `pendingJoinRequestCount` 接口后端不存在，需标注为后端遗留

### 2.5 specs/mention-member/spec.md 问题

1. `GET /circle/{circleId}/mentionable-members` 接口后端不存在，仅有 Service 层方法，需标注为后端遗留

### 2.6 proposal.md 问题

1. Capabilities 中列出了 `circle-announcement`，但 specs/ 目录下无对应 spec
2. "新增 API 对接"提到 12 个接口，实际需核实数量

---

## 三、建议修复方案

### 3.1 前端文档修复（立即执行）

1. **修复所有 spec 中的 API 路径**，使其与后端实际实现一致
2. **在涉及的接口调用处补充 `circleId` 参数说明**
3. **为公告功能补充 spec 文件** 或从 proposal Capabilities 中移除

### 3.2 后端遗留代码标记（创建 backend-issues.md）

以下后端功能缺失或未完成，前端开发需知晓：

| 编号 | 问题 | 严重程度 | 影响范围 |
|------|------|---------|---------|
| BE-01 | 缺少 `GET /circle/{circleId}/mentionable-members` Controller 端点 | 高 | @成员功能完全依赖此接口 |
| BE-02 | 缺少 `DELETE /circle-announcement/{id}` 接口 | 中 | 公告删除功能无法实现 |
| BE-03 | 缺少 `pendingJoinRequestCount` 查询接口 | 中 | 管理入口角标无法实现 |
| BE-04 | `handleMute` 不接受禁言时长参数，且实现为 TODO | 中 | 禁言功能无法正常工作 |
| BE-05 | 举报提交 `CircleReportReq` 无举报原因枚举校验 | 低 | 前端需自行维护枚举映射 |
