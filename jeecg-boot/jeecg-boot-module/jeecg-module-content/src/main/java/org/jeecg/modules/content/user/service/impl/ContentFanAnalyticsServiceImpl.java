package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentFanTrendDaily;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentFanTrendDailyMapper;
import org.jeecg.modules.content.user.mapper.ContentUserActivitySnapshotMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.IContentFanAnalyticsService;
import org.jeecg.modules.content.user.vo.ContentFanProfileVO;
import org.jeecg.modules.content.user.vo.ContentFanTrendVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserItemVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 粉丝数据分析服务实现。
 */
@Service
public class ContentFanAnalyticsServiceImpl implements IContentFanAnalyticsService {

    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final int MIN_FAN_COUNT_FOR_PROFILE = 100;
    private static final String ACTIVE_STATUS = "ACTIVE";

    @Resource
    private ContentUserRelationMapper relationMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentFanTrendDailyMapper fanTrendDailyMapper;

    @Resource
    private ContentUserActivitySnapshotMapper activitySnapshotMapper;

    @Override
    public ContentRelationUserPageVO listFans(String userId, String keyword, Long pageNo, Long pageSize) {
        validateUserId(userId);
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);

        List<String> keywordUserIds = findUserIdsByKeyword(keyword);
        if (keyword != null && !keyword.trim().isEmpty() && keywordUserIds.isEmpty()) {
            return emptyPage(currentPage, currentSize);
        }

        IPage<ContentUserRelation> page = relationMapper.selectPage(new Page<>(currentPage, currentSize),
            Wrappers.<ContentUserRelation>lambdaQuery()
                .eq(ContentUserRelation::getTargetUserId, userId)
                .eq(ContentUserRelation::getFollowed, Boolean.TRUE)
                .in(keywordUserIds != null && !keywordUserIds.isEmpty(), ContentUserRelation::getOwnerUserId, keywordUserIds)
                .orderByDesc(ContentUserRelation::getFollowedAt));

        List<ContentRelationUserItemVO> records = buildFanItems(page.getRecords());
        return new ContentRelationUserPageVO()
            .setRecords(records)
            .setTotal(page.getTotal())
            .setPageNo(currentPage)
            .setPageSize(currentSize);
    }

    @Override
    public List<ContentFanTrendVO> getFanTrend(String userId, String period, LocalDate startDate, LocalDate endDate) {
        validateUserId(userId);
        if (startDate == null || endDate == null) {
            throw new JeecgBootException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new JeecgBootException("开始日期不能晚于结束日期");
        }

        List<ContentFanTrendDaily> dailyData = fanTrendDailyMapper.selectList(
            Wrappers.<ContentFanTrendDaily>lambdaQuery()
                .eq(ContentFanTrendDaily::getUserId, userId)
                .ge(ContentFanTrendDaily::getDate, startDate)
                .le(ContentFanTrendDaily::getDate, endDate)
                .orderByAsc(ContentFanTrendDaily::getDate));

        List<ContentFanTrendVO> trendList = dailyData.stream()
            .map(d -> new ContentFanTrendVO().setDate(d.getDate()).setNewFollowerCount(d.getNewFollowerCount()))
            .toList();

        return aggregateByPeriod(trendList, period);
    }

    @Override
    public ContentFanProfileVO getFanProfile(String userId) {
        validateUserId(userId);

        long fanCount = relationMapper.selectCount(
            Wrappers.<ContentUserRelation>lambdaQuery()
                .eq(ContentUserRelation::getTargetUserId, userId)
                .eq(ContentUserRelation::getFollowed, Boolean.TRUE));

        ContentFanProfileVO profile = new ContentFanProfileVO().setFanCount((int) fanCount);

        if (fanCount < MIN_FAN_COUNT_FOR_PROFILE) {
            profile.setHint("粉丝数不足100人，暂不支持画像分析");
            return profile;
        }

        List<ContentUserRelation> fans = relationMapper.selectList(
            Wrappers.<ContentUserRelation>lambdaQuery()
                .eq(ContentUserRelation::getTargetUserId, userId)
                .eq(ContentUserRelation::getFollowed, Boolean.TRUE));

        List<String> fanUserIds = fans.stream()
            .map(ContentUserRelation::getOwnerUserId)
            .toList();

        List<ContentUserProfile> fanProfiles = profileMapper.selectList(
            Wrappers.<ContentUserProfile>lambdaQuery()
                .in(ContentUserProfile::getUserId, fanUserIds));

        Map<String, Integer> regionDistribution = fanProfiles.stream()
            .map(ContentUserProfile::getRegion)
            .filter(r -> r != null && !r.trim().isEmpty())
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.summingInt(e -> 1)));

        profile.setRegionDistribution(regionDistribution);
        return profile;
    }

    @Override
    public void exportFans(String userId, HttpServletResponse response) {
        validateUserId(userId);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=fans_" + userId + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("用户ID,昵称,地区,关注时间");

            List<ContentUserRelation> fans = relationMapper.selectList(
                Wrappers.<ContentUserRelation>lambdaQuery()
                    .eq(ContentUserRelation::getTargetUserId, userId)
                    .eq(ContentUserRelation::getFollowed, Boolean.TRUE)
                    .orderByDesc(ContentUserRelation::getFollowedAt));

            if (fans.isEmpty()) {
                return;
            }

            List<String> fanUserIds = fans.stream()
                .map(ContentUserRelation::getOwnerUserId)
                .toList();

            Map<String, ContentUserProfile> profileMap = profileMapper.selectList(
                Wrappers.<ContentUserProfile>lambdaQuery()
                    .in(ContentUserProfile::getUserId, fanUserIds))
                .stream()
                .collect(Collectors.toMap(ContentUserProfile::getUserId, Function.identity(), (a, b) -> a));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (ContentUserRelation fan : fans) {
                ContentUserProfile profile = profileMap.get(fan.getOwnerUserId());
                String nickname = profile != null ? desensitize(profile.getNickname()) : "";
                String region = profile != null ? profile.getRegion() : "";
                String followedAt = fan.getFollowedAt() != null ?
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fan.getFollowedAt()) : "";
                writer.println(escapeCsv(fan.getOwnerUserId()) + "," + escapeCsv(nickname) + "," + escapeCsv(region) + "," + escapeCsv(followedAt));
            }
            writer.flush();
        } catch (IOException e) {
            throw new JeecgBootException("导出失败");
        }
    }

    private List<ContentFanTrendVO> aggregateByPeriod(List<ContentFanTrendVO> dailyData, String period) {
        if (dailyData.isEmpty()) {
            return Collections.emptyList();
        }
        if ("day".equalsIgnoreCase(period) || period == null || period.trim().isEmpty()) {
            return dailyData;
        }

        Map<LocalDate, Integer> aggregated = new LinkedHashMap<>();
        for (ContentFanTrendVO item : dailyData) {
            LocalDate key;
            if ("week".equalsIgnoreCase(period)) {
                key = item.getDate().with(java.time.DayOfWeek.MONDAY);
            } else if ("month".equalsIgnoreCase(period)) {
                key = item.getDate().withDayOfMonth(1);
            } else {
                key = item.getDate();
            }
            aggregated.merge(key, item.getNewFollowerCount(), Integer::sum);
        }

        return aggregated.entrySet().stream()
            .map(e -> new ContentFanTrendVO().setDate(e.getKey()).setNewFollowerCount(e.getValue()))
            .toList();
    }

    private List<ContentRelationUserItemVO> buildFanItems(List<ContentUserRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ownerUserIds = relations.stream()
            .map(ContentUserRelation::getOwnerUserId)
            .toList();
        Map<String, ContentUserProfile> profileMap = profileMapper.selectList(
                Wrappers.<ContentUserProfile>lambdaQuery().in(ContentUserProfile::getUserId, ownerUserIds))
            .stream()
            .collect(Collectors.toMap(ContentUserProfile::getUserId, Function.identity(), (a, b) -> a));
        Map<String, ContentUserActivitySnapshot> activityMap = activitySnapshotMapper.selectList(
                Wrappers.<ContentUserActivitySnapshot>lambdaQuery()
                    .in(ContentUserActivitySnapshot::getActorUserId, ownerUserIds)
                    .eq(ContentUserActivitySnapshot::getSnapshotStatus, ACTIVE_STATUS)
                    .orderByDesc(ContentUserActivitySnapshot::getActivityTime))
            .stream()
            .collect(Collectors.toMap(ContentUserActivitySnapshot::getActorUserId, Function.identity(), (a, b) -> a));

        return relations.stream()
            .map(r -> ContentRelationUserItemVO.from(r, profileMap.get(r.getOwnerUserId()), activityMap.get(r.getOwnerUserId())))
            .toList();
    }

    private List<String> findUserIdsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return profileMapper.selectList(Wrappers.<ContentUserProfile>lambdaQuery()
                .like(ContentUserProfile::getNickname, keyword.trim()))
            .stream()
            .map(ContentUserProfile::getUserId)
            .toList();
    }

    private String desensitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + "*".repeat(value.length() - 2) + value.charAt(value.length() - 1);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException("用户ID不能为空");
        }
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1L ? 1L : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        long size = pageSize == null || pageSize < 1L ? DEFAULT_PAGE_SIZE : pageSize;
        if (size > MAX_PAGE_SIZE) {
            throw new JeecgBootException("分页大小不能超过100");
        }
        return size;
    }

    private ContentRelationUserPageVO emptyPage(long pageNo, long pageSize) {
        return new ContentRelationUserPageVO()
            .setRecords(Collections.emptyList())
            .setTotal(0L)
            .setPageNo(pageNo)
            .setPageSize(pageSize);
    }
}
