package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserReport;
import org.jeecg.modules.content.user.entity.ContentUserStatusRecord;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;

import java.util.Date;

/**
 * Service contract for growth penalty record creation.
 */
public interface IContentUserGrowthPenaltyRecordService {

    void createFromGovernanceRecord(ContentUserStatusRecord record,
                                    ContentUserStatusChangeReq req,
                                    Date executeTime);

    void createFromReportHandle(ContentUserReport report,
                                ContentReportHandleReq req,
                                String governanceRecordId,
                                Date executeTime);
}
