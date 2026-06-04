## ADDED Requirements

> **API 路径**:
> - 删除频道: `DELETE /api/v1/channels/{id}` (已存在)
> - 撤销删除: `POST /api/v1/channels/{id}/cancel-delete` (已存在)
> - 删除前置校验: `GET /api/v1/channels/{id}/delete-check` (待后端实现)
> **Controller**: ChannelController
> **前端封装**: `src/api/content/channel/index.ts` - `deleteChannel()`, `cancelDelete()`, `checkDeletePrecondition()`

### Requirement: 发起频道删除

频道主 SHALL 能通过频道管理页 > 设置区域的红色危险按钮"删除频道"发起删除。点击后先调用接口校验前置条件，满足条件弹出二次确认弹窗，不满足条件弹窗显示阻塞原因列表。系统频道不显示"删除频道"按钮。

#### Scenario: 前置条件满足后弹出确认
- **WHEN** 频道主点击"删除频道"且前置条件校验通过
- **THEN** 弹出二次确认弹窗

#### Scenario: 前置条件不满足
- **WHEN** 频道主点击"删除频道"但前置条件不满足
- **THEN** 弹窗显示阻塞原因列表（如"频道内仍有 3 篇未清理内容"），无"继续删除"按钮

#### Scenario: 系统频道不显示删除按钮
- **WHEN** 管理员查看系统频道的设置区域
- **THEN** "删除频道"按钮不显示

### Requirement: 删除二次确认

删除确认弹窗 SHALL 要求用户输入频道名称进行确认。弹窗标题"确认删除频道"，正文说明 7 天冷静期机制，确认操作需输入频道名称对照，按钮为"取消"和"确认删除"（红色危险按钮）。

#### Scenario: 输入频道名确认删除
- **WHEN** 用户在确认弹窗中输入频道名称并点击"确认删除"
- **THEN** 频道进入 7 天冷静期，频道管理页顶部显示冷静期通知条

#### Scenario: 输入错误的频道名
- **WHEN** 用户输入的频道名称与实际不符
- **THEN** "确认删除"按钮保持禁用

### Requirement: 删除冷静期展示与撤销

删除后频道管理页顶部 SHALL 显示醒目通知条"频道正在删除冷静期中，剩余 X 天。[撤销删除]"。点击"撤销删除"弹窗确认后，频道恢复为 Active 状态。

#### Scenario: 冷静期通知条展示
- **WHEN** 频道处于 DeleteCooling 状态
- **THEN** 频道管理页顶部显示冷静期通知条，含剩余天数和"撤销删除"链接

#### Scenario: 撤销删除恢复频道
- **WHEN** 用户点击"撤销删除"并确认
- **THEN** 频道恢复为 Active 状态，管理入口恢复

### Requirement: 组织频道删除审批

组织频道删除 SHALL 提示"需要组织最高管理员确认"，发起后等待最高管理员审批。

#### Scenario: 组织频道删除需审批
- **WHEN** 组织频道主发起删除
- **THEN** 弹窗提示"需要组织最高管理员确认"，发起后进入等待审批状态
