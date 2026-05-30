package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferStatus {

    PENDING(0, "待确认"),
    ACCEPTED(1, "已接受"),
    REJECTED(2, "已拒绝"),
    EXPIRED(3, "已过期");

    @EnumValue
    private final int code;

    @JsonValue
    private final String desc;

    public static TransferStatus fromCode(int code) {
        for (TransferStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知的转让状态: " + code);
    }
}
