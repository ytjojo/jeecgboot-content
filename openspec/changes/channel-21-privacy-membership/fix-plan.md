# 修复计划 — channel-21-privacy-membership

**生成时间**: 2026-06-30
**审核文档数**: 4 (drift-report/review-report/verify-report/verify)
**总问题数**: 19

---

## 修复项

### BE-001 - assignRole接口无权限校验导致越权风险

**来源**: drift-report-20260627-084036.md
**位置**: controller/ChannelMemberController.java:74-79
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 在assignRole方法中添加权限校验逻辑：
   - 校验当前用户是否为频道主或管理员
   - 校验操作者角色级别高于被分配角色（普通成员不可分配管理员）
   - 不可将频道主角色分配给他人（频道主唯一）
2. 参考项目其他权限校验写法，使用@RequiresPermissions或在Biz层校验
3. 为removeMembers、muteMember、blacklist等所有治理操作补充权限校验
4. 补充ChannelMemberBizService中的权限校验方法，Controller不直接写业务逻辑

**验证方式**:
- 普通用户调用assignRole返回403权限不足
- 管理员不可将其他用户设为频道主
- 频道主可分配管理员/编辑/成员角色
- 所有治理操作API通过权限校验单元测试

**状态**: pending

---

### BE-002 - API路径使用单数+RPC风格，不符合RESTful规范和复数资源前缀

**来源**: drift-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: ChannelMemberController.java:25 及所有成员/治理Controller
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端+文档更新

**修复步骤**:
1. 统一Controller路径前缀从`/api/v1/content/channel/member`改为`/api/v1/content/channels/{channelId}/members`（复数资源）
2. 所有路径必须包含`{channelId}`作为路径参数，而非通过@RequestParam传递
3. 重构RPC风格路径为RESTful资源风格：
   - POST /join/free → POST /api/v1/content/channels/{channelId}/members (自由加入)
   - POST /join/apply → POST /api/v1/content/channels/{channelId}/members/applications (申请加入)
   - POST /leave → DELETE /api/v1/content/channels/{channelId}/members/me (退出)
   - POST /assign-role → POST /api/v1/content/channels/{channelId}/members/{memberId}/role (分配角色)
   - GET /list → GET /api/v1/content/channels/{channelId}/members (成员列表)
   - GET /applications/pending → GET /api/v1/content/channels/{channelId}/members/applications (待审列表)
   - POST /applications/approve → POST /api/v1/content/channels/{channelId}/members/applications/{appId}/approve (批准)
4. 更新其他Controller路径：
   - ChannelGovernanceController: `/api/v1/content/channels/{channelId}/governance/*`
   - ChannelSubscriptionController: `/api/v1/content/channels/{channelId}/subscriptions/*`
   - ChannelInviteController: `/api/v1/content/channels/{channelId}/invites/*`
5. 注意：必须使用`/api/v1/content/channels/`（复数）前缀，与EPIC-20 ChannelController保持一致
6. 同步更新design.md中的API契约表格和所有spec文件中的路径示例

**验证方式**:
- 所有API路径符合复数资源层级风格：/api/v1/content/channels/{channelId}/...
- channelId通过@PathVariable获取，不再通过@RequestParam传递
- 前端API调用路径同步更新后联调通过
- 所有Controller单元测试更新后通过

**状态**: pending

---

### BE-003 - Controller直接返回Entity而非VO，违反架构规范

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: ChannelMemberController.java:83 listMembers, searchMembers
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 创建MemberVO、MemberListVO、SubscriptionVO等VO类，只暴露前端需要的字段
2. 将listMembers返回类型从`IPage<ChannelMember>`改为`IPage<MemberVO>`
3. 在Biz层或Service层添加Entity→VO转换逻辑，禁止在Controller直接返回Entity
4. 检查所有Controller方法：list/search/get等查询接口禁止直接返回Entity
5. 敏感字段（createBy、updateBy、delFlag等内部审计字段）不返回给前端
6. 移除getUserChannelRelation在Controller层的VO组装逻辑，迁移到ChannelMemberBizService

**验证方式**:
- 所有API响应中不包含Entity的内部字段（如del_flag、create_by等）
- Controller层无业务逻辑和VO组装代码
- 所有查询接口返回对应的VO类型
- 单元测试验证响应结构符合VO定义

**状态**: pending

---

### BE-004 - 查询类接口错误使用@PostMapping，应使用@GetMapping

**来源**: review-report-20260627-084036.md
**位置**: ChannelMemberController、ChannelGovernanceController等所有查询接口
**优先级**: BLOCK
**依赖**: BE-002（路径重构时一并修复）
**类型**: 代码修复-后端

**修复步骤**:
1. 将所有查询类接口的@PostMapping改为@GetMapping：
   - GET /applications/pending（待审列表）
   - GET /blacklist/list（黑名单列表）
   - GET /log（治理日志列表）
   - GET /list（成员列表、订阅列表等）
   - GET /search（搜索成员）
   - GET /status/{channelId}（订阅状态）
   - GET /relation（用户频道关系）
2. 写操作（创建、更新、删除、批准、拒绝等）保持@PostMapping
3. 在重构API路径（BE-002）时一并修正HTTP方法

**验证方式**:
- 所有查询接口使用GET方法
- 所有写操作使用POST方法
- HTTP方法符合RESTful语义
- 前端调用HTTP方法同步更新

**状态**: pending

---

### BE-005 - design.md缺少完整API契约定义

**来源**: review-report-20260627-084036.md
**位置**: design.md
**优先级**: BLOCK
**依赖**: BE-002
**类型**: 文档修复

**修复步骤**:
1. 在design.md中补充完整的API契约章节，每个API包含：
   - HTTP方法、完整路径（使用正确的复数前缀）
   - 请求参数（路径参数、Query参数、RequestBody结构）
   - 响应体结构（VO定义）
   - 错误码列表（400参数错误、403权限不足、404不存在、409冲突等）
   - 权限要求（哪些角色可调用）
2. 补充所有Controller的API定义：ChannelMember、ChannelSubscription、ChannelGovernance、ChannelInvite
3. 包含正向漂移的API文档：getUserChannelRelation、searchMembers等
4. 补充错误码区间定义，与EPIC-20保持一致
5. 补充权限矩阵表格：各角色可执行的操作

**验证方式**:
- design.md中API定义与重构后的Controller实现一一对应
- 前后端可以基于文档进行接口对齐
- 所有错误码有明确定义

**状态**: pending

---

### BE-006 - 缺少黑名单优先级高于邀请的边界Scenario

**来源**: review-report-20260627-084036.md
**位置**: specs/channel-blacklist/spec.md
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复+代码验证

**修复步骤**:
1. 在specs/channel-blacklist/spec.md中补充Scenario：
   - **Scenario**: 用户在黑名单中但持有有效邀请时，使用邀请加入应被拒绝
   - Given 用户A在频道黑名单中
   - When 用户A使用有效邀请码/邀请链接尝试加入
   - Then 系统返回"您无法加入此频道"错误，邀请不被消耗
   - And 黑名单检查在邀请校验之前执行
2. 在ChannelInviteBizService或ChannelMemberBizService的joinByInvite逻辑中验证：
   - 加入流程第一步检查黑名单，命中则直接返回
   - 黑名单检查优先级 > 邀请有效性校验 > 冷却期校验 > 隐私设置校验
3. 补充对应的单元测试用例

**验证方式**:
- 黑名单用户使用邀请无法加入，返回明确错误
- 邀请码不被消耗
- 单元测试覆盖该边界场景

**状态**: pending

---

### BE-007 - 缺少批量治理操作部分失败处理的Scenario和实现

**来源**: review-report-20260627-084036.md
**位置**: specs/channel-member-removal/spec.md, specs/channel-member-mute/spec.md
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复+代码修复

**修复步骤**:
1. 在对应spec文件中补充批量操作Scenario：
   - **Scenario**: 批量操作部分成功部分失败
   - Given 批量处理N个目标用户，其中M个无效（不存在/权限不足/已被处理）
   - When 执行批量操作
   - Then 返回批量操作结果：每项包含success/fail状态、失败原因
   - And 成功的项正常执行并记录日志，失败的项不影响其他项处理
   - And 不回滚已成功的操作
2. 定义批量操作响应VO：BatchOperationResultVO，包含totalCount、successCount、failCount、details列表
3. 修改批量移除/批量禁言/批量批准/批量拒绝接口实现：
   - 使用循环逐个处理，捕获单个项的异常
   - 收集每个项的处理结果
   - 全部处理完成后返回汇总结果
4. 前端可基于结果展示失败原因并支持重试失败项

**验证方式**:
- 批量操作中部分目标无效时，有效目标正常处理
- 返回结果包含每个项的处理状态和失败原因
- 成功的操作记录审计日志
- 单元测试覆盖部分失败场景

**状态**: pending

---

### BE-008 - design.md表结构描述与实际SQL不一致

**来源**: drift-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: design.md File Structure, Migration Plan
**优先级**: FLAG
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新design.md中关于privacy字段的描述：
   - 原描述："+privacy_type, +join_method"
   - 修正：privacy字段在EPIC-20的V_channel_infrastructure.sql中已存在（名为privacy），本期仅扩展join_method字段
2. 补充content_channel_subscription_group_rel关联表说明：
   - 这是多对多关系需要的正向漂移，应补充到File Structure中
   - 添加对应的Entity（ChannelSubscriptionGroupRel）、Mapper、Service说明
3. 更新关于content_channel_member_role表的描述：
   - 原设计：独立表存储角色变更历史
   - 实际实现：角色直接存储在content_channel_member.role TINYINT字段
   - 更新D1决策说明：本期为简化实现，角色直接存储在member表，暂不追踪角色变更历史，后续迭代可扩展独立角色历史表
   - 从File Structure中移除ChannelMemberRole相关的Entity/Mapper/Service（或标记为TODO）
4. 更新Migration Plan：新增表数量从9张调整为实际数量（包含group_rel表，不含member_role表）

**验证方式**:
- design.md中表清单与V_channel_privacy_membership.sql一致
- D1决策记录与实际实现一致
- 文档不引用不存在的Entity/Mapper/Service

**状态**: pending

---

### BE-009 - ChannelMember.role字段类型为Integer，未使用MemberRole枚举

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: entity/ChannelMember.java
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 修改ChannelMember.java中role字段类型：
   - 从`private Integer role;`改为`private MemberRole role;`
2. 确认MemberRole枚举是否已正确定义（OWNER/ADMIN/EDITOR/MEMBER）
3. 添加MyBatis Plus类型处理器或使用@EnumValue注解，确保枚举与数据库TINYINT正确映射
4. 检查所有使用role字段进行比较、赋值的地方，更新为使用枚举而非魔法数字
5. 检查DTO/VO中的role字段类型是否同步更新
6. 参考项目中其他枚举字段的处理方式（如status、type等字段）

**验证方式**:
- role字段使用MemberRole枚举类型，无魔法数字
- 数据库读写时枚举值与TINYINT正确转换
- 角色比较逻辑使用枚举常量，类型安全
- 相关单元测试通过

**状态**: pending

---

### BE-010 - content_channel_member表缺少is_muted冗余字段

**来源**: drift-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: V_channel_privacy_membership.sql, entity/ChannelMember.java
**优先级**: FLAG
**依赖**: 无
**类型**: 双向调整（选其一）

**修复步骤**:
方案A（推荐，按原设计补充冗余字段）：
1. 在V_channel_privacy_membership.sql中为content_channel_member表添加字段：
   `is_muted TINYINT DEFAULT 0 COMMENT '是否禁言：0-否，1-是'`
2. 在ChannelMember.java实体类中添加isMuted字段
3. 在禁言/解禁操作中同步更新member表的is_muted字段
4. 列表查询和关系判断时优先使用冗余字段，避免频繁join mute表
5. 添加Flyway增量脚本，确保已有数据库可平滑升级

方案B（更新设计文档）：
1. 更新design.md D5决策：移除"member表只需一个is_muted冗余字段"的描述
2. 说明禁言状态统一通过content_channel_mute表判断，不使用冗余字段
3. 在查询时通过left join或单独查询判断禁言状态
4. 评估性能影响：成员列表查询需join mute表，确保有合适索引

**建议**: 采用方案A，冗余字段提升查询性能，且禁言状态是高频访问字段

**验证方式**:
（方案A）
- member表存在is_muted字段
- 禁言/解禁操作同步更新该字段
- 成员列表查询使用冗余字段判断禁言状态，性能符合P95<=500ms要求

**状态**: pending

---

### BE-011 - 默认关注系统频道策略未实现

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: ChannelSubscriptionBizService
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 明确默认关注触发时机：用户注册完成后，或首次进入内容社区时
2. 定义系统频道配置方式：可在数据库配置表或配置文件中维护默认关注的系统频道ID列表
3. 在用户注册完成的事件监听器中（或首次进入内容社区时）：
   - 获取默认关注的系统频道列表
   - 过滤用户已订阅的频道
   - 批量创建订阅关系
   - 记录订阅来源为"system_default"
4. 确保默认订阅可被用户取消，不强制
5. 在design.md和proposal.md中关闭对应的Open Question

**验证方式**:
- 新用户注册后自动订阅配置的系统频道
- 订阅来源标记为系统默认
- 用户可手动取消默认订阅
- 补充单元测试验证默认关注逻辑

**状态**: pending

---

### BE-012 - 禁言到期自动解封定时任务未找到

**来源**: verify-report-20260627-084036.md
**位置**: 定时任务类（推测为ChannelScheduledTask）
**优先级**: FLAG
**依赖**: BE-010（若采用方案A需同步更新is_muted字段）
**类型**: 代码修复-后端

**修复步骤**:
1. 创建或更新定时任务类（如ChannelScheduledTask）：
   - 添加@Scheduled注解，配置执行频率（如每小时执行一次）
2. 实现unmuteExpired方法：
   - 查询content_channel_mute表中end_time <= now()且未手动解除的记录
   - 批量更新这些记录的状态为"已过期自动解除"
   - 若采用is_muted冗余字段方案，同步更新member表is_muted=0
   - 记录治理日志（系统自动解禁）
3. 添加分布式锁，避免多实例重复执行
4. 配置定时任务开关，支持运维临时关闭
5. 在design.md中补充定时任务说明

**验证方式**:
- 禁言到期后自动解除，无需管理员手动操作
- 自动解禁记录审计日志
- 定时任务不重复执行
- 单元测试验证过期解禁逻辑

**状态**: pending

---

### BE-013 - 冷却期主动解除机制未明确

**来源**: verify-report-20260627-084036.md
**位置**: ChannelMemberBizService
**优先级**: FLAG
**依赖**: 无
**类型**: 文档修复+代码补充

**修复步骤**:
1. 在design.md D4决策中补充冷却期解除方式说明：
   - 被动解除：冷却期结束后用户可自行加入（已实现）
   - 主动解除：管理员可提前解除冷却期（需补充）
2. 添加管理员解除冷却期API：
   - POST /api/v1/content/channels/{channelId}/members/{userId}/cooling-cancel
   - 权限：仅频道主/管理员可操作
   - 需要填写解除原因
   - 记录审计日志
3. 在治理操作菜单中添加"解除冷却期"入口（前端后续实现）

**验证方式**:
- 文档明确冷却期解除的两种方式
- 管理员可提前解除用户冷却期
- 解除操作记录审计日志
- 被解除冷却期的用户可立即重新加入

**状态**: pending

---

### BE-014 - plan.md缺少详细TDD红→绿步骤

**来源**: review-report-20260627-084036.md
**位置**: plan.md
**优先级**: FLAG
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 参考EPIC-20的plan.md格式，补充详细的TDD步骤：
   - 每个Capability的Red阶段（写测试→测试失败）
   - Green阶段（写实现→测试通过）
   - Refactor阶段（重构→测试保持通过）
2. 按模块组织TDD步骤：隐私设置→加入方式→订阅→成员→治理→邀请
3. 补充每个步骤的验证命令：mvn test指定测试类
4. 补充测试数据准备说明

**验证方式**:
- plan.md包含完整的TDD流程步骤
- 可按plan.md步骤逐步实现并验证
- 每个阶段有明确的验收命令

**状态**: pending

---

### BE-015 - 订阅提醒开关API缺失

**来源**: backend-issues.md(frontend), verify-report-20260627-084036.md
**位置**: ChannelSubscriptionController
**优先级**: FLAG（P2，不影响核心功能）
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 添加更新订阅提醒API：
   - PUT /api/v1/content/channels/{channelId}/subscriptions/reminder
   - 参数：enabled (boolean)
   - 权限：订阅者本人
2. 在ChannelSubscriptionServiceImpl中实现updateReminder方法：
   - 更新remind_enabled字段
3. 如果本期不实现，在proposal.md中标记为Non-Goals或后续迭代

**验证方式**:
- 用户可开关某频道的订阅提醒
- remind_enabled字段正确更新
- 提醒推送服务读取该字段决定是否推送
- 单元测试覆盖

**状态**: pending

---

### BE-016 - 订阅分组管理API缺失

**来源**: backend-issues.md(frontend)
**位置**: ChannelSubscriptionController
**优先级**: FLAG（P2，不影响核心功能）
**依赖**: BE-008（group_rel表文档已补充）
**类型**: 代码修复-后端

**修复步骤**:
1. 确认是否已有移动频道到分组的API
2. 如果缺失，添加：
   - PUT /api/v1/content/channels/{channelId}/subscriptions/group
   - 参数：groupId (null表示移出分组)
   - 权限：订阅者本人
3. 确保ChannelSubscriptionGroup和ChannelSubscriptionGroupRel的CRUD完整
4. 如果本期不实现移动分组功能，在文档中标记为后续迭代

**验证方式**:
- 可将订阅的频道移动到指定分组
- 分组关系正确保存在group_rel表中
- 分组列表查询正确显示组内频道

**状态**: pending

---

### BE-017 - 邀请使用记录表缺失，无法追溯谁用了哪个邀请

**来源**: drift-report-20260627-084036.md
**位置**: V_channel_privacy_membership.sql
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档更新或后续迭代

**修复步骤**:
1. 评估邀请使用记录的必要性：当前Invite表有use_count字段，但无法记录谁用了
2. 如果需要追溯：新建content_channel_invite_use表，记录invite_id、user_id、use_time、ip等
3. 如果本期不需要：更新design.md D6决策说明：本期仅记录使用次数，不记录详细使用历史，后续迭代补充
4. 在Open Questions中标记为已决策

**建议**: 本期保持简化实现，更新文档说明，后续迭代再补充使用历史表

**验证方式**:
- 文档明确说明邀请使用记录的实现范围
- 若补充使用表，邀请使用时正确插入记录

**状态**: pending

---

### BE-018 - 批量操作每个目标的权限校验未明确

**来源**: drift-report-20260627-084036.md
**位置**: ChannelGovernanceBizService
**优先级**: ADVISORY
**依赖**: BE-001, BE-007
**类型**: 代码修复-后端

**修复步骤**:
1. 在批量操作实现中，逐个校验操作者对每个目标的权限：
   - 不能处理角色高于自己的用户
   - 不能处理频道主
   - 不能处理自己
2. 无权限的目标在批量结果中标记为失败，原因是"权限不足"
3. 不要因为一个目标无权限就拒绝整个批量请求

**验证方式**:
- 批量操作中包含无权限处理的目标时，该目标失败，其他目标正常处理
- 返回结果明确说明哪些目标因权限不足失败
- 单元测试覆盖批量权限校验场景

**状态**: pending

---

### BE-019 - 通知服务接口未定义，治理通知如何发送不明确

**来源**: review-report-20260627-084036.md
**位置**: design.md Risks
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复+Mock实现

**修复步骤**:
1. 定义通知服务接口：ChannelNotificationService
   - 方法：notifyJoinApproved、notifyJoinRejected、notifyRemoved、notifyMuted、notifyUnmuted、notifyBlacklisted等
2. 本期实现Mock版本：仅记录日志，不实际发送推送
3. 在design.md中说明：通知服务本期Mock实现，后续对接统一消息中心
4. 关闭design.md中对应的Open Question

**验证方式**:
- 治理操作调用通知接口不报错
- 通知内容正确（接收人、类型、参数）
- Mock实现仅记录日志，不依赖外部服务
- 文档明确通知服务的当前状态和后续计划

**状态**: pending
