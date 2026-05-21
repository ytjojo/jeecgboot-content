package org.jeecg.modules.content.user.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区拉黑屏蔽迁移脚本契约测试。
 */
class ContentBlockingMutingMigrationTest {

    private static final Path MIGRATION = Path.of("src/main/resources/flyway/sql/mysql/V3.9.1_57__content_blocking_muting.sql");
    private static final Path ROLLBACK = Path.of("src/main/resources/flyway/sql/mysql/V3.9.1_57__content_blocking_muting_rollback.sql");

    @Test
    void shouldCreateBlockingMutingFeedbackAndFilterTables() throws IOException {
        String sql = readSql(MIGRATION);
        List<String> tables = List.of(
            "content_user_block",
            "content_user_mute",
            "content_user_filter_rule",
            "content_user_not_interested"
        );

        assertThat(tables)
            .allSatisfy(table -> assertThat(sql).contains("CREATE TABLE IF NOT EXISTS `" + table + "`"));
    }

    @Test
    void shouldKeepUniqueIndexesAndSafeDefaultsForUserProtectionRules() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "UNIQUE KEY `uk_content_user_block_pair` (`user_id`,`blocked_user_id`)",
            "UNIQUE KEY `uk_content_user_mute_pair` (`user_id`,`muted_user_id`)",
            "UNIQUE KEY `uk_content_user_filter_rule` (`user_id`,`rule_type`,`normalized_value`,`status`)",
            "UNIQUE KEY `uk_content_user_not_interested` (`user_id`,`content_id`,`content_type`)",
            "`block_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "`mute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "`match_scope` varchar(32) NOT NULL DEFAULT 'FEED'",
            "`feedback_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "`status` varchar(32) NOT NULL DEFAULT 'ACTIVE'"
        );
    }

    @Test
    void shouldAddQueryIndexesForBoundaryAndFeedFiltering() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "KEY `idx_content_user_block_user` (`user_id`,`status`,`block_time`)",
            "KEY `idx_content_user_block_blocked` (`blocked_user_id`,`status`)",
            "KEY `idx_content_user_mute_user` (`user_id`,`status`,`mute_time`)",
            "KEY `idx_content_user_filter_rule_user` (`user_id`,`status`,`rule_type`)",
            "KEY `idx_content_user_filter_rule_expire` (`status`,`expires_at`)",
            "KEY `idx_content_user_not_interested_user` (`user_id`,`status`,`feedback_time`)"
        );
    }

    @Test
    void shouldRollbackCreatedTablesInDependencySafeOrder() throws IOException {
        String rollback = readSql(ROLLBACK);
        List<String> tables = List.of(
            "content_user_not_interested",
            "content_user_filter_rule",
            "content_user_mute",
            "content_user_block"
        );

        assertThat(tables)
            .allSatisfy(table -> assertThat(rollback).contains("DROP TABLE IF EXISTS `" + table + "`"));
        assertThat(rollback.indexOf("content_user_not_interested")).isLessThan(rollback.indexOf("content_user_filter_rule"));
        assertThat(rollback.indexOf("content_user_filter_rule")).isLessThan(rollback.indexOf("content_user_mute"));
        assertThat(rollback.indexOf("content_user_mute")).isLessThan(rollback.indexOf("content_user_block"));
    }

    private String readSql(Path path) throws IOException {
        return Files.readString(path);
    }
}
