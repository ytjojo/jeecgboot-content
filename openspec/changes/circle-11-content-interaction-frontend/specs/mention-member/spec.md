## ADDED Requirements

### Requirement: 输入 @ 触发成员选择浮层
用户在内容发布框或评论输入框中输入 `@` 字符时，SHALL 弹出成员选择浮层。

#### Scenario: 输入 @ 触发浮层
- **WHEN** 用户在输入框中输入 `@` 字符
- **THEN** 弹出浮层，展示当前圈子成员列表

#### Scenario: 浮层自动定位
- **WHEN** 输入框下方空间不足
- **THEN** 浮层自动翻转到输入框上方展示

### Requirement: 成员搜索过滤
成员选择浮层 SHALL 支持按昵称/姓名模糊搜索过滤。

#### Scenario: 输入关键词搜索
- **WHEN** 用户在搜索框中输入关键词
- **THEN** 成员列表按昵称/姓名模糊匹配过滤，防抖 300ms

#### Scenario: 搜索无结果
- **WHEN** 搜索关键词无匹配成员
- **THEN** 显示"未找到匹配成员"

#### Scenario: 圈子无成员
- **WHEN** 当前圈子无成员
- **THEN** 浮层显示"暂无可提及成员"

### Requirement: 选择成员插入提及标记
用户选择成员后，SHALL 在输入框中插入 @提及标记。

#### Scenario: 点击选择成员
- **WHEN** 用户点击成员列表中的某位成员
- **THEN** 输入框中插入 @提及标记（纯文本：`@{userId:xxx}昵称`，富文本：`<span class="mention" data-user-id="xxx">@昵称</span>`），浮层关闭

#### Scenario: 键盘选择成员
- **WHEN** 用户使用键盘上下键导航并按 Enter 确认
- **THEN** 选中当前高亮成员，插入提及标记，浮层关闭

#### Scenario: 关闭浮层
- **WHEN** 用户点击浮层外部或按 Esc
- **THEN** 浮层关闭，不插入标记

### Requirement: @提及内容解析渲染
包含 @提及标记的内容 SHALL 被解析为可点击链接。

#### Scenario: 渲染纯文本提及
- **WHEN** 内容包含 `@{userId:xxx}昵称` 格式
- **THEN** 前端正则匹配并渲染为可点击链接，点击跳转用户主页

#### Scenario: 渲染富文本提及
- **WHEN** 内容包含 `<span class="mention" data-user-id="xxx">@昵称</span>` 标签
- **THEN** 前端解析标签并渲染为可点击链接，点击跳转用户主页

### Requirement: @成员列表懒加载
> **后端现状**: 无独立的 @成员查询 Controller 端点。前端复用 `GET /content/circle/member/list?circleId={circleId}&current=1&size=100` 获取圈子成员列表作为 @提及候选列表。

@成员列表 SHALL 采用懒加载策略，首次打开浮层时请求。

#### Scenario: 首次打开浮层
- **WHEN** 用户首次打开 @成员浮层
- **THEN** 调用 `GET /content/circle/member/list?circleId={circleId}` 接口加载成员列表

#### Scenario: 后续打开浮层
- **WHEN** 用户再次打开 @成员浮层
- **THEN** 使用缓存数据，不重复请求

#### Scenario: 成员列表加载失败
- **WHEN** 成员列表接口请求失败（网络错误或服务端错误）
- **THEN** 浮层显示"加载失败"和"重试"按钮
