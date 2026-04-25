package org.jeecg.modules.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.channel.constant.ChannelStatusEnum;
import org.jeecg.modules.channel.constant.ViewPermissionEnum;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecg.modules.content.constant.PostPermissionEnum;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * 频道实体类
 * 对应数据库表：channels
 * 用于管理频道的基本信息、权限设置、统计数据等
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@TableName(value = "channels", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "频道实体")
public class ChannelEntity extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 频道ID - 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "频道ID")
    private String id;

    /**
     * 频道名称
     */
    @Excel(name = "频道名称", width = 15)
    @Schema(description = "频道名称")
    private String channelName;

    /**
     * 频道描述
     */
    @Excel(name = "频道描述", width = 30)
    @Schema(description = "频道描述")
    private String description;

    /**
     * 频道头像
     */
    @Excel(name = "频道头像", width = 15)
    @Schema(description = "频道头像")
    private String channelAvatar;

    /**
     * 频道封面图
     */
    @Excel(name = "频道封面图", width = 15)
    @Schema(description = "频道封面图")
    private String channelCoverImage;

    /**
     * 频道分类
     */
    @Excel(name = "频道分类", width = 15)
    @Schema(description = "频道分类")
    private String category;

     /**
     * 频道分类ID
     */
    @TableField("category_id")
    @Schema(description = "频道分类ID")
    private String categoryId;

    /**
     * 父频道ID（支持子频道）
     */
    @Schema(description = "父频道ID")
    private String parentChannelId;

    /**
     * 频道创建者ID
     */
    @Excel(name = "创建者ID", width = 15)
    @Schema(description = "频道创建者ID")
    private String ownerId;

    /**
     * 是否公开频道：0-私有 1-公开
     */
    @Excel(name = "是否公开", width = 10, replace = { "私有_0", "公开_1" })
    @Schema(description = "是否公开频道：0-私有 1-公开")
    private Integer isPublic;

    /**
     * 加入方式：1-自由加入 2-申请加入 3-邀请加入
     */
    @Excel(name = "加入方式", width = 10, replace = { "自由加入_1", "申请加入_2", "邀请加入_3" })
    @Schema(description = "加入方式：1-自由加入 2-申请加入 3-邀请加入")
    private JoinTypeEnum joinType;

    /**
     * 发帖权限：1-所有成员 2-管理员 3-指定成员
     */
    @Excel(name = "发帖权限", width = 10, replace = { "任何人_0", "所有成员_1", "版主_2", "管理员_3", "指定成员_4" })
    @Schema(description = "发帖权限：0-任何人 1-所有成员 2-版主 3-管理员 4-指定成员")
    private PostPermissionEnum postPermission;


    /**
     * 查看权限：1-所有成员 2-管理员 3-指定成员
     */
    @Excel(name = "查看权限", width = 10, replace = { "任何人_0 ,所有成员_1", "管理员_2", "指定成员_3" })
    @Schema(description = "查看权限：0-任何人 1-所有成员 2-管理员 3-指定成员")
    private ViewPermissionEnum viewPermission;

    /**
     * 允许的内容类型（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "允许的内容类型")
    private List<String> allowedContentTypes;

    /**
     * 成员数量
     */
    @Excel(name = "成员数量", width = 10)
    @Schema(description = "成员数量")
    private Long memberCount;

    /**
     * 内容数量
     */
    @Excel(name = "内容数量", width = 10)
    @Schema(description = "内容数量")
    private Long contentCount;

    /**
     * 最大成员数限制
     */
    @Schema(description = "最大成员数限制")
    private Integer maxMembers;

    /**
     * 频道状态:  0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3
     * REJECTED-审核拒绝
     */
    @Schema(description = "频道状态: 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝")
    private ChannelStatusEnum status;

    /**
     * 排序权重
     */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /**
     * 是否推荐：0-否 1-是
     */
    @Excel(name = "是否推荐", width = 10, replace = { "否_0", "是_1" })
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommend;

    /**
     * 频道规则
     */
    @Schema(description = "频道规则")
    private String rules;

    /**
     * 频道公告
     */
    @Schema(description = "频道公告")
    private String announcement;

    /**
     * 频道标签（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "频道标签")
    private List<String> tags;

    /**
     * 扩展数据（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "扩展数据")
    private Object extData;

    /**
     * 删除标识：0-正常 1-删除
     */
    @Schema(description = "删除标识")
    @TableLogic()
    private Integer delFlag;

}