package org.jeecg.modules.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link EnableStatusEnum} 单测。
 *
 * 覆盖审计报告 P1 项：5 个业务方法 + getByValue/getByName 的合法与边界场景。
 */
class EnableStatusEnumTest {

    // ---------------------------------------------------------------------
    // 元数据契约
    // ---------------------------------------------------------------------

    @Test
    void shouldExpose5ConstantsWithUniqueValueAndCompleteMetadata() {
        assertThat(EnableStatusEnum.values()).hasSize(5);

        for (EnableStatusEnum status : EnableStatusEnum.values()) {
            assertThat(status.getValue()).isNotNull();
            assertThat(status.getName()).isNotBlank();
            assertThat(status.getDescription()).isNotBlank();
        }

        assertThat(EnableStatusEnum.values())
            .extracting(EnableStatusEnum::getValue)
            .doesNotHaveDuplicates();
    }

    // ---------------------------------------------------------------------
    // getByValue
    // ---------------------------------------------------------------------

    @Test
    void getByValue_shouldReturnEnumForEachDefinedValue() {
        assertThat(EnableStatusEnum.getByValue(0)).isEqualTo(EnableStatusEnum.DISABLED);
        assertThat(EnableStatusEnum.getByValue(1)).isEqualTo(EnableStatusEnum.ENABLED);
        assertThat(EnableStatusEnum.getByValue(2)).isEqualTo(EnableStatusEnum.REVIEWING);
        assertThat(EnableStatusEnum.getByValue(3)).isEqualTo(EnableStatusEnum.REJECTED);
        assertThat(EnableStatusEnum.getByValue(-1)).isEqualTo(EnableStatusEnum.DELETED);
    }

    @Test
    void getByValue_shouldReturnNullForUnknownValue() {
        assertThat(EnableStatusEnum.getByValue(99)).isNull();
        assertThat(EnableStatusEnum.getByValue(4)).isNull();
    }

    @Test
    void getByValue_shouldReturnNullForNullInput() {
        assertThat(EnableStatusEnum.getByValue(null)).isNull();
    }

    // ---------------------------------------------------------------------
    // getByName
    // ---------------------------------------------------------------------

    @Test
    void getByName_shouldReturnEnumForEachDefinedName() {
        assertThat(EnableStatusEnum.getByName("disabled")).isEqualTo(EnableStatusEnum.DISABLED);
        assertThat(EnableStatusEnum.getByName("enabled")).isEqualTo(EnableStatusEnum.ENABLED);
        assertThat(EnableStatusEnum.getByName("reviewing")).isEqualTo(EnableStatusEnum.REVIEWING);
        assertThat(EnableStatusEnum.getByName("rejected")).isEqualTo(EnableStatusEnum.REJECTED);
        assertThat(EnableStatusEnum.getByName("deleted")).isEqualTo(EnableStatusEnum.DELETED);
    }

    @Test
    void getByName_shouldReturnNullForUnknownName() {
        assertThat(EnableStatusEnum.getByName("unknown")).isNull();
        assertThat(EnableStatusEnum.getByName("Enabled")).isNull(); // 大小写敏感
    }

    @Test
    void getByName_shouldReturnNullForNullOrBlank() {
        assertThat(EnableStatusEnum.getByName(null)).isNull();
        assertThat(EnableStatusEnum.getByName("")).isNull();
        assertThat(EnableStatusEnum.getByName("   ")).isNull();
    }

    // ---------------------------------------------------------------------
    // 状态判定（5 个常量逐一覆盖）
    // ---------------------------------------------------------------------

    @Test
    void isNormal_isTrueOnlyForEnabled() {
        assertThat(EnableStatusEnum.ENABLED.isNormal()).isTrue();
        assertThat(EnableStatusEnum.DISABLED.isNormal()).isFalse();
        assertThat(EnableStatusEnum.REVIEWING.isNormal()).isFalse();
        assertThat(EnableStatusEnum.REJECTED.isNormal()).isFalse();
        assertThat(EnableStatusEnum.DELETED.isNormal()).isFalse();
    }

    @Test
    void isDisabled_isTrueOnlyForDisabled() {
        assertThat(EnableStatusEnum.DISABLED.isDisabled()).isTrue();
        assertThat(EnableStatusEnum.ENABLED.isDisabled()).isFalse();
        assertThat(EnableStatusEnum.REVIEWING.isDisabled()).isFalse();
        assertThat(EnableStatusEnum.REJECTED.isDisabled()).isFalse();
        assertThat(EnableStatusEnum.DELETED.isDisabled()).isFalse();
    }

    @Test
    void isReviewing_isTrueOnlyForReviewing() {
        assertThat(EnableStatusEnum.REVIEWING.isReviewing()).isTrue();
        assertThat(EnableStatusEnum.DISABLED.isReviewing()).isFalse();
        assertThat(EnableStatusEnum.ENABLED.isReviewing()).isFalse();
        assertThat(EnableStatusEnum.REJECTED.isReviewing()).isFalse();
        assertThat(EnableStatusEnum.DELETED.isReviewing()).isFalse();
    }
}
