import type { AppRouteRecordRaw } from '/@/router/types';

const ContentChannelAdminRoutes: AppRouteRecordRaw[] = [
  // 频道管理
  {
    path: '/content/channel/admin',
    name: 'ContentChannelAdmin',
    component: () => import('/@/views/content/channel/admin/index.vue'),
    meta: { title: '频道管理', hideMenu: true },
  },
  // 数据看板
  {
    path: '/content/channel/stats',
    name: 'ContentChannelStats',
    component: () => import('/@/views/content/channel/stats/index.vue'),
    meta: { title: '数据看板', hideMenu: true },
  },
  // 数据导出
  {
    path: '/content/channel/export',
    name: 'ContentChannelExport',
    component: () => import('/@/views/content/channel/export/index.vue'),
    meta: { title: '数据导出', hideMenu: true },
  },
  // 审核队列
  {
    path: '/content/channel/review',
    name: 'ContentChannelReview',
    component: () => import('/@/views/content/channel/review/index.vue'),
    meta: { title: '审核队列', hideMenu: true },
  },
  // 频道治理后台
  {
    path: '/content/channel/governance',
    name: 'ContentChannelGovernance',
    component: () => import('/@/views/content/channel/governance/index.vue'),
    meta: { title: '频道治理', hideMenu: true },
  },
  // 频道治理详情
  {
    path: '/content/channel/governance/:channelId',
    name: 'ContentChannelGovernanceDetail',
    component: () => import('/@/views/content/channel/governance/detail/index.vue'),
    meta: { title: '治理详情', hideMenu: true },
  },
  // 审计日志
  {
    path: '/content/channel/audit-log',
    name: 'ContentChannelAuditLog',
    component: () => import('/@/views/content/channel/audit-log/index.vue'),
    meta: { title: '审计日志', hideMenu: true },
  },
  // 申诉管理
  {
    path: '/content/channel/appeal',
    name: 'ContentChannelAppeal',
    component: () => import('/@/views/content/channel/appeal/index.vue'),
    meta: { title: '申诉管理', hideMenu: true },
  },
];

export default ContentChannelAdminRoutes;
