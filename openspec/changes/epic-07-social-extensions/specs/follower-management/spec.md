## ADDED Requirements

### Requirement: 粉丝列表分页查询
系统 SHALL 提供某用户的粉丝列表查询能力，按关注时间倒序排列，支持分页和搜索。

#### Scenario: 查看粉丝列表
- GIVEN: 用户 A 有 30 个粉丝
- WHEN: 调用粉丝列表查询接口
- THEN: 返回 30 个粉丝信息，按关注时间倒序排列

#### Scenario: 分页加载粉丝
- GIVEN: 用户 A 有 200 个粉丝
- WHEN: 调用查询接口，分页参数 pageSize=50, pageNo=1
- THEN: 返回第一页 50 个粉丝，totalCount 为 200

#### Scenario: 搜索特定粉丝
- GIVEN: 用户 A 有 100 个粉丝，其中一个昵称为"测试用户"
- WHEN: 调用查询接口，keyword="测试"
- THEN: 返回昵称或用户名包含"测试"的粉丝

### Requirement: 新增粉丝趋势统计
系统 SHALL 提供按天/周/月统计的新增粉丝数查询能力。

#### Scenario: 按天查看新增粉丝趋势
- GIVEN: 用户 A 在过去 7 天每天新增 5 个粉丝
- WHEN: 查询按天统计的新增粉丝趋势
- THEN: 返回 7 条记录，每条显示日期和新增粉丝数

#### Scenario: 按周查看新增粉丝趋势
- GIVEN: 用户 A 在过去 4 周每周新增 20 个粉丝
- WHEN: 查询按周统计的新增粉丝趋势
- THEN: 返回 4 条记录，每条显示周次和新增粉丝数

#### Scenario: 点击某天查看新增详情
- GIVEN: 用户 A 在某天新增了 10 个粉丝
- WHEN: 查询该天的新增粉丝明细
- THEN: 返回当天新增的 10 个粉丝的详细信息

### Requirement: 粉丝数据导出
系统 SHALL 提供粉丝数据的 CSV 导出能力，导出前对个人信息脱敏。

#### Scenario: 导出粉丝数据
- GIVEN: 用户 A 有 500 个粉丝
- WHEN: 调用导出接口
- THEN: 返回脱敏后的 CSV 文件，包含粉丝昵称、关注时间等字段，不包含邮箱/手机号等敏感信息
