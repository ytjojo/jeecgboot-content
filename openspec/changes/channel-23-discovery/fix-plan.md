# 修复计划 — channel-23-discovery

**生成时间**: 2026-06-30
**审核文档数**: 3 (drift-report/review-report/verify-report) + backend-issues(前端记录)
**总问题数**: 23
**整体评估**: ❌ BLOCK问题 - SQL完全缺失、3个后端API阻塞前端、API路径/分层违规

---

## 修复项

### BE-001 - Flyway SQL迁移脚本完全缺失，7张表未创建

**来源**: drift-report CRITICAL ARCH-C01, verify-report CRIT-01, review-report BLOCK-01/FLAG-01
**位置**: db/migration/ 目录
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端（SQL+Entity）

**修复步骤**:
1. 创建迁移脚本V3__channel_discovery_tables.sql（版本号V3与现有V1/V2递增一致，不使用V3.9.1_63__）
2. 包含7张发现相关表：
   - content_channel_category：频道分类（含id、name、parent_id、path、level、sort、status、icon、description）
   - content_channel_tag：频道标签（id、channel_id、name、use_count、create_by等）
   - content_channel_tag_relation：标签内容关联（tag_id、content_id、channel_id）
   - content_channel_editorial_pick：编辑精选（channel_id、content_id、title、summary、sort、start_time、end_time、status）
   - content_channel_recommendation_user：用户不感兴趣反馈（user_id、channel_id、create_time）
   - content_channel_ranking_snapshot：榜单快照（ranking_type、period_type、snapshot_date、data_json）
   - content_channel_recommendation_feedback：推荐反馈（可选，搜索反馈表单独考虑）
3. 所有表添加：
   - 主键id VARCHAR(32)（雪花ID）
   - 外键字段（channel_id、parent_id等）VARCHAR(32)
   - create_by、create_time、update_by、update_time审计字段
   - **del_flag TINYINT(1) DEFAULT 0** 软删除字段（BE-005）
   - 必要索引（name、status、parent_id等）
4. 分类表path字段设计：
   - 存储父级ID路径如"0,parentId1,parentId2,"方便查询
   - 长度限制：最多4级分类，path最长128字符
   - 添加循环引用检测（BE-009）
5. 确保表名前缀统一为content_channel_，与EPIC-20/21/22一致
6. Entity类@TableName注解与SQL表名一一对应

**验证方式**:
- 应用启动Flyway迁移成功执行
- 7张表在数据库中正确创建
- 表名/字段类型/索引与Entity一致
- 主键/外键都是VARCHAR(32)
- 所有表有del_flag字段
- 分类层级测试验证4级限制和path生成正确

**状态**: pending

---

### BE-002 - 所有管理接口缺权限校验注解，存在越权风险

**来源**: drift-report CRITICAL ARCH-C02, verify-report CRIT-03
**位置**: 所有运营后台Controller（分类CRUD、精选管理）
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 区分公开接口和管理接口：
   - 公开接口（无需登录）：发现页聚合、分类树浏览、搜索、排行榜、公开精选、分类浏览
   - 管理接口（需运营权限）：分类增删改、启用/停用分类、标签CRUD、精选CRUD、榜单配置
2. 为所有管理接口添加@RequiresPermissions注解：
   - 分类管理：content:channel:category:add/edit/disable/enable
   - 标签管理：content:channel:tag:add/edit/delete
   - 精选管理：content:channel:pick:add/edit/delete
   - 榜单管理：content:channel:ranking:config
3. 在Biz/Service层补充权限校验（防止前端绕过）：
   - 管理接口校验用户是否有运营角色
   - 标签编辑/删除校验是否为频道管理员（频道级标签vs平台级分类需区分）
4. 公开接口允许匿名访问但需做限流（BE-012）
5. 不感兴趣反馈、推荐个性化等用户相关接口要求登录

**验证方式**:
- 未登录用户访问管理接口返回401/403
- 普通用户无法调用分类/精选管理接口
- 运营角色可正常管理
- 公开接口可正常访问但有流控
- 单元测试覆盖权限场景

**状态**: pending

---

### BE-003 - Controller直接返回Entity，未使用VO隔离

**来源**: drift-report CRITICAL ARCH-C03, verify-report CRIT-02
**位置**: ContentChannelCategoryController等所有Controller
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 为所有API创建对应VO类：
   - ChannelCategoryVO：分类列表/树节点（id、name、parentId、level、sort、status、icon、hasChildren、children[]）
   - ChannelCategorySimpleVO：分类选择器简化版
   - ChannelTagVO：标签VO
   - ChannelEditorialPickVO：精选VO
   - ChannelRankingVO：榜单项VO
   - ChannelCardVO：频道卡片VO（发现页、搜索结果用）
   - ChannelDiscoveryHomeVO：发现页聚合VO
   - ChannelSearchResultVO：搜索结果VO
   - BrowseChannelVO：分类浏览结果VO
2. 在Biz/Service层实现Entity→VO转换：
   - 使用ConvertUtil或手动转换
   - 敏感字段（create_by、update_by、del_flag、内部状态）不暴露
   - 分类树组装在Biz层完成（构建children层级）
3. 所有Controller返回值统一改为Result<VO>或Result<IPage<VO>>
4. 禁止Controller方法返回Result<Entity>、Result<IPage<Entity>>
5. BI-5精选分页接口直接返回ChannelEditorialPickVO

**验证方式**:
- 所有API响应无内部字段泄露
- Controller返回类型都是VO
- 分类树正确组装children层级
- TypeScript类型与VO字段对齐
- 单元测试验证转换逻辑

**状态**: pending

---

### BE-004 - API路径使用单数+RPC风格，需重构为RESTful复数资源风格

**来源**: drift-report DRIFT-C02/C03, review-report BLOCK-01
**位置**: 所有Controller @RequestMapping
**优先级**: BLOCK
**依赖**: BE-008（Controller分层重构时一并修改）
**类型**: 代码修复-后端+文档

**修复步骤**:
1. 统一所有Controller路径为复数资源风格，保持前缀/api/v1/content/：
   - /api/v1/content/channel/category/* → /api/v1/content/channels/categories/*（平台分类）
   - /api/v1/content/channel/tag/* → /api/v1/content/channels/{channelId}/tags/*（频道标签）
   - /api/v1/content/channel/editorial-pick/* → /api/v1/content/channels/{channelId}/editorial-picks/*
   - /api/v1/content/channel/search → /api/v1/content/channels/search
   - /api/v1/content/channel/ranking/* → /api/v1/content/rankings/*（榜单是全局的？需明确）
   - /api/v1/content/channel/recommendation/* → /api/v1/content/channels/recommendations/*
   - /api/v1/content/channel/discovery/home → /api/v1/content/channels/discovery/home
   - /api/v1/content/channel/browse/* → /api/v1/content/channels/browse/*
   > 注意：分类是平台级资源还是频道级？需明确。平台分类用/channels/categories，频道标签用/channels/{channelId}/tags。
2. HTTP方法语义化（遵循项目写操作统一POST的惯例，但查询用GET）：
   - 查询列表/详情：@GetMapping
   - 创建：@PostMapping
   - 更新：@PostMapping("/update")或按项目惯例
   - 启用/禁用：@PostMapping("/{id}/enable")、@PostMapping("/{id}/disable")
   - 删除：@PostMapping("/delete")
   > 遵循JeecgBoot项目惯例，写操作统一用POST，不用PUT/PATCH/DELETE，与其他模块保持一致。
3. channelId/pk等ID通过@PathVariable传递，不通过@RequestParam
4. 更新所有spec和design.md中的API路径定义
5. 删除重复Controller（如有）
6. 补充BI-1到BI-6缺失的Controller端点（BE-006）

**验证方式**:
- 所有路径使用复数名词，资源层级清晰
- 路径参数通过URL传递
- 查询用GET，写操作用POST（项目惯例）
- 无/create、/update、/disable等RPC风格后缀？
  > 注：JeecgBoot项目惯例可能允许/create、/update这样的路径，参考现有Controller风格。
- 前后端联调路径一致无404
- 所有单元测试更新通过

**状态**: pending

---

### BE-005 - 所有表缺少del_flag软删除字段

**来源**: drift-report ARCH-W04, review-report FLAG-02
**位置**: V3__channel_discovery_tables.sql 所有表
**优先级**: BLOCK
**依赖**: BE-001（建表时一并添加）
**类型**: 代码修复-后端（SQL+Entity）

**修复步骤**:
1. 为7张表都添加del_flag字段：
   `del_flag TINYINT(1) DEFAULT 0 COMMENT '删除标记(0-正常,1-已删除)'`
2. Entity类添加delFlag字段，加上@TableLogic注解
3. 确保MyBatis Plus查询自动过滤del_flag=1的记录
4. 删除操作改为逻辑删除（调用removeById或update set del_flag=1）
5. 分类/标签停用状态用status字段，删除用del_flag，两者概念区分：
   - status=DISABLED：停用，不可被选择但历史数据保留
   - del_flag=1：已删除，不展示在任何列表
6. 精选过期用end_time控制，不用del_flag

**验证方式**:
- 删除操作执行逻辑删除而非物理删除
- 查询接口不返回已删除记录
- 停用/启用/删除语义清晰分离
- 单元测试验证软删除逻辑

**状态**: pending

---

### BE-006 - 补充backend-issues.md列出的6个缺失API端点

**来源**: frontend backend-issues.md BI-1~BI-6
**位置**: 相关Controller和Service
**优先级**: BLOCK
**依赖**: BE-003, BE-004
**类型**: 代码修复-后端

**修复步骤**:
1. **BI-1: 发现页聚合接口**：
   - 创建ContentChannelDiscoveryController
   - 映射GET /api/v1/content/channels/discovery/home
   - 从登录态获取userId（未登录则不传，返回非个性化推荐）
   - 返回ChannelDiscoveryHomeVO：包含recommendations、hotRanking、editorialPicks、categories
   - 调用ContentChannelDiscoveryBiz.getDiscoveryData(userId)
2. **BI-2: 分类启用接口**：
   - IContentChannelCategoryService添加enableCategory(String categoryId)
   - Controller添加POST /api/v1/content/channels/categories/{id}/enable
   - 逻辑：status改为ENABLED，刷新分类树缓存
3. **BI-3: 标签编辑接口**：
   - 创建ChannelTagUpdateReq（tagId、name）
   - IContentChannelTagService添加updateTag(ChannelTagUpdateReq req)
   - Controller添加POST /api/v1/content/channels/{channelId}/tags/update
   - 校验：名称非空、长度≤20、同频道下不重复
4. **BI-4: 搜索结果反馈接口（P1）**：
   - 添加POST /api/v1/content/channels/search/feedback
   - 入参：query、helpful(boolean)
   - 从登录态获取userId
   - 记录搜索反馈用于优化
5. **BI-5: 精选管理分页列表接口（P1）**：
   - 创建ChannelEditorialPickQueryReq（status、pageNo、pageSize）
   - IContentChannelEditorialPickService添加pageQuery方法
   - Controller添加GET /api/v1/content/channels/{channelId}/editorial-picks/page
   - 支持按状态筛选
6. **BI-6: BrowseController分类浏览实现（P2）**：
   - 实现ContentChannelBrowseController.browseByCategory方法体
   - 调用ChannelVisibilityService过滤
   - 按分类ID查询公开频道列表并分页返回
7. 所有新接口都要通过Biz层编排，添加权限校验（管理接口），返回VO
8. 优先实现BI-1/BI-2/BI-3（P0阻塞前端），BI-4/BI-5/BI-6可后续迭代

**验证方式**:
- 6个缺失端点均可正常访问
- BI-1发现页返回聚合数据
- 分类启用/停用正常工作
- 标签可编辑重命名
- 精选管理可分页按状态筛选
- 分类浏览返回经过可见性过滤的频道列表
- 所有接口有权限控制
- 单元测试覆盖每个端点

**状态**: pending

---

### BE-007 - Controller绕过Biz层直接注入Service，分层架构违规

**来源**: drift-report ARCH-W01, verify-report WARN-01/COH-WARN-01
**位置**: 所有Controller
**优先级**: FLAG
**依赖**: BE-004（路径重构时一并重构分层）
**类型**: 代码修复-后端（架构重构）

**修复步骤**:
1. 严格遵循Controller → Biz → Service → Mapper分层：
   - Controller：参数校验、VO转换、HTTP响应封装
   - Biz层：跨Service编排、事务控制、缓存刷新、聚合逻辑
   - Service层：单表CRUD、领域逻辑
   - Mapper层：数据访问
2. 重构ContentChannelCategoryController：
   - 移除直接注入IContentChannelCategoryService
   - 改为注入ContentChannelCategoryBiz
   - 分类树组装、状态变更后刷新缓存等逻辑移到Biz层
3. 重构其他Controller：
   - 所有写操作必须经过Biz层
   - 简单查询可直接调用Service但建议也走Biz保持统一
4. Biz层实现：
   - ContentChannelCategoryBiz：分类CRUD编排、树构建、缓存管理
   - ContentChannelDiscoveryBiz：发现页数据聚合（已有）
   - ContentChannelSearchBiz：搜索编排（如需要）
   - ContentChannelRankingBiz：榜单计算编排
   - ContentChannelTagBiz：标签管理编排
   - ContentChannelEditorialPickBiz：精选管理编排
5. 确保跨Service操作在Biz层加@Transactional事务注解

**验证方式**:
- Controller只注入Biz，不直接注入Mapper
- Biz注入多个Service编排跨聚合逻辑
- Service单表逻辑不依赖Biz或其他Service
- 事务边界在Biz层
- 分层依赖方向正确
- 相关测试通过

**状态**: pending

---

### BE-008 - specs缺少完整API契约定义

**来源**: review-report BLOCK-02
**位置**: specs/ 7个spec文件
**优先级**: BLOCK
**依赖**: BE-004（API路径确定后补充）
**类型**: 文档修复

**修复步骤**:
1. 在每个spec.md中补充完整API契约表格：
   - HTTP方法、完整路径
   - 请求参数（路径参数、Query、RequestBody）
   - 响应体结构（VO字段说明和类型）
   - 错误码列表
   - 是否需要登录、权限要求
   - 是否有缓存/限流
2. 覆盖7个capability：
   - channel-taxonomy：分类/标签基础CRUD API
   - channel-category-browse：分类浏览、分类树
   - channel-tags：标签CRUD、标签筛选
   - channel-search：搜索、搜索建议、搜索反馈
   - channel-recommendations：推荐列表、不感兴趣
   - channel-rankings：各类榜单、榜单快照
   - channel-editorial-picks：精选CRUD、活跃精选列表
3. 明确错误码区间：
   - 400xxx：参数错误（分类不存在、标签重名）
   - 403xxx：权限不足
   - 404xxx：资源不存在
   - 429xxx：请求过于频繁
   - 500xxx：服务器内部错误
4. 补充VO字段定义和类型说明
5. 明确哪些接口是公开的、哪些需要管理权限

**验证方式**:
- 每个API在spec中有完整定义
- 前后端可基于spec对齐接口无需额外沟通
- 所有错误码有明确定义
- spec与最终Controller实现一一对应
- 字段类型和必填性清晰

**状态**: pending

---

### BE-009 - design.md重复定义Decision D1，且缺少path字段长度校验

**来源**: review-report BLOCK-03, FLAG-05
**位置**: design.md:32-42, 44-61
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复+实现校验

**修复步骤**:
1. 合并重复的Decision D1"分类树存储方案"：
   - 保留最终版本：邻接表模型 + path字段冗余（用于方便查询子树）
   - 删除重复的Decision章节
   - 明确最终选型：parent_id + path + level，不使用闭包表
2. 在Decision中补充path字段设计：
   - path格式：逗号分隔的ID路径，如",rootId,parentId,currentId,"
   - 每级ID长度32字符，最多4级 → 最大长度约135字符
   - 字段类型VARCHAR(255)留足冗余
3. 实现层级循环引用检测逻辑：
   - 更新分类时检查不能将自己设为自己的父级
   - 不能将子分类设为父级（避免循环）
   - 最多支持4级分类，level校验
4. 分类移动时更新所有子分类的path和level字段
5. 关闭Open Questions中相关问题
6. 补充path生成和更新逻辑的单元测试场景

**验证方式**:
- design.md无重复Decision章节
- path字段格式和长度约束明确
- 循环引用检测有测试覆盖
- 超过4级分类无法创建
- 移动分类后子分类path正确更新

**状态**: pending

---

### BE-010 - Service接口使用I前缀，与EPIC-20/21/22命名不一致

**来源**: drift-report DRIFT-W06, verify-report COH-WARN-02
**位置**: IContentChannelCategoryService等接口
**优先级**: FLAG
**依赖**: BE-007（分层重构时一并处理）
**类型**: 代码修复-后端（命名统一）

**修复步骤**:
1. 统一Service接口命名风格：去掉I前缀
   - IContentChannelCategoryService → ContentChannelCategoryService
   - IContentChannelTagService → ContentChannelTagService
   - IContentChannelEditorialPickService → ContentChannelEditorialPickService
   - IContentChannelSearchService → ContentChannelSearchService
   - IContentChannelVisibilityService → ContentChannelVisibilityService
   - 其他所有IContentChannel*Service同理
2. 实现类保持XxxServiceImpl命名不变
3. 更新所有注入点（Controller、Biz、其他Service）
4. 重命名文件时注意git history保留
5. 与EPIC-20/21/22的Service命名风格保持完全一致

**验证方式**:
- 无IContentChannel*Service接口
- 所有Service命名为ContentChannel*Service
- 编译通过，注入正常
- 全模块测试通过
- 与其他EPIC模块命名风格一致

**状态**: pending

---

### BE-011 - 定时任务缺少分布式锁保护

**来源**: drift-report ARCH-W02, review-report ADV-03
**位置**: scheduled/ 目录定时任务类
**优先级**: FLAG
**依赖**: BE-007（Biz层实现后）
**类型**: 代码修复-后端

**修复步骤**:
1. 为榜单每日更新、推荐缓存刷新等定时任务添加Redis分布式锁：
   - 锁key：lock:channel:ranking:daily、lock:channel:recommendation:refresh等
   - 锁过期时间：10分钟（长于单次任务最大执行时间）
   - tryLock获取锁失败则跳过本次执行
2. 实现方式：
   - 使用RLock，添加@Scheduled注解
   - tryLock(0, 10, TimeUnit.MINUTES)
   - finally块中unlock释放锁
   - 锁获取失败记录warn日志
3. 任务执行幂等性校验：
   - 按日期生成快照，当日已有快照则不重复生成
   - 推荐缓存刷新使用版本号或时间戳避免重复
4. 定时任务目录scheduled/已存在，更新design.md File Structure从task/改为scheduled/
5. 添加定时任务监控和告警：
   - 任务执行时间过长告警
   - 任务失败告警
6. 补充分布式锁并发单元测试

**验证方式**:
- 多实例部署定时任务不重复执行
- 锁正常释放，不会死锁
- 异常情况锁自动过期
- 榜单每日只生成一份快照
- 幂等性保证重复执行无副作用
- 并发测试通过

**状态**: pending

---

### BE-012 - 公开搜索/发现接口缺少查询超时和限流配置

**来源**: drift-report ARCH-W03, review-report FLAG-03
**位置**: 搜索Controller、发现页Controller
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 添加查询超时限制：
   - 搜索接口设置查询超时3秒（可配置）
   - 超时返回降级结果或友好提示
2. 强制分页限制：
   - pageSize最大50条，防止一次查询大量数据
   - 默认pageSize=10/20
3. 添加限流保护：
   - 公开搜索接口：同一用户/IP每分钟最多30次
   - 发现页聚合接口：同一用户每分钟最多60次
   - 使用Redis限流或Guava RateLimiter
   - 超限返回429 Too Many Requests
4. 搜索性能优化：
   - MySQL FULLTEXT索引（如果使用MySQL搜索）
   - 热门搜索词缓存（5分钟TTL）
   - 空查询/短查询（<2字符）快速返回
   - 添加慢查询日志监控
5. 降级策略：
   - 聚合接口某部分失败（如推荐服务异常）时，不返回500，返回其他部分+降级标记
   - 参考前端已有降级逻辑，后端提供部分可用数据
6. P99性能保障措施：
   - 索引优化
   - 热门数据缓存
   - 查询超时熔断

**验证方式**:
- 搜索超过3秒返回超时或降级
- pageSize超过50被截断或拒绝
- 高频请求返回429
- 慢查询有日志
- 部分服务降级时聚合接口仍返回可用数据
- 单元测试覆盖限流和降级场景

**状态**: pending

---

### BE-013 - ChannelVisibilityService需验证所有公开入口都经过过滤

**来源**: drift-report 分层合规性检查, verify-report WARN-02
**位置**: 所有公开发现接口（search/browse/recommendation/ranking/home）
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端（验证+修复）

**修复步骤**:
1. 检查所有公开发现接口是否调用ChannelVisibilityService过滤：
   - 发现页home聚合
   - 分类浏览browseByCategory
   - 搜索search
   - 推荐列表
   - 排行榜
   - 精选列表
   - 分类树
2. 过滤规则：
   - 私有频道（PRIVATE）：仅成员可见，公开入口不返回
   - 冻结/禁用/审核中频道：公开入口不返回
   - 已删除频道（del_flag=1）：不返回
3. 实现方式：
   - 在Service层统一添加可见性过滤
   - 或在MyBatis XML中统一拼接过滤条件
   - 推荐在Biz层统一调用visibilityService过滤
4. 分类树本身是公开的（平台分类），不需要过滤
5. 榜单只统计公开可见频道的数据
6. 添加单元测试验证：
   - 私有频道不出现在公开列表
   - 冻结频道不出现在公开列表
   - 管理员视角可看到更多（按需）

**验证方式**:
- 所有公开发现接口都经过可见性过滤
- 私有/冻结频道无法被非成员搜索/浏览到
- 分类浏览只返回公开频道
- 排行榜只统计公开频道数据
- 测试覆盖各种状态频道的可见性

**状态**: pending

---

### BE-014 - Controller缺少@Validated类级注解和参数校验

**来源**: drift-report DRIFT-W03
**位置**: 所有Controller
**优先级**: FLAG
**依赖**: BE-003（创建Req时一并添加）
**类型**: 代码修复-后端

**修复步骤**:
1. 所有Controller类添加@Validated注解
2. 为所有Req DTO添加JSR-303校验注解：
   - @NotBlank、@NotNull、@Size、@Min、@Max、@Length等
3. 定义校验分组：Create.class、Update.class
4. 方法参数添加@Validated(Create.class)等
5. 补充校验规则：
   - 分类名称：非空，2-20字符
   - 标签名称：非空，2-20字符，同频道不重复
   - 精选标题：非空，≤50字符
   - 分页参数：current ≥ 1，size 1-50
   - 搜索关键词：≤50字符
   - 排序参数：只能是允许的值（hot/new等）
6. 全局异常处理器捕获校验异常返回友好提示
7. 校验失败返回400错误和具体字段原因

**验证方式**:
- 缺失必填参数返回400
- 参数格式错误（超长、非法值）返回400
- 校验错误提示包含字段名和具体原因
- 全局统一异常格式
- 单元测试覆盖参数校验场景

**状态**: pending

---

### BE-015 - 3个Open Question未给出最终决策

**来源**: review-report FLAG-04
**位置**: design.md Open Questions
**优先级**: FLAG
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 与产品/设计确认3个Open Question并更新design.md：
   - Q1：推荐算法口径（规则推荐占比 vs 协同过滤）→ 第一阶段规则推荐明确：
     * 新用户（无历史）：热门榜 + 精选
     * 有历史用户：关注频道内容 + 相似频道内容 + 热门
   - Q2：编辑精选与算法推荐差异化展示 → 明确：
     * 精选有专门标签"编辑精选"
     * 位置在推荐流前3位有固定坑位或置顶
   - Q3：活跃度扣分规则 → 明确：
     * 频道30天无新内容降权
     * 90天无内容不在推荐中展示
     * 具体扣分权重可配置（配置文件或数据库）
2. 在design.md中补充决策和默认值
3. 关闭Open Questions章节
4. 基于决策实现推荐和排序逻辑
5. 如果无法立即确认，使用保守默认值并标注TODO后续调整

**验证方式**:
- design.md无未关闭的Open Questions
- 推荐逻辑和活跃度扣规则有明确实现
- 精选与普通推荐有明确区分
- 默认值可运行不阻塞开发

**状态**: pending

---

### BE-016 - 榜单计算权重硬编码，无可配置性

**来源**: review-report ADV-01
**位置**: 榜单计算Service
**优先级**: ADVISORY
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 将榜单权重配置从硬编码改为可配置：
   - 方式一：application.yml配置（简单快速）
   - 方式二：数据库配置表（运营可调整）
   - 本期建议使用配置文件方式
2. 配置项示例：
   ```yaml
   channel:
     ranking:
       hot:
         contentWeight: 0.4  # 内容量权重
         memberWeight: 0.3   # 成员数权重
         activityWeight: 0.3 # 活跃度权重
       weights可动态刷新（@RefreshScope或定时刷新）
   ```
3. 定义配置类ChannelRankingProperties
4. 计算逻辑从配置读取权重而非硬编码
5. 添加权重合法性校验（总和=1.0，各权重0-1之间）
6. 文档中说明权重调整方式

**验证方式**:
- 权重不硬编码在代码中
- 修改配置无需重新打包即可生效（或明确重启生效）
- 权重和等于1校验
- 不同权重配置榜单结果正确变化
- 单元测试使用固定权重验证

**状态**: pending

---

### BE-017 - 实体命名ContentChannel*前缀与其他模块Channel*不一致

**来源**: drift-report DRIFT-W02, verify-report COH-WARN-03
**位置**: 所有Entity类
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档记录（不修改代码）

**修复步骤**:
1. 记录命名差异：
   - EPIC-20/21/22实体使用Channel前缀（Channel、ChannelMember等）
   - EPIC-23实体使用ContentChannel前缀（ContentChannelCategory等）
2. 决策：保持ContentChannel前缀命名，理由：
   - 已有大量代码使用此命名
   - 改名风险高且无实质收益
   - ContentChannel明确表示"内容社区-频道"领域
3. 在design.md中记录此命名约定，后续discovery相关模块统一使用ContentChannel前缀
4. 不进行全模块重构，避免不必要的risk
5. 在AGENTS.md或模块文档中说明此命名差异，避免新开发者困惑

**验证方式**:
- design.md中记录命名约定
- 新代码保持一致使用ContentChannel前缀
- 无混淆

**状态**: pending

---

### BE-018 - 定时任务测试缺少分布式锁并发场景覆盖

**来源**: review-report ADV-03
**位置**: 测试目录
**优先级**: ADVISORY
**依赖**: BE-011
**类型**: 代码修复-后端（测试补充）

**修复步骤**:
1. 补充定时任务单元测试：
   - 正常场景：单实例执行成功
   - 并发场景：多线程/多实例同时触发，只有一个执行
   - 异常场景：任务执行中抛出异常，锁正确释放
   - 幂等场景：重复执行不产生重复数据
2. 测试榜单每日更新：
   - 当日无快照：正确生成
   - 当日已有快照：跳过
3. 测试推荐缓存刷新：
   - 刷新前缓存存在，刷新后缓存更新
   - 并发刷新不产生问题
4. 使用Mock Redis测试锁机制
5. 测试覆盖锁超时自动释放场景

**验证方式**:
- 并发测试多实例只有一个执行成功
- 异常锁不泄漏
- 幂等测试通过
- 覆盖率达标

**状态**: pending

---

### BE-019 - 精选有效期过期自动下线逻辑

**来源**: PRD/design隐含需求
**位置**: ContentChannelEditorialPickService/Biz
**优先级**: FLAG
**依赖**: BE-011
**类型**: 代码修复-后端

**修复步骤**:
1. 实现精选有效期逻辑：
   - start_time > now()：未到展示时间，不返回
   - start_time ≤ now() ≤ end_time：生效中，在精选列表展示
   - end_time < now()：已过期，不返回在活跃精选列表
2. listActivePicks()查询条件：
   - status = ENABLED
   - start_time ≤ NOW()
   - end_time ≥ NOW() 或 end_time IS NULL（永久有效）
3. 可添加定时任务每日凌晨清理过期精选状态（可选，过期靠查询条件过滤即可）
4. 管理后台列表显示精选状态：未开始/生效中/已过期
5. 返回给前端的VO中包含剩余有效天数

**验证方式**:
- 未开始的精选不出现在用户端精选列表
- 已过期精选不出现在用户端
- 管理后台可看到所有状态精选
- 管理员可创建永久有效精选（end_time不传或设为很大值）
- 单元测试覆盖时间边界

**状态**: pending

---

### BE-020 - 分类树缓存设计和刷新机制

**来源**: 分类树变更后需要实时可见
**位置**: ContentChannelCategoryBiz
**优先级**: FLAG
**依赖**: BE-007
**类型**: 代码修复-后端

**修复步骤**:
1. 分类树添加Redis缓存：
   - 缓存key：channel:category:tree
   - 缓存TTL：1小时（或永久有效直到变更）
   - 缓存值：完整分类树VO列表
2. 缓存刷新时机：
   - 分类新增/更新/启用/停用/删除时主动删除缓存
   - 下次查询时重建缓存
3. 注意：
   - 分类是平台级数据，不是频道级，缓存key不需要channelId
   - 缓存一致性：先更新数据库再删除缓存（Cache-Aside模式）
4. 前端获取分类树时优先返回缓存版本
5. 添加缓存穿透保护（不存在时缓存空值短时间）
6. 管理端修改分类后用户端立即可见（缓存主动失效）

**验证方式**:
- 第一次查询构建缓存
- 第二次查询命中缓存性能提升
- 修改分类后缓存自动失效
- 用户端看到最新分类树
- 单元测试验证缓存行为

**状态**: pending

---

### BE-021 - 不感兴趣反馈逻辑实现验证

**来源**: verify-report Requirement覆盖
**位置**: ContentChannelRecommendationService
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-后端（验证+完善）

**修复步骤**:
1. 验证不感兴趣功能正确实现：
   - 用户点击"不感兴趣"记录channelId + userId
   - 推荐列表排除用户标记不感兴趣的频道
   - 不感兴趣操作可撤销（可选）
2. 补充校验：
   - 不能对不存在的频道反馈
   - 重复反馈幂等处理（不重复插入）
   - 未登录用户不能操作（返回401）
3. 推荐算法考虑不感兴趣反馈：
   - 直接过滤不感兴趣频道
   - 降权相似频道（如果算法支持）
4. 添加单元测试覆盖：
   - 正常反馈
   - 重复反馈幂等
   - 未登录禁止
   - 反馈后推荐列表排除

**验证方式**:
- 标记不感兴趣的频道不在推荐中出现
- 重复操作不报错
- 未登录无法操作
- 单元测试通过

**状态**: pending

---

### BE-022 - 搜索建议/热词功能补充

**来源**: PRD搜索体验要求
**位置**: ContentChannelSearchService
**优先级**: ADVISORY
**依赖**: BE-012
**类型**: 代码修复-后端

**修复步骤**:
1. 实现搜索建议接口：
   - GET /api/v1/content/channels/search/suggestions
   - 参数：keyword(前缀匹配)
   - 返回：匹配的频道名/分类名/标签名建议列表（最多10条）
2. 实现热词接口：
   - GET /api/v1/content/channels/search/hot-keywords
   - 返回最近7天热门搜索词（top 20）
   - 数据来自搜索记录统计
3. 搜索记录表：
   - content_channel_search_log（可选，可后续迭代）
   - 记录：user_id、keyword、result_count、search_time
   - 用于统计热词和搜索质量优化
4. 本期如果未做搜索日志表，热词可返回预设的热门关键词
5. 搜索建议优先匹配频道名称，其次标签，其次分类

**验证方式**:
- 输入前缀返回匹配建议
- 热词列表返回热门搜索
- 搜索建议响应快速（<100ms）
- 单元测试覆盖

**状态**: pending

---

### BE-023 - 更新design.md定时任务目录从task/改为scheduled/

**来源**: drift-report DRIFT-W01
**位置**: design.md File Structure
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新design.md File Structure章节：
   - 将task/目录改为scheduled/
   - 列出定时任务类：ChannelRankingDailyTask、ChannelRecommendationRefreshTask等
2. 确认代码实际放在scheduled/目录下
3. 文档中其他提到task/的地方一并更新
4. 保持与实际代码结构一致

**验证方式**:
- design.md目录结构与实际代码一致
- 无task/目录引用
- 开发者按文档可正确找到文件

**状态**: pending
