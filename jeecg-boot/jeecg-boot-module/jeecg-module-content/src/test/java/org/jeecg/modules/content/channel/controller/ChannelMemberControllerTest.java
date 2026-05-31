package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelMemberBizService;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.ChannelJoinApplicationService;
import org.jeecg.modules.content.channel.service.ChannelMemberListService;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道成员控制器测试
 * 验证成员管理接口正确委托给 service 层
 */
@ExtendWith(MockitoExtension.class)
class ChannelMemberControllerTest {

    @Mock
    private ChannelMemberBizService memberBizService;
    @Mock
    private ChannelMemberService memberService;
    @Mock
    private ChannelMemberListService memberListService;
    @Mock
    private ChannelJoinApplicationService applicationService;

    @InjectMocks
    private ChannelMemberController controller;

    private void setUserContext(String userId) {
        LoginUser user = new LoginUser();
        user.setId(userId);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            JSON.toJSONString(user), null));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_join_free() {
        // 自由加入接口应调用 bizService 的 joinByFree
        setUserContext("user1");

        Result<String> result = controller.joinFree("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("加入成功");
        verify(memberBizService).joinByFree("ch1", "user1");
    }

    @Test
    void should_join_apply() {
        // 申请加入接口应调用 bizService 的 joinByReview
        setUserContext("user1");

        Result<String> result = controller.joinApply("ch1", "希望加入");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("申请已提交");
        verify(memberBizService).joinByReview("ch1", "user1", "希望加入");
    }

    @Test
    void should_leave_channel() {
        // 退出频道接口应调用 service 的 removeMember
        setUserContext("user1");

        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getByChannelAndUser("ch1", "user1")).thenReturn(member);

        Result<String> result = controller.leave("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已退出");
        verify(memberService).removeMember("m1");
    }

    @Test
    void should_reject_owner_leaving() {
        // 频道主不能直接退出，需先转让
        setUserContext("owner1");

        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.OWNER.getCode());
        when(memberService.getByChannelAndUser("ch1", "owner1")).thenReturn(member);

        Result<String> result = controller.leave("ch1");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("频道主不可直接退出");
    }

    @Test
    void should_assign_role() {
        // 分配角色接口应调用 service 的 assignRole
        setUserContext("owner1");

        Result<String> result = controller.assignRole("m1", MemberRole.ADMIN);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("角色已更新");
        verify(memberService).assignRole("m1", MemberRole.ADMIN, "owner1");
    }

    @Test
    void should_approve_application() {
        // 批准申请接口应调用 bizService 的 approveAndAddMember
        setUserContext("admin1");

        Result<String> result = controller.approveApplication("app1", "欢迎加入");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已批准");
        verify(memberBizService).approveAndAddMember("app1", "admin1", "欢迎加入");
    }

    @Test
    void should_reject_application() {
        // 拒绝申请接口应调用 applicationService 的 reject
        setUserContext("admin1");

        Result<String> result = controller.rejectApplication("app1", "不符合条件");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已拒绝");
        verify(applicationService).reject("app1", "admin1", "不符合条件");
    }
}
