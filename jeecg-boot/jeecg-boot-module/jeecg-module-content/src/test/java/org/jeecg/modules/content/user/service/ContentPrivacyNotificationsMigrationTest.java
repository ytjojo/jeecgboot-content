package org.jeecg.modules.content.user.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 隐私通知迁移合同测试。
 * 验证 Flyway 迁移文件和回滚文件的存在性及关键内容。
 */
class ContentPrivacyNotificationsMigrationTest {

    private static final String MIGRATION_PATH = "flyway/sql/mysql/V3.9.1_58__content_privacy_notifications.sql";
    private static final String ROLLBACK_PATH = "flyway/sql/mysql/V3.9.1_58__content_privacy_notifications_rollback.sql";

    @Test
    void migrationFileShouldExist() {
        assertThat(readResource(MIGRATION_PATH)).isNotEmpty();
    }

    @Test
    void rollbackFileShouldExist() {
        assertThat(readResource(ROLLBACK_PATH)).isNotEmpty();
    }

    @Test
    void migrationShouldContainOnlineStatusVisibility() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("online_status_visibility");
    }

    @Test
    void migrationShouldContainBrowseHistoryVisibility() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("browse_history_visibility");
    }

    @Test
    void migrationShouldContainLikeActivityVisibility() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("like_activity_visibility");
    }

    @Test
    void migrationShouldContainFavoriteVisibility() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("favorite_visibility");
    }

    @Test
    void migrationShouldContainThirdPartyAuthTable() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("content_user_third_party_auth");
    }

    @Test
    void migrationShouldContainAuditLogTable() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("content_notification_audit_log");
    }

    @Test
    void rollbackShouldContainDropAuditLog() {
        assertThat(readResource(ROLLBACK_PATH)).containsIgnoringCase("content_notification_audit_log");
    }

    @Test
    void rollbackShouldContainDropThirdPartyAuth() {
        assertThat(readResource(ROLLBACK_PATH)).containsIgnoringCase("content_user_third_party_auth");
    }

    @Test
    void rollbackShouldContainDropVisibilityColumns() {
        String rollback = readResource(ROLLBACK_PATH);
        assertThat(rollback).containsIgnoringCase("browse_history_visibility");
        assertThat(rollback).containsIgnoringCase("like_activity_visibility");
        assertThat(rollback).containsIgnoringCase("favorite_visibility");
        assertThat(rollback).containsIgnoringCase("online_status_visibility");
    }

    private String readResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
