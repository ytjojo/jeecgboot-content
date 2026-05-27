package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.entity.ContentUserPrivacySetting;
import org.jeecg.modules.content.user.mapper.ContentUserPrivacySettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserVisibilityPolicyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserVisibilityPolicyServiceTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @Mock
    private ContentUserPrivacySettingMapper privacySettingMapper;

    @InjectMocks
    private ContentUserVisibilityPolicyServiceImpl policyService;

    @Test
    void shouldRejectUserSearchWhenOwnerDisablesSearch() {
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("owner")
            .setAllowUserSearch(false);
        when(privacySettingMapper.selectByUserId("owner")).thenReturn(privacySetting);

        assertThat(policyService.canSearchUser("owner", "viewer")).isFalse();
    }

    @Test
    void shouldStillAllowContentViewWhenViewerMutedOwner() {
        when(relationMapper.selectByPair("owner", "viewer")).thenReturn(null);

        assertThat(policyService.canViewContent("owner", "viewer")).isTrue();
    }

    @Test
    void shouldRejectContentViewWhenOwnerBlocksViewer() {
        ContentUserRelation ownerToViewer = new ContentUserRelation()
            .setOwnerUserId("owner")
            .setTargetUserId("viewer")
            .setBlockedByOwner(true);
        when(relationMapper.selectByPair("owner", "viewer")).thenReturn(ownerToViewer);

        assertThat(policyService.canViewContent("owner", "viewer")).isFalse();
    }

    @Test
    void shouldRejectContentViewWhenViewerHasBlacklistedOwner() {
        ContentUserRelation viewerToOwner = new ContentUserRelation()
            .setOwnerUserId("viewer")
            .setTargetUserId("owner")
            .setBlacklisted(true)
            .setBlockedByOwner(true);
        when(relationMapper.selectByPair("owner", "viewer")).thenReturn(null);
        when(relationMapper.selectByPair("viewer", "owner")).thenReturn(viewerToOwner);

        assertThat(policyService.canViewContent("owner", "viewer")).isFalse();
    }

    @Test
    void shouldRejectOnlineStatusWhenMutualOnlyAndNotMutualFollow() {
        // 互关可见，但只有单向关注，不允许查看
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("owner")
            .setOnlineStatusVisibility("MUTUAL_ONLY");
        when(privacySettingMapper.selectByUserId("owner")).thenReturn(privacySetting);

        // viewer 关注了 owner，但 owner 没有关注 viewer
        ContentUserRelation viewerToOwner = new ContentUserRelation()
            .setOwnerUserId("viewer")
            .setTargetUserId("owner")
            .setFollowed(true);
        when(relationMapper.selectByPair("viewer", "owner")).thenReturn(viewerToOwner);
        when(relationMapper.selectByPair("owner", "viewer")).thenReturn(null);

        assertThat(policyService.canViewOnlineStatus("owner", "viewer")).isFalse();
    }

    @Test
    void shouldAllowOnlineStatusWhenMutualOnlyAndMutualFollow() {
        // 互关可见，双向关注，允许查看
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("owner")
            .setOnlineStatusVisibility("MUTUAL_ONLY");
        when(privacySettingMapper.selectByUserId("owner")).thenReturn(privacySetting);

        ContentUserRelation viewerToOwner = new ContentUserRelation()
            .setOwnerUserId("viewer")
            .setTargetUserId("owner")
            .setFollowed(true);
        ContentUserRelation ownerToViewer = new ContentUserRelation()
            .setOwnerUserId("owner")
            .setTargetUserId("viewer")
            .setFollowed(true);
        when(relationMapper.selectByPair("viewer", "owner")).thenReturn(viewerToOwner);
        when(relationMapper.selectByPair("owner", "viewer")).thenReturn(ownerToViewer);

        assertThat(policyService.canViewOnlineStatus("owner", "viewer")).isTrue();
    }

    @Test
    void shouldAllowOnlineStatusWhenPublic() {
        // 公开可见，任何用户都可以查看
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("owner")
            .setOnlineStatusVisibility("PUBLIC");
        when(privacySettingMapper.selectByUserId("owner")).thenReturn(privacySetting);

        assertThat(policyService.canViewOnlineStatus("owner", "viewer")).isTrue();
    }

    @Test
    void shouldRejectOnlineStatusWhenHidden() {
        // 隐藏状态，除本人外无人可见
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("owner")
            .setOnlineStatusVisibility("HIDDEN");
        when(privacySettingMapper.selectByUserId("owner")).thenReturn(privacySetting);

        assertThat(policyService.canViewOnlineStatus("owner", "viewer")).isFalse();
    }

    // ===== canViewActivity 测试 =====

    @Test
    void shouldAllowActivityViewWhenPublic() {
        // PUBLIC 时任何人可见
        assertThat(policyService.canViewActivity("owner", "viewer", "PUBLIC")).isTrue();
    }

    @Test
    void shouldRejectActivityViewWhenPrivate() {
        // PRIVATE 时非本人不可见
        assertThat(policyService.canViewActivity("owner", "viewer", "PRIVATE")).isFalse();
    }

    @Test
    void shouldAllowActivityViewForOwnerWhenPrivate() {
        // PRIVATE 时 owner 自己可见
        assertThat(policyService.canViewActivity("owner", "owner", "PRIVATE")).isTrue();
    }

    @Test
    void shouldRejectActivityViewWhenFollowersOnlyAndNotFollower() {
        // FOLLOWERS_ONLY 且非粉丝时拒绝
        when(relationMapper.selectByPair("viewer", "owner")).thenReturn(null);

        assertThat(policyService.canViewActivity("owner", "viewer", "FOLLOWERS_ONLY")).isFalse();
    }

    @Test
    void shouldAllowActivityViewWhenMutualOnlyAndMutualFollow() {
        // MUTUAL_ONLY 且互关时允许
        ContentUserRelation viewerToOwner = new ContentUserRelation()
            .setOwnerUserId("viewer")
            .setTargetUserId("owner")
            .setFollowed(true);
        ContentUserRelation ownerToViewer = new ContentUserRelation()
            .setOwnerUserId("owner")
            .setTargetUserId("viewer")
            .setFollowed(true);
        when(relationMapper.selectByPair("viewer", "owner")).thenReturn(viewerToOwner);
        when(relationMapper.selectByPair("owner", "viewer")).thenReturn(ownerToViewer);

        assertThat(policyService.canViewActivity("owner", "viewer", "MUTUAL_ONLY")).isTrue();
    }

    // ===== shouldNoindexProfile 测试 =====

    @Test
    void shouldReturnNoindexWhenSearchEngineIndexDisabled() {
        // 用户禁用搜索引擎索引时，应返回 noindex
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("user1")
            .setAllowSearchEngineIndex(false);
        when(privacySettingMapper.selectByUserId("user1")).thenReturn(privacySetting);

        assertThat(policyService.shouldNoindexProfile("user1")).isTrue();
    }

    @Test
    void shouldNotNoindexWhenSearchEngineIndexEnabled() {
        // 用户启用搜索引擎索引时，不应返回 noindex
        ContentUserPrivacySetting privacySetting = new ContentUserPrivacySetting()
            .setUserId("user2")
            .setAllowSearchEngineIndex(true);
        when(privacySettingMapper.selectByUserId("user2")).thenReturn(privacySetting);

        assertThat(policyService.shouldNoindexProfile("user2")).isFalse();
    }

    @Test
    void shouldReturnNoindexWhenPrivacySettingNotFound() {
        // 无隐私设置时，默认返回 noindex（安全默认值）
        when(privacySettingMapper.selectByUserId("user3")).thenReturn(null);

        assertThat(policyService.shouldNoindexProfile("user3")).isTrue();
    }
}
