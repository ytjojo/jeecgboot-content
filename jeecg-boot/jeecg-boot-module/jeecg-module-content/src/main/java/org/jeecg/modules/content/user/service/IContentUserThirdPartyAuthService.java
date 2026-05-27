package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthVO;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthorizationDetailVO;

import java.util.List;

/**
 * 内容社区用户第三方授权服务。
 */
public interface IContentUserThirdPartyAuthService {

    /**
     * 查询指定用户的所有活跃授权列表。
     */
    List<ContentThirdPartyAuthVO> listActiveAuths(String userId);

    /**
     * 撤销指定授权。撤销后 token hash 清除，状态标记为 REVOKED。
     *
     * @return 撤销是否成功
     */
    boolean revokeAuth(String userId, String authId);

    /**
     * 查询指定授权的详情。
     *
     * @return 授权详情，不存在时返回 null
     */
    ContentThirdPartyAuthorizationDetailVO getAuthDetail(String userId, String authId);
}
