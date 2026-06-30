package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelMemberBizService;
import org.jeecg.modules.content.channel.entity.ChannelJoinApplication;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.ChannelJoinApplicationService;
import org.jeecg.modules.content.channel.service.ChannelMemberListService;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.service.ChannelMuteService;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.jeecg.modules.content.channel.vo.UserChannelRelationVO;
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
class ChannelMemberControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";
    private static final String OWNER_USER_ID = "owner-id";
    private static final String ADMIN_USER_ID = "admin-id";

    @Mock
    private ChannelMemberBizService memberBizService;
    @Mock
    private ChannelMemberService memberService;
    @Mock
    private ChannelMemberListService memberListService;
    @Mock
    private ChannelJoinApplicationService applicationService;
    @Mock
    private ChannelSubscriptionService subscriptionService;
    @Mock
    private ChannelMuteService muteService;

    @InjectMocks
    private ChannelMemberController controller;

    private void setUserContext(String userId, String username) {
        LoginUser user = new LoginUser();
        user.setId(userId);
        user.setUsername(username);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            JSON.toJSONString(user), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void mockAdminPermission(String channelId, String userId) {
        ChannelMember adminMember = new ChannelMember();
        adminMember.setRole(MemberRole.ADMIN.getCode());
        adminMember.setChannelId(channelId);
        lenient().when(memberService.getByChannelAndUser(eq(channelId), eq(userId))).thenReturn(adminMember);
    }

    @BeforeEach
    void setUp() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_join_free() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);

        Result<String> result = controller.joinFree("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("加入成功");
        verify(memberBizService).joinByFree("ch1", TEST_USER_ID);
    }

    @Test
    void should_join_apply() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);

        Result<String> result = controller.joinApply("ch1", "希望加入");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("申请已提交");
        verify(memberBizService).joinByReview("ch1", TEST_USER_ID, "希望加入");
    }

    @Test
    void should_leave_channel() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);

        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getByChannelAndUser("ch1", TEST_USER_ID)).thenReturn(member);

        Result<String> result = controller.leave("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已退出");
        verify(memberService).removeMember("m1");
    }

    @Test
    void should_reject_owner_leaving() {
        setUserContext(OWNER_USER_ID, TEST_USERNAME);

        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.OWNER.getCode());
        when(memberService.getByChannelAndUser("ch1", OWNER_USER_ID)).thenReturn(member);

        Result<String> result = controller.leave("ch1");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("频道主不可直接退出");
    }

    @Test
    void should_assign_role() {
        setUserContext(ADMIN_USER_ID, TEST_USERNAME);

        ChannelMember targetMember = new ChannelMember();
        targetMember.setId("m1");
        targetMember.setChannelId("ch1");
        when(memberService.getById("m1")).thenReturn(targetMember);
        mockAdminPermission("ch1", ADMIN_USER_ID);

        Result<String> result = controller.assignRole("m1", MemberRole.ADMIN);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("角色已更新");
        verify(memberService).assignRole("m1", MemberRole.ADMIN, ADMIN_USER_ID);
    }

    @Test
    void should_approve_application() {
        setUserContext(ADMIN_USER_ID, TEST_USERNAME);

        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setChannelId("ch1");
        when(applicationService.getById("app1")).thenReturn(app);
        mockAdminPermission("ch1", ADMIN_USER_ID);

        Result<String> result = controller.approveApplication("app1", "欢迎加入");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已批准");
        verify(memberBizService).approveAndAddMember("app1", ADMIN_USER_ID, "欢迎加入");
    }

    @Test
    void should_reject_application() {
        setUserContext(ADMIN_USER_ID, TEST_USERNAME);

        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setChannelId("ch1");
        when(applicationService.getById("app1")).thenReturn(app);
        mockAdminPermission("ch1", ADMIN_USER_ID);

        Result<String> result = controller.rejectApplication("app1", "不符合条件");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已拒绝");
        verify(applicationService).reject("app1", ADMIN_USER_ID, "不符合条件");
    }

    @Test
    void should_get_user_channel_relation_as_member() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);

        ChannelMember member = new ChannelMember();
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getByChannelAndUser("ch1", TEST_USER_ID)).thenReturn(member);
        when(subscriptionService.isSubscribed("ch1", TEST_USER_ID)).thenReturn(true);
        when(muteService.isMuted("ch1", TEST_USER_ID)).thenReturn(false);

        Result<UserChannelRelationVO> result = controller.getUserChannelRelation("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult().getIsMember()).isTrue();
        assertThat(result.getResult().getRole()).isEqualTo(MemberRole.MEMBER.getCode());
        assertThat(result.getResult().getIsSubscribed()).isTrue();
        assertThat(result.getResult().getIsMuted()).isFalse();
    }

    @Test
    void should_get_user_channel_relation_as_non_member() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);

        when(memberService.getByChannelAndUser("ch1", TEST_USER_ID)).thenReturn(null);
        when(subscriptionService.isSubscribed("ch1", TEST_USER_ID)).thenReturn(false);
        when(muteService.isMuted("ch1", TEST_USER_ID)).thenReturn(false);

        Result<UserChannelRelationVO> result = controller.getUserChannelRelation("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult().getIsMember()).isFalse();
        assertThat(result.getResult().getRole()).isNull();
        assertThat(result.getResult().getIsSubscribed()).isFalse();
        assertThat(result.getResult().getIsMuted()).isFalse();
    }

    @Test
    void should_get_user_channel_relation_when_muted() {
        setUserContext(TEST_USER_ID, TEST_USERNAME);

        ChannelMember member = new ChannelMember();
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getByChannelAndUser("ch1", TEST_USER_ID)).thenReturn(member);
        when(subscriptionService.isSubscribed("ch1", TEST_USER_ID)).thenReturn(true);
        when(muteService.isMuted("ch1", TEST_USER_ID)).thenReturn(true);

        Result<UserChannelRelationVO> result = controller.getUserChannelRelation("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult().getIsMuted()).isTrue();
    }
}
