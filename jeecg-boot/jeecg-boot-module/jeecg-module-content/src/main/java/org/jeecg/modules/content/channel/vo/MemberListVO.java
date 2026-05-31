package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "成员列表响应")
public class MemberListVO {

    @Schema(description = "成员列表")
    private List<MemberVO> members;

    @Schema(description = "总数")
    private Long total;
}
