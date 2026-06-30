package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.req.governance.GovernanceContentListReq;
import org.jeecg.modules.content.channel.req.governance.RecycleBinListReq;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.vo.governance.GovernanceContentItemVO;
import org.jeecg.modules.content.channel.vo.governance.RecycleBinItemVO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelContentGovernanceControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelGovernanceBiz channelGovernanceBiz;
    @Mock
    private ChannelMemberService memberService;

    @InjectMocks
    private ChannelContentGovernanceController controller;

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
        when(memberService.getByChannelAndUser(any(), eq(TEST_USER_ID))).thenReturn(adminMember);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_execute_governance() {
        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch1");
        req.setContentId("c1");
        req.setAction("pin");

        Result<Void> result = controller.governance(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelGovernanceBiz).executeGovernance(req, TEST_USER_ID);
    }

    @Test
    void should_get_content_list() {
        Page<GovernanceContentItemVO> page = new Page<>(1, 10);
        when(channelGovernanceBiz.getContentList(any(GovernanceContentListReq.class))).thenReturn(page);

        GovernanceContentListReq req = new GovernanceContentListReq();
        req.setChannelId("ch1");
        req.setCurrent(1);
        req.setSize(10);

        Result<Page<GovernanceContentItemVO>> result = controller.getContentList(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelGovernanceBiz).getContentList(req);
    }

    @Test
    void should_get_recycle_bin_list() {
        Page<RecycleBinItemVO> page = new Page<>(1, 10);
        when(channelGovernanceBiz.getRecycleBinList(any(RecycleBinListReq.class))).thenReturn(page);

        RecycleBinListReq req = new RecycleBinListReq();
        req.setChannelId("ch1");
        req.setCurrent(1);
        req.setSize(10);

        Result<Page<RecycleBinItemVO>> result = controller.getRecycleBinList(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelGovernanceBiz).getRecycleBinList(req);
    }
}
