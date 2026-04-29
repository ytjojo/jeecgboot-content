package org.jeecg.modules.content.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.enums.ContentUserStatusEnum;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
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

@Slf4j
@Service
public class ContentAccountServiceImpl implements IContentAccountService {

    @Resource
    private SystemUserAccountGateway systemUserAccountGateway;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Resource
    private ContentUserStatusRecordMapper statusRecordMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ContentPasswordResetReq req) {
        systemUserAccountGateway.resetPassword(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initiateCancel(String userId, String operatorUserId, String reason) {
        systemUserAccountGateway.markCancelled(userId);
        ContentUserStatusRecord record = new ContentUserStatusRecord();
        record.setId(UUIDGenerator.generate());
        record
            .setUserId(userId)
            .setCurrentStatus(ContentUserStatusEnum.NORMAL.getCode())
            .setTargetStatus(ContentUserStatusEnum.CANCEL_PENDING.getCode())
            .setOperatorUserId(operatorUserId)
            .setReason(reason)
            .setTriggerSource("USER_CANCEL_APPLY")
            .setRecoverable(Boolean.TRUE)
            .setEffectiveStartTime(new Date());
        statusRecordMapper.insert(record);
    }
}
