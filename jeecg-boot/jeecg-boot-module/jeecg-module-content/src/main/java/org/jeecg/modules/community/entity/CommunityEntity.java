package org.jeecg.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.community.enums.CommunityTypeEnum;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecg.modules.content.constant.PostPermissionEnum;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.util.Map;

/**
 * 社群实体类
 * 对应数据库表：content
 * 用于存储社群的基本信息和配置
 * 
 * @author system
 * @since 2024-12-16
 */

@TableName("communities")
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "Community", description = "社群")
public class CommunityEntity extends JeecgEntity {
    private static final long serialVersionUID = 1L;

  

    /**
     * 社区名称
     */
    @Excel(name = "社区名称", width = 15)
    @Schema(description = "社区名称")
    @TableField("name")
    private String name;

    /**
     * 社区描述
     */
    @Excel(name = "社区描述", width = 30)
    @Schema(description = "社区描述")
    @TableField("description")
    private String description;

    /**
     * 社区头像
     */
    @Excel(name = "社区头像", width = 50)
    @Schema(description = "社区头像")
    @TableField("avatar")
    private String avatar;

    /**
     * 社区封面
     */
    @Excel(name = "社区封面", width = 50)
    @Schema(description = "社区封面")
    @TableField("cover_image")
    private String coverImage;

    /**
     * 社区类型：1-公开 2-私密 3-付费
     */
    @Excel(name = "社区类型", width = 15, dicCode = "community_type")
    @Schema(description = "社区类型：1-公开 2-私密 3-付费")
    @TableField("type")
    private CommunityTypeEnum type;

    /**
     * 加入方式：1-自由加入 2-申请加入 3-邀请加入
     */
    @Excel(name = "加入方式", width = 15, dicCode = "community_join_type")
    @Schema(description = "加入方式：1-自由加入 2-申请加入 3-邀请加入")
    @TableField("join_type")
    private JoinTypeEnum joinType;

    /**
     * 发帖权限：1-所有成员 2-管理员 3-指定成员
     */
    @Excel(name = "发帖权限", width = 15, dicCode = "post_permission")
    @Schema(description = "发帖权限：1-所有成员 2-管理员 3-指定成员")
    @TableField("post_permission")
    private PostPermissionEnum postPermission;

    /**
     * 成员数量
     */
    @Excel(name = "成员数量", width = 15)
    @Schema(description = "成员数量")
    @TableField("member_count")
    private Long memberCount;

    
     /**
     * 最大成员数量
     */
    @Excel(name = "最大成员数量", width = 15)
    @Schema(description = "最大成员数量")
    @TableField("max_member_count")
    private Long maxMemberCount;

    /**
     * 内容数量
     */
    @Excel(name = "内容数量", width = 15)
    @Schema(description = "内容数量")
    @TableField("content_count")
    private Long contentCount;

    /**
     * 状态：1-正常 2-禁用 3-审核中
     */
    @Excel(name = "状态", width = 15, dicCode = "community_status")
    @Schema(description = "状态：1-正常 2-禁用 3-审核中")
    @TableField("status")
    private Integer status;


    /**
     * 社区规则
     */
    @Schema(description = "社区规则")
    @TableField("rules")
    private String rules;

    /**
     * 社区公告
     */
    @Schema(description = "社区公告")
    @TableField("announcement")
    private String announcement;

    /**
     * 社区标签（JSON格式）
     */
    @Schema(description = "社区标签")
    @TableField(value  ="tags",typeHandler= JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据")
    @TableField(value ="ext_data",typeHandler= JacksonTypeHandler.class)
    private Map<String, Object> extData;

    /**
     * 邀请码
     */
    @Excel(name = "邀请码", width = 15)
    @Schema(description = "邀请码")
    @TableField("invite_code")
    private String inviteCode;

    /**
     * 删除标志：0-正常 1-删除
     */
    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志")
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

   
}