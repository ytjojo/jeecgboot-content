package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.mapper.CircleRecommendSourceMapper;
import org.jeecg.modules.content.circle.service.ICircleRecommendService;
import org.jeecg.modules.content.circle.vo.CircleRecommendVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public CircleRecommendVO getRecommendations(String userId, int limit) {
        // 1. 检查用户是否已加入圈子
        List<String> joinedCircleIds = memberMapper.selectCircleIdsByUserId(userId);

        List<Circle> candidates;
        String sourceType;

        // 查询更多候选用于多样性控制
        int queryLimit = limit * 2;
        if (joinedCircleIds.isEmpty()) {
            candidates = circleMapper.selectHotCircles(queryLimit);
            sourceType = "HOT";
        } else {
            candidates = circleMapper.selectRecommendCandidates(userId, queryLimit);
            sourceType = "RECOMMEND";
        }

        // 2. 多样性控制：同一分类占比不超过60%
        candidates = applyDiversityControl(candidates, limit);

        // 3. 构建返回结果并记录来源
        CircleRecommendVO vo = new CircleRecommendVO();
        List<CircleRecommendVO.CircleRecommendItem> items = new ArrayList<>();
        List<CircleRecommendSource> sources = new ArrayList<>();

        for (Circle circle : candidates) {
            CircleRecommendSource source = new CircleRecommendSource();
            source.setId(IdWorker.getIdStr());
            source.setCircleId(circle.getId());
            source.setUserId(userId);
            source.setSourceType(sourceType);
            sources.add(source);

            CircleRecommendVO.CircleRecommendItem item = new CircleRecommendVO.CircleRecommendItem();
            item.setCircleId(circle.getId());
            item.setCircleName(circle.getName());
            item.setDescription(circle.getDescription());
            item.setMemberCount(circle.getMemberCount());
            item.setCategory(circle.getCategory());
            item.setPrivacyType(circle.getPrivacyType() != null ? circle.getPrivacyType().name() : null);
            items.add(item);
        }

        // 批量插入推荐来源记录
        sourceMapper.insertBatch(sources);

        // 设置sourceId
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSourceId(sources.get(i).getId());
        }

        vo.setItems(items);
        return vo;
    }

    @Override
    public void recordClick(String sourceId, String userId) {
        sourceMapper.updateClickTime(sourceId, userId);
    }

    @Override
    public void recordJoin(String sourceId, String userId) {
        sourceMapper.updateJoinTime(sourceId, userId);
    }

    @Override
    public void recordExposure(String sourceId, String userId) {
        sourceMapper.updateExposureTime(sourceId, userId);
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
