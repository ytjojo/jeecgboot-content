package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_privacy_setting")
@Schema(description = "内容社区用户隐私设置")
public class ContentUserPrivacySetting extends JeecgEntity {

    private String userId;
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
