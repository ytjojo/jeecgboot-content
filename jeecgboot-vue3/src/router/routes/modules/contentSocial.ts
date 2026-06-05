import type { AppRouteRecordRaw } from '/@/router/types';

const ContentSocialRoutes: AppRouteRecordRaw[] = [
  {
    path: '/content/mutual-follow',
    name: 'ContentMutualFollow',
    component: () => import('/@/views/content/mutual-follow/MutualFollowList.vue'),
    meta: { title: '互关好友', hideMenu: true },
  },
  {
    path: '/content/fan',
    name: 'ContentFan',
    component: () => import('/@/views/content/fan/FanList.vue'),
    meta: { title: '粉丝管理', hideMenu: true },
  },
  {
    path: '/content/invite',
    name: 'ContentInvite',
    component: () => import('/@/views/content/invite/InviteShare.vue'),
    meta: { title: '邀请好友', hideMenu: true },
  },
  {
    path: '/invite/:inviteCode',
    name: 'InviteLanding',
    component: () => import('/@/views/content/invite/LandingPage.vue'),
    meta: { title: '邀请注册', ignoreAuth: true, hideMenu: true },
  },
];

export default ContentSocialRoutes;
