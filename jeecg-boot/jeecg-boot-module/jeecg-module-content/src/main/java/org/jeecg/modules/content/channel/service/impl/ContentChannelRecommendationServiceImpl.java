package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ContentChannelNotInterested;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;
import org.jeecg.modules.content.channel.mapper.ContentChannelNotInterestedMapper;
import org.jeecg.modules.content.channel.mapper.ContentChannelRecommendationCacheMapper;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelRecommendationServiceImpl
        extends ServiceImpl<ContentChannelRecommendationCacheMapper, ContentChannelRecommendationCache>
        implements IContentChannelRecommendationService {

    @Resource
    private ContentChannelNotInterestedMapper notInterestedMapper;

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Override
    public IPage<ChannelRecommendationVO> getRecommendations(String userId, ChannelRecommendationQueryReq req) {
        List<String> notInterestedChannelIds = getNotInterestedChannelIds(userId);

        Page<ContentChannelRecommendationCache> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<ContentChannelRecommendationCache> wrapper = Wrappers.<ContentChannelRecommendationCache>lambdaQuery()
                .eq(ContentChannelRecommendationCache::getUserId, userId)
                .eq(ContentChannelRecommendationCache::getRecommendationStatus, 1)
                .notIn(!notInterestedChannelIds.isEmpty(),
                        ContentChannelRecommendationCache::getChannelId, notInterestedChannelIds)
                .orderByDesc(ContentChannelRecommendationCache::getRankingScore);

        IPage<ContentChannelRecommendationCache> cachePage = page(page, wrapper);

        return cachePage.convert(cache -> {
            ChannelRecommendationVO vo = new ChannelRecommendationVO();
            vo.setChannelId(cache.getChannelId());
            vo.setRecommendationReason(cache.getRecommendationReason());
            vo.setRankingScore(cache.getRankingScore());
            return vo;
        });
    }

    @Override
    public void markNotInterested(String userId, String channelId) {
        // 检查是否已存在，避免唯一约束冲突
        ContentChannelNotInterested existing = notInterestedMapper.selectOne(
                Wrappers.<ContentChannelNotInterested>lambdaQuery()
                        .eq(ContentChannelNotInterested::getUserId, userId)
                        .eq(ContentChannelNotInterested::getChannelId, channelId));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);

        if (existing != null) {
            // 已存在则更新过期时间
            existing.setExpireTime(cal.getTime());
            notInterestedMapper.updateById(existing);
            return;
        }

        ContentChannelNotInterested ni = new ContentChannelNotInterested();
        ni.setUserId(userId);
        ni.setChannelId(channelId);
        ni.setExpireTime(cal.getTime());
        notInterestedMapper.insert(ni);
    }

    @Override
    public IPage<ChannelRecommendationVO> getColdStartRecommendations(ChannelRecommendationQueryReq req) {
        Page<ChannelRecommendationVO> page = new Page<>(req.getPageNo(), req.getPageSize());
        return page;
    }

    private List<String> getNotInterestedChannelIds(String userId) {
        return notInterestedMapper.selectList(
                Wrappers.<ContentChannelNotInterested>lambdaQuery()
                        .eq(ContentChannelNotInterested::getUserId, userId)
                        .gt(ContentChannelNotInterested::getExpireTime, new Date()))
                .stream()
                .map(ContentChannelNotInterested::getChannelId)
                .collect(Collectors.toList());
    }
}
