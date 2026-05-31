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
import java.util.function.IntFunction;

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
    public CircleRankingVO getHotRanking(int limit) {
        return getRanking(HOT_RANKING_KEY, "HOT", limit, circleMapper::selectHotCircles);
    }

    @Override
    public CircleRankingVO getNewRanking(int limit) {
        return getRanking(NEW_RANKING_KEY, "NEW", limit, circleMapper::selectNewCircles);
    }

    @SuppressWarnings("unchecked")
    private CircleRankingVO getRanking(String redisKey, String type, int limit,
                                        IntFunction<List<Circle>> loader) {
        List<Circle> circles = (List<Circle>) redisTemplate.opsForValue().get(redisKey);
        if (circles == null) {
            circles = loader.apply(CACHE_LIMIT);
            redisTemplate.opsForValue().set(redisKey, circles, 2, TimeUnit.HOURS);
        }
        List<Circle> result = circles.size() > limit ? circles.subList(0, limit) : circles;
        return buildRankingVO(type, result);
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
