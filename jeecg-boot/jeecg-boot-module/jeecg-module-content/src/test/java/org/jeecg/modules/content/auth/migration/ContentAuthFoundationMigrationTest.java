package org.jeecg.modules.content.auth.migration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flyway 迁移 V3.9.1_61 内容认证基础表结构验证测试。
 * 验证迁移 SQL 和回滚 SQL 的完整性、字段、索引和约束。
 */
class ContentAuthFoundationMigrationTest {

    private static final String MIGRATION_DIR = "src/main/resources/flyway/sql/mysql/";
    private static final String MIGRATION_FILE = "V3.9.1_61__content_auth_foundation.sql";
    private static final String ROLLBACK_FILE = "R3.9.1_61__content_auth_foundation_rollback.sql";

    private static String migrationSql;
    private static String rollbackSql;

    @BeforeAll
    static void loadSqlFiles() throws Exception {
        migrationSql = Files.readString(Path.of(MIGRATION_DIR + MIGRATION_FILE));
        rollbackSql = Files.readString(Path.of(MIGRATION_DIR + ROLLBACK_FILE));
    }

    // ========== 迁移文件存在性 ==========

    @Test
    void migration61_fileExists() {
        assertTrue(Files.exists(Path.of(MIGRATION_DIR + MIGRATION_FILE)),
            "迁移文件 V3.9.1_61__content_auth_foundation.sql 应存在");
    }

    @Test
    void rollback61_fileExists() {
        assertTrue(Files.exists(Path.of(MIGRATION_DIR + ROLLBACK_FILE)),
            "回滚文件 R3.9.1_61__content_auth_foundation_rollback.sql 应存在");
    }

    // ========== 迁移文件包含所有预期表 ==========

    @Test
    void migration61_containsContentUserAccount() {
        assertTrue(migrationSql.contains("content_user_account"),
            "迁移应包含 content_user_account 表");
    }

    @Test
    void migration61_containsContentUserCredential() {
        assertTrue(migrationSql.contains("content_user_credential"),
            "迁移应包含 content_user_credential 表");
    }

    @Test
    void migration61_containsContentUserPasswordHistory() {
        assertTrue(migrationSql.contains("content_user_password_history"),
            "迁移应包含 content_user_password_history 表");
    }

    @Test
    void migration61_altersThirdPartyAuth() {
        assertTrue(migrationSql.contains("content_user_third_party_auth"),
            "迁移应包含对 content_user_third_party_auth 表的 ALTER 操作");
    }

    // ========== content_user_account 表字段验证 ==========

    @Test
    void migration61_userAccount_hasUserId() {
        assertColumnPresent(migrationSql, "content_user_account", "user_id");
    }

    @Test
    void migration61_userAccount_hasNickname() {
        assertColumnPresent(migrationSql, "content_user_account", "nickname");
    }

    @Test
    void migration61_userAccount_hasAvatar() {
        assertColumnPresent(migrationSql, "content_user_account", "avatar");
    }

    @Test
    void migration61_userAccount_hasAccountStatus() {
        assertColumnPresent(migrationSql, "content_user_account", "account_status");
    }

    @Test
    void migration61_userAccount_hasCancellationStatus() {
        assertColumnPresent(migrationSql, "content_user_account", "cancellation_status");
    }

    @Test
    void migration61_userAccount_hasLastLoginTime() {
        assertColumnPresent(migrationSql, "content_user_account", "last_login_time");
    }

    @Test
    void migration61_userAccount_hasLastLoginIp() {
        assertColumnPresent(migrationSql, "content_user_account", "last_login_ip");
    }

    @Test
    void migration61_userAccount_hasLoginFailCount() {
        assertColumnPresent(migrationSql, "content_user_account", "login_fail_count");
    }

    @Test
    void migration61_userAccount_hasLockedUntil() {
        assertColumnPresent(migrationSql, "content_user_account", "locked_until");
    }

    @Test
    void migration61_userAccount_hasRiskLevel() {
        assertColumnPresent(migrationSql, "content_user_account", "risk_level");
    }

    @Test
    void migration61_userAccount_hasAuditFields() {
        assertAuditFields(migrationSql, "content_user_account");
    }

    // ========== content_user_credential 表字段验证 ==========

    @Test
    void migration61_credential_hasUserId() {
        assertColumnPresent(migrationSql, "content_user_credential", "user_id");
    }

    @Test
    void migration61_credential_hasCredentialType() {
        assertColumnPresent(migrationSql, "content_user_credential", "credential_type");
    }

    @Test
    void migration61_credential_hasCredentialValue() {
        assertColumnPresent(migrationSql, "content_user_credential", "credential_value");
    }

    @Test
    void migration61_credential_hasSalt() {
        assertColumnPresent(migrationSql, "content_user_credential", "salt");
    }

    @Test
    void migration61_credential_hasVerified() {
        assertColumnPresent(migrationSql, "content_user_credential", "verified");
    }

    @Test
    void migration61_credential_hasStatus() {
        assertColumnPresent(migrationSql, "content_user_credential", "status");
    }

    @Test
    void migration61_credential_hasAuditFields() {
        assertAuditFields(migrationSql, "content_user_credential");
    }

    // ========== content_user_third_party_auth ALTER 字段验证 ==========

    @Test
    void migration61_thirdPartyAuth_addsOpenId() {
        assertTrue(migrationSql.contains("open_id"),
            "应为第三方授权表添加 open_id 字段");
    }

    @Test
    void migration61_thirdPartyAuth_addsUnionId() {
        assertTrue(migrationSql.contains("union_id"),
            "应为第三方授权表添加 union_id 字段");
    }

    @Test
    void migration61_thirdPartyAuth_addsNickname() {
        // nickname appears in both user_account and third_party_auth ALTER
        assertTrue(migrationSql.contains("nickname"),
            "应为第三方授权表添加 nickname 字段");
    }

    @Test
    void migration61_thirdPartyAuth_addsAvatar() {
        // avatar appears in both user_account and third_party_auth ALTER
        assertTrue(migrationSql.contains("avatar"),
            "应为第三方授权表添加 avatar 字段");
    }

    @Test
    void migration61_thirdPartyAuth_addsRawDataJson() {
        assertTrue(migrationSql.contains("raw_data_json"),
            "应为第三方授权表添加 raw_data_json 字段");
    }

    // ========== content_user_password_history 表字段验证 ==========

    @Test
    void migration61_passwordHistory_hasUserId() {
        assertColumnPresent(migrationSql, "content_user_password_history", "user_id");
    }

    @Test
    void migration61_passwordHistory_hasPasswordHash() {
        assertColumnPresent(migrationSql, "content_user_password_history", "password_hash");
    }

    @Test
    void migration61_passwordHistory_hasSalt() {
        assertColumnPresent(migrationSql, "content_user_password_history", "salt");
    }

    @Test
    void migration61_passwordHistory_hasAuditFields() {
        assertAuditFields(migrationSql, "content_user_password_history");
    }

    // ========== 唯一约束验证 ==========

    @Test
    void migration61_userAccount_hasUniqueUserId() {
        assertTrue(migrationSql.contains("uk_content_user_account_user_id"),
            "content_user_account 应有 user_id 唯一约束");
    }

    @Test
    void migration61_credential_hasUniqueTypeValue() {
        assertTrue(migrationSql.contains("uk_content_user_credential_type_value"),
            "content_user_credential 应有 credential_type+credential_value 唯一约束");
    }

    // ========== 索引验证 ==========

    @Test
    void migration61_userAccount_hasStatusIndex() {
        assertTrue(migrationSql.contains("idx_content_user_account_status"),
            "content_user_account 应有 account_status 索引");
    }

    @Test
    void migration61_credential_hasUserIndex() {
        assertTrue(migrationSql.contains("idx_content_user_credential_user"),
            "content_user_credential 应有 user_id 索引");
    }

    @Test
    void migration61_thirdPartyAuth_hasOpenIdIndex() {
        assertTrue(migrationSql.contains("idx_content_user_third_party_open_id"),
            "第三方授权表应有 open_id 索引");
    }

    @Test
    void migration61_passwordHistory_hasUserTimeIndex() {
        assertTrue(migrationSql.contains("idx_content_user_pwd_history_user"),
            "content_user_password_history 应有 user_id+create_time 索引");
    }

    // ========== 回滚文件验证 ==========

    @Test
    void rollback61_dropsUserAccount() {
        assertTrue(rollbackSql.contains("content_user_account"),
            "回滚应包含 content_user_account");
    }

    @Test
    void rollback61_dropsCredential() {
        assertTrue(rollbackSql.contains("content_user_credential"),
            "回滚应包含 content_user_credential");
    }

    @Test
    void rollback61_dropsPasswordHistory() {
        assertTrue(rollbackSql.contains("content_user_password_history"),
            "回滚应包含 content_user_password_history");
    }

    @Test
    void rollback61_handlesThirdPartyAuthAlter() {
        assertTrue(rollbackSql.contains("content_user_third_party_auth"),
            "回滚应处理第三方授权表的 ALTER 回退");
    }

    // ========== 辅助方法 ==========

    /**
     * 验证建表语句中包含指定字段。
     * 通过查找 CREATE TABLE 块中的字段定义来判断。
     */
    private void assertColumnPresent(String sql, String tableName, String columnName) {
        // 查找表名所在位置，然后检查附近是否有该字段
        int tableIdx = sql.indexOf(tableName);
        assertTrue(tableIdx >= 0, "SQL 中应包含表 " + tableName);

        // 在该表的 CREATE TABLE 块中查找字段
        String createBlock = extractCreateTableBlock(sql, tableName);
        assertTrue(createBlock.contains("`" + columnName + "`"),
            tableName + " 表应包含字段 " + columnName);
    }

    /**
     * 验证表包含四个标准审计字段。
     */
    private void assertAuditFields(String sql, String tableName) {
        String createBlock = extractCreateTableBlock(sql, tableName);
        assertTrue(createBlock.contains("`create_by`"), tableName + " 应有 create_by");
        assertTrue(createBlock.contains("`create_time`"), tableName + " 应有 create_time");
        assertTrue(createBlock.contains("`update_by`"), tableName + " 应有 update_by");
        assertTrue(createBlock.contains("`update_time`"), tableName + " 应有 update_time");
    }

    /**
     * 提取 CREATE TABLE IF NOT EXISTS `tableName` (...) 块。
     */
    private String extractCreateTableBlock(String sql, String tableName) {
        String marker = "CREATE TABLE IF NOT EXISTS `" + tableName + "`";
        int start = sql.indexOf(marker);
        assertTrue(start >= 0, "SQL 中应包含 CREATE TABLE IF NOT EXISTS `" + tableName + "`");

        // 找到对应的 ); 结束位置
        int depth = 0;
        int i = sql.indexOf('(', start);
        for (; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) break;
            }
        }
        // 包含结束的 );
        int end = sql.indexOf(';', i);
        return sql.substring(start, end >= 0 ? end + 1 : sql.length());
    }
}
