package org.jeecg.modules.content.user.req.support;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ContentAppealCreateReq {

    private String userId;
    private String appealType;
    private String targetId;
    private String targetType;
    private String reason;
    private String evidenceJson;
}
