import type { AppRouteRecordRaw } from '/@/router/types';

const ContentChannelAdminRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/channel/admin',
    name: 'ContentChannelAdmin',
    component: () => import('/@/views/content/channel/admin/index.vue'),
    meta: { title: '频道管理', hideMenu: true },
  },
  {
    path: '/content/channel/review',
    name: 'ContentChannelReview',
    component: () => import('/@/views/content/channel/review/index.vue'),
    meta: { title: '审核队列', hideMenu: true },
  },
];

export default ContentChannelAdminRoutes;
