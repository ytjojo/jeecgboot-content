package org.jeecg.modules.content.channel.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.GovernanceAction;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelGovernanceBizServiceTest {

    @Mock
    private ChannelMemberService memberService;
    @Mock
    private ChannelMuteService muteService;
    @Mock
    private ChannelBlacklistService blacklistService;
    @Mock
    private ChannelGovernanceLogService logService;

    @InjectMocks
    private ChannelGovernanceBizService governanceService;

    @Test
    void should_remove_member() {
        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setChannelId("ch1");
        member.setUserId("user1");
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getById("m1")).thenReturn(member);

        ChannelMember operator = new ChannelMember();
        operator.setRole(MemberRole.ADMIN.getCode());
        when(memberService.getByChannelAndUser("ch1", "admin1")).thenReturn(operator);

        governanceService.removeMember("m1", "admin1", "违规");

        verify(memberService).removeMember("m1");
        verify(logService).log(eq(GovernanceAction.REMOVE), eq("ch1"), eq("admin1"), eq("user1"), eq("违规"), any());
    }

    @Test
    void should_reject_removing_owner() {
        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.OWNER.getCode());
        when(memberService.getById("m1")).thenReturn(member);

        assertThatThrownBy(() -> governanceService.removeMember("m1", "admin1", "违规"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("不能移除频道主");
    }

    @Test
    void should_mute_member() {
        ChannelMember operator = new ChannelMember();
        operator.setRole(MemberRole.ADMIN.getCode());
        when(memberService.getByChannelAndUser("ch1", "admin1")).thenReturn(operator);

        ChannelMember target = new ChannelMember();
        target.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getByChannelAndUser("ch1", "user1")).thenReturn(target);

        governanceService.muteMember("ch1", "user1", "admin1", "违规", 7);

        verify(muteService).mute("ch1", "user1", "admin1", "违规", 7);
        verify(logService).log(eq(GovernanceAction.MUTE), any(), any(), any(), any(), any());
    }

    @Test
    void should_reject_muting_owner() {
        ChannelMember operator = new ChannelMember();
        operator.setRole(MemberRole.ADMIN.getCode());
        when(memberService.getByChannelAndUser("ch1", "admin1")).thenReturn(operator);

        ChannelMember target = new ChannelMember();
        target.setRole(MemberRole.OWNER.getCode());
        when(memberService.getByChannelAndUser("ch1", "owner1")).thenReturn(target);

        assertThatThrownBy(() -> governanceService.muteMember("ch1", "owner1", "admin1", "违规", 7))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("不能对频道主执行此操作");
    }
}
