package org.jeecg.modules.content.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * View object for content user growth.
 */
@Data
@Accessors(chain = true)
public class ContentUserGrowthVO {

    private String userId;
    private Integer pointBalance;
    private Integer growthValue;
    private Integer level;
}
