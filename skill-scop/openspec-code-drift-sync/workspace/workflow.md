# 技能工作流定义

## 整体流程

```
                      ┌─────────────────────┐
                      │   用户触发技能        │
                      │ /openspec-code-drift │
                      └──────────┬──────────┘
                                 │
                      ┌──────────▼──────────┐
                      │ Step 1: 选择 Change  │
                      │ + 识别类型/配对       │
                      └──────────┬──────────┘
                                 │
                      ┌──────────▼──────────┐
                      │ Step 2: 加载上下文    │
                      │ artifacts + git diff │
                      └──────────┬──────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
     ┌────────▼────────┐ ┌──────▼──────┐ ┌────────▼────────┐
     │ Step 3a: 后端漂移 │ │Step 3b: 前端│ │ Step 3c: 架构   │
     │ 检测 subagent    │ │漂移检测 sub │ │ 审核 subagent   │
     │ (6 维度)         │ │(5 维度)     │ │ (7 维度)        │
     └────────┬────────┘ └──────┬──────┘ └────────┬────────┘
              │                  │                  │
              └──────────────────┼──────────────────┘
                                 │
                      ┌──────────▼──────────┐
                      │ Step 4: 汇总报告     │
                      │ subagent 合并 3 输出 │
                      └──────────┬──────────┘
                                 │
                      ┌──────────▼──────────┐
                      │ Step 5: 输出报告     │
                      │ + 门禁判定           │
                      └─────────────────────┘
```

## Step 1：选择 Change 并识别类型

### 输入
- 用户可能提供 change 名称，也可能不提供

### 操作
1. 若未指定 change，运行 `openspec list --json` 获取可用 changes
2. 使用 **AskUserQuestion** 让用户选择
3. 识别 change 类型：
   - 名称以 `-frontend` 结尾 → **前端 change**
   - 否则 → **后端 change**
4. 从 `docs/requirements/prd/decomposition/change-prd-mapping.yaml` 获取：
   - `domain`、`epic`、PRD 路径
   - 配对 change 名称（若有）
5. 检查配对 change 目录是否存在（仅存在时才执行跨端检测）

### 输出
- change 名称、类型、目录路径
- 配对 change 信息
- PRD 路径

---

## Step 2：加载上下文

### 操作
1. 运行 `openspec status --change "<name>" --json` 获取元数据
2. 运行 `openspec instructions apply --change "<name>" --json` 获取 artifact 路径
3. 读取所有 artifacts（proposal.md、design.md、specs/*.md、tasks.md）
4. 获取变更文件列表：`git diff --name-only <base-branch>..<feature-branch>`
5. 读取项目架构规范：`docs/agent-context/architecture.md`
6. 若存在配对 change，同样加载配对 change 的 artifacts

### 输出
- contextFiles（artifact 文件路径映射）
- changedFiles（变更文件列表，按模块/包分类）
- 架构规范参考

---

## Step 3a：后端漂移检测 subagent

### 触发条件
- change 类型为后端（非 `-frontend` 结尾）
- 或者 change 有配对后端 change

### subagent 类型
`Explore`（只读）+ `Grep` + `Glob` + `Read`

### subagent 边界
- 代码搜索范围：`jeecg-boot/jeecg-boot-module/jeecg-module-content/` + change 涉及的其他后端模块
- 文档范围：change 目录下的 specs/、design.md、tasks.md
- 输出：结构化 JSON（差异表），写入临时文件

### 检查维度
见 `prompt-openspec.md` 中的 6 维度：
1. API 端点对比
2. VO/DTO 字段完整性
3. 业务规则覆盖
4. 设计决策遵循
5. 数据一致性
6. 上下游引用

### subagent prompt 模板
见 `prompt-openspec.md`（raw 目录），需包装为 subagent 可执行的格式。

---

## Step 3b：前端漂移检测 subagent

### 触发条件
- change 类型为前端（`-frontend` 结尾）
- 或者 change 有配对前端 change

### subagent 类型
`Explore`（只读）+ `Grep` + `Glob` + `Read`

### subagent 边界
- 代码搜索范围：`jeecgboot-vue3/src/` + change 涉及的前端目录
- 文档范围：change 目录下的 specs/、design.md、tasks.md
- 输出：结构化 JSON（差异表），写入临时文件

### 检查维度
见 `prompts-frotend.md` 中的 5 维度：
1. 完整性检查
2. 前后端接口一致性
3. VO/DTO 字段级对齐
4. 设计决策有效性
5. 降级策略验证

### subagent prompt 模板
见 `prompts-frotend.md`（raw 目录），需包装为 subagent 可执行的格式。

---

## Step 3c：架构审核 subagent

### 触发条件
- 始终执行（任何 change 类型都需要架构审核）

### subagent 类型
`Explore`（只读）+ `Grep` + `Glob` + `Read`

### subagent 边界
- 代码搜索范围：change 涉及的变更文件
- 架构规范参考：`docs/agent-context/architecture.md`、`docs/agent-context/springboot-coding-conventions.md`
- 输出：结构化 JSON（问题清单），写入临时文件

### 检查维度
见 `architecture-audit.md` 中的 7 维度：
- A: 分层架构合规性
- B: 模块边界隔离
- C: 依赖方向正确性
- D: 命名与组织规范
- E: 过度工程化检测
- F: 安全架构基线
- G: 可观测性架构

---

## Step 4：汇总报告

### 操作
1. 读取 Step 3a/3b/3c 的 subagent 输出（JSON 文件）
2. 合并所有发现的问题
3. 按严重级别分组：CRITICAL → WARNING → SUGGESTION
4. 去重（同一文件同一行号的相同问题）
5. 对冲突漂移应用真相源策略判定
6. 计算各维度得分
7. 生成综合门禁判定

### 输出文件（禁止覆盖）

> **硬规则**：写入前必须检查目标路径是否存在。若存在同名文件，自动追加秒级精度后缀。

**单文件输出**：`{changeDir}/verify-report-{timestamp}.md`

**内容结构**：
```
├── 门禁判定 + 得分总览
├── 第一部分：漂移检测（后端 B-1~B-6 + 前端 F-1~F-5）
├── 第二部分：架构审核（A~G 七维度）
├── 问题合并统计 + 阻断问题
└── 修复优先级 + 量化指标 + 后续步骤
```

**时间戳格式**：`YYYY-MM-DD-HH-MM`（分钟精度），若冲突则降级到 `YYYY-MM-DD-HH-MM-SS`（秒精度）。

**写入前检查**：
```bash
# 伪代码
if [ -f "{target_path}" ]; then
  # 文件名追加秒级精度重试
  target_path = "{changeDir}/verify-report-{YYYY-MM-DD-HH-MM-SS}.md"
fi
# 仍冲突则报错，要求用户手动清理
```

---

## Step 5：输出摘要与门禁判定

### 摘要格式
```markdown
## 同步审核完成: <change-name>

### 门禁判定: ✅ 通过 / ❌ 阻断

### 漂移检测
| 维度 | 检查项 | CRITICAL | WARNING | SUGGESTION | 得分 |
|------|--------|----------|---------|------------|------|
| ... | | | | | |

### 架构审核
| 维度 | 检查项 | CRITICAL | WARNING | ADVISORY | 得分 |
|------|--------|----------|---------|----------|------|
| ... | | | | | |

### 关键问题
[列出所有 CRITICAL 级别问题]

### 建议
- 修复 CRITICAL 后重新验证
- 查看详细报告: drift-report.md / architecture-audit-report.md
```

### 门禁规则
- 存在 CRITICAL → **阻断归档**，输出修复清单
- 仅 WARNING/SUGGESTION → **允许归档**，建议记录 tech debt
- 全部通过 → **允许归档**

---

## 跳过条件

以下情况可跳过部分检查：
- change 仅有 tasks.md（无 specs/design） → 仅执行任务完成度检查 + 架构审核
- change 仅有后端无前端配对 → 跳过 Step 3b
- change 仅有前端无后端配对 → 跳过 Step 3a，Step 3c 聚焦前端架构审核
- `git diff` 变更文件为 0 → 报告异常，建议检查分支状态

## 并行执行

Step 3a、3b、3c 可以**并行**执行（三个 subagent 互不依赖），完成后汇总。

## 错误处理

- subagent 超时 → 该维度标记为 "未完成"，报告中说明原因
- artifact 缺失 → 跳过依赖该 artifact 的检查，报告中标注
- git diff 失败 → 尝试其他方式获取变更范围（如手动指定文件列表）
