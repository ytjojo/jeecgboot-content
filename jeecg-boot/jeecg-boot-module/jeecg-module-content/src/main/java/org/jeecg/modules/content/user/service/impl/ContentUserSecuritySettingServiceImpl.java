package org.jeecg.modules.content.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.user.service.IContentUserSecuritySettingService;
import org.jeecg.modules.content.user.vo.ContentUserSecuritySettingVO;
import org.springframework.stereotype.Service;

/**
 * 用户账号安全设置服务实现。
 * 当前阶段返回默认值，后续 EPIC-01 中接入真实的设备管理/密码/两步验证服务。
 */
@Slf4j
@Service
public class ContentUserSecuritySettingServiceImpl implements IContentUserSecuritySettingService {

    @Override
    public ContentUserSecuritySettingVO getSecuritySetting(String userId) {
        ContentUserSecuritySettingVO vo = new ContentUserSecuritySettingVO();
        try {
            // 后续接入真实设备管理服务
            vo.setDeviceManagementEnabled(true);
        } catch (Exception e) {
            log.warn("查询设备管理设置异常，使用默认值: userId={}", userId, e);
            vo.setDeviceManagementEnabled(true);
        }
        try {
            // 后续接入真实密码策略服务
            vo.setPasswordChangeEnabled(true);
        } catch (Exception e) {
            log.warn("查询密码修改设置异常，使用默认值: userId={}", userId, e);
            vo.setPasswordChangeEnabled(true);
        }
        try {
            // 后续接入真实两步验证服务
            vo.setTwoFactorEnabled(false);
        } catch (Exception e) {
            log.warn("查询两步验证设置异常，使用默认值: userId={}", userId, e);
            vo.setTwoFactorEnabled(false);
        }
        try {
            // 后续接入真实登录提醒服务
            vo.setLoginAlertEnabled(true);
        } catch (Exception e) {
            log.warn("查询登录提醒设置异常，使用默认值: userId={}", userId, e);
            vo.setLoginAlertEnabled(true);
        }
        return vo;
    }
}
