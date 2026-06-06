## ADDED Requirements

### Requirement: 圈子管理员可置顶内容
圈子管理员（版主/创建者）SHALL 能够对圈子内容执行置顶操作。置顶后内容排列在非置顶内容之前，多条置顶按 `pinned_at` 倒序排列。

#### Scenario: 版主置顶内容
- **WHEN** 版主在内容卡片操作菜单中点击"置顶"
- **THEN** 调用 `PUT /circle-content/{contentId}/pin?circleId={circleId}` 接口，成功后内容移动到列表顶部，展示置顶标识，Toast 提示"已置顶"

#### Scenario: 版主取消置顶
- **WHEN** 版主在已置顶内容的操作菜单中点击"取消置顶"
- **THEN** 弹出确认框"取消置顶后该内容将按普通排序展示"，确认后调用 API，内容恢复普通排序，Toast 提示"已取消置顶"

#### Scenario: 多条置顶排序
- **WHEN** 圈子内有多条置顶内容
- **THEN** 置顶内容按 `pinned_at` 倒序排列（后置顶的在前），非置顶内容按原有排序排列在置顶内容之后

### Requirement: 圈子管理员可标记精华
圈子管理员（版主/创建者）SHALL 能够对圈子内容标记精华。精华内容在列表和详情页展示精华标识（金色徽章）。

#### Scenario: 版主标记精华
- **WHEN** 版主在内容卡片操作菜单中点击"精华"
- **THEN** 调用 `PUT /circle-content/{contentId}/featured?circleId={circleId}` 接口，成功后内容卡片左上角展示精华标识，Toast 提示"已标记精华"

#### Scenario: 版主取消精华
- **WHEN** 版主在已精华内容的操作菜单中点击"取消精华"
- **THEN** 调用 API，成功后精华标识移除，Toast 提示"已取消精华"

### Requirement: 普通成员不可执行置顶/精华操作
普通成员 SHALL 看不到置顶/精华操作选项。若通过其他方式触发，前端 SHALL 提示"权限不足"。

#### Scenario: 普通成员查看操作菜单
- **WHEN** 普通成员打开内容卡片的操作菜单
- **THEN** 菜单中仅展示"举报"选项，不展示"置顶""精华"选项

#### Scenario: 普通成员尝试置顶操作
- **WHEN** 普通成员通过接口直接调用置顶 API
- **THEN** 前端 Toast 提示"权限不足"

### Requirement: 置顶/精华操作即时更新
置顶/精华操作成功后，列表 SHALL 即时更新状态，无需刷新页面。

#### Scenario: 置顶后列表即时更新
- **WHEN** 管理员置顶某内容
- **THEN** 列表立即重新排序，置顶内容移动到顶部，置顶标识即时展示
