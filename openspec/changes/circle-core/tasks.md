## 1. 数据库与 Entity 层

- [ ] 1.1 创建 Flyway SQL 迁移脚本，包含 circle、circle_member、circle_governance_log 三张表及索引
- [ ] 1.2 创建 Circle Entity，包含枚举字段（privacy_type、join_type、status）
- [ ] 1.3 创建 CircleMember Entity，包含枚举字段（role、status）
- [ ] 1.4 创建 CircleGovernanceLog Entity
- [ ] 1.5 创建 CircleMapper 接口及 XML
- [ ] 1.6 创建 CircleMemberMapper 接口及 XML
- [ ] 1.7 创建 CircleGovernanceLogMapper 接口及 XML
- [ ] 1.8 编写 Entity 单元测试（枚举值验证）

## 2. Service 层

- [ ] 2.1 创建 CircleService 接口及实现，包含圈子 CRUD 逻辑
- [ ] 2.2 创建 CircleMemberService 接口及实现，包含成员状态管理、禁言到期检查
- [ ] 2.3 创建 CircleGovernanceLogService 接口及实现，包含日志记录和查询
- [ ] 2.4 编写 CircleService 单元测试
- [ ] 2.5 编写 CircleMemberService 单元测试
- [ ] 2.6 编写 CircleGovernanceLogService 单元测试

## 3. Biz 层（圈子创建编排）

- [ ] 3.1 创建 CircleBiz，编排圈子创建流程（名称唯一性校验 + 敏感词检测 + 创建）
- [ ] 3.2 创建 CircleMemberBiz，编排成员加入流程（密码验证 + 满员检查 + 黑名单检查 + 加入）
- [ ] 3.3 编写 CircleBiz 单元测试
- [ ] 3.4 编写 CircleMemberBiz 单元测试

## 4. Req/VO/DTO 层

- [ ] 4.1 创建 CircleCreateReq、CircleUpdateReq、CircleSearchReq 请求对象
- [ ] 4.2 创建 CircleJoinReq、CircleMemberUpdateReq 请求对象
- [ ] 4.3 创建 CircleVO、CircleMemberVO、CircleSearchResultVO 响应对象
- [ ] 4.4 创建 CircleDTO、CircleMemberDTO 传输对象

## 5. Controller 层

- [ ] 5.1 创建 CircleController，实现圈子 CRUD API
- [ ] 5.2 创建 CircleMemberController，实现成员管理 API（加入/退出/角色变更/禁言/移除）
- [ ] 5.3 创建 CircleSearchController，实现搜索 API
- [ ] 5.4 编写 CircleController WebMvcTest
- [ ] 5.5 编写 CircleMemberController WebMvcTest
- [ ] 5.6 编写 CircleSearchController WebMvcTest

## 6. 验证

- [ ] 6.1 运行所有单元测试，确保通过
- [ ] 6.2 运行 Flyway 迁移，验证数据库表创建正确
- [ ] 6.3 验证圈子创建完整流程（创建 → 设置隐私 → 设置加入方式）
- [ ] 6.4 验证成员管理完整流程（加入 → 角色变更 → 禁言 → 解禁 → 移除）
- [ ] 6.5 验证搜索功能（关键词搜索 → 结果展示 → 降级处理）
