package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 内容社区第三方授权视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区第三方授权视图")
public class ContentThirdPartyAuthVO {

    @Schema(description = "授权记录ID")
    private String authId;

    @Schema(description = "第三方应用名称")
    private String appName;

    @Schema(description = "授权时间")
    private Date authTime;

    @Schema(description = "授权范围列表")
    private List<String> scopes;

    @Schema(description = "授权状态: ACTIVE/REVOKED")
    private String status;
}
