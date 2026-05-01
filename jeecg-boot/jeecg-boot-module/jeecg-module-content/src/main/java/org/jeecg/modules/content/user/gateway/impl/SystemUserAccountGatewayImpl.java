package org.jeecg.modules.content.user.gateway.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;

/**
 * Gateway for system user account.
 */
@Slf4j
@Component
public class SystemUserAccountGatewayImpl implements SystemUserAccountGateway {

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * Creates a platform user account and returns the account identifier.
     */
    @Override
    public String createUser(ContentRegisterReq req) {
        if (req.getMobile() != null && sysUserMapper.getUserByPhone(req.getMobile()) != null) {
            throw new JeecgBootException("手机号已注册");
        }
        if (req.getEmail() != null && sysUserMapper.getUserByEmail(req.getEmail()) != null) {
            throw new JeecgBootException("邮箱已注册");
        }
        String username = oConvertUtils.isEmpty(req.getUsername()) ? req.getMobile() : req.getUsername();
        if (sysUserMapper.getUserByName(username) != null) {
            throw new JeecgBootException("用户名已存在");
        }
        String salt = oConvertUtils.randomGen(8);
        Date now = new Date();
        SysUser user = new SysUser()
            .setId(UUIDGenerator.generate())
            .setUsername(username)
            .setRealname(req.getNickname())
            .setPhone(req.getMobile())
            .setEmail(req.getEmail())
            .setSalt(salt)
            .setPassword(PasswordUtil.encrypt(username, req.getPassword(), salt))
            .setAvatar(null)
            .setStatus(1)
            .setDelFlag(CommonConstant.DEL_FLAG_0)
            .setCreateTime(now)
            .setUpdateTime(now)
            .setLastPwdUpdateTime(now);
        sysUserMapper.insert(user);
        return user.getId();
    }

    /**
     * Resets the account password for the matched platform user.
     */
    @Override
    public void resetPassword(ContentPasswordResetReq req) {
        SysUser user = resolveUser(req);
        if (user == null) {
            throw new JeecgBootException("未找到对应平台账号");
        }
        String salt = oConvertUtils.randomGen(8);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(user.getUsername(), req.getNewPassword(), salt));
        user.setLastPwdUpdateTime(new Date());
        sysUserMapper.updateById(user);
    }

    /**
     * Gets the platform user by identifier.
     */
    @Override
    public SysUser getById(String userId) {
        return sysUserMapper.selectById(userId);
    }

    /**
     * Marks the platform account as cancelled or disabled.
     */
    @Override
    public void markCancelled(String userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new JeecgBootException("未找到对应平台账号");
        }
        user.setStatus(2);
        user.setUpdateTime(new Date());
        sysUserMapper.updateById(user);
    }

    private SysUser resolveUser(ContentPasswordResetReq req) {
        if (oConvertUtils.isNotEmpty(req.getUserId())) {
            return sysUserMapper.selectById(req.getUserId());
        }
        if (oConvertUtils.isNotEmpty(req.getMobile())) {
            return sysUserMapper.getUserByPhone(req.getMobile());
        }
        if (oConvertUtils.isNotEmpty(req.getEmail())) {
            return sysUserMapper.getUserByEmail(req.getEmail());
        }
        return null;
    }
}
