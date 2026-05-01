package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserAppeal;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.mapper.ContentUserAppealMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserStatusRecordMapper;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.content.user.service.IContentAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation for content account.
 */
@Slf4j
@Service
public class ContentAccountServiceImpl implements IContentAccountService {

    private static final long CANCEL_COOLING_PERIOD_MILLIS = TimeUnit.DAYS.toMillis(7);

    @Resource
    private SystemUserAccountGateway systemUserAccountGateway;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Resource
    private ContentUserStatusRecordMapper statusRecordMapper;

    @Resource
    private ContentUserAppealMapper appealMapper;

    /**
     * Registers a community user by mobile and initializes the user profile.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerByMobile(ContentRegisterReq req) {
        String userId = systemUserAccountGateway.createUser(req);
        ContentUserProfile profile = new ContentUserProfile();
        profile.setId(UUIDGenerator.generate());
        profile
            .setUserId(userId)
            .setNickname(req.getNickname())
            .setAvatar(null)
            .setStatus(ContentUserStatusEnum.REGISTERED_INCOMPLETE.getCode())
            .setLevel(1)
            .setPointBalance(0)
            .setGrowthValue(0);
        profileMapper.insert(profile);

        ContentUserNotificationSetting notificationSetting = ContentUserNotificationSetting.defaults(userId);
        notificationSetting.setId(UUIDGenerator.generate());
        notificationSettingMapper.insert(notificationSetting);
        return userId;
    }

    /**
     * Resets the account password for the matched platform user.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ContentPasswordResetReq req) {
        // 密码找回属于敏感操作，必须先完成二次校验。
        if (!Boolean.TRUE.equals(req.getSecondaryVerified())) {
            throw new JeecgBootException("密码重置需先完成二次校验");
        }
        systemUserAccountGateway.resetPassword(req);
    }

    /**
     * Starts the account cancellation flow and records the pending status.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initiateCancel(String userId, String operatorUserId, String reason) {
        validateCancelPrerequisites(userId);
        Date now = new Date();
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        String currentStatus = profile != null && profile.getStatus() != null
            ? profile.getStatus()
            : ContentUserStatusEnum.NORMAL.getCode();
        ContentUserStatusRecord record = new ContentUserStatusRecord();
        record.setId(UUIDGenerator.generate());
        record
            .setUserId(userId)
            .setCurrentStatus(currentStatus)
            .setTargetStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode())
            .setOperatorUserId(operatorUserId)
            .setReason(reason)
            .setTriggerSource("USER_CANCEL_APPLY")
            .setRecoverable(Boolean.TRUE)
            .setEffectiveStartTime(now)
            .setEffectiveEndTime(new Date(now.getTime() + CANCEL_COOLING_PERIOD_MILLIS));
        statusRecordMapper.insert(record);
        if (profile != null) {
            profile.setStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
            profileMapper.updateById(profile);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeCancel(String userId, String operatorUserId) {
        Date now = new Date();
        ContentUserProfile profile = requireProfile(userId);
        ContentUserStatusRecord latestRecord = requirePendingCancelRecord(userId);
        if (latestRecord.getEffectiveEndTime() != null && latestRecord.getEffectiveEndTime().after(now)) {
            throw new JeecgBootException("注销冷静期未结束");
        }
        systemUserAccountGateway.markCancelled(userId);
        profile.setStatus(ContentUserStatusEnum.CANCELLED.getCode());
        profileMapper.updateById(profile);
        ContentUserStatusRecord record = new ContentUserStatusRecord();
        record.setId(UUIDGenerator.generate());
        record.setUserId(userId);
        record.setCurrentStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        record.setTargetStatus(ContentUserStatusEnum.CANCELLED.getCode());
        record.setOperatorUserId(operatorUserId);
        record.setTriggerSource("USER_CANCEL_COMPLETE");
        record.setRecoverable(Boolean.FALSE);
        record.setEffectiveStartTime(now);
        record.setReason("冷静期结束，完成注销");
        statusRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeCancel(String userId, String operatorUserId, String reason) {
        Date now = new Date();
        ContentUserProfile profile = requireProfile(userId);
        ContentUserStatusRecord latestRecord = requirePendingCancelRecord(userId);
        String restoredStatus = latestRecord.getCurrentStatus() == null
            ? ContentUserStatusEnum.NORMAL.getCode()
            : latestRecord.getCurrentStatus();
        profile.setStatus(restoredStatus);
        profileMapper.updateById(profile);
        ContentUserStatusRecord record = new ContentUserStatusRecord();
        record.setId(UUIDGenerator.generate());
        record.setUserId(userId);
        record.setCurrentStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode());
        record.setTargetStatus(restoredStatus);
        record.setOperatorUserId(operatorUserId);
        record.setTriggerSource("USER_CANCEL_REVOKE");
        record.setRecoverable(Boolean.TRUE);
        record.setEffectiveStartTime(now);
        record.setReason(reason);
        statusRecordMapper.insert(record);
    }

    private void validateCancelPrerequisites(String userId) {
        if (appealMapper == null) {
            return;
        }
        ContentUserAppeal pendingAppeal = appealMapper.selectOne(
            Wrappers.<ContentUserAppeal>lambdaQuery()
                .eq(ContentUserAppeal::getUserId, userId)
                .eq(ContentUserAppeal::getStatus, "PENDING")
                .last("limit 1")
        );
        if (pendingAppeal != null) {
            throw new JeecgBootException("存在待处理申诉，暂不可注销");
        }
    }

    private ContentUserProfile requireProfile(String userId) {
        ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在");
        }
        return profile;
    }

    private ContentUserStatusRecord requirePendingCancelRecord(String userId) {
        ContentUserStatusRecord latestRecord = statusRecordMapper.selectLatestByUserId(userId);
        if (latestRecord == null || !ContentUserStatusEnum.CANCEL_PENDING.getCode().equals(latestRecord.getTargetStatus())) {
            throw new JeecgBootException("当前未处于注销冷静期");
        }
        return latestRecord;
    }
}
