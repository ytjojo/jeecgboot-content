package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelMergeBiz;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.req.ChannelMergeReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道合并控制器测试
 * 验证 validate/executeMerge 流程
 */
@ExtendWith(MockitoExtension.class)
class ChannelMergeControllerTest {

    @Mock
    private ChannelMergeBiz mergeBiz;

    @InjectMocks
    private ChannelMergeController controller;

    @Test
    void should_validate_merge() {
        ChannelMergeReq req = new ChannelMergeReq();
        req.setSourceChannelId("src");
        req.setTargetChannelId("tgt");
        Map<String, Object> impact = new HashMap<>();
        impact.put("needOrgApproval", false);
        when(mergeBiz.validateMerge("src", "tgt")).thenReturn(impact);

        Result<Map<String, Object>> result = controller.validateMerge(req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).containsKey("needOrgApproval");
    }

    @Test
    void should_execute_merge_directly_when_no_org_approval() {
        ChannelMergeReq req = new ChannelMergeReq();
        req.setSourceChannelId("src");
        req.setTargetChannelId("tgt");

        Map<String, Object> impact = new HashMap<>();
        impact.put("needOrgApproval", false);
        when(mergeBiz.validateMerge("src", "tgt")).thenReturn(impact);

        Result<?> result = controller.executeMerge(req);

        assertThat(result.isSuccess()).isTrue();
        verify(mergeBiz).executeMerge("src", "tgt", "current-user-id");
        verify(mergeBiz, never()).submitMergeForReview(any(), any(), any());
    }

    @Test
    void should_submit_org_merge_for_review() {
        ChannelMergeReq req = new ChannelMergeReq();
        req.setSourceChannelId("src");
        req.setTargetChannelId("tgt");

        Map<String, Object> impact = new HashMap<>();
        impact.put("needOrgApproval", true);
        impact.put("sourceChannelType", ChannelType.ORGANIZATION);
        when(mergeBiz.validateMerge("src", "tgt")).thenReturn(impact);

        ChannelReview review = new ChannelReview();
        review.setReviewId("rv1");
        when(mergeBiz.submitMergeForReview("src", "tgt", "current-user-id")).thenReturn(review);

        Result<?> result = controller.executeMerge(req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).asString().contains("审核ID");
        verify(mergeBiz).submitMergeForReview("src", "tgt", "current-user-id");
        verify(mergeBiz, never()).executeMerge(any(), any(), any());
    }
}
