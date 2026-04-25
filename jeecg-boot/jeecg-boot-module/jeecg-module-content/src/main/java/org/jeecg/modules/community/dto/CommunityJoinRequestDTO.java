package org.jeecg.modules.community.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
/**
 * 社群加入申请DTO
 * 用于申请加入社群时的数据传输
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityJoinRequestDTO", description = "社群加入申请参数")
public class CommunityJoinRequestDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 社群ID
     */
    @NotBlank(message = "社群ID不能为空")
    @Schema(description = "社群ID", required = true)
    private String communityId;
    
    /**
     * 申请理由
     */
    @Size(max = 500, message = "申请理由长度不能超过500个字符")
    @Schema(description = "申请理由")
    private String reason;



    /**
     * 社群名称（需要关联查询获取）
     */
    @Schema(description = "社群名称")
    private String communityName;
    
    /**
     * 社群头像URL（需要关联查询获取）
     */
    @Schema(description = "社群头像URL")
    private String communityAvatarUrl;
    
    /**
     * 申请人昵称（需要关联查询获取）
     */
    @Schema(description = "申请人昵称")
    private String applicantNickname;
    
    /**
     * 申请人头像URL（需要关联查询获取）
     */
    @Schema(description = "申请人头像URL")
    private String applicantAvatarUrl;
    
    /**
     * 申请状态名称（需要通过枚举转换获取）
     */
    @Schema(description = "申请状态名称")
    private String statusName;
    
    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private String reviewerId;
    
    /**
     * 审核人昵称（需要关联查询获取）
     */
    @Schema(description = "审核人昵称")
    private String reviewerNickname;
    
    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    private Date reviewTime;
    
    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComment;
    
    /**
     * 拒绝原因
     */
    @Schema(description = "拒绝原因")
    private String rejectReason;
    
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

     /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    private String id;
    
    /**
     * 申请人ID
     */
    @Schema(description = "申请人ID")
    private String applicantId;
    
    /**
     * 申请状态（1-待审核 2-已通过 3-已拒绝 4-已撤销）
     */
    @Schema(description = "申请状态")
    private Integer status;
    

    /**
     * 申请时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间")
    private Date applyTime;
}