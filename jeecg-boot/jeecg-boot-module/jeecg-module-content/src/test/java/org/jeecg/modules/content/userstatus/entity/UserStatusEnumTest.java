package org.jeecg.modules.content.userstatus.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户状态枚举测试。
 * 验证枚举包含 9 种状态、状态码唯一、元数据完整。
 */
class UserStatusEnumTest {

    @Test
    void shouldContainExactly9StatusValues() {
        UserStatusEnum[] values = UserStatusEnum.values();
        assertThat(values).hasSize(9);
    }

    @Test
    void shouldContainAllRequiredStatuses() {
        Set<String> statusNames = Arrays.stream(UserStatusEnum.values())
            .map(UserStatusEnum::name)
            .collect(Collectors.toSet());

        assertThat(statusNames).containsExactlyInAnyOrder(
            "GUEST",
            "REGISTERED_INCOMPLETE",
            "NORMAL",
            "MUTED",
            "RESTRICTED_RECOMMEND",
            "FROZEN",
            "BANNED",
            "DEACTIVATING",
            "DEACTIVATED"
        );
    }

    @Test
    void shouldHaveUniqueStatusCodes() {
        Set<Integer> codes = Arrays.stream(UserStatusEnum.values())
            .map(UserStatusEnum::getCode)
            .collect(Collectors.toSet());

        assertThat(codes).hasSize(9);
    }

    @Test
    void shouldHaveNonNullMetadataForEachStatus() {
        for (UserStatusEnum status : UserStatusEnum.values()) {
            assertThat(status.getCode()).isGreaterThan(0);
            assertThat(status.getName()).isNotBlank();
            assertThat(status.getDisplayName()).isNotBlank();
            assertThat(status.getDescription()).isNotBlank();
        }
    }

    @Test
    void shouldLookupByCode() {
        for (UserStatusEnum status : UserStatusEnum.values()) {
            assertThat(UserStatusEnum.fromCode(status.getCode())).isEqualTo(status);
        }
    }

    @Test
    void shouldReturnNullForInvalidCode() {
        assertThat(UserStatusEnum.fromCode(999)).isNull();
        assertThat(UserStatusEnum.fromCode(-1)).isNull();
    }
}
