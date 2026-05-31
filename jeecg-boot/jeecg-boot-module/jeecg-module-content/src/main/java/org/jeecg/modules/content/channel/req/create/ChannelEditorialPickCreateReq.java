package org.jeecg.modules.content.channel.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "创建编辑精选请求")
public class ChannelEditorialPickCreateReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "推荐语")
    private String recommendationText;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效开始时间")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效结束时间")
    private Date endTime;

    @NotBlank(message = "操作人ID不能为空")
    @Schema(description = "操作人ID")
    private String operatorId;
}
