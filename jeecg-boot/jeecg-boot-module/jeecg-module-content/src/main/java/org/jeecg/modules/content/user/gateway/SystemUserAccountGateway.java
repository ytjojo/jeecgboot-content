package org.jeecg.modules.content.user.gateway;

import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.system.entity.SysUser;

/**
 * Gateway for system user account.
 */
public interface SystemUserAccountGateway {

    String createUser(ContentRegisterReq req);

    void resetPassword(ContentPasswordResetReq req);

    SysUser getById(String userId);

    void markCancelled(String userId);
}
