# 验证审核文档: user-05-blocking-muting-frontend

**审核日期**: 2026-06-04
**审核范围**: 前端 change 全部制品 + 后端 API 代码比对
**审核方法**: 逐个读取后端 Controller 源码，验证 API 端点存在性与契约一致性

---

## 验证结果摘要

| 维度 | 状态 |
|------|------|
| Completeness | 0/45 tasks 完成，全部待实现 |
| Correctness | 12/13 后端 API 已存在，1 个缺失（mute-list） |
| Coherence | design.md / spec.md / tasks.md 与后端基本一致 |

---

## 后端 API 验证详情

### 已确认存在的端点（12/13）

| 端点 | Controller 文件 | 状态 |
|------|----------------|------|
| `POST /content/user/relation/block` | ContentUserRelationController:78 | ✅ 存在，参数: @RequestParam userId, targetUserId |
| `POST /content/user/relation/unblock` | ContentUserRelationController:89 | ✅ 存在，参数: @RequestParam userId, targetUserId |
| `GET /content/user/relation/blacklist` | ContentUserRelationController:248 | ✅ 存在，参数: @RequestParam userId, pageNo, pageSize |
| `GET /content/user/relation/detail` | ContentUserRelationController:133 | ✅ 存在，参数: @RequestParam userId, targetUserId |
| `GET /content/user/relation/block-mute/help` | ContentUserRelationController:269 | ✅ 存在，无参数，返回 ContentBlockMuteHelpVO |
| `POST /content/user/relation/mute` | ContentUserRelationController:100 | ✅ 存在，参数: @RequestParam userId, targetUserId |
| `POST /content/user/relation/mute/cancel` | ContentUserRelationController:111 | ✅ 存在，参数: @RequestParam userId, targetUserId |
| `POST /content/user/filter-rule` | ContentUserFilterRuleController:39 | ✅ 存在，参数: @RequestParam userId, ruleType, value, daysValid? |
| `POST /content/user/filter-rule/delete` | ContentUserFilterRuleController:71 | ✅ 存在，参数: @RequestParam userId, ruleId |
| `POST /content/user/filter-rule/batch-delete` | ContentUserFilterRuleController:82 | ✅ 存在，参数: @RequestParam userId, @RequestBody List<String> ruleIds |
| `GET /content/user/filter-rule/list` | ContentUserFilterRuleController:93 | ✅ 存在，参数: @RequestParam userId, ruleType?, pageNo, pageSize |
| `POST /content/user/not-interested` | ContentUserNotInterestedController:29 | ✅ 存在，参数: @RequestParam userId, contentId, contentType |

### 缺失的端点（1/13）

| 端点 | 优先级 | 影响范围 |
|------|--------|----------|
| `GET /content/user/relation/mute-list` | WARNING | 阻塞任务 6.5-6.6（屏蔽列表管理页 - 屏蔽用户 Tab） |

**详情**: `IContentUserRelationService` 接口仅有 `mute()` 和 `unmute()` 方法，无 `listMutedUsers()` 或类似分页查询方法。`ContentUserRelationController` 中无 `/mute-list` 端点。

**建议**: 在后端补充以下端点:
```
GET /content/user/relation/mute-list
参数: @RequestParam userId, @RequestParam pageNo, @RequestParam pageSize
返回: Result<ContentUserMuteListPageVO>
```
需要同步补充: Service 接口方法、ServiceImpl 实现、Mapper 查询、VO 定义。

---

## 前端文档问题列表

### CRITICAL 问题

**无**。所有 spec 和 design 中引用的后端 API 路径（除 mute-list 外）均已在代码库中确认存在。

### WARNING 问题

#### W1: 旧 verification.md 中的 CRITICAL 判定已过时

**问题**: 原 `verification.md` 中 CRITICAL #1（Filter Rule Controller 缺失）和 CRITICAL #2（Not Interested Controller 缺失）的判定基于当时的代码状态，但这两个 Controller 已在后续开发中实现:
- `ContentUserFilterRuleController.java` — 完整实现 4 个端点
- `ContentUserNotInterestedController.java` — 完整实现 1 个端点

**建议**: 标记旧 verification.md 为过时文档，以本次审核结论为准。

#### W2: spec.md 中 not-interested 参数风格描述不精确

**问题**: spec.md 第 55 行写道:
> 静默调用 `POST /content/user/not-interested`（@RequestParam: userId, contentId, contentType）

虽然参数名称正确，但 spec 中同时存在另一处（第 167-180 行 API 对齐表）未提及 not-interested 端点的参数风格。设计文档 design.md 第 70 行统一声明"所有写操作使用 POST，参数通过 @RequestParam 传递"，与实际一致。

**建议**: 在 spec.md 的 API 对齐表（第 167-180 行）中补充 `POST /content/user/not-interested` 条目，保持完整性。

#### W3: tasks.md 中 mute-list 端点假设可能无法实现

**问题**: tasks.md 第 65 行（任务 6.5-6.6）假设 `GET /content/user/relation/mute-list` 端点存在，但后端尚未实现。

**建议**: 任务 6.5-6.6 标记为"依赖后端补充 mute-list 端点"，或在实现时先用 filter-rule/list（ruleType=USER_MUTE）作为替代方案。

#### W4: ContentUserRelationVO 字段名 blacklisted 与 API 路径 block 不一致

**问题**: 后端 VO 字段名为 `blacklisted`（boolean），但 API 路径使用 `block`/`unblock`。前端 Store 缓存时需注意字段映射:
- `blacklisted` — 是否拉黑对方
- `blockedByOwner` — 是否被对方拉黑
- `muted` — 是否屏蔽对方

**建议**: 在 tasks.md 或 design.md 中补充字段映射说明，避免前端开发时混淆。

### SUGGESTION 问题

#### S1: spec.md 中批量删除屏蔽规则参数风格需注意

**问题**: `POST /content/user/filter-rule/batch-delete` 的后端实现使用 `@RequestBody List<String> ruleIds`（JSON 数组），而非 @RequestParam。这与其他端点的 @RequestParam 风格不一致。

**建议**: 前端 API 封装时，此端点使用 `defHttp.post(url, ruleIds)` 传递 JSON body，而非查询参数。

#### S2: ContentBlockMuteHelpVO 返回结构可作为前端文案来源

**问题**: `GET /content/user/relation/block-mute/help` 返回的 `ContentBlockMuteHelpVO` 包含:
- `blockConfirmation` — 拉黑确认文案
- `muteConfirmation` — 屏蔽确认文案
- `unblockConfirmation` — 解除拉黑文案
- `blockVsMuteComparison` — 拉黑与屏蔽对比说明

**建议**: 前端确认弹窗和隐私设置帮助面板可直接使用此端点获取文案，减少硬编码。

---

## 建议修复方案

### 优先级 1：后端补充 mute-list 端点（阻塞任务 6.5-6.6）

1. `IContentUserRelationService` 新增 `listMutedUsers(userId, pageNo, pageSize)` 方法
2. `ContentUserRelationServiceImpl` 实现查询逻辑
3. `ContentUserRelationController` 新增 `GET /mute-list` 端点
4. 定义 `ContentUserMuteListPageVO`（含 mutedUserId, nickname, avatar, muteTime）
5. 编写 WebMvcTest 验证

### 优先级 2：更新前端文档（不阻塞实现）

1. spec.md API 对齐表补充 not-interested 和 mute-list 条目
2. design.md 补充 mute-list 端点依赖说明
3. tasks.md 任务 6.5-6.6 标记后端依赖

### 优先级 3：标记旧文档过时

1. 将原 `verification.md` 移至 `docs/deprecated/` 或添加过时声明

---

## 最终评估

**1 个 WARNING 级后端缺失**（mute-list 端点），不阻塞大部分前端任务实现。

**可立即开始的任务**（不依赖 mute-list）:
- 任务 1.1-1.4: API 封装层（除 mute-list 相关部分）
- 任务 2.1-2.5: Pinia Store
- 任务 3.1-3.7: 操作入口组件
- 任务 4.1-4.5: 不感兴趣与屏蔽词折叠
- 任务 5.1-5.3: 被拉黑状态页面
- 任务 6.1-6.4, 6.7-6.13: 隐私设置页（除屏蔽用户 Tab）
- 任务 7.1-7.4: 响应式适配
- 任务 8.1-8.4: 路由注册

**需等待后端的任务**:
- 任务 6.5-6.6: 屏蔽列表管理页 - 屏蔽用户 Tab
