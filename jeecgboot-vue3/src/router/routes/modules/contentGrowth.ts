import type { AppRouteRecordRaw } from '/@/router/types';

const ContentGrowthRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/my-badges',
    name: 'ContentMyBadges',
    component: () => import('/@/views/content/growth/my-badges/index.vue'),
    meta: { title: '我的勋章', hideMenu: true },
  },
  {
    path: '/content/point-detail',
    name: 'ContentPointDetail',
    component: () => import('/@/views/content/growth/point-detail/index.vue'),
    meta: { title: '积分明细', hideMenu: true },
  },
  {
    path: '/content/point-mall',
    name: 'ContentPointMall',
    component: () => import('/@/views/content/growth/point-mall/index.vue'),
    meta: { title: '积分商城', hideMenu: true },
  },
  {
    path: '/content/my-level',
    name: 'ContentMyLevel',
    component: () => import('/@/views/content/growth/my-level/index.vue'),
    meta: { title: '我的等级', hideMenu: true },
  },
  {
    path: '/content/badge-manage',
    name: 'ContentBadgeManage',
    component: () => import('/@/views/content/growth/badge-manage/index.vue'),
    meta: { title: '勋章管理', hideMenu: true, authority: ['admin'] },
  },
];

export default ContentGrowthRoutes;
