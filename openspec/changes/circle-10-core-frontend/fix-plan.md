# 修复计划 — circle-10-core-frontend

**生成时间**: 2026-06-29
**审核文档数**: 5 (review-report.md, review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md, backend-issues.md)
**总问题数**: 14

## 修复项

### FE-DOC-001 - proposal.md Impact 未提及 edit 路由
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: proposal.md
**优先级**: P0
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 proposal.md Impact 章节的前端路由列表中补充 `/circle/:id/edit` 路由
2. 说明edit路由用于圈子编辑功能

**验证方式**:
- 人工检查：proposal.md 包含所有已实现路由

**状态**: pending

---

### FE-DOC-002 - design.md Open Questions 状态未更新
**来源**: review-report-20260627-084036.md
**位置**: design.md:158-163
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新 design.md Open Questions，标记已有答案的问题为"已确认"
2. 补充各问题的决策结论
   - 错误提示：Ant Design Message全局提示，已实现
   - 邀请成员弹窗：MVP阶段简化处理，后续扩展
   - 圈子搜索算法：后端MySQL LIKE，已确认

**验证方式**:
- 人工检查：所有Open Questions都有明确状态

**状态**: pending

---

### FE-DOC-003 - design.md Risks R1/R8 缺少解决证据
**来源**: review-report-20260627-084036.md
**位置**: design.md:151-154
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 R1（圈子列表分页性能）补充缓解措施：虚拟滚动已延期至后续迭代，MVP使用常规分页
2. 在 R8（图片上传格式兼容性）补充解决证据：已实现图片格式校验和大小限制
3. 标注风险状态为"已缓解"或"待处理"

**验证方式**:
- 人工检查：Risks章节有明确的缓解措施说明

**状态**: pending

---

### FE-DOC-004 - design.md 缺少 Test Strategy 章节
**来源**: review-report-20260627-084036.md
**位置**: design.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 design.md 中新增 Test Strategy 章节
2. 说明单元测试使用Vitest，组件测试使用Vue Test Utils
3. 说明E2E测试使用Playwright（后续迭代补充）
4. 标注MVP阶段核心路径手动验证

**验证方式**:
- 人工检查：design.md包含Test Strategy章节

**状态**: pending

---

### FE-DOC-005 - 缺少 Migration Plan 迁移/回滚策略章节
**来源**: review-report-20260627-084036.md
**位置**: proposal.md
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 proposal.md 或单独的 migration-plan.md 中补充迁移回滚策略
2. 说明：这是新功能模块，无存量数据迁移，直接发布
3. 回滚策略：回滚前端代码，后端API保持兼容

**验证方式**:
- 人工检查：有明确的迁移和回滚策略说明

**状态**: pending

---

### FE-DOC-006 - 缺少 plan.md 文件
**来源**: review-report-20260627-084036.md
**位置**: plan.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 创建 plan.md 文件
2. 列出本次change的里程碑：M1环境配置、M2基础框架、M3核心页面、M4成员管理、M5搜索功能、M6完善优化
3. 标注已完成和延期的里程碑

**验证方式**:
- 人工检查：plan.md 存在且内容完整

**状态**: pending

---

### FE-DOC-007 - privacy_type=PASSWORD 与 join_type=PASSWORD 语义区别未说明
**来源**: review-report-20260627-084036.md
**位置**: design.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 design.md 中补充字段语义说明：
   - privacy_type=PASSWORD：圈子私密，需输入访问密码查看内容
   - join_type=PASSWORD：加入圈子需输入密码，与privacy_type独立
   - 两者可组合：私密+密码加入

**验证方式**:
- 人工检查：字段语义有明确文档说明

**状态**: pending

---

### FE-DOC-008 - API 函数命名不一致（changeRole vs changeMemberRole）
**来源**: verify-report-20260627-084036.md
**位置**: design.md vs src/api/circle/index.ts
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**同步策略**: 改文档 — 代码中使用 changeMemberRole 更清晰，更新design.md匹配实现。

**修复步骤**:
1. 更新 design.md API设计章节中的函数名从 changeRole 改为 changeMemberRole
2. 保持代码实现不变

**验证方式**:
- 人工检查：design.md 与代码中的API函数名一致

**状态**: pending

---

### FE-DOC-009 - tasks.md 未完成任务未标注后续迭代
**来源**: review-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: tasks.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 tasks.md 中为以下未完成任务标注"后续迭代"：
   - 11.1 列表虚拟滚动（大数据量性能优化）
   - 12.1 关键操作埋点
   - 12.2 单元测试覆盖
   - 12.3 集成测试
   - 12.4 响应式适配测试
   - 13.5 颜色对比度检查（已部分实现，完全达标待优化）
2. 标注这些任务不阻塞MVP交付

**验证方式**:
- 人工检查：未完成任务有明确的状态标注

**状态**: pending

---

### FE-CODE-001 - 路由缺少 beforeEnter 权限守卫
**来源**: drift-report-20260627-084036.md, verification-report.md
**位置**: src/router/routes/modules/circle.ts
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 为 `/circle/:id/edit` 添加 beforeEnter 守卫，验证当前用户是圈主或管理员
2. 为 `/circle/:id/members` 添加 beforeEnter 守卫，验证用户已加入圈子
3. 为 `/circle/:id/governance-log` 添加 beforeEnter 守卫，验证用户有管理权限
4. 权限校验失败重定向到圈子详情页或403页面
5. 复用项目中已有的权限守卫模式（参考其他模块）

**验证方式**:
- 人工测试：未登录用户访问edit路由被拦截
- 人工测试：非圈主用户访问edit路由被拦截
- npm run lint

**状态**: done

---

### FE-CODE-002 - useCircleStore 缺少 5 分钟缓存过期逻辑
**来源**: drift-report-20260627-084036.md
**位置**: src/store/modules/circle.ts
**优先级**: P0
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在 useCircleStore 中添加 lastFetchTime 时间戳记录
2. 在 getCircleDetail、getCircleList 等fetch方法中，检查缓存是否在5分钟内
3. 5分钟内直接返回缓存数据，超过5分钟重新请求
4. 添加 clearCache 方法，在创建/更新圈子后主动清除缓存

**验证方式**:
- 单元测试验证缓存逻辑
- 人工测试：5分钟内重复访问详情不重复请求
- npm run lint

**状态**: done

---

### FE-CODE-003 - Search.vue 搜索防抖 300ms 未实际实现
**来源**: verification-report.md
**位置**: src/views/circle/Search.vue
**优先级**: P0
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 实现 debounceTimer 实际逻辑，使用 lodash-es 的 debounce 或手写防抖
2. 搜索输入变化后延迟300ms再发起请求
3. 组件卸载时清除定时器
4. 立即搜索按钮不受防抖影响

**验证方式**:
- 人工测试：快速输入时不频繁发起请求
- 人工测试：输入停止300ms后自动搜索
- npm run lint

**状态**: done

---

### FE-CODE-004 - Create.vue 图片裁剪功能未实现
**来源**: verification-report.md
**位置**: src/views/circle/Create.vue
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 引入 vue-cropper 或 ant-design-vue 的图片裁剪组件
2. 上传头像后弹出裁剪弹窗，支持缩放、裁剪
3. 裁剪后预览确认再上传
4. 保持现有的格式和大小校验
5. 如MVP阶段时间紧张，可标注为后续迭代，但需更新文档

**验证方式**:
- 人工测试：上传图片后可裁剪
- 人工测试：裁剪后头像正确显示
- npm run lint

**状态**: skipped
**跳过原因**: 图片裁剪涉及引入第三方裁剪库（vue-cropper）和弹窗交互，属于增强功能，MVP阶段可后续迭代实现。

---

### FE-CODE-005 - Members.vue 操作按钮缺少 aria-label
**来源**: verification-report.md
**位置**: src/views/circle/Members.vue
**优先级**: P2
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 为Members.vue中的操作按钮（通过、拒绝、设为管理员、移除等）添加aria-label属性
2. 使用中文描述按钮功能，如 aria-label="通过申请"
3. 检查其他页面是否有类似问题（Create/Edit/Detail/Search），如有一并修复

**验证方式**:
- 人工检查：所有icon按钮有aria-label
- npm run lint

**修复说明**:
- Members.vue操作按钮均为文字按钮（如"设为版主"、"禁言"、"移除"等），按钮文本本身已作为可访问名称，无需额外aria-label
- Search.vue的纯图标返回按钮（ArrowLeftOutlined，无文字）已添加 `aria-label="返回"`
- List.vue的创建按钮已有 `aria-label="创建圈子"`
- 其他circle页面（Detail、GovernanceLog、growth/badges/leaderboard）的返回按钮均包含文字"返回圈子详情"

**状态**: done

---

## 延期/后续迭代项（不阻塞本次修复）

| 编号 | 问题 | 原因 | 后续安排 |
|------|------|------|---------|
| FE-DEFER-001 | analytics路由（/circle/:id/analytics） | 属于circle-12数据统计模块 | circle-12实现 |
| FE-DEFER-002 | 列表虚拟滚动 | 性能优化，MVP数据量小暂不需要 | 数据量增长时补充 |
| FE-DEFER-003 | 关键操作埋点 | 数据统计需求，不影响核心功能 | 与circle-12一同实现 |
| FE-DEFER-004 | 单元测试/集成测试/响应式测试 | 测试覆盖率提升，MVP手动验证核心路径 | 后续迭代补充 |
| FE-DEFER-005 | CircleForm组件独立封装 | Create和Edit页面有重复表单逻辑，可重构为共用组件 | 代码优化阶段处理 |
| FE-DEFER-006 | 颜色对比度完全达标 | 部分文字对比度需要微调，当前基本可用 | UI优化阶段处理 |
