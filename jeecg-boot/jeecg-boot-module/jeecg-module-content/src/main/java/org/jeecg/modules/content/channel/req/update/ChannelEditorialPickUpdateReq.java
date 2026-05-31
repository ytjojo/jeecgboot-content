package org.jeecg.modules.content.channel.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "更新编辑精选请求")
public class ChannelEditorialPickUpdateReq {

    @NotBlank(message = "精选ID不能为空")
    @Schema(description = "精选ID")
    private String id;

    @Schema(description = "推荐语")
    private String recommendationText;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效结束时间")
    private Date endTime;

    @Schema(description = "状态 0=下线 1=上线")
    private Integer status;
}
