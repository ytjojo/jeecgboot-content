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
@TableName("channel_recycle_bin")
@Schema(description = "频道回收站")
public class ChannelRecycleBin extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "原作者ID")
    private String originalAuthorId;

    @Schema(description = "删除人ID")
    private String deletedBy;

    @Schema(description = "删除时间")
    private Date deleteTime;

    @Schema(description = "删除原因")
    private String deleteReason;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "是否已恢复")
    private Boolean isRestored;

    @Schema(description = "恢复人ID")
    private String restoredBy;

    @Schema(description = "恢复时间")
    private Date restoreTime;
}
