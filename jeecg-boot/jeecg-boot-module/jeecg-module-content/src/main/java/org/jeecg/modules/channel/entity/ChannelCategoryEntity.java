package org.jeecg.modules.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 频道分类实体类
 * 对应数据库表：channel_categories
 * 用于管理频道的分类信息
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@TableName("channel_categories")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "频道分类实体")
public class ChannelCategoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID - 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    private String id;

    /**
     * 分类名称
     */
    @Excel(name = "分类名称", width = 20)
    @Schema(description = "分类名称")
    private String name;

    /**
     * 分类描述
     */
    @Excel(name = "分类描述", width = 30)
    @Schema(description = "分类描述")
    private String description;

    /**
     * 父分类ID
     */
    @Excel(name = "父分类ID", width = 15)
    @Schema(description = "父分类ID")
    private String parentId;

    /**
     * 分类图标
     */
    @Excel(name = "分类图标", width = 30)
    @Schema(description = "分类图标")
    private String icon;

    /**
     * 分类颜色
     */
    @Excel(name = "分类颜色", width = 10)
    @Schema(description = "分类颜色")
    private String color;

    /**
     * 排序权重
     */
    @Excel(name = "排序权重", width = 10)
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /**
     * 分类状态：0-禁用 1-启用
     */
    @Excel(name = "分类状态", width = 10, replace = {"禁用_0", "启用_1"})
    @Schema(description = "分类状态：0-禁用 1-启用")
    private Integer status;

    /**
     * 是否显示：0-隐藏 1-显示
     */
    @Excel(name = "分类层级", width = 10, replace = {"隐藏_0", "显示_1"})
    @Schema(description = "分类层级")
    private Integer level;

    /**
     * 频道数量
     */
    @Excel(name = "频道数量", width = 10)
    @Schema(description = "频道数量")
    private Integer channelCount;

    /**
     * 删除标识：0-正常 1-已删除
     */
    @TableLogic
    @Schema(description = "删除标识")
    private Integer delFlag;

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
    private LocalDateTime createTime;

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
    private LocalDateTime updateTime;

}