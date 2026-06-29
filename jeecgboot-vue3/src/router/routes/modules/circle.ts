import type { AppRouteModule } from '/@/router/types';
import type { RouteLocationNormalized } from 'vue-router';
import { LAYOUT } from '/@/router/constant';
import { getCircleDetail } from '/@/api/content/circle';
import { useCircleStoreWithOut } from '/@/store/modules/circle';
import { useMessage } from '/@/hooks/web/useMessage';

const { createMessage } = useMessage();

async function checkCirclePermission(
  to: RouteLocationNormalized,
  requireRole: 'admin' | 'member',
) {
  const circleId = to.params.id as string;
  const circleStore = useCircleStoreWithOut();

  try {
    const circle = await getCircleDetail(circleId);
    circleStore.setCurrentCircle(circle);

    if (requireRole === 'admin') {
      const isAdmin = circle.myRole === 'CREATOR' || circle.myRole === 'MODERATOR';
      if (!isAdmin) {
        createMessage.warning('您没有管理权限');
        return { path: `/circle/${circleId}`, replace: true };
      }
    }

    if (requireRole === 'member') {
      if (!circle.joined) {
        createMessage.warning('请先加入圈子');
        return { path: `/circle/${circleId}`, replace: true };
      }
    }

    return true;
  } catch {
    return { path: `/circle/${circleId}`, replace: true };
  }
}

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
      beforeEnter: (to) => checkCirclePermission(to, 'admin'),
    },
    {
      path: ':id/members',
      name: 'CircleMembers',
      component: () => import('/@/views/circle/Members.vue'),
      meta: {
        title: '成员管理',
      },
      beforeEnter: (to) => checkCirclePermission(to, 'member'),
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
      beforeEnter: (to) => checkCirclePermission(to, 'admin'),
    },
    {
      path: ':id/growth',
      name: 'CircleGrowth',
      component: () => import('/@/views/circle/growth/index.vue'),
      meta: {
        title: '成长中心',
      },
    },
    {
      path: ':id/badges',
      name: 'CircleBadges',
      component: () => import('/@/views/circle/badges/index.vue'),
      meta: {
        title: '徽章墙',
      },
    },
    {
      path: ':id/leaderboard',
      name: 'CircleLeaderboard',
      component: () => import('/@/views/circle/leaderboard/index.vue'),
      meta: {
        title: '排行榜',
      },
    },
  ],
};

export default circle;
