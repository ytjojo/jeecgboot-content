package org.jeecg.modules.content.user.growth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.growth.entity.CircleMemberAchievement;
import org.jeecg.modules.content.user.growth.enums.AchievementTypeEnum;
import org.jeecg.modules.content.user.growth.vo.AchievementVO;

import java.util.List;

public interface IAchievementService extends IService<CircleMemberAchievement> {

    void checkAndAward(String circleId, String userId);

    void revoke(String circleId, String userId, AchievementTypeEnum type);

    List<AchievementVO> getMemberAchievements(String circleId, String userId);
}
