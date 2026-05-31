package org.jeecg.modules.content.user.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.growth.constant.GrowthConstant;
import org.jeecg.modules.content.user.growth.entity.CircleGrowthLog;
import org.jeecg.modules.content.user.growth.entity.CircleLeaderboardSnapshot;
import org.jeecg.modules.content.user.growth.enums.LeaderboardDimensionEnum;
import org.jeecg.modules.content.user.growth.mapper.CircleGrowthLogMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleLeaderboardSnapshotMapper;
import org.jeecg.modules.content.user.growth.service.ILeaderboardService;
import org.jeecg.modules.content.user.growth.vo.LeaderboardEntryVO;
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

    @Override
    public List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId) {
        LambdaQueryWrapper<CircleLeaderboardSnapshot> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleLeaderboardSnapshot::getCircleId, circleId)
          .eq(CircleLeaderboardSnapshot::getDimension, dimension)
          .eq(CircleLeaderboardSnapshot::getPeriod, period)
          .orderByAsc(CircleLeaderboardSnapshot::getRankNum)
          .last("LIMIT " + GrowthConstant.LEADERBOARD_TOP_N);
        List<CircleLeaderboardSnapshot> snapshots = this.list(qw);

        List<LeaderboardEntryVO> entries = new ArrayList<>();
        for (CircleLeaderboardSnapshot s : snapshots) {
            LeaderboardEntryVO vo = new LeaderboardEntryVO();
            vo.setUserId(s.getUserId());
            vo.setScore(s.getScore());
            vo.setRankNum(s.getRankNum());
            vo.setHighlighted(s.getUserId().equals(currentUserId));
            entries.add(vo);
        }
        return entries;
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
            for (String period : new String[]{"WEEK", "MONTH", "ALL"}) {
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
