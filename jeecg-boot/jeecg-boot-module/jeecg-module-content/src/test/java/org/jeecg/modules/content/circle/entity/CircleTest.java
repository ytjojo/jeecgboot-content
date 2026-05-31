package org.jeecg.modules.content.circle.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Circle Entity")
class CircleTest {

    @Test
    @DisplayName("PrivacyType enum values")
    void privacyType_enumValues() {
        Circle.PrivacyType[] values = Circle.PrivacyType.values();
        assertEquals(3, values.length);
        assertNotNull(Circle.PrivacyType.valueOf("PUBLIC"));
        assertNotNull(Circle.PrivacyType.valueOf("PRIVATE"));
        assertNotNull(Circle.PrivacyType.valueOf("PASSWORD"));
    }

    @Test
    @DisplayName("JoinType enum values")
    void joinType_enumValues() {
        Circle.JoinType[] values = Circle.JoinType.values();
        assertEquals(4, values.length);
        assertNotNull(Circle.JoinType.valueOf("DIRECT"));
        assertNotNull(Circle.JoinType.valueOf("APPROVAL"));
        assertNotNull(Circle.JoinType.valueOf("INVITE"));
        assertNotNull(Circle.JoinType.valueOf("PASSWORD"));
    }

    @Test
    @DisplayName("Status enum values")
    void status_enumValues() {
        Circle.Status[] values = Circle.Status.values();
        assertEquals(2, values.length);
        assertNotNull(Circle.Status.valueOf("ACTIVE"));
        assertNotNull(Circle.Status.valueOf("DISABLED"));
    }
}
