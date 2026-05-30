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
@TableName("content_channel_invite")
@Schema(description = "频道邀请表")
public class ChannelInvite extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "邀请码")
    private String code;

    @Schema(description = "类型: 1=邀请码 2=邀请链接")
    private Integer type;

    @Schema(description = "最大使用次数，NULL表示不限")
    private Integer maxUses;

    @Schema(description = "已使用次数")
    private Integer usedCount;

    @Schema(description = "过期时间，NULL表示不过期")
    private Date expireTime;

    @Schema(description = "状态: 1=有效 2=已用完 3=已撤销 4=已过期")
    private Integer status;

    @Schema(description = "创建人ID")
    private String creatorId;
}
