package org.jeecg.modules.content.user.growth.controller;

import org.jeecg.modules.content.user.growth.service.IAchievementService;
import org.jeecg.modules.content.user.growth.vo.AchievementVO;
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
class AchievementControllerTest {

    @Mock
    private IAchievementService achievementService;

    @InjectMocks
    private AchievementController controller;

    @Test
    @DisplayName("获取徽章列表返回200和徽章数据")
    void getAchievements_returnsOkWithBadges() {
        AchievementVO badge = new AchievementVO();
        badge.setAchievementType("CONTINUOUS_CREATOR");
        badge.setName("持续创作者");
        badge.setEarned(true);
        when(achievementService.getMemberAchievements("c1", "u1"))
                .thenReturn(List.of(badge));

        var result = controller.getAchievements("c1", "u1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).hasSize(1);
        assertThat(result.getResult().get(0).getAchievementType()).isEqualTo("CONTINUOUS_CREATOR");
        assertThat(result.getResult().get(0).getEarned()).isTrue();
        verify(achievementService).getMemberAchievements("c1", "u1");
    }
}
