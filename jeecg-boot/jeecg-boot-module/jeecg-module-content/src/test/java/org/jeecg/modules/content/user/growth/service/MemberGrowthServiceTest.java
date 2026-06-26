package org.jeecg.modules.content.user.growth.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.modules.content.circle.growth.entity.CircleGrowthLog;
import org.jeecg.modules.content.circle.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.circle.growth.enums.GrowthActionEnum;
import org.jeecg.modules.content.circle.growth.mapper.CircleGrowthLogMapper;
import org.jeecg.modules.content.circle.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.circle.growth.service.impl.MemberGrowthServiceImpl;
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

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberGrowthServiceTest {

    @Mock
    private CircleMemberGrowthMapper growthMapper;
    @Mock
    private CircleGrowthLogMapper growthLogMapper;
    @InjectMocks
    private MemberGrowthServiceImpl service;

    private static final String CIRCLE_ID = "circle1";
    private static final String USER_ID = "user1";

    @BeforeAll
    static void initLambdaCache() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleGrowthLog.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleMemberGrowth.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", growthMapper);
    }

    @Test
    @DisplayName("发帖成功后获得10点经验值和10点贡献值")
    void addExperience_post_success() {
        when(growthLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        doReturn(null).when(growthMapper).selectOne(any(), anyBoolean());

        service.addExperience(CIRCLE_ID, USER_ID, GrowthActionEnum.POST, "post1");

        ArgumentCaptor<CircleGrowthLog> logCaptor = ArgumentCaptor.forClass(CircleGrowthLog.class);
        verify(growthLogMapper).insert((CircleGrowthLog) logCaptor.capture());
        assertThat(logCaptor.getValue().getExpPoints()).isEqualTo(10);
        assertThat(logCaptor.getValue().getContributionPoints()).isEqualTo(10);
        assertThat(logCaptor.getValue().getRevoked()).isFalse();

        ArgumentCaptor<CircleMemberGrowth> growthCaptor = ArgumentCaptor.forClass(CircleMemberGrowth.class);
        verify(growthMapper).insertOrUpdate((CircleMemberGrowth) growthCaptor.capture());
        assertThat(growthCaptor.getValue().getExpPoints()).isEqualTo(10);
        assertThat(growthCaptor.getValue().getContributionPoints()).isEqualTo(10);
        assertThat(growthCaptor.getValue().getPostCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("每日经验值达到100点上限后不再增加")
    void addExperience_dailyCapReached_noExpAdded() {
        CircleGrowthLog maxLog = new CircleGrowthLog()
                .setCircleId(CIRCLE_ID).setUserId(USER_ID)
                .setActionType("POST").setExpPoints(100)
                .setBizDate(LocalDate.now()).setRevoked(false);
        when(growthLogMapper.selectList(any())).thenReturn(java.util.List.of(maxLog));

        service.addExperience(CIRCLE_ID, USER_ID, GrowthActionEnum.POST, "post2");

        verify(growthLogMapper, never()).insert(any(CircleGrowthLog.class));
    }

    @Test
    @DisplayName("内容删除后回退对应经验值和贡献值")
    void revokeExperience_success() {
        CircleGrowthLog existingLog = new CircleGrowthLog()
                .setCircleId(CIRCLE_ID).setUserId(USER_ID)
                .setActionType(GrowthActionEnum.POST.getCode())
                .setExpPoints(10).setContributionPoints(10)
                .setBizId("post1").setBizDate(LocalDate.now()).setRevoked(false);
        when(growthLogMapper.selectOne(any())).thenReturn(existingLog);

        CircleMemberGrowth growth = new CircleMemberGrowth()
                .setCircleId(CIRCLE_ID).setUserId(USER_ID)
                .setExpPoints(20).setContributionPoints(20).setLevel(1)
                .setPostCount(2).setCommentCount(0).setFeaturedCount(0);
        doReturn(growth).when(growthMapper).selectOne(any(), anyBoolean());

        service.revokeExperience(CIRCLE_ID, USER_ID, GrowthActionEnum.POST, "post1");

        assertThat(growth.getExpPoints()).isEqualTo(10);
        assertThat(growth.getContributionPoints()).isEqualTo(10);
        assertThat(existingLog.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("近7天无参与行为时返回0天")
    void getParticipationDays_noActivity_returnsZero() {
        when(growthLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        int days = service.getParticipationDays(CIRCLE_ID, USER_ID);

        assertThat(days).isEqualTo(0);
    }

    @Test
    @DisplayName("近7天有参与行为时返回正确天数")
    void getParticipationDays_hasActivity_returnsDays() {
        CircleGrowthLog log1 = new CircleGrowthLog().setBizDate(LocalDate.now());
        CircleGrowthLog log2 = new CircleGrowthLog().setBizDate(LocalDate.now().minusDays(1));
        when(growthLogMapper.selectList(any())).thenReturn(java.util.List.of(log1, log2));

        int days = service.getParticipationDays(CIRCLE_ID, USER_ID);

        assertThat(days).isEqualTo(2);
    }

    @Test
    @DisplayName("获取成长信息返回完整的VO数据")
    void getGrowthInfo_returnsCompleteVO() {
        CircleMemberGrowth growth = new CircleMemberGrowth()
                .setCircleId(CIRCLE_ID).setUserId(USER_ID)
                .setExpPoints(150).setContributionPoints(80)
                .setLevel(2).setPostCount(5).setCommentCount(3).setFeaturedCount(1);
        doReturn(growth).when(growthMapper).selectOne(any(), anyBoolean());

        // baseMapper.selectCount for rank: return 0 (rank = 0 + 1 = 1)
        when(growthMapper.selectCount(any())).thenReturn(0L);

        // todayExp: one log with 15 exp today
        CircleGrowthLog todayLog = new CircleGrowthLog()
                .setCircleId(CIRCLE_ID).setUserId(USER_ID)
                .setExpPoints(15).setRevoked(false).setBizDate(LocalDate.now());
        when(growthLogMapper.selectList(any()))
                .thenReturn(java.util.List.of(            // first call: getParticipationDays
                        new CircleGrowthLog().setBizDate(LocalDate.now()),
                        new CircleGrowthLog().setBizDate(LocalDate.now().minusDays(1))))
                .thenReturn(java.util.List.of(todayLog));  // second call: todayExp

        var vo = service.getGrowthInfo(CIRCLE_ID, USER_ID);

        assertThat(vo.getCircleId()).isEqualTo(CIRCLE_ID);
        assertThat(vo.getExpPoints()).isEqualTo(150);
        assertThat(vo.getContributionPoints()).isEqualTo(80);
        assertThat(vo.getLevel()).isEqualTo(2);
        assertThat(vo.getPostCount()).isEqualTo(5);
        assertThat(vo.getRank()).isEqualTo(1);
        // L2: threshold = MEMBER_LEVEL_THRESHOLDS[2] = 300, current threshold = 100
        // progress% = (150 - 100) * 100 / (300 - 100) = 25
        assertThat(vo.getNextLevelThreshold()).isEqualTo(300);
        assertThat(vo.getProgressPercent()).isEqualTo(25);
        assertThat(vo.getParticipationDays()).isEqualTo(2);
        assertThat(vo.getTodayExp()).isEqualTo(15);
        assertThat(vo.getDailyExpLimit()).isEqualTo(100);
    }
}
