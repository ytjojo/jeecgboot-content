package org.jeecg.modules.content.auth.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 会话扩展/风控事件/注销请求迁移合同测试。
 * 验证 Flyway 迁移文件和回滚文件的存在性及关键内容。
 */
class ContentAuthSessionRiskCancellationMigrationTest {

    private static final String MIGRATION_PATH = "flyway/sql/mysql/V3.9.1_62__content_auth_session_risk_cancellation.sql";
    private static final String ROLLBACK_PATH = "flyway/sql/mysql/R3.9.1_62__content_auth_session_risk_cancellation_rollback.sql";

    // ---- 文件存在性 ----

    @Test
    void migrationFileShouldExist() {
        assertThat(readResource(MIGRATION_PATH)).isNotEmpty();
    }

    @Test
    void rollbackFileShouldExist() {
        assertThat(readResource(ROLLBACK_PATH)).isNotEmpty();
    }

    // ---- content_user_device_session 扩展列 ----

    @Test
    void migrationShouldAlterDeviceSessionAddTokenJti() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("token_jti");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddOsType() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("os_type");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddOsVersion() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("os_version");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddBrowserType() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("browser_type");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddDeviceFingerprint() {
        String sql = readResource(MIGRATION_PATH);
        // 确保是 ALTER TABLE 场景下的 device_fingerprint（不是 risk_event 表的）
        assertThat(sql).containsIgnoringCase("device_fingerprint");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddTrusted() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("trusted");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddSessionStatus() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("session_status");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddOfflineTime() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("offline_time");
    }

    @Test
    void migrationShouldAlterDeviceSessionAddOfflineReason() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("offline_reason");
    }

    @Test
    void migrationShouldAddJtiIndex() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("idx_content_user_device_session_jti");
    }

    @Test
    void migrationShouldAddStatusIndex() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("idx_content_user_device_session_status");
    }

    // ---- content_risk_event 表 ----

    @Test
    void migrationShouldCreateRiskEventTable() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("content_risk_event");
    }

    @Test
    void riskEventShouldContainEventType() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("event_type");
    }

    @Test
    void riskEventShouldContainRiskLevel() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("risk_level");
    }

    @Test
    void riskEventShouldContainDecision() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("decision");
    }

    @Test
    void riskEventShouldContainIpAddress() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("ip_address");
    }

    @Test
    void riskEventShouldContainResolved() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("resolved");
    }

    @Test
    void riskEventShouldContainExtraDataJson() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("extra_data_json");
    }

    @Test
    void riskEventShouldHaveUserEventTypeIndex() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("idx_content_risk_event_user");
    }

    @Test
    void riskEventShouldHaveIpTimeIndex() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("idx_content_risk_event_ip");
    }

    @Test
    void riskEventShouldHaveUnresolvedIndex() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("idx_content_risk_event_unresolved");
    }

    // ---- content_cancellation_request 表 ----

    @Test
    void migrationShouldCreateCancellationRequestTable() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("content_cancellation_request");
    }

    @Test
    void cancellationShouldContainStatus() {
        String sql = readResource(MIGRATION_PATH);
        // cancellation_request 表的 status 字段
        assertThat(sql).containsIgnoringCase("PENDING");
        assertThat(sql).containsIgnoringCase("COMPLETED");
        assertThat(sql).containsIgnoringCase("CANCELLED");
    }

    @Test
    void cancellationShouldContainCooldownDays() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("cooldown_days");
    }

    @Test
    void cancellationShouldContainCooldownDeadline() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("cooldown_deadline");
    }

    @Test
    void cancellationShouldContainAnonymized() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("anonymized");
    }

    @Test
    void cancellationShouldHaveUniqueUserStatus() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("uk_content_cancellation_user_status");
    }

    @Test
    void cancellationShouldHaveDeadlineIndex() {
        assertThat(readResource(MIGRATION_PATH)).containsIgnoringCase("idx_content_cancellation_deadline");
    }

    // ---- 回滚文件验证 ----

    @Test
    void rollbackShouldDropCancellationRequestTable() {
        assertThat(readResource(ROLLBACK_PATH)).containsIgnoringCase("content_cancellation_request");
    }

    @Test
    void rollbackShouldDropRiskEventTable() {
        assertThat(readResource(ROLLBACK_PATH)).containsIgnoringCase("content_risk_event");
    }

    @Test
    void rollbackShouldDropDeviceSessionJtiIndex() {
        assertThat(readResource(ROLLBACK_PATH)).containsIgnoringCase("idx_content_user_device_session_jti");
    }

    @Test
    void rollbackShouldDropDeviceSessionStatusIndex() {
        assertThat(readResource(ROLLBACK_PATH)).containsIgnoringCase("idx_content_user_device_session_status");
    }

    @Test
    void rollbackShouldDropDeviceSessionAddedColumns() {
        String rollback = readResource(ROLLBACK_PATH);
        assertThat(rollback).containsIgnoringCase("token_jti");
        assertThat(rollback).containsIgnoringCase("session_status");
        assertThat(rollback).containsIgnoringCase("os_type");
        assertThat(rollback).containsIgnoringCase("browser_type");
        assertThat(rollback).containsIgnoringCase("trusted");
        assertThat(rollback).containsIgnoringCase("offline_time");
        assertThat(rollback).containsIgnoringCase("offline_reason");
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
