# openspec-code-drift-sync 需求文档 v1

## 1. 技能目标

在 OpenSpec change 完成后、归档前，深度检查代码与文档之间的同步状态，发现漂移（Drift）问题，同时审核代码的架构质量。确保文档反映真实代码行为，代码遵循架构规范。

## 2. 核心能力

### 2.1 漂移检测（文档 vs 代码）

#### 2.1.1 后端漂移检测（6 维度）

| 编号 | 维度 | 对比目标 | 来源 |
|------|------|---------|------|
| B-1 | API 端点对比 | spec/design API 定义 vs Controller 实际端点 | prompt-openspec.md |
| B-2 | VO/DTO 字段完整性 | spec 声明的字段 vs VO 声明 + Service 赋值 | prompt-openspec.md |
| B-3 | 业务规则覆盖 | spec Scenario vs Service 代码分支 | prompt-openspec.md |
| B-4 | 设计决策遵循 | design.md Decisions vs 实际代码实现 | prompt-openspec.md |
| B-5 | 数据一致性 | Flyway SQL vs Entity vs spec 常量 | prompt-openspec.md |
| B-6 | 上下游引用 | 涉及 API/Service 的调用链和文档引用 | prompt-openspec.md |

#### 2.1.2 前端漂移检测（5 维度）

| 编号 | 维度 | 对比目标 | 来源 |
|------|------|---------|------|
| F-1 | 完整性检查 | tasks.md checkbox + spec Requirement 覆盖 | prompts-frotend.md |
| F-2 | 前后端接口一致性 | 前端 API 封装 vs 后端 Controller | prompts-frotend.md |
| F-3 | VO/DTO 字段级对齐 | spec 字段 vs 前端类型定义 | prompts-frotend.md |
| F-4 | 设计决策有效性 | design.md Decisions vs 实际前端代码 | prompts-frotend.md |
| F-5 | 降级策略验证 | design.md 降级方案 vs 前端实际处理 | prompts-frotend.md |

### 2.2 架构审核（代码 vs 架构规范，7 维度）

| 编号 | 维度 | 检查目标 | 来源 |
|------|------|---------|------|
| A | 分层架构合规性 | Controller→Biz→Service→Mapper 调用链跳层检测 | architecture-audit.md |
| B | 模块边界隔离 | 跨模块 import/调用合规性 | architecture-audit.md |
| C | 依赖方向正确性 | 循环依赖、反向依赖检测 | architecture-audit.md |
| D | 命名与组织规范 | 类名、目录位置、包结构 | architecture-audit.md |
| E | 过度工程化检测 | 不必要抽象、过度拆分 | architecture-audit.md |
| F | 安全架构基线 | 权限注解、SQL 安全、敏感数据 | architecture-audit.md |
| G | 可观测性架构 | 日志、异常处理、事务边界 | architecture-audit.md |

## 3. 漂移类型定义

| 类型 | 定义 | 处理 |
|------|------|------|
| 负向漂移 | 文档已定义，代码未实现或不完整 | 补齐代码 |
| 正向漂移 | 代码实现了文档未覆盖的场景 | 补充文档 |
| 冲突漂移 | 代码与文档都存在但互相矛盾 | 按真相源策略判定 |
| 完成度漂移 | 文档定义功能，代码完全缺失 | 标记待实现 |

## 4. 严重级别

| 级别 | 定义 | 处理方式 |
|------|------|---------|
| CRITICAL | 影响核心功能、业务逻辑或架构约束 | 立即阻断，必须修复 |
| WARNING | 影响代码质量或可维护性 | 建议修复，可延后 |
| SUGGESTION | 优化建议，不影响功能 | 记录，人工判断 |

> 架构审核使用 ADVISORY 级别（等同 SUGGESTION，在统一报告中合并）。

## 5. 真相源策略

| 冲突场景 | 真相源 | 行动 |
|---------|--------|------|
| 表面冲突（命名风格、路径格式差异） | 代码 | 更新文档 |
| 代码实现比文档更完善 | 代码 | 更新文档 |
| 文档设计比代码更合理 | 文档 | 修复代码 |
| 涉及核心业务规则 | 文档 | 修复代码（或经确认后更新文档） |
| 涉及安全约束 | 文档 | 修复代码 |
| 双向都有缺陷 | 无 | 人工介入，重新设计 |

## 6. 输入

- change 目录路径（`openspec/changes/<name>/`）
- change 类型（后端/前端，名称是否以 `-frontend` 结尾）
- change 涉及的变更文件列表（`git diff base..head --name-only`）
- 配对 change 信息（来自 `change-prd-mapping.yaml`）
- 项目架构规范文档（`docs/agent-context/architecture.md`）

## 7. 输出产物

| 文件 | 内容 | 位置 |
|------|------|------|
| `verify-report-{timestamp}.md` | 完整同步审核报告：漂移检测（第一部分） + 架构审核（第二部分） + 门禁判定 | `{changeDir}/` |

> **禁止覆盖**：文件名带时间戳（`YYYY-MM-DD-HH-MM`），写入前检查目标路径是否存在。若同名已存在，追加秒级精度（`-SS`）。禁止使用不带时间戳的固定文件名。
>
> **结构**：一份报告包含漂移检测和架构审核两个部分，用 `## 第一部分` / `## 第二部分` 分隔，头部为门禁判定和得分总览。

## 8. 门禁规则

- 存在 CRITICAL 问题 → **禁止归档**，必须修复
- 仅 WARNING/SUGGESTION → **可以归档**，建议记录 tech debt
- 全部通过 → **可以归档**

## 8.5. 与 Code Review 的边界

本技能与 code review 是**正交关系**，不可互相替代：

| | Code Review | Code-Drift-Sync |
|--|-------------|-----------------|
| **对比目标** | 代码 vs 代码质量标准 | 代码 vs 文档/架构规范 |
| **核心问题** | "代码写得对不对、好不好？" | "代码有没有按文档说的做？" |
| **典型发现** | N+1 查询、空指针、安全漏洞 | API 路径不一致、字段缺失、跳层调用 |
| **触发时机** | 每次 PR/commit | change 完成后、归档前 |

**本技能不检查**：逻辑错误、算法复杂度、代码风格、N+1 查询、空指针（属于 code review）。
**本技能只检查**：文档一致性、字段对齐、Scenario 覆盖、架构分层、模块边界、安全基线（属于同步审核）。

## 9. 约束条件

- 必须通过 subagent 执行（不得在主 agent 中直接执行检查）
- 必须实际读取代码文件，不可仅凭文件名或目录结构推断
- 每个问题必须有具体位置（文件路径:行号）和修复建议
- 中文输出，专业术语保留英文
- 不修改任何已有 artifacts，只生成报告

## 10. 待解决问题

1. 前端漂移检测的具体执行方式（是否需要浏览器/MCP 支持）？
2. 循环依赖检测的自动化程度（需要分析 Spring 容器还是仅静态 import 分析）？
3. 跨端检测是否只在实际有配对 change 时才执行？
4. 全量扫描的性能优化策略？
