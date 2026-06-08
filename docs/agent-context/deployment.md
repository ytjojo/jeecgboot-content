# 部署与排障

## 常用环境变量
### 后端
```bash
SPRING_PROFILES_ACTIVE=dev
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=jeecg-boot
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 前端

环境变量文件：
- `.env` — 基础配置（端口 3100、应用标题、SSO/Qiankun 开关）
- `.env.development` — 开发环境（mock 启用、代理到 `localhost:8080/jeecg-boot`）
- `.env.production` — 生产环境（mock 关闭、gzip 压缩）
- `.env.docker` — Docker 生产构建配置
- `.env.dockercloud` — Docker 云构建配置
- `.env.prod_electron` — Electron 生产构建配置

常用变量：
```bash
VITE_GLOB_API_URL=http://localhost:8080/jeecg-boot
VITE_GLOB_DOMAIN_URL=http://localhost:8080/jeecg-boot
VITE_PROXY=["/jeecgboot","http://localhost:8080/jeecg-boot"]
```

> `VITE_GLOB_*` 变量在构建后通过 `dist/_app.config.js` 注入，可在不重新构建的情况下修改。

## 生产部署
- 后端：`mvn clean package -P prod`
- 前端：`pnpm build`
- Docker：使用仓库内的 Compose 或 Dockerfile
- 生产参数放对应环境配置文件，不写入规则文件

### 前端构建细节

Manual chunks 分包策略：
- `vue-vendor` — Vue 核心
- `antd-vue-vendor` — Ant Design Vue
- `vxe-table-vendor` — vxe-table
- `emoji-mart-vue-fast` — 表情选择器
- `china-area-data-vendor` — 中国省市区数据

Post-build 脚本：
- `build/script/postBuild.ts` — 生成运行时配置（`dist/_app.config.js`）
- `build/script/copyChat.ts` — 复制聊天相关资源

生产环境通过 esbuild 自动移除 `console` 和 `debugger` 语句。

## 微服务部署
- 启动 Nacos
- 部署网关服务
- 部署业务服务
- 前端配置网关访问地址

## 常见排障点
- 端口冲突：后端 `8080`、MySQL `3306`、Redis `6379`
- 数据库连接异常：检查 `application.yml` 或环境变量
- 前端代理异常：检查 `vite.config.ts` 或 `.env.*`
- 微服务异常：确认 Nacos、Redis 和依赖中间件已启动
