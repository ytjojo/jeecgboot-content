package org.jeecg.modules.content.circle.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CircleMember Entity")
class CircleMemberTest {

    @Test
    @DisplayName("Role enum values")
    void role_enumValues() {
        CircleMember.Role[] values = CircleMember.Role.values();
        assertEquals(3, values.length);
        assertNotNull(CircleMember.Role.valueOf("CREATOR"));
        assertNotNull(CircleMember.Role.valueOf("MODERATOR"));
        assertNotNull(CircleMember.Role.valueOf("MEMBER"));
    }

    @Test
    @DisplayName("Status enum values")
    void status_enumValues() {
        CircleMember.Status[] values = CircleMember.Status.values();
        assertEquals(3, values.length);
        assertNotNull(CircleMember.Status.valueOf("ACTIVE"));
        assertNotNull(CircleMember.Status.valueOf("MUTED"));
        assertNotNull(CircleMember.Status.valueOf("REMOVED"));
    }
}
