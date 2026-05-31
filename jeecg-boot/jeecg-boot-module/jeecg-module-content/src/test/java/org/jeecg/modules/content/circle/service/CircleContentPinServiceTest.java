package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.circle.mapper.CircleContentMapper;
import org.jeecg.modules.content.circle.service.impl.CircleContentPinServiceImpl;
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

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 圈子内容置顶与精华服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleContentPinServiceTest {

    @Mock
    private CircleContentMapper circleContentMapper;

    @InjectMocks
    private CircleContentPinServiceImpl circleContentPinService;

    private static final String TEST_CONTENT_ID = "content001";

    @BeforeAll
    static void initMybatisPlusCache() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                CircleContent.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(circleContentPinService, "baseMapper", circleContentMapper);
    }

    private CircleContent createTestContent() {
        CircleContent content = new CircleContent();
        content.setId(TEST_CONTENT_ID);
        content.setCircleId("circle001");
        content.setUserId("user001");
        content.setContent("测试内容");
        content.setContentType("POST");
        content.setStatus("ACTIVE");
        content.setIsPinned(false);
        content.setIsFeatured(false);
        content.setDeleted(false);
        return content;
    }

    // ==================== pinContent ====================

    @Nested
    @DisplayName("pinContent - 置顶内容")
    class PinContent {

        @Test
        @DisplayName("置顶 - 设置isPinned=true和pinnedAt")
        void pin_setsPinnedTrueAndPinnedAt() {
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.pinContent(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }
    }

    // ==================== unpinContent ====================

    @Nested
    @DisplayName("unpinContent - 取消置顶")
    class UnpinContent {

        @Test
        @DisplayName("取消置顶 - 设置isPinned=false和pinnedAt=null")
        void unpin_setsPinnedFalseAndPinnedAtNull() {
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.unpinContent(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }
    }

    // ==================== featureContent ====================

    @Nested
    @DisplayName("featureContent - 设为精华")
    class FeatureContent {

        @Test
        @DisplayName("设精华 - 设置isFeatured=true和featuredAt")
        void feature_setsFeaturedTrueAndFeaturedAt() {
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.featureContent(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }
    }

    // ==================== unfeatureContent ====================

    @Nested
    @DisplayName("unfeatureContent - 取消精华")
    class UnfeatureContent {

        @Test
        @DisplayName("取消精华 - 设置isFeatured=false和featuredAt=null")
        void unfeature_setsFeaturedFalseAndFeaturedAtNull() {
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.unfeatureContent(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }
    }

    // ==================== togglePin ====================

    @Nested
    @DisplayName("togglePin - 切换置顶状态")
    class TogglePin {

        @Test
        @DisplayName("当前未置顶 - 执行置顶")
        void togglePin_notPinned_pins() {
            CircleContent content = createTestContent();
            content.setIsPinned(false);
            when(circleContentMapper.selectById(TEST_CONTENT_ID)).thenReturn(content);
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.togglePin(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }

        @Test
        @DisplayName("当前已置顶 - 取消置顶")
        void togglePin_alreadyPinned_unpins() {
            CircleContent content = createTestContent();
            content.setIsPinned(true);
            content.setPinnedAt(new Date());
            when(circleContentMapper.selectById(TEST_CONTENT_ID)).thenReturn(content);
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.togglePin(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }
    }

    // ==================== toggleFeature ====================

    @Nested
    @DisplayName("toggleFeature - 切换精华状态")
    class ToggleFeature {

        @Test
        @DisplayName("当前非精华 - 设为精华")
        void toggleFeature_notFeatured_features() {
            CircleContent content = createTestContent();
            content.setIsFeatured(false);
            when(circleContentMapper.selectById(TEST_CONTENT_ID)).thenReturn(content);
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.toggleFeature(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }

        @Test
        @DisplayName("当前已是精华 - 取消精华")
        void toggleFeature_alreadyFeatured_unfeatures() {
            CircleContent content = createTestContent();
            content.setIsFeatured(true);
            content.setFeaturedAt(new Date());
            when(circleContentMapper.selectById(TEST_CONTENT_ID)).thenReturn(content);
            when(circleContentMapper.update(any(), any())).thenReturn(1);

            circleContentPinService.toggleFeature(TEST_CONTENT_ID);

            verify(circleContentMapper).update(any(), any());
        }
    }
}
