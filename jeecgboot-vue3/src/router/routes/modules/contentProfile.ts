import type { AppRouteRecordRaw } from '/@/router/types';

const ContentProfileRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/profile/edit',
    name: 'ContentProfileEdit',
    component: () => import('/@/views/content/profile/edit/index.vue'),
    meta: { title: '编辑资料', hideMenu: true },
  },
  {
    path: '/content/profile/homepage-settings',
    name: 'ContentProfileHomepageSettings',
    component: () => import('/@/views/content/profile/homepage-settings/index.vue'),
    meta: { title: '主页设置', hideMenu: true },
  },
  {
    path: '/content/profile/privacy',
    name: 'ContentProfilePrivacy',
    component: () => import('/@/views/content/profile/privacy/index.vue'),
    meta: { title: '隐私设置', hideMenu: true },
  },
  {
    path: '/content/profile/history',
    name: 'ContentProfileHistory',
    component: () => import('/@/views/content/profile/history/index.vue'),
    meta: { title: '历史记录', hideMenu: true },
  },
];

export default ContentProfileRoutes;
