## 1. 基础设施层

- [x] 1.1 创建 API 封装层 `src/api/content/channel/index.ts` 和 `src/api/content/channel/model/channelModel.ts`，封装全部 15 个接口（创建、列表、详情、编辑、删除、转让、审核、校验等），使用 defHttp
- [x] 1.2 创建 Pinia Store `src/store/modules/channel.ts`，定义 currentChannel、channelList、reviewQueue、channelTypeOptions、channelStatusOptions 状态
- [x] 1.3 创建共用枚举类型定义：ChannelType（system/personal/organization）、ChannelStatus（DRAFT/PENDING_REVIEW/ACTIVE/REJECTED/DELETE_COOLING/DELETED）
- [x] 1.4 创建 ChannelTypeTag 组件（根据类型显示不同颜色 Tag：system=蓝、personal=绿、organization=紫）
- [x] 1.5 创建 ChannelStatusTag 组件（根据状态显示不同颜色和文案）
- [x] 1.6 创建 ChannelForm 组件（频道创建/编辑表单，根据频道类型动态渲染字段，schema 驱动）
- [x] 1.7 注册用户端路由：`/api/v1/content/channel/create`、`/api/v1/content/channel/list`、`/api/v1/content/channel/manage/:id`
- [x] 1.8 注册后台端路由：`/api/v1/content/channel/admin`、`/api/v1/content/channel/review`

## 2. 频道创建

- [x] 2.1 实现 ChannelCreateSteps 分步向导组件：Step 1 展示个人频道和组织频道两个大卡片选择，Step 2 根据类型动态渲染表单
- [x] 2.2 实现用户端频道创建页面 `/api/v1/content/channel/create`，集成 Steps 组件和 ChannelForm
- [x] 2.3 实现频道名称唯一性校验逻辑：失焦触发、300ms 防抖、校验中显示 loading 图标、重名红色提示
- [x] 2.4 实现表单校验规则：名称必填 1-50 字符、简介必填 1-200 字符、图标必填 ≤2MB、封面选填 ≤5MB、分类必填
- [x] 2.5 实现组织频道创建差异：自动展示绑定组织信息（不可编辑）、默认公开无隐私设置选项、上限 50 个
- [x] 2.6 实现审核等待页：频道名称和状态展示、审核进度时间线（Timeline 组件）、预计审核时间动态计时、帮助入口、"返回我的频道"按钮
- [x] 2.7 实现后台系统频道创建 Modal 弹窗：表单字段（名称、简介、图标、封面、分类、置顶权重），提交后直接创建状态为 Active
- [x] 2.8 实现创建页状态与边界：未完成账号验证禁用、账号冻结禁用、已达上限禁用、提交失败保留表单

## 3. 我的频道列表

- [x] 3.1 实现"我的频道"列表页 `/api/v1/content/channel/list`，使用 JVxeTable 展示频道列表
- [x] 3.2 实现列表列定义：频道图标、频道名称、频道类型 Tag、审核状态 Tag、创建时间（支持排序）、操作列
- [x] 3.3 实现筛选区：频道类型下拉筛选、审核状态下拉筛选
- [x] 3.4 实现操作列按状态区分：Active→"管理"、PendingReview→"查看详情"、Rejected→"重新提交"、DeleteCooling→"撤销删除"
- [x] 3.5 实现空状态：无频道时显示空状态插图和"创建你的第一个频道"引导按钮
- [x] 3.6 实现审核状态排序：PendingReview 和 Rejected 置顶

## 4. 频道管理与编辑

- [x] 4.1 实现频道管理页 `/api/v1/content/channel/manage/:id`，Tab 结构（概览/编辑信息/设置），顶部显示 ChannelStatusTag
- [x] 4.2 实现概览 Tab：频道基本信息摘要（名称、图标、类型、状态）+ 关键数据摘要占位
- [x] 4.3 实现编辑信息 Tab：Drawer 承载编辑表单（宽度 480px），ChannelForm 复用
- [x] 4.4 实现关键字段审核标识：浅黄色背景 + 橙色 Tag"修改需审核"，首次修改弹出一次性提示
- [x] 4.5 实现编辑保存逻辑：仅非关键字段→立即生效；包含关键字段→进入审核，提示"关键信息修改已提交审核"
- [x] 4.6 实现名称唯一性校验（编辑时排除当前频道自身）
- [x] 4.7 实现系统频道编辑：管理员编辑系统频道无"修改需审核"标识，修改直接生效
- [x] 4.8 实现审核期间编辑限制：弹窗提示"当前有审核中的修改，请审核完成后再编辑"
- [x] 4.9 实现关键字段修改被拒绝反馈：顶部通知条显示拒绝原因和"重新编辑"按钮
- [x] 4.10 实现 DeleteCooling 状态通知条：醒目通知条"频道正在删除冷静期中，剩余 X 天。[撤销删除]"

## 5. 频道转让

- [x] 5.1 实现 TransferConfirmModal 组件：第一步搜索目标用户，第二步确认转让信息
- [x] 5.2 实现用户搜索交互：输入 2 字符触发、300ms 防抖、下拉显示头像和昵称、排除当前用户、冻结用户置灰不可选
- [x] 5.3 实现转让二次确认弹窗：文案"确认将频道 [频道名] 转让给 [用户名]？转让后您将降为管理员，此操作不可撤销"
- [x] 5.4 实现组织频道转让规则：搜索范围限定同组织管理员、placeholder 改为"搜索组织内管理员"
- [x] 5.5 实现转让按钮状态：系统频道不显示、转让请求已存在时禁用
- [x] 5.6 实现转让历史记录展示：设置区域展示发起时间、目标用户、状态、结果

## 6. 频道删除

- [x] 6.1 实现 DeleteConfirmModal 组件：前置条件校验（loading→满足弹确认/不满足显示阻塞原因）、输入频道名确认
- [x] 6.2 实现删除前置条件校验：调用 delete-check 接口，返回阻塞原因列表时无"继续删除"按钮
- [x] 6.3 实现冷静期展示与撤销：通知条"剩余 X 天"、"撤销删除"弹窗确认后恢复 Active
- [x] 6.4 实现删除按钮状态：系统频道不显示、组织频道提示"需要组织最高管理员确认"

## 7. 后台频道管理

- [x] 7.1 实现后台频道管理页 `/api/v1/content/channel/admin`，JVxeTable 展示全量频道列表
- [x] 7.2 实现列表列定义：图标、名称、类型 Tag、状态 Tag、归属、分类、置顶权重、创建时间、操作
- [x] 7.3 实现筛选区：频道类型、审核状态、归属分类、创建时间范围、名称模糊搜索，筛选区可收起
- [x] 7.4 实现操作列：所有状态"查看详情"、系统频道"编辑"、DeleteCooling"强制删除"（二次确认）
- [x] 7.5 实现批量审核操作：选中多条 PendingReview 记录后批量通过/拒绝
- [x] 7.6 实现 ChannelDetailDrawer 组件：后台端查看频道详情

## 8. 审核队列

- [x] 8.1 实现审核队列页 `/api/v1/content/channel/review`，Table 展示 PendingReview 频道列表
- [x] 8.2 实现列表列定义：频道名称、类型、提交人、提交时间、等待时长（超 24h 标红）、操作
- [x] 8.3 实现待审核数量统计顶部展示和超时记录行高亮
- [x] 8.4 实现审核详情 Drawer（宽度 560px）：新建频道展示完整信息，编辑触发的展示 diff 对比
- [x] 8.5 实现 ReviewDiffViewer 组件：文本字段左右 diff（删除红色删除线、新增绿色背景）、图片并排展示、未修改字段折叠、"共修改 N 个字段"摘要
- [x] 8.6 实现审核操作面板（ReviewActionPanel）：通过（绿色，无需原因）、拒绝（红色，必填原因）、退回修改（橙色，必填建议）
- [x] 8.7 实现并发审核防护：后操作者提交时提示"该频道已被审核"
- [x] 8.8 实现空状态：无待审核频道时显示"暂无待审核频道"

## 9. 响应式适配与优化

- [x] 9.1 实现移动端频道列表：表格转卡片列表，操作使用 ActionSheet
- [x] 9.2 实现移动端创建/编辑：弹窗/抽屉转全屏页面，表单字段垂直堆叠
- [x] 9.3 实现移动端审核队列：表格转卡片，审核操作按钮固定底部
- [x] 9.4 实现移动端筛选区折叠：折叠为"筛选"按钮，点击展开筛选面板
- [x] 9.5 实现路由懒加载：频道相关页面使用动态 import
- [x] 9.6 实现图片上传前客户端压缩
