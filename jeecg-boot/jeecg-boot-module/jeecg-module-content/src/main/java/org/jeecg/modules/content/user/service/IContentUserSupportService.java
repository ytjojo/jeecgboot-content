package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;

/**
 * Service contract for content user support.
 */
public interface IContentUserSupportService {

    String createAppeal(ContentAppealCreateReq req);
}
