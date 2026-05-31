package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("circle_recommend_source")
@Schema(description = "圈子推荐来源追踪")
public class CircleRecommendSource {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "来源类型：RECOMMEND-推荐, HOT-热门榜单, NEW-新增榜单")
    private String sourceType;

    @Schema(description = "来源ID")
    private String sourceId;

    @Schema(description = "点击时间")
    private LocalDateTime clickTime;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
