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
    @DisplayName("获取排行榜返回200和完整条目字段")
    void getLeaderboard_returnsOkWithEntries() {
        LeaderboardEntryVO entry = new LeaderboardEntryVO();
        entry.setUserId("u1");
        entry.setScore(100);
        entry.setRankNum(1);
        entry.setHighlighted(true);
        entry.setGap(0);
        entry.setUsername("测试用户");
        entry.setAvatar("/avatar/u1.png");
        when(leaderboardService.getLeaderboard("c1", "EXP", "WEEK", "u1"))
                .thenReturn(List.of(entry));

        var result = controller.getLeaderboard("c1", "EXP", "WEEK", "u1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).hasSize(1);
        var res = result.getResult().get(0);
        assertThat(res.getUserId()).isEqualTo("u1");
        assertThat(res.getScore()).isEqualTo(100);
        assertThat(res.getRankNum()).isEqualTo(1);
        assertThat(res.getHighlighted()).isTrue();
        assertThat(res.getGap()).isEqualTo(0);
        assertThat(res.getUsername()).isEqualTo("测试用户");
        assertThat(res.getAvatar()).isEqualTo("/avatar/u1.png");
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
