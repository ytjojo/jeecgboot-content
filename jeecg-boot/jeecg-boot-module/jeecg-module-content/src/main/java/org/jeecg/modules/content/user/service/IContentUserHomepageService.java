package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.profile.ContentUserHomepageUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserHomepageModuleVO;

import java.util.List;

/**
 * 内容社区主页个性化服务契约。
 */
public interface IContentUserHomepageService {

    List<ContentUserHomepageModuleVO> listModules(String userId);

    void updateHomepage(String userId, ContentUserHomepageUpdateReq req);

    void restoreDefaults(String userId);
}
