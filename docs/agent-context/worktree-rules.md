# Git Worktree 详细规则

> 本文件由 `AGENTS.md` 引用，包含执行层实现细节。主 agent 启动时必须阅读。

---

## 场景识别：会话启动时必须执行

会话启动后，主 agent 首先判断当前所处环境和任务并行需求，确定场景：

```
检查当前目录是否为 worktree（git rev-parse --git-dir）
├── 已在某个 worktree 中
│    ├── 后续任务无需并行 subagent → 【场景 A】在此 worktree 直接工作
│    └── 后续任务需要并行 subagent → 【场景 B】保留此 worktree + 新建并行 worktree
└── 在主 worktree 中
     ├── 后续任务无需并行 subagent → 【场景 C】新建 1 个 worktree
     └── 后续任务需要并行 subagent → 【场景 D】新建 N 个 worktree 并行
```

**注册规则**（启动时写入 registry，格式含来源前缀）：
```bash
# 已有 worktree（场景 A/B 中已存在的）→ 不纳入本会话清理范围
echo "existing:<worktree-path>" >> .claude/worktree-registry.txt

# 本会话创建的 worktree（场景 B/C/D 新建的）→ 会话结束时清理
echo "created:<worktree-path>" >> .claude/worktree-registry.txt
```

---

## 安全机制

**命名防冲突**：创建前执行 `git worktree list`，名称冲突则追加随机后缀。

**所有权标记**：创建后立即写入（包含来源分支 + 会话标识 + 所有权模式）：
```bash
WORKTREE_SESSION_MARKER="$(date -u +%Y%m%dT%H%M%SZ)-$$"
echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) $(whoami) source=$(git branch --show-current) session=$WORKTREE_SESSION_MARKER ownership=<exclusive|shared>" > <worktree-path>/.worktree-owner
```
subagent 启动后必须读取 `.worktree-owner`，获取 `source=` 字段确认来源分支，获取 `ownership=` 字段确认自己的清理职责。

**并发安全**：`locked` 的 worktree 属于其他活跃 session，绝对不可触碰。孤立 worktree（无 `.worktree-owner` 文件）报告用户决定。

**`--force` 只在 3 种场景允许**：已确认所有权、已确认合并、用户明确要求。

---

## 引用计数（shared worktree 必须维护）

`ownership=shared` 的 worktree，每个使用它的 subagent 必须在进入和退出时写入引用记录：
```bash
# subagent 进入 worktree 时登记
echo "+1 $(date -u +%Y%m%dT%H%M%SZ) agent=$AGENT_ID" >> <worktree-path>/.worktree-refcount

# subagent 完成后注销（不清理 worktree，只写标记）
echo "-1 $(date -u +%Y%m%dT%H%M%SZ) agent=$AGENT_ID status=done" >> <worktree-path>/.worktree-refcount
```

主 agent 清理前检查引用计数：
```bash
ACTIVE=$(grep -c "^+1" <worktree-path>/.worktree-refcount 2>/dev/null || echo 0)
DONE=$(grep -c "^-1" <worktree-path>/.worktree-refcount 2>/dev/null || echo 0)
IN_USE=$((ACTIVE - DONE))
# IN_USE > 0 → 跳过，仍有 subagent 在使用
# IN_USE == 0 → 可以执行清理校验
```

---

## subagent 完成动作

```
ownership=exclusive 的 subagent 完成后：
  1. 合并代码回来源分支
  2. 执行 4 项清理校验（全通过才继续）
  3. git worktree remove <path> && git branch -d <branch>

ownership=shared 的 subagent 完成后：
  1. 合并代码回来源分支
  2. 写注销标记：echo "-1 ..." >> .worktree-refcount
  3. 停止，不执行 git worktree remove（等主 agent 兜底）
```

---

## 清理前 4 项校验（任一失败 → 停止，报告用户）

```bash
cat <worktree-path>/.worktree-owner                     # 1. 确认 session= 匹配当前会话
git worktree list | grep <path>                         # 2. 确认未被 locked
git branch --merged springboot3_content | grep <branch> # 3. 确认已合并回来源分支
git -C <worktree-path> status --short                   # 4. 确认无未提交改动
# shared worktree 额外执行：
# 5. 确认引用计数 IN_USE == 0
```

---

## 主 agent 兜底清理（会话结束时执行）

按 registry 文件逐行处理，仅处理 `created:` 前缀条目：
```bash
while IFS= read -r line; do
  [[ "$line" != created:* ]] && continue   # existing: 跳过
  path="${line#created:}"

  # 读取 ownership
  ownership=$(grep -o 'ownership=[^ ]*' "$path/.worktree-owner" | cut -d= -f2)

  # shared: 额外检查引用计数
  if [[ "$ownership" == "shared" ]]; then
    ACTIVE=$(grep -c "^+1" "$path/.worktree-refcount" 2>/dev/null || echo 0)
    DONE=$(grep -c "^-1" "$path/.worktree-refcount" 2>/dev/null || echo 0)
    [[ $((ACTIVE - DONE)) -gt 0 ]] && echo "⚠️ $path 仍有活跃 subagent，跳过" && continue
  fi

  # exclusive: subagent 应已清理，若仍存在说明 subagent 异常退出，主 agent 兜底
  # 执行 4 项校验后清理
  do_cleanup "$path"
done < .claude/worktree-registry.txt
# 全部处理完毕后删除 registry 文件
```

**红线**：不匹配 session 不碰、locked 不碰、无 `.worktree-owner` 不碰、`existing:` 前缀不碰、不以 `git worktree list` 全量输出为依据。

---

**Worktree 追踪**：主 agent 每次创建 worktree 时，必须将路径追加写入 registry：
```bash
echo "created:<worktree-path>" >> .claude/worktree-registry.txt
```
该文件是会话结束清理的唯一依据——只清理 `created:` 前缀条目，`existing:` 前缀条目绝不清理，绝不凭 `git worktree list` 全量输出自行判断。
