package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelStatsBiz;
import org.jeecg.modules.content.channel.constant.ChannelStatsConstant;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelStatsControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelStatsBiz channelStatsBiz;
    @Mock
    private ChannelMemberService memberService;

    @InjectMocks
    private ChannelStatsController controller;

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
    void should_get_core_stats() {
        ChannelStatsVO vo = ChannelStatsVO.builder().channelId("ch1").subscriberCount(10).build();
        when(channelStatsBiz.getCoreStats("ch1")).thenReturn(vo);

        Result<ChannelStatsVO> result = controller.getCoreStats("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(channelStatsBiz).getCoreStats("ch1");
    }

    @Test
    void should_get_trend_data() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        when(channelStatsBiz.getTrendData("ch1", start, end, ChannelStatsConstant.STAT_TYPE_DAILY))
            .thenReturn(null);

        Result<?> result = controller.getTrendData("ch1", start, end, ChannelStatsConstant.STAT_TYPE_DAILY);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_reject_invalid_stat_type() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        Result<?> result = controller.getTrendData("ch1", start, end, "yearly");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("无效的统计类型");
    }

    @Test
    void should_get_hot_content() {
        when(channelStatsBiz.getHotContent("ch1", 5, 7)).thenReturn(Collections.emptyList());

        Result<?> result = controller.getHotContent("ch1", 5, 7);

        assertThat(result.isSuccess()).isTrue();
        verify(channelStatsBiz).getHotContent("ch1", 5, 7);
    }

    @Test
    void should_get_user_analysis() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        when(channelStatsBiz.getUserAnalysis("ch1", start, end)).thenReturn(null);

        Result<?> result = controller.getUserAnalysis("ch1", start, end);

        assertThat(result.isSuccess()).isTrue();
    }
}
