package org.jeecg.modules.content.user.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区资料管理迁移脚本契约测试。
 */
class ContentUserProfileManagementMigrationTest {

    private static final Path MIGRATION = Path.of("src/main/resources/flyway/sql/mysql/V3.9.1_53__content_user_profile_management.sql");
    private static final Path ROLLBACK = Path.of("src/main/resources/flyway/sql/mysql/R3.9.1_53__content_user_profile_management_rollback.sql");

    @Test
    void shouldCreateProfileManagementTablesWithIndexesAndDefaults() throws IOException {
        String sql = Files.readString(MIGRATION);

        assertThat(sql).contains(
            "CREATE TABLE IF NOT EXISTS `content_user_profile_review`",
            "CREATE TABLE IF NOT EXISTS `content_user_homepage_module`",
            "CREATE TABLE IF NOT EXISTS `content_user_verification_badge`",
            "CREATE TABLE IF NOT EXISTS `content_user_profile_history`",
            "DEFAULT 'PENDING'",
            "DEFAULT 'ACTIVE'",
            "DEFAULT 0 COMMENT '是否过期'",
            "UNIQUE KEY `uk_content_user_homepage_module` (`user_id`,`module_key`)",
            "KEY `idx_content_user_profile_review_user` (`user_id`,`review_status`,`create_time`)",
            "KEY `idx_content_user_verification_badge_user` (`user_id`,`status`,`badge_type`)",
            "KEY `idx_content_user_profile_history_user` (`user_id`,`history_type`,`expired`,`create_time`)"
        );
    }

    @Test
    void shouldExtendExistingProfileAndPrivacyTablesWithCompatibleDefaults() throws IOException {
        String sql = Files.readString(MIGRATION);

        assertThat(sql).contains(
            "ADD COLUMN `profile_completion_state` varchar(32) NOT NULL DEFAULT 'INCOMPLETE'",
            "ADD COLUMN `profile_review_status` varchar(32) NOT NULL DEFAULT 'NONE'",
            "ADD COLUMN `profile_version` int NOT NULL DEFAULT 0",
            "ADD COLUMN `personal_link_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC'",
            "ADD COLUMN `verification_badge_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC'",
            "ADD COLUMN `contact_badge_visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE'"
        );
    }

    @Test
    void shouldRollbackCreatedTablesAndExtendedColumns() throws IOException {
        String rollback = Files.readString(ROLLBACK);
        List<String> tables = List.of(
            "content_user_profile_history",
            "content_user_verification_badge",
            "content_user_homepage_module",
            "content_user_profile_review"
        );

        assertThat(tables).allSatisfy(table -> assertThat(rollback).contains("DROP TABLE IF EXISTS `" + table + "`"));
        assertThat(rollback).contains(
            "DROP COLUMN `profile_version`",
            "DROP COLUMN `profile_review_status`",
            "DROP COLUMN `profile_completion_state`",
            "DROP COLUMN `contact_badge_visibility`",
            "DROP COLUMN `verification_badge_visibility`",
            "DROP COLUMN `personal_link_visibility`"
        );
    }
}
