package org.jeecg.modules.content.auth.service;

/**
 * IP地理位置解析端口。
 */
public interface IpGeolocationPort {

    /**
     * 解析IP地址的地理位置。
     *
     * @param ip IP地址
     * @return 地理位置信息(如"北京市 电信")，解析失败返回null
     */
    String resolve(String ip);
}
