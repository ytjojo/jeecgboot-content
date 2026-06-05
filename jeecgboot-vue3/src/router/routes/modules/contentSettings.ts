import type { AppRouteRecordRaw } from '/@/router/types';

const ContentSettingsRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/settings/notification',
    name: 'ContentNotificationSettings',
    component: () => import('/@/views/content/settings/notification/index.vue'),
    meta: { title: '通知设置', hideMenu: true },
  },
  {
    path: '/content/settings/privacy',
    name: 'ContentPrivacySettings',
    component: () => import('/@/views/content/settings/privacy/index.vue'),
    meta: { title: '隐私设置', hideMenu: true },
  },
  {
    path: '/content/settings/third-party',
    name: 'ContentThirdPartyAuth',
    component: () => import('/@/views/content/settings/third-party/index.vue'),
    meta: { title: '第三方授权管理', hideMenu: true },
  },
  {
    path: '/content/settings/security',
    name: 'ContentAccountSecurity',
    component: () => import('/@/views/content/settings/security/index.vue'),
    meta: { title: '账户安全', hideMenu: true },
  },
];

export default ContentSettingsRoutes;
