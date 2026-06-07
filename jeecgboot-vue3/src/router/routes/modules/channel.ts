import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

const channel: AppRouteModule = {
  path: '/channel',
  name: 'Channel',
  component: LAYOUT,
  redirect: '/channel/subscriptions',
  meta: {
    orderNo: 20,
    icon: 'mdi:television',
    title: '频道管理',
  },
  children: [
    {
      path: 'governance',
      name: 'ChannelGovernanceHome',
      component: () => import('/@/views/channel/governance/index.vue'),
      meta: {
        title: '内容治理',
      },
    },
    {
      path: 'subscriptions',
      name: 'ChannelSubscriptions',
      component: () => import('/@/views/channel/subscription/SubscriptionList.vue'),
      meta: {
        title: '我的订阅',
      },
    },
    {
      path: ':id/members',
      name: 'ChannelMembers',
      component: () => import('/@/views/channel/members/MemberList.vue'),
      meta: {
        title: '成员管理',
      },
    },
    {
      path: ':id/members/pending',
      name: 'ChannelPendingApplications',
      component: () => import('/@/views/channel/members/PendingApplications.vue'),
      meta: {
        title: '待审核申请',
      },
    },
    {
      path: ':id/blacklist',
      name: 'ChannelBlacklist',
      component: () => import('/@/views/channel/blacklist/BlacklistPage.vue'),
      meta: {
        title: '黑名单',
      },
    },
    {
      path: ':id/governance',
      name: 'ChannelGovernance',
      component: () => import('/@/views/channel/governance/index.vue'),
      meta: {
        title: '内容治理',
        ignoreKeepAlive: false,
      },
    },
    {
      path: ':id/governance/log',
      name: 'ChannelGovernanceLog',
      component: () => import('/@/views/channel/governance/GovernanceLog.vue'),
      meta: {
        title: '治理日志',
      },
    },
  ],
};

export default channel;
