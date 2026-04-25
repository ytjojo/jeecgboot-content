package org.jeecg.modules.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体类与数据库表字段同步检测运行器
 * 独立运行的测试工具，不依赖JUnit框架
 * 
 * @author jeecg-boot
 * @date 2024-12-19
 */
@SpringBootApplication(scanBasePackages = "org.jeecg")
public class EntityFieldSyncTestRunner {
    


    /***
     * 
     * 
     * cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/jeecg-boot-module/jeecg-module-content && java -cp "$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout):target/classes:target/test-classes" org.jeecg.modules.content.entity.EntityFieldSyncTestRunner 
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("=== Entity字段同步检测工具 ===");
        
        EntityFieldSyncTestRunner runner = new EntityFieldSyncTestRunner();
        
        // 直接运行字段同步检测，不启动Spring Boot应用
        runner.runAllPackageFieldSyncTest();
        
        System.out.println("\n=== 检测完成 ===");
    }
    
    public void runAllPackageFieldSyncTest() {
        runPackageFieldSyncTest("org.jeecg.modules.content.entity");
        runPackageFieldSyncTest("org.jeecg.modules.channel.entity");
        runPackageFieldSyncTest("org.jeecg.modules.community.entity");
    }

    /**
     * 运行包下所有实体类的字段同步检测
     * @param packageName
     */
    public void runPackageFieldSyncTest(String packageName) {
        Set<Class<?>> entityClasses = getEntityClasses(packageName);
        for (Class<?> entityClass : entityClasses) {
            runFieldSyncTest(entityClass);
        }
    }

    /**
     * 获取包下所有实体类class
     * 要判断是否有@TableName注解
     * @param packageName
     * @return
     */
    public Set<Class<?>> getEntityClasses(String packageName) {
        Set<Class<?>> entityClasses = new HashSet<>();

        // 扫描包下所有类
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        try (java.util.Scanner scanner = new java.util.Scanner(classLoader.getResourceAsStream(path))) {
            while (scanner.hasNext()) {
                String className = scanner.next();
                if (className.endsWith(".class")) {
                    className = className.substring(0, className.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(packageName + '.' + className);
                        if (clazz.isAnnotationPresent(TableName.class)) {
                            entityClasses.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("加载类失败: " + className);
                    }
                }
            }
        }
        
        // 添加其他实体类
        return entityClasses;
    }
    /**
     * 运行字段同步检测
     */
    public void runFieldSyncTest(Class<?> entityClass) {
        try {
            // 检测ContentEntity与contents表的字段同步
            checkEntityFieldSync(entityClass, getTableName(entityClass));
            
        } catch (Exception e) {
            System.err.println("字段同步检测失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

       /**
     * 获取表名
     */
    private String getTableName(Class<?> entityClass) {
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        if (tableNameAnnotation != null && !tableNameAnnotation.value().isEmpty()) {
            return tableNameAnnotation.value();
        }
        return convertToUnderscoreCase(entityClass.getSimpleName());
    }
    
    /**
     * 测试实体类与数据库表字段同步性
     * @param entityClass 实体类
     * @param tableName 表名（可选，如果为null则从实体类注解获取）
     */
    public void checkEntityFieldSync(Class<?> entityClass, String tableName) {
        System.out.println("\n--- 检测 " + entityClass.getSimpleName() + " 字段同步 ---");
        
        // 获取实际表名
        String actualTableName = tableName;
        if (actualTableName == null) {
            TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
            if (tableNameAnnotation != null) {
                actualTableName = tableNameAnnotation.value();
            } else {
                actualTableName = convertToSnakeCase(entityClass.getSimpleName());
            }
        }
        
        // 获取数据库表字段
        Set<String> dbFields = getDatabaseFields(actualTableName);
        
        // 获取Entity字段
        Set<String> entityFields = getEntityFields(entityClass);
        
        // 比较字段差异
        Set<String> onlyInDb = new HashSet<>(dbFields);
        onlyInDb.removeAll(entityFields);
        
        Set<String> onlyInEntity = new HashSet<>(entityFields);
        onlyInEntity.removeAll(dbFields);
        
        // 输出结果
        System.out.println("表名: " + actualTableName);
        System.out.println("数据库字段数量: " + dbFields.size());
        System.out.println("Entity字段数量: " + entityFields.size());
        
        if (onlyInDb.isEmpty() && onlyInEntity.isEmpty()) {
            System.out.println("✅ 字段同步检测通过！所有字段都匹配。");
        } else {
            System.out.println("❌ 发现字段不匹配:");
            
            if (!onlyInDb.isEmpty()) {
                System.out.println("仅在数据库中存在的字段 (" + onlyInDb.size() + "个):");
                onlyInDb.forEach(field -> System.out.println("  - " + field));
            }
            
            if (!onlyInEntity.isEmpty()) {
                System.out.println("仅在Entity中存在的字段 (" + onlyInEntity.size() + "个):");
                onlyInEntity.forEach(field -> System.out.println("  - " + field));
            }
        }
    }
    
    /**
     * 获取数据库字段信息
     * @param tableName 表名
     * @return 字段名集合
     */
    private Set<String> getDatabaseFields(String tableName) {
        Set<String> fields = new HashSet<>();
        
        // 数据库连接配置 - 请根据实际情况修改
        String dbUrl = "jdbc:mysql://localhost:3306/jeecg-boot?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8";
        String username = "root";
        String password = "root";
        String driver = "com.mysql.cj.jdbc.Driver";
        
        try {
            // 加载MySQL驱动
            Class.forName(driver);
            
            // 建立数据库连接
            try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                // 获取表的字段信息
                try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        // 将数据库字段名转换为驼峰命名
                        String camelCaseName = convertToCamelCase(columnName);
                        fields.add(camelCaseName);
                    }
                }
                
                System.out.println("从数据库表 " + tableName + " 获取到 " + fields.size() + " 个字段");
                
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            System.err.println("请检查数据库连接配置是否正确");
        }
        
        return fields;
    }
    
    /**
     * 通过反射获取Entity类字段信息
     */
    private Set<String> getEntityFields(Class<?> entityClass) {
        Set<String> fields = new HashSet<>();
        
        // 获取当前类及父类的所有字段
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            
            for (Field field : declaredFields) {
                // 跳过静态字段和序列化字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
                    "serialVersionUID".equals(field.getName())) {
                    continue;
                }
                
                // 检查是否有@TableField(exist = false)注解
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField != null && !tableField.exist()) {
                    continue; // 跳过不存在于数据库的字段
                }
                
                // 获取数据库字段名
                String dbFieldName = getDbFieldName(field);
                fields.add(dbFieldName);
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }
    
    /**
     * 获取字段对应的数据库字段名
     */
    private String getDbFieldName(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        
        if (tableField != null && !tableField.value().isEmpty()) {
            // 如果有@TableField注解且指定了value，使用注解值
            return convertToCamelCase(tableField.value());
        } else {
            // 否则使用字段名
            return field.getName();
        }
    }
    
    /**
     * 将下划线命名转换为驼峰命名
     */
    private String convertToCamelCase(String underscoreName) {
        if (underscoreName == null || underscoreName.isEmpty()) {
            return underscoreName;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : underscoreName.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * 将驼峰命名转换为下划线命名
     */
    private String convertToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

     /**
     * 将驼峰命名转换为下划线命名
     */
    private String convertToUnderscoreCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }
        
        return result.toString();
    }









   
    
    /**
     * 获取映射到数据库的字段
     */
    private Set<String> getDbMappedFields(Class<?> entityClass) {
        Set<String> fields = new HashSet<>();
        
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            
            for (Field field : declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
                    "serialVersionUID".equals(field.getName())) {
                    continue;
                }
                
                // 检查是否排除
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField != null && !tableField.exist()) {
                    continue;
                }
                
                String dbFieldName = getDbFieldName(field);
                fields.add(dbFieldName);
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }
    
    /**
     * 获取排除的字段
     */
    private Set<String> getExcludedFields(Class<?> entityClass) {
        Set<String> fields = new HashSet<>();
        
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            
            for (Field field : declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
                    "serialVersionUID".equals(field.getName())) {
                    continue;
                }
                
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField != null && !tableField.exist()) {
                    fields.add(field.getName());
                }
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }
    
    
}