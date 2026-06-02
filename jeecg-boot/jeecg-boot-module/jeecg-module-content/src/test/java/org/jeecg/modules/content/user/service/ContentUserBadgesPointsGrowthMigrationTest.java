package org.jeecg.modules.content.user.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区勋章积分成长迁移脚本契约测试。
 */
class ContentUserBadgesPointsGrowthMigrationTest {

    private static final Path MIGRATION = Path.of("src/main/resources/flyway/sql/mysql/V3.9.1_55__content_user_badges_points_growth.sql");
    private static final Path ROLLBACK = Path.of("src/main/resources/flyway/sql/mysql/R3.9.1_55__content_user_badges_points_growth_rollback.sql");

    @Test
    void shouldCreateRewardLevelExchangeAndDecayTables() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "CREATE TABLE IF NOT EXISTS `content_user_reward_rule`",
            "CREATE TABLE IF NOT EXISTS `content_user_reward_event`",
            "CREATE TABLE IF NOT EXISTS `content_user_level_config`",
            "CREATE TABLE IF NOT EXISTS `content_user_level_benefit_config`",
            "CREATE TABLE IF NOT EXISTS `content_user_exchange_goods`",
            "CREATE TABLE IF NOT EXISTS `content_user_exchange_order`",
            "CREATE TABLE IF NOT EXISTS `content_user_feature_unlock`",
            "CREATE TABLE IF NOT EXISTS `content_user_virtual_gift_record`",
            "CREATE TABLE IF NOT EXISTS `content_user_growth_decay_state`"
        );
    }

    @Test
    void shouldKeepIdempotencyCapAndBalanceIndexes() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "UNIQUE KEY `uk_content_user_reward_event_event` (`event_id`)",
            "KEY `idx_content_user_reward_event_bucket` (`source_type`,`daily_bucket`,`user_id`)",
            "KEY `idx_content_user_exchange_order_user` (`user_id`,`create_time`)",
            "UNIQUE KEY `uk_content_user_feature_unlock` (`user_id`,`feature_code`)",
            "KEY `idx_content_user_growth_decay_candidate` (`status`,`last_active_time`)"
        );
    }

    @Test
    void shouldExtendExistingBadgeAndLedgerAuditFields() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "ADD COLUMN `category` varchar(32)",
            "ADD COLUMN `icon_url` varchar(500)",
            "ADD COLUMN `display_order` int",
            "ADD COLUMN `recycled_by` varchar(32)",
            "ADD COLUMN `source_description` varchar(255)",
            "ADD COLUMN `event_id` varchar(64)",
            "ADD COLUMN `rule_snapshot_json` text"
        );
    }

    @Test
    void shouldRollbackAllCreatedTablesAndExtendedColumns() throws IOException {
        String rollback = readSql(ROLLBACK);
        List<String> tables = List.of(
            "content_user_reward_rule",
            "content_user_reward_event",
            "content_user_level_config",
            "content_user_level_benefit_config",
            "content_user_exchange_goods",
            "content_user_exchange_order",
            "content_user_feature_unlock",
            "content_user_virtual_gift_record",
            "content_user_growth_decay_state"
        );

        assertThat(tables)
            .allSatisfy(table -> assertThat(rollback).contains("DROP TABLE IF EXISTS `" + table + "`"));
        assertThat(rollback).contains(
            "DROP COLUMN `category`",
            "DROP COLUMN `display_order`",
            "DROP COLUMN `source_description`",
            "DROP COLUMN `event_id`",
            "DROP COLUMN `rule_snapshot_json`"
        );
    }

    private String readSql(Path path) throws IOException {
        return Files.readString(path);
    }
}
