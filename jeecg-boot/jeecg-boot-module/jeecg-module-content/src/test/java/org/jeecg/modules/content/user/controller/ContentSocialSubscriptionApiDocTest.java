package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.constant.ContentUserErrorCode;
import org.jeecg.modules.content.user.req.settings.ContentFeedSettingUpdateReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionNotificationPreferenceReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionSourceReq;
import org.jeecg.modules.content.user.vo.ContentFollowFeedItemVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 社交订阅接口文档契约测试。
 */
class ContentSocialSubscriptionApiDocTest {

    @Test
    void shouldDocumentRelationSubscriptionAndSettingsControllers() {
        assertThat(ContentUserRelationController.class.getAnnotation(Tag.class).name()).isEqualTo("内容社区用户关系");
        assertThat(ContentUserSubscriptionController.class.getAnnotation(Tag.class).name()).isEqualTo("内容社区用户订阅");
        assertThat(ContentUserSettingsController.class.getAnnotation(Tag.class).name()).isEqualTo("内容社区用户设置");

        assertOperations(ContentUserRelationController.class, "followFeed", "recommendations");
        assertOperations(ContentUserSubscriptionController.class, "feed", "plaza", "sourceDetail", "subscribeSource");
        assertOperations(ContentUserSettingsController.class, "getFeedSetting", "updateFeedSetting");
    }

    @Test
    void shouldDocumentKeyRequestAndResponseFields() throws NoSuchFieldException {
        assertThat(ContentFeedSettingUpdateReq.class.getDeclaredField("activityTypes").getAnnotation(Schema.class).description())
            .contains("启用");
        assertThat(ContentSubscriptionNotificationPreferenceReq.class.getDeclaredField("notificationChannels").getAnnotation(Schema.class).description())
            .contains("通知渠道");
        assertThat(ContentSubscriptionSourceReq.class.getDeclaredField("sourceType").getAnnotation(Schema.class).description())
            .contains("订阅源类型");
        assertThat(ContentFollowFeedItemVO.class.getDeclaredField("activityType").getAnnotation(Schema.class).description())
            .contains("动态类型");
        assertThat(ContentUserSubscriptionVO.class.getDeclaredField("notificationSummary").getAnnotation(Schema.class).description())
            .contains("通知摘要");
        assertThat(ContentSubscriptionSourceDetailVO.class.getDeclaredField("recentContentSummary").getAnnotation(Schema.class).description())
            .contains("最近内容摘要");
    }

    @Test
    void shouldExposeSocialSubscriptionErrorCodesAndResultErrorShape() {
        assertThat(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_INVALID).isEqualTo(5492);
        assertThat(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_FORBIDDEN).isEqualTo(5493);
        assertThat(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_INVALID).isEqualTo(5494);
        assertThat(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_NOT_FOUND).isEqualTo(5495);
        assertThat(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_NOTIFICATION_INVALID).isEqualTo(5496);

        JeecgBootException exception = new JeecgBootException("订阅源类型不支持",
            ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_INVALID);
        Result<?> result = Result.error(exception.getErrCode(), exception.getMessage());

        assertThat(result.getCode()).isEqualTo(ContentUserErrorCode.SOCIAL_SUBSCRIPTION_SOURCE_INVALID);
        assertThat(result.getMessage()).isEqualTo("订阅源类型不支持");
        assertThat(result.isSuccess()).isFalse();
    }

    private void assertOperations(Class<?> controllerClass, String... methodNames) {
        Set<String> expected = Set.of(methodNames);
        Set<String> documented = Arrays.stream(controllerClass.getDeclaredMethods())
            .filter(method -> expected.contains(method.getName()))
            .filter(method -> method.getAnnotation(Operation.class) != null)
            .map(Method::getName)
            .collect(Collectors.toSet());
        assertThat(documented).containsExactlyInAnyOrderElementsOf(expected);
    }
}
