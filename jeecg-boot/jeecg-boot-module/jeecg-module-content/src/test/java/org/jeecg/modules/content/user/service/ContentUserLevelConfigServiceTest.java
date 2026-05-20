package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.mapper.ContentUserLevelConfigMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserLevelConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 内容社区用户等级配置服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserLevelConfigServiceTest {

    @Mock
    private ContentUserLevelConfigMapper levelConfigMapper;

    private ContentUserLevelConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentUserLevelConfigServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", levelConfigMapper);
    }

    @Test
    void shouldCalculateLevelByConfiguredThresholds() {
        when(levelConfigMapper.selectList(any())).thenReturn(List.of(
            level(1, 0),
            level(2, 50),
            level(3, 200)
        ));

        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(80)).isEqualTo(2);
        assertThat(service.calculateLevel(250)).isEqualTo(3);
    }

    @Test
    void shouldFallbackToDefaultFormulaWhenNoLevelConfigExists() {
        when(levelConfigMapper.selectList(any())).thenReturn(List.of());

        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(230)).isEqualTo(3);
    }

    @Test
    void shouldRejectDuplicateLevelNegativeThresholdAndNonIncreasingThreshold() {
        when(levelConfigMapper.selectList(any())).thenReturn(List.of(level(1, 0), level(1, 100)));
        assertThatThrownBy(() -> service.listValidEnabledLevels()).isInstanceOf(JeecgBootException.class);

        when(levelConfigMapper.selectList(any())).thenReturn(List.of(level(1, -1)));
        assertThatThrownBy(() -> service.listValidEnabledLevels()).isInstanceOf(JeecgBootException.class);

        when(levelConfigMapper.selectList(any())).thenReturn(List.of(level(1, 0), level(2, 0)));
        assertThatThrownBy(() -> service.listValidEnabledLevels()).isInstanceOf(JeecgBootException.class);
    }

    private ContentUserLevelConfig level(int level, int threshold) {
        return new ContentUserLevelConfig()
            .setLevel(level)
            .setLevelName("Lv" + level)
            .setGrowthThreshold(threshold)
            .setBadgeStyleKey("lv-" + level)
            .setEnabled(Boolean.TRUE);
    }
}
