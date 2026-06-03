## ADDED Requirements

### Requirement: 通知类型独立开关
系统 SHALL 为七类通知（点赞、评论、关注、收藏、@我、私信、订阅更新）分别提供独立的主开关（Switch 组件）。每个开关控制该类通知是否启用。

#### Scenario: 关闭点赞通知开关
- **WHEN** 用户将点赞通知的 Switch 关闭并点击保存
- **THEN** 系统调用 `POST /content/user/settings/notification/update`，`likeNoticeEnabled` 为 false，保存成功后显示"通知设置已保存"

#### Scenario: 安全类通知不可关闭
- **WHEN** 用户查看通知类型列表
- **THEN** 安全类通知行（异地登录、密码修改等）显示"始终开启"标签，Switch 和渠道 Checkbox 均为 disabled 状态，显示锁图标

#### Scenario: 通知开关字段为 null
- **WHEN** 后端返回某通知类型的开关字段为 null
- **THEN** 前端默认显示为开启状态（Switch 为 true）

### Requirement: 通知渠道配置
系统 SHALL 为每类通知提供四个渠道选项（App内 / 推送 / 短信 / 邮件），使用 Checkbox 组件，支持多选。渠道配置存储在 `channelConfig` 对象中，各类型对应 `likeChannels`、`commentChannels` 等字段。

#### Scenario: 主开关关闭时渠道置灰
- **WHEN** 用户关闭某通知类型的主开关
- **THEN** 该行的渠道 Checkbox 组整体置灰（disabled），但保留上次选择状态

#### Scenario: 主开关开启时渠道恢复
- **WHEN** 用户重新开启某通知类型的主开关
- **THEN** 渠道 Checkbox 恢复可操作状态，显示之前保存的渠道选择

#### Scenario: 渠道配置为 null 时的默认值
- **WHEN** 后端返回某通知类型的渠道配置为 null
- **THEN** 前端默认选中 App内 + 推送两个渠道

### Requirement: 通知设置保存
系统 SHALL 在页面底部提供统一的"保存"按钮，点击后并发发送通知开关和免打扰规则两个请求。保存中按钮显示 loading 并禁用。

#### Scenario: 保存成功
- **WHEN** 用户点击保存，两个请求均成功
- **THEN** 全局消息提示"通知设置已保存"，新设置立即生效

#### Scenario: 部分保存失败
- **WHEN** 用户点击保存，仅一个请求失败
- **THEN** 全局错误提示，提示具体失败项，保留用户已修改的状态

#### Scenario: 免打扰规则未变更时不发送请求
- **WHEN** 用户仅修改了通知开关，未修改免打扰规则
- **THEN** 仅发送通知开关更新请求，不发送免打扰规则更新请求

### Requirement: 免打扰多时段配置
系统 SHALL 提供免打扰规则列表，支持新增、删除、编辑规则。每条规则包含启用状态、开始时间、结束时间、日期类型、摘要模式。

#### Scenario: 新增免打扰时段
- **WHEN** 用户点击"新增时段"按钮
- **THEN** 列表底部追加一条新规则，时间默认 22:00-07:00，日期类型默认"每天"

#### Scenario: 免打扰规则为空时关闭功能
- **WHEN** 免打扰规则列表为空且无规则启用
- **THEN** 免打扰功能整体关闭

#### Scenario: 启用但未填写时间时校验失败
- **WHEN** 用户启用某条规则但未选择时间就保存
- **THEN** 表单校验失败，时间字段标红提示"请选择时间"

#### Scenario: 开始时间等于结束时间
- **WHEN** 用户设置开始时间和结束时间相同
- **THEN** 视为全天免打扰，显示提示文案

#### Scenario: 最多 5 条规则限制
- **WHEN** 已有 5 条免打扰规则
- **THEN** "新增时段"按钮隐藏或禁用

### Requirement: 免打扰临时关闭
系统 SHALL 提供"暂时关闭免打扰（1小时）"按钮，点击后进入倒计时状态。

#### Scenario: 点击临时关闭
- **WHEN** 用户点击"暂时关闭免打扰（1小时）"
- **THEN** 按钮变为倒计时状态"免打扰已关闭，XX:XX 后恢复"，调用接口发送 `temporaryDisable: true`

#### Scenario: 页面加载时检测临时关闭状态
- **WHEN** 页面加载，`DndRuleVO.temporaryDisableUntil` 大于当前时间
- **THEN** 按钮自动进入倒计时状态，显示剩余恢复时间

#### Scenario: 倒计时结束
- **WHEN** 临时关闭倒计时归零
- **THEN** 按钮恢复为原始状态"暂时关闭免打扰（1小时）"

### Requirement: 安全通知提示
系统 SHALL 在免打扰区域底部显示提示文案，告知用户安全类通知不受免打扰影响。

#### Scenario: 显示安全通知提示
- **WHEN** 用户查看免打扰设置区域
- **THEN** 底部显示"安全类通知（异地登录、密码修改等）不受免打扰影响"
