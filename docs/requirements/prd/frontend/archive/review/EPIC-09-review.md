# EPIC-09 用户状态与生命周期治理 -- 前端 PRD 审核报告

> **审核人**: 🏗️ Winston (System Architect)
> **审核日期**: 2026-06-02
> **审核视角**: 系统架构 -- 技术可行性、可扩展性、组件设计

---

## 总体评价

PRD 整体质量较高，覆盖面完整，从管理员后台到用户端全链路均有涉及。状态机模型（9 种状态）定义清晰，组件拆分合理，API 设计符合 RESTful 规范。但在状态一致性保障、Store 设计的可扩展性、以及跨 EPIC 依赖的耦合风险上存在需要关注的架构问题。

---

## 优点

1. **状态枚举定义清晰** -- `UserStatusEnum` 以字符串枚举定义 9 种状态，前后端对齐成本低，可读性好。

2. **组件拆分粒度合理** -- 状态标签（StatusTag）、状态变更弹窗（StatusChangeModal）、解禁弹窗（StatusReleaseModal）、历史抽屉（StatusHistoryDrawer）等组件职责单一，符合 SRP 原则，可独立测试和复用。

3. **并发控制已有考虑** -- US-02 验收标准第 6 条明确提出乐观锁机制和冲突提示，6.2 节响应拦截处理也有对应说明，说明作者对并发场景有意识。

4. **交互流程图完备** -- 8.1-8.4 节的状态变更、解禁、登录拦截、互动拦截四个核心流程均有明确的步骤描述，开发人员理解歧义小。

5. **性能指标具体可测** -- 状态查询 <100ms、权限检查 <50ms、导出 <10s 等指标明确，有验收基准。

6. **响应式设计提前规划** -- 9.1-9.2 节明确了断点策略和移动端适配方案，避免后期返工。

---

## 问题与建议

### 🔴 1. UserStatusStore 与 useUserStore 状态同步存在竞态风险

**位置**: 5.2 节 "与现有 Store 的集成"

PRD 描述登录成功后自动调用 `fetchCurrentStatus()`，状态为 FROZEN/BANNED 时重定向到拦截页。但未说明以下场景的处理：

- 用户已在线时被管理员变更为 FROZEN/BANNED，此时 UserStatusStore 中的 `currentStatus` 仍为 NORMAL，直到下次 `fetchCurrentStatus()` 才会更新。
- 互动拦截检查依赖 UserStatusStore 中的 `currentStatus`，如果状态缓存未及时刷新，禁言用户仍可发表内容。

**建议**: 
- 引入 WebSocket 或 SSE 推送机制，后端状态变更时实时通知前端刷新 UserStatusStore。
- 若实时推送不在本期范围，至少在关键操作（发表评论、发布动态）前增加一次后端状态校验请求，而非仅依赖本地 Store 缓存。
- 在 PRD 中显式声明该限制及缓解措施。

### 🔴 2. 缺少状态转换矩阵的前端校验层

**位置**: 8.1 节 "状态变更流程"

PRD 提到"下拉框仅显示当前状态允许转换的状态"，但未定义完整的状态转换矩阵（哪些状态可以转换到哪些状态）。前端需要这份矩阵来：
- 驱动 StatusChangeModal 中的下拉选项过滤
- 在提交前做前端侧的合法性校验（双重校验，减少无效请求）

**建议**: 
- 新增一节"状态转换矩阵"，以表格形式列出所有合法的状态转换路径（如 NORMAL -> MUTED 合法，GUEST -> BANNED 非法）。
- 矩阵数据建议由后端 API 返回（如 `/api/content/user-status/transitions/{currentStatus}`），前端动态渲染可选项，避免前后端硬编码不一致。

### 🟡 3. StatusCountdown 组件缺少定时刷新策略

**位置**: 4.2 节 "StatusCountdown.vue"

该组件负责展示禁言/封禁的剩余时间倒计时。PRD 未说明：
- 倒计时是纯前端计算（基于 endTime 减去当前时间）还是需要轮询后端？
- 用户长时间停留在页面（如浏览器 Tab 后台挂起），倒计时精度如何保证？
- 到期自动解禁后，前端如何感知并更新状态？

**建议**:
- 明确采用纯前端 dayjs 计算 + `setInterval` 定时器方案，并在组件销毁时清理定时器。
- 倒计时归零时触发一次 `fetchCurrentStatus()` 验证后端是否已自动解禁。
- 考虑 `document.visibilitychange` 事件，页面重新可见时刷新倒计时基准。

### 🟡 4. API 路径设计存在 RESTful 风格不一致

**位置**: 4.3 节 API 接口表

部分接口路径设计不一致：
- `GET /api/content/user-status/{userId}` 和 `GET /api/content/user-status/list` 路由存在歧义 -- `/list` 可能与 `{userId}` 冲突（如果 userId 恰好为 "list"）。
- `POST /api/content/user-status/{userId}/change` 和 `POST /api/content/user-status/{userId}/release` 使用动词而非名词，不符合 RESTful 惯例。

**建议**:
- 将 list 接口改为 `GET /api/content/user-status?userId=xxx&status=xxx`，与查询单个用户的接口合并，通过参数区分。
- 或使用更明确的路径前缀区分：`GET /api/content/user-status/users` (列表) vs `GET /api/content/user-status/users/{userId}` (单个)。
- change/release 可接受为 RPC 风格，但在文档中注明是设计选择而非疏忽。

### 🟡 5. 审计日志导出使用 Web Worker 方案需细化

**位置**: 10.1 节 "优化策略"

PRD 提到"导出操作使用 Web Worker 避免阻塞主线程"，但 API 设计中导出接口是 `POST .../export` 返回 blob（6.1 节），这意味着：
- 如果后端直接返回 Excel/CSV 文件流，前端无需 Web Worker -- 只需处理下载即可。
- Web Worker 只在前端生成导出文件时才有意义（如从已有数据生成 CSV）。

**建议**:
- 明确导出是后端生成文件还是前端生成。如果是后端生成（更常见），删除 Web Worker 相关描述，改为描述 blob 下载和进度提示。
- 如果确实是前端生成，说明数据来源和 Worker 的具体职责。

### 🟡 6. 互动拦截的前端检查策略不够健壮

**位置**: 8.4 节 "互动拦截流程"

PRD 描述拦截逻辑为"前端检查用户状态（通过 UserStatusStore）"。这存在两个问题：
- 仅靠前端检查可被绕过（用户可直接调用 API）。
- 多个拦截点（评论、私信、动态）需要在每个入口重复检查逻辑。

**建议**:
- 前端拦截仅作为 UX 优化（即时反馈），后端必须独立校验（这是安全兜底）。
- 抽取统一的拦截工具函数（如 `checkUserActionPermission(actionType): boolean`），所有互动入口调用同一函数，避免检查逻辑散落各处。
- 在 PRD 中明确"前端拦截 + 后端校验"双重机制。

### 🟡 7. 登录拦截流程与现有登录逻辑的集成点未明确

**位置**: 8.3 节 "登录拦截流程"

PRD 描述登录后根据状态跳转到拦截页，但未说明：
- 现有登录接口（`/api/auth/login`）是否需要改造以返回用户状态？还是登录成功后额外调用 `fetchCurrentStatus()`？
- 冻结/封禁用户是否在登录阶段就拦截（不发 token），还是登录成功后再拦截（发了 token 但跳转拦截页）？
- `/login/blocked` 路由是否需要登录态才能访问？

**建议**:
- 明确推荐方案：登录接口直接返回用户状态字段，冻结/封禁用户不签发有效 token，直接返回拦截页所需信息（状态、原因、期限）。
- 这避免了"登录成功再拦截"的安全隐患（token 已签发但功能受限）。
- 与后端对齐登录接口的响应结构变更。

### 🟢 8. Pinia Store 粒度可进一步优化

**位置**: 5.1 节 "UserStatusStore"

当前 UserStatusStore 承载了三类职责：当前用户状态管理、管理员操作（变更/解禁/批量解禁）、安全核验。随着功能演进，这个 Store 可能膨胀。

**建议**:
- 考虑拆分为：
  - `useUserStatusStore` -- 仅管理当前用户的 `currentStatus`、`statusDetail`、`statusHistory`
  - `useAdminUserStatusStore` -- 管理员侧的列表查询、变更、解禁、批量操作
- 当前规模下不拆也可以，但建议在代码注释中标注职责边界，为后续拆分预留认知基础。

### 🟢 9. 安全核验页缺少防刷机制说明

**位置**: 3.5 节 "安全核验页" 和 US-09

PRD 提到 60 秒倒计时，但未说明：
- 验证码输入错误次数限制（如 5 次锁定 15 分钟）
- 同一手机号发送验证码的频率限制

**建议**:
- 在验收标准中增加"验证码错误超过 N 次后锁定 X 分钟"的防刷规则。
- 发送验证码接口的频率限制由后端实现，但前端应处理 429 响应并提示用户等待时间。

### 🟢 10. 路由权限配置未提及

**位置**: 3.1-3.2 节路由定义

管理后台路由 `/content/user-status/manage` 和 `/content/user-status/audit-log` 的菜单权限配置未说明。在 JeecgBoot 体系中，菜单通常通过数据库配置 + 权限码控制。

**建议**:
- 补充说明新增菜单的权限码定义（如 `content:user-status:manage`、`content:user-status:audit`）。
- 用户端路由 `/user/account-status` 是否需要登录态守卫，需明确。

---

## 架构建议

### 1. 状态一致性保障方案

建议采用分层策略：
- **L1 -- 本地缓存**: UserStatusStore 缓存当前状态，用于即时 UI 渲染和拦截判断。
- **L2 -- 请求校验**: 关键操作（发帖、评论、私信）前附加状态校验请求或在现有请求中由后端校验。
- **L3 -- 实时推送**（可选，非本期）: WebSocket 推送状态变更事件，实时刷新 Store。

### 2. 状态转换矩阵外部化

将状态转换矩阵从前后端硬编码中解耦：
- 后端维护权威的状态转换规则。
- 前端通过 API 获取当前状态可转换的目标状态列表。
- 好处：规则变更无需前端发版。

### 3. 拦截层统一抽象

建议封装统一的拦截拦截层：
```typescript
// composables/useStatusGuard.ts
export function useStatusGuard() {
  const statusStore = useUserStatusStore();
  
  const canPerformAction = async (action: 'comment' | 'message' | 'post') => {
    await statusStore.refreshStatus(); // 刷新状态
    // 返回 { allowed: boolean, reason?: string, endTime?: string }
  };
  
  return { canPerformAction };
}
```
所有互动入口调用此 composable，统一封装拦截逻辑和提示弹窗。

### 4. 与 EPIC-08 申诉系统的接口契约

PRD 多处提到"跳转到 EPIC-08 申诉系统"，但未定义跳转时需要传递的参数（如 userId、statusType、punishmentId）。建议：
- 定义明确的路由参数或 query 参数结构。
- 与 EPIC-08 团队对齐接口契约，避免集成阶段返工。

---

## 总结

EPIC-09 前端 PRD 在功能覆盖、组件设计、交互流程方面完成度高，可作为开发基线。主要风险集中在：

1. **状态一致性**（🔴高优）-- 在线用户被状态变更后，前端 Store 缓存可能过期，导致拦截失效。需明确缓解策略。
2. **状态转换矩阵缺失**（🔴高优）-- 前端无法独立完成合法转换校验，需补充矩阵定义或 API。
3. **登录拦截与现有登录流程的集成方案**（🟡中优）-- 需与后端对齐是在登录阶段还是登录后拦截。

建议在开发启动前完成上述 🔴 问题的方案确认，🟡 级问题可在各里程碑内解决。整体架构设计合理，组件复用度高，预计 6 周三期内可交付。
