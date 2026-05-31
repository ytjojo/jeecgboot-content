package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_content_governance_log")
@Schema(description = "频道内容治理日志")
public class ChannelContentGovernanceLog extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "操作者ID")
    private String operatorId;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作详情JSON")
    private String actionDetail;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "操作结果：SUCCESS/FAILED")
    private String result;
}
