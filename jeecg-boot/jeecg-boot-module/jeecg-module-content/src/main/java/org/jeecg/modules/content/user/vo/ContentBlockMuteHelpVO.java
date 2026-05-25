package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 拉黑/屏蔽操作确认文案和帮助说明响应。
 */
@Data
@Accessors(chain = true)
@Schema(description = "拉黑/屏蔽操作确认文案和帮助说明")
public class ContentBlockMuteHelpVO {

    @Schema(description = "拉黑确认文案")
    private String blockConfirmation;

    @Schema(description = "屏蔽确认文案")
    private String muteConfirmation;

    @Schema(description = "解除拉黑确认文案")
    private String unblockConfirmation;

    @Schema(description = "拉黑与屏蔽对比帮助说明")
    private String blockVsMuteComparison;
}
