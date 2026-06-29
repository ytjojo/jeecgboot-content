# 修复计划 — channel-20-infrastructure-frontend

**生成时间**: 2026-06-30
**审核文档数**: 5 (review-report/drift-report/verify-report/review-report/verification-review/backend-issues，排除已标记解决的文档问题)
**总问题数**: 23

---

## 修复项

### FE-001 - confirmTransfer/rejectTransfer API路径拼接错误

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: src/api/content/channel/index.ts:76,80
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 修改confirmTransfer方法签名，添加channelId参数
2. 修改路径拼接逻辑：从`Api.detail + 'transfer/' + transferId + '/confirm'`改为`Api.detail + channelId + '/transfer/' + transferId + '/confirm'`
3. 同样修改rejectTransfer方法，添加channelId参数，修正路径拼接
4. 更新所有调用这两个方法的地方，传入正确的channelId
5. 注意：API前缀统一使用/api/v1/content/channel/（根据任务说明）

**验证方式**:
- 转让确认/拒绝接口能正常调用，不返回404
- 路径格式正确：/api/v1/content/channels/{id}/transfer/{transferId}/confirm
- 转让流程端到端测试通过

**状态**: pending

---

### FE-002 - 缺少getAdminChannelList后台列表API方法

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: src/api/content/channel/index.ts
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在API封装文件中新增getAdminChannelList方法
2. 该方法调用后台专用API：GET /api/v1/content/admin/channels/list
3. 支持分页参数（current、size）和筛选参数（channelType、status、keyword、createTimeRange）
4. 修改admin/index.vue页面，将错误调用getChannelList改为调用getAdminChannelList
5. 确保后台API路径包含/admin/前缀

**验证方式**:
- 后台管理页面能正常获取全量频道列表
- 筛选条件（类型、状态、关键词）能正确传递给后端
- 分页功能正常工作

**状态**: pending

---

### FE-003 - 缺少getReviewDetail审核详情API方法

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: src/api/content/channel/index.ts
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在API封装文件中新增getReviewDetail方法
2. 该方法调用：GET /api/v1/content/channel/review/detail/{id}（或后端提供的审核详情接口路径）
3. 返回审核详情数据，包含修改前后的diff信息
4. 在审核队列页面中，点击查看详情时调用该接口
5. 确认后端对应接口路径是否正确

**验证方式**:
- 审核详情Drawer能正常加载数据
- 能获取到审核记录的完整信息和diff数据
- 接口路径正确，不返回404

**状态**: pending

---

### FE-004 - ReviewDiffViewer组件完全缺失

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/components/（应在此目录）
**优先级**: BLOCK
**依赖**: FE-003
**类型**: 代码修复-前端

**修复步骤**:
1. 创建ReviewDiffViewer.vue组件，放在src/views/content/channel/components/目录下
2. 实现文本diff对比功能：
   - 支持名称、简介等文本字段的diff展示
   - 支持图标、封面等图片的对比展示
   - 可以使用jsdiff等轻量库或自己实现简单的行级diff
3. 组件props：oldData（修改前数据）、newData（修改后数据）
4. 使用绿色/红色高亮显示新增/删除/修改的内容
5. 在审核详情Drawer中引入并使用该组件
6. 在design.md中补充diff对比方案说明

**验证方式**:
- 审核时能看到频道信息修改前后的对比
- 文本修改有高亮标记
- 图片能展示前后对比
- 组件在审核详情页正常渲染

**状态**: pending

---

### FE-005 - TransferConfirmModal用户搜索是空实现

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/manage/TransferConfirmModal.vue:78-80
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 实现handleSearch用户搜索方法
2. 复用项目已有的用户搜索API（参考circle模块或用户模块的用户选择实现）
3. 添加搜索防抖（300ms）
4. userOptions从API返回结果赋值，而非空数组
5. 支持按用户名/昵称搜索
6. 组织频道转让时，搜索范围限定为组织管理员
7. 选中用户后正确设置toUserId

**验证方式**:
- 转让时能搜索到目标用户
- 搜索结果正确展示用户昵称和头像
- 选中用户后能正常发起转让
- 组织频道只能搜索到组织内管理员

**状态**: pending

---

### FE-006 - ChannelForm图片上传是模拟实现

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/components/jeecg/channel/ChannelForm.vue:172-184
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 对接项目真实的上传接口：src/api/sys/upload.ts或JUpload组件
2. 移除URL.createObjectURL本地blob URL的模拟实现
3. 实现图片上传前的压缩和格式校验（支持jpg/png/webp，大小<=2MB图标/5MB封面）
4. 上传成功后获取服务器返回的真实URL，赋值给iconUrl/coverUrl字段
5. 添加上传进度展示和上传失败重试
6. 复用项目已有的Upload组件，避免重复实现

**验证方式**:
- 选择图片后能正常上传到服务器
- 表单提交时iconUrl/coverUrl是服务器URL而非本地blob URL
- 上传失败有错误提示
- 图片大小和格式校验生效

**状态**: pending

---

### FE-007 - 缺少"退回修改"审核操作

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/review/index.vue
**优先级**: BLOCK
**依赖**: FE-003
**类型**: 代码修复-前端

**修复步骤**:
1. 在审核操作面板中添加"退回修改"按钮
2. 点击退回修改时，弹出Modal要求填写修改建议（note字段必填）
3. 调用审核接口时，action类型为RETURN（区别于APPROVE/REJECT）
4. 补充ReviewActionType枚举定义：APPROVE=通过，REJECT=拒绝，RETURN=退回修改
5. 批量审核时也支持退回操作
6. 在specs中补充退回修改的Scenario

**验证方式**:
- 审核队列页面有"退回修改"按钮
- 点击后弹出填写修改建议的弹窗
- 提交后退回到PendingReview或对应状态
- 频道主能看到退回原因

**状态**: pending

---

### FE-008 - 缺少重复提交防护

**来源**: review-report-20260627-084036.md
**位置**: 所有提交操作按钮
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 使用项目的useLockFn hook包裹所有提交函数
2. 或者在submitting状态时禁用按钮并显示loading
3. 覆盖以下关键操作：
   - 创建频道提交
   - 编辑保存
   - 发起转让
   - 确认/拒绝转让
   - 删除确认/撤销删除
   - 审核操作（通过/拒绝/退回）
4. 提交成功或失败后解除锁定

**验证方式**:
- 快速双击提交按钮不会发送重复请求
- 提交过程中按钮显示loading且禁用
- 请求完成后按钮恢复可点击状态

**状态**: pending

---

### FE-009 - 缺少表单中途离开未保存提示

**来源**: review-report-20260627-084036.md
**位置**: src/views/content/channel/create/, src/views/content/channel/manage/（编辑表单）
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在创建和编辑表单页面添加路由离开守卫
2. 监听beforeunload事件，在表单有未保存修改时提示用户
3. 使用hasUnsavedChanges状态变量跟踪表单是否被修改
4. 离开页面（包括路由跳转、关闭标签页）时弹出确认提示："您有未保存的修改，确定要离开吗？"
5. 表单提交成功后重置hasUnsavedChanges状态

**验证方式**:
- 表单有修改时点击浏览器返回/关闭标签页会弹出提示
- 路由跳转到其他页面会弹出确认
- 提交成功后离开不会提示

**状态**: pending

---

### FE-010 - 缺少网络超时/断网UI反馈

**来源**: review-report-20260627-084036.md
**位置**: 所有API请求处
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 封装统一的请求错误处理，在defHttp拦截器中处理超时和网络错误
2. 网络超时时显示明确提示："网络请求超时，请检查网络后重试"
3. 断网时显示全局提示或页面级提示
4. 关键页面添加重试按钮
5. 在design.md中补充网络异常处理策略
6. 按钮loading状态在请求失败时也要正确解除

**验证方式**:
- 模拟网络超时，显示友好的错误提示
- 断网状态下有明确的用户反馈
- 错误提示不是笼统的"加载失败"，而是具体原因

**状态**: pending

---

### FE-011 - Token过期场景处理说明缺失

**来源**: review-report-20260627-084036.md
**位置**: design.md
**优先级**: BLOCK
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 在design.md中补充Token过期处理策略
2. 说明项目全局拦截器已处理Token自动刷新和跳转登录
3. 表单页面如果Token过期，跳转登录后是否保留表单数据（根据项目现有机制）
4. 关键操作前检查登录状态，避免提交时才发现Token过期
5. 若项目已有全局处理，文档中说明"复用项目现有Token刷新机制"即可

**验证方式**:
- design.md中有Token过期处理说明
- 与项目全局认证机制一致

**状态**: pending

---

### FE-012 - coolingDays硬编码为7

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/manage/index.vue:168
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 移除coolingDays的硬编码return 7
2. 从channel数据中获取deleteCoolingEndTime字段
3. 根据当前时间和deleteCoolingEndTime动态计算剩余天数
4. 正确处理：已到期、剩余N天、剩余N小时等情况
5. 冷静期结束后更新UI状态

**验证方式**:
- 冷静期剩余天数根据实际时间动态计算
- 不是固定显示7天
- 到期后状态正确更新

**状态**: pending

---

### FE-013 - 转让历史记录key错误

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/manage/index.vue:91
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 将v-for循环中的:key="item.id"改为:key="item.transferId"
2. 确保ChannelTransferVO中使用transferId作为主键字段
3. 检查转让历史列表中其他字段引用是否正确（fromUserName/toUserName/status等）

**验证方式**:
- 转让历史列表key不重复、不报错
- 每条记录正确渲染
- 控制台无key相关警告

**状态**: pending

---

### FE-014 - 编辑表单未使用Drawer承载

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/manage/index.vue
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 将编辑表单从Tab pane内直接展示改为Drawer承载
2. Drawer宽度设置为480px（按spec要求）
3. 点击"编辑"按钮打开Drawer，关闭/保存后关闭Drawer
4. Drawer内放置ChannelForm组件
5. 编辑表单数据加载和保存逻辑保持不变
6. 如果不使用Drawer，需更新spec文档说明原因（建议遵循spec决策）

**验证方式**:
- 点击编辑按钮弹出480px宽的Drawer
- Drawer内是频道编辑表单
- 保存/取消后关闭Drawer
- 编辑体验符合spec要求

**状态**: pending

---

### FE-015 - 列表使用a-table而非JVxeTable

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/list/index.vue, admin/index.vue, review/index.vue
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 将频道列表页的a-table替换为项目统一的JVxeTable或useTable hook
2. 复用项目现有的列表页封装（useListPage等）
3. 保持现有列定义和筛选功能
4. 确保分页、排序、筛选功能正常
5. 如果项目已统一使用某种表格组件，遵循项目现有规范（如确认JVxeTable是项目标准）

**验证方式**:
- 列表使用与其他模块一致的表格组件
- 分页、筛选功能正常
- 风格与项目其他列表页统一

**状态**: pending

---

### FE-016 - 关键字段审核标识缺失

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/components/jeecg/channel/ChannelForm.vue
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在ChannelForm中，为关键字段（名称、简介、图标、封面、分类）添加浅黄色背景标识
2. 在关键字段标签旁添加橙色"需审核"Tag
3. 非关键字段保持普通样式
4. 在表单顶部添加提示："标记为橙色的字段修改后需重新审核"
5. 区分创建时（所有字段都审核）和编辑时（仅关键字段审核）的展示

**验证方式**:
- 关键字段有浅黄色背景
- 字段旁有"需审核"橙色Tag
- 用户能清楚区分哪些字段修改需要审核

**状态**: pending

---

### FE-017 - 名称唯一性校验未实现

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/components/jeecg/channel/ChannelForm.vue, src/views/content/channel/create/ChannelCreateSteps.vue
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在频道名称输入框添加失焦校验事件
2. 使用300ms防抖调用check-name接口
3. 编辑时传递excludeId参数排除当前频道自身
4. 名称已存在时显示红色错误提示："该频道名称已被使用"
5. 名称可用时显示绿色提示或不提示
6. 提交前再次校验，避免防抖期间的并发问题

**验证方式**:
- 输入名称后失焦自动校验唯一性
- 重名时实时显示错误提示
- 编辑时排除当前频道
- 有防抖不会频繁发请求

**状态**: pending

---

### FE-018 - 审核列表缺少等待时长列和超时高亮

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/review/index.vue
**优先级**: FLAG
**依赖**: FE-003
**类型**: 代码修复-前端

**修复步骤**:
1. 在审核队列表格中添加"等待时长"列
2. 根据submitTime计算等待时长：X小时/X分钟
3. 超过24小时（或配置的超时阈值）的行添加高亮样式（如浅黄色/浅红色背景）
4. 明确是后端返回waitDuration还是前端计算（建议后端返回waitDuration或isTimeout字段）
5. 可按等待时长排序

**验证方式**:
- 审核列表有"等待时长"列
- 等待超过24小时的审核项有高亮标记
- 管理员能快速识别超时审核

**状态**: pending

---

### FE-019 - 缺少顶部待审核数量统计

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/review/index.vue
**优先级**: FLAG
**依赖**: FE-002
**类型**: 代码修复-前端

**修复步骤**:
1. 在审核队列页面顶部添加统计卡片区域
2. 展示待审核总数、今日新增、超时未处理等统计数据
3. 从后台列表接口或专门的统计接口获取数据
4. 统计数据可点击跳转到对应筛选状态

**验证方式**:
- 审核队列页面顶部有待审核数量统计
- 数据与实际列表数量一致
- 点击统计项可筛选对应状态

**状态**: pending

---

### FE-020 - 批量审核未筛选PendingReview状态

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/admin/index.vue:214-250
**优先级**: FLAG
**依赖**: FE-007
**类型**: 代码修复-前端

**修复步骤**:
1. 执行批量审核前，先筛选出选中项中状态为PendingReview的ID
2. 非PendingReview状态的频道跳过审核，或提示用户
3. 考虑使用Promise.all并发请求（注意后端限流），或保持串行但显示进度
4. 批量操作完成后显示结果：成功X条，跳过X条，失败X条
5. 批量操作前添加确认提示

**验证方式**:
- 批量审核只处理待审核状态的频道
- 不会对已审核频道重复执行审核
- 批量完成后显示操作结果统计

**状态**: pending

---

### FE-021 - rejectReason硬编码为空

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: src/views/content/channel/manage/index.vue:26
**优先级**: FLAG
**依赖**: BE-006（后端返回字段）
**类型**: 代码修复-前端

**修复步骤**:
1. 从channel详情接口或审核记录接口获取rejectReason字段
2. 移除硬编码的空字符串
3. 在拒绝通知条中显示真实的拒绝原因
4. 没有拒绝原因时不显示该区域

**验证方式**:
- 被拒绝的频道能看到具体拒绝原因
- rejectReason从接口获取而非硬编码
- 无拒绝原因时通知条正常显示通用提示

**状态**: pending

---

### FE-022 - design.md中路由路径与前端实际路由不符

**来源**: review-report-20260627-084036.md, review-report.md
**位置**: design.md:29-33, tasks.md:1.7-1.8
**优先级**: FLAG
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 修正design.md中的路由路径定义：
   - 前端页面路由使用/content/channel/*（不含/api/v1前缀，这是页面路由不是API路由）
   - API路由才使用/api/v1/content/channel/前缀
2. 明确区分：
   - 前端页面路由（Vue Router）：/content/channel/create, /content/channel/list等
   - 后端API路径：/api/v1/content/channels/*等
3. 更新tasks.md中对应的路由路径描述
4. 统一文档中API路径前缀，都使用/api/v1/content/channel/开头（单数channel？需确认后端是/channels/还是/channel/，根据任务说明用/api/v1/content/channel/）

**验证方式**:
- design.md中页面路由和API路由区分清晰
- 路径描述与实际实现一致
- 前端跳转不会出现404

**状态**: pending

---

### FE-023 - 前端状态枚举与后端对齐

**来源**: review-report-20260627-084036.md
**位置**: src/api/content/channel/model/channelModel.ts:5,20-29
**优先级**: FLAG
**依赖**: BE-012
**类型**: 文档修复+代码检查

**修复步骤**:
1. 与后端确认ChannelStatus枚举的序列化格式：返回数字还是字符串
2. 根据后端实际返回调整前端ChannelStatus枚举定义
3. 核对ChannelVO所有字段与后端VO一一对应：
   - categoryId/categoryName
   - orgId/organizationId
   - rejectReason
   - waitDuration（审核列表）
   - transferId（转让记录）
4. 转让状态枚举（PENDING/ACCEPTED/REJECTED/EXPIRED）与后端对齐
5. 在design.md中补充字段对照表

**验证方式**:
- 前端状态值与后端返回一致
- 状态判断逻辑正确
- 无字段缺失或类型错误

**状态**: pending
