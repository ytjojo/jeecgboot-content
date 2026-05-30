package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.mapper.CircleAnnouncementMapper;
import org.jeecg.modules.content.circle.service.impl.CircleAnnouncementServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 圈子公告服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleAnnouncementServiceTest {

    @Mock
    private CircleAnnouncementMapper circleAnnouncementMapper;

    @InjectMocks
    private CircleAnnouncementServiceImpl circleAnnouncementService;

    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_CONTENT = "测试公告内容";

    @BeforeAll
    static void initLambdaCache() {
        // 初始化 MybatisPlus Lambda 缓存，使 lambdaUpdate() 在纯 Mockito 环境下可用
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                CircleAnnouncement.class);
    }

    @BeforeEach
    void setUp() {
        // ServiceImpl 的 baseMapper 需要手动注入，Mockito 不会自动设置
        ReflectionTestUtils.setField(circleAnnouncementService, "baseMapper", circleAnnouncementMapper);
    }

    private CircleAnnouncement createTestAnnouncement() {
        CircleAnnouncement announcement = new CircleAnnouncement();
        announcement.setCircleId(TEST_CIRCLE_ID);
        announcement.setContent(TEST_CONTENT);
        return announcement;
    }

    // ==================== publish ====================

    @Nested
    @DisplayName("publish - 发布公告")
    class Publish {

        @Test
        @DisplayName("发布新公告 - 旧公告置为INACTIVE，新公告状态为ACTIVE")
        void publish_deactivatesOldAndSavesNew() {
            CircleAnnouncement announcement = createTestAnnouncement();

            when(circleAnnouncementMapper.update(any(), any()))
                    .thenReturn(1);
            when(circleAnnouncementMapper.insert(any(CircleAnnouncement.class)))
                    .thenReturn(1);

            circleAnnouncementService.publish(announcement);

            // 验证旧公告被置为INACTIVE
            verify(circleAnnouncementMapper).update(any(), any());
            // 验证新公告被保存
            verify(circleAnnouncementMapper).insert(any(CircleAnnouncement.class));
            assertThat(announcement.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("首次发布公告 - 没有旧公告，直接保存新公告")
        void publish_noOldAnnouncement_savesNewDirectly() {
            CircleAnnouncement announcement = createTestAnnouncement();

            when(circleAnnouncementMapper.update(any(), any()))
                    .thenReturn(0);
            when(circleAnnouncementMapper.insert(any(CircleAnnouncement.class)))
                    .thenReturn(1);

            circleAnnouncementService.publish(announcement);

            verify(circleAnnouncementMapper).insert(any(CircleAnnouncement.class));
            assertThat(announcement.getStatus()).isEqualTo("ACTIVE");
        }
    }

    // ==================== getActiveByCircleId ====================

    @Nested
    @DisplayName("getActiveByCircleId - 查询有效公告")
    class GetActiveByCircleId {

        @Test
        @DisplayName("存在有效公告 - 返回公告")
        void getActiveByCircleId_returnsAnnouncement() {
            CircleAnnouncement expected = createTestAnnouncement();
            expected.setId("ann001");
            expected.setStatus("ACTIVE");
            when(circleAnnouncementMapper.selectActiveByCircleId(TEST_CIRCLE_ID)).thenReturn(expected);

            CircleAnnouncement result = circleAnnouncementService.getActiveByCircleId(TEST_CIRCLE_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("ann001");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("公告已过期 - 返回null")
        void getActiveByCircleId_expired_returnsNull() {
            when(circleAnnouncementMapper.selectActiveByCircleId(TEST_CIRCLE_ID)).thenReturn(null);

            CircleAnnouncement result = circleAnnouncementService.getActiveByCircleId(TEST_CIRCLE_ID);

            assertThat(result).isNull();
        }
    }
}
