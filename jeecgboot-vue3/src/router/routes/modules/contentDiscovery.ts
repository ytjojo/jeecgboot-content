import type { AppRouteRecordRaw } from '/@/router/types';

const ContentDiscoveryRoutes: AppRouteRecordRaw[] = [
  {
    path: '/channel/discovery',
    name: 'ChannelDiscovery',
    component: () => import('/@/views/content/channel/discovery/index.vue'),
    meta: { title: '频道发现', hideMenu: true },
  },
  {
    path: '/channel/category',
    name: 'ChannelCategoryBrowse',
    component: () => import('/@/views/content/channel/category/index.vue'),
    meta: { title: '分类浏览', hideMenu: true },
  },
  {
    path: '/channel/search',
    name: 'ChannelSearch',
    component: () => import('/@/views/content/channel/search/index.vue'),
    meta: { title: '频道搜索', hideMenu: true },
  },
  {
    path: '/channel/ranking',
    name: 'ChannelRanking',
    component: () => import('/@/views/content/channel/ranking/index.vue'),
    meta: { title: '频道排行榜', hideMenu: true },
  },
  {
    path: '/channel/category-manage',
    name: 'ChannelCategoryManage',
    component: () => import('/@/views/content/channel/category-manage/index.vue'),
    meta: { title: '分类管理', hideMenu: true },
  },
  {
    path: '/channel/editorial-pick-manage',
    name: 'ChannelEditorialPickManage',
    component: () => import('/@/views/content/channel/editorial-pick-manage/index.vue'),
    meta: { title: '编辑精选管理', hideMenu: true },
  },
];

export default ContentDiscoveryRoutes;
