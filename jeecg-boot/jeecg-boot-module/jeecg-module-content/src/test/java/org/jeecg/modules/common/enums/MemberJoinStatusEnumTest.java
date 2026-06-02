package org.jeecg.modules.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MemberJoinStatusEnum} 单测。
 *
 * 覆盖审计报告 P1 项：状态机三段划分（isActive / isInactive / isPending），
 * 重点验证 PENDING/INVITING 不被算作非活跃，确保三集合对所有 6 个常量无遗漏无重叠。
 */
class MemberJoinStatusEnumTest {

    // ---------------------------------------------------------------------
    // 元数据契约
    // ---------------------------------------------------------------------

    @Test
    void shouldExpose6ConstantsWithUniqueValueAndCompleteMetadata() {
        assertThat(MemberJoinStatusEnum.values()).hasSize(6);

        for (MemberJoinStatusEnum status : MemberJoinStatusEnum.values()) {
            assertThat(status.getValue()).isNotNull();
            assertThat(status.getName()).isNotBlank();
            assertThat(status.getDescription()).isNotBlank();
        }

        assertThat(MemberJoinStatusEnum.values())
            .extracting(MemberJoinStatusEnum::getValue)
            .doesNotHaveDuplicates();
    }

    // ---------------------------------------------------------------------
    // getByValue
    // ---------------------------------------------------------------------

    @Test
    void getByValue_shouldReturnEnumForEachDefinedValue() {
        assertThat(MemberJoinStatusEnum.getByValue(0)).isEqualTo(MemberJoinStatusEnum.EXITED);
        assertThat(MemberJoinStatusEnum.getByValue(1)).isEqualTo(MemberJoinStatusEnum.NORMAL);
        assertThat(MemberJoinStatusEnum.getByValue(2)).isEqualTo(MemberJoinStatusEnum.PENDING);
        assertThat(MemberJoinStatusEnum.getByValue(3)).isEqualTo(MemberJoinStatusEnum.KICKED);
        assertThat(MemberJoinStatusEnum.getByValue(4)).isEqualTo(MemberJoinStatusEnum.REJECTED);
        assertThat(MemberJoinStatusEnum.getByValue(5)).isEqualTo(MemberJoinStatusEnum.INVITING);
    }

    @Test
    void getByValue_shouldReturnNullForUnknownValue() {
        assertThat(MemberJoinStatusEnum.getByValue(99)).isNull();
        assertThat(MemberJoinStatusEnum.getByValue(-1)).isNull();
    }

    @Test
    void getByValue_shouldReturnNullForNullInput() {
        assertThat(MemberJoinStatusEnum.getByValue(null)).isNull();
    }

    // ---------------------------------------------------------------------
    // getByName
    // ---------------------------------------------------------------------

    @Test
    void getByName_shouldReturnEnumForEachDefinedName() {
        assertThat(MemberJoinStatusEnum.getByName("exited")).isEqualTo(MemberJoinStatusEnum.EXITED);
        assertThat(MemberJoinStatusEnum.getByName("normal")).isEqualTo(MemberJoinStatusEnum.NORMAL);
        assertThat(MemberJoinStatusEnum.getByName("pending")).isEqualTo(MemberJoinStatusEnum.PENDING);
        assertThat(MemberJoinStatusEnum.getByName("kicked")).isEqualTo(MemberJoinStatusEnum.KICKED);
        assertThat(MemberJoinStatusEnum.getByName("rejected")).isEqualTo(MemberJoinStatusEnum.REJECTED);
        assertThat(MemberJoinStatusEnum.getByName("inviting")).isEqualTo(MemberJoinStatusEnum.INVITING);
    }

    @Test
    void getByName_shouldReturnNullForUnknownName() {
        assertThat(MemberJoinStatusEnum.getByName("unknown")).isNull();
    }

    @Test
    void getByName_shouldReturnNullForNullOrBlank() {
        assertThat(MemberJoinStatusEnum.getByName(null)).isNull();
        assertThat(MemberJoinStatusEnum.getByName("")).isNull();
        assertThat(MemberJoinStatusEnum.getByName("  ")).isNull();
    }

    // ---------------------------------------------------------------------
    // 状态机三段划分
    // ---------------------------------------------------------------------

    @Test
    void isActive_isTrueOnlyForNormal() {
        assertThat(MemberJoinStatusEnum.NORMAL.isActive()).isTrue();
        assertThat(MemberJoinStatusEnum.EXITED.isActive()).isFalse();
        assertThat(MemberJoinStatusEnum.PENDING.isActive()).isFalse();
        assertThat(MemberJoinStatusEnum.KICKED.isActive()).isFalse();
        assertThat(MemberJoinStatusEnum.REJECTED.isActive()).isFalse();
        assertThat(MemberJoinStatusEnum.INVITING.isActive()).isFalse();
    }

    @Test
    void isInactive_isTrueOnlyForExitedKickedRejected() {
        // 审计特别指出：PENDING/INVITING 绝不能被算作非活跃
        assertThat(MemberJoinStatusEnum.EXITED.isInactive()).isTrue();
        assertThat(MemberJoinStatusEnum.KICKED.isInactive()).isTrue();
        assertThat(MemberJoinStatusEnum.REJECTED.isInactive()).isTrue();
        assertThat(MemberJoinStatusEnum.NORMAL.isInactive()).isFalse();
        assertThat(MemberJoinStatusEnum.PENDING.isInactive()).isFalse();
        assertThat(MemberJoinStatusEnum.INVITING.isInactive()).isFalse();
    }

    @Test
    void isPending_isTrueOnlyForPendingAndInviting() {
        assertThat(MemberJoinStatusEnum.PENDING.isPending()).isTrue();
        assertThat(MemberJoinStatusEnum.INVITING.isPending()).isTrue();
        assertThat(MemberJoinStatusEnum.NORMAL.isPending()).isFalse();
        assertThat(MemberJoinStatusEnum.EXITED.isPending()).isFalse();
        assertThat(MemberJoinStatusEnum.KICKED.isPending()).isFalse();
        assertThat(MemberJoinStatusEnum.REJECTED.isPending()).isFalse();
    }

    /**
     * 三段集合对所有 6 个常量无遗漏、无重叠：
     * NORMAL(active) / EXITED,KICKED,REJECTED(inactive) / PENDING,INVITING(pending) = 6。
     */
    @Test
    void statePartition_shouldCoverAll6ConstantsWithoutOverlap() {
        int covered = 0;
        for (MemberJoinStatusEnum status : MemberJoinStatusEnum.values()) {
            int hits = (status.isActive() ? 1 : 0)
                + (status.isInactive() ? 1 : 0)
                + (status.isPending() ? 1 : 0);
            assertThat(hits)
                .as("状态 %s 应恰好属于一个分区", status)
                .isEqualTo(1);
            covered++;
        }
        assertThat(covered).isEqualTo(6);
    }
}
