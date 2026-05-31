package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelInvite;
import org.jeecg.modules.content.channel.enums.InviteStatus;
import org.jeecg.modules.content.channel.mapper.ChannelInviteMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelInviteServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 邀请码完整生命周期测试
 * 覆盖：创建 -> 验证 -> 使用 -> 用完 -> 过期 -> 撤销 的完整流程
 */
@ExtendWith(MockitoExtension.class)
class ChannelInviteFlowTest {

    @BeforeAll
    static void initLambdaCache() {
        // 初始化 MyBatis Plus lambda 缓存，使 LambdaUpdateWrapper 能正常工作
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            ChannelInvite.class);
    }

    @Mock
    private ChannelInviteMapper inviteMapper;

    @InjectMocks
    private ChannelInviteServiceImpl inviteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inviteService, "baseMapper", inviteMapper);
    }

    @Test
    void should_create_invite_with_code() {
        // 创建邀请码时应自动生成唯一码并设置初始状态为有效
        ChannelInvite result = inviteService.createInvite("ch1", 1, 10, 7, "user1");

        verify(inviteMapper).insert(any(ChannelInvite.class));
        assertThat(result.getCode()).isNotNull().hasSize(16);
        assertThat(result.getStatus()).isEqualTo(InviteStatus.ACTIVE.getCode());
        assertThat(result.getUsedCount()).isEqualTo(0);
        assertThat(result.getExpireTime()).isAfter(new Date());
    }

    @Test
    void should_validate_active_invite() {
        // 有效且未过期的邀请码应通过验证
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
    void should_increment_used_count_on_use() {
        // 使用邀请码后，已用次数应增加
        ChannelInviteService spyService = spy(inviteService);
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setExpireTime(new Date(System.currentTimeMillis() + 86400000));
        invite.setMaxUses(10);
        invite.setUsedCount(5);
        doReturn(invite).when(spyService).getOne(any());

        spyService.useInvite("INVITE123");

        verify(inviteMapper).update(any(), any());
    }

    @Test
    void should_reject_expired_invite() {
        // 过期的邀请码不应通过验证，防止使用无效邀请加入
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
        // 已用完的邀请码不应通过验证，防止超额使用
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
    void should_revoke_invite() {
        // 撤销邀请码后状态应变为REVOKED
        ChannelInvite invite = new ChannelInvite();
        invite.setId("inv1");
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        when(inviteMapper.selectById("inv1")).thenReturn(invite);

        inviteService.revokeInvite("inv1", "admin1");

        assertThat(invite.getStatus()).isEqualTo(InviteStatus.REVOKED.getCode());
        verify(inviteMapper).updateById(invite);
    }

    @Test
    void should_reject_using_revoked_invite() {
        // 已撤销的邀请码不应通过验证
        ChannelInviteService spyService = spy(inviteService);
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.REVOKED.getCode());
        doReturn(invite).when(spyService).getOne(any());

        assertThatThrownBy(() -> spyService.validateInvite("INVITE123"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已失效");
    }

    @Test
    void should_reject_nonexistent_invite_code() {
        // 不存在的邀请码应明确报错
        ChannelInviteService spyService = spy(inviteService);
        doReturn(null).when(spyService).getOne(any());

        assertThatThrownBy(() -> spyService.validateInvite("NONEXISTENT"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("邀请码不存在");
    }

    @Test
    void should_revoke_nonexistent_invite_throws() {
        // 撒销不存在的邀请应报错
        when(inviteMapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> inviteService.revokeInvite("nonexistent", "admin1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("邀请不存在");
    }
}
