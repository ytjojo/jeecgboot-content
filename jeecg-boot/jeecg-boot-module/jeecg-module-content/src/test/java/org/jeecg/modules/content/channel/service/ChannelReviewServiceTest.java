package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道审核服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelReviewServiceTest {

    @Mock
    private ChannelReviewMapper reviewMapper;

    @InjectMocks
    private ChannelReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reviewService, "baseMapper", reviewMapper);
    }

    @Test
    void should_create_approved_review() {
        ChannelReview review = reviewService.createReview("ch1", "rv1", ReviewResult.PASS, "ok");

        assertThat(review.getStatus()).isEqualTo("approved");
        assertThat(review.getReviewerId()).isEqualTo("rv1");
        assertThat(review.getChannelId()).isEqualTo("ch1");
        assertThat(review.getReviewReason()).isEqualTo("ok");
        assertThat(review.getReviewId()).isNotBlank();
    }

    @Test
    void should_create_rejected_review() {
        ChannelReview review = reviewService.createReview("ch1", "rv1", ReviewResult.REJECT, "bad");

        assertThat(review.getStatus()).isEqualTo("rejected");
    }

    @Test
    void should_create_returned_review() {
        ChannelReview review = reviewService.createReview("ch1", "rv1", ReviewResult.RETURN_FOR_EDIT, "fix");

        assertThat(review.getStatus()).isEqualTo("returned");
    }

    @Test
    void should_submit_pending_review() {
        ChannelReview review = reviewService.submitReview("ch1", "create", "user1", "new ch");

        assertThat(review.getStatus()).isEqualTo("pending");
        assertThat(review.getReviewType()).isEqualTo("create");
        assertThat(review.getApplicantId()).isEqualTo("user1");
        assertThat(review.getSubmitTime()).isNotNull();
    }

    @Test
    void should_list_reviews_by_channel_in_desc_order() {
        reviewService.listReviewsByChannelId("ch1");

        verify(reviewMapper).selectList(any(LambdaQueryWrapper.class));
    }
}
