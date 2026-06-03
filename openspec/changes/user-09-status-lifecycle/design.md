## Context

当前内容社区模块（jeecg-module-content）缺少统一的用户状态管理。用户处罚逻辑分散在各业务模块中，无审计日志，解禁流程不完善。需要建立统一的状态机引擎、功能限制策略、审计系统和恢复机制。

现有架构基于 Spring Boot + MyBatis Plus，分层为 Controller → BizManageService → Service → Mapper。内容社区模块位于 `jeecg-boot/jeecg-boot-module/jeecg-module-content/`。

## Goals / Non-Goals

**Goals:**
- 定义 9 种用户状态及状态机流转规则
- 在接口层统一执行功能限制检查
- 所有状态变更自动记录审计日志
- 支持自动解禁、人工解禁和申诉恢复

**Non-Goals:**
- 不实现独立风控引擎（仅预留风控触发接口）
- 不实现审计日志冷存储归档
- 不实现补偿机制详细逻辑（仅预留接口）

## Decisions

### Decision 1: 状态机实现方式

**选择**: 枚举 + 状态转换表（Map-based）

**理由**:
- 状态数量固定（9 种），枚举足够表达
- 状态转换规则用 Map<当前状态, Set<允许的目标状态>> 定义，简单直观
- 无需引入外部状态机框架（如 Spring StateMachine），降低复杂度
- 转换规则集中管理，易于测试和维护

**替代方案**:
- Spring StateMachine: 功能强大但过于重量级，9 种状态不需要复杂状态机
- 数据库驱动状态表: 灵活但增加查询开销，状态规则变更不频繁

### Decision 2: 审计日志存储

**选择**: 独立审计日志表 `content_user_status_audit_log`，应用层保证写入

**理由**:
- 审计日志与业务数据分离，避免影响业务表性能
- 应用层写入简单可靠，无需数据库触发器
- 支持按用户 ID 和时间范围高效查询

**替代方案**:
- 数据库触发器: 侵入性强，不易维护
- 消息队列异步写入: 增加复杂度，日志可能丢失

### Decision 3: 功能限制检查位置

**选择**: 自定义注解 + AOP 切面在 Controller 层统一检查

**理由**:
- 与现有分层架构一致（Controller 层负责鉴权）
- 注解声明式，业务代码无侵入
- 统一拦截，避免遗漏

**替代方案**:
- BizManageService 层检查: 分散在各业务方法中，易遗漏
- Filter/Interceptor: 粒度不够细，难以按接口区分限制类型

### Decision 4: 自动解禁机制

**选择**: Spring @Scheduled 定时任务，每 5 分钟扫描到期处罚

**理由**:
- 与现有定时任务机制一致
- 5 分钟间隔平衡实时性和资源消耗
- 查询条件简单（status + endTime），索引友好

**替代方案**:
- 延迟队列（Redis/RabbitMQ）: 增加外部依赖，运维成本高
- 每个处罚单独定时: 任务数量爆炸，管理复杂

## Risks / Trade-offs

- **[状态转换遗漏]** → 通过状态转换表的完整性测试覆盖所有合法/非法转换
- **[审计日志写入失败]** → 审计日志写入失败时记录错误日志并告警，不阻断业务流程
- **[定时任务延迟]** → 5 分钟间隔内用户可能仍受限制，可接受的延迟
- **[并发状态变更]** → 使用数据库乐观锁（version 字段）防止并发冲突

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/
├── controller/
│   └── UserStatusController.java          # 状态查询和管理接口
├── biz/
│   └── UserStatusBizManageService.java    # 状态变更编排（含审计）
├── service/
│   ├── UserStatusService.java             # 状态机核心逻辑
│   └── UserStatusAuditLogService.java     # 审计日志服务
├── service/impl/
│   ├── UserStatusServiceImpl.java
│   └── UserStatusAuditLogServiceImpl.java
├── mapper/
│   ├── UserStatusAuditLogMapper.java
│   └── UserStatusAuditLogMapper.xml
├── entity/
│   ├── UserStatusAuditLog.java            # 审计日志实体
│   └── UserStatusEnum.java                # 用户状态枚举
├── model/
│   ├── UserStatusTransition.java          # 状态转换规则定义
│   └── UserRestriction.java               # 功能限制定义
├── vo/
│   ├── UserStatusVO.java                  # 状态查询响应
│   └── UserStatusHistoryVO.java           # 状态历史响应
├── req/
│   ├── UserStatusChangeReq.java           # 状态变更请求
│   └── UserStatusQueryReq.java            # 状态查询请求
├── annotation/
│   └── CheckUserStatus.java               # 状态检查注解
├── aspect/
│   └── UserStatusCheckAspect.java         # 状态检查 AOP 切面
├── scheduler/
│   └── UserStatusAutoReleaseScheduler.java # 自动解禁定时任务
└── config/
    └── UserStatusConfig.java              # 状态配置（保留策略等）

jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/userstatus/
├── service/
│   ├── UserStatusServiceTest.java         # 状态机核心逻辑测试
│   └── UserStatusAuditLogServiceTest.java # 审计日志测试
├── biz/
│   └── UserStatusBizManageServiceTest.java # 编排逻辑测试
├── aspect/
│   └── UserStatusCheckAspectTest.java     # AOP 切面测试
└── scheduler/
    └── UserStatusAutoReleaseSchedulerTest.java # 定时任务测试
```

## Test Strategy

- **UserStatusServiceTest**: 测试状态机所有合法/非法转换、边界条件、并发场景
- **UserStatusAuditLogServiceTest**: 测试审计日志写入、查询、导出
- **UserStatusBizManageServiceTest**: 测试状态变更编排（含审计写入）、自动解禁流程
- **UserStatusCheckAspectTest**: 测试各状态的功能限制拦截、注解参数解析
- **UserStatusAutoReleaseSchedulerTest**: 测试定时任务扫描、批量解禁、异常处理

## Migration Plan

1. **Week 1-2**: 创建数据库表（审计日志表、用户表字段扩展）
2. **Week 2-3**: 实现状态机核心逻辑和功能限制策略
3. **Week 3-4**: 实现审计日志系统
4. **Week 5-6**: 实现解禁恢复机制和定时任务
5. **回滚策略**: 功能开关控制，可逐步放量；数据库变更通过 Flyway 管理，支持回滚脚本

## Open Questions

- 用户表状态字段是否需要与现有用户表（sys_user）合并？还是内容社区独立用户表？
- 审计日志保留 3 年的具体归档策略（冷存储方案待定）
- 申诉恢复与 EPIC-08 的具体集成接口待确认
