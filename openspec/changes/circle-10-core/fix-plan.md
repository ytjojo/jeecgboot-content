# 修复计划 — circle-10-core

**生成时间**: 2026-06-29
**审核文档数**: 5 (review-report.md, review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md, verify.md)
**总问题数**: 12

## 修复项

### BE-DOC-001 - design.md 治理日志 API 路径不完整
**来源**: review-report-20260627-084036.md
**位置**: design.md:119-123
**优先级**: P0
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 编辑 design.md，在「治理日志」API表格中补充完整路径前缀 `/api/v1/content/circle/governance-log`
2. 补充分页参数说明（circleId, pageNum, pageSize）
3. 补充响应字段格式说明

**验证方式**:
- 人工检查：API Endpoints表格完整，包含治理日志完整路径和参数说明

**状态**: pending

---

### BE-DOC-002 - Flyway 版本号不一致
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: tasks.md:3, plan.md
**优先级**: P0
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新 tasks.md Task 1.1 中的 Flyway 版本号从 V3.9.1_63 改为 V3.9.1_64__content_circle_tables.sql
2. 检查并更新 plan.md（如存在）中的版本号

**验证方式**:
- 人工检查：文档版本号与实际文件 V3.9.1_64__content_circle_tables.sql 一致

**状态**: pending

---

### BE-DOC-003 - File Structure 缺少 CircleGovernanceLogController 且 Mapper 路径错误
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: design.md:154-221
**优先级**: P0
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 design.md File Structure 的 controller/ 目录下补充 CircleGovernanceLogController.java
2. 将 Mapper XML 路径从 `mapper/api/v1/content/circle/` 修正为 `mapper/content/circle/`
3. 在 test/controller/ 目录下补充 CircleGovernanceLogControllerWebMvcTest.java（如已存在则确认）

**验证方式**:
- 人工检查：File Structure 与实际代码结构一致

**状态**: pending

---

### BE-DOC-004 - 延期项 7.2.2 未明确关联后续 change
**来源**: review-report-20260627-084036.md
**位置**: tasks.md:66
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 tasks.md 7.2.2 延期项标注"由 circle-11-content-interaction 补充"

**验证方式**:
- 人工检查：延期项有明确的后续change标注

**状态**: pending

---

### BE-DOC-005 - proposal.md 搜索服务依赖状态表述模糊
**来源**: review-report-20260627-084036.md
**位置**: proposal.md:64
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新 proposal.md Impact章节"依赖"部分，明确"搜索服务：MVP阶段使用MySQL LIKE，已确认"

**验证方式**:
- 人工检查：proposal.md 中搜索实现方式表述清晰

**状态**: pending

---

### BE-DOC-006 - 三表设计缺少外键约束说明
**来源**: review-report-20260627-084036.md
**位置**: design.md:22-38
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在 design.md Decision 1 末尾补充："不使用物理外键，由应用层保证数据一致性（符合JeecgBoot和MyBatis-Plus惯例）"

**验证方式**:
- 人工检查：外键策略有明确文档说明

**状态**: pending

---

### BE-DOC-007 - DTO 层描述与实际实现不符
**来源**: drift-report-20260627-084036.md
**位置**: design.md:195-197
**优先级**: P0
**依赖**: 无
**类型**: 文档修复

**同步策略**: 改文档 — 实际代码未实现DTO层，使用Entity/VO直接转换，这是合理的简化，更新文档匹配实现。

**修复步骤**:
1. 更新 design.md File Structure，移除 dto/ 目录及 CircleDTO.java、CircleMemberDTO.java
2. 在 Decision 或 Risks 中说明："不单独引入DTO层，Req/VO直接与Entity转换，减少样板代码"

**验证方式**:
- 人工检查：File Structure 与实际代码一致

**状态**: pending

---

### BE-CODE-001 - 公开接口匿名访问 NPE 问题
**来源**: drift-report-20260627-084036.md
**位置**: CircleController.java, CircleSearchController.java
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-后端

**修复步骤**:
1. 在 public-list、search 等匿名可访问接口中，将 `SecureUtil.currentUser().getId()` 改为安全获取方式
2. 使用 try-catch 或可选认证方式，未登录时返回 null 而非抛出异常
3. joined 字段处理：未登录时返回 false，不查询加入状态
4. 补充单元测试覆盖匿名访问场景

**验证方式**:
- 运行 CircleControllerWebMvcTest 和 CircleSearchControllerWebMvcTest，验证匿名访问不抛异常
- mvn test -pl jeecg-boot-module/jeecg-module-content -am

**状态**: done

---

### BE-CODE-002 - incrementMemberCount 非原子操作并发风险
**来源**: verify-report-20260627-084036.md
**位置**: CircleServiceImpl.java:971-978
**优先级**: CRITICAL
**依赖**: BE-CODE-001
**类型**: 代码修复-后端

**修复步骤**:
1. 修改 incrementMemberCount 方法，使用SQL原子更新：
   `UPDATE content_circle SET member_count = member_count + 1 WHERE id = ? AND member_count < max_member_count`
2. 根据 affected rows 判断是否满员：0表示已满员，1表示加入成功
3. 对应的 decrementMemberCount 方法也改为原子更新
4. 补充并发场景单元测试

**验证方式**:
- 单元测试验证原子更新逻辑
- mvn test -pl jeecg-boot-module/jeecg-module-content -am
- 注：经检查，原子更新已在现有代码中正确实现（CircleMapper使用SQL原子更新+affected rows判断）

**状态**: done

---

### BE-CODE-003 - myList/publicList 业务逻辑泄漏到 Controller 层
**来源**: drift-report-20260627-084036.md
**位置**: CircleController.java:88-117
**优先级**: P0
**依赖**: BE-CODE-001
**类型**: 代码修复-后端

**修复步骤**:
1. 在 ICircleService 或 CircleBiz 中新增 myList、publicList 方法，封装查询逻辑和VO组装
2. 将 CircleController 中 myList、publicList 方法内的数据库查询和VO组装逻辑下沉到Service/Biz层
3. Controller 只做参数校验和调用Service/Biz
4. 保持接口返回格式不变
5. 补充/更新单元测试

**验证方式**:
- 检查 Controller 层无直接数据库查询操作
- mvn test -pl jeecg-boot-module/jeecg-module-content -am

**状态**: done

---

### BE-CODE-004 - 搜索结果 joined 字段硬编码为 false
**来源**: verify-report-20260627-084036.md
**位置**: CircleSearchController.java:65
**优先级**: P0
**依赖**: BE-CODE-001, BE-CODE-003
**类型**: 代码修复-后端

**修复步骤**:
1. 复用 public-list/member-list 中的逻辑，在搜索结果中查询当前用户加入状态
2. 未登录用户返回 false
3. 已登录用户批量查询加入状态（避免N+1问题）
4. 补充单元测试

**验证方式**:
- 单元测试验证搜索结果joined字段正确
- mvn test -pl jeecg-boot-module/jeecg-module-content -am

**状态**: done

---

### BE-CODE-005 - check-name 接口异常处理不区分异常类型
**来源**: verify-report-20260627-084036.md
**位置**: CircleController.java:165-173
**优先级**: P1
**依赖**: BE-CODE-003
**类型**: 代码修复-后端

**修复步骤**:
1. 修改 check-name 接口异常处理，区分"名称检查正常返回可用/不可用"和"系统异常"
2. 系统异常时返回明确的错误码或消息，而非静默返回 false
3. 补充单元测试覆盖异常场景

**验证方式**:
- 单元测试验证正常和异常场景返回
- mvn test -pl jeecg-boot-module/jeecg-module-content -am

**状态**: done

---

## 延期/后续迭代项（不阻塞本次修复）

| 编号 | 问题 | 原因 | 后续安排 |
|------|------|------|---------|
| BE-DEFER-001 | CircleErrorCode 错误码枚举 | design.md已注明后续迭代引入 | 下一个版本 |
| BE-DEFER-002 | 7.2.2 敏感词检测测试 | 依赖敏感词服务集成 | circle-11补充 |
| BE-DEFER-003 | 7.2.4 my-list/public-list端点WebMvcTest | 需重构测试mock | 后续补充 |
| BE-DEFER-004 | 数据库连接/查询超时场景 | 全局异常处理已覆盖 | 按需补充 |
| BE-DEFER-005 | member_count定期一致性校验 | 运营工具，非MVP必需 | 后续迭代 |
| BE-DEFER-006 | CircleGovernanceLogController（公告等）属于circle-11 | 不在本change范围 | circle-11处理 |
