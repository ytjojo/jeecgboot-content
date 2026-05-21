package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserProfileReview;
import org.jeecg.modules.content.user.biz.ContentUserRelationBoundaryBizService;
import org.jeecg.modules.content.user.mapper.ContentUserVerificationBadgeMapper;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private ContentUserRelationBoundaryBizService relationBoundaryBizService;

    @Mock
    private ContentUserProfileReviewMapper profileReviewMapper;

    @Mock
    private ContentUserHomepageModuleMapper homepageModuleMapper;

    @Mock
    private ContentUserVerificationBadgeMapper verificationBadgeMapper;

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
    void shouldRejectProfileWhenViewerHasBlockedOwner() {
        when(relationBoundaryBizService.isBlockedEitherWay("viewer_1", "author_2")).thenReturn(true);
        when(relationBoundaryBizService.isBlockedBy("viewer_1", "author_2")).thenReturn(true);

        assertThatThrownBy(() -> profileService.getProfile("author_2", "viewer_1"))
            .isInstanceOf(org.jeecg.common.exception.JeecgBootException.class)
            .hasMessage("您已拉黑该用户，无法查看其内容");
        verify(profileMapper, never()).selectByUserId("author_2");
    }

    @Test
    void shouldReturnNonRevealingErrorWhenViewerIsBlockedByOwner() {
        when(relationBoundaryBizService.isBlockedEitherWay("viewer_1", "author_2")).thenReturn(true);
        when(relationBoundaryBizService.isBlockedBy("viewer_1", "author_2")).thenReturn(false);

        assertThatThrownBy(() -> profileService.getProfile("author_2", "viewer_1"))
            .isInstanceOf(org.jeecg.common.exception.JeecgBootException.class)
            .hasMessage("用户不存在");
        verify(profileMapper, never()).selectByUserId("author_2");
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

    @Test
    void shouldAllowFifthProfileUpdateAndRejectSixthForDailyQuota() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname("新昵称")
            .setAvatar("https://cdn.example.com/new.png");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(5L, 6L);
        when(profileAuditAdapter.review(any(ContentUserProfileUpdateReq.class)))
            .thenReturn(new IContentUserProfileAuditAdapter.AuditResult(false, null));

        profileService.updateProfile("u1", req);

        assertThatThrownBy(() -> profileService.updateProfile("u1", req))
            .hasMessageContaining("今日资料修改次数已达上限");
        verify(valueOperations, times(2)).increment(anyString());
    }

    @Test
    void shouldExpireProfileQuotaKeyOnFirstDailyUpdate() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname("新昵称")
            .setAvatar("https://cdn.example.com/new.png");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(profileAuditAdapter.review(any(ContentUserProfileUpdateReq.class)))
            .thenReturn(new IContentUserProfileAuditAdapter.AuditResult(false, null));

        profileService.updateProfile("u1", req);

        verify(redisTemplate).expire(anyString(), org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(TimeUnit.DAYS));
    }

    @Test
    void shouldRejectPendingReviewBeforeChangingProfileAgain() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq()
            .setNickname("新昵称")
            .setAvatar("https://cdn.example.com/new.png");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(profileReviewMapper.selectPendingByUserId("u1")).thenReturn(new ContentUserProfileReview());

        assertThatThrownBy(() -> profileService.updateProfile("u1", req))
            .hasMessageContaining("资料正在审核中");

        verify(profileMapper, never()).updateById(any(ContentUserProfile.class));
    }

    @Test
    void shouldApproveReviewAndRecordEffectiveHistory() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileReview review = new ContentUserProfileReview()
            .setUserId("u1")
            .setReviewStatus("PENDING")
            .setTargetSnapshotJson("{\"nickname\":\"新昵称\",\"avatar\":\"https://cdn.example.com/new.png\"}");
        review.setId("review1");
        when(profileReviewMapper.selectById("review1")).thenReturn(review);
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);

        profileService.handleProfileReview(new org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq()
            .setReviewId("review1")
            .setReviewStatus("APPROVED")
            .setOperatorUserId("admin"));

        verify(historyService).recordEffectiveChange("u1", "NICKNAME", "旧昵称", "review1");
        verify(historyService).recordEffectiveChange("u1", "AVATAR", "https://cdn.example.com/old.png", "review1");
        verify(profileMapper).updateById(org.mockito.ArgumentMatchers.argThat(
            (ContentUserProfile item) -> "新昵称".equals(item.getNickname()) && "NONE".equals(item.getProfileReviewStatus())
        ));
    }

    @Test
    void shouldRejectReviewAndKeepOldProfileValue() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        ContentUserProfileReview review = new ContentUserProfileReview()
            .setUserId("u1")
            .setReviewStatus("PENDING")
            .setTargetSnapshotJson("{\"nickname\":\"新昵称\",\"avatar\":\"https://cdn.example.com/new.png\"}");
        review.setId("review1");
        when(profileReviewMapper.selectById("review1")).thenReturn(review);
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);

        profileService.handleProfileReview(new org.jeecg.modules.content.user.req.profile.ContentUserReviewHandleReq()
            .setReviewId("review1")
            .setReviewStatus("REJECTED")
            .setRejectReason("昵称违规")
            .setOperatorUserId("admin"));

        assertThat(profile.getNickname()).isEqualTo("旧昵称");
        assertThat(review.getRejectReason()).isEqualTo("昵称违规");
        verify(historyService, never()).recordEffectiveChange(anyString(), anyString(), anyString(), any());
    }

    @Test
    void shouldRejectInvalidProfileFieldsBeforePublishing() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setNickname("旧昵称")
            .setAvatar("https://cdn.example.com/old.png");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);

        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("").setAvatar("a.png")))
            .hasMessageContaining("昵称不能为空");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("123456789012345678901").setAvatar("a.png")))
            .hasMessageContaining("昵称长度不能超过20位");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("新昵称").setAvatar("a.png").setBio("a".repeat(501))))
            .hasMessageContaining("个人简介长度不能超过500位");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("新昵称").setAvatar("a.png").setGender(9)))
            .hasMessageContaining("性别取值不合法");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("新昵称").setAvatar("a.png").setBirthday(new Date(System.currentTimeMillis() + 86_400_000L))))
            .hasMessageContaining("生日不能晚于当前日期");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("新昵称").setAvatar("a.png").setRegion("a".repeat(65))))
            .hasMessageContaining("地区长度不能超过64位");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("新昵称").setAvatar("a.png").setProfession("a".repeat(65))))
            .hasMessageContaining("职业长度不能超过64位");
        assertThatThrownBy(() -> profileService.updateProfile("u1", new ContentUserProfileUpdateReq().setNickname("新昵称").setAvatar("a.png").setPersonalLink("ftp://example.com")))
            .hasMessageContaining("个人链接格式不合法");
    }

    @Test
    void shouldApplyAllPrivacyFieldVisibilityAndEvictPublicCache() {
        ContentUserPrivacySetting privacy = new ContentUserPrivacySetting()
            .setUserId("u1")
            .setBirthdayVisibility("PUBLIC")
            .setGenderVisibility("PUBLIC");
        when(privacyMapper.selectByUserId("u1")).thenReturn(privacy);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        profileService.updatePrivacy("u1", new org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq()
            .setBirthdayVisibility("PRIVATE")
            .setGenderVisibility("FOLLOWERS_ONLY")
            .setRegionVisibility("MUTUAL_ONLY")
            .setProfessionVisibility("PRIVATE")
            .setPersonalLinkVisibility("PUBLIC")
            .setVerificationBadgeVisibility("PRIVATE")
            .setContactBadgeVisibility("PRIVATE")
            .setHomepageVisibility("PUBLIC")
            .setDynamicVisibility("FOLLOWERS_ONLY"));

        assertThat(privacy.getBirthdayVisibility()).isEqualTo("PRIVATE");
        verify(redisTemplate).delete("content:user:profile:public:u1");
    }

    @Test
    void shouldRejectPrivacyLimitAndInvalidVisibility() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(11L);

        assertThatThrownBy(() -> profileService.updatePrivacy("u1", new org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq()))
            .hasMessageContaining("隐私设置修改过于频繁");

        when(valueOperations.increment(anyString())).thenReturn(1L);
        assertThatThrownBy(() -> profileService.updatePrivacy("u1", new org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq()
            .setBirthdayVisibility("BAD")))
            .hasMessageContaining("可见范围取值不合法");
    }

    @Test
    void shouldInitializeLegacyCertificationAndDefaultHomepageModulesIdempotently() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setCertificationType("PERSONAL")
            .setCertificationLabel("个人认证");
        when(profileMapper.selectList(null)).thenReturn(List.of(profile));
        when(homepageModuleMapper.selectByUserId("u1")).thenReturn(List.of(), List.of(new org.jeecg.modules.content.user.entity.ContentUserHomepageModule()));
        when(verificationBadgeMapper.selectActiveByUserId("u1")).thenReturn(List.of(), List.of(new org.jeecg.modules.content.user.entity.ContentUserVerificationBadge()));

        assertThat(profileService.initializeCompatibilityData()).isEqualTo(5);
        assertThat(profileService.initializeCompatibilityData()).isZero();

        verify(homepageModuleMapper, times(4)).insert(any(org.jeecg.modules.content.user.entity.ContentUserHomepageModule.class));
        verify(verificationBadgeMapper).insert(any(org.jeecg.modules.content.user.entity.ContentUserVerificationBadge.class));
    }
}
