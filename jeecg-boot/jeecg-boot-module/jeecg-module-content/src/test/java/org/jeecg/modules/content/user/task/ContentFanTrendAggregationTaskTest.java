package org.jeecg.modules.content.user.task;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.jeecg.modules.content.user.entity.ContentFanTrendDaily;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentFanTrendDailyMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentFanTrendAggregationTaskTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @Mock
    private ContentFanTrendDailyMapper fanTrendDailyMapper;

    @InjectMocks
    private ContentFanTrendAggregationTask task;

    @Test
    void shouldInsertNewRecordWhenNoExistingTrendRow() {
        ContentUserRelation r1 = new ContentUserRelation().setTargetUserId("u_a");
        ContentUserRelation r2 = new ContentUserRelation().setTargetUserId("u_a");
        ContentUserRelation r3 = new ContentUserRelation().setTargetUserId("u_b");
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(r1, r2, r3));
        when(fanTrendDailyMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        task.aggregateDailyFanTrend();

        ArgumentCaptor<ContentFanTrendDaily> captor = ArgumentCaptor.forClass(ContentFanTrendDaily.class);
        verify(fanTrendDailyMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        List<ContentFanTrendDaily> saved = captor.getAllValues();
        assertThat(saved).extracting(ContentFanTrendDaily::getUserId)
            .containsExactlyInAnyOrder("u_a", "u_b");
        assertThat(saved).extracting(ContentFanTrendDaily::getNewFollowerCount)
            .containsExactlyInAnyOrder(2, 1);
    }

    @Test
    void shouldUpdateExistingRecordWhenRowAlreadyExists() {
        ContentUserRelation r1 = new ContentUserRelation().setTargetUserId("u_a");
        ContentUserRelation r2 = new ContentUserRelation().setTargetUserId("u_a");
        ContentUserRelation r3 = new ContentUserRelation().setTargetUserId("u_a");
        ContentFanTrendDaily existing = new ContentFanTrendDaily()
            .setUserId("u_a")
            .setNewFollowerCount(0);
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(r1, r2, r3));
        when(fanTrendDailyMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        task.aggregateDailyFanTrend();

        ArgumentCaptor<ContentFanTrendDaily> captor = ArgumentCaptor.forClass(ContentFanTrendDaily.class);
        verify(fanTrendDailyMapper).updateById(captor.capture());
        assertThat(captor.getValue().getNewFollowerCount()).isEqualTo(3);
        verify(fanTrendDailyMapper, never()).insert(any(ContentFanTrendDaily.class));
    }

    @Test
    void shouldBeNoOpWhenNoNewFollowers() {
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        task.aggregateDailyFanTrend();

        verify(fanTrendDailyMapper, never()).selectOne(any(Wrapper.class));
        verify(fanTrendDailyMapper, never()).insert(any(ContentFanTrendDaily.class));
        verify(fanTrendDailyMapper, never()).updateById(any(ContentFanTrendDaily.class));
    }

    @Test
    void shouldGroupCountsByTargetUser() {
        ContentUserRelation r1 = new ContentUserRelation().setTargetUserId("u_x");
        ContentUserRelation r2 = new ContentUserRelation().setTargetUserId("u_y");
        ContentUserRelation r3 = new ContentUserRelation().setTargetUserId("u_x");
        ContentUserRelation r4 = new ContentUserRelation().setTargetUserId("u_x");
        ContentUserRelation r5 = new ContentUserRelation().setTargetUserId("u_y");
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(r1, r2, r3, r4, r5));
        when(fanTrendDailyMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        task.aggregateDailyFanTrend();

        ArgumentCaptor<ContentFanTrendDaily> captor = ArgumentCaptor.forClass(ContentFanTrendDaily.class);
        verify(fanTrendDailyMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        List<ContentFanTrendDaily> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        ContentFanTrendDaily xRow = saved.stream()
            .filter(r -> "u_x".equals(r.getUserId())).findFirst().orElseThrow();
        ContentFanTrendDaily yRow = saved.stream()
            .filter(r -> "u_y".equals(r.getUserId())).findFirst().orElseThrow();
        assertThat(xRow.getNewFollowerCount()).isEqualTo(3);
        assertThat(yRow.getNewFollowerCount()).isEqualTo(2);
    }

    @Test
    void shouldQueryRelationsWithinYesterdayDateRange() {
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        task.aggregateDailyFanTrend();

        ArgumentCaptor<Wrapper<ContentUserRelation>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(relationMapper).selectList(captor.capture());
        Wrapper<ContentUserRelation> wrapper = captor.getValue();
        assertThat(wrapper).isNotNull();
    }

    @Test
    void shouldIgnoreUnfollowedRelations() {
        ContentUserRelation followed = new ContentUserRelation()
            .setTargetUserId("u_a")
            .setFollowed(Boolean.TRUE)
            .setFollowedAt(new Date());
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(followed));
        when(fanTrendDailyMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        task.aggregateDailyFanTrend();

        ArgumentCaptor<ContentFanTrendDaily> captor = ArgumentCaptor.forClass(ContentFanTrendDaily.class);
        verify(fanTrendDailyMapper, org.mockito.Mockito.times(1)).insert(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("u_a");
        assertThat(captor.getValue().getNewFollowerCount()).isEqualTo(1);
    }
}
