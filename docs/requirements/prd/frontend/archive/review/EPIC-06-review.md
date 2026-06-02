# EPIC-06 隐私设置与通知管理 — 前端 PRD 审核报告

> **审核人**: Amelia (Senior Software Engineer)
> **审核日期**: 2026-06-02
> **审核视角**: 工程实现 — 可测试性、复杂度、API 契约

## 总体评价

PRD 整体结构清晰，四个页面的功能拆分合理，交互规则描述详尽，响应式断点和测试要点覆盖全面。但从工程实现角度看，存在若干 API 契约不一致、字段命名偏差、以及边界场景未定义的问题，需要在开发前修正，否则会导致前后端联调时产生大量返工。

## 优点

- **组件选型明确**：直接引用了 `frontend-standards.md` 中的组件清单和路径，开发时无需二次确认，减少歧义
- **状态管理策略务实**：明确采用页面级 `ref`/`reactive` 而非全局 Pinia Store，符合设置类页面"加载-编辑-提交-丢弃"的单向数据流，降低复杂度
- **异常测试覆盖充分**：E-01 到 E-10 覆盖了 null 值降级、网络异常、越权操作等关键边界，可直接转化为单元测试用例
- **免打扰规则设计灵活**：多时段 + 跨午夜 + 摘要模式 + 临时关闭的组合设计完整，`DndRuleItem` 数据结构已由后端 VO 明确定义，前端有清晰的数据契约
- **响应式设计三级断点**：PC / 平板 / 移动端均有具体布局方案，移动端从 Table 转卡片堆叠的策略合理

## 问题与建议

1. **🔴高 — API 路径前缀不一致：第三方授权接口路径与项目规范冲突**
   PRD 第5节定义的第三方授权接口使用 `/api/v1/auth/third-party` 前缀，但项目中所有内容社区接口统一使用 `/content/` 前缀（如 `/content/user/settings/notification`）。后端 Controller 的 `@RequestMapping` 也是 `/content/user/settings`。第三方授权接口若使用 `/api/v1/` 前缀，需要额外的网关路由配置或独立 Controller。
   **建议**：与后端确认第三方授权的实际路径，统一为 `/content/user/auth/third-party` 或明确说明该接口由独立的认证服务提供。

2. **🔴高 — 隐私设置可见性枚举值不一致：PRD 定义三级，后端支持四级**
   PRD 第3.2.1节定义动态可见性选项为 `PUBLIC / FOLLOWERS_ONLY / PRIVATE` 三个值，但后端 `ContentUserPrivacyUpdateReq` 的 `@Pattern` 校验正则为 `^(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)$`，支持四级。PRD 缺少 `MUTUAL_ONLY`（仅互关可见）的说明。
   **建议**：PRD 需补充 `MUTUAL_ONLY` 选项的显示文案和交互说明，或明确告知后端移除此值。

3. **🔴高 — 通知设置缺少 `subscriptionNoticeEnabled` 字段映射**
   后端实体 `ContentUserNotificationSetting` 包含 `subscriptionNoticeEnabled`（订阅更新通知开关）和 `subscriptionDefaultChannels`、`subscriptionDefaultFrequency` 字段，但 PRD 第3.1.1节仅列出六类通知（点赞/评论/关注/收藏/@我/私信），未包含"订阅更新"。`ContentNotificationChannelConfigVO` 也未提供 `subscriptionChannels` 字段。
   **建议**：明确"订阅更新"通知是否在本期范围内。若在，前端需增加第七行开关；若不在，后端需确认这些字段的处理方式（默认值/忽略）。

4. **🟡中 — 通知渠道字段命名与后端 VO 不匹配**
   PRD 第3.1.1节描述渠道为"App内 / 推送 / 短信 / 邮件"四个 Checkbox，但未定义渠道的枚举值。后端 `ContentNotificationChannelConfigVO` 使用 `List<String>` 存储各类型渠道，如 `likeChannels`。PRD 需明确渠道值的字符串标识（如 `IN_APP / PUSH / SMS / EMAIL`），否则前端提交的值可能与后端解析不一致。
   **建议**：在 API 对接章节补充渠道枚举值定义表。

5. **🟡中 — 通知更新接口合并了开关和渠道，但免打扰单独接口，提交策略需明确**
   PRD 第3.1.1节的"保存行为"调用 `POST /content/user/settings/notification/update`，后端 `ContentUserNotificationUpdateReq` 确实同时包含开关字段和 `channelConfig`。但免打扰规则通过独立接口 `/notification/dnd/update` 更新。PRD 中"保存"按钮只有一个，需明确是单次提交拆分两次请求，还是需要两个独立的保存按钮。
   **建议**：建议采用单次保存拆分两次并发请求的策略（Promise.all），在交互设计章节明确说明。

6. **🟡中 — 隐私设置页缺少 GET 接口的响应结构定义**
   PRD 定义了 `GET /content/user/settings/privacy` 获取隐私设置，但未给出响应体结构。后端 `ContentUserPrivacySetting` 实体包含 17 个字段（生日/性别/地区/职业等），远超 PRD 中隐私设置页展示的 5 项（浏览记录/点赞动态/收藏夹/在线状态/搜索引擎索引）。
   **建议**：前端只需提取页面展示的 5 个字段，忽略其余字段。需在 PRD 中明确映射关系：`browseHistoryVisibility` -> 浏览记录可见性，`likeActivityVisibility` -> 点赞动态可见性，`favoriteVisibility` -> 收藏夹可见性，`onlineStatusVisibility` -> 在线状态，`allowSearchEngineIndex` -> 搜索引擎索引。

7. **🟡中 — 隐私设置接口缺少 userId 传递方式说明**
   后端 `updatePrivacy` 和 `getNotification` 等接口均通过 `@RequestParam("userId")` 接收 userId，即 URL 查询参数。但 PRD 的 API 封装示例代码中 `defHttp.get/post` 未体现 userId 参数的传递。
   **建议**：API 封装示例需补充 userId 参数，或说明 userId 由全局请求拦截器自动注入。

8. **🟡中 — 安全设置返回结构与 PRD 描述不一致**
   PRD 第3.4.1节描述返回格式为 `{ deviceManagement: { enabled, lastActive }, passwordChange: { lastModified }, twoFactor: { enabled, method }, loginAlert: { enabled } }`（嵌套对象），但后端 `ContentUserSecuritySettingVO` 是扁平结构：`deviceManagementEnabled`、`passwordChangeEnabled`、`twoFactorEnabled`、`loginAlertEnabled`（均为 Boolean），没有 `lastActive`、`lastModified`、`method` 等详情字段。
   **建议**：以实际后端 VO 为准更新 PRD，或与后端协商补充缺失字段。

9. **🟡中 — 免打扰临时关闭的前端状态管理未定义**
   PRD 描述"暂时关闭免打扰（1小时）"按钮需显示倒计时，后端 `DndRuleVO` 提供 `temporaryDisableUntil`（Unix 毫秒时间戳）。但前端在页面刷新或重新进入时，需从 `temporaryDisableUntil` 计算剩余时间并恢复倒计时 UI。PRD 未描述这一恢复逻辑。
   **建议**：在交互设计章节补充：页面加载时检查 `temporaryDisableUntil`，若大于当前时间则自动进入倒计时状态。

10. **🟡中 — 第三方授权撤销接口路径中 authId 类型未定义**
    PRD 使用 `DELETE /api/v1/auth/third-party/{authId}`，但未说明 `authId` 是 UUID、自增 ID 还是其他格式。前端路由参数和 API 封装中的类型标注（`string`）可能不够精确。
    **建议**：与后端确认 authId 的格式和长度约束。

11. **🟢低 — 搜索引擎索引控制的"认证用户推荐开启"逻辑需明确**
    PRD 第3.2.3节提到"认证用户/创作者默认建议开启（显示推荐标签）"，但未说明前端如何判断用户是否为认证用户。需依赖用户信息接口返回的认证状态字段。
    **建议**：明确依赖的用户信息字段（如 `userType` 或 `isVerified`），避免前端自行猜测。

12. **🟢低 — 移动端通知渠道 Checkbox 纵向排列可能占用过多空间**
    PRD R-01 描述移动端渠道选择纵向排列，四个渠道（App内/推送/短信/邮件）纵向排列将占用较大高度。考虑使用 `a-checkbox-group` 配合 `flex-wrap` 横向两列布局，或折叠到"高级设置"区域。
    **建议**：移动端渠道选择改为 2x2 网格或可折叠区域。

13. **🟢低 — 账户安全页的"状态描述"数据缺失**
    PRD 第3.4.1节卡片需展示"上次修改时间"（密码修改）和"验证方式"（两步验证），但后端 `ContentUserSecuritySettingVO` 仅返回 Boolean 值，不含这些详情。
    **建议**：前端可简化为仅展示"已启用/未启用"状态，或与后端协商补充字段。

## 实现建议

1. **API 层封装**：建议在 `src/api/content/` 下新建 `userSettings.ts`，集中管理所有设置相关接口。使用 TypeScript interface 定义请求/响应类型，与后端 VO/Req 一一对应。

2. **表单组件复用**：通知设置和隐私设置均为"配置式表单"，建议封装 `useSettingsForm` hook，统一处理加载 -> 编辑 -> 校验 -> 提交 -> 成功/失败提示的生命周期。

3. **免打扰规则组件**：建议将免打扰规则列表抽取为独立组件 `DndRuleList.vue`，内部管理规则的增删改和时间校验逻辑，通过 `v-model` 与父组件通信。

4. **路由懒加载**：四个页面均为低频访问的设置页，务必使用 `() => import(...)` 懒加载，避免打入主 bundle。

5. **TypeScript 类型定义**：建议新建 `types/settings.ts`，定义所有枚举类型（`VisibilityLevel`、`OnlineStatus`、`NotificationType`、`NotificationChannel`）和接口类型，与后端保持强类型契约。

6. **乐观更新策略**：登录提醒开关（账户安全页）属于低风险操作，可采用乐观更新（先切 UI 再发请求），失败时回滚。其余设置页建议采用保守策略（请求成功后再更新 UI）。

## 总结

PRD 功能设计完整、交互规则细致，具备直接进入开发阶段的基础。但需优先解决 **3 个高优先级问题**（API 路径前缀一致性、可见性枚举值对齐、订阅通知字段处理），否则前后端联调将产生阻塞。建议在开发启动前召开一次前后端对齐会议，逐项确认 API 契约细节。
