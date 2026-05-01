package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;

import java.util.List;

/**
 * Service contract for content user support.
 */
public interface IContentUserSupportService {

    String createAppeal(ContentAppealCreateReq req);

    ContentUserAppealProgressVO getAppealProgress(String userId, String appealId);

    List<ContentUserAppealProgressVO> listAppeals(String userId);

    String createReport(ContentReportCreateReq req);

    ContentHelpCenterVO getHelpCenter();

    ContentCustomerServiceVO getCustomerServiceEntry(String userId);

    String handleAppeal(ContentAppealHandleReq req);

    String handleReport(ContentReportHandleReq req);

    ContentUserReportProgressVO getReportProgress(String userId, String reportId);

    ContentUserReportAdminPageVO listReportsForAdmin(ContentUserReportAdminQueryReq req);

    ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId);
}
