package org.jeecg.modules.content.circle.growth.controller;

import org.jeecg.modules.content.circle.growth.biz.ICircleGrowthBiz;
import org.jeecg.modules.content.circle.growth.vo.MemberGrowthVO;
import org.jeecg.modules.content.circle.growth.vo.ParticipationVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberGrowthControllerTest {

    @Mock
    private ICircleGrowthBiz circleGrowthBiz;

    @InjectMocks
    private MemberGrowthController controller;

    @Test
    @DisplayName("获取成长信息返回200和完整字段数据")
    void getMyGrowthInfo_returnsOkWithData() {
        MemberGrowthVO vo = new MemberGrowthVO();
        vo.setCircleId("c1");
        vo.setExpPoints(50);
        vo.setContributionPoints(30);
        vo.setLevel(2);
        vo.setPostCount(5);
        vo.setParticipationDays(3);
        vo.setRank(5);
        vo.setNextLevelThreshold(300);
        vo.setProgressPercent(25);
        vo.setTodayExp(15);
        vo.setDailyExpLimit(100);
        when(circleGrowthBiz.getMyGrowthInfo("c1", "u1")).thenReturn(vo);

        var result = controller.getMyGrowthInfo("c1");

        assertThat(result.getCode()).isEqualTo(200);
        var res = result.getResult();
        assertThat(res.getCircleId()).isEqualTo("c1");
        assertThat(res.getExpPoints()).isEqualTo(50);
        assertThat(res.getContributionPoints()).isEqualTo(30);
        assertThat(res.getLevel()).isEqualTo(2);
        assertThat(res.getPostCount()).isEqualTo(5);
        assertThat(res.getParticipationDays()).isEqualTo(3);
        assertThat(res.getRank()).isEqualTo(5);
        assertThat(res.getNextLevelThreshold()).isEqualTo(300);
        assertThat(res.getProgressPercent()).isEqualTo(25);
        assertThat(res.getTodayExp()).isEqualTo(15);
        assertThat(res.getDailyExpLimit()).isEqualTo(100);
        verify(circleGrowthBiz).getMyGrowthInfo("c1", null);
    }

    @Test
    @DisplayName("获取连续参与进度返回200和完整数据")
    void getMyParticipationProgress_returnsOkWithData() {
        ParticipationVO vo = new ParticipationVO();
        vo.setDays(5);
        vo.setDailyStatus(Arrays.asList(
                new ParticipationVO.DayStatus("2026-06-26", true),
                new ParticipationVO.DayStatus("2026-06-25", true),
                new ParticipationVO.DayStatus("2026-06-24", false)
        ));
        when(circleGrowthBiz.getMyParticipationProgress("c1", "u1")).thenReturn(vo);

        var result = controller.getMyParticipationProgress("c1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult().getDays()).isEqualTo(5);
        assertThat(result.getResult().getDailyStatus()).hasSize(3);
        assertThat(result.getResult().getDailyStatus().get(0).getParticipated()).isTrue();
        assertThat(result.getResult().getDailyStatus().get(2).getParticipated()).isFalse();
        verify(circleGrowthBiz).getMyParticipationProgress("c1", null);
    }
}
