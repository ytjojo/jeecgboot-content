## ADDED Requirements

### Requirement: 动态可见性四级控制
系统 SHALL 为浏览记录、点赞动态、收藏夹三项分别提供四级可见性控制（公开 / 仅关注者可见 / 仅互关可见 / 仅自己可见），使用 Select 下拉选择器。

#### Scenario: 设置浏览记录为仅自己可见
- **WHEN** 用户将浏览记录可见性设置为"仅自己可见"并保存
- **THEN** 该项下方显示提示文案"其他用户将无法在你的主页看到此内容"，保存后调用 `POST /content/user/settings/privacy/update`

#### Scenario: 可见性字段为 null 时的默认值
- **WHEN** 后端返回某项可见性字段为 null
- **THEN** 前端默认显示"公开"（PUBLIC）

#### Scenario: 收藏夹字段名映射
- **WHEN** 后端返回 `favoriteVisibility` 字段
- **THEN** 前端映射为 `favoritesVisibility`（注意 s 差异），保存时反向映射

### Requirement: 在线状态可见性
系统 SHALL 提供在线状态三级控制（公开 / 隐藏 / 仅互关可见），使用 Radio Group 横向排列。

#### Scenario: 设置在线状态为隐藏
- **WHEN** 用户选择"隐藏"选项
- **THEN** 显示提示"其他用户将看到你为离线状态"

#### Scenario: 设置在线状态为仅互关可见
- **WHEN** 用户选择"仅互关可见"选项
- **THEN** 显示提示"仅与你互关的好友可看到你的在线状态"

#### Scenario: 在线状态为 null 时的默认值
- **WHEN** 后端返回在线状态字段为 null
- **THEN** 前端默认显示"公开"（PUBLIC）

### Requirement: 搜索引擎索引控制
系统 SHALL 提供搜索引擎索引开关（Switch），控制个人主页是否被搜索引擎收录。

#### Scenario: 关闭搜索引擎索引
- **WHEN** 用户关闭搜索引擎索引开关
- **THEN** 显示文案"搜索引擎将不再收录你的个人主页"，默认关闭（保护隐私优先）

#### Scenario: 认证用户显示推荐标签
- **WHEN** 认证用户或创作者查看搜索引擎索引设置
- **THEN** 显示推荐开启标签

#### Scenario: 搜索引擎索引为 null 时的默认值
- **WHEN** 后端返回搜索引擎索引字段为 null
- **THEN** 前端默认显示为关闭状态

### Requirement: 隐私设置保存
系统 SHALL 在页面底部提供"保存"按钮，点击后调用隐私设置更新接口。

#### Scenario: 保存成功
- **WHEN** 用户点击保存，请求成功
- **THEN** 全局消息提示"隐私设置已保存"，缓存 5 分钟内失效，对其他用户即时生效

#### Scenario: 保存失败
- **WHEN** 用户点击保存，请求失败
- **THEN** 全局错误提示，保留用户已修改的状态

### Requirement: 页面数据加载
系统 SHALL 在页面加载时调用 `GET /content/user/settings/privacy` 获取当前隐私设置。

> **后端状态**: 该 GET 端点尚未实现（`ContentUserSettingsController` 中仅有 `POST /privacy/update`）。需后端补充，或前端临时改为从 `GET /content/user/profile/detail` 提取隐私字段。详见 `backend-issues.md`。

#### Scenario: 加载中显示骨架屏
- **WHEN** 用户进入隐私设置页
- **THEN** 数据加载完成前显示骨架屏（a-skeleton），加载完成后替换为实际内容

#### Scenario: 未登录访问
- **WHEN** 未登录用户尝试访问隐私设置页
- **THEN** 路由守卫拦截，跳转登录页
