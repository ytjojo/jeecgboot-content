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
}
