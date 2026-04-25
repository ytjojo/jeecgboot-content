package org.jeecg.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 社群邀请实体类
 * 对应数据库表：community_invitations
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_invitations")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityInvitation", description = "社群邀请")
public class CommunityInvitation extends JeecgEntity {
    
    private static final long serialVersionUID = 1L;
    

    
    /**
     * 社区ID
     */
    @Excel(name = "社区ID", width = 15)
    @Schema(description = "社区ID")
    @TableField("community_id")
    private String communityId;
    
    /**
     * 邀请人ID
     */
    @Excel(name = "邀请人ID", width = 15)
    @Schema(description = "邀请人ID")
    @TableField("inviter_id")
    private String inviterId;
    
    /**
     * 被邀请人ID（可为空，表示公开邀请）
     */
    @Excel(name = "被邀请人ID", width = 15)
    @Schema(description = "被邀请人ID")
    @TableField("invitee_id")
    private String inviteeId;
    
    /**
     * 邀请码
     */
    @Excel(name = "邀请码", width = 20)
    @Schema(description = "邀请码")
    @TableField("invitation_code")
    private String invitationCode;

    /**
     * 邀请消息
     */
    @Excel(name = "邀请消息", width = 50)
    @Schema(description = "邀请消息")
    @TableField("invitation_message")
    private String invitationMessage;


    /**
     * 被邀请人手机号（公开邀请时必填）
     */
    @Excel(name = "被邀请人手机号", width = 15)
    @Schema(description = "被邀请人手机号")
    @TableField("invitee_phone")
    private String inviteePhone;


    /**
     * 邀请状态：1-待接受 2-已接受 3-已拒绝 4-已过期 5-已撤销
     */
    @Excel(name = "邀请状态", width = 15, dicCode = "community_invitation_status")
    @Schema(description = "邀请状态：1-待接受 2-已接受 3-已拒绝 4-已过期 5-已撤销")
    @TableField("status")
    private Integer status;



    /**
     * 过期时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "过期时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间")
    @TableField("expire_time")
    private Date expireTime;

    /**
     * 接受时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "接受时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "接受时间")
    @TableField("accept_time")
    private Date acceptTime;

    /**
     * 拒绝时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "拒绝时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "拒绝时间")
    @TableField("rejected_at")
    private Date rejectedAt;
    
    /**
     * 拒绝原因
     */
    @Excel(name = "拒绝原因", width = 50)
    @Schema(description = "拒绝原因")
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 邀请渠道：1-系统内 2-邮件 3-短信 4-微信 5-QQ 6-其他
     */
    @Excel(name = "邀请渠道", width = 15, dicCode = "invitation_channel")
    @Schema(description = "邀请渠道：1-系统内 2-邮件 3-短信 4-微信 5-QQ 6-其他")
    @TableField("invitation_channel")
    private Integer invitationChannel;


    
    /**
     * 删除标志：0-正常 1-删除
     */
    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志")
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    
}