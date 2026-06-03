## ADDED Requirements

### Requirement: Point balance display
系统 SHALL 展示用户当前积分余额、今日获取、今日消耗。

#### Scenario: User views point balance
- **WHEN** 用户进入积分明细页或积分商城页
- **THEN** 顶部展示当前积分余额（大字号）、今日获取和今日消耗（小字）

### Requirement: Point ledger query
系统 SHALL 提供积分明细查询功能，支持类型筛选和时间范围筛选，分页展示。

#### Scenario: User queries point ledger
- **WHEN** 用户进入 `/content/point-detail` 页面
- **THEN** 展示积分流水列表，列包含：时间、类型（获取/消耗标签）、变更数量（+绿色/-红色）、余额、来源说明、业务ID

#### Scenario: User filters by type
- **WHEN** 用户在类型筛选中选择"仅获取"
- **THEN** 列表仅显示获取类型记录，自动刷新（防抖 300ms）

#### Scenario: User filters by time range
- **WHEN** 用户选择时间范围
- **THEN** 列表仅显示该时间范围内的记录，自动刷新（防抖 300ms）

#### Scenario: User resets filters
- **WHEN** 用户点击"重置"按钮
- **THEN** 清空所有筛选条件并立即刷新列表

### Requirement: Point exchange mall
系统 SHALL 提供积分商城页面，展示可兑换商品列表。

#### Scenario: User views exchange mall
- **WHEN** 用户进入 `/content/point-mall` 页面
- **THEN** 展示商品卡片网格，每张卡片包含商品图标/图片、商品名称、所需积分、库存状态、兑换按钮

#### Scenario: User exchanges with sufficient points
- **WHEN** 用户点击"兑换"按钮且积分充足
- **THEN** 打开兑换确认弹窗，显示商品信息、所需积分、当前余额，"确认兑换"按钮可点击

#### Scenario: User exchanges with insufficient points
- **WHEN** 用户点击"兑换"按钮且积分不足
- **THEN** 弹窗显示"积分不足，还差 XX 积分"，"确认兑换"按钮禁用

#### Scenario: Exchange succeeds
- **WHEN** 用户点击"确认兑换"且 API 返回成功
- **THEN** 提示"兑换成功"，立即更新本地余额（乐观更新），关闭弹窗

#### Scenario: Exchange fails due to stock
- **WHEN** 兑换 API 返回库存不足错误
- **THEN** 弹窗内提示错误信息，保留弹窗状态，恢复按钮可点击

#### Scenario: Sold out goods
- **WHEN** 商品库存为 0
- **THEN** 商品卡片置灰，"兑换"按钮禁用并显示"已售罄"

### Requirement: Exchange concurrency control
系统 SHALL 防止积分兑换的并发竞态问题。

#### Scenario: Prevent duplicate exchange requests
- **WHEN** 用户快速连续点击"确认兑换"
- **THEN** 仅触发一次 API 请求，按钮在请求期间完全禁用（disabled + loading）

#### Scenario: Idempotent exchange request
- **WHEN** 前端发起兑换请求
- **THEN** 请求携带唯一 requestId（前端生成 UUID），后端基于 requestId 做幂等校验

#### Scenario: Modal lock during exchange
- **WHEN** 兑换请求 pending 期间
- **THEN** 弹窗不可关闭（禁用右上角关闭按钮和遮罩层点击关闭）

### Requirement: Feature unlock with points
系统 SHALL 支持使用积分解锁功能。

#### Scenario: User unlocks a feature
- **WHEN** 用户触发功能解锁操作
- **THEN** 调用 POST `/content/user/feature/unlock` API，成功后扣除积分并解锁功能

### Requirement: Virtual gift sending
系统 SHALL 支持使用积分赠送虚拟礼物。

#### Scenario: User sends a gift
- **WHEN** 用户在虚拟礼物赠送弹窗选择礼物并确认赠送
- **THEN** 调用 POST `/content/user/gift/send` API，原子性扣积分+发记录+发通知

### Requirement: Point empty state
系统 SHALL 在无积分记录时展示空状态引导。

#### Scenario: No point records
- **WHEN** 用户进入积分明细页且无记录
- **THEN** 显示"暂无积分记录"和引导文案

#### Scenario: Filter returns no results
- **WHEN** 筛选条件无匹配记录
- **THEN** 显示"没有符合条件的记录"
