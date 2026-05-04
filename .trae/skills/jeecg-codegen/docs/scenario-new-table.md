# 场景：新建表全量生成

适用条件：用户没有现成表，只给出业务需求、字段描述或页面诉求，希望直接生成 JeecgBoot 模块。

## 必做步骤

1. 从用户描述中提取表名、实体名、功能名、字段列表和业务含义。
2. 判断表形态：单表、树表或一对多。
3. 用智能字段推导规则补齐 DB 类型、Java 类型、前端控件和校验方式。
4. 默认补齐系统字段；树表补齐 `pid`、`has_child`；一对多补齐主子表结构。
5. 生成建表 DDL 与菜单 SQL 的执行计划。
6. 输出结构摘要，用户确认后再落地代码和 Flyway SQL。

## 提取优先级

- 用户明确指定优先于自动推导。
- 字段语义优先于通用默认值。
- 字典编码若用户已指定，直接使用；否则再做智能匹配。

## SQL 要求

- Flyway 文件必须先放建表 DDL，再放菜单权限 SQL。
- 版本号按当天已有文件自动递增。
- 菜单 ID 使用真实毫秒时间戳生成。

## 输出摘要至少包含

- 表名、实体名、模块名、视图目录。
- 字段清单：名称、DB 类型、Java 类型、控件、字典、是否必填。
- 表类型与前端风格。
- 将生成的 Flyway 文件说明。

## 模板入口

- 单表模板：`docs/reference-template-single-table.md`
- 树表差异：`docs/reference-template-tree-table.md`
- 一对多差异：`docs/reference-template-master-detail.md`
- 建表 DDL：`docs/reference-template-field-ddl.md`
