import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

const userStatus: AppRouteModule = {
  path: '/content/user-status',
  name: 'UserStatus',
  component: LAYOUT,
  redirect: '/content/user-status/manage',
  meta: {
    orderNo: 50,
    icon: 'ant-design:user-switch-outlined',
    title: '用户状态管理',
  },
  children: [
    {
      path: 'manage',
      name: 'UserStatusManage',
      component: () => import('/@/views/content/user-status/manage/index.vue'),
      meta: {
        title: '状态管理',
      },
    },
    {
      path: 'audit-log',
      name: 'UserStatusAuditLog',
      component: () => import('/@/views/content/user-status/audit-log/index.vue'),
      meta: {
        title: '审计日志',
      },
    },
  ],
};

const userAccount: AppRouteModule = {
  path: '/user',
  name: 'UserAccount',
  component: LAYOUT,
  redirect: '/user/account-status',
  meta: {
    orderNo: 90,
    icon: 'ant-design:user-outlined',
    title: '个人中心',
  },
  children: [
    {
      path: 'account-status',
      name: 'AccountStatus',
      component: () => import('/@/views/user/account-status/index.vue'),
      meta: {
        title: '账号状态',
      },
    },
  ],
};

export default [userStatus, userAccount];
