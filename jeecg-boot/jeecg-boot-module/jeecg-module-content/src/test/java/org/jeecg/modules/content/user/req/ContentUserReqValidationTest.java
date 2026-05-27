package org.jeecg.modules.content.user.req;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jeecg.modules.content.user.req.account.ContentAccountBindEmailReq;
import org.jeecg.modules.content.user.req.account.ContentAccountBindMobileReq;
import org.jeecg.modules.content.user.req.account.ContentAccountUnbindEmailReq;
import org.jeecg.modules.content.user.req.account.ContentAccountUnbindMobileReq;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.req.growth.ContentPointAdjustReq;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.req.relation.ContentBatchRelationReq;
import org.jeecg.modules.content.user.req.relation.ContentFollowReq;
import org.jeecg.modules.content.user.req.settings.ContentNotificationChannelConfigReq;
import org.jeecg.modules.content.user.req.settings.ContentNotificationDndRuleReq;
import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.dto.ContentUserRewardEventDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentUserReqValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectInvalidRegisterRequest() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setMobile("123")
            .setPassword("123")
            .setNickname("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("mobile"));
        assertTrue(fields.contains("password"));
        assertTrue(fields.contains("nickname"));
    }

    @Test
    void shouldRejectInvalidEmailRegisterRequest() {
        ContentEmailRegisterReq req = new ContentEmailRegisterReq()
            .setEmail("bad")
            .setPassword("123")
            .setNickname("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("email"));
        assertTrue(fields.contains("password"));
        assertTrue(fields.contains("nickname"));
    }

    @Test
    void shouldRejectInvalidBindMobileRequest() {
        ContentAccountBindMobileReq req = new ContentAccountBindMobileReq()
            .setUserId("")
            .setMobile("123")
            .setSecondaryVerified(null);

        Set<String> fields = validate(req);

        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("mobile"));
    }

    @Test
    void shouldRejectInvalidBindEmailRequest() {
        ContentAccountBindEmailReq req = new ContentAccountBindEmailReq()
            .setUserId("")
            .setEmail("bad");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("email"));
    }

    @Test
    void shouldRejectInvalidUnbindRequests() {
        ContentAccountUnbindMobileReq unbindMobileReq = new ContentAccountUnbindMobileReq()
            .setUserId("");
        ContentAccountUnbindEmailReq unbindEmailReq = new ContentAccountUnbindEmailReq()
            .setUserId("");

        Set<String> unbindMobileFields = validate(unbindMobileReq);
        Set<String> unbindEmailFields = validate(unbindEmailReq);

        assertTrue(unbindMobileFields.contains("userId"));
        assertTrue(unbindEmailFields.contains("userId"));
    }

    @Test
    void shouldRejectInvalidStatusChangeRequest() {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("")
            .setCurrentStatus("INVALID")
            .setTargetStatus("NORMAL")
            .setOperatorUserId("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("currentStatus"));
        assertTrue(fields.contains("operatorUserId"));
    }

    @Test
    void shouldRejectInvalidPointAdjustRequest() {
        ContentPointAdjustReq req = new ContentPointAdjustReq()
            .setUserId("")
            .setSourceType("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("sourceType"));
    }

    @Test
    void shouldRejectInvalidRewardEventRequest() {
        ContentUserRewardEventDTO req = new ContentUserRewardEventDTO()
            .setUserId("")
            .setSourceType("")
            .setEventId("")
            .setBizId("x".repeat(65))
            .setDailyBucket("2026-01-01");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("sourceType"));
        assertTrue(fields.contains("eventId"));
        assertTrue(fields.contains("bizId"));
        assertTrue(fields.contains("dailyBucket"));
    }

    @Test
    void shouldRejectInvalidPrivacyUpdateRequest() {
        ContentUserPrivacyUpdateReq req = new ContentUserPrivacyUpdateReq()
            .setBirthdayVisibility("UNKNOWN");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("birthdayVisibility"));
    }

    @Test
    void shouldRejectInvalidOnlineStatusVisibility() {
        ContentUserPrivacyUpdateReq req = new ContentUserPrivacyUpdateReq()
            .setOnlineStatusVisibility("INVALID");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("onlineStatusVisibility"));
    }

    @Test
    void shouldAcceptValidOnlineStatusVisibility() {
        for (String value : List.of("PUBLIC", "HIDDEN", "MUTUAL_ONLY")) {
            ContentUserPrivacyUpdateReq req = new ContentUserPrivacyUpdateReq()
                .setOnlineStatusVisibility(value);

            Set<String> fields = validate(req);

            assertTrue(!fields.contains("onlineStatusVisibility"),
                "Expected no violation for onlineStatusVisibility='" + value + "'");
        }
    }

    @Test
    void shouldAcceptNullOnlineStatusVisibility() {
        ContentUserPrivacyUpdateReq req = new ContentUserPrivacyUpdateReq();

        Set<String> fields = validate(req);

        assertTrue(!fields.contains("onlineStatusVisibility"),
            "Expected no violation for null onlineStatusVisibility");
    }

    @Test
    void shouldRejectInvalidProfileUpdateRequest() {
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname("123456789012345678901")
            .setBio("x".repeat(501));

        Set<String> fields = validate(req);

        assertTrue(fields.contains("nickname"));
        assertTrue(fields.contains("bio"));
    }

    @Test
    void shouldRejectInvalidNotificationUpdateRequest() {
        ContentUserNotificationUpdateReq req = new ContentUserNotificationUpdateReq()
            .setChannelConfig(new ContentNotificationChannelConfigReq()
                .setLikeChannels(List.of("BAD")))
            .setDndRule(new ContentNotificationDndRuleReq()
                .setEnabled(Boolean.TRUE)
                .setStartTime("25:00"));

        Set<String> fields = validate(req);

        assertTrue(fields.contains("channelConfig.likeChannels[0].<list element>"));
        assertTrue(fields.contains("dndRule.startTime"));
    }

    @Test
    void shouldRejectInvalidBatchRelationRequest() {
        ContentBatchRelationReq req = new ContentBatchRelationReq()
            .setTargetUserIds(List.of("", "u2"));

        Set<String> fields = validate(req);

        assertTrue(fields.contains("targetUserIds[0].<list element>"));
    }

    @Test
    void shouldRejectInvalidFollowRequest() {
        ContentFollowReq req = new ContentFollowReq()
            .setTargetUserId("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("targetUserId"));
    }

    @Test
    void shouldRejectInvalidSubscriptionRequest() {
        ContentSubscriptionReq req = new ContentSubscriptionReq()
            .setSourceType("")
            .setSourceId("")
            .setSourceName("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("sourceType"));
        assertTrue(fields.contains("sourceId"));
        assertTrue(fields.contains("sourceName"));
    }

    @Test
    void shouldRejectInvalidAppealRequest() {
        ContentAppealCreateReq req = new ContentAppealCreateReq()
            .setUserId("")
            .setAppealType("")
            .setTargetId("")
            .setTargetType("")
            .setReason("");

        Set<String> fields = validate(req);

        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("appealType"));
        assertTrue(fields.contains("targetId"));
        assertTrue(fields.contains("targetType"));
        assertTrue(fields.contains("reason"));
    }

    private <T> Set<String> validate(T req) {
        return validator.validate(req).stream()
            .map(ConstraintViolation::getPropertyPath)
            .map(Object::toString)
            .collect(Collectors.toSet());
    }
}
