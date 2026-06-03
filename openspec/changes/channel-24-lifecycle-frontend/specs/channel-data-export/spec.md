## ADDED Requirements

### Requirement: 导出配置
系统 SHALL 支持用户配置导出参数，包括时间范围、字段选择、格式选择。

#### Scenario: 导出配置弹窗展示
- **WHEN** 用户点击"导出数据"按钮
- **THEN** 打开导出配置 Modal

#### Scenario: 字段选择
- **WHEN** 用户打开导出配置弹窗
- **THEN** 展示字段多选 Checkbox Group，默认全选

#### Scenario: 格式选择
- **WHEN** 用户打开导出配置弹窗
- **THEN** 展示格式选择 Radio（Excel/CSV）

#### Scenario: 预计行数展示
- **WHEN** 用户选择字段和时间范围
- **THEN** 实时展示预计行数

### Requirement: 导出任务提交
系统 SHALL 支持提交导出任务，支持同步和异步两种模式。

#### Scenario: 小数据量直接下载
- **WHEN** 预计行数 <= 10,000
- **THEN** 直接下载文件

#### Scenario: 大数据量异步处理
- **WHEN** 预计行数 > 10,000
- **THEN** 提交异步任务，Toast 提示"导出任务已提交，请在导出历史中查看"

#### Scenario: 防重复提交
- **WHEN** 用户点击导出按钮
- **THEN** 按钮进入 loading 状态并禁用

### Requirement: 导出历史列表
系统 SHALL 展示导出历史列表，支持查看状态和下载。

#### Scenario: 导出历史列表展示
- **WHEN** 用户进入导出历史页面
- **THEN** 展示导出时间、导出范围、格式、行数、状态、操作

#### Scenario: 处理中状态展示
- **WHEN** 导出任务状态为 processing
- **THEN** 展示进度指示

#### Scenario: 已完成状态下载
- **WHEN** 导出任务状态为 completed
- **THEN** 展示下载按钮

#### Scenario: 失败状态重试
- **WHEN** 导出任务状态为 failed
- **THEN** 展示重试按钮和失败原因

#### Scenario: 下载过期处理
- **WHEN** 导出文件下载超过 7 天
- **THEN** 下载按钮置灰并提示"文件已过期"

### Requirement: 导出任务轮询
系统 SHALL 自动轮询导出任务状态，直到所有任务完成。

#### Scenario: 轮询启动
- **WHEN** 页面进入且存在 processing 状态任务
- **THEN** 启动 3 秒间隔轮询

#### Scenario: 批量查询
- **WHEN** 轮询触发
- **THEN** 批量查询所有 processing 状态任务状态

#### Scenario: 轮询终止
- **WHEN** 所有 processing 状态任务变为 completed 或 failed
- **THEN** 停止轮询

#### Scenario: 页面离开取消轮询
- **WHEN** 用户离开页面
- **THEN** 清除轮询定时器

### Requirement: 导出权限控制
系统 SHALL 根据用户权限控制导出功能。

#### Scenario: 无权限用户导出被拒
- **WHEN** 无权限用户尝试导出
- **THEN** 展示权限不足提示

#### Scenario: 导出字段权限控制
- **WHEN** 用户导出数据
- **THEN** 导出字段不包含超出权限的字段
