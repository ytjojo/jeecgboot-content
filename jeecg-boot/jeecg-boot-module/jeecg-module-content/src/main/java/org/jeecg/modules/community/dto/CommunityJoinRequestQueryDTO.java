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
 * 社群加入申请查询DTO
 * 用于社群加入申请列表查询和条件筛选
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityJoinRequestQueryDTO", description = "社群加入申请查询参数")
public class CommunityJoinRequestQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 社群ID
     */
    @Schema(description = "社群ID")
    private String communityId;
    
    /**
     * 申请人ID
     */
    @Schema(description = "申请人ID")
    private String applicantId;
    
    /**
     * 申请人昵称（模糊查询）
     */
    @Schema(description = "申请人昵称")
    private String applicantNickname;
    
    /**
     * 申请状态列表（1-待审核 2-已通过 3-已拒绝 4-已撤销）
     */
    @Schema(description = "申请状态列表")
    private List<Integer> statusList;
    
    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private String reviewerId;
    
    /**
     * 申请时间开始
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间开始")
    private Date applyTimeStart;
    
    /**
     * 申请时间结束
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间结束")
    private Date applyTimeEnd;
    
    /**
     * 审核时间开始
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间开始")
    private Date reviewTimeStart;
    
    /**
     * 审核时间结束
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间结束")
    private Date reviewTimeEnd;
    
    /**
     * 申请来源（web-网页 mobile-移动端 api-API调用）
     */
    @Schema(description = "申请来源")
    private String source;
    
    /**
     * 是否即将过期（用于查询即将过期的申请）
     */
    @Schema(description = "是否即将过期")
    private Boolean expiringSoon;
    
    /**
     * 过期时间阈值（小时数，用于查询即将过期的申请）
     */
    @Schema(description = "过期时间阈值（小时）")
    private Integer expirationHours;
}