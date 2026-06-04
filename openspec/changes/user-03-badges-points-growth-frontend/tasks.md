## 1. API 封装层

- [ ] 1.1 创建 `src/api/content/badge.ts`：封装勋章相关 API（badgeCatalog、badgeDetail、wornBadges、saveWornBadges、recycleBadge）—— 实际端点均在 `/content/user/growth/badge/*`
- [ ] 1.2 创建 `src/api/content/point.ts`：封装积分相关 API（growthSummary[含积分余额]、pointLedger、exchangeGoods、createExchange、unlockFeature、getFeatureUnlock、sendGift）—— 实际端点均在 `/content/user/growth/point/*`
- [ ] 1.3 创建 `src/api/content/growth.ts`：封装等级相关 API（growthSummary[含等级信息]、levelConfig、levelBenefit、decayRule）—— 实际端点均在 `/content/user/growth/*`

## 2. SVG 占位图标资源

- [ ] 2.1 创建 `src/assets/icons/badge-fallback/` 目录，添加四个分类的 SVG 占位图标（achievement.svg、identity.svg、activity.svg、relationship.svg）

## 3. Pinia Store 模块

- [ ] 3.1 创建 `src/store/modules/badge.ts`：实现 useBadgeStore（badgeList 缓存、wornBadges 缓存、activeCategory、fetchBadgeList、fetchWornBadges、updateWearConfig、refreshAfterRecycle）
- [ ] 3.2 创建 `src/store/modules/point.ts`：实现 usePointStore（balance、todayEarn、todaySpend、exchangeGoods、fetchBalance、fetchExchangeGoods、refreshAfterExchange）
- [ ] 3.3 创建 `src/store/modules/growth.ts`：实现 useGrowthStore（levelInfo、decayState、levelConfig、fetchLevelInfo、fetchLevelConfig、checkLevelUp）+ mitt 事件总线导出

## 4. 全局升级事件机制

- [ ] 4.1 修改 `src/utils/http/axios/index.ts`：在 defHttp 响应拦截器中增加 `levelChanged` 字段检测，检测到时通过 mitt 广播 `growth:level-up` 事件 **[阻塞] 后端尚未在响应中携带 levelChanged 字段，前端先完成代码实现**
- [ ] 4.2 修改 `src/App.vue`：监听 `growth:level-up` 事件，实现 7 天冷却期逻辑，弹出全局祝贺弹窗（LevelUpCongratsModal）

## 5. 勋章系统组件

- [ ] 5.1 创建 `src/components/content/BadgeCard/`：勋章卡片组件（图标、名称、状态标签、进度条、勾选模式）
- [ ] 5.2 创建 `src/components/content/BadgeGrid/`：勋章网格容器组件（基于 CardList 扩展，支持选择模式，最多 5 个）
- [ ] 5.3 创建 `src/components/content/BadgeDetail/`：勋章详情弹窗内容组件（已获得/未获得/已过期三种状态）
- [ ] 5.4 创建 `src/components/content/BadgeDisplay/`：佩戴勋章展示组件（prop 驱动，支持 small/medium 尺寸，Tooltip，图片 fallback，懒加载）

## 6. 积分系统组件

- [ ] 6.1 创建 `src/components/content/PointBalance/`：积分余额展示卡片组件（余额、今日获取、今日消耗）
- [ ] 6.2 创建 `src/components/content/ExchangeConfirm/`：兑换确认弹窗内容组件（商品信息、积分校验、差额提示、requestId 幂等）
- [ ] 6.3 创建 `src/components/content/GiftSendModal/`：虚拟礼物赠送弹窗组件

## 7. 等级成长组件

- [ ] 7.1 创建 `src/components/content/LevelCard/`：等级信息卡片组件（徽章、等级名称、经验值进度条、升级提示）
- [ ] 7.2 创建 `src/components/content/LevelBenefitList/`：等级权益列表展示组件
- [ ] 7.3 创建 `src/components/content/GrowthProgress/`：经验值进度条组件（含动画过渡、警告色）
- [ ] 7.4 创建 `src/components/content/DecayWarning/`：衰减状态警告提示组件（衰减中/保护期/已降级三种状态）

## 8. 勋章页面

- [ ] 8.1 创建 `/content/my-badges` 页面：顶部统计区、分类标签页、勋章卡片网格、空状态、加载骨架屏
- [ ] 8.2 实现勋章佩戴编辑模式：勾选逻辑（最多 5 个）、保存/取消、API 调用、缓存刷新
- [ ] 8.3 实现勋章详情弹窗：useModal hook 集成、三种状态内容展示、移动端 Drawer 降级
- [ ] 8.4 创建 `/content/badge-manage` 页面（管理员）：查询表单、数据表格、回收确认弹窗（二次确认、回收原因必填）

## 9. 积分页面

- [ ] 9.1 创建 `/content/point-detail` 页面：余额卡片、筛选区（类型 Select + 时间 DatePicker）、积分流水表格、分页、防抖 300ms
- [ ] 9.2 实现积分明细响应式：移动端表格转卡片列表
- [ ] 9.3 创建 `/content/point-mall` 页面：余额展示、分类标签、商品卡片网格、兑换按钮
- [ ] 9.4 实现兑换交互流程：兑换确认弹窗、积分校验、requestId 幂等、乐观更新、弹窗锁定、防重复提交

## 10. 等级成长页面

- [ ] 10.1 创建 `/content/my-level` 页面：等级信息卡片、积分与成长值分栏、等级权益卡片、等级体系说明（可折叠）、衰减状态提示区
- [ ] 10.2 实现升级祝贺弹窗组件（LevelUpCongratsModal）：新等级徽章、解锁权益列表、确认按钮
- [ ] 10.3 实现等级动画效果：CountTo 数字跳动、进度条动画过渡

## 11. 路由配置

- [ ] 11.1 在路由配置中添加 5 个新页面路由：`/content/my-badges`、`/content/point-detail`、`/content/point-mall`、`/content/my-level`、`/content/badge-manage`
- [ ] 11.2 配置路由权限：勋章管理页需管理员角色权限校验

## 12. 响应式适配

- [ ] 12.1 实现勋章页响应式：桌面 4 列、平板 3 列、移动端 2 列，分类标签横向滚动
- [ ] 12.2 实现积分明细响应式：筛选区堆叠布局、移动端表格转卡片列表
- [ ] 12.3 实现商城页响应式：商品卡片网格自适应列数
- [ ] 12.4 实现等级页响应式：移动端分栏堆叠、权益列表横向滚动卡片
- [ ] 12.5 实现弹窗响应式：桌面端 Modal、移动端 Drawer
