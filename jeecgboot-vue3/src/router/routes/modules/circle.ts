import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

const circle: AppRouteModule = {
  path: '/circle',
  name: 'Circle',
  component: LAYOUT,
  redirect: '/circle/list',
  meta: {
    orderNo: 21,
    icon: 'mdi:circle-outline',
    title: '圈子',
  },
  children: [
    {
      path: 'list',
      name: 'CircleList',
      component: () => import('/@/views/circle/List.vue'),
      meta: {
        title: '圈子列表',
        ignoreKeepAlive: false,
      },
    },
    {
      path: 'create',
      name: 'CircleCreate',
      component: () => import('/@/views/circle/Create.vue'),
      meta: {
        title: '创建圈子',
      },
    },
    {
      path: ':id',
      name: 'CircleDetail',
      component: () => import('/@/views/circle/Detail.vue'),
      meta: {
        title: '圈子详情',
      },
    },
    {
      path: ':id/edit',
      name: 'CircleEdit',
      component: () => import('/@/views/circle/Edit.vue'),
      meta: {
        title: '编辑圈子',
      },
    },
    {
      path: ':id/members',
      name: 'CircleMembers',
      component: () => import('/@/views/circle/Members.vue'),
      meta: {
        title: '成员管理',
      },
    },
    {
      path: 'search',
      name: 'CircleSearch',
      component: () => import('/@/views/circle/Search.vue'),
      meta: {
        title: '圈子搜索',
      },
    },
    {
      path: ':id/governance-log',
      name: 'CircleGovernanceLog',
      component: () => import('/@/views/circle/GovernanceLog.vue'),
      meta: {
        title: '治理日志',
      },
    },
  ],
};

export default circle;
