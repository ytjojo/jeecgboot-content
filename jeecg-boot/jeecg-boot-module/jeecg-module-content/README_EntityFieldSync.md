# 实体类与数据库表字段同步检测工具

## 概述

本工具用于检测Java实体类（Entity）与数据库表字段之间的同步性，确保代码与数据库结构保持一致。

## 工具文件

### 1. EntityFieldSyncTest.java
- **功能**: 基于JUnit的完整测试类，支持数据库连接检测
- **特点**: 
  - 连接真实数据库进行字段对比
  - 支持MyBatis-Plus注解解析
  - 提供详细的差异报告
- **使用场景**: 集成测试环境，需要数据库连接

### 2. SimpleEntityFieldChecker.java  
- **功能**: 独立运行的简化检测工具
- **特点**:
  - 无需数据库连接
  - 仅分析实体类字段定义
  - 快速验证字段映射配置
- **使用场景**: 开发阶段快速检查，CI/CD流水线

## 使用方法

### 方法一：运行简化检测工具（推荐）

```bash
# 进入模块目录
cd jeecg-boot-module/jeecg-module-content

# 编译检测工具
javac -cp "$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout):target/classes" \
  src/test/java/org/jeecg/modules/content/entity/SimpleEntityFieldChecker.java \
  -d target/test-classes

# 运行检测
java -cp "$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout):target/classes:target/test-classes" \
  org.jeecg.modules.content.entity.SimpleEntityFieldChecker
```

### 方法二：使用JUnit测试（需要数据库）

```bash
# 确保数据库配置正确
# 运行测试
mvn test -Dtest=EntityFieldSyncTest -DskipTests=false
```

## 检测结果说明

### 输出信息包含：

1. **基本信息**
   - 实体类名称
   - 对应的数据库表名
   - 字段统计信息

2. **字段列表**
   - 映射到数据库的字段
   - 排除的字段（@TableField(exist = false)）

3. **字段映射详情**
   - 字段名 -> 数据库字段名
   - 自定义映射标识
   - 字段类型信息

### 示例输出：

```
=== ContentEntity 字段分析 ===
对应表名: contents

字段统计:
  - 总字段数: 45
  - 映射到数据库的字段数: 43
  - 排除的字段数: 2

映射到数据库的字段列表:
  - acceptedAt
  - address
  - authorId
  - categoryId
  - commentCount
  ...

排除的字段列表 (@TableField(exist = false)):
  - authorAvatar
  - authorName

字段映射详情:
  title (String) -> title
  content (String) -> content
  authorId (String) -> author_id [自定义映射]
  categoryId (String) -> category_id [自定义映射]
  ...
  authorName (String) -> [排除字段]
  authorAvatar (String) -> [排除字段]
```

## 支持的注解

### @TableName
- 指定实体类对应的数据库表名
- 如果未指定，使用类名转下划线格式

### @TableField
- `value`: 指定字段对应的数据库列名
- `exist = false`: 标记字段不存在于数据库中

## 字段命名转换规则

### 驼峰转下划线
- `authorId` -> `author_id`
- `categoryId` -> `category_id`
- `createTime` -> `create_time`

### 下划线转驼峰
- `author_id` -> `authorId`
- `category_id` -> `categoryId`
- `create_time` -> `createTime`

## 常见问题

### 1. 编译错误
**问题**: 找不到依赖类
**解决**: 确保先执行 `mvn compile` 编译项目

### 2. 字段不匹配
**问题**: 实体类字段与数据库字段不一致
**解决**: 
- 检查 `@TableField` 注解配置
- 确认数据库表结构是否最新
- 验证字段命名转换规则

### 3. 排除字段处理
**问题**: 某些字段不应该映射到数据库
**解决**: 使用 `@TableField(exist = false)` 注解

## 扩展使用

### 检测其他实体类

修改 `SimpleEntityFieldChecker.java` 中的 `main` 方法：

```java
public static void main(String[] args) {
    SimpleEntityFieldChecker checker = new SimpleEntityFieldChecker();
    
    // 检测多个实体类
    checker.analyzeEntity(ContentEntity.class);
    checker.analyzeEntity(ChannelEntity.class);
    checker.analyzeEntity(CategoryEntity.class);
}
```

### 批量检测

创建批量检测脚本：

```java
// 获取包下所有Entity类
Reflections reflections = new Reflections("org.jeecg.modules");
Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(TableName.class);

for (Class<?> entityClass : entityClasses) {
    checker.analyzeEntity(entityClass);
}
```

## 最佳实践

1. **开发阶段**: 使用 `SimpleEntityFieldChecker` 快速验证
2. **集成测试**: 使用 `EntityFieldSyncTest` 连接数据库验证
3. **CI/CD**: 集成到构建流程中自动检测
4. **定期检查**: 在数据库结构变更后及时检测

## 注意事项

1. 确保实体类继承关系正确处理
2. 注意静态字段和 `serialVersionUID` 会被自动排除
3. 自定义字段映射需要正确配置 `@TableField` 注解
4. 数据库连接检测需要正确的数据源配置




cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/jeecg-boot-module/jeecg-module-content && java -cp "$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout):target/classes:target/test-classes" org.jeecg.modules.content.entity.EntityFieldSyncTestRunner 