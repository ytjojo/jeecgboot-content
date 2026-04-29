package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;

import java.util.List;

/**
 * Service contract for content user governance.
 */
public interface IContentUserGovernanceService {

    void changeStatus(ContentUserStatusChangeReq req);

    boolean canExecuteAction(String userId, String actionType);

    ContentUserStatusVO getCurrentStatus(String userId);

    List<ContentUserDeviceSession> listDeviceSessions(String userId);

    void offlineDeviceSession(String userId, String sessionId);
}
