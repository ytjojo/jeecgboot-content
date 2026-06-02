package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.mapper.ContentChannelEditorialPickMapper;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelEditorialPickServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentChannelEditorialPickServiceTest {

    @Mock
    private ContentChannelEditorialPickMapper editorialPickMapper;

    @InjectMocks
    private ContentChannelEditorialPickServiceImpl editorialPickService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(editorialPickService, "baseMapper", editorialPickMapper);
    }

    @Test
    void createPick_shouldCreateValidPick() {
        when(editorialPickMapper.insert(any(ContentChannelEditorialPick.class))).thenReturn(1);

        ChannelEditorialPickCreateReq req = new ChannelEditorialPickCreateReq();
        req.setChannelId("ch1");
        req.setRecommendationText("优质技术频道");
        req.setStartTime(new Date());
        req.setOperatorId("op1");

        ContentChannelEditorialPick pick = editorialPickService.createPick(req);

        assertThat(pick.getChannelId()).isEqualTo("ch1");
        assertThat(pick.getStatus()).isEqualTo(1);
        verify(editorialPickMapper).insert(any(ContentChannelEditorialPick.class));
    }

    @Test
    void updatePick_shouldThrowWhenNotFound() {
        when(editorialPickMapper.selectById("nonexistent")).thenReturn(null);

        ChannelEditorialPickUpdateReq req = new ChannelEditorialPickUpdateReq();
        req.setId("nonexistent");

        assertThatThrownBy(() -> editorialPickService.updatePick(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("精选记录不存在");
    }

    @Test
    void updatePick_shouldUpdateRecommendationText() {
        ContentChannelEditorialPick existing = new ContentChannelEditorialPick();
        existing.setId("pick1");
        existing.setRecommendationText("旧推荐语");

        when(editorialPickMapper.selectById("pick1")).thenReturn(existing);
        when(editorialPickMapper.updateById(any(ContentChannelEditorialPick.class))).thenReturn(1);

        ChannelEditorialPickUpdateReq req = new ChannelEditorialPickUpdateReq();
        req.setId("pick1");
        req.setRecommendationText("更新后的推荐语");

        editorialPickService.updatePick(req);

        verify(editorialPickMapper).updateById(argThat((ContentChannelEditorialPick p) -> p.getRecommendationText().equals("更新后的推荐语")));
    }

    @Test
    void removePick_shouldSetStatusToZero() {
        ContentChannelEditorialPick existing = new ContentChannelEditorialPick();
        existing.setId("pick1");
        existing.setStatus(1);

        when(editorialPickMapper.selectById("pick1")).thenReturn(existing);
        when(editorialPickMapper.updateById(any(ContentChannelEditorialPick.class))).thenReturn(1);

        editorialPickService.removePick("pick1");

        verify(editorialPickMapper).updateById(argThat((ContentChannelEditorialPick p) -> p.getStatus() == 0));
    }

    @Test
    void removePick_shouldThrowWhenNotFound() {
        when(editorialPickMapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> editorialPickService.removePick("nonexistent"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("精选记录不存在");
    }

    @Test
    void listActivePicks_shouldReturnActivePicks() {
        ContentChannelEditorialPick pick = new ContentChannelEditorialPick();
        pick.setId("pick1");
        pick.setChannelId("ch1");
        pick.setRecommendationText("推荐语");
        pick.setStartTime(new Date());
        pick.setStatus(1);

        when(editorialPickMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pick));

        List<ChannelEditorialPickVO> result = editorialPickService.listActivePicks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecommendationText()).isEqualTo("推荐语");
    }
}
