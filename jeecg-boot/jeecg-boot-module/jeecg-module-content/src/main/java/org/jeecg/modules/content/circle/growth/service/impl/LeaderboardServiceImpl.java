package org.jeecg.modules.content.circle.growth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.content.circle.growth.constant.GrowthConstant;
import org.jeecg.modules.content.circle.growth.entity.CircleGrowthLog;
import org.jeecg.modules.content.circle.growth.entity.CircleLeaderboardSnapshot;
import org.jeecg.modules.content.circle.growth.enums.LeaderboardDimensionEnum;
import org.jeecg.modules.content.circle.growth.mapper.CircleGrowthLogMapper;
import org.jeecg.modules.content.circle.growth.mapper.CircleLeaderboardSnapshotMapper;
import org.jeecg.modules.content.circle.growth.service.ILeaderboardService;
import org.jeecg.modules.content.circle.growth.vo.LeaderboardEntryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaderboardServiceImpl extends ServiceImpl<CircleLeaderboardSnapshotMapper, CircleLeaderboardSnapshot>
        implements ILeaderboardService {

    @Resource
    private CircleGrowthLogMapper growthLogMapper;
    @Resource
    private ISysBaseAPI sysBaseAPI;

    @Override
    public List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId) {
        LambdaQueryWrapper<CircleLeaderboardSnapshot> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleLeaderboardSnapshot::getCircleId, circleId)
          .eq(CircleLeaderboardSnapshot::getDimension, dimension)
          .eq(CircleLeaderboardSnapshot::getPeriod, period)
          .orderByAsc(CircleLeaderboardSnapshot::getRankNum)
          .last("LIMIT " + GrowthConstant.LEADERBOARD_TOP_N);
        List<CircleLeaderboardSnapshot> snapshots = this.list(qw);

        boolean userInList = false;
        Integer prevScore = null;
        List<LeaderboardEntryVO> entries = new ArrayList<>();
        for (CircleLeaderboardSnapshot s : snapshots) {
            LeaderboardEntryVO vo = new LeaderboardEntryVO();
            vo.setUserId(s.getUserId());
            vo.setScore(s.getScore());
            vo.setRankNum(s.getRankNum());
            vo.setHighlighted(s.getUserId().equals(currentUserId));
            vo.setGap(prevScore != null ? prevScore - s.getScore() : 0);
            prevScore = s.getScore();
            if (s.getUserId().equals(currentUserId)) {
                userInList = true;
            }
            entries.add(vo);
        }

        // 当前用户不在 Top 50 中，查询其排名和得分并追加到列表末尾
        if (!userInList && currentUserId != null) {
            int userScore = computeUserScore(circleId, dimension, period, currentUserId);
            if (userScore > 0) {
                // 查询快照中得分高于当前用户的条目数量作为最低排名估计
                LambdaQueryWrapper<CircleLeaderboardSnapshot> countQw = new LambdaQueryWrapper<>();
                countQw.eq(CircleLeaderboardSnapshot::getCircleId, circleId)
                       .eq(CircleLeaderboardSnapshot::getDimension, dimension)
                       .eq(CircleLeaderboardSnapshot::getPeriod, period)
                       .gt(CircleLeaderboardSnapshot::getScore, userScore);
                long higherCountInSnapshot = this.count(countQw);
                int rank = (int) higherCountInSnapshot + 1;

                LeaderboardEntryVO userEntry = new LeaderboardEntryVO();
                userEntry.setUserId(currentUserId);
                userEntry.setScore(userScore);
                userEntry.setRankNum(rank);
                userEntry.setHighlighted(true);
                userEntry.setGap(prevScore != null ? prevScore - userScore : 0);
                entries.add(userEntry);
            }
        }

        // 批量查询用户信息，填充 username 和 avatar
        if (!entries.isEmpty()) {
            String ids = entries.stream()
                    .map(LeaderboardEntryVO::getUserId)
                    .distinct()
                    .collect(Collectors.joining(","));
            try {
                List<JSONObject> users = sysBaseAPI.queryUsersByIds(ids);
                Map<String, JSONObject> userMap = users.stream()
                        .collect(Collectors.toMap(
                                u -> u.getString("id"),
                                u -> u,
                                (u1, u2) -> u1));
                for (LeaderboardEntryVO entry : entries) {
                    JSONObject user = userMap.get(entry.getUserId());
                    if (user != null) {
                        entry.setUsername(user.getString("username"));
                        entry.setAvatar(user.getString("avatar"));
                    }
                }
            } catch (Exception e) {
                log.warn("批量查询排行榜用户信息失败: circleId={}, dimension={}, period={}", circleId, dimension, period, e);
            }
        }

        return entries;
    }

    private int computeUserScore(String circleId, String dimension, String period, String userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "WEEK" -> today.minusDays(6);
            case "MONTH" -> today.minusDays(29);
            default -> LocalDate.of(2000, 1, 1);
        };

        LambdaQueryWrapper<CircleGrowthLog> logQw = new LambdaQueryWrapper<>();
        logQw.eq(CircleGrowthLog::getCircleId, circleId)
             .eq(CircleGrowthLog::getUserId, userId)
             .eq(CircleGrowthLog::getRevoked, false)
             .ge(CircleGrowthLog::getBizDate, startDate);
        List<CircleGrowthLog> logs = growthLogMapper.selectList(logQw);

        return logs.stream().mapToInt(log -> switch (dimension) {
            case "EXP" -> log.getExpPoints() != null ? log.getExpPoints() : 0;
            case "CONTRIBUTION" -> log.getContributionPoints() != null ? log.getContributionPoints() : 0;
            case "POST" -> "POST".equals(log.getActionType()) ? 1 : 0;
            default -> 0;
        }).sum();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshSnapshot(String circleId) {
        LambdaQueryWrapper<CircleLeaderboardSnapshot> deleteQw = new LambdaQueryWrapper<>();
        deleteQw.eq(CircleLeaderboardSnapshot::getCircleId, circleId);
        this.remove(deleteQw);

        // 一次查询所有日志，供所有维度和周期共用
        LocalDate today = LocalDate.now();
        LocalDate allTimeStart = LocalDate.of(2000, 1, 1);
        LambdaQueryWrapper<CircleGrowthLog> logQw = new LambdaQueryWrapper<>();
        logQw.eq(CircleGrowthLog::getCircleId, circleId)
             .eq(CircleGrowthLog::getRevoked, false)
             .ge(CircleGrowthLog::getBizDate, allTimeStart);
        List<CircleGrowthLog> allLogs = growthLogMapper.selectList(logQw);

        LocalDateTime now = LocalDateTime.now();
        List<CircleLeaderboardSnapshot> batch = new ArrayList<>();
        for (LeaderboardDimensionEnum dim : LeaderboardDimensionEnum.values()) {
            for (String period : GrowthConstant.LEADERBOARD_PERIODS) {
                batch.addAll(buildSnapshots(circleId, dim.getCode(), period, allLogs, today, now));
            }
        }
        this.saveBatch(batch);
    }

    private List<CircleLeaderboardSnapshot> buildSnapshots(String circleId, String dimension, String period,
                                                            List<CircleGrowthLog> allLogs, LocalDate today,
                                                            LocalDateTime snapshotTime) {
        LocalDate startDate = switch (period) {
            case "WEEK" -> today.minusDays(6);
            case "MONTH" -> today.minusDays(29);
            default -> LocalDate.of(2000, 1, 1);
        };

        Map<String, Integer> userScores = allLogs.stream()
                .filter(log -> !log.getBizDate().isBefore(startDate))
                .collect(Collectors.groupingBy(
                        CircleGrowthLog::getUserId,
                        Collectors.summingInt(log -> switch (dimension) {
                            case "EXP" -> log.getExpPoints() != null ? log.getExpPoints() : 0;
                            case "CONTRIBUTION" -> log.getContributionPoints() != null ? log.getContributionPoints() : 0;
                            case "POST" -> "POST".equals(log.getActionType()) ? 1 : 0;
                            default -> 0;
                        })
                ));

        List<Map.Entry<String, Integer>> sorted = userScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(GrowthConstant.LEADERBOARD_TOP_N)
                .toList();

        List<CircleLeaderboardSnapshot> snapshots = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            CircleLeaderboardSnapshot snapshot = new CircleLeaderboardSnapshot()
                    .setCircleId(circleId)
                    .setUserId(entry.getKey())
                    .setDimension(dimension)
                    .setPeriod(period)
                    .setScore(entry.getValue())
                    .setRankNum(rank++)
                    .setSnapshotTime(snapshotTime);
            snapshots.add(snapshot);
        }
        return snapshots;
    }
}
