package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserPrivacySettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserProfileServiceTest {

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserPrivacySettingMapper privacyMapper;

    @Mock
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    @InjectMocks
    private ContentUserProfileServiceImpl profileService;

    @Test
    void shouldHideBirthdayWhenViewerIsNotFollower() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("author_1")
            .setNickname("作者")
            .setBirthday(new Date());
        ContentUserPrivacySetting privacy = new ContentUserPrivacySetting()
            .setUserId("author_1")
            .setBirthdayVisibility("FOLLOWERS_ONLY");
        when(profileMapper.selectByUserId("author_1")).thenReturn(profile);
        when(privacyMapper.selectByUserId("author_1")).thenReturn(privacy);
        when(visibilityPolicyService.canViewField("author_1", "viewer_2", "FOLLOWERS_ONLY")).thenReturn(false);

        assertThat(profileService.getProfile("author_1", "viewer_2").getBirthday()).isNull();
    }
}
