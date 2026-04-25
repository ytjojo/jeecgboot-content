package org.jeecg.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 社群成员查询DTO
 * 用于社群成员列表查询和条件筛选
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityMemberQueryDTO", description = "社群成员查询参数")
public class CommunityMemberQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 社群ID
     */
    @NotBlank(message = "社群ID不能为空")
    @Schema(description = "社群ID", required = true)
    private String communityId;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String userId;
    
    /**
     * 成员昵称（模糊查询）
     */
    @Schema(description = "成员昵称")
    private String nickname;
    
    /**
     * 成员角色列表（1-普通成员 2-版主 3-管理员 4-创建者）
     */
    @Schema(description = "成员角色列表")
    private List<Integer> roleList;
    
    /**
     * 成员状态列表（1-正常 2-禁言 3-已退出）
     */
    @Schema(description = "成员状态列表")
    private List<Integer> statusList;
    
    /**
     * 加入时间开始
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "加入时间开始")
    private Date joinTimeStart;
    
    /**
     * 加入时间结束
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "加入时间结束")
    private Date joinTimeEnd;
    
    /**
     * 最后活跃时间开始
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后活跃时间开始")
    private Date lastActiveTimeStart;
    
    /**
     * 最后活跃时间结束
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后活跃时间结束")
    private Date lastActiveTimeEnd;
    
    /**
     * 最小积分
     */
    @Schema(description = "最小积分")
    private Integer minPoints;
    
    /**
     * 最大积分
     */
    @Schema(description = "最大积分")
    private Integer maxPoints;
    
    /**
     * 排序字段（joinTime-加入时间，lastActiveTime-最后活跃时间，points-积分，postCount-发帖数）
     */
    @Schema(description = "排序字段")
    private String orderBy;
    
    /**
     * 排序方向（asc-升序，desc-降序）
     */
    @Schema(description = "排序方向")
    private String orderDirection;
}