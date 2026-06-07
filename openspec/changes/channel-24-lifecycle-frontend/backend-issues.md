# 后端遗留代码问题

**Change**: channel-24-lifecycle-frontend
**创建时间**: 2026-06-04
**状态**: 待处理

---

## 概述

前端开发依赖的后端 API 接口中，有 6 个接口尚未实现。这些接口是前端功能正常运行的必要条件，需要在前端开发开始前或同步完成。

---

## 待实现 API 接口清单

### P0 - 阻塞前端开发

#### 1. 互动数据接口

**所属 Controller**: `ChannelStatsController`
**接口路径**: `GET /api/v1/content/channel/stats/interaction`
**功能描述**: 返回频道的互动数据统计

**请求参数**:
```java
@RequestParam("channelId") String channelId
@RequestParam(value = "startDate", required = false) String startDate
@RequestParam(value = "endDate", required = false) String endDate
```

**响应数据结构**:
```json
{
  "code": 200,
  "result": {
    "likeCount": 1234,
    "commentCount": 567,
    "favoriteCount": 890,
    "shareCount": 123,
    "validVisitCount": 4567,
    "newContentCount": 89,
    "contentTypeDistribution": {
      "article": 45,
      "video": 30,
      "image": 14
    }
  }
}
```

**实现建议**:
- 在 `ChannelStatsBiz` 中新增 `getInteractionStats()` 方法
- 从 `channel_stats` 表或独立的互动统计表查询数据
- 支持时间范围筛选

---

#### 2. 导出历史列表接口

**所属 Controller**: `ChannelExportController`
**接口路径**: `GET /api/v1/content/channel/export/history`
**功能描述**: 返回用户的导出历史记录

**请求参数**:
```java
@RequestParam("channelId") String channelId
@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo
@RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize
```

**响应数据结构**:
```json
{
  "code": 200,
  "result": {
    "records": [
      {
        "id": "export-001",
        "channelId": "ch-001",
        "exportType": "stats",
        "format": "xlsx",
        "status": "completed",
        "fileUrl": "/files/export/xxx.xlsx",
        "rowCount": 15000,
        "createTime": "2026-06-01 10:00:00",
        "expireTime": "2026-06-08 10:00:00"
      }
    ],
    "total": 10,
    "pageNo": 1,
    "pageSize": 20
  }
}
```

**实现建议**:
- 从 `channel_export_task` 表查询
- 按创建时间倒序排列
- 支持分页

---

#### 3. 审核详情接口

**所属 Controller**: `ChannelReviewController`
**接口路径**: `GET /api/v1/content/channel/review/detail/{id}`
**功能描述**: 返回审核申请的详细信息

**请求参数**:
```java
@PathVariable("id") String reviewId
```

**响应数据结构**:
```json
{
  "code": 200,
  "result": {
    "id": "review-001",
    "channelId": "ch-001",
    "channelName": "测试频道",
    "channelType": "personal",
    "applicantId": "user-001",
    "applicantName": "张三",
    "applicationType": "create",
    "status": "pending",
    "submitTime": "2026-06-01 10:00:00",
    "channelDescription": "频道简介...",
    "channelIcon": "/icons/xxx.png",
    "channelCover": "/covers/xxx.jpg",
    "category": "技术",
    "historyReviews": [
      {
        "id": "review-000",
        "status": "rejected",
        "reason": "信息不完整",
        "reviewTime": "2026-05-28 14:00:00"
      }
    ]
  }
}
```

**实现建议**:
- 查询 `channel_review` 表
- 关联查询频道信息
- 返回历史审核记录

---

#### 4. 恢复可见接口

**所属 Controller**: `ChannelLifecycleController`
**接口路径**: `POST /api/v1/content/channel/lifecycle/restore-visibility`
**功能描述**: 将 Hidden 状态的频道恢复为 Active

**请求参数**:
```json
{
  "channelId": "ch-001",
  "reason": "违规内容已整改完毕"
}
```

**响应数据结构**:
```json
{
  "code": 200,
  "message": "操作成功"
}
```

**实现建议**:
- 在 `ChannelLifecycleBiz` 中新增 `restoreVisibility()` 方法
- 校验当前状态必须为 Hidden
- 更新频道状态为 Active
- 记录审计日志

---

#### 5. 按频道查询审计日志接口

**所属 Controller**: `ChannelLifecycleController`
**接口路径**: `GET /api/v1/content/channel/lifecycle/logs`
**功能描述**: 支持按频道 ID 查询审计日志

**请求参数**:
```java
@RequestParam(value = "channelId", required = false) String channelId
@RequestParam(value = "operatorId", required = false) String operatorId
@RequestParam(value = "actionType", required = false) String actionType
@RequestParam(value = "startDate", required = false) String startDate
@RequestParam(value = "endDate", required = false) String endDate
@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo
@RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize
```

**响应数据结构**:
```json
{
  "code": 200,
  "result": {
    "records": [
      {
        "id": "log-001",
        "channelId": "ch-001",
        "channelName": "测试频道",
        "operatorId": "admin-001",
        "operatorName": "管理员",
        "actionType": "freeze",
        "beforeStatus": "Active",
        "afterStatus": "ReadonlyFrozen",
        "reason": "违规内容",
        "affectScope": "频道整体",
        "createTime": "2026-06-01 10:00:00"
      }
    ],
    "total": 50,
    "pageNo": 1,
    "pageSize": 20
  }
}
```

**实现建议**:
- 当前 `/logs` 接口已存在，需增加 `channelId` 查询参数支持
- 从 `content_channel_governance_log` 表查询
- 支持多条件筛选和分页

---

#### 6. 申诉详情接口

**所属 Controller**: `ChannelLifecycleController`
**接口路径**: `GET /api/v1/content/channel/lifecycle/appeal/detail/{id}`
**功能描述**: 返回申诉的详细信息

**请求参数**:
```java
@PathVariable("id") String appealId
```

**响应数据结构**:
```json
{
  "code": 200,
  "result": {
    "id": "appeal-001",
    "channelId": "ch-001",
    "channelName": "测试频道",
    "punishmentType": "freeze",
    "punishmentReason": "违规内容",
    "appellantId": "user-001",
    "appellantName": "张三",
    "appealReason": "内容已整改",
    "supplementaryMaterials": ["/files/xxx.jpg"],
    "status": "pending",
    "submitTime": "2026-06-01 10:00:00",
    "handleRecords": [
      {
        "handlerId": "admin-001",
        "handlerName": "管理员",
        "result": "pending",
        "handleTime": null,
        "handleReason": null
      }
    ]
  }
}
```

**实现建议**:
- 查询 `channel_appeal` 表
- 关联查询处罚信息
- 返回历史处理记录

---

## 实现优先级

| 优先级 | 接口 | 阻塞程度 | 预计工作量 |
|--------|------|----------|------------|
| P0 | 互动数据接口 | 阻塞数据看板页面 | 2h |
| P0 | 导出历史列表接口 | 阻塞数据导出页面 | 1h |
| P0 | 审核详情接口 | 阻塞审核队列页面 | 2h |
| P0 | 恢复可见接口 | 阻塞频道治理页面 | 1h |
| P0 | 按频道查询审计日志 | 阻塞审计日志页面 | 1h |
| P0 | 申诉详情接口 | 阻塞申诉管理页面 | 2h |

**总预计工作量**: 9 小时

---

## 与前端开发的协调建议

### 方案 1：后端先行（推荐）

1. 后端优先实现 6 个缺失接口
2. 前端等待后端完成后再开始 API 层开发
3. 前端可先进行环境准备、组件开发等不依赖 API 的工作

### 方案 2：前后端并行

1. 前端使用 Mock 数据开始开发
2. 后端同步实现缺失接口
3. 接口完成后，前端切换到真实 API

### 方案 3：前端自定义 API 层

1. 前端在 API 层预留接口定义
2. 使用类型定义和接口文档
3. 后端实现后，前端只需调整 API 调用

---

## 相关文件

- **Controller**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/api/v1/content/channel/controller/`
- **Service**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/api/v1/content/channel/service/`
- **Biz**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/api/v1/content/channel/biz/`
- **Entity**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/api/v1/content/channel/entity/`

---

## 备注

1. 以上接口设计基于 design.md 中的需求描述，具体实现细节可与后端开发协商调整
2. 部分接口可能已存在于其他 Controller 中（如 ChannelAdminController），需确认是否可复用
3. 廏计日志接口可能只需扩展现有 `/logs` 接口的查询参数，而非新增接口
