package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.req.settings.ContentFeedSettingUpdateReq;
import org.jeecg.modules.content.user.service.impl.ContentUserFeedSettingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * 关注流设置服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserFeedSettingServiceTest {

    @Mock
    private ContentUserFeedSettingMapper feedSettingMapper;

    @InjectMocks
    private ContentUserFeedSettingServiceImpl feedSettingService;

    @Test
    void shouldReturnExistingFeedSetting() {
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserFeedSetting()
            .setUserId("u1")
            .setPublishEnabled(true)
            .setLikeEnabled(false)
            .setFavoriteEnabled(false)
            .setActivityTypes("PUBLISH"));

        var result = feedSettingService.getSetting("u1");

        assertThat(result.getActivityTypes()).containsExactly("PUBLISH");
        assertThat(result.getPublishEnabled()).isTrue();
        verify(feedSettingMapper, never()).insert(any(ContentUserFeedSetting.class));
    }

    @Test
    void shouldSaveNormalizedActivityTypes() {
        ContentUserFeedSetting setting = new ContentUserFeedSetting()
            .setUserId("u1")
            .setPublishEnabled(true)
            .setLikeEnabled(true)
            .setFavoriteEnabled(true)
            .setActivityTypes("PUBLISH,LIKE,FAVORITE");
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(setting);

        var result = feedSettingService.updateSetting("u1", new ContentFeedSettingUpdateReq()
            .setActivityTypes(List.of(" publish ", "favorite")));

        assertThat(setting.getActivityTypes()).isEqualTo("PUBLISH,FAVORITE");
        assertThat(setting.getPublishEnabled()).isTrue();
        assertThat(setting.getLikeEnabled()).isFalse();
        assertThat(setting.getFavoriteEnabled()).isTrue();
        assertThat(result.getActivityTypes()).containsExactly("PUBLISH", "FAVORITE");
        verify(feedSettingMapper).updateById(setting);
    }

    @Test
    void shouldRejectInvalidActivityTypesBeforeSaving() {
        assertThatThrownBy(() -> feedSettingService.updateSetting("u1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("动态类型列表不能为空");
        assertThatThrownBy(() -> feedSettingService.updateSetting("u1", new ContentFeedSettingUpdateReq()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("动态类型列表不能为空");
        assertThatThrownBy(() -> feedSettingService.updateSetting("u1", new ContentFeedSettingUpdateReq().setActivityTypes(List.of())))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("动态类型列表不能为空");
        assertThatThrownBy(() -> feedSettingService.updateSetting("u1", new ContentFeedSettingUpdateReq().setActivityTypes(List.of("PUBLISH", "PUBLISH"))))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("动态类型不能重复");
        assertThatThrownBy(() -> feedSettingService.updateSetting("u1", new ContentFeedSettingUpdateReq().setActivityTypes(List.of("UNKNOWN"))))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("动态类型不支持");

        verify(feedSettingMapper, never()).updateById(any(ContentUserFeedSetting.class));
    }
}
