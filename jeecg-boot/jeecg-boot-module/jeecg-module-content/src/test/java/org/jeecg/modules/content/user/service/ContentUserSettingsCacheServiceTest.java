package org.jeecg.modules.content.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户设置缓存服务单元测试。
 * 验证 Redis key 格式、TTL 以及缓存驱逐行为。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserSettingsCacheServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ContentUserSettingsCacheService cacheService;

    /** 隐私设置读取应使用正确的 key 格式 content:privacy:{userId}。 */
    @Test
    void getPrivacySettingShouldUseCorrectKey() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("content:privacy:user1")).thenReturn("{\"birthdayVisibility\":\"PUBLIC\"}");

        String result = cacheService.getPrivacySetting("user1");

        assertThat(result).isEqualTo("{\"birthdayVisibility\":\"PUBLIC\"}");
        verify(valueOperations).get("content:privacy:user1");
    }

    /** 隐私设置缓存应使用正确的 key 和 300 秒 TTL。 */
    @Test
    void cachePrivacySettingShouldSetWithTtl() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.cachePrivacySetting("user1", "{\"birthdayVisibility\":\"PUBLIC\"}");

        verify(valueOperations).set("content:privacy:user1", "{\"birthdayVisibility\":\"PUBLIC\"}", 300, TimeUnit.SECONDS);
    }

    /** 隐私设置驱逐应删除正确的 key。 */
    @Test
    void evictPrivacyShouldDeleteCorrectKey() {
        cacheService.evictPrivacy("user1");

        verify(stringRedisTemplate).delete("content:privacy:user1");
    }

    /** 通知设置读取应使用正确的 key 格式 content:notification:{userId}。 */
    @Test
    void getNotificationSettingShouldUseCorrectKey() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("content:notification:user1")).thenReturn("{\"likeNoticeEnabled\":true}");

        String result = cacheService.getNotificationSetting("user1");

        assertThat(result).isEqualTo("{\"likeNoticeEnabled\":true}");
        verify(valueOperations).get("content:notification:user1");
    }

    /** 通知设置缓存应使用正确的 key 和 300 秒 TTL。 */
    @Test
    void cacheNotificationSettingShouldSetWithTtl() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.cacheNotificationSetting("user1", "{\"likeNoticeEnabled\":true}");

        verify(valueOperations).set("content:notification:user1", "{\"likeNoticeEnabled\":true}", 300, TimeUnit.SECONDS);
    }

    /** 通知设置驱逐应删除正确的 key。 */
    @Test
    void evictNotificationShouldDeleteCorrectKey() {
        cacheService.evictNotification("user1");

        verify(stringRedisTemplate).delete("content:notification:user1");
    }
}
