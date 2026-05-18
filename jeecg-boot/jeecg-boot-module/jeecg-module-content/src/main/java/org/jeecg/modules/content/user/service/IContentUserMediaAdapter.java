package org.jeecg.modules.content.user.service;

/**
 * 内容社区资料素材处理适配接口。
 */
public interface IContentUserMediaAdapter {

    void validateAvatar(String avatar);

    void validateHomepageBackground(String homepageBackground);
}
