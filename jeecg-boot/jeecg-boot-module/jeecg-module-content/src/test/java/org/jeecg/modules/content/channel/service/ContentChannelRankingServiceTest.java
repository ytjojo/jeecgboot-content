package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.mapper.ContentChannelRankingSnapshotMapper;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelRankingServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentChannelRankingServiceTest {

    @Mock
    private ContentChannelRankingSnapshotMapper rankingSnapshotMapper;

    @InjectMocks
    private ContentChannelRankingServiceImpl rankingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rankingService, "baseMapper", rankingSnapshotMapper);
    }

    @Test
    void getHotRanking_shouldReturnSortedByPosition() {
        ContentChannelRankingSnapshot s1 = new ContentChannelRankingSnapshot();
        s1.setChannelId("ch1");
        s1.setRankingType("HOT");
        s1.setDimension("DAILY");
        s1.setRankPosition(1);
        s1.setScore(new BigDecimal("88.5000"));
        s1.setSnapshotDate(new Date());

        ContentChannelRankingSnapshot s2 = new ContentChannelRankingSnapshot();
        s2.setChannelId("ch2");
        s2.setRankingType("HOT");
        s2.setDimension("DAILY");
        s2.setRankPosition(2);
        s2.setScore(new BigDecimal("75.0000"));
        s2.setSnapshotDate(new Date());

        when(rankingSnapshotMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(s1, s2));

        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        req.setDimension("DAILY");

        List<ChannelRankingItemVO> result = rankingService.getHotRanking(req);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRankPosition()).isEqualTo(1);
        assertThat(result.get(1).getRankPosition()).isEqualTo(2);
    }

    @Test
    void getNewRanking_shouldReturnNewChannelRanking() {
        ContentChannelRankingSnapshot s = new ContentChannelRankingSnapshot();
        s.setChannelId("ch1");
        s.setRankingType("NEW");
        s.setDimension("DAILY");
        s.setRankPosition(1);
        s.setScore(new BigDecimal("92.0000"));
        s.setSnapshotDate(new Date());

        when(rankingSnapshotMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(s));

        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        req.setDimension("DAILY");

        List<ChannelRankingItemVO> result = rankingService.getNewRanking(req);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getSystemRanking_shouldReturnSystemChannelRanking() {
        ContentChannelRankingSnapshot s = new ContentChannelRankingSnapshot();
        s.setChannelId("ch1");
        s.setRankingType("SYSTEM");
        s.setDimension("DAILY");
        s.setRankPosition(1);
        s.setScore(new BigDecimal("100.0000"));
        s.setSnapshotDate(new Date());

        when(rankingSnapshotMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(s));

        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        req.setDimension("DAILY");

        List<ChannelRankingItemVO> result = rankingService.getSystemRanking(req);

        assertThat(result).isNotEmpty();
    }
}
