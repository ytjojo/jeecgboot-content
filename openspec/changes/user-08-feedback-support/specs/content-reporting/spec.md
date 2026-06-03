## ADDED Requirements

### Requirement: 举报类型选择
系统 SHALL 支持用户在举报时选择举报类型，包括但不限于色情、暴力、诈骗、骚扰等。

#### Scenario: 显示举报类型选项
- GIVEN: 用户发现违规内容并点击"举报"按钮
- WHEN: 系统弹出举报表单
- THEN: 系统显示举报类型选项列表，每个类型包含说明文字

### Requirement: 举报证据上传
系统 SHALL 支持用户上传图片或视频作为举报证据，单个文件最大 10MB。

#### Scenario: 上传图片证据
- GIVEN: 用户在举报表单中选择上传证据
- WHEN: 用户选择图片文件（<=10MB）
- THEN: 系统上传图片并显示在证据列表中

#### Scenario: 上传视频证据
- GIVEN: 用户在举报表单中选择上传视频
- WHEN: 用户选择视频文件（<=10MB）
- THEN: 系统上传视频并显示在证据列表中

#### Scenario: 文件超过大小限制
- GIVEN: 用户选择超过 10MB 的文件
- WHEN: 用户尝试上传
- THEN: 系统提示"文件大小不能超过 10MB"

### Requirement: 举报防重复
系统 SHALL 禁止同一用户对同一对象重复举报。

#### Scenario: 首次举报成功
- GIVEN: 用户未举报过某内容
- WHEN: 用户提交举报
- THEN: 系统创建举报记录并返回举报编号

#### Scenario: 重复举报被拒绝
- GIVEN: 用户已举报过某内容（同一 targetType + targetId）
- WHEN: 用户再次尝试举报同一对象
- THEN: 系统提示"您已举报过该内容，请勿重复举报"

### Requirement: 举报提交反馈
系统 SHALL 在举报提交成功后显示确认信息并生成举报编号。

#### Scenario: 举报提交成功
- GIVEN: 用户填写完整举报表单
- WHEN: 用户点击提交
- THEN: 系统显示"举报已提交，我们将尽快处理"并返回举报编号
