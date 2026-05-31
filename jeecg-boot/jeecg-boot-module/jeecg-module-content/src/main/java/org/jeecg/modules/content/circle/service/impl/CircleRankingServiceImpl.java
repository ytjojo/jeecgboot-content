package org.jeecg.modules.content.circle.service.impl;

import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.service.ICircleRankingService;
import org.jeecg.modules.content.circle.vo.CircleRankingVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CircleRankingServiceImpl implements ICircleRankingService {

    private static final String HOT_RANKING_KEY = "circle:ranking:hot";
    private static final String NEW_RANKING_KEY = "circle:ranking:new";
    private static final int CACHE_LIMIT = 100;

    @Resource
    private CircleMapper circleMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public CircleRankingVO getHotRanking(int limit) {
        limit = Math.max(1, Math.min(limit, CACHE_LIMIT));

        List<Circle> circles = (List<Circle>) redisTemplate.opsForValue().get(HOT_RANKING_KEY);
        if (circles == null) {
            circles = circleMapper.selectHotCircles(CACHE_LIMIT);
            redisTemplate.opsForValue().set(HOT_RANKING_KEY, circles, 2, TimeUnit.HOURS);
        }
        // 按请求的limit截取
        List<Circle> result = circles.size() > limit ? circles.subList(0, limit) : circles;
        return buildRankingVO("HOT", result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CircleRankingVO getNewRanking(int limit) {
        limit = Math.max(1, Math.min(limit, CACHE_LIMIT));

        List<Circle> circles = (List<Circle>) redisTemplate.opsForValue().get(NEW_RANKING_KEY);
        if (circles == null) {
            circles = circleMapper.selectNewCircles(CACHE_LIMIT);
            redisTemplate.opsForValue().set(NEW_RANKING_KEY, circles, 2, TimeUnit.HOURS);
        }
        // 按请求的limit截取
        List<Circle> result = circles.size() > limit ? circles.subList(0, limit) : circles;
        return buildRankingVO("NEW", result);
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
