## Why

频道数量增长后，用户缺少高效发现机制，只能通过直接链接或订阅关系找到频道；新用户冷启动体验差，优质频道缺乏曝光渠道。需要建立频道发现的完整前端能力，让用户通过推荐、排行榜、编辑精选、分类浏览和搜索等多种入口高效发现频道，同时为平台运营提供分类管理、精选配置等后台工具。

## What Changes

- 新增频道发现聚合页（推荐 + 排行榜 + 精选 + 分类入口 + 搜索入口），路由 `/channel/discovery`
- 新增分类浏览页，支持按官方分类树浏览全部可发现频道
- 新增频道搜索结果页，支持关键词搜索和多条件筛选
- 新增排行榜页，展示热门/新晋/系统频道排行榜
- 新增运营后台分类管理页，支持多级树形分类的增删改查和启停
- 新增运营后台编辑精选管理页，支持标记频道为精选并配置推荐语和有效期
- 新增频道管理后台标签管理模块，支持频道内自定义标签的增删改
- 新增频道创建/编辑表单中的分类选择组件（主分类 + 副分类）
- 新增 6 个业务组件：ChannelCard、CategoryTreeNav、SearchBar、FilterPanel、RankingList、CategoryManageTree
- 新增 3 个 Store：useChannelDiscoveryStore、useChannelCategoryStore、useChannelSearchStore
- 对接 19 个后端 API（分类、标签、推荐、排行榜、精选、搜索、浏览、发现聚合）

## Capabilities

### New Capabilities

- `channel-discovery`: 频道发现聚合页，包含推荐频道、排行榜入口、编辑精选、分类入口和搜索入口的聚合展示
- `channel-category-browse`: 分类浏览页，按分类树浏览频道，支持排序筛选和分页加载
- `channel-search`: 频道搜索结果页，关键词搜索 + 多条件筛选 + 结果排序
- `channel-ranking`: 排行榜页，热门/新晋/系统三类榜单，支持日/周/月维度切换
- `channel-category-manage`: 运营后台分类管理，多级树形分类的增删改查、启停、拖拽排序
- `channel-editorial-pick-manage`: 运营后台编辑精选管理，频道标记为精选、推荐语和有效期配置
- `channel-tag-manage`: 频道管理后台标签管理，频道内自定义标签的增删改
- `channel-category-select`: 频道创建/编辑表单中的分类选择组件，主分类必填 + 副分类最多 3 个

### Modified Capabilities

（无已有 capability 需要修改）

## Impact

- **前端路由**: 新增 6 个页面路由（发现页、分类浏览、搜索结果、排行榜、分类管理、精选管理）
- **组件层**: 新增 6 个业务组件，复用现有 Page/Tree/Table/Modal/Form/Description 等基础组件
- **状态管理**: 新增 3 个 Pinia Store，复用 useUserStore/usePermissionStore/useAppStore
- **API 层**: 新增 19 个 API 接口调用，使用 defHttp 封装。推荐接口和搜索接口需要 `userId` 参数，前端从 useUserStore 自动注入
- **依赖**: 依赖后端频道可见性服务（ChannelVisibilityService）已完成过滤，前端无需额外过滤
- **后端遗留**: 聚合接口 Controller、分类启用、标签编辑、搜索反馈 4 个端点尚待后端补充（见 backend-issues.md）
- **性能**: 搜索 P99 <= 200ms，推荐 P95 <= 500ms，需实现 5 分钟缓存策略和防抖/骨架屏/懒加载等优化
