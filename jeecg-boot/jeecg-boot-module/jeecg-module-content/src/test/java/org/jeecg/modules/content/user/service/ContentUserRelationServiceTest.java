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
            .setFollowed(false)
            .setBlacklisted(false);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        relationService.follow("u1", "u2", null);
        relationService.blacklist("u1", "u2");

        assertThat(relation.getFollowed()).isFalse();
        assertThat(relation.getBlacklisted()).isTrue();
    }
}
