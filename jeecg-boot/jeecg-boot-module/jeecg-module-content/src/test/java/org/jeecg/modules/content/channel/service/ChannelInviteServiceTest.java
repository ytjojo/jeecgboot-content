package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelInvite;
import org.jeecg.modules.content.channel.enums.InviteStatus;
import org.jeecg.modules.content.channel.mapper.ChannelInviteMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelInviteServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelInviteServiceTest {

    @Mock
    private ChannelInviteMapper inviteMapper;

    @InjectMocks
    private ChannelInviteServiceImpl inviteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inviteService, "baseMapper", inviteMapper);
    }

    @Test
    void should_validate_active_invite() {
        ChannelInviteService spyService = spy(inviteService);
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setExpireTime(new Date(System.currentTimeMillis() + 86400000 * 7));
        invite.setMaxUses(10);
        invite.setUsedCount(5);
        doReturn(invite).when(spyService).getOne(any());

        assertThat(spyService.validateInvite("INVITE123")).isTrue();
    }

    @Test
    void should_reject_expired_invite() {
        ChannelInviteService spyService = spy(inviteService);
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setExpireTime(new Date(System.currentTimeMillis() - 86400000));
        doReturn(invite).when(spyService).getOne(any());

        assertThatThrownBy(() -> spyService.validateInvite("INVITE123"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已过期");
    }

    @Test
    void should_reject_used_up_invite() {
        ChannelInviteService spyService = spy(inviteService);
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setExpireTime(new Date(System.currentTimeMillis() + 86400000));
        invite.setMaxUses(10);
        invite.setUsedCount(10);
        doReturn(invite).when(spyService).getOne(any());

        assertThatThrownBy(() -> spyService.validateInvite("INVITE123"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已用完");
    }

    @Test
    void should_create_invite() {
        ChannelInvite result = inviteService.createInvite("ch1", 1, 10, 7, "user1");

        verify(inviteMapper).insert(any(ChannelInvite.class));
    }
}
