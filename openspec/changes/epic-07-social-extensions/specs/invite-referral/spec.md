## ADDED Requirements

### Requirement: 邀请码生成与获取
系统 SHALL 为每个注册用户提供唯一的邀请码和对应的分享链接。邀请码 SHALL 不可变更。

#### Scenario: 首次获取邀请码
- GIVEN: 用户 A 从未生成过邀请码
- WHEN: 用户 A 进入"邀请好友"页面
- THEN: 系统自动生成并返回唯一邀请码和分享链接

#### Scenario: 再次获取邀请码
- GIVEN: 用户 A 已有邀请码
- WHEN: 用户 A 再次进入"邀请好友"页面
- THEN: 返回同一邀请码（幂等，不重复生成）

#### Scenario: 复制分享链接
- GIVEN: 用户 A 有分享链接
- WHEN: 用户 A 点击"复制链接"
- THEN: 链接被复制到剪贴板

### Requirement: 邀请关系记录
系统 SHALL 在受邀人注册成功时记录邀请关系，包含邀请人、被邀请人、注册时间、奖励发放状态。

#### Scenario: 通过邀请链接注册
- GIVEN: 用户 B 通过用户 A 的邀请链接完成注册
- WHEN: 注册流程完成
- THEN: 系统记录 A 邀请 B 的关系，标记注册时间

#### Scenario: 重复注册不重复记录
- GIVEN: 用户 B 已通过用户 A 的邀请链接注册过
- WHEN: 用户 B 再次使用同一邀请链接
- THEN: 不创建新的邀请记录

### Requirement: 邀请奖励自动发放
系统 SHALL 在邀请关系确认后自动发放奖励（如 50 积分）。

#### Scenario: 成功邀请后发放奖励
- GIVEN: 用户 B 通过用户 A 的邀请码注册成功
- WHEN: 邀请关系确认
- THEN: 系统自动为用户 A 发放 50 积分

#### Scenario: 防刷奖励
- GIVEN: 同一 IP 下多个账号使用同一邀请码注册
- WHEN: 系统检测到异常注册模式
- THEN: 暂缓奖励发放，进入风控审核

### Requirement: 邀请记录与统计查询
系统 SHALL 提供邀请记录和邀请收益统计的查询能力。

#### Scenario: 查询邀请记录
- GIVEN: 用户 A 已邀请了 3 人
- WHEN: 查询邀请记录
- THEN: 返回已邀请的 3 人列表，包含注册时间和奖励信息

#### Scenario: 查询邀请收益统计
- GIVEN: 用户 A 累计邀请 10 人，获得 500 积分
- WHEN: 查询邀请统计
- THEN: 返回累计邀请人数、成功注册数和总奖励
