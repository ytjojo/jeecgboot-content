## ADDED Requirements

### Requirement: Privacy settings page
系统 SHALL 提供隐私设置页面，支持为每个资料字段设置可见性级别。入口为设置 → 隐私设置或个人中心 → 隐私设置。

#### Scenario: Load privacy settings
- **WHEN** 用户进入隐私设置页
- **THEN** 页面显示骨架屏加载状态，加载完成后展示各字段当前可见性设置

#### Scenario: Save privacy settings
- **WHEN** 用户修改可见性后点击保存
- **THEN** 系统调用 `/content/user/privacy/settings/update` 接口，成功后显示"隐私设置已更新"

### Requirement: Field visibility levels
系统 SHALL 支持四种可见性级别：公开（所有人可见）、仅关注者（关注你的人可见）、互关可见（互相关注的人可见）、仅自己（仅自己可见）。

#### Scenario: Set field to followers only
- **WHEN** 用户将"简介"字段的可见性设为"仅关注者"
- **THEN** 保存后仅关注该用户的人可以看到其简介，其他用户看到的是隐藏状态

#### Scenario: Nickname and avatar always public
- **WHEN** 用户查看隐私设置列表
- **THEN** 昵称和头像默认公开且不可修改（置灰 + Tooltip"昵称和头像始终公开"）

### Requirement: Default visibility setting
系统 SHALL 支持设置默认可见性级别，仅影响新添加的字段，不改变已有字段设置。

#### Scenario: Set default visibility
- **WHEN** 用户修改"默认可见性"下拉选择
- **THEN** 该设置保存后仅影响后续新增的字段，已有字段的可见性不受影响

### Requirement: Batch visibility operation
系统 SHALL 支持"一键全部设为"快捷操作，批量设置所有可修改字段的可见性。

#### Scenario: Batch set all fields
- **WHEN** 用户选择"一键全部设为" → "仅自己"并确认
- **THEN** 系统弹出确认框"确定将所有可修改字段设为仅自己吗？此操作会覆盖当前各字段的单独设置"，确认后批量更新，显示"已将 X 个字段设为仅自己"，变化字段高亮 2 秒

#### Scenario: Undo batch operation
- **WHEN** 用户完成批量操作后点击"撤销"按钮（5 秒内有效）
- **THEN** 系统恢复批量操作前的状态，5 秒后撤销按钮自动消失

### Requirement: Privacy update frequency limit
系统 SHALL 限制用户每小时隐私修改次数为 10 次。

#### Scenario: Frequency limit reached
- **WHEN** 用户每小时修改超过 10 次后尝试保存
- **THEN** 保存按钮禁用，显示黄色提示条"操作过于频繁，请稍后再试"

### Requirement: Privacy cache immediate effect
系统 SHALL 在隐私设置保存成功后立即刷新本地缓存的用户资料数据。

#### Scenario: Refresh local cache after save
- **WHEN** 用户保存隐私设置成功
- **THEN** 前端主动调用 `/content/user/profile/current` 刷新本地缓存，提示"隐私设置已更新，新设置将立即对新访问者生效"

#### Scenario: Silent cache inconsistency handling
- **WHEN** 因缓存导致短暂数据不一致
- **THEN** 系统不向用户展示技术细节，静默处理

### Requirement: Privacy settings responsive layout
隐私设置页面 SHALL 适配 PC/移动端布局。PC 端列表最大宽度 640px 居中，移动端全宽单列。

#### Scenario: PC layout
- **WHEN** 用户在 PC 端访问隐私设置
- **THEN** 列表最大宽度 640px 居中，每行字段名 + 可见性选择左右排列

#### Scenario: Mobile layout
- **WHEN** 用户在移动端访问隐私设置
- **THEN** 全宽单列，字段名在上，可见性选择在下，Select 组件改为 ActionSheet 底部选择器
