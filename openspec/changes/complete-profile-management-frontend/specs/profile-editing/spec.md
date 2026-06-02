## ADDED Requirements

### Requirement: Edit profile form page
系统 SHALL 提供编辑资料页面，支持用户编辑昵称、简介、性别、生日、地区、职业、个人链接等字段。页面包含顶部导航栏（返回 + 标题 + 保存按钮）、头像区域、表单区域、底部操作栏。

#### Scenario: Load edit profile page
- **WHEN** 用户从个人中心点击"编辑资料"按钮进入编辑页
- **THEN** 页面显示骨架屏加载状态，加载完成后展示当前用户资料数据，表单字段回填当前值

#### Scenario: Save profile successfully
- **WHEN** 用户修改表单字段后点击保存按钮
- **THEN** 系统校验通过后调用 `/content/user/profile/update` 接口，保存成功后显示全局消息"资料已更新"并返回个人中心

#### Scenario: Cancel with unsaved changes
- **WHEN** 用户修改了表单字段后点击返回按钮
- **THEN** 系统弹出确认框"您有未保存的修改，确定离开吗？"

#### Scenario: Cancel without changes
- **WHEN** 用户未修改任何字段（isDirty=false）点击返回按钮
- **THEN** 系统直接返回上一页，不弹出确认框

### Requirement: Profile field validation
系统 SHALL 对资料字段进行实时校验和提交校验。昵称为必填字段，最大 20 字符；简介最大 500 字符并显示字数统计；个人链接需符合 URL 格式。

#### Scenario: Empty nickname validation
- **WHEN** 用户清空昵称输入框并触发校验
- **THEN** 输入框下方显示红色错误提示"请输入昵称"

#### Scenario: Sensitive word in nickname
- **WHEN** 用户输入含敏感词的昵称并保存
- **THEN** 系统返回错误，显示提示"昵称包含不当内容，请修改"

#### Scenario: Character count display
- **WHEN** 用户在简介文本域中输入内容
- **THEN** 文本域右下角实时显示已输入字数/最大字数（如 123/500）

#### Scenario: URL format validation
- **WHEN** 用户在个人链接输入框输入非法 URL 并失去焦点
- **THEN** 输入框下方显示红色错误提示，阻止提交

### Requirement: Avatar upload and cropping
系统 SHALL 提供头像上传裁剪弹窗，支持 JPG/PNG/WebP 格式，最大 5MB。裁剪比例锁定 1:1，支持滚轮缩放和滑块缩放。

#### Scenario: Upload valid avatar
- **WHEN** 用户选择符合格式和大小要求的图片文件
- **THEN** 系统打开裁剪弹窗，显示裁剪框和预览区（圆形 48px + 小方形 32px）

#### Scenario: Upload invalid format
- **WHEN** 用户选择 BMP 格式图片
- **THEN** 系统在文件选择阶段拦截，显示提示"仅支持 JPG、PNG、WebP 格式"

#### Scenario: Upload oversized file
- **WHEN** 用户选择超过 5MB 的图片
- **THEN** 系统在文件选择阶段拦截，显示提示"图片大小必须小于 5MB"

#### Scenario: Confirm crop and upload
- **WHEN** 用户裁剪完成后点击"确定"按钮
- **THEN** 系统上传裁剪结果，显示上传进度，成功后关闭弹窗并更新编辑页头像预览

#### Scenario: Upload failure
- **WHEN** 头像上传接口返回失败
- **THEN** 弹窗不关闭，显示"上传失败，请重试"

### Requirement: Profile review status
系统 SHALL 展示资料审核状态，包括待审核、审核通过、审核不通过三种状态。

#### Scenario: Pending review status
- **WHEN** 用户资料处于"待审核"状态
- **THEN** 编辑页表单顶部显示黄色 Alert"您的资料正在审核中，预计 24 小时内完成"，保存按钮禁用，字段不可编辑

#### Scenario: Review rejected
- **WHEN** 用户资料审核不通过
- **THEN** 编辑页显示红色 Alert"资料审核未通过，原因：{reason}"，提供"重新编辑"按钮

#### Scenario: Review approved notification
- **WHEN** 用户资料审核通过
- **THEN** 系统通过 Notification 组件推送"资料审核已通过"

### Requirement: Profile update frequency limit
系统 SHALL 限制用户每日资料修改次数为 5 次。

#### Scenario: Show remaining updates
- **WHEN** 用户进入编辑资料页
- **THEN** 页面加载时查询当日剩余修改次数，在表单顶部显示提示条"今日还可修改 X 次"

#### Scenario: Limit reached
- **WHEN** 用户当日修改次数达到 5 次后尝试保存
- **THEN** 保存按钮置灰，hover 提示"今日修改次数已达上限，请明天再试"

### Requirement: Prevent duplicate submission
系统 SHALL 在保存按钮点击后进入 loading 状态，防止重复提交。

#### Scenario: Double click save button
- **WHEN** 用户快速点击保存按钮多次
- **THEN** 仅发送一次请求，按钮显示 loading 并禁用
