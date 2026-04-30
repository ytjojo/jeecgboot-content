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
```bash
VITE_GLOB_API_URL=http://localhost:8080/jeecg-boot
VITE_GLOB_DOMAIN_URL=http://localhost:8080/jeecg-boot
VITE_PROXY=["/jeecgboot","http://localhost:8080/jeecg-boot"]
```

## 生产部署
- 后端：`mvn clean package -P prod`
- 前端：`pnpm build`
- Docker：使用仓库内的 Compose 或 Dockerfile
- 生产参数放对应环境配置文件，不写入规则文件

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
