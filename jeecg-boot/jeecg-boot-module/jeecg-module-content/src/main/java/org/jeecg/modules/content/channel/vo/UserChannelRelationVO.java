package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户频道关系VO")
public class UserChannelRelationVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "是否为频道成员")
    private Boolean isMember;

    @Schema(description = "成员角色: 1=频道主 2=管理员 3=内容编辑 4=普通成员")
    private Integer role;

    @Schema(description = "是否已订阅")
    private Boolean isSubscribed;

    @Schema(description = "是否被禁言")
    private Boolean isMuted;
}
