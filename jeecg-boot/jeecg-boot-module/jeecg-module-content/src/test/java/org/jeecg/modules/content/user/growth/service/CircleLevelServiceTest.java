package org.jeecg.modules.content.user.growth.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.modules.content.circle.growth.entity.CircleLevel;
import org.jeecg.modules.content.circle.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.circle.growth.mapper.CircleLevelMapper;
import org.jeecg.modules.content.circle.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.circle.growth.service.impl.CircleLevelServiceImpl;
import org.jeecg.modules.content.circle.growth.vo.CircleBenefitVO;
import org.jeecg.modules.content.circle.growth.vo.CircleLevelVO;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircleLevelServiceTest {

    @Mock
    private CircleLevelMapper levelMapper;
    @Mock
    private CircleMemberGrowthMapper growthMapper;
    @Mock
    private IContentNotificationService notificationService;
    @Mock
    private ISysBaseAPI sysBaseAPI;
    @InjectMocks
    private CircleLevelServiceImpl service;

    @BeforeAll
    static void initLambdaCache() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleLevel.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleMemberGrowth.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", levelMapper);
        ReflectionTestUtils.setField(service, "sysBaseAPI", sysBaseAPI);
    }

    @Test
    @DisplayName("成长分达到L2门槛时等级提升为L2")
    void updateLevel_score100_promotesToL2() {
        CircleLevel level = new CircleLevel()
                .setCircleId("circle1").setLevel(1).setGrowthScore(100)
                .setMemberScore(40).setContentScore(30).setActivityScore(30);
        doReturn(level).when(levelMapper).selectOne(any(), anyBoolean());

        service.updateLevel("circle1");

        assertThat(level.getLevel()).isEqualTo(2);
        verify(levelMapper).updateById((CircleLevel) level);
    }

    @Test
    @DisplayName("成长分未达到下一等级门槛时不升级")
    void updateLevel_scoreBelowThreshold_noChange() {
        CircleLevel level = new CircleLevel()
                .setCircleId("circle1").setLevel(2).setGrowthScore(150)
                .setMemberScore(50).setContentScore(50).setActivityScore(50);
        doReturn(level).when(levelMapper).selectOne(any(), anyBoolean());

        service.updateLevel("circle1");

        assertThat(level.getLevel()).isEqualTo(2);
        verify(levelMapper, never()).updateById(any(CircleLevel.class));
    }

    @Test
    @DisplayName("计算成长分时聚合成员规模、内容贡献和活跃互动得分")
    void calculateGrowthScore_aggregatesScores() {
        doReturn(null).when(levelMapper).selectOne(any(), anyBoolean());
        CircleMemberGrowth m1 = new CircleMemberGrowth()
                .setCircleId("c1").setUserId("u1")
                .setPostCount(5).setCommentCount(10).setFeaturedCount(1);
        when(growthMapper.selectList(any())).thenReturn(Collections.singletonList(m1));

        service.calculateGrowthScore("c1");

        verify(levelMapper).insertOrUpdate(any(CircleLevel.class));
    }

    @Test
    @DisplayName("获取等级信息时返回正确的进度百分比")
    void getLevelInfo_returnsCorrectProgress() {
        CircleLevel level = new CircleLevel()
                .setCircleId("c1").setLevel(2).setGrowthScore(200)
                .setMemberScore(80).setContentScore(60).setActivityScore(60);
        doReturn(level).when(levelMapper).selectOne(any(), anyBoolean());

        CircleLevelVO vo = service.getLevelInfo("c1");

        assertThat(vo.getLevel()).isEqualTo(2);
        assertThat(vo.getLevelName()).isEqualTo("活跃圈");
        assertThat(vo.getGrowthScore()).isEqualTo(200);
        assertThat(vo.getNextLevelThreshold()).isEqualTo(300);
        assertThat(vo.getProgressPercent()).isBetween(0, 100);
    }

    @Test
    @DisplayName("L3等级返回全量5个权益，前3个已解锁后2个未解锁")
    void getLevelInfo_l3_returnsAllBenefitsWithUnlockStatus() {
        CircleLevel level = new CircleLevel()
                .setCircleId("c1").setLevel(3).setGrowthScore(450)
                .setMemberScore(150).setContentScore(180).setActivityScore(120);
        doReturn(level).when(levelMapper).selectOne(any(), anyBoolean());

        CircleLevelVO vo = service.getLevelInfo("c1");

        assertThat(vo.getBenefits()).hasSize(5);
        assertThat(vo.getBenefits().get(0).getName()).isEqualTo("基础展示");
        assertThat(vo.getBenefits().get(0).getUnlocked()).isTrue();
        assertThat(vo.getBenefits().get(1).getName()).isEqualTo("排行榜入口");
        assertThat(vo.getBenefits().get(1).getUnlocked()).isTrue();
        assertThat(vo.getBenefits().get(2).getName()).isEqualTo("徽章墙");
        assertThat(vo.getBenefits().get(2).getUnlocked()).isTrue();
        assertThat(vo.getBenefits().get(3).getName()).isEqualTo("推荐权重提升");
        assertThat(vo.getBenefits().get(3).getUnlocked()).isFalse();
        assertThat(vo.getBenefits().get(4).getName()).isEqualTo("全部权益");
        assertThat(vo.getBenefits().get(4).getUnlocked()).isFalse();
    }

    @Test
    @DisplayName("L5最高等级所有5个权益都已解锁")
    void getLevelInfo_l5_allBenefitsUnlocked() {
        CircleLevel level = new CircleLevel()
                .setCircleId("c1").setLevel(5).setGrowthScore(1000)
                .setMemberScore(400).setContentScore(300).setActivityScore(300);
        doReturn(level).when(levelMapper).selectOne(any(), anyBoolean());

        CircleLevelVO vo = service.getLevelInfo("c1");

        assertThat(vo.getBenefits()).hasSize(5);
        for (CircleBenefitVO benefit : vo.getBenefits()) {
            assertThat(benefit.getUnlocked()).isTrue();
        }
    }
}
