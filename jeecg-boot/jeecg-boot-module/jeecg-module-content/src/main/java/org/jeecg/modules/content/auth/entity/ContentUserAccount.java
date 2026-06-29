package org.jeecg.modules.content.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 用户认证账号实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_account")
public class ContentUserAccount extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "账号状态")
    private String accountStatus;

    @Schema(description = "注销状态")
    private String cancellationStatus;

    @Schema(description = "注销申请时间")
    private Date cancelApplyTime;

    @Schema(description = "注销完成时间")
    private Date cancelCompleteTime;

    @Schema(description = "最近登录时间")
    private Date lastLoginTime;

    @Schema(description = "最近登录IP")
    private String lastLoginIp;

    @Schema(description = "最近登录地点")
    private String lastLoginLocation;

    @Schema(description = "登录失败次数")
    private Integer loginFailCount;

    @Schema(description = "锁定截止时间")
    private Date lockedUntil;

    @Schema(description = "风险等级 0-100")
    private Integer riskLevel;
}
