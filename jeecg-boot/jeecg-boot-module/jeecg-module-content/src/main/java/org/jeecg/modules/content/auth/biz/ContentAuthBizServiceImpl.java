package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.entity.ContentUserPasswordHistory;
import org.jeecg.modules.content.auth.enums.AuthIdentityTypeEnum;
import org.jeecg.modules.content.auth.enums.CredentialTypeEnum;
import org.jeecg.modules.content.auth.enums.ThirdPartyProviderEnum;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserPasswordHistoryMapper;
import org.jeecg.modules.content.auth.req.*;
import org.jeecg.modules.content.auth.service.EmailSenderPort;
import org.jeecg.modules.content.auth.service.IContentTokenService;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.jeecg.modules.content.auth.service.LoginTokenGeneratorPort;
import org.jeecg.modules.content.auth.dto.ThirdPartyAuthResult;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserThirdPartyAuthMapper;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 认证业务编排服务实现。
 */
@Slf4j
@Service
public class ContentAuthBizServiceImpl implements ContentAuthBizService {

    /** 手机号正则 */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");
    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @Resource
    private ContentUserCredentialMapper credentialMapper;

    @Resource
    private ContentUserAccountMapper accountMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Resource
    private SystemUserAccountGateway systemUserAccountGateway;

    @Resource
    private IContentTokenService tokenService;

    @Resource
    private EmailSenderPort emailSenderPort;

    @Resource
    private IContentVerificationCodeService codeService;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private LoginTokenGeneratorPort loginTokenGeneratorPort;

    @Resource
    private ContentUserThirdPartyAuthMapper thirdPartyAuthMapper;

    @Resource
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Resource
    private ContentUserPasswordHistoryMapper passwordHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerByMobile(ContentAuthMobileRegisterReq req) {
        // 1. 校验短信验证码
        boolean codeValid = codeService.verifyCode(
                VerificationCodeSceneEnum.REGISTER, req.getMobile(), req.getCode());
        if (!codeValid) {
            throw new JeecgBootException("验证码无效或已过期");
        }

        // 2. 检查手机号是否已注册
        Long count = credentialMapper.selectCount(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.SMS_CODE.getCode())
                        .eq(ContentUserCredential::getCredentialValue, req.getMobile())
        );
        if (count > 0) {
            throw new JeecgBootException("该手机号已被注册");
        }

        // 3. 创建平台用户
        ContentRegisterReq gatewayReq = new ContentRegisterReq()
                .setMobile(req.getMobile())
                .setNickname(req.getNickname())
                .setInviteCode(req.getInviteCode());
        String userId = systemUserAccountGateway.createUser(gatewayReq);

        // 4. 创建凭证记录
        ContentUserCredential credential = new ContentUserCredential()
                .setUserId(userId)
                .setCredentialType(CredentialTypeEnum.SMS_CODE.getCode())
                .setCredentialValue(req.getMobile())
                .setVerified(true)
                .setStatus("ACTIVE");
        credentialMapper.insert(credential);

        // 5. 创建账号记录
        ContentUserAccount account = new ContentUserAccount()
                .setUserId(userId)
                .setNickname(req.getNickname())
                .setAccountStatus("ACTIVE")
                .setCancellationStatus("NONE");
        accountMapper.insert(account);

        // 6. 初始化用户资料
        ContentUserProfile profile = new ContentUserProfile()
                .setUserId(userId)
                .setNickname(req.getNickname())
                .setLevel(1)
                .setPointBalance(0)
                .setGrowthValue(0);
        profileMapper.insert(profile);

        // 7. 初始化通知设置
        ContentUserNotificationSetting notificationSetting = ContentUserNotificationSetting.defaults(userId);
        notificationSettingMapper.insert(notificationSetting);

        log.info("用户手机号注册成功, userId={}, mobile={}", userId, req.getMobile());
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerByEmail(ContentAuthEmailRegisterReq req) {
        // 1. 检查邮箱是否已注册
        Long count = credentialMapper.selectCount(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.EMAIL_CODE.getCode())
                        .eq(ContentUserCredential::getCredentialValue, req.getEmail())
        );
        if (count > 0) {
            throw new JeecgBootException("该邮箱已被注册");
        }

        // 2. 创建平台用户
        ContentEmailRegisterReq gatewayReq = new ContentEmailRegisterReq()
                .setEmail(req.getEmail())
                .setPassword(req.getPassword())
                .setNickname(req.getNickname())
                .setInviteCode(req.getInviteCode());
        String userId = systemUserAccountGateway.createUserByEmail(gatewayReq);

        // 3. 创建凭证记录
        ContentUserCredential credential = new ContentUserCredential()
                .setUserId(userId)
                .setCredentialType(CredentialTypeEnum.EMAIL_CODE.getCode())
                .setCredentialValue(req.getEmail())
                .setVerified(false)
                .setStatus("ACTIVE");
        credentialMapper.insert(credential);

        // 4. 创建账号记录
        ContentUserAccount account = new ContentUserAccount()
                .setUserId(userId)
                .setNickname(req.getNickname())
                .setAccountStatus("ACTIVE")
                .setCancellationStatus("NONE");
        accountMapper.insert(account);

        // 5. 初始化用户资料
        ContentUserProfile profile = new ContentUserProfile()
                .setUserId(userId)
                .setNickname(req.getNickname());
        profileMapper.insert(profile);

        // 6. 生成邮箱验证token并发送确认邮件
        String token = tokenService.generateEmailVerifyToken(userId, req.getEmail());
        String confirmUrl = "https://example.com/auth/confirm-email?token=" + token;
        String htmlContent = "<p>请点击以下链接确认您的邮箱：</p><a href=\"" + confirmUrl + "\">确认邮箱</a>";
        emailSenderPort.send(req.getEmail(), "请确认您的邮箱", htmlContent);

        log.info("用户邮箱注册成功, userId={}, email={}", userId, req.getEmail());
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmEmail(String token) {
        // 1. 验证并消费token
        String userId = tokenService.validateAndConsumeToken(token, "EMAIL_VERIFY");
        if (userId == null) {
            throw new JeecgBootException("验证链接无效或已过期");
        }

        // 2. 查找邮箱凭证
        ContentUserCredential credential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, userId)
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.EMAIL_CODE.getCode())
        );
        if (credential == null) {
            throw new JeecgBootException("邮箱凭证不存在");
        }

        // 3. 标记为已验证
        credential.setVerified(true)
                .setVerifyTime(new Date());
        credentialMapper.updateById(credential);

        log.info("邮箱验证成功, userId={}", userId);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThirdPartyAuthResult loginByThirdParty(
            String provider, String openId, String unionId,
            String thirdPartyNickname, String thirdPartyAvatar, String rawJson) {

        // 1. 校验provider
        if (provider == null || !ThirdPartyProviderEnum.codes().contains(provider)) {
            throw new JeecgBootException("不支持的第三方平台");
        }

        // 2. 校验openId
        if (openId == null || openId.trim().isEmpty()) {
            throw new JeecgBootException("第三方开放ID不能为空");
        }

        // 3. 查询已有绑定
        ContentUserThirdPartyAuth existingBinding = thirdPartyAuthMapper.selectOne(
                new LambdaQueryWrapper<ContentUserThirdPartyAuth>()
                        .eq(ContentUserThirdPartyAuth::getAppName, provider)
                        .eq(ContentUserThirdPartyAuth::getOpenId, openId)
        );

        if (existingBinding != null) {
            // 绑定存在，检查状态
            if ("REVOKED".equals(existingBinding.getStatus())) {
                throw new JeecgBootException("授权已取消");
            }

            // ACTIVE绑定：检查账号状态
            ContentUserAccount account = accountMapper.selectActiveByUserId(existingBinding.getUserId());
            if (account == null) {
                throw new JeecgBootException("账号已注销");
            }

            // 返回已有用户
            return new ThirdPartyAuthResult()
                    .setUserId(existingBinding.getUserId())
                    .setNewUser(false)
                    .setProfileIncomplete(false);
        }

        // 4. 无已有绑定，创建新用户
        String userId = systemUserAccountGateway.createUserByThirdParty(thirdPartyNickname);

        // 5. 创建账号记录
        ContentUserAccount newAccount = new ContentUserAccount()
                .setUserId(userId)
                .setNickname(thirdPartyNickname)
                .setAccountStatus("ACTIVE")
                .setCancellationStatus("NONE");
        accountMapper.insert(newAccount);

        // 6. 创建第三方凭证
        ContentUserCredential credential = new ContentUserCredential()
                .setUserId(userId)
                .setCredentialType(CredentialTypeEnum.THIRD_PARTY.getCode())
                .setCredentialValue(provider + ":" + openId)
                .setStatus("ACTIVE");
        credentialMapper.insert(credential);

        // 7. 创建第三方授权绑定
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth()
                .setUserId(userId)
                .setAppName(provider)
                .setOpenId(openId)
                .setUnionId(unionId)
                .setNickname(thirdPartyNickname)
                .setAvatar(thirdPartyAvatar)
                .setRawDataJson(rawJson)
                .setAuthTime(new Date())
                .setStatus("ACTIVE");
        thirdPartyAuthMapper.insert(auth);

        // 8. 初始化用户资料
        ContentUserProfile profile = new ContentUserProfile()
                .setUserId(userId)
                .setNickname(thirdPartyNickname)
                .setAvatar(thirdPartyAvatar)
                .setProfileCompletionState("INCOMPLETE")
                .setLevel(1)
                .setPointBalance(0)
                .setGrowthValue(0);
        profileMapper.insert(profile);

        // 9. 初始化通知设置
        ContentUserNotificationSetting notificationSetting = ContentUserNotificationSetting.defaults(userId);
        notificationSettingMapper.insert(notificationSetting);

        log.info("第三方登录新用户注册成功, userId={}, provider={}, openId={}", userId, provider, openId);
        return new ThirdPartyAuthResult()
                .setUserId(userId)
                .setNewUser(true)
                .setProfileIncomplete(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginResult loginByPassword(ContentAuthLoginReq req) {
        String identifier = req.getIdentifier();

        // 1. 判断凭证类型：手机号 or 邮箱
        AuthIdentityTypeEnum identityType = resolveIdentityType(identifier);

        // 2. 查找凭证
        ContentUserCredential credential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.PASSWORD.getCode())
                        .eq(ContentUserCredential::getCredentialValue, identifier)
        );
        if (credential == null) {
            // 通用错误信息，不暴露账号是否存在
            throw new JeecgBootException("账号或密码错误");
        }

        // 3. 获取账号，检查状态
        ContentUserAccount account = accountMapper.selectActiveByUserId(credential.getUserId());
        if (account == null) {
            // 可能已被注销或删除
            throw new JeecgBootException("账号已注销");
        }

        // 4. 检查账号是否被锁定
        if (account.getLockedUntil() != null && account.getLockedUntil().after(new Date())) {
            throw new JeecgBootException("账号已锁定，请稍后再试");
        }

        // 5. 校验密码
        String encryptedPassword = PasswordUtil.encrypt(req.getPassword(), identifier, credential.getSalt());
        if (!encryptedPassword.equals(credential.getCredentialValue())) {
            handlePasswordFail(account);
            throw new JeecgBootException("账号或密码错误");
        }

        // 6. 登录成功：清除失败计数，更新登录信息
        redisTemplate.delete(AuthRedisKeyConstant.PWD_FAIL_PREFIX + account.getId());
        account.setLastLoginTime(new Date());
        accountMapper.updateById(account);

        log.info("密码登录成功, userId={}, identityType={}", credential.getUserId(), identityType);
        return createLoginResult(credential.getUserId(), "PC");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginResult loginBySms(ContentAuthSmsLoginReq req) {
        // 1. 校验短信验证码
        boolean codeValid = codeService.verifyCode(VerificationCodeSceneEnum.LOGIN, req.getMobile(), req.getCode());
        if (!codeValid) {
            throw new JeecgBootException("验证码错误或已过期");
        }

        // 2. 查找手机号凭证
        ContentUserCredential credential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.PASSWORD.getCode())
                        .eq(ContentUserCredential::getCredentialValue, req.getMobile())
        );
        if (credential == null) {
            throw new JeecgBootException("该手机号未注册");
        }

        // 3. 获取账号，检查状态
        ContentUserAccount account = accountMapper.selectActiveByUserId(credential.getUserId());
        if (account == null) {
            throw new JeecgBootException("账号已注销");
        }

        // 4. 更新登录信息
        account.setLastLoginTime(new Date());
        accountMapper.updateById(account);

        log.info("短信验证码登录成功, userId={}, mobile={}", credential.getUserId(), req.getMobile());
        return createLoginResult(credential.getUserId(), "PC");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindMobile(ContentAuthBindMobileReq req) {
        bindCredential(req.getUserId(), req.getMobile(), CredentialTypeEnum.SMS_CODE,
                VerificationCodeSceneEnum.BIND_MOBILE, req.getCode());
        log.info("手机号绑定成功, userId={}, mobile={}", req.getUserId(), req.getMobile());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindEmail(ContentAuthBindEmailReq req) {
        bindCredential(req.getUserId(), req.getEmail(), CredentialTypeEnum.EMAIL_CODE,
                VerificationCodeSceneEnum.BIND_EMAIL, req.getCode());
        log.info("邮箱绑定成功, userId={}, email={}", req.getUserId(), req.getEmail());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebindMobile(ContentAuthRebindMobileReq req) {
        rebindCredential(req.getUserId(), req.getOldCode(), req.getNewMobile(), req.getNewCode(),
                CredentialTypeEnum.SMS_CODE, VerificationCodeSceneEnum.UNBIND_MOBILE, VerificationCodeSceneEnum.BIND_MOBILE);
        log.info("手机号换绑成功, userId={}, newMobile={}", req.getUserId(), req.getNewMobile());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebindEmail(ContentAuthRebindEmailReq req) {
        rebindCredential(req.getUserId(), req.getOldCode(), req.getNewEmail(), req.getNewCode(),
                CredentialTypeEnum.EMAIL_CODE, VerificationCodeSceneEnum.UNBIND_EMAIL, VerificationCodeSceneEnum.BIND_EMAIL);
        log.info("邮箱换绑成功, userId={}, newEmail={}", req.getUserId(), req.getNewEmail());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindMobile(ContentAuthUnbindMobileReq req) {
        unbindCredential(req.getUserId(), req.getCode(), CredentialTypeEnum.SMS_CODE,
                VerificationCodeSceneEnum.UNBIND_MOBILE);
        log.info("手机号解绑成功, userId={}", req.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindEmail(ContentAuthUnbindEmailReq req) {
        unbindCredential(req.getUserId(), req.getCode(), CredentialTypeEnum.EMAIL_CODE,
                VerificationCodeSceneEnum.UNBIND_EMAIL);
        log.info("邮箱解绑成功, userId={}", req.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ContentAuthPasswordResetReq req) {
        // 1. 根据重置类型校验验证码
        String identifier;
        if ("MOBILE".equals(req.getResetType())) {
            // 查找手机号凭证获取标识符
            ContentUserCredential smsCredential = credentialMapper.selectOne(
                    new LambdaQueryWrapper<ContentUserCredential>()
                            .eq(ContentUserCredential::getUserId, req.getUserId())
                            .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.SMS_CODE.getCode())
                            .eq(ContentUserCredential::getStatus, "ACTIVE")
            );
            if (smsCredential == null) {
                throw new JeecgBootException("当前未绑定手机号");
            }
            identifier = smsCredential.getCredentialValue();
            boolean codeValid = codeService.verifyCode(
                    VerificationCodeSceneEnum.RESET_PASSWORD, identifier, req.getCode());
            if (!codeValid) {
                throw new JeecgBootException("验证码无效或已过期");
            }
        } else if ("EMAIL".equals(req.getResetType())) {
            ContentUserCredential emailCredential = credentialMapper.selectOne(
                    new LambdaQueryWrapper<ContentUserCredential>()
                            .eq(ContentUserCredential::getUserId, req.getUserId())
                            .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.EMAIL_CODE.getCode())
                            .eq(ContentUserCredential::getStatus, "ACTIVE")
            );
            if (emailCredential == null) {
                throw new JeecgBootException("当前未绑定邮箱");
            }
            identifier = emailCredential.getCredentialValue();
            boolean codeValid = codeService.verifyCode(
                    VerificationCodeSceneEnum.RESET_PASSWORD, identifier, req.getCode());
            if (!codeValid) {
                throw new JeecgBootException("验证码无效或已过期");
            }
        } else {
            throw new JeecgBootException("不支持的重置类型");
        }

        checkPasswordHistory(req.getUserId(), req.getNewPassword(), identifier);
        updatePassword(req.getUserId(), req.getNewPassword(), identifier);
        log.info("密码重置成功, userId={}, resetType={}", req.getUserId(), req.getResetType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindThirdParty(ContentAuthBindThirdPartyReq req) {
        // 1. 校验provider
        if (req.getProvider() == null || !ThirdPartyProviderEnum.codes().contains(req.getProvider())) {
            throw new JeecgBootException("不支持的第三方平台");
        }

        // 2. 校验openId
        if (req.getOpenId() == null || req.getOpenId().trim().isEmpty()) {
            throw new JeecgBootException("第三方开放ID不能为空");
        }

        // 3. 查询已有绑定
        ContentUserThirdPartyAuth existingBinding = thirdPartyAuthMapper.selectOne(
                new LambdaQueryWrapper<ContentUserThirdPartyAuth>()
                        .eq(ContentUserThirdPartyAuth::getAppName, req.getProvider())
                        .eq(ContentUserThirdPartyAuth::getOpenId, req.getOpenId())
        );

        if (existingBinding != null) {
            if (req.getUserId().equals(existingBinding.getUserId())) {
                throw new JeecgBootException("该第三方账号已绑定");
            } else {
                throw new JeecgBootException("该第三方账号已被其他用户绑定");
            }
        }

        // 4. 创建第三方授权绑定
        ContentUserThirdPartyAuth auth = new ContentUserThirdPartyAuth();
        auth.setUserId(req.getUserId());
        auth.setAppName(req.getProvider());
        auth.setOpenId(req.getOpenId());
        auth.setUnionId(req.getUnionId());
        auth.setAuthTime(new Date());
        auth.setStatus("ACTIVE");
        thirdPartyAuthMapper.insert(auth);

        // 5. 创建THIRD_PARTY凭证
        ContentUserCredential credential = new ContentUserCredential()
                .setUserId(req.getUserId())
                .setCredentialType(CredentialTypeEnum.THIRD_PARTY.getCode())
                .setCredentialValue(req.getProvider() + ":" + req.getOpenId())
                .setVerified(true)
                .setStatus("ACTIVE");
        credentialMapper.insert(credential);

        log.info("第三方账号绑定成功, userId={}, provider={}, openId={}", req.getUserId(), req.getProvider(), req.getOpenId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindThirdParty(ContentAuthUnbindThirdPartyReq req) {
        // 1. 查找绑定记录
        ContentUserThirdPartyAuth binding = thirdPartyAuthMapper.selectOne(
                new LambdaQueryWrapper<ContentUserThirdPartyAuth>()
                        .eq(ContentUserThirdPartyAuth::getUserId, req.getUserId())
                        .eq(ContentUserThirdPartyAuth::getAppName, req.getProvider())
                        .eq(ContentUserThirdPartyAuth::getStatus, "ACTIVE")
        );
        if (binding == null || !req.getUserId().equals(binding.getUserId())) {
            throw new JeecgBootException("未找到绑定记录");
        }

        // 2. 检查是否为最后一种登录方式
        long activeLoginMethods = countActiveLoginMethods(req.getUserId());
        if (activeLoginMethods <= 1) {
            throw new JeecgBootException("不能解绑最后一种登录方式，请先绑定其他方式");
        }

        // 4. 撤销第三方授权绑定
        binding.setStatus("REVOKED");
        binding.setRevokedAt(new Date());
        thirdPartyAuthMapper.updateById(binding);

        // 5. 禁用对应的THIRD_PARTY凭证
        ContentUserCredential credential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, req.getUserId())
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.THIRD_PARTY.getCode())
                        .eq(ContentUserCredential::getCredentialValue, req.getProvider() + ":" + binding.getOpenId())
                        .eq(ContentUserCredential::getStatus, "ACTIVE")
        );
        if (credential != null) {
            credential.setStatus("DISABLED");
            credentialMapper.updateById(credential);
        }

        log.info("第三方账号解绑成功, userId={}, provider={}", req.getUserId(), req.getProvider());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByMobile(ContentAuthResetPasswordByMobileReq req) {
        // 1. 校验短信验证码
        boolean codeValid = codeService.verifyCode(
                VerificationCodeSceneEnum.RESET_PASSWORD, req.getMobile(), req.getCode());
        if (!codeValid) {
            throw new JeecgBootException("验证码无效或已过期");
        }

        // 2. 查找手机号凭证获取userId
        ContentUserCredential smsCredential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.SMS_CODE.getCode())
                        .eq(ContentUserCredential::getCredentialValue, req.getMobile())
        );
        if (smsCredential == null) {
            throw new JeecgBootException("该手机号未注册");
        }
        String userId = smsCredential.getUserId();
        checkPasswordHistory(userId, req.getNewPassword(), req.getMobile());
        updatePassword(userId, req.getNewPassword(), req.getMobile());
        log.info("手机号重置密码成功, userId={}, mobile={}", userId, req.getMobile());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByEmail(ContentAuthResetPasswordByEmailReq req) {
        // 1. 验证并消费token
        String userId = tokenService.validateAndConsumeToken(req.getToken(), "PASSWORD_RESET");
        if (userId == null) {
            throw new JeecgBootException("重置链接无效或已过期");
        }

        // 2. 查找邮箱凭证获取邮箱地址
        ContentUserCredential emailCredential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, userId)
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.EMAIL_CODE.getCode())
        );
        if (emailCredential == null) {
            throw new JeecgBootException("邮箱凭证不存在");
        }
        String email = emailCredential.getCredentialValue();
        checkPasswordHistory(userId, req.getNewPassword(), email);
        updatePassword(userId, req.getNewPassword(), email);
        log.info("邮箱重置密码成功, userId={}", userId);
    }

    private AuthLoginResult createLoginResult(String userId, String clientType) {
        String token = loginTokenGeneratorPort.generateToken(userId, clientType);
        String jti = UUID.randomUUID().toString();
        ContentUserDeviceSession session = new ContentUserDeviceSession()
                .setUserId(userId)
                .setSessionToken(token)
                .setTokenJti(jti)
                .setSessionStatus("ACTIVE")
                .setLastActiveTime(new Date());
        deviceSessionMapper.insert(session);
        return new AuthLoginResult()
                .setUserId(userId)
                .setAccessToken(token)
                .setTokenType("Bearer")
                .setJti(jti);
    }

    private void bindCredential(String userId, String value, CredentialTypeEnum credType,
                                VerificationCodeSceneEnum scene, String code) {
        boolean codeValid = codeService.verifyCode(scene, value, code);
        if (!codeValid) {
            throw new JeecgBootException("验证码无效或已过期");
        }
        ContentUserCredential existing = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, credType.getCode())
                        .eq(ContentUserCredential::getCredentialValue, value)
                        .eq(ContentUserCredential::getStatus, "ACTIVE")
        );
        if (existing != null) {
            throw new JeecgBootException(credencyTypeToLabel(credType) + "已被其他账号绑定");
        }
        ContentUserCredential credential = new ContentUserCredential()
                .setUserId(userId)
                .setCredentialType(credType.getCode())
                .setCredentialValue(value)
                .setVerified(true)
                .setVerifyTime(new Date())
                .setStatus("ACTIVE");
        credentialMapper.insert(credential);
    }

    private void unbindCredential(String userId, String code, CredentialTypeEnum credType,
                                  VerificationCodeSceneEnum scene) {
        ContentUserCredential credential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, userId)
                        .eq(ContentUserCredential::getCredentialType, credType.getCode())
                        .eq(ContentUserCredential::getStatus, "ACTIVE")
        );
        if (credential == null) {
            throw new JeecgBootException("当前未绑定" + credencyTypeToLabel(credType));
        }
        boolean codeValid = codeService.verifyCode(scene, credential.getCredentialValue(), code);
        if (!codeValid) {
            throw new JeecgBootException("验证码无效或已过期");
        }
        long activeLoginMethods = countActiveLoginMethods(userId);
        if (activeLoginMethods <= 1) {
            throw new JeecgBootException("不能解绑最后一种登录方式，请先绑定其他方式");
        }
        credential.setStatus("DISABLED");
        credentialMapper.updateById(credential);
    }

    private void rebindCredential(String userId, String oldCode, String newValue, String newCode,
                                  CredentialTypeEnum credType,
                                  VerificationCodeSceneEnum unbindScene,
                                  VerificationCodeSceneEnum bindScene) {
        // 查找旧凭证
        ContentUserCredential oldCredential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, userId)
                        .eq(ContentUserCredential::getCredentialType, credType.getCode())
                        .eq(ContentUserCredential::getStatus, "ACTIVE")
        );
        if (oldCredential == null) {
            throw new JeecgBootException("当前未绑定" + credencyTypeToLabel(credType));
        }
        String oldValue = oldCredential.getCredentialValue();
        // 验证旧值验证码
        boolean oldCodeValid = codeService.verifyCode(unbindScene, oldValue, oldCode);
        if (!oldCodeValid) {
            throw new JeecgBootException("旧验证码无效或已过期");
        }
        // 验证新值验证码
        boolean newCodeValid = codeService.verifyCode(bindScene, newValue, newCode);
        if (!newCodeValid) {
            throw new JeecgBootException("新验证码无效或已过期");
        }
        // 检查新值是否已被其他用户绑定
        Long conflictCount = credentialMapper.selectCount(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getCredentialType, credType.getCode())
                        .eq(ContentUserCredential::getCredentialValue, newValue)
                        .eq(ContentUserCredential::getStatus, "ACTIVE")
        );
        if (conflictCount > 0) {
            throw new JeecgBootException(credencyTypeToLabel(credType) + "已被其他用户绑定");
        }
        // 禁用旧凭证
        oldCredential.setStatus("DISABLED");
        credentialMapper.updateById(oldCredential);
        // 创建新凭证
        ContentUserCredential newCredential = new ContentUserCredential()
                .setUserId(userId)
                .setCredentialType(credType.getCode())
                .setCredentialValue(newValue)
                .setVerified(true)
                .setStatus("ACTIVE");
        credentialMapper.insert(newCredential);
    }

    private void updatePassword(String userId, String newPassword, String identifier) {
        ContentUserCredential passwordCredential = credentialMapper.selectOne(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, userId)
                        .eq(ContentUserCredential::getCredentialType, CredentialTypeEnum.PASSWORD.getCode())
        );
        String newSalt = UUID.randomUUID().toString().substring(0, 8);
        String newEncryptedPassword = PasswordUtil.encrypt(newPassword, identifier, newSalt);
        if (passwordCredential != null) {
            savePasswordHistory(userId, passwordCredential.getCredentialValue(), passwordCredential.getSalt());
            passwordCredential.setCredentialValue(newEncryptedPassword);
            passwordCredential.setSalt(newSalt);
            credentialMapper.updateById(passwordCredential);
        } else {
            ContentUserCredential newCredential = new ContentUserCredential()
                    .setUserId(userId)
                    .setCredentialType(CredentialTypeEnum.PASSWORD.getCode())
                    .setCredentialValue(newEncryptedPassword)
                    .setSalt(newSalt)
                    .setStatus("ACTIVE");
            credentialMapper.insert(newCredential);
        }
        savePasswordHistory(userId, newEncryptedPassword, newSalt);
    }

    /**
     * 统计用户当前活跃的登录方式数量（MOBILE + EMAIL + THIRD_PARTY）。
     */
    private long countActiveLoginMethods(String userId) {
        return credentialMapper.selectCount(
                new LambdaQueryWrapper<ContentUserCredential>()
                        .eq(ContentUserCredential::getUserId, userId)
                        .eq(ContentUserCredential::getStatus, "ACTIVE")
                        .in(ContentUserCredential::getCredentialType,
                                CredentialTypeEnum.SMS_CODE.getCode(),
                                CredentialTypeEnum.EMAIL_CODE.getCode(),
                                CredentialTypeEnum.THIRD_PARTY.getCode())
        );
    }

    private static String credencyTypeToLabel(CredentialTypeEnum type) {
        return switch (type) {
            case SMS_CODE -> "手机号";
            case EMAIL_CODE -> "邮箱";
            case THIRD_PARTY -> "第三方账号";
            default -> "凭证";
        };
    }

    /**
     * 根据标识符判断身份类型。
     */
    private AuthIdentityTypeEnum resolveIdentityType(String identifier) {
        if (MOBILE_PATTERN.matcher(identifier).matches()) {
            return AuthIdentityTypeEnum.MOBILE;
        }
        if (EMAIL_PATTERN.matcher(identifier).matches()) {
            return AuthIdentityTypeEnum.EMAIL;
        }
        // 默认当作手机号处理
        return AuthIdentityTypeEnum.MOBILE;
    }

    /**
     * 检查新密码是否在最近3次密码历史中。
     *
     * @param userId      用户ID
     * @param newPassword 新密码明文
     * @param identifier  加密用的标识符（手机号或邮箱）
     */
    private void checkPasswordHistory(String userId, String newPassword, String identifier) {
        List<ContentUserPasswordHistory> historyList = passwordHistoryMapper.selectList(
                new LambdaQueryWrapper<ContentUserPasswordHistory>()
                        .eq(ContentUserPasswordHistory::getUserId, userId)
                        .orderByDesc(ContentUserPasswordHistory::getCreateTime)
                        .last("LIMIT 3")
        );

        for (ContentUserPasswordHistory history : historyList) {
            String encryptedNewPassword = PasswordUtil.encrypt(newPassword, identifier, history.getSalt());
            if (encryptedNewPassword != null && encryptedNewPassword.equals(history.getPasswordHash())) {
                throw new JeecgBootException("不能使用最近3次使用过的密码");
            }
        }
    }

    /**
     * 保存密码到历史记录。
     *
     * @param userId       用户ID
     * @param passwordHash 密码哈希
     * @param salt         盐值
     */
    private void savePasswordHistory(String userId, String passwordHash, String salt) {
        ContentUserPasswordHistory history = new ContentUserPasswordHistory()
                .setUserId(userId)
                .setPasswordHash(passwordHash)
                .setSalt(salt);
        passwordHistoryMapper.insert(history);
    }

    /**
     * 处理密码错误：累加失败计数，达到阈值时触发验证码挑战或锁定账号。
     */
    private void handlePasswordFail(ContentUserAccount account) {
        String failKey = AuthRedisKeyConstant.PWD_FAIL_PREFIX + account.getId();
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount == null) {
            failCount = 1L;
        }
        // 设置过期时间（仅首次设置）
        if (failCount == 1) {
            redisTemplate.expire(failKey, AuthRedisKeyConstant.PWD_FAIL_TTL, TimeUnit.SECONDS);
        }

        if (failCount >= AuthRedisKeyConstant.LOGIN_FAIL_LOCK_THRESHOLD) {
            // 锁定账号30分钟
            account.setLockedUntil(new Date(System.currentTimeMillis() + AuthRedisKeyConstant.LOGIN_FAIL_LOCK_DURATION_MS));
            accountMapper.updateById(account);
            throw new JeecgBootException("账号已锁定，请30分钟后再试");
        }
        if (failCount >= AuthRedisKeyConstant.LOGIN_FAIL_CAPTCHA_THRESHOLD) {
            throw new JeecgBootException("密码错误次数过多，请输入验证码");
        }
    }
}
