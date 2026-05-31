package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.enums.PublishPermissionEnum;
import org.jeecg.modules.content.channel.service.impl.ChannelContentPublishServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChannelContentPublishServiceTest {

    private ChannelContentPublishServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChannelContentPublishServiceImpl();
    }

    @Test
    void adminOnly_shouldRejectNormalMember() {
        String result = service.checkPublishPermission("MEMBER", PublishPermissionEnum.ADMIN_ONLY.getCode(), false, false);
        assertEquals("REJECT", result);
    }

    @Test
    void adminOnly_shouldAllowAdmin() {
        String result = service.checkPublishPermission("ADMIN", PublishPermissionEnum.ADMIN_ONLY.getCode(), false, false);
        assertEquals("ALLOW", result);
    }

    @Test
    void preReview_shouldReturnReview() {
        String result = service.checkPublishPermission("MEMBER", PublishPermissionEnum.PRE_REVIEW.getCode(), false, false);
        assertEquals("REVIEW", result);
    }

    @Test
    void mutedUser_shouldBeRejected() {
        String result = service.checkPublishPermission("ADMIN", PublishPermissionEnum.ALL_MEMBERS.getCode(), true, false);
        assertEquals("REJECT", result);
    }

    @Test
    void blacklistedUser_shouldBeRejected() {
        String result = service.checkPublishPermission("ADMIN", PublishPermissionEnum.ALL_MEMBERS.getCode(), false, true);
        assertEquals("REJECT", result);
    }

    @Test
    void publicSubmit_nonMember_shouldReview() {
        String result = service.checkPublishPermission("NON_MEMBER", PublishPermissionEnum.PUBLIC_SUBMIT.getCode(), false, false);
        assertEquals("REVIEW", result);
    }

    @Test
    void allMembers_nonMember_shouldReject() {
        String result = service.checkPublishPermission("NON_MEMBER", PublishPermissionEnum.ALL_MEMBERS.getCode(), false, false);
        assertEquals("REJECT", result);
    }
}
