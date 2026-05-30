package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.Channel;

public interface ChannelService extends IService<Channel> {

    /**
     * 校验名称在用户频道范围内是否唯一
     * @param name 频道名称
     * @param excludeId 排除的频道ID（编辑时排除自身）
     * @return true=唯一, false=已存在
     */
    boolean checkNameUnique(String name, String excludeId);
}
