package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "圈子榜单VO")
public class CircleRankingVO {
    @Schema(description = "榜单类型：HOT-热门, NEW-新增")
    private String type;

    @Schema(description = "榜单圈子列表")
    private List<CircleRankingItem> items;

    @Data
    @Schema(description = "榜单圈子项")
    public static class CircleRankingItem {
        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "圈子ID")
        private String circleId;

        @Schema(description = "圈子名称")
        private String circleName;

        @Schema(description = "圈子简介")
        private String description;

        @Schema(description = "成员数")
        private Integer memberCount;

        @Schema(description = "分类")
        private String category;

        @Schema(description = "创建时间")
        private String createTime;
    }
}
