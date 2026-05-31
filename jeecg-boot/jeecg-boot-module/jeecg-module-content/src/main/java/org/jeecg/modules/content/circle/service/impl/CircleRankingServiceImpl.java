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

    @Resource
    private CircleMapper circleMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public CircleRankingVO getHotRanking(int limit) {
        // limit 上限限制
        limit = Math.min(limit, 100);

        // 先读 Redis 缓存
        List<Circle> circles = (List<Circle>) redisTemplate.opsForValue().get(HOT_RANKING_KEY);
        if (circles == null) {
            // 缓存未命中，查数据库
            circles = circleMapper.selectHotCircles(limit);
            redisTemplate.opsForValue().set(HOT_RANKING_KEY, circles, 2, TimeUnit.HOURS);
        }
        return buildRankingVO("HOT", circles);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CircleRankingVO getNewRanking(int limit) {
        // limit 上限限制
        limit = Math.min(limit, 100);

        // 先读 Redis 缓存
        List<Circle> circles = (List<Circle>) redisTemplate.opsForValue().get(NEW_RANKING_KEY);
        if (circles == null) {
            // 缓存未命中，查数据库
            circles = circleMapper.selectNewCircles(limit);
            redisTemplate.opsForValue().set(NEW_RANKING_KEY, circles, 2, TimeUnit.HOURS);
        }
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
