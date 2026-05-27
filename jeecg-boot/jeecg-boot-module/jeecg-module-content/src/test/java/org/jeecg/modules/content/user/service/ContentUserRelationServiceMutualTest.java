package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserRelationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 互关判定逻辑单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserRelationServiceMutualTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @InjectMocks
    private ContentUserRelationServiceImpl relationService;

    @Test
    void shouldReturnTrueWhenBothUsersFollowEachOther() {
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(followedRelation("u1", "u2"));
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(followedRelation("u2", "u1"));

        assertThat(relationService.isMutualFollow("u1", "u2")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOnlyOneDirectionFollows() {
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(followedRelation("u1", "u2"));
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(null);

        assertThat(relationService.isMutualFollow("u1", "u2")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNeitherFollows() {
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);

        assertThat(relationService.isMutualFollow("u1", "u2")).isFalse();
    }

    @Test
    void shouldReturnFalseAfterUnfollow() {
        ContentUserRelation unfollowed = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(false);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(unfollowed);

        assertThat(relationService.isMutualFollow("u1", "u2")).isFalse();
    }

    @Test
    void shouldReturnFalseForSameUser() {
        assertThat(relationService.isMutualFollow("u1", "u1")).isFalse();
    }

    @Test
    void shouldReturnFalseForNullInputs() {
        assertThat(relationService.isMutualFollow(null, "u2")).isFalse();
        assertThat(relationService.isMutualFollow("u1", null)).isFalse();
    }

    private ContentUserRelation followedRelation(String owner, String target) {
        return new ContentUserRelation()
            .setOwnerUserId(owner)
            .setTargetUserId(target)
            .setFollowed(true);
    }
}
