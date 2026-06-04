## ADDED Requirements

> **API 路径**:
> - 发起转让: `POST /api/v1/channels/{id}/transfer` (已存在)
> - 确认转让: `POST /api/v1/channels/transfer/{transferId}/confirm` (已存在)
> - 拒绝转让: `POST /api/v1/channels/transfer/{transferId}/reject` (已存在)
> - 转让历史查询: `GET /api/v1/channels/{id}/transfers` (待后端实现)
> - 待确认转让查询: `GET /api/v1/channels/{id}/transfer/pending` (待后端实现)
> **Controller**: ChannelController
> **前端封装**: `src/api/content/channel/index.ts` - `transferChannel()`, `confirmTransfer()`, `rejectTransfer()`, `getTransferHistory()`

### Requirement: 发起频道转让

频道主 SHALL 能通过频道管理页 > 设置区域的"转让频道"按钮打开 Modal 弹窗，搜索并选择目标用户，确认转让信息后发起转让请求。系统频道不显示"转让频道"按钮。

#### Scenario: 个人频道主发起转让
- **WHEN** 个人频道主点击"转让频道"，搜索并选择目标用户，确认转让
- **THEN** 系统发起转让请求，目标用户在消息通知中收到转让请求

#### Scenario: 系统频道不显示转让按钮
- **WHEN** 管理员查看系统频道的设置区域
- **THEN** "转让频道"按钮不显示

#### Scenario: 转让请求已存在时禁用
- **WHEN** 频道已有进行中的转让请求
- **THEN** "转让频道"按钮禁用，提示"已有进行中的转让请求"

### Requirement: 转让目标用户搜索

转让弹窗中的用户搜索 SHALL 支持输入 2 个字符后触发搜索，300ms 防抖。搜索结果下拉列表显示用户头像和昵称，排除当前用户自身。冻结用户在搜索结果中显示但置灰，hover 提示"该用户账号已被冻结"，不可选中。

#### Scenario: 搜索目标用户
- **WHEN** 用户输入 2 个以上字符
- **THEN** 300ms 防抖后触发搜索，下拉列表显示匹配用户的头像和昵称

#### Scenario: 冻结用户不可选
- **WHEN** 搜索结果包含冻结用户
- **THEN** 冻结用户置灰显示，hover 提示"该用户账号已被冻结"，不可选中

#### Scenario: 无搜索结果
- **WHEN** 搜索无匹配用户
- **THEN** 显示"未找到匹配的用户"

### Requirement: 组织频道转让规则

组织频道转让 SHALL 限定目标用户为同组织管理员，搜索框 placeholder 改为"搜索组织内管理员"。确认弹窗提示"组织频道仅可在组织管理员间转移"。

#### Scenario: 组织频道转让搜索范围
- **WHEN** 组织频道主发起转让
- **THEN** 搜索框 placeholder 为"搜索组织内管理员"，搜索范围限定为同组织管理员

### Requirement: 转让二次确认

最终转让 SHALL 弹出二次确认弹窗，文案"确认将频道 [频道名] 转让给 [用户名]？转让后您将降为管理员，此操作不可撤销。"

#### Scenario: 转让二次确认
- **WHEN** 用户选择目标用户并点击下一步
- **THEN** 弹出二次确认弹窗，显示频道名、目标用户名和转让后果说明

### Requirement: 目标用户确认转让

目标用户 SHALL 在消息通知中收到转让请求，可选择"接受"或"拒绝"。接受后转让完成，拒绝后频道所有权不变。

#### Scenario: 目标用户接受转让
- **WHEN** 目标用户在消息通知中点击"接受"
- **THEN** 频道所有权转让给目标用户，原频道主降为管理员

#### Scenario: 目标用户拒绝转让
- **WHEN** 目标用户在消息通知中点击"拒绝"
- **THEN** 频道所有权不变，消息通知显示"转让被拒绝"

### Requirement: 转让超时处理

转让请求 SHALL 在 7 天后自动过期。超时后消息通知显示"转让请求已过期"，频道所有权不变。

#### Scenario: 转让请求超时
- **WHEN** 转让请求发出 7 天后目标用户未操作
- **THEN** 转让请求自动过期，消息通知显示"转让请求已过期"

### Requirement: 转让历史记录

频道管理页设置区域 SHALL 展示转让历史，包含发起时间、目标用户、状态、结果。

#### Scenario: 查看转让历史
- **WHEN** 用户进入频道管理页设置区域
- **THEN** 展示该频道的转让历史记录列表
