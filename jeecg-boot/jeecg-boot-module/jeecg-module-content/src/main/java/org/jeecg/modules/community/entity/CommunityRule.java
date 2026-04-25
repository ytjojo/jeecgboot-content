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
 * 社群规则实体类
 * 对应数据库表：community_rules
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_rules")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityRule", description = "社群规则")
public class CommunityRule extends JeecgEntity {
    
    private static final long serialVersionUID = 1L;
    

    /**
     * 社区ID
     */
    @Excel(name = "社区ID", width = 15)
    @Schema(description = "社区ID")
    @TableField("community_id")
    private String communityId;
    
    /**
     * 规则类型：1-社区规则 2-发帖规则 3-评论规则 4-行为规范 5-其他
     */
    @Excel(name = "规则类型", width = 15, dicCode = "community_rule_type")
    @Schema(description = "规则类型：1-社区规则 2-发帖规则 3-评论规则 4-行为规范 5-其他")
    @TableField("rule_type")
    private Integer ruleType;
    
    /**
     * 规则标题
     */
    @Excel(name = "规则标题", width = 30)
    @Schema(description = "规则标题")
    @TableField("title")
    private String title;
    
    /**
     * 规则内容
     */
    @Excel(name = "规则内容", width = 50)
    @Schema(description = "规则内容")
    @TableField("content")
    private String content;

    /**
     * 规则级别：1-建议 2-警告 3-强制 4-禁止
     */
    @Excel(name = "规则级别", width = 15, dicCode = "community_rule_level")
    @Schema(description = "规则级别：1-建议 2-警告 3-强制 4-禁止")
    @TableField("rule_level")
    private Integer ruleLevel;

    /**
     * 违规处罚：1-警告 2-禁言 3-踢出 4-封禁 5-其他
     */
    @Excel(name = "违规处罚", width = 15, dicCode = "community_rule_punishment")
    @Schema(description = "违规处罚：1-警告 2-禁言 3-踢出 4-封禁 5-其他")
    @TableField("punishment")
    private Integer punishment;

    /**
     * 处罚时长（分钟，0表示永久）
     */
    @Excel(name = "处罚时长", width = 15)
    @Schema(description = "处罚时长（分钟）")
    @TableField("punishment_duration")
    private Integer punishmentDuration;
    
    /**
     * 规则状态：1-启用 2-禁用
     */
    @Excel(name = "规则状态", width = 15, dicCode = "community_rule_status")
    @Schema(description = "规则状态：0-草稿 1-启用 2-禁用")
    @TableField("status")
    private Integer status;
    
    /**
     * 显示顺序
     */
    @Excel(name = "显示顺序", width = 15)
    @Schema(description = "显示顺序")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否必读：0-否 1-是
     */
    @Excel(name = "是否必读", width = 15, dicCode = "yes_no")
    @Schema(description = "是否必读：0-否 1-是")
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 是否在加入时显示：0-否 1-是
     */
    @Excel(name = "是否在加入时显示", width = 20, dicCode = "yes_no")
    @Schema(description = "是否在加入时显示：0-否 1-是")
    @TableField("show_on_join")
    private Integer showOnJoin;
    
    /**
     * 生效时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "生效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效时间")
    @TableField("effective_at")
    private Date effectiveAt;
    
    /**
     * 失效时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "失效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "失效时间")
    @TableField("expires_at")
    private Date expiresAt;

    /**
     * 规则版本
     */
    @Excel(name = "规则版本", width = 15)
    @Schema(description = "规则版本")
    @TableField("version")
    private String version;

    /**
     * 父规则ID（用于规则层级）
     */
    @Excel(name = "父规则ID", width = 15)
    @Schema(description = "父规则ID")
    @TableField("parent_id")
    private String parentId;

    /**
     * 规则路径（层级路径）
     */
    @Schema(description = "规则路径")
    @TableField("rule_path")
    private String rulePath;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据")
    @TableField("ext_data")
    private String extData;
    
    /**
     * 删除标志：0-正常 1-删除
     */
    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志")
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    
}