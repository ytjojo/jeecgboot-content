# 常用命令

## 后端
```bash
cd jeecg-boot
mvn clean install
```

```bash
cd jeecg-boot/jeecg-module-system/jeecg-system-start
mvn spring-boot:run
```

```bash
cd jeecg-boot
mvn clean install -P SpringCloud
```

## 前端
```bash
cd jeecgboot-vue3
pnpm install
pnpm dev
```

```bash
cd jeecgboot-vue3
pnpm build
pnpm build:docker
```

## Docker
```bash
docker-compose up -d
```

## 说明
- 前端默认使用 `pnpm`
- 后端默认使用 `mvn`
- 命令可能随模块演进而变化，若与模块内文档冲突，以模块内文档为准
