package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelMergeBiz;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.req.ChannelMergeReq;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelMergeControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelMergeBiz mergeBiz;
    @Mock
    private ChannelMemberService memberService;

    @InjectMocks
    private ChannelMergeController controller;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(TEST_USER_ID);
        loginUser.setUsername(TEST_USERNAME);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                JSON.toJSONString(loginUser), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChannelMember adminMember = new ChannelMember();
        adminMember.setRole(MemberRole.ADMIN.getCode());
        lenient().when(memberService.getByChannelAndUser(any(), eq(TEST_USER_ID))).thenReturn(adminMember);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

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
        verify(mergeBiz).executeMerge("src", "tgt", TEST_USER_ID);
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
        when(mergeBiz.submitMergeForReview("src", "tgt", TEST_USER_ID)).thenReturn(review);

        Result<?> result = controller.executeMerge(req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).asString().contains("审核ID");
        verify(mergeBiz).submitMergeForReview("src", "tgt", TEST_USER_ID);
        verify(mergeBiz, never()).executeMerge(any(), any(), any());
    }
}
