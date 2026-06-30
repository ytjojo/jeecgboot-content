package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBizService;
import org.jeecg.modules.content.channel.entity.ChannelBlacklist;
import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.ChannelBlacklistService;
import org.jeecg.modules.content.channel.service.ChannelGovernanceLogService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelGovernanceControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelGovernanceBizService governanceBizService;
    @Mock
    private ChannelBlacklistService blacklistService;
    @Mock
    private ChannelGovernanceLogService governanceLogService;
    @Mock
    private ChannelMemberService memberService;

    @InjectMocks
    private ChannelGovernanceController controller;

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
        adminMember.setChannelId("ch1");
        lenient().when(memberService.getByChannelAndUser(any(), eq(TEST_USER_ID))).thenReturn(adminMember);
        lenient().when(memberService.getById(any())).thenReturn(adminMember);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_remove_member() {
        ChannelMember targetMember = new ChannelMember();
        targetMember.setId("m1");
        targetMember.setChannelId("ch1");
        when(memberService.getById("m1")).thenReturn(targetMember);

        Result<String> result = controller.removeMember("m1", "违规");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已移除");
        verify(governanceBizService).removeMember("m1", TEST_USER_ID, "违规");
    }

    @Test
    void should_mute_member() {
        Result<String> result = controller.muteMember("ch1", "user1", 7, "违规发言");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已禁言");
        verify(governanceBizService).muteMember("ch1", "user1", TEST_USER_ID, "违规发言", 7);
    }

    @Test
    void should_unmute_member() {
        Result<String> result = controller.unmuteMember("ch1", "user1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已解除禁言");
        verify(governanceBizService).unmuteMember("ch1", "user1", TEST_USER_ID);
    }

    @Test
    void should_add_to_blacklist() {
        Result<String> result = controller.addToBlacklist("ch1", "user1", "骚扰");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已加入黑名单");
        verify(governanceBizService).addToBlacklist("ch1", "user1", TEST_USER_ID, "骚扰");
    }

    @Test
    void should_remove_from_blacklist() {
        Result<String> result = controller.removeFromBlacklist("ch1", "user1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已移出黑名单");
        verify(governanceBizService).removeFromBlacklist("ch1", "user1", TEST_USER_ID);
    }

    @Test
    void should_list_blacklist() {
        ChannelBlacklist entry = new ChannelBlacklist();
        entry.setId("bl1");
        when(blacklistService.listByChannel("ch1")).thenReturn(List.of(entry));

        Result<List<ChannelBlacklist>> result = controller.listBlacklist("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).hasSize(1);
        verify(blacklistService).listByChannel("ch1");
    }

    @Test
    void should_list_governance_logs() {
        ChannelGovernanceLog log = new ChannelGovernanceLog();
        log.setId("log1");
        Page<ChannelGovernanceLog> page = new Page<>(1, 20);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(governanceLogService.listByChannel("ch1", null, 1, 20)).thenReturn(page);

        Result<IPage<ChannelGovernanceLog>> result = controller.listGovernanceLogs("ch1", null, 1, 20);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult().getRecords()).hasSize(1);
    }

    @Test
    void should_list_governance_logs_with_action_filter() {
        Page<ChannelGovernanceLog> page = new Page<>(1, 20);
        page.setRecords(List.of());
        page.setTotal(0);
        when(governanceLogService.listByChannel("ch1", 2, 1, 20)).thenReturn(page);

        Result<IPage<ChannelGovernanceLog>> result = controller.listGovernanceLogs("ch1", 2, 1, 20);

        assertThat(result.getCode()).isEqualTo(200);
        verify(governanceLogService).listByChannel("ch1", 2, 1, 20);
    }
}
