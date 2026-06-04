## ADDED Requirements

### Requirement: Level information display
系统 SHALL 展示用户当前等级、经验值进度、等级名称和等级徽章。

#### Scenario: User views level info
- **WHEN** 用户进入 `/content/my-level` 页面
- **THEN** 顶部展示等级徽章图标（大图）、等级名称（如 "LV.5 成长达人"）、经验值进度条（当前值/下一等级阈值）、升级提示

#### Scenario: User at max level
- **WHEN** 用户已达最高等级
- **THEN** 进度条满格，显示"已达最高等级"

### Requirement: Points and growth value separation
系统 SHALL 将积分与成长值分开展示，明确区分"消耗型"和"成长型"两种资产。

#### Scenario: User views separated assets
- **WHEN** 用户进入我的等级页
- **THEN** 积分与成长值分栏展示：左栏为积分余额+今日获取+今日消耗，右栏为成长值+等级+经验衰减状态

### Requirement: Level benefits display
系统 SHALL 展示当前等级对应的权益列表。

#### Scenario: User views level benefits
- **WHEN** 用户查看等级权益卡片
- **THEN** 展示等级徽章、评论框特效预览、文件上传额度、视频清晰度、话题创建额度、客服优先级

### Requirement: Level system explanation
系统 SHALL 提供等级体系说明，包括等级阈值表、经验获取规则、经验衰减规则。

#### Scenario: User views level system rules
- **WHEN** 用户点击等级体系说明（可折叠面板）
- **THEN** 展开显示等级阈值表、经验获取规则、经验衰减规则（30天未登录开始衰减，7天保护期）

### Requirement: Level up congratulations
系统 SHALL 在用户升级时弹出全局祝贺弹窗，展示新等级和解锁的权益。

#### Scenario: Level up detected via API response **[阻塞: 后端未实现 levelChanged 字段]**
- **WHEN** 任意 API 响应携带 `levelChanged` 字段
- **THEN** defHttp 拦截器检测到后通过 mitt 广播 `growth:level-up` 事件

#### Scenario: Global congratulations popup
- **WHEN** App.vue 监听到 `growth:level-up` 事件且不在 7 天冷却期内
- **THEN** 弹出全局祝贺弹窗："恭喜您升级到 LV.X！" + 新等级徽章 + 解锁的新权益列表

#### Scenario: Cooldown period prevents popup
- **WHEN** 用户 7 天内已看过升级弹窗
- **THEN** 升级正常生效但不弹窗，用户进入等级页时通过数据对比自行感知

#### Scenario: Duplicate level-up events in same tick
- **WHEN** 多个 API 响应同时携带 `levelChanged`
- **THEN** 仅触发一次弹窗（事件去重，同一轮事件循环内合并）

### Requirement: Level up animation
系统 SHALL 在升级时播放数字跳动动画。

#### Scenario: Level up animation on level page
- **WHEN** 用户在我的等级页且触发升级
- **THEN** 等级信息卡片播放数字跳动动画（CountTo 组件），动画流畅 60fps
