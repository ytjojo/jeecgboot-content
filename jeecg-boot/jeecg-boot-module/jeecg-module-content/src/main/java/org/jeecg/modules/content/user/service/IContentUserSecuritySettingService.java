package org.jeecg.modules.content.user.service;

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
}
