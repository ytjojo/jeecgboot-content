package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserProfileReview;
import org.jeecg.modules.content.user.mapper.ContentUserHomepageModuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPrivacySettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileReviewMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.impl.ContentUserProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserProfileServiceTest {

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserPrivacySettingMapper privacyMapper;

    @Mock
    private IContentUserVisibilityPolicyService visibilityPolicyService;

    @Mock
    private ContentUserProfileReviewMapper profileReviewMapper;

    @Mock
    private ContentUserHomepageModuleMapper homepageModuleMapper;

    @Mock
    private IContentUserProfileAuditAdapter profileAuditAdapter;

    @Mock
    private IContentUserMediaAdapter mediaAdapter;

    @Mock
    private IContentUserProfileHistoryService historyService;

    @Mock
    private IContentUserVerificationBadgeService verificationBadgeService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

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

    @Test
    void shouldSendRiskyProfileUpdateToReviewInsteadOfPublishing() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname("含有违规词")
            .setAvatar("https://cdn.example.com/new.png");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(profileAuditAdapter.review(any(ContentUserProfileUpdateReq.class)))
            .thenReturn(new IContentUserProfileAuditAdapter.AuditResult(true, "资料命中风险规则"));

        profileService.updateProfile("u1", req);

        verify(profileReviewMapper).insert(any(ContentUserProfileReview.class));
        verify(profileMapper, never()).updateById(org.mockito.ArgumentMatchers.argThat(
            (ContentUserProfile item) -> "含有违规词".equals(item.getNickname())
        ));
    }

    @Test
    void shouldRejectProfileUpdateWhenDailyLimitExceeded() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname("新昵称")
            .setAvatar("https://cdn.example.com/new.png");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(6L);

        assertThatThrownBy(() -> profileService.updateProfile("u1", req))
            .hasMessageContaining("今日资料修改次数已达上限");

        verify(profileMapper, never()).updateById(any(ContentUserProfile.class));
    }
}
