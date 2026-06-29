# Fix Plan - circle-11-content-interaction

**生成时间**: 2026-06-30
**Change**: circle-11-content-interaction (后端)
**配对前端Change**: circle-11-content-interaction-frontend

---

## FixItem 列表

### BE-001 - 更新tasks.md任务状态为已完成
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: tasks.md:1-64
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 遍历tasks.md中所有38个任务checkbox 2. 根据verify.md验证结果，将已实现的任务标记为`[x]` 3. 标注W1/W3为待circle-core完成后补充
**验证方式**: - tasks.md中已完成任务标记为[x] - 待跟进项标注为[~]并说明原因
**状态**: pending

---

### BE-002 - 修正design.md包路径和File Structure
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: design.md:77-154
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 将包路径从`content/user/`修正为`content/circle/` 2. 补充缺失的CircleMentionController/CircleMentionService/Biz相关文件 3. 补充CircleContentController到File Structure 4. 验证文件结构与实际代码一致
**验证方式**: - File Structure中列出的文件路径与实际代码路径完全一致 - 包含所有Controller/Service/Biz/Mapper/Entity文件
**状态**: pending

---

### BE-003 - 更新design.md API路径与实际实现对齐
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: design.md:60-67
**优先级**: CRITICAL
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 新增完整的API Endpoints章节 2. 列出所有Controller的实际路径：
   - 置顶: `PUT /api/v1/content/circle/content/{contentId}/pin?circleId={circleId}`
   - 精华: `PUT /api/v1/content/circle/content/{contentId}/featured?circleId={circleId}`
   - 公告发布: `POST /api/v1/content/circle/announcement/`
   - 公告删除: `DELETE /api/v1/content/circle/announcement/{id}`
   - 生效公告: `GET /api/v1/content/circle/announcement/active/{circleId}`
   - 历史公告: `GET /api/v1/content/circle/announcement/history/{circleId}`
   - 待审核列表: `GET /api/v1/content/circle/join-review/pending/{circleId}`
   - 批准申请: `POST /api/v1/content/circle/join-review/approve?circleId={circleId}`
   - 拒绝申请: `POST /api/v1/content/circle/join-review/reject?circleId={circleId}`
   - 提交举报: `POST /api/v1/content/circle/report/`
   - 举报列表: `GET /api/v1/content/circle/report/list/{circleId}?status={status}`
   - 删除举报内容: `POST /api/v1/content/circle/report/{reportId}/delete-content?circleId={circleId}`
   - 忽略举报: `POST /api/v1/content/circle/report/{reportId}/ignore?circleId={circleId}`
   - 禁言用户: `POST /api/v1/content/circle/report/{reportId}/mute?circleId={circleId}`
3. 标注举报处理从单一handle拆分为3个接口是正向改进
**验证方式**: - API路径列表与verify-report.md中验证的实际路径完全一致 - 包含HTTP方法和必需参数说明
**状态**: pending

---

### BE-004 - 关闭design.md Open Questions或指定跟进计划
**来源**: review-report-20260627-084036.md
**位置**: design.md:178-182
**优先级**: HIGH
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. circle_content表字段确认：已通过circle-core实现，标注为已解决 2. 通知系统升级：标注为后续迭代任务，当前使用stub可接受 3. 超时提醒渠道：当前仅前端视觉提示，站内信通知后续实现，标注决策
**验证方式**: - 每个Open Question都有明确的状态和后续计划 - 无遗留未关闭的问题
**状态**: pending

---

### BE-005 - 更新design.md Flyway版本号
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: design.md:139
**优先级**: HIGH
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 将Flyway版本号从V3.9.1_60修正为V3.9.1_63 2. 验证迁移脚本文件名一致
**验证方式**: - design.md中版本号与实际V3.9.1_63__circle_content_interaction.sql一致
**状态**: pending

---

### BE-006 - 更新design.md Context章节状态描述
**来源**: review-report-20260627-084036.md
**位置**: design.md:1-8
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 修改"circle-core的PRD已完成但代码尚未构建"为"circle-core已实现基础功能" 2. 更新Context描述为当前实际状态
**验证方式**: - Context描述与项目实际状态一致
**状态**: pending

---

### BE-007 - 补充design.md @提及功能API设计
**来源**: review-report-20260627-084036.md
**位置**: design.md:60-67
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 补充CircleMention相关API设计 2. 说明@提及解析作为内容发布的内部服务 3. 说明mentionable-members端点待补充Controller（见BE-013）
**验证方式**: - API章节包含@提及相关接口说明 - 明确提及解析的调用时机
**状态**: pending

---

### BE-008 - 补充design.md错误码和权限矩阵
**来源**: review-report-20260627-084036.md
**位置**: design.md全文档
**优先级**: MEDIUM
**依赖**: 无
**类型**: 文档修复
**修复步骤**: 1. 参考circle-10格式补充错误码表 2. 补充权限矩阵（哪些角色可以执行哪些操作） 3. 明确普通成员/版主/创建者的权限边界
**验证方式**: - design.md包含完整的错误处理矩阵 - 包含角色-操作权限对照表
**状态**: pending

---

### BE-009 - 统一认证方式为SecureUtil
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: 多个Controller文件
**优先级**: MEDIUM
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**: 1. 扫描所有Controller，将JwtUtil.getUserNameByToken(request)替换为SecureUtil.currentUser().getId() 2. 保持认证方式统一 3. 验证匿名访问接口不会抛出NPE
**验证方式**: - 所有Controller统一使用SecureUtil - 单元测试全部通过
**状态**: pending

---

### BE-010 - 统一分页参数命名为pageNum/pageSize
**来源**: review-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: CircleJoinReviewController.java:44-45
**优先级**: LOW
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**: 1. 将CircleJoinReviewController中的current/size参数改为pageNum/pageSize 2. 与其他Controller保持一致 3. 更新相关Service和Mapper
**验证方式**: - 所有分页接口统一使用pageNum/pageSize参数 - 前端调用无兼容性问题
**状态**: pending

---

### BE-011 - 将Controller层权限校验下沉到Biz层
**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: CircleReportController.java:60-63
**优先级**: MEDIUM
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**: 1. 将CircleReportController中的权限判断逻辑（member==null或role==MEMBER）移至CircleReportBizService 2. 参考CircleContentPinBizService的checkManagePermission模式 3. 补充单元测试验证权限校验 4. code-review-2026067.md显示此问题已部分修复，需验证
**验证方式**: - Controller层无直接权限判断逻辑 - Biz层统一处理权限校验 - 单元测试覆盖权限场景
**状态**: pending

---

### BE-012 - 补充mentionable-members Controller端点
**来源**: backend-issues.md, verification-review.md, review-report.md
**位置**: 新增Controller端点
**优先级**: CRITICAL
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**: 1. 在CircleContentController或新增CircleMentionController中添加GET端点 2. 路径: `GET /api/v1/content/circle/{circleId}/mentionable-members?keyword={keyword}` 3. 调用ICircleMentionService.getMentionCandidates() 4. 返回MentionMemberVO列表 5. 补充单元测试和WebMvcTest
**验证方式**: - 端点可正常访问 - 返回当前圈子可提及成员列表 - 支持keyword搜索参数
**状态**: pending

---

### BE-013 - 实现禁言功能handleMute（待circle-core禁言服务就绪）
**来源**: verify.md:W1, backend-issues.md:BE-04
**位置**: CircleReportServiceImpl.java:75
**优先级**: HIGH
**依赖**: circle-core禁言服务
**类型**: 代码修复-后端
**修复步骤**: 1. 等待circle-core禁言服务就绪 2. 实现handleMute方法，调用circleMemberService.muteMember() 3. 支持禁言时长参数（duration） 4. 补充禁言通知调用（verify.md显示W2已修复） 5. 补充单元测试
**验证方式**: - 禁言操作实际生效 - 禁言时长参数正确传递 - 通知正确发送 - 单元测试覆盖
**状态**: pending

---

### BE-014 - 实现批准加入创建圈员记录（待circle-core成员服务就绪）
**来源**: verify.md:W3
**位置**: CircleJoinReviewServiceImpl.approve()
**优先级**: HIGH
**依赖**: circle-core成员服务
**类型**: 代码修复-后端
**修复步骤**: 1. 等待circle-core成员服务就绪 2. 在approve()方法中添加circleMemberMapper.insert()调用 3. 创建圈员记录，角色为MEMBER 4. 补充单元测试
**验证方式**: - 批准申请后用户成功加入圈子 - circle_member表有对应记录 - 单元测试覆盖
**状态**: pending

---

### BE-015 - 修正公告发布路径末尾斜杠
**来源**: verify-report-20260627-084036.md
**位置**: CircleAnnouncementController.java:37
**优先级**: LOW
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**: 1. 将@PostMapping("/")改为@PostMapping("")或@PostMapping 2. 保持RESTful路径风格一致
**验证方式**: - 路径无多余斜杠 - API正常工作
**状态**: pending

---

### BE-016 - 验证Service层circleId归属校验
**来源**: drift-report-20260627-084036.md:S-11-003
**位置**: 多个Service/Biz
**优先级**: MEDIUM
**依赖**: 无
**类型**: 代码修复-后端
**修复步骤**: 1. 验证所有接收circleId参数的Service方法都有校验操作者是否为该圈子管理员 2. 防止篡改circleId操作其他圈子内容 3. 补充权限校验测试用例
**验证方式**: - 跨圈子操作被正确拒绝 - 单元测试覆盖越权场景
**状态**: pending

---

## 执行顺序建议

1. **第一阶段（文档修复-阻断项）**: BE-001, BE-002, BE-003
2. **第二阶段（文档修复-高优先级）**: BE-004, BE-005, BE-006, BE-007, BE-008
3. **第三阶段（代码修复-后端）**: BE-009, BE-011, BE-012, BE-015, BE-016
4. **第四阶段（待依赖）**: BE-010, BE-013, BE-014

---

## 依赖项汇总

| 依赖项 | 影响FixItem | 状态 |
|--------|------------|------|
| circle-core禁言服务 | BE-013 | 待实现 |
| circle-core成员服务 | BE-014 | 待实现 |
