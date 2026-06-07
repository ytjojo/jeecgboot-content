# channel-privacy-settings Specification

## Purpose
TBD - created by archiving change channel-21-privacy-membership-frontend. Update Purpose after archive.
## Requirements
### Requirement: 频道隐私设置 UI

系统 SHALL 在频道设置页提供隐私设置表单，支持频道主将频道设为公开或私有。

#### Scenario: 频道主设置频道为公开
- **WHEN** 频道主在设置页选择"公开"并确认保存
- **THEN** 调用隐私更新 API，保存成功后展示"隐私设置已更新"消息

#### Scenario: 频道主设置频道为私有
- **WHEN** 频道主在设置页选择"私有"并确认保存
- **THEN** 弹出确认 Modal 说明影响，确认后调用 API 保存

#### Scenario: 系统频道隐私选项锁定
- **WHEN** 频道为系统频道
- **THEN** Radio 禁用并展示 Alert 提示"系统频道必须公开，不允许设置为私有"

#### Scenario: 加载状态展示
- **WHEN** 隐私设置正在加载
- **THEN** Radio.Group 展示 skeleton

#### Scenario: 保存失败保留用户选择
- **WHEN** 保存隐私设置失败
- **THEN** 保留用户当前选择，展示错误提示

#### Scenario: 权限不足处理
- **WHEN** 当前用户无权修改频道设置
- **THEN** 页面展示"您无权修改频道设置"，操作按钮隐藏

### Requirement: 隐私变更影响确认弹窗

系统 SHALL 在隐私状态切换时展示影响说明确认弹窗，确认按钮使用危险操作样式（红色）。

#### Scenario: 公开转私有确认弹窗
- **WHEN** 频道主将频道从公开切换为私有
- **THEN** 弹出 Modal，标题"确认设为私有频道？"，内容说明"频道将退出公开搜索和推荐，非成员将无法浏览受限内容。当前订阅者不受影响。"

#### Scenario: 私有转公开确认弹窗
- **WHEN** 频道主将频道从私有切换为公开
- **THEN** 弹出 Modal，标题"确认设为公开频道？"，内容说明"频道内容将对所有人可见，可被搜索和推荐。"

#### Scenario: 确认按钮样式
- **WHEN** 影响确认弹窗展示
- **THEN** 确认按钮为红色危险操作样式，取消按钮为默认样式

### Requirement: 加入方式配置 UI

系统 SHALL 在频道设置页提供加入方式配置表单，支持自由加入、审核加入、邀请加入三种模式。

#### Scenario: 选择自由加入
- **WHEN** 频道主选择"自由加入"
- **THEN** 无额外配置项，保存后新用户可直接加入

#### Scenario: 选择审核加入
- **WHEN** 频道主选择"审核加入"
- **THEN** 展示 Switch（是否允许被拒绝后再次申请）和 InputNumber（再次申请间隔小时数，默认 24）

#### Scenario: 选择邀请加入
- **WHEN** 频道主选择"邀请加入"
- **THEN** 展示"创建邀请"按钮和已有邀请列表表格

#### Scenario: 邀请列表为空状态
- **WHEN** 邀请列表为空
- **THEN** 展示空状态"暂无邀请，点击上方按钮创建"

### Requirement: 邀请创建 Drawer

系统 SHALL 提供邀请创建 Drawer 表单，支持创建邀请码和邀请链接。

#### Scenario: 创建邀请码
- **WHEN** 频道主在 Drawer 中选择邀请类型为"邀请码"，填写有效期和可用次数，确认创建
- **THEN** 生成邀请码，展示复制按钮

#### Scenario: 创建邀请链接
- **WHEN** 频道主在 Drawer 中选择邀请类型为"邀请链接"，填写有效期和可用次数，确认创建
- **THEN** 生成邀请链接，展示复制按钮

#### Scenario: 邀请过期状态展示
- **WHEN** 邀请已过期
- **THEN** 状态标签展示"已过期"（灰色）

#### Scenario: 邀请已用完状态展示
- **WHEN** 邀请已用完
- **THEN** 状态标签展示"已用完"（灰色）

#### Scenario: 邀请已撤销状态展示
- **WHEN** 邀请已撤销
- **THEN** 状态标签展示"已撤销"（橙色）

#### Scenario: 邀请有效状态展示
- **WHEN** 邀请有效
- **THEN** 状态标签展示"有效"（绿色）

