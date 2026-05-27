## ADDED Requirements

### Requirement: Fan list with pagination
系统 SHALL 提供粉丝列表查询接口，返回当前用户的粉丝列表，按关注时间倒序排列，支持分页和关键词搜索。

#### Scenario: View fan list
- GIVEN: 用户 A 有 500 个粉丝
- WHEN: 用户 A 请求粉丝列表（page=1, size=20）
- THEN: 系统返回最近关注的 20 个粉丝信息（昵称、头像、关注时间），总数为 500

#### Scenario: Search fans by keyword
- GIVEN: 用户 A 有多个粉丝
- WHEN: 用户 A 搜索关键词"张"
- THEN: 系统返回昵称或用户名包含"张"的粉丝列表

#### Scenario: Fan list pagination
- GIVEN: 用户 A 有 500 个粉丝
- WHEN: 用户 A 请求第 2 页（page=2, size=20）
- THEN: 系统返回第 21-40 个粉丝

### Requirement: New fan trend statistics
系统 SHALL 提供新增粉丝趋势统计接口，按天/周/月维度聚合新增粉丝数量，返回可图表化的时序数据。

#### Scenario: Daily fan trend
- GIVEN: 用户 A 过去 30 天每天都有新增粉丝
- WHEN: 用户 A 请求按天维度的粉丝趋势
- THEN: 系统返回最近 30 天每天的新增粉丝数，格式为 `[{date, count}]`

#### Scenario: Weekly fan trend
- GIVEN: 用户 A 请求按周维度的粉丝趋势
- WHEN: 系统聚合数据
- THEN: 系统返回最近 12 周每周的新增粉丝数

#### Scenario: Click on specific day shows details
- GIVEN: 用户 A 查看粉丝趋势图表
- WHEN: 用户 A 点击某天的数据点
- THEN: 系统返回当天新增粉丝列表和可能的关联事件（如发布了内容）

### Requirement: Fan profile analytics
系统 SHALL 提供粉丝画像分析接口，基于粉丝群体的兴趣标签、地域分布和活跃时段生成聚合画像。当粉丝数不足 100 时，系统 SHALL 返回提示信息而非空数据。

#### Scenario: View fan interest distribution
- GIVEN: 用户 A 有 500 个粉丝，粉丝们有不同的兴趣标签
- WHEN: 用户 A 请求粉丝画像
- THEN: 系统返回兴趣分布（如 科技 30%、娱乐 25%、体育 20%），以百分比形式展示

#### Scenario: View fan region distribution
- GIVEN: 用户 A 的粉丝分布在不同地区
- WHEN: 用户 A 请求地域分布
- THEN: 系统返回粉丝主要地区分布数据

#### Scenario: View fan active hours
- GIVEN: 用户 A 的粉丝在不同时间段活跃
- WHEN: 用户 A 请求活跃时段分析
- THEN: 系统返回 24 小时维度的粉丝活跃热力图数据

#### Scenario: Insufficient fans for analytics
- GIVEN: 用户 A 只有 50 个粉丝
- WHEN: 用户 A 请求粉丝画像
- THEN: 系统返回提示"粉丝数量不足，暂无法生成画像"（HTTP 200，code=INSUFFICIENT_FANS）

#### Scenario: Export fan analytics data
- GIVEN: 用户 A 查看粉丝画像
- WHEN: 用户 A 请求导出数据
- THEN: 系统生成脱敏后的 CSV 文件（仅包含聚合数据，不含粉丝个人信息）
