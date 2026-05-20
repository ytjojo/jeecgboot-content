package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.entity.ContentUserRelationGroup;
import org.jeecg.modules.content.user.mapper.ContentUserActivitySnapshotMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationGroupMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.req.relation.ContentRelationGroupReq;
import org.jeecg.modules.content.user.service.impl.ContentUserRelationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ContentUserRelationServiceTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @Mock
    private ContentUserRelationGroupMapper relationGroupMapper;

    @Mock
    private ContentUserActivitySnapshotMapper activitySnapshotMapper;

    @Mock
    private ContentUserFeedSettingMapper feedSettingMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private IContentUserVisibilityPolicyService visibilityPolicyService;

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
        when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile().setUserId("u2"));
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
        when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile().setUserId("u2"));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(null);
        when(relationGroupMapper.selectById("group-a")).thenReturn(activeGroup("group-a", "u1", "核心关注", false));

        relationService.specialFollow("u1", "u2", "group-a");

        assertThat(relation.getFollowed()).isTrue();
        assertThat(relation.getSpecialFollow()).isTrue();
        assertThat(relation.getRelationGroupId()).isEqualTo("group-a");
    }

    @Test
    void shouldRejectSelfFollowBeforeCreatingRelation() {
        assertThatThrownBy(() -> relationService.follow("u1", "u1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("不能关注或操作自己");

        verify(relationMapper, never()).insert(any(ContentUserRelation.class));
    }

    @Test
    void shouldRejectBlankAndOverLengthTargetBeforeCreatingRelation() {
        assertThatThrownBy(() -> relationService.follow("u1", " ", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户ID不能为空");

        assertThatThrownBy(() -> relationService.follow("u1", "t".repeat(65), null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户ID长度不能超过64位");

        verify(relationMapper, never()).insert(any(ContentUserRelation.class));
    }

    @Test
    void shouldRejectUnknownTargetBeforeCreatingRelation() {
        when(profileMapper.selectByUserId("missing-user")).thenReturn(null);

        assertThatThrownBy(() -> relationService.follow("u1", "missing-user", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户不存在");

        verify(relationMapper, never()).insert(any(ContentUserRelation.class));
    }

    @Test
    void shouldKeepUnfollowIdempotentWhenRelationDoesNotExist() {
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);

        relationService.unfollow("u1", "u2");

        verify(relationMapper, never()).insert(any(ContentUserRelation.class));
        verify(relationMapper, never()).updateById(any(ContentUserRelation.class));
    }

    @Test
    void shouldCancelSpecialFollowWithoutRemovingNormalFollow() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        relationService.cancelSpecialFollow("u1", "u2");

        assertThat(relation.getFollowed()).isTrue();
        assertThat(relation.getSpecialFollow()).isFalse();
    }

    @Test
    void shouldExposeRelationDetailWithoutEntityOnlyFields() {
        Date followedAt = new Date();
        Date specialFollowAt = new Date();
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true)
            .setMuted(false)
            .setBlacklisted(false)
            .setBlockedByOwner(false)
            .setRelationGroupId("group-a")
            .setFollowedAt(followedAt)
            .setSpecialFollowAt(specialFollowAt);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        var result = relationService.getRelation("u1", "u2");

        assertThat(result.getOwnerUserId()).isEqualTo("u1");
        assertThat(result.getTargetUserId()).isEqualTo("u2");
        assertThat(result.getRelationGroupId()).isEqualTo("group-a");
        assertThat(result.getFollowedAt()).isEqualTo(followedAt);
        assertThat(result.getSpecialFollowAt()).isEqualTo(specialFollowAt);
    }

    @Test
    void shouldKeepRelationDetailReadOnlyWhenRelationDoesNotExist() {
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);

        var result = relationService.getRelation("u1", "u2");

        assertThat(result.getOwnerUserId()).isEqualTo("u1");
        assertThat(result.getTargetUserId()).isEqualTo("u2");
        assertThat(result.getFollowed()).isFalse();
        verify(relationMapper, never()).insert(any(ContentUserRelation.class));
        verify(relationMapper, never()).updateById(any(ContentUserRelation.class));
    }

    @Test
    void shouldAssignDefaultGroupWhenFollowHasNoExplicitGroup() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setBlacklisted(false)
            .setBlockedByOwner(false);
        when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile().setUserId("u2"));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(null);
        when(relationGroupMapper.selectOne(any())).thenReturn(activeGroup("default-g", "u1", "默认分组", true));

        relationService.follow("u1", "u2", null);

        assertThat(relation.getRelationGroupId()).isEqualTo("default-g");
        assertThat(relation.getFollowed()).isTrue();
    }

    @Test
    void shouldCreateCustomGroupWithTrimmedNameAndSortOrder() {
        when(relationGroupMapper.selectOne(any())).thenReturn(null);
        ArgumentCaptor<ContentUserRelationGroup> captor = ArgumentCaptor.forClass(ContentUserRelationGroup.class);

        var result = relationService.createGroup("u1", new ContentRelationGroupReq()
            .setGroupName("  核心关注  ")
            .setSortOrder(9));

        verify(relationGroupMapper).insert(captor.capture());
        assertThat(captor.getValue().getOwnerUserId()).isEqualTo("u1");
        assertThat(captor.getValue().getGroupName()).isEqualTo("核心关注");
        assertThat(captor.getValue().getSortOrder()).isEqualTo(9);
        assertThat(captor.getValue().getIsDefault()).isFalse();
        assertThat(result.getGroupName()).isEqualTo("核心关注");
    }

    @Test
    void shouldRejectInvalidAndDuplicateGroupRequests() {
        assertThatThrownBy(() -> relationService.createGroup(null, new ContentRelationGroupReq()
                .setGroupName("核心关注")
                .setSortOrder(1)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("当前用户ID不能为空");

        assertThatThrownBy(() -> relationService.createGroup("u1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分组请求不能为空");

        assertThatThrownBy(() -> relationService.createGroup("u1", new ContentRelationGroupReq()
                .setGroupName(" ")
                .setSortOrder(1)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分组名称不能为空");

        assertThatThrownBy(() -> relationService.createGroup("u1", new ContentRelationGroupReq()
                .setGroupName("组".repeat(65))
                .setSortOrder(1)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分组名称长度不能超过64位");

        assertThatThrownBy(() -> relationService.createGroup("u1", new ContentRelationGroupReq()
                .setGroupName("核心关注")
                .setSortOrder(null)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("排序值不能为空");

        assertThatThrownBy(() -> relationService.createGroup("u1", new ContentRelationGroupReq()
                .setGroupName("核心关注")
                .setSortOrder(-1)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("排序值不能小于0");

        when(relationGroupMapper.selectOne(any())).thenReturn(activeGroup("g1", "u1", "核心关注", false));
        assertThatThrownBy(() -> relationService.createGroup("u1", new ContentRelationGroupReq()
                .setGroupName("核心关注")
                .setSortOrder(1)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分组名称已存在");
    }

    @Test
    void shouldRenameCustomGroupAndRejectDeletingDefaultGroup() {
        ContentUserRelationGroup group = activeGroup("g1", "u1", "旧分组", false);
        when(relationGroupMapper.selectById("g1")).thenReturn(group);
        when(relationGroupMapper.selectOne(any())).thenReturn(null);

        var result = relationService.renameGroup("u1", "g1", new ContentRelationGroupReq()
            .setGroupName("新分组")
            .setSortOrder(3));

        assertThat(group.getGroupName()).isEqualTo("新分组");
        assertThat(group.getSortOrder()).isEqualTo(3);
        assertThat(result.getGroupName()).isEqualTo("新分组");

        when(relationGroupMapper.selectById("default-g")).thenReturn(activeGroup("default-g", "u1", "默认分组", true));
        assertThatThrownBy(() -> relationService.deleteGroup("u1", "default-g"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("默认分组不允许删除");
    }

    @Test
    void shouldDeleteCustomGroupAndMoveFollowedUsersBackToDefaultGroup() {
        ContentUserRelationGroup customGroup = activeGroup("g1", "u1", "核心关注", false);
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setRelationGroupId("g1");
        when(relationGroupMapper.selectById("g1")).thenReturn(customGroup);
        when(relationGroupMapper.selectOne(any())).thenReturn(activeGroup("default-g", "u1", "默认分组", true));
        when(relationMapper.selectList(any())).thenReturn(List.of(relation));

        relationService.deleteGroup("u1", "g1");

        assertThat(relation.getRelationGroupId()).isEqualTo("default-g");
        assertThat(customGroup.getGroupStatus()).isEqualTo("DELETED");
    }

    @Test
    void shouldMoveFollowedUsersToOwnedGroupAndReportUnknownRelations() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setRelationGroupId("old-g");
        when(relationGroupMapper.selectById("g1")).thenReturn(activeGroup("g1", "u1", "核心关注", false));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u1", "missing")).thenReturn(null);

        var result = relationService.moveTargetsToGroup("u1", List.of("u2", "missing"), "g1");

        assertThat(relation.getRelationGroupId()).isEqualTo("g1");
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getReason()).isEqualTo("关注关系不存在");
    }

    @Test
    void shouldRejectUnknownAndNonOwnerGroupWhenMovingTargets() {
        assertThatThrownBy(() -> relationService.moveTargetsToGroup("u1", List.of("u2"), "g".repeat(65)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("关系分组ID长度不能超过64位");

        when(relationGroupMapper.selectById("missing-g")).thenReturn(null);
        assertThatThrownBy(() -> relationService.moveTargetsToGroup("u1", List.of("u2"), "missing-g"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("关系分组不存在或无权操作");

        when(relationGroupMapper.selectById("other-g")).thenReturn(activeGroup("other-g", "u9", "他人分组", false));
        assertThatThrownBy(() -> relationService.moveTargetsToGroup("u1", List.of("u2"), "other-g"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("关系分组不存在或无权操作");
    }

    @Test
    void shouldRejectOverLimitTargetListBeforeMovingRelations() {
        when(relationGroupMapper.selectById("g1")).thenReturn(activeGroup("g1", "u1", "核心关注", false));

        assertThatThrownBy(() -> relationService.moveTargetsToGroup("u1", java.util.Collections.nCopies(101, "u2"), "g1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户ID数量不能超过100个");

        verify(relationMapper, never()).selectByPair("u1", "u2");
    }

    @Test
    void shouldRejectDuplicateTargetsBeforeBatchRelationOperation() {
        assertThatThrownBy(() -> relationService.batchUnfollow("u1", List.of("u2", "u2")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户ID不能重复");

        verify(relationMapper, never()).selectByPair("u1", "u2");
    }

    @Test
    void shouldBatchUnfollowAndReturnPartialFailureDetail() {
        ContentUserRelation followed = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(followed);
        when(relationMapper.selectByPair("u1", "missing")).thenReturn(null);

        var result = relationService.batchUnfollow("u1", List.of("u2", "missing"));

        assertThat(followed.getFollowed()).isFalse();
        assertThat(followed.getSpecialFollow()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getTargetUserId()).isEqualTo("missing");
        assertThat(result.getFailures().get(0).getReason()).isEqualTo("关注关系不存在");
    }

    @Test
    void shouldBatchCancelSpecialFollowWithoutCancellingNormalFollow() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true)
            .setSpecialFollowAt(new Date());
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u1", "u3")).thenReturn(new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u3")
            .setFollowed(true)
            .setSpecialFollow(false));

        var result = relationService.batchCancelSpecialFollow("u1", List.of("u2", "u3"));

        assertThat(relation.getFollowed()).isTrue();
        assertThat(relation.getSpecialFollow()).isFalse();
        assertThat(relation.getSpecialFollowAt()).isNull();
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getReason()).isEqualTo("特别关注关系不存在");
    }

    @Test
    void shouldSyncCountsOnlyWhenFollowStateActuallyChanges() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(false)
            .setSpecialFollow(false)
            .setBlacklisted(false)
            .setBlockedByOwner(false);
        when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile().setUserId("u2"));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);
        when(relationMapper.selectByPair("u2", "u1")).thenReturn(null);
        when(relationGroupMapper.selectOne(any())).thenReturn(activeGroup("default-g", "u1", "默认分组", true));

        relationService.follow("u1", "u2", null);
        relationService.follow("u1", "u2", null);

        verify(profileMapper, times(1)).changeFollowingCount("u1", 1);
        verify(profileMapper, times(1)).changeFollowerCount("u2", 1);
    }

    @Test
    void shouldSyncCountsWhenUnfollowClearsSpecialFollow() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true)
            .setSpecialFollowAt(new Date());
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        relationService.unfollow("u1", "u2");

        verify(profileMapper).changeFollowingCount("u1", -1);
        verify(profileMapper).changeFollowerCount("u2", -1);
        verify(profileMapper).changeSpecialFollowCount("u1", -1);
    }

    @Test
    void shouldSyncCountsForSuccessfulBatchItemsOnly() {
        ContentUserRelation followed = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(followed);
        when(relationMapper.selectByPair("u1", "missing")).thenReturn(null);

        var result = relationService.batchUnfollow("u1", List.of("u2", "missing"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(profileMapper).changeFollowingCount("u1", -1);
        verify(profileMapper).changeFollowerCount("u2", -1);
        verify(profileMapper).changeSpecialFollowCount("u1", -1);
        verify(profileMapper, never()).changeFollowerCount("missing", -1);
    }

    @Test
    void shouldRemoveFromCustomGroupWithoutCancellingFollowState() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(true)
            .setRelationGroupId("g1");
        when(relationGroupMapper.selectOne(any())).thenReturn(activeGroup("default-g", "u1", "默认分组", true));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(relation);

        var result = relationService.removeTargetsFromGroup("u1", List.of("u2"));

        assertThat(relation.getRelationGroupId()).isEqualTo("default-g");
        assertThat(relation.getFollowed()).isTrue();
        assertThat(relation.getSpecialFollow()).isTrue();
        assertThat(result.getSuccessCount()).isEqualTo(1);
    }

    @Test
    void shouldListFollowedUsersByGroupAndKeywordWithProfileSummary() {
        Date followedAt = new Date();
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setRelationGroupId("g1")
            .setFollowed(true)
            .setSpecialFollow(false)
            .setRelationStatus("ACTIVE")
            .setFollowedAt(followedAt);
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u2")
            .setNickname("小明")
            .setAvatar("avatar.png")
            .setBio("内容创作者");
        when(relationGroupMapper.selectById("g1")).thenReturn(activeGroup("g1", "u1", "核心关注", false));
        when(profileMapper.selectList(any())).thenReturn(List.of(profile), List.of(profile));
        when(relationMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentUserRelation> page = invocation.getArgument(0);
            page.setRecords(List.of(relation));
            page.setTotal(1L);
            return page;
        });
        when(activitySnapshotMapper.selectList(any())).thenReturn(List.of());

        var result = relationService.listFollowedUsers("u1", "g1", "小", 2L, 1L);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPageNo()).isEqualTo(2L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getNickname()).isEqualTo("小明");
        assertThat(result.getRecords().get(0).getRelationGroupId()).isEqualTo("g1");
    }

    @Test
    void shouldRejectInvalidFollowListFilters() {
        assertThatThrownBy(() -> relationService.listFollowedUsers(" ", null, null, 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("当前用户ID不能为空");

        assertThatThrownBy(() -> relationService.listFollowedUsers("u1", null, "关".repeat(65), 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("关键词长度不能超过64位");

        assertThatThrownBy(() -> relationService.listFollowedUsers("u1", null, null, 1L, 101L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分页大小不能超过100");

        when(relationGroupMapper.selectById("missing-g")).thenReturn(null);
        assertThatThrownBy(() -> relationService.listFollowedUsers("u1", "missing-g", null, 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("关系分组不存在或无权操作");
    }

    @Test
    void shouldReturnEmptyFollowListWhenKeywordMatchesNoProfiles() {
        when(profileMapper.selectList(any())).thenReturn(List.of());

        var result = relationService.listFollowedUsers("u1", null, "不存在", 0L, 0L);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getPageNo()).isEqualTo(1L);
        assertThat(result.getPageSize()).isEqualTo(10L);
        assertThat(result.getRecords()).isEmpty();
        verify(relationMapper, never()).selectPage(any(), any());
    }

    @Test
    void shouldListSpecialFollowedUsersWithLatestActivityHint() {
        Date latestActivityTime = new Date();
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setRelationGroupId("g1")
            .setFollowed(true)
            .setSpecialFollow(true)
            .setRelationStatus("ACTIVE")
            .setFollowedAt(new Date())
            .setLastInteractionTime(latestActivityTime);
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u2")
            .setNickname("特别关注");
        ContentUserActivitySnapshot snapshot = new ContentUserActivitySnapshot()
            .setActorUserId("u2")
            .setSummary("发布了新内容")
            .setActivityTime(latestActivityTime)
            .setSnapshotStatus("ACTIVE");
        when(relationMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentUserRelation> page = invocation.getArgument(0);
            page.setRecords(List.of(relation));
            page.setTotal(1L);
            return page;
        });
        when(profileMapper.selectList(any())).thenReturn(List.of(profile));
        when(activitySnapshotMapper.selectList(any())).thenReturn(List.of(snapshot));

        var result = relationService.listSpecialFollowedUsers("u1", 1L, 10L);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getEmptyStateCode()).isNull();
        assertThat(result.getRecords().get(0).getSpecialFollow()).isTrue();
        assertThat(result.getRecords().get(0).getLatestActivityHint()).isEqualTo("发布了新内容");
        assertThat(result.getRecords().get(0).getLatestActivityTime()).isEqualTo(latestActivityTime);
    }

    @Test
    void shouldReturnGuidanceStateForEmptySpecialFollowList() {
        when(relationMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<ContentUserRelation> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0L);
            return page;
        });

        var result = relationService.listSpecialFollowedUsers("u1", 1L, 10L);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getEmptyStateCode()).isEqualTo("NO_SPECIAL_FOLLOW");
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

    @Test
    void shouldListFollowFeedWithEnabledTypesAndSpecialPriority() {
        Date older = new Date(1000L);
        Date newer = new Date(2000L);
        ContentUserRelation normal = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(false)
            .setRelationStatus("ACTIVE");
        ContentUserRelation special = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u3")
            .setFollowed(true)
            .setSpecialFollow(true)
            .setRelationStatus("ACTIVE");
        when(relationMapper.selectList(any())).thenReturn(List.of(normal, special));
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserFeedSetting()
            .setUserId("u1")
            .setActivityTypes("PUBLISH,LIKE"));
        when(activitySnapshotMapper.selectList(any())).thenReturn(List.of(
            snapshot("s-normal", "u2", "PUBLISH", newer, "PUBLIC"),
            snapshot("s-special", "u3", "LIKE", older, "PUBLIC")
        ));
        when(visibilityPolicyService.canViewContent("u2", "u1")).thenReturn(true);
        when(visibilityPolicyService.canViewContent("u3", "u1")).thenReturn(true);

        var result = relationService.listFollowFeed("u1", 1L, 10L);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getRecords()).extracting("snapshotId")
            .containsExactly("s-special", "s-normal");
        assertThat(result.getHasMore()).isFalse();
    }

    @Test
    void shouldHideUnfollowedMutedBlockedAndInvisibleActivitiesFromFeed() {
        ContentUserRelation visible = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setSpecialFollow(false)
            .setMuted(false)
            .setRelationStatus("ACTIVE");
        ContentUserRelation muted = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u3")
            .setFollowed(true)
            .setMuted(true)
            .setRelationStatus("ACTIVE");
        when(relationMapper.selectList(any())).thenReturn(List.of(visible, muted));
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserFeedSetting()
            .setUserId("u1")
            .setActivityTypes("PUBLISH,FAVORITE"));
        when(activitySnapshotMapper.selectList(any())).thenReturn(List.of(
            snapshot("s-visible", "u2", "PUBLISH", new Date(3000L), "PUBLIC"),
            snapshot("s-private", "u2", "PUBLISH", new Date(4000L), "PRIVATE"),
            snapshot("s-muted", "u3", "PUBLISH", new Date(5000L), "PUBLIC")
        ));
        when(visibilityPolicyService.canViewContent("u2", "u1")).thenReturn(true);

        var result = relationService.listFollowFeed("u1", 1L, 10L);

        assertThat(result.getRecords()).extracting("snapshotId")
            .containsExactly("s-visible");
    }

    @Test
    void shouldReturnStableFollowFeedPage() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("u1")
            .setTargetUserId("u2")
            .setFollowed(true)
            .setRelationStatus("ACTIVE");
        when(relationMapper.selectList(any())).thenReturn(List.of(relation));
        when(feedSettingMapper.selectByUserId("u1")).thenReturn(new ContentUserFeedSetting()
            .setUserId("u1")
            .setActivityTypes("PUBLISH"));
        when(activitySnapshotMapper.selectList(any())).thenReturn(List.of(
            snapshot("s3", "u2", "PUBLISH", new Date(3000L), "PUBLIC"),
            snapshot("s2", "u2", "PUBLISH", new Date(2000L), "PUBLIC"),
            snapshot("s1", "u2", "PUBLISH", new Date(1000L), "PUBLIC")
        ));
        when(visibilityPolicyService.canViewContent("u2", "u1")).thenReturn(true);

        var result = relationService.listFollowFeed("u1", 2L, 1L);

        assertThat(result.getRecords()).extracting("snapshotId")
            .containsExactly("s2");
        assertThat(result.getHasMore()).isTrue();
    }

    private ContentUserRelationGroup activeGroup(String groupId, String ownerUserId, String groupName, boolean defaultGroup) {
        ContentUserRelationGroup group = new ContentUserRelationGroup()
            .setOwnerUserId(ownerUserId)
            .setGroupName(groupName)
            .setSortOrder(0)
            .setIsDefault(defaultGroup)
            .setGroupStatus("ACTIVE");
        group.setId(groupId);
        return group;
    }

    private ContentUserActivitySnapshot snapshot(String id, String actorUserId, String activityType, Date activityTime, String visibleScope) {
        ContentUserActivitySnapshot snapshot = new ContentUserActivitySnapshot()
            .setActorUserId(actorUserId)
            .setActivityType(activityType)
            .setBizType("ARTICLE")
            .setBizId(id + "-biz")
            .setSummary("动态")
            .setActivityTime(activityTime)
            .setVisibleScope(visibleScope)
            .setSnapshotStatus("ACTIVE");
        snapshot.setId(id);
        return snapshot;
    }
}
