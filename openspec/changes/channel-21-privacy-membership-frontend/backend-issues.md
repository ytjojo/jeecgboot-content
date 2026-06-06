# 后端遗留代码问题清单

**变更名称**: channel-21-privacy-membership-frontend
**创建日期**: 2026-06-04
**状态**: 待后端团队确认

---

## 1. 缺失的 API 端点

### 1.1 P0 优先级（影响核心功能）

#### 1.1.1 订阅状态查询

**问题描述**: 前端需要查询当前用户对某频道的订阅状态，后端 Service 层已有 `isSubscribed()` 方法，但 Controller 未暴露端点。

**后端现状**:
- Service: `ChannelSubscriptionService.isSubscribed(String channelId, String userId)`
- Controller: 无对应端点

**建议实现**:
```java
// ChannelSubscriptionController.java
@Operation(summary = "查询订阅状态")
@GetMapping("/status/{channelId}")
public Result<Boolean> getSubscriptionStatus(@PathVariable String channelId) {
    String userId = SecureUtil.currentUser().getId();
    return Result.OK(subscriptionService.isSubscribed(channelId, userId));
}
```

**前端依赖**: tasks.md 3.1 订阅按钮状态机需要此 API

---

#### 1.1.2 黑名单列表查询

**问题描述**: 前端需要展示黑名单用户列表，后端 Service 层已有 `listBlacklistedUserIds()` 方法，但 Controller 未暴露端点。

**后端现状**:
- Service: `ChannelBlacklistService.listBlacklistedUserIds(String channelId)`
- Controller: 无对应端点

**建议实现**:
```java
// ChannelGovernanceController.java
@Operation(summary = "黑名单列表")
@GetMapping("/blacklist/list")
public Result<List<ChannelBlacklist>> listBlacklist(@RequestParam String channelId) {
    return Result.OK(blacklistService.listByChannel(channelId));
}
```

**前端依赖**: tasks.md 6.1 黑名单管理页面需要此 API

---

#### 1.1.3 治理日志列表查询

**问题描述**: 前端需要展示治理操作日志，后端 Service 层已有 `ChannelGovernanceLogService`，但 Controller 未暴露查询端点。

**后端现状**:
- Service: `ChannelGovernanceLogService` (仅有 log 方法)
- Controller: 无查询端点

**建议实现**:
```java
// ChannelGovernanceController.java
@Operation(summary = "治理日志列表")
@GetMapping("/log")
public Result<IPage<ChannelGovernanceLog>> listGovernanceLogs(
        @RequestParam String channelId,
        @RequestParam(required = false) String actionType,
        @RequestParam(required = false) String startTime,
        @RequestParam(required = false) String endTime,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize) {
    return Result.OK(governanceLogService.listLogs(channelId, actionType, startTime, endTime, pageNum, pageSize));
}
```

**前端依赖**: tasks.md 6.2 治理日志页面需要此 API

---

#### 1.1.4 隐私设置更新

**问题描述**: 前端需要独立的隐私设置更新 API，后端目前只有通用的频道更新接口。

**后端现状**:
- Controller: `ChannelController.PUT /{id}` (通用更新)
- 无专用隐私设置端点

**建议实现**:

**方案 A：扩展现有 Controller**
```java
// ChannelController.java
@Operation(summary = "更新隐私设置")
@PutMapping("/{id}/privacy")
public Result<Void> updatePrivacy(@PathVariable String id,
                                   @RequestParam Integer privacyType) {
    String userId = SecureUtil.currentUser().getId();
    channelBizManageService.updatePrivacy(id, privacyType, userId);
    return Result.OK();
}
```

**方案 B：使用现有 API**
前端使用 `PUT /api/v1/channels/{id}` 并在 DTO 中传递 `privacyType` 字段。

**前端依赖**: tasks.md 2.1 隐私设置页面需要此 API

---

#### 1.1.5 加入方式更新

**问题描述**: 前端需要独立的加入方式更新 API，后端目前只有通用的频道更新接口。

**后端现状**:
- Controller: `ChannelController.PUT /{id}` (通用更新)
- 无专用加入方式端点

**建议实现**:

**方案 A：扩展现有 Controller**
```java
// ChannelController.java
@Operation(summary = "更新加入方式")
@PutMapping("/{id}/join-method")
public Result<Void> updateJoinMethod(@PathVariable String id,
                                      @RequestParam Integer joinMethod,
                                      @RequestParam(required = false) Boolean allowReapply,
                                      @RequestParam(required = false) Integer reapplyIntervalHours) {
    String userId = SecureUtil.currentUser().getId();
    channelBizManageService.updateJoinMethod(id, joinMethod, allowReapply, reapplyIntervalHours, userId);
    return Result.OK();
}
```

**方案 B：使用现有 API**
前端使用 `PUT /api/v1/channels/{id}` 并在 DTO 中传递 `joinMethod`、`allowReapply`、`reapplyIntervalHours` 字段。

**前端依赖**: tasks.md 2.3 加入方式配置页面需要此 API

---

#### 1.1.6 用户频道关系查询（升级为 P0）

> **FLAG-15 修正**: 此 API 原列为 P1，但它是 `useChannelContext` composable 的核心依赖，而所有频道页面均依赖该 composable。缺少此 API 将导致整个频道页面体系无法运行，因此升级为 P0。

**问题描述**: 前端 `useChannelContext` 需要查询用户与频道的关系（角色、订阅状态、禁言状态等），后端无独立端点。

**后端现状**: 无对应实现

**建议实现**:
```java
// ChannelMemberController.java
@Operation(summary = "查询用户频道关系")
@GetMapping("/relation")
public Result<UserChannelRelationVO> getUserChannelRelation(@RequestParam String channelId) {
    String userId = SecureUtil.currentUser().getId();
    return Result.OK(memberBizService.getUserChannelRelation(channelId, userId));
}
```

**前端依赖**: tasks.md 1.7 useChannelContext composable — 所有频道页面的基础依赖

**临时方案**: 在后端实现到位前，前端可通过组合已有 API 模拟此接口：
- `getSubscriptionStatus(channelId)` 获取订阅状态
- `getMemberList(channelId)` 查询当前用户获取角色和禁言状态
- 在 `useChannelContext` 中合并上述结果，待后端端点就绪后替换为单一调用

---

### 1.2 P1 优先级（影响扩展功能）

#### 1.2.1 更新提醒设置

**问题描述**: 前端计划实现订阅提醒开关功能，后端无对应 API。

**后端现状**: 无对应实现

**建议实现**:
```java
// ChannelSubscriptionController.java
@Operation(summary = "更新提醒设置")
@PutMapping("/reminder")
public Result<Void> updateReminder(@RequestParam String channelId,
                                    @RequestParam boolean enabled) {
    String userId = SecureUtil.currentUser().getId();
    subscriptionService.updateReminder(channelId, userId, enabled);
    return Result.OK();
}
```

**前端依赖**: tasks.md 3.5 订阅列表提醒开关

**建议**: 标记为 P2，后续迭代实现

---

#### 1.2.2 移动频道到分组

**问题描述**: 前端计划实现将订阅的频道移动到指定分组，后端无对应 API。

**后端现状**: 无对应实现

**建议实现**:
```java
// ChannelSubscriptionController.java
@Operation(summary = "移动频道到分组")
@PutMapping("/move-group")
public Result<Void> moveChannelToGroup(@RequestParam String channelId,
                                        @RequestParam String groupId) {
    String userId = SecureUtil.currentUser().getId();
    subscriptionService.moveToGroup(channelId, userId, groupId);
    return Result.OK();
}
```

**前端依赖**: tasks.md 3.5 订阅列表分组管理

**建议**: 标记为 P2，后续迭代实现

---

## 2. API 路径不一致问题

### 2.1 路径前缀不一致

**问题**: 前端 plan.md 中使用 `/api/channel/subscription/`，后端实际为 `/channel/subscription/`

**影响**: 联调时可能出现 404 错误

**解决方案**:
1. **方案 A**：前端配置代理，将 `/api/channel/` 转发到后端 `/channel/`
2. **方案 B**：修改前端 API 路径，去掉 `/api` 前缀
3. **方案 C**：后端 Controller 添加 `/api` 前缀

**建议**: 采用方案 A，保持前端路径一致性

---

### 2.2 groupUpdate 路径不存在

**问题**: 前端 plan.md 中使用 `PUT /group/update`，后端实际为 `POST /group/rename`

**影响**: 更新分组名称功能无法正常工作

**解决方案**:
1. 修改前端 API 路径为 `/group/rename`
2. 或后端添加 `PUT /group/update` 端点

**建议**: 修改前端 API 路径，与后端保持一致

---

## 3. 数据结构差异

### 3.1 分组更新接口参数

**前端期望**:
```typescript
updateSubscriptionGroup(data: { groupId: string; name: string })
```

**后端实际**:
```java
@PostMapping("/group/rename")
public Result<String> renameGroup(@RequestParam String groupId, @RequestParam String newName)
```

**差异**: 前端使用 PUT + JSON body，后端使用 POST + @RequestParam

**建议**: 统一为 POST + @RequestParam 或 PUT + @RequestBody

---

## 4. 待确认事项

### 4.1 隐私设置实现方式

**问题**: 隐私设置是独立 API 还是通过通用频道更新接口实现？

**选项**:
- A: 创建独立的 `ChannelPrivacyController`
- B: 在 `ChannelController` 中添加隐私相关端点
- C: 使用现有 `PUT /{id}` 接口，在 DTO 中扩展字段

**建议**: 采用方案 C，减少 API 数量，保持接口简洁

---

### 4.2 加入方式配置存储

**问题**: 加入方式配置（允许再次申请、间隔小时数）存储在哪个字段？

**待确认**:
- Channel 表是否有 `joinMethod`、`allowReapply`、`reapplyIntervalHours` 字段？
- 是否需要新建配置表？

---

### 4.3 治理日志查询条件

**问题**: 治理日志支持哪些查询条件？

**待确认**:
- 是否支持按操作类型筛选？
- 是否支持按时间范围筛选？
- 是否支持按操作者筛选？

---

## 5. 优先级建议

| 优先级 | API | 影响范围 | 建议 |
|--------|-----|---------|------|
| P0 | 订阅状态查询 | 订阅按钮状态机 | 立即实现 |
| P0 | 黑名单列表 | 黑名单管理页面 | 立即实现 |
| P0 | 治理日志列表 | 治理日志页面 | 立即实现 |
| P0 | 隐私设置更新 | 隐私设置页面 | 立即实现 |
| P0 | 加入方式更新 | 加入方式配置 | 立即实现 |
| P0 | 用户频道关系查询 | useChannelContext（所有页面基础依赖） | 立即实现，临时方案：组合 getSubscriptionStatus + getMemberList |
| P2 | 提醒设置更新 | 订阅列表 | 后续迭代 |
| P2 | 移动频道到分组 | 订阅列表 | 后续迭代 |

---

## 6. 附录：后端代码位置

| 文件 | 路径 |
|------|------|
| ChannelSubscriptionController | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelSubscriptionController.java` |
| ChannelMemberController | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMemberController.java` |
| ChannelInviteController | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelInviteController.java` |
| ChannelGovernanceController | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelGovernanceController.java` |
| ChannelController | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelController.java` |
| ChannelSubscriptionService | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelSubscriptionService.java` |
| ChannelBlacklistService | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelBlacklistService.java` |
| ChannelGovernanceLogService | `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelGovernanceLogService.java` |
