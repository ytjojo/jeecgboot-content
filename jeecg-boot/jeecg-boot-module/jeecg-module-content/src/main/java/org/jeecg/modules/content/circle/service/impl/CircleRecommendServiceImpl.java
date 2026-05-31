package org.jeecg.modules.content.circle.service.impl;

import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.mapper.CircleRecommendSourceMapper;
import org.jeecg.modules.content.circle.service.ICircleRecommendService;
import org.jeecg.modules.content.circle.vo.CircleRecommendVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CircleRecommendServiceImpl implements ICircleRecommendService {

    @Resource
    private CircleMapper circleMapper;

    @Resource
    private CircleMemberMapper memberMapper;

    @Resource
    private CircleRecommendSourceMapper sourceMapper;

    @Override
    public CircleRecommendVO getRecommendations(String userId, int limit) {
        // 1. 检查用户是否已加入圈子
        List<CircleMember> joinedMembers = memberMapper.selectByUserId(userId);
        List<String> joinedCircleIds = joinedMembers.stream()
                .map(CircleMember::getCircleId)
                .collect(Collectors.toList());

        List<Circle> candidates;
        String sourceType;

        if (joinedCircleIds.isEmpty()) {
            // 新用户：返回热门榜单
            candidates = circleMapper.selectHotCircles(limit);
            sourceType = "HOT";
        } else {
            // 已加入用户：基于兴趣推荐
            candidates = circleMapper.selectRecommendCandidates(userId, limit);
            sourceType = "RECOMMEND";
        }

        // 2. 多样性控制：同一分类占比不超过60%
        candidates = applyDiversityControl(candidates, limit);

        // 3. 构建返回结果并记录来源
        CircleRecommendVO vo = new CircleRecommendVO();
        List<CircleRecommendVO.CircleRecommendItem> items = new ArrayList<>();

        for (Circle circle : candidates) {
            // 记录推荐来源
            CircleRecommendSource source = new CircleRecommendSource();
            source.setCircleId(circle.getId());
            source.setUserId(userId);
            source.setSourceType(sourceType);
            sourceMapper.insert(source);

            // 构建推荐项
            CircleRecommendVO.CircleRecommendItem item = new CircleRecommendVO.CircleRecommendItem();
            item.setCircleId(circle.getId());
            item.setCircleName(circle.getName());
            item.setDescription(circle.getDescription());
            item.setMemberCount(circle.getMemberCount());
            item.setCategory(circle.getCategory());
            item.setPrivacyType(circle.getPrivacyType() != null ? circle.getPrivacyType().name() : null);
            item.setSourceId(source.getId());
            items.add(item);
        }

        vo.setItems(items);
        return vo;
    }

    @Override
    public void recordClick(String sourceId, String userId) {
        sourceMapper.updateClickTime(sourceId);
    }

    @Override
    public void recordJoin(String sourceId, String userId) {
        sourceMapper.updateJoinTime(sourceId);
    }

    private List<Circle> applyDiversityControl(List<Circle> candidates, int limit) {
        if (candidates.size() <= limit) {
            return candidates;
        }

        // 简单实现：按分类分组，每个分类最多占60%
        int maxPerCategory = (int) Math.ceil(limit * 0.6);
        return candidates.stream()
                .collect(Collectors.groupingBy(c -> c.getCategory() != null ? c.getCategory() : "default"))
                .values().stream()
                .flatMap(list -> list.stream().limit(maxPerCategory))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
