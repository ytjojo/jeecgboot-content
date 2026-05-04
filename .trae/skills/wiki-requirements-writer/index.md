# Wiki Requirements Writer Index

本目录提供一个完整的 `Wiki 风格需求文档` 技能包，适用于需求生成、重写、审计和结构化整理。

## 文件清单

- `SKILL.md`
  - 主技能定义
  - 说明何时触发、如何输出、如何补齐缺失信息

- `templates/wiki-requirements-template.md`
  - 标准 Wiki 需求模板
  - 适合从零开始撰写分层需求文档

- `examples/simple-wiki-demo.md`
  - 最小示例
  - 演示 `BR / UR / SR / DR` 分层、双向引用、错误矩阵

- `checklists/wiki-quality-checklist.md`
  - 质量审查清单
  - 适合用于审查已有 PRD、Wiki 页面或 Agent 输出结果

## 推荐用法

### 生成新文档

1. 从 `templates/wiki-requirements-template.md` 开始
2. 根据复杂度选择轻量分层或完整 `BR / UR / SR / DR`
3. 参照 `examples/simple-wiki-demo.md` 对齐输出风格
4. 使用 `checklists/wiki-quality-checklist.md` 做最终复核

### 重写现有文档

1. 先判断原文档是否为线性 PRD、零散笔记或已存在的 Wiki
2. 按 `SKILL.md` 中的层级规则和追溯规则重组内容
3. 用检查清单核对缺失项、模糊项和不可测试项

### 审计现有文档

1. 打开 `checklists/wiki-quality-checklist.md`
2. 按结构、追溯、验收、NFR、Agent 上下文五个维度检查
3. 输出问题列表、严重级别、修复建议

## 输出模式

- `Wiki Full Spec`
- `Wiki Rewrite`
- `Wiki Gap Audit`
- `Wiki Lean Spec`

## 适用场景

- `Confluence` 层级页面
- `Notion` 知识库需求页
- `飞书` 文档目录化需求
- `语雀` 分层需求规格
- AI Agent 可消费的结构化需求文档
