package org.jeecg.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * 社群公告实体类
 * 对应数据库表：community_announcements
 * 用于存储社群公告信息
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_announcements")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityAnnouncement", description = "社群公告")
public class CommunityAnnouncement extends JeecgEntity{
    private static final long serialVersionUID = 1L;

    /**
     * 社区ID
     */
    @Excel(name = "社区ID", width = 15)
    @Schema(description = "社区ID")
    @TableField("community_id")
    private String communityId;

    /**
     * 公告标题
     */
    @Excel(name = "公告标题", width = 30)
    @Schema(description = "公告标题")
    @TableField("title")
    private String title;

    /**
     * 公告内容
     */
    @Excel(name = "公告内容", width = 50)
    @Schema(description = "公告内容")
    @TableField("content")
    private String content;

    /**
     * 公告类型：1-普通公告 2-重要公告 3-紧急公告
     */
    @Excel(name = "公告类型", width = 15, dicCode = "community_announcement_type")
    @Schema(description = "公告类型：1-普通公告 2-重要公告 3-紧急公告")
    @TableField("type")
    private Integer type;

    /**
     * 是否置顶：0-否 1-是
     */
    @Excel(name = "是否置顶", width = 15, dicCode = "yes_no")
    @Schema(description = "是否置顶：0-否 1-是")
    @TableField("is_pinned")
    private Integer isPinned;

    /**
     * 发布状态：1-草稿 2-已发布 3-已撤回
     */
    @Excel(name = "发布状态", width = 15, dicCode = "community_announcement_status")
    @Schema(description = "发布状态：1-草稿 2-已发布 3-已撤回")
    @TableField("status")
    private Integer status;

    /**
     * 发布时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间")
    @TableField("publish_time")
    private Date publishTime;

   

    /**
     * 失效时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "失效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "失效时间")
    @TableField("expire_time")
    private Date expireTime;

    

    /**
     * 排序号
     */
    @Excel(name = "排序号", width = 15)
    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

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



    /**
     * 阅读次数
     */
    @Schema(description = "阅读次数")
    @TableField("read_count")
    private Long readCount;

    /**
     * 发布人ID
     */
    @Schema(description = "发布人ID")
    @TableField("publisher_id")
    private String publisherId;
}