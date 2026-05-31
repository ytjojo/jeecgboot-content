package org.jeecg.modules.content.user.growth.controller;

import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.modules.content.user.growth.vo.CircleLevelVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("获取圈子等级信息返回200和正确数据")
    void getLevelInfo_returnsOkWithData() {
        CircleLevelVO vo = new CircleLevelVO();
        vo.setLevel(3);
        vo.setLevelName("优质圈");
        vo.setGrowthScore(450);
        vo.setNextLevelThreshold(600);
        vo.setProgressPercent(50);
        when(circleLevelService.getLevelInfo("c1")).thenReturn(vo);

        var result = controller.getLevelInfo("c1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult().getLevel()).isEqualTo(3);
        assertThat(result.getResult().getLevelName()).isEqualTo("优质圈");
        verify(circleLevelService).getLevelInfo("c1");
    }
}
