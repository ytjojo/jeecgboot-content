# Fix Plan - circle-11-content-interaction-frontend

**生成时间**: 2026-06-30
**Change**: circle-11-content-interaction-frontend (前端)
**配对后端Change**: circle-11-content-interaction

---

## 重要说明：模块边界澄清

根据drift-report和verify-report的审核结论，**channel和circle是两个独立模块**：
- **channel模块**: 频道功能，使用`/api/v1/content/channel/`前缀，对应`src/api/content/channel/`、`src/views/channel/`、`channelGovernanceStore`/`channelReviewStore`
- **circle模块**: 圈子功能，使用`/api/v1/content/circle/`前缀，对应`src/api/content/circle/`、`src/views/circle/`、`useCircleStore`

circle-11范围仅包含circle模块功能，channel模块功能不属于本epic。

---

## FixItem 列表

### FE-001 - 澄清design.md D6决策，修正模块混淆
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: design.md:74-88
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 重写D6决策，明确说明：
   - circle-11使用`useCircleStore`（已存在于`src/store/modules/circle.ts`）
   - `channelGovernanceStore`和`channelReviewStore`属于channel模块，不属于circle-11
   - 不新建`useCircleInteractionStore`，复用`useCircleStore`
2. 删除对channel Store的错误引用
3. 补充说明circle和channel是两个独立模块
**验证方式**: - design.md D6决策明确区分circle和channel模块 - 无错误的Store命名引用
**状态**: pending

---

### FE-002 - 修正design.md API路径，使用正确前缀和路径
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: design.md:36-40
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 修正D1决策中的API路径：
   - `/circle-content/{contentId}/pin` → `/api/v1/content/circle/content/{contentId}/pin?circleId={circleId}`
   - `/circle-content/{contentId}/featured` → `/api/v1/content/circle/content/{contentId}/featured?circleId={circleId}`
2. 明确说明`circleId`作为必需查询参数传递
3. 补充完整的API路径表，与后端实际实现对齐（参考后端BE-003的API列表）
4. 修正路径名错误：`circle-content` → `circle/content`
**验证方式**: - design.md中所有API路径使用`/api/v1/content/circle/`前缀 - 包含circleId必需参数说明 - 与后端API路径完全一致
**状态**: pending

---

### FE-003 - 创建缺失的circle-announcement/spec.md
**来源**: review-report.md:BLOCK-001, review-report-20260627-084036.md, verification-review.md
**位置**: specs/circle-announcement/spec.md
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 创建`specs/circle-announcement/`目录 2. 创建spec.md，包含以下Requirement：
   - 发布公告（版主/创建者可发布，替换旧公告）
   - 公告展示（圈子顶部展示，仅一条生效）
   - 公告有效期（过期自动隐藏）
   - 普通成员无发布权限
   - 公告编辑/删除（待后端BE-002实现后补充）
3. 参考后端circle-announcement/spec.md的Requirement
4. 补充完整的Scenario（WHEN/THEN格式）
**验证方式**: - specs/circle-announcement/spec.md存在 - 包含5个核心Requirement - Scenario覆盖成功/失败/权限场景
**状态**: pending

---

### FE-004 - 修正specs中举报处理API的HTTP方法和路径
**来源**: review-report.md:BLOCK-002, verification-review.md, review-report-20260627-084036.md
**位置**: specs/content-report/spec.md
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 将所有举报处理接口从PUT改为POST：
   - `PUT /circle-report/{id}/delete-content` → `POST /api/v1/content/circle/report/{reportId}/delete-content?circleId={circleId}`
   - `PUT /circle-report/{id}/ignore` → `POST /api/v1/content/circle/report/{reportId}/ignore?circleId={circleId}`
   - `PUT /circle-report/{id}/mute-user` → `POST /api/v1/content/circle/report/{reportId}/mute?circleId={circleId}`
2. 修正路径`/mute-user`为`/mute`
3. 补充circleId必需查询参数
**验证方式**: - 所有举报处理API使用POST方法 - 路径正确为/api/v1/content/circle/report/... - 包含circleId参数
**状态**: pending

---

### FE-005 - 修正specs中加入申请审核API的路径和方法
**来源**: review-report.md:BLOCK-003, verification-review.md, review-report-20260627-084036.md
**位置**: specs/join-request-review/spec.md
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 修正API路径和方法：
   - `PUT /circle-join-request/{id}/approve` → `POST /api/v1/content/circle/join-review/approve?circleId={circleId}`（body传requestId）
   - `PUT /circle-join-request/{id}/reject` → `POST /api/v1/content/circle/join-review/reject?circleId={circleId}`（body传requestId + rejectReason）
2. 修正路径前缀：`circle-join-request` → `circle-join-review`
3. 参数传递方式从path variable改为request body
4. 补充circleId必需查询参数
5. 明确加入申请审核功能当前复用channel模块实现，circle独立实现待后续补充
**验证方式**: - 加入审核API使用POST方法 - 路径正确为/api/v1/content/circle/join-review/... - 参数传递方式说明正确
**状态**: pending

---

### FE-006 - 为禁言时长参数定义明确的降级方案
**来源**: review-report.md:BLOCK-004, backend-issues.md:BE-04, design.md:95-96
**位置**: specs/content-report/spec.md, design.md Risks节
**优先级**: CRITICAL
**依赖**: BE-013（后端禁言实现）
**类型**: 文档修复
**修复步骤**: 1. 在spec.md的禁言Scenario中明确标注降级方案：
   - 前端禁言弹窗仍展示时长选择UI（1小时/1天/7天/30天/永久）
   - 当前提交API时不传duration参数（后端忽略），默认使用后端固定时长
   - 待后端BE-013实现duration参数支持后，再对接传递
2. 在design.md R6风险中更新状态为"已确定降级方案"
3. 在backend-issues.md中保持BE-04记录
**验证方式**: - spec.md明确说明降级方案 - design.md R6风险状态更新 - 前端实现与降级方案一致
**状态**: pending

---

### FE-007 - 修正所有spec中API缺少circleId参数的问题
**来源**: review-report.md:FLAG-006/FLAG-015, verification-review.md
**位置**: specs/content-pin-featured/spec.md, specs/content-report/spec.md
**优先级**: HIGH
**依赖**: FE-004, FE-005
**类型**: 文档修复
**修复步骤**: 1. 在content-pin-featured/spec.md的置顶/精华Scenario中补充circleId参数说明 2. 确认content-report/spec.md中所有API都已包含circleId（在FE-004中完成） 3. 统一参数说明格式
**验证方式**: - 所有需要circleId的API都在Scenario中明确说明参数传递
**状态**: pending

---

### FE-008 - 修正公告查询路径前后端不一致
**来源**: review-report.md:FLAG-009, verification-review.md
**位置**: specs/circle-announcement/spec.md, PRD 5.2节
**优先级**: HIGH
**依赖**: FE-003
**类型**: 文档修复
**修复步骤**: 1. 将公告查询路径从`GET /circle-announcement/current?circleId={id}`统一为`GET /api/v1/content/circle/announcement/active/{circleId}` 2. 参数传递方式从query改为path variable 3. 在新建的circle-announcement spec中使用正确路径
**验证方式**: - 公告查询路径与后端实现一致（/active/{circleId}）
**状态**: pending

---

### FE-009 - 更新design.md R1风险状态（circle-core已完成）
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: design.md:92
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 更新R1风险描述，说明circle-core前端已完成 2. 移除该风险或标记为已解决
**验证方式**: - design.md R1风险状态准确反映当前实际情况
**状态**: pending

---

### FE-010 - 补充design.md Test Strategy章节
**来源**: review-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: design.md新增章节
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 参考circle-10格式补充Test Strategy章节 2. 说明测试框架（Vitest + Vue Test Utils） 3. 列出关键组件的测试覆盖要求：
   - CircleAnnouncementBar: 4 tests（已实现）
   - MentionMemberPicker: 12 tests（已实现）
   - CircleContentActionMenu: 5 tests（已实现）
   - ReportList/ReportCard/ReportDetailDrawer: 测试已实现
4. 在tasks.md中补充测试任务项（与实际已实现的测试对应）
**验证方式**: - design.md包含Test Strategy章节 - 说明测试覆盖范围和已实现的测试
**状态**: pending

---

### FE-011 - 补充design.md路由方案决策
**来源**: review-report.md:FLAG-001, drift-report-20260627-084036.md
**位置**: design.md Decisions节
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 在D0路由方案决策中补充：
   - circle模块当前路由已在src/router/routes/modules/circle.ts中配置
   - 加入申请审核和举报处理页面当前复用channel模块实现（路径：/channel/governance/...）
   - circle独立路由（/circle/:id/join-requests、/circle/:id/reports）待后续补充
2. 说明当前复用channel组件的架构决策原因
**验证方式**: - design.md明确说明路由现状和后续计划
**状态**: pending

---

### FE-012 - 更新proposal.md，修正Store命名和API数量
**来源**: review-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: proposal.md:31-40
**优先级**: LOW
**依赖**: FE-001
**类型**: 文档修复
**修复步骤**: 1. 将"新增Store: useCircleInteractionStore"改为"复用useCircleStore" 2. 更新API对接数量，区分circle已实现API和channel模块API 3. 明确说明channel模块不属于本epic范围
**验证方式**: - proposal.md中Store命名与实际一致 - API数量和范围描述准确
**状态**: pending

---

### FE-013 - 修复announcement.ts路径双斜杠问题
**来源**: verify-report-20260627-084036.md:P3
**位置**: src/api/content/circle/announcement.ts:15
**优先级**: LOW
**依赖**: 无
**类型**: 代码修复-前端
**修复步骤**: 1. 将`${Api.publish}/`改为`${Api.publish}`，去掉末尾多余斜杠 2. 避免请求URL出现双斜杠`//`
**验证方式**: - API请求URL无双斜杠 - 公告发布功能正常
**状态**: pending

---

### FE-014 - 补充specs中loading/error状态场景
**来源**: review-report.md:FLAG-006/007/008
**位置**: 多个spec.md文件
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 在content-pin-featured/spec.md中补充：
   - WHEN 置顶API调用中 THEN 按钮显示loading
   - WHEN API调用失败 THEN Toast提示"操作失败"
2. 在mention-member/spec.md中补充：
   - WHEN 成员列表加载失败 THEN 显示重试按钮
3. 在join-request-review/spec.md中补充：
   - WHEN 批量批准部分失败 THEN 已成功的卡片移出列表，失败的保留并提示
**验证方式**: - 各spec包含loading和error状态的Scenario
**状态**: pending

---

### FE-015 - 明确support/report模块为共享模块
**来源**: drift-report-20260627-084036.md
**位置**: design.md新增说明
**优先级**: LOW
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 在design.md中补充说明：举报组件位于`src/views/support/report/`为跨模块共享组件，circle和channel模块都可使用 2. 明确依赖关系
**验证方式**: - design.md说明support/report的共享定位
**状态**: pending

---

### FE-016 - 标注后端遗留API的前端降级方案
**来源**: backend-issues.md, verification-review.md
**位置**: 各spec.md和design.md
**优先级**: HIGH
**依赖**: BE-012, BE-002
**类型**: 文档修复
**修复步骤**: 1. 在mention-member/spec.md中标注：
   - `GET /mentionable-members`端点待后端BE-012实现
   - 降级方案：当前复用`GET /api/v1/content/circle/member/list`接口获取成员列表
2. 在circle-announcement/spec.md中标注：
   - `DELETE /announcement/{id}`端点待后端BE-002实现
   - 降级方案：当前不实现删除功能，仅支持发布新公告覆盖
3. 在join-request-review/spec.md中标注：
   - `pendingJoinRequestCount`接口待后端实现
   - 降级方案：当前管理入口角标暂不展示，待接口就绪后补充
**验证方式**: - 各spec明确标注后端遗留API和对应的降级方案
**状态**: pending

---

## 执行顺序建议

1. **第一阶段（文档修复-阻断项）**: FE-001, FE-002, FE-003, FE-004, FE-005, FE-006
2. **第二阶段（文档修复-高优先级）**: FE-007, FE-008, FE-016
3. **第三阶段（文档修复-中/低优先级）**: FE-009, FE-010, FE-011, FE-012, FE-014, FE-015
4. **第四阶段（代码修复-前端小问题）**: FE-013

---

## 依赖项汇总

| 依赖项 | 影响FixItem | 状态 |
|--------|------------|------|
| 后端BE-002（公告删除API） | FE-008, FE-016 | 待实现 |
| 后端BE-012（mentionable-members端点） | FE-016 | 待实现 |
| 后端BE-013（禁言时长参数） | FE-006 | 待实现 |
| 后端pendingJoinRequestCount接口 | FE-016 | 待实现 |
| circle独立审核页面实现 | FE-011 | 待后续规划 |

---

## 已验证通过的功能（无需修复）

根据verify-report和code-review结果，以下功能已正确实现：
1. ✅ circle目录下所有API使用正确的`/api/v1/content/circle/`前缀
2. ✅ HTTP方法规范（PUT/POST/DELETE/GET使用正确）
3. ✅ 置顶/精华API和组件实现（CircleContentActionMenu, CircleContentCard）
4. ✅ 圈子公告API和组件实现（CircleAnnouncementBar, CircleAnnouncementManage）
5. ✅ @成员选择器组件和useMention逻辑
6. ✅ 内容举报API（删除/忽略/禁言）路径正确
7. ✅ Tinymce富文本编辑器按需加载
8. ✅ 68个单元测试全部通过
