package org.jeecg.modules.content.user.growth.controller;

import org.jeecg.modules.content.circle.growth.controller.AchievementController;
import org.jeecg.modules.content.circle.growth.service.IAchievementService;
import org.jeecg.modules.content.circle.growth.vo.AchievementVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
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
    @DisplayName("获取徽章列表返回200和完整字段数据")
    void getAchievements_returnsOkWithBadges() {
        AchievementVO badge = new AchievementVO();
        badge.setAchievementType("CONTINUOUS_CREATOR");
        badge.setName("持续创作者");
        badge.setDescription("累计发布10篇内容");
        badge.setIconUrl("/icons/creator.png");
        badge.setEarned(true);
        badge.setEarnedDate(new Date());
        badge.setConditionDesc("发布10篇内容");
        badge.setCurrentProgress(10);
        badge.setTargetProgress(10);
        badge.setStatus("EARNED");
        when(achievementService.getMemberAchievements("c1", "u1"))
                .thenReturn(List.of(badge));

        var result = controller.getAchievements("c1", "u1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).hasSize(1);
        var res = result.getResult().get(0);
        assertThat(res.getAchievementType()).isEqualTo("CONTINUOUS_CREATOR");
        assertThat(res.getName()).isEqualTo("持续创作者");
        assertThat(res.getDescription()).isEqualTo("累计发布10篇内容");
        assertThat(res.getIconUrl()).isEqualTo("/icons/creator.png");
        assertThat(res.getEarned()).isTrue();
        assertThat(res.getEarnedDate()).isNotNull();
        assertThat(res.getConditionDesc()).isEqualTo("发布10篇内容");
        assertThat(res.getCurrentProgress()).isEqualTo(10);
        assertThat(res.getTargetProgress()).isEqualTo(10);
        assertThat(res.getStatus()).isEqualTo("EARNED");
        verify(achievementService).getMemberAchievements("c1", "u1");
    }
}
