
# JeecgBoot 添加菜单流程

  1. 前端路由（页面组件注册）

  在 src/router/routes/modules/xxx.ts 定义路由模块：

  import type { AppRouteModule } from '/@/router/types';
  import { LAYOUT } from '/@/router/constant';

  const channel: AppRouteModule = {
    path: '/channel',
    name: 'Channel',
    component: LAYOUT,
    meta: { orderNo: 20, icon: 'mdi:television', title: '社区' },
    children: [
      {
        path: 'governance',
        name: 'ChannelGovernance',
        component: () => import('/@/views/channel/governance/index.vue'),
        meta: { title: '内容治理' },
      },
    ],
  };
  export default channel;

  关键点：前端路由只负责页面加载，不会自动出现在左侧菜单。

  ---
  2. 后端 SQL（菜单权限注册）
  
  JeecgBoot 使用 PermissionModeEnum.BACK，菜单从 sys_permission 表读取。通过 Flyway SQL 迁移添加。

  一级菜单（menu_type=0）：
  INSERT INTO `sys_permission`
    (id, parent_id, name, url, component, menu_type, sort_no, icon, is_leaf, hidden, del_flag, status, create_by, create_time)
  VALUES
    ('1930223114757612001', '', '社区', '/channel', 'layouts/default/index',
     0, 20.00, 'mdi:television', 0, 0, 0, '1', 'admin', NOW());

  子菜单（menu_type=1）：
  INSERT INTO `sys_permission`
    (id, parent_id, name, url, component, menu_type, sort_no, icon, is_leaf, hidden, del_flag, status, create_by, create_time)
  VALUES
    ('1930223114757612002', '1930223114757612001', '内容治理', '/channel/governance',
     'channel/governance/index', 1, 1.00, 'ant-design:audit-twotone', 1, 0, 0, '1', 'admin', NOW());

  SQL 字段说明：

  ┌───────────┬──────────────┬───────────────────────┬──────────────────────┐
  │   字段    │     说明     │       一级菜单        │        子菜单        │
  ├───────────┼──────────────┼───────────────────────┼──────────────────────┤
  │ parent_id │ 父菜单ID     │ 空字符串              │ 父菜单的 id          │
  ├───────────┼──────────────┼───────────────────────┼──────────────────────┤
  │ url       │ 路由路径     │ /channel              │ /channel/governance  │
  ├───────────┼──────────────┼───────────────────────┼──────────────────────┤
  │ component │ 前端组件     │ layouts/default/index │ 对应路由的 component │
  ├───────────┼──────────────┼───────────────────────┼──────────────────────┤
  │ menu_type │ 菜单类型     │ 0                     │ 1                    │
  ├───────────┼──────────────┼───────────────────────┼──────────────────────┤
  │ is_leaf   │ 是否叶子节点 │ 0（有子菜单）         │ 1（无子菜单）        │
  ├───────────┼──────────────┼───────────────────────┼──────────────────────┤
  │ sort_no   │ 排序号       │ 越小越靠前            │ 同上                 │
  └───────────┴──────────────┴───────────────────────┴──────────────────────┘

  ---
  3. 对应关系
  
  前端路由 (channel.ts)          后端菜单 (sys_permission)
  ─────────────────────         ─────────────────────────
  path: '/channel'        ←→    url: '/channel'           (一级菜单)
    path: 'governance'    ←→    url: '/channel/governance' (子菜单)
    component: 'xxx'      ←→    component: 'xxx'           (必须一致)

  流程总结：前端路由定义页面 → 后端 SQL 注册菜单 → 两者通过 url 和 component 对应 → 页面出现在左侧菜单栏。