# 验证审核报告

**变更名称**: channel-21-privacy-membership-frontend
**验证日期**: 2026-06-04
**验证人**: openspec-verify-agent

---

## 1. 验证结果摘要

| 项目 | 状态 | 说明 |
|------|------|------|
| 文档完整性 | ✅ 通过 | proposal.md、design.md、tasks.md、6个spec文件齐全 |
| 后端 API 存在性 | ⚠️ 部分缺失 | 6个 Controller 存在，但有 6 个 API 端点缺失 |
| 前后端接口一致性 | ❌ 不一致 | 前端 plan.md 中定义的部分 API 路径在后端不存在 |
| tasks.md 状态 | ⏳ 未开始 | 所有 55 个任务均为 `- [ ]` 状态 |

---

## 2. 后端 API 验证详情

### 2.1 已存在的后端 Controller

| Controller | 路径前缀 | 状态 | 端点数 |
|-----------|---------|------|--------|
| ChannelSubscriptionController | `/channel/subscription` | ✅ 存在 | 7 |
| ChannelMemberController | `/channel/member` | ✅ 存在 | 9 |
| ChannelInviteController | `/channel/invite` | ✅ 存在 | 4 |
| ChannelGovernanceController | `/channel/governance` | ✅ 存在 | 5 |
| ChannelController | `/api/v1/channels` | ✅ 存在 | 8 |
| ChannelAdminController | `/api/v1/admin/channels` | ✅ 存在 | 2 |

### 2.2 已存在的 API 端点

**ChannelSubscriptionController** (`/channel/subscription`):
- `POST /subscribe` - 订阅频道
- `POST /unsubscribe` - 取消订阅
- `GET /list` - 订阅列表
- `POST /group/create` - 创建分组
- `GET /group/list` - 分组列表
- `POST /group/rename` - 重命名分组
- `POST /group/delete` - 删除分组

**ChannelMemberController** (`/channel/member`):
- `POST /join/free` - 自由加入频道
- `POST /join/apply` - 提交加入申请
- `POST /leave` - 退出频道
- `POST /assign-role` - 分配角色
- `GET /list` - 成员列表
- `GET /search` - 搜索成员
- `GET /applications/pending` - 待审核列表
- `POST /applications/approve` - 批准申请
- `POST /applications/reject` - 拒绝申请

**ChannelInviteController** (`/channel/invite`):
- `POST /create` - 创建邀请
- `GET /list` - 查看邀请列表
- `POST /revoke` - 撤销邀请
- `POST /use` - 使用邀请码加入

**ChannelGovernanceController** (`/channel/governance`):
- `POST /remove` - 移除成员
- `POST /mute` - 禁言成员
- `POST /unmute` - 解除禁言
- `POST /blacklist/add` - 加入黑名单
- `POST /blacklist/remove` - 移出黑名单

### 2.3 缺失的后端 API 端点

| 缺失 API | 前端期望路径 | 后端现状 | 优先级 |
|----------|-------------|---------|--------|
| 订阅状态查询 | `/api/channel/subscription/status/{channelId}` | Service 层有 `isSubscribed()` 方法，但无 Controller 端点 | P0 |
| 更新提醒设置 | `/api/channel/subscription/reminder` | 无对应实现 | P1 |
| 移动频道到分组 | `/api/channel/subscription/move-group` | 无对应实现 | P1 |
| 隐私设置更新 | `/api/channel/privacy/updatePrivacy` | 无专用 Controller，ChannelController 有 `PUT /{id}` 可更新频道信息 | P0 |
| 加入方式更新 | `/api/channel/privacy/updateJoinMethod` | 无专用 Controller | P0 |
| 黑名单列表 | `/api/channel/blacklist/list` | Service 层有 `listBlacklistedUserIds()`，但无 Controller 端点 | P0 |
| 治理日志列表 | `/api/channel/governance/log` | Service 层有 `ChannelGovernanceLogService`，但无 Controller 端点 | P0 |
| 用户频道关系查询 | (未在 plan.md 中明确定义) | 无对应实现 | P1 |

---

## 3. 前端文档问题列表

### 3.1 API 路径不一致

| 问题 | 文件 | 行号 | 说明 |
|------|------|------|------|
| API 路径前缀不一致 | plan.md | 26-36 | 前端使用 `/api/channel/subscription/`，后端实际为 `/channel/subscription/` |
| 缺少后端前缀 | plan.md | 26-36 | 后端 Controller 使用 `/channel/subscription`，前端应使用相同路径或配置代理 |
| groupUpdate 路径不存在 | plan.md | 59 | 后端只有 `/group/rename`，没有 `/group/update` |

### 3.2 功能与 API 不匹配

| 问题 | 文件 | 说明 |
|------|------|------|
| 提醒设置功能 | tasks.md 3.5 | 前端计划实现提醒开关，但后端无对应 API |
| 移动分组功能 | tasks.md 3.5 | 前端计划实现移动频道到分组，但后端无对应 API |
| 申请状态查询 | plan.md | 前端需要查询用户申请状态，后端无独立端点 |
| 隐私设置独立页面 | tasks.md 2.1-2.2 | 前端计划独立隐私设置页面，后端无专用 Controller |

### 3.3 设计文档问题

| 问题 | 文件 | 说明 |
|------|------|------|
| 路由路径假设 | design.md Open Questions #1 | 假设已有 `/channel/:id` 路由，需确认 |
| 订阅列表入口 | design.md Open Questions #2 | 假设在个人中心，需确认具体位置 |
| 页面组织方式 | design.md Open Questions #3 | 成员管理等页面是否为标签页，需确认 |

---

## 4. 建议修复方案

### 4.1 后端 API 补充（高优先级）

**方案 A：扩展现有 Controller**
1. 在 `ChannelSubscriptionController` 中添加：
   - `GET /status/{channelId}` - 查询订阅状态
   - `PUT /reminder` - 更新提醒设置
   - `PUT /move-group` - 移动频道到分组

2. 在 `ChannelGovernanceController` 中添加：
   - `GET /blacklist/list` - 黑名单列表
   - `GET /log` - 治理日志列表

3. 创建 `ChannelPrivacyController` 或在 `ChannelController` 中添加：
   - `PUT /{id}/privacy` - 更新隐私设置
   - `PUT /{id}/join-method` - 更新加入方式

**方案 B：前端适配现有 API**
1. 使用 `ChannelController.PUT /{id}` 更新频道信息（包含隐私和加入方式）
2. 使用 `ChannelMemberController.GET /applications/pending` 间接判断申请状态
3. 提醒设置和移动分组功能标记为 P2，后续迭代实现

### 4.2 前端文档修复（立即执行）

1. **统一 API 路径前缀**：将 plan.md 中的 `/api/channel/` 改为 `/channel/`，或在前端配置代理
2. **修正 groupUpdate 路径**：改为 `/group/rename`
3. **标记缺失功能**：在 tasks.md 中将提醒设置、移动分组标记为 P2
4. **补充 Open Questions 答案**：确认路由结构和页面组织方式

### 4.3 文档补充建议

1. 在 design.md 中补充 API 路径对照表
2. 在 specs 中明确标注哪些 API 已存在、哪些需要后端补充
3. 创建 `backend-issues.md` 记录需要后端配合的事项

---

## 5. 验证结论

**总体评估**: 前端设计文档质量良好，但与后端 API 存在一定程度的不匹配。

**主要风险**:
1. 6 个 API 端点缺失，影响隐私设置、黑名单列表、治理日志等核心功能
2. API 路径前缀不一致，可能导致联调问题
3. 部分功能（提醒设置、移动分组）后端未规划

**建议行动**:
1. **立即**：修复前端文档中的 API 路径问题
2. **短期**：与后端团队确认缺失 API 的实现计划
3. **中期**：补充后端 API 或调整前端功能范围

---

## 6. 附录：后端遗留代码清单

详见 `backend-issues.md` 文件。
