## ADDED Requirements

### Requirement: 处罚通知包含申诉入口
系统 SHALL 在处罚通知中包含"申诉"入口链接。

#### Scenario: 收到内容删除通知
- GIVEN: 用户的内容被删除
- WHEN: 用户收到处罚通知
- THEN: 通知中包含"申诉"入口

### Requirement: 申诉提交
系统 SHALL 支持用户填写申诉理由并提交申诉，生成申诉编号。

#### Scenario: 提交申诉成功
- GIVEN: 用户点击"申诉"并填写申诉理由
- WHEN: 用户提交申诉
- THEN: 系统创建申诉记录并返回申诉编号

### Requirement: 申诉列表与状态
系统 SHALL 提供"我的申诉"页面，展示申诉列表和处理状态。

#### Scenario: 查看申诉列表
- GIVEN: 用户已提交过申诉
- WHEN: 用户进入"我的申诉"页面
- THEN: 系统显示申诉列表，包含申诉类型、目标、提交时间和当前状态

#### Scenario: 查看审核中申诉
- GIVEN: 用户有一条审核中的申诉
- WHEN: 用户查看该申诉状态
- THEN: 系统显示"审核中"及预计处理时间

### Requirement: 申诉审核结果通知
系统 SHALL 在申诉审核完成后通知用户结果。

#### Scenario: 申诉成功通知
- GIVEN: 申诉审核通过
- WHEN: 审核团队批准申诉
- THEN: 系统通知用户"申诉成功，处罚已撤销"

#### Scenario: 申诉驳回通知
- GIVEN: 申诉审核不通过
- WHEN: 审核团队驳回申诉
- THEN: 系统通知用户"申诉未通过，原因：XXX"
