package org.jeecg.modules.content.user.req.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区用户账号安全设置更新请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户账号安全设置更新请求")
public class ContentUserSecurityUpdateReq {

    @Schema(description = "登录提醒是否开启", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean loginAlertEnabled;
}
