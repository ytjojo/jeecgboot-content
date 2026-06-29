# FixPlan: circle-13-growth-incentive (后端)

> **生成时间**: 2026-06-30
> **来源**: drift-report-20260627、review-report-20260627、verify-report-20260627、verify.md
> **说明**: 前端代码已合并到主分支，本plan聚焦后端剩余问题

---

## 修复总览

| 优先级 | 数量 |
|--------|------|
| BLOCK | 3 |
| CRITICAL | 2 |
| P1 | 4 |
| P2 | 3 |

---

### BE-001 - 所有接口添加身份认证，修复水平越权漏洞
**来源**: drift-report-20260627-084036.md:140-149, verify-report-20260627-084036.md:64
**位置**: 4个Controller（CircleLevelController、MemberGrowthController、AchievementController、LeaderboardController）
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端
**状态**: done
**修复步骤**:
1. 对于查询自己成长数据的接口（getGrowthInfo、getParticipationProgress、getAchievements），从`SecureUtil.currentUser()`获取当前登录用户ID，移除@RequestParam中的userId参数
2. 对于查询圈子公共信息的接口（getLevelInfo、getLeaderboard），circleId参数保留，但需校验当前用户是否有权限访问该圈子（是否为成员）
3. 排行榜接口的currentUserId不再由前端传入，改为从当前登录用户获取
4. 添加权限校验：确保用户只能查看自己在已加入圈子内的成长数据
**验证方式**:
- 未登录用户访问接口返回401
- 用户A传入用户B的userId无法访问用户B的数据
- 非圈子成员无法查看该圈子的成长数据

---

### BE-002 - 将成长逻辑集成到业务流程中
**来源**: drift-report-20260627-084036.md:183, verify-report-20260627-084036.md:62
**位置**: CircleContentBizService及相关业务逻辑
**优先级**: BLOCK
**依赖**: BE-003（Biz层抽取后集成更清晰）
**类型**: 代码修复-后端
**状态**: skipped
**跳过原因**: 当前代码库无ApplicationEventPublisher事件机制，集成需要修改circle-11等其他模块的发帖/评论/加精/删帖核心业务逻辑，属于跨change大范围改动，不适合在本次audit-fix中完成。建议后续独立change实现，通过事件机制解耦。
**修复步骤**:
1. 在发帖成功后调用`memberGrowthService.addExperience(circleId, userId, GrowthActionEnum.POST, postId)`
2. 在评论成功后调用`memberGrowthService.addExperience(circleId, userId, GrowthActionEnum.COMMENT, commentId)`
3. 在内容加精后调用`memberGrowthService.addExperience(circleId, userId, GrowthActionEnum.FEATURED, contentId)`
4. 在内容删除/撤销加精后调用`memberGrowthService.revokeExperience(circleId, userId, action, bizId)`
5. 经验值变更后异步调用`achievementService.checkAndAward(circleId, userId)`检查徽章
6. 成员退出圈子时调用`achievementService.revoke(...)`撤销相关徽章
**验证方式**:
- 发帖后成员经验值+10、贡献值+10
- 删帖后经验值/贡献值相应扣减
- 满足徽章条件后自动获得徽章
- 经验值达到等级门槛后自动升级

---

### BE-003 - 抽取Biz层，遵循项目分层规范
**来源**: drift-report-20260627-084036.md:107-114, verify-report-20260627-084036.md:71
**位置**: 新增biz包
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端
**状态**: done
**修复步骤**:
1. 在`circle/growth/`下创建`biz/`子包
2. 创建`CircleGrowthBizService`类，编排以下业务逻辑：
   - 行为触发经验值增加 → 检查等级升级 → 异步检查徽章
   - 行为撤销经验值回退
   - 成员退出圈子处理
3. Controller注入BizService而非多个Service，BizService注入所需的Service
4. 将Controller中的业务编排逻辑移到Biz层
**验证方式**:
- Controller只做参数校验和结果返回，不直接注入多个Service
- 业务编排逻辑集中在Biz层
- 符合controller→biz→service→mapper分层规范

---

### BE-004 - 补充缺失的2个徽章初始化数据
**来源**: verify-report-20260627-084036.md:63, PENDING-ISSUES.md:33
**位置**: 新增Flyway迁移脚本或更新现有脚本
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端
**状态**: done
**修复步骤**:
1. 新增Flyway迁移脚本（如V3.9.1_XX__add_missing_achievements.sql）
2. 插入CONTENT_MILESTONE（内容里程碑，50篇内容）和SOCIAL_BUTTERFLY（社交达人，邀请5人加入圈子）的初始数据
3. 确认AchievementTypeEnum枚举是否已包含这两个类型，如缺失则补充
4. AchievementServiceImpl中补充这两个徽章的判定逻辑
**验证方式**:
- 数据库circle_achievement表有6条初始化记录
- 50篇内容后能获得CONTENT_MILESTONE徽章
- 邀请5人加入圈子后能获得SOCIAL_BUTTERFLY徽章（需依赖邀请记录表）

---

### BE-005 - 修复测试文件包路径错误
**来源**: review-report-20260627-084036.md:123, verify-report-20260627-084036.md:70
**位置**: src/test/java下的测试文件
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端
**状态**: done
**修复步骤**:
1. 将测试文件从`src/test/java/.../user/growth/`移动到`src/test/java/.../circle/growth/`
2. 更新测试类中的package声明
3. 修复因包路径变更导致的import错误
4. 运行全量单元测试确认通过
**验证方式**:
- 测试文件路径与主代码路径一致（circle/growth/）
- `mvn test -pl jeecg-module-content -am` 全部通过

---

### BE-006 - 更新design.md文档与实际代码一致
**来源**: drift-report-20260627-084036.md:118-121, review-report-20260627-084036.md:47-48
**位置**: design.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 更新D6决策：包路径从`content/user/growth/`改为`content/circle/growth/`
2. 更新D7决策：API路径划分：
   - 圈子等级：`/api/v1/content/circle/growth/level/`
   - 成员成长：`/api/v1/content/circle/member_growth/`
   - 徽章/排行榜：`/api/v1/content/circle/growth/`
3. 更新Context部分："当前代码库中尚无圈子相关Java代码"改为"基于EPIC-10/11/12已有的circle模块扩展"
4. 删除MemberGrowthVO字段表中的重复字段定义（expPoints/contributionPoints/level/levelName重复部分）
5. 更新File Structure章节，与实际目录结构一致
**验证方式**:
- design.md描述的包路径、API路径与实际代码一致
- 无字段重复定义
- Context描述符合当前代码库状态

---

### BE-007 - 同步plan.md中的Flyway版本号
**来源**: verify.md:247-253
**位置**: plan.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复
**修复步骤**:
1. 将plan.md中所有V3.9.1_63改为V3.9.1_67
2. 将plan.md中包路径user/growth改为circle/growth（与实际代码一致）
3. 更新plan.md中的Controller路径为实际路径
**验证方式**:
- plan.md中版本号与实际迁移脚本一致（V3.9.1_67）
- 包路径与实际代码一致

---

### BE-008 - 添加接口参数校验注解
**来源**: drift-report-20260627-084036.md:155-161
**位置**: 4个Controller
**优先级**: P1
**依赖**: BE-001（认证修复后参数可能变化）
**类型**: 代码修复-后端
**状态**: done
**修复步骤**:
1. 在Controller类上添加@Validated注解
2. 对必填参数添加@NotBlank/@NotNull注解
3. 对枚举类型参数（dimension、period）添加@Pattern校验或自定义枚举校验
4. 对circleId等长度敏感参数添加@Size校验
**验证方式**:
- 空circleId参数返回400错误
- 非法的dimension/period值返回400错误
- 参数校验不通过时有明确的错误提示

---

### BE-009 - 修复@Async调用问题和事务问题
**来源**: drift-report-20260627-084036.md:172
**位置**: AchievementServiceImpl
**优先级**: P1
**依赖**: BE-003（Biz层抽取后重构更方便）
**类型**: 代码修复-后端
**修复步骤**:
1. 将@Async注解的checkAndAward方法通过Biz层注入的代理调用，避免同类内调用不生效
2. 分离@Async和@Transactional：异步方法内不要直接加@Transactional，改为调用内部带事务的方法
3. 或者改为通过ApplicationEventPublisher发布事件，异步监听器处理徽章检查
**验证方式**:
- 经验值增加后徽章检查异步执行
- 徽章发放失败不影响主流程（发帖/评论成功）
- 异步执行有异常日志不吞掉

---

### BE-010 - 移除不属于本模块的CircleInviteRecord
**来源**: drift-report-20260627-084036.md:66,203
**位置**: entity/CircleInviteRecord.java及对应Mapper
**优先级**: P2
**依赖**: 确认该Entity属于其他功能模块
**类型**: 代码修复-后端
**修复步骤**:
1. 确认CircleInviteRecord是否属于其他change/模块（如邀请功能）
2. 如果不属于本模块，删除CircleInviteRecord.java和CircleInviteRecordMapper.java
3. 如果属于邀请功能，将其移动到正确的包路径下
**验证方式**:
- growth包下没有多余的Entity和Mapper
- 编译通过，无引用错误

---

### BE-011 - 优化排行榜周期常量和定时任务性能
**来源**: verify-report-20260627-084036.md:73, verify.md:257-261
**位置**: GrowthConstant.java、CircleGrowthScheduler.java
**优先级**: P2
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**:
1. 将排行榜周期常量（WEEK/MONTH/ALL）提取到LeaderboardPeriodEnum或GrowthConstant中
2. 定时任务分页查询CircleLevel，避免一次性加载所有圈子
3. 只处理最近有活动的圈子（如近7天有成长行为的圈子）
**验证方式**:
- 周期值不硬编码在多处
- 大量圈子时定时任务不会OOM
- 排行榜数据正常刷新

---

### BE-012 - 代码风格优化
**来源**: verify-report-20260627-084036.md:80-82
**位置**: MemberGrowthServiceImpl、AchievementServiceImpl
**优先级**: P2
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**:
1. 移除MemberGrowthServiceImpl中不必要的null检查（@Resource注入的Mapper不可能为null）
2. 替换LambdaQueryWrapper中的全限定类名为import后的短类名
3. period参数添加默认值容错或枚举校验
**验证方式**:
- 代码无多余的null判断
- 无全限定类名使用
- 代码风格符合项目规范
