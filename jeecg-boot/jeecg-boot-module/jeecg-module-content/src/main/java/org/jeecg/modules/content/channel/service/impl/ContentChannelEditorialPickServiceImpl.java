package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.mapper.ContentChannelEditorialPickMapper;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelEditorialPickServiceImpl
        extends ServiceImpl<ContentChannelEditorialPickMapper, ContentChannelEditorialPick>
        implements IContentChannelEditorialPickService {

    @Override
    public ContentChannelEditorialPick createPick(ChannelEditorialPickCreateReq req) {
        ContentChannelEditorialPick pick = new ContentChannelEditorialPick();
        pick.setChannelId(req.getChannelId());
        pick.setRecommendationText(req.getRecommendationText());
        pick.setStartTime(req.getStartTime());
        pick.setEndTime(req.getEndTime());
        pick.setStatus(1);
        pick.setOperatorId(req.getOperatorId());
        save(pick);
        return pick;
    }

    @Override
    public void updatePick(ChannelEditorialPickUpdateReq req) {
        ContentChannelEditorialPick pick = getById(req.getId());
        if (pick == null) {
            throw new JeecgBootException("精选记录不存在");
        }
        if (req.getRecommendationText() != null) {
            pick.setRecommendationText(req.getRecommendationText());
        }
        if (req.getEndTime() != null) {
            pick.setEndTime(req.getEndTime());
        }
        if (req.getStatus() != null) {
            pick.setStatus(req.getStatus());
        }
        updateById(pick);
    }

    @Override
    public void removePick(String pickId) {
        ContentChannelEditorialPick pick = getById(pickId);
        if (pick == null) {
            throw new JeecgBootException("精选记录不存在");
        }
        pick.setStatus(0);
        updateById(pick);
    }

    @Override
    public List<ChannelEditorialPickVO> listActivePicks() {
        Date now = new Date();
        return list(Wrappers.<ContentChannelEditorialPick>lambdaQuery()
                .eq(ContentChannelEditorialPick::getStatus, 1)
                .le(ContentChannelEditorialPick::getStartTime, now)
                .and(w -> w.isNull(ContentChannelEditorialPick::getEndTime)
                        .or()
                        .ge(ContentChannelEditorialPick::getEndTime, now)))
                .stream()
                .map(p -> {
                    ChannelEditorialPickVO vo = new ChannelEditorialPickVO();
                    vo.setId(p.getId());
                    vo.setChannelId(p.getChannelId());
                    vo.setRecommendationText(p.getRecommendationText());
                    vo.setStartTime(p.getStartTime());
                    vo.setEndTime(p.getEndTime());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
