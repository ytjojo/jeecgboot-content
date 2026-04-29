package org.jeecg.modules.content.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for content user audit log.
 */
@Data
@Accessors(chain = true)
public class ContentUserAuditLogVO {

    private String userId;
    private String eventType;
    private String eventContent;
    private String operatorUserId;
    private Date eventTime;
}
