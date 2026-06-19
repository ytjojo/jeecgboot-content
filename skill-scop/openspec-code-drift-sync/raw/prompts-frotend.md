

  请对 openspec change `{change-name}` 执行文档-代码一致性验证，防止文档腐化。按以下维度逐一检查，输出报告到 `openspec/changes/{change-name}/verify-report-{YYYY-MM-DD-HH-MM.md`。

  ## 检查维度

  ### 1. 完整性检查
  - 读取 tasks.md，统计 `- [ ]` vs `- [x]`，列出所有未完成任务
  - 读取 specs/ 下每个 spec.md，提取所有 `### Requirement:` 和 `#### Scenario:`
  - 对每个 requirement：搜索代码库确认是否有对应实现（文件路径+行号）

  ### 2. 前后端接口一致性（核心）
  - 从 spec.md 和 design.md 提取所有 API 端点（HTTP 方法 + 路径 + 参数 + 返回类型）
  - 搜索后端 Controller：读取每个端点的方法签名，确认：
    - 路径是否一致
    - 参数名、参数类型、是否必填是否与 spec 一致
    - 返回类型（VO 类名）是否与 spec 一致
  - 搜索前端 API 封装：确认前端调用的接口函数签名是否与后端匹配
  - 对比前后端参数：前端传参名 vs 后端 `@RequestParam` 名、前端传参类型 vs 后端参数类型
  - 标的：找出所有「后端有但前端未对接」「前端调了但后端不存在」「参数名/类型不一致」

  ### 3. VO/DTO 字段级对齐
  - 读取每个后端 VO 类的完整字段列表（`private Xxx fieldName;`）
  - 与 spec.md 或 design.md 中的字段映射表逐字段对比
  - 标的：找出「spec 声称有但 VO 缺失」「VO 有但 spec 未提及」「字段类型不一致」

  ### 4. 设计决策有效性
  - 读取 design.md，提取所有 `### D*:` 决策
  - 验证每个决策在代码中是否被遵循（目录结构、命名约定、缓存策略、降级方案等）
  - 标的：找出「设计已过时」「实现偏离设计」「设计合理但代码未执行」

  ### 5. 降级策略验证
  - 如果 design.md 有「缺失字段降级策略」表，逐项验证：
    - 后端确认该字段是否确实缺失（读 VO 源码）
    - 降级方案是否可执行（如「前端硬编码 100」→ 搜索确认前端或后端是否有对应常量）

  ## 输出格式

  报告结构：
  1. 摘要表（Completeness / Correctness / API 对齐 / Coherence 各维度 Status）
  2. 按 CRITICAL → WARNING → SUGGESTION 三级列出所有发现问题
  3. 每个问题包含：文件引用（`file.ts:123`）、具体差异描述、可操作修复建议
  4. 最终评估：是否有 CRITICAL 阻挡归档

  ## 关键原则
  - 必须实际读取代码文件，不可仅凭文件名或目录结构推断
  - 区分「尚未实现」vs「实现错误」——后者才需要标记为漂移
  - 区分「独立功能」vs「错误实现」——同一目录下可能有不同体系的代码
  - 参数名大小写/命名风格差异（如 badgeId vs badgeCode）应标记为 WARNING
  - 不确定时降低严重级别（CRITICAL → WARNING