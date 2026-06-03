## Context

内容社区模块 `jeecg-module-content` 已有基础支撑：
- **实体**: `ContentUserReport`、`ContentUserAppeal` 已实现，包含状态流转和证据存储
- **服务**: `IContentUserSupportService` 及其实现 `ContentUserSupportServiceImpl` 已覆盖举报创建/处理、申诉创建/处理、帮助中心入口、客服路由
- **治理联动**: 申诉审批通过后自动恢复用户状态（`restoreGovernanceStatusIfNecessary`），积分处罚恢复已接入
- **客服分层**: 已实现基于用户等级/状态的路由（`SMART_FIRST`/`MANUAL_PRIORITY`/`APPEAL_PRIORITY`）

**缺失能力**（本次需补齐）：
1. 举报防重复（同一用户对同一对象只能举报一次）
2. 申诉次数限制（最多 3 次）
3. 帮助中心全文搜索
4. 客服会话历史记录
5. 客服服务评分
6. 举报/申诉处理完成后的用户通知
7. PRD 中客服优先级阈值（LV.15+）与现有实现（level>=5）的对齐

## Goals / Non-Goals

**Goals:**
- 补齐 PRD 中定义的 9 个能力的缺失逻辑
- 保持现有实现不变，仅新增/修改必要的校验和功能
- 所有新增逻辑必须有对应的单元测试

**Non-Goals:**
- 不重构现有实体和服务结构
- 不实现智能客服 NLP 引擎（使用现有引擎）
- 不实现帮助中心 CMS 完整后端（仅提供搜索接口）
- 不实现独立 IM 系统（客服实时对话由现有 IM 支撑）

## Decisions

### D1: 举报防重复校验位置
- **选择**: 在 `ContentUserSupportServiceImpl.createReport()` 中新增查询校验
- **理由**: 服务层校验可以在事务内保证一致性，避免并发重复提交
- **替代方案**: 数据库唯一索引（userId + targetId + targetType）— 更严格但灵活性低

### D2: 申诉次数限制实现
- **选择**: 在 `createAppeal()` 中查询同一 targetId 的已申诉次数，超过 3 次拒绝
- **理由**: 与现有实体结构兼容，`targetId` 已关联原始处罚记录
- **替代方案**: 新增申诉次数字段 — 增加冗余，且历史数据需要迁移

### D3: 帮助中心搜索实现
- **选择**: 基于 MyBatis-Plus 的 LIKE 查询实现简单搜索，后续可升级为全文检索
- **理由**: MVP 阶段数据量小，LIKE 查询足够，避免引入 Elasticsearch 依赖
- **替代方案**: Elasticsearch — 过度设计，增加运维复杂度

### D4: 客服优先级阈值
- **选择**: 将现有 `level >= 5` 改为 `level >= 15`，与 PRD 对齐
- **理由**: PRD 明确要求 LV.15+ 才享受优先客服
- **影响**: 现有高等级用户（5-14）将不再享受优先客服，需确认业务可接受

### D5: 客服会话与历史记录
- **选择**: 新增 `ContentCustomerServiceSession` 实体存储会话记录，30 天后自动归档
- **理由**: 需要持久化会话数据用于历史查询和审计
- **实现**: 定时任务清理 30 天前的会话记录

## Risks / Trade-offs

- **[风险] 优先级阈值变更影响现有用户** → 提前通知运营团队，灰度切换
- **[风险] LIKE 搜索性能** → 初期数据量小可接受，后续根据数据增长评估升级方案
- **[风险] 申诉次数限制可能误判** → 同一处罚的多次申诉视为 1 次（按 targetId 分组）

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/
├── entity/
│   └── ContentCustomerServiceSession.java          # 新增：客服会话实体
├── mapper/
│   └── ContentCustomerServiceSessionMapper.java     # 新增：客服会话 Mapper
├── service/
│   └── impl/
│       └── ContentUserSupportServiceImpl.java       # 修改：新增防重复、次数限制、搜索、会话
├── vo/
│   ├── ContentHelpSearchResultVO.java               # 新增：搜索结果 VO
│   ├── ContentServiceSessionVO.java                 # 新增：会话记录 VO
│   └── ContentServiceSessionPageVO.java             # 新增：会话分页 VO
└── req/support/
    ├── ContentServiceSessionQueryReq.java           # 新增：会话查询请求
    └── ContentServiceRatingReq.java                 # 新增：服务评分请求

jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/user/
└── ContentCustomerServiceSessionMapper.xml          # 新增：Mapper XML

src/test/java/org/jeecg/modules/content/user/service/impl/
└── ContentUserSupportServiceImplTest.java           # 修改：新增测试用例
```

## Test Strategy

| 测试文件 | 测试内容 |
|---------|---------|
| `ContentUserSupportServiceImplTest.java` | 举报防重复校验、申诉次数限制（第1/2/3次/超限）、帮助搜索、客服优先级路由、会话 CRUD、评分 |

## Migration Plan

1. 新增 `content_customer_service_session` 表（DDL 脚本）
2. 修改 `ContentUserSupportServiceImpl` 中的优先级阈值（level 5 → 15）
3. 部署后验证举报防重复、申诉次数限制、帮助搜索功能

## Open Questions

- 客服实时对话是否复用现有 IM 系统？（假设是）
- 帮助文章数据从何而来？（假设由运营通过 CMS 后台管理）
