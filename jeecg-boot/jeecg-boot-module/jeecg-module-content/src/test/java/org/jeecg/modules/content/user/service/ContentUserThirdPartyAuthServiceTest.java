package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;
import org.jeecg.modules.content.user.mapper.ContentUserThirdPartyAuthMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserThirdPartyAuthServiceImpl;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthVO;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthorizationDetailVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContentUserThirdPartyAuthServiceImpl 服务测试。
 * 验证活跃授权列表查询和撤销逻辑。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserThirdPartyAuthServiceTest {

    @Mock
    private ContentUserThirdPartyAuthMapper mapper;

    @Mock
    private ContentThirdPartyTokenRevocationPort tokenRevocationPort;

    @InjectMocks
    private ContentUserThirdPartyAuthServiceImpl service;

    /**
     * listActiveAuths 应将实体列表映射为 VO 列表。
     */
    @Test
    void listActiveAuthsShouldReturnMappedVOs() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setAppName("微信")
            .setAuthTime(new Date())
            .setScopes("[\"read\"]")
            .setStatus("ACTIVE");
        auth.setId("auth-1");
        when(mapper.selectActiveByUserId("user-1")).thenReturn(List.of(auth));

        List<ContentThirdPartyAuthVO> result = service.listActiveAuths("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthId()).isEqualTo("auth-1");
        assertThat(result.get(0).getAppName()).isEqualTo("微信");
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        assertThat(result.get(0).getScopes()).containsExactly("read");
    }

    /**
     * listActiveAuths 在无活跃授权时应返回空列表。
     */
    @Test
    void listActiveAuthsShouldReturnEmptyListWhenNoActive() {
        when(mapper.selectActiveByUserId("user-1")).thenReturn(List.of());

        List<ContentThirdPartyAuthVO> result = service.listActiveAuths("user-1");

        assertThat(result).isEmpty();
    }

    /**
     * revokeAuth 在授权存在且为 ACTIVE 时应成功撤销。
     */
    @Test
    void revokeAuthShouldSucceedWhenAuthExistsAndIsActive() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setStatus("ACTIVE");
        auth.setId("auth-1");
        when(mapper.selectByAuthIdAndUserId("auth-1", "user-1")).thenReturn(auth);
        when(mapper.revokeByAuthIdAndUserId(eq("auth-1"), eq("user-1"), any(Date.class))).thenReturn(1);

        boolean result = service.revokeAuth("user-1", "auth-1");

        assertThat(result).isTrue();
        verify(mapper).revokeByAuthIdAndUserId(eq("auth-1"), eq("user-1"), any(Date.class));
    }

    /**
     * revokeAuth 在授权记录不存在时应抛出异常。
     */
    @Test
    void revokeAuthShouldThrowWhenAuthNotFound() {
        when(mapper.selectByAuthIdAndUserId("nonexistent", "user-1")).thenReturn(null);

        assertThatThrownBy(() -> service.revokeAuth("user-1", "nonexistent"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("授权记录不存在");
    }

    /**
     * revokeAuth 在授权已被撤销时应抛出异常。
     */
    @Test
    void revokeAuthShouldThrowWhenAuthAlreadyRevoked() {
        ContentUserThirdPartyAuth revokedAuth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setStatus("REVOKED");
        revokedAuth.setId("auth-1");
        when(mapper.selectByAuthIdAndUserId("auth-1", "user-1")).thenReturn(revokedAuth);

        assertThatThrownBy(() -> service.revokeAuth("user-1", "auth-1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("授权已被撤销");
    }

    /**
     * revokeAuth 应在数据库撤销成功后调用 Token 撤销端口。
     */
    @Test
    void revokeAuthShouldCallTokenRevocationPort() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setStatus("ACTIVE")
            .setTokenHash("hash-abc")
            .setRefreshTokenHash("hash-xyz");
        auth.setId("auth-1");
        when(mapper.selectByAuthIdAndUserId("auth-1", "user-1")).thenReturn(auth);
        when(mapper.revokeByAuthIdAndUserId(eq("auth-1"), eq("user-1"), any(Date.class))).thenReturn(1);
        when(tokenRevocationPort.revokeTokens("auth-1", "hash-abc", "hash-xyz")).thenReturn(true);

        boolean result = service.revokeAuth("user-1", "auth-1");

        assertThat(result).isTrue();
        verify(tokenRevocationPort).revokeTokens("auth-1", "hash-abc", "hash-xyz");
    }

    /**
     * revokeAuth 在 token hash 为 null 时仍应调用撤销端口。
     */
    @Test
    void revokeAuthShouldCallTokenRevocationPortWithNullHashes() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setStatus("ACTIVE");
        auth.setId("auth-1");
        when(mapper.selectByAuthIdAndUserId("auth-1", "user-1")).thenReturn(auth);
        when(mapper.revokeByAuthIdAndUserId(eq("auth-1"), eq("user-1"), any(Date.class))).thenReturn(1);
        when(tokenRevocationPort.revokeTokens("auth-1", null, null)).thenReturn(true);

        boolean result = service.revokeAuth("user-1", "auth-1");

        assertThat(result).isTrue();
        verify(tokenRevocationPort).revokeTokens("auth-1", null, null);
    }

    /**
     * getAuthDetail 在授权存在时应返回详情。
     */
    @Test
    void getAuthDetailShouldReturnDetailWhenExists() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setAppName("微信")
            .setAuthTime(new Date())
            .setScopes("[\"read\",\"write\"]")
            .setStatus("ACTIVE")
            .setRevokedAt(null);
        auth.setId("auth-1");
        when(mapper.selectByAuthIdAndUserId("auth-1", "user-1")).thenReturn(auth);

        ContentThirdPartyAuthorizationDetailVO result = service.getAuthDetail("user-1", "auth-1");

        assertThat(result).isNotNull();
        assertThat(result.getAuthId()).isEqualTo("auth-1");
        assertThat(result.getAppName()).isEqualTo("微信");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getScopes()).containsExactly("read", "write");
        assertThat(result.getRevokedAt()).isNull();
    }

    /**
     * getAuthDetail 在授权记录不存在时应抛出异常。
     */
    @Test
    void getAuthDetailShouldThrowWhenNotFound() {
        when(mapper.selectByAuthIdAndUserId("nonexistent", "user-1")).thenReturn(null);

        assertThatThrownBy(() -> service.getAuthDetail("user-1", "nonexistent"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("授权记录不存在");
    }
}
