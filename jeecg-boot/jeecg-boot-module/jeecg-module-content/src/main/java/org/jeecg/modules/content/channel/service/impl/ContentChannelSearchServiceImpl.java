package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelSearchService;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ContentChannelSearchServiceImpl implements IContentChannelSearchService {

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Override
    public IPage<ChannelSearchResultVO> search(String userId, ChannelSearchQueryReq req) {
        Page<ChannelSearchResultVO> page = new Page<>(req.getPageNo(), req.getPageSize());
        return page;
    }
}
