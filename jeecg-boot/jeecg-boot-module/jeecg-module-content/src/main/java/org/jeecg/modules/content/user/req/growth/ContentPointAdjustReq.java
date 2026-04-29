package org.jeecg.modules.content.user.req.growth;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content point adjust.
 */
@Data
@Accessors(chain = true)
public class ContentPointAdjustReq {

    private String userId;
    private String sourceType;
    private Integer pointDelta;
    private Integer growthDelta;
    private String bizId;
}
