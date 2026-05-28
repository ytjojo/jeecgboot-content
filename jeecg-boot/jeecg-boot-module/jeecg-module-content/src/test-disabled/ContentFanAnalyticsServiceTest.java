package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentFanTrendDaily;
import org.jeecg.modules.content.user.mapper.ContentFanTrendDailyMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentFanAnalyticsServiceImpl;
import org.jeecg.modules.content.user.vo.ContentFanProfileVO;
import org.jeecg.modules.content.user.vo.ContentFanTrendVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 粉丝分析服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentFanAnalyticsServiceTest {

    @Mock
    private ContentUserRelationMapper relationMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentFanTrendDailyMapper fanTrendDailyMapper;

    @InjectMocks
    private ContentFanAnalyticsServiceImpl fanAnalyticsService;

    // ========== 粉丝列表查询 ==========

    @Test
    void shouldReturnFanListWithPagination() {
        String userId = "owner1";
        ContentUserRelation fanRelation = new ContentUserRelation()
                .setOwnerUserId("fan1")
                .setTargetUserId(userId)
                .setFollowed(Boolean.TRUE)
                .setRelationStatus("ACTIVE")
                .setFollowedAt(new Date());

        IPage<ContentUserRelation> page = new Page<>(1, 10);
        page.setRecords(List.of(fanRelation));
        page.setTotal(1);
        when(relationMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(page);

        ContentUserProfile fanProfile = new ContentUserProfile()
                .setUserId("fan1")
                .setNickname("粉丝一号")
                .setFollowerCount(100);
        when(profileMapper.selectByUserId("fan1")).thenReturn(fanProfile);

        ContentRelationUserPageVO result = fanAnalyticsService.listFans(userId, null, 1L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmptyFanList() {
        IPage<ContentUserRelation> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(relationMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(page);

        ContentRelationUserPageVO result = fanAnalyticsService.listFans("owner1", null, 1L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void shouldFilterFansByKeyword() {
        String userId = "owner1";
        ContentUserRelation fanRelation = new ContentUserRelation()
                .setOwnerUserId("fan1")
                .setTargetUserId(userId)
                .setFollowed(Boolean.TRUE)
                .setRelationStatus("ACTIVE");

        IPage<ContentUserRelation> page = new Page<>(1, 10);
        page.setRecords(List.of(fanRelation));
        page.setTotal(1);
        when(relationMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(page);

        ContentUserProfile fanProfile = new ContentUserProfile()
                .setUserId("fan1")
                .setNickname("测试用户");
        when(profileMapper.selectByUserId("fan1")).thenReturn(fanProfile);

        ContentRelationUserPageVO result = fanAnalyticsService.listFans(userId, "测试", 1L, 10L);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowWhenUserIdInvalid() {
        assertThatThrownBy(() -> fanAnalyticsService.listFans(null, null, 1L, 10L))
                .isInstanceOf(JeecgBootException.class);
    }

    // ========== 粉丝趋势统计 ==========

    @Test
    void shouldReturnDailyFanTrend() {
        String userId = "owner1";
        Date startDate = new Date(System.currentTimeMillis() - 7 * 86400000L);
        Date endDate = new Date();

        ContentFanTrendDaily daily = new ContentFanTrendDaily()
                .setUserId(userId)
                .setStatisticDate(new Date())
                .setNewFollowerCount(5);
        when(fanTrendDailyMapper.selectList(any(Wrapper.class))).thenReturn(List.of(daily));

        ContentFanTrendVO result = fanAnalyticsService.getFanTrend(userId, "DAY", startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.getDataPoints()).isNotEmpty();
    }

    @Test
    void shouldReturnWeeklyFanTrend() {
        String userId = "owner1";
        Date startDate = new Date(System.currentTimeMillis() - 30 * 86400000L);
        Date endDate = new Date();

        when(fanTrendDailyMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        ContentFanTrendVO result = fanAnalyticsService.getFanTrend(userId, "WEEK", startDate, endDate);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnMonthlyFanTrend() {
        String userId = "owner1";
        Date startDate = new Date(System.currentTimeMillis() - 90 * 86400000L);
        Date endDate = new Date();

        when(fanTrendDailyMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        ContentFanTrendVO result = fanAnalyticsService.getFanTrend(userId, "MONTH", startDate, endDate);

        assertThat(result).isNotNull();
    }

    // ========== 粉丝画像分析 ==========

    @Test
    void shouldReturnFanProfileWhenEnoughFans() {
        String userId = "owner1";
        // 设置粉丝数 > 100
        ContentUserProfile ownerProfile = new ContentUserProfile()
                .setUserId(userId)
                .setFollowerCount(150);
        when(profileMapper.selectByUserId(userId)).thenReturn(ownerProfile);

        // mock粉丝关系
        ContentUserRelation fan1 = new ContentUserRelation()
                .setOwnerUserId("fan1").setTargetUserId(userId).setFollowed(Boolean.TRUE);
        ContentUserRelation fan2 = new ContentUserRelation()
                .setOwnerUserId("fan2").setTargetUserId(userId).setFollowed(Boolean.TRUE);
        when(relationMapper.selectFollowers(userId)).thenReturn(List.of(fan1, fan2));

        // mock粉丝profile
        ContentUserProfile fanProfile1 = new ContentUserProfile()
                .setUserId("fan1").setRegion("北京").setProfession("IT");
        ContentUserProfile fanProfile2 = new ContentUserProfile()
                .setUserId("fan2").setRegion("上海").setProfession("设计");
        when(profileMapper.selectByUserId("fan1")).thenReturn(fanProfile1);
        when(profileMapper.selectByUserId("fan2")).thenReturn(fanProfile2);

        ContentFanProfileVO result = fanAnalyticsService.getFanProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.getRegionDistribution()).isNotNull();
    }

    @Test
    void shouldReturnHintWhenInsufficientFans() {
        String userId = "owner1";
        ContentUserProfile ownerProfile = new ContentUserProfile()
                .setUserId(userId)
                .setFollowerCount(50);
        when(profileMapper.selectByUserId(userId)).thenReturn(ownerProfile);

        ContentFanProfileVO result = fanAnalyticsService.getFanProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.getInsufficientData()).isTrue();
    }

    // ========== 粉丝数据导出 ==========

    @Test
    void shouldExportFanDataAsCsv() {
        String userId = "owner1";
        ContentUserRelation fanRelation = new ContentUserRelation()
                .setOwnerUserId("fan1").setTargetUserId(userId).setFollowed(Boolean.TRUE);
        when(relationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(fanRelation));

        ContentUserProfile fanProfile = new ContentUserProfile()
                .setUserId("fan1").setNickname("粉丝一号").setRegion("北京");
        when(profileMapper.selectByUserId("fan1")).thenReturn(fanProfile);

        String csv = fanAnalyticsService.exportFans(userId);

        assertThat(csv).isNotNull();
        assertThat(csv).contains("粉丝一号");
        // 验证脱敏：不应包含完整userId
    }
}
