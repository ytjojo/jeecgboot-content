## ADDED Requirements

### Requirement: 更新日志列表
系统 SHALL 提供"更新日志"页面，按时间倒序展示版本更新记录。

#### Scenario: 查看更新日志
- GIVEN: 用户进入"更新日志"页面
- WHEN: 页面加载完成
- THEN: 系统显示按时间倒序排列的版本更新记录列表

### Requirement: 版本详情展示
系统 SHALL 在每个版本记录中展示版本号、更新日期、新增功能、优化内容和修复问题。

#### Scenario: 查看版本详情
- GIVEN: 用户点击某个版本记录
- WHEN: 页面展开详情
- THEN: 系统显示版本号、更新日期、新增功能列表、优化内容列表和修复问题列表

### Requirement: 更新日志搜索
系统 SHALL 支持用户搜索功能名称以查找相关更新记录。

#### Scenario: 搜索更新记录
- GIVEN: 用户想了解某功能的变更
- WHEN: 用户搜索功能名称
- THEN: 系统显示包含该功能的更新记录

### Requirement: 新版本提示
系统 SHALL 在有新版本发布时，用户首次登录提示查看更新。

#### Scenario: 首次登录看到新版本提示
- GIVEN: 有新版本发布
- WHEN: 用户首次登录
- THEN: 系统提示"查看最新版本更新"
