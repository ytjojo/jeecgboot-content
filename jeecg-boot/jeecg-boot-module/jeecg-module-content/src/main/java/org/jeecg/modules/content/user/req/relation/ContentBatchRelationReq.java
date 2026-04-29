package org.jeecg.modules.content.user.req.relation;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request model for content batch relation.
 */
@Data
@Accessors(chain = true)
public class ContentBatchRelationReq {

    private List<String> targetUserIds;
    private String relationGroupId;
}
