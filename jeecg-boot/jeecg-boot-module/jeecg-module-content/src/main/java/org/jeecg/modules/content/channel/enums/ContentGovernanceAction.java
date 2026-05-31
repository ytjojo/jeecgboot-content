package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentGovernanceAction {

    PIN("PIN", "置顶"),
    UNPIN("UNPIN", "取消置顶"),
    FEATURE("FEATURE", "精选"),
    UNFEATURE("UNFEATURE", "取消精选"),
    DELETE("DELETE", "删除"),
    RESTORE("RESTORE", "恢复"),
    MOVE("MOVE", "移出频道"),
    EDIT_ASSIST("EDIT_ASSIST", "编辑协助");

    private final String code;
    private final String desc;
}
