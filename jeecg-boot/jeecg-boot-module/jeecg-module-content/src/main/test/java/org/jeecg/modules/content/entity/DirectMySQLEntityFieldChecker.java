package org.jeecg.modules.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 直接连接MySQL数据库的实体类字段同步检测工具
 * 不依赖Spring Boot，直接使用JDBC连接数据库
 * 
 * @author jeecg-boot
 * @date 2024-12-19
 */
public class DirectMySQLEntityFieldChecker {
    
    // 数据库连接配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/jeecg-boot?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    public static void main(String[] args) {
        System.out.println("=== 直接连接MySQL数据库进行字段同步检测 ===");
        
        DirectMySQLEntityFieldChecker checker = new DirectMySQLEntityFieldChecker();
        
        try {
            // 测试ContentEntity与contents表字段同步性
            checker.testEntityFieldSync(ContentEntity.class, "contents");
            
            System.out.println("\n=== 字段同步检测完成 ===");
        } catch (Exception e) {
            System.err.println("检测执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 通用的Entity字段同步测试方法
     */
    public void testEntityFieldSync(Class<?> entityClass, String tableName) throws SQLException, ClassNotFoundException {
        // 获取表名注解
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        String actualTableName = tableName;
        if (tableNameAnnotation != null && !tableNameAnnotation.value().isEmpty()) {
            actualTableName = tableNameAnnotation.value();
        }
        
        // 获取数据库表字段
        Set<String> dbFields = getDatabaseFields(actualTableName);
        
        // 获取Entity字段
        Set<String> entityFields = getEntityFields(entityClass);
        
        // 对比差异
        Set<String> missingInEntity = new HashSet<>(dbFields);
        missingInEntity.removeAll(entityFields);
        
        Set<String> missingInDb = new HashSet<>(entityFields);
        missingInDb.removeAll(dbFields);
        
        // 输出详细信息
        System.out.println("\n=== " + entityClass.getSimpleName() + " 字段同步检测 ===");
        System.out.println("数据库连接: " + DB_URL);
        System.out.println("表名: " + actualTableName);
        System.out.println("数据库字段数量: " + dbFields.size());
        System.out.println("Entity字段数量: " + entityFields.size());
        
        System.out.println("\n数据库字段列表:");
        dbFields.stream().sorted().forEach(field -> System.out.println("  - " + field));
        
        System.out.println("\nEntity字段列表:");
        entityFields.stream().sorted().forEach(field -> System.out.println("  - " + field));
        
        if (!missingInEntity.isEmpty()) {
            System.out.println("\n❌ Entity中缺失的字段:");
            missingInEntity.stream().sorted().forEach(field -> System.out.println("  - " + field));
        }
        
        if (!missingInDb.isEmpty()) {
            System.out.println("\n❌ 数据库中缺失的字段:");
            missingInDb.stream().sorted().forEach(field -> System.out.println("  - " + field));
        }
        
        if (missingInEntity.isEmpty() && missingInDb.isEmpty()) {
            System.out.println("\n✅ 字段同步检测通过！所有字段都匹配。");
        } else {
            System.out.println("\n⚠️  字段同步检测发现差异，请检查上述缺失字段。");
        }
    }
    
    /**
     * 直接从MySQL数据库获取表字段信息
     */
    private Set<String> getDatabaseFields(String tableName) throws SQLException, ClassNotFoundException {
        Set<String> fields = new HashSet<>();
        
        // 加载MySQL驱动
        Class.forName(DB_DRIVER);
        
        // 建立数据库连接
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            System.out.println("✅ 数据库连接成功");
            
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取表字段信息
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String columnType = rs.getString("TYPE_NAME");
                    String columnSize = rs.getString("COLUMN_SIZE");
                    String nullable = rs.getString("IS_NULLABLE");
                    
                    // 转换为驼峰命名
                    String camelCaseName = convertToCamelCase(columnName);
                    fields.add(camelCaseName);
                    
                    // 输出详细字段信息（可选）
                    System.out.println("  数据库字段: " + columnName + " -> " + camelCaseName + 
                                     " (类型: " + columnType + ", 大小: " + columnSize + 
                                     ", 可空: " + nullable + ")");
                }
            }
            
            // 验证表是否存在
            if (fields.isEmpty()) {
                System.out.println("⚠️  警告: 表 '" + tableName + "' 不存在或没有字段");
                
                // 列出数据库中的所有表
                System.out.println("\n数据库中的表列表:");
                try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        String tableNameInDb = tables.getString("TABLE_NAME");
                        System.out.println("  - " + tableNameInDb);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            System.err.println("请检查数据库配置:");
            System.err.println("  URL: " + DB_URL);
            System.err.println("  用户名: " + DB_USERNAME);
            System.err.println("  密码: " + (DB_PASSWORD.isEmpty() ? "空" : "已设置"));
            throw e;
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
            // 如果有@TableField注解且指定了value，使用注解值转驼峰
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
     * 测试数据库连接
     */
    public static void testConnection() {
        System.out.println("=== 测试数据库连接 ===");
        
        try {
            Class.forName(DB_DRIVER);
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                System.out.println("✅ 数据库连接测试成功");
                System.out.println("数据库产品: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("数据库版本: " + connection.getMetaData().getDatabaseProductVersion());
                System.out.println("驱动版本: " + connection.getMetaData().getDriverVersion());
            }
        } catch (Exception e) {
            System.err.println("❌ 数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}