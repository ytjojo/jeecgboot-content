# FixPlan: circle-13-growth-incentive-frontend (前端)

> **生成时间**: 2026-06-30
> **来源**: review-report.md、PENDING-ISSUES.md、circle-growth-api-conventions-audit-2026-06-25.md
> **说明**: 前端代码已合并到主分支(springboot3_content)，BLOCK级问题已在之前修复中处理，本plan聚焦文档完善和FLAG级改进项

---

## 修复总览

| 优先级 | 数量 |
|--------|------|
| BLOCK | 0（已修复） |
| P1 | 5 |
| P2 | 8 |

---

### FE-001 - 确认proposal.md API路径修正状态
**来源**: review-report.md:71-83,637
**位置**: proposal.md:11
**优先级**: P1
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 验证proposal.md第11行的API路径是否已修正为：
   - 成员成长：`GET /api/v1/content/circle/member_growth/info?circleId=&userId=`
   - 成就徽章：`GET /api/v1/content/circle/growth/achievement/list?circleId=&userId=`
   - 排行榜：`GET /api/v1/content/circle/growth/leaderboard?circleId=&dimension=&period=&currentUserId=`
2. 确认是否已补充连续参与接口：`GET /api/v1/content/circle/member_growth/participation?circleId=&userId=`
3. 如未修正则执行修正
**验证方式**:
- proposal.md中所有API路径与design.md、后端实际接口一致

---

### FE-002 - 确认等级降级规则修正状态
**来源**: review-report.md:116-129,638
**位置**: specs/member-growth/spec.md:95-97
**优先级**: P1
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 验证specs/member-growth/spec.md中"等级下降展示"Scenario是否已替换为"经验值扣减但等级不下降"Scenario：
   ```
   #### Scenario: 经验值扣减但等级不下降
   - **WHEN** 经验值因内容删除/撤回/违规被扣减至低于当前等级门槛
   - **THEN** 成员等级保持不变，仅经验值数值和进度条更新，页面提示「经验值已调整」
   ```
2. 如未修正则执行修正
**验证方式**:
- 前端spec中等级规则与后端一致（不降级）
- 前后端无业务规则矛盾

---

### FE-003 - 修正排行榜参数名 type/range → dimension/period
**来源**: review-report.md:612-619（FLAG-014，澄清新增）
**位置**: specs/leaderboard/spec.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 检查specs/leaderboard/spec.md中参数名是否为`type`/`range`
2. 将`type`统一改为`dimension`（维度：EXP/CONTRIBUTION/POST，大写枚举值）
3. 将`range`统一改为`period`（周期：WEEK/MONTH/ALL，大写枚举值）
4. 确认所有Scenario中参数名与后端VO字段名、design.md一致
**验证方式**:
- 排行榜接口参数名使用dimension/period
- 与后端Controller接收参数名一致

---

### FE-004 - 补充WebSocket通知容错和降级方案
**来源**: review-report.md:162-167,311-316（FLAG-003、FLAG-011）
**位置**: design.md D4、specs/badge-system/spec.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 在design.md或badge-system spec中补充WebSocket容错Scenario：
   - WHEN WebSocket消息体缺少circleId或通知类型字段，THEN 仅展示通用Toast不触发数据刷新
2. 补充WebSocket不可用时的降级策略：
   - WebSocket连接失败时降级为每60秒轮询一次成长数据，或
   - 在页面顶部显示「实时通知未连接」提示
3. 补充等级提升通知的WebSocket Scenario到member-growth spec和circle-level spec
**验证方式**:
- 消息格式异常时有明确降级行为
- WebSocket断开时用户有感知或有轮询兜底
- 等级提升和徽章获得都有对应通知Scenario

---

### FE-005 - 明确streakDetail和totalBadges字段降级方案
**来源**: review-report.md:169-183,501-515（FLAG-004、FLAG-005、FLAG-C001、FLAG-C002）
**位置**: design.md D9、specs/member-growth/spec.md、specs/badge-system/spec.md
**优先级**: P1
**依赖**: 后端确认是否扩展字段
**类型**: 文档修复
**修复步骤**:
1. 明确streakDetail（7天每日明细）降级方案：
   - 后端扩展前，连续参与时间轴仅展示连续天数数字，不渲染7天每日圆点
   - 在spec中补充此降级行为Scenario
2. 明确totalBadges/totalBadgeCount降级方案：
   - 后端未补充字段前，通过徽章列表长度计算已获得徽章总数
   - 未获得徽章总数不单独展示或通过配置数据计算
   - 在badge-system spec中补充此降级行为
3. 与后端确认是否计划在本期补充这两个字段，若后端补充则前端对接真实字段
**验证方式**:
- 字段缺失时UI有降级展示，不出现空白或报错
- spec中明确定义了降级行为

---

### FE-006 - 补充tasks.md DoD标准收尾项
**来源**: review-report.md:85-90,398-403（FLAG-001、FLAG-013）
**位置**: tasks.md
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 在tasks.md末尾补充第9分组「9. 质量门禁与收尾」
2. 包含AGENTS.md要求的DoD收尾项：
   - [ ] 流程确认 — subagent + TDD
   - [ ] Code Review
   - [ ] 覆盖率 ≥ 90%
   - [ ] 模块全量测试 100%
   - [ ] 合并 + 验证 + 清理 worktree
**验证方式**:
- tasks.md包含完整的质量门禁收尾任务

---

### FE-007 - 补充徽章撤销状态UI细节
**来源**: review-report.md:200-205（FLAG-006）
**位置**: specs/badge-system/spec.md:55-69
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 在badge-system spec中补充"已撤销"标签细节：
   - 标签位置：徽章卡片右上角
   - 标签样式：灰色文字带删除线
   - 交互：不可点击查看详情
2. 明确徽章总数计算规则：撤销徽章不计入已获得总数
**验证方式**:
- 撤销徽章的UI展示有明确规范
- 开发和测试对撤销状态理解一致

---

### FE-008 - 补充排行榜更新时间提示
**来源**: review-report.md:214-219（FLAG-008）
**位置**: specs/leaderboard/spec.md
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 在leaderboard spec中补充Scenario：
   ```
   #### Scenario: 榜单更新时间提示
   - **WHEN** 用户查看排行榜
   - **THEN** 页面底部展示「榜单每小时更新一次，上次更新时间：{time}」提示
   ```
**验证方式**:
- 用户知道排行榜不是实时更新
- 有上次更新时间展示

---

### FE-009 - 补充资源不存在和网络异常边界场景
**来源**: review-report.md:297-309（FLAG-009、FLAG-010）
**位置**: 所有specs
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 在各页面spec中补充圈子已解散/成员已退出场景：
   - WHEN 圈子已解散或用户已不在圈子中，THEN 展示「圈子已不存在」提示并提供返回圈子列表入口
2. 补充网络异常差异化提示：
   - 网络超时：显示"请求超时，请检查网络"
   - 服务端错误：显示"服务暂时不可用，请稍后重试"
   - 断网：显示"网络连接已断开，请检查网络设置"
3. 确保重试按钮在各种错误场景下可用
**验证方式**:
- 资源不存在时有明确提示和返回入口
- 不同网络错误有差异化文案
- 不出现笼统的"加载失败"

---

### FE-010 - 明确XSS防护策略
**来源**: review-report.md:334-339（FLAG-012）
**位置**: design.md
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 在design.md中补充安全规范：
   - 所有后端返回的文本字段使用Vue默认模板插值渲染（自动转义）
   - 禁止使用v-html渲染用户生成内容（徽章名称、描述、用户名等）
   - 如确需渲染富文本，必须使用DOMPurify等库进行XSS过滤
**验证方式**:
- design.md中有明确的XSS防护规范
- 开发时遵循此规范

---

### FE-011 - 同步design.md上下文信息与后端对齐
**来源**: circle-growth-api-conventions-audit-2026-06-25.md
**位置**: design.md Context章节
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 确认design.md中三套成长体系划分正确：
   - 全局内容社区用户成长（EPIC-03）：/api/v1/content/user/growth/，无circleId
   - 圈子等级（EPIC-13）：/api/v1/content/circle/growth/level/，有circleId
   - 圈子内成员成长（EPIC-13）：/api/v1/content/circle/member_growth/，有circleId+userId
2. 明确所有枚举值为大写字符串（EXP/CONTRIBUTION/POST、WEEK/MONTH/ALL）
3. 确认VO字段直接复用后端字段名，不做重命名映射
**验证方式**:
- 三套体系路径划分清晰
- 枚举值大小写明确
- 前后端字段名一致无歧义

---

### FE-012 - 验证与后端实际字段对齐
**来源**: circle-growth-api-conventions-audit-2026-06-25.md:95-116
**位置**: design.md VO字段映射
**优先级**: P2
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 验证前端TypeScript类型定义与后端VO实际字段对齐：
   - expPoints（非experience）
   - contributionPoints（非contribution）
   - nextLevelThreshold（非nextLevelExp/nextLevelScore）
   - participationDays（非streakDays）
   - rankNum（非rank）
   - username（非userName）
   - avatar（非userAvatar）
   - 徽章status: EARNED/CLOSE/UNEARNED（非earned+nearComplete布尔值）
   - 排行榜返回List<LeaderboardEntryVO>（无包装对象）
2. 如发现不一致则修正前端类型定义或文档
**验证方式**:
- 前端字段名与后端VO完全一致
- 无错误的字段重命名
- 排行榜返回结构与实际一致

---

## 前后端协同待确认项

| 编号 | 问题 | 确认方 | 影响 |
|------|------|--------|------|
| DEP-1 | `/participation`接口是否补充`streakDetail: boolean[]`字段 | 后端 | 7天时间轴实现方式 |
| DEP-2 | MemberGrowthVO是否补充`totalBadges`/`totalBadgeCount`字段 | 后端 | 徽章总数展示方式 |
| DEP-3 | SOCIAL_BUTTERFLY徽章是否在本期实现（需邀请记录表circle_id） | 产品+后端 | 徽章种类数量 |
| DEP-4 | WebSocket通知消息体具体格式 | 后端 | 通知监听实现 |
