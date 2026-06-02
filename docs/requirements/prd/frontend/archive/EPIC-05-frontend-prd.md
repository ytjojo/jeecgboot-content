# EPIC-05 - 屏蔽与静音 前端 PRD

## 1. 概述

### 1.1 背景

内容社区当前缺少清晰的拉黑和屏蔽能力，用户遇到骚扰、冲突或信息噪音时无法有效管理个人空间。EPIC-05 需要前端在用户主页、内容卡片、评论区、隐私设置和帮助说明中提供完整的拉黑/屏蔽入口、确认框和状态文案。

### 1.2 核心概念

| 概念 | 定义 | 影响范围 |
|------|------|----------|
| **拉黑（Block）** | 双向切断 | 双方无法查看彼此内容、主页、评论、动态；已有互关自动解除；评论/点赞/私信/@ 全部拦截 |
| **屏蔽（Mute）** | 单向降噪 | 屏蔽方信息流不再展示被屏蔽者内容；但可访问主页；关注关系保留；对方不受影响 |
| **不感兴趣** | 反馈降噪 | 记录反馈，减少类似内容推荐；可选择屏蔽该类型或话题 |
| **屏蔽词** | 关键词/正则过滤 | 信息流中匹配屏蔽词的内容被折叠或隐藏 |

### 1.3 范围

- 包含：拉黑操作与确认、屏蔽操作与确认、黑名单管理页面、屏蔽列表管理页面、屏蔽词设置、不感兴趣反馈弹窗、行为边界说明文案、被拉黑后的模糊提示页面
- 不包含：内容审核系统、自动风控拦截、管理后台批量治理

### 1.4 依赖

- EPIC-01（用户注册登录）：用户身份基础
- EPIC-04（关注与订阅）：拉黑后自动解除互关的联动
- EPIC-09（通知设置）：屏蔽与通知设置的联动

---

## 2. 用户故事

### 2.1 拉黑相关

| 故事ID | 用户故事 | 优先级 |
|--------|----------|--------|
| US-01 | 作为用户，我希望在主页或评论区拉黑某用户，以便彻底切断双向互动 | 高 |
| US-02 | 作为用户，我希望拉黑后双方无法查看彼此内容，互关自动解除 | 高 |
| US-03 | 作为用户，我希望在黑名单页面查看和管理已拉黑用户 | 中 |
| US-04 | 作为用户，我希望拉黑操作不通知对方，保护隐私 | 高 |

### 2.2 屏蔽相关

| 故事ID | 用户故事 | 优先级 |
|--------|----------|--------|
| US-05 | 作为用户，我希望屏蔽某用户动态，信息流不再展示但可访问主页 | 高 |
| US-06 | 作为用户，我希望对内容选择"不感兴趣"并屏蔽类型或话题 | 中 |
| US-07 | 作为用户，我希望设置屏蔽词过滤不适内容 | 低 |
| US-08 | 作为用户，我希望临时屏蔽话题/Tag，阶段性降噪 | 低 |
| US-09 | 作为用户，我希望独立管理屏蔽列表与黑名单 | 中 |

### 2.3 行为边界

| 故事ID | 用户故事 | 优先级 |
|--------|----------|--------|
| US-10 | 作为用户，我希望操作前看到清晰的说明，理解拉黑和屏蔽的区别 | 中 |

---

## 3. 页面设计

### 3.1 页面清单

| 页面 | 路由 | 说明 |
|------|------|------|
| 黑名单管理 | `/settings/blacklist` | 查看和管理已拉黑用户 |
| 屏蔽列表管理 | `/settings/mute-list` | 查看和管理屏蔽用户、话题、类型、屏蔽词 |
| 屏蔽词设置 | `/settings/mute-list#keywords` | 屏蔽列表页面的锚点区域 |
| 被拉黑提示页 | 由主页路由拦截展示 | 被拉黑用户访问时的模糊提示 |

### 3.2 黑名单管理页面

**页面结构**:
- 顶部：页面标题"黑名单" + 返回按钮
- 说明区域：简短文案"拉黑后双方无法查看彼此内容，关注关系已自动解除"
- 列表区域：已拉黑用户卡片列表，按拉黑时间倒序
- 空状态：无拉黑用户时展示空状态插图和文案

**用户卡片信息**:
- 用户头像（圆形）
- 用户昵称
- 拉黑时间（格式：YYYY-MM-DD）
- 操作按钮："解除拉黑"

**交互流程**:
1. 页面加载 → 调用黑名单 API → 渲染列表
2. 点击"解除拉黑" → 弹出确认框（说明"拉黑已解除，但关注关系不会自动恢复，如需重新关注请手动操作"）→ 确认 → 调用解除拉黑 API → 移除卡片
3. 支持下拉刷新和滚动加载更多

### 3.3 屏蔽列表管理页面

**页面结构**:
- 顶部：页面标题"屏蔽设置" + 返回按钮
- Tab 切换：屏蔽用户 | 屏蔽话题 | 屏蔽类型 | 屏蔽词 | 临时屏蔽
- 各 Tab 下为对应的管理列表
- 支持批量选择和批量取消

**Tab 内容**:

**屏蔽用户 Tab**:
- 用户卡片：头像、昵称、屏蔽时间
- 操作：单个取消屏蔽 / 批量取消

**屏蔽话题 Tab**:
- 话题标签列表：话题名称、屏蔽时间
- 临时屏蔽项额外显示剩余天数
- 操作：单个取消 / 批量取消

**屏蔽类型 Tab**:
- 内容类型列表（如：视频、图文、纯文字）
- 操作：单个取消

**屏蔽词 Tab**:
- 关键词/正则列表：规则文本、规则类型标签（关键词/正则）
- 添加入口：输入框 + 添加按钮
- 操作：单个删除 / 批量删除

**临时屏蔽 Tab**:
- 临时屏蔽列表：话题名称、剩余天数、创建时间
- 操作：提前取消

### 3.4 被拉黑提示页面

当被拉黑用户尝试访问拉黑者主页时，页面展示模糊提示：
- 方案一：显示"该用户不存在"（推荐，最安全）
- 方案二：显示空白页面
- 不透露被拉黑状态

当拉黑者自己访问被拉黑用户主页时：
- 显示提示："您已拉黑该用户，无法查看其内容"
- 提供"解除拉黑"快捷入口

---

## 4. 组件设计

### 4.1 BlockConfirmModal - 拉黑确认弹窗

**触发位置**:
- 用户主页（更多操作菜单）
- 评论区（评论者头像长按/更多操作）
- 内容卡片（作者更多操作菜单）

**弹窗内容**:
- 标题：确认拉黑
- 说明文案："拉黑后双方将无法查看彼此内容，已有关注关系将解除。对方不会收到通知。"
- 按钮："取消" / "确认拉黑"（红色强调）

**Props**:
```typescript
interface BlockConfirmModalProps {
  visible: boolean;
  targetUser: {
    userId: string;
    nickname: string;
    avatar: string;
  };
  onConfirm: () => Promise<void>;
  onCancel: () => void;
}
```

### 4.2 MuteConfirmModal - 屏蔽确认弹窗

**触发位置**:
- 用户主页（更多操作菜单）
- 内容卡片（作者更多操作菜单）

**弹窗内容**:
- 标题：屏蔽该用户
- 说明文案："屏蔽后您将不再看到该用户的内容，但仍可访问其主页，对方不受影响。"
- 按钮："取消" / "确认屏蔽"

**Props**:
```typescript
interface MuteConfirmModalProps {
  visible: boolean;
  targetUser: {
    userId: string;
    nickname: string;
  };
  onConfirm: () => Promise<void>;
  onCancel: () => void;
}
```

### 4.3 NotInterestedPanel - 不感兴趣反馈面板

**触发位置**:
- 内容卡片（更多操作菜单中选择"不感兴趣"）

**面板内容**:
- 第一步：点击"不感兴趣" → 记录反馈，Toast 提示"已标记，将减少类似推荐"
- 第二步（可选展开）：提供选项
  - "屏蔽此类内容"（如视频、图文）
  - "屏蔽该话题"（如 #八卦）
  - "屏蔽此话题 7 天"（临时屏蔽）
  - "不再提示"

**Props**:
```typescript
interface NotInterestedPanelProps {
  visible: boolean;
  contentId: string;
  contentType: string;
  topics: string[];
  onFeedback: (contentId: string) => Promise<void>;
  onBlockType: (contentType: string) => Promise<void>;
  onBlockTopic: (topic: string, temporary?: number) => Promise<void>;
  onClose: () => void;
}
```

### 4.4 BlockedContentPlaceholder - 屏蔽词折叠占位

**触发位置**:
- 信息流中命中屏蔽词的内容卡片

**展示状态**:
- 折叠态：显示"该内容包含屏蔽词，已折叠" + "点击展开"按钮
- 展开态：正常展示内容

**Props**:
```typescript
interface BlockedContentPlaceholderProps {
  contentId: string;
  isFolded: boolean;
  onExpand: (contentId: string) => void;
}
```

### 4.5 UserActionMenu - 用户操作菜单

**触发位置**:
- 用户主页右上角更多按钮
- 评论区评论者头像/昵称区域
- 内容卡片作者区域

**菜单项**（根据当前关系动态展示）:

| 菜单项 | 展示条件 | 说明 |
|--------|----------|------|
| 拉黑该用户 | 未拉黑时 | 点击弹出 BlockConfirmModal |
| 取消拉黑 | 已拉黑时 | 点击弹出确认框 |
| 屏蔽该用户 | 未屏蔽且未拉黑时 | 点击弹出 MuteConfirmModal |
| 取消屏蔽 | 已屏蔽时 | 点击直接取消 |
| 举报 | 始终展示 | 跳转举报流程（不在本 EPIC 范围） |

**Props**:
```typescript
interface UserActionMenuProps {
  targetUser: {
    userId: string;
    nickname: string;
    avatar: string;
  };
  isBlocked: boolean;
  isMuted: boolean;
  onBlock: () => void;
  onUnblock: () => void;
  onMute: () => void;
  onUnmute: () => void;
  onReport: () => void;
}
```

### 4.6 KeywordFilterInput - 屏蔽词输入组件

**位置**：屏蔽词设置 Tab

**功能**:
- 输入框：支持输入关键词或正则表达式
- 类型切换：关键词 / 正则（Radio 或 SegmentedControl）
- 添加按钮
- 输入校验：不能为空、长度限制、正则合法性校验
- 已有规则列表展示和删除

**Props**:
```typescript
interface KeywordFilterInputProps {
  rules: FilterRule[];
  onAdd: (rule: { text: string; type: 'KEYWORD' | 'REGEX' }) => Promise<void>;
  onDelete: (ruleId: string) => Promise<void>;
  onBatchDelete: (ruleIds: string[]) => Promise<void>;
}

interface FilterRule {
  id: string;
  text: string;
  type: 'KEYWORD' | 'REGEX';
  createdAt: string;
}
```

---

## 5. API 对接

### 5.1 拉黑相关 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 拉黑用户 | POST | `/api/content/block` | 请求体：`{ targetUserId }` |
| 解除拉黑 | POST | `/api/content/unblock` | 请求体：`{ targetUserId }` |
| 黑名单列表 | GET | `/api/content/blacklist` | 分页参数：`page`, `pageSize` |
| 查询拉黑状态 | GET | `/api/content/block/status/{targetUserId}` | 返回：`{ isBlocked, isBlockedByTarget }` |

### 5.2 屏蔽相关 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 屏蔽用户 | POST | `/api/content/mute` | 请求体：`{ targetUserId }` |
| 取消屏蔽 | POST | `/api/content/unmute` | 请求体：`{ targetUserId }` |
| 屏蔽用户列表 | GET | `/api/content/mute/users` | 分页参数 |
| 查询屏蔽状态 | GET | `/api/content/mute/status/{targetUserId}` | 返回：`{ isMuted }` |

### 5.3 不感兴趣与过滤规则 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 标记不感兴趣 | POST | `/api/content/not-interested` | 请求体：`{ contentId, contentType, topic }` |
| 添加过滤规则 | POST | `/api/content/filter-rule` | 请求体：`{ ruleType, ruleValue, expiresAt }` |
| 删除过滤规则 | DELETE | `/api/content/filter-rule/{ruleId}` | - |
| 批量删除过滤规则 | POST | `/api/content/filter-rule/batch-delete` | 请求体：`{ ruleIds: string[] }` |
| 过滤规则列表 | GET | `/api/content/filter-rules` | 查询参数：`ruleType`（TOPIC/CONTENT_TYPE/WORD/REGEX） |

### 5.4 屏蔽词 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 添加屏蔽词 | POST | `/api/content/filter-rule` | `ruleType: WORD` 或 `REGEX` |
| 屏蔽词列表 | GET | `/api/content/filter-rules?ruleType=WORD,REGEX` | - |
| 删除屏蔽词 | DELETE | `/api/content/filter-rule/{ruleId}` | - |

### 5.5 前端数据模型

```typescript
// 拉黑用户
interface BlockedUser {
  userId: string;
  nickname: string;
  avatar: string;
  blockTime: string; // ISO 8601
}

// 屏蔽用户
interface MutedUser {
  userId: string;
  nickname: string;
  avatar: string;
  muteTime: string;
}

// 过滤规则
interface FilterRule {
  id: string;
  ruleType: 'WORD' | 'REGEX' | 'TOPIC' | 'CONTENT_TYPE';
  ruleValue: string;
  expiresAt: string | null; // null 表示永久
  createdAt: string;
}

// 不感兴趣反馈
interface NotInterestedFeedback {
  contentId: string;
  contentType: string;
  topic?: string;
}
```

---

## 6. 状态管理

### 6.1 全局状态

使用 Pinia store 管理以下状态：

```typescript
// stores/blocking.ts
interface BlockingStore {
  // 当前用户的拉黑/屏蔽关系缓存（用于快速判断）
  blockedUserIds: Set<string>;
  mutedUserIds: Set<string>;

  // 操作方法
  blockUser(targetUserId: string): Promise<void>;
  unblockUser(targetUserId: string): Promise<void>;
  muteUser(targetUserId: string): Promise<void>;
  unmuteUser(targetUserId: string): Promise<void>;

  // 状态查询
  isBlocked(userId: string): boolean;
  isMuted(userId: string): boolean;

  // 批量加载（页面初始化时）
  loadBlockingStatus(userIds: string[]): Promise<void>;
}
```

### 6.2 页面状态

```typescript
// 黑名单页面状态
interface BlacklistPageState {
  users: BlockedUser[];
  loading: boolean;
  page: number;
  hasMore: boolean;
}

// 屏蔽列表页面状态
interface MuteListPageState {
  activeTab: 'users' | 'topics' | 'types' | 'keywords' | 'temporary';
  users: MutedUser[];
  rules: FilterRule[];
  loading: boolean;
  selectedIds: Set<string>; // 批量选择
  isBatchMode: boolean;
}
```

### 6.3 缓存策略

- 拉黑/屏蔽状态：页面级缓存，操作后立即更新本地状态并同步服务端
- 黑名单/屏蔽列表：每次进入页面重新加载，支持下拉刷新
- 屏蔽词列表：操作后立即更新本地列表

---

## 7. 交互设计

### 7.1 拉黑交互流程

```
用户操作入口（主页/评论区/内容卡片）
  → 点击"拉黑该用户"
  → 弹出确认弹窗（说明拉黑后果）
  → 点击"确认拉黑"
  → 调用 API
  → 成功：Toast "已拉黑" + 页面刷新（移除该用户内容）
  → 失败：Toast "操作失败，请重试"
```

**拉黑后页面变化**:
- 信息流：立即移除该用户的所有内容
- 主页：跳转到被拉黑提示页
- 评论区：移除该用户的所有评论

### 7.2 解除拉黑交互流程

```
入口一：黑名单管理页面
  → 点击"解除拉黑"
  → 弹出确认弹窗（说明关注关系不会自动恢复）
  → 确认 → API 调用 → 列表移除该用户

入口二：访问被拉黑用户主页
  → 显示"您已拉黑该用户"
  → 点击"解除拉黑"
  → 弹出确认弹窗
  → 确认 → API 调用 → 刷新页面展示正常主页
```

### 7.3 屏蔽交互流程

```
用户操作入口（主页/内容卡片）
  → 点击"屏蔽该用户"
  → 弹出确认弹窗（说明屏蔽是单向降噪）
  → 点击"确认屏蔽"
  → 调用 API
  → 成功：Toast "已屏蔽，将不再看到该用户的内容"
  → 信息流刷新移除该用户内容
```

### 7.4 不感兴趣交互流程

```
内容卡片 → 更多操作 → "不感兴趣"
  → 记录反馈 + Toast "已标记，将减少类似推荐"
  → 展开二级选项面板：
    - "屏蔽此类内容" → 添加 CONTENT_TYPE 规则
    - "屏蔽该话题" → 添加 TOPIC 规则
    - "屏蔽此话题 7 天" → 添加临时 TOPIC 规则（expiresAt = 7天后）
    - "不再提示" → 关闭面板
```

### 7.5 屏蔽词管理交互流程

```
屏蔽词设置 Tab
  → 输入框输入关键词/正则
  → 选择类型（关键词/正则）
  → 点击"添加"
  → 前端校验（非空、长度、正则合法性）
  → 调用 API
  → 成功：添加到列表 + 清空输入框
  → 失败：Toast 错误提示
```

### 7.6 批量操作交互流程

```
屏蔽列表页面
  → 点击"批量管理"
  → 进入批量选择模式
  → 勾选多个项目
  → 点击"批量取消"
  → 弹出确认框
  → 确认 → 调用批量 API
  → 成功：移除已选项 + 退出批量模式
```

### 7.7 边界教育文案

| 场景 | 文案 |
|------|------|
| 拉黑确认框 | "拉黑后双方将无法查看彼此内容，已有关注关系将解除。对方不会收到通知。" |
| 屏蔽确认框 | "屏蔽后您将不再看到该用户的内容，但仍可访问其主页，对方不受影响。" |
| 解除拉黑确认框 | "拉黑已解除，但关注关系不会自动恢复，如需重新关注请手动操作。" |
| 被拉黑方访问主页 | "该用户不存在" 或空白页面 |
| 拉黑方访问被拉黑主页 | "您已拉黑该用户，无法查看其内容" |
| 屏蔽词命中内容 | "该内容包含屏蔽词，已折叠" |
| 帮助中心搜索 | "屏蔽是单向降噪：您不再看到对方内容，但可访问主页。拉黑是双向切断：双方完全无法互动。" |

---

## 8. 响应式设计

### 8.1 移动端（< 768px）

- 黑名单/屏蔽列表：全屏页面，卡片式布局
- 确认弹窗：底部弹出式 ActionSheet
- 不感兴趣面板：底部弹出式面板
- 屏蔽词输入：全宽输入框
- 批量操作：底部固定操作栏

### 8.2 平板端（768px - 1024px）

- 黑名单/屏蔽列表：居中容器，最大宽度 600px
- 确认弹窗：居中 Modal
- 不感兴趣面板：居中 Modal 或 Popover

### 8.3 桌面端（> 1024px）

- 黑名单/屏蔽列表：居中容器，最大宽度 800px
- 确认弹窗：居中 Modal
- 不感兴趣面板：Popover 气泡卡片
- 屏蔽词管理：左右分栏（输入区 + 列表区）

---

## 9. 性能要求

### 9.1 响应时间

| 操作 | 目标响应时间 |
|------|-------------|
| 拉黑/解除拉黑操作 | < 500ms |
| 屏蔽/取消屏蔽操作 | < 500ms |
| 不感兴趣反馈 | < 300ms |
| 屏蔽词添加/删除 | < 300ms |
| 黑名单页面加载 | < 1s |
| 屏蔽列表页面加载 | < 1s |

### 9.2 优化策略

- 拉黑/屏蔽操作采用乐观更新：先更新 UI 状态，API 失败时回滚
- 黑名单/屏蔽列表使用分页加载，默认每页 20 条
- 屏蔽词匹配结果由后端返回，前端不做二次过滤
- 确认弹窗组件按需加载（lazy load）
- 批量操作使用防抖，避免重复提交

### 9.3 离线与弱网

- 操作失败时展示 Toast 提示，支持重试
- 弱网环境下拉黑/屏蔽操作需等待服务端确认后再更新 UI（非乐观更新），避免状态不一致

---

## 10. 测试要点

### 10.1 功能测试

**拉黑功能**:
- [ ] 从主页拉黑用户，确认弹窗展示正确文案
- [ ] 拉黑后信息流移除该用户内容
- [ ] 拉黑后访问被拉黑用户主页显示"您已拉黑该用户"
- [ ] 被拉黑方访问拉黑者主页显示"用户不存在"或空白
- [ ] 拉黑后互关自动解除
- [ ] 被拉黑方评论/点赞/私信/@ 操作被拦截
- [ ] 拉黑方无法 @ 被拉黑用户
- [ ] 拉黑和解除拉黑不发送通知
- [ ] 解除拉黑后可正常访问主页，关注关系不自动恢复

**黑名单管理**:
- [ ] 黑名单按拉黑时间倒序展示
- [ ] 解除拉黑后用户从列表移除
- [ ] 空状态正确展示
- [ ] 分页加载正常

**屏蔽功能**:
- [ ] 从主页/内容卡片屏蔽用户
- [ ] 屏蔽后信息流移除该用户内容
- [ ] 屏蔽后仍可访问被屏蔽用户主页
- [ ] 屏蔽后关注关系保留
- [ ] 被屏蔽方不受影响（单向）
- [ ] 取消屏蔽后信息流恢复展示

**不感兴趣**:
- [ ] 点击"不感兴趣"记录反馈
- [ ] 二级选项面板展示正确
- [ ] 选择屏蔽类型/话题后规则生效
- [ ] 临时屏蔽到期自动取消
- [ ] 临时屏蔽可提前取消

**屏蔽词**:
- [ ] 添加关键词屏蔽词生效
- [ ] 添加正则屏蔽词生效（含合法性校验）
- [ ] 屏蔽词命中内容正确折叠
- [ ] 折叠内容可展开查看
- [ ] 删除屏蔽词后恢复展示
- [ ] 输入校验（空值、长度、正则合法性）

**批量操作**:
- [ ] 批量选择模式切换
- [ ] 批量取消屏蔽/删除屏蔽词
- [ ] 批量操作确认弹窗

### 10.2 边界测试

- [ ] 拉黑自己（应被拒绝）
- [ ] 拉黑不存在的用户
- [ ] 重复拉黑同一用户（幂等）
- [ ] 重复屏蔽同一用户（幂等）
- [ ] 添加重复屏蔽词
- [ ] 添加无效正则表达式
- [ ] 屏蔽词长度超限
- [ ] 空列表状态展示
- [ ] 网络异常时操作失败处理

### 10.3 兼容性测试

- [ ] 移动端（iOS/Android）布局和交互
- [ ] 平板端布局适配
- [ ] 桌面端布局适配
- [ ] 暗色模式适配
- [ ] 无障碍访问（屏幕阅读器、键盘导航）

### 10.4 性能测试

- [ ] 黑名单大量数据（1000+）列表渲染性能
- [ ] 屏蔽词大量数据（100+）列表渲染性能
- [ ] 频繁操作（快速连续点击）防抖处理
- [ ] 乐观更新失败回滚的 UI 一致性
