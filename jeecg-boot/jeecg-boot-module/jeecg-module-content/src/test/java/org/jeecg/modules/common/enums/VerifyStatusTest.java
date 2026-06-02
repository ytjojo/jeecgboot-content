package org.jeecg.modules.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link VerifyStatus} 单测。
 *
 * 覆盖审计报告 P1 项：canVerify 仅在 NOT_VERIFIED/VERIFY_FAILED 为 true 的硬规则。
 */
class VerifyStatusTest {

    // ---------------------------------------------------------------------
    // 元数据契约
    // ---------------------------------------------------------------------

    @Test
    void shouldExpose4ConstantsWithUniqueValueAndCompleteMetadata() {
        assertThat(VerifyStatus.values()).hasSize(4);

        for (VerifyStatus status : VerifyStatus.values()) {
            assertThat(status.getValue()).isNotNull();
            assertThat(status.getName()).isNotBlank();
            assertThat(status.getDescription()).isNotBlank();
        }

        assertThat(VerifyStatus.values())
            .extracting(VerifyStatus::getValue)
            .doesNotHaveDuplicates();
    }

    // ---------------------------------------------------------------------
    // getByCode
    // ---------------------------------------------------------------------

    @Test
    void getByCode_shouldReturnEnumForEachDefinedCode() {
        assertThat(VerifyStatus.getByCode(0)).isEqualTo(VerifyStatus.NOT_VERIFIED);
        assertThat(VerifyStatus.getByCode(1)).isEqualTo(VerifyStatus.VERIFYING);
        assertThat(VerifyStatus.getByCode(2)).isEqualTo(VerifyStatus.VERIFIED);
        assertThat(VerifyStatus.getByCode(3)).isEqualTo(VerifyStatus.VERIFY_FAILED);
    }

    @Test
    void getByCode_shouldReturnNullForUnknownCode() {
        assertThat(VerifyStatus.getByCode(99)).isNull();
        assertThat(VerifyStatus.getByCode(-1)).isNull();
    }

    @Test
    void getByCode_shouldReturnNullForNullInput() {
        assertThat(VerifyStatus.getByCode(null)).isNull();
    }

    // ---------------------------------------------------------------------
    // 状态判定
    // ---------------------------------------------------------------------

    @Test
    void isVerified_isTrueOnlyForVerified() {
        assertThat(VerifyStatus.VERIFIED.isVerified()).isTrue();
        assertThat(VerifyStatus.NOT_VERIFIED.isVerified()).isFalse();
        assertThat(VerifyStatus.VERIFYING.isVerified()).isFalse();
        assertThat(VerifyStatus.VERIFY_FAILED.isVerified()).isFalse();
    }

    @Test
    void isVerifying_isTrueOnlyForVerifying() {
        assertThat(VerifyStatus.VERIFYING.isVerifying()).isTrue();
        assertThat(VerifyStatus.NOT_VERIFIED.isVerifying()).isFalse();
        assertThat(VerifyStatus.VERIFIED.isVerifying()).isFalse();
        assertThat(VerifyStatus.VERIFY_FAILED.isVerifying()).isFalse();
    }

    @Test
    void canVerify_isTrueOnlyForNotVerifiedAndVerifyFailed() {
        // 审计明确指出：VERIFYING/VERIFIED 不可重复发起认证
        assertThat(VerifyStatus.NOT_VERIFIED.canVerify()).isTrue();
        assertThat(VerifyStatus.VERIFY_FAILED.canVerify()).isTrue();
        assertThat(VerifyStatus.VERIFYING.canVerify()).isFalse();
        assertThat(VerifyStatus.VERIFIED.canVerify()).isFalse();
    }
}
