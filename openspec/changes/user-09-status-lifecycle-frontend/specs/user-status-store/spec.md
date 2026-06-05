## ADDED Requirements

### Requirement: UserStatusStore 缓存当前用户状态

UserStatusStore (Pinia) MUST 缓存当前用户状态信息，用于即时 UI 渲染和拦截判断。缓存有效期为单次会话内有效，页面刷新时重新获取。

#### Scenario: 登录后获取状态
- **WHEN** 用户登录成功
- **THEN** 自动调用 fetchCurrentStatus() 获取用户状态并缓存到 Store

#### Scenario: 页面刷新重新获取
- **WHEN** 用户刷新页面
- **THEN** 重新调用 fetchCurrentStatus() 更新 Store 缓存

#### Scenario: fetchCurrentStatus 失败
- **WHEN** fetchCurrentStatus() API 请求失败
- **THEN** Store 保持上一次缓存状态，不阻断页面渲染

---

### Requirement: UserStatusStore 提供状态管理 actions

UserStatusStore MUST 提供以下 actions：fetchCurrentStatus、fetchUserStatus、fetchStatusHistory、fetchTransitions、changeStatus、releaseUser、batchRelease、verifySecurity、refreshStatus。

#### Scenario: fetchCurrentStatus
- **WHEN** 调用 fetchCurrentStatus()
- **THEN** 请求 GET /api/content/user-status/current?userId={userId}，更新 currentStatus 和 statusDetail
- **NOTE**: 后端 API 需要 userId 参数，前端从 useUserStore 获取当前登录用户 ID

#### Scenario: changeStatus
- **WHEN** 调用 changeStatus(userId, payload)
- **THEN** 请求 POST /api/content/user-status/users/{userId}/change，成功后刷新用户状态

#### Scenario: releaseUser
- **WHEN** 调用 releaseUser(userId, reason)
- **THEN** 请求 POST /api/content/user-status/users/{userId}/release，成功后刷新用户状态

---

### Requirement: 状态变更后刷新权限

状态变更后 MUST 刷新权限码，确保功能限制即时生效。

#### Scenario: 状态变更后权限刷新
- **WHEN** 用户状态变更（如从正常变为禁言）
- **THEN** 调用 usePermissionStore 刷新权限码

---

### Requirement: 403 状态码自动重定向

HTTP 响应拦截器收到 403 + 用户状态相关错误码时，MUST 自动刷新 UserStatusStore 并重定向到对应拦截页。

#### Scenario: 403 冻结错误码
- **WHEN** API 返回 403 + USER_STATUS_FROZEN 错误码
- **THEN** 刷新 UserStatusStore，重定向到安全核验页

#### Scenario: 403 封禁错误码
- **WHEN** API 返回 403 + USER_STATUS_BANNED 错误码
- **THEN** 刷新 UserStatusStore，重定向到封禁提示页
