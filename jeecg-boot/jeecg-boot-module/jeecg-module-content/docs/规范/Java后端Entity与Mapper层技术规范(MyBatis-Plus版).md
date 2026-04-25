# Java后端Entity与Mapper层技术规范 (MyBatis-Plus版)

## 1. 概述

本规范旨在统一JeecgBoot项目中基于MyBatis-Plus的Entity实体类和Mapper数据访问层的设计与实现，确保代码风格统一、可维护性强、性能优良。

## 2. Entity实体类规范

### 2.1 基本规范

1. **命名规范**：
   - 实体类名采用大驼峰命名法，以`Entity`结尾，如`UserEntity`
   - 字段名采用小驼峰命名法，如`userName`

2. **注解使用**：
   - 使用`@Data`、`@Accessors(chain = true)`简化代码
   - 使用`@TableName`指定表名
   - 使用`@TableId`指定主键字段
   - 使用`@TableField`指定普通字段，支持字段映射
   - 继承`JeecgEntity`基类获取通用字段（id, createBy, createTime, updateBy, updateTime等）

3. **示例**：
```java
@Data
@TableName("sys_user")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserEntity extends JeecgEntity {
    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 状态：1-启用 0-禁用
     */
    @TableField("status")
    private Integer status;
}
```

### 2.2 字段规范

1. **主键字段**：
   - 使用`@TableId(type = IdType.ASSIGN_ID)`注解
   - 类型为`String`，使用雪花算法生成唯一ID

2. **逻辑删除字段**：
   - 使用`@TableLogic`注解标记逻辑删除字段
   - 字段名通常为`delFlag`，0表示正常，1表示已删除

3. **字段映射**：
   - 当数据库字段名与实体类属性名不一致时，使用`@TableField("column_name")`指定映射关系
   - 忽略非数据库字段使用`@TableField(exist = false)`

## 3. Mapper数据访问层规范

### 3.1 基本规范

1. **命名规范**：
   - Mapper接口名以`Mapper`结尾，如`UserMapper`
   - 继承`BaseMapper<Entity>`获取通用CRUD方法

2. **注解使用**：
   - 使用`@Mapper`标记Mapper接口
   - 自定义查询方法使用`@Select`、`@Update`等注解

3. **示例**：
```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND del_flag = 0")
    UserEntity selectByUsername(@Param("username") String username);
}
```

### 3.2 方法规范

1. **通用方法**：
   - 继承`BaseMapper`获得17个通用方法，包括：
     - 插入：`insert`
     - 删除：`deleteById`、`deleteByMap`、`delete`等
     - 更新：`updateById`、`update`
     - 查询：`selectById`、`selectBatchIds`、`selectByMap`、`selectOne`、`selectCount`、`selectList`、`selectPage`

2. **自定义方法**：
   - 方法名应清晰表达查询意图
   - 使用`@Param`注解标记参数
   - 复杂查询可使用MyBatis的XML映射文件

## 4. 最佳实践

### 4.1 性能优化

1. **字段选择**：
   - 使用条件构造器的`.select()`方法指定查询字段，避免`SELECT *`
   - 只查询需要的字段，减少网络传输和内存消耗

2. **分页查询**：
   - 使用MyBatis-Plus分页插件进行分页查询
   - 避免在内存中进行分页

3. **批量操作**：
   - 使用批量插入、更新、删除方法提高性能
   - 如`insertBatchSomeColumn`、`updateBatchById`等

### 4.2 安全性

1. **SQL注入防护**：
   - 使用`#{}`参数绑定，避免`${}`直接拼接
   - 复杂查询使用条件构造器

2. **敏感信息处理**：
   - 密码等敏感信息不应在实体类中直接暴露
   - 查询时应过滤敏感字段

### 4.3 可维护性

1. **注释规范**：
   - 类、方法、字段应有清晰注释
   - 使用`@Schema(description = "description")`描述字段含义

2. **代码复用**：
   - 公共字段继承基类
   - 通用方法使用MyBatis-Plus提供的实现

## 5. 常见问题及解决方案

### 5.1 逻辑删除

1. **配置**：
   - 在`application.yml`中配置全局逻辑删除规则
   - 使用`@TableLogic`标记逻辑删除字段

2. **注意事项**：
   - 逻辑删除字段在查询时会自动加上删除条件
   - 物理删除需要使用`delete`方法并手动构造条件

### 5.2 字段填充

1. **自动填充**：
   - 使用MetaObjectHandler实现自动填充创建人、创建时间等字段
   - 配合`@TableField(fill = FieldFill.INSERT)`等注解使用

### 5.3 多数据源

1. **配置**：
   - 使用`dynamic-datasource-spring-boot-starter`实现多数据源
   - 通过`@DS`注解切换数据源

## 6. 总结

本规范基于MyBatis-Plus特性，结合项目实际需求制定。遵循本规范可以提高开发效率、保证代码质量、优化系统性能。开发人员应严格按照规范执行，确保项目代码风格统一、结构清晰、易于维护。