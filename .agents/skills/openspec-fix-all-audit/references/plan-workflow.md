# `plan` 子命令详细流程

> 从 `SKILL.md` 第二层引用。主 agent 执行 plan 子命令时读取。

---

## 步骤 1：解析输入，确定 ChangePair 列表

### 1.1 确定目标 change 列表

- **单 change 模式**：用户指定具体 change 目录（如 `openspec/changes/user-03-badges-points-growth/`）→ 目标为这一个 change
- **批量模式**：用户指定 changes 根目录（如 `openspec/changes/`）→ 扫描所有 change 子目录

### 1.2 过滤 change

读取 `docs/requirements/prd/decomposition/change-prd-mapping.yaml`，过滤掉：
- `status: archived` 的 change
- 审核文档中所有 Issue 均标记为 done/completed 的 change

### 1.3 前后端成对配对

在 `change-prd-mapping.yaml` 中查找配对：
- 后端 `{name}` → 前端配对 `{name}_frontend`
- 前端 `{name}_frontend` → 后端配对去掉 `_frontend` 后缀
- 无配对时单独成为一个 ChangePair
- **单 change 模式下**：自动查找配对（扫描 proposal/design + mapping 文件）

每个 ChangePair 包含：
- `backend_change`: 后端 change 目录名（可能为 null）
- `frontend_change`: 前端 change 目录名（可能为 null）
- `prd_path`: 对应的 PRD 文档路径
- `frontend_prd_path`: 对应的前端 PRD 文档路径（如有）

---

## 步骤 2：并行 dispatch subagent 分析

为每个 ChangePair 启动一个 **只读 subagent**，并行执行。

### 每个 subagent 的输入

- ChangePair 的完整信息（后端 change 路径、前端 change 路径、PRD 路径）
- 该 pair 下所有审核文档的文件列表

### 每个 subagent 的工作流程

1. 扫描 change 目录下的 6 类审核文档：
   - `review-report-*.md`
   - `drift-report-*.md`
   - `verify-report-*.md`
   - `verify.md`
   - `verification-review.md`
   - `backend-issues.md`

2. 过滤全部已标记完成的审核文档（所有 Issue 均为 done/completed 的跳过）

3. 读取规范文档（proposal.md、design.md、specs/、tasks.md）和 PRD 作为上下文

4. 解析 Issue（大模型自行解析，不需要为不同格式编写不同逻辑）

5. **过滤非代码实现问题**：忽略与代码/文档修改无关的问题（如 git 操作建议、纯流程规范），只保留需要修改代码或规范文档的问题

6. **漂移问题精准识别同步策略**：
   - **改文档**：代码实现正确，文档描述过时 → 更新文档以匹配代码
   - **改代码**：文档规范正确，代码实现偏离 → 修改代码以符合文档
   - **双向调整**：双方都有问题 → 分别修正
   - 判断依据：以 proposal/design 中的设计意图为准，考虑代码的实际可运行性和测试覆盖，优先保持向后兼容

7. 生成 FixItem 列表，按依赖排序

8. **严格使用 `templates/fix-plan.md` 中的 FixItem 模板**，将 FixItem 写入各 change 目录下的 `fix-plan.md`

9. **执行模板中的「必填字段自检清单」**，全部通过后才能返回

10. 输出**元数据摘要**（FixItem 编号、文件路径、实体名、优先级、依赖列表）

---

## 步骤 3：主 agent 汇总

收集 subagent 元数据摘要，执行跨 pair 冲突检测：

1. **跨 pair 同名文件检测**：两个不同 ChangePair 的 FixItem 涉及同一文件路径 → 标记冲突，需串行化
2. **跨 pair 同名实体检测**：两个不同 ChangePair 的 FixItem 涉及同一实体名（如 Store 名称、API 路径、表名）→ 标记「疑似跨 pair 依赖」，供用户确认
3. **依赖关系全局排序**：先修上游，再修下游

---

## 步骤 4：输出 fix-plan

- 批量模式：生成全局汇总 `openspec/changes/fix-plan.md`
- 每个 change 目录下生成独立的 `fix-plan.md`
- 向用户展示修复统计摘要（总 Issue 数、按优先级分布、跨 change 依赖数）
- **等待用户确认后**，进入 fix 阶段
