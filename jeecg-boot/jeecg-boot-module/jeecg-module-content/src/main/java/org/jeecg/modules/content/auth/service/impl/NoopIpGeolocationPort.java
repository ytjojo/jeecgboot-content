package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.service.IpGeolocationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * IP地理位置解析端口的空实现，未配置实际解析服务时返回null。
 */
@Slf4j
@Component
@ConditionalOnMissingBean(IpGeolocationPort.class)
public class NoopIpGeolocationPort implements IpGeolocationPort {

    @Override
    public String resolve(String ip) {
        log.warn("[NoopIpGeolocationPort] IP地理解析未配置实际服务, ip={}", ip);
        return null;
    }
}
