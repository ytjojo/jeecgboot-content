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
 * 社群公告阅读记录实体类
 * 对应数据库表：community_announcement_reads
 * 用于记录用户对社群公告的阅读情况
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_announcement_reads")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityAnnouncementRead", description = "社群公告阅读记录")
public class CommunityAnnouncementRead extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @Excel(name = "公告ID", width = 15)
    @Schema(description = "公告ID")
    @TableField("announcement_id")
    private String announcementId;

    /**
     * 用户ID
     */
    @Excel(name = "用户ID", width = 15)
    @Schema(description = "用户ID")
    @TableField("user_id")
    private String userId;

    /**
     * 阅读时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "阅读时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "阅读时间")
    @TableField("read_time")
    private Date readTime;

  

}