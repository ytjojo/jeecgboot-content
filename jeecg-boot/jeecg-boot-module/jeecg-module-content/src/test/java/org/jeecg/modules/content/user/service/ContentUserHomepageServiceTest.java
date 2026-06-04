package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserHomepageModule;
import org.jeecg.modules.content.user.mapper.ContentUserHomepageModuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageModuleReq;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageUpdateReq;
import org.jeecg.modules.content.user.service.impl.ContentUserHomepageServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * 内容社区主页个性化服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserHomepageServiceTest {

    @Mock
    private ContentUserHomepageModuleMapper homepageModuleMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private IContentUserMediaAdapter mediaAdapter;

    @Mock
    private IContentUserProfileService profileService;

    @InjectMocks
    private ContentUserHomepageServiceImpl homepageService;

    @Test
    void shouldSaveThemeColorBackgroundAndModules() {
        ContentUserProfile profile = new ContentUserProfile().setUserId("u1");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(profileService.getProfile(eq("u1"), eq("u1"))).thenReturn(new ContentUserProfileVO().setUserId("u1"));

        homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq()
            .setHomepageBackground("https://cdn.example.com/bg.webp?size=1024&width=1200&height=800")
            .setThemeColor("#123abc")
            .setModules(List.of(module("POSTS", true, 0), module("ABOUT", false, 1))));

        verify(mediaAdapter).validateHomepageBackground("https://cdn.example.com/bg.webp?size=1024&width=1200&height=800");
        verify(profileMapper).updateById(profile);
        verify(homepageModuleMapper, times(2)).insert(any(ContentUserHomepageModule.class));
    }

    @Test
    void shouldRejectInvalidThemeAndModuleConfiguration() {
        when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile().setUserId("u1"));

        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setThemeColor("")))
            .hasMessageContaining("主题色格式不合法");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setThemeColor("blue")))
            .hasMessageContaining("主题色格式不合法");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setThemeColor("#123456789")))
            .hasMessageContaining("主题色格式不合法");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setModules(List.of())))
            .hasMessageContaining("主页模块不能为空");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setModules(List.of(module("UNKNOWN", true, 0)))))
            .hasMessageContaining("未知主页模块");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setModules(List.of(module("POSTS", true, 0), module("POSTS", true, 1)))))
            .hasMessageContaining("主页模块不能重复");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setModules(List.of(module("POSTS", true, -1)))))
            .hasMessageContaining("主页模块排序不合法");
        assertThatThrownBy(() -> homepageService.updateHomepage("u1", new ContentUserHomepageUpdateReq().setModules(List.of(module("POSTS", false, 0)))))
            .hasMessageContaining("至少保留一个主页模块");
    }

    @Test
    void shouldRestoreHomepageDefaults() {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId("u1")
            .setHomepageBackground("https://cdn.example.com/bg.png")
            .setThemeColor("#000000");
        when(profileMapper.selectByUserId("u1")).thenReturn(profile);
        when(profileService.getProfile(eq("u1"), eq("u1"))).thenReturn(new ContentUserProfileVO().setUserId("u1").setThemeColor("#1677ff"));

        homepageService.restoreDefaults("u1");

        verify(profileMapper).updateById(org.mockito.ArgumentMatchers.argThat(
            (ContentUserProfile item) -> item.getHomepageBackground() == null && "#1677ff".equals(item.getThemeColor())
        ));
    }

    private ContentUserHomepageModuleReq module(String key, boolean visible, int sortOrder) {
        return new ContentUserHomepageModuleReq().setModuleKey(key).setVisible(visible).setSortOrder(sortOrder);
    }
}
