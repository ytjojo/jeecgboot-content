package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.service.IContentUserMediaAdapter;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * 默认资料素材适配器，基于 URL 与查询参数中的素材元数据执行轻量校验。
 */
@Service
public class ContentUserMediaAdapterImpl implements IContentUserMediaAdapter {

    private static final long MAX_IMAGE_SIZE = 5L * 1024L * 1024L;
    private static final int MIN_DIMENSION = 1;
    private static final int MAX_AVATAR_DIMENSION = 4096;
    private static final int MAX_BACKGROUND_DIMENSION = 8192;

    @Override
    public void validateAvatar(String avatar) {
        validateImageUrl(avatar, "头像", MAX_AVATAR_DIMENSION);
    }

    @Override
    public void validateHomepageBackground(String homepageBackground) {
        if (homepageBackground == null) {
            return;
        }
        validateImageUrl(homepageBackground, "主页背景图", MAX_BACKGROUND_DIMENSION);
    }

    private void validateImageUrl(String imageUrl, String fieldName, int maxDimension) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new JeecgBootException(fieldName + "不能为空");
        }
        URI uri = parseUri(imageUrl, fieldName);
        String lower = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp"))) {
            throw new JeecgBootException(fieldName + "仅支持 JPG、PNG、WebP");
        }
        validateLongQuery(uri, "size", value -> value <= 0 || value > MAX_IMAGE_SIZE, fieldName + "大小不能超过5MB");
        validateLongQuery(uri, "width", value -> value < MIN_DIMENSION || value > maxDimension, fieldName + "宽度不合法");
        validateLongQuery(uri, "height", value -> value < MIN_DIMENSION || value > maxDimension, fieldName + "高度不合法");
    }

    private URI parseUri(String imageUrl, String fieldName) {
        try {
            return new URI(imageUrl.trim());
        } catch (URISyntaxException ex) {
            throw new JeecgBootException(fieldName + "地址格式不合法");
        }
    }

    private void validateLongQuery(URI uri, String name, java.util.function.LongPredicate invalid, String message) {
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && name.equals(parts[0])) {
                try {
                    long value = Long.parseLong(parts[1]);
                    if (invalid.test(value)) {
                        throw new JeecgBootException(message);
                    }
                } catch (NumberFormatException ex) {
                    throw new JeecgBootException(message);
                }
            }
        }
    }
}
