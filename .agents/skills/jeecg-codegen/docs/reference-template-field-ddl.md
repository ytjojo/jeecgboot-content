## 适用场景

- 需要把业务字段语义映射为 DB 类型、Java 类型和前端控件。
- 需要生成建表 DDL。
- 需要在生成前快速核对字段类型、查询组件和表单组件。

## 必读前置

- 先确认字段语义和用户指定是否已收集完整。
- 先判断字段来自现有表还是新建表，避免覆盖真实 DDL。
- 若字段涉及字典、树表、一对多子表，需结合其他模板文档一起看。

## 常见误用

- 用语义推导结果覆盖现有表的真实字段定义。
- 只看表单组件，不同步检查查询组件和导出格式。
- 生成 DDL 时遗漏系统字段、索引或默认值策略。

## D. 字段类型完整映射速查表

| 业务语义 | DB列类型 | Java类型 | @Excel format | vue3 FormSchema component | vue3Native 控件 | 查询组件 |
|----------|---------|----------|---------------|--------------------------|----------------|---------|
| 名称/编码/标题 | varchar(100) | String | - | Input | a-input | JInput |
| 金额/价格 | decimal(10,2) | BigDecimal | - | InputNumber | a-input-number | InputNumber |
| 整数/数量 | int(11) | Integer | - | InputNumber | a-input-number | InputNumber |
| 浮点数 | double | Double | - | InputNumber | a-input-number | InputNumber |
| 状态/类型(字典) | varchar(10) | String | dicCode | JDictSelectTag | JDictSelectTag | JDictSelectTag |
| 单选(字典) | varchar(10) | String | dicCode | JDictSelectTag(type=radio) | a-radio-group | JDictSelectTag |
| 多选(字典) | varchar(200) | String | dicCode | JDictSelectTag(type=checkbox) | a-checkbox-group | - |
| 开关/是否 | varchar(2) | String | - | JSwitch | a-switch | - |
| 日期 | date | Date | yyyy-MM-dd | DatePicker | a-date-picker | DatePicker |
| 日期时间 | datetime | Date | yyyy-MM-dd HH:mm:ss | DatePicker(showTime) | a-date-picker(showTime) | DatePicker(showTime) |
| 长文本/备注 | text | String | - | InputTextArea | a-textarea | - |
| 富文本 | text | String | - | JEditor | JEditor | - |
| Markdown | text | String | - | JMarkdownEditor | JMarkdownEditor | - |
| 图片 | varchar(1000) | String | - | JImageUpload | JImageUpload | - |
| 文件/附件 | varchar(1000) | String | - | JUpload | JUpload | - |
| 用户选择 | varchar(32) | String | dictTable=sys_user | JSelectUserByDept | JSelectUserByDept | - |
| 部门选择 | varchar(32) | String | dictTable=sys_depart | JSelectDept | JSelectDept | - |
| 分类树 | varchar(64) | String | - | JCategorySelect | JCategorySelect | JCategorySelect |
| 搜索选择 | varchar(32) | String | dictTable | JSearchSelect | JSearchSelect | JSearchSelect |
| 省市区 | varchar(200) | String | - | JAreaLinkage | JAreaLinkage | - |
| 排序号 | int(11) | Integer | - | InputNumber | a-input-number | - |

## E. 建表 DDL 模板（如需要自动建表）

```sql
CREATE TABLE `{{tableName}}` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  -- 业务字段
  -- `field_name` varchar(100) DEFAULT NULL COMMENT '字段注释',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{{description}}';
```

树表额外字段：
```sql
  `pid` varchar(36) DEFAULT NULL COMMENT '父级节点',
  `has_child` varchar(3) DEFAULT NULL COMMENT '是否有子节点',
```

子表额外字段：
```sql
  `{{main_table_name}}_id` varchar(36) DEFAULT NULL COMMENT '主表外键',
```

---
