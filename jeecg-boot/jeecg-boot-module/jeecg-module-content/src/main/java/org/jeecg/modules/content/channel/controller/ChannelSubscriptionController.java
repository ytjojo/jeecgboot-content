package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;
import org.jeecg.modules.content.channel.entity.ChannelSubscriptionGroup;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionGroupService;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "频道订阅管理")
@RestController
@RequestMapping("/channel/subscription")
public class ChannelSubscriptionController {

    @Resource
    private ChannelSubscriptionService subscriptionService;
    @Resource
    private ChannelSubscriptionGroupService groupService;

    @Operation(summary = "订阅频道")
    @PostMapping("/subscribe")
    public Result<String> subscribe(@RequestParam String channelId) {
        String userId = "currentUserId";
        subscriptionService.subscribe(channelId, userId);
        return Result.OK("订阅成功");
    }

    @Operation(summary = "取消订阅")
    @PostMapping("/unsubscribe")
    public Result<String> unsubscribe(@RequestParam String channelId) {
        String userId = "currentUserId";
        subscriptionService.unsubscribe(channelId, userId);
        return Result.OK("已取消订阅");
    }

    @Operation(summary = "订阅列表")
    @GetMapping("/list")
    public Result<List<ChannelSubscription>> listSubscriptions() {
        String userId = "currentUserId";
        return Result.OK(subscriptionService.listByUser(userId));
    }

    @Operation(summary = "创建分组")
    @PostMapping("/group/create")
    public Result<ChannelSubscriptionGroup> createGroup(@RequestParam String groupName) {
        String userId = "currentUserId";
        return Result.OK(groupService.createGroup(userId, groupName));
    }

    @Operation(summary = "分组列表")
    @GetMapping("/group/list")
    public Result<List<ChannelSubscriptionGroup>> listGroups() {
        String userId = "currentUserId";
        return Result.OK(groupService.listByUser(userId));
    }

    @Operation(summary = "重命名分组")
    @PostMapping("/group/rename")
    public Result<String> renameGroup(@RequestParam String groupId, @RequestParam String newName) {
        String userId = "currentUserId";
        groupService.renameGroup(groupId, newName, userId);
        return Result.OK("已重命名");
    }

    @Operation(summary = "删除分组")
    @PostMapping("/group/delete")
    public Result<String> deleteGroup(@RequestParam String groupId) {
        String userId = "currentUserId";
        groupService.deleteGroup(groupId, userId);
        return Result.OK("已删除");
    }
}
