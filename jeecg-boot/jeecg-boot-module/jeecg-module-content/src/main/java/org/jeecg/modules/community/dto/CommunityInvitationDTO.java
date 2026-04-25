package org.jeecg.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 社群邀请详情DTO
 * 继承基础DTO，用于邀请详情展示
 * 包含邀请的完整信息和关联数据
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityInvitationDetailDTO", description = "社群邀请详情DTO")
public class CommunityInvitationDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    

      /**
     * 邀请ID
     */
    @Schema(description = "邀请ID")
    private String id;
    
    /**
     * 社群ID
     */
    @Schema(description = "社群ID")
    private String communityId;
    
    /**
     * 邀请人ID
     */
    @Schema(description = "邀请人ID")
    private String inviterId;
    
    /**
     * 被邀请人ID
     */
    @Schema(description = "被邀请人ID")
    private String inviteeId;
    
    /**
     * 邀请状态（1-待处理 2-已接受 3-已拒绝 4-已过期）
     */
    @Schema(description = "邀请状态")
    private Integer status;
    
    /**
     * 邀请消息
     */
    @Schema(description = "邀请消息")
    private String message;
    
    /**
     * 邀请码
     */
    @Schema(description = "邀请码")
    private String inviteCode;
    
    /**
     * 过期时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间")
    private Date expireTime;
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
     * 邀请人昵称（需要关联查询获取）
     */
    @Schema(description = "邀请人昵称")
    private String inviterNickname;
    
    /**
     * 邀请人头像URL（需要关联查询获取）
     */
    @Schema(description = "邀请人头像URL")
    private String inviterAvatarUrl;
    
    /**
     * 被邀请人昵称（需要关联查询获取）
     */
    @Schema(description = "被邀请人昵称")
    private String inviteeNickname;
    
    /**
     * 被邀请人头像URL（需要关联查询获取）
     */
    @Schema(description = "被邀请人头像URL")
    private String inviteeAvatarUrl;
    
    /**
     * 邀请状态名称（需要通过枚举转换获取）
     */
    @Schema(description = "邀请状态名称")
    private String statusName;
    
    /**
     * 处理时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "处理时间")
    private Date processTime;
    
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
     * 邀请有效期（小时）
     */
    @Schema(description = "邀请有效期（小时）")
    private Integer validHours;
    
}