# 后端遗留问题清单

**创建时间**: 2026-06-04
**关联 Change**: circle-11-content-interaction-frontend

---

## 问题汇总

| 编号 | 问题 | 严重程度 | 影响范围 | 状态 |
|------|------|---------|---------|------|
| BE-01 | 缺少 `GET /api/v1/content/circle/{circleId}/mentionable-members` Controller 端点 | 高 | @成员功能完全依赖此接口 | 待实现 |
| BE-02 | 缺少 `DELETE /circle-announcement/{id}` 接口 | 中 | 公告删除功能无法实现 | 待实现 |
| BE-03 | 缺少 `pendingJoinRequestCount` 查询接口 | 中 | 管理入口角标无法实现 | 待实现 |
| BE-04 | `handleMute` 不接受禁言时长参数，且实现为 TODO | 中 | 禁言功能无法正常工作 | 待实现 |
| BE-05 | 举报提交 `CircleReportReq` 无举报原因枚举校验 | 低 | 前端需自行维护枚举映射 | 可接受 |

---

## 详细说明

### BE-01: 缺少 @成员查询 Controller 端点

**问题描述**: 后端已有 `ICircleMentionService.getMentionCandidates()` Service 层方法，但无对应 Controller 端点暴露给前端。

**期望接口**:
```
GET /api/v1/content/circle/{circleId}/mentionable-members
```

**影响**: @成员选择浮层无法获取成员列表，功能完全不可用。

**建议**: 在现有 Controller 中添加端点，调用 `ICircleMentionService.getMentionCandidates()` 并返回成员列表。

---

### BE-02: 缺少公告删除接口

**问题描述**: 后端 `CircleAnnouncementController` 仅有 `publish` 和 `getActive` 接口，无删除公告接口。

**期望接口**:
```
DELETE /circle-announcement/{id}
```

**影响**: 管理员无法删除已发布的公告，只能发布新公告覆盖。

**建议**: 添加删除接口，支持按公告 ID 删除。考虑到"单条生效"模型，也可设计为"取消当前公告"语义。

---

### BE-03: 缺少待审核申请计数接口

**问题描述**: 前端需要在圈子管理入口展示待审核申请数量的角标，后端无此查询接口。

**期望接口**:
```
GET /api/v1/content/circle/{circleId}/pending-join-request-count
返回: { count: number }
```

**影响**: 管理入口无法展示红点/角标提醒管理员有待处理申请。

**建议**: 添加聚合查询接口，返回指定圈子的待审核申请数量。

---

### BE-04: 禁言功能未完整实现

**问题描述**: 后端 `CircleReportController.handleMute()` 方法中禁言逻辑标注为 `// TODO: 调用禁言服务对被举报用户执行禁言`，且不接受禁言时长参数。

**当前实现**:
```java
// TODO: 调用禁言服务对被举报用户执行禁言
```

**期望实现**:
1. 接受禁言时长参数（duration: 1h/1d/7d/30d/permanent）
2. 调用禁言服务记录禁言状态
3. 禁言到期后自动解除

**影响**: 禁言用户操作无实际效果，举报处理流程不完整。

**建议**: 实现禁言服务，支持可配置的禁言时长，并与圈子成员状态关联。

---

### BE-05: 举报原因枚举无后端校验（可接受）

**问题描述**: 前端定义的举报原因枚举（AD/PORNO/ATTACK/OTHER）在后端 `CircleReportReq` 中无校验。

**当前状态**: 后端接受任意字符串作为举报原因。

**影响**: 前端需自行维护枚举映射表将枚举码转为中文文案。功能可用，但前后端未共享枚举定义。

**建议**: 后续可在后端添加枚举校验，或提供原因文案查询接口。当前可接受前端维护映射。

---

## 前端开发注意事项

1. **接口联调顺序**: 优先对接已实现的接口（置顶/精华、举报提交/列表、申请审核批准/拒绝），缺失接口待后端补充后联调
2. **Mock 策略**: 对缺失接口使用 Mock 数据开发前端逻辑，确保接口就绪后可快速切换
3. **降级方案**: 
   - @成员功能：可先使用圈子成员列表接口替代
   - 公告删除：可先不实现，依赖发布新公告覆盖
   - 管理角标：可先不展示，待接口就绪后补充
