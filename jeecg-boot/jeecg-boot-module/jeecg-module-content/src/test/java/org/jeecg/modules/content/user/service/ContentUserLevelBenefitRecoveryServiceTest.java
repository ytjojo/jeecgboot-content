package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.user.entity.ContentUserGrowthPenaltyRecord;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitPenaltyRecord;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitPenaltyRecordMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserLevelBenefitRecoveryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 等级权益恢复服务测试。
 * 验证处罚记录恢复逻辑、前置条件校验和权益启用状态查询。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserLevelBenefitRecoveryServiceTest {

    @Mock
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    @InjectMocks
    private ContentUserLevelBenefitRecoveryServiceImpl recoveryService;

    // ===== recoverByPenaltyRecord tests =====

    @Test
    void shouldReturnZeroWhenRecordIsNull() {
        // When
        int result = recoveryService.recoverByPenaltyRecord(null, "operator001", new Date(), "恢复原因");

        // Then
        assertThat(result).isEqualTo(0);
        verifyNoInteractions(levelBenefitPenaltyRecordMapper);
    }

    @Test
    void shouldReturnZeroWhenRecordIdBlank() {
        // Given
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord();
        record.setId("");

        // When
        int result = recoveryService.recoverByPenaltyRecord(record, "operator001", new Date(), "恢复原因");

        // Then
        assertThat(result).isEqualTo(0);
        verifyNoInteractions(levelBenefitPenaltyRecordMapper);
    }

    @Test
    void shouldReturnZeroWhenNoPendingRecords() {
        // Given
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord();
        record.setId("penalty001");
        when(levelBenefitPenaltyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        int result = recoveryService.recoverByPenaltyRecord(record, "operator001", new Date(), "恢复原因");

        // Then
        assertThat(result).isEqualTo(0);
        verify(levelBenefitPenaltyRecordMapper, never()).updateById(any(ContentUserLevelBenefitPenaltyRecord.class));
    }

    @Test
    void shouldRecoverMultipleRecords() {
        // Given
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord();
        record.setId("penalty001");

        ContentUserLevelBenefitPenaltyRecord item1 = new ContentUserLevelBenefitPenaltyRecord();
        item1.setId("benefit001");
        item1.setPreviousEnabled(true);

        ContentUserLevelBenefitPenaltyRecord item2 = new ContentUserLevelBenefitPenaltyRecord();
        item2.setId("benefit002");
        item2.setPreviousEnabled(false);

        when(levelBenefitPenaltyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(item1, item2));
        when(levelBenefitPenaltyRecordMapper.updateById(any(ContentUserLevelBenefitPenaltyRecord.class))).thenReturn(1);

        Date executeTime = new Date();

        // When
        int result = recoveryService.recoverByPenaltyRecord(record, "operator001", executeTime, "恢复原因");

        // Then
        assertThat(result).isEqualTo(2);
        verify(levelBenefitPenaltyRecordMapper, times(2)).updateById(any(ContentUserLevelBenefitPenaltyRecord.class));
    }

    @Test
    void shouldRestorePreviousEnabled() {
        // Given
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord();
        record.setId("penalty001");

        ContentUserLevelBenefitPenaltyRecord item = new ContentUserLevelBenefitPenaltyRecord();
        item.setId("benefit001");
        item.setPreviousEnabled(true);

        when(levelBenefitPenaltyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(item));
        when(levelBenefitPenaltyRecordMapper.updateById(any(ContentUserLevelBenefitPenaltyRecord.class))).thenReturn(1);

        Date executeTime = new Date();

        // When
        recoveryService.recoverByPenaltyRecord(record, "operator001", executeTime, "恢复原因");

        // Then
        assertThat(item.getCurrentEnabled()).isTrue();
        assertThat(item.getRecoverStatus()).isEqualTo("RECOVERED");
        assertThat(item.getRecoverReason()).isEqualTo("恢复原因");
        assertThat(item.getRecoveredBy()).isEqualTo("operator001");
        assertThat(item.getRecoveredAt()).isEqualTo(executeTime);
    }

    @Test
    void shouldDefaultCurrentEnabledWhenPreviousNull() {
        // Given
        ContentUserGrowthPenaltyRecord record = new ContentUserGrowthPenaltyRecord();
        record.setId("penalty001");

        ContentUserLevelBenefitPenaltyRecord item = new ContentUserLevelBenefitPenaltyRecord();
        item.setId("benefit001");
        item.setPreviousEnabled(null);

        when(levelBenefitPenaltyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(item));
        when(levelBenefitPenaltyRecordMapper.updateById(any(ContentUserLevelBenefitPenaltyRecord.class))).thenReturn(1);

        // When
        recoveryService.recoverByPenaltyRecord(record, "operator001", new Date(), "原因");

        // Then - Boolean.TRUE.equals(null) is false
        assertThat(item.getCurrentEnabled()).isFalse();
    }

    // ===== hasEnabledBenefit tests =====

    @Test
    void shouldReturnFalseForBlankUserId() {
        // When
        boolean result = recoveryService.hasEnabledBenefit("", "BENEFIT001");

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(levelBenefitPenaltyRecordMapper);
    }

    @Test
    void shouldReturnTrueWhenBenefitExists() {
        // Given
        when(levelBenefitPenaltyRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When
        boolean result = recoveryService.hasEnabledBenefit("user001", "BENEFIT001");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoBenefit() {
        // Given
        when(levelBenefitPenaltyRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        boolean result = recoveryService.hasEnabledBenefit("user001", "BENEFIT001");

        // Then
        assertThat(result).isFalse();
    }
}
