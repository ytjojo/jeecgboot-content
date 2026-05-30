package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.mapper.ChannelMemberMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelMemberServiceTest {

    @Mock
    private ChannelMemberMapper memberMapper;

    @InjectMocks
    private ChannelMemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(memberService, "baseMapper", memberMapper);
    }

    @Test
    void should_add_member_with_role() {
        ChannelMemberService spyService = spy(memberService);
        doReturn(null).when(spyService).getByChannelAndUser("ch1", "user1");

        spyService.addMember("ch1", "user1", MemberRole.MEMBER);

        verify(memberMapper).insert(any(ChannelMember.class));
    }

    @Test
    void should_reject_duplicate_member() {
        ChannelMemberService spyService = spy(memberService);
        ChannelMember existing = new ChannelMember();
        existing.setId("m1");
        existing.setCoolingEndTime(null);
        doReturn(existing).when(spyService).getByChannelAndUser("ch1", "user1");

        assertThatThrownBy(() -> spyService.addMember("ch1", "user1", MemberRole.MEMBER))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已是频道成员");
    }

    @Test
    void should_reject_during_cooling_period() {
        ChannelMemberService spyService = spy(memberService);
        ChannelMember existing = new ChannelMember();
        existing.setId("m1");
        existing.setCoolingEndTime(new Date(System.currentTimeMillis() + 86400000 * 3));
        doReturn(existing).when(spyService).getByChannelAndUser("ch1", "user1");

        assertThatThrownBy(() -> spyService.addMember("ch1", "user1", MemberRole.MEMBER))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("冷却期");
    }

    @Test
    void should_assign_role() {
        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberMapper.selectById("m1")).thenReturn(member);

        memberService.assignRole("m1", MemberRole.ADMIN, "owner1");

        assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN.getCode());
        verify(memberMapper).updateById(member);
    }
}
