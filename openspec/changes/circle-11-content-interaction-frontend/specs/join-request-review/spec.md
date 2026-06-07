## ADDED Requirements

### Requirement: 查看加入申请列表
圈子管理员 SHALL 能够查看加入申请列表，支持按状态筛选（待处理/已处理/全部）。

#### Scenario: 查看待处理申请
- **WHEN** 管理员进入加入申请审核页
- **THEN** 调用 `GET /circle-join-review/list?circleId={circleId}&status=PENDING` 接口，默认展示"待处理"标签页，按申请时间排序

#### Scenario: 筛选已处理申请
- **WHEN** 管理员点击"已处理"标签
- **THEN** 调用 `GET /circle-join-review/list?circleId={circleId}&status=APPROVED,REJECTED` 接口，展示已批准和已拒绝的申请记录

#### Scenario: 空状态
- **WHEN** 无待处理申请
- **THEN** 展示空状态"暂无待处理申请"

#### Scenario: 列表加载失败
- **WHEN** 申请列表接口请求失败
- **THEN** 展示"加载失败"和"重试"按钮

### Requirement: 批准加入申请
圈子管理员 SHALL 能够批准用户的加入申请。

#### Scenario: 批准单条申请
- **WHEN** 管理员点击"批准"
- **THEN** 弹出确认框"确认批准该用户的加入申请？"，确认后调用 `POST /circle-join-review/approve?circleId={circleId}`（body: `{ requestId }`），卡片从列表移除，Toast 提示"已批准"

#### Scenario: 批量批准
- **WHEN** 管理员勾选多条申请后点击"批量批准"
- **THEN** 逐条调用批准 API，成功后所有已批准卡片从列表移除

#### Scenario: 批量批准部分失败
- **WHEN** 批量批准中部分申请处理失败
- **THEN** 已成功的卡片从列表移除，失败的卡片保留并 Toast 提示"部分操作失败，请重试"

### Requirement: 拒绝加入申请
圈子管理员 SHALL 能够拒绝用户的加入申请，拒绝原因为必填。

#### Scenario: 拒绝单条申请
- **WHEN** 管理员点击"拒绝"
- **THEN** 弹出输入框要求填写拒绝原因（必填），确认后调用 `POST /circle-join-review/reject?circleId={circleId}`（body: `{ requestId, rejectReason }`），卡片从列表移除，Toast 提示"已拒绝"

#### Scenario: 拒绝原因为空提交
- **WHEN** 管理员未填写拒绝原因直接确认
- **THEN** 输入框下方显示"请填写拒绝原因"

### Requirement: 超时提醒
超过 3 天未处理的申请 SHALL 展示橙色警告标识。

#### Scenario: 申请超时显示警告
- **WHEN** 申请的 `applyTime` 超过 3 天且状态为待处理
- **THEN** 申请卡片显示橙色警告标识

#### Scenario: 管理入口角标
- **WHEN** 有待处理的加入申请
- **THEN** 调用 `GET /circle-join-review/pending/{circleId}` 获取待审核列表，角标数字取列表长度。后端无独立 count 接口，前端通过列表长度计算

### Requirement: 加入申请列表响应式设计
加入申请列表 SHALL 在移动端展示为卡片列表。

#### Scenario: 移动端展示
- **WHEN** 屏幕宽度小于 768px
- **THEN** 列表改为卡片列表（卡片化），筛选区改为下拉菜单，操作按钮改为底部固定栏
