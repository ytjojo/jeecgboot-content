package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserGrowthLedger;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.IContentUserGrowthService;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentUserGrowthServiceImpl implements IContentUserGrowthService {

    @Resource
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Resource
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBehavior(String userId, String sourceType, int pointDelta, int growthDelta) {
        if (pointDelta != 0) {
            pointLedgerMapper.insert(ContentUserPointLedger.of(userId, sourceType, null, pointDelta, "BEHAVIOR_AWARD"));
        }
        if (growthDelta != 0) {
            growthLedgerMapper.insert(ContentUserGrowthLedger.of(userId, sourceType, null, growthDelta, "BEHAVIOR_AWARD"));
        }
        updateProfileSummary(userId, pointDelta, growthDelta);
        if (auditLogMapper != null) {
            auditLogMapper.insert(ContentUserAuditLog.behaviorAwarded(userId, sourceType, pointDelta, growthDelta));
        }
    }

    @Override
    public ContentUserGrowthVO getGrowthSummary(String userId) {
        ContentUserProfile profile = profileMapper.selectOne(
            Wrappers.<ContentUserProfile>lambdaQuery().eq(ContentUserProfile::getUserId, userId).last("limit 1")
        );
        if (profile == null) {
            return new ContentUserGrowthVO()
                .setUserId(userId)
                .setPointBalance(0)
                .setGrowthValue(0)
                .setLevel(1);
        }
        return new ContentUserGrowthVO()
            .setUserId(userId)
            .setPointBalance(defaultZero(profile.getPointBalance()))
            .setGrowthValue(defaultZero(profile.getGrowthValue()))
            .setLevel(defaultLevel(profile.getLevel(), profile.getGrowthValue()));
    }

    private void updateProfileSummary(String userId, int pointDelta, int growthDelta) {
        if (profileMapper == null) {
            return;
        }
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            return;
        }
        int nextPointBalance = defaultZero(profile.getPointBalance()) + pointDelta;
        int nextGrowthValue = defaultZero(profile.getGrowthValue()) + growthDelta;
        profile.setPointBalance(Math.max(nextPointBalance, 0));
        profile.setGrowthValue(Math.max(nextGrowthValue, 0));
        profile.setLevel(calculateLevel(profile.getGrowthValue()));
        profileMapper.updateById(profile);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultLevel(Integer level, Integer growthValue) {
        return level == null ? calculateLevel(defaultZero(growthValue)) : level;
    }

    private int calculateLevel(Integer growthValue) {
        int safeGrowth = Math.max(defaultZero(growthValue), 0);
        return Math.max(1, safeGrowth / 100 + 1);
    }
}
