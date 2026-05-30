package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleContent;

/**
 * 圈子内容置顶与精华服务接口。
 */
public interface ICircleContentPinService extends IService<CircleContent> {

    /**
     * 置顶内容
     *
     * @param contentId 内容ID
     */
    void pinContent(String contentId);

    /**
     * 取消置顶
     *
     * @param contentId 内容ID
     */
    void unpinContent(String contentId);

    /**
     * 设为精华
     *
     * @param contentId 内容ID
     */
    void featureContent(String contentId);

    /**
     * 取消精华
     *
     * @param contentId 内容ID
     */
    void unfeatureContent(String contentId);

    /**
     * 切换置顶状态
     *
     * @param contentId 内容ID
     */
    void togglePin(String contentId);

    /**
     * 切换精华状态
     *
     * @param contentId 内容ID
     */
    void toggleFeature(String contentId);
}
