# 后端遗留问题文档

**文档用途**: 记录前端功能实现所需的后端 API 缺失问题
**创建时间**: 2026-06-04
**关联 Change**: channel-20-infrastructure-frontend

---

## 1. 问题概览

前端频道管理功能需要 15 个后端 API 支持，其中 **5 个 API 尚未实现**，需要后端团队补充开发。

| 优先级 | 数量 | 说明 |
|--------|------|------|
| CRITICAL | 2 | 核心功能必需，不实现则前端页面无法工作 |
| HIGH | 2 | 重要功能，影响用户体验 |
| MEDIUM | 1 | 辅助功能，可后续迭代 |

---

## 2. 缺失 API 详细说明

### 2.1 频道列表查询接口 [CRITICAL]

**用途**: "我的频道"页面展示用户创建的所有频道列表

**建议实现**:
```java
// 文件: ChannelController.java
// 路径: /api/v1/channels

@GetMapping("/list")
@Operation(summary = "查询我的频道列表")
public Result<IPage<ChannelVO>> listMyChannels(
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) String channelType,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword) {
    String userId = SecureUtil.currentUser().getId();
    // 查询条件: owner_id = userId
    // 支持按 channelType 和 status 筛选
    // 支持按 keyword 模糊搜索名称
    // 默认按 created_time 倒序排列
    // PendingReview 和 Rejected 状态置顶
}
```

**响应数据结构**:
```java
public class ChannelVO {
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private String coverUrl;
    private String channelType;  // system/personal/organization
    private String status;       // DRAFT/PENDING_REVIEW/ACTIVE/REJECTED/DELETE_COOLING/DELETED
    private String categoryName;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

**前端使用场景**:
- 任务 3.1: 实现"我的频道"列表页
- 任务 3.2: 实现列表列定义
- 任务 3.3: 实现筛选区
- 任务 3.6: 实现审核状态排序

---

### 2.2 删除前置条件校验接口 [CRITICAL]

**用途**: 删除频道前校验是否满足删除条件，不满足时展示阻塞原因

**建议实现**:
```java
// 文件: ChannelController.java
// 路径: /api/v1/channels

@GetMapping("/{id}/delete-check")
@Operation(summary = "删除前置条件校验")
public Result<DeleteCheckResultVO> checkDeletePrecondition(@PathVariable String id) {
    String userId = SecureUtil.currentUser().getId();
    // 检查项:
    // 1. 是否有未清理的内容
    // 2. 是否有待处理的转让请求
    // 3. 是否有待审核的修改
    // 4. 是否是组织频道（需要组织管理员确认）
}
```

**响应数据结构**:
```java
public class DeleteCheckResultVO {
    private boolean canDelete;           // 是否可删除
    private List<String> blockReasons;   // 阻塞原因列表
    private boolean needOrgAdminConfirm; // 是否需要组织管理员确认
}
```

**前端使用场景**:
- 任务 6.1: 实现 DeleteConfirmModal 组件
- 任务 6.2: 实现删除前置条件校验

---

### 2.3 转让历史查询接口 [HIGH]

**用途**: 频道管理页设置区域展示转让历史记录

**建议实现**:
```java
// 文件: ChannelController.java
// 路径: /api/v1/channels

@GetMapping("/{id}/transfers")
@Operation(summary = "查询转让历史")
public Result<List<ChannelTransferVO>> getTransferHistory(@PathVariable String id) {
    // 查询该频道的所有转让记录
    // 按创建时间倒序排列
}
```

**响应数据结构**:
```java
public class ChannelTransferVO {
    private String transferId;
    private String channelId;
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String toUserName;
    private String status;        // pending/confirmed/rejected/expired
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
}
```

**前端使用场景**:
- 任务 5.6: 实现转让历史记录展示

---

### 2.4 名称唯一性校验接口 [HIGH]

**用途**: 创建和编辑频道时实时校验名称是否已存在

**建议实现**:
```java
// 文件: ChannelController.java
// 路径: /api/v1/channels

@GetMapping("/check-name")
@Operation(summary = "校验频道名称唯一性")
public Result<NameCheckResultVO> checkNameUnique(
        @RequestParam String name,
        @RequestParam(required = false) String excludeId) {
    // excludeId: 编辑时排除当前频道自身
    // 返回是否可用
}
```

**响应数据结构**:
```java
public class NameCheckResultVO {
    private boolean available;  // 是否可用
    private String message;     // 不可用时的提示信息
}
```

**前端使用场景**:
- 任务 2.3: 实现频道名称唯一性校验逻辑
- 任务 4.6: 实现名称唯一性校验（编辑时排除当前频道自身）

---

### 2.5 查询待确认转让请求接口 [MEDIUM]

**用途**: 检查频道是否有进行中的转让请求，控制转让按钮状态

**建议实现**:
```java
// 文件: ChannelController.java
// 路径: /api/v1/channels

@GetMapping("/{id}/transfer/pending")
@Operation(summary = "查询待确认的转让请求")
public Result<ChannelTransferVO> getPendingTransfer(@PathVariable String id) {
    // 查询该频道状态为 pending 的转让请求
    // 无则返回 null
}
```

**前端使用场景**:
- 任务 5.5: 实现转让按钮状态（转让请求已存在时禁用）

---

## 3. 已有 API 需确认的问题

### 3.1 API 路径前缀不一致

后端存在多种路径前缀，需确认是否有统一计划：

| Controller | 当前前缀 | 建议 |
|------------|----------|------|
| ChannelController | `/api/v1/channels` | 保持 |
| ChannelAdminController | `/api/v1/admin/channels` | 保持 |
| ChannelReviewController | `/api/v1/content/channel/review` | 考虑简化 |
| ChannelLifecycleController | `/api/v1/content/channel/lifecycle` | 考虑简化 |
| ChannelGovernanceController | `/channel/governance` | 需统一前缀 |
| ChannelPublishController | `/api/v1/content/channel/publish` | 需统一前缀 |

### 3.2 ChannelReviewController 中的 TODO

文件 `ChannelReviewController.java` 第 117-118 行存在 TODO:
```java
private String getCurrentUserId() {
    // TODO: 从安全上下文获取当前用户ID
    return "current-user-id";
}
```

需要实现从安全上下文获取真实用户 ID。

### 3.3 ChannelLifecycleController 中的 TODO

文件 `ChannelLifecycleController.java` 第 159-161 行存在同样的 TODO:
```java
private String getCurrentUserId() {
    // TODO: 从安全上下文获取当前用户ID
    return "current-user-id";
}
```

需要实现从安全上下文获取真实用户 ID。

---

## 4. 实现建议

### 4.1 开发顺序

建议按以下顺序实现缺失 API：

1. **第一优先级** (阻塞前端核心功能):
   - 频道列表查询接口
   - 删除前置条件校验接口

2. **第二优先级** (影响用户体验):
   - 名称唯一性校验接口
   - 转让历史查询接口

3. **第三优先级** (辅助功能):
   - 查询待确认转让请求接口

### 4.2 实现注意事项

1. **权限校验**: 所有接口需校验当前用户是否有权限操作
2. **数据校验**: 参数需使用 Jakarta Validation 注解校验
3. **异常处理**: 使用 `Result.error()` 返回友好错误信息
4. **日志记录**: 关键操作需记录操作日志
5. **单元测试**: 每个接口需编写单元测试，覆盖率 ≥ 90%

### 4.3 与现有代码的集成

- 复用现有的 `ChannelService`、`ChannelBizManageService` 等服务
- 遵循现有的代码组织规范（Controller → Biz → Service → Mapper）
- 使用现有的 `ChannelConvertUtil` 进行 VO 转换

---

## 5. 验收标准

后端 API 完成的标准：

1. **功能完整**: 接口实现满足前端需求
2. **测试通过**: 单元测试覆盖率 ≥ 90%，全部通过
3. **文档更新**: Swagger 注解完整，API 文档可访问
4. **代码规范**: 符合项目编码规范，无代码质量问题
5. **性能达标**: 响应时间 < 200ms（简单查询）

---

**联系方式**: 如有疑问请联系前端开发团队
**文档版本**: v1.0
**最后更新**: 2026-06-04
