package org.jeecg.modules.content.entity;

import java.sql.*;

/**
 * MySQL数据库连接测试工具
 * 用于验证数据库连接配置是否正确
 * 
 * @author jeecg-boot
 * @date 2024-12-19
 */
public class MySQLConnectionTest {
    
    // 数据库连接配置 - 请根据实际情况修改
    private static final String DB_URL = "jdbc:mysql://localhost:3306/jeecg-boot?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    public static void main(String[] args) {
        System.out.println("=== MySQL数据库连接测试 ===");
        
        MySQLConnectionTest test = new MySQLConnectionTest();
        
        // 测试基本连接
        test.testBasicConnection();
        
        // 测试数据库信息
        test.testDatabaseInfo();
        
        // 测试表查询
        test.testTableQuery();
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    /**
     * 测试基本数据库连接
     */
    public void testBasicConnection() {
        System.out.println("\n--- 基本连接测试 ---");
        
        try {
            // 加载MySQL驱动
            Class.forName(DB_DRIVER);
            System.out.println("✅ MySQL驱动加载成功");
            
            // 建立连接
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                System.out.println("✅ 数据库连接成功");
                System.out.println("连接URL: " + DB_URL);
                System.out.println("用户名: " + DB_USERNAME);
                System.out.println("连接状态: " + (connection.isClosed() ? "已关闭" : "已连接"));
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL驱动未找到: " + e.getMessage());
            System.err.println("请确保MySQL驱动已添加到classpath中");
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("SQL状态: " + e.getSQLState());
            
            // 提供常见错误的解决建议
            if (e.getMessage().contains("Access denied")) {
                System.err.println("💡 建议: 检查用户名和密码是否正确");
            } else if (e.getMessage().contains("Connection refused")) {
                System.err.println("💡 建议: 检查MySQL服务是否启动，端口是否正确");
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("💡 建议: 检查数据库名称是否正确");
            }
        }
    }
    
    /**
     * 测试数据库信息获取
     */
    public void testDatabaseInfo() {
        System.out.println("\n--- 数据库信息测试 ---");
        
        try {
            Class.forName(DB_DRIVER);
            
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                System.out.println("数据库产品名称: " + metaData.getDatabaseProductName());
                System.out.println("数据库产品版本: " + metaData.getDatabaseProductVersion());
                System.out.println("驱动名称: " + metaData.getDriverName());
                System.out.println("驱动版本: " + metaData.getDriverVersion());
                System.out.println("JDBC版本: " + metaData.getJDBCMajorVersion() + "." + metaData.getJDBCMinorVersion());
                System.out.println("数据库URL: " + metaData.getURL());
                System.out.println("用户名: " + metaData.getUserName());
                
                // 获取数据库支持的功能
                System.out.println("支持事务: " + metaData.supportsTransactions());
                System.out.println("支持批量更新: " + metaData.supportsBatchUpdates());
                System.out.println("支持存储过程: " + metaData.supportsStoredProcedures());
                
            }
            
        } catch (Exception e) {
            System.err.println("❌ 获取数据库信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试表查询
     */
    public void testTableQuery() {
        System.out.println("\n--- 表查询测试 ---");
        
        try {
            Class.forName(DB_DRIVER);
            
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                // 列出所有表
                System.out.println("数据库中的表列表:");
                try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                    int tableCount = 0;
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        String tableType = tables.getString("TABLE_TYPE");
                        System.out.println("  " + (++tableCount) + ". " + tableName + " (" + tableType + ")");
                    }
                    System.out.println("总计: " + tableCount + " 个表");
                }
                
                // 检查contents表是否存在
                System.out.println("\n检查contents表:");
                try (ResultSet contentsTable = metaData.getTables(null, null, "contents", new String[]{"TABLE"})) {
                    if (contentsTable.next()) {
                        System.out.println("✅ contents表存在");
                        
                        // 获取contents表的字段信息
                        System.out.println("contents表字段信息:");
                        try (ResultSet columns = metaData.getColumns(null, null, "contents", null)) {
                            int columnCount = 0;
                            while (columns.next()) {
                                String columnName = columns.getString("COLUMN_NAME");
                                String columnType = columns.getString("TYPE_NAME");
                                int columnSize = columns.getInt("COLUMN_SIZE");
                                String nullable = columns.getString("IS_NULLABLE");
                                String defaultValue = columns.getString("COLUMN_DEF");
                                
                                System.out.println("  " + (++columnCount) + ". " + columnName + 
                                                 " (" + columnType + "(" + columnSize + ")" +
                                                 ", 可空: " + nullable +
                                                 (defaultValue != null ? ", 默认值: " + defaultValue : "") + ")");
                            }
                            System.out.println("总计: " + columnCount + " 个字段");
                        }
                    } else {
                        System.out.println("❌ contents表不存在");
                    }
                }
                
                // 测试简单查询
                System.out.println("\n测试简单查询:");
                try (Statement stmt = connection.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM contents")) {
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            System.out.println("✅ contents表记录数: " + count);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("❌ 查询contents表失败: " + e.getMessage());
                }
                
            }
            
        } catch (Exception e) {
            System.err.println("❌ 表查询测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改数据库连接配置的方法
     */
    public static void printConfigurationHelp() {
        System.out.println("\n=== 数据库配置说明 ===");
        System.out.println("如需修改数据库连接配置，请编辑以下常量:");
        System.out.println("DB_URL: " + DB_URL);
        System.out.println("DB_USERNAME: " + DB_USERNAME);
        System.out.println("DB_PASSWORD: " + DB_PASSWORD);
        System.out.println("DB_DRIVER: " + DB_DRIVER);
        System.out.println("\n常见配置示例:");
        System.out.println("本地MySQL: jdbc:mysql://localhost:3306/数据库名");
        System.out.println("远程MySQL: jdbc:mysql://服务器IP:3306/数据库名");
        System.out.println("带参数: jdbc:mysql://localhost:3306/数据库名?useUnicode=true&characterEncoding=utf8");
    }
}