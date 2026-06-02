## ADDED Requirements

### Requirement: 推荐曝光上报

系统 SHALL 使用 IntersectionObserver 检测推荐卡片进入视口，延迟 500ms 批量合并上报曝光事件。

#### Scenario: 推荐卡片进入视口触发曝光
- **WHEN** 推荐卡片 50% 面积进入视口（threshold: 0.5）
- **THEN** 将圈子 ID 加入待上报集合

#### Scenario: 批量合并上报
- **WHEN** 待上报集合中有新增 ID 后延迟 500ms 无新 ID 加入
- **THEN** 调用 POST /recommend/exposure 批量上报所有待上报圈子 ID

#### Scenario: 500ms 内有新卡片进入
- **WHEN** 500ms 内有新卡片进入视口
- **THEN** 重置计时器（防抖模式），继续等待

---

### Requirement: 曝光去重策略

系统 SHALL 维护一个 Set<string> 记录本次会话已上报的圈子 ID，同一圈子在同一页面生命周期内 SHALL 仅上报一次曝光。

#### Scenario: 同一圈子不重复上报
- **WHEN** 用户反复滚动同一区域，同一圈子多次进入视口
- **THEN** 仅在首次进入时上报曝光，后续不重复上报

#### Scenario: 去重集合独立于 DOM
- **WHEN** 引入虚拟滚动后 DOM 节点被回收
- **THEN** 已进入上报集合的 ID 不受影响，仍保持去重

---

### Requirement: 页面离开保底上报

系统 SHALL 监听 visibilitychange 和 beforeunload 事件，在页面关闭或跳转时使用 navigator.sendBeacon 保底上报剩余未上报数据。

#### Scenario: 页面关闭时保底上报
- **WHEN** 用户关闭页面或切换到其他页面（visibilityState === 'hidden'）
- **THEN** 立即 flush 待上报集合，使用 navigator.sendBeacon 发送剩余数据

#### Scenario: beforeunload 备用保底
- **WHEN** visibilitychange 未触发（兼容性问题）
- **THEN** beforeunload 事件作为备用保底，发送剩余数据

---

### Requirement: 推荐点击上报

系统 SHALL 在用户点击推荐圈子卡片时即时上报点击事件，上报不阻塞页面跳转。

#### Scenario: 点击推荐圈子上报
- **WHEN** 用户点击推荐 Tab 中的圈子卡片
- **THEN** 调用 POST /recommend/click 上报（携带圈子 ID + source），同时跳转详情页

#### Scenario: 上报不阻塞跳转
- **WHEN** 点击上报请求未完成
- **THEN** 页面跳转不等待上报完成，上报使用异步发送

---

### Requirement: 上报失败静默处理

系统 SHALL 在曝光或点击上报失败时静默处理，不重试，不阻塞用户操作。

#### Scenario: 曝光上报失败
- **WHEN** POST /recommend/exposure 请求失败
- **THEN** 静默处理，不重试，不展示错误提示，仅开发环境控制台打印日志

#### Scenario: 点击上报失败
- **WHEN** POST /recommend/click 请求失败
- **THEN** 静默处理，不重试，不展示错误提示，仅开发环境控制台打印日志
