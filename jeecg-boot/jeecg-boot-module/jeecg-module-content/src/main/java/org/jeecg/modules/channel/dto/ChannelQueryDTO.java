package org.jeecg.modules.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import org.jeecg.modules.channel.constant.ChannelStatusEnum;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 频道查询DTO类
 * 用于频道查询时的参数传输
 * 包含各种查询条件和分页参数
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "频道查询DTO")
public class ChannelQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 频道ID
     */
    @Schema(description = "频道ID")
    private String id;

    /**
     * 频道名称（模糊查询）
     */
    @Schema(description = "频道名称")
    private String name;

    /**
     * 频道分类ID
     */
    @Schema(description = "频道分类ID")
    private String categoryId;

    /**
     * 频道分类ID列表
     */
    @Schema(description = "频道分类ID列表")
    private List<String> categoryIds;

    /**
     * 父频道ID
     */
    @Schema(description = "父频道ID")
    private String parentChannelId;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private String ownerId;

    /**
     * 创建者ID列表
     */
    @Schema(description = "创建者ID列表")
    private List<String> ownerIds;

    /**
     * 是否公开：0-私有 1-公开
     */
    @Schema(description = "是否公开：0-私有 1-公开")
    private Integer isPublic;

    /**
     * 加入方式：1-自由加入 2-申请加入 3-邀请加入
     */
    @Schema(description = "加入方式：1-自由加入 2-申请加入 3-邀请加入")
    private JoinTypeEnum joinType;

    /**
     * 频道状态: -1 DELETED 频道删除, 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3
     * REJECTED-审核拒绝
     */
    @Schema(description = "频道状态: -1 DELETED 频道删除, 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝")
    private ChannelStatusEnum status;

    /**
     * 频道状态列表
     */
    @Schema(description = "频道状态列表")
    private List<Integer> statusList;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommend;

    /**
     * 最小成员数
     */
    @Schema(description = "最小成员数")
    private Long minMemberCount;

    /**
     * 最大成员数
     */
    @Schema(description = "最大成员数")
    private Long maxMemberCount;

    /**
     * 最小内容数
     */
    @Schema(description = "最小内容数")
    private Long minContentCount;

    /**
     * 最大内容数
     */
    @Schema(description = "最大内容数")
    private Long maxContentCount;

    /**
     * 频道标签（模糊查询）
     */
    @Schema(description = "频道标签")
    private String tag;

    /**
     * 频道标签列表
     */
    @Schema(description = "频道标签列表")
    private List<String> tags;

    /**
     * 关键词搜索（名称、描述、标签）
     */
    @Schema(description = "关键词搜索")
    private String keyword;

    /**
     * 创建时间开始
     */
    @Schema(description = "创建时间开始")
    private LocalDateTime createTimeStart;

    /**
     * 创建时间结束
     */
    @Schema(description = "创建时间结束")
    private LocalDateTime createTimeEnd;

    /**
     * 更新时间开始
     */
    @Schema(description = "更新时间开始")
    private LocalDateTime updateTimeStart;

    /**
     * 更新时间结束
     */
    @Schema(description = "更新时间结束")
    private LocalDateTime updateTimeEnd;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String orderBy;

    /**
     * 排序方向：asc-升序 desc-降序
     */
    @Schema(description = "排序方向")
    private String orderDirection;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer pageNo;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize;
}