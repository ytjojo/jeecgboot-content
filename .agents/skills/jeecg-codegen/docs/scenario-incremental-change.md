# 场景：增量修改已有模块

适用条件：用户要给已有模块加字段、删字段、改字段，而不是重新生成整个模块。

## 识别信号

- `加字段`、`增加字段`、`新增字段`
- `删字段`、`删除字段`
- `改字段`、`修改字段`
- `给某模块加一个字段`、`把某字段改成...`

## 必做步骤

1. 识别目标模块：优先用表名、实体名、模块名三者交叉确认。
2. 搜索并读取现有文件，至少覆盖 `Entity.java`、`*.data.ts`、`*List.vue`、`*Modal.vue`，必要时补读 `*Form.vue`。
3. 从实体和前端配置中解析当前字段列表，确认真实现状。
4. 按新增、删除、修改三类动作分别生成变更方案。
5. 先展示文件级修改摘要，用户确认后再精确编辑。
6. 生成对应 `ALTER TABLE` Flyway SQL。

## 修改范围

- 后端：字段声明、注解、导入、查询规则、特殊方法。
- 前端：`columns`、`formSchema`、`searchFormSchema`、`superQuerySchema`、Native 表单模板。
- SQL：`ADD COLUMN`、`DROP COLUMN`、`MODIFY COLUMN`。

## 硬规则

- 不允许只改单个文件后就结束，必须检查前后端和 SQL 是否同步。
- 修改前必须展示“哪些文件改、每个文件改什么”。
- 如果现有代码有手工定制痕迹，优先做最小改动，不覆盖用户自定义逻辑。

## 模板入口

- 增量修改总入口：`docs/reference-template-incremental-change.md`
- 字段规则和组件映射：读取 `docs/reference-field-mapping.md`。
- 涉及树表或一对多时，再补读 `docs/reference-table-patterns.md`。
