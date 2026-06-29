# 修复计划 — channel-21-privacy-membership-frontend

**生成时间**: 2026-06-30
**审核文档数**: 4 (backend-issues/review-report/verification-review/verify)
**总问题数**: 11

---

## 修复项

### FE-001 - 后端API路径重构后前端API调用路径需同步更新

**来源**: BE-002（后端路径重构）
**位置**: src/api/content/channel/ 所有API文件
**优先级**: BLOCK
**依赖**: BE-002（后端API路径重构完成）
**类型**: 代码修复-前端

**修复步骤**:
1. 待后端完成API路径重构（BE-002）后，同步更新所有前端API调用路径：
   - 旧路径：`/api/v1/content/channel/member/*` → 新路径：`/api/v1/content/channels/{channelId}/members/*`
   - 旧路径：`/api/v1/content/channel/subscription/*` → 新路径：`/api/v1/content/channels/{channelId}/subscriptions/*`
   - 旧路径：`/api/v1/content/channel/governance/*` → 新路径：`/api/v1/content/channels/{channelId}/governance/*`
   - 旧路径：`/api/v1/content/channel/invite/*` → 新路径：`/api/v1/content/channels/{channelId}/invites/*`
2. 所有路径参数从@RequestParam改为@PathVariable传递，channelId放入URL路径中
3. 同步更新HTTP方法：查询接口改为GET，写操作保持POST
4. 注意：ChannelController的隐私设置和加入方式更新路径保持为`/api/v1/content/channels/privacy`和`/api/v1/content/channels/join-method`（无channelId路径参数，从请求体获取）
5. 更新useChannelContext中的API调用路径

**验证方式**:
- 所有API路径使用复数资源风格，与后端重构后一致
- 联调时无404错误
- 所有组件单元测试更新后通过
- E2E测试验证核心流程（订阅、加入、管理）正常

**状态**: pending

---

### FE-002 - 邀请管理列表页spec缺失

**来源**: review-report.md FLAG-1
**位置**: specs/channel-privacy-settings/spec.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在channel-privacy-settings capability中补充邀请管理列表页的Scenario：
   - **Scenario**: 查看邀请列表
   - Given 频道主/管理员进入邀请管理页面
   - When 页面加载
   - Then 展示所有邀请记录，包含邀请码/链接、创建时间、创建者、状态（有效/已用完/已撤销/已过期）、已使用次数
   - And 支持按状态筛选、按创建时间排序
   - **Scenario**: 撤销邀请
   - Given 存在一个有效邀请
   - When 管理员点击撤销并确认
   - Then 邀请状态变为已撤销，该邀请码/链接不可再使用
   - **Scenario**: 复制邀请链接/邀请码
   - Given 存在一个有效邀请
   - When 点击复制按钮
   - Then 邀请码或邀请链接复制到剪贴板，显示成功提示
2. 可以考虑独立出channel-invite-management spec，但本期可补充到channel-privacy-settings中
3. 如果前端已有邀请管理页面实现，确保spec与实现一致

**验证方式**:
- spec文档包含邀请列表的完整交互场景
- 若实现邀请管理页，场景与实现一致
- 邀请管理功能验收时有据可依

**状态**: pending

---

### FE-003 - spec scenario缺少量化验收标准

**来源**: review-report.md FLAG-5
**位置**: specs/ 所有scenario
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 为关键交互场景补充量化验收标准：
   - 订阅按钮点击后"立即"变为已订阅状态 → 明确为< 200ms（乐观更新）
   - 搜索成员"实时过滤" → 明确防抖时间300ms（与PRD一致）
   - 页面首屏加载"快" → 明确首屏加载 < 2s（来自proposal.md）
   - 列表翻页响应"快" → 明确< 500ms
2. 在spec文档开头或design.md中统一引用PRD中的性能指标
3. 为状态转换添加明确的时间阈值，避免模糊描述

**验证方式**:
- 关键scenario有可量化的验收标准
- 性能指标与PRD NFR章节一致
- 测试时可基于量化标准编写断言

**状态**: pending

---

### FE-004 - 交互组件离线态处理不完整

**来源**: review-report.md FLAG-8
**位置**: specs/channel-subscription/spec.md, specs/channel-join-application/spec.md 等
**优先级**: P2
**依赖**: 无
**类型**: 文档修复+代码补充

**修复步骤**:
1. 在SubscribeButton spec中补充离线态Scenario：
   - **Scenario**: 离线状态点击订阅按钮
   - Given 用户网络离线（navigator.onLine === false）
   - When 用户点击订阅/取消订阅按钮
   - Then 显示"当前网络不可用，请检查网络连接"提示
   - And 不发送API请求
   - And 按钮状态不变
2. 在JoinApplyModal spec中补充离线态Scenario：
   - 离线时提交申请显示网络错误提示，不丢失表单内容
3. 在其他关键交互组件（MuteModal、MemberList批量操作等）中评估是否需要补充离线态处理
4. 实现层面可复用useChannelContext中的网络状态检测，统一错误提示

**验证方式**:
- 离线状态下点击交互按钮显示友好提示
- 不发送无效API请求
- 网络恢复后可正常操作
- 相关单元测试覆盖离线场景

**状态**: pending

---

### FE-005 - design.md中5个Open Questions未闭环

**来源**: review-report.md ADVISORY-1
**位置**: design.md Open Questions章节
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 闭环以下Open Questions：
   - **Q1: 频道主页的具体入口和路由** → 根据EPIC-23（发现）和实际路由配置，明确路由为`/content/channels/:id`
   - **Q2: 订阅列表页面的入口位置** → 明确在个人中心"我的订阅"菜单，路由为`/content/subscriptions`
   - **Q3: 成员管理页的页面组织方式** → 明确在频道详情页内使用标签页（成员/治理/设置），不单独全屏页面
   - **Q4: 通知系统接口** → 本期使用站内消息，通知服务Mock实现（见后端BE-019）
   - **Q5: 邀请链接格式** → 明确格式为`/content/channels/join?code={inviteCode}`或`/content/invite/{inviteCode}`
2. 在design.md中更新Open Questions章节，给出明确答案或标记为后续迭代决策
3. 同步更新到相关spec的描述中

**验证方式**:
- Open Questions章节所有问题有明确答案或决策标记
- 路由和页面组织方式与实际实现一致
- 设计决策有记录可追溯

**状态**: pending

---

### FE-006 - plan.md API路径需与后端重构后的最终路径对齐

**来源**: BE-002
**位置**: plan.md, src/api/content/channel/
**优先级**: ADVISORY
**依赖**: BE-002, FE-001
**类型**: 文档修复

**修复步骤**:
1. 待后端API路径最终确定后，同步更新plan.md中的所有API路径示例
2. 确保channelPrivacy.ts、channelMember.ts、channelSubscription.ts等API文件中的路径与后端一致
3. 注意HTTP方法的同步（查询用GET，写操作用POST）
4. 路径参数和请求体格式与后端VO/DTO对齐

**验证方式**:
- plan.md中的代码示例与实际实现一致
- 新人按plan.md可以正确实现API调用
- 无过时的路径或HTTP方法

**状态**: pending

---

### FE-007 - 错误码处理策略需补充

**来源**: verification-review.md
**位置**: plan.md, src/api/content/channel/
**优先级**: ADVISORY
**依赖**: BE-005（后端错误码定义）
**类型**: 文档修复+代码补充

**修复步骤**:
1. 在plan.md中补充错误码处理策略：
   - 400 Bad Request: 显示具体参数错误提示（如申请理由字数不符）
   - 401 Unauthorized: 跳转登录页
   - 403 Forbidden: 显示"您没有权限执行此操作"
   - 404 Not Found: 显示"频道不存在"或"内容不存在"
   - 409 Conflict: 显示冲突原因（如"已订阅"、"申请已提交"、"在冷却期内"）
   - 429 Too Many Requests: 显示"操作过于频繁，请稍后再试"
   - 500 Internal Server Error: 显示"服务器错误，请稍后重试"
2. 在API封装层统一处理错误提示，或使用defHttp的错误拦截器
3. 确保每个业务场景的错误提示友好且可操作

**验证方式**:
- 各错误码有对应的用户提示文案
- 错误提示符合场景上下文（如冷却期提示剩余时间）
- 不暴露技术错误细节给用户
- 单元测试覆盖错误处理逻辑

**状态**: pending

---

### FE-008 - 分页参数契约统一

**来源**: verification-review.md
**位置**: plan.md, src/api/content/channel/
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 统一所有分页API的参数格式，使用MyBatis Plus约定：
   - pageNum: 页码，从1开始
   - pageSize: 每页条数，默认20
2. 在plan.md中明确分页参数命名和格式
3. 确保成员列表、待审队列、订阅列表、治理日志、黑名单列表等分页接口参数一致
4. 响应分页结构使用项目标准的IPage格式（records/total/size/current等）

**验证方式**:
- 所有分页API参数命名一致（pageNum/pageSize）
- 分页响应结构统一
- 分页组件可复用同一套数据结构

**状态**: pending

---

### FE-009 - proposal.md中API数量表述需精确化

**来源**: review-report.md ADVISORY-3
**位置**: proposal.md
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 统计实际API数量（约28个，包含新增的关系查询、状态查询、日志列表等）
2. 更新proposal.md中"约25个API接口调用"的表述为实际数量
3. 可以按模块分类统计：成员管理x个、订阅管理x个、治理操作x个、邀请x个、设置x个

**验证方式**:
- proposal.md中API数量与plan.md中定义的API函数数量一致
- 无模糊的约数表述

**状态**: pending

---

### FE-010 - 集成测试场景纳入tasks.md或明确标记为E2E

**来源**: review-report.md ADVISORY-5
**位置**: design.md Test Strategy, tasks.md
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 对于design.md Test Strategy中列出的4个集成测试场景：
   - 场景1：完整加入流程（公开→自由加入→订阅→退出）
   - 场景2：完整审核流程（申请→待审→批准→成员→被移除→冷却期）
   - 场景3：完整治理流程（禁言→到期自动解禁→黑名单→移出）
   - 场景4：完整邀请流程（创建邀请→使用邀请加入→撤销邀请）
2. 在tasks.md中明确：
   - 这些集成测试场景是否包含在单元测试中
   - 若作为E2E测试，标记为后续迭代或单独的E2E任务
3. 避免遗漏重要流程测试

**验证方式**:
- Test Strategy中的测试场景在tasks.md中有对应实现或明确标记
- 核心业务流程有自动化测试覆盖
- 测试分层明确（单元/集成/E2E）

**状态**: pending

---

### FE-011 - SubscribeButton状态转换矩阵补充

**来源**: review-report.md ADVISORY-8
**位置**: specs/channel-subscription/spec.md
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在SubscribeButton spec中补充状态转换矩阵：
   | 当前状态 | 触发事件 | 下一个状态 | 条件 |
   |---------|---------|-----------|------|
   | idle | 已在黑名单 | blacklisted | - |
   | idle | 已被禁言 | muted | - |
   | idle | 已订阅 | subscribed | - |
   | idle | 申请待审核 | pending | 审核加入频道 |
   | idle | 在冷却期 | cooldown | 被移除后7天内 |
   | idle | 需要申请 | apply | 审核加入频道 |
   | idle | 可加入/订阅 | idle | 显示主按钮 |
   | ... | ... | ... | ... |
2. 明确6种状态的优先级顺序：blacklisted > muted > subscribed > pending > cooldown > apply > idle
3. 补充关键状态转换路径的Scenario，如：被移出黑名单→重新变为idle、禁言到期→恢复subscribed状态等

**验证方式**:
- 所有状态转换有明确定义
- 状态优先级清晰无冲突
- 边界情况（如同时在黑名单和冷却期）处理明确
- 组件实现可基于状态矩阵无歧义开发

**状态**: pending
