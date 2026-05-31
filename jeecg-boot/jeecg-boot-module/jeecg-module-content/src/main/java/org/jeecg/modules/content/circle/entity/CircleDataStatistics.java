package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_data_statistics")
@Schema(description = "圈子数据统计")
public class CircleDataStatistics extends JeecgEntity {
    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "统计日期")
    private LocalDate statDate;

    @Schema(description = "成员总数")
    private Integer memberCount;

    @Schema(description = "新增成员数")
    private Integer newMemberCount;

    @Schema(description = "帖子总数")
    private Integer postCount;

    @Schema(description = "新增帖子数")
    private Integer newPostCount;

    @Schema(description = "活跃用户数")
    private Integer activeCount;
}
