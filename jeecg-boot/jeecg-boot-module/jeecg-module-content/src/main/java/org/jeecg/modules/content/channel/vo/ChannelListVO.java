package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "频道列表VO")
public class ChannelListVO {

    @Schema(description = "频道ID")
    private String id;

    @Schema(description = "频道名称")
    private String name;

    @Schema(description = "频道图标URL")
    private String iconUrl;

    @Schema(description = "频道类型")
    private Integer channelType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "隐私设置")
    private Integer privacy;

    @Schema(description = "创建时间")
    private Date createTime;
}
