package org.jeecg.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 社群加入申请实体类
 * 对应数据库表：community_join_requests
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_join_requests")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityJoinRequest", description = "社群加入申请")
public class CommunityJoinRequest extends JeecgEntity {
    
    private static final long serialVersionUID = 1L;
 
    /**
     * 社区ID
     */
    @Excel(name = "社区ID", width = 15)
    @Schema(description = "社区ID")
    @TableField("community_id")
    private String communityId;
    
    /**
     * 申请用户ID
     */
    @Excel(name = "申请用户ID", width = 15)
    @Schema(description = "申请用户ID")
    @TableField("user_id")
    private String userId;
    
    /**
     * 申请消息
     */
    @Excel(name = "申请消息", width = 50)
    @Schema(description = "申请消息")
    @TableField("request_message")
    private String requestMessage;
    
    /**
     * 申请状态（0-待审核 1-已通过 2-已拒绝 3-已取消）/**
     * 申请状态：1-待审核 2-已通过 3-已拒绝 4-已撤销
     */
    @Excel(name = "申请状态", width = 15, dicCode = "community_join_request_status")
    @Schema(description = "申请状态：1-待审核 2-已通过 3-已拒绝 4-已撤销")
    @TableField("status")
    private Integer status; 

    

    
    /**
     * 审核人ID
     */
    @Excel(name = "审核人ID", width = 15)
    @Schema(description = "审核人ID")
    @TableField("reviewer_id")
    private String reviewerId;
    
    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    @TableField("review_time")
    private Date reviewTime;
    
    /**
     * 审核备注
     */
    @Excel(name = "审核备注", width = 50)
    @Schema(description = "审核备注")
    @TableField("review_note")
    private String reviewNote;
    
    /**
     * 申请时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "申请时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间")
    @TableField("create_time")
    private Date createTime;
    
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
     * 申请来源：1-直接申请 2-邀请链接 3-搜索发现 4-推荐
     */
    @Excel(name = "申请来源", width = 15, dicCode = "community_join_source")
    @Schema(description = "申请来源：1-直接申请 2-邀请链接 3-搜索发现 4-推荐")
    @TableField("source")
    private Integer source;

    
    /**
     * 删除标志：0-正常 1-删除
     */
    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志")
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
    
   
}