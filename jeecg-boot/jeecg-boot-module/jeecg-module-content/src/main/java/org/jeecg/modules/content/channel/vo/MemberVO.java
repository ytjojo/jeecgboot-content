package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "成员详情")
public class MemberVO {

    @Schema(description = "成员ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "角色: 1=频道主, 2=管理员, 3=内容编辑, 4=普通成员")
    private Integer role;

    @Schema(description = "角色描述")
    private String roleDesc;

    @Schema(description = "是否禁言")
    private Boolean muted;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;
}
