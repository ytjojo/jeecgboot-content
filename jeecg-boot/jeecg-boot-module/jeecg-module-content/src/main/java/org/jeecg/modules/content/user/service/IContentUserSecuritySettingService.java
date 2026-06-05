package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.settings.ContentUserSecurityUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserSecuritySettingVO;

/**
 * 用户账号安全设置服务接口。
 */
public interface IContentUserSecuritySettingService {

    /**
     * 查询用户账号安全设置。
     *
     * @param userId 用户ID
     * @return 安全设置视图
     */
    ContentUserSecuritySettingVO getSecuritySetting(String userId);

    /**
     * 更新用户账号安全设置。
     *
     * @param userId 用户ID
     * @param req    更新请求
     * @return 更新后的安全设置视图
     */
    ContentUserSecuritySettingVO updateSecuritySetting(String userId, ContentUserSecurityUpdateReq req);
}
