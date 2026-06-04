# 后端遗留代码问题

**关联前端 change**: user-05-blocking-muting-frontend
**关联后端 change**: user-05-blocking-muting
**创建日期**: 2026-06-04

---

## 问题 1: 缺少屏蔽用户列表分页查询端点

### 问题描述

`GET /content/user/relation/mute-list` 端点尚未实现。前端屏蔽列表管理页的"屏蔽用户" Tab 需要此端点获取已屏蔽用户的分页列表。

### 影响范围

- 前端任务 6.5: 创建 MuteListPage.vue
- 前端任务 6.6: 实现屏蔽用户 Tab（用户列表 + 取消屏蔽）

### 现有后端代码

**Controller**: `ContentUserRelationController.java`
- 已有 `POST /mute`（屏蔽用户）和 `POST /mute/cancel`（解除屏蔽）
- 缺少 `GET /mute-list`（查询屏蔽用户列表）

**Service**: `IContentUserRelationService.java`
- 已有 `mute(userId, targetUserId)` 和 `unmute(userId, targetUserId)`
- 缺少 `listMutedUsers(userId, pageNo, pageSize)` 或类似方法

### 建议实现方案

#### 1. Service 接口新增方法

```java
// IContentUserRelationService.java
/**
 * 分页查询当前用户屏蔽的用户列表。
 */
ContentUserMuteListPageVO listMutedUsers(String userId, Long pageNo, Long pageSize);
```

#### 2. ServiceImpl 实现

查询 `content_user_relation` 表中 `muted = true` 的记录，关联用户表获取头像和昵称。

#### 3. Controller 新增端点

```java
// ContentUserRelationController.java
@Operation(summary = "分页查询屏蔽列表")
@GetMapping("/mute-list")
public Result<ContentUserMuteListPageVO> muteList(
    @RequestParam("userId") String userId,
    @RequestParam(value = "pageNo", required = false, defaultValue = "1") Long pageNo,
    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
    return Result.OK(relationService.listMutedUsers(userId, pageNo, pageSize));
}
```

#### 4. VO 定义

```java
// ContentUserMuteListPageVO.java
@Data
public class ContentUserMuteListPageVO {
    private List<ContentMuteUserItemVO> records;
    private Long total;
    private Long pageNo;
    private Long pageSize;
}

// ContentMuteUserItemVO.java
@Data
public class ContentMuteUserItemVO {
    private String mutedUserId;
    private String nickname;
    private String avatar;
    private Date muteTime;
}
```

#### 5. 测试

在 `ContentUserRelationControllerWebMvcTest.java` 中补充:
- 正常分页查询测试
- 空列表测试
- 参数校验测试

### 验收标准

- `GET /content/user/relation/mute-list?userId=xxx&pageNo=1&pageSize=10` 返回 200 + 分页数据
- `mvn test -pl jeecg-boot-module/jeecg-module-content -am` 全量通过

---

## 已确认不存在的问题（旧 verification.md 误判）

以下问题在旧 `verification.md` 中被标记为 CRITICAL，但经本次代码验证已确认**不存在**:

| 旧问题 | 当前状态 | 验证依据 |
|--------|----------|----------|
| Filter Rule Controller 缺失 | ✅ 已实现 | `ContentUserFilterRuleController.java` 完整实现 4 个端点 |
| Not Interested Controller 缺失 | ✅ 已实现 | `ContentUserNotInterestedController.java` 完整实现 1 个端点 |
| API 路径 blacklist → block 未修复 | ✅ 已修复 | Controller 中使用 `block`/`unblock` 路径 |
