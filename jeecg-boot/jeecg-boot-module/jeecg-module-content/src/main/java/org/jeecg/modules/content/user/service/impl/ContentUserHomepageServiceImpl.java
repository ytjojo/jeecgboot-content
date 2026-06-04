package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserHomepageModule;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserHomepageModuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageModuleReq;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserHomepageService;
import org.jeecg.modules.content.user.service.IContentUserMediaAdapter;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.vo.ContentUserHomepageModuleVO;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 内容社区主页个性化服务实现。
 */
@Service
public class ContentUserHomepageServiceImpl implements IContentUserHomepageService {

    private static final Set<String> SUPPORTED_MODULES = Set.of("POSTS", "COLLECTIONS", "BADGES", "ABOUT");
    private static final String DEFAULT_THEME = "#1677ff";

    @Resource
    private ContentUserHomepageModuleMapper homepageModuleMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private IContentUserMediaAdapter mediaAdapter;

    @Lazy
    @Resource
    private IContentUserProfileService profileService;

    @Override
    public List<ContentUserHomepageModuleVO> listModules(String userId) {
        return homepageModuleMapper.selectByUserId(userId).stream().map(ContentUserHomepageModuleVO::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserProfileVO updateHomepage(String userId, ContentUserHomepageUpdateReq req) {
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在");
        }
        mediaAdapter.validateHomepageBackground(req.getHomepageBackground());
        profile.setHomepageBackground(req.getHomepageBackground());
        profile.setThemeColor(normalizeThemeColor(req.getThemeColor()));
        profileMapper.updateById(profile);
        if (req.getModules() != null) {
            saveModules(userId, req.getModules());
        }
        return profileService.getProfile(userId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserProfileVO restoreDefaults(String userId) {
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile != null) {
            profile.setHomepageBackground(null);
            profile.setThemeColor(DEFAULT_THEME);
            profileMapper.updateById(profile);
        }
        homepageModuleMapper.delete(Wrappers.<ContentUserHomepageModule>lambdaQuery()
            .eq(ContentUserHomepageModule::getUserId, userId));
        return profileService.getProfile(userId, userId);
    }

    private void saveModules(String userId, List<ContentUserHomepageModuleReq> modules) {
        if (modules.isEmpty()) {
            throw new JeecgBootException("主页模块不能为空");
        }
        Set<String> moduleKeys = new HashSet<>();
        Set<Integer> sortOrders = new HashSet<>();
        boolean hasVisible = false;
        for (ContentUserHomepageModuleReq item : modules) {
            if (!SUPPORTED_MODULES.contains(item.getModuleKey())) {
                throw new JeecgBootException("未知主页模块：" + item.getModuleKey());
            }
            if (!moduleKeys.add(item.getModuleKey())) {
                throw new JeecgBootException("主页模块不能重复");
            }
            if (item.getSortOrder() == null || item.getSortOrder() < 0 || !sortOrders.add(item.getSortOrder())) {
                throw new JeecgBootException("主页模块排序不合法");
            }
            hasVisible = hasVisible || Boolean.TRUE.equals(item.getVisible());
        }
        if (!hasVisible) {
            throw new JeecgBootException("至少保留一个主页模块");
        }
        homepageModuleMapper.delete(Wrappers.<ContentUserHomepageModule>lambdaQuery()
            .eq(ContentUserHomepageModule::getUserId, userId));
        for (ContentUserHomepageModuleReq item : modules) {
            ContentUserHomepageModule module = new ContentUserHomepageModule()
                .setUserId(userId)
                .setModuleKey(item.getModuleKey())
                .setModuleName(item.getModuleKey())
                .setVisible(item.getVisible())
                .setSortOrder(item.getSortOrder());
            module.setId(UUIDGenerator.generate());
            homepageModuleMapper.insert(module);
        }
    }

    private String normalizeThemeColor(String themeColor) {
        if (themeColor == null) {
            return DEFAULT_THEME;
        }
        if (!themeColor.matches("^#[0-9a-fA-F]{6}$")) {
            throw new JeecgBootException("主题色格式不合法");
        }
        return themeColor;
    }
}
