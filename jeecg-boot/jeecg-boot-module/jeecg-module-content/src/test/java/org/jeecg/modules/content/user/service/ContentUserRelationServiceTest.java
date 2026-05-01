package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserRelationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserRelationServiceTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @InjectMocks
    private ContentUserRelationServiceImpl relationService;

    @Test
    void shouldUnfollowAutomaticallyWhenRequesterBlacklistsTarget() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setBlacklisted(false);
        ContentUserRelation reverseRelation = new ContentUserRelation()
            .setOwnerUserId("u2")
            .setTargetUserId("u1")
            .setFollowed(true)
            .setSpecialFollow(true);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(reverseRelation);

        relationService.blacklist("u1", "u2");

        assertThat(relation.getFollowed()).isFalse();
        assertThat(relation.getBlacklisted()).isTrue();
        assertThat(reverseRelation.getFollowed()).isFalse();
        assertThat(reverseRelation.getSpecialFollow()).isFalse();
    }

    @Test
    void shouldReopenInteractionWhenRequesterRemovesTargetFromBlacklist() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setBlacklisted(true)
            .setMuted(true)
            .setBlockedByOwner(true)
            .setFollowed(false)
            .setSpecialFollow(false);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        relationService.unblacklist("u1", "u2");

        assertThat(relation.getBlacklisted()).isFalse();
        assertThat(relation.getMuted()).isFalse();
        assertThat(relation.getBlockedByOwner()).isFalse();
    }

    @Test
    void shouldRejectFollowWhenRequesterHasBlacklistedTarget() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setBlacklisted(true)
            .setBlockedByOwner(true);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        assertThatThrownBy(() -> relationService.follow("u1", "u2", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("拉黑关系中不可关注");
    }

    @Test
    void shouldEnableSpecialFollowForFollowedRelation() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(false)
            .setBlacklisted(false)
            .setBlockedByOwner(false);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(null);

        relationService.specialFollow("u1", "u2", "group-a");

        assertThat(relation.getFollowed()).isTrue();
        assertThat(relation.getSpecialFollow()).isTrue();
        assertThat(relation.getRelationGroupId()).isEqualTo("group-a");
    }

    @Test
    void shouldRestoreVisibilityNoiseSettingWhenRequesterCancelsMute() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setMuted(true);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        relationService.unmute("u1", "u2");

        assertThat(relation.getMuted()).isFalse();
    }
}
