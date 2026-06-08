# 架构与目录

## 后端总体目录
```text
jeecg-boot/
├── db/
├── jeecg-boot-base-core/
├── jeecg-module-system/
├── jeecg-boot-module/
└── jeecg-server-cloud/
```

## 前端总体目录
```text
jeecgboot-vue3/
├── build/                    # Vite 插件、构建脚本、主题生成
├── src/
│   ├── api/                  # API 定义（sys/, common/, demo/, content/）
│   ├── components/jeecg/     # Jeecg 专用组件（JVxeTable, OnLine 等）
│   ├── layouts/default/      # 主布局（header, sider, tabs, menu）
│   ├── settings/             # 项目配置（design, components, locale, encryption）
│   ├── utils/http/axios/     # HTTP 客户端配置
│   ├── store/modules/        # Pinia 状态管理
│   ├── views/system/         # 系统管理页面
│   ├── views/super/          # 动态发现的扩展模块
│   └── locales/lang/         # 国际化语言文件
└── types/                    # 全局 TypeScript 类型声明
```

## 前端启动流程（src/main.ts）

`createApp` → createRouter → setupStore (pinia) → setupProps → i18n → initAppConfigStore → registerPackages (@jeecg/online) → registerGlobComp (Ant Design 核心组件) → SSO login → registerSuper (动态模块发现) → setupRouter → guards → directives → error handler → registerThirdComp (vxe-table, emoji, dayjs) → setupElectron → router.isReady() → mount

## 前端路由与权限

- **权限模式：BACK** — 路由和菜单从后端 API 获取，通过 `getBackMenuAndPerms()` 加载
- 动态路由在运行时添加，存储在 `src/store/modules/permission.ts`
- 静态路由：login、oauth2-login、token-login、error pages、AI dashboard
- 路由模式：HTML5 history（Electron 环境下使用 hash mode）
- Super 模块通过 `import.meta.glob('./**/register.ts')` 动态发现，入口在 `src/views/super/registerSuper.ts`

## 前端状态管理（Pinia）

核心 store（`src/store/modules/`）：
- `user.ts`（app-user）— auth token、用户信息、角色、租户、字典项
- `permission.ts`（app-permission）— 动态路由、权限码、后端菜单
- `app.ts`（app）— 项目配置、主题、布局设置
- `locale.ts`（app-locale）— 国际化语言
- `multipleTab.ts`（app-multiple-tab）— 标签页状态

认证信息通过 `src/utils/auth/index.ts` 持久化到 localStorage。

## 前端组件注册机制

- **自动导入**：`unplugin-vue-components` + `AntDesignVueResolver`，模板中直接使用 Ant Design Vue 组件无需手动 import
- **全局手动注册**：`registerGlobComp.ts` 注册 Icon、AIcon、JUploadButton、Button、TinyMCE Editor
- **第三方组件**：`registerThirdComp.ts` 注册 vxe-table（全量导入）、emoji picker、dayjs 插件
- **异步加载**：重型组件使用 `src/utils/factory/createAsyncComponent.tsx` 按需加载

## 前端图标系统

三种图标方案：
1. **Iconify 运行时** — `<Icon icon="mdi:home" />`，通过 CDN 懒加载
2. **SVG sprites** — `<Icon icon="icon-name|svg" />`，通过 `vite-plugin-svg-icons` 生成
3. **unplugin-icons** — `import IconName from '~icons/collection/name'`，编译时 tree-shaking

## 前端主题系统

- Less 变量由 `build/generate/generateModifyVars.ts` 生成
- 暗色模式通过 Ant Design Vue `theme.darkAlgorithm` 实现
- CSS 变量 `--j-global-primary-color` 动态设置主题色
- CSS 类前缀：`jeecg`（定义在 `src/settings/designSetting.ts`）

## 前端外部包

- `@jeecg/online` 和 `@jeecg/aiflow` 是外部 monorepo 包，因 CJS 兼容性问题被排除在 Vite optimizeDeps 之外
- 通过 `registerPackages(app)` 在 main.ts 中注册

## 前端性能优化模式

**关键：非关键模块使用动态导入**
- 文件顶层的静态 `import` 会导致整个依赖链在首屏加载
- 使用 `await import('module')` 或 `import('path').then()` 进行懒加载
- 关键文件：
  - `src/settings/registerThirdComp.ts` — vxe-table、emoji picker（mount 后加载）
  - `src/views/super/registerSuper.ts` — 动态模块发现
  - 非关键 Ant Design Vue 组件异步加载

**Vite optimizeDeps**
- 预打包依赖（`vite.config.ts`）：dayjs, axios, pinia, nprogress, qs, crypto-js, md5, sortablejs, xe-utils, vue-i18n, lodash-es, xss, mockjs
- 外部包（`@jeecg/*`）因 CJS 问题被排除

## 微前端（Qiankun）

- 可作为主应用（托管子应用）或子应用（嵌入父应用）运行
- 配置在 `src/qiankun/`，子应用通过 `VITE_APP_SUB_*` 环境变量配置
- 子应用模式通过 `VITE_GLOB_QIANKUN_MICRO_APP_NAME` 激活

## Electron 支持

- `src/electron/` — 使用 hash 路由模式
- 平台检测：`VITE_GLOB_RUN_PLATFORM === 'electron'`

## 内容社区模块目录约定
内容社区模块主要位于 `jeecg-boot/jeecg-boot-module/jeecg-module-content/`。

推荐分层如下：
```text
org.jeecg.modules.content.{module_name}/
├── controller/
├── biz/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── model/
├── vo/
├── req/
├── req/query/
├── req/create/
├── req/update/
├── dto/
├── config/
├── constant/
└── util/
```

## 分层职责
- `controller/`：REST API 入口，只负责参数接收、鉴权、调用服务和返回结果
- `biz/`：多表、多聚合、跨领域编排逻辑
- `service/`：单聚合或单表业务逻辑
- `mapper/`：MyBatis / MyBatis-Plus 持久层
- `entity/`：数据库实体
- `req/`：接口请求对象
- `vo/`：接口响应对象
- `dto/`：内部数据传输对象

## 文档管理建议
- 规则放 `AGENTS.md`
- 设计方案、技术说明、外部库笔记放普通文档
- 不在根规则文件中重复维护大段架构说明
