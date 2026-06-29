# 修复计划 — channel-20-infrastructure

**生成时间**: 2026-06-30
**审核文档数**: 3 (review-report/drift-report/verify-report)
**总问题数**: 21

---

## 修复项

### BE-001 - ChannelReview实体@TableName表名映射错误

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: entity/ChannelReview.java:14
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 找到ChannelReview.java实体类，将@TableName注解从"channel_review"修改为"content_channel_review"，与Flyway脚本V_channel_infrastructure.sql中的表名一致

**验证方式**:
- 启动应用，测试审核记录CRUD操作不报表不存在错误
- 运行ChannelReview相关单元测试

**状态**: pending

---

### BE-002 - ChannelBizManageService直接注入Mapper违反分层原则

**来源**: drift-report-20260627-084036.md
**位置**: biz/ChannelBizManageService.java:48
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 移除ChannelBizManageService中对ChannelContentPublishMapper的直接注入
2. 改为注入对应的Service层（如ChannelContentPublishService），通过Service层调用数据访问
3. 调整相关方法调用，确保分层依赖方向正确（Controller→Biz→Service→Mapper）

**验证方式**:
- 代码编译通过
- 相关业务流程测试正常
- 检查所有Biz层类无直接注入Mapper的情况

**状态**: pending

---

### BE-003 - 组织频道创建isOrgCertified硬编码为true

**来源**: verify-report-20260627-084036.md
**位置**: controller/ChannelController.java:65
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 注入组织认证服务（IOrganizationAuthService或类似服务）
2. 在createOrganizationChannel方法中，调用组织认证服务校验当前用户是否为已认证组织的管理员
3. 在组织认证系统就绪前，可临时禁用组织频道创建或添加TODO注释标记待实现
4. 修复ChannelController中硬编码的true参数

**验证方式**:
- 未认证组织用户无法创建组织频道
- 组织认证校验逻辑正常工作
- 相关单元测试覆盖权限校验场景

**状态**: pending

---

### BE-004 - plan.md主键类型与JeecgBoot框架不匹配

**来源**: review-report-20260627-084036.md
**位置**: plan.md:29
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新plan.md中SQL DDL脚本，将主键id从BIGINT UNSIGNED AUTO_INCREMENT修改为VARCHAR(32)
2. 更新plan.md中所有实体示例，主键类型从Long改为String，使用ASSIGN_ID（雪花算法）
3. 更新ownerId、categoryId、organizationId等ID字段类型从Long改为String
4. 更新时间类型说明，统一使用java.util.Date（继承JeecgEntity）或说明LocalDateTime与Date的适配方式
5. 补充说明：JeecgBoot基类JeecgEntity提供createBy/createTime/updateBy/updateTime审计字段

**验证方式**:
- plan.md中的代码示例与实际JeecgBoot框架规范一致
- 文档描述与现有Entity定义一致

**状态**: pending

---

### BE-005 - SQL脚本缺少软删除字段说明

**来源**: review-report-20260627-084036.md
**位置**: V_channel_infrastructure.sql (plan.md:28-52)
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 确认项目使用JeecgBoot的逻辑删除机制（@TableLogic注解）
2. 如果使用逻辑删除，在SQL脚本中添加del_flag TINYINT DEFAULT 0字段
3. 如果使用租户隔离或其他机制，在plan.md和design.md中补充说明软删除的实现方案
4. 在Entity类中添加@TableLogic注解（如需要）

**验证方式**:
- 删除操作执行逻辑删除而非物理删除
- 文档中软删除实现方案清晰明确

**状态**: pending

---

### BE-006 - design.md缺少API契约定义

**来源**: review-report-20260627-084036.md
**位置**: design.md:88-142
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在design.md中补充API契约章节，包含所有用户端和后台API的完整定义
2. 每个API需包含：HTTP方法、路径、请求参数、请求体结构、响应体结构、错误码
3. 注意Channel API路径统一使用/api/v1/content/channel/前缀（根据任务说明）
4. 补充错误码区间定义和权限矩阵说明
5. 包含正向漂移新增的API：拒绝转让、删除前置检查、转让历史、待确认转让

**验证方式**:
- design.md中API定义与实际Controller实现一致
- 前后端可以基于文档进行接口对齐

**状态**: pending

---

### BE-007 - Controller写操作HTTP方法不符合AGENTS.md规范

**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: ChannelController.java:141, 173等
**优先级**: FLAG
**依赖**: BE-006（先更新文档契约）
**类型**: 代码修复-后端

**修复步骤**:
1. 按照AGENTS.md规范，所有写操作（创建、更新、删除）统一使用@PostMapping
2. 将@PutMapping("/{id}")（编辑频道）改为@PostMapping("/update")或符合项目规范的POST路径
3. 将@DeleteMapping("/{id}")（删除频道）改为@PostMapping("/{id}/delete-request")
4. 路径风格统一：移除RPC风格后缀（如/create改为POST集合根路径），或遵循项目现有API风格
5. 注意：根据任务说明，Channel API路径使用/api/v1/content/channel/前缀

**验证方式**:
- 所有写操作API使用POST方法
- API路径风格一致
- 前端调用与后端路径匹配

**状态**: pending

---

### BE-008 - 名称唯一性校验状态列表不完整

**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: service/impl/ChannelServiceImpl.java NAME_OCCUPIED_STATUSES
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 确认ReadonlyFrozen、Hidden、Archived状态是否已在EPIC-24中定义
2. 如果已定义，将这些状态添加到NAME_OCCUPIED_STATUSES校验列表中
3. 如果尚未定义，在ChannelStatus枚举中预留这些状态值或添加注释标记
4. 校验范围应包含：PENDING_REVIEW、ACTIVE、DELETE_COOLING、READONLY_FROZEN、HIDDEN、ARCHIVED
5. Deleted状态是否释放名称按PRD说明：本期假设释放

**验证方式**:
- 冻结/隐藏/归档频道的名称无法被新频道占用
- 名称唯一性校验状态列表与PRD"用户频道范围"定义一致

**状态**: pending

---

### BE-009 - 转让完成后未将原频道主降为管理员

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: biz/ChannelBizManageService.java:184 (confirmTransfer)
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 在confirmTransfer方法中，更新ownerId为新用户后
2. 添加逻辑将原owner设置为频道管理员角色
3. 需要确认频道成员角色表结构（EPIC-21可能已定义）
4. 如果角色功能在EPIC-21实现，添加TODO标记并在文档中说明依赖
5. 记录审计日志

**验证方式**:
- 转让完成后原频道主保留管理员权限
- 新用户成为频道所有者
- 相关审计日志完整记录

**状态**: pending

---

### BE-010 - 个人/组织频道创建未校验基础账号要求

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: biz/ChannelBizManageService.java:61-79 (createPersonalChannel)
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 注入用户服务（IUserService或SysUserService）获取当前用户信息
2. 校验用户是否满足基础账号要求：
   - 账号未被冻结
   - 已完成手机号或邮箱验证
   - 未处于禁止创建频道的风控状态
3. 校验不通过时抛出业务异常，返回明确的错误提示
4. 个人频道创建时privacy字段使用DTO传入值，而非硬编码为1
5. 个人频道创建时强制organizationId为null

**验证方式**:
- 不满足基础账号要求的用户无法创建频道
- 返回明确的错误原因
- 个人频道privacy设置生效
- 个人频道organizationId为null

**状态**: pending

---

### BE-011 - ChannelAdminController缺少权限注解

**来源**: drift-report-20260627-084036.md
**位置**: controller/ChannelAdminController.java 所有方法
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 为ChannelAdminController所有方法添加权限注解
2. 使用@RequiresPermissions或@PreAuthorize注解，配置对应的权限标识
3. 如：@RequiresPermissions("content:channel:admin")、@RequiresPermissions("content:channel:review")
4. 确保系统频道创建、审核、强制删除等高风险操作有严格的权限校验
5. 参考项目其他后台Controller的权限注解写法

**验证方式**:
- 无权限用户无法访问后台频道管理API
- 权限配置与菜单权限标识一致
- 接口鉴权测试通过

**状态**: pending

---

### BE-012 - ChannelReview扩展字段未同步到文档

**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: design.md, specs/channel-review/spec.md
**优先级**: FLAG
**依赖**: BE-006
**类型**: 文档修复

**修复步骤**:
1. 更新design.md中的ChannelReview实体定义，同步实际扩展的字段：
   - reviewId: 审核记录ID
   - reviewType: 审核类型（创建/编辑）
   - applicantId: 申请人ID
   - submitTime: 提交时间
   - reviewTime: 审核时间
   - timeoutFlag: 超时标记
   - targetChannelId: 目标频道ID
   - status: 审核状态（pending/approved/rejected/returned）替代原result枚举
2. 更新specs/channel-review/spec.md，同步新的审核记录模型
3. 说明这是EPIC-24审核流程增强的合理超前实现
4. 更新plan.md中相关实体定义

**验证方式**:
- 文档中ChannelReview字段与实际Entity一致
- 审核状态枚举定义清晰

**状态**: pending

---

### BE-013 - 正向漂移API未同步到文档

**来源**: drift-report-20260627-084036.md
**位置**: design.md, specs/
**优先级**: FLAG
**依赖**: BE-006
**类型**: 文档修复

**修复步骤**:
1. 在design.md和对应specs中补充以下正向漂移API的文档：
   - POST /{id}/transfer/{transferId}/reject - 拒绝转让
   - GET /{id}/delete-check - 删除前置检查
   - GET /{id}/transfers - 转让历史查询
   - GET /{id}/transfer/pending - 待确认转让查询
2. 补充定时任务相关文档：冷静期处理、转让超时、审核超时、不活跃频道扫描（EPIC-24超前实现需说明）
3. 在Open Questions中关闭已明确的问题，或说明Mock实现方案

**验证方式**:
- 所有已实现的API在文档中有对应定义
- 超前实现的功能有标注说明

**状态**: pending

---

### BE-014 - 个人频道创建时organizationId未强制为null

**来源**: review-report-20260627-084036.md
**位置**: biz/ChannelBizManageService.java createPersonalChannel
**优先级**: FLAG
**依赖**: BE-010
**类型**: 代码修复-后端

**修复步骤**:
1. 在buildChannelFromDTO或createPersonalChannel方法中
2. 显式设置channel.setOrganizationId(null)
3. 根据channelType设置差异化字段：
   - 个人频道：organizationId=null，channelType=PERSONAL
   - 组织频道：organizationId=当前用户所属组织ID，channelType=ORGANIZATION
   - 系统频道：channelType=SYSTEM

**验证方式**:
- 个人频道的organizationId字段始终为null
- 组织频道自动绑定所属组织ID

**状态**: pending
