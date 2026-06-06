---
epicId: EPIC-21
title: 频道隐私、订阅与成员管理 — 前端 PRD
status: draft
date: '2026-06-02'
sourceEpic: docs/requirements/prd/decomposition/channel/EPIC-21-channel-privacy-membership.md
sourceDesign: openspec/changes/channel-21-privacy-membership-frontend/design.md
sourceProposal: openspec/changes/channel-21-privacy-membership-frontend/proposal.md
---

# 频道隐私、订阅与成员管理 — 前端 PRD

> **史诗ID**: EPIC-21
> **域**: channel（频道域）
> **变更ID**: channel-21-privacy-membership-frontend
> **版本**: 1.0
> **前置依赖**: EPIC-20
> **日期**: 2026-06-02

## 1. 概述

### 需求目标

为频道模块补充隐私控制、订阅机制、成员角色和成员治理的前端能力，让频道从"内容容器"升级为"可运营、可参与、可治理的社区单元"。

**要解决的问题**：
- 频道无隐私状态区分，所有内容对所有人可见
- 用户无法主动订阅频道并在信息流中获得加权展示
- 频道主无法管理成员角色、加入规则和违规成员
- 缺乏加入申请审核、禁言、黑名单等治理界面

**期望结果**：
- 频道主可在设置页配置隐私和加入规则
- 用户可在频道主页完成订阅/取消订阅/申请加入
- 管理员可在成员管理页完成角色分配、移除、禁言、黑名单操作
- 用户可在订阅列表页管理已订阅频道

### 目标用户与使用场景

| 用户角色 | 使用场景 | 入口 |
|---------|---------|------|
| 频道主 | 配置频道隐私状态和加入方式 | 频道设置页 |
| 频道管理员 | 审核加入申请、管理成员、执行治理操作 | 成员管理页、待审队列 |
| 内容编辑 | 浏览成员列表（只读） | 成员管理页 |
| 普通成员 | 订阅/取消订阅频道、查看订阅列表 | 频道主页、订阅列表页 |
| 内容消费者 | 申请加入私有频道、使用邀请加入 | 频道主页、邀请链接 |

**设备场景**：桌面端 + 移动端响应式

### 范围定义

#### 本期范围
- 频道隐私设置（公开/私有）
- 加入方式配置（自由/审核/邀请）
- 加入申请提交与审核
- 频道订阅/取消订阅
- 订阅列表管理（分组、搜索、提醒控制）
- 成员角色分配与成员列表
- 移除成员（含冷却期提示）
- 禁言/解除禁言
- 黑名单管理
- 治理操作日志查看

#### 非本期范围
- 频道创建与所有权管理（EPIC-20）
- 内容发布权限详细规则（EPIC-22）
- 频道推荐与发现算法（EPIC-23）
- 完整付费频道闭环
- 平台级黑名单联动

---

## 2. 功能列表

| 功能名称 | 功能概述 | 适用角色 | 优先级 | 关联页面 |
|---------|---------|---------|-------|---------|
| 频道隐私设置 | 设置频道公开/私有状态，展示变更影响提示 | 频道主 | P0 | 频道设置页 |
| 加入方式配置 | 配置自由/审核/邀请三种加入模式 | 频道主 | P0 | 频道设置页 |
| 订阅/取消订阅 | 在频道主页订阅或取消订阅频道 | 普通成员、内容消费者 | P0 | 频道主页 |
| 申请加入 | 向私有频道提交加入申请 | 内容消费者 | P0 | 频道主页 |
| 加入申请审核 | 审核待审申请，支持批量操作 | 频道管理员 | P0 | 待审队列页 |
| 邀请管理 | 创建/撤销邀请码和邀请链接 | 频道管理员 | P1 | 邀请管理页 |
| 订阅列表管理 | 分组、搜索、提醒控制 | 普通成员 | P1 | 订阅列表页 |
| 成员列表 | 展示成员信息，按角色筛选、搜索 | 频道管理员 | P0 | 成员管理页 |
| 角色分配 | 为成员分配管理员/编辑/普通成员角色 | 频道主 | P0 | 成员管理页 |
| 移除成员 | 移除成员，含冷却期提示和批量操作 | 频道管理员 | P0 | 成员管理页 |
| 禁言/解除禁言 | 设置禁言时长或手动解除 | 频道管理员 | P0 | 成员管理页 |
| 黑名单管理 | 加入/移出黑名单，查看黑名单列表 | 频道管理员 | P1 | 黑名单管理页 |
| 治理操作日志 | 查看移除/禁言/黑名单操作记录 | 频道管理员 | P1 | 治理日志页 |

---

## 3. 功能详细说明

### 3.1 频道隐私设置

**入口**：频道设置页 → 隐私设置分组

**核心流程**：
1. 频道主进入频道设置页
2. 在"隐私设置"区域选择"公开"或"私有"
3. 若频道为系统频道，隐私选项锁定为"公开"，Radio 禁用并展示提示"系统频道必须公开"
4. 切换隐私状态时，页面展示影响说明弹窗（见下方交互要求）
5. 确认后保存，按钮显示 loading

**交互要求**：
- 使用 Radio.Group 切换公开/私有
- 系统频道：Radio 禁用，下方展示 `<Alert type="info" message="系统频道必须公开，不允许设置为私有" />`
- 切换时弹出确认 Modal：
  - 公开→私有：标题"确认设为私有频道？"，内容说明"频道将退出公开搜索和推荐，非成员将无法浏览受限内容。当前订阅者不受影响。"
  - 私有→公开：标题"确认设为公开频道？"，内容说明"频道内容将对所有人可见，可被搜索和推荐。"
- 确认按钮为危险操作样式（红色），取消按钮为默认样式
- 保存成功后 `useMessage().createMessage('隐私设置已更新')`

**状态与边界**：
- 加载中：Radio.Group 展示 skeleton
- 保存中：确认按钮显示 loading 并禁用
- 保存失败：保留用户选择，展示错误提示
- 权限不足：页面展示"您无权修改频道设置"，操作按钮隐藏

---

### 3.2 加入方式配置

**入口**：频道设置页 → 加入方式分组

**核心流程**：
1. 频道主在"加入方式"区域选择自由加入/审核加入/邀请加入
2. 选择不同方式时，下方展示对应配置项
3. 保存后新加入请求按最新规则处理

**交互要求**：
- 使用 Radio.Group 选择加入方式
- 选择"自由加入"时无额外配置
- 选择"审核加入"时展示：
  - Switch：是否允许被拒绝后再次申请
  - InputNumber：再次申请间隔（小时），默认 24
- 选择"邀请加入"时展示：
  - Button"创建邀请"→ 打开邀请创建 Drawer
  - 已有邀请列表（Table：邀请码/链接、有效期、已用次数/总次数、状态、操作）
- 邀请创建 Drawer 表单字段：
  - 邀请类型：Radio.Group（邀请码/邀请链接）
  - 有效期：DatePicker.RangePicker
  - 可用次数：InputNumber，最小 1
  - 确认创建 → 生成邀请码/链接，展示复制按钮

**状态与边界**：
- 邀请列表为空：展示空状态"暂无邀请，点击上方按钮创建"
- 邀请已过期：状态标签展示"已过期"（灰色）
- 邀请已用完：状态标签展示"已用完"（灰色）
- 邀请已撤销：状态标签展示"已撤销"（橙色）
- 邀请有效：状态标签展示"有效"（绿色）

---

### 3.3 订阅/取消订阅

**入口**：频道主页 → 操作按钮区

**核心流程**：
1. 用户进入频道主页
2. 页面根据用户与频道的关系展示不同按钮：
   - 未订阅公开频道：展示"订阅"主按钮
   - 已订阅：展示"已订阅"按钮（带下拉菜单：取消订阅）
   - 未订阅私有频道（非成员）：展示"申请加入"按钮
   - 私有频道已加入但未订阅：展示"订阅"按钮
   - 被禁言：展示"已禁言"标签 + 订阅状态
   - 被黑名单：不展示订阅/加入按钮，展示"您无法加入此频道"
3. 点击"订阅"→ 按钮变为 loading → 成功后变为"已订阅"
4. 点击"已订阅"下拉 → "取消订阅"→ 二次确认 → 取消成功

**交互要求**：
- "订阅"按钮为主按钮样式（Primary），放在频道名称/简介下方
- "已订阅"按钮为默认样式，hover 展示下拉菜单
- 取消订阅需二次确认 Modal："确认取消订阅？取消后您将不再收到该频道的更新推送。"
- 私有频道非成员点击订阅时，引导至"申请加入"流程
- 订阅成功后页面顶部展示成功消息
- 按钮状态变更需有过渡动画（loading → 完成）

**乐观更新与缓存失效**：
- 采用乐观更新策略：点击"订阅"后立即更新按钮状态为"已订阅"（不等待接口返回），请求失败时回滚按钮状态并展示错误提示
- 取消订阅同理：点击确认后立即更新按钮状态为"订阅"
- 操作成功后需同步更新以下数据：
  - `useChannelContext` 中的 `isSubscribed` 字段
  - 使 `/channel/subscription/list` 的缓存失效（订阅列表页下次访问重新拉取）
  - 使 `/channel/subscription/status/{channelId}` 的缓存失效

**状态与边界**：
- 订阅操作中：按钮显示 loading（乐观更新场景下短暂展示）
- 订阅失败：回滚乐观更新，保留原状态，展示错误提示
- 网络异常：按钮恢复原状态，提示"网络异常，请重试"

---

### 3.4 申请加入私有频道

**入口**：频道主页 → "申请加入"按钮

**核心流程**：
1. 用户进入私有频道主页
2. 页面展示频道基础信息（名称、简介、头像），不展示受限内容
3. 用户点击"申请加入"
4. 弹出申请表单 Modal
5. 提交后按钮变为"待审核"状态

**交互要求**：
- "申请加入"按钮为主按钮样式（Primary）
- 申请表单 Modal：
  - 标题："申请加入 [频道名称]"
  - TextArea：申请理由（必填，最少 10 字，最多 200 字）
  - 实时字数统计
  - 底部：取消 + 提交按钮
- 提交后频道主页按钮变为"待审核"（Disabled 样式 + Tooltip"您的申请正在审核中"）
- 若已有未处理申请，按钮直接展示"待审核"，点击提示"您已有一个待审核申请"

**乐观更新与缓存失效**：
- 提交申请后立即更新按钮为"待审核"状态（不等待接口返回），失败时回滚并展示错误提示
- 操作成功后需使以下缓存失效：
  - `/channel/member/applications/status/{channelId}`（申请状态）
  - `/channel/member/applications/pending`（待审列表，管理员侧）

**状态与边界**：
- 已有待审申请：按钮禁用，Tooltip 提示
- 已被拒绝：展示"重新申请"按钮（若频道允许再次申请）
- 冷却期内：展示"冷却期剩余 X 天"，按钮禁用
- 被黑名单：展示"您无法加入此频道"

---

### 3.5 加入申请审核

**入口**：频道管理 → 待审队列

**核心流程**：
1. 管理员进入待审队列页
2. 展示待审申请列表
3. 管理员可单条批准/拒绝或批量操作
4. 操作完成后申请移出待审列表

**布局与结构**：
```
页面顶部：标题"待审队列" + 统计数字"共 N 条待审"
筛选区：申请时间范围（DatePicker.RangePicker）
操作区：批量批准按钮 + 批量拒绝按钮（选中后激活）
表格区：
  - 列：复选框 | 申请人头像+昵称 | 申请理由 | 申请时间 | 是否超时 | 操作
  - 超过 48 小时的申请：行背景高亮（浅橙色）+ "超时"标签
  - 操作列：批准按钮（绿色文字）| 拒绝按钮（红色文字）
```

**交互要求**：
- 批量操作：表格第一列为 Checkbox，选中后顶部操作区展示"已选 N 项 批准 拒绝"
- 单条批准：直接点击，无需二次确认
- 单条拒绝：弹出 Modal 填写拒绝原因（TextArea，必填）
- 批量拒绝：弹出 Modal 填写统一拒绝原因
- 每条操作完成后表格行淡出移除
- 批量操作结果：弹出结果 Modal，展示"成功 N 条，失败 N 条"，失败项列出原因
- **批量接口契约**：采用单次批量请求（一次请求传数组），后端返回每条处理结果（成功/失败 + 原因）。批量接口非原子操作，允许部分成功。响应格式：`{ code: 200, result: { success: number, failed: number, details: Array<{ applicationId, success, errorMessage }> } }`
- **前端实现**：提交批量请求后展示 loading 状态的 Modal，请求完成后逐条展示处理结果。不采用逐条请求方式，避免网络开销和并发状态管理复杂度

**缓存失效**：
- 批准/拒绝操作成功后需使以下缓存失效：
  - `/channel/member/applications/pending`（待审列表）
  - `/channel/member/list`（成员列表，批准后新增成员）
  - `/channel/subscription/status/{channelId}`（被批准用户的订阅状态）

**状态与边界**：
- 空列表：展示空状态"暂无待审核的加入申请" + 引导文案
- 加载中：表格展示 Skeleton
- 操作失败：行保留，展示错误提示
- 权限不足：页面展示"您无权审核加入申请"

---

### 3.6 邀请管理

**入口**：频道管理 → 邀请管理

**布局与结构**：
```
页面顶部：标题"邀请管理" + "创建邀请"按钮（Primary）
表格区：
  - 列：邀请码/链接 | 类型 | 创建者 | 有效期 | 已用次数/总次数 | 状态 | 操作
  - 操作列：复制（邀请码/链接）| 撤销
```

**交互要求**：
- 创建邀请：Drawer 表单（见 3.2 节邀请创建 Drawer）
- 复制：点击后复制到剪贴板，`useMessage().createMessage('已复制到剪贴板')`
- 撤销：二次确认 Modal"撤销后该邀请立即失效，已通过该邀请加入的成员不受影响。确认撤销？"
- 撤销成功后状态标签更新为"已撤销"

---

### 3.7 订阅列表管理

**入口**：个人中心 → 我的订阅（或频道模块导航入口）

**布局与结构**：
```
页面顶部：标题"我的订阅" + 搜索框（Input.Search）
左侧/顶部标签页：分组筛选（全部 | 默认分组 | 自定义分组...）+ "新建分组"按钮
内容区：
  - 卡片列表或列表形式
  - 每项：频道头像 + 频道名称 + 最新内容摘要 + 订阅来源标签 + 提醒开关（Switch）+ 取消订阅
  - 默认关注频道展示"系统推荐"标签（蓝色 Tag）
```

**交互要求**：
- 搜索：输入关键词实时过滤，按频道名称模糊匹配
- 分组切换：点击标签页切换分组视图
- 新建分组：弹出 Modal 输入分组名称
- 移动频道到分组：频道卡片上右键菜单或长按菜单 → "移动到分组" → 选择目标分组
- 提醒开关：Switch 切换，无需确认，切换成功后展示成功消息
- 取消订阅：二次确认 Modal，确认后卡片移除

**状态与边界**：
- 空订阅列表：展示空状态插图"暂无订阅频道" + "去发现频道"按钮
- 加载中：卡片列表展示 Skeleton
- 搜索无结果：展示"未找到匹配的频道"

---

### 3.8 成员列表与角色分配

**入口**：频道管理 → 成员管理

**布局与结构**：
```
页面顶部：标题"成员管理" + 统计数字"共 N 位成员"
筛选区：角色筛选（Select：全部/频道主/管理员/内容编辑/普通成员）+ 搜索框 + 排序（加入时间正序/倒序）
操作区：批量移除按钮 + 批量禁言按钮（选中后激活）
表格区：
  - 列：复选框 | 头像+昵称 | 角色（Tag）| 加入时间 | 贡献数 | 治理状态 | 操作
  - 角色标签颜色：频道主（紫色）、管理员（蓝色）、内容编辑（绿色）、普通成员（默认）
  - 治理状态：正常（无标签）、已禁言（橙色 Tag + 到期时间）、冷却期中（灰色 Tag）
  - 操作列：下拉菜单（Dropdown）
    - 频道主可见：修改角色（子菜单：管理员/内容编辑/普通成员）| 移除 | 禁言 | 加入黑名单
    - 管理员可见：移除 | 禁言 | 加入黑名单（不可修改频道主角色）
    - 内容编辑/普通成员：无操作列
```

**交互要求**：
- 角色筛选：Select 组件，默认"全部"
- 搜索：Input.Search，按昵称模糊匹配
- 修改角色：Dropdown 子菜单选择 → 二次确认 Modal"确认将 [昵称] 的角色从 [原角色] 变更为 [新角色]？"
- 移除成员：Modal 二次确认，内容"确认将 [昵称] 移出频道？移除后 7 天内该用户无法再次加入。" + TextArea 填写原因（必填）
- 禁言成员：Modal，包含 Select 选择禁言时长（1天/7天/30天/永久）+ TextArea 填写原因（必填）+ 确认按钮
- 加入黑名单：Modal 二次确认"确认将 [昵称] 加入黑名单？该用户将无法申请加入或通过邀请进入频道。" + TextArea 填写原因（必填）
- 批量操作：同待审队列模式，选中后激活批量按钮
- **批量接口契约**：同 3.5 节，采用单次批量请求，后端允许部分成功，返回逐条处理结果。响应格式：`{ code: 200, result: { success: number, failed: number, details: Array<{ memberId, success, errorMessage }> } }`
- 操作成功后刷新列表

**缓存失效**：
- 角色变更/移除/禁言/加黑名单操作成功后需使以下缓存失效：
  - `/channel/member/list`（成员列表）
  - `useChannelContext` 中的 `userRelation` 和 `memberRole`（若操作对象是当前用户）
  - `/channel/governance/blacklist/list`（黑名单列表，加黑名单操作时）

**状态与边界**：
- 空列表：展示空状态"暂无成员" + "邀请成员"按钮
- 权限不足：页面展示"您无权管理成员"
- 无法操作频道主：操作菜单中频道主行不展示操作选项，或操作时提示"无法对频道主执行此操作"
- 无法操作高权限成员：操作时提示"权限不足，无法操作该成员"

---

### 3.9 黑名单管理

**入口**：频道管理 → 黑名单

**布局与结构**：
```
页面顶部：标题"黑名单" + 统计数字
表格区：
  - 列：头像+昵称 | 拉黑时间 | 操作人 | 原因 | 状态 | 操作
  - 操作列：移出黑名单
```

**交互要求**：
- 移出黑名单：二次确认 Modal"确认将 [昵称] 移出黑名单？移出后该用户可按频道当前加入规则重新申请或加入。"
- 空列表：展示空状态"暂无黑名单用户"

---

### 3.10 治理操作日志

**入口**：频道管理 → 治理日志

**布局与结构**：
```
筛选区：操作类型（Select：全部/移除/禁言/解除禁言/加入黑名单/移出黑名单）+ 时间范围 + 操作者搜索
表格区：
  - 列：操作类型（Tag）| 操作者 | 目标用户 | 时间 | 原因 | 详情
  - 详情列：点击查看治理详情 Drawer
```

**交互要求**：
- 操作类型标签颜色：移除（红色）、禁言（橙色）、解除禁言（绿色）、加入黑名单（灰色）、移出黑名单（蓝色）
- 详情 Drawer：展示完整的治理记录，包含操作前后状态对比
- 支持导出（可选，P2）

---

## 4. 组件选型

基于项目 frontend-standards.md（Vue 3 + Ant Design Vue 4），以下为各功能模块的组件选型：

| 功能场景 | 推荐组件 | 路径 | 说明 |
|---------|---------|------|------|
| 隐私设置 Radio | Form + a-radio-group | `/@/components/Form/` | schema 驱动表单 |
| 加入方式配置 | Form + a-radio-group + a-switch + a-input-number | `/@/components/Form/` | 动态表单 |
| 邀请创建表单 | Drawer + Form | `/@/components/Drawer/` + `/@/components/Form/` | 侧边抽屉表单 |
| 申请加入表单 | Modal + Form | `/@/components/Modal/` | 弹窗表单 |
| 待审队列列表 | Table（基础表格） | `/@/components/Table/` | 简单列表展示 |
| 邀请列表 | Table | `/@/components/Table/` | 列表展示 + 操作 |
| 订阅列表 | CardList 或 Table | `/@/components/CardList/` 或 `/@/components/Table/` | 卡片形式更直观 |
| 成员列表 | JVxeTable | `/@/components/jeecg/JVxeTable/` | 支持行操作、批量操作、筛选 |
| 黑名单列表 | Table | `/@/components/Table/` | 简单列表展示 |
| 治理日志列表 | Table | `/@/components/Table/` | 筛选 + 列表 |
| 操作确认弹窗 | Modal | `/@/components/Modal/` | 危险操作二次确认 |
| 治理详情 | Drawer + Description | `/@/components/Drawer/` + `/@/components/Description/` | 侧边详情查看 |
| 订阅状态按钮 | Button（Primary/Default） | `/@/components/Button/` | 支持 loading 状态 |
| 角色标签 | a-tag（Ant Design Vue 内置） | - | 颜色区分角色 |
| 分组标签页 | a-tabs（Ant Design Vue 内置） | - | 分组筛选 |
| 提醒开关 | a-switch（Ant Design Vue 内置） | - | 开关控制 |
| 搜索 | Input.Search | `/@/components/` | 模糊搜索 |
| 空状态 | a-empty（Ant Design Vue 内置） | - | 统一空状态 |
| 消息提示 | useMessage | `/@/hooks/web/` | 操作反馈 |

**Hooks 使用**：
- `useTable`：成员列表、待审队列、黑名单、治理日志的表格逻辑
- `useForm`：申请表单、邀请创建表单、治理操作表单
- `useModal`：确认弹窗、结果展示弹窗
- `useDrawer`：邀请管理、治理详情
- `useMessage`：操作成功/失败消息提示
- `usePermission`：操作权限判断（隐藏/禁用无权操作）

**Store / Composable 使用**：
- `useUserStore`：获取当前用户信息和权限
- 新增 `useChannelContext(channelId)` composable（见 6.2 节）：按 channelId 隔离管理频道上下文、用户与频道关系、成员角色。**不使用全局 Pinia Store**，避免多频道切换时数据串台

---

## 5. API 对接

所有 API 通过 `defHttp` 封装调用，响应格式统一为 `{ code: 200, result: any, message: string, success: boolean }`。

### 5.1 订阅相关

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 订阅频道 | POST | `/channel/subscription/subscribe` | `{ channelId }` |
| 取消订阅 | POST | `/channel/subscription/unsubscribe` | `{ channelId }` |
| 查询订阅状态 | GET | `/channel/subscription/status/{channelId}` | 返回 `{ subscribed, source }` |
| 订阅列表 | GET | `/channel/subscription/list` | 支持分组筛选、搜索 |
| 创建分组 | POST | `/channel/subscription/group/create` | `{ name }` |
| 重命名分组 | POST | `/channel/subscription/group/rename` | `{ groupId, name }` |
| 删除分组 | POST | `/channel/subscription/group/delete` | `{ groupId }` |
| 分组列表 | GET | `/channel/subscription/group/list` | 返回分组列表 |
| 更新提醒设置 | PUT | `/channel/subscription/update-reminder` | `{ channelId, enabled }` |
| 移动频道到分组 | POST | `/channel/subscription/move-to-group` | `{ channelId, groupId }` |

### 5.2 成员与加入相关

> **memberId 语义说明**：`memberId` 为频道内的成员记录 ID（非用户 ID），需从成员列表接口获取。所有成员/治理相关接口统一携带 `channelId` 参数，前端无需额外查询 memberId 即可确定操作上下文。

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 提交加入申请 | POST | `/channel/member/join/apply` | `{ channelId, reason }` |
| 查询申请状态 | GET | `/channel/member/applications/status/{channelId}` | 返回 `{ status, canReapply }` |
| 待审列表 | GET | `/channel/member/applications/pending` | `{ channelId }`, 支持分页、筛选 |
| 批准申请 | POST | `/channel/member/applications/approve` | `{ channelId, applicationIds[] }` |
| 拒绝申请 | POST | `/channel/member/applications/reject` | `{ channelId, applicationIds[], reason }` |
| 成员列表 | GET | `/channel/member/list` | `{ channelId }`, 支持角色筛选、搜索、排序、分页 |
| 修改角色 | POST | `/channel/member/assign-role` | `{ channelId, memberId, role }` |
| 移除成员 | POST | `/channel/governance/remove` | `{ channelId, memberIds[], reason }` |
| 禁言成员 | POST | `/channel/governance/mute` | `{ channelId, memberId, duration, reason }` |
| 解除禁言 | POST | `/channel/governance/unmute` | `{ channelId, memberId }` |

### 5.3 黑名单相关

> **说明**：黑名单为频道级（非平台级），所有接口必须携带 `channelId`。

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 加入黑名单 | POST | `/channel/governance/blacklist/add` | `{ channelId, userId, reason }` |
| 移出黑名单 | POST | `/channel/governance/blacklist/remove` | `{ channelId, userId }` |
| 黑名单列表 | GET | `/channel/governance/blacklist/list` | `{ channelId }`, 支持分页 |

### 5.4 邀请相关

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 创建邀请 | POST | `/channel/invite/create` | `{ type, expireTime, maxUses }` |
| 邀请列表 | GET | `/channel/invite/list` | 支持分页 |
| 撤销邀请 | POST | `/channel/invite/revoke` | `{ inviteId }` |
| 使用邀请加入 | POST | `/channel/invite/use` | `{ inviteCode }` |

### 5.5 隐私与设置相关

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 更新隐私 | PUT | `/api/v1/channels/privacy` | `{ channelId, privacyType }` |
| 更新加入方式 | PUT | `/api/v1/channels/join-method` | `{ channelId, joinMethod, config }` |
| 治理日志 | GET | `/channel/governance/log` | 支持筛选、分页 |

---

## 6. 状态管理

### 6.1 现有 Store 复用

- `useUserStore`：获取当前用户 ID、角色、权限码，用于判断操作权限
- `usePermissionStore`：路由权限和按钮权限码，控制页面/按钮可见性

### 6.2 新增 Channel Context Composable（推荐方案）

> **架构说明**：不使用全局 Pinia Store 管理频道上下文，改用 composable 方案。原因：Pinia store 默认全局单例，而频道页面按 `channelId` 动态切换，单例 Store 会导致用户在频道 A 操作后切到频道 B 时数据串台。Composable 方案天然按 channelId 隔离上下文。

新增 `src/composables/useChannelContext.ts`，以 composable 形式管理频道上下文：

```typescript
// src/composables/useChannelContext.ts
export function useChannelContext(channelId: Ref<string>) {
  // 响应式频道上下文，天然随 channelId 变化，避免数据串台
  const channelInfo = ref<ChannelInfo | null>(null);
  const userRelation = ref<UserChannelRelation | null>(null);
  const privacyType = ref<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const joinMethod = ref<'FREE' | 'REVIEW' | 'INVITE'>('FREE');
  const isSubscribed = ref(false);
  const memberRole = ref<string | null>(null);
  const isMuted = ref(false);
  const isBlacklisted = ref(false);

  // 权限判断 computed
  const canManageMembers = computed(() => {
    const role = userRelation.value?.role;
    return role === 'OWNER' || role === 'ADMIN';
  });
  const canPublish = computed(() => {
    return memberRole.value && !isMuted.value && !isBlacklisted.value;
  });

  // 数据加载
  async function loadContext() {
    const [info, relation] = await Promise.all([
      getChannelInfo(channelId.value),
      getUserChannelRelation(channelId.value),
    ]);
    channelInfo.value = info;
    userRelation.value = relation;
    privacyType.value = info.privacyType;
    joinMethod.value = info.joinMethod;
    isSubscribed.value = relation.isSubscribed;
    memberRole.value = relation.role;
    isMuted.value = relation.isMuted;
    isBlacklisted.value = relation.isBlacklisted;
  }

  // 重置状态（频道切换时调用）
  function resetContext() {
    channelInfo.value = null;
    userRelation.value = null;
    isSubscribed.value = false;
    memberRole.value = null;
    isMuted.value = false;
    isBlacklisted.value = false;
  }

  return {
    channelInfo, userRelation, privacyType, joinMethod,
    isSubscribed, memberRole, isMuted, isBlacklisted,
    canManageMembers, canPublish,
    loadContext, resetContext,
  };
}
```

**使用方式**：在频道相关页面的根组件中调用 `useChannelContext`，通过 `provide/inject` 向子组件传递，确保同一频道内共享上下文、不同频道间天然隔离。

**路由守卫配合**：在频道路由的 `beforeRouteUpdate` 中调用 `resetContext()` + `loadContext()`，确保切换频道时数据正确刷新。

### 6.3 本地状态

- 表单数据、筛选条件、分页参数等使用组件内 `ref`/`reactive` 管理
- 弹窗/抽屉的开关状态通过 `useModal`/`useDrawer` 管理

---

## 7. 交互设计

### 7.1 通用交互规范

| 场景 | 交互方式 |
|------|---------|
| 数据加载 | 骨架屏（Skeleton）或 Spin |
| 操作成功 | 顶部消息提示（useMessage） |
| 操作失败 | 顶部错误消息 + 保留用户输入 |
| 危险操作（移除/黑名单/撤销邀请） | 二次确认 Modal，红色确认按钮 |
| 批量操作结果 | 结果 Modal，展示成功/失败数量和失败原因 |
| 防重复提交 | 提交按钮 loading + 禁用 |
| 空状态 | 插图 + 引导文案 + 下一步操作按钮 |
| 权限不足 | 页面级：居中提示文案；按钮级：隐藏或禁用 |

### 7.2 频道主页操作按钮状态机

```
[公开频道，未订阅] → 展示"订阅" → 点击 → [已订阅]
[已订阅] → 点击下拉"取消订阅" → 确认 → [未订阅]

[私有频道，非成员，未申请] → 展示"申请加入" → 点击 → 弹出申请表单 → 提交 → [待审核]
[待审核] → 展示"待审核"（禁用）

[私有频道，非成员，已拒绝] → 若允许再次申请 → 展示"重新申请"
[私有频道，非成员，冷却期] → 展示"冷却期剩余 X 天"（禁用）
[私有频道，非成员，被黑名单] → 展示"您无法加入此频道"

[私有频道，已加入，未订阅] → 展示"订阅"
[私有频道，已加入，已订阅] → 展示"已订阅"
```

### 7.3 关键页面信息层级

**频道设置页**：
1. 第一层级：隐私状态（Radio.Group）
2. 第二层级：加入方式（Radio.Group + 动态配置项）
3. 第三层级：邀请管理（仅邀请加入方式时展示）

**成员管理页**：
1. 第一层级：统计数字 + 搜索框 + 角色筛选
2. 第二层级：成员列表表格（角色标签、治理状态一目了然）
3. 第三层级：操作菜单（下拉，按需展开）

**订阅列表页**：
1. 第一层级：搜索框 + 分组标签页
2. 第二层级：订阅频道列表（卡片形式）
3. 第三层级：每张卡片的操作（提醒开关、取消订阅）

---

## 8. 响应式设计

### 8.1 通用规则

- 断点：移动端 < 768px，桌面端 >= 768px
- 移动端优先保证核心操作可见

### 8.2 各页面响应式策略

| 页面 | 桌面端布局 | 移动端适配 |
|------|-----------|-----------|
| 频道设置页 | 左右分栏 Form | 全宽单列堆叠，Radio.Group 纵向排列 |
| 频道主页 | 横向布局（频道信息 + 操作区） | 纵向堆叠，操作按钮固定底部或跟随内容区 |
| 待审队列 | 完整 Table | 表格转为卡片列表，操作按钮收进卡片底部 |
| 订阅列表 | 卡片网格（2-3 列） | 单列卡片列表，搜索框固定顶部 |
| 成员管理 | 完整 Table + 筛选区 | 筛选区折叠为"筛选"按钮触发 Drawer；表格转为卡片列表，操作改为长按/滑动菜单 |
| 黑名单 | 完整 Table | 卡片列表 |
| 治理日志 | 完整 Table + 筛选区 | 筛选区折叠；表格转为卡片列表 |
| 邀请管理 | 完整 Table | 卡片列表，复制按钮常驻 |

### 8.3 移动端特殊处理

- 批量操作：降级为多选列表 + 底部固定操作栏
- 操作按钮：保持最小触控区域 44x44px
- 弹窗/抽屉：移动端 Drawer 从底部弹出（占屏幕 80% 高度）
- 表单：移动端表单字段全宽，TextArea 自动撑高

---

## 9. 性能要求

| 场景 | 指标 | 目标值 |
|------|------|-------|
| 页面首屏加载 | 频道主页、设置页、成员管理页 | < 2s（WiFi） |
| 核心操作响应 | 订阅、取消订阅、申请加入、审核、角色变更 | P95 <= 500ms |
| 列表加载 | 成员列表、订阅列表、待审队列 | < 1s（分页 20 条） |
| 批量操作 | 批量审核、批量移除 | 单次批量请求，提交后展示 loading Modal，完成后展示逐条结果（成功/失败+原因） |
| 搜索响应 | 成员搜索、订阅搜索 | 防抖 300ms，< 500ms 响应 |

**前端优化措施**：
- 列表分页加载，避免一次性拉取全量数据
- 搜索输入 300ms 防抖
- `useChannelContext` composable 缓存用户关系数据，减少重复请求
- 批量操作结果异步返回，避免长时间等待

---

## 10. 测试要点

### 10.1 功能测试

| 测试场景 | 验证点 |
|---------|-------|
| 隐私设置 - 公开转私有 | 影响提示弹窗展示正确，保存后新访问按私有规则处理 |
| 隐私设置 - 系统频道 | Radio 禁用，提示"系统频道必须公开" |
| 加入方式 - 自由加入 | 用户点击加入后立即成为成员 |
| 加入方式 - 审核加入 | 申请提交成功，待审列表展示正确 |
| 加入方式 - 邀请加入 | 有效邀请可加入，过期/撤销/用完邀请被拒绝 |
| 订阅/取消订阅 | 按钮状态正确切换，信息流加权可验证 |
| 私有频道订阅引导 | 非成员点击订阅时引导至申请加入 |
| 待审队列 - 超时高亮 | 48 小时未处理申请行高亮展示 |
| 批量审核 | 每条结果展示成功/失败，失败项有原因 |
| 成员列表 - 角色筛选 | 按角色筛选结果正确 |
| 角色分配 | 变更后立即生效，操作记录完整 |
| 移除成员 | 冷却期提示正确，7 天内无法重新加入 |
| 禁言 | 禁言期间发布被拦截，到期自动解封 |
| 黑名单 | 黑名单用户无法加入，优先级高于邀请 |
| 权限控制 | 无权限用户不可见操作按钮或被拒绝访问 |

### 10.2 交互测试

| 测试场景 | 验证点 |
|---------|-------|
| 加载状态 | 所有数据请求展示 Skeleton/Spin |
| 空状态 | 各列表空状态展示引导文案和操作入口 |
| 错误反馈 | 操作失败展示错误消息，保留用户输入 |
| 防重复提交 | 提交按钮 loading + 禁用 |
| 危险操作确认 | 移除/黑名单/撤销等操作需二次确认 |
| 响应式 | 移动端核心操作可见，表格转卡片，操作不溢出 |

### 10.3 权限测试

| 测试场景 | 验证点 |
|---------|-------|
| 普通成员访问设置页 | 拒绝访问，提示权限不足 |
| 管理员修改频道主角色 | 操作被拒绝 |
| 内容编辑访问成员管理 | 无操作按钮 |
| 非成员访问私有频道 | 展示基础信息 + 加入方式，不展示受限内容 |

### 10.4 边界测试

| 测试场景 | 验证点 |
|---------|-------|
| 同一用户重复申请 | 提示"已有一个待审核申请" |
| 冷却期内重新加入 | 提示冷却期剩余时间 |
| 黑名单用户使用邀请 | 拒绝加入，提示"您无法加入此频道" |
| 批量操作部分失败 | 结果 Modal 正确展示成功/失败 |
| 长文本输入 | 申请理由、拒绝原因截断或滚动正常 |

---

## 11. 待确认问题 / 默认假设

| 问题 | 默认假设 |
|------|---------|
| 频道主页的具体入口和路由？ | 假设已有 `/channel/:id` 路由，频道设置页为 `/channel/:id/settings`，成员管理为 `/channel/:id/members` |
| 订阅列表页面的入口位置？ | 假设在个人中心或频道模块导航中有"我的订阅"入口 |
| 成员管理页、黑名单页、治理日志页是否为同一页面的不同标签页？ | 假设为频道管理下的子页面，通过左侧菜单或顶部标签页切换 |
| 通知系统的具体接口？ | 假设通知由后端异步处理，前端仅展示操作结果 |
| 邀请链接的格式和域名？ | 假设由后端生成完整链接，前端仅展示和复制 |
| 默认关注的系统频道清单从哪获取？ | 假设由后端接口返回，前端按接口数据展示"系统推荐"标签 |
| 治理日志是否需要导出功能？ | 本期假设为 P2，不优先实现 |
| 私有频道分享链接的行为？ | 假设允许分享，落地页展示基础信息 + 加入方式 |
| 被拒绝后再次申请的间隔时间配置入口？ | 假设在频道设置页的"审核加入"配置项中 |
