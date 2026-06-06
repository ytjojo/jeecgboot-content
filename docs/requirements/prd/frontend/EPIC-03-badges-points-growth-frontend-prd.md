# EPIC-03 勋章、积分与成长体系 — 前端 PRD

> **史诗ID**: EPIC-03
> **域**: user（用户域）
> **变更ID**: user-03-badges-points-growth-frontend
> **版本**: 1.0
> **前置依赖**: 无
> **日期**: 2026-06-02

---

## 1. 概述

### 1.1 需求目标

为内容社区构建游戏化激励体系的前端交互层，覆盖勋章展示与佩戴、积分获取与消耗明细、等级成长与权益展示、经验衰减规则公示四大模块。

**要解决的问题**：
- 用户缺少勋章获取进度的可视化展示，无法明确努力方向
- 积分获取和消耗无明细查询入口，用户无法对账
- 等级规则和权益不透明，用户缺乏长期激励感知
- 经验衰减规则未公示，降级体验缺乏引导

**期望达成的结果**：
- 勋章佩戴率 >40%
- 积分兑换率 >60%
- 日活跃用户提升 30%
- 用户平均停留时长提升 25%

### 1.2 目标用户与使用场景

| 角色 | 使用场景 | 入口 |
|------|---------|------|
| 注册用户 | 查看勋章、佩戴勋章、查询积分明细、查看等级和权益 | 个人主页 → 我的勋章 / 积分明细 / 我的等级 |
| 注册用户 | 兑换商城权益、解锁功能、赠送虚拟礼物 | 个人主页 → 积分商城 / 积分明细 → 兑换入口 |
| 注册用户 | 查看他人勋章与等级 | 他人主页、帖子卡片、评论区 |
| 管理员 | 回收违规勋章 | 管理后台 → 用户管理 → 勋章管理 |

**设备场景**: 桌面端为主，移动端需响应式适配。

### 1.3 范围定义

#### 本期范围
- 勋章分类展示、获取条件与进度、佩戴设置（最多 5 个）、主页/帖子/评论区域展示、过期勋章展示、勋章详情弹窗
- 积分获取明细、消耗明细、类型筛选、时间范围筛选、分页查询
- 积分兑换商城页面、兑换确认弹窗、功能解锁、虚拟礼物赠送
- 等级与经验值展示、等级权益展示、升级祝贺提示、等级徽章展示
- 经验衰减规则公示页面、降级保护状态展示、衰减提醒通知

#### 非本期范围
- 独立支付结算和创作者收益提现页面
- 完整商城履约、物流、库存管理页面
- 推荐算法配置后台
- 任务中心完整 UI（本期仅展示任务完成奖励结果）

---

## 2. 用户故事

| 编号 | 用户故事 | 优先级 | 依赖 |
|------|---------|--------|------|
| US-3.1.1 | 作为用户，我希望看到勋章分类（成就/身份/活动/关系）和获取条件与进度，以便理解勋章意义并明确努力方向 | P0 | EPIC-01 |
| US-3.1.2 | 作为用户，我希望选择佩戴最多 5 个勋章并在主页/帖子/评论区展示，以便体现成就与身份 | P0 | US-3.1.1 |
| US-3.1.3 | 作为用户，我希望过期勋章自动收起；作为管理员，我能回收违规勋章并留痕 | P1 | US-3.1.2 |
| US-3.2.1 | 作为用户，我希望通过日常行为获得积分，并看到每日上限提示 | P0 | EPIC-01 |
| US-3.2.2 | 作为用户，我希望通过创作激励获得积分 | P0 | US-3.2.1 |
| US-3.2.3 | 作为用户，我希望通过社交行为和任务体系获得积分 | P1 | US-3.2.1 |
| US-3.2.4 | 作为用户，我希望在兑换商城使用积分兑换权益 | P0 | US-3.2.1 |
| US-3.2.5 | 作为用户，我希望使用积分解锁功能和赠送礼物 | P1 | US-3.2.4 |
| US-3.2.6 | 作为用户，我希望查询积分获取与消耗明细，并支持筛选 | P0 | US-3.2.1 |
| US-3.3.1 | 作为用户，我希望通过行为获得经验值并提升等级 | P0 | EPIC-01 |
| US-3.3.2 | 作为用户，我希望积分与成长值分开展示，理解"消耗型"与"成长型"差异 | P0 | US-3.2.1, US-3.3.1 |
| US-3.3.3 | 作为用户，我希望不同等级有标识特权和功能特权 | P1 | US-3.3.1 |
| US-3.3.4 | 作为用户，我希望等级对内容分发有小幅加权 | P2 | US-3.3.1 |
| US-3.3.5 | 作为用户，我希望了解经验值衰减规则和降级保护机制 | P1 | US-3.3.1 |

---

## 3. 页面设计

### 3.1 页面清单

| 页面 | 路由建议 | 说明 |
|------|---------|------|
| 我的勋章页 | `/content/my-badges` | 勋章分类浏览、佩戴设置入口 |
| 勋章详情弹窗 | 弹窗组件 | 展示勋章条件、进度、获得时间 |
| 积分明细页 | `/content/point-detail` | 积分流水列表、筛选、余额展示 |
| 积分商城页 | `/content/point-mall` | 可兑换商品卡片列表 |
| 兑换确认弹窗 | 弹窗组件 | 确认兑换、积分余额校验 |
| 功能解锁弹窗 | 弹窗组件 | 确认解锁、积分扣除 |
| 虚拟礼物赠送弹窗 | 弹窗组件 | 选择礼物、确认赠送 |
| 我的等级页 | `/content/my-level` | 等级信息、经验进度、权益展示、衰减规则 |
| 勋章管理页（管理员） | `/content/badge-manage` | 勋章回收操作、审计日志 |

### 3.2 我的勋章页

**页面结构**：
```
Page
  ├── 顶部统计区：已获得勋章数 / 佩戴中勋章数 / 总勋章数
  ├── 分类标签页（Tabs）：全部 / 成就类 / 身份类 / 活动类 / 关系类 / 已过期
  ├── 佩戴设置入口按钮（右上角）
  └── 勋章卡片网格（CardList）
       ├── 已获得勋章：彩色图标 + 名称 + 获得日期角标
       ├── 未获得勋章：灰色图标 + 名称 + 进度条（如 7/10）
       └── 已过期勋章：灰色图标 + "已过期" 标签
```

**交互要求**：
- 点击勋章卡片 → 打开勋章详情弹窗
- 点击"佩戴设置"按钮 → 进入佩戴编辑模式，卡片变为可勾选状态
- 佩戴编辑模式下：勾选勋章（最多 5 个），已勾选勋章高亮边框，超出 5 个时提示"最多佩戴 5 个勋章"
- 保存佩戴设置 → 调用 API → 成功提示"佩戴设置已更新" → 退出编辑模式
- 取消佩戴 → 恢复之前的选择状态

**勋章详情弹窗**：
- 已获得勋章：勋章大图 + 名称 + 分类标签 + 获得时间 + 有效期（如有）+ 佩戴状态
- 未获得勋章：勋章大图 + 名称 + 分类标签 + 获取条件说明 + 进度条（当前值/目标值）+ 剩余要求文字
- 已过期勋章：勋章大图 + 名称 + "已过期" 状态标签 + 过期时间

**状态与边界情况**：
- 空状态：无勋章时显示空状态插图 + "暂无勋章，完成任务可获得勋章"引导文案
- 加载状态：骨架屏占位
- 勋章图片加载失败：显示默认占位图标

### 3.3 积分明细页

**页面结构**：
```
Page
  ├── 顶部余额卡片：当前积分余额（大字号）+ 今日获取 / 今日消耗（小字）
  ├── 筛选区（行内布局）
  │    ├── 类型筛选（Select）：全部 / 仅获取 / 仅消耗
  │    ├── 时间范围（DatePicker.RangePicker）：开始日期 ~ 结束日期
  │    └── 重置按钮
  └── 积分流水列表（Table）
       ├── 列：时间 | 类型（获取/消耗标签）| 变更数量（+/- 颜色区分）| 余额 | 来源说明 | 业务ID
       └── 分页（Pagination）
```

**交互要求**：
- 所有筛选条件变更后均自动刷新列表（防抖 300ms），移除"查询"按钮，仅保留"重置"按钮
- 防抖适用范围：Select 切换（类型筛选）和 DatePicker 变更（时间范围选择）
- 点击"重置"清空筛选条件并立即刷新
- 积分获取数量显示为绿色 `+XX`，消耗显示为红色 `-XX`
- 列表按时间倒序排列
- 滚动加载或分页加载（推荐分页，每页 20 条）

**状态与边界情况**：
- 空状态：无积分记录时显示"暂无积分记录" + 引导文案
- 筛选无结果：显示"没有符合条件的记录"
- 加载状态：Table 骨架屏

### 3.4 积分商城页

**页面结构**：
```
Page
  ├── 顶部余额展示：当前积分余额
  ├── 分类标签（可选）：全部 / 虚拟装扮 / 曝光券 / 功能解锁 / 虚拟礼物
  └── 商品卡片网格（CardList）
       ├── 商品图标/图片
       ├── 商品名称
       ├── 所需积分（醒目展示）
       ├── 库存状态（有库存 / 已售罄）
       └── 兑换按钮
```

**交互要求**：
- 点击"兑换"按钮 → 打开兑换确认弹窗
- 兑换确认弹窗：商品信息 + 所需积分 + 当前余额 + 差额提示（积分不足时显示"还差 XX 积分"）+ 确认/取消按钮
- 积分不足时，"确认兑换"按钮禁用，显示差额提示
- 兑换成功 → 全局消息提示"兑换成功" + 更新余额 → 关闭弹窗
- 兑换失败 → 弹窗内显示错误信息，保留弹窗状态
- 已售罄商品：卡片置灰，"兑换"按钮禁用并显示"已售罄"

**状态与边界情况**：
- 空状态：无可兑换商品时显示"暂无可兑换商品"
- 积分余额为 0：所有兑换按钮禁用，提示"积分不足"

### 3.5 我的等级页

**页面结构**：
```
Page
  ├── 等级信息卡片（顶部醒目区域）
  │    ├── 等级徽章图标（大图）
  │    ├── 等级名称（如 "LV.5 成长达人"）
  │    ├── 经验值进度条（当前经验值 / 下一等级阈值）
  │    └── 升级提示（如 "再获得 200 经验值即可升级"）
  ├── 积分与成长值分栏展示
  │    ├── 左栏：积分余额 + 今日获取 + 今日消耗
  │    └── 右栏：成长值 + 等级 + 经验衰减状态（如有）
  ├── 当前等级权益卡片
  │    ├── 等级徽章
  │    ├── 评论框特效预览
  │    ├── 文件上传额度
  │    ├── 视频清晰度
  │    ├── 话题创建额度
  │    └── 客服优先级
  ├── 等级体系说明（可折叠面板）
  │    ├── 等级阈值表
  │    ├── 经验获取规则
  │    └── 经验衰减规则（30天未登录开始衰减，7天保护期）
  └── 衰减状态提示区（条件展示）
       ├── 衰减中：提示"您已 X 天未登录，经验值正在衰减"
       ├── 保护期中：提示"您处于降级保护期，还剩 X 天"
       └── 已降级：提示"您的等级已降至 LV.X"
```

**交互要求**：
- 经验值进度条使用动画过渡，升级时播放庆祝动画（数字跳动 + 光效）
- 升级发生时弹出祝贺弹窗："恭喜您升级到 LV.X！" + 解锁的新权益列表
- 等级体系说明默认折叠，点击展开查看详细规则
- 衰减状态提示区使用警告色背景，仅在对应状态下展示
- 积分与成长值分栏展示，明确区分"消耗型"和"成长型"两种资产

**状态与边界情况**：
- 最高等级：进度条满格，显示"已达最高等级"
- 衰减中：进度条显示衰减后的值，配合警告色
- 保护期中：进度条显示当前值，底部提示保护期剩余天数

### 3.6 勋章管理页（管理员）

**页面结构**：
```
Page
  ├── 查询表单（Form + schemas）
  │    ├── 用户ID / 用户名
  │    ├── 勋章名称
  │    └── 查询 / 重置按钮
  ├── 数据表格（Table）
  │    ├── 列：用户 | 勋章 | 获得时间 | 状态 | 操作
  │    └── 操作列：回收按钮
  └── 回收确认弹窗（Modal）
       ├── 回收原因（必填文本域）
       └── 确认 / 取消按钮
```

**交互要求**：
- 点击"回收"按钮 → 打开回收确认弹窗
- 回收原因必填，最多 200 字，实时字数统计
- 确认回收 → 调用 API → 成功提示"勋章已回收" → 刷新列表
- 回收操作不可撤销，需二次确认

---

## 4. 组件设计

### 4.1 新增业务组件

| 组件名 | 路径 | 说明 |
|--------|------|------|
| `BadgeCard` | `src/components/content/BadgeCard/` | 勋章卡片，展示图标、名称、状态、进度 |
| `BadgeGrid` | `src/components/content/BadgeGrid/` | 勋章网格容器，支持选择模式 |
| `BadgeDetail` | `src/components/content/BadgeDetail/` | 勋章详情弹窗内容 |
| `BadgeDisplay` | `src/components/content/BadgeDisplay/` | 佩戴勋章展示组件（用于主页/帖子/评论），通过 `badges` prop 接收数据，内部不发起 API 请求 |
| `PointBalance` | `src/components/content/PointBalance/` | 积分余额展示卡片 |
| `LevelCard` | `src/components/content/LevelCard/` | 等级信息卡片（徽章 + 进度条 + 等级名称） |
| `LevelBenefitList` | `src/components/content/LevelBenefitList/` | 等级权益列表展示 |
| `GrowthProgress` | `src/components/content/GrowthProgress/` | 经验值进度条（含动画） |
| `ExchangeConfirm` | `src/components/content/ExchangeConfirm/` | 兑换确认弹窗内容 |
| `GiftSendModal` | `src/components/content/GiftSendModal/` | 虚拟礼物赠送弹窗 |
| `DecayWarning` | `src/components/content/DecayWarning/` | 衰减状态警告提示组件 |

### 4.2 复用现有组件

| 场景 | 复用组件 | 说明 |
|------|---------|------|
| 积分明细表格 | `Table`（`src/components/Table/`） | 配置 columns、api、pagination |
| 勋章管理表格 | `Table` | 管理员列表页 |
| 兑换确认弹窗 | `Modal`（`src/components/Modal/`） | 使用 useModal hook |
| 勋章详情弹窗 | `Modal` | 使用 useModal hook |
| 查询表单 | `Form`（`src/components/Form/`） | schema 驱动配置 |
| 页面容器 | `Page`（`src/components/Page/`） | 统一页面布局 |
| 按钮 | `Button`（`src/components/Button/`） | 支持权限控制 |
| 图标 | `Icon`（`src/components/Icon/`） | Iconify + SVG |
| 数字动画 | `CountTo`（`src/components/CountTo/`） | 积分/经验值变化动画 |
| 卡片列表 | `CardList`（`src/components/CardList/`） | 勋章网格、商品网格 |
| 描述列表 | `Description`（`src/components/Description/`） | 勋章详情、等级权益 |
| 消息提示 | `useMessage`（`src/hooks/web/`） | 操作反馈 |
| 抽屉 | `Drawer`（`src/components/Drawer/`） | 移动端详情查看 |

### 4.3 BadgeDisplay 数据传递策略

`BadgeDisplay` 组件采用 **prop 驱动** 设计，内部不发起 API 请求，避免 N+1 请求问题。

**Props 定义**：

```typescript
interface BadgeDisplayProps {
  badges: Array<{
    badgeCode: string;
    badgeName: string;
    iconUrl: string;
    rarity?: string;
  }>;
  size?: 'small' | 'medium'; // small=24px(帖子/评论), medium=32px(主页)
  maxDisplay?: number; // 最多显示数量，默认 5
}
```

**各使用场景的数据来源**：

| 使用场景 | 数据来源 | 说明 |
|---------|---------|------|
| 个人主页 | 页面级调用 `getWornBadges()`，通过 prop 传入 | 单次请求 |
| 帖子列表 | 帖子列表 API 响应中直接返回 `authorBadges` 字段 | 后端 join 查询，避免 N+1 |
| 帖子详情/评论区 | 帖子详情 API 响应中返回作者 `authorBadges` 字段 | 后端 join 查询 |
| 他人主页 | 页面级调用 `getWornBadges(userId)`，通过 prop 传入 | 单次请求 |

**后端接口调整要求**：帖子列表和帖子详情接口需在响应中增加 `authorBadges` 字段，包含作者佩戴的勋章信息（badgeCode、badgeName、iconUrl、rarity）。

### 4.4 勋章图片资源管理策略

**图片尺寸规范**：

| 用途 | 尺寸 | CSS 类名 | 使用场景 |
|------|------|---------|---------|
| 小图 | 24px x 24px | `.badge-icon-sm` | 帖子卡片、评论区佩戴展示 |
| 中图 | 48px x 48px | `.badge-icon-md` | 勋章卡片列表、商城商品 |
| 大图 | 96px x 96px | `.badge-icon-lg` | 勋章详情弹窗、等级徽章 |

**图片资源来源**：
- 勋章图片由运营通过管理后台上传，存储在 CDN
- 后端统一返回图片 URL（建议返回大图 URL），前端通过 CSS `background-size: contain` 控制显示尺寸
- 图片格式优先级：WebP > PNG > SVG，CDN 自动适配

**Fallback 策略**：
- 图片加载失败时显示 SVG 占位图标（体积小、可缩放、无锯齿）
- SVG 占位图标按勋章分类区分样式（成就/身份/活动/关系各一个默认图标）
- 占位图标内置于前端项目 `src/assets/icons/badge-fallback/` 目录

**加载策略**：
- 勋章列表页：所有勋章图片使用 `loading="lazy"` 懒加载
- 勋章详情弹窗：打开时预加载大图，避免弹窗内图片闪烁
- 佩戴勋章展示：小图优先从缓存读取，无缓存时异步加载

---

## 5. API 对接

### 5.1 勋章相关 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取勋章分类列表 | GET | `/content/user/growth/badge/catalog` | 返回按分类分组的勋章定义和用户授予状态 |
| 获取勋章详情 | GET | `/content/user/growth/badge/detail` | query: `badgeCode`，返回勋章条件、进度、授予信息 |
| 获取佩戴勋章 | GET | `/content/user/growth/badge/worn` | 返回用户当前佩戴的勋章列表 |
| 保存佩戴设置 | POST | `/content/user/growth/badge/wear` | body: `{ badgeIds: string[] }`，最多 5 个 |
| 获取用户佩戴展示 | GET | `/content/user/growth/badge/worn` | query: `userId`，返回指定用户佩戴的勋章（他人查看用） |
| 回收勋章（管理员） | POST | `/content/user/growth/badge/recycle` | body: `{ grantId, reason }` |

### 5.2 积分相关 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取积分余额 | GET | `/content/user/growth/summary` | 返回当前余额、今日获取、今日消耗及等级信息（合并接口） |
| 查询积分明细 | GET | `/content/user/growth/point/ledger` | params: `type, startTime, endTime, page, pageSize` |
| 获取兑换商品列表 | GET | `/content/user/growth/point/exchange/goods` | 返回可兑换商品列表（含库存） |
| 兑换商品 | POST | `/content/user/growth/point/exchange` | body: `{ goodsId, quantity, requestId }`，requestId 为前端生成的 UUID，用于幂等校验 |
| 解锁功能 | POST | `/content/user/growth/point/feature/unlock` | body: `{ featureCode }` |
| 赠送礼物 | POST | `/content/user/growth/point/gift/send` | body: `{ giftId, receiverId, quantity, message }` |

### 5.3 等级相关 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取等级信息 | GET | `/content/user/growth/summary` | 返回等级、经验值、进度、权益、衰减状态（合并接口，含积分余额） |
| 获取等级配置 | GET | `/content/user/growth/level/config` | 返回等级阈值表、权益配置 |
| 获取衰减规则 | GET | `/content/user/growth/decay/rule` | 返回衰减规则说明 |

### 5.4 API 封装规范

所有 API 封装放在 `src/api/content/` 目录下，使用 `defHttp` 统一请求：

```typescript
// src/api/content/badge.ts
import { defHttp } from '/@/utils/http/axios';

export const getBadgeCatalog = () => defHttp.get({ url: '/content/user/growth/badge/catalog' });
export const getBadgeDetail = (badgeCode: string) => defHttp.get({ url: '/content/user/growth/badge/detail', params: { badgeCode } });
export const getWornBadges = (userId?: string) => defHttp.get({ url: '/content/user/growth/badge/worn', params: { userId } });
export const saveWearConfig = (badgeIds: string[]) => defHttp.post({ url: '/content/user/growth/badge/wear', data: { badgeIds } });

// src/api/content/point.ts
export const getGrowthSummary = () => defHttp.get({ url: '/content/user/growth/summary' });
export const getPointLedger = (params) => defHttp.get({ url: '/content/user/growth/point/ledger', params });
export const getExchangeGoods = () => defHttp.get({ url: '/content/user/growth/point/exchange/goods' });
export const createExchange = (data: { goodsId: string; quantity: number; requestId: string }) => defHttp.post({ url: '/content/user/growth/point/exchange', data });

// src/api/content/growth.ts
export const getLevelConfig = () => defHttp.get({ url: '/content/user/growth/level/config' });
export const getDecayRule = () => defHttp.get({ url: '/content/user/growth/decay/rule' });
```

**响应格式**: `{ code: 200, result: any, message: string, success: boolean }`

---

## 6. 状态管理

### 6.1 新增 Store 模块

| Store | 文件 | 用途 |
|-------|------|------|
| `useBadgeStore` | `src/store/modules/badge.ts` | 勋章列表缓存、佩戴勋章缓存、分类筛选状态 |
| `usePointStore` | `src/store/modules/point.ts` | 积分余额缓存、商城商品缓存 |
| `useGrowthStore` | `src/store/modules/growth.ts` | 等级信息缓存、权益缓存、衰减状态 |

### 6.2 Store 设计要点

**useBadgeStore**：
- `badgeList`: 勋章列表（按分类分组），缓存有效期 5 分钟
- `wornBadges`: 当前佩戴的勋章列表，最多 5 个
- `activeCategory`: 当前选中的分类标签
- `fetchBadgeList()`: 拉取并缓存勋章列表
- `fetchWornBadges()`: 拉取佩戴勋章
- `updateWearConfig(badgeIds)`: 更新佩戴设置并同步缓存
- `refreshAfterRecycle()`: 勋章回收后刷新列表和佩戴状态

**usePointStore**：
- `balance`: 当前积分余额
- `todayEarn`: 今日获取
- `todaySpend`: 今日消耗
- `exchangeGoods`: 兑换商品列表
- `fetchBalance()`: 拉取余额
- `fetchExchangeGoods()`: 拉取商品列表
- `refreshAfterExchange()`: 兑换成功后立即刷新余额和商品库存

**useGrowthStore**：
- `levelInfo`: 等级、经验值、进度、权益
- `decayState`: 衰减状态（正常/衰减中/保护期/已降级）
- `levelConfig`: 等级阈值配置
- `fetchLevelInfo()`: 拉取等级信息
- `fetchLevelConfig()`: 拉取等级配置
- `checkLevelUp(previousLevel)`: 对比前后等级，检测升级事件并广播

### 6.3 跨 Store 状态同步机制

三个 Store 之间存在数据联动关系，需要保障跨 Store 数据一致性。采用 **Pinia `$onAction` + mitt 事件总线** 混合方案：

**联动场景与处理方式**：

| 场景 | 触发 Store | 联动操作 | 方式 |
|------|-----------|---------|------|
| 兑换商品成功 | `usePointStore` | 余额变更 → 刷新 `usePointStore.balance`；成长值可能变化 → 调用 `useGrowthStore.fetchLevelInfo()` | `$onAction` 拦截 |
| 勋章回收 | `useBadgeStore` | 刷新 `badgeList` + `wornBadges`，回收可能影响等级 → 调用 `useGrowthStore.fetchLevelInfo()` | `$onAction` 拦截 |
| 升级/降级事件 | `useGrowthStore` | 全局广播升级事件，触发祝贺弹窗 | mitt 事件总线 |
| 佩戴勋章变更 | `useBadgeStore` | 通知其他页面刷新佩戴展示区 | `$onAction` 拦截 |

**实现方案**：
- Store 内部联动：在 action 中直接调用其他 Store 的刷新方法（如 `usePointStore.createExchange()` 内调用 `useGrowthStore.fetchLevelInfo()`）
- 全局事件广播：使用 mitt 事件总线广播升级/降级事件，供任意页面监听（详见 6.4 节）

### 6.4 全局升级事件监听机制

升级事件可能在任意页面触发（非仅"我的等级页"），需要全局监听机制。

**实现方案**（采用方案 C + 方案 A 组合）：
1. **API 响应标记**：后端在所有 API 响应中可选携带 `levelChanged: { newLevel, previousLevel }` 字段
2. **全局拦截器**：在 `defHttp` 响应拦截器中检测 `levelChanged` 字段，检测到时通过 mitt 广播 `growth:level-up` 事件
3. **App.vue 监听**：在 `App.vue` 中监听 `growth:level-up` 事件，触发全局祝贺弹窗（`LevelUpCongratsModal`）
4. **降级保护**：用户 7 天内仅触发一次升级弹窗（本地 `localStorage` 记录上次弹窗时间）

```typescript
// src/utils/http/axios/index.ts（defHttp 响应拦截器补充）
if (response.data?.levelChanged) {
  const eventBus = useGrowthEventBus();
  eventBus.emit('growth:level-up', response.data.levelChanged);
}

// src/App.vue
import { useGrowthEventBus } from '/@/store/modules/growth';
const eventBus = useGrowthEventBus();
eventBus.on('growth:level-up', (payload) => {
  // 检查 7 天内是否已弹窗
  const lastShown = localStorage.getItem('lastLevelUpPopup');
  if (lastShown && Date.now() - Number(lastShown) < 7 * 24 * 60 * 60 * 1000) return;
  showLevelUpCongratsModal(payload);
  localStorage.setItem('lastLevelUpPopup', String(Date.now()));
});
```

### 6.3 缓存策略

- 勋章列表、等级配置等低频变更数据：Pinia 缓存 + 5 分钟 TTL
- 积分余额、等级经验值等高频变更数据：每次进入页面时刷新
- 佩戴勋章：操作后立即刷新缓存
- 兑换商品列表：进入商城页面时刷新

---

## 7. 组件选型

> 基于 `frontend-standards.md` 规范，优先复用现有组件。

| 需求场景 | 选用组件 | 备选 | 说明 |
|---------|---------|------|------|
| 勋章卡片网格 | `CardList` + 自定义 `BadgeCard` | - | 卡片内含图标、名称、进度条、状态标签 |
| 勋章分类切换 | Ant Design Vue `Tabs` | - | 自动导入，无需手动 import |
| 勋章佩戴选择 | `BadgeGrid`（基于 `CardList` 扩展） | - | 支持勾选模式，最多 5 个 |
| 勋章详情弹窗 | `Modal` + `Description` | `Drawer` | 移动端可用 Drawer 替代 |
| 积分明细表格 | `Table`（`src/components/Table/`） | `JVxeTable` | 不需要行编辑，基础 Table 即可 |
| 积分筛选表单 | `Form`（`src/components/Form/`） | - | schema 驱动，含 Select + DatePicker |
| 兑换商品卡片网格 | `CardList` | - | 卡片内含图片、名称、积分、兑换按钮 |
| 兑换确认弹窗 | `Modal` | - | 使用 useModal hook |
| 等级信息展示 | `Description` + 自定义 `LevelCard` | - | 等级徽章 + 进度条 + 权益列表 |
| 经验值进度条 | Ant Design Vue `Progress` | 自定义 `GrowthProgress` | 需支持动画过渡 |
| 等级体系说明 | Ant Design Vue `Collapse` | - | 可折叠面板 |
| 数字跳动动画 | `CountTo`（`src/components/CountTo/`） | - | 积分/经验值变化时使用 |
| 管理员勋章表格 | `Table` | `JVxeTable` | 列表展示 + 操作列 |
| 管理员查询表单 | `Form` | - | schema 驱动 |
| 操作反馈 | `useMessage` | - | 全局消息提示 |
| 权限控制 | `Button`（auth 属性） | `usePermission` | 管理员操作按钮 |

---

## 8. 交互设计

### 8.1 通用交互规则

- **加载状态**：所有数据请求期间显示骨架屏（`Skeleton`）或 `Spin`
- **空状态**：无数据时显示 `Empty` 组件 + 引导文案
- **错误反馈**：操作失败时显示 `useMessage().createMessage.error()` 错误提示，保留用户已输入内容
- **防重复提交**：提交类按钮在请求期间显示 loading 并禁用
- **危险操作确认**：勋章回收、积分兑换等不可逆操作需 `Modal.confirm` 二次确认
- **成功反馈**：操作成功后显示全局消息提示 + 自动关闭弹窗

### 8.2 勋章佩戴交互流程

```
用户点击"佩戴设置"
  → 进入编辑模式，勋章卡片出现勾选框
  → 用户勾选勋章（已获得的勋章才可勾选）
  → 勾选超过 5 个时提示"最多佩戴 5 个勋章"并阻止勾选
  → 用户点击"保存"
  → 按钮 loading + 调用 API
  → 成功：提示"佩戴设置已更新" + 退出编辑模式 + 刷新展示
  → 失败：提示错误信息 + 保持编辑模式
  → 用户点击"取消"：恢复之前的选择状态 + 退出编辑模式
```

### 8.3 积分兑换交互流程

```
用户在商城点击"兑换"按钮
  → 打开兑换确认弹窗
  → 弹窗展示：商品信息 + 所需积分 + 当前余额
  → 积分充足：显示"确认兑换"按钮（可点击）
  → 积分不足："确认兑换"按钮禁用 + 显示"积分不足，还差 XX 积分"
  → 用户点击"确认兑换"
  → 按钮 loading + 按钮完全禁用（防止重复点击）+ 调用 API（携带唯一 requestId）
  → 成功：提示"兑换成功" + 立即更新本地余额（乐观更新）+ 关闭弹窗
  → 失败（库存不足/并发等）：弹窗内提示错误信息 + 保留弹窗 + 恢复按钮可点击状态
  → 用户点击"取消"：关闭弹窗
```

**并发控制机制**：
- **前端防重复提交**：确认兑换按钮在请求 pending 期间完全禁用（`disabled + loading`），不响应任何点击事件
- **乐观更新**：兑换 API 返回成功后，立即用响应中的最新余额更新 `usePointStore.balance`，避免用户在余额未刷新时看到旧余额再次兑换
- **幂等请求**：每次兑换请求携带唯一 `requestId`（前端生成 UUID），后端基于 `requestId` 做幂等校验，防止网络重试导致重复兑换
- **弹窗锁定**：兑换请求 pending 期间，弹窗不可关闭（禁用右上角关闭按钮和遮罩层点击关闭），防止用户关闭弹窗后重复打开

### 8.4 勋章回收交互流程（管理员）

```
管理员在勋章管理表格点击"回收"
  → 打开回收确认弹窗
  → 弹窗展示：用户信息 + 勋章信息 + 回收原因输入框（必填，最多 200 字）
  → 输入原因后点击"确认回收"
  → 二次确认："确认回收该勋章？此操作不可撤销"
  → 确认后调用 API
  → 成功：提示"勋章已回收" + 关闭弹窗 + 刷新列表
  → 失败：提示错误信息 + 保留弹窗
```

### 8.5 升级祝贺交互

升级事件可能在任意页面触发（帖子列表、个人主页、积分商城等），需全局监听（详见 6.4 节实现方案）。

**全局升级祝贺流程**：
```
任意页面 API 响应携带 levelChanged 字段
  → defHttp 拦截器检测到 levelChanged
  → 通过 mitt 广播 growth:level-up 事件
  → App.vue 监听事件，检查 7 天弹窗冷却期
  → 未在冷却期内 → 弹出全局祝贺弹窗："恭喜您升级到 LV.X！"
  → 弹窗内容：新等级徽章 + 解锁的新权益列表 + "太棒了"确认按钮
  → 用户点击确认 → 关闭弹窗
  → 记录弹窗时间到 localStorage
  → 若当前在我的等级页 → 等级信息卡片播放数字跳动动画（CountTo）
```

**边界情况**：
- 多个 API 响应同时携带 `levelChanged`：仅触发一次弹窗（事件去重，同一轮事件循环内合并）
- 用户在弹窗冷却期内再次升级：升级正常生效，弹窗延迟到冷却期结束后下次进入页面时展示
- 网络断开恢复后：不补发升级弹窗，用户进入等级页时通过数据对比自行感知

### 8.6 勋章悬停展示

在帖子卡片、评论区等位置展示佩戴勋章时：
- 勋章图标以小尺寸（24px）横向排列，最多显示 5 个
- 鼠标悬停在勋章图标上 → Tooltip 显示勋章名称 + 获得原因
- 移动端：点击勋章图标显示 Tooltip

---

## 9. 响应式设计

### 9.1 断点规范

| 断点 | 宽度 | 布局策略 |
|------|------|---------|
| 桌面端 | >= 1200px | 标准多列布局 |
| 平板端 | 768px - 1199px | 两列布局，部分区域折叠 |
| 移动端 | < 768px | 单列布局，卡片堆叠 |

### 9.2 各页面响应式策略

**我的勋章页**：
- 桌面端：勋章卡片 4 列网格
- 平板端：勋章卡片 3 列网格
- 移动端：勋章卡片 2 列网格，分类标签可横向滚动

**积分明细页**：
- 桌面端：筛选区行内布局，表格完整展示所有列
- 平板端：筛选区两行布局，表格隐藏"业务ID"列
- 移动端：筛选区堆叠布局，表格转为卡片列表（每条记录一张卡片，展示时间、类型、数量、来源）

**积分商城页**：
- 桌面端：商品卡片 4 列网格
- 平板端：商品卡片 3 列网格
- 移动端：商品卡片 2 列网格

**我的等级页**：
- 桌面端：积分与成长值左右分栏
- 平板端：保持分栏，缩小间距
- 移动端：积分与成长值上下堆叠，等级权益列表改为横向滚动卡片

**勋章详情弹窗**：
- 桌面端：标准 Modal（宽度 480px）
- 移动端：转为 Drawer（从底部滑出）

---

## 10. 性能要求

| 指标 | 要求 | 说明 |
|------|------|------|
| 勋章图片加载 | < 1 秒 | 使用懒加载 + WebP 格式 + CDN |
| 勋章列表渲染 | < 500ms | 虚拟滚动（勋章数量 >50 时） |
| 积分明细查询 | < 1 秒 | 分页加载，每页 20 条 |
| 等级信息加载 | < 500ms | Pinia 缓存 + 骨架屏 |
| 兑换操作响应 | < 2 秒 | 按钮 loading + 全局消息 |
| 兑换商品列表 | < 1 秒 | 分页加载 |
| 升级动画 | 60fps | CountTo 数字动画，不阻塞主线程 |
| 佩戴勋章展示 | 即时 | 缓存优先，后台静默刷新 |

**优化策略**：
- 勋章图片使用 `loading="lazy"` + WebP 格式
- 勋章列表超过 50 个时启用虚拟滚动
- 积分明细使用分页而非无限滚动（便于对账定位）
- 等级配置、勋章列表等低频变更数据使用 Pinia 缓存 + TTL
- API 请求使用防抖（筛选操作 300ms 防抖）

---

## 11. 测试要点

### 11.1 功能测试

| 测试项 | 验证内容 |
|--------|---------|
| 勋章分类展示 | 四个分类（成就/身份/活动/关系）正确分组，"已过期"单独分类 |
| 勋章进度展示 | 未获得勋章显示进度条（当前值/目标值），进度数据准确 |
| 佩戴设置 | 最多选择 5 个，超出时提示；保存后立即生效；取消恢复原状态 |
| 佩戴展示 | 主页/帖子/评论区正确展示佩戴的勋章图标和 Tooltip；BadgeDisplay 通过 prop 接收数据，无额外 API 请求 |
| 勋章图片 fallback | 图片加载失败时显示 SVG 分类占位图标；懒加载正常生效 |
| 勋章过期 | 过期勋章自动移入"已过期"分类，从佩戴列表移除 |
| 勋章回收（管理员） | 回收原因必填，回收后用户收到通知，审计日志记录 |
| 积分明细 | 按时间倒序，获取/消耗类型筛选准确，时间范围筛选准确 |
| 积分兑换 | 积分充足时兑换成功并扣减余额，积分不足时提示差额 |
| 功能解锁 | 首次消耗积分解锁，再次使用不再扣费 |
| 礼物赠送 | 原子性：扣积分 + 发记录 + 发通知同时成功或失败 |
| 等级展示 | 积分与成长值分开展示，等级阈值准确 |
| 升级提示 | 经验值达到阈值时弹出祝贺弹窗，展示新权益 |
| 经验衰减 | 30 天未登录开始衰减，7 天保护期，活跃后停止衰减 |
| 降级保护 | 保护期内活跃恢复等级，保护期结束后降级并通知 |

### 11.2 交互测试

| 测试项 | 验证内容 |
|--------|---------|
| 防重复提交 | 兑换/赠送/佩戴保存等操作按钮在请求期间 loading + 禁用 |
| 兑换并发控制 | 快速连续点击"确认兑换"仅触发一次请求；兑换成功后余额立即更新；携带唯一 requestId |
| 危险操作确认 | 勋章回收需二次确认 |
| 空状态 | 无勋章/无积分记录/无兑换商品时显示正确引导 |
| 加载状态 | 所有数据请求期间显示骨架屏 |
| 错误处理 | API 失败时显示错误提示，保留用户输入 |
| 筛选联动 | 类型/时间筛选切换后自动或手动刷新列表 |

### 11.3 响应式测试

| 测试项 | 验证内容 |
|--------|---------|
| 勋章网格 | 桌面 4 列、平板 3 列、移动端 2 列 |
| 积分表格 | 移动端转为卡片列表 |
| 商城卡片 | 桌面 4 列、平板 3 列、移动端 2 列 |
| 等级页分栏 | 移动端堆叠为单列 |
| 弹窗适配 | 移动端转为底部 Drawer |

### 11.4 性能测试

| 测试项 | 验证内容 |
|--------|---------|
| 勋章图片 | 懒加载生效，首屏不加载非可视区域图片 |
| 大量勋章 | 50+ 勋章时列表渲染不卡顿 |
| 积分明细 | 1000+ 记录分页查询响应 < 1 秒 |
| 升级动画 | 动画流畅 60fps，不阻塞页面交互 |

---

## 12. 待确认问题 / 默认假设

### 待确认问题

1. **勋章动态特效资源**：运营上传配置还是固定枚举样式 key？本期假设为固定枚举样式 key（如 `none`、`glow`、`sparkle`），不支持自定义上传。
2. **积分兑换商品库存**：本期是否只处理虚拟权益库存（无限量/有限量），还是需要真实库存锁定？假设本期仅虚拟权益库存。
3. **等级推荐加权**：由当前模块提供权重接口即可，还是需要同步写入内容推荐特征表？假设本期仅提供权重接口。
4. **任务中心 UI**：任务系统是否已有独立 EPIC？假设本期仅展示任务完成后的奖励结果，不建设任务中心完整 UI。
5. **勋章佩戴展示位置**：除了主页、帖子卡片、评论区，是否还需要在其他位置展示（如私信、通知列表）？假设本期仅覆盖主页、帖子卡片、评论区三个位置。
6. **积分明细导出**：是否需要支持导出功能？假设本期不支持导出，仅支持页面查询。

### 默认假设

- 勋章图标使用运营预设的图片资源，支持 PNG/SVG/WebP 格式，单个图标不超过 200KB
- 积分明细查询默认分页每页 20 条，最大支持 100 条/页
- 等级阈值配置通过管理后台维护，前端仅读取展示
- 衰减规则说明页面为静态内容 + 动态配置参数混合展示
- 勋章详情弹窗在桌面端使用 Modal，移动端使用底部 Drawer
- 所有列表页默认按时间倒序排列
- 兑换商城商品排序由后端控制，前端不做二次排序
- 管理员勋章管理页面需要管理员角色权限校验
