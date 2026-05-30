package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel")
@Schema(description = "频道表")
public class Channel extends JeecgEntity {

    @Schema(description = "频道名称")
    private String name;

    @Schema(description = "频道简介")
    private String description;

    @Schema(description = "频道图标URL")
    private String iconUrl;

    @Schema(description = "频道封面URL")
    private String coverUrl;

    @Schema(description = "频道类型: 1=system, 2=personal, 3=organization")
    private ChannelType channelType;

    @Schema(description = "状态: 0=Draft, 1=PendingReview, 2=Active, 3=Rejected, 4=DeleteCooling, 5=Deleted")
    private ChannelStatus status;

    @Schema(description = "隐私设置: 1=公开, 2=私有")
    private Integer privacy;

    @Schema(description = "加入方式: 1=自由加入 2=审核加入 3=邀请加入")
    private Integer joinMethod;

    @Schema(description = "归属分类ID")
    private String categoryId;

    @Schema(description = "频道主用户ID")
    private String ownerId;

    @Schema(description = "组织ID(组织频道必填)")
    private String organizationId;

    @Schema(description = "置顶权重(系统频道)")
    private Integer pinWeight;

    @Schema(description = "冷静期结束时间")
    private Date deleteCoolingEndTime;
}
