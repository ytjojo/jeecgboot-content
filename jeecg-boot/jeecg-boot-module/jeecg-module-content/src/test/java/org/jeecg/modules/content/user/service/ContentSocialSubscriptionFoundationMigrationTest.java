package org.jeecg.modules.content.user.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区社交订阅基础迁移脚本契约测试。
 */
class ContentSocialSubscriptionFoundationMigrationTest {

    private static final Path MIGRATION = Path.of("src/main/resources/flyway/sql/mysql/V3.9.1_54__content_social_subscription_foundation.sql");
    private static final Path ROLLBACK = Path.of("src/main/resources/flyway/sql/mysql/R3.9.1_54__content_social_subscription_foundation_rollback.sql");

    @Test
    void shouldExtendExistingRelationSubscriptionAndNotificationTables() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "ADD COLUMN `relation_status` varchar(32) NOT NULL DEFAULT 'ACTIVE'",
            "ADD COLUMN `last_interaction_time` datetime DEFAULT NULL",
            "ADD COLUMN `group_status` varchar(32) NOT NULL DEFAULT 'ACTIVE'",
            "ADD COLUMN `subscription_status` varchar(32) NOT NULL DEFAULT 'ACTIVE'",
            "ADD COLUMN `subscribed_at` datetime DEFAULT CURRENT_TIMESTAMP",
            "ADD COLUMN `last_update_time` datetime DEFAULT NULL",
            "ADD COLUMN `subscription_notice_enabled` tinyint(1) NOT NULL DEFAULT 1",
            "ADD COLUMN `subscription_default_channels` varchar(255) NOT NULL DEFAULT 'IN_APP,PUSH'",
            "ADD COLUMN `subscription_default_frequency` varchar(32) NOT NULL DEFAULT 'REALTIME'"
        );
    }

    @Test
    void shouldCreateFollowFeedRecommendationSourceAndPreferenceTables() throws IOException {
        String sql = readSql(MIGRATION);
        List<String> tables = List.of(
            "content_user_feed_setting",
            "content_user_activity_snapshot",
            "content_user_follow_recommendation",
            "content_subscription_source",
            "content_subscription_notification_preference"
        );

        assertThat(tables)
            .allSatisfy(table -> assertThat(sql).contains("CREATE TABLE IF NOT EXISTS `" + table + "`"));
    }

    @Test
    void shouldKeepDefaultValuesUniqueConstraintsAndStatusIndexes() throws IOException {
        String sql = readSql(MIGRATION);

        assertThat(sql).contains(
            "KEY `idx_content_user_relation_follow_group` (`owner_user_id`,`followed`,`relation_group_id`,`followed_at`)",
            "KEY `idx_content_user_relation_special` (`owner_user_id`,`special_follow`,`special_follow_at`)",
            "UNIQUE KEY `uk_content_user_relation_group_name` (`owner_user_id`,`group_name`,`group_status`)",
            "KEY `idx_content_user_subscription_user_status` (`user_id`,`subscription_status`,`paused`,`update_time`)",
            "UNIQUE KEY `uk_content_user_feed_setting_user` (`user_id`)",
            "UNIQUE KEY `uk_content_user_activity_snapshot_biz` (`activity_type`,`biz_type`,`biz_id`)",
            "KEY `idx_content_user_activity_snapshot_actor` (`actor_user_id`,`snapshot_status`,`activity_time`)",
            "UNIQUE KEY `uk_content_user_follow_recommendation_pair` (`user_id`,`target_user_id`)",
            "KEY `idx_content_user_follow_recommendation_rank` (`user_id`,`recommendation_status`,`ranking_score`)",
            "UNIQUE KEY `uk_content_subscription_source` (`source_type`,`source_id`)",
            "KEY `idx_content_subscription_source_hot` (`enabled`,`category`,`heat_score`)",
            "UNIQUE KEY `uk_content_subscription_notification_subscription` (`subscription_id`)",
            "KEY `idx_content_subscription_notification_user` (`user_id`,`preference_status`)"
        );
    }

    @Test
    void shouldRollbackCreatedTablesIndexesAndExtendedColumns() throws IOException {
        String rollback = readSql(ROLLBACK);
        List<String> tables = List.of(
            "content_subscription_notification_preference",
            "content_subscription_source",
            "content_user_follow_recommendation",
            "content_user_activity_snapshot",
            "content_user_feed_setting"
        );

        assertThat(tables)
            .allSatisfy(table -> assertThat(rollback).contains("DROP TABLE IF EXISTS `" + table + "`"));
        assertThat(rollback).contains(
            "DROP KEY `idx_content_user_relation_follow_group`",
            "DROP KEY `uk_content_user_relation_group_name`",
            "DROP KEY `idx_content_user_subscription_user_status`",
            "DROP COLUMN `relation_status`",
            "DROP COLUMN `group_status`",
            "DROP COLUMN `subscription_status`",
            "DROP COLUMN `subscription_notice_enabled`"
        );
    }

    private String readSql(Path path) throws IOException {
        return Files.readString(path);
    }
}
