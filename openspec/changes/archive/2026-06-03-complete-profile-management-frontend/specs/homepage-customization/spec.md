## ADDED Requirements

### Requirement: Homepage settings page
系统 SHALL 提供主页设置页面，支持背景图设置、主题色选择、模块配置。入口为个人主页右上角齿轮图标。

#### Scenario: Load homepage settings
- **WHEN** 用户从个人主页点击齿轮图标进入主页设置
- **THEN** 页面调用 `GET /content/user/profile/homepage/modules?userId=X` 加载模块列表，并回填当前背景图与主题色

#### Scenario: Save homepage settings
- **WHEN** 用户修改背景图或主题色后点击保存
- **THEN** 系统调用 `POST /content/user/profile/homepage/update?userId=X` 提交，成功后显示"主页设置已更新"

### Requirement: Background image upload via OSS
系统 SHALL 支持主页背景图上传，校验规则复用头像上传（JPG/PNG/WebP，≤5MB），裁剪比例 16:9，通过 OSS 客户端直传。

#### Scenario: Upload background image
- **WHEN** 用户点击"更换背景"按钮并选择有效图片
- **THEN** 系统打开 16:9 比例裁剪弹窗，裁剪完成后 OSS 直传并更新预览

#### Scenario: Restore default background
- **WHEN** 用户点击"恢复默认"按钮
- **THEN** 系统调用 `POST /content/user/profile/homepage/defaults/restore?userId=X`，清除自定义背景，预览区显示平台默认渐变背景

### Requirement: Theme color selection
系统 SHALL 支持主题色选择，提供 8-12 个预设颜色和自定义颜色输入。

#### Scenario: Select preset color
- **WHEN** 用户点击预设色板中的某个颜色
- **THEN** 该颜色显示对勾图标，预览区实时更新主题色效果

#### Scenario: Custom color input
- **WHEN** 用户通过 ColorPicker 输入自定义颜色
- **THEN** 预览区实时更新主题色效果

#### Scenario: Restore default theme
- **WHEN** 用户点击"恢复全部默认"
- **THEN** 系统调用 `/content/user/profile/homepage/defaults/restore`，清除自定义背景和主题色，使用平台默认样式

### Requirement: Theme color contrast accessibility
系统 SHALL 自动校验主题色与文字的对比度（WCAG AA 标准，对比度 ≥ 4.5:1），对比度不足时自动调整文字颜色。

#### Scenario: Low contrast warning
- **WHEN** 用户选择浅色主题色（如 #fffffe）
- **THEN** 系统显示黄色警告"当前主题色与文字对比度不足，可能影响可读性"，并自动将文字颜色调整为黑色

#### Scenario: High contrast auto-adjust
- **WHEN** 用户选择深色主题色
- **THEN** 系统自动将文字颜色调整为白色，预览区实时展示调整效果

### Requirement: Homepage module configuration
系统 SHALL 支持主页模块的显隐开关和拖拽排序。

#### Scenario: Toggle module visibility
- **WHEN** 用户关闭某个模块的 Switch 开关
- **THEN** 该模块从主页隐藏，保存后生效

#### Scenario: Drag to reorder modules
- **WHEN** 用户拖拽模块列表项调整顺序
- **THEN** 列表自动标记为"未保存"状态，显示"保存"按钮

#### Scenario: All modules hidden
- **WHEN** 用户关闭所有模块开关
- **THEN** 保存按钮禁用，提示"至少需要保留一个模块"

#### Scenario: Restore default module config
- **WHEN** 用户点击"恢复默认排序"
- **THEN** 系统调用 `/content/user/profile/homepage/defaults/restore` 重置为默认模块顺序和显隐配置

### Requirement: Mobile drag interaction
移动端 SHALL 通过长按（300ms）触发拖拽模式，拖拽手柄最小触摸热区 44px x 44px。

#### Scenario: Long press to drag on mobile
- **WHEN** 用户在移动端长按模块列表项 300ms
- **THEN** 进入拖拽模式，列表项增加阴影效果，目标位置显示蓝色插入线

#### Scenario: Keyboard accessibility for module reorder
- **WHEN** 用户使用键盘上下箭头键调整模块顺序
- **THEN** 模块按箭头方向移动，Enter 键确认，Escape 键取消

### Requirement: Homepage settings responsive layout
主页设置页面 SHALL 适配 PC/平板/移动端布局。

#### Scenario: PC layout
- **WHEN** 用户在 PC 端（≥1200px）访问主页设置
- **THEN** 背景图设置和主题色设置左右分栏，右侧实时预览

#### Scenario: Mobile layout with preview drawer
- **WHEN** 用户在移动端点击"预览主页效果"按钮
- **THEN** 以全屏 Drawer 形式从底部展开预览，支持下滑手势关闭
