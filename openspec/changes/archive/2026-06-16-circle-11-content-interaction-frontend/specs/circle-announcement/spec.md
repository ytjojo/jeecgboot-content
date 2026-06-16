## ADDED Requirements

> **实现状态**: 公告发布功能已实现在 `src/views/channel/governance/AnnouncementManage.vue`（治理管理工具页），包含 Tinymce 富文本编辑器和历史版本管理（支持 `restoreAnnouncementVersion` 恢复历史版本）。公告顶部展示栏（`CircleAnnouncementBar`）已实现——`src/views/circle/components/CircleAnnouncementBar.vue`，支持公告内容展示、展开/收起、过期自动隐藏。
>
> **前后端对齐**: ✅ `CircleAnnouncement` 后端已有 `expireAt` 字段（`CircleAnnouncement.java`、`CircleAnnouncementReq.java`、`CircleAnnouncementVO.java`）。后端新增 `GET /history/{circleId}` 端点查询历史公告。前端 API 封装在 `src/api/content/circle/announcement.ts`（`publishCircleAnnouncement` / `deleteCircleAnnouncement` / `getActiveCircleAnnouncement` / `getCircleAnnouncementHistory`）。

### Requirement: 圈子管理员可发布公告
圈子管理员（版主/创建者）SHALL 能够发布公告，公告内容使用 Tinymce 富文本编辑器，有效期为必填项（仅截止时间）。

#### Scenario: 版主发布公告
- **WHEN** 版主在圈子管理区域点击"发布公告"，填写内容和有效期，点击"发布"
- **THEN** 调用 `POST /api/v1/content/channel/announcement` 接口（实际接口 `saveAnnouncement`，参数 `{ channelId, title, content, version }`），成功后关闭弹窗，Toast 提示"公告已发布"

#### Scenario: 已有公告时发布新公告
- **WHEN** 版主发布公告，且当前已有生效公告
- **THEN** 弹出确认框"当前已有生效公告，发布新公告将替换旧公告，是否继续？"，确认后调用 API，旧公告失效

#### Scenario: 公告内容为空提交
- **WHEN** 版主未填写公告内容直接点击"发布"
- **THEN** 字段下方显示红色错误提示"请输入公告内容"

#### Scenario: 有效期已过提交
- **WHEN** 版主设置的有效期早于当前时间
- **THEN** 字段下方显示"有效期不得早于当前时间"

### Requirement: 圈子公告顶部展示
圈子公告 SHALL 在圈子内容列表页顶部展示，仅当有生效公告时展示。

#### Scenario: 有生效公告时展示
- **WHEN** 圈子有生效公告
- **THEN** 调用 `GET /api/v1/content/channel/announcement/channel/{channelId}` 接口获取公告（实际接口 `getAnnouncement`），内容列表页顶部展示公告栏——注意：该展示功能实际代码中未实现，公告在治理管理页而非内容列表顶部展示

#### Scenario: 无公告时隐藏
- **WHEN** 圈子无生效公告（接口返回空）
- **THEN** 公告栏整个隐藏，不占空间

#### Scenario: 公告过期自动隐藏
- **WHEN** 公告到达有效期截止时间
- **THEN** 公告栏自动隐藏

#### Scenario: 展开/收起公告
- **WHEN** 用户点击"展开"
- **THEN** 显示完整公告内容，按钮变为"收起"

### Requirement: 圈子管理员可删除公告
圈子管理员 SHALL 能够删除当前生效公告。

#### Scenario: 管理员删除公告
- **WHEN** 管理员点击"删除"
- **THEN** 确认框"确认删除该公告？"，确认后调用 `DELETE /api/v1/content/channel/announcement/{id}` 接口（实际接口 `deleteAnnouncement`），成功后公告栏消失，Toast 提示"公告已删除"

#### Scenario: 删除操作失败
- **WHEN** 删除公告 API 调用失败
- **THEN** Toast 提示"删除失败，请重试"

### Requirement: 普通成员不可发布公告
普通成员 SHALL 看不到"发布公告"按钮入口。

#### Scenario: 普通成员查看公告区域
- **WHEN** 普通成员浏览圈子内容列表
- **THEN** 仅可查看公告内容，不展示"发布公告""删除"操作

### Requirement: 公告操作 loading 反馈
公告发布和删除操作 SHALL 提供明确的 loading 反馈。

#### Scenario: 发布/删除操作中 loading
- **WHEN** 管理员点击发布或删除
- **THEN** 操作按钮显示 loading 状态，防止重复点击

#### Scenario: 公告加载失败
- **WHEN** 公告查询接口请求失败
- **THEN** 公告栏不展示（静默失败，不影响页面其他功能）
