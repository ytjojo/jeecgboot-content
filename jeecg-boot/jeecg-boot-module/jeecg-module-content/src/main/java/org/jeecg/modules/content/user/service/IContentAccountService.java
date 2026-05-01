package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;

/**
 * Service contract for content account.
 */
public interface IContentAccountService {

    String registerByMobile(ContentRegisterReq req);

    void resetPassword(ContentPasswordResetReq req);

    void initiateCancel(String userId, String operatorUserId, String reason);

    void completeCancel(String userId, String operatorUserId);

    void revokeCancel(String userId, String operatorUserId, String reason);
}
