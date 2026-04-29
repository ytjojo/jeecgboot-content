package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentUserEnumContractTest {

    @Test
    void shouldExposeExpectedLifecycleStatuses() {
        assertThat(ContentUserStatusEnum.codes())
            .containsExactly(
                "GUEST",
                "REGISTERED_INCOMPLETE",
                "NORMAL",
                "MUTED",
                "RECOMMENDATION_LIMITED",
                "FROZEN",
                "BANNED",
                "CANCEL_PENDING",
                "CANCELLED"
            );
    }
}
