package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.circle.mapper.CircleContentMapper;
import org.jeecg.modules.content.circle.vo.CircleContentVO;
import org.jeecg.modules.content.user.service.IContentUserBadgeService;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleContentBizService 单元测试")
class CircleContentBizServiceTest {

    @Mock
    private CircleContentMapper circleContentMapper;

    @Mock
    private IContentUserBadgeService contentUserBadgeService;

    @InjectMocks
    private CircleContentBizService circleContentBizService;

    private CircleContent post1;
    private CircleContent post2;
    private ContentUserBadgeVO badge1;
    private ContentUserBadgeVO badge2;

    @BeforeEach
    void setUp() {
        post1 = new CircleContent();
        post1.setId("post-1");
        post1.setCircleId("circle-1");
        post1.setUserId("user-A");
        post1.setContent("Hello World");
        post1.setContentType("POST");
        post1.setStatus("PUBLISHED");
        post1.setIsPinned(false);
        post1.setIsFeatured(false);
        post1.setDeleted(false);

        post2 = new CircleContent();
        post2.setId("post-2");
        post2.setCircleId("circle-1");
        post2.setUserId("user-B");
        post2.setContent("Another post");
        post2.setContentType("POST");
        post2.setStatus("PUBLISHED");
        post2.setIsPinned(false);
        post2.setIsFeatured(false);
        post2.setDeleted(false);

        badge1 = new ContentUserBadgeVO();
        badge1.setBadgeCode("PIONEER");
        badge1.setBadgeName("先锋者");
        badge1.setDisplaying(true);

        badge2 = new ContentUserBadgeVO();
        badge2.setBadgeCode("EXPERT");
        badge2.setBadgeName("专家");
        badge2.setDisplaying(true);
    }

    @Nested
    @DisplayName("listPostsWithAuthorBadges 方法")
    class ListPostsWithAuthorBadges {

        @Test
        @DisplayName("空圈子应返回空列表")
        void should_return_empty_list_when_circle_has_no_posts() {
            when(circleContentMapper.selectCircleContentList("circle-1")).thenReturn(Collections.emptyList());

            List<CircleContentVO> result = circleContentBizService.listPostsWithAuthorBadges("circle-1");

            assertTrue(result.isEmpty());
            verify(contentUserBadgeService, never()).listWornBadges(anyString());
        }

        @Test
        @DisplayName("mapper 返回 null 时应返回空列表")
        void should_return_empty_list_when_mapper_returns_null() {
            when(circleContentMapper.selectCircleContentList("circle-1")).thenReturn(null);

            List<CircleContentVO> result = circleContentBizService.listPostsWithAuthorBadges("circle-1");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("应正确转换实体字段并填充 authorBadges")
        void should_convert_entity_and_populate_author_badges() {
            when(circleContentMapper.selectCircleContentList("circle-1")).thenReturn(Arrays.asList(post1, post2));
            when(contentUserBadgeService.listWornBadges("user-A")).thenReturn(Collections.singletonList(badge1));
            when(contentUserBadgeService.listWornBadges("user-B")).thenReturn(Collections.singletonList(badge2));

            List<CircleContentVO> result = circleContentBizService.listPostsWithAuthorBadges("circle-1");

            assertEquals(2, result.size());

            CircleContentVO vo1 = result.get(0);
            assertEquals("post-1", vo1.getId());
            assertEquals("user-A", vo1.getUserId());
            assertEquals("Hello World", vo1.getContent());
            assertEquals(1, vo1.getAuthorBadges().size());
            assertEquals("PIONEER", vo1.getAuthorBadges().get(0).getBadgeCode());

            CircleContentVO vo2 = result.get(1);
            assertEquals("post-2", vo2.getId());
            assertEquals("user-B", vo2.getUserId());
            assertEquals(1, vo2.getAuthorBadges().size());
            assertEquals("EXPERT", vo2.getAuthorBadges().get(0).getBadgeCode());
        }

        @Test
        @DisplayName("同一作者多条帖子只查询一次勋章")
        void should_query_badges_only_once_per_user() {
            CircleContent post3 = new CircleContent();
            post3.setId("post-3");
            post3.setCircleId("circle-1");
            post3.setUserId("user-A");
            post3.setContent("Second post by user-A");
            post3.setContentType("POST");
            post3.setStatus("PUBLISHED");

            when(circleContentMapper.selectCircleContentList("circle-1")).thenReturn(Arrays.asList(post1, post3));
            when(contentUserBadgeService.listWornBadges("user-A")).thenReturn(Collections.singletonList(badge1));

            List<CircleContentVO> result = circleContentBizService.listPostsWithAuthorBadges("circle-1");

            assertEquals(2, result.size());
            // user-A 只查询一次勋章
            verify(contentUserBadgeService, times(1)).listWornBadges("user-A");
        }

        @Test
        @DisplayName("用户无佩戴勋章时 authorBadges 应为空列表")
        void should_set_empty_badges_when_user_has_no_worn_badges() {
            when(circleContentMapper.selectCircleContentList("circle-1")).thenReturn(Collections.singletonList(post1));
            when(contentUserBadgeService.listWornBadges("user-A")).thenReturn(Collections.emptyList());

            List<CircleContentVO> result = circleContentBizService.listPostsWithAuthorBadges("circle-1");

            assertEquals(1, result.size());
            assertNotNull(result.get(0).getAuthorBadges());
            assertTrue(result.get(0).getAuthorBadges().isEmpty());
        }
    }
}
