## Why

当前内容社区缺少社交关系扩展能力：用户间互关关系不清晰（无互关标识）、创作者无法了解粉丝增长趋势、社区缺少邀请传播机制、治理角色缺乏透明度。这些问题导致互关率低、创作者流失、社区增长依赖自然流量、管理效率低下。本期通过前端实现互关标识、粉丝管理、邀请分享和社区角色展示，提升用户社交互动和社区治理效率。

## What Changes

- 新增互关标识组件（MutualFollowBadge），在用户主页和评论区展示互关关系
- 新增互关好友列表页，支持搜索和取消互关操作
- 新增"仅互关可见"私密内容发布与展示逻辑（发布表单新增可见性选项、Feed 流/用户主页/搜索结果的可见性控制）
- 新增粉丝管理页，包含粉丝列表（分页+搜索）和粉丝趋势图表（ECharts 折线图，支持日/周/月维度切换）
- 新增邀请分享页，包含邀请码生成、复制链接、邀请记录和收益统计
- 新增邀请链接落地页（`/invite/:inviteCode`），展示邀请人信息和注册引导
- 新增社区角色标签组件（CommunityRoleBadge），在评论区展示创作者/版主/管理员角色
- 新增版主/管理员评论区管理操作（删除评论、警告用户）及快捷跳转 EPIC-09 用户管理页
- 新增审计日志页（管理员专用），支持按操作类型和时间筛选
- 新增互关状态缓存 Store（useMutualFollowStore）和邀请信息缓存 Store（useInviteStore）
- 新增 API 封装文件：`invite.ts`、`fan-analytics.ts`、`governance.ts`

## Capabilities

### New Capabilities

- `mutual-follow`: 互关标识展示、互关好友列表页、互关状态缓存管理
- `private-content`: "仅互关可见"私密内容的发布、展示（Feed 流/用户主页/搜索）、权限控制
- `fan-analytics`: 粉丝列表（分页+搜索）、粉丝趋势图表（ECharts，日/周/月维度）
- `invite-system`: 邀请码生成与分享、邀请记录与收益统计、邀请链接落地页（含转化漏斗设计）
- `community-roles`: 社区角色标签组件（评论区展示创作者/版主/管理员标识）
- `moderation`: 版主/管理员评论区轻量管理操作（删除评论、警告用户）+ 跳转 EPIC-09 用户管理页
- `audit-log`: 审计日志页（管理员专用，按操作类型/时间筛选）

### Modified Capabilities

<!-- 无已存在的 spec 需要修改 -->

## Impact

- **前端页面**: 新增 5 个页面（互关好友列表、粉丝管理、邀请分享、邀请落地页、审计日志）+ 2 个内嵌组件区域（评论区角色标签、评论区管理操作）
- **前端组件**: 新增 3 个业务组件（MutualFollowBadge、CommunityRoleBadge、ModeratorActionModal）
- **API 层**: 新增 3 个 API 封装文件（invite.ts、fan-analytics.ts、governance.ts），对接 10+ 个后端接口
- **状态管理**: 新增 2 个 Store（useMutualFollowStore、useInviteStore），复用 useUserStore
- **依赖**: 需安装 ECharts（`echarts`）用于趋势图表和画像图表
- **路由**: 新增 6 条前端路由（互关列表、粉丝管理、邀请分享、邀请落地页、审计日志 + 系统管理菜单）
- **权限**: 需集成 usePermission Hook 控制版主/管理员操作按钮显隐
- **关联模块**: 依赖 EPIC-04（关注体系）、EPIC-01（注册登录）、EPIC-03（积分体系）、EPIC-02（个人资料）、EPIC-09（用户状态与治理）
