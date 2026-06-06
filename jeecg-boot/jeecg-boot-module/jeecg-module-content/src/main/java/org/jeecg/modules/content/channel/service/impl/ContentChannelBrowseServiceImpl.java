package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.mapper.ContentChannelCategoryMapper;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelBrowseService;
import org.jeecg.modules.content.channel.vo.ChannelBrowseItemVO;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionSourceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ContentChannelBrowseServiceImpl extends ServiceImpl<ChannelMapper, Channel>
        implements IContentChannelBrowseService {

    @Resource
    private ContentSubscriptionSourceMapper subscriptionSourceMapper;

    @Resource
    private ContentChannelCategoryMapper categoryMapper;

    @Override
    public IPage<ChannelBrowseItemVO> browseByCategory(ChannelBrowseQueryReq req) {
        // 1. 构建查询条件：状态为 ACTIVE，隐私为公开
        LambdaQueryWrapper<Channel> wrapper = Wrappers.<Channel>lambdaQuery()
                .eq(Channel::getStatus, ChannelStatus.ACTIVE)
                .eq(Channel::getPrivacy, 1);

        // 按分类筛选
        if (req.getCategoryId() != null && !req.getCategoryId().isEmpty()) {
            wrapper.eq(Channel::getCategoryId, req.getCategoryId());
        }

        // 按频道类型筛选
        ChannelType channelType = parseChannelType(req.getChannelType());
        if (channelType != null) {
            wrapper.eq(Channel::getChannelType, channelType);
        }

        // 按创建时间排序（如果选择 CREATE_TIME）
        if ("CREATE_TIME".equals(req.getSortBy())) {
            wrapper.orderByDesc(Channel::getCreateTime);
        }

        // 2. 分页查询频道
        Page<Channel> page = new Page<>(req.getPageNo(), req.getPageSize());
        IPage<Channel> channelPage = baseMapper.selectPage(page, wrapper);

        if (channelPage.getRecords().isEmpty()) {
            Page<ChannelBrowseItemVO> emptyPage = new Page<>(req.getPageNo(), req.getPageSize(), 0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        // 3. 获取频道ID列表
        List<String> channelIds = channelPage.getRecords().stream()
                .map(Channel::getId)
                .collect(Collectors.toList());

        // 4. 查询订阅源信息
        Map<String, ContentSubscriptionSource> subscriptionMap = querySubscriptionSources(channelIds);

        // 5. 查询分类名称
        Map<String, String> categoryNameMap = queryCategoryNames(channelPage.getRecords());

        // 6. 转换为 VO
        List<ChannelBrowseItemVO> voList = channelPage.getRecords().stream()
                .map(channel -> convertToVO(channel, subscriptionMap, categoryNameMap))
                .collect(Collectors.toList());

        // 7. 排序（非 CREATE_TIME 排序）
        if (!"CREATE_TIME".equals(req.getSortBy())) {
            sortVoList(voList, req.getSortBy());
        }

        // 8. 构建返回分页对象
        Page<ChannelBrowseItemVO> resultPage = new Page<>(req.getPageNo(), req.getPageSize(),
                channelPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    private Map<String, ContentSubscriptionSource> querySubscriptionSources(List<String> channelIds) {
        if (channelIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<ContentSubscriptionSource> wrapper = Wrappers.<ContentSubscriptionSource>lambdaQuery()
                .eq(ContentSubscriptionSource::getSourceType, "CHANNEL")
                .in(ContentSubscriptionSource::getSourceId, channelIds);
        List<ContentSubscriptionSource> sources = subscriptionSourceMapper.selectList(wrapper);
        return sources.stream()
                .collect(Collectors.toMap(ContentSubscriptionSource::getSourceId, Function.identity(),
                        (existing, replacement) -> existing));
    }

    private Map<String, String> queryCategoryNames(List<Channel> channels) {
        Set<String> categoryIds = channels.stream()
                .map(Channel::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<ContentChannelCategory> wrapper = Wrappers.<ContentChannelCategory>lambdaQuery()
                .in(ContentChannelCategory::getId, categoryIds);
        List<ContentChannelCategory> categories = categoryMapper.selectList(wrapper);
        return categories.stream()
                .collect(Collectors.toMap(ContentChannelCategory::getId, ContentChannelCategory::getName,
                        (existing, replacement) -> existing));
    }

    private ChannelBrowseItemVO convertToVO(Channel channel,
                                            Map<String, ContentSubscriptionSource> subscriptionMap,
                                            Map<String, String> categoryNameMap) {
        ChannelBrowseItemVO vo = new ChannelBrowseItemVO();
        vo.setChannelId(channel.getId());
        vo.setChannelName(channel.getName());
        vo.setChannelIcon(channel.getIconUrl());
        vo.setChannelType(channel.getChannelType() != null ? channel.getChannelType().name() : null);
        vo.setDescription(channel.getDescription());
        vo.setCategoryName(categoryNameMap.get(channel.getCategoryId()));

        // 从订阅源获取订阅数
        ContentSubscriptionSource source = subscriptionMap.get(channel.getId());
        vo.setSubscriberCount(source != null && source.getSubscriberCount() != null
                ? source.getSubscriberCount().longValue() : 0L);
        return vo;
    }

    private void sortVoList(List<ChannelBrowseItemVO> voList, String sortBy) {
        Comparator<ChannelBrowseItemVO> comparator;
        if ("ACTIVITY".equals(sortBy)) {
            // ACTIVITY 排序需要热度分，但 VO 中没有此字段，降级为订阅数排序
            comparator = Comparator.comparing(ChannelBrowseItemVO::getSubscriberCount, Comparator.reverseOrder());
        } else {
            // 默认按订阅数排序
            comparator = Comparator.comparing(ChannelBrowseItemVO::getSubscriberCount, Comparator.reverseOrder());
        }
        voList.sort(comparator);
    }

    private ChannelType parseChannelType(String channelType) {
        if (channelType == null || channelType.isEmpty()) {
            return null;
        }
        try {
            return ChannelType.valueOf(channelType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
