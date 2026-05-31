package org.jeecg.modules.content.user.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 圈子成员成长记录实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_member_growth")
@Schema(description = "圈子成员成长记录")
public class CircleMemberGrowth extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "经验值")
    private Integer expPoints;

    @Schema(description = "贡献值")
    private Integer contributionPoints;

    @Schema(description = "成员等级")
    private Integer level;

    @Schema(description = "发帖数")
    private Integer postCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "精华数")
    private Integer featuredCount;
}
