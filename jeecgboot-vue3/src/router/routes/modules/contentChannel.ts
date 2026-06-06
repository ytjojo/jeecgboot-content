import type { AppRouteRecordRaw } from '/@/router/types';

const ContentChannelRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/channel/create',
    name: 'ContentChannelCreate',
    component: () => import('/@/views/content/channel/create/index.vue'),
    meta: { title: '创建频道', hideMenu: true },
  },
  {
    path: '/content/channel/list',
    name: 'ContentChannelList',
    component: () => import('/@/views/content/channel/list/index.vue'),
    meta: { title: '我的频道', hideMenu: true },
  },
  {
    path: '/content/channel/manage/:id',
    name: 'ContentChannelManage',
    component: () => import('/@/views/content/channel/manage/index.vue'),
    meta: { title: '频道管理', hideMenu: true },
  },
];

export default ContentChannelRoutes;
