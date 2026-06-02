import type { AppRouteRecordRaw } from '/@/router/types';

const ContentAuthRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/login',
    name: 'ContentLogin',
    component: () => import('/@/views/content/auth/login/index.vue'),
    meta: { title: '登录', ignoreAuth: true, hideMenu: true },
  },
  {
    path: '/content/register',
    name: 'ContentRegister',
    component: () => import('/@/views/content/auth/register/index.vue'),
    meta: { title: '注册', ignoreAuth: true, hideMenu: true },
  },
  {
    path: '/content/forgot-password',
    name: 'ContentForgotPassword',
    component: () => import('/@/views/content/auth/forgot-password/index.vue'),
    meta: { title: '找回密码', ignoreAuth: true, hideMenu: true },
  },
  {
    path: '/content/account-security',
    name: 'ContentAccountSecurity',
    component: () => import('/@/views/content/auth/account-security/index.vue'),
    meta: { title: '账号安全', hideMenu: true },
  },
  {
    path: '/content/account-security/devices',
    name: 'ContentAccountSecurityDevices',
    component: () => import('/@/views/content/auth/account-security/devices/index.vue'),
    meta: { title: '设备管理', hideMenu: true },
  },
  {
    path: '/content/account-security/cancellation',
    name: 'ContentAccountSecurityCancellation',
    component: () => import('/@/views/content/auth/account-security/cancellation/index.vue'),
    meta: { title: '账号注销', hideMenu: true },
  },
];

export default ContentAuthRoutes;
