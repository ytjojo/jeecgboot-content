package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentInviteCode;
import org.jeecg.modules.content.user.entity.ContentInviteRecord;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.mapper.ContentInviteCodeMapper;
import org.jeecg.modules.content.user.mapper.ContentInviteRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentInviteServiceImpl;
import org.jeecg.modules.content.user.vo.ContentInviteCodeVO;
import org.jeecg.modules.content.user.vo.ContentInviteStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentInviteServiceTest {

    @Mock
    private ContentInviteCodeMapper inviteCodeMapper;

    @Mock
    private ContentInviteRecordMapper inviteRecordMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private IContentUserRewardRuleService rewardRuleService;

    @InjectMocks
    private ContentInviteServiceImpl inviteService;

    @Test
    void shouldGenerateNewInviteCodeWhenNoneExists() {
        when(inviteCodeMapper.selectOne(any())).thenReturn(null);
        when(inviteCodeMapper.selectCount(any())).thenReturn(0L);
        when(inviteCodeMapper.insert(any(ContentInviteCode.class))).thenReturn(1);

        ContentInviteCodeVO result = inviteService.generateOrGetInviteCode("u_001");

        assertThat(result.getInviteCode()).isNotNull().hasSize(8);
        assertThat(result.getInviteUrl()).contains(result.getInviteCode());
        verify(inviteCodeMapper).insert(any(ContentInviteCode.class));
    }

    @Test
    void shouldReturnExistingInviteCode() {
        ContentInviteCode existing = new ContentInviteCode()
            .setUserId("u_001")
            .setInviteCode("ABCD1234");
        when(inviteCodeMapper.selectOne(any())).thenReturn(existing);

        ContentInviteCodeVO result = inviteService.generateOrGetInviteCode("u_001");

        assertThat(result.getInviteCode()).isEqualTo("ABCD1234");
        verify(inviteCodeMapper, never()).insert(any(ContentInviteCode.class));
    }

    @Test
    void shouldRejectEmptyUserId() {
        assertThatThrownBy(() -> inviteService.generateOrGetInviteCode(""))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户ID不能为空");
    }

    @Test
    void shouldRejectSelfInvite() {
        ContentInviteCode code = new ContentInviteCode()
            .setUserId("u_001")
            .setInviteCode("ABCD1234");
        when(inviteCodeMapper.selectOne(any())).thenReturn(code);

        assertThatThrownBy(() -> inviteService.bindInviteRelation("ABCD1234", "u_001"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("不能邀请自己");
    }

    @Test
    void shouldRejectDuplicateInvite() {
        ContentInviteCode code = new ContentInviteCode()
            .setUserId("u_001")
            .setInviteCode("ABCD1234");
        when(inviteCodeMapper.selectOne(any())).thenReturn(code);

        ContentInviteRecord existingRecord = new ContentInviteRecord()
            .setInviterUserId("u_001")
            .setInviteeUserId("u_002");
        when(inviteRecordMapper.selectOne(any())).thenReturn(existingRecord);

        assertThatThrownBy(() -> inviteService.bindInviteRelation("ABCD1234", "u_002"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("该用户已被邀请");
    }

    @Test
    void shouldRejectInvalidInviteCode() {
        when(inviteCodeMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> inviteService.bindInviteRelation("INVALID1", "u_002"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("邀请码无效");
    }

    @Test
    void shouldGrantRewardPointsOnInvite() {
        ContentInviteCode code = new ContentInviteCode()
            .setUserId("u_001")
            .setInviteCode("ABCD1234");
        when(inviteCodeMapper.selectOne(any())).thenReturn(code);
        when(inviteRecordMapper.selectOne(any())).thenReturn(null);

        ContentUserRewardRule rule = new ContentUserRewardRule()
            .setRuleCode("INVITE_REGISTER")
            .setSourceType("INVITE_REGISTER")
            .setPointAmount(50)
            .setDailyPointCap(500)
            .setEnabled(true);
        when(rewardRuleService.getEnabledRule("INVITE_REGISTER")).thenReturn(Optional.of(rule));

        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u_001")
            .setPointBalance(100);
        when(profileMapper.selectByUserId("u_001")).thenReturn(profile);
        when(profileMapper.updateById(any(ContentUserProfile.class))).thenReturn(1);
        when(inviteRecordMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());
        when(inviteRecordMapper.insert(any(ContentInviteRecord.class))).thenReturn(1);

        inviteService.bindInviteRelation("ABCD1234", "u_002");

        ArgumentCaptor<ContentInviteRecord> recordCaptor = ArgumentCaptor.forClass(ContentInviteRecord.class);
        verify(inviteRecordMapper).insert(recordCaptor.capture());
        ContentInviteRecord record = recordCaptor.getValue();
        assertThat(record.getInviterUserId()).isEqualTo("u_001");
        assertThat(record.getInviteeUserId()).isEqualTo("u_002");
        assertThat(record.getRewardPoint()).isEqualTo(50);
        assertThat(record.getRewardStatus()).isEqualTo("GRANTED");

        ArgumentCaptor<ContentUserProfile> profileCaptor = ArgumentCaptor.forClass(ContentUserProfile.class);
        verify(profileMapper).updateById(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getPointBalance()).isEqualTo(150);
    }

    @Test
    void shouldRespectDailyPointCap() {
        ContentInviteCode code = new ContentInviteCode()
            .setUserId("u_001")
            .setInviteCode("ABCD1234");
        when(inviteCodeMapper.selectOne(any())).thenReturn(code);
        when(inviteRecordMapper.selectOne(any())).thenReturn(null);

        ContentUserRewardRule rule = new ContentUserRewardRule()
            .setRuleCode("INVITE_REGISTER")
            .setSourceType("INVITE_REGISTER")
            .setPointAmount(50)
            .setDailyPointCap(100)
            .setEnabled(true);
        when(rewardRuleService.getEnabledRule("INVITE_REGISTER")).thenReturn(Optional.of(rule));

        ContentInviteRecord todayRecord = new ContentInviteRecord()
            .setRewardPoint(80)
            .setRewardStatus("GRANTED");
        when(inviteRecordMapper.selectList(any())).thenReturn(java.util.List.of(todayRecord));

        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u_001")
            .setPointBalance(200);
        when(profileMapper.selectByUserId("u_001")).thenReturn(profile);
        when(profileMapper.updateById(any(ContentUserProfile.class))).thenReturn(1);
        when(inviteRecordMapper.insert(any(ContentInviteRecord.class))).thenReturn(1);

        inviteService.bindInviteRelation("ABCD1234", "u_003");

        ArgumentCaptor<ContentInviteRecord> recordCaptor = ArgumentCaptor.forClass(ContentInviteRecord.class);
        verify(inviteRecordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getRewardPoint()).isEqualTo(20);
        assertThat(recordCaptor.getValue().getRewardStatus()).isEqualTo("GRANTED");
    }

    @Test
    void shouldSkipRewardWhenNoRuleConfigured() {
        ContentInviteCode code = new ContentInviteCode()
            .setUserId("u_001")
            .setInviteCode("ABCD1234");
        when(inviteCodeMapper.selectOne(any())).thenReturn(code);
        when(inviteRecordMapper.selectOne(any())).thenReturn(null);
        when(rewardRuleService.getEnabledRule("INVITE_REGISTER")).thenReturn(Optional.empty());
        when(inviteRecordMapper.insert(any(ContentInviteRecord.class))).thenReturn(1);

        inviteService.bindInviteRelation("ABCD1234", "u_002");

        ArgumentCaptor<ContentInviteRecord> recordCaptor = ArgumentCaptor.forClass(ContentInviteRecord.class);
        verify(inviteRecordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getRewardPoint()).isEqualTo(0);
        assertThat(recordCaptor.getValue().getRewardStatus()).isEqualTo("PENDING");
        verify(profileMapper, never()).updateById(any(ContentUserProfile.class));
    }

    @Test
    void shouldReturnStatsWithZeroPointsWhenNoInvites() {
        when(inviteRecordMapper.selectCount(any())).thenReturn(0L);
        when(inviteRecordMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        ContentInviteStatsVO stats = inviteService.getInviteStats("u_001");

        assertThat(stats.getTotalInvites()).isEqualTo(0);
        assertThat(stats.getSuccessfulRegistrations()).isEqualTo(0);
        assertThat(stats.getTotalPointsEarned()).isEqualTo(0);
    }
}
