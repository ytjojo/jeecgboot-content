package org.jeecg.modules.content.user.growth.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.modules.content.user.growth.entity.CircleMemberAchievement;
import org.jeecg.modules.content.user.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.user.growth.enums.AchievementTypeEnum;
import org.jeecg.modules.content.user.growth.mapper.CircleAchievementMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberAchievementMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.user.growth.service.impl.AchievementServiceImpl;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementServiceTest {

    @Mock
    private CircleAchievementMapper achievementMapper;
    @Mock
    private CircleMemberAchievementMapper memberAchievementMapper;
    @Mock
    private CircleMemberGrowthMapper growthMapper;
    @Mock
    private IMemberGrowthService memberGrowthService;
    @Mock
    private IContentNotificationService notificationService;
    @InjectMocks
    private AchievementServiceImpl service;

    @BeforeAll
    static void initLambdaCache() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleMemberAchievement.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleMemberGrowth.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", memberAchievementMapper);
    }

    @Test
    @DisplayName("累计发布10篇可见内容后获得持续创作者徽章")
    void checkAndAward_continuousCreator_awardsBadge() {
        CircleMemberGrowth growth = new CircleMemberGrowth()
                .setCircleId("c1").setUserId("u1").setPostCount(10);
        when(growthMapper.selectOne(any())).thenReturn(growth);
        when(memberAchievementMapper.selectCount(any())).thenReturn(0L);
        when(memberGrowthService.getParticipationDays("c1", "u1")).thenReturn(0);

        service.checkAndAward("c1", "u1");

        ArgumentCaptor<CircleMemberAchievement> captor = ArgumentCaptor.forClass(CircleMemberAchievement.class);
        verify(memberAchievementMapper, atLeastOnce()).insert((CircleMemberAchievement) captor.capture());
        assertThat(captor.getAllValues().stream()
                .map(CircleMemberAchievement::getAchievementType)
                .anyMatch(t -> t.equals(AchievementTypeEnum.CONTINUOUS_CREATOR.getCode()))).isTrue();
    }

    @Test
    @DisplayName("已获得的徽章不会重复发放")
    void checkAndAward_alreadyAwarded_noDuplicate() {
        CircleMemberGrowth growth = new CircleMemberGrowth()
                .setCircleId("c1").setUserId("u1").setPostCount(10);
        when(growthMapper.selectOne(any())).thenReturn(growth);
        when(memberAchievementMapper.selectCount(any())).thenReturn(1L);
        when(memberGrowthService.getParticipationDays("c1", "u1")).thenReturn(0);

        service.checkAndAward("c1", "u1");

        verify(memberAchievementMapper, never()).insert(any(CircleMemberAchievement.class));
    }

    @Test
    @DisplayName("内容违规后撤销对应徽章")
    void revoke_success() {
        CircleMemberAchievement achievement = new CircleMemberAchievement()
                .setCircleId("c1").setUserId("u1")
                .setAchievementType(AchievementTypeEnum.CONTINUOUS_CREATOR.getCode())
                .setRevoked(false);
        doReturn(achievement).when(memberAchievementMapper).selectOne(any(), anyBoolean());

        service.revoke("c1", "u1", AchievementTypeEnum.CONTINUOUS_CREATOR);

        assertThat(achievement.getRevoked()).isTrue();
        verify(memberAchievementMapper).updateById((CircleMemberAchievement) achievement);
    }
}
