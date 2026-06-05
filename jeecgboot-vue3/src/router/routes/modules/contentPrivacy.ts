import type { AppRouteRecordRaw } from '/@/router/types';

const ContentPrivacyRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/privacy/settings',
    name: 'ContentPrivacySettings',
    component: () => import('/@/views/content/privacy/PrivacySettingsPage.vue'),
    meta: { title: '隐私设置', hideMenu: true },
  },
  {
    path: '/content/privacy/blacklist',
    name: 'ContentPrivacyBlacklist',
    component: () => import('/@/views/content/privacy/BlacklistPage.vue'),
    meta: { title: '黑名单管理', hideMenu: true },
  },
  {
    path: '/content/privacy/mute-list',
    name: 'ContentPrivacyMuteList',
    component: () => import('/@/views/content/privacy/MuteListPage.vue'),
    meta: { title: '屏蔽列表', hideMenu: true },
  },
  {
    path: '/content/privacy/keyword-filter',
    name: 'ContentPrivacyKeywordFilter',
    component: () => import('/@/views/content/privacy/KeywordFilterPage.vue'),
    meta: { title: '屏蔽词设置', hideMenu: true },
  },
];

export default ContentPrivacyRoutes;
