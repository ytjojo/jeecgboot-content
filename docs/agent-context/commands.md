# 常用命令

## 后端

### 编译
```bash
cd jeecg-boot
mvn clean install
```

```bash
cd jeecg-boot
mvn clean install -P SpringCloud
```

### 启动（单体架构）
```bash
cd jeecg-boot/jeecg-module-system/jeecg-system-start
mvn spring-boot:run
```

> 启动入口为 `jeecg-system-start`，内容社区模块（`jeecg-module-content`）作为依赖自动加载，无需单独启动。

### 内容社区模块 — 单元测试

全量测试：
```bash
cd jeecg-boot
mvn test -pl jeecg-boot-module/jeecg-module-content -am
```

单个测试类：
```bash
cd jeecg-boot
mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=CircleMemberServiceTest
```

单个测试方法：
```bash
cd jeecg-boot
mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=CircleMemberServiceTest#testJoinCircle
```

> `-am` 会自动构建依赖模块（base-core、system-biz 等），首次运行或依赖变更时必须带。

## 前端

### 开发
```bash
cd jeecgboot-vue3
pnpm install
pnpm dev
```

### 构建
```bash
cd jeecgboot-vue3
pnpm build
pnpm build:docker
pnpm build:dockercloud
pnpm build:report      # 构建 + 包体积分析
pnpm preview           # 构建 + 预览
```

### 代码检查与格式化
```bash
cd jeecgboot-vue3
npx eslint src/path/to/file.vue              # ESLint 检查单个文件
npx stylelint "src/**/*.{vue,less,css}"      # Stylelint 样式检查
pnpm batch:prettier                          # Prettier 格式化所有 src 文件
```

### 工具命令
```bash
cd jeecgboot-vue3
pnpm clean:cache      # 清除 Vite 缓存
pnpm gen:icon         # 重新生成图标数据
pnpm reinstall        # 清除 node_modules 并重新安装
```

### 前端单元测试（Vitest）

全量测试：
```bash
cd jeecgboot-vue3
npx vitest run
```

单个测试文件：
```bash
cd jeecgboot-vue3
npx vitest run src/views/channel/__tests__/channelPublishStore.test.ts
```

Watch 模式（开发时持续监听）：
```bash
cd jeecgboot-vue3
npx vitest
```

覆盖率报告：
```bash
cd jeecgboot-vue3
npx vitest run --coverage
```

> 测试文件约定：`__tests__/*.test.ts` 或同目录 `*.spec.ts`。
> 测试环境：jsdom，setup 文件位于 `tests/setup.ts`。

## Docker
```bash
docker-compose up -d
```

## 说明
- 前端默认使用 `pnpm`，测试框架为 Vitest
- 后端默认使用 `mvn`，测试框架为 JUnit 5
- 命令可能随模块演进而变化，若与模块内文档冲突，以模块内文档为准
