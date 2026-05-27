package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;
import org.jeecg.modules.content.user.mapper.ContentUserThirdPartyAuthMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContentUserThirdPartyAuthMapper 契约测试。
 * 使用 Mockito 验证 Mapper 接口的方法签名和调用行为符合预期。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserThirdPartyAuthMapperContractTest {

    @Mock
    private ContentUserThirdPartyAuthMapper mapper;

    /**
     * selectActiveByUserId 应返回指定用户的活跃授权列表。
     */
    @Test
    void selectActiveByUserIdShouldReturnActiveAuths() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setStatus("ACTIVE")
            .setAppName("微信");
        auth.setId("auth-1");
        when(mapper.selectActiveByUserId("user-1")).thenReturn(List.of(auth));

        List<ContentUserThirdPartyAuth> result = mapper.selectActiveByUserId("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        verify(mapper).selectActiveByUserId("user-1");
    }

    /**
     * selectByAuthIdAndUserId 应返回匹配的单条授权记录。
     */
    @Test
    void selectByAuthIdAndUserIdShouldReturnSingleAuth() {
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
            .setUserId("user-1")
            .setStatus("ACTIVE");
        auth.setId("auth-1");
        when(mapper.selectByAuthIdAndUserId("auth-1", "user-1")).thenReturn(auth);

        ContentUserThirdPartyAuth result = mapper.selectByAuthIdAndUserId("auth-1", "user-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("auth-1");
        verify(mapper).selectByAuthIdAndUserId("auth-1", "user-1");
    }

    /**
     * selectByAuthIdAndUserId 在记录不存在时应返回 null。
     */
    @Test
    void selectByAuthIdAndUserIdShouldReturnNullWhenNotFound() {
        when(mapper.selectByAuthIdAndUserId("nonexistent", "user-1")).thenReturn(null);

        ContentUserThirdPartyAuth result = mapper.selectByAuthIdAndUserId("nonexistent", "user-1");

        assertThat(result).isNull();
    }

    /**
     * revokeByAuthIdAndUserId 应返回受影响的行数。
     */
    @Test
    void revokeByAuthIdAndUserIdShouldReturnAffectedRows() {
        Date revokedAt = new Date();
        when(mapper.revokeByAuthIdAndUserId("auth-1", "user-1", revokedAt)).thenReturn(1);

        int rows = mapper.revokeByAuthIdAndUserId("auth-1", "user-1", revokedAt);

        assertThat(rows).isEqualTo(1);
        verify(mapper).revokeByAuthIdAndUserId("auth-1", "user-1", revokedAt);
    }

    /**
     * 当授权记录不存在或已撤销时，revokeByAuthIdAndUserId 应返回 0。
     */
    @Test
    void revokeByAuthIdAndUserIdShouldReturnZeroWhenNoMatch() {
        Date revokedAt = new Date();
        when(mapper.revokeByAuthIdAndUserId("nonexistent", "user-1", revokedAt)).thenReturn(0);

        int rows = mapper.revokeByAuthIdAndUserId("nonexistent", "user-1", revokedAt);

        assertThat(rows).isZero();
    }
}
