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
@TableName("content_channel_member")
@Schema(description = "频道成员表")
public class ChannelMember extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色: 1=频道主 2=管理员 3=内容编辑 4=普通成员")
    private Integer role;

    @Schema(description = "加入时间")
    private Date joinTime;

    @Schema(description = "冷却期结束时间")
    private Date coolingEndTime;
}
