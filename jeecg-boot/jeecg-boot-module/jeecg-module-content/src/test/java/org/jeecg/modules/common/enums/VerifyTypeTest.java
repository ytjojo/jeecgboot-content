package org.jeecg.modules.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link VerifyType} 单测。
 *
 * 覆盖审计报告 P1 项：isOfficialType 排除 PERSONAL，requiresVerificationMaterial 排除 NONE，
 * 两者集合不同，必须互推验证。
 */
class VerifyTypeTest {

    // ---------------------------------------------------------------------
    // 元数据契约
    // ---------------------------------------------------------------------

    @Test
    void shouldExpose6ConstantsWithUniqueValueAndCompleteMetadata() {
        assertThat(VerifyType.values()).hasSize(6);

        for (VerifyType type : VerifyType.values()) {
            assertThat(type.getValue()).isNotNull();
            assertThat(type.getName()).isNotBlank();
            assertThat(type.getDescription()).isNotBlank();
        }

        assertThat(VerifyType.values())
            .extracting(VerifyType::getValue)
            .doesNotHaveDuplicates();
    }

    // ---------------------------------------------------------------------
    // getByValue
    // ---------------------------------------------------------------------

    @Test
    void getByValue_shouldReturnEnumForEachDefinedValue() {
        assertThat(VerifyType.getByValue(0)).isEqualTo(VerifyType.NONE);
        assertThat(VerifyType.getByValue(1)).isEqualTo(VerifyType.PERSONAL);
        assertThat(VerifyType.getByValue(2)).isEqualTo(VerifyType.BIG_V);
        assertThat(VerifyType.getByValue(3)).isEqualTo(VerifyType.OFFICIAL);
        assertThat(VerifyType.getByValue(4)).isEqualTo(VerifyType.ENTERPRISE);
        assertThat(VerifyType.getByValue(5)).isEqualTo(VerifyType.INSTITUTION);
    }

    @Test
    void getByValue_shouldReturnNullForUnknownValue() {
        assertThat(VerifyType.getByValue(99)).isNull();
        assertThat(VerifyType.getByValue(-1)).isNull();
    }

    @Test
    void getByValue_shouldReturnNullForNullInput() {
        assertThat(VerifyType.getByValue(null)).isNull();
    }

    // ---------------------------------------------------------------------
    // 单值类型判定
    // ---------------------------------------------------------------------

    @Test
    void isPersonal_isTrueOnlyForPersonal() {
        assertThat(VerifyType.PERSONAL.isPersonal()).isTrue();
        for (VerifyType type : VerifyType.values()) {
            if (type == VerifyType.PERSONAL) {
                continue;
            }
            assertThat(type.isPersonal())
                .as("isPersonal 应仅 PERSONAL 为 true，实际 %s 也返回 true", type)
                .isFalse();
        }
    }

    @Test
    void isEnterprise_isTrueOnlyForEnterprise() {
        assertThat(VerifyType.ENTERPRISE.isEnterprise()).isTrue();
        for (VerifyType type : VerifyType.values()) {
            if (type == VerifyType.ENTERPRISE) {
                continue;
            }
            assertThat(type.isEnterprise())
                .as("isEnterprise 应仅 ENTERPRISE 为 true，实际 %s 也返回 true", type)
                .isFalse();
        }
    }

    @Test
    void isInstitution_isTrueOnlyForInstitution() {
        assertThat(VerifyType.INSTITUTION.isInstitution()).isTrue();
        for (VerifyType type : VerifyType.values()) {
            if (type == VerifyType.INSTITUTION) {
                continue;
            }
            assertThat(type.isInstitution())
                .as("isInstitution 应仅 INSTITUTION 为 true，实际 %s 也返回 true", type)
                .isFalse();
        }
    }

    // ---------------------------------------------------------------------
    // 集合判定（互推验证：PERSONAL 非官方、其余 4 种官方；NONE 不要求材料、其余 5 种要求）
    // ---------------------------------------------------------------------

    @Test
    void isOfficialType_isTrueForBigVOfficialEnterpriseInstitutionOnly() {
        // 审计明确要求：PERSONAL 不是官方类
        assertThat(VerifyType.BIG_V.isOfficialType()).isTrue();
        assertThat(VerifyType.OFFICIAL.isOfficialType()).isTrue();
        assertThat(VerifyType.ENTERPRISE.isOfficialType()).isTrue();
        assertThat(VerifyType.INSTITUTION.isOfficialType()).isTrue();
        assertThat(VerifyType.PERSONAL.isOfficialType()).isFalse();
        assertThat(VerifyType.NONE.isOfficialType()).isFalse();
    }

    @Test
    void requiresVerificationMaterial_isFalseOnlyForNone() {
        // 审计明确要求：除 NONE 外其他 5 个全部需要材料
        assertThat(VerifyType.NONE.requiresVerificationMaterial()).isFalse();
        assertThat(VerifyType.PERSONAL.requiresVerificationMaterial()).isTrue();
        assertThat(VerifyType.BIG_V.requiresVerificationMaterial()).isTrue();
        assertThat(VerifyType.OFFICIAL.requiresVerificationMaterial()).isTrue();
        assertThat(VerifyType.ENTERPRISE.requiresVerificationMaterial()).isTrue();
        assertThat(VerifyType.INSTITUTION.requiresVerificationMaterial()).isTrue();
    }

    /**
     * 互推关系：isOfficialType 与 requiresVerificationMaterial 的补集必须不同
     * —— PERSONAL 不在官方类，但需要材料；NONE 不在官方类，也不需要材料。
     */
    @Test
    void officialTypeAndMaterialRequirement_setsAreNotIdentical() {
        int officialCount = 0;
        int materialCount = 0;
        for (VerifyType type : VerifyType.values()) {
            if (type.isOfficialType()) {
                officialCount++;
            }
            if (type.requiresVerificationMaterial()) {
                materialCount++;
            }
        }
        // 官方类 4 个，材料要求 5 个，二者集合大小不同
        assertThat(officialCount).isEqualTo(4);
        assertThat(materialCount).isEqualTo(5);
        // NONE 是补集差异：不在官方类 ∩ 不需要材料
        assertThat(VerifyType.NONE.isOfficialType()).isFalse();
        assertThat(VerifyType.NONE.requiresVerificationMaterial()).isFalse();
        // PERSONAL 是补集差异：不在官方类 ∩ 需要材料
        assertThat(VerifyType.PERSONAL.isOfficialType()).isFalse();
        assertThat(VerifyType.PERSONAL.requiresVerificationMaterial()).isTrue();
    }
}
