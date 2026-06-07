## ADDED Requirements

### Requirement: AnnouncementEditor 频道公告管理页面

系统 SHALL 提供频道公告管理页面，支持公告的发布、编辑、删除、预览和历史版本管理。

#### Scenario: 加载当前公告
- **WHEN** 管理员进入公告管理页
- **THEN** 调用 `GET /api/v1/content/channel/announcement/channel/{channelId}` 获取当前公告，展示公告状态（已发布/未发布）和公告内容

#### Scenario: 编辑公告内容
- **WHEN** 管理员在 Tinymce 富文本编辑器中编辑公告
- **THEN** 支持标题栏、加粗、斜体、链接、图片、列表等富文本编辑功能

#### Scenario: 预览公告
- **WHEN** 管理员点击"预览"
- **THEN** 在下方预览区展示渲染效果，通过 `POST /api/v1/content/channel/announcement/preview` 接口过滤不安全内容（**后端待实现**）

#### Scenario: 发布公告
- **WHEN** 管理员点击"发布公告"
- **THEN** 弹出二次确认弹窗"确认发布此公告？发布后将展示在频道顶部"，确认后调用 API 发布，前端立即更新频道顶部展示

#### Scenario: 保存草稿
- **WHEN** 管理员点击"保存草稿"
- **THEN** 调用 API 保存但不发布，记录修改人和修改时间

#### Scenario: 删除公告
- **WHEN** 管理员点击"删除公告"
- **THEN** 弹出二次确认弹窗"确认删除公告？删除后频道顶部不再展示"，确认后删除，公告进入历史版本

#### Scenario: 查看公告历史版本
- **WHEN** 管理员在公告管理页底部查看"公告历史"
- **THEN** 调用 `GET /api/v1/content/channel/announcement/{channelId}/history` 展示最近 3 个历史版本（**后端待实现**）（版本号、修改人、修改时间），支持"恢复此版本"操作

#### Scenario: 恢复历史版本
- **WHEN** 管理员点击某历史版本的"恢复此版本"
- **THEN** 调用 `POST /api/v1/content/channel/announcement/restore/{versionId}` 恢复为当前公告（**后端待实现**）

#### Scenario: 并发编辑冲突
- **WHEN** 管理员保存公告时后端检测到版本冲突
- **THEN** 前端提示"公告已被其他人修改，请刷新后重试"，展示最新版本内容供用户合并

#### Scenario: 发布生效延迟提示
- **WHEN** 公告发布后后端返回缓存刷新失败
- **THEN** 提示"公告已发布，频道顶部展示可能有短暂延迟"

#### Scenario: 移动端编辑器布局
- **WHEN** 视口宽度 < md 断点
- **THEN** 编辑器全宽展示，预览切换为 Tab 页

---

### Requirement: 频道顶部公告展示

系统 SHALL 在频道页面顶部展示当前公告。

#### Scenario: 展示频道公告
- **WHEN** 频道有已发布公告
- **THEN** 频道页面顶部展示公告内容，支持折叠/展开

#### Scenario: 无公告状态
- **WHEN** 频道无已发布公告
- **THEN** 频道顶部不展示公告区域
