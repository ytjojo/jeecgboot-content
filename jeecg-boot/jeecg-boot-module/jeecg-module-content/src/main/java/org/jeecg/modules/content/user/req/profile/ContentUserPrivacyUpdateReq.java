package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户隐私更新请求")
public class ContentUserPrivacyUpdateReq {

    private String birthdayVisibility;
    private String genderVisibility;
    private String regionVisibility;
    private String professionVisibility;
    private String homepageVisibility;
    private String dynamicVisibility;
    private Boolean onlineStatusVisible;
    private Boolean allowSearchEngineIndex;
    private Boolean allowUserSearch;
}
