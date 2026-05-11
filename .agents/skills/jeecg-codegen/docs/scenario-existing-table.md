# 场景：现有表全量生成

适用条件：用户给出真实表名，希望基于现有数据库表反向生成 JeecgBoot 代码。

## 必做步骤

1. 先确认目标数据库名，不能直接使用默认库名。
2. 查询目标表 DDL 和字段明细，作为唯一可信输入。
3. 从 DDL 中识别主键策略、系统字段、树表字段、一对多关联痕迹。
4. 根据字段注释优先推导控件；注释不足时再按 DB 类型映射。
5. 输出字段摘要，等待用户确认后再生成文件。
6. 先读取 `codegen-reference.md`，再进入对应模板文件输出代码。

## 必查信息

- `SHOW CREATE TABLE` 获取完整建表语句。
- `information_schema.COLUMNS` 获取字段类型、可空、默认值、注释、主键、额外属性。
- 如果需要复用字典，再查询系统字典或分类字典。

## 判断重点

- 主键是否为 `AUTO_INCREMENT`，决定 `@TableId` 类型。
- 是否真实包含 `create_by`、`create_time`、`update_by`、`update_time`、`sys_org_code`。
- 是否包含 `pid`、`has_child` 等树表字段。
- 是否存在明显主子表关系，必要时切换到一对多模式。

## 输出摘要至少包含

- 表名、实体名、模块名。
- 字段列表：字段名、DB 类型、Java 类型、前端控件、是否字典。
- 主键策略、系统字段识别结果。
- 表类型：单表 / 树表 / 一对多。
- 计划读取的模板章节。

## 模板入口

- 单表：`docs/reference-template-single-table.md`
- 树表：`docs/reference-template-tree-table.md`
- 一对多：`docs/reference-template-master-detail.md`

## 回退策略

- 数据库无法连接时，明确告知用户，并在项目 SQL 文件中搜索表定义作为降级方案。
- 降级方案只用于补充，不应在已能连库时替代真实 DDL。
