## ADDED Requirements

### Requirement: 设备列表查看
系统 SHALL 在设备管理页展示当前用户所有活跃设备列表，包含设备图标（PC/手机/平板）、设备名称、最后登录时间、IP 地址、地理位置、信任状态标签。当前设备显示"当前设备"标签并置顶。

#### Scenario: 查看设备列表
- **WHEN** 用户进入个人中心 → 账号安全 → 设备管理
- **THEN** 系统调用 `GET /api/v1/account-security/devices`，按最后登录时间倒序显示设备列表

#### Scenario: 设备列表为空
- **WHEN** 用户所有设备已下线
- **THEN** 显示空状态插图"暂无登录设备"

#### Scenario: 加载中状态
- **WHEN** 设备列表正在加载
- **THEN** 显示骨架屏

### Requirement: 设备下线
系统 SHALL 支持用户主动下线非当前设备。当前设备的"下线"按钮禁用。

#### Scenario: 下线非当前设备
- **WHEN** 用户点击非当前设备的"下线"按钮并确认
- **THEN** 系统调用 `POST /api/v1/account-security/devices/revoke`，设备从列表移除，显示"已下线"消息提示

#### Scenario: 当前设备下线按钮禁用
- **WHEN** 用户查看当前设备
- **THEN** "下线"按钮置灰禁用，显示"当前设备"标签

#### Scenario: 下线确认弹窗
- **WHEN** 用户点击"下线"按钮
- **THEN** 弹出确认弹窗"确定下线该设备吗？下线后该设备需要重新登录"

### Requirement: 信任设备管理
系统 SHALL 支持用户标记设备为信任设备或取消信任。信任设备登录时跳过异常登录检测。

#### Scenario: 信任设备
- **WHEN** 用户点击未信任设备的"信任设备"按钮并确认
- **THEN** 系统调用 `POST /api/v1/account-security/devices/trust`，设备信任状态标签变为"信任"（绿色）

#### Scenario: 取消信任
- **WHEN** 用户点击信任设备的"取消信任"按钮并确认
- **THEN** 系统调用 `POST /api/v1/account-security/devices/untrust`，标签变为"未信任"

#### Scenario: 当前设备信任状态不可修改
- **WHEN** 用户查看当前设备
- **THEN** 信任/取消信任按钮禁用

### Requirement: 设备数上限
系统 SHALL 限制最多 5 台设备同时在线。超出时新设备登录自动挤出最早设备。

#### Scenario: 第 6 台设备登录
- **WHEN** 用户在第 6 台设备登录
- **THEN** 最早登录的设备被自动挤出，被挤出设备在列表中标记"已被挤出"
