package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.enums.ContentUserFilterRuleTypeEnum;
import org.jeecg.modules.content.user.enums.ContentUserProtectionStatusEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内容社区拉黑屏蔽枚举契约测试。
 */
class ContentBlockingMutingEnumContractTest {

    @Test
    void shouldExposeProtectionStatusesAndRuleTypes() {
        assertThat(ContentUserProtectionStatusEnum.codes())
            .containsExactly("ACTIVE", "CANCELLED", "EXPIRED");
        assertThat(ContentUserFilterRuleTypeEnum.codes())
            .containsExactly("WORD", "REGEX", "TOPIC", "CONTENT_TYPE");
    }

    @Test
    void shouldParseCodesWithCaseAndBlankTolerance() {
        assertThat(ContentUserProtectionStatusEnum.ofCode(" active ")).isEqualTo(ContentUserProtectionStatusEnum.ACTIVE);
        assertThat(ContentUserFilterRuleTypeEnum.ofCode(" topic ")).isEqualTo(ContentUserFilterRuleTypeEnum.TOPIC);
    }

    @Test
    void shouldRejectNullBlankIllegalAndOverLengthStatusCodes() {
        assertThatThrownBy(() -> ContentUserProtectionStatusEnum.ofCode(null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> ContentUserProtectionStatusEnum.ofCode(" ")).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> ContentUserProtectionStatusEnum.ofCode("UNKNOWN")).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> ContentUserProtectionStatusEnum.ofCode("A".repeat(33))).isInstanceOf(JeecgBootException.class);
    }

    @Test
    void shouldRejectNullBlankIllegalAndOverLengthRuleTypeCodes() {
        assertThatThrownBy(() -> ContentUserFilterRuleTypeEnum.ofCode(null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> ContentUserFilterRuleTypeEnum.ofCode(" ")).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> ContentUserFilterRuleTypeEnum.ofCode("UNKNOWN")).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> ContentUserFilterRuleTypeEnum.ofCode("A".repeat(33))).isInstanceOf(JeecgBootException.class);
    }
}
