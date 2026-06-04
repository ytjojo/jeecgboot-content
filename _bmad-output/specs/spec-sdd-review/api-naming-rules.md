# API 命名规范

## 核心原则

API 路径是**业务语义的表达**，不是技术实现的映射。路径必须让消费方（前端、第三方）无需了解后端实现细节即可理解其含义。

## 命名规则

### R1: 资源名用复数名词

```
正确: GET /users, GET /users/{id}, GET /articles
错误: GET /user, GET /getUser, GET /userList
```

### R2: 动作用 HTTP method 表达

```
正确: POST /users/{id}/blocks    (拉黑用户)
正确: DELETE /users/{id}/blocks  (解除拉黑)
错误: POST /blackList            (技术术语 + 非 RESTful)
错误: POST /user/blockUser       (路径含动词)
```

### R3: 路径中禁止技术实现术语

以下词汇不得出现在 API 路径中：

| 禁止词汇 | 正确替代 | 说明 |
|---------|---------|------|
| blackList | /users/{id}/blocks | 业务语义：用户拉黑 |
| whiteList | /users/{id}/allows | 业务语义：用户白名单 |
| cache | 直接用资源路径 | 缓存是实现细节 |
| queue | 直接用资源路径 | 队列是实现细节 |
| db/database | 直接用资源路径 | 存储是实现细节 |
| redis/mongo/mysql | 直接用资源路径 | 技术选型是实现细节 |
| rpc/grpc | 直接用资源路径 | 通信协议是实现细节 |
| callback/webhook | /events 或 /notifications | 业务语义：事件通知 |

### R4: 路径层级反映资源关系

```
正确: GET /users/{userId}/blocks         (用户的拉黑列表)
正确: GET /users/{userId}/blocks/{blockId} (特定拉黑记录)
错误: GET /userBlock                      (扁平化，丢失资源关系)
错误: GET /getBlackListByUserId           (RPC 风格)
```

### R5: 无 RPC 风格路径

禁止：
```
GET /getUserList          → GET /users
POST /submitOrder         → POST /orders
GET /fetchUserInfo        → GET /users/{id}
POST /updatePassword      → PUT /users/{id}/password
GET /queryOrderDetail     → GET /orders/{id}
POST /deleteArticle       → DELETE /articles/{id}
```

### R6: 路径全小写，单词用连字符分隔

```
正确: GET /user-profiles, GET /order-items
错误: GET /userProfiles, GET /UserProfiles, GET /user_profiles
```

### R7: 版本号在路径开头或请求头

```
方式一: GET /v1/users, GET /v2/users
方式二: Accept: application/vnd.api.v1+json
```

## 反面案例库

### 案例 1: blackList（单用户拉黑）

**问题**: `POST /blackList` — 技术术语、非 RESTful、无资源关系

**正确**: `POST /users/{userId}/blocks`
- 请求体: `{ "reason": "spam", "duration": "7d" }`
- 响应: `{ "userId": "123", "blockedAt": "2026-06-04T10:00:00Z", "expiresAt": "2026-06-11T10:00:00Z" }`

**解除拉黑**: `DELETE /users/{userId}/blocks`

**查询拉黑列表**: `GET /users/{userId}/blocks`

**查询是否被拉黑**: `GET /users/{userId}/blocks/status`

### 案例 2: getUserList

**问题**: `GET /getUserList` — RPC 风格、路径含动词

**正确**: `GET /users?status=active&page=1&size=20`
- 用 query parameter 表达筛选条件
- 用 HTTP method 表达操作意图

### 案例 3: submitOrder

**问题**: `POST /submitOrder` — RPC 风格

**正确**: `POST /orders`
- 请求体包含订单数据
- 响应包含创建的订单

## 审核检查方法

审核 SDD 中的 API 定义时，逐条检查：

1. 提取 API 路径
2. 检查路径中每个单词：
   - 是资源名词？→ 检查是否复数
   - 是动词？→ 违规（R2、R5）
   - 是技术术语？→ 违规（R3）
3. 检查路径层级是否反映资源关系（R4）
4. 检查大小写和分隔符（R6）
5. 记录违规项，标注严重级别
