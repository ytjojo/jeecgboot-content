package org.jeecg.modules.content.user.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecg.modules.content.user.constant.ContentUserErrorCode;
import org.jeecg.modules.content.user.controller.ContentUserProfileController;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区资料接口文档与错误码契约测试。
 */
class ContentUserProfileApiDocTest {

    @Test
    void shouldDocumentProfileControllerOperations() {
        for (Method method : ContentUserProfileController.class.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                assertThat(method.getAnnotation(Operation.class))
                    .as(method.getName() + " 必须有 Knife4j Operation")
                    .isNotNull();
            }
        }
    }

    @Test
    void shouldDocumentRequestAndResponseFieldsWithChineseDescriptions() {
        assertChineseSchema(ContentUserProfileUpdateReq.class, "nickname", "昵称");
        assertChineseSchema(ContentUserProfileUpdateReq.class, "avatar", "头像");
        assertChineseSchema(ContentUserHomepageUpdateReq.class, "themeColor", "主题色");
        assertChineseSchema(ContentUserPrivacyUpdateReq.class, "verificationBadgeVisibility", "认证标识可见范围");
        assertChineseSchema(ContentUserProfileVO.class, "verificationBadges", "认证标识列表");
    }

    @Test
    void shouldExposeProfileManagementErrorCodes() {
        assertThat(ContentUserErrorCode.PROFILE_UPDATE_INVALID).isEqualTo(5500);
        assertThat(ContentUserErrorCode.PROFILE_UPDATE_LIMIT_EXCEEDED).isEqualTo(5501);
        assertThat(ContentUserErrorCode.PROFILE_HOMEPAGE_INVALID).isEqualTo(5510);
        assertThat(ContentUserErrorCode.PROFILE_VERIFICATION_BADGE_NOT_FOUND).isEqualTo(5520);
        assertThat(ContentUserErrorCode.PROFILE_PRIVACY_INVALID).isEqualTo(5530);
        assertThat(ContentUserErrorCode.PROFILE_HISTORY_UNAVAILABLE).isEqualTo(5540);
    }

    private void assertChineseSchema(Class<?> type, String fieldName, String expectedDescription) {
        try {
            Schema schema = type.getDeclaredField(fieldName).getAnnotation(Schema.class);
            assertThat(schema).as(type.getSimpleName() + "." + fieldName).isNotNull();
            assertThat(schema.description()).contains(expectedDescription);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
    }
}
