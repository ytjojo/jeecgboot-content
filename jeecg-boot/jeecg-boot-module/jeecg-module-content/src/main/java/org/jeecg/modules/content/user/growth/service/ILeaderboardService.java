package org.jeecg.modules.content.user.growth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.growth.entity.CircleLeaderboardSnapshot;
import org.jeecg.modules.content.user.growth.vo.LeaderboardEntryVO;

import java.util.List;

public interface ILeaderboardService extends IService<CircleLeaderboardSnapshot> {

    List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId);

    void refreshSnapshot(String circleId);
}
