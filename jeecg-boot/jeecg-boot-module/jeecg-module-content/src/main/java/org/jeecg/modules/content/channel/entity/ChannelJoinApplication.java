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
@TableName("content_channel_join_application")
@Schema(description = "加入申请表")
public class ChannelJoinApplication extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "申请理由")
    private String reason;

    @Schema(description = "状态: 1=待审核 2=已批准 3=已拒绝")
    private Integer status;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private Date reviewTime;

    @Schema(description = "审核理由")
    private String reviewReason;
}
