# 修复计划 — circle-12-analytics-discovery（后端）

**生成时间**: 2026-06-30
**审核文档数**: 4（drift-report、review-report、verify-report、verify.md）
**总问题数**: 21
**误报过滤说明**: verify-report声称"测试类全部缺失"为误报，实际存在8个测试类且全部PASS；recordExposure接口已存在（CircleRecommendController.java:47-53）。

---

## 修复项

### B-BLOCK-001 - CircleDataController数据统计接口缺少权限校验

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleDataController.java:25-48
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 在CircleDataController中注入CircleMapper或ICircleService用于查询圈子信息
2. 在getStatistics和exportCsv方法中，通过SecureUtil.currentUser()获取当前用户
3. 查询圈子信息，校验当前用户是否为圈子创建者（createBy）或版主（需查询circle_member表中角色为MODERATOR的记录）
4. 非授权用户返回Result.error(403, "无权限访问")
5. 参考其他模块的权限校验模式（如CircleGovernanceLogController）

**验证方式**:
- 启动应用，使用普通成员账号访问数据统计接口应返回403
- 使用创建者/版主账号访问应正常返回数据
- 运行现有CircleDataControllerTest，补充权限校验测试用例

---

### B-BLOCK-002 - circle_recommend_source表缺少exposure_time字段

**来源**: drift-report-20260627-084036.md
**位置**: flyway/sql/mysql/（需新增迁移脚本）
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 新增Flyway迁移脚本V3.9.1_70__circle_recommend_source_add_exposure_time.sql
2. SQL内容：`ALTER TABLE circle_recommend_source ADD COLUMN exposure_time datetime DEFAULT NULL COMMENT '曝光时间' AFTER source_id;`
3. 为exposure_time添加索引：`ALTER TABLE circle_recommend_source ADD KEY idx_exposure_time (exposure_time);`
4. 确认CircleRecommendSourceMapper中updateExposureTime方法的SQL引用正确字段名

**验证方式**:
- 执行Flyway迁移，确认字段添加成功
- 调用recordExposure接口不报错
- 检查数据库中exposure_time字段被正确更新

---

### B-P0-001 - 定时任务中帖子数/新增帖子数/活跃用户数硬编码为0

**来源**: verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/scheduler/CircleDataAggregationScheduler.java:46-48
**优先级**: P0
**依赖**: B-BLOCK-002（同模块修改，可并行）
**类型**: 代码修复-后端

**修复步骤**:
1. 在CircleDataStatisticsMapper中新增selectPostStatsGroupByCircle方法，统计每个圈子的帖子总数和当日新增帖子数
2. 在CircleMemberMapper中新增selectActiveUserStatsGroupByCircle方法，统计当日活跃用户数（近30天有发帖/评论行为的用户）
3. 在CircleDataAggregationScheduler.aggregateData中注入所需Mapper
4. 替换硬编码的0，将查询结果填入stats.setPostCount()、stats.setNewPostCount()、stats.setActiveCount()
5. 编写对应的Mapper XML SQL

**验证方式**:
- 手动触发定时任务，检查circle_data_statistics表中post_count/new_post_count/active_count不再为0
- 运行CircleDataAggregationSchedulerTest

---

### B-P1-001 - 定时任务catch块rethrow导致调度终止

**来源**: verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/scheduler/CircleDataAggregationScheduler.java:67
**优先级**: P1
**依赖**: B-P0-001（同文件修改）
**类型**: 代码修复-后端

**修复步骤**:
1. 删除catch块中的`throw e;`语句
2. 保留log.error记录异常信息，确保定时任务下次调度继续执行
3. 参考CircleRankingScheduler.java的异常处理方式（只记录日志，不rethrow）

**验证方式**:
- 模拟定时任务执行异常，确认后续调度仍正常执行

---

### B-P1-002 - CSV导出缺少CSV注入防护

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleDataServiceImpl.java:66-77
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 在CircleDataServiceImpl中新增私有方法escapeCsvField(String value)
2. 对以=、+、-、@、\t、\r开头的字段，在前面添加单引号前缀
3. 对字段中的逗号、引号、换行符进行CSV标准转义
4. 在exportCsv方法构建CSV行时，对每个字段值调用escapeCsvField

**验证方式**:
- 使用包含公式注入字符的测试数据导出CSV，在Excel中打开不应执行公式
- 运行现有测试确认CSV格式正确

---

### B-P1-003 - 推荐列表未过滤用户已加入的圈子

**来源**: verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleRecommendServiceImpl.java:33-51
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 在applyDiversityControl之后（或之前），使用joinedCircleIds过滤candidates
2. 添加过滤逻辑：`candidates = candidates.stream().filter(c -> !joinedCircleIds.contains(c.getId())).collect(Collectors.toList());`
3. 如果过滤后候选数量不足limit，从热门圈子中补充（降级逻辑，当前已有selectHotCircles调用）

**验证方式**:
- 运行CircleRecommendServiceTest，补充已加入圈子过滤的测试用例
- 已加入某圈子的用户不应在推荐列表中看到该圈子

---

### B-P1-004 - 榜单定时任务刷新数量与Service缓存数量不一致

**来源**: verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/scheduler/CircleRankingScheduler.java:30,34
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 将CircleRankingScheduler中selectHotCircles(20)和selectNewCircles(20)的参数改为100
2. 确保与CircleRankingServiceImpl.CACHE_LIMIT(100)一致
3. 考虑将CACHE_LIMIT常量提取到共享位置或直接引用ServiceImpl的常量

**验证方式**:
- 触发定时任务后，请求limit=50的榜单能正常返回50条数据
- 运行CircleRankingSchedulerTest

---

### B-P1-005 - 日期参数缺少合法性校验

**来源**: drift-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleDataController.java:29-30
**优先级**: P1
**依赖**: B-BLOCK-001（同Controller修改）
**类型**: 代码修复-后端

**修复步骤**:
1. 在getStatistics和exportCsv方法中添加参数校验逻辑
2. 校验endDate不能早于startDate
3. 校验日期范围不能超过90天（与前端disabledDate限制一致）
4. 校验失败返回Result.error("日期参数不合法")

**验证方式**:
- 传入endDate < startDate应返回错误
- 传入超过90天的范围应返回错误
- 正常参数应正常返回数据

---

### B-P2-001 - CircleRankingController limit参数缺少校验注解

**来源**: drift-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleRankingController.java:23,30
**优先级**: P2
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 为getHotRanking和getNewRanking方法的limit参数添加@Min(1) @Max(100)注解
2. 参考CircleRecommendController的参数校验写法

**验证方式**:
- 传入limit=0或limit>100应返回参数校验错误

---

### B-P2-002 - CSV导出中文文件名编码问题

**来源**: verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleDataController.java:43
**优先级**: P2
**依赖**: B-BLOCK-001（同Controller修改）
**类型**: 代码修复-后端

**修复步骤**:
1. 查询圈子名称（注入CircleMapper）
2. 使用URLEncoder对文件名进行编码，格式：`{圈子名称}_{startDate}_{endDate}.csv`
3. 设置Content-Disposition头：`attachment; filename*=UTF-8''{encodedFilename}`
4. 参考项目中其他CSV/Excel导出的文件名处理方式

**验证方式**:
- 下载CSV文件，中文文件名在浏览器中正常显示不乱码

---

### B-P2-003 - CircleRankingServiceImpl缓存实体而非VO，无空值缓存

**来源**: verify-report-20260627-084036.md
**位置**: jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleRankingServiceImpl.java:42-46
**优先级**: P2
**依赖**: B-P1-004（缓存数量统一）
**类型**: 代码修复-后端

**修复步骤**:
1. 将getRanking方法改为先从Redis取List<CircleRankingVO.CircleRankingItem>而非List<Circle>
2. 构建VO后缓存items列表而非Circle实体列表（避免实体懒加载/循环引用问题）
3. 当查询结果为空列表时，也缓存一个短TTL（如5分钟）的空列表，防止缓存击穿
4. 调整CircleRankingScheduler同步缓存VO对象，保持一致

**验证方式**:
- 首次请求后查看Redis，缓存的是VO对象可正常序列化
- 无数据时不会频繁查库
- 运行CircleRankingServiceTest

---

### B-DOC-P1-001 - plan.md中API路径示例缺少/api/v1/content/前缀

**来源**: drift-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: plan.md:683,723,1291,1337及多处
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 全局替换plan.md中所有`/api/circle/`为`/api/v1/content/circle/`
2. 检查所有API示例路径，确保与Controller的@RequestMapping一致
3. 特别检查：`/api/circle/circle-1/data/statistics` → `/api/v1/content/circle/{circleId}/data/statistics`

**验证方式**:
- 全文搜索确认无遗漏的`/api/circle/`路径（排除非API路径的提及）

---

### B-DOC-P1-002 - plan.md内容从Task 8开始截断

**来源**: review-report-20260627-084036.md
**位置**: plan.md（Task 8榜单服务及后续任务缺失）
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 补全Task 8（榜单服务）、Task 9（榜单控制器）、Task 10（榜单定时任务）的实现步骤
2. 由于代码已实现，参考实际代码编写简要步骤说明（Service→Controller→Scheduler的创建和关键方法）
3. 标注前端联调和集成测试任务待前端change完成后执行
4. 不需要写详细代码示例，保持与前面Task风格一致的步骤列表即可

**验证方式**:
- plan.md包含完整的Task 1-10（后端部分）

---

### B-DOC-P1-003 - design.md数据统计缓存描述与实际实现不符

**来源**: drift-report-20260627-084036.md
**位置**: design.md（数据统计方式描述）
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 找到design.md中关于"定时任务聚合+缓存"的数据统计描述
2. 修改为"定时任务每30分钟聚合一次，Service层直接查询数据库返回结果"（数据量小，单条/少量记录查询，无需额外Redis缓存）
3. 说明榜单模块使用Redis缓存（2小时TTL）是因为榜单计算开销较大且访问频繁
4. 在Architecture Decisions章节更新决策记录，说明数据统计不使用缓存的理由

**验证方式**:
- design.md描述与CircleDataServiceImpl实际实现一致

---

### B-DOC-P2-001 - design.md包路径缺少.circle层级

**来源**: drift-report-20260627-084036.md
**位置**: design.md:92-121
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 将design.md中所有`org.jeecg.modules.content`包路径更新为`org.jeecg.modules.content.circle`
2. 检查File Structure章节中的包路径说明

**验证方式**:
- 包路径与实际代码一致

---

### B-DOC-P2-002 - design.md File Structure缺少scheduler/vo包说明

**来源**: drift-report-20260627-084036.md
**位置**: design.md File Structure章节
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在File Structure中补充scheduler/目录说明（定时任务：CircleDataAggregationScheduler、CircleRankingScheduler）
2. 补充vo/目录说明（视图对象：CircleDataStatisticsVO、CircleRecommendVO、CircleRankingVO）
3. 保持与现有controller/service/mapper/entity的描述格式一致

**验证方式**:
- File Structure列出所有实际存在的包

---

### B-DOC-P2-003 - plan.md数据库版本号错误

**来源**: review-report-20260627-084036.md
**位置**: plan.md:27-30
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 将plan.md中迁移脚本版本号V3.9.1_63/V3.9.1_64更新为V3.9.1_65/V3.9.1_66
2. 补充新增的V3.9.1_70迁移脚本说明（添加exposure_time字段）

**验证方式**:
- 版本号与flyway/sql/mysql/目录下实际文件名一致

---

### B-DOC-P2-004 - plan.md circle_recommend_source表结构缺少exposure_time字段

**来源**: review-report-20260627-084036.md
**位置**: plan.md:63-75
**优先级**: P2
**依赖**: B-BLOCK-002（字段添加完成后）
**类型**: 文档修复

**修复步骤**:
1. 在plan.md的circle_recommend_source表结构定义中补充`exposure_time datetime COMMENT '曝光时间'`字段
2. 字段位置在source_id之后，click_time之前

**验证方式**:
- 表结构文档与实际数据库表一致

---

### B-DOC-P2-005 - design.md未记录recordExposure接口

**来源**: drift-report-20260627-084036.md
**位置**: design.md（推荐接口列表）
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在design.md推荐模块API列表中补充recordExposure接口（POST /recommend/exposure）
2. 说明该接口用于记录推荐卡片的曝光事件，参数为sourceId
3. 这是超出初始设计的合理补充，支持前端曝光埋点

**验证方式**:
- design.md推荐接口列表包含曝光上报接口

---

### B-DOC-P2-006 - specs/circle-data-analytics缺少版主权限正向场景

**来源**: review-report-20260627-084036.md
**位置**: specs/circle-data-analytics/spec.md
**优先级**: P2
**依赖**: B-BLOCK-001（权限校验实现后）
**类型**: 文档修复

**修复步骤**:
1. 在circle-data-analytics/spec.md中补充版主访问数据统计的正向Scenario
2. 描述："Given 当前用户是圈子版主 When 请求数据统计接口 Then 正常返回数据"
3. 明确版主（MODERATOR）和创建者（CREATOR）均有权限访问

**验证方式**:
- spec包含创建者和版主两个授权角色的访问场景

---

### B-DOC-P2-007 - specs/circle-recommendation未明确私有圈子展示字段

**来源**: review-report-20260627-084036.md
**位置**: specs/circle-recommendation/spec.md:15-17
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 明确私有圈子在推荐列表中展示的字段范围：circleId、circleName、description、memberCount、category、privacyType
2. 说明不展示的字段（如非公开内容）
3. 说明私有圈子加入需走审核流程的交互逻辑

**验证方式**:
- spec中"私有圈子推荐"场景明确列出展示字段

---

## 修复依赖关系图

```
B-BLOCK-001 (权限校验) ──┬── B-P1-005 (日期校验)
                         ├── B-P2-002 (文件名编码)
B-BLOCK-002 (exposure字段) ── B-DOC-P2-004 (表结构文档)
B-P0-001 (帖子统计) ── B-P1-001 (异常rethrow)
B-P1-004 (榜单数量) ── B-P2-003 (VO缓存)
```

## 按优先级统计

| 优先级 | 代码修复 | 文档修复 | 合计 |
|--------|---------|---------|------|
| BLOCK | 2 | 0 | 2 |
| P0 | 1 | 0 | 1 |
| P1 | 5 | 3 | 8 |
| P2 | 3 | 7 | 10 |
| **合计** | **11** | **10** | **21** |
