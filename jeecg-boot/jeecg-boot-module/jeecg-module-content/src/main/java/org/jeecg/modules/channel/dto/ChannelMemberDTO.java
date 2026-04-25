package org.jeecg.modules.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.jeecg.modules.channel.constant.MemberRoleEnum;
import org.jeecg.modules.channel.constant.ChannelConstant;
import org.jeecg.modules.channel.constant.MemberJoinStatusEnum;
import org.jeecg.modules.channel.entity.ChannelMemberEntity;
import org.springframework.beans.BeanUtils;

/**
 * 频道成员数据传输对象
 * 合并了ChannelMemberBaseDTO和ChannelMemberDetailDTO的所有功能
 * 用于频道成员相关的数据传输，包含成员的完整信息
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "频道成员数据传输对象")
public class ChannelMemberDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 基础字段（来自ChannelMemberBaseDTO） ==========
    public ChannelMemberDTO() {

        this.role = MemberRoleEnum.GUEST;
        this.muteType = ChannelConstant.MuteType.UNMUTE;

    }

    /**
     * 成员关系ID
     */
    @Schema(description = "成员关系ID")
    private String id;

    /**
     * 频道ID
     */
    @Schema(description = "频道ID")
    private String channelId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 成员角色：0-访客 1-普通成员 2-版主 3-管理员 4-拥有者
     */
    @Schema(description = "成员角色：0-访客 1-普通成员 2-版主 3-管理员 4-拥有者")
    private MemberRoleEnum role;

    /**
     * 成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝 5-邀请中
     */
    @Schema(description = "成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝 5-邀请中")
    private MemberJoinStatusEnum status;

    /**
     * 加入时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    // ========== 详情字段（来自ChannelMemberDetailDTO） ==========

    /**
     * 特殊权限设置
     */
    @Schema(description = "特殊权限设置")
    private Object permissions;

    // ========== 用户相关字段 ==========

    /**
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String userEmail;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    private String userPhone;

    // ========== 频道相关字段 ==========

    /**
     * 频道名称
     */
    @Schema(description = "频道名称")
    private String channelName;

    /**
     * 频道头像
     */
    @Schema(description = "频道头像")
    private String channelAvatar;

    // ========== 邀请相关字段 ==========

    /**
     * 邀请人ID
     */
    @Schema(description = "邀请人ID")
    private String inviterId;

    /**
     * 邀请人名称
     */
    @Schema(description = "邀请人名称")
    private String inviterName;

    /**
     * 禁言原因
     * 当权限类型为禁言时，记录禁言的具体原因
     */
    @Schema(description = "禁言原因")
    private String muteReason;

    /**
     * 禁言操作人ID
     * 记录执行禁言操作的管理员或系统用户ID
     */
    @Schema(description = "禁言操作人ID")
    private String muteOperatorId;

    /**
     * 禁言开始时间
     * 禁言生效的具体时间点，可以设置延迟生效
     */
    @Schema(description = "禁言开始时间")
    private LocalDateTime muteStartTime;

    /**
     * 禁言结束时间
     * 禁言解除的时间点，null表示永久禁言
     */
    @Schema(description = "禁言结束时间，null表示永久禁言")
    private LocalDateTime muteEndTime;

    /**
     * 是否为永久禁言
     * true-永久禁言，false-临时禁言
     */
    @Schema(description = "是否为永久禁言")
    private Boolean isPermanentMute;

    /**
     * 禁言类型
     * 0-未禁言 1-发言禁言 2-评论禁言 3-全局禁言（发言+评论）
     */
    @Schema(description = "禁言类型：0-未禁言 1-发言禁言 2-评论禁言 3-全局禁言")
    private Integer muteType;

    // ========== 系统字段 ==========

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
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
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public static ChannelMemberDTO fromEntity(ChannelMemberEntity entity) {
        if (entity != null) {
            ChannelMemberDTO dto = new ChannelMemberDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }
        return null;
    }

    public ChannelMemberEntity toEntity() {
        ChannelMemberEntity entity = new ChannelMemberEntity();
        BeanUtils.copyProperties(this, entity);
        return entity;
    }
}