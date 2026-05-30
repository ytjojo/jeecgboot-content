package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.circle.mapper.CircleContentMapper;
import org.jeecg.modules.content.circle.service.ICircleContentPinService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 圈子内容置顶与精华服务实现。
 */
@Service
public class CircleContentPinServiceImpl extends ServiceImpl<CircleContentMapper, CircleContent>
        implements ICircleContentPinService {

    @Override
    public void pinContent(String contentId) {
        lambdaUpdate()
                .eq(CircleContent::getId, contentId)
                .set(CircleContent::getIsPinned, true)
                .set(CircleContent::getPinnedAt, new Date())
                .update();
    }

    @Override
    public void unpinContent(String contentId) {
        lambdaUpdate()
                .eq(CircleContent::getId, contentId)
                .set(CircleContent::getIsPinned, false)
                .set(CircleContent::getPinnedAt, null)
                .update();
    }

    @Override
    public void featureContent(String contentId) {
        lambdaUpdate()
                .eq(CircleContent::getId, contentId)
                .set(CircleContent::getIsFeatured, true)
                .set(CircleContent::getFeaturedAt, new Date())
                .update();
    }

    @Override
    public void unfeatureContent(String contentId) {
        lambdaUpdate()
                .eq(CircleContent::getId, contentId)
                .set(CircleContent::getIsFeatured, false)
                .set(CircleContent::getFeaturedAt, null)
                .update();
    }

    @Override
    public void togglePin(String contentId) {
        CircleContent content = getById(contentId);
        if (Boolean.TRUE.equals(content.getIsPinned())) {
            unpinContent(contentId);
        } else {
            pinContent(contentId);
        }
    }

    @Override
    public void toggleFeature(String contentId) {
        CircleContent content = getById(contentId);
        if (Boolean.TRUE.equals(content.getIsFeatured())) {
            unfeatureContent(contentId);
        } else {
            featureContent(contentId);
        }
    }
}
