package org.jeecg.modules.content.circle.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CircleGovernanceLog Entity")
class CircleGovernanceLogTest {

    @Test
    @DisplayName("Action enum values")
    void action_enumValues() {
        CircleGovernanceLog.Action[] values = CircleGovernanceLog.Action.values();
        assertEquals(4, values.length);
        assertNotNull(CircleGovernanceLog.Action.valueOf("MUTE"));
        assertNotNull(CircleGovernanceLog.Action.valueOf("UNMUTE"));
        assertNotNull(CircleGovernanceLog.Action.valueOf("REMOVE"));
        assertNotNull(CircleGovernanceLog.Action.valueOf("ROLE_CHANGE"));
    }
}
