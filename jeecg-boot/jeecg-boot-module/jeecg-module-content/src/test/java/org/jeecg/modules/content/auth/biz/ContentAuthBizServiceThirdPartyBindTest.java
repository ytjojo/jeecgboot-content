package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.req.ContentAuthBindThirdPartyReq;
import org.jeecg.modules.content.auth.req.ContentAuthUnbindThirdPartyReq;
import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;
import org.jeecg.modules.content.user.mapper.ContentUserThirdPartyAuthMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Third-party account bind/unbind business logic tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Third-party account bind and unbind")
class ContentAuthBizServiceThirdPartyBindTest {

    @Mock
    private ContentUserThirdPartyAuthMapper thirdPartyAuthMapper;
    @Mock
    private ContentUserCredentialMapper credentialMapper;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    private static final String USER_ID = "u_1001";
    private static final String ANOTHER_USER_ID = "u_1002";
    private static final String PROVIDER = "WECHAT";
    private static final String OPEN_ID = "wx_open_123";
    private static final String UNION_ID = "wx_union_456";

    @Nested
    @DisplayName("bindThirdParty")
    class BindThirdParty {

        @Test
        @DisplayName("success - valid provider, openId not bound")
        void bindThirdParty_success() {
            ContentAuthBindThirdPartyReq req = new ContentAuthBindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER).setOpenId(OPEN_ID).setUnionId(UNION_ID);
            when(thirdPartyAuthMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            bizService.bindThirdParty(req);

            ArgumentCaptor<ContentUserThirdPartyAuth> authCaptor = ArgumentCaptor.forClass(ContentUserThirdPartyAuth.class);
            verify((BaseMapper<ContentUserThirdPartyAuth>) thirdPartyAuthMapper).insert(authCaptor.capture());
            assertThat(authCaptor.getValue().getUserId()).isEqualTo(USER_ID);
            assertThat(authCaptor.getValue().getAppName()).isEqualTo(PROVIDER);
            assertThat(authCaptor.getValue().getOpenId()).isEqualTo(OPEN_ID);
            assertThat(authCaptor.getValue().getUnionId()).isEqualTo(UNION_ID);
            assertThat(authCaptor.getValue().getStatus()).isEqualTo("ACTIVE");
            assertThat(authCaptor.getValue().getAuthTime()).isNotNull();

            ArgumentCaptor<ContentUserCredential> credCaptor = ArgumentCaptor.forClass(ContentUserCredential.class);
            verify((BaseMapper<ContentUserCredential>) credentialMapper).insert(credCaptor.capture());
            assertThat(credCaptor.getValue().getUserId()).isEqualTo(USER_ID);
            assertThat(credCaptor.getValue().getCredentialType()).isEqualTo(CredentialTypeEnum.THIRD_PARTY.getCode());
            assertThat(credCaptor.getValue().getCredentialValue()).isEqualTo(PROVIDER + ":" + OPEN_ID);
            assertThat(credCaptor.getValue().getVerified()).isTrue();
            assertThat(credCaptor.getValue().getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("fail - invalid provider")
        void bindThirdParty_invalidProvider() {
            ContentAuthBindThirdPartyReq req = new ContentAuthBindThirdPartyReq()
                    .setUserId(USER_ID).setProvider("FACEBOOK").setOpenId(OPEN_ID);
            assertThatThrownBy(() -> bizService.bindThirdParty(req))
                    .isInstanceOf(JeecgBootException.class).hasMessage("不支持的第三方平台");
            verify((BaseMapper<ContentUserThirdPartyAuth>) thirdPartyAuthMapper, never()).insert((ContentUserThirdPartyAuth) any());
        }

        @Test
        @DisplayName("fail - empty openId")
        void bindThirdParty_emptyOpenId() {
            ContentAuthBindThirdPartyReq req = new ContentAuthBindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER).setOpenId("");
            assertThatThrownBy(() -> bizService.bindThirdParty(req))
                    .isInstanceOf(JeecgBootException.class).hasMessage("第三方开放ID不能为空");
            verify((BaseMapper<ContentUserThirdPartyAuth>) thirdPartyAuthMapper, never()).insert((ContentUserThirdPartyAuth) any());
        }

        @Test
        @DisplayName("fail - already bound to same user")
        void bindThirdParty_alreadyBoundToSameUser() {
            ContentAuthBindThirdPartyReq req = new ContentAuthBindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER).setOpenId(OPEN_ID);
            ContentUserThirdPartyAuth existing = new ContentUserThirdPartyAuth()
                    .setUserId(USER_ID).setAppName(PROVIDER).setOpenId(OPEN_ID).setStatus("ACTIVE");
            when(thirdPartyAuthMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            assertThatThrownBy(() -> bizService.bindThirdParty(req))
                    .isInstanceOf(JeecgBootException.class).hasMessage("该第三方账号已绑定");
            verify((BaseMapper<ContentUserThirdPartyAuth>) thirdPartyAuthMapper, never()).insert((ContentUserThirdPartyAuth) any());
        }

        @Test
        @DisplayName("fail - bound to different user")
        void bindThirdParty_boundToDifferentUser() {
            ContentAuthBindThirdPartyReq req = new ContentAuthBindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER).setOpenId(OPEN_ID);
            ContentUserThirdPartyAuth other = new ContentUserThirdPartyAuth()
                    .setUserId(ANOTHER_USER_ID).setAppName(PROVIDER).setOpenId(OPEN_ID).setStatus("ACTIVE");
            when(thirdPartyAuthMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(other);
            assertThatThrownBy(() -> bizService.bindThirdParty(req))
                    .isInstanceOf(JeecgBootException.class).hasMessage("该第三方账号已被其他用户绑定");
            verify((BaseMapper<ContentUserThirdPartyAuth>) thirdPartyAuthMapper, never()).insert((ContentUserThirdPartyAuth) any());
        }
    }

    @Nested
    @DisplayName("unbindThirdParty")
    class UnbindThirdParty {

        @Test
        @DisplayName("success - has other login methods")
        void unbindThirdParty_success() {
            ContentAuthUnbindThirdPartyReq req = new ContentAuthUnbindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER);
            ContentUserThirdPartyAuth binding = new ContentUserThirdPartyAuth()
                    .setUserId(USER_ID).setAppName(PROVIDER).setOpenId(OPEN_ID).setStatus("ACTIVE");
            when(thirdPartyAuthMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(binding);
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

            ContentUserCredential thirdPartyCred = new ContentUserCredential()
                    .setUserId(USER_ID).setCredentialType(CredentialTypeEnum.THIRD_PARTY.getCode())
                    .setCredentialValue(PROVIDER + ":" + OPEN_ID).setStatus("ACTIVE");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(thirdPartyCred);

            bizService.unbindThirdParty(req);

            ArgumentCaptor<ContentUserThirdPartyAuth> authCaptor = ArgumentCaptor.forClass(ContentUserThirdPartyAuth.class);
            verify(thirdPartyAuthMapper).updateById(authCaptor.capture());
            assertThat(authCaptor.getValue().getStatus()).isEqualTo("REVOKED");
            assertThat(authCaptor.getValue().getRevokedAt()).isNotNull();

            ArgumentCaptor<ContentUserCredential> credCaptor = ArgumentCaptor.forClass(ContentUserCredential.class);
            verify(credentialMapper).updateById(credCaptor.capture());
            assertThat(credCaptor.getValue().getStatus()).isEqualTo("DISABLED");
        }

        @Test
        @DisplayName("fail - last login method")
        void unbindThirdParty_lastMethod() {
            ContentAuthUnbindThirdPartyReq req = new ContentAuthUnbindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER);
            ContentUserThirdPartyAuth binding = new ContentUserThirdPartyAuth()
                    .setUserId(USER_ID).setAppName(PROVIDER).setOpenId(OPEN_ID).setStatus("ACTIVE");
            when(thirdPartyAuthMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(binding);
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
            assertThatThrownBy(() -> bizService.unbindThirdParty(req))
                    .isInstanceOf(JeecgBootException.class).hasMessage("不能解绑最后一种登录方式，请先绑定其他方式");
            verify(thirdPartyAuthMapper, never()).updateById(any(ContentUserThirdPartyAuth.class));
        }

        @Test
        @DisplayName("fail - binding not found")
        void unbindThirdParty_notFound() {
            ContentAuthUnbindThirdPartyReq req = new ContentAuthUnbindThirdPartyReq()
                    .setUserId(USER_ID).setProvider(PROVIDER);
            when(thirdPartyAuthMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            assertThatThrownBy(() -> bizService.unbindThirdParty(req))
                    .isInstanceOf(JeecgBootException.class).hasMessage("未找到绑定记录");
            verify(thirdPartyAuthMapper, never()).updateById(any(ContentUserThirdPartyAuth.class));
        }
    }
}
