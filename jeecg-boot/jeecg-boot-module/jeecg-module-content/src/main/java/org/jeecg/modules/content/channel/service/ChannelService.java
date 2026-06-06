package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.req.ChannelListQuery;

public interface ChannelService extends IService<Channel> {

    /**
     * 校验名称在用户频道范围内是否唯一
     * @param name 频道名称
     * @param excludeId 排除的频道ID（编辑时排除自身）
     * @return true=唯一, false=已存在
     */
    boolean checkNameUnique(String name, String excludeId);

    /**
     * 查询用户的频道列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<Channel> listMyChannels(Page<Channel> page, String userId, ChannelListQuery query);

    /**
     * 查询所有频道列表（后台管理用，不限制 owner）
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<Channel> listAllChannels(Page<Channel> page, ChannelListQuery query);
}
