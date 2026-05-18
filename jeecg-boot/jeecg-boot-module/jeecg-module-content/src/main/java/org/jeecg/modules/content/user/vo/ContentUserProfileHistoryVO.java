package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserProfileHistory;

import java.util.Date;

/**
 * 内容社区资料历史视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区资料历史视图")
public class ContentUserProfileHistoryVO {

    @Schema(description = "历史ID")
    private String id;

    @Schema(description = "历史类型")
    private String historyType;

    @Schema(description = "历史值")
    private String historyValue;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "过期时间")
    private Date expiresAt;

    public static ContentUserProfileHistoryVO from(ContentUserProfileHistory history) {
        return new ContentUserProfileHistoryVO()
            .setId(history.getId())
            .setHistoryType(history.getHistoryType())
            .setHistoryValue(history.getHistoryValue())
            .setCreateTime(history.getCreateTime())
            .setExpiresAt(history.getExpiresAt());
    }
}
