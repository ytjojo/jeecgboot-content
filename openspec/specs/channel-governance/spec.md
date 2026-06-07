# channel-governance Specification

## Purpose
TBD - created by archiving change channel-21-privacy-membership-frontend. Update Purpose after archive.
## Requirements
### Requirement: 黑名单管理页面

系统 SHALL 提供黑名单管理页面，展示黑名单用户列表，支持移出黑名单操作。

#### Scenario: 展示黑名单列表
- **WHEN** 管理员进入黑名单页面
- **THEN** 展示黑名单列表，包含头像+昵称、拉黑时间、操作人、原因、状态、操作列

#### Scenario: 移出黑名单
- **WHEN** 管理员点击操作列的"移出黑名单"
- **THEN** 弹出确认 Modal"确认将 [昵称] 移出黑名单？移出后该用户可按频道当前加入规则重新申请或加入。"，确认后调用移出 API

#### Scenario: 移出成功后列表刷新
- **WHEN** 移出黑名单操作成功
- **THEN** 刷新黑名单列表，使黑名单缓存失效

#### Scenario: 空黑名单列表
- **WHEN** 黑名单为空
- **THEN** 展示空状态"暂无黑名单用户"

### Requirement: 加入黑名单功能

系统 SHALL 支持管理员将频道成员加入黑名单，加入后该用户无法申请加入或通过邀请进入频道。

#### Scenario: 从成员列表加入黑名单
- **WHEN** 管理员在成员列表操作菜单中选择"加入黑名单"
- **THEN** 弹出确认 Modal"确认将 [昵称] 加入黑名单？该用户将无法申请加入或通过邀请进入频道。" + TextArea 填写原因（必填）

#### Scenario: 加入黑名单后缓存失效
- **WHEN** 加入黑名单操作成功
- **THEN** 使成员列表和黑名单列表缓存失效

### Requirement: 治理操作日志页面

系统 SHALL 提供治理操作日志页面，展示移除、禁言、黑名单等操作记录。

#### Scenario: 展示治理日志列表
- **WHEN** 管理员进入治理日志页面
- **THEN** 展示日志列表，包含操作类型（Tag）、操作者、目标用户、时间、原因、详情列

#### Scenario: 操作类型标签颜色
- **WHEN** 日志列表展示操作类型标签
- **THEN** 移除（红色）、禁言（橙色）、解除禁言（绿色）、加入黑名单（灰色）、移出黑名单（蓝色）

#### Scenario: 筛选功能
- **WHEN** 管理员选择筛选条件（操作类型、时间范围、操作者搜索）
- **THEN** 列表按选定条件过滤

#### Scenario: 查看治理详情
- **WHEN** 管理员点击详情列的"查看"
- **THEN** 展开治理详情 Drawer，展示完整治理记录和操作前后状态对比

