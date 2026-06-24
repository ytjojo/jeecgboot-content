package org.jeecg.modules.content.user.growth.controller;

import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.modules.content.user.growth.vo.CircleBenefitVO;
import org.jeecg.modules.content.user.growth.vo.CircleLevelVO;
import org.jeecg.modules.content.user.growth.vo.LevelConditionVO;
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
class CircleLevelControllerTest {

    @Mock
    private ICircleLevelService circleLevelService;

    @InjectMocks
    private CircleLevelController controller;

    @Test
    @DisplayName("获取圈子等级信息返回200和完整字段数据")
    void getLevelInfo_returnsOkWithData() {
        CircleLevelVO vo = new CircleLevelVO();
        vo.setLevel(3);
        vo.setLevelName("优质圈");
        vo.setGrowthScore(450);
        vo.setNextLevelThreshold(600);
        vo.setProgressPercent(50);
        vo.setBenefits(List.of(
                new CircleBenefitVO().setName("基础展示").setUnlocked(true),
                new CircleBenefitVO().setName("排行榜入口").setUnlocked(true),
                new CircleBenefitVO().setName("徽章墙").setUnlocked(true),
                new CircleBenefitVO().setName("推荐权重提升").setUnlocked(false),
                new CircleBenefitVO().setName("全部权益").setUnlocked(false)
        ));
        vo.setMemberScore(150);
        vo.setContentScore(180);
        vo.setActivityScore(120);
        LevelConditionVO cond = new LevelConditionVO();
        cond.setType("MEMBER");
        cond.setLabel("成员规模");
        cond.setCurrent(150);
        cond.setRequired(400);
        cond.setGap(250);
        vo.setNextLevelConditions(List.of(cond));
        when(circleLevelService.getLevelInfo("c1")).thenReturn(vo);

        var result = controller.getLevelInfo("c1");

        assertThat(result.getCode()).isEqualTo(200);
        var res = result.getResult();
        assertThat(res.getLevel()).isEqualTo(3);
        assertThat(res.getLevelName()).isEqualTo("优质圈");
        assertThat(res.getGrowthScore()).isEqualTo(450);
        assertThat(res.getNextLevelThreshold()).isEqualTo(600);
        assertThat(res.getProgressPercent()).isEqualTo(50);
        assertThat(res.getBenefits()).hasSize(5);
        assertThat(res.getBenefits().get(0).getName()).isEqualTo("基础展示");
        assertThat(res.getBenefits().get(0).getUnlocked()).isTrue();
        assertThat(res.getBenefits().get(2).getName()).isEqualTo("徽章墙");
        assertThat(res.getBenefits().get(2).getUnlocked()).isTrue();
        assertThat(res.getBenefits().get(3).getName()).isEqualTo("推荐权重提升");
        assertThat(res.getBenefits().get(3).getUnlocked()).isFalse();
        assertThat(res.getMemberScore()).isEqualTo(150);
        assertThat(res.getContentScore()).isEqualTo(180);
        assertThat(res.getActivityScore()).isEqualTo(120);
        assertThat(res.getNextLevelConditions()).hasSize(1);
        assertThat(res.getNextLevelConditions().get(0).getGap()).isEqualTo(250);
        verify(circleLevelService).getLevelInfo("c1");
    }
}
