## ADDED Requirements

### Requirement: 申诉列表
系统 SHALL 展示申诉列表，支持筛选和分页。

#### Scenario: 申诉列表展示
- **WHEN** 平台运营进入申诉管理页面
- **THEN** 展示申诉编号、频道名称、处罚类型、申诉人、提交时间、状态、操作

#### Scenario: 筛选条件
- **WHEN** 用户设置筛选条件（申诉状态、频道名称、提交时间范围）
- **THEN** 列表按条件过滤

#### Scenario: 空状态展示
- **WHEN** 无申诉记录
- **THEN** 展示空状态插图和引导文案

### Requirement: 申诉详情查看
系统 SHALL 支持查看申诉详情。

#### Scenario: 查看申诉详情
- **WHEN** 用户点击"查看详情"
- **THEN** 展示处罚信息、申诉说明、补充材料、历史处理记录

### Requirement: 申诉提交
系统 SHALL 支持频道主提交申诉。

#### Scenario: 打开申诉弹窗
- **WHEN** 频道主在处罚通知中点击"申诉"入口
- **THEN** 打开 AppealModal

#### Scenario: 填写申诉信息
- **WHEN** 频道主填写申诉说明和上传补充材料
- **THEN** 表单校验通过

#### Scenario: 提交申诉
- **WHEN** 频道主点击"提交申诉"
- **THEN** API 调用成功，Modal 关闭，Toast 提示"申诉已提交"

### Requirement: 申诉处理
系统 SHALL 支持平台运营处理申诉。

#### Scenario: 打开处理弹窗
- **WHEN** 平台运营点击"处理"
- **THEN** 打开处理 Modal

#### Scenario: 处理结果选择
- **WHEN** 平台运营选择处理结果
- **THEN** 可选择"恢复状态"或"维持原处理"

#### Scenario: 填写处理说明
- **WHEN** 平台运营填写处理说明
- **THEN** 表单校验通过

#### Scenario: 提交处理结果
- **WHEN** 平台运营点击"确认"
- **THEN** API 调用成功，Modal 关闭，列表刷新

### Requirement: 申诉超时提醒
系统 SHALL 对超时未处理的申诉展示提醒标记。

#### Scenario: 超时提醒标记
- **WHEN** 申诉首次响应超过 3 个工作日
- **THEN** 展示超时提醒标记

### Requirement: 申诉记录查看
系统 SHALL 在频道治理详情页展示申诉记录。

#### Scenario: 申诉记录 Tab
- **WHEN** 用户在频道治理详情页查看申诉记录 Tab
- **THEN** 展示该频道的申诉记录列表
