package org.jeecg.modules.content.user.gateway;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.content.user.gateway.impl.SystemUserAccountGatewayImpl;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemUserAccountGatewayImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SystemUserAccountGatewayImpl gateway;

    @Test
    void shouldCreateSysUserWhenAllUniquenessChecksPass() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setUsername("alice")
            .setMobile("13800000001")
            .setEmail("alice@example.com")
            .setPassword("Pass@123")
            .setNickname("Alice");
        when(sysUserMapper.getUserByPhone("13800000001")).thenReturn(null);
        when(sysUserMapper.getUserByEmail("alice@example.com")).thenReturn(null);
        when(sysUserMapper.getUserByName("alice")).thenReturn(null);

        String userId = gateway.createUser(req);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        SysUser saved = captor.getValue();
        assertThat(userId).isEqualTo(saved.getId());
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getRealname()).isEqualTo("Alice");
        assertThat(saved.getPhone()).isEqualTo("13800000001");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getSalt()).isNotBlank();
        assertThat(saved.getSalt()).hasSize(8);
        assertThat(saved.getPassword()).isNotBlank();
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getCreateTime()).isNotNull();
        assertThat(saved.getUpdateTime()).isNotNull();
        assertThat(saved.getLastPwdUpdateTime()).isNotNull();
    }

    @Test
    void shouldDefaultUsernameToMobileWhenUsernameBlank() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setUsername(null)
            .setMobile("13800000002")
            .setPassword("Pass@123")
            .setNickname("小张");
        when(sysUserMapper.getUserByPhone("13800000002")).thenReturn(null);
        when(sysUserMapper.getUserByName("13800000002")).thenReturn(null);

        String userId = gateway.createUser(req);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("13800000002");
        assertThat(userId).isNotBlank();
    }

    @Test
    void shouldRejectMobileAlreadyRegistered() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setMobile("13800000001")
            .setPassword("Pass@123")
            .setNickname("Alice");
        when(sysUserMapper.getUserByPhone("13800000001")).thenReturn(new SysUser().setId("existing"));

        assertThatThrownBy(() -> gateway.createUser(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("手机号已注册");
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void shouldRejectEmailAlreadyRegistered() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setMobile("13800000001")
            .setEmail("dup@example.com")
            .setPassword("Pass@123")
            .setNickname("Alice");
        when(sysUserMapper.getUserByPhone("13800000001")).thenReturn(null);
        when(sysUserMapper.getUserByEmail("dup@example.com")).thenReturn(new SysUser().setId("existing"));

        assertThatThrownBy(() -> gateway.createUser(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("邮箱已注册");
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void shouldRejectUsernameAlreadyExists() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setUsername("dupname")
            .setMobile("13800000001")
            .setPassword("Pass@123")
            .setNickname("Alice");
        when(sysUserMapper.getUserByPhone("13800000001")).thenReturn(null);
        when(sysUserMapper.getUserByName("dupname")).thenReturn(new SysUser().setId("existing"));

        assertThatThrownBy(() -> gateway.createUser(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户名已存在");
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void shouldEncryptPasswordUsingUsernameAndSalt() {
        ContentRegisterReq req = new ContentRegisterReq()
            .setUsername("bob")
            .setMobile("13800000003")
            .setPassword("Secret@2024")
            .setNickname("Bob");
        when(sysUserMapper.getUserByPhone(any())).thenReturn(null);
        when(sysUserMapper.getUserByName("bob")).thenReturn(null);

        gateway.createUser(req);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        SysUser saved = captor.getValue();
        String expected = PasswordUtil.encrypt("bob", "Secret@2024", saved.getSalt());
        assertThat(saved.getPassword()).isEqualTo(expected);
    }

    @Test
    void shouldCreateUserByEmailAndBuildUser() {
        ContentEmailRegisterReq req = new ContentEmailRegisterReq()
            .setEmail("user@example.com")
            .setPassword("Pass@123")
            .setNickname("邮箱用户");
        when(sysUserMapper.getUserByEmail("user@example.com")).thenReturn(null);
        when(sysUserMapper.getUserByName("user@example.com")).thenReturn(null);

        String userId = gateway.createUserByEmail(req);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        SysUser saved = captor.getValue();
        assertThat(userId).isEqualTo(saved.getId());
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getUsername()).isEqualTo("user@example.com");
    }

    @Test
    void shouldRejectEmailAlreadyRegisteredOnEmailRegister() {
        ContentEmailRegisterReq req = new ContentEmailRegisterReq()
            .setEmail("dup@example.com")
            .setPassword("Pass@123")
            .setNickname("Dup");
        when(sysUserMapper.getUserByEmail("dup@example.com")).thenReturn(new SysUser().setId("existing"));

        assertThatThrownBy(() -> gateway.createUserByEmail(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("邮箱已注册");
    }

    @Test
    void shouldUseProvidedUsernameOnEmailRegisterAndRejectIfExists() {
        ContentEmailRegisterReq req = new ContentEmailRegisterReq()
            .setUsername("dup_username")
            .setEmail("ok@example.com")
            .setPassword("Pass@123")
            .setNickname("OK");
        when(sysUserMapper.getUserByEmail("ok@example.com")).thenReturn(null);
        when(sysUserMapper.getUserByName("dup_username")).thenReturn(new SysUser().setId("existing"));

        assertThatThrownBy(() -> gateway.createUserByEmail(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户名已存在");
    }

    @Test
    void shouldResetPasswordByUserIdWithFreshSalt() {
        ContentPasswordResetReq req = new ContentPasswordResetReq()
            .setUserId("u_1")
            .setNewPassword("NewPass@999")
            .setSecondaryVerified(Boolean.TRUE);
        SysUser existing = new SysUser()
            .setId("u_1")
            .setUsername("alice")
            .setSalt("oldsalt")
            .setPassword("oldpw");
        when(sysUserMapper.selectById("u_1")).thenReturn(existing);

        gateway.resetPassword(req);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        SysUser updated = captor.getValue();
        assertThat(updated.getSalt()).isNotEqualTo("oldsalt");
        assertThat(updated.getSalt()).hasSize(8);
        assertThat(updated.getPassword())
            .isEqualTo(PasswordUtil.encrypt("alice", "NewPass@999", updated.getSalt()));
        assertThat(updated.getLastPwdUpdateTime()).isNotNull();
    }

    @Test
    void shouldResetPasswordByMobile() {
        ContentPasswordResetReq req = new ContentPasswordResetReq()
            .setMobile("13800000010")
            .setNewPassword("NewPass@999");
        SysUser existing = new SysUser()
            .setId("u_2")
            .setUsername("bob");
        when(sysUserMapper.getUserByPhone("13800000010")).thenReturn(existing);

        gateway.resetPassword(req);

        verify(sysUserMapper).updateById(any(SysUser.class));
    }

    @Test
    void shouldResetPasswordByEmail() {
        ContentPasswordResetReq req = new ContentPasswordResetReq()
            .setEmail("a@b.com")
            .setNewPassword("NewPass@999");
        SysUser existing = new SysUser()
            .setId("u_3")
            .setUsername("carol");
        when(sysUserMapper.getUserByEmail("a@b.com")).thenReturn(existing);

        gateway.resetPassword(req);

        verify(sysUserMapper).updateById(any(SysUser.class));
    }

    @Test
    void shouldThrowWhenPasswordResetCannotResolveUser() {
        ContentPasswordResetReq req = new ContentPasswordResetReq()
            .setUserId("missing")
            .setNewPassword("NewPass@999");
        when(sysUserMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> gateway.resetPassword(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("未找到对应平台账号");
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void shouldDelegateGetByIdToMapper() {
        SysUser expected = new SysUser().setId("u_x");
        when(sysUserMapper.selectById("u_x")).thenReturn(expected);

        SysUser actual = gateway.getById("u_x");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void shouldBindMobileWhenUnboundToOtherUser() {
        SysUser target = new SysUser().setId("u_a").setUsername("a");
        when(sysUserMapper.getUserByPhone("13800000011")).thenReturn(null);
        when(sysUserMapper.selectById("u_a")).thenReturn(target);

        SysUser result = gateway.bindMobile("u_a", "13800000011");

        assertThat(result.getPhone()).isEqualTo("13800000011");
        assertThat(result.getUpdateTime()).isNotNull();
        verify(sysUserMapper).updateById(target);
    }

    @Test
    void shouldAllowBindMobileToSameUser() {
        SysUser target = new SysUser().setId("u_a").setUsername("a");
        when(sysUserMapper.getUserByPhone("13800000011")).thenReturn(target);
        when(sysUserMapper.selectById("u_a")).thenReturn(target);

        SysUser result = gateway.bindMobile("u_a", "13800000011");

        assertThat(result.getPhone()).isEqualTo("13800000011");
        verify(sysUserMapper).updateById(target);
    }

    @Test
    void shouldRejectBindMobileWhenUsedByOtherUser() {
        SysUser other = new SysUser().setId("u_b").setUsername("b");
        when(sysUserMapper.getUserByPhone("13800000011")).thenReturn(other);

        assertThatThrownBy(() -> gateway.bindMobile("u_a", "13800000011"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("手机号已绑定其他账号");
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void shouldBindEmailWhenUnboundToOtherUser() {
        SysUser target = new SysUser().setId("u_a").setUsername("a");
        when(sysUserMapper.getUserByEmail("a@x.com")).thenReturn(null);
        when(sysUserMapper.selectById("u_a")).thenReturn(target);

        SysUser result = gateway.bindEmail("u_a", "a@x.com");

        assertThat(result.getEmail()).isEqualTo("a@x.com");
        verify(sysUserMapper).updateById(target);
    }

    @Test
    void shouldRejectBindEmailWhenUsedByOtherUser() {
        SysUser other = new SysUser().setId("u_b").setUsername("b");
        when(sysUserMapper.getUserByEmail("a@x.com")).thenReturn(other);

        assertThatThrownBy(() -> gateway.bindEmail("u_a", "a@x.com"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("邮箱已绑定其他账号");
    }

    @Test
    void shouldUnbindMobileAndClearPhone() {
        SysUser target = new SysUser().setId("u_a").setPhone("13800000000");
        when(sysUserMapper.selectById("u_a")).thenReturn(target);

        SysUser result = gateway.unbindMobile("u_a");

        assertThat(result.getPhone()).isNull();
        assertThat(result.getUpdateTime()).isNotNull();
        verify(sysUserMapper).updateById(target);
    }

    @Test
    void shouldUnbindEmailAndClearEmail() {
        SysUser target = new SysUser().setId("u_a").setEmail("a@x.com");
        when(sysUserMapper.selectById("u_a")).thenReturn(target);

        SysUser result = gateway.unbindEmail("u_a");

        assertThat(result.getEmail()).isNull();
        assertThat(result.getUpdateTime()).isNotNull();
        verify(sysUserMapper).updateById(target);
    }

    @Test
    void shouldThrowWhenUnbindOnMissingUser() {
        when(sysUserMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> gateway.unbindMobile("missing"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("未找到对应平台账号");
    }

    @Test
    void shouldMarkCancelledBySettingStatusToTwo() {
        SysUser target = new SysUser().setId("u_a").setStatus(1);
        when(sysUserMapper.selectById("u_a")).thenReturn(target);

        gateway.markCancelled("u_a");

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(2);
        assertThat(captor.getValue().getUpdateTime()).isNotNull();
    }

    @Test
    void shouldThrowWhenMarkCancelledOnMissingUser() {
        when(sysUserMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> gateway.markCancelled("missing"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("未找到对应平台账号");
    }

    @Test
    void shouldCreateThirdPartyUserWithPrefixedUsername() {
        String userId = gateway.createUserByThirdParty("Nick");

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(captor.capture());
        SysUser saved = captor.getValue();
        assertThat(userId).isEqualTo(saved.getId());
        assertThat(saved.getUsername()).startsWith("tp_");
        assertThat(saved.getRealname()).isEqualTo("Nick");
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getEmail()).isNull();
        assertThat(saved.getSalt()).isNull();
        assertThat(saved.getPassword()).isNull();
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getCreateTime()).isNotNull();
        assertThat(saved.getUpdateTime()).isNotNull();
    }
}
