package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_content_review")
@Schema(description = "频道待审区")
public class ChannelContentReview extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "提交者ID")
    private String submitterId;

    @Schema(description = "审核状态：PENDING/APPROVED/REJECTED")
    private String reviewStatus;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private Date reviewTime;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "来源场景：PUBLISH/ADD_EXISTING/MOVE")
    private String sourceScene;

    @Schema(description = "命中规则")
    private String hitRule;
}
