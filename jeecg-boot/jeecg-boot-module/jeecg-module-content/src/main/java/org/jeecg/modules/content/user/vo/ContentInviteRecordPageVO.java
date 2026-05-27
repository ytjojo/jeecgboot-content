package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 内容社区邀请记录分页结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区邀请记录分页结果")
public class ContentInviteRecordPageVO {

    @Schema(description = "当前页")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "邀请记录")
    private List<Item> records;

    /**
     * 单条邀请记录。
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "邀请记录明细")
    public static class Item {

        @Schema(description = "记录ID")
        private String id;

        @Schema(description = "被邀请人用户ID")
        private String inviteeUserId;

        @Schema(description = "邀请码")
        private String inviteCode;

        @Schema(description = "注册时间")
        private Date registeredAt;

        @Schema(description = "奖励积分")
        private Integer rewardPoint;

        @Schema(description = "奖励状态")
        private String rewardStatus;
    }
}
