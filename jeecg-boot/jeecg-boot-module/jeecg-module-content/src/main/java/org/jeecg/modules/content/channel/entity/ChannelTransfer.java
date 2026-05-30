package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.channel.enums.TransferStatus;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_transfer")
@Schema(description = "频道转让记录表")
public class ChannelTransfer extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "发起转让用户ID")
    private String fromUserId;

    @Schema(description = "目标用户ID")
    private String toUserId;

    @Schema(description = "转让状态")
    private TransferStatus status;

    @Schema(description = "过期时间")
    private Date expireTime;
}
