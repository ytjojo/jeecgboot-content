package org.jeecg.modules.content.user.gateway.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.content.user.gateway.SystemUserAccountGateway;
import org.jeecg.modules.content.user.req.account.ContentEmailRegisterReq;
import org.jeecg.modules.content.user.req.account.ContentPasswordResetReq;
import org.jeecg.modules.content.user.req.account.ContentRegisterReq;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;

/**
 * 平台用户账号网关实现。
 */
@Slf4j
@Component
public class SystemUserAccountGatewayImpl implements SystemUserAccountGateway {

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 创建平台账号并返回账号标识。
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
     * 通过邮箱创建平台账号并返回账号标识。
     */
    @Override
    public String createUserByEmail(ContentEmailRegisterReq req) {
        if (sysUserMapper.getUserByEmail(req.getEmail()) != null) {
            throw new JeecgBootException("邮箱已注册");
        }
        String username = resolveEmailRegisterUsername(req);
        SysUser user = buildUser(username, null, req.getEmail(), req.getPassword(), req.getNickname());
        sysUserMapper.insert(user);
        return user.getId();
    }

    /**
     * 为匹配到的平台账号重置密码。
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
     * 按账号标识查询平台用户。
     */
    @Override
    public SysUser getById(String userId) {
        return sysUserMapper.selectById(userId);
    }

    /**
     * 为指定账号绑定手机号。
     */
    @Override
    public SysUser bindMobile(String userId, String mobile) {
        SysUser existing = sysUserMapper.getUserByPhone(mobile);
        if (existing != null && !userId.equals(existing.getId())) {
            throw new JeecgBootException("手机号已绑定其他账号");
        }
        SysUser user = requireUser(userId);
        user.setPhone(mobile);
        user.setUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return user;
    }

    /**
     * 为指定账号绑定邮箱。
     */
    @Override
    public SysUser bindEmail(String userId, String email) {
        SysUser existing = sysUserMapper.getUserByEmail(email);
        if (existing != null && !userId.equals(existing.getId())) {
            throw new JeecgBootException("邮箱已绑定其他账号");
        }
        SysUser user = requireUser(userId);
        user.setEmail(email);
        user.setUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return user;
    }

    /**
     * 清空指定账号已绑定的手机号。
     */
    @Override
    public SysUser unbindMobile(String userId) {
        SysUser user = requireUser(userId);
        user.setPhone(null);
        user.setUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return user;
    }

    /**
     * 清空指定账号已绑定的邮箱。
     */
    @Override
    public SysUser unbindEmail(String userId) {
        SysUser user = requireUser(userId);
        user.setEmail(null);
        user.setUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return user;
    }

    /**
     * 将平台账号标记为已注销或已停用。
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

    /**
     * 统一构建平台账号，避免手机号注册和邮箱注册重复拼装。
     */
    private SysUser buildUser(String username, String mobile, String email, String password, String nickname) {
        String salt = oConvertUtils.randomGen(8);
        Date now = new Date();
        return new SysUser()
            .setId(UUIDGenerator.generate())
            .setUsername(username)
            .setRealname(nickname)
            .setPhone(mobile)
            .setEmail(email)
            .setSalt(salt)
            .setPassword(PasswordUtil.encrypt(username, password, salt))
            .setAvatar(null)
            .setStatus(1)
            .setDelFlag(CommonConstant.DEL_FLAG_0)
            .setCreateTime(now)
            .setUpdateTime(now)
            .setLastPwdUpdateTime(now);
    }

    /**
     * 邮箱注册默认使用邮箱作为用户名候选，冲突时追加短随机后缀。
     */
    private String resolveEmailRegisterUsername(ContentEmailRegisterReq req) {
        if (oConvertUtils.isNotEmpty(req.getUsername())) {
            if (sysUserMapper.getUserByName(req.getUsername()) != null) {
                throw new JeecgBootException("用户名已存在");
            }
            return req.getUsername();
        }
        String username = req.getEmail();
        if (sysUserMapper.getUserByName(username) == null) {
            return username;
        }
        username = username + "_" + oConvertUtils.randomGen(4);
        if (sysUserMapper.getUserByName(username) != null) {
            throw new JeecgBootException("用户名已存在");
        }
        return username;
    }

    /**
     * 统一校验平台账号是否存在。
     */
    private SysUser requireUser(String userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new JeecgBootException("未找到对应平台账号");
        }
        return user;
    }
}
