package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "圈子推荐VO")
public class CircleRecommendVO {
    @Schema(description = "推荐圈子列表")
    private List<CircleRecommendItem> items;

    @Data
    @Schema(description = "推荐圈子项")
    public static class CircleRecommendItem {
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

        @Schema(description = "公开/私有")
        private String privacyType;

        @Schema(description = "推荐来源追踪ID")
        private String sourceId;
    }
}
