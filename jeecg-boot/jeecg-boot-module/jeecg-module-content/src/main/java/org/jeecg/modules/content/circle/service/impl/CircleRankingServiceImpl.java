package org.jeecg.modules.content.circle.service.impl;

import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.service.ICircleRankingService;
import org.jeecg.modules.content.circle.vo.CircleRankingVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CircleRankingServiceImpl implements ICircleRankingService {

    @Resource
    private CircleMapper circleMapper;

    @Override
    public CircleRankingVO getHotRanking(int limit) {
        List<Circle> circles = circleMapper.selectHotCircles(limit);
        return buildRankingVO("HOT", circles);
    }

    @Override
    public CircleRankingVO getNewRanking(int limit) {
        List<Circle> circles = circleMapper.selectNewCircles(limit);
        return buildRankingVO("NEW", circles);
    }

    private CircleRankingVO buildRankingVO(String type, List<Circle> circles) {
        CircleRankingVO vo = new CircleRankingVO();
        vo.setType(type);

        List<CircleRankingVO.CircleRankingItem> items = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            CircleRankingVO.CircleRankingItem item = new CircleRankingVO.CircleRankingItem();
            item.setRank(i + 1);
            item.setCircleId(circle.getId());
            item.setCircleName(circle.getName());
            item.setDescription(circle.getDescription());
            item.setMemberCount(circle.getMemberCount());
            item.setCategory(circle.getCategory());
            item.setCreateTime(circle.getCreateTime() != null ? circle.getCreateTime().toString() : null);
            items.add(item);
        }

        vo.setItems(items);
        return vo;
    }
}
