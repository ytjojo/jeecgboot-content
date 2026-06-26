package org.jeecg.modules.content.circle.growth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.circle.growth.enums.GrowthActionEnum;
import org.jeecg.modules.content.circle.growth.vo.MemberGrowthVO;

public interface IMemberGrowthService extends IService<CircleMemberGrowth> {

    void addExperience(String circleId, String userId, GrowthActionEnum action, String bizId);

    void revokeExperience(String circleId, String userId, GrowthActionEnum action, String bizId);

    MemberGrowthVO getGrowthInfo(String circleId, String userId);

    int getParticipationDays(String circleId, String userId);
}
