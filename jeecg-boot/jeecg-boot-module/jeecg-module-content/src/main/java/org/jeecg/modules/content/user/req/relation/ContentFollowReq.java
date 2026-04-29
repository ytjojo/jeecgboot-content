package org.jeecg.modules.content.user.req.relation;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Request model for content follow.
 */
@Data
@Accessors(chain = true)
public class ContentFollowReq {

    private String targetUserId;
    private String relationGroupId;
}
