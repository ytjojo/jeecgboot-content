## ADDED Requirements

### Requirement: 更新日志页面 SHALL 按时间倒序展示版本记录

更新日志页面 SHALL 使用时间线（Timeline）按时间倒序展示版本记录。每个版本节点 SHALL 显示：版本号、更新日期、新增功能（绿色标签+列表）、优化内容（蓝色标签+列表）、修复问题（橙色标签+列表）。

#### Scenario: 查看更新日志
- **WHEN** 用户进入更新日志页面
- **THEN** 页面按时间倒序展示版本记录，每个版本显示版本号、日期和分类内容列表

---

### Requirement: 更新日志 SHALL 支持搜索功能名称

更新日志页面 SHALL 支持搜索功能名称。搜索时 SHALL 实时过滤，匹配的版本节点 SHALL 高亮关键词。

#### Scenario: 搜索功能名称
- **WHEN** 用户输入功能名称进行搜索
- **THEN** 列表仅显示包含匹配功能的版本节点，关键词高亮

#### Scenario: 搜索无结果
- **WHEN** 用户搜索不存在的功能名称
- **THEN** 列表为空，可选择清空搜索恢复全部展示

---

### Requirement: 首次登录有新版本时 SHALL 弹出提示

有新版本发布时，用户首次登录 SHALL 弹出 Modal 提示"查看最新版本更新"，点击 SHALL 跳转至更新日志页。用户关闭后本次登录不再提示。

#### Scenario: 首次登录有新版本
- **WHEN** 用户首次登录且有新版本发布
- **THEN** 弹出 Modal 提示"查看最新版本更新"

#### Scenario: 点击查看更新
- **WHEN** 用户点击 Modal 中的"查看更新"
- **THEN** 跳转至更新日志页

#### Scenario: 关闭提示
- **WHEN** 用户关闭 Modal
- **THEN** 本次登录不再弹出该提示

---

### Requirement: 更新日志移动端 SHALL 适配响应式布局

移动端（<768px）时间线节点 SHALL 改为卡片堆叠布局。

#### Scenario: 移动端更新日志布局
- **WHEN** 用户在移动端访问更新日志
- **THEN** 时间线节点以卡片堆叠方式展示

---

## 后端依赖

| API | 后端状态 | 说明 |
|-----|---------|------|
| `GET /api/v1/content/user/support/changelog/list` | ✅ 已暴露（2026-06-05） | `getChangelog(userId)` 已在控制器中暴露为 HTTP 端点 |

**数据结构差异**:
- 前端期望 `{ id, version, releaseDate, features[], improvements[], bugfixes[] }`
- 后端 `ContentChangelogVO` 实际为 `{ version, releaseDate, additions[], improvements[], fixes[] }`（无 `id`，`features` 对应 `additions`，`bugfixes` 对应 `fixes`）— 前端需做字段映射
