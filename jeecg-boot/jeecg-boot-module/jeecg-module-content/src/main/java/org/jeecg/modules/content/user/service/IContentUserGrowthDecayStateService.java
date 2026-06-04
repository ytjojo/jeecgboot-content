package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserGrowthDecayState;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayStatusVO;

import java.util.Date;
import java.util.List;

/**
 * 内容社区成长值衰减状态服务契约。
 */
public interface IContentUserGrowthDecayStateService extends IService<ContentUserGrowthDecayState> {

    /**
     * 查询当前默认衰减规则说明。
     */
    ContentUserGrowthDecayRuleVO getDecayRule();

    /**
     * 校验并返回可执行的衰减规则。
     */
    ContentUserGrowthDecayRuleVO validateDecayRule(ContentUserGrowthDecayRuleVO rule);

    /**
     * 按默认规则筛选衰减候选用户。
     */
    List<ContentUserProfile> listDecayCandidates(Date runTime);

    /**
     * 按指定规则筛选衰减候选用户。
     */
    List<ContentUserProfile> listDecayCandidates(Date runTime, ContentUserGrowthDecayRuleVO rule);

    /**
     * 按默认规则执行衰减任务。
     */
    int executeDecay(Date runTime);

    /**
     * 按指定规则执行衰减任务。
     */
    int executeDecay(Date runTime, ContentUserGrowthDecayRuleVO rule);

    /**
     * 记录用户活跃，并在成长值恢复阈值时清除降级保护。
     */
    void markUserActive(String userId, Date activeTime, Integer currentGrowthValue);

    /**
     * 查询指定用户的衰减状态。
     */
    ContentUserGrowthDecayStatusVO getDecayStatus(String userId);
}
