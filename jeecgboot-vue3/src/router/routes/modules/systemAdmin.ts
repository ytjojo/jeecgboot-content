import type { AppRouteRecordRaw } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

// 系统治理审计日志（内容审核、违规记录等）
const systemAdminRoutes: AppRouteRecordRaw[] = [
  {
    path: '/system',
    name: 'SystemAdmin',
    component: LAYOUT,
    meta: {
      title: '系统管理',
      hideMenu: true,
    },
    children: [
      {
        path: 'audit-log',
        name: 'SystemAuditLog',
        component: () => import('/@/views/system/audit-log/index.vue'),
        meta: {
          title: '审计日志',
          hideMenu: true,
        },
      },
    ],
  },
];

export default systemAdminRoutes;
