## Why

内容社区缺少游戏化激励体系，用户无法感知成就积累、积分资产和成长进度，导致活跃度和留存率偏低。需要构建勋章展示与佩戴、积分获取与消耗、等级成长与权益展示、经验衰减规则公示四大前端模块，提升用户参与感和长期粘性。

**目标指标**：勋章佩戴率 >40%、积分兑换率 >60%、日活提升 30%、停留时长提升 25%。

## What Changes

- 新增勋章系统前端页面：勋章分类浏览、获取进度展示、佩戴设置（最多 5 个）、勋章详情弹窗、过期勋章管理
- 新增勋章佩戴展示：在个人主页、帖子卡片、评论区展示佩戴的勋章图标
- 新增积分系统前端页面：积分余额展示、积分明细查询（支持类型/时间筛选）、积分商城、兑换确认弹窗、功能解锁、虚拟礼物赠送
- 新增等级成长前端页面：等级信息展示、经验值进度条、等级权益列表、升级祝贺弹窗（全局监听）
- 新增经验衰减展示：衰减规则公示、降级保护状态提示、衰减提醒通知
- 新增管理员勋章管理页面：勋章回收操作、回收原因记录
- 新增 11 个业务组件、3 个 Pinia Store 模块、3 组 API 封装
- 新增全局升级事件监听机制（defHttp 拦截器 + mitt 事件总线）

## Capabilities

### New Capabilities

- `badge-system`: 勋章分类展示、获取条件与进度、佩戴设置、勋章详情弹窗、过期勋章管理、管理员回收
- `point-system`: 积分余额展示、积分明细查询（筛选+分页）、积分商城兑换、功能解锁、虚拟礼物赠送
- `growth-level`: 等级信息展示、经验值进度条、等级权益、升级祝贺弹窗（全局事件监听）、积分与成长值分栏展示
- `decay-notice`: 经验衰减规则公示、降级保护状态展示、衰减提醒通知
- `badge-display`: 佩戴勋章在主页/帖子/评论区的展示组件（prop 驱动，避免 N+1 请求）

### Modified Capabilities

（无已有 spec 需要修改）

## Impact

- **新增页面路由**：`/content/my-badges`、`/content/point-detail`、`/content/point-mall`、`/content/my-level`、`/content/badge-manage`
- **新增组件目录**：`src/components/content/` 下 11 个业务组件
- **新增 Store 模块**：`src/store/modules/badge.ts`、`point.ts`、`growth.ts`
- **新增 API 封装**：`src/api/content/badge.ts`、`point.ts`、`growth.ts`
- **修改全局拦截器**：`src/utils/http/axios/index.ts` 增加 levelChanged 检测
- **修改 App.vue**：增加全局升级事件监听
- **新增 SVG 占位图标**：`src/assets/icons/badge-fallback/` 目录
- **后端接口依赖**：帖子列表/详情接口需增加 `authorBadges` 字段（后端 join 查询）
- **依赖 EPIC-01**：勋章/积分/等级数据模型由 EPIC-01 后端提供
