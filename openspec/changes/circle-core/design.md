## Context

内容社区模块（jeecg-module-content）当前包含 auth、user、userstatus 三个子包，遵循 controller/biz/service/mapper/entity/req/vo/dto 分层架构。圈子功能作为新的子包 `circle` 引入，需要与现有用户账号系统集成。

现有技术栈：Spring Boot 3 + MyBatis-Plus + MySQL，使用 JeecgBoot 框架的代码生成和基础组件。

## Goals / Non-Goals

**Goals:**
- 在 jeecg-module-content 中新增 circle 子包，提供圈子创建、成员管理和搜索能力
- 遵循现有分层架构（controller/biz/service/mapper/entity/req/vo/dto）
- 通过 TDD 驱动开发，确保核心业务逻辑有完整测试覆盖
- 为后续 EPIC-11/12/13 提供可扩展的基础

**Non-Goals:**
- 不实现内容置顶/精华、公告、审核机制
- 不实现数据统计和推荐算法
- 不修改现有用户账号系统的接口

## Decisions

### Decision 1: 数据库表设计

**选择**: 三张核心表 — circle、circle_member、circle_governance_log

**circle 表**:
- id, name (唯一索引), description, icon_url, cover_url, category, privacy_type (PUBLIC/PRIVATE/PASSWORD), join_type (DIRECT/APPROVAL/INVITE/PASSWORD), password_hash, creator_id, member_count, max_member_count, status, create_time, update_time

**circle_member 表**:
- id, circle_id, user_id, role (CREATOR/MODERATOR/MEMBER), status (ACTIVE/MUTED/REMOVED), mute_end_time, create_time, update_time
- 联合唯一索引: (circle_id, user_id)

**circle_governance_log 表**:
- id, circle_id, operator_id, target_user_id, action (MUTE/UNMUTE/REMOVE/ROLE_CHANGE), reason, duration, create_time

**理由**: 三表分离职责清晰，治理日志独立便于审计和追溯。成员表的 status 字段支持禁言状态和到期自动解除。

### Decision 2: 角色与权限模型

**选择**: 枚举角色 + 权限检查在 Service 层实现

角色层级: CREATOR > MODERATOR > MEMBER
- CREATOR: 所有操作权限
- MODERATOR: 内容管理、成员禁言/移除（不可管理其他版主）
- MEMBER: 基础参与权限

**替代方案**: RBAC 权限表
**理由**: 圈子场景角色固定且层级简单，枚举足够，RBAC 过度设计。

### Decision 3: 禁言自动解除机制

**选择**: 查询时检查 mute_end_time，过期则自动更新状态

**替代方案**: 定时任务轮询解除
**理由**: 查询时检查更简单可靠，无需额外定时任务。定时任务在高并发下可能有延迟。MVP 阶段查询检查足够，后续可按需引入定时任务优化。

### Decision 4: 搜索实现

**选择**: MySQL LIKE 查询作为 MVP 实现

**替代方案**: Elasticsearch
**理由**: MVP 阶段数据量小（目标 50 个圈子），MySQL LIKE 足以满足 P95 < 500ms 要求。后续数据量增长时可迁移至 ES。

### Decision 5: 敏感词检测

**选择**: 调用现有敏感词服务（如无则用本地词库匹配）

**理由**: 复用平台已有能力，避免重复建设。本地词库作为降级方案。

### Decision 6: 密码保护圈子

**选择**: 使用 BCrypt 加密存储密码，加入时验证

**理由**: BCrypt 是业界标准，JeecgBoot 已有依赖。密码不可明文存储或展示。

## Risks / Trade-offs

- **[风险] 圈子名称唯一性冲突** → 数据库唯一索引 + 应用层预检查，冲突时返回明确提示
- **[风险] 禁言到期检查性能** → 查询时检查 mute_end_time，索引优化。后续可引入定时任务
- **[风险] 敏感词库不完善** → 降级到本地词库，持续更新。结合人工审核（EPIC-11）
- **[权衡] MySQL LIKE vs ES 搜索** → MVP 阶段 LIKE 足够，数据量增长后迁移 ES，需预留搜索接口抽象
- **[权衡] 成员数上限检查** → 使用 member_count 字段原子更新 + 数据库约束，避免并发问题

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/
├── main/java/org/jeecg/modules/content/circle/
│   ├── controller/
│   │   ├── CircleController.java              # 圈子 CRUD API
│   │   ├── CircleMemberController.java        # 成员管理 API
│   │   └── CircleSearchController.java        # 搜索 API
│   ├── biz/
│   │   ├── CircleBiz.java                     # 圈子创建编排（含敏感词检测）
│   │   └── CircleMemberBiz.java               # 成员加入/退出编排
│   ├── service/
│   │   ├── CircleService.java
│   │   ├── CircleMemberService.java
│   │   ├── CircleGovernanceLogService.java
│   │   └── impl/
│   │       ├── CircleServiceImpl.java
│   │       ├── CircleMemberServiceImpl.java
│   │       └── CircleGovernanceLogServiceImpl.java
│   ├── mapper/
│   │   ├── CircleMapper.java
│   │   ├── CircleMemberMapper.java
│   │   └── CircleGovernanceLogMapper.java
│   ├── entity/
│   │   ├── Circle.java
│   │   ├── CircleMember.java
│   │   └── CircleGovernanceLog.java
│   ├── req/
│   │   ├── query/
│   │   │   └── CircleSearchReq.java
│   │   ├── create/
│   │   │   ├── CircleCreateReq.java
│   │   │   └── CircleJoinReq.java
│   │   └── update/
│   │       ├── CircleUpdateReq.java
│   │       └── CircleMemberUpdateReq.java
│   ├── vo/
│   │   ├── CircleVO.java
│   │   ├── CircleMemberVO.java
│   │   └── CircleSearchResultVO.java
│   └── dto/
│       ├── CircleDTO.java
│       └── CircleMemberDTO.java
├── main/resources/mapper/content/circle/
│   ├── CircleMapper.xml
│   ├── CircleMemberMapper.xml
│   └── CircleGovernanceLogMapper.xml
└── test/java/org/jeecg/modules/content/circle/
    ├── controller/
    │   ├── CircleControllerWebMvcTest.java
    │   ├── CircleMemberControllerWebMvcTest.java
    │   └── CircleSearchControllerWebMvcTest.java
    ├── biz/
    │   ├── CircleBizTest.java
    │   └── CircleMemberBizTest.java
    ├── service/
    │   ├── CircleServiceTest.java
    │   ├── CircleMemberServiceTest.java
    │   └── CircleGovernanceLogServiceTest.java
    └── entity/
        ├── CircleTest.java
        └── CircleMemberTest.java

# Flyway SQL
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/db/migration/
└── V{version}__create_circle_tables.sql
```

## Test Strategy

**Controller 测试** (WebMvcTest):
- CircleControllerWebMvcTest: 创建/更新/查询圈子接口的请求验证和响应格式
- CircleMemberControllerWebMvcTest: 加入/退出/角色变更/禁言/移除接口
- CircleSearchControllerWebMvcTest: 搜索接口参数验证和结果格式

**Biz 测试** (单元测试):
- CircleBizTest: 创建圈子编排流程（含敏感词检测、名称唯一性校验）
- CircleMemberBizTest: 加入流程编排（含密码验证、满员检查、黑名单检查）

**Service 测试** (单元测试):
- CircleServiceTest: 圈子 CRUD 业务逻辑
- CircleMemberServiceTest: 成员状态管理、禁言到期检查
- CircleGovernanceLogServiceTest: 治理日志记录

**Entity 测试**:
- CircleTest: 枚举值验证（privacy_type, join_type, status）
- CircleMemberTest: 角色和状态枚举验证

## Migration Plan

1. **Week 1**: 创建数据库表（Flyway migration），实现 Entity/Mapper 层
2. **Week 1-2**: 实现 Service/Biz 层，完成圈子创建和设置
3. **Week 2**: 实现成员管理（加入/退出/角色/禁言/移除）
4. **Week 3**: 实现搜索能力，集成测试
5. **Week 4**: 端到端测试，性能优化，上线

**回滚策略**: 数据库表为新增，不影响现有功能。如需回滚，删除新增表和代码即可。

## Open Questions

- 搜索服务当前部署状态？是否已有 Elasticsearch 集群？
- 敏感词检测服务是否已存在？还是需要新建？
- 圈子分类标签是否需要预定义枚举，还是允许自由输入？
