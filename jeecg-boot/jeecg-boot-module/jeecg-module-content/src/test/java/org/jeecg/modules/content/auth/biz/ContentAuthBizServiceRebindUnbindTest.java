package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.req.ContentAuthRebindEmailReq;
import org.jeecg.modules.content.auth.req.ContentAuthRebindMobileReq;
import org.jeecg.modules.content.auth.req.ContentAuthUnbindEmailReq;
import org.jeecg.modules.content.auth.req.ContentAuthUnbindMobileReq;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 换绑和解绑业务逻辑测试。
 * 覆盖手机号/邮箱换绑、解绑的核心场景，以及"最后一种登录方式"保护。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ContentAuthBizServiceRebindUnbindTest {

    @Mock
    private ContentUserCredentialMapper credentialMapper;
    @Mock
    private IContentVerificationCodeService codeService;

    @InjectMocks
    private ContentAuthBizServiceImpl bizService;

    @Captor
    private ArgumentCaptor<ContentUserCredential> credentialCaptor;

    private static final String TEST_USER_ID = "u_1001";
    private static final String OLD_MOBILE = "13800138000";
    private static final String NEW_MOBILE = "13900139000";
    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";

    private ContentUserCredential activeMobileCredential;
    private ContentUserCredential activeEmailCredential;

    @BeforeEach
    void setUp() {
        activeMobileCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.SMS_CODE.getCode())
                .setCredentialValue(OLD_MOBILE)
                .setVerified(true)
                .setStatus("ACTIVE");

        activeEmailCredential = new ContentUserCredential()
                .setUserId(TEST_USER_ID)
                .setCredentialType(CredentialTypeEnum.EMAIL_CODE.getCode())
                .setCredentialValue(OLD_EMAIL)
                .setVerified(true)
                .setStatus("ACTIVE");
    }

    // ==================== 换绑手机号测试 ====================

    @Nested
    @DisplayName("换绑手机号")
    class RebindMobile {

        @Test
        @DisplayName("双验证码均有效 - 旧凭证禁用，新凭证创建")
        void rebindMobile_success() {
            // given
            ContentAuthRebindMobileReq req = new ContentAuthRebindMobileReq()
                    .setUserId(TEST_USER_ID)
                    .setOldCode("111111")
                    .setNewMobile(NEW_MOBILE)
                    .setNewCode("222222");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeMobileCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_MOBILE, OLD_MOBILE, "111111"))
                    .thenReturn(true);
            when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_MOBILE, NEW_MOBILE, "222222"))
                    .thenReturn(true);
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(0L);

            // when
            bizService.rebindMobile(req);

            // then: 旧凭证状态更新为DISABLED
            verify(credentialMapper, atLeast(1)).updateById(credentialCaptor.capture());
            ContentUserCredential disabledCred = credentialCaptor.getValue();
            assertThat(disabledCred.getStatus()).isEqualTo("DISABLED");
            assertThat(disabledCred.getCredentialValue()).isEqualTo(OLD_MOBILE);

            // then: 新凭证被插入
            verify(credentialMapper).insert(credentialCaptor.capture());
            ContentUserCredential newCred = credentialCaptor.getValue();
            assertThat(newCred.getCredentialType()).isEqualTo(CredentialTypeEnum.SMS_CODE.getCode());
            assertThat(newCred.getCredentialValue()).isEqualTo(NEW_MOBILE);
            assertThat(newCred.getStatus()).isEqualTo("ACTIVE");
            assertThat(newCred.getVerified()).isTrue();
        }

        @Test
        @DisplayName("旧手机号验证码无效 - 抛出异常")
        void rebindMobile_invalidOldCode_throwsError() {
            // given
            ContentAuthRebindMobileReq req = new ContentAuthRebindMobileReq()
                    .setUserId(TEST_USER_ID)
                    .setOldCode("000000")
                    .setNewMobile(NEW_MOBILE)
                    .setNewCode("222222");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeMobileCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_MOBILE, OLD_MOBILE, "000000"))
                    .thenReturn(false);

            // when / then
            assertThatThrownBy(() -> bizService.rebindMobile(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("旧手机号验证码无效或已过期");

            // 不应有新凭证插入
            verify(credentialMapper, never()).insert(any(ContentUserCredential.class));
        }

        @Test
        @DisplayName("新手机号已被其他用户绑定 - 抛出异常")
        void rebindMobile_newMobileAlreadyBound_throwsError() {
            // given
            ContentAuthRebindMobileReq req = new ContentAuthRebindMobileReq()
                    .setUserId(TEST_USER_ID)
                    .setOldCode("111111")
                    .setNewMobile(NEW_MOBILE)
                    .setNewCode("222222");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeMobileCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_MOBILE, OLD_MOBILE, "111111"))
                    .thenReturn(true);
            when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_MOBILE, NEW_MOBILE, "222222"))
                    .thenReturn(true);
            // 新手机号已有其他用户绑定
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(1L);

            // when / then
            assertThatThrownBy(() -> bizService.rebindMobile(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("该手机号已被其他用户绑定");

            // 旧凭证不应被禁用
            verify(credentialMapper, never()).updateById(any(ContentUserCredential.class));
        }
    }

    // ==================== 换绑邮箱测试 ====================

    @Nested
    @DisplayName("换绑邮箱")
    class RebindEmail {

        @Test
        @DisplayName("双验证码均有效 - 旧凭证禁用，新凭证创建")
        void rebindEmail_success() {
            // given
            ContentAuthRebindEmailReq req = new ContentAuthRebindEmailReq()
                    .setUserId(TEST_USER_ID)
                    .setOldCode("111111")
                    .setNewEmail(NEW_EMAIL)
                    .setNewCode("222222");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeEmailCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_EMAIL, OLD_EMAIL, "111111"))
                    .thenReturn(true);
            when(codeService.verifyCode(VerificationCodeSceneEnum.BIND_EMAIL, NEW_EMAIL, "222222"))
                    .thenReturn(true);
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(0L);

            // when
            bizService.rebindEmail(req);

            // then: 旧凭证状态更新为DISABLED
            verify(credentialMapper, atLeast(1)).updateById(credentialCaptor.capture());
            ContentUserCredential disabledCred = credentialCaptor.getValue();
            assertThat(disabledCred.getStatus()).isEqualTo("DISABLED");
            assertThat(disabledCred.getCredentialValue()).isEqualTo(OLD_EMAIL);

            // then: 新凭证被插入
            verify(credentialMapper).insert(credentialCaptor.capture());
            ContentUserCredential newCred = credentialCaptor.getValue();
            assertThat(newCred.getCredentialType()).isEqualTo(CredentialTypeEnum.EMAIL_CODE.getCode());
            assertThat(newCred.getCredentialValue()).isEqualTo(NEW_EMAIL);
            assertThat(newCred.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("旧邮箱验证码无效 - 抛出异常")
        void rebindEmail_invalidOldCode_throwsError() {
            // given
            ContentAuthRebindEmailReq req = new ContentAuthRebindEmailReq()
                    .setUserId(TEST_USER_ID)
                    .setOldCode("000000")
                    .setNewEmail(NEW_EMAIL)
                    .setNewCode("222222");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeEmailCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_EMAIL, OLD_EMAIL, "000000"))
                    .thenReturn(false);

            // when / then
            assertThatThrownBy(() -> bizService.rebindEmail(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("旧邮箱验证码无效或已过期");

            verify(credentialMapper, never()).insert(any(ContentUserCredential.class));
        }
    }

    // ==================== 解绑手机号测试 ====================

    @Nested
    @DisplayName("解绑手机号")
    class UnbindMobile {

        @Test
        @DisplayName("有其他登录方式 - 解绑成功")
        void unbindMobile_success_withOtherMethods() {
            // given: 用户同时有手机号和邮箱两种登录方式
            ContentAuthUnbindMobileReq req = new ContentAuthUnbindMobileReq()
                    .setUserId(TEST_USER_ID)
                    .setCode("111111");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeMobileCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_MOBILE, OLD_MOBILE, "111111"))
                    .thenReturn(true);
            // countActiveLoginMethods 返回2（手机号+邮箱）
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(2L);

            // when
            bizService.unbindMobile(req);

            // then: 凭证状态更新为DISABLED
            verify(credentialMapper).updateById(credentialCaptor.capture());
            ContentUserCredential disabledCred = credentialCaptor.getValue();
            assertThat(disabledCred.getStatus()).isEqualTo("DISABLED");
            assertThat(disabledCred.getCredentialValue()).isEqualTo(OLD_MOBILE);
        }

        @Test
        @DisplayName("最后一种登录方式 - 抛出异常阻止解绑")
        void unbindMobile_lastMethod_throwsError() {
            // given: 用户只有手机号一种登录方式
            ContentAuthUnbindMobileReq req = new ContentAuthUnbindMobileReq()
                    .setUserId(TEST_USER_ID)
                    .setCode("111111");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeMobileCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_MOBILE, OLD_MOBILE, "111111"))
                    .thenReturn(true);
            // countActiveLoginMethods 返回1（只有手机号）
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(1L);

            // when / then
            assertThatThrownBy(() -> bizService.unbindMobile(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("不能解绑最后一种登录方式，请先绑定其他方式");

            // 凭证不应被禁用
            verify(credentialMapper, never()).updateById(any(ContentUserCredential.class));
        }
    }

    // ==================== 解绑邮箱测试 ====================

    @Nested
    @DisplayName("解绑邮箱")
    class UnbindEmail {

        @Test
        @DisplayName("有其他登录方式 - 解绑成功")
        void unbindEmail_success_withOtherMethods() {
            // given: 用户同时有邮箱和第三方两种登录方式
            ContentAuthUnbindEmailReq req = new ContentAuthUnbindEmailReq()
                    .setUserId(TEST_USER_ID)
                    .setCode("111111");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeEmailCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_EMAIL, OLD_EMAIL, "111111"))
                    .thenReturn(true);
            // countActiveLoginMethods 返回2（邮箱+第三方）
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(2L);

            // when
            bizService.unbindEmail(req);

            // then: 凭证状态更新为DISABLED
            verify(credentialMapper).updateById(credentialCaptor.capture());
            ContentUserCredential disabledCred = credentialCaptor.getValue();
            assertThat(disabledCred.getStatus()).isEqualTo("DISABLED");
            assertThat(disabledCred.getCredentialValue()).isEqualTo(OLD_EMAIL);
        }

        @Test
        @DisplayName("最后一种登录方式 - 抛出异常阻止解绑")
        void unbindEmail_lastMethod_throwsError() {
            // given: 用户只有邮箱一种登录方式
            ContentAuthUnbindEmailReq req = new ContentAuthUnbindEmailReq()
                    .setUserId(TEST_USER_ID)
                    .setCode("111111");
            when(credentialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(activeEmailCredential);
            when(codeService.verifyCode(VerificationCodeSceneEnum.UNBIND_EMAIL, OLD_EMAIL, "111111"))
                    .thenReturn(true);
            // countActiveLoginMethods 返回1（只有邮箱）
            when(credentialMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(1L);

            // when / then
            assertThatThrownBy(() -> bizService.unbindEmail(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("不能解绑最后一种登录方式，请先绑定其他方式");

            // 凭证不应被禁用
            verify(credentialMapper, never()).updateById(any(ContentUserCredential.class));
        }
    }
}
