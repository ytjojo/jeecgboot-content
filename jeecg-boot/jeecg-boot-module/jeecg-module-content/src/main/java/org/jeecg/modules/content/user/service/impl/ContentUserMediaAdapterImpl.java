package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.service.IContentUserMediaAdapter;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 默认资料素材适配器，先基于 URL 后缀执行格式约束。
 */
@Service
public class ContentUserMediaAdapterImpl implements IContentUserMediaAdapter {

    @Override
    public void validateAvatar(String avatar) {
        validateImageUrl(avatar, "头像");
    }

    @Override
    public void validateHomepageBackground(String homepageBackground) {
        if (homepageBackground == null) {
            return;
        }
        validateImageUrl(homepageBackground, "主页背景图");
    }

    private void validateImageUrl(String imageUrl, String fieldName) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new JeecgBootException(fieldName + "不能为空");
        }
        String lower = imageUrl.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp"))) {
            throw new JeecgBootException(fieldName + "仅支持 JPG、PNG、WebP");
        }
    }
}
