# EPIC-09 用户状态与生命周期治理 -- 前端 PRD

> **史诗ID**: EPIC-09
> **域**: user（用户域）
> **变更ID**: user-09-status-lifecycle-frontend
> **版本**: 1.0
> **前置依赖**: 无
> **日期**: 2026-06-02

---

## 1. 概述

### 1.1 需求目标

内容社区缺少统一的用户状态管理模型和审计机制。本变更补齐以下能力：

- 定义 9 种用户状态（游客/未完善/正常/禁言/限制推荐/冻结/封禁/注销中/已注销），统一状态机管理
- 管理员可手动变更用户状态、查看状态历史、执行解禁操作
- 所有状态变更自动记录审计日志，支持追溯和合规导出
- 用户可在个人中心查看自身状态及处罚详情，被禁言/封禁时收到明确提示
- 支持自动解禁、人工解禁和申诉恢复（对接 EPIC-08 申诉系统）

### 1.2 目标用户与使用场景

| 角色 | 使用场景 | 入口 |
|------|---------|------|
| 注册用户 | 查看自身状态、处罚详情、剩余时间；被限制时收到提示 | 个人中心"账号状态"菜单 |
| 管理员 | 查询用户状态、手动变更状态、查看审计日志、执行解禁、导出审计报告 | 后台管理"用户状态管理"模块 |
| 被禁言用户 | 发表评论/私信/发动态时被拦截并提示禁言信息 | 各互动入口的拦截提示 |
| 被冻结/封禁用户 | 登录时被阻止并提示原因和期限 | 登录页面拦截 |

### 1.3 范围定义

#### 本期范围
- 用户状态管理页（管理员后台）：状态查询、状态变更、批量操作
- 审计日志页（管理员后台）：日志列表、筛选、详情、导出
- 用户端状态展示：个人中心"账号状态"页，展示当前状态、处罚详情、历史记录
- 登录拦截提示：冻结/封禁用户登录时的拦截页面
- 互动拦截提示：禁言用户发表评论/私信/发动态时的拦截提示

#### 非本期范围
- 独立风控引擎 UI（本次不实现机器学习异常检测相关界面）
- 审计日志冷存储管理界面
- 补偿机制管理界面（仅预留接口）

---

## 2. 用户故事

### US-01 管理员查询用户状态
**作为** 管理员，**我希望** 通过用户 ID 或用户名查询用户当前状态及状态详情，**以便** 了解用户当前受何种限制。

**验收标准**:
1. 输入用户 ID 或用户名后，展示该用户的当前状态（9 种状态之一）
2. 展示状态详情：状态开始时间、状态结束时间、状态原因、操作人
3. 查询响应时间 <100ms
4. 查询不存在的用户时提示"用户不存在"

### US-02 管理员手动变更用户状态
**作为** 管理员，**我希望** 手动变更用户状态（如禁言、封禁、解禁），填写原因和期限，**以便** 执行治理操作。

**验收标准**:
1. 选择目标状态后，根据状态类型展示对应表单（原因、期限等）
2. 禁言/封禁操作需填写原因和期限（支持永久封禁选项）
3. 解禁操作需填写解禁原因
4. 提交前进行二次确认："确定将用户 XXX 变更为 XXX 状态？"
5. 操作成功后刷新用户状态信息并提示"状态变更成功"
6. 并发冲突时提示"状态已变更，请刷新后重试"（乐观锁机制）
7. 非法状态转换时提示"当前状态不允许执行此操作"

### US-03 管理员查看审计日志
**作为** 管理员，**我希望** 查看用户状态变更的审计日志，按时间倒序排列，支持筛选和导出，**以便** 进行合规审计和追溯。

**验收标准**:
1. 审计日志列表展示：变更时间、用户 ID、原状态、新状态、操作人、触发原因
2. 支持按用户 ID、时间范围、操作类型（系统/管理员）筛选
3. 点击日志行可查看详情：完整字段信息（log_id、user_id、from_status、to_status、operator_id、operator_type、trigger_reason、rule_id、start_time、end_time、remark、ip_address、created_at）
4. 支持导出 Excel/CSV 文件
5. 审计日志为只读，不可修改或删除

### US-04 管理员执行解禁操作
**作为** 管理员，**我希望** 对被处罚用户执行提前解禁操作，**以便** 灵活处理特殊情况。

**验收标准**:
1. 在用户状态详情页或状态管理列表中提供"解禁"按钮
2. 点击后弹出确认弹窗，需填写解禁原因
3. 确认后立即恢复用户状态为"正常"
4. 操作记录写入审计日志
5. 解禁成功后发送站内通知给用户

### US-05 用户查看自身状态
**作为** 用户，**我希望** 在个人中心查看我的账号当前状态、处罚详情和剩余时间，**以便** 了解账号情况。

**验收标准**:
1. 个人中心"账号状态"页面展示当前状态标签（带颜色标识）
2. 非正常状态时展示：处罚原因、开始时间、结束时间、剩余时间
3. 禁言状态显示"禁言中，剩余 X 天 X 小时"
4. 正常状态显示"您的账号状态正常"
5. 展示历史状态变更记录列表（按时间倒序）

### US-06 用户查看状态历史
**作为** 用户，**我希望** 查看我的状态变更历史记录，**以便** 了解账号状态变化过程。

**验收标准**:
1. "状态历史"页面展示按时间倒序排列的所有状态变更记录
2. 每条记录展示：变更时间、原状态、新状态、原因（脱敏显示）
3. 支持分页加载
4. 用户只能查看自己的状态历史

### US-07 禁言用户互动拦截提示
**作为** 被禁言用户，**当我** 尝试发表评论/私信/发动态时，**我希望** 收到清晰的禁言提示，**以便** 了解限制原因和期限。

**验收标准**:
1. 禁言用户点击"发表评论"按钮时，弹出提示"您已被禁言，禁言期限：{endTime}，原因：{reason}"
2. 禁言用户点击"发送私信"按钮时，弹出相同提示
3. 禁言用户点击"发布动态"按钮时，弹出相同提示
4. 提示信息包含禁言剩余时间和原因
5. 禁言用户仍可浏览内容、点赞、收藏（被动互动不受影响）

### US-08 冻结/封禁用户登录拦截
**作为** 被冻结/封禁用户，**当我** 尝试登录时，**我希望** 收到明确的拦截提示，**以便** 了解账号状态和后续操作。

**验收标准**:
1. 冻结用户登录时，页面显示"账号已冻结，请进行安全核验"，提供"安全核验"按钮
2. 封禁用户登录时，页面显示"账号已被封禁，封禁期限：{endTime}，原因：{reason}"
3. 永久封禁显示"账号已被永久封禁，原因：{reason}"
4. 封禁页面提供"申诉"入口（跳转到 EPIC-08 申诉系统）
5. 拦截页面不显示正常的登录表单

### US-09 冻结用户安全核验解冻
**作为** 被冻结用户，**当我** 完成安全核验（如手机验证码），**我希望** 账号恢复正常，**以便** 继续使用平台。

**验收标准**:
1. 安全核验页面展示手机验证码输入框
2. 发送验证码后显示倒计时（60 秒）
3. 验证码正确后自动恢复账号为"正常"状态
4. 核验成功后跳转到首页并提示"账号已恢复正常"
5. 验证码错误时提示"验证码错误，请重新输入"

---

## 3. 页面设计

### 3.1 管理后台 -- 用户状态管理页

**路由**: `/content/user-status/manage`

**页面布局**: 列表页模式

```
Page
  └── 查询表单 (Form)
  │   ├── 用户ID/用户名 输入框
  │   ├── 用户状态下拉选择（全部/正常/禁言/限制推荐/冻结/封禁/注销中/已注销）
  │   └── 查询/重置按钮
  └── 操作按钮区 (Button)
  │   ├── 批量解禁
  │   └── 导出
  └── 数据表格 (JVxeTable)
      ├── 用户ID
      ├── 用户名
      ├── 当前状态（Tag 标签，不同状态不同颜色）
      ├── 状态开始时间
      ├── 状态结束时间
      ├── 状态原因
      ├── 操作人
      └── 操作列
          ├── 查看详情（跳转状态详情）
          ├── 变更状态（弹窗）
          ├── 解禁（弹窗确认）
          └── 状态历史（抽屉）
  └── 变更状态弹窗 (Modal + Form)
      ├── 目标状态下拉选择（仅显示允许转换的状态）
      ├── 原因文本域
      ├── 期限选择（日期时间选择器，禁言/封禁时显示）
      └── 永久封禁复选框（封禁时显示）
  └── 解禁确认弹窗 (Modal)
      ├── 解禁原因文本域
      └── 确认/取消按钮
```

### 3.2 管理后台 -- 审计日志页

**路由**: `/content/user-status/audit-log`

**页面布局**: 列表页模式

```
Page
  └── 查询表单 (Form)
  │   ├── 用户ID 输入框
  │   ├── 操作类型下拉选择（全部/系统自动/管理员手动）
  │   ├── 时间范围选择器（RangePicker）
  │   └── 查询/重置按钮
  └── 操作按钮区 (Button)
  │   └── 导出 Excel/CSV
  └── 数据表格 (JVxeTable)
      ├── 日志ID
      ├── 用户ID
      ├── 原状态（Tag）
      ├── 新状态（Tag）
      ├── 操作人
      ├── 操作类型（系统/管理员）
      ├── 触发原因
      ├── 操作时间
      └── 操作列
          └── 查看详情（弹窗）
  └── 详情弹窗 (Modal + Description)
      ├── log_id
      ├── user_id
      ├── from_status
      ├── to_status
      ├── operator_id
      ├── operator_type
      ├── trigger_reason
      ├── rule_id
      ├── start_time
      ├── end_time
      ├── remark
      ├── ip_address
      └── created_at
```

### 3.3 用户端 -- 账号状态页

**路由**: `/user/account-status`

**页面布局**: 详情页模式

```
Page
  └── 当前状态卡片 (Card)
  │   ├── 状态标签（Tag，带颜色）
  │   ├── 状态描述文字
  │   ├── 处罚原因（非正常状态时显示）
  │   ├── 开始时间
  │   ├── 结束时间（永久封禁显示"永久"）
  │   ├── 剩余时间倒计时（禁言/临时封禁时显示）
  │   └── 操作按钮
  │       ├── 申诉（跳转 EPIC-08 申诉系统）
  │       └── 安全核验（冻结状态时显示）
  └── 状态历史列表 (Table)
      ├── 变更时间
      ├── 原状态
      ├── 新状态
      └── 原因（脱敏）
```

### 3.4 用户端 -- 登录拦截页

**路由**: `/login/blocked`

**页面布局**: 单页模式

```
Page
  └── 拦截信息卡片 (Card)
  │   ├── 状态图标（冻结/封禁对应的图标）
  │   ├── 标题："账号已冻结" 或 "账号已被封禁"
  │   ├── 原因说明
  │   ├── 期限信息
  │   ├── 剩余时间（临时封禁时显示）
  │   └── 操作按钮
  │       ├── 安全核验（冻结时显示）
  │       ├── 申诉（封禁时显示，跳转 EPIC-08）
  │       └── 返回登录页
```

### 3.5 用户端 -- 安全核验页

**路由**: `/login/verify`

**页面布局**: 表单页模式

```
Page
  └── 核验表单 (Form)
  │   ├── 手机号码显示（部分脱敏：138****1234）
  │   ├── 验证码输入框
  │   ├── 发送验证码按钮（带 60 秒倒计时）
  │   └── 提交按钮
```

---

## 4. 组件设计

### 4.1 核心页面组件

#### UserStatusManage.vue -- 用户状态管理页
- **路径**: `src/views/content/user-status/manage/index.vue`
- **职责**: 管理员查询和管理用户状态
- **子组件**: StatusChangeModal.vue, StatusReleaseModal.vue, StatusHistoryDrawer.vue

#### AuditLogList.vue -- 审计日志页
- **路径**: `src/views/content/user-status/audit-log/index.vue`
- **职责**: 审计日志查询、详情查看、导出
- **子组件**: AuditLogDetailModal.vue

#### AccountStatus.vue -- 用户端账号状态页
- **路径**: `src/views/user/account-status/index.vue`
- **职责**: 用户查看自身状态和历史记录

#### LoginBlocked.vue -- 登录拦截页
- **路径**: `src/views/login/blocked/index.vue`
- **职责**: 冻结/封禁用户的登录拦截提示

#### SecurityVerify.vue -- 安全核验页
- **路径**: `src/views/login/verify/index.vue`
- **职责**: 冻结用户手机验证码核验

### 4.2 功能组件

#### StatusTag.vue -- 状态标签组件
- **路径**: `src/components/jeecg/UserStatus/StatusTag.vue`
- **职责**: 根据状态枚举值渲染带颜色的标签
- **Props**: `status` (UserStatusEnum), `showTooltip` (boolean)
- **状态颜色映射**:
  - GUEST: 灰色 (default)
  - REGISTERED_INCOMPLETE: 蓝色 (processing)
  - NORMAL: 绿色 (success)
  - MUTED: 橙色 (warning)
  - RESTRICTED_RECOMMEND: 黄色 (warning)
  - FROZEN: 红色 (error)
  - BANNED: 深红色 (error)
  - DEACTIVATING: 紫色 (processing)
  - DEACTIVATED: 灰色 (default)

#### StatusChangeModal.vue -- 状态变更弹窗
- **路径**: `src/components/jeecg/UserStatus/StatusChangeModal.vue`
- **职责**: 管理员变更用户状态的表单弹窗
- **Props**: `userId` (string), `currentStatus` (UserStatusEnum)
- **Events**: `@success` 变更成功回调

#### StatusReleaseModal.vue -- 解禁确认弹窗
- **路径**: `src/components/jeecg/UserStatus/StatusReleaseModal.vue`
- **职责**: 管理员解禁操作的确认弹窗
- **Props**: `userId` (string), `userName` (string)
- **Events**: `@success` 解禁成功回调

#### StatusHistoryDrawer.vue -- 状态历史抽屉
- **路径**: `src/components/jeecg/UserStatus/StatusHistoryDrawer.vue`
- **职责**: 以抽屉形式展示用户状态变更历史
- **Props**: `userId` (string), `visible` (boolean)
- **Events**: `@close` 关闭抽屉

#### AuditLogDetailModal.vue -- 审计日志详情弹窗
- **路径**: `src/components/jeecg/UserStatus/AuditLogDetailModal.vue`
- **职责**: 展示审计日志完整字段信息
- **Props**: `logId` (string)

#### StatusCountdown.vue -- 状态倒计时组件
- **路径**: `src/components/jeecg/UserStatus/StatusCountdown.vue`
- **职责**: 展示禁言/封禁剩余时间倒计时
- **Props**: `endTime` (string), `status` (UserStatusEnum)
- **倒计时实现方案**:
  - 纯前端计算：基于 `dayjs(endTime).diff(dayjs())` 计算剩余时间，使用 `setInterval` 每秒更新
  - 组件销毁时（`onUnmounted`）清理定时器，防止内存泄漏
  - 监听 `document.visibilitychange` 事件：页面从后台恢复可见时，立即刷新倒计时基准值，修正后台挂起期间的精度偏差
  - 倒计时归零时触发 `fetchCurrentStatus()` 验证后端是否已自动解禁，若已解禁则更新 UserStatusStore 并刷新页面状态展示

### 4.3 API 接口

> **设计说明**: 列表查询使用 `/users` 路径前缀与单个用户查询的 `/{userId}` 路径区分，避免 `/list` 与 `{userId}` 路径冲突。`change` 和 `release` 采用 RPC 风格端点，因其语义为操作动作而非资源名词，这是有意的设计选择。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询当前用户状态 | GET | `/api/content/user-status/current` | 返回当前用户状态详情 |
| 查询指定用户状态 | GET | `/api/content/user-status/users/{userId}` | 管理员查询指定用户状态 |
| 用户状态列表 | GET | `/api/content/user-status/users` | 管理员分页查询用户状态列表，支持 query 参数筛选 |
| 获取可转换状态列表 | GET | `/api/content/user-status/transitions/{currentStatus}` | 返回当前状态允许转换的目标状态列表 |
| 变更用户状态 | POST | `/api/content/user-status/users/{userId}/change` | 管理员手动变更用户状态（RPC 风格） |
| 解禁用户 | POST | `/api/content/user-status/users/{userId}/release` | 管理员手动解禁（RPC 风格） |
| 批量解禁 | POST | `/api/content/user-status/users/batch-release` | 批量解禁操作 |
| 用户状态历史 | GET | `/api/content/user-status/users/{userId}/history` | 查询用户状态变更历史 |
| 审计日志列表 | GET | `/api/content/user-status-audit/logs` | 分页查询审计日志 |
| 审计日志详情 | GET | `/api/content/user-status-audit/logs/{logId}` | 查询审计日志详情 |
| 用户审计日志 | GET | `/api/content/user-status-audit/logs/user/{userId}` | 查询指定用户的审计日志 |
| 导出审计日志 | POST | `/api/content/user-status-audit/export` | 导出审计日志 Excel/CSV，后端生成文件流 |
| 安全核验 | POST | `/api/content/user-status/verify` | 冻结用户安全核验 |
| 发送验证码 | POST | `/api/content/user-status/send-code` | 发送手机验证码 |

---

## 5. 状态管理

### 5.1 UserStatusStore (Pinia)

**路径**: `src/store/modules/userStatus.ts`

```typescript
interface UserStatusState {
  // 当前用户状态
  currentStatus: UserStatusEnum | null;
  statusDetail: UserStatusDetail | null;
  statusHistory: UserStatusHistoryItem[];
  // 加载状态
  loading: boolean;
}

interface UserStatusDetail {
  userId: string;
  status: UserStatusEnum;
  statusStartTime: string;
  statusEndTime: string | null;
  statusReason: string;
  statusOperatorId: string;
}

interface UserStatusHistoryItem {
  logId: string;
  fromStatus: UserStatusEnum;
  toStatus: UserStatusEnum;
  triggerReason: string;
  operatorId: string;
  operatorType: 'SYSTEM' | 'ADMIN';
  createdAt: string;
}

enum UserStatusEnum {
  GUEST = 'GUEST',
  REGISTERED_INCOMPLETE = 'REGISTERED_INCOMPLETE',
  NORMAL = 'NORMAL',
  MUTED = 'MUTED',
  RESTRICTED_RECOMMEND = 'RESTRICTED_RECOMMEND',
  FROZEN = 'FROZEN',
  BANNED = 'BANNED',
  DEACTIVATING = 'DEACTIVATING',
  DEACTIVATED = 'DEACTIVATED',
}
```

**Store Actions**:
- `fetchCurrentStatus()` -- 获取当前用户状态
- `fetchUserStatus(userId)` -- 获取指定用户状态（管理员）
- `fetchStatusHistory(userId)` -- 获取状态变更历史
- `fetchTransitions(currentStatus)` -- 获取当前状态可转换的目标状态列表
- `changeStatus(userId, payload)` -- 变更用户状态
- `releaseUser(userId, reason)` -- 解禁用户
- `batchRelease(userIds, reason)` -- 批量解禁
- `verifySecurity(code)` -- 安全核验
- `refreshStatus()` -- 刷新当前用户状态（供 useStatusGuard 调用）

### 5.2 与现有 Store 的集成

- **useUserStore**: 登录成功后自动调用 `fetchCurrentStatus()` 获取用户状态；状态为 FROZEN/BANNED 时重定向到拦截页
- **usePermissionStore**: 状态变更后刷新权限码，确保功能限制即时生效

### 5.3 状态一致性保障策略

在线用户被管理员变更为 FROZEN/BANNED/MUTED 时，UserStatusStore 中的 `currentStatus` 可能仍为旧值，导致拦截失效。采用分层策略保障状态一致性：

**L1 -- 本地缓存（本期实现）**
- UserStatusStore 缓存当前状态，用于即时 UI 渲染和拦截判断
- 缓存有效期：单次会话内有效，页面刷新时重新获取

**L2 -- 请求级校验（本期实现）**
- 关键互动操作（发表评论、发布动态、发送私信）发起时，后端必须独立校验用户状态，不依赖前端缓存
- 前端拦截仅作为 UX 优化（即时反馈），后端校验为安全兜底
- 所有互动类 API 响应中若返回状态拦截错误码（如 `USER_STATUS_MUTED`），前端立即刷新 UserStatusStore 并弹出拦截提示

**L3 -- 实时推送（后续迭代，非本期范围）**
- 通过 WebSocket 或 SSE 推送状态变更事件，后端状态变更时实时通知前端刷新 UserStatusStore
- 本期不做，但接口设计预留事件推送扩展点

---

## 6. API 对接

### 6.1 API 封装

**路径**: `src/api/content/userStatus.ts`

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  CurrentStatus = '/api/content/user-status/current',
  Users = '/api/content/user-status/users',
  Transitions = '/api/content/user-status/transitions',
  BatchRelease = '/api/content/user-status/users/batch-release',
  AuditLogList = '/api/content/user-status-audit/logs',
  AuditLogDetail = '/api/content/user-status-audit/logs',
  UserAuditLog = '/api/content/user-status-audit/logs/user',
  AuditLogExport = '/api/content/user-status-audit/export',
  SecurityVerify = '/api/content/user-status/verify',
  SendCode = '/api/content/user-status/send-code',
}

// 查询当前用户状态
export const getCurrentStatus = () => defHttp.get({ url: Api.CurrentStatus });

// 查询指定用户状态
export const getUserStatus = (userId: string) =>
  defHttp.get({ url: `${Api.Users}/${userId}` });

// 用户状态列表（管理员）
export const getStatusList = (params: UserStatusQueryReq) =>
  defHttp.get({ url: Api.Users, params });

// 获取可转换状态列表
export const getTransitions = (currentStatus: string) =>
  defHttp.get({ url: `${Api.Transitions}/${currentStatus}` });

// 变更用户状态
export const changeUserStatus = (userId: string, data: UserStatusChangeReq) =>
  defHttp.post({ url: `${Api.Users}/${userId}/change`, data });

// 解禁用户
export const releaseUser = (userId: string, reason: string) =>
  defHttp.post({ url: `${Api.Users}/${userId}/release`, data: { reason } });

// 批量解禁
export const batchReleaseUsers = (userIds: string[], reason: string) =>
  defHttp.post({ url: Api.BatchRelease, data: { userIds, reason } });

// 用户状态历史
export const getStatusHistory = (userId: string) =>
  defHttp.get({ url: `${Api.Users}/${userId}/history` });

// 审计日志列表
export const getAuditLogList = (params: AuditLogQueryReq) =>
  defHttp.get({ url: Api.AuditLogList, params });

// 审计日志详情
export const getAuditLogDetail = (logId: string) =>
  defHttp.get({ url: `${Api.AuditLogDetail}/${logId}` });

// 用户审计日志
export const getUserAuditLogs = (userId: string, params?: { startTime?: string; endTime?: string }) =>
  defHttp.get({ url: `${Api.UserAuditLog}/${userId}`, params });

// 导出审计日志（后端生成文件流，前端 blob 下载）
export const exportAuditLogs = (params: AuditLogExportReq) =>
  defHttp.post({ url: Api.AuditLogExport, data: params, responseType: 'blob' });

// 安全核验
export const verifySecurity = (data: { phone: string; code: string }) =>
  defHttp.post({ url: Api.SecurityVerify, data });

// 发送验证码
export const sendVerifyCode = (phone: string) =>
  defHttp.post({ url: Api.SendCode, data: { phone } });
```

### 6.2 响应拦截处理

- 403 状态码 + 用户状态相关错误码：根据错误码重定向到对应拦截页面
- 并发冲突（乐观锁）：提示"状态已变更，请刷新后重试"
- 非法状态转换：提示"当前状态不允许执行此操作"

---

## 7. 组件选型

基于 `frontend-standards.md` 规范，以下为各场景的组件选型：

| 需求场景 | 推荐组件 | 路径 | 说明 |
|---------|---------|------|------|
| 用户状态列表 | JVxeTable | `src/components/jeecg/JVxeTable/` | 支持行选择、批量操作、分页 |
| 状态变更表单 | Modal + Form | `src/components/Modal/` + `src/components/Form/` | schema 驱动的配置式表单 |
| 解禁确认 | Modal | `src/components/Modal/` | 简单确认弹窗 |
| 状态历史 | Drawer + Table | `src/components/Drawer/` + `src/components/Table/` | 抽屉内嵌表格 |
| 审计日志详情 | Modal + Description | `src/components/Modal/` + `src/components/Description/` | 描述列表展示详情 |
| 状态标签 | Tag (Ant Design Vue) | - | 不同颜色标识不同状态 |
| 时间范围筛选 | RangePicker (Ant Design Vue) | - | 审计日志时间范围筛选 |
| 剩余时间倒计时 | 自定义 StatusCountdown | 新建组件 | 基于 dayjs 计算剩余时间 |
| 导出操作 | Button | `src/components/Button/` | 触发导出下载 |
| 权限控制 | Button (auth) | `src/components/Button/` | 管理员操作按钮权限控制 |

---

## 8. 交互设计

### 8.1 状态变更流程

```
管理员点击"变更状态" 
  → 弹出状态变更弹窗
  → 调用 GET /api/content/user-status/transitions/{currentStatus} 获取可转换状态列表
  → 选择目标状态（下拉框，仅显示 API 返回的允许转换状态）
  → 填写原因（必填）
  → 填写期限（禁言/封禁时显示，支持日期时间选择器 + 永久选项）
  → 点击"确定"
  → 二次确认弹窗："确定将用户 XXX 从 XXX 变更为 XXX？"
  → 确认后提交 API
  → 成功：刷新列表 + 提示"状态变更成功"
  → 失败：显示错误信息
```

### 8.1.1 状态转换矩阵

前端 StatusChangeModal 需要根据当前状态过滤可选目标状态。状态转换矩阵由后端 API 动态返回，前端不硬编码。

**获取可转换状态 API**:
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取可转换状态列表 | GET | `/api/content/user-status/transitions/{currentStatus}` | 返回当前状态允许转换的目标状态列表 |

**参考矩阵（以后端 API 返回为准）**:

| 当前状态 \ 目标状态 | NORMAL | MUTED | RESTRICTED_RECOMMEND | FROZEN | BANNED | DEACTIVATING |
|---------------------|--------|-------|----------------------|--------|--------|--------------|
| NORMAL | - | Y | Y | Y | Y | Y |
| MUTED | Y | - | Y | Y | Y | Y |
| RESTRICTED_RECOMMEND | Y | Y | - | Y | Y | Y |
| FROZEN | Y | N | N | - | Y | N |
| BANNED | Y | N | N | N | - | N |
| DEACTIVATING | Y | N | N | N | N | - |
| GUEST | N | N | N | N | N | N |
| REGISTERED_INCOMPLETE | N | N | N | N | N | N |
| DEACTIVATED | N | N | N | N | N | N |

> **说明**: GUEST、REGISTERED_INCOMPLETE、DEACTIVATED 为终态或初始态，管理员不可直接变更。Y=允许转换，N=不允许，-=自身。

**前端实现要点**:
- StatusChangeModal 打开时调用 `GET /api/content/user-status/transitions/{currentStatus}` 获取可选列表
- 下拉框仅渲染返回的可转换状态
- 提交前前端做合法性二次校验，后端做最终校验（双重校验）
- API 返回空列表时，禁用"变更状态"按钮并提示"当前状态无可执行的转换操作"

### 8.2 解禁流程

```
管理员点击"解禁"
  → 弹出解禁确认弹窗
  → 填写解禁原因（必填）
  → 点击"确定解禁"
  → 二次确认："确定解禁用户 XXX？"
  → 确认后提交 API
  → 成功：刷新列表 + 提示"解禁成功" + 自动发送站内通知给用户
  → 失败：显示错误信息
```

### 8.3 登录拦截流程

> **集成方案**: 登录接口（`/api/auth/login`）改造为直接返回用户状态字段。冻结/封禁用户在登录阶段即拦截，不签发有效 token，直接返回拦截页所需信息（状态、原因、期限）。这避免了"登录成功再拦截"的安全隐患（token 已签发但功能受限）。

**登录接口响应结构变更**:

```typescript
// 正常用户登录响应
interface LoginSuccessResponse {
  code: 200;
  result: {
    token: string;
    // ... 其他字段
  };
}

// 冻结/封禁用户登录响应（不返回 token）
interface LoginBlockedResponse {
  code: 403;
  result: {
    userStatus: 'FROZEN' | 'BANNED';
    statusReason: string;
    statusEndTime: string | null;  // null 表示永久封禁
    phone: string;  // 冻结时用于安全核验，部分脱敏
  };
}
```

**流程**:
```
用户输入账号密码登录
  → 后端校验账号密码正确后，检查用户状态
  → 状态为 NORMAL/其他正常状态：
     → 正常签发 token，返回登录成功
     → 前端调用 fetchCurrentStatus() 更新 UserStatusStore
  → 状态为 FROZEN：
     → 不签发 token
     → 返回 403 + 冻结信息（phone 脱敏）
     → 前端跳转到安全核验页面（/login/verify）
     → 用户输入手机验证码
     → 验证通过 → 后端恢复状态为 NORMAL → 重新登录或自动签发 token
     → 验证失败 → 提示错误
  → 状态为 BANNED：
     → 不签发 token
     → 返回 403 + 封禁信息（原因、期限）
     → 前端跳转到封禁提示页面（/login/blocked）
     → 显示封禁原因、期限、剩余时间
     → 提供"申诉"按钮（跳转 EPIC-08，通过 query 参数传递 userId 和 statusType）
```

**路由访问控制**:
- `/login/blocked` 和 `/login/verify` 不需要登录态（token），通过登录接口返回的数据驱动页面展示
- 登录拦截页的路由守卫检查：若无拦截数据（直接访问 URL），重定向到登录页

### 8.4 互动拦截流程

> **双重拦截机制**: 前端拦截仅作为 UX 优化（即时反馈，避免无效请求），后端必须独立校验用户状态作为安全兜底。前端拦截可被绕过（如直接调用 API），因此不作为安全屏障。

**统一拦截工具函数**:

```typescript
// composables/useStatusGuard.ts
export function useStatusGuard() {
  const statusStore = useUserStatusStore();

  /**
   * 检查用户是否可执行指定互动操作
   * @param action 操作类型：'comment' | 'message' | 'post'
   * @returns { allowed: boolean, reason?: string, endTime?: string }
   */
  const canPerformAction = async (action: 'comment' | 'message' | 'post') => {
    // 先尝试从缓存判断，减少请求
    if (statusStore.currentStatus === UserStatusEnum.MUTED) {
      return {
        allowed: false,
        reason: statusStore.statusDetail?.statusReason,
        endTime: statusStore.statusDetail?.statusEndTime,
      };
    }
    return { allowed: true };
  };

  /**
   * 拦截时弹出统一提示弹窗
   */
  const showBlockModal = (reason?: string, endTime?: string) => {
    Modal.warning({
      title: '操作受限',
      content: `您已被禁言，禁言期限：${endTime || '永久'}，原因：${reason || '违反社区规范'}`,
    });
  };

  return { canPerformAction, showBlockModal };
}
```

**拦截流程**:
```
禁言用户点击"发表评论" / "发送私信" / "发布动态"
  → 调用 useStatusGuard().canPerformAction(action)
  → allowed === false：
     → 拦截操作
     → 调用 showBlockModal(reason, endTime)
     → 用户点击"知道了"关闭弹窗
  → allowed === true：
     → 正常执行操作
     → 后端接口仍独立校验用户状态
     → 若后端返回 USER_STATUS_MUTED 错误码：
        → 前端刷新 UserStatusStore
        → 弹出拦截提示（兜底场景）
```

**所有互动入口统一调用 `useStatusGuard`**:
- 评论组件（CommentInput.vue）
- 私信组件（MessageInput.vue）
- 动态发布组件（PostEditor.vue）
- 不得在各入口散落独立的状态检查逻辑

### 8.5 状态颜色与图标设计

| 状态 | 颜色 | 图标 | 说明 |
|------|------|------|------|
| GUEST | 灰色 | UserOutlined | 游客 |
| REGISTERED_INCOMPLETE | 蓝色 | EditOutlined | 未完善资料 |
| NORMAL | 绿色 | CheckCircleOutlined | 正常 |
| MUTED | 橙色 | AudioMutedOutlined | 禁言 |
| RESTRICTED_RECOMMEND | 黄色 | EyeInvisibleOutlined | 限制推荐 |
| FROZEN | 红色 | LockOutlined | 冻结 |
| BANNED | 深红色 | StopOutlined | 封禁 |
| DEACTIVATING | 紫色 | ClockCircleOutlined | 注销中 |
| DEACTIVATED | 灰色 | DeleteOutlined | 已注销 |

---

## 9. 响应式设计

### 9.1 断点策略

| 断点 | 宽度 | 布局调整 |
|------|------|---------|
| 移动端 | <768px | 单列布局，表格改为卡片列表，筛选条件折叠 |
| 平板 | 768px-1200px | 双列布局，表格精简列数 |
| 桌面端 | >1200px | 完整布局，表格展示所有列 |

### 9.2 移动端适配

- 用户状态管理页：表格改为卡片列表，操作按钮改为下拉菜单
- 审计日志页：表格精简为关键字段，详情改为全屏页面
- 账号状态页：卡片全宽展示，历史记录改为时间轴
- 登录拦截页：居中卡片布局，按钮全宽
- 安全核验页：表单全宽，按钮全宽

---

## 10. 性能要求

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 状态查询响应时间 | <100ms | 查询当前用户状态 |
| 权限检查延迟 | <50ms | 互动拦截时的状态检查 |
| 审计日志列表加载 | <1s | 分页查询，每页 20 条 |
| 导出审计日志 | <10s | 万级数据量导出 |
| 首屏加载时间 | <3s | 3G 网络环境 |
| 状态变更操作 | <500ms | 包含二次确认的完整流程 |

### 10.1 优化策略

- 用户状态信息缓存到 UserStatusStore，避免重复请求
- 审计日志列表使用虚拟滚动（JVxeTable 内置支持）
- 导出操作由后端生成 Excel/CSV 文件流，前端通过 blob 下载处理，展示下载进度提示（如 Loading 状态）；前端不使用 Web Worker，因文件生成职责在后端
- 状态标签组件使用 memo 避免不必要的重渲染
- 登录拦截页使用轻量级组件，最小化加载资源

---

## 11. 测试要点

### 11.1 功能测试

| 测试场景 | 测试要点 | 优先级 |
|---------|---------|--------|
| 状态查询 | 查询当前用户状态、管理员查询指定用户状态 | P0 |
| 状态变更 | 合法转换成功、非法转换拒绝、必填字段校验 | P0 |
| 解禁操作 | 单个解禁、批量解禁、解禁后功能恢复 | P0 |
| 审计日志 | 列表查询、筛选、详情查看、导出 | P0 |
| 登录拦截 | 冻结用户拦截、封禁用户拦截、安全核验通过 | P0 |
| 互动拦截 | 禁言用户评论拦截、私信拦截、动态拦截 | P0 |
| 用户端状态展示 | 状态标签、处罚详情、剩余时间倒计时 | P1 |
| 状态历史 | 用户查看自身历史、管理员查看指定用户历史 | P1 |
| 并发冲突 | 乐观锁机制、冲突提示 | P1 |
| 响应式布局 | 移动端/平板/桌面端布局适配 | P2 |

### 11.2 边界条件测试

- 状态转换边界：所有 9 种状态之间的合法/非法转换组合
- 时间边界：禁言/封禁刚好到期时的自动解禁
- 永久封禁：statusEndTime 为空或极远未来时间
- 空数据：无状态历史记录、无审计日志时的空状态展示
- 大数据量：审计日志万级数据的分页和导出性能
- 并发操作：两个管理员同时变更同一用户状态

### 11.3 兼容性测试

- 浏览器：Chrome、Firefox、Safari、Edge 最新两个大版本
- 移动端：iOS Safari、Android Chrome
- 屏幕分辨率：1920x1080、1366x768、375x667（iPhone SE）

---

## 12. 里程碑与交付计划

| 里程碑 | 周期 | 交付内容 |
|--------|------|---------|
| M1 | Week 1-2 | 用户状态管理页 + 状态变更功能 + 用户端账号状态页 |
| M2 | Week 3-4 | 审计日志页 + 导出功能 + 登录拦截页 + 安全核验页 |
| M3 | Week 5-6 | 互动拦截提示 + 状态历史 + 批量操作 + 响应式适配 + 测试验收 |

---

## 13. 依赖关系

- **后端 API**: 依赖 EPIC-09 后端实现（UserStatusController、UserStatusAuditLogController）
- **EPIC-01**: 用户注册流程中需设置默认状态为 NORMAL
- **EPIC-08**: 申诉系统提供"申诉"入口跳转
- **现有组件**: 复用 JVxeTable、Form、Modal、Drawer、Description、Button 等现有组件
- **路由配置**: 需在后台管理菜单中添加用户状态管理和审计日志菜单项
