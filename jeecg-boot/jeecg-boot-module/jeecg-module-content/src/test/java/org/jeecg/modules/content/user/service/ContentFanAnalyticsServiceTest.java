package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentFanTrendDaily;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentFanTrendDailyMapper;
import org.jeecg.modules.content.user.mapper.ContentUserActivitySnapshotMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.impl.ContentFanAnalyticsServiceImpl;
import org.jeecg.modules.content.user.vo.ContentFanProfileVO;
import org.jeecg.modules.content.user.vo.ContentFanTrendVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 粉丝数据分析服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentFanAnalyticsServiceTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentFanTrendDailyMapper fanTrendDailyMapper;

    @Mock
    private ContentUserActivitySnapshotMapper activitySnapshotMapper;

    @InjectMocks
    private ContentFanAnalyticsServiceImpl fanAnalyticsService;

    @Test
    void shouldReturnPaginatedFanList() {
        ContentUserRelation relation = new ContentUserRelation()
            .setOwnerUserId("fan1")
            .setTargetUserId("user1")
            .setFollowed(true)
            .setFollowedAt(new Date());

        Page<ContentUserRelation> page = new Page<>(1, 10);
        page.setRecords(List.of(relation));
        page.setTotal(1);

        when(relationMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(profileMapper.selectList(any())).thenReturn(List.of(
            new ContentUserProfile().setUserId("fan1").setNickname("粉丝1")));
        when(activitySnapshotMapper.selectList(any())).thenReturn(List.of());

        ContentRelationUserPageVO result = fanAnalyticsService.listFans("user1", null, 1L, 10L);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords().get(0).getTargetUserId()).isEqualTo("fan1");
    }

    @Test
    void shouldReturnEmptyPageWhenKeywordMatchesNoProfiles() {
        when(profileMapper.selectList(any())).thenReturn(List.of());

        ContentRelationUserPageVO result = fanAnalyticsService.listFans("user1", "不存在的昵称", 1L, 10L);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0L);
    }

    @Test
    void shouldReturnDailyTrendData() {
        when(fanTrendDailyMapper.selectList(any())).thenReturn(List.of(
            new ContentFanTrendDaily().setUserId("user1").setDate(LocalDate.of(2026, 5, 20)).setNewFollowerCount(10),
            new ContentFanTrendDaily().setUserId("user1").setDate(LocalDate.of(2026, 5, 21)).setNewFollowerCount(15)));

        List<ContentFanTrendVO> result = fanAnalyticsService.getFanTrend("user1", "day",
            LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 21));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNewFollowerCount()).isEqualTo(10);
        assertThat(result.get(1).getNewFollowerCount()).isEqualTo(15);
    }

    @Test
    void shouldAggregateTrendByWeek() {
        when(fanTrendDailyMapper.selectList(any())).thenReturn(List.of(
            new ContentFanTrendDaily().setUserId("user1").setDate(LocalDate.of(2026, 5, 18)).setNewFollowerCount(5),
            new ContentFanTrendDaily().setUserId("user1").setDate(LocalDate.of(2026, 5, 19)).setNewFollowerCount(10),
            new ContentFanTrendDaily().setUserId("user1").setDate(LocalDate.of(2026, 5, 25)).setNewFollowerCount(8)));

        List<ContentFanTrendVO> result = fanAnalyticsService.getFanTrend("user1", "week",
            LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 25));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNewFollowerCount()).isEqualTo(15);
        assertThat(result.get(1).getNewFollowerCount()).isEqualTo(8);
    }

    @Test
    void shouldRejectTrendWhenDatesAreNull() {
        assertThatThrownBy(() -> fanAnalyticsService.getFanTrend("user1", "day", null, LocalDate.now()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("开始日期和结束日期不能为空");
    }

    @Test
    void shouldRejectTrendWhenStartDateAfterEndDate() {
        assertThatThrownBy(() -> fanAnalyticsService.getFanTrend("user1", "day",
            LocalDate.of(2026, 5, 21), LocalDate.of(2026, 5, 20)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("开始日期不能晚于结束日期");
    }

    @Test
    void shouldReturnHintWhenFanCountInsufficient() {
        when(relationMapper.selectCount(any())).thenReturn(50L);

        ContentFanProfileVO result = fanAnalyticsService.getFanProfile("user1");

        assertThat(result.getFanCount()).isEqualTo(50);
        assertThat(result.getHint()).isEqualTo("粉丝数不足100人，暂不支持画像分析");
        assertThat(result.getRegionDistribution()).isNull();
    }

    @Test
    void shouldReturnRegionDistributionWhenFanCountSufficient() {
        when(relationMapper.selectCount(any())).thenReturn(150L);
        when(relationMapper.selectList(any())).thenReturn(List.of(
            new ContentUserRelation().setOwnerUserId("fan1").setTargetUserId("user1"),
            new ContentUserRelation().setOwnerUserId("fan2").setTargetUserId("user1"),
            new ContentUserRelation().setOwnerUserId("fan3").setTargetUserId("user1")));
        when(profileMapper.selectList(any())).thenReturn(List.of(
            new ContentUserProfile().setUserId("fan1").setRegion("北京"),
            new ContentUserProfile().setUserId("fan2").setRegion("北京"),
            new ContentUserProfile().setUserId("fan3").setRegion("上海")));

        ContentFanProfileVO result = fanAnalyticsService.getFanProfile("user1");

        assertThat(result.getFanCount()).isEqualTo(150);
        assertThat(result.getHint()).isNull();
        assertThat(result.getRegionDistribution()).containsEntry("北京", 2);
        assertThat(result.getRegionDistribution()).containsEntry("上海", 1);
    }

    @Test
    void shouldRejectWhenUserIdIsBlank() {
        assertThatThrownBy(() -> fanAnalyticsService.listFans(null, null, 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户ID不能为空");
        assertThatThrownBy(() -> fanAnalyticsService.getFanTrend("", "day", LocalDate.now(), LocalDate.now()))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户ID不能为空");
        assertThatThrownBy(() -> fanAnalyticsService.getFanProfile("  "))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户ID不能为空");
    }

    @Test
    void shouldRejectPageSizeOver100() {
        assertThatThrownBy(() -> fanAnalyticsService.listFans("user1", null, 1L, 101L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分页大小不能超过100");
    }
}
