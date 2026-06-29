# channel-24-lifecycle-stats Fix Plan

**生成时间**: 2026-06-30
**基于报告**: drift-report-20260627-084036.md, review-report-20260627-084036.md, verify-report-20260627-084036.md, verify.md

---

## FixItem 列表

### BE-001 - Flyway SQL迁移脚本完全缺失，5张表未创建
**来源**: drift-report CRITICAL ARCH-C01/DRIFT-C01, review-report BLOCK-01, verify-report CRIT-01
**位置**: db/migration/ 目录
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端（SQL+Entity）
**修复步骤**:
1. 创建迁移脚本V4__channel_lifecycle_stats.sql（版本号V4与现有V1/V2/V3递增一致）
2. 包含5张生命周期相关表：
   - content_channel_stats：频道统计汇总表（含id、channel_id、stat_date、stat_type、subscribe_count、content_count、pv、uv、like_count、comment_count、favorite_count、share_count、valid_visit_count、create_by等）
   - content_channel_export_task：导出任务表（id、channel_id、export_type、format、fields、time_range、status、file_url、row_count、expire_time、fail_reason、create_by等）
   - content_channel_review：审核记录表（id、channel_id、application_type、applicant_id、status、channel_name、channel_icon、channel_cover、category、reviewer_id、review_time、reject_reason、create_by等）
   - content_channel_lifecycle_log：生命周期变更日志表（id、channel_id、operator_id、action_type、before_status、after_status、reason、affect_scope、notify_result、create_by等）
   - content_channel_appeal：申诉记录表（id、channel_id、punishment_type、punishment_reason、appellant_id、appeal_reason、supplementary_materials、status、handler_id、handle_reason、handle_time、create_by等）
3. 所有表添加：
   - 主键id VARCHAR(32)（雪花ID）
   - 外键字段（channel_id等）VARCHAR(32)
   - create_by、create_time、update_by、update_time审计字段
   - **del_flag TINYINT(1) DEFAULT 0** 软删除字段（BE-005）
   - 必要索引（channel_id、stat_date、status、create_time等）
4. content_channel_stats表索引设计：
   - (channel_id, stat_date, stat_type) 唯一索引
   - stat_date 索引（按时间查询）
   - 数据保留策略：保留1年统计数据，定时清理更早数据
5. 确保表名前缀统一为content_channel_，与EPIC-20/21/22/23一致
6. Entity类@TableName注解与SQL表名一一对应，实体类重命名为ContentChannelStats、ContentChannelExportTask等
**验证方式**:
- 应用启动Flyway迁移成功执行
- 5张表在数据库中正确创建
- 表名/字段类型/索引与Entity一致
- 主键/外键都是VARCHAR(32)
- 所有表有del_flag字段
- 统计汇总表索引正确，查询性能达标
**状态**: pending

---

### BE-002 - 表名前缀再次回退到channel_，与channel-23的content_前缀不一致
**来源**: drift-report DRIFT-C02/ARCH-W05, review-report BLOCK-01, verify-report COH-WARN-01
**位置**: entity/*.java, @TableName注解
**优先级**: BLOCK
**依赖**: BE-001（SQL脚本创建时统一）
**类型**: 代码修复-后端（命名重构）
**修复步骤**:
1. 所有实体类重命名：
   - ChannelStats → ContentChannelStats
   - ChannelExportTask → ContentChannelExportTask
   - ChannelReview → ContentChannelReview
   - ChannelLifecycleLog → ContentChannelLifecycleLog
   - ChannelAppeal → ContentChannelAppeal
2. 所有@TableName注解表名改为：
   - "content_channel_stats"
   - "content_channel_export_task"
   - "content_channel_review"
   - "content_channel_lifecycle_log"
   - "content_channel_appeal"
3. 同步更新Mapper、Service、Biz、Controller中所有实体引用
4. 检查常量类、枚举类中是否有硬编码表名，一并更新
**验证方式**:
- 所有实体类名以ContentChannel开头
- @TableName注解值以content_channel_开头
- 编译无错误
- 与channel-23命名风格一致
**状态**: pending

---

### BE-003 - 敏感统计接口无任何权限校验，任何登录用户可查看任意频道运营数据
**来源**: drift-report CRITICAL ARCH-C02/DRIFT-W05, verify-report CRIT-02
**位置**: ChannelStatsController.java
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端（安全）
**修复步骤**:
1. 在ChannelStatsController类或方法上添加@RequiresPermissions注解：
   - 统计查询：需要"channel:stats:view"权限
   - 频道主/管理员可查看自己管理的频道统计
   - 平台运营可查看所有频道统计
2. 在ChannelStatsBiz层添加权限校验逻辑：
   - 校验当前用户是否为该频道的频道主/管理员，或是否为平台运营
   - 无权限返回403 Forbidden
3. 为其他敏感接口同步添加权限校验：
   - ChannelExportController：导出权限（channel:export:create）
   - ChannelReviewController：审核权限（channel:review:handle）
   - ChannelLifecycleController：生命周期操作权限（channel:lifecycle:*）
   - 审计日志查询：仅运营或频道主可查看对应频道日志
4. 记录所有敏感操作访问日志
5. 参考项目其他Controller（如EPIC-22治理接口）的权限校验写法
**验证方式**:
- 普通用户调用其他频道统计接口返回403
- 频道主只能查看自己频道的统计
- 平台运营可查看所有频道统计
- 导出/审核/生命周期操作都有权限校验
- 单元测试覆盖权限校验场景
**状态**: pending

---

### BE-004 - API路径不RESTful，使用单数/channel和@RequestParam传channelId
**来源**: drift-report CRITICAL DRIFT-C03, review-report ADV-03, verify-report SUGG-01
**位置**: 所有Controller（ChannelStatsController等）
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端（API重构）
**修复步骤**:
1. 重构所有Controller路径为RESTful复数嵌套资源风格，统一使用`/api/v1/content/channels/{channelId}/`前缀：
   - 旧：`GET /api/v1/content/channel/stats/core` (RequestParam channelId)
   - 新：`GET /api/v1/content/channels/{channelId}/stats/core` (PathVariable channelId)
2. 各模块路径重构：
   - 统计模块：`/channels/{channelId}/stats/core`、`/trend`、`/interaction`、`/hot-content`、`/user-analysis`
   - 导出模块：`/channels/{channelId}/exports`（POST创建）、`/channels/{channelId}/exports/{taskId}`（GET状态）、`/channels/{channelId}/exports/history`（GET历史）、`/channels/{channelId}/exports/{taskId}/download`（GET下载）
   - 审核模块：`/channels/reviews`（GET列表，运营视角不需要channelId）、`/channels/reviews/{reviewId}`（GET详情）、`/channels/reviews/{reviewId}/action`（POST操作）
   - 生命周期模块：`/channels/{channelId}/freeze`、`/unfreeze`、`/hide`、`/restore-visibility`、`/restrict-recommend`、`/close`、`/archive`、`/channels/{channelId}/lifecycle-logs`（GET日志）
   - 申诉模块：`/channels/{channelId}/appeals`（POST提交）、`/channels/appeals`（GET列表，运营视角）、`/channels/appeals/{appealId}`（GET详情）、`/channels/appeals/{appealId}/handle`（POST处理）
   - 合并模块：`/channels/merge/validate`（POST校验）、`/channels/merge/execute`（POST执行）
3. 所有channelId参数从@RequestParam改为@PathVariable
4. HTTP方法语义正确：查询用GET，创建用POST，更新用PUT/PATCH，删除用DELETE
5. Controller类添加@Validated注解（DRIFT-W04）
6. 为所有接口添加参数校验注解（@NotNull、@Min、@Max等）
**验证方式**:
- 所有API路径使用`/api/v1/content/channels/`复数前缀
- channelId在URL路径中而非query参数
- HTTP方法语义正确
- 联调时无404错误
- 参数校验生效，非法参数返回400
- 与EPIC-21/22/23路径风格一致
**状态**: pending

---

### BE-005 - 所有表缺少软删除del_flag字段
**来源**: review-report FLAG-04
**位置**: SQL迁移脚本、Entity类
**优先级**: BLOCK
**依赖**: BE-001（SQL脚本创建时同步添加）
**类型**: 代码修复-后端（SQL+Entity）
**修复步骤**:
1. 在V4__channel_lifecycle_stats.sql中为所有5张表添加del_flag TINYINT(1) DEFAULT 0字段
2. 所有Entity类继承JeecgEntity（自动包含del_flag等审计字段），或添加@TableLogic注解的delFlag字段
3. 检查Mapper.xml查询是否自动过滤del_flag=1的记录
4. 所有删除操作为逻辑删除（更新del_flag=1）而非物理删除
**验证方式**:
- 所有表有del_flag字段
- 删除操作后记录仍在数据库但del_flag=1
- 查询不返回已删除记录
**状态**: pending

---

### BE-006 - 9个spec API契约严重不足，未明确定义HTTP method/path/req/vo
**来源**: review-report BLOCK-02
**位置**: specs/ 目录下9个spec
**优先级**: BLOCK
**依赖**: BE-004（API重构完成后同步文档）
**类型**: 文档修复-后端specs
**修复步骤**:
1. 为9个spec分别补充核心API契约表格，至少包含：
   - channel-stats-dashboard：5个统计API
   - channel-data-export：4个导出API
   - channel-review-flow：3个审核API
   - channel-freeze-unfreeze：2个冻结/解冻API
   - channel-archive：归档相关API
   - channel-merge：2个合并API
   - channel-violation-handling：违规处理API
   - channel-inactivity-governance：定时任务逻辑说明
   - channel-lifecycle-audit：日志和申诉API
2. 每个API契约包含：
   - HTTP方法
   - 完整路径（与BE-004重构后一致）
   - 请求参数（path/query/body）及类型、是否必填
   - 响应VO结构
   - 错误码定义
   - 权限要求
3. 补充错误码枚举：
   - 40910-40915：状态冲突类错误
   - 40310：权限不足
   - 40020-40021、40920、42920、50020-50021：导出类错误
4. 补充状态流转矩阵（BE-009）
**验证方式**:
- 每个spec有完整的API契约表格
- 路径/方法/参数与实际代码一致
- 前后端对齐无歧义
**状态**: pending

---

### BE-007 - 9个能力仅Stats模块相对完整，其他核心生命周期功能未验证完成度
**来源**: drift-report CRITICAL ARCH-C03, verify-report CRIT-03, verify.md（仅21/55任务有基础设施）
**位置**: 整体实现
**优先级**: CRITICAL
**依赖**: BE-001（SQL脚本）、BE-002（实体命名）
**类型**: 代码修复-后端（功能补全）
**修复步骤**:
1. 逐个验证9个能力模块的实现完整性：
   - ✅ channel-stats-dashboard：基本完整，需补充互动数据接口（BE-008）、预聚合逻辑（BE-010）
   - 🔶 channel-data-export：Controller/Entity存在，需补充导出历史接口（BE-008）、异步生成、文件清理（BE-012）
   - 🔶 channel-review-flow：需补充审核详情接口（BE-008）、关键字段修改审核、超时标记（BE-014）、通知
   - 🔶 channel-freeze-unfreeze：需补充冻结后发布拦截（与EPIC-22联动）、审计日志、申诉入口
   - ❌ channel-archive：不活跃识别、自动归档、手动归档逻辑待验证/补充
   - ❌ channel-merge：事务/数据迁移逻辑高风险，需补充Saga补偿机制（BE-011）
   - 🔶 channel-violation-handling：需补充与EPIC-23可见性规则联动
   - 🔶 channel-inactivity-governance：每日扫描定时任务待验证/补充（BE-014）
   - 🔶 channel-lifecycle-audit：需补充按频道查询日志接口（BE-008）、申诉详情接口（BE-008）、SLA监控
2. 补充6个缺失的API接口（与前端backend-issues.md对齐）：
   - 互动数据接口：GET /channels/{channelId}/stats/interaction
   - 导出历史列表：GET /channels/{channelId}/exports/history
   - 审核详情：GET /channels/reviews/{reviewId}
   - 恢复可见：POST /channels/{channelId}/restore-visibility
   - 按频道查询审计日志：GET /channels/{channelId}/lifecycle-logs（增加channelId参数支持）
   - 申诉详情：GET /channels/appeals/{appealId}
3. 补充所有缺失的业务逻辑，参考PRD验收标准
4. 更新tasks.md，准确标记哪些任务完成哪些未完成，避免虚假勾选
5. 建议：考虑拆分change为更小颗粒度（如stats单独一个change，lifecycle一个change，governance一个change），降低review和合并风险
**验证方式**:
- 所有9个能力的核心流程可跑通
- 6个缺失API接口实现完成
- 单元测试覆盖所有核心场景
- 测试覆盖率≥80%
- tasks.md真实反映完成状态
**状态**: pending

---

### BE-008 - 补充前端依赖的6个缺失API接口
**来源**: backend-issues.md（前端）, verification-review.md, verify-report WARN-02/03/04
**位置**: 各Controller/Biz/Service
**优先级**: P0
**依赖**: BE-004（API路径重构）
**类型**: 代码修复-后端（API补充）
**修复步骤**:
1. 互动数据接口（ChannelStatsBiz/Controller）：
   - GET /api/v1/content/channels/{channelId}/stats/interaction
   - 参数：channelId, startDate, endDate
   - 返回：likeCount, commentCount, favoriteCount, shareCount, validVisitCount, newContentCount, contentTypeDistribution
2. 导出历史列表接口（ChannelExportBiz/Controller）：
   - GET /api/v1/content/channels/{channelId}/exports/history
   - 参数：channelId, pageNo, pageSize
   - 返回：分页的导出任务列表（records、total等）
3. 审核详情接口（ChannelReviewBiz/Controller）：
   - GET /api/v1/content/channels/reviews/{reviewId}
   - 返回：审核申请详情，包含频道信息、申请人信息、历史审核记录
4. 恢复可见接口（ChannelLifecycleBiz/Controller）：
   - POST /api/v1/content/channels/{channelId}/restore-visibility
   - 请求体：{ reason }
   - 校验当前状态必须为Hidden，更新为Active，记录审计日志
5. 按频道查询审计日志接口（ChannelLifecycleBiz/Controller）：
   - GET /api/v1/content/channels/{channelId}/lifecycle-logs（扩展现有/logs接口）
   - 参数：channelId（可选）、operatorId、actionType、startDate、endDate、pageNo、pageSize
   - 支持按频道筛选日志
6. 申诉详情接口（ChannelLifecycleBiz/Controller）：
   - GET /api/v1/content/channels/appeals/{appealId}
   - 返回：申诉详情，包含处罚信息、申诉材料、历史处理记录
7. 为所有接口添加参数校验和权限校验
**验证方式**:
- 6个接口可正常调用
- 返回数据结构与前端API定义一致
- 参数校验和权限校验生效
- 单元测试覆盖
**状态**: pending

---

### BE-009 - 生命周期状态机合法流转矩阵不完整，非法流转无法拦截
**来源**: review-report FLAG-05
**位置**: ChannelLifecycleBiz, ChannelLifecycleStatus枚举
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-后端（状态机）
**修复步骤**:
1. 定义完整的8种状态枚举：PendingReview、Active、ReadonlyFrozen、Hidden、Archived、Merged、Closed、Deleted
2. 定义合法流转矩阵：
   - PendingReview → Active（审核通过）、Closed（审核拒绝/关闭）
   - Active → ReadonlyFrozen（冻结）、Hidden（强制隐藏）、Archived（归档）、Merged（合并）、Closed（永久关闭）、PendingReview（关键字段修改审核）
   - ReadonlyFrozen → Active（解冻）、Hidden（强制隐藏）、Closed（永久关闭）
   - Hidden → Active（恢复可见）、Closed（永久关闭）
   - Archived → Active（恢复运营，需确认）
   - Merged → 无（终态）
   - Closed → 无（终态，不可恢复）
   - Deleted → 无（终态）
3. 在ChannelLifecycleBiz中实现状态流转校验方法：
   - 每次状态变更前校验from→to是否合法
   - 非法流转返回409状态冲突错误（40915）
4. 为每个状态变更操作添加前置状态校验
5. 单元测试覆盖所有合法/非法流转场景
**验证方式**:
- 合法流转成功
- 非法流转返回409错误
- 所有状态转换都记录审计日志
- 单元测试覆盖8×8=64种流转组合
**状态**: pending

---

### BE-010 - 统计查询无预聚合/缓存逻辑，P99 <= 1秒性能无法保障
**来源**: drift-report ARCH-W01/DRIFT-W06, review-report ADV-02, verify-report WARN-01
**位置**: ChannelStatsBiz, ChannelStatsService
**优先级**: P1
**依赖**: BE-001（SQL脚本创建content_channel_stats汇总表）
**类型**: 代码修复-后端（性能优化）
**修复步骤**:
1. 实现5分钟定时预聚合任务（@Scheduled或XXL-Job）：
   - 定时（每5分钟）从原始事件表（订阅、内容、互动、浏览）聚合统计数据
   - 聚合结果写入content_channel_stats汇总表，按stat_date+stat_type维度存储
   - 支持日/周/月维度聚合
2. 查询时直接查汇总表而非实时COUNT大表
3. 添加Redis缓存热点数据：
   - 核心指标缓存5分钟
   - 热门内容缓存10分钟
   - 趋势数据缓存15分钟
4. 为hot-content接口参数添加最大值限制（DRIFT-W07）：
   - limit最大100
   - days最大365
5. 分页查询强制分页，禁止一次返回全量数据
**验证方式**:
- 大频道（10万+内容）查询P95 <= 1秒
- 数据延迟P99 <= 5分钟
- 预聚合任务正常执行，数据准确
- 缓存命中率达标
- 超过limit/days最大值的请求返回400
**状态**: pending

---

### BE-011 - 频道合并事务边界未定义，无补偿机制，并发/失败会导致数据不一致
**来源**: drift-report ARCH-W02, review-report FLAG-02
**位置**: ChannelMergeBiz
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-后端（事务/一致性）
**修复步骤**:
1. 明确合并采用Saga补偿事务模式而非大事务
2. 拆分合并步骤为独立可回滚操作：
   - Step1：校验源频道和目标频道状态（都必须是Active，不能是Deleted/Closed/Hidden/PendingReview/Merged）
   - Step2：加分布式锁（Redis锁），防止并发合并
   - Step3：迁移内容（content表channel_id更新为目标频道）
   - Step4：迁移订阅关系（subscription表channel_id更新，去重）
   - Step5：迁移其他关联数据（评论、点赞、收藏等可选）
   - Step6：源频道状态改为Merged，记录target_channel_id
   - Step7：发送通知给订阅者
   - Step8：记录审计日志
3. 为每个步骤定义回滚逻辑：
   - Step3失败：回滚已迁移的内容channel_id
   - Step4失败：回滚内容和订阅
   - Step6失败：回滚所有数据迁移，源频道恢复Active
4. 记录每步执行状态到channel_merge_log表，失败时可根据日志手动或自动补偿
5. 合并操作前必须展示影响范围预览（内容数、订阅数、历史链接影响）
6. 组织频道合并需组织最高管理员审批后才能执行
7. 添加合并操作权限校验（仅频道主+审批通过）
**验证方式**:
- 合并成功场景：源频道变为Merged，内容/订阅正确迁移
- 中途失败场景：数据一致，无半迁移状态
- 并发合并场景：分布式锁生效，只有一个能执行
- 不可合并状态（Closed/Merged等）被拦截
- 审计日志完整记录
**状态**: pending

---

### BE-012 - 数据导出无异步+内存保护，大导出可能OOM，过期文件无清理
**来源**: drift-report ARCH-W03, review-report FLAG-03, verify-report WARN-02
**位置**: ChannelExportBiz
**优先级**: P1
**依赖**: BE-001（导出任务表）
**类型**: 代码修复-后端（导出）
**修复步骤**:
1. 实现异步导出：
   - 超过10,000行必须异步处理，返回任务ID
   - 使用独立线程池执行导出任务，避免阻塞请求线程
   - 任务状态更新到content_channel_export_task表（processing/completed/failed）
2. 内存保护：
   - 使用SXSSF（Apache POI流式写入）处理Excel，避免全量加载到内存
   - CSV使用BufferedWriter逐行写入
   - 分页查询数据，每次查询1000条写入后flush
   - 单次导出行数上限100,000行，超过返回错误
3. 文件管理：
   - 导出文件存储在临时目录
   - 文件名包含UUID避免冲突
   - 下载链接带签名校验，防止越权下载
   - 文件有效期7天，过期后不可下载
4. 定时清理任务：
   - 每天凌晨清理过期文件（超过7天）和失败任务记录
   - 清理临时目录残留文件
5. 导出权限校验：仅频道主/管理员可导出对应频道数据
6. 导出频率限制：同一频道5分钟内只能创建一个导出任务
**验证方式**:
- 10万行导出不OOM，异步完成
- 文件7天后过期不可下载
- 越权下载被拦截
- 过期文件被定时清理
- 频率限制生效
**状态**: pending

---

### BE-013 - 所有接口缺少参数校验注解，部分参数无最大值限制
**来源**: drift-report ARCH-W04/DRIFT-W07
**位置**: 所有Controller、Req类
**优先级**: P2
**依赖**: BE-004（API重构）
**类型**: 代码修复-后端（参数校验）
**修复步骤**:
1. 所有Controller类添加@Validated注解
2. 所有Req类字段添加JSR-380校验注解：
   - @NotNull、@NotBlank、@NotEmpty
   - @Min、@Max（分页参数：pageNo>=1, pageSize<=100）
   - @Size、@Length
   - @Pattern（如日期格式）
3. 接口参数添加校验：
   - channelId：@NotBlank
   - timeRange：校验日期范围合法性，startDate <= endDate
   - limit：@Min(1) @Max(100)
   - days：@Min(1) @Max(365)
4. 配置全局参数校验异常处理器，返回400错误和具体字段错误信息
**验证方式**:
- 非法参数返回400，包含具体错误字段信息
- 超过pageSize=100的请求被拦截
- 日期范围不合法被拦截
**状态**: pending

---

### BE-014 - 定时任务配置未验证，不活跃扫描/统计刷新/审核超时可能未生效
**来源**: verify-report WARN-04
**位置**: 定时任务配置类
**优先级**: P2
**依赖**: BE-010（统计预聚合）、BE-007（功能补全）
**类型**: 代码修复-后端（定时任务）
**修复步骤**:
1. 验证并配置3个核心定时任务：
   - 统计刷新任务：每5分钟执行，聚合最新统计数据到汇总表（BE-010）
   - 不活跃频道扫描任务：每天凌晨1点执行，识别连续6个月无活动频道，发送提醒（EPIC-24 AC）
   - 审核超时标记任务：每小时执行，标记超过24小时未处理的审核申请
   - 导出文件清理任务：每天凌晨2点执行，清理过期文件（BE-012）
2. 使用项目现有定时任务框架（XXL-Job或Spring @Scheduled）
3. 为定时任务添加：
   - 执行日志记录
   - 异常告警（失败时通知运维）
   - 监控指标（执行时长、成功率）
   - 分布式锁（防止多实例重复执行）
4. 不活跃识别逻辑：
   - 连续6个月无新增公开内容
   - 连续6个月无有效互动
   - 连续6个月无频道信息维护
   - 排除PendingReview/Deleted/Closed/Merged状态频道
   - 个人频道：提醒1个月无改善自动归档
   - 组织频道：通知组织最高管理员
**验证方式**:
- 所有定时任务按预期cron执行
- 不活跃频道被正确识别
- 审核超时被正确标记
- 多实例部署不重复执行
- 执行异常有日志和告警
**状态**: pending

---

### BE-015 - 所有测试完全缺失（除Stats模块），TDD流程未执行
**来源**: verify.md（仅Stats相关测试存在）, verify-report（测试覆盖率约25%）
**位置**: src/test/ 目录
**优先级**: P1
**依赖**: 所有功能修复
**类型**: 代码修复-后端（测试补充）
**修复步骤**:
1. 按tasks.md要求补充所有单元测试和集成测试：
   - Biz层测试：ChannelStatsBizTest（补充）、ChannelExportBizTest、ChannelReviewBizTest、ChannelLifecycleBizTest、ChannelMergeBizTest
   - Service层测试：ChannelStatsServiceTest、ChannelExportTaskServiceTest、ChannelReviewServiceTest、ChannelLifecycleLogServiceTest、ChannelAppealServiceTest
   - Controller层集成测试：所有Controller的API测试
2. 测试必须覆盖：
   - 权限校验（BE-003）：无权限、频道主、运营不同角色
   - 状态机流转（BE-009）：所有合法/非法流转
   - 合并流程（BE-011）：成功、失败回滚、并发场景
   - 导出流程（BE-012）：同步/异步、大文件、权限、过期
   - 参数校验（BE-013）：边界值、非法参数
   - API路径（BE-004）：重构后所有API路径可访问
3. 遵循TDD流程：先写测试→红灯→实现→绿灯→重构
4. 确保单元测试覆盖率≥80%，核心路径100%覆盖
5. 禁止使用placeholder测试，必须真实Mock依赖
**验证方式**:
- mvn test运行所有测试通过
- 覆盖率报告≥80%
- 边界场景测试覆盖
- 测试包含真实断言，不是仅验证方法调用
**状态**: pending

---

## 修复优先级总览

| 优先级 | 数量 | 项 |
|--------|------|-----|
| BLOCK | 5 | BE-001, BE-002, BE-005, BE-006, BE-004(CRITICAL) |
| CRITICAL | 2 | BE-003, BE-007 |
| P0 | 1 | BE-008 |
| P1 | 5 | BE-009, BE-010, BE-011, BE-012, BE-015 |
| P2 | 2 | BE-013, BE-014 |
| **总计** | **15** | |
