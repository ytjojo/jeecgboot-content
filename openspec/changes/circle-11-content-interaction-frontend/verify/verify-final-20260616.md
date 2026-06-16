# Verification Report: `circle-11-content-interaction-frontend`

**验证日期**: 2026-06-16 | **方式**: 逐文件读取前后端代码，对比 spec/design/tasks/backend-issues 四层工件

---

## 摘要

| Dimension | Status |
|-----------|--------|
| Completeness | 42/42 tasks 标记完成，但 4 个 CRITICAL 问题 |
| Correctness | **存在架构级 Bug：Circle ID 与 Channel ID 混用** |
| Coherence | 实现偏离 spec，错误使用 Channel governance 替代 Circle 自有 API |

**核心结论**: 后端证实 Circle 和 Channel 是两个独立业务实体（无外键关联、不同表、独立 ID 序列）。前端正确地实现了置顶/精华/公告功能逻辑，但**错误地**使用了 Channel governance API（`/api/v1/content/channel/governance`），将 `circle.id` 当作 `channelId` 传给后端。应使用 Circle 自有 API。80/80 测试通过。

---

## 后端实体证据：Circle 与 Channel 完全独立

### Circle — 表 `content_circle`

```java
// jeecg-boot/.../circle/entity/Circle.java
@TableName("content_circle")
public class Circle extends JeecgEntity {
    private String name;
    private String description;
    // ... 无 channelId 字段
}
```

### Channel — 表 `content_channel`

```java
// jeecg-boot/.../channel/entity/Channel.java
@TableName("content_channel")
public class Channel extends JeecgEntity {
    private String name;
    private ChannelType channelType;
    // ... 无 circleId 字段
}
```

### CircleContent — 表 `circle_content`

```java
// jeecg-boot/.../circle/entity/CircleContent.java
@TableName("circle_content")
public class CircleContent extends JeecgEntity {
    private String circleId;
    private Boolean isPinned;       // ← 置顶字段已存在
    private Date pinnedAt;          // ← 置顶时间已存在
    private Boolean isFeatured;     // ← 精华字段已存在
    private Date featuredAt;        // ← 精华时间已存在
    // ... 无 channelId 字段
}
```

**关键事实**:
1. Circle 和 Channel 是两张独立的数据库表，各自独立主键序列
2. 两个实体之间**没有任何外键字段**（Circle 无 `channelId`，Channel 无 `circleId`）
3. `CircleContent` 直接关联 `circleId`，已有 `isPinned`/`isFeatured` 字段
4. Circle 和 Channel 在不同的 Java package 下（`circle.entity` vs `channel.entity`）
5. Circle 有独立的 API 前缀 `/api/v1/content/circle/`，Channel 有独立前缀 `/api/v1/content/channel/`

---

## CRITICAL 问题（必须修复）

### C1: 🔴 架构级 Bug — `Detail.vue` 将 Circle ID 错当 Channel ID 使用

- **Bug 位置**: `src/views/circle/Detail.vue:205`
  ```typescript
  await executeGovernance({ contentId, channelId: circle.value!.id, action: govAction });
  ```
  `circle.value!.id` 来自 `content_circle` 表的主键，`channelId` 参数对应 `content_channel` 表的主键。**两个表独立自增，ID 不可能对应上**。

- **影响链路**:
  ```
  CircleContentCard.vue (line 16-20)
    → 引用 GovernanceActionMenu.vue（channel 组件）
    → emit('governanceAction', action, contentId)
  Detail.vue:193 handleGovernanceAction()
    → executeGovernance({ channelId: circle.value!.id }) // ❌ 错误
    → POST /api/v1/content/channel/governance  // ❌ 应调用 Circle API
  ```

- **后端证据**:
  - `Circle.java`: 表 `content_circle`，无 `channelId` 字段
  - `Channel.java`: 表 `content_channel`，无 `circleId` 字段
  - `CircleContent.java`: 表 `circle_content`，已有 `isPinned`/`isFeatured` 字段

- **正确做法**: 使用 Circle 自有 API（即原始 spec 定义的接口）:
  ```
  PUT /api/v1/content/circle-content/{contentId}/pin?circleId={circleId}
  PUT /api/v1/content/circle-content/{contentId}/featured?circleId={circleId}
  ```
  `CircleContent` 实体本身就有 `isPinned`/`pinnedAt`/`isFeatured`/`featuredAt` 字段，不需要经过 Channel。

- **修复方向**:
  1. 创建 `src/api/content/circle/content.ts` — 封装 Circle content 置顶/精华 API
  2. 创建 `CircleContentActionMenu.vue`（circle-specific 组件）替代 `GovernanceActionMenu.vue`（channel 组件）
  3. 修改 `Detail.vue` 调用 Circle 自己的 API
  4. 或者：如果后端确认 circle ↔ channel 存在 1:1 映射通道（如 `Circle` 加 `channelId` 字段），需后端先补齐

### C2: CircleAnnouncementBar 组件未被任何页面集成

- **文件**: `src/views/circle/components/CircleAnnouncementBar.vue` (103行)
- **问题**: 组件已实现（展开/收起、expireAt 过期检查、静默失败），但 `Detail.vue` 和其他 circle 页面均未导入使用。通过 `grep -rn "CircleAnnouncementBar" src/views/circle/` 验证，**仅在测试文件中被引用**。
- **影响**: 公告栏无法在圈子内容列表页顶部展示。spec Requirement "圈子公告顶部展示" 未满足。
- **修复**: 在 `circle/Detail.vue` 的 feed Tab 顶部添加 `<CircleAnnouncementBar :circle-id="circle.id" />`

### C3: GovernanceActionMenu 缺少角色权限控制和"举报"菜单项

- **文件**: `src/views/channel/components/GovernanceActionMenu.vue:6-12`
- **问题**: 所有菜单项（置顶/取消置顶/精华/取消精华/移出/编辑协助/删除）对所有有权限用户可见，不区分 CREATOR/MODERATOR/MEMBER。且缺少 spec 要求的"举报"菜单选项。
- **spec 要求**: "普通成员仅显示举报选项" (content-pin-featured Requirement)
- **修复方向** (与 C1 一起考虑):
  1. 若创建 `CircleContentActionMenu.vue`（circle 组件），在新组件中实现角色区分
  2. 若保留 `GovernanceActionMenu.vue`（channel 组件），添加 `role` prop + 举报选项

### C4: CircleAnnouncementBar 缺少定时过期检查

- **文件**: `src/views/circle/components/CircleAnnouncementBar.vue:67`
- **问题**: `loadAnnouncement()` 仅在 `onMounted` 时调用一次。如果用户在页面上等待公告过期时间到达，公告栏不会自动隐藏。
- **spec 要求**: "公告到达有效期截止时间，公告栏自动隐藏"
- **修复**: 添加 `setInterval` 定时器（如每 60 秒检查一次 expireAt），`onUnmounted` 清理

---

## WARNING 问题（应该修复）

### W1: AnnouncementManage.vue Tinymce 使用静态 import 而非按需加载

- **文件**: `src/views/channel/governance/AnnouncementManage.vue:45`
- **当前**: `import { Tinymce } from '/@/components/Tinymce'`（静态导入）
- **spec 要求**: `defineAsyncComponent` + dynamic import 按需加载（design.md D1.5）
- **影响**: 治理页首屏加载会包含 Tinymce 编辑器（约 500KB+），即使管理员不操作公告管理 Tab
- **修复**: 改用 `const Tinymce = defineAsyncComponent(() => import('/@/components/Tinymce'))`

### W2: AnnouncementManage.vue 表单缺少 expireAt 有效期字段

- **文件**: `src/views/channel/governance/AnnouncementManage.vue:11-17`
- **问题**: 公告表单只有 `title` 和 `content`，无 `expireAt` 选择器。`saveAnnouncement` API 参数不传 `expireAt`
- **对比**: Circle 公告 API (`circle/announcement.ts:14`) 支持 `expireAt?: string`
- **修复**: 前端添加 DatePicker + API 添加 expireAt 参数 + 后端 `ChannelAnnouncement` 补齐字段

### W3: ReportModal 举报原因枚举与 spec 不一致 + 缺少"其他"必填校验

- **文件**: `src/views/support/report/components/ReportModal.vue:97-103,161-164`
- **问题 1**: 实际枚举为 `porn/violence/fraud/harassment/other`，spec 定义为 `AD/PORNO/ATTACK/OTHER`
- **问题 2**: 选择"其他"时未要求 description 必填
- **spec 要求**: "选择'其他'原因时补充说明变为必填"
- **修复**: 添加 `if (formData.reportType === 'other' && !formData.description.trim()) { message.warning('请填写补充说明'); return; }`

### W4: GovernanceActionMenu 缺少 loading 状态

- **文件**: `src/views/channel/components/GovernanceActionMenu.vue`
- **问题**: 点击菜单项后无 loading 反馈，用户可能重复点击
- **spec 要求**: "操作按钮显示 loading 状态，防止重复点击"
- **修复**: 添加 `loading` prop 和 `disabled` 状态

### W5: ReviewQueue.vue 是"内容审核"而非"加入申请审核"

- **文件**: `src/views/channel/governance/ReviewQueue.vue`
- **观察**: 该组件实现的是内容审核流程（columns 包含 `contentType`/`submitter`/`sourceScene`/`hitRule`），而非 spec 定义的"加入申请审核"（用户申请加入圈子/频道）
- **修复**: 确认是否需要独立的 JoinRequestReview 组件，或更新 spec 匹配实际实现

---

## SUGGESTION（可选改进）

### S1: AnnouncementManage.vue 已有公告时发布仅显示通用确认，未区分"替换"场景

- **文件**: `AnnouncementManage.vue:126-146`
- **当前**: `handlePublish` 显示通用文案，未区分已有公告时"替换旧公告"
- **spec 要求**: "当前已有生效公告，发布新公告将替换旧公告，是否继续？"

### S2: @成员浮层缺少自动翻转定位

- **文件**: `MentionMemberPicker.vue:71-75` 样式 `position: absolute; bottom: 100%`
- **spec 要求**: "输入框下方空间不足时，浮层自动翻转到输入框上方展示"
- **当前**: 固定 `bottom: 100%`，不可翻转

### S3: useMention 正则边缘情况 — 昵称含 `@` 字符时截断

- **文件**: `useMention.ts:145` 正则 `/(@\{userId:(.+?)\})(.+?)(?=@|$)/g`
- **实际风险**: 低（昵称通常不含 `@`）

### S4: MyComment.vue 新旧组件并存

- `src/views/circle/components/MyComment.vue` (新) vs `src/components/jeecg/comment/MyComment.vue` (旧)

---

## 后端需要提供的接口

| 编号 | 接口 | 严重程度 | 说明 |
|------|------|---------|------|
| BE-01 | `GET /api/v1/content/circle/{circleId}/mentionable-members` | **HIGH** | Service 层 `ICircleMentionService.getMentionCandidates()` 已存在，需暴露 Controller 端点。前端目前用 `getMemberList` 替代 |
| BE-02 | `PUT /api/v1/content/circle-content/{contentId}/pin?circleId=xxx` | **HIGH** | Circle content 置顶 API。`CircleContent.isPinned` 字段已存在，需确认 Controller 端点是否已暴露或需新增 |
| BE-03 | `PUT /api/v1/content/circle-content/{contentId}/featured?circleId=xxx` | **HIGH** | Circle content 精华 API。`CircleContent.isFeatured` 字段已存在，同上 |
| BE-04 | `ChannelAnnouncement` 补齐 `expireAt` 字段 | **MEDIUM** | Channel 公告 API `saveAnnouncement` 不支持 `expireAt`。Circle 公告 API 已支持 |
| BE-05 | `POST /circle-report/{reportId}/mute` 透传 `duration` 参数 | **MEDIUM** | 后端 `CircleMemberUpdateReq` 支持 `muteDuration`，但举报禁言接口未透传 |
| BE-06 | `DELETE /api/v1/content/circle/announcement/{id}` | **LOW** | Circle 公告删除接口。前端 `deleteCircleAnnouncement` 已封装，需确认后端端点存在 |

---

## 测试覆盖

| 测试文件 | 测试数 | 状态 |
|---------|--------|------|
| useMention.test.ts | 21 | ✅ |
| MentionMemberPicker.test.ts | 12 | ✅ |
| MyComment.test.ts | 11 | ✅ |
| CircleAnnouncementBar.test.ts | 4 | ✅ |
| ReportDetailDrawer.test.ts | 5 | ✅ |
| ReportCard.test.ts | 12 | ✅ |
| ReportList.test.ts | 12 | ✅ (已修复权限 mock) |
| **合计** | **80** | **80/80 通过** |

---

## 实现与 Spec 偏离对照表

| Spec/Task 期望 | 实际实现 | API 体系 | 问题 |
|---|---|---|---|
| `src/api/circle/content.ts` — togglePin/toggleFeatured | `executeGovernance({channelId, action:'PIN'/'FEATURE'})` | **误用 Channel API** | C1 — Circle ID ≠ Channel ID |
| `CircleContentActionMenu`（circle 组件）| `GovernanceActionMenu.vue`（channel 组件）| **误用 Channel 组件** | C1/C3 — 组件体系错误 |
| `useCircleInteractionStore` | `channelGovernanceStore` + `channelReviewStore` | **误用 Channel Store** | Store 管理的是 channel 内容列表 |
| `CircleAnnouncementBar` → `Detail.vue` | 组件存在但未集成 | — | C2 |
| 加入申请审核 | `ReviewQueue.vue`（内容审核）| **误用 Channel 审核** | W5 — 审核对象不同 |
| `src/api/circle/announcement.ts` | `channel/announcement.ts` (AnnouncementManage 使用) | **混用** | Channel 公告缺 expireAt |

---

## 最终评估

| 类别 | 数量 | 说明 |
|------|------|------|
| CRITICAL | **4** | C1: Circle ID 错当 Channel ID（架构 Bug）、C2: CircleAnnouncementBar 未集成、C3: GovernanceActionMenu 缺角色权限+举报、C4: 缺定时过期检查 |
| WARNING | 5 | Tinymce 静态加载、缺 expireAt、举报枚举不一致、缺 loading、ReviewQueue 语义差异 |
| SUGGESTION | 4 | 替换确认文案、浮层翻转、正则边缘、新旧组件并存 |
| 后端缺失接口 | **6** | mentionable-members (HIGH)、circle-content pin/featured (HIGH×2)、expireAt (MEDIUM)、mute duration (MEDIUM)、delete announcement (LOW) |

**核心结论**: 代码模块（@成员、公告栏、举报处理）实现质量良好，但 **置顶/精华功能的 API 调用链存在架构级 Bug**——错误地将 Circle ID 当作 Channel ID 传给 Channel governance API。Circle 和 Channel 是两个独立的业务实体，后端 `CircleContent` 已有 `isPinned`/`isFeatured` 字段，应使用 Circle 自有 API 实现置顶/精华操作。

**归档建议**: ❌ 不建议归档。4 个 CRITICAL 问题必须修复，其中 C1 是架构级 Bug。
