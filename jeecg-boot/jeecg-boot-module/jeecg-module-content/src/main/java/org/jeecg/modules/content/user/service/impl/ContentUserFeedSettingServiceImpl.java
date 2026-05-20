package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.req.settings.ContentFeedSettingUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserFeedSettingService;
import org.jeecg.modules.content.user.vo.ContentUserFeedSettingVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 内容社区关注流设置服务实现。
 */
@Service
public class ContentUserFeedSettingServiceImpl
    extends ServiceImpl<ContentUserFeedSettingMapper, ContentUserFeedSetting>
    implements IContentUserFeedSettingService {

    private static final int USER_ID_MAX_LENGTH = 64;
    private static final Set<String> SUPPORTED_TYPES = Set.of("PUBLISH", "LIKE", "FAVORITE");

    @Resource
    private ContentUserFeedSettingMapper feedSettingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserFeedSettingVO getSetting(String userId) {
        requireValidUserId(userId);
        ContentUserFeedSetting setting = getOrCreateDefault(userId);
        return ContentUserFeedSettingVO.from(setting);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserFeedSettingVO updateSetting(String userId, ContentFeedSettingUpdateReq req) {
        requireValidUserId(userId);
        String activityTypes = normalizeActivityTypes(req);
        ContentUserFeedSetting setting = getOrCreateDefault(userId);
        setting.setActivityTypes(activityTypes);
        setting.setPublishEnabled(activityTypes.contains("PUBLISH"));
        setting.setLikeEnabled(activityTypes.contains("LIKE"));
        setting.setFavoriteEnabled(activityTypes.contains("FAVORITE"));
        feedSettingMapper.updateById(setting);
        return ContentUserFeedSettingVO.from(setting);
    }

    private ContentUserFeedSetting getOrCreateDefault(String userId) {
        ContentUserFeedSetting setting = feedSettingMapper.selectByUserId(userId);
        if (setting != null) {
            return setting;
        }
        ContentUserFeedSetting created = defaultSetting(userId);
        feedSettingMapper.insert(created);
        return created;
    }

    private ContentUserFeedSetting defaultSetting(String userId) {
        ContentUserFeedSetting setting = new ContentUserFeedSetting()
            .setUserId(userId)
            .setPublishEnabled(Boolean.TRUE)
            .setLikeEnabled(Boolean.TRUE)
            .setFavoriteEnabled(Boolean.TRUE)
            .setActivityTypes("PUBLISH,LIKE,FAVORITE");
        setting.setId(UUIDGenerator.generate());
        return setting;
    }

    private void requireValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        if (userId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("用户ID长度不能超过64位");
        }
    }

    private String normalizeActivityTypes(ContentFeedSettingUpdateReq req) {
        if (req == null || req.getActivityTypes() == null) {
            throw new JeecgBootException("动态类型列表不能为空");
        }
        List<String> activityTypes = req.getActivityTypes();
        if (activityTypes.isEmpty()) {
            throw new JeecgBootException("动态类型列表不能为空");
        }
        LinkedHashSet<String> normalizedTypes = new LinkedHashSet<>();
        for (String type : activityTypes) {
            if (type == null || type.trim().isEmpty()) {
                throw new JeecgBootException("动态类型不能为空");
            }
            String normalizedType = type.trim().toUpperCase();
            if (!SUPPORTED_TYPES.contains(normalizedType)) {
                throw new JeecgBootException("动态类型不支持");
            }
            if (!normalizedTypes.add(normalizedType)) {
                throw new JeecgBootException("动态类型不能重复");
            }
        }
        return String.join(",", normalizedTypes);
    }
}
