# 修复计划 — channel-22-content-governance

**生成时间**: 2026-06-30
**审核文档数**: 3 (drift-report/review-report/verify-report)
**总问题数**: 28
**整体评估**: ❌ 严重问题 - 核心逻辑80%未实现、测试0%覆盖、存在7个CRITICAL架构问题

---

## 修复项

### BE-001 - 所有测试完全缺失，TDD流程未执行

**来源**: verify-report-20260627-084036.md CRIT-01
**位置**: src/test/ 目录
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端（测试补充）

**修复步骤**:
1. 按tasks.md要求补充所有单元测试和集成测试：
   - Biz层测试：ChannelPublishBizTest、ChannelReviewBizTest、ChannelGovernanceBizTest、ChannelAnnouncementBizTest
   - Service层测试：ChannelContentPublishServiceTest、ChannelScheduledPublishServiceTest等
   - Controller层集成测试
2. 测试必须覆盖：
   - 四种发布权限模型校验
   - 禁言/黑名单用户发布拦截
   - 发布限额（小时/日/字数）校验
   - 定时发布到达时重新校验逻辑
   - 审核流程（批准/拒绝/批量）
   - 治理操作（置顶/精华/删除/恢复/移出）
   - 公告CRUD和XSS过滤
3. 遵循TDD流程：先写测试→红灯→实现→绿灯→重构
4. 确保单元测试覆盖率≥80%，核心路径100%覆盖
5. 禁止使用placeholder测试，必须真实Mock依赖（ChannelMemberService、ChannelMuteService等）

**验证方式**:
- mvn test运行所有测试通过
- 覆盖率报告≥80%
- 边界场景（限额超限、无权限、黑名单命中）测试覆盖
- 测试包含真实断言，不是仅验证方法调用

**状态**: pending

---

### BE-002 - Biz层直接注入Mapper违反分层架构

**来源**: drift-report-20260627-084036.md ARCH-C01
**位置**: biz/impl/ChannelPublishBizImpl.java 及其他Biz类
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端（架构重构）

**修复步骤**:
1. 重构ChannelPublishBizImpl：
   - 移除对publishMapper、reviewMapper的直接@Resource注入
   - 改为注入对应的Service：ChannelContentPublishService、ChannelContentReviewService
   - 单表CRUD操作全部通过Service层调用
   - Biz层只负责跨Service编排和事务边界控制
2. 检查所有Biz类（ChannelReviewBiz、ChannelGovernanceBiz、ChannelAnnouncementBiz）：
   - 禁止直接注入任何Mapper
   - 所有数据访问必须通过Service层
3. 检查Controller层：
   - 禁止Controller直接调用Service（除简单查询外）
   - 写操作必须经过Biz层
4. 分层依赖方向严格为：Controller → Biz → Service → Mapper

**验证方式**:
- 代码扫描确认Biz层无Mapper注入
- 所有跨表操作在Biz层编排
- 单表逻辑在Service层实现
- 相关测试通过，架构合规性检查通过

**状态**: pending

---

### BE-003 - 多频道发布权限校验是placeholder，真实逻辑未实现

**来源**: verify-report-20260627-084036.md CRIT-03
**位置**: biz/impl/ChannelPublishBizImpl.java 权限校验部分
**优先级**: CRITICAL
**依赖**: BE-002
**类型**: 代码修复-后端

**修复步骤**:
1. 移除permissionResult硬编码"ALLOW"的placeholder代码
2. 注入并调用EPIC-21的依赖Service：
   - ChannelMemberService：查询用户在目标频道的角色
   - ChannelMuteService：校验用户是否被禁言
   - ChannelBlacklistService：校验用户是否在黑名单
3. 实现完整的发布权限校验链：
   - 第一步：校验频道是否存在且状态正常
   - 第二步：校验用户是否为频道成员（公开频道订阅者也可发布？需明确）
   - 第三步：校验用户角色是否有发布权限
   - 第四步：校验用户未被禁言
   - 第五步：校验用户不在黑名单
   - 第六步：校验发布限额（小时/日/字数）
   - 第七步：根据publish_permission配置决定直接发布还是入审
4. 四种发布权限模型：
   - 所有人可发布
   - 仅成员可发布
   - 仅编辑/管理员可发布
   - 仅频道主可发布
5. 校验失败时返回明确的错误码和原因

**验证方式**:
- 禁言用户发布返回403禁言错误
- 黑名单用户发布返回403黑名单错误
- 无发布权限角色返回403权限不足
- 超限额返回429或明确错误
- 四种权限模型逐一测试验证
- 单元测试覆盖所有校验分支

**状态**: pending

---

### BE-004 - 所有Controller缺少权限校验注解，存在越权风险

**来源**: drift-report-20260627-084036.md ARCH-C03
**位置**: 所有Controller类
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 为每个Controller方法添加权限校验：
   - @RequiresPermissions注解配置菜单权限标识
   - 在Biz层补充频道角色校验（频道主/管理员才可执行管理操作）
2. 权限矩阵：
   - 发布提交：登录用户（还需通过频道发布权限校验）
   - 审核操作：频道主/管理员
   - 治理操作（置顶/精华/删除/移出）：频道主/管理员
   - 公告管理：频道主/管理员
   - 定时发布管理：发布者本人/频道主/管理员
   - 回收站管理：频道主/管理员
3. 参考EPIC-21的权限校验写法，确保：
   - 普通用户无法置顶/删除/审核内容
   - 编辑只能发布和编辑自己的内容，不能审核
   - 频道主拥有最高权限
4. 补充权限相关单元测试

**验证方式**:
- 无权限用户调用管理API返回403
- 不同角色权限边界正确
- 频道数据隔离（A频道管理员不能操作B频道）
- 所有接口有权限校验，不存在匿名访问管理接口

**状态**: pending

---

### BE-005 - 表名缺少content_前缀，与EPIC-20命名规范不一致

**来源**: drift-report-20260627-084036.md DRIFT-C05, review-report BLOCK-01
**位置**: V_channel_content_governance.sql, 所有Entity@TableName
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端（SQL+Entity）

**修复步骤**:
1. 修改所有表名添加content_前缀：
   - channel_content_publish → content_channel_publish
   - channel_content_review → content_channel_review
   - channel_scheduled_publish → content_channel_scheduled_publish
   - channel_publish_limit → content_channel_publish_limit
   - channel_recycle_bin → content_channel_recycle_bin
   - channel_announcement → content_channel_announcement
   - channel_content_governance_log → content_channel_content_governance_log
2. 更新所有Entity类的@TableName注解对应新表名
3. 更新Mapper XML中的表名
4. 重新生成Flyway脚本或创建增量迁移脚本
5. 确保表名前缀与EPIC-20的content_channel、EPIC-21的content_channel_member等保持一致

**验证方式**:
- 所有表名以content_channel_开头
- Entity@TableName与SQL表名一致
- 应用启动不报表不存在错误
- 查询/插入/更新操作正常

**状态**: pending

---

### BE-006 - 主键类型VARCHAR(36)不符合JeecgBoot雪花ID规范

**来源**: drift-report-20260627-084036.md DRIFT-C06, review-report BLOCK-02
**位置**: V_channel_content_governance.sql 主键定义
**优先级**: BLOCK
**依赖**: BE-005（表名修正时一并修改）
**类型**: 代码修复-后端（SQL）

**修复步骤**:
1. 将所有表主键id字段从VARCHAR(36)改为VARCHAR(32)
2. 确保所有Entity继承JeecgEntity（已有String id字段，使用ASSIGN_ID雪花算法）
3. 检查所有外键关联字段（channel_id、content_id、submitter_id、reviewer_id等）也使用VARCHAR(32)
4. 与EPIC-20/EPIC-21保持一致

**验证方式**:
- 所有主键/外键字段VARCHAR(32)
- Entity使用String类型id，无Long类型主键
- 雪花ID生成正常
- 外键关联查询正常

**状态**: pending

---

### BE-007 - 治理日志表名和实体类重复且不一致

**来源**: drift-report-20260627-084036.md DRIFT-C04, review-report BLOCK-04
**位置**: Entity目录、SQL
**优先级**: BLOCK
**依赖**: BE-005
**类型**: 代码修复-后端

**修复步骤**:
1. 统一治理日志命名：使用ChannelContentGovernanceLog，表名content_channel_content_governance_log
2. 删除重复的ChannelGovernanceLog实体（EPIC-21已有ChannelGovernanceLog用于成员治理，这里是内容治理，需区分）
3. 明确区分：
   - EPIC-21的ChannelGovernanceLog（content_channel_governance_log）：成员治理（移除/禁言/黑名单）
   - EPIC-22的ChannelContentGovernanceLog（content_channel_content_governance_log）：内容治理（置顶/精华/删除/编辑协助）
4. 更新所有引用ChannelGovernanceLog的地方改为正确的实体类
5. 删除多余的Mapper/Service
6. 如果EPIC-21和EPIC-22的治理日志可以合并到一张表，需明确决策并更新文档

**建议**: 合并为一张统一的content_channel_governance_log表，增加target_type字段区分治理对象（member/content），避免表拆分过细。

**验证方式**:
- 只存在一个治理日志实体（或明确区分内容/成员治理日志）
- 无重复Mapper/Service文件
- 治理操作日志正确写入
- 查询无歧义

**状态**: pending

---

### BE-008 - API路径使用单数+RPC风格，需重构为复数RESTful风格

**来源**: drift-report-20260627-084036.md DRIFT-C01/C02, review-report BLOCK-06
**位置**: 所有Controller类的@RequestMapping
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端+文档

**修复步骤**:
1. 统一所有Controller路径前缀为复数资源风格：
   - /api/v1/content/channel/publish → /api/v1/content/channels/{channelId}/publications
   - /api/v1/content/channel/review → /api/v1/content/channels/{channelId}/publications/reviews
   - /api/v1/content/channel/announcement → /api/v1/content/channels/{channelId}/announcements
   - /api/v1/content/channel/governance → /api/v1/content/channels/{channelId}/contents/{contentId}/governance 或 保持统一入口但路径带channelId
2. 重构路径层级：
   - 所有频道相关路径包含{channelId}路径参数
   - 不通过@RequestParam传递channelId
3. HTTP方法语义化：
   - 查询用@GetMapping
   - 创建用@PostMapping
   - 更新用@PostMapping（遵循项目写操作统一POST的规范）
   - 删除用@PostMapping（如/delete, /restore）
4. 删除重复Controller：ChannelReviewController和ChannelContentReviewController合并为一个
5. 更新所有spec和design.md中的API路径定义
6. 注意：保持全局前缀/api/v1/content/不变

**验证方式**:
- 所有API路径符合：/api/v1/content/channels/{channelId}/...
- channelId通过@PathVariable获取
- 无RPC风格路径（如/assign-role、/action）
- 前后端联调路径一致
- 所有单元测试更新后通过

**状态**: pending

---

### BE-009 - Controller直接返回Entity而非VO，存在数据泄漏风险

**来源**: drift-report-20260627-084036.md ARCH-C02, verify-report CRIT-02
**位置**: ChannelAnnouncementController等所有Controller
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 为所有API创建对应的VO类，放在vo/channel/子目录下：
   - PublishChannelVO、PublishResultVO、PublishLimitVO
   - ReviewItemVO、ReviewStatsVO、ReviewActionVO
   - AnnouncementVO、AnnouncementSummaryVO
   - GovernanceContentVO、RecycleBinItemVO、GovernanceLogVO
2. 所有Controller方法返回值类型改为对应VO
3. 在Biz层或Service层实现Entity→VO转换逻辑
   - 使用ConvertUtil或手动转换
   - 敏感字段（create_by、update_by、del_flag、内部状态）不暴露给前端
4. 禁止Controller方法直接返回Entity、IPage<Entity>等类型
5. 统一分页结构使用项目标准的IPage<VO>

**验证方式**:
- 所有API响应中无内部字段（create_by/del_flag等）
- 所有Controller返回VO类型
- 无直接返回Entity的方法
- TypeScript类型定义与VO字段对齐

**状态**: pending

---

### BE-010 - 所有表缺少软删除字段del_flag

**来源**: drift-report-20260627-084036.md DRIFT-W04, review-report FLAG-06
**位置**: V_channel_content_governance.sql 所有表
**优先级**: BLOCK
**依赖**: BE-005
**类型**: 代码修复-后端（SQL+Entity）

**修复步骤**:
1. 为所有表添加del_flag字段：
   `del_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记(0-正常,1-已删除)'`
2. 在所有Entity类中添加delFlag字段并加上@TableLogic注解
3. 确保查询时MyBatis Plus自动过滤已删除记录
4. 删除操作改为逻辑删除（update del_flag=1），不使用物理删除
5. 回收站功能基于del_flag=1的记录实现，不需要独立回收站表？需重新评估Decision 5：
   - 方案A：保留独立回收站表，记录额外的删除原因/删除人/删除时间
   - 方案B：使用del_flag+content_channel关联表扩展字段，不建独立表
   - 建议：采用方案B简化表结构，在关联表增加delete_reason、delete_by、delete_time字段

**验证方式**:
- 删除操作执行逻辑删除而非物理删除
- 查询接口不返回del_flag=1的记录（除回收站外）
- 回收站可正常展示已删除内容
- 恢复操作正确设置del_flag=0

**状态**: pending

---

### BE-011 - ChannelAnnouncement表存在重复字段created_by/create_by

**来源**: review-report BLOCK-03
**位置**: V_channel_content_governance.sql channel_announcement表
**优先级**: BLOCK
**依赖**: BE-005
**类型**: 代码修复-后端（SQL）

**修复步骤**:
1. 删除ChannelAnnouncement表中多余的created_by字段
2. 使用JeecgEntity基类提供的create_by、create_time、update_by、update_time字段
3. 检查Entity类，移除重复声明的createdBy等字段，直接继承JeecgEntity
4. 检查其他表是否也有类似的字段重复问题

**验证方式**:
- 无重复字段定义
- Entity正确继承JeecgEntity
- 审计字段自动填充正常
- 所有CRUD操作审计字段正确

**状态**: pending

---

### BE-012 - 定时发布调度任务完全未实现

**来源**: verify-report CRIT-04
**位置**: 定时任务配置类、ChannelScheduledPublishBiz
**优先级**: BLOCK
**依赖**: BE-002, BE-003
**类型**: 代码修复-后端

**修复步骤**:
1. 确定调度框架（Spring Scheduled或XXL-Job，根据项目现有技术栈选择）
2. 实现定时发布扫描任务：
   - @Scheduled(fixedDelay = 60000) 每分钟执行一次
   - 查询publish_time <= now()且status=PENDING的定时任务
   - 添加分布式锁避免多实例重复执行
   - 分批处理（每次100条）避免压力过大
3. 对每个到期任务重新执行完整校验链：
   - 频道状态校验
   - 用户权限/禁言/黑名单校验
   - 发布限额校验
   - 校验通过则执行发布或入审
   - 校验失败则标记为FAILED并通知用户
4. 实现定时发布CRUD API：
   - 创建定时发布
   - 查询我的定时发布列表
   - 取消定时发布
   - 更新发布时间
5. 添加定时发布状态枚举：PENDING/PUBLISHED/FAILED/CANCELED

**验证方式**:
- 到达发布时间后内容自动发布/入审
- 校验失败有通知且状态标记
- 多实例部署不重复发布
- 定时发布CRUD操作正常
- 有单元测试覆盖调度逻辑

**状态**: pending

---

### BE-013 - 置顶/精华/删除/回收站/移出/编辑协助等治理Service缺失

**来源**: verify-report CRIT-05
**位置**: service/channel/ 目录
**优先级**: BLOCK
**依赖**: BE-002, BE-007
**类型**: 代码修复-后端

**修复步骤**:
1. 按design.md File Structure补充所有缺失的Service接口和实现：
   - ChannelContentPinService：置顶/取消置顶
   - ChannelContentFeatureService：精华/取消精华
   - ChannelRecycleBinService：回收站列表/永久删除/恢复
   - ChannelContentMoveService：移出频道
   - ChannelEditAssistService：编辑协助/修订历史
   - ChannelContentGovernanceLogService：治理日志记录/查询
2. 实现每个治理操作的业务逻辑：
   - 权限校验（操作者必须是频道主/管理员）
   - 状态校验（如已置顶内容不能重复置顶）
   - 更新内容关联表状态
   - 记录治理日志
   - 发送通知给作者
3. 批量操作支持部分成功部分失败，返回每条结果
4. 编辑协助功能：
   - 限定可修改字段（标题、标签、摘要、错别字）
   - 记录修改前后对比（修订历史）
   - 需要作者确认或管理员直接生效？需明确
5. 所有治理操作在Biz层编排，不直接在Controller写逻辑

**验证方式**:
- 置顶/取消置顶功能正常
- 精华/取消精华功能正常
- 内容删除进入回收站，可恢复
- 移出频道内容不可见
- 编辑协助保留修订历史
- 所有操作记录治理日志
- 无权限用户无法执行治理操作
- 单元测试覆盖所有治理操作

**状态**: pending

---

### BE-014 - specs中缺少完整API契约定义

**来源**: review-report BLOCK-05
**位置**: specs/ 所有5个spec文件
**优先级**: BLOCK
**依赖**: BE-008（API路径确定后补充）
**类型**: 文档修复

**修复步骤**:
1. 在每个spec.md中补充完整API契约表格：
   - HTTP方法、完整路径
   - 请求参数（路径参数、Query、RequestBody）
   - 响应体结构（VO字段说明）
   - 错误码列表
   - 权限要求
2. 包含5个capability的所有API：
   - channel-publishing：发布、可发布频道列表、发布结果、定时发布CRUD、限额校验
   - channel-add-existing-content：搜索可添加内容、添加已有内容
   - channel-content-moderation：待审列表、审核操作、审核统计
   - channel-content-governance：内容列表、置顶/精华/删除/恢复/移出/编辑协助、回收站、治理日志
   - channel-announcements：公告CRUD、预览、历史版本、版本恢复
3. 明确错误码区间：
   - 400xxx：参数错误
   - 403xxx：权限不足
   - 409xxx：业务冲突（如重复置顶、限额超限）
   - 500xxx：服务器内部错误
4. 补充VO字段定义

**验证方式**:
- 每个API在spec中有完整定义
- 前后端可以基于spec对齐接口
- 所有错误码有明确定义和用户提示
- 与实际Controller实现一一对应

**状态**: pending

---

### BE-015 - 发布限额逻辑未实现

**来源**: verify-report Correctness验证
**位置**: ChannelPublishLimitService
**优先级**: FLAG
**依赖**: BE-003
**类型**: 代码修复-后端

**修复步骤**:
1. 实现发布限额配置和校验：
   - 每小时发布上限
   - 每日发布上限
   - 内容字数下限（防止灌水）
   - 可在频道配置中设置
2. 使用Redis计数或数据库统计，考虑：
   - 计数准确性（发布成功才计数，失败/拒绝不计数）
   - 过期自动重置（小时级按自然小时，日级按自然日）
   - 频道可配置不同限额
3. 在发布流程中调用限额校验：
   - 超限返回明确错误："今日发布次数已达上限(N次)"
   - 距离下次可发布的剩余时间
4. 限额配置管理API（频道管理端）

**验证方式**:
- 达到小时/日上限后无法发布
- 返回明确的剩余时间/次数
- 管理员可调整限额配置
- 字数不足无法提交
- 单元测试覆盖限额边界

**状态**: pending

---

### BE-016 - 公告富文本缺少XSS安全过滤

**来源**: drift-report DRIFT-W06
**位置**: ChannelAnnouncementBiz保存逻辑
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 引入项目已有的XSS过滤工具（或使用Jsoup）
2. 在公告保存前对content字段进行XSS过滤：
   - 移除危险标签（script、iframe、on*事件等）
   - 保留安全标签和属性（p、br、strong、em、ul/ol/li、a[href]、img[src]等）
3. 预览API可返回过滤后的HTML供前端预览
4. 配置白名单允许的标签和属性
5. 同样检查是否有其他用户输入富文本的地方需要过滤

**验证方式**:
- XSS攻击脚本（<script>alert(1)</script>）被过滤
- 安全格式（粗体、列表、链接）保留
- 公告预览展示正确
- 单元测试覆盖XSS过滤场景

**状态**: pending

---

### BE-017 - add-existing接口三种场景权限校验不完整

**来源**: verify-report SUGG-01
**位置**: ChannelPublishBiz.addExistingContent
**优先级**: FLAG
**依赖**: BE-003, BE-013
**类型**: 代码修复-后端

**修复步骤**:
1. 区分三种添加已有内容到频道的场景：
   - 场景1：系统频道由平台运营添加任意内容
   - 场景2：个人/组织频道主添加自己的内容
   - 场景3：频道主/管理员添加他人内容到频道（需明确是否允许）
2. 分别实现权限校验：
   - 系统频道：校验操作者是否平台运营
   - 自己的内容：校验内容作者是当前用户
   - 他人内容：校验当前用户是频道主/管理员，且内容公开
3. 添加内容到频道时：
   - 检查内容是否已在该频道（重复添加）
   - 检查频道内容数量上限
   - 记录添加人和添加时间
   - 发送通知给作者（如果是添加他人内容）
4. 不重复创建内容，只添加频道-内容关联

**验证方式**:
- 普通用户不能添加他人内容到任意频道
- 系统频道运营可添加内容
- 作者可将自己的内容添加到自己管理的频道
- 重复添加返回友好提示
- 单元测试覆盖三种场景

**状态**: pending

---

### BE-018 - 频道数量上限N未定义具体值

**来源**: review-report FLAG-01
**位置**: specs/channel-publishing/spec.md, 配置
**优先级**: FLAG
**依赖**: 无
**类型**: 文档修复+配置

**修复步骤**:
1. 明确用户可管理/可发布的频道数量上限：
   - 默认值：个人可创建频道5个、可发布到的频道数量根据角色定
   - 系统配置项支持调整
2. 在spec中明确具体数值，避免硬编码
3. 在获取可发布频道列表API中校验上限，超限给出提示
4. 如果是创建频道的上限，应在EPIC-20中控制；这里是发布权限的频道数量

**验证方式**:
- 默认值在配置文件或常量中定义
- 超限时无法在更多频道发布
- 提示信息明确
- 文档有明确说明

**状态**: pending

---

### BE-019 - 定时发布调度框架未确定

**来源**: review-report FLAG-02
**位置**: design.md Open Questions
**优先级**: FLAG
**依赖**: BE-012
**类型**: 文档修复

**修复步骤**:
1. 调研项目现有调度框架：
   - 检查是否已有XXL-Job配置
   - 如果没有，使用Spring @Scheduled简化实现（本期）
2. 在design.md中记录选型决策和理由
3. 如果用Spring Scheduled：
   - 配置cron表达式或fixedDelay
   - 添加分布式锁（Redis lock）防止多实例重复执行
   - 配置任务线程池大小
4. 如果用XXL-Job：
   - 配置任务Handler
   - 在控制台配置调度周期
5. 关闭Open Questions中对应的问题

**验证方式**:
- design.md中有明确的调度框架选型
- 定时任务正常执行不重复
- 文档与实际实现一致

**状态**: pending

---

### BE-020 - Biz层异常捕获过于宽泛，吞掉异常

**来源**: drift-report ARCH-W02
**位置**: 所有Biz类的catch块
**优先级**: FLAG
**依赖**: BE-002
**类型**: 代码修复-后端

**修复步骤**:
1. 搜索所有catch (Exception e)块
2. 改为精确捕获业务异常：
   - 如BusinessException、PermissionDeniedException等
   - 非预期异常（NullPointerException、SQLException等）不要捕获，让事务回滚并抛出
3. 异常处理原则：
   - 可预期的业务异常（无权限、限额超限、状态错误）：捕获并返回明确错误码
   - 不可预期的系统异常：不捕获，由全局异常处理器统一处理，事务回滚
   - 禁止空catch块或只打日志不处理
4. 确保@Transactional注解正确配置，异常时事务回滚

**验证方式**:
- 无catch (Exception e)宽泛捕获
- 业务异常返回明确错误码
- 系统异常正确回滚事务
- 错误日志有完整堆栈便于排查
- 单元测试验证异常场景

**状态**: pending

---

### BE-021 - 定时任务无分布式锁保护

**来源**: drift-report ARCH-W03
**位置**: Scheduled任务类
**优先级**: FLAG
**依赖**: BE-012
**类型**: 代码修复-后端

**修复步骤**:
1. 为所有定时任务添加Redis分布式锁：
   - 锁key：lock:channel:scheduled-publish（按任务类型区分）
   - 锁过期时间：5分钟（长于单次任务最大执行时间）
   - 获取锁失败则跳过本次执行
2. 实现方式：
   - 使用RLock tryLock()
   - finally块中释放锁
   - 添加锁获取失败日志
3. 任务执行时幂等校验：
   - 即使重复执行，也不会重复发布
   - 通过状态判断（PENDING→PUBLISHING）避免并发问题

**验证方式**:
- 多实例部署时定时任务不重复执行
- 锁释放正常，不会死锁
- 异常情况锁自动过期释放
- 幂等性保证重复执行无副作用

**状态**: pending

---

### BE-022 - 缺少@Validated分组校验

**来源**: drift-report ARCH-W01
**位置**: 所有Req类、Controller方法参数
**优先级**: FLAG
**依赖**: BE-009（创建VO/Req时一并添加）
**类型**: 代码修复-后端

**修复步骤**:
1. 为所有Req DTO添加JSR-303校验注解：
   - @NotBlank、@NotNull、@Size、@Min、@Max、@Length等
2. 定义校验分组：Create.class、Update.class
   - 创建时必填字段id不需要，其他必填
   - 更新时id必填，其他字段可选
3. Controller方法参数添加@Validated(Create.class)或@Validated(Update.class)
4. 全局异常处理器捕获MethodArgumentNotValidException，返回友好错误提示
5. 补充校验：
   - 发布内容字数范围
   - 公告标题长度
   - 定时发布时间必须晚于当前时间
   - 拒绝原因必填且长度限制

**验证方式**:
- 缺失必填参数返回400错误
- 参数格式错误（如超长）返回400错误
- 创建和更新场景校验规则正确区分
- 错误提示包含字段名和具体原因
- 单元测试覆盖参数校验场景

**状态**: pending

---

### BE-023 - 发布结果失败通知未实现

**来源**: verify-report WARN-03
**位置**: 发布/审核流程
**优先级**: FLAG
**依赖**: BE-003, BE-019（通知服务Mock）
**类型**: 代码修复-后端

**修复步骤**:
1. 定义通知服务接口ChannelNotificationService（参考EPIC-21 BE-019）
2. 本期实现Mock版本：仅记录日志
3. 在以下场景发送通知：
   - 提交发布成功（待审核/已发布）
   - 审核通过/拒绝通知给作者
   - 定时发布成功/失败通知
   - 治理操作通知给作者（被置顶/精华/删除/禁言等）
4. 通知内容包含：
   - 操作类型
   - 内容标题
   - 频道名称
   - 原因（如拒绝原因、删除原因）
   - 跳转链接
5. 在design.md中说明通知服务本期Mock实现

**验证方式**:
- 关键操作触发通知调用
- Mock实现不报错，记录日志
- 后续对接消息中心时只需替换实现类
- 文档说明清晰

**状态**: pending

---

### BE-024 - 批量审核接口缺失

**来源**: verify-report WARN-02
**位置**: ChannelReviewController
**优先级**: FLAG
**依赖**: BE-002, BE-013
**类型**: 代码修复-后端

**修复步骤**:
1. 实现批量审核API：
   - POST /api/v1/content/channels/{channelId}/publications/reviews/batch
   - 参数：reviewIds[]、action（APPROVE/REJECT）、rejectReason?
2. 实现逻辑：
   - 校验操作者权限
   - 逐个处理每个审核单
   - 返回批量结果：total、successCount、failCount、details（每条id+success+failReason）
   - 部分失败不回滚已成功的
3. 批量批准时检查：
   - 审核单状态必须是PENDING
   - 只能审核本频道的审核单
4. 发送批量审核结果通知（可选，本期可只记录日志）

**验证方式**:
- 批量审核部分成功部分失败返回详细结果
- 无权限/状态错误的审核单标记失败
- 成功的审核单正确流转状态
- 单元测试覆盖批量场景

**状态**: pending

---

### BE-025 - 治理操作VO缺失

**来源**: verify-report WARN-01
**位置**: vo/channel/governance/ 目录
**优先级**: FLAG
**依赖**: BE-009
**类型**: 代码修复-后端

**修复步骤**:
1. 补充治理模块所有VO：
   - GovernanceContentVO：频道内容列表项（标题、作者、发布时间、状态、置顶/精华标记）
   - RecycleBinItemVO：回收站项（内容标题、删除人、删除时间、删除原因、剩余天数）
   - GovernanceLogVO：治理日志项（操作者、操作类型、对象、原因、时间）
   - BatchGovernanceResultVO：批量操作结果
   - EditHistoryVO：编辑协助修订历史
2. 补充Req类：
   - PinContentReq、FeatureContentReq、DeleteContentReq、RestoreContentReq
   - MoveContentReq、EditContentReq、BatchGovernanceReq
3. 统一分页结构
4. 字段添加注释说明

**验证方式**:
- 所有治理API有对应的Req/VO
- 字段类型与数据库一致
- 前端可基于VO定义TypeScript类型
- 无敏感字段泄露

**状态**: pending

---

### BE-026 - plan.md缺少AddExistingContent的TDD步骤

**来源**: review-report FLAG-05
**位置**: plan.md
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 参考plan.md已有章节的格式，补充AddExistingContent的TDD步骤：
   - Red阶段：编写测试（三种场景权限、重复添加校验）→ 测试失败
   - Green阶段：实现Service/Biz/Controller逻辑 → 测试通过
   - Refactor阶段：重构优化，保持测试通过
2. 补充对应的测试验证命令
3. 确保plan覆盖所有7个tasks.md中的任务项

**验证方式**:
- plan.md覆盖所有capability的TDD步骤
- 按plan可逐步实现所有功能
- 每个阶段有明确的验收命令

**状态**: pending

---

### BE-027 - 未明确引用EPIC-21的依赖服务接口

**来源**: review-report ADV-02
**位置**: design.md Context章节
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在design.md中明确列出依赖的EPIC-21服务接口：
   - ChannelMemberService：查询用户角色、成员列表
   - ChannelMuteService：校验禁言状态、查询禁言到期时间
   - ChannelBlacklistService：校验黑名单状态
   - ChannelMemberBizService：成员权限校验统一入口
2. 说明这些是已存在的依赖，不需要重新实现
3. 如有需要，在ChannelContentPublishBiz中通过@Resource注入
4. 补充这些服务的方法名和参数说明（或引用EPIC-21的文档）

**验证方式**:
- design.md明确列出外部依赖
- 注入的Service真实存在且方法名正确
- 无重复造轮子

**状态**: pending

---

### BE-028 - 多频道发布事务边界未明确定义

**来源**: review-report FLAG-03
**位置**: design.md Decision 6
**优先级**: ADVISORY
**依赖**: BE-002
**类型**: 文档修复+实现

**修复步骤**:
1. 明确多频道发布的事务策略（Decision 6已说明但实现需验证）：
   - 每个频道的发布操作在独立事务中
   - 不使用大事务包裹所有频道
   - 一个频道失败不影响其他频道
   - 每个频道返回独立的success/fail状态
2. 在实现中使用PROPAGATION_REQUIRES_NEW或手动控制事务边界
3. 失败的频道记录失败原因，不整体回滚
4. 在spec中补充部分失败Scenario
5. 返回批量发布结果：每个频道的发布状态、失败原因、审核/发布结果详情

**验证方式**:
- 多频道发布中一个频道失败不影响其他
- 每个频道操作原子性（要么成功要么失败，无中间状态）
- 前端可根据返回结果展示逐频道状态
- 单元测试覆盖部分失败场景

**状态**: pending
