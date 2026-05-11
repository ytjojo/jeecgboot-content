package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.account.ContentAccountBindEmailReq;
import org.jeecg.modules.content.user.req.account.ContentAccountBindMobileReq;
import org.jeecg.modules.content.user.req.account.ContentAccountUnbindEmailReq;
import org.jeecg.modules.content.user.req.account.ContentAccountUnbindMobileReq;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;

/**
 * Service contract for content account.
 */
public interface IContentAccountService {

    String registerByMobile(ContentRegisterReq req);

    String registerByEmail(ContentEmailRegisterReq req);

    void bindMobile(ContentAccountBindMobileReq req);

    void bindEmail(ContentAccountBindEmailReq req);

    void unbindMobile(ContentAccountUnbindMobileReq req);

    void unbindEmail(ContentAccountUnbindEmailReq req);

    void resetPassword(ContentPasswordResetReq req);

    void initiateCancel(String userId, String operatorUserId, String reason);

    void completeCancel(String userId, String operatorUserId);

    void revokeCancel(String userId, String operatorUserId, String reason);
}
