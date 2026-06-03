# profile-editing Specification

## Purpose
TBD - created by archiving change complete-profile-management-frontend. Update Purpose after archive.
## Requirements
### Requirement: Edit profile form page
系统 SHALL 提供编辑资料页面，支持用户编辑昵称、简介、性别、生日、地区、职业、个人链接等字段。表单提交调用 `POST /content/user/profile/update?userId=X`。

#### Scenario: Load edit profile page
- **WHEN** 用户从个人中心点击"编辑资料"按钮进入编辑页
- **THEN** 页面调用 `GET /content/user/profile/detail?ownerUserId=X&viewerUserId=X` 加载数据，表单字段回填当前值

#### Scenario: Save profile successfully
- **WHEN** 用户修改表单字段后点击保存按钮
- **THEN** 系统校验通过后调用 `POST /content/user/profile/update?userId=X` 提交 `ContentUserProfileUpdateReq`，保存成功后显示全局消息"资料已更新"并返回个人中心

#### Scenario: Cancel with unsaved changes
- **WHEN** 用户修改了表单字段后点击返回按钮
- **THEN** 系统弹出确认框"您有未保存的修改，确定离开吗？"

#### Scenario: Cancel without changes
- **WHEN** 用户未修改任何字段（isDirty=false）点击返回按钮
- **THEN** 系统直接返回上一页，不弹出确认框

### Requirement: Profile field validation
系统 SHALL 对资料字段进行实时校验和提交校验。nickname 必填（≤30 字符）、avatar 必填（≤512 字符）、bio（≤500 字符）、gender（MALE|FEMALE|OTHER|UNKNOWN）、birthday（过去日期）、region（≤64 字符）、profession（≤64 字符）、personalLink（http(s):// 格式，≤256 字符）。

#### Scenario: Empty nickname validation
- **WHEN** 用户清空 nickname 输入框并触发校验
- **THEN** 输入框下方显示红色错误提示"请输入昵称"

#### Scenario: Sensitive word in nickname
- **WHEN** 用户输入含敏感词的昵称并保存
- **THEN** 后端返回错误，前端显示提示"昵称包含不当内容，请修改"

#### Scenario: Character count display
- **WHEN** 用户在 bio 文本域中输入内容
- **THEN** 文本域右下角实时显示已输入字数/最大字数（如 123/500）

#### Scenario: URL format validation
- **WHEN** 用户在 personalLink 输入框输入非法 URL 并失去焦点
- **THEN** 输入框下方显示红色错误提示，阻止提交

#### Scenario: Future birthday validation
- **WHEN** 用户选择未来日期作为 birthday
- **THEN** DatePicker 拒绝该选择并提示"生日不能为未来日期"

### Requirement: Avatar and background upload via OSS direct upload
系统 SHALL 通过 OSS 客户端直传实现头像/背景图上传，不调用内容社区上传端点。

#### Scenario: Upload valid avatar via OSS
- **WHEN** 用户选择符合格式（JPG/PNG/WebP）和大小（≤5MB）要求的本地图片
- **THEN** 系统使用 STS 临时凭证调用 OSS SDK 上传图片，上传完成后回填 CDN URL 到表单 avatar 字段

#### Scenario: Upload invalid format rejected client-side
- **WHEN** 用户选择 BMP 格式图片
- **THEN** 系统在文件选择阶段拦截，显示提示"仅支持 JPG、PNG、WebP 格式"

#### Scenario: Upload oversized file rejected client-side
- **WHEN** 用户选择超过 5MB 的图片
- **THEN** 系统在文件选择阶段拦截，显示提示"图片大小必须小于 5MB"

#### Scenario: Background image upload with 16:9 crop
- **WHEN** 用户在主页设置页点击"更换背景"并选择有效图片
- **THEN** 系统以 16:9 比例打开裁剪弹窗，裁剪完成后 OSS 上传并更新预览

#### Scenario: Upload failure preserves avatar
- **WHEN** OSS 上传失败
- **THEN** 弹窗不关闭，显示"上传失败，请重试"

### Requirement: Profile review status display
系统 SHALL 展示资料审核状态，基于 `ContentUserProfileVO.profileReviewStatus` 字段。

#### Scenario: Pending review status
- **WHEN** 用户资料处于"待审核"状态（`profileReviewStatus=PENDING`）
- **THEN** 编辑页表单顶部显示黄色 Alert"您的资料正在审核中"，保存按钮禁用，字段不可编辑

#### Scenario: Review rejected
- **WHEN** 用户资料审核不通过（`profileReviewStatus=REJECTED`）
- **THEN** 编辑页显示红色 Alert"资料审核未通过，原因：{reason}"，提供"重新编辑"按钮

#### Scenario: Review approved notification
- **WHEN** 用户资料审核通过
- **THEN** 系统通过 Notification 组件推送"资料审核已通过"

### Requirement: Prevent duplicate submission
系统 SHALL 在保存按钮点击后进入 loading 状态，防止重复提交。

#### Scenario: Double click save button
- **WHEN** 用户快速点击保存按钮多次
- **THEN** 仅发送一次请求，按钮显示 loading 并禁用

### Requirement: Frequency limit is not displayed
系统 SHALL **不**展示"今日还可修改 X 次"提示；后端未暴露 update-count 接口。

#### Scenario: Save fails due to backend frequency limit
- **WHEN** 后端检测到频控超限并返回业务错误
- **THEN** 前端通过全局消息提示后端错误码对应的友好提示

