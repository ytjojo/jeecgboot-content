package org.jeecg.modules.content.user.req.subscription;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ContentSubscriptionReq {

    private String sourceType;
    private String sourceId;
    private String sourceName;
    private String notificationChannels;
    private String notificationFrequency;
}
