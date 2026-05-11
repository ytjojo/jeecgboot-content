package org.jeecg.modules.content.user.gateway;

import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.system.entity.SysUser;

/**
 * Gateway for system user account.
 */
public interface SystemUserAccountGateway {
    
    String createUser(ContentRegisterReq req);

    String createUserByEmail(ContentEmailRegisterReq req);

    void resetPassword(ContentPasswordResetReq req);

    SysUser getById(String userId);

    SysUser bindMobile(String userId, String mobile);

    SysUser bindEmail(String userId, String email);

    SysUser unbindMobile(String userId);

    SysUser unbindEmail(String userId);

    void markCancelled(String userId);
}
