package org.jeecg.modules.content.user.growth.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.modules.content.user.growth.entity.CircleGrowthLog;
import org.jeecg.modules.content.user.growth.entity.CircleLeaderboardSnapshot;
import org.jeecg.modules.content.user.growth.mapper.CircleGrowthLogMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleLeaderboardSnapshotMapper;
import org.jeecg.modules.content.user.growth.service.impl.LeaderboardServiceImpl;
import org.jeecg.modules.content.user.growth.vo.LeaderboardEntryVO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private CircleLeaderboardSnapshotMapper snapshotMapper;
    @Mock
    private CircleGrowthLogMapper growthLogMapper;
    @InjectMocks
    private LeaderboardServiceImpl service;

    @BeforeAll
    static void initLambdaCache() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleLeaderboardSnapshot.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CircleGrowthLog.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", snapshotMapper);
    }

    @Test
    @DisplayName("排行榜返回Top50成员并高亮当前用户")
    void getLeaderboard_top50_highlightsCurrentUser() {
        List<CircleLeaderboardSnapshot> snapshots = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            CircleLeaderboardSnapshot s = new CircleLeaderboardSnapshot()
                    .setCircleId("c1").setUserId("user" + i)
                    .setDimension("EXP").setPeriod("WEEK")
                    .setScore(100 - i).setRankNum(i);
            snapshots.add(s);
        }
        when(snapshotMapper.selectList(any())).thenReturn(snapshots);

        List<LeaderboardEntryVO> result = service.getLeaderboard("c1", "EXP", "WEEK", "user5");

        assertThat(result).hasSize(50);
        assertThat(result.get(4).getHighlighted()).isTrue();
        assertThat(result.get(4).getUserId()).isEqualTo("user5");
    }

    @Test
    @DisplayName("当前用户未进入Top50时返回空高亮")
    void getLeaderboard_userNotInTop50_noHighlight() {
        List<CircleLeaderboardSnapshot> snapshots = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            CircleLeaderboardSnapshot s = new CircleLeaderboardSnapshot()
                    .setCircleId("c1").setUserId("user" + i)
                    .setDimension("EXP").setPeriod("WEEK")
                    .setScore(100 - i).setRankNum(i);
            snapshots.add(s);
        }
        when(snapshotMapper.selectList(any())).thenReturn(snapshots);

        List<LeaderboardEntryVO> result = service.getLeaderboard("c1", "EXP", "WEEK", "outsider");

        assertThat(result).allMatch(e -> !e.getHighlighted());
    }

    @Test
    @DisplayName("空排行榜返回空列表")
    void getLeaderboard_empty_returnsEmptyList() {
        when(snapshotMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<LeaderboardEntryVO> result = service.getLeaderboard("c1", "EXP", "WEEK", "user1");

        assertThat(result).isEmpty();
    }
}
