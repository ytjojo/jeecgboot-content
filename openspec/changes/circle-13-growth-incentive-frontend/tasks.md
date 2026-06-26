## 1. 基础设施搭建

- [ ] 1.1 创建 API 封装文件 `src/api/content/growth.ts`，定义 `getCircleLevelInfo`、`getCircleLevelConfig`、`getCircleLevelBenefit`、`getMemberGrowth`、`getParticipationDays`、`getAchievements`、`getLeaderboard` 七个接口函数和对应 TypeScript 类型
- [ ] 1.2 创建 Pinia Store `src/store/modules/circleGrowth.ts`，定义 `CircleGrowthState` 状态结构和缓存策略（按 circleId 缓存）
- [ ] 1.3 创建组件目录 `src/components/circle/growth/` 和页面目录结构

## 2. 圈子等级展示组件

- [ ] 2.1 实现 `CircleLevelBadge` 组件 — 圈子等级标识（L1-L5 图标 + 名称 + 颜色映射）
- [ ] 2.2 实现 `CircleLevelProgress` 组件 — 等级进度条（当前等级 → 下一等级 + 百分比 + 分项指标展开）
- [ ] 2.3 在圈子详情页嵌入等级与成长区块（等级标识 + 进度条 + 已解锁权益）
- [ ] 2.4 对接 GET `/api/v1/content/circle/growth/level/info?circleId={circleId}` 接口，处理加载态、成功态、失败态

## 3. 成员个人成长信息页

- [ ] 3.1 实现 `GrowthOverviewCard` 组件 — 成长概览卡片（经验值、贡献值、排名三列 + CountTo 动画）
- [ ] 3.2 实现 `ParticipationStreak` 组件 — 连续参与进度（7 天时间轴，实心圆/空心圆/横线三种状态）
- [ ] 3.3 实现 `DailyExpBar` 组件 — 今日经验进度条（当前/100 上限，满格状态）
- [ ] 3.4 创建个人成长信息页 `src/views/circle/growth/index.vue`，组合上述组件 + 徽章摘要区域
- [ ] 3.5 对接 GET `/api/v1/content/circle/member_growth/info?circleId={circleId}&userId={userId}` 和 GET `/api/v1/content/circle/member_growth/participation?circleId={circleId}&userId={userId}` 接口，使用 `Promise.all` 并行请求等级信息、成长信息和连续参与天数
- [ ] 3.6 配置路由 `/circle/:id/growth` 挂载到圈子详情页子路由

## 4. 徽章系统

- [ ] 4.1 实现 `BadgeCard` 组件 — 单个徽章卡片（已获得点亮/未获得灰色/即将达成橙色边框 3 种状态）
- [ ] 4.2 实现 `BadgeWall` 组件 — 徽章墙（分组展示已获得/未获得徽章）
- [ ] 4.3 实现 `BadgeDetailModal` 组件 — 徽章详情弹窗（Modal + 条件说明 + 进度 + 获得时间）
- [ ] 4.4 创建徽章墙页 `src/views/circle/badges/index.vue`
- [ ] 4.5 对接 GET `/api/v1/content/circle/growth/achievement/list?circleId={circleId}&userId={userId}` 接口
- [ ] 4.6 配置路由 `/circle/:id/badges` 挂载到圈子详情页子路由

## 5. 排行榜

- [ ] 5.1 实现 `LeaderboardTabs` 组件 — 维度切换（经验值/贡献值/发帖数 Tab）+ 周期切换（本周/本月/累计 Segmented）
- [ ] 5.2 实现 `LeaderboardList` 组件 — 排行榜列表（Top 50 列表 + 前三名金银铜色 + 当前用户高亮 + 底部我的排名）
- [ ] 5.3 创建排行榜页 `src/views/circle/leaderboard/index.vue`
- [ ] 5.4 对接 GET `/api/v1/content/circle/growth/leaderboard?circleId={circleId}&dimension={dimension}&period={period}&currentUserId={userId}` 接口，支持 dimension 和 period 参数切换
- [ ] 5.5 配置路由 `/circle/:id/leaderboard` 挂载到圈子详情页子路由

## 6. 通知与实时刷新

- [ ] 6.1 接入站内通知 WebSocket，监听等级提升和徽章获得事件
- [ ] 6.2 收到通知后触发 Toast 提示（3 秒自动消失）+ 刷新对应圈子的成长数据
- [ ] 6.3 在圈子详情页、个人成长页、徽章墙页集成通知监听逻辑

## 7. 响应式适配与交互细节

- [ ] 7.1 桌面端（>=1200px）布局适配 — 三列概览卡、徽章网格 4 列
- [ ] 7.2 平板端（768-1199px）布局适配 — 徽章网格 3 列
- [ ] 7.3 移动端（375-767px）布局适配 — 概览卡单列堆叠、徽章网格 2 列、等级进度条垂直显示
- [ ] 7.4 骨架屏（Skeleton）集成 — 所有页面首屏加载态
- [ ] 7.5 空状态处理 — 各页面空状态展示引导文案和操作入口
- [ ] 7.6 错误处理 — 接口失败时展示错误提示 + 重试按钮
- [ ] 7.7 路由懒加载 — 成长相关页面使用动态 import 按需加载

## 8. 测试验证

- [ ] 8.1 功能测试 — 圈子等级展示（L1-L5 标识、进度条、权益）
- [ ] 8.2 功能测试 — 经验值系统（获得、上限、扣除、等级回退）
- [ ] 8.3 功能测试 — 连续参与进度（7 天时间轴、里程碑达成）
- [ ] 8.4 功能测试 — 徽章系统（获得、撤销、墙展示、详情弹窗）
- [ ] 8.5 功能测试 — 排行榜（维度切换、周期切换、用户高亮、空状态）
- [ ] 8.6 响应式测试 — 375px / 768px / 1200px 三个断点验证
- [ ] 8.7 性能测试 — 个人成长页和排行榜页 P95 < 1 秒
