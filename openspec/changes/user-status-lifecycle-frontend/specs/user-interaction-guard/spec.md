## ADDED Requirements

### Requirement: 统一互动拦截 composable

所有互动入口（评论/私信/动态）MUST 统一调用 useStatusGuard composable 进行状态检查，不得散落独立的状态检查逻辑。

#### Scenario: 禁言用户发表评论
- **WHEN** 禁言用户点击"发表评论"按钮
- **THEN** 调用 useStatusGuard().canPerformAction('comment')，返回 allowed=false，弹出提示"您已被禁言，禁言期限：{endTime}，原因：{reason}"

#### Scenario: 禁言用户发送私信
- **WHEN** 禁言用户点击"发送私信"按钮
- **THEN** 调用 useStatusGuard().canPerformAction('message')，返回 allowed=false，弹出禁言提示

#### Scenario: 禁言用户发布动态
- **WHEN** 禁言用户点击"发布动态"按钮
- **THEN** 调用 useStatusGuard().canPerformAction('post')，返回 allowed=false，弹出禁言提示

#### Scenario: 正常用户互动
- **WHEN** 正常状态用户执行互动操作
- **THEN** canPerformAction 返回 allowed=true，正常执行操作

---

### Requirement: 互动 API 后端拦截兜底

前端拦截仅作为 UX 优化，后端 MUST 独立校验用户状态。互动类 API 返回状态拦截错误码时，前端 MUST 自动刷新 UserStatusStore 并弹出拦截提示。

#### Scenario: 前端拦截被绕过后端兜底
- **WHEN** 前端拦截被绕过（如直接调用 API），后端返回 USER_STATUS_MUTED 错误码
- **THEN** 前端刷新 UserStatusStore，弹出拦截提示

---

### Requirement: 禁言用户被动互动不受影响

禁言用户 MUST 仍可浏览内容、点赞、收藏（被动互动不受影响）。

#### Scenario: 禁言用户浏览和点赞
- **WHEN** 禁言用户浏览内容、点赞、收藏
- **THEN** 操作正常执行，不被拦截
