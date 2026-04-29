package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ContentUserStatusEnum {
    GUEST("GUEST", "游客"),
    REGISTERED_INCOMPLETE("REGISTERED_INCOMPLETE", "已注册未完善资料"),
    NORMAL("NORMAL", "正常"),
    MUTED("MUTED", "禁言"),
    RECOMMENDATION_LIMITED("RECOMMENDATION_LIMITED", "限制推荐"),
    FROZEN("FROZEN", "冻结"),
    BANNED("BANNED", "封禁"),
    CANCEL_PENDING("CANCEL_PENDING", "注销中"),
    CANCELLED("CANCELLED", "已注销");

    private final String code;
    private final String description;

    public static List<String> codes() {
        return Arrays.stream(values()).map(ContentUserStatusEnum::getCode).collect(Collectors.toList());
    }
}
