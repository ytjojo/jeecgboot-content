# JeecgBoot 代码生成参考模板

本文档包含 JeecgBoot CRUD 代码的完整模板骨架，用 `{{变量}}` 标注替换位置。

## 适用场景

- 单表 CRUD 的完整代码生成。
- 需要标准后端 6 件套和单表前端页面模板。
- 需要 `vue3` 或 `vue3Native` 单表模板。
- 需要菜单权限 SQL 模板。

## 必读前置

- 先读取 `SKILL.md` 判断当前是否属于单表场景。
- 若来自现有表生成，先完成 DDL 读取和字段摘要确认。
- 若来自新建表生成，先完成字段推导和结构摘要确认。
- 若后续识别成树表或一对多，应立即转读对应模板文档。

## 常见误用

- 明明是树表，却只套用单表模板。
- 明明是一对多，却遗漏子表模板和 VO 模板。
- 没判断前端风格，就混用 `vue3` 和 `vue3Native` 代码片段。
- 未确认字段、字典和主键策略就直接套模板。

## 变量说明

| 变量 | 说明 | 示例 |
|------|------|------|
| `{{tableName}}` | 数据库表名 | `biz_goods` |
| `{{entityName}}` | 实体类名(PascalCase) | `BizGoods` |
| `{{entityName_uncap}}` | 实体变量名(camelCase) | `bizGoods` |
| `{{entityPackage}}` | 模块包名 | `biz` |
| `{{entityPackagePath}}` | URL路径(同entityPackage或含/) | `biz` |
| `{{description}}` | 功能描述 | `商品管理` |
| `{{today}}` | 生成日期 | `2026-03-11` |
| `{{timestamp}}` | 13位毫秒级真实时间戳(用于菜单ID，通过`date +%s%3N`获取) | `1741704000123` |
| `{{moduleRoot}}` | 后端模块根路径 | `jeecg-module-system/jeecg-system-biz` |
| `{{viewDir}}` | 前端视图目录 | `biz/goods` |

---

## A. 单表模式

### 生成文件清单

**后端 6 个文件：**
1. `{{moduleRoot}}/src/main/java/org/jeecg/modules/{{entityPackage}}/entity/{{entityName}}.java`
2. `{{moduleRoot}}/src/main/java/org/jeecg/modules/{{entityPackage}}/controller/{{entityName}}Controller.java`
3. `{{moduleRoot}}/src/main/java/org/jeecg/modules/{{entityPackage}}/service/I{{entityName}}Service.java`
4. `{{moduleRoot}}/src/main/java/org/jeecg/modules/{{entityPackage}}/service/impl/{{entityName}}ServiceImpl.java`
5. `{{moduleRoot}}/src/main/java/org/jeecg/modules/{{entityPackage}}/mapper/{{entityName}}Mapper.java`
6. `{{moduleRoot}}/src/main/java/org/jeecg/modules/{{entityPackage}}/mapper/xml/{{entityName}}Mapper.xml`

**前端 - vue3 封装风格（4个文件）：**
7. `src/views/{{viewDir}}/{{entityName}}.api.ts`
8. `src/views/{{viewDir}}/{{entityName}}.data.ts`
9. `src/views/{{viewDir}}/{{entityName}}List.vue`
10. `src/views/{{viewDir}}/components/{{entityName}}Modal.vue`

**前端 - vue3Native 原生风格（5个文件）：**
7. `src/views/{{viewDir}}/{{entityName}}.api.ts`
8. `src/views/{{viewDir}}/{{entityName}}.data.ts`
9. `src/views/{{viewDir}}/{{entityName}}List.vue`
10. `src/views/{{viewDir}}/components/{{entityName}}Modal.vue`
11. `src/views/{{viewDir}}/components/{{entityName}}Form.vue`

**SQL（1个文件）：**
12. Flyway SQL: `jeecg-module-system/jeecg-system-start/src/main/resources/db/flyway/V{{version}}__{{description}}.sql`

---

### A1. Entity.java

```java
package org.jeecg.modules.{{entityPackage}}.entity;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: {{description}}
 * @Author: jeecg-boot
 * @Date: {{today}}
 * @Version: V1.0
 */
@Data
@TableName("{{tableName}}")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "{{description}}")
public class {{entityName}} implements Serializable {
    private static final long serialVersionUID = 1L;

    // === 主键字段（根据表DDL自适应） ===

    // --- 方式1: JeecgBoot 标准字符串主键 (varchar(36)/varchar(32), 无AUTO_INCREMENT) ---
    // @TableId(type = IdType.ASSIGN_ID)
    // @Schema(description = "主键")
    // private String id;

    // --- 方式2: int 自增主键 (int AUTO_INCREMENT) ---
    // @TableId(type = IdType.AUTO)
    // @Schema(description = "主键")
    // private Integer id;

    // --- 方式3: bigint 自增主键 (bigint AUTO_INCREMENT) ---
    // @TableId(type = IdType.AUTO)
    // @Schema(description = "主键")
    // private Long id;

    // --- 方式4: bigint 雪花ID (bigint, 无AUTO_INCREMENT) ---
    // @TableId(type = IdType.ASSIGN_ID)
    // @Schema(description = "主键")
    // private Long id;

    // === 业务字段（根据需求生成） ===
    // 每个业务字段按以下规则生成注解：

    // --- String 字段 ---
    // @Excel(name = "字段注释", width = 15)
    // private String fieldName;

    // --- 带字典的 String 字段 ---
    // @Excel(name = "字段注释", width = 15, dicCode = "dict_code")
    // @Dict(dicCode = "dict_code")
    // private String fieldName;

    // --- 关联表字典的 String 字段 ---
    // @Excel(name = "字段注释", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
    // @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    // private String fieldName;

    // --- Integer 字段 ---
    // @Excel(name = "字段注释", width = 15)
    // private Integer fieldName;

    // --- BigDecimal 字段 ---
    // @Excel(name = "字段注释", width = 15)
    // private BigDecimal fieldName;

    // --- Date 字段 ---
    // @Excel(name = "字段注释", width = 15, format = "yyyy-MM-dd")
    // @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    // @DateTimeFormat(pattern = "yyyy-MM-dd")
    // private Date fieldName;

    // --- DateTime 字段 ---
    // @Excel(name = "字段注释", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    // @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private Date fieldName;

    // === 系统字段（仅在表中实际存在时才生成，不要盲目添加！） ===

    // --- 以下每个字段都需要检查表DDL中是否存在对应列，不存在则不生成 ---

    // 如果表有 create_by 列:
    // /**创建人*/
    // @Schema(description = "创建人")
    // private String createBy;

    // 如果表有 create_time 列:
    // /**创建日期*/
    // @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // @Schema(description = "创建日期")
    // private Date createTime;

    // 如果表有 update_by 列:
    // /**更新人*/
    // @Schema(description = "更新人")
    // private String updateBy;

    // 如果表有 update_time 列:
    // /**更新日期*/
    // @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // @Schema(description = "更新日期")
    // private Date updateTime;

    // 如果表有 sys_org_code 列:
    // /**所属部门*/
    // @Schema(description = "所属部门")
    // private String sysOrgCode;

    // 新建表时默认添加全部系统字段；已有表按实际DDL决定。
}
```

**字典注解规则：**
- 下拉/单选/多选/搜索框 + 字典编码: `@Dict(dicCode = "xxx")`
- 下拉/单选/多选/搜索框 + 字典表: `@Dict(dictTable = "tableName", dicText = "textField", dicCode = "codeField")`
- 用户选择: `@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")`
- 部门选择: `@Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")`

---

### A2. Controller.java

```java
package org.jeecg.modules.{{entityPackage}}.controller;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.{{entityPackage}}.entity.{{entityName}};
import org.jeecg.modules.{{entityPackage}}.service.I{{entityName}}Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * @Description: {{description}}
 * @Author: jeecg-boot
 * @Date: {{today}}
 * @Version: V1.0
 */
@Tag(name = "{{description}}")
@RestController
@RequestMapping("/{{entityPackagePath}}/{{entityName_uncap}}")
@Slf4j
public class {{entityName}}Controller extends JeecgController<{{entityName}}, I{{entityName}}Service> {
    @Autowired
    private I{{entityName}}Service {{entityName_uncap}}Service;

    /**
     * 分页列表查询
     */
    @Operation(summary = "{{description}}-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<{{entityName}}>> queryPageList({{entityName}} {{entityName_uncap}},
                                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest req) {
        QueryWrapper<{{entityName}}> queryWrapper = QueryGenerator.initQueryWrapper({{entityName_uncap}}, req.getParameterMap());
        Page<{{entityName}}> page = new Page<>(pageNo, pageSize);
        IPage<{{entityName}}> pageList = {{entityName_uncap}}Service.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     */
    @AutoLog(value = "{{description}}-添加")
    @Operation(summary = "{{description}}-添加")
    @RequiresPermissions("{{entityPackage}}:{{tableName}}:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody {{entityName}} {{entityName_uncap}}) {
        {{entityName_uncap}}Service.save({{entityName_uncap}});
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     */
    @AutoLog(value = "{{description}}-编辑")
    @Operation(summary = "{{description}}-编辑")
    @RequiresPermissions("{{entityPackage}}:{{tableName}}:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody {{entityName}} {{entityName_uncap}}) {
        {{entityName_uncap}}Service.updateById({{entityName_uncap}});
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     * 注意：参数类型需与Entity主键类型一致
     * - String主键: @RequestParam(name = "id", required = true) String id
     * - Integer主键: @RequestParam(name = "id", required = true) Integer id
     * - Long主键:    @RequestParam(name = "id", required = true) Long id
     */
    @AutoLog(value = "{{description}}-通过id删除")
    @Operation(summary = "{{description}}-通过id删除")
    @RequiresPermissions("{{entityPackage}}:{{tableName}}:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        {{entityName_uncap}}Service.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     * 注意：当主键为 Integer/Long 时，需将 ids 转为对应类型的 List：
     * - Integer: Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(Collectors.toList())
     * - Long:    Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList())
     * - String:  Arrays.asList(ids.split(","))
     */
    @AutoLog(value = "{{description}}-批量删除")
    @Operation(summary = "{{description}}-批量删除")
    @RequiresPermissions("{{entityPackage}}:{{tableName}}:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.{{entityName_uncap}}Service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     * 注意：参数类型需与Entity主键类型一致（同 delete 方法）
     */
    @Operation(summary = "{{description}}-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<{{entityName}}> queryById(@RequestParam(name = "id", required = true) String id) {
        {{entityName}} {{entityName_uncap}} = {{entityName_uncap}}Service.getById(id);
        if ({{entityName_uncap}} == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK({{entityName_uncap}});
    }

    /**
     * 导出excel
     */
    @RequiresPermissions("{{entityPackage}}:{{tableName}}:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, {{entityName}} {{entityName_uncap}}) {
        return super.exportXls(request, {{entityName_uncap}}, {{entityName}}.class, "{{description}}");
    }

    /**
     * 通过excel导入数据
     */
    @RequiresPermissions("{{entityPackage}}:{{tableName}}:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, {{entityName}}.class);
    }
}
```

**注意：** 如果查询字段中有下拉/单选/多选/复选框类型的，需要添加自定义查询规则：
```java
// 在 queryPageList 方法中：
Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
customeRuleMap.put("status", QueryRuleEnum.LIKE_WITH_OR);
QueryWrapper<{{entityName}}> queryWrapper = QueryGenerator.initQueryWrapper({{entityName_uncap}}, req.getParameterMap(), customeRuleMap);
```

---

### A3. IService.java

```java
package org.jeecg.modules.{{entityPackage}}.service;

import org.jeecg.modules.{{entityPackage}}.entity.{{entityName}};
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: {{description}}
 * @Author: jeecg-boot
 * @Date: {{today}}
 * @Version: V1.0
 */
public interface I{{entityName}}Service extends IService<{{entityName}}> {
}
```

---

### A4. ServiceImpl.java

```java
package org.jeecg.modules.{{entityPackage}}.service.impl;

import org.jeecg.modules.{{entityPackage}}.entity.{{entityName}};
import org.jeecg.modules.{{entityPackage}}.mapper.{{entityName}}Mapper;
import org.jeecg.modules.{{entityPackage}}.service.I{{entityName}}Service;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: {{description}}
 * @Author: jeecg-boot
 * @Date: {{today}}
 * @Version: V1.0
 */
@Service
public class {{entityName}}ServiceImpl extends ServiceImpl<{{entityName}}Mapper, {{entityName}}> implements I{{entityName}}Service {
}
```

---

### A5. Mapper.java

```java
package org.jeecg.modules.{{entityPackage}}.mapper;

import org.jeecg.modules.{{entityPackage}}.entity.{{entityName}};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: {{description}}
 * @Author: jeecg-boot
 * @Date: {{today}}
 * @Version: V1.0
 */
public interface {{entityName}}Mapper extends BaseMapper<{{entityName}}> {
}
```

---

### A6. Mapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.{{entityPackage}}.mapper.{{entityName}}Mapper">
</mapper>
```

---

### A7. API文件 (vue3 和 vue3Native 通用)

```typescript
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  list = '/{{entityPackagePath}}/{{entityName_uncap}}/list',
  save = '/{{entityPackagePath}}/{{entityName_uncap}}/add',
  edit = '/{{entityPackagePath}}/{{entityName_uncap}}/edit',
  deleteOne = '/{{entityPackagePath}}/{{entityName_uncap}}/delete',
  deleteBatch = '/{{entityPackagePath}}/{{entityName_uncap}}/deleteBatch',
  importExcel = '/{{entityPackagePath}}/{{entityName_uncap}}/importExcel',
  exportXls = '/{{entityPackagePath}}/{{entityName_uncap}}/exportXls',
}

/**
 * 导出api
 */
export const getExportUrl = Api.exportXls;

/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;

/**
 * 列表接口
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 删除单个
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量删除
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * 保存或者更新
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
```

**vue3Native 单表风格差异：** `saveOrUpdate` 使用 `{ isTransformResponse: false }` 选项（Modal 中手动判断 `res.success`）：
```typescript
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params }, { isTransformResponse: false });
};
```

**vue3Native 一对多风格差异：** `saveOrUpdate` **不用** `isTransformResponse: false`（Form 中直接 `await saveOrUpdate(values, isUpdate)` 即可）。子表查询导出为**函数**，需额外导出 `queryDataById`。详见 **C13 节**。

---

### A8. Data文件 - vue3 封装风格

```typescript
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';

// 列表列定义
export const columns: BasicColumn[] = [
  // --- 普通字符串列 ---
  // {
  //   title: '字段名称',
  //   align: 'center',
  //   dataIndex: 'fieldName',
  // },

  // --- 日期列（截取前10位） ---
  // {
  //   title: '日期',
  //   align: 'center',
  //   dataIndex: 'dateField',
  //   customRender: ({ text }) => {
  //     text = !text ? '' : (text.length > 10 ? text.substr(0, 10) : text);
  //     return text;
  //   },
  // },

  // --- 字典翻译列（自动渲染_dictText后缀） ---
  // {
  //   title: '状态',
  //   align: 'center',
  //   dataIndex: 'status_dictText',
  // },

  // --- Switch 列 ---
  // {
  //   title: '是否启用',
  //   align: 'center',
  //   dataIndex: 'enabled',
  //   customRender: ({ text }) => {
  //     return render.renderSwitch(text, [{ text: '是', value: 'Y' }, { text: '否', value: 'N' }]);
  //   },
  // },

  // --- 图片列 ---
  // {
  //   title: '图片',
  //   align: 'center',
  //   dataIndex: 'imageField',
  //   customRender: render.renderImage,
  // },

  // --- 分类树列 ---
  // {
  //   title: '分类',
  //   align: 'center',
  //   dataIndex: 'categoryField',
  //   customRender: ({ text }) => {
  //     return render.renderCategoryTree(text, 'categoryDictCode');
  //   },
  // },
];

// 查询表单 Schema
export const searchFormSchema: FormSchema[] = [
  // --- 文本查询 ---
  // {
  //   label: '名称',
  //   field: 'name',
  //   component: 'JInput',
  //   colProps: { span: 6 },
  // },

  // --- 字典下拉查询 ---
  // {
  //   label: '状态',
  //   field: 'status',
  //   component: 'JDictSelectTag',
  //   componentProps: { dictCode: 'dict_code' },
  //   colProps: { span: 6 },
  // },

  // --- 日期范围查询 ---
  // {
  //   label: '创建日期',
  //   field: 'createTime',
  //   component: 'RangePicker',
  //   componentProps: { showTime: true },
  //   colProps: { span: 6 },
  // },
];

// 编辑表单 Schema
export const formSchema: FormSchema[] = [
  // 隐藏ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },

  // --- 文本输入 ---
  // {
  //   label: '名称',
  //   field: 'name',
  //   required: true,
  //   component: 'Input',
  //   componentProps: { placeholder: '请输入名称' },
  // },

  // --- 数字输入 ---
  // {
  //   label: '数量',
  //   field: 'quantity',
  //   component: 'InputNumber',
  //   componentProps: { placeholder: '请输入数量' },
  // },

  // --- 字典下拉 ---
  // {
  //   label: '状态',
  //   field: 'status',
  //   component: 'JDictSelectTag',
  //   componentProps: { dictCode: 'dict_code', placeholder: '请选择状态' },
  // },

  // --- 关联表字典下拉 ---
  // {
  //   label: '类型',
  //   field: 'type',
  //   component: 'JDictSelectTag',
  //   componentProps: { dictCode: 'tableName,textField,codeField', placeholder: '请选择类型' },
  // },

  // --- Switch ---
  // {
  //   label: '是否启用',
  //   field: 'enabled',
  //   component: 'JSwitch',
  //   componentProps: { options: ['Y', 'N'] },
  // },

  // --- 日期选择 ---
  // {
  //   label: '日期',
  //   field: 'dateField',
  //   component: 'DatePicker',
  //   componentProps: { showTime: false, valueFormat: 'YYYY-MM-DD', placeholder: '请选择日期' },
  // },

  // --- 日期时间选择 ---
  // {
  //   label: '日期时间',
  //   field: 'datetimeField',
  //   component: 'DatePicker',
  //   componentProps: { showTime: true, valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: '请选择日期时间' },
  // },

  // --- 文本域 ---
  // {
  //   label: '备注',
  //   field: 'remark',
  //   component: 'InputTextArea',
  //   componentProps: { placeholder: '请输入备注' },
  // },

  // --- 富文本编辑器 ---
  // {
  //   label: '内容',
  //   field: 'content',
  //   component: 'JEditor',
  // },

  // --- 图片上传 ---
  // {
  //   label: '图片',
  //   field: 'imageField',
  //   component: 'JImageUpload',
  // },

  // --- 文件上传 ---
  // {
  //   label: '附件',
  //   field: 'fileField',
  //   component: 'JUpload',
  // },

  // --- 用户选择 ---
  // {
  //   label: '负责人',
  //   field: 'userId',
  //   component: 'JSelectUserByDept',
  //   componentProps: { labelKey: 'realname' },
  // },

  // --- 部门选择 ---
  // {
  //   label: '部门',
  //   field: 'deptId',
  //   component: 'JSelectDept',
  // },

  // --- 分类树选择 ---
  // {
  //   label: '分类',
  //   field: 'categoryField',
  //   component: 'JCategorySelect',
  //   componentProps: { pcode: 'categoryDictCode' },
  // },

  // --- 搜索选择 ---
  // {
  //   label: '搜索',
  //   field: 'searchField',
  //   component: 'JSearchSelect',
  //   componentProps: { dict: 'tableName,textField,codeField', placeholder: '请选择' },
  // },
];

// 高级查询配置
export const superQuerySchema = {
  // fieldName: { title: '字段名', order: 0, view: 'text' },
  // status: { title: '状态', order: 1, view: 'list', dictCode: 'dict_code' },
  // dateField: { title: '日期', order: 2, view: 'date' },
  // datetimeField: { title: '日期时间', order: 3, view: 'datetime' },
  // quantity: { title: '数量', order: 4, view: 'number' },
};
```

**高级查询 view 类型映射：**
- string → `text`
- int/double/BigDecimal → `number`
- date → `date`
- datetime → `datetime`
- 字典字段(list/radio/checkbox) → `list`, 带 `dictCode`
- 关联表字典 → `list_multi` 或 `sel_search`, 带 `dictTable/dictCode/dictText`
- switch → `radio`
- user_select → `sel_user`
- dept_select → `sel_depart`

---

### A9. List页面 - vue3 封装风格

```vue
<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'{{entityPackage}}:{{tableName}}:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
        <!-- 富文本回显 -->
        <!-- <template v-if="column.dataIndex==='content'">
          <div v-html="text"></div>
        </template> -->
        <!-- 文件下载 -->
        <!-- <template v-if="column.dataIndex==='fileField'">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无文件</span>
          <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download-outlined" size="small" @click="downloadFile(text)">下载</a-button>
        </template> -->
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <{{entityName}}Modal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="{{entityPackage}}-{{entityName_uncap}}" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import {{entityName}}Modal from './components/{{entityName}}Modal.vue';
  import { columns, searchFormSchema, superQuerySchema } from './{{entityName}}.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './{{entityName}}.api';
  import { downloadFile } from '/@/utils/common/renderUtils';

  const queryParam = reactive<any>({});
  const checkedKeys = ref<Array<string | number>>([]);
  // 注册 modal
  const [registerModal, { openModal }] = useModal();
  // 注册 table
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '{{description}}',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [
          // 数字/时间范围查询映射
          // ['fieldName', ['fieldName_begin', 'fieldName_end']],
        ],
        fieldMapToTime: [
          // 日期范围查询映射
          // ['dateField', ['dateField_begin', 'dateField_end'], 'YYYY-MM-DD'],
          // ['datetimeField', ['datetimeField_begin', 'datetimeField_end'], 'YYYY-MM-DD HH:mm:ss'],
        ],
      },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: '{{description}}',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  // 高级查询配置
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * 高级查询事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    reload();
  }

  /**
   * 新增事件
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: '{{entityPackage}}:{{tableName}}:edit',
      },
    ];
  }

  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: '{{entityPackage}}:{{tableName}}:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker-range) {
    width: 100%;
  }
</style>
```

---

### A10. Modal组件 - vue3 封装风格

```vue
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="{{entityName}}Form" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../{{entityName}}.data';
  import { saveOrUpdate } from '../{{entityName}}.api';

  // Emits声明
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const isDetail = ref(false);

  // 表单配置
  const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
    // 单列布局用 labelWidth: 150
    labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
    // 多列布局用 baseColProps: { span: 12 } (双列) 或 { span: 8 } (三列)
    baseColProps: { span: 24 },
  });

  // 表单赋值
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // 重置表单
    await resetFields();
    setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
    isUpdate.value = !!data?.isUpdate;
    isDetail.value = !!data?.showFooter;
    if (unref(isUpdate)) {
      // 表单赋值
      await setFieldsValue({
        ...data.record,
      });
    }
    // 隐藏底部时禁用整个表单
    setProps({ disabled: !data?.showFooter });
  });

  // 设置标题
  const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));

  // 表单提交事件
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      // 提交表单
      await saveOrUpdate(values, isUpdate.value);
      // 关闭弹窗
      closeModal();
      // 刷新列表
      emit('success');
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) {
    width: 100%;
  }
  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
```

**宽度规则：**
- 单列表单(fieldRowNum=1): width=800, baseColProps={span:24}
- 双列表单(fieldRowNum=2): width=1000, baseColProps={span:12}
- 三列表单(fieldRowNum=3): width=1200, baseColProps={span:8}
- 四列表单(fieldRowNum=4): width=1280, baseColProps={span:6}

---

### A8N. Data文件 - vue3Native 原生风格

vue3Native 的 data.ts 只包含 columns 和 superQuerySchema，不包含 formSchema（表单在模板中直接写控件）。

```typescript
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';

// 列表列定义（与 vue3 封装风格完全相同）
export const columns: BasicColumn[] = [
  // ... 同 A8 的 columns
];

// 高级查询配置（与 vue3 封装风格完全相同）
export const superQuerySchema = {
  // ... 同 A8 的 superQuerySchema
};
```

---

### A9N. List页面 - vue3Native 原生风格

```vue
<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'{{entityPackage}}:{{tableName}}:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽（同 vue3 封装风格）-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <{{entityName}}Modal ref="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="{{entityPackage}}-{{entityName_uncap}}" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import {{entityName}}Modal from './components/{{entityName}}Modal.vue';
  import { columns, superQuerySchema } from './{{entityName}}.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './{{entityName}}.api';
  import { downloadFile } from '/@/utils/common/renderUtils';

  const queryParam = reactive<any>({});
  const registerModal = ref();

  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '{{description}}',
      api: list,
      columns,
      canResize: true,
      useSearchForm: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: '{{description}}',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  // 高级查询配置
  const superQueryConfig = reactive(superQuerySchema);

  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    reload();
  }

  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }

  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }

  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
  }

  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }

  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: '{{entityPackage}}:{{tableName}}:edit',
      },
    ];
  }

  function getDropDownAction(record) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: '{{entityPackage}}:{{tableName}}:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker-range) {
    width: 100%;
  }
</style>
```

---

### A10N. Modal组件 - vue3Native 原生风格

```vue
<template>
  <j-modal :title="title" :width="800" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="关闭">
    <{{entityName}}Form ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false" />
    <template #footer>
      <a-button @click="handleCancel">取消</a-button>
      <a-button :class="{ 'jee-hidden': disableSubmit }" type="primary" @click="handleOk">确认</a-button>
    </template>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import {{entityName}}Form from './{{entityName}}Form.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';

  const title = ref<string>('');
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const emit = defineEmits(['register', 'success']);

  /**
   * 新增
   */
  function add() {
    title.value = '新增';
    visible.value = true;
    nextTick(() => {
      registerForm.value.add();
    });
  }

  /**
   * 编辑
   */
  function edit(record) {
    title.value = disableSubmit.value ? '详情' : '编辑';
    visible.value = true;
    nextTick(() => {
      registerForm.value.edit(record);
    });
  }

  /**
   * 确定按钮点击事件
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * form保存回调事件
   */
  function submitCallback() {
    handleCancel();
    emit('success');
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  defineExpose({
    add,
    edit,
    disableSubmit,
  });
</script>

<style lang="less">
  .jee-hidden {
    display: none !important;
  }
</style>
```

---

### A11N. Form组件 - vue3Native 原生风格

```vue
<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="{{entityName}}Form">
          <a-row>
            <!-- 每个字段生成一个 a-col + a-form-item -->

            <!-- === 文本输入 === -->
            <!-- <a-col :span="24">
              <a-form-item label="名称" v-bind="validateInfos.name" id="{{entityName}}Form-name" name="name">
                <a-input v-model:value="formData.name" placeholder="请输入名称" allow-clear />
              </a-form-item>
            </a-col> -->

            <!-- === 数字输入 === -->
            <!-- <a-col :span="24">
              <a-form-item label="数量" v-bind="validateInfos.quantity" id="{{entityName}}Form-quantity" name="quantity">
                <a-input-number v-model:value="formData.quantity" placeholder="请输入数量" style="width: 100%" />
              </a-form-item>
            </a-col> -->

            <!-- === 字典下拉 === -->
            <!-- <a-col :span="24">
              <a-form-item label="状态" v-bind="validateInfos.status" id="{{entityName}}Form-status" name="status">
                <JDictSelectTag v-model:value="formData.status" dictCode="dict_code" placeholder="请选择状态" />
              </a-form-item>
            </a-col> -->

            <!-- === Switch === -->
            <!-- <a-col :span="24">
              <a-form-item label="是否启用" v-bind="validateInfos.enabled" id="{{entityName}}Form-enabled" name="enabled">
                <a-switch v-model:checked="formData.enabled" checkedValue="Y" unCheckedValue="N" />
              </a-form-item>
            </a-col> -->

            <!-- === 日期选择 === -->
            <!-- <a-col :span="24">
              <a-form-item label="日期" v-bind="validateInfos.dateField" id="{{entityName}}Form-dateField" name="dateField">
                <a-date-picker v-model:value="formData.dateField" placeholder="请选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </a-form-item>
            </a-col> -->

            <!-- === 日期时间选择 === -->
            <!-- <a-col :span="24">
              <a-form-item label="日期时间" v-bind="validateInfos.datetimeField" id="{{entityName}}Form-datetimeField" name="datetimeField">
                <a-date-picker v-model:value="formData.datetimeField" placeholder="请选择日期时间" :showTime="true" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
              </a-form-item>
            </a-col> -->

            <!-- === 文本域 === -->
            <!-- <a-col :span="24">
              <a-form-item label="备注" v-bind="validateInfos.remark" id="{{entityName}}Form-remark" name="remark">
                <a-textarea v-model:value="formData.remark" placeholder="请输入备注" :rows="4" />
              </a-form-item>
            </a-col> -->

            <!-- === 图片上传 === -->
            <!-- <a-col :span="24">
              <a-form-item label="图片" v-bind="validateInfos.imageField" id="{{entityName}}Form-imageField" name="imageField">
                <JImageUpload v-model:value="formData.imageField" />
              </a-form-item>
            </a-col> -->

            <!-- === 文件上传 === -->
            <!-- <a-col :span="24">
              <a-form-item label="附件" v-bind="validateInfos.fileField" id="{{entityName}}Form-fileField" name="fileField">
                <JUpload v-model:value="formData.fileField" />
              </a-form-item>
            </a-col> -->

            <!-- === 富文本 === -->
            <!-- <a-col :span="24">
              <a-form-item label="内容" v-bind="validateInfos.content" id="{{entityName}}Form-content" name="content">
                <JEditor v-model:value="formData.content" />
              </a-form-item>
            </a-col> -->

            <!-- === 用户选择 === -->
            <!-- <a-col :span="24">
              <a-form-item label="负责人" v-bind="validateInfos.userId" id="{{entityName}}Form-userId" name="userId">
                <JSelectUserByDept v-model:value="formData.userId" />
              </a-form-item>
            </a-col> -->

            <!-- === 部门选择 === -->
            <!-- <a-col :span="24">
              <a-form-item label="部门" v-bind="validateInfos.deptId" id="{{entityName}}Form-deptId" name="deptId">
                <JSelectDept v-model:value="formData.deptId" />
              </a-form-item>
            </a-col> -->

            <!-- === 搜索选择 === -->
            <!-- <a-col :span="24">
              <a-form-item label="搜索" v-bind="validateInfos.searchField" id="{{entityName}}Form-searchField" name="searchField">
                <JSearchSelect v-model:value="formData.searchField" dict="tableName,textField,codeField" placeholder="请选择" />
              </a-form-item>
            </a-col> -->
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../{{entityName}}.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  // 按需导入组件（根据实际用到的字段类型）
  // import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  // import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  // import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUpload.vue';
  // import JUpload from '/@/components/Form/src/jeecg/components/JUpload.vue';
  // import JEditor from '/@/components/Form/src/jeecg/components/JEditor.vue';
  // import JSelectUserByDept from '/@/components/Form/src/jeecg/components/JSelectUserByDept.vue';
  // import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({}) },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    // 业务字段初始值
    // name: '',
    // status: '',
    // ... 所有字段默认值
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);

  // 表单验证规则
  const validatorRules = reactive({
    // name: [{ required: true, message: '请输入名称!' }],
    // ... 必填字段的验证规则
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      if (props.formData.disabled === false) {
        return false;
      } else {
        return true;
      }
    }
    return props.formDisabled;
  });

  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if (record.hasOwnProperty(key)) {
          tmpData[key] = record[key];
        }
      });
      // 赋值
      Object.assign(formData, tmpData);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      await validate();
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    }
    confirmLoading.value = true;
    const isUpdate = ref<boolean>(false);
    let model = formData;
    if (model.id) {
      isUpdate.value = true;
    }
    // 处理数组类型字段（多选等）
    for (let data in model) {
      if (model[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        if (valueType === 'string') {
          model[data] = model[data].join(',');
        }
      }
    }
    await saveOrUpdate(model, isUpdate.value)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 14px;
  }
</style>
```

**vue3Native 多列布局：**
- 单列: `<a-col :span="24">`
- 双列: `<a-col :span="12">`
- 三列: `<a-col :span="8">`

---

### A12. 菜单权限 SQL

```sql
-- 注意：该页面对应的前台目录为 views/{{viewDir}} 文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值

-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('{{timestamp}}01', NULL, '{{description}}', '/{{entityPackagePath}}/{{entityName_uncap}}List', '{{viewDir}}/{{entityName}}List', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '{{today}} 00:00:00', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('{{timestamp}}02', '{{timestamp}}01', '添加{{description}}', NULL, NULL, 0, NULL, NULL, 2, '{{entityPackage}}:{{tableName}}:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '{{today}} 00:00:00', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('{{timestamp}}03', '{{timestamp}}01', '编辑{{description}}', NULL, NULL, 0, NULL, NULL, 2, '{{entityPackage}}:{{tableName}}:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '{{today}} 00:00:00', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('{{timestamp}}04', '{{timestamp}}01', '删除{{description}}', NULL, NULL, 0, NULL, NULL, 2, '{{entityPackage}}:{{tableName}}:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '{{today}} 00:00:00', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('{{timestamp}}05', '{{timestamp}}01', '批量删除{{description}}', NULL, NULL, 0, NULL, NULL, 2, '{{entityPackage}}:{{tableName}}:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '{{today}} 00:00:00', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('{{timestamp}}06', '{{timestamp}}01', '导出excel_{{description}}', NULL, NULL, 0, NULL, NULL, 2, '{{entityPackage}}:{{tableName}}:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '{{today}} 00:00:00', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('{{timestamp}}07', '{{timestamp}}01', '导入excel_{{description}}', NULL, NULL, 0, NULL, NULL, 2, '{{entityPackage}}:{{tableName}}:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '{{today}} 00:00:00', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（admin角色）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}08', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}01', NULL, '{{today}} 00:00:00', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}09', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}02', NULL, '{{today}} 00:00:00', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}10', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}03', NULL, '{{today}} 00:00:00', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}11', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}04', NULL, '{{today}} 00:00:00', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}12', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}05', NULL, '{{today}} 00:00:00', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}13', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}06', NULL, '{{today}} 00:00:00', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('{{timestamp}}14', 'f6817f48af4fb3af11b9e8bf182f618b', '{{timestamp}}07', NULL, '{{today}} 00:00:00', '127.0.0.1');
```

---
