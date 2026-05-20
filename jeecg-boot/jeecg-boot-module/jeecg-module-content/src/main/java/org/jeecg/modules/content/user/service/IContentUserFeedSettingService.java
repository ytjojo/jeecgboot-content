package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.req.settings.ContentFeedSettingUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserFeedSettingVO;

/**
 * 内容社区关注流设置服务契约。
 */
public interface IContentUserFeedSettingService extends IService<ContentUserFeedSetting> {

    ContentUserFeedSettingVO getSetting(String userId);

    ContentUserFeedSettingVO updateSetting(String userId, ContentFeedSettingUpdateReq req);
}
