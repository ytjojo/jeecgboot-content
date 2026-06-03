## ADDED Requirements

### Requirement: 拉黑用户操作
系统 SHALL 在用户主页、内容卡片和评论区提供拉黑操作入口。点击后弹出确认弹窗，确认后调用拉黑接口，成功后显示全局消息"已拉黑"。

#### Scenario: 从用户主页拉黑用户
- **WHEN** 用户在用户主页点击「更多操作」→「拉黑该用户」
- **THEN** 弹出确认弹窗，标题"确认拉黑"，正文说明拉黑后果，确认按钮为红色 danger 样式
- **WHEN** 用户点击「确认拉黑」
- **THEN** 按钮显示 loading 并禁用，调用拉黑接口，成功后关闭弹窗并显示"已拉黑"

#### Scenario: 从内容卡片拉黑用户
- **WHEN** 用户在内容卡片点击「···」→「拉黑该用户」
- **THEN** 弹出拉黑确认弹窗，确认后调用拉黑接口并刷新信息流

#### Scenario: 从评论区拉黑用户
- **WHEN** 用户在评论项点击「···」→「拉黑该用户」
- **THEN** 弹出拉黑确认弹窗，确认后调用拉黑接口

#### Scenario: 拉黑自己
- **WHEN** 用户尝试拉黑自己
- **THEN** 前端拦截，不弹出确认弹窗，不调用接口

#### Scenario: 拉黑操作失败
- **WHEN** 拉黑接口返回失败
- **THEN** 关闭弹窗，显示全局错误消息"操作失败，请重试"

### Requirement: 解除拉黑操作
系统 SHALL 在黑名单管理页提供解除拉黑操作。点击后弹出确认弹窗，说明关注关系不会自动恢复。

#### Scenario: 解除拉黑
- **WHEN** 用户在黑名单列表点击「解除拉黑」
- **THEN** 弹出确认弹窗，正文说明"解除拉黑后，您可以正常查看该用户内容，但之前的关注关系不会自动恢复。确定解除拉黑？"
- **WHEN** 用户点击确认
- **THEN** 调用解除拉黑接口，成功后从列表移除并显示"已解除拉黑"

### Requirement: 屏蔽用户操作
系统 SHALL 在用户主页、内容卡片和评论区提供屏蔽操作入口。点击后弹出说明性弹窗，确认后调用屏蔽接口。

#### Scenario: 屏蔽用户
- **WHEN** 用户点击「屏蔽该用户」
- **THEN** 弹出确认弹窗，标题"确认屏蔽"，正文说明"屏蔽后，您将不再看到该用户在信息流中的内容，但仍可访问其主页。对方不受影响，关注关系保持不变。确定屏蔽？"
- **WHEN** 用户点击确认
- **THEN** 调用屏蔽接口，成功后显示"已屏蔽，该用户内容将不再出现在信息流中"

#### Scenario: 取消屏蔽用户
- **WHEN** 用户在屏蔽列表点击「取消屏蔽」
- **THEN** 立即生效，无需确认弹窗，调用取消屏蔽接口

### Requirement: 不感兴趣反馈
系统 SHALL 在内容卡片提供"不感兴趣"操作，点击后立即从信息流移除该内容，并在原位置显示气泡选项。

#### Scenario: 点击不感兴趣
- **WHEN** 用户在内容卡片点击「···」→「不感兴趣」
- **THEN** 内容卡片立即从视图移除（乐观更新），同时静默调用 `POST /content/user/not-interested`（@RequestParam: userId, contentId, contentType）

#### Scenario: 气泡选项展示
- **WHEN** 不感兴趣接口返回成功
- **THEN** 在内容卡片原位置显示气泡，选项根据内容数据动态生成：
  - 若 `category` 字段存在，显示「屏蔽此类内容」
  - 若 `topics` 数组非空，显示「屏蔽该话题」
  - 始终显示「知道了」

#### Scenario: 选择屏蔽此类内容
- **WHEN** 用户点击「屏蔽此类内容」
- **THEN** 调用 `POST /content/user/filter-rule`（userId, ruleType=CONTENT_TYPE, value=category），成功后显示"已屏蔽该类型内容"

#### Scenario: 选择屏蔽该话题
- **WHEN** 用户点击「屏蔽该话题」
- **THEN** 调用 `POST /content/user/filter-rule`（userId, ruleType=TOPIC, value=topic），成功后显示"已屏蔽该话题"

### Requirement: 黑名单管理页
系统 SHALL 提供黑名单管理页，展示已拉黑用户列表，支持搜索和解除拉黑。

#### Scenario: 查看黑名单
- **WHEN** 用户进入设置 > 隐私设置 > 黑名单
- **THEN** 显示已拉黑用户列表，包含头像、昵称、拉黑时间，按拉黑时间倒序排列

#### Scenario: 搜索黑名单
- **WHEN** 用户在搜索框输入用户昵称
- **THEN** 列表按昵称过滤显示

#### Scenario: 空状态
- **WHEN** 黑名单为空
- **THEN** 显示空状态插图 + 文案"暂无拉黑用户"

### Requirement: 屏蔽列表管理页
系统 SHALL 提供屏蔽列表管理页，含屏蔽用户、屏蔽话题、屏蔽内容类型、临时屏蔽四个 Tab。

#### Scenario: Tab 切换
- **WHEN** 用户进入屏蔽列表管理页
- **THEN** 默认选中"屏蔽用户" Tab，可切换到其他 Tab

#### Scenario: 批量取消屏蔽
- **WHEN** 用户勾选多条记录后点击"批量取消屏蔽"
- **THEN** 弹出确认弹窗"确定取消选中的 N 项屏蔽？"，确认后批量删除

#### Scenario: 临时屏蔽倒计时
- **WHEN** 用户查看临时屏蔽 Tab
- **THEN** 显示剩余天数/小时（如"剩余 5 天"），到期后自动从列表消失

### Requirement: 屏蔽词设置页
系统 SHALL 提供屏蔽词设置页，支持添加关键词和正则表达式。

#### Scenario: 添加关键词
- **WHEN** 用户输入关键词并点击添加（或按回车）
- **THEN** 调用 `POST /content/user/filter-rule`（userId, ruleType=KEYWORD, value=keyword），成功后添加到列表

#### Scenario: 添加正则表达式
- **WHEN** 用户输入以 `/` 开头和结尾的正则表达式
- **THEN** 前端校验格式，有效则调用 `POST /content/user/filter-rule`（userId, ruleType=REGEX, value=regex），无效则提示"正则表达式格式错误，请检查"

#### Scenario: 重复屏蔽词
- **WHEN** 用户添加已存在的屏蔽词
- **THEN** 提示"该屏蔽词已存在"

#### Scenario: 屏蔽词数量上限
- **WHEN** 屏蔽词数量达到 100 个
- **THEN** 提示"屏蔽词数量已达上限"，禁止继续添加

#### Scenario: 删除屏蔽词
- **WHEN** 用户点击删除按钮
- **THEN** 调用 `POST /content/user/filter-rule/delete`（userId, ruleId），立即删除，3 秒内显示撤销提示条

### Requirement: 隐私设置聚合页
系统 SHALL 提供隐私设置聚合页，统一管理黑名单、屏蔽列表和屏蔽词入口。

#### Scenario: 入口卡片展示
- **WHEN** 用户进入设置 > 隐私设置
- **THEN** 显示三个入口卡片：黑名单（含数量角标）、屏蔽列表（含合计角标）、屏蔽词设置

#### Scenario: 帮助说明
- **WHEN** 用户展开"拉黑与屏蔽的区别"折叠面板
- **THEN** 显示拉黑与屏蔽的对比表格

### Requirement: 被拉黑状态页面
系统 SHALL 根据访问者身份展示不同的被拉黑状态页面。

#### Scenario: 拉黑发起方访问被拉黑用户主页
- **WHEN** 拉黑发起方访问被拉黑用户主页
- **THEN** 显示模糊占位图 + 文案"无法查看该用户" + 副文案"您已拉黑该用户，如需查看可在黑名单中解除拉黑" + 按钮"前往黑名单管理"

#### Scenario: 被拉黑方访问拉黑发起方主页
- **WHEN** 被拉黑方访问拉黑发起方主页
- **THEN** 显示标准 404 风格占位页 + 文案"该用户不存在"，与真实不存在的用户页面一致

### Requirement: 屏蔽词命中内容展示
系统 SHALL 对被屏蔽词命中的内容默认折叠，提供展开/收起功能。

#### Scenario: 内容被屏蔽词命中
- **WHEN** 信息流中内容被屏蔽词命中
- **THEN** 内容卡片默认折叠，显示提示条"该内容包含屏蔽词，已折叠"，右侧「展开」按钮

#### Scenario: 展开命中内容
- **WHEN** 用户点击「展开」
- **THEN** 正常显示完整内容，提示条变为「收起」

### Requirement: API 封装
系统 SHALL 按领域拆分 API 封装，使用项目标准 `defHttp` 模式，路径和方法对齐实际后端端点。

#### Scenario: API 文件结构
- **WHEN** 开发者查看 API 文件
- **THEN** 存在 `src/api/content/block.ts`、`src/api/content/mute.ts`、`src/api/content/filterRule.ts`，每个文件使用 `enum Api` + 独立函数模式

#### Scenario: API 路径对齐后端
- **WHEN** 前端调用 API
- **THEN** 路径、HTTP 方法、参数风格与后端一致：
  - `POST /content/user/relation/block` — 拉黑（@RequestParam: userId, targetUserId）
  - `POST /content/user/relation/unblock` — 解除拉黑
  - `POST /content/user/relation/mute` — 屏蔽
  - `POST /content/user/relation/mute/cancel` — 解除屏蔽
  - `GET /content/user/relation/detail` — 查询关系
  - `GET /content/user/relation/blacklist` — 黑名单分页
  - `GET /content/user/relation/mute-list` — 屏蔽列表分页
  - `GET /content/user/relation/block-mute/help` — 帮助说明
  - `POST /content/user/filter-rule` — 添加屏蔽规则
  - `POST /content/user/filter-rule/delete` — 删除屏蔽规则
  - `POST /content/user/filter-rule/batch-delete` — 批量删除屏蔽规则
  - `GET /content/user/filter-rule/list` — 查询屏蔽规则列表
  - `POST /content/user/not-interested` — 不感兴趣反馈

### Requirement: 关系状态缓存
系统 SHALL 使用 Pinia Store 缓存用户关系状态，减少重复请求。

#### Scenario: 关系状态查询缓存
- **WHEN** 组件查询与目标用户的关系状态
- **THEN** 优先从 Store 缓存读取，缓存未命中时调用 `GET /content/user/relation/detail`（userId, targetUserId）并写入缓存

#### Scenario: 操作后更新缓存
- **WHEN** 拉黑/屏蔽/解除操作成功
- **THEN** 立即更新本地缓存，不等待下次查询

#### Scenario: 登出清空缓存
- **WHEN** 用户登出
- **THEN** 清空关系缓存
