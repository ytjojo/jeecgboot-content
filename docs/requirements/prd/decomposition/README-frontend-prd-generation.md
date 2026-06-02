# 前端 PRD 并行生成指南

## 文件结构

```
docs/requirements/prd/decomposition/
├── change-prd-mapping.yaml          # Change → PRD 映射配置
├── agent-prompt-template.md         # Agent prompt 模板
├── run-parallel-agents.sh           # 并行调度脚本
└── README-frontend-prd-generation.md  # 本文档

docs/requirements/prd/frontend/      # 输出目录
├── EPIC-01-frontend-prd.md
├── EPIC-02-frontend-prd.md
└── ...
```

## 使用方法

### 方法 1: 使用调度脚本（推荐）

```bash
# 并行处理所有 changes
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/requirements/prd/decomposition
./run-parallel-agents.sh

# 只处理单个 change
./run-parallel-agents.sh add-user-authentication
```

### 方法 2: 在 Claude Code 中手动调用 Agent

在 Claude Code 中，你可以直接调用 Agent 工具，为每个 change 启动独立的 agent：

```python
# 示例：为 add-user-authentication 生成前端 PRD
Agent({
    "description": "生成 EPIC-01 前端 PRD",
    "prompt": """
读取以下文件：
1. /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/openspec/changes/add-user-authentication/design.md
2. /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/openspec/changes/add-user-authentication/proposal.md
3. /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/openspec/changes/add-user-authentication/specs/ 下所有文件
4. /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/requirements/prd/decomposition/user/EPIC-01-user-authentication.md

根据这些文档，生成前端 PRD，保存到：
/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/requirements/prd/frontend/EPIC-01-frontend-prd.md

使用 agent-prompt-template.md 中的文档结构。
""",
    "run_in_background": True
})
```

### 方法 3: 批量调用（无人监督）

在 Claude Code 中一次性启动所有 agent：

```
请根据 change-prd-mapping.yaml 的映射，为每个 change 启动一个独立的 agent 生成前端 PRD。
每个 agent 只读取自己负责的文件，输出到 frontend/ 目录。
所有 agent 后台并行运行。
```

## 上下文精确性保证

每个 agent 的上下文严格限制为：

1. **Change 文档**：只读取 `openspec/changes/{change_name}/` 下的文件
   - `design.md` - 技术设计
   - `proposal.md` - 变更提案
   - `specs/` - 规格说明

2. **PRD 文档**：只读取对应的 PRD 文件
   - `docs/requirements/prd/decomposition/{domain}/EPIC-XX-*.md`

3. **输出隔离**：每个 agent 写入独立的输出文件

## 昙射表

| Change | Epic | Domain | PRD 文件 |
|--------|------|--------|----------|
| add-user-authentication | EPIC-01 | user | user/EPIC-01-user-authentication.md |
| complete-profile-management | EPIC-02 | user | user/EPIC-02-profile-management.md |
| complete-badges-points-growth | EPIC-03 | user | user/EPIC-03-badges-points-growth.md |
| social-subscription | EPIC-04 | user | user/EPIC-04-social-subscription.md |
| add-blocking-muting | EPIC-05 | user | user/EPIC-05-blocking-muting.md |
| privacy-notifications | EPIC-06 | user | user/EPIC-06-privacy-notifications.md |
| social-extensions | EPIC-07 | user | user/EPIC-07-social-extensions.md |
| feedback-support-system | EPIC-08 | user | user/EPIC-08-feedback-support.md |
| user-status-lifecycle | EPIC-09 | user | user/EPIC-09-user-status-lifecycle.md |
| circle-core | EPIC-10 | circle | circle/EPIC-10-circle-core.md |
| circle-content-interaction | EPIC-11 | circle | circle/EPIC-11-circle-content-interaction.md |
| circle-analytics-discovery | EPIC-12 | circle | circle/EPIC-12-circle-analytics-discovery.md |
| circle-growth-incentive | EPIC-13 | circle | circle/EPIC-13-circle-growth-incentive.md |
| channel-infrastructure | EPIC-20 | channel | channel/EPIC-20-channel-infrastructure.md |
| channel-privacy-membership | EPIC-21 | channel | channel/EPIC-21-channel-privacy-membership.md |
| channel-content-governance | EPIC-22 | channel | channel/EPIC-22-channel-content-governance.md |
| channel-discovery | EPIC-23 | channel | channel/EPIC-23-channel-discovery.md |
| channel-lifecycle-stats | EPIC-24 | channel | channel/EPIC-24-channel-lifecycle.md |

## 监控和日志

- 日志目录：`docs/requirements/prd/frontend/logs/`
- 每个 agent 的日志：`{EPIC_NUMBER}.log`
- 检查进度：`ls -la docs/requirements/prd/frontend/*.md`

## 故障处理

1. **某个 agent 失败**：查看对应日志，手动重新运行该 change
2. **全部重新生成**：删除 `frontend/` 目录下的文件，重新运行脚本
3. **增加新 change**：在 `change-prd-mapping.yaml` 中添加映射条目
