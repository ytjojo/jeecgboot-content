package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitRecoveryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Service implementation for level benefit recovery orchestration.
 */
@Service
public class ContentUserLevelBenefitRecoveryServiceImpl
    extends ServiceImpl<ContentUserLevelBenefitPenaltyRecordMapper, ContentUserLevelBenefitPenaltyRecord>
    implements IContentUserLevelBenefitRecoveryService {

    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_RECOVERED = "RECOVERED";

    @Resource
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    /**
     * Restores pending level benefits linked to the specified growth penalty record.
     */
    @Override
    public int recoverByPenaltyRecord(ContentUserGrowthPenaltyRecord record,
                                      String operatorUserId,
                                      Date executeTime,
                                      String reason) {
        if (record == null || !StringUtils.hasText(record.getId())) {
            return 0;
        }
        List<ContentUserLevelBenefitPenaltyRecord> records = levelBenefitPenaltyRecordMapper.selectList(
            Wrappers.<ContentUserLevelBenefitPenaltyRecord>lambdaQuery()
                .eq(ContentUserLevelBenefitPenaltyRecord::getPenaltyRecordId, record.getId())
                .eq(ContentUserLevelBenefitPenaltyRecord::getRecoverStatus, STATUS_PENDING_RECOVER)
        );
        if (records == null || records.isEmpty()) {
            return 0;
        }
        int recoveredCount = 0;
        for (ContentUserLevelBenefitPenaltyRecord item : records) {
            item.setCurrentEnabled(Boolean.TRUE.equals(item.getPreviousEnabled()));
            item.setRecoverStatus(STATUS_RECOVERED);
            item.setRecoverReason(reason);
            item.setRecoveredBy(operatorUserId);
            item.setRecoveredAt(executeTime);
            levelBenefitPenaltyRecordMapper.updateById(item);
            recoveredCount++;
        }
        return recoveredCount;
    }

    /**
     * Checks whether the specified benefit is explicitly enabled for the user.
     */
    @Override
    public boolean hasEnabledBenefit(String userId, String benefitCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(benefitCode)) {
            return false;
        }
        Long count = levelBenefitPenaltyRecordMapper.selectCount(
            Wrappers.<ContentUserLevelBenefitPenaltyRecord>lambdaQuery()
                .eq(ContentUserLevelBenefitPenaltyRecord::getUserId, userId)
                .eq(ContentUserLevelBenefitPenaltyRecord::getBenefitCode, benefitCode)
                .eq(ContentUserLevelBenefitPenaltyRecord::getCurrentEnabled, Boolean.TRUE)
                .eq(ContentUserLevelBenefitPenaltyRecord::getRecoverStatus, STATUS_RECOVERED)
                .last("limit 1")
        );
        return count != null && count > 0L;
    }
}
