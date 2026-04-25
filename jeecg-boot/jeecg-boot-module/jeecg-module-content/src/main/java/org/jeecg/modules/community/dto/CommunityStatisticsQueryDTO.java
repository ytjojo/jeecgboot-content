package org.jeecg.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 社群统计查询DTO
 * 用于查询社群统计数据时的参数传输
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityStatisticsQueryDTO", description = "社群统计查询参数")
public class CommunityStatisticsQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 社群ID列表
     */
    @Schema(description = "社群ID列表")
    private List<String> communityIds;
    
    /**
     * 单个社群ID
     */
    @Schema(description = "社群ID")
    private String communityId;
    
    /**
     * 统计日期开始
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "统计日期开始")
    private Date statisticsDateStart;
    
    /**
     * 统计日期结束
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "统计日期结束")
    private Date statisticsDateEnd;
    
    /**
     * 统计类型（day-按天，week-按周，month-按月，year-按年）
     */
    @Schema(description = "统计类型")
    private String statisticsType;
    
    /**
     * 统计指标列表（memberCount-成员数，postCount-帖子数，commentCount-评论数，likeCount-点赞数，viewCount-浏览量）
     */
    @Schema(description = "统计指标列表")
    private List<String> metrics;
    
    /**
     * 是否包含趋势数据
     */
    @Schema(description = "是否包含趋势数据")
    private Boolean includeTrend;
    
    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String orderBy;
    
    /**
     * 排序方向（asc-升序，desc-降序）
     */
    @Schema(description = "排序方向")
    private String orderDirection;
}