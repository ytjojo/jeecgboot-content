package org.jeecg.modules.content.user.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.growth.constant.GrowthConstant;
import org.jeecg.modules.content.user.growth.entity.CircleLevel;
import org.jeecg.modules.content.user.growth.entity.CircleMemberGrowth;
import org.jeecg.modules.content.user.growth.enums.CircleLevelEnum;
import org.jeecg.modules.content.user.growth.mapper.CircleLevelMapper;
import org.jeecg.modules.content.user.growth.mapper.CircleMemberGrowthMapper;
import org.jeecg.modules.content.user.growth.service.ICircleLevelService;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.content.user.growth.vo.CircleLevelVO;
import org.jeecg.modules.content.user.growth.vo.LevelConditionVO;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CircleLevelServiceImpl extends ServiceImpl<CircleLevelMapper, CircleLevel>
        implements ICircleLevelService {

    @Resource
    private CircleMemberGrowthMapper growthMapper;
    @Resource
    private IContentNotificationService notificationService;
    @Resource
    private ISysBaseAPI sysBaseAPI;

    @Override
    public void calculateGrowthScore(String circleId) {
        List<CircleMemberGrowth> members = loadCircleMembers(circleId);
        int memberScore = calculateMemberScore(members);
        int contentScore = calculateContentScore(members);
        int activityScore = calculateActivityScore(members);
        int total = Math.min(memberScore + contentScore + activityScore, GrowthConstant.MAX_GROWTH_SCORE);

        CircleLevel level = getOrCreateLevel(circleId);
        level.setMemberScore(memberScore)
             .setContentScore(contentScore)
             .setActivityScore(activityScore)
             .setGrowthScore(total);
        this.saveOrUpdate(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLevel(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        CircleLevelEnum newLevelEnum = CircleLevelEnum.ofScore(level.getGrowthScore());
        int oldLevel = level.getLevel();

        if (newLevelEnum.getLevel() > oldLevel) {
            level.setLevel(newLevelEnum.getLevel());
            this.updateById(level);
            notifyLevelUpgrade(circleId, newLevelEnum);
            log.info("圈子{}等级从L{}提升为L{}", circleId, oldLevel, newLevelEnum.getLevel());
        }
    }

    @Override
    public CircleLevelVO getLevelInfo(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        CircleLevelEnum currentEnum = CircleLevelEnum.ofScore(level.getGrowthScore());

        CircleLevelVO vo = new CircleLevelVO();
        vo.setLevel(level.getLevel());
        vo.setLevelName(currentEnum.getName());
        vo.setGrowthScore(level.getGrowthScore());

        CircleLevelEnum[] values = CircleLevelEnum.values();
        int nextThreshold = GrowthConstant.MAX_GROWTH_SCORE;
        if (currentEnum.getLevel() < 5) {
            nextThreshold = values[currentEnum.getLevel()].getThreshold();
        }
        vo.setNextLevelThreshold(nextThreshold);

        int prevThreshold = currentEnum.getThreshold();
        int range = nextThreshold - prevThreshold;
        int progress = range > 0 ? (level.getGrowthScore() - prevThreshold) * 100 / range : 100;
        vo.setProgressPercent(Math.min(progress, 100));

        // 已解锁权益
        vo.setBenefits(buildBenefits(currentEnum.getLevel()));

        // 分项得分
        vo.setMemberScore(level.getMemberScore());
        vo.setContentScore(level.getContentScore());
        vo.setActivityScore(level.getActivityScore());

        // 下一等级各项条件（维度上限与 calculateMemberScore/contentScore/activityScore 保持一致）
        List<LevelConditionVO> conditions = new ArrayList<>();
        int memberCap = 400;
        int contentCap = 300;
        int interactionCap = 300;
        conditions.add(buildCondition("MEMBER", "成员规模", level.getMemberScore(), memberCap));
        conditions.add(buildCondition("CONTENT", "内容贡献", level.getContentScore(), contentCap));
        conditions.add(buildCondition("INTERACTION", "活跃互动", level.getActivityScore(), interactionCap));
        vo.setNextLevelConditions(conditions);

        return vo;
    }

    private List<CircleMemberGrowth> loadCircleMembers(String circleId) {
        LambdaQueryWrapper<CircleMemberGrowth> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleMemberGrowth::getCircleId, circleId);
        return growthMapper.selectList(qw);
    }

    private int calculateMemberScore(List<CircleMemberGrowth> members) {
        return (int) Math.min(members.size() * 2L, 400);
    }

    private int calculateContentScore(List<CircleMemberGrowth> members) {
        int totalPosts = 0;
        int totalFeatured = 0;
        for (CircleMemberGrowth g : members) {
            totalPosts += g.getPostCount() != null ? g.getPostCount() : 0;
            totalFeatured += g.getFeaturedCount() != null ? g.getFeaturedCount() : 0;
        }
        return (int) Math.min(totalPosts * 2L + totalFeatured * 5L, 300);
    }

    private int calculateActivityScore(List<CircleMemberGrowth> members) {
        int totalComments = 0;
        for (CircleMemberGrowth g : members) {
            totalComments += g.getCommentCount() != null ? g.getCommentCount() : 0;
        }
        return (int) Math.min(totalComments, 300);
    }

    private CircleLevel getOrCreateLevel(String circleId) {
        LambdaQueryWrapper<CircleLevel> qw = new LambdaQueryWrapper<>();
        qw.eq(CircleLevel::getCircleId, circleId);
        CircleLevel level = this.getOne(qw);
        if (level == null) {
            level = new CircleLevel()
                    .setCircleId(circleId)
                    .setLevel(1)
                    .setGrowthScore(0)
                    .setMemberScore(0)
                    .setContentScore(0)
                    .setActivityScore(0);
        }
        return level;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAndUpdateLevel(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        List<CircleMemberGrowth> members = loadCircleMembers(circleId);
        int memberScore = calculateMemberScore(members);
        int contentScore = calculateContentScore(members);
        int activityScore = calculateActivityScore(members);
        int total = Math.min(memberScore + contentScore + activityScore, GrowthConstant.MAX_GROWTH_SCORE);

        level.setMemberScore(memberScore)
             .setContentScore(contentScore)
             .setActivityScore(activityScore)
             .setGrowthScore(total);

        CircleLevelEnum newLevelEnum = CircleLevelEnum.ofScore(total);
        int oldLevel = level.getLevel();
        if (newLevelEnum.getLevel() > oldLevel) {
            level.setLevel(newLevelEnum.getLevel());
            notifyLevelUpgrade(circleId, newLevelEnum);
            log.info("圈子{}等级从L{}提升为L{}", circleId, oldLevel, newLevelEnum.getLevel());
        }
        this.saveOrUpdate(level);
    }

    private List<String> buildBenefits(int level) {
        List<String> all = Arrays.asList("基础展示", "排行榜入口", "徽章墙", "推荐权重提升", "全部权益");
        return new ArrayList<>(all.subList(0, Math.min(level, all.size())));
    }

    private LevelConditionVO buildCondition(String type, String label, Integer current, int cap) {
        LevelConditionVO c = new LevelConditionVO();
        c.setType(type);
        c.setLabel(label);
        c.setCurrent(current != null ? current : 0);
        c.setRequired(cap);
        c.setGap(Math.max(0, cap - (current != null ? current : 0)));
        return c;
    }

    private void notifyLevelUpgrade(String circleId, CircleLevelEnum newLevel) {
        // NOTE: sendNotification 的第一个参数在接口声明中为 userId，此处传入 circleId。
        // ContentNotificationServiceImpl 当前仅将参数写入审计日志 ContentNotificationAuditLog.userId 字段。
        // 实时推送通过 sysBaseAPI.sendWebSocketMsg 向圈子全体成员发送。
        try {
            notificationService.sendNotification(
                    circleId,
                    "CIRCLE_LEVEL_UP",
                    "圈子等级提升",
                    "恭喜！您的圈子已提升至" + newLevel.getName()
            );
        } catch (Exception e) {
            log.warn("发送等级提升通知失败: circleId={}", circleId, e);
        }

        // WebSocket 实时推送给圈子全体成员
        try {
            List<CircleMemberGrowth> members = loadCircleMembers(circleId);
            if (!members.isEmpty()) {
                String[] userIds = members.stream()
                        .map(CircleMemberGrowth::getUserId)
                        .distinct()
                        .toArray(String[]::new);
                com.alibaba.fastjson.JSONObject cmd = new com.alibaba.fastjson.JSONObject();
                cmd.put("type", "CIRCLE_LEVEL_UP");
                cmd.put("circleId", circleId);
                cmd.put("level", newLevel.getLevel());
                cmd.put("levelName", newLevel.getName());
                cmd.put("title", "圈子等级提升");
                cmd.put("content", "恭喜！您的圈子已提升至" + newLevel.getName());
                sysBaseAPI.sendWebSocketMsg(userIds, cmd.toJSONString());
            }
        } catch (Exception e) {
            log.warn("WebSocket推送等级提升通知失败: circleId={}", circleId, e);
        }
    }
}
