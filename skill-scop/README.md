# 技能管理文档区

本目录用于存放 AI Agent 技能的创建、迭代及相关决策文档。

## 目录结构

```
skill-scop/
├── {skill-name}/          # 技能目录（必须使用英文命名）
│   ├── raw/               # 人类准备的原始文档
│   │   └── ...            # 作为技能创建/迭代的上下文输入
│   └── workspace/         # 人机协作文档区
│       ├── decisions.md   # 决策记录
│       ├── requirements-v{N}.md  # 带版本号的需求文档
│       ├── workflow.md    # 技能工作流定义
│       ├── dimensions.md  # 评估维度
│       ├── metrics.md     # 性能指标
│       └── templates/     # 输出文档模板
└── README.md              # 本文件
```

## 核心概念

### raw（原始输入）
- 由人类准备的文档区域
- 作为创建或迭代技能的上下文输入
- 可包含：需求描述、参考资料、示例数据、业务规则等

### workspace（协作空间）
- 人与 Agent 协作产生的文档集合
- 包含：决策记录、迭代需求、工作流定义、评估维度、指标定义、输出模板等
- 为技能的创建和迭代提供参考依据

## 使用流程

### 创建新技能
1. 创建以技能名称命名的目录（英文，如 `code-review`、`api-design`）
2. 在 `raw/` 中放入原始需求文档、参考资料
3. 在 `workspace/` 中与 Agent 协作：
   - 明确技能目标和边界
   - 定义工作流步骤
   - 设定评估维度和指标
   - 设计输出文档模板
4. 基于 workspace 文档生成最终技能定义

### 迭代现有技能
1. 在 `workspace/` 中创建带版本号的需求文档（如 `requirements-v2.md`）
2. 记录决策和变更原因
3. 更新工作流、维度、指标等
4. 基于新版本 workspace 文档更新技能定义

## 命名规范

- **技能目录**：必须使用英文，kebab-case 格式（如 `git-commit`、`test-generation`）
- **版本文档**：使用 `requirements-v{N}.md` 格式（如 `requirements-v1.md`、`requirements-v2.md`）
- **决策记录**：统一使用 `decisions.md`

## 注意事项

- 每个技能独立一个目录，保持隔离
- workspace 文档应保留完整历史，便于追溯决策演进
- 版本升级时需说明变更原因和影响范围
