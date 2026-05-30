## Context

内容社区模块 (`jeecg-module-content`) 当前已有 `auth`、`user`、`userstatus` 子模块。频道功能需要新增 `channel` 子模块，遵循项目既有的分层架构（Controller → BizManageService → Service → Mapper）。

项目使用 Spring Boot + MyBatis Plus，数据库表命名 snake_case，Entity 使用 `@Data` + `@TableName`，依赖注入统一用 `@Resource`。

频道是内容聚合的核心单元，需要支持 system/personal/organization 三种类型，各有不同的创建规则、权限模型和管理方式。本设计建立频道的数据模型、创建流程、审核状态机、编辑/转让/删除能力。

## Goals / Non-Goals

**Goals:**
- 建立统一的频道数据模型，支持三种类型的基础字段和差异化字段
- 实现三类频道的创建流程，包含完整的校验规则
- 实现审核状态机（Draft → PendingReview → Active/Rejected）
- 实现频道信息编辑（区分关键字段与非关键字段）
- 实现频道转让（个人/组织差异化规则）和删除（7天冷静期）
- 所有关键操作记录审计日志

**Non-Goals:**
- 频道隐私与加入规则（EPIC-21）
- 频道内容发布（EPIC-22）
- 频道推荐与发现（EPIC-23）
- 频道数据统计与生命周期管理（EPIC-24）
- 付费订阅能力

## Decisions

### D1: 频道类型使用枚举字段而非多表

**选择**: 单表 `content_channel` + `channel_type` 枚举字段（system/personal/organization）

**理由**:
- 三类频道共享 80% 以上字段（名称、简介、图标、封面、状态、分类）
- 差异字段较少（系统频道有置顶权重，组织频道有组织ID），可用可空字段处理
- 单表简化查询和统计，后续推荐/发现模块不需要 UNION 多表
- 类型切换需走迁移流程，单表可通过审计记录追踪

**替代方案**: 三张独立表 → 字段重复多，跨类型查询复杂，不采纳

### D2: 审核状态机使用状态字段 + 审核记录表

**选择**: `content_channel.status` 字段（TINYINT 枚举）+ `content_channel_review` 独立审核记录表

**理由**:
- 状态字段便于列表查询和筛选
- 审核记录表存储每次审核的完整信息（审核人、结果、原因、时间），支持追溯
- 状态枚举: 0=Draft, 1=PendingReview, 2=Active, 3=Rejected, 4=DeleteCooling, 5=Deleted
- 后续 EPIC-24 可扩展冻结、隐藏、归档等状态

### D3: 名称唯一性校验使用数据库唯一索引 + 范围条件

**选择**: 数据库层面不建全局唯一索引，由应用层在创建/改名时校验

**理由**:
- 用户频道范围（personal + organization）共享唯一性，系统频道不参与
- Deleted 状态的频道是否释放名称由平台策略决定，不适合用数据库唯一索引
- 应用层校验时查询 PendingReview + Active + DeleteCooling + ReadonlyFrozen + Hidden + Archived 状态的频道
- 使用 Redis 缓存已占用的名称，减少数据库查询

### D4: 删除冷静期使用定时任务 + 状态字段

**选择**: 频道进入 DeleteCooling 状态，记录 `delete_cooling_end_time`，定时任务扫描到期记录

**理由**:
- 7天冷静期内用户可撤销，状态回退到 Active
- 定时任务每小时扫描到期记录，批量处理为 Deleted
- 不使用延迟消息队列，降低系统复杂度

### D5: 转让流程使用状态机 + 确认机制

**选择**: `content_channel_transfer` 表记录转让请求，状态流转: Pending → Accepted/Rejected/Expired

**理由**:
- 个人频道转让需目标用户确认，组织频道仅可在组织管理员间转移
- 转让请求有超时机制（默认7天），超时自动失效
- 转让完成后记录审计日志，原频道主降为管理员

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 组织认证系统未就绪 | 组织频道创建被阻塞 | 先实现系统频道和个人频道，组织频道以认证结果为前置条件 |
| 内容安全审核服务响应慢 | 用户创建体验差 | 设置 24h SLA，超时提醒，提供人工处理队列 |
| 名称唯一性范围不清 | 创建/编辑冲突 | 严格执行"用户频道范围"定义，作为发布前验收项 |
| 删除条件涉及未来付费订阅 | 预留校验无法落地 | 本期返回"无未了结订阅"，保留产品规则和审计字段 |
| 单表设计在数据量大时查询慢 | 列表页性能下降 | 按状态和类型建联合索引，必要时按类型分表 |

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
├── controller/
│   ├── ChannelController.java              # 用户端频道 API
│   └── ChannelAdminController.java         # 后台频道管理 API
├── biz/
│   └── ChannelBizManageService.java        # 频道业务编排（创建、编辑、转让、删除）
├── service/
│   ├── ChannelService.java                 # 频道表 Service 接口
│   ├── ChannelReviewService.java           # 审核记录 Service 接口
│   ├── ChannelTransferService.java         # 转让记录 Service 接口
│   └── impl/
│       ├── ChannelServiceImpl.java
│       ├── ChannelReviewServiceImpl.java
│       └── ChannelTransferServiceImpl.java
├── mapper/
│   ├── ChannelMapper.java
│   ├── ChannelReviewMapper.java
│   └── ChannelTransferMapper.java
├── entity/
│   ├── Channel.java                        # 频道实体
│   ├── ChannelReview.java                  # 审核记录实体
│   └── ChannelTransfer.java                # 转让记录实体
├── enums/
│   ├── ChannelType.java                    # 频道类型枚举
│   ├── ChannelStatus.java                  # 频道状态枚举
│   └── ReviewResult.java                   # 审核结果枚举
├── dto/
│   ├── CreateChannelDTO.java               # 创建频道请求
│   ├── UpdateChannelDTO.java               # 更新频道请求
│   ├── TransferChannelDTO.java             # 转让频道请求
│   └── DeleteChannelDTO.java               # 删除频道请求
├── vo/
│   ├── ChannelVO.java                      # 频道详情响应
│   └── ChannelListVO.java                  # 频道列表响应
├── req/
│   ├── query/
│   │   └── ChannelQueryReq.java            # 频道查询条件
│   ├── create/
│   │   └── ChannelCreateReq.java           # 创建频道请求
│   └── update/
│       └── ChannelUpdateReq.java           # 更新频道请求
└── constant/
    └── ChannelConstants.java               # 频道相关常量

src/main/resources/mapper/content/channel/
├── ChannelMapper.xml
├── ChannelReviewMapper.xml
└── ChannelTransferMapper.xml

src/main/resources/db/migration/
└── V_channel_infrastructure.sql            # 频道表结构迁移脚本
```

## Test Strategy

| 测试文件 | 测试策略 |
|---------|---------|
| `ChannelBizManageServiceTest.java` | 单元测试：创建/编辑/转让/删除的业务编排逻辑，Mock Service 层 |
| `ChannelServiceTest.java` | 单元测试：名称唯一性校验、状态查询、按类型筛选 |
| `ChannelControllerTest.java` | 集成测试：API 请求参数校验、权限校验、响应格式 |
| `ChannelAdminControllerTest.java` | 集成测试：后台管理 API、系统频道创建、审核处理 |
| `ChannelReviewStateMachineTest.java` | 单元测试：审核状态流转（PendingReview → Active/Rejected） |
| `ChannelTransferFlowTest.java` | 集成测试：转让发起 → 目标确认 → 完成/超时/拒绝 |
| `ChannelDeleteCoolingTest.java` | 集成测试：删除确认 → 冷静期 → 撤销/到期处理 |

## Migration Plan

1. **数据库迁移**: 执行 `V_channel_infrastructure.sql` 创建三张表（channel、channel_review、channel_transfer）
2. **后端部署**: 部署 `jeecg-module-content` 新增的 channel 子模块
3. **验证**: 通过后台管理页面创建系统频道，验证流程
4. **回滚策略**: 删除新增的三张表，移除 channel 子模块代码

## Open Questions

1. 内容安全审核服务的具体接口协议？（本期假设为同步接口，返回通过/拒绝/退回修改）
2. 频道分类体系是否已有？（本期假设已有平台分类体系，频道复用）
3. Deleted 状态的频道是否释放名称？（本期假设释放，由应用层控制）
4. 通知服务的具体接口？（本期假设为异步通知，不阻塞主流程）
