package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentServiceSessionQueryReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentChangelogVO;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentHelpSearchResultVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionPageVO;
import org.jeecg.modules.content.user.vo.ContentServiceSessionVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;

import java.util.List;

/**
 * Service contract for content user support.
 */
public interface IContentUserSupportService {

    String createAppeal(ContentAppealCreateReq req);

    ContentUserAppealProgressVO getAppealProgress(String userId, String appealId);

    ContentUserAppealPageVO listAppeals(String userId, Long pageNo, Long pageSize);

    String createReport(ContentReportCreateReq req);

    ContentHelpCenterVO getHelpCenter(String userId);

    List<ContentHelpSearchResultVO> searchHelpArticles(String userId, String keyword);

    ContentCustomerServiceVO getCustomerServiceEntry(String userId);

    String handleAppeal(ContentAppealHandleReq req);

    String handleReport(ContentReportHandleReq req);

    ContentUserReportProgressVO getReportProgress(String userId, String reportId);

    ContentUserReportPageVO listReportsForUser(String userId, Long pageNo, Long pageSize);

    ContentUserReportDetailVO getReportDetailForUser(String userId, String reportId);

    ContentUserAppealDetailVO getAppealDetail(String userId, String appealId);

    ContentUserReportAdminPageVO listReportsForAdmin(ContentUserReportAdminQueryReq req);

    ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId);

    ContentServiceSessionPageVO listServiceSessions(ContentServiceSessionQueryReq req);

    String createServiceSession(String userId, String sessionType);

    String rateService(String userId, String sessionId, Integer rating, String comment);

    List<ContentChangelogVO> getChangelog(String userId);

    String withdrawReport(String userId, String reportId);

    String withdrawAppeal(String userId, String appealId);

    List<ContentHelpCenterEntryVO> getHelpCategories(String userId);

    ContentHelpSearchResultVO getHelpArticleDetail(String userId, String articleId);

    String submitArticleFeedback(String userId, String articleId, Boolean helpful);
}
