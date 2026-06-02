## ADDED Requirements

### Requirement: Decay rule display
系统 SHALL 展示经验值衰减规则说明，包括触发条件、衰减速率、保护期机制。

#### Scenario: User views decay rules
- **WHEN** 用户进入我的等级页查看等级体系说明
- **THEN** 展示经验衰减规则：30天未登录开始衰减，7天保护期，活跃后停止衰减

### Requirement: Decay status warning
系统 SHALL 根据用户当前衰减状态展示对应的警告提示。

#### Scenario: User is in decay period
- **WHEN** 用户处于衰减中状态
- **THEN** 显示警告提示"您已 X 天未登录，经验值正在衰减"，使用警告色背景

#### Scenario: User is in protection period
- **WHEN** 用户处于降级保护期
- **THEN** 显示提示"您处于降级保护期，还剩 X 天"，使用警告色背景

#### Scenario: User has been downgraded
- **WHEN** 用户等级已下降
- **THEN** 显示提示"您的等级已降至 LV.X"，使用警告色背景

#### Scenario: User is active (no decay)
- **WHEN** 用户处于正常状态（无衰减）
- **THEN** 不展示衰减状态提示区

### Requirement: Decay status in level page
系统 SHALL 在我的等级页的积分与成长值分栏中展示衰减状态。

#### Scenario: Decay state shown in growth column
- **WHEN** 用户查看我的等级页右栏（成长值区域）
- **THEN** 展示当前经验衰减状态（正常/衰减中/保护期/已降级）

### Requirement: Progress bar decay indication
系统 SHALL 在进度条上反映衰减状态。

#### Scenario: Progress bar during decay
- **WHEN** 用户处于衰减中状态
- **THEN** 进度条显示衰减后的值，配合警告色

#### Scenario: Progress bar during protection
- **WHEN** 用户处于保护期
- **THEN** 进度条显示当前值，底部提示保护期剩余天数
