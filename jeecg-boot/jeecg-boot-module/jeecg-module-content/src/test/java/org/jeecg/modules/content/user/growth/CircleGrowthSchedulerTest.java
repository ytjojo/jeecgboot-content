package org.jeecg.modules.content.user.growth;

import org.jeecg.modules.content.user.growth.entity.CircleLevel;
import org.jeecg.modules.content.user.growth.mapper.CircleLevelMapper;
import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.modules.content.user.growth.service.ILeaderboardService;
import org.jeecg.modules.content.user.growth.task.CircleGrowthScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircleGrowthSchedulerTest {

    @Mock
    private ICircleLevelService circleLevelService;

    @Mock
    private ILeaderboardService leaderboardService;

    @Mock
    private CircleLevelMapper levelMapper;

    @InjectMocks
    private CircleGrowthScheduler scheduler;

    @Test
    void shouldRecalculateAndUpdateLevelForAllCircles() {
        CircleLevel c1 = new CircleLevel().setCircleId("c1");
        CircleLevel c2 = new CircleLevel().setCircleId("c2");
        when(levelMapper.selectList(null)).thenReturn(List.of(c1, c2));

        scheduler.updateCircleLevels();

        verify(circleLevelService).recalculateAndUpdateLevel("c1");
        verify(circleLevelService).recalculateAndUpdateLevel("c2");
    }

    @Test
    void shouldSkipServiceCallWhenNoCirclesExist() {
        when(levelMapper.selectList(null)).thenReturn(List.of());

        scheduler.updateCircleLevels();

        verify(circleLevelService, never()).recalculateAndUpdateLevel(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldIsolateFailureWhenSingleCircleUpdateThrows() {
        CircleLevel c1 = new CircleLevel().setCircleId("c1");
        CircleLevel c2 = new CircleLevel().setCircleId("c2");
        when(levelMapper.selectList(null)).thenReturn(List.of(c1, c2));
        doThrow(new RuntimeException("db error"))
            .when(circleLevelService).recalculateAndUpdateLevel("c1");

        scheduler.updateCircleLevels();

        verify(circleLevelService).recalculateAndUpdateLevel("c1");
        verify(circleLevelService).recalculateAndUpdateLevel("c2");
    }

    @Test
    void shouldRefreshLeaderboardSnapshotForAllCircles() {
        CircleLevel c1 = new CircleLevel().setCircleId("c1");
        CircleLevel c2 = new CircleLevel().setCircleId("c2");
        when(levelMapper.selectList(null)).thenReturn(List.of(c1, c2));

        scheduler.refreshLeaderboards();

        verify(leaderboardService).refreshSnapshot("c1");
        verify(leaderboardService).refreshSnapshot("c2");
    }

    @Test
    void shouldSkipLeaderboardRefreshWhenNoCirclesExist() {
        when(levelMapper.selectList(null)).thenReturn(List.of());

        scheduler.refreshLeaderboards();

        verify(leaderboardService, never()).refreshSnapshot(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldIsolateFailureWhenSingleCircleLeaderboardRefreshThrows() {
        CircleLevel c1 = new CircleLevel().setCircleId("c1");
        CircleLevel c2 = new CircleLevel().setCircleId("c2");
        CircleLevel c3 = new CircleLevel().setCircleId("c3");
        when(levelMapper.selectList(null)).thenReturn(List.of(c1, c2, c3));
        doThrow(new RuntimeException("redis timeout"))
            .when(leaderboardService).refreshSnapshot("c2");

        scheduler.refreshLeaderboards();

        verify(leaderboardService).refreshSnapshot("c1");
        verify(leaderboardService).refreshSnapshot("c2");
        verify(leaderboardService).refreshSnapshot("c3");
        verify(leaderboardService, times(3)).refreshSnapshot(org.mockito.ArgumentMatchers.anyString());
    }
}
