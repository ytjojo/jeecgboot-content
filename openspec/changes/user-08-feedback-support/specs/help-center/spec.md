## ADDED Requirements

### Requirement: 帮助中心分类展示
系统 SHALL 在帮助中心页面展示常见问题分类（账号、隐私、举报、积分等）。

#### Scenario: 进入帮助中心
- GIVEN: 用户遇到问题
- WHEN: 用户进入"帮助中心"
- THEN: 系统显示常见问题分类列表（账号安全、举报申诉、隐私设置等）

### Requirement: 帮助文章搜索
系统 SHALL 支持用户通过关键词搜索帮助文章。

#### Scenario: 搜索帮助文章
- GIVEN: 用户想搜索特定问题
- WHEN: 用户输入关键词并搜索
- THEN: 系统返回相关的帮助文章列表，响应时间 <500ms

#### Scenario: 搜索无结果
- GIVEN: 用户输入的关键词无匹配文章
- WHEN: 用户执行搜索
- THEN: 系统显示"未找到相关文章"并提示联系客服

### Requirement: 帮助文章详情
系统 SHALL 展示帮助文章的详细步骤说明和截图。

#### Scenario: 查看帮助文章
- GIVEN: 用户点击某篇帮助文章
- WHEN: 页面加载完成
- THEN: 系统显示详细的步骤说明和截图

### Requirement: 文章反馈机制
系统 SHALL 支持用户对帮助文章进行"有用/无用"反馈。

#### Scenario: 标记文章有用
- GIVEN: 用户阅读了帮助文章
- WHEN: 用户点击"有用"
- THEN: 系统记录反馈并优化该文章排序

#### Scenario: 标记文章无用
- GIVEN: 用户阅读了帮助文章
- WHEN: 用户点击"无用"
- THEN: 系统记录反馈并提示"是否联系客服？"
