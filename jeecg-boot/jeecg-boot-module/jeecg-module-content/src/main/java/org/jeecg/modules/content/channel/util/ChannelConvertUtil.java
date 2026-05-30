package org.jeecg.modules.content.channel.util;

import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.vo.ChannelVO;
import org.springframework.beans.BeanUtils;

public final class ChannelConvertUtil {

    private ChannelConvertUtil() {}

    public static ChannelVO toVO(Channel channel) {
        ChannelVO vo = new ChannelVO();
        BeanUtils.copyProperties(channel, vo);
        return vo;
    }
}
