package org.jeecg.modules.content.user.growth.controller;

import org.jeecg.modules.content.user.growth.service.ILeaderboardService;
import org.jeecg.modules.content.user.growth.vo.LeaderboardEntryVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private ILeaderboardService leaderboardService;

    @InjectMocks
    private LeaderboardController controller;

    @Test
    @DisplayName("获取排行榜返回200和条目列表")
    void getLeaderboard_returnsOkWithEntries() {
        LeaderboardEntryVO entry = new LeaderboardEntryVO();
        entry.setUserId("u1");
        entry.setScore(100);
        entry.setRankNum(1);
        entry.setHighlighted(true);
        when(leaderboardService.getLeaderboard("c1", "EXP", "WEEK", "u1"))
                .thenReturn(List.of(entry));

        var result = controller.getLeaderboard("c1", "EXP", "WEEK", "u1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).hasSize(1);
        assertThat(result.getResult().get(0).getUserId()).isEqualTo("u1");
        assertThat(result.getResult().get(0).getHighlighted()).isTrue();
        verify(leaderboardService).getLeaderboard("c1", "EXP", "WEEK", "u1");
    }

    @Test
    @DisplayName("排行榜默认周期为WEEK")
    void getLeaderboard_defaultPeriodIsWeek() {
        when(leaderboardService.getLeaderboard("c1", "EXP", "WEEK", "u1"))
                .thenReturn(List.of());

        var result = controller.getLeaderboard("c1", "EXP", "WEEK", "u1");

        assertThat(result.getCode()).isEqualTo(200);
        verify(leaderboardService).getLeaderboard("c1", "EXP", "WEEK", "u1");
    }
}
