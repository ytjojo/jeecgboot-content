# AGENTS.md

## 作用范围
本文件适用于 `jeecgboot-vue3/` 下的前端代码。
若本目录中的 `CLAUDE.md` 提供更细的实现背景，可作为补充参考。


## Frontend Architecture

- **Permission mode**: BACK — routes/menus fetched from backend API, dynamically registered
- **State**: Pinia stores in `src/store/modules/` (user, permission, app, locale, multipleTab)
- **API**: Custom Axios wrapper (`defHttp`) in `src/utils/http/axios/`, MD5 request signing
- **Path aliases**: `/@/` → `src/`, `/#/` → `types/`
- **Auto-import**: Ant Design Vue components auto-imported via unplugin
- **External packages**: `@jeecg/online` (online forms), `@jeecg/aiflow` (AI flow designer)


## 前端硬规则
- 使用 `pnpm` 作为包管理器
- 默认采用 Vue 3 + TypeScript + Vite + Ant Design Vue 既有栈和目录组织方式
- 统一使用现有 API 封装和 `src/api/` 目录，不绕开项目既有请求层
- 路径别名优先使用 `/@/` 指向 `src/`
- 涉及菜单、权限、动态路由时，先确认是否依赖后端返回，不直接破坏现有权限装配链路
- 优先复用现有组件、hooks、store 和工具函数，避免平行造轮子



## 参考
- 本地补充说明：`CLAUDE.md`
- 仓库级背景资料：`../docs/agent-context/project-overview.md`、`../docs/agent-context/commands.md`
