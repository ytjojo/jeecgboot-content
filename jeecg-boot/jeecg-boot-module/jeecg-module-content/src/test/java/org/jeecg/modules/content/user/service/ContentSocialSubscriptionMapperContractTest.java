package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.entity.ContentUserFollowRecommendation;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionNotificationPreferenceMapper;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionSourceMapper;
import org.jeecg.modules.content.user.mapper.ContentUserActivitySnapshotMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFollowRecommendationMapper;
import org.jeecg.modules.content.user.service.impl.ContentSubscriptionNotificationPreferenceServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentSubscriptionSourceServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserActivitySnapshotServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserFeedSettingServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserFollowRecommendationServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区社交订阅 Mapper 与基础 Service 契约测试。
 */
class ContentSocialSubscriptionMapperContractTest {

    @Test
    void shouldExposeBaseMapperForNewTables() {
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserFeedSettingMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserActivitySnapshotMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserFollowRecommendationMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentSubscriptionSourceMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentSubscriptionNotificationPreferenceMapper.class);
    }

    @Test
    void shouldExposeMybatisPlusCrudServicesForNewTables() {
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserFeedSettingServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserActivitySnapshotServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserFollowRecommendationServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentSubscriptionSourceServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentSubscriptionNotificationPreferenceServiceImpl.class);
    }

    @Test
    void shouldKeepMapperLookupMethodsForUniqueBusinessKeys() throws NoSuchMethodException {
        Method selectFeedSetting = ContentUserFeedSettingMapper.class.getMethod("selectByUserId", String.class);
        Method selectSource = ContentSubscriptionSourceMapper.class.getMethod("selectBySource", String.class, String.class);
        Method selectPreference = ContentSubscriptionNotificationPreferenceMapper.class.getMethod("selectBySubscriptionId", String.class);

        assertThat(selectFeedSetting.getReturnType()).isEqualTo(ContentUserFeedSetting.class);
        assertThat(selectSource.getReturnType()).isEqualTo(ContentSubscriptionSource.class);
        assertThat(selectPreference.getReturnType()).isEqualTo(ContentSubscriptionNotificationPreference.class);
    }

    @Test
    void shouldUseNullableJavaTypesForOptionalSnapshotAndPreferenceFields() throws NoSuchFieldException {
        assertThat(ContentUserActivitySnapshot.class.getDeclaredField("summary").getType()).isEqualTo(String.class);
        assertThat(ContentUserActivitySnapshot.class.getDeclaredField("activityTime").getType()).isEqualTo(java.util.Date.class);
        assertThat(ContentUserFollowRecommendation.class.getDeclaredField("expiresAt").getType()).isEqualTo(java.util.Date.class);
        assertThat(ContentSubscriptionSource.class.getDeclaredField("category").getType()).isEqualTo(String.class);
        assertThat(ContentSubscriptionSource.class.getDeclaredField("coverUrl").getType()).isEqualTo(String.class);
        assertThat(ContentSubscriptionNotificationPreference.class.getDeclaredField("dndStartTime").getType()).isEqualTo(String.class);
        assertThat(ContentSubscriptionNotificationPreference.class.getDeclaredField("dndEndTime").getType()).isEqualTo(String.class);
    }
}
