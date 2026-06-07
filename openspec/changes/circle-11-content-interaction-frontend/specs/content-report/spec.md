## ADDED Requirements

### Requirement: 成员可提交内容举报
圈子成员 SHALL 能够举报违规内容，举报原因为必填。

#### Scenario: 提交举报
- **WHEN** 成员在内容详情页或内容卡片操作菜单中点击"举报"，选择举报原因，点击"提交举报"
- **THEN** 调用 `POST /circle-report` 接口，成功后关闭弹窗，Toast 提示"举报已提交，管理员将尽快处理"

#### Scenario: 选择"其他"原因时补充说明必填
- **WHEN** 成员选择"其他"作为举报原因
- **THEN** 补充说明变为必填，未填写时提示"请填写补充说明"

#### Scenario: 重复举报
- **WHEN** 成员对已举报过的内容再次点击"举报"
- **THEN** 操作菜单中"举报"选项置灰，Tooltip 提示"已提交过举报"

### Requirement: 举报原因枚举
举报原因 SHALL 使用固定枚举：违规广告(AD)、色情低俗(PORNO)、恶意攻击(ATTACK)、其他(OTHER)。

#### Scenario: 展示举报原因
- **WHEN** 管理员查看举报列表
- **THEN** 使用后端返回的 `reasonLabel` 字段展示中文文案，或前端维护映射表进行转换

### Requirement: 管理员可查看举报列表
圈子管理员（创建者/版主）SHALL 能够查看举报列表，支持按状态筛选（待处理/已处理/已忽略）。

#### Scenario: 查看待处理举报
- **WHEN** 管理员进入举报处理页
- **THEN** 默认展示"待处理"标签页

#### Scenario: 空状态
- **WHEN** 无待处理举报
- **THEN** 展示空状态"暂无待处理举报"

### Requirement: 管理员可删除被举报内容
圈子管理员 SHALL 能够删除被举报内容。

#### Scenario: 删除被举报内容
- **WHEN** 管理员点击"删除内容"
- **THEN** 确认框"确认删除该内容？删除后举报者将收到通知"，确认后调用 `POST /circle-report/{reportId}/delete-content?circleId={circleId}`，卡片状态更新，Toast 提示"已删除"

#### Scenario: 被举报内容已被删除
- **WHEN** 被举报内容已被删除
- **THEN** 卡片显示"该内容已被删除"，仅展示忽略选项

### Requirement: 管理员可忽略举报
圈子管理员 SHALL 能够忽略举报。

#### Scenario: 忽略举报
- **WHEN** 管理员点击"忽略举报"
- **THEN** 确认框"确认忽略该举报？举报者将收到通知"，确认后调用 `POST /circle-report/{reportId}/ignore?circleId={circleId}`，卡片状态更新，Toast 提示"已忽略"

### Requirement: 创建者可禁言用户
圈子创建者 SHALL 能够禁言被举报用户，禁言时长选项为：1小时/1天/7天/30天/永久。

#### Scenario: 创建者禁言用户
- **WHEN** 创建者点击"禁言用户"，选择禁言时长，确认
- **THEN** 调用 `POST /circle-report/{reportId}/mute?circleId={circleId}`，卡片状态更新，Toast 提示"已禁言"
- **后端现状**: `POST /circle-report/{reportId}/mute` 不接受禁言时长参数。后端 `CircleMemberUpdateReq` 支持 `muteDuration`（1h/24h/7d/PERMANENT），但举报禁言接口未透传。降级方案：前端禁言弹窗仍展示时长选择 UI，但提交时不传 duration 参数（后端忽略），待后端对齐后再对接

#### Scenario: 版主无禁言权限
- **WHEN** 版主查看举报处理页
- **THEN** 不展示"禁言用户"按钮

### Requirement: 举报处理页权限控制
举报处理页入口 SHALL 仅对圈子管理员（创建者+版主）可见。

#### Scenario: 普通成员无入口
- **WHEN** 普通成员浏览圈子
- **THEN** 无举报处理页入口

### Requirement: 举报处理页响应式设计
举报处理页 SHALL 在移动端展示为卡片列表。

#### Scenario: 移动端展示
- **WHEN** 屏幕宽度小于 768px
- **THEN** 列表改为卡片列表，操作按钮收进"更多"菜单

### Requirement: 举报操作 loading/error 反馈
举报处理操作 SHALL 提供明确的 loading 和错误反馈。

#### Scenario: 删除/忽略/禁言操作中 loading
- **WHEN** 管理员点击删除内容/忽略举报/禁言用户
- **THEN** 操作按钮显示 loading 状态，防止重复点击

#### Scenario: 删除/忽略/禁言操作失败
- **WHEN** 操作 API 调用失败
- **THEN** Toast 提示"操作失败，请重试"，按钮恢复可点击状态

#### Scenario: 举报列表加载失败
- **WHEN** 举报列表接口请求失败
- **THEN** 展示"加载失败"和"重试"按钮
