# JeecgBoot前端配置服务器地址方式

## 1. 概述

JeecgBoot前端项目基于Vue3构建，支持多种环境配置方式来设置后端服务器地址。本文档详细说明了各种配置方式和最佳实践。

## 2. 环境配置文件

### 2.1 配置文件说明

JeecgBoot前端项目根目录下包含多个环境配置文件：

```
jeecgboot-vue3/
├── .env                    # 所有环境的默认配置
├── .env.development        # 开发环境配置
├── .env.production         # 生产环境配置
├── .env.docker            # Docker环境配置
├── .env.dockercloud       # Docker云环境配置
└── .env.prod_electron     # Electron生产环境配置
```

### 2.2 配置文件优先级

配置文件加载优先级（从高到低）：
1. `.env.[mode].local` - 本地环境特定配置（被git忽略）
2. `.env.local` - 本地环境配置（被git忽略）
3. `.env.[mode]` - 环境特定配置
4. `.env` - 默认配置

## 3. 服务器地址配置

### 3.1 开发环境配置

**文件：`.env.development`**

```bash
# 开发环境配置
NODE_ENV=development

# 后端API地址
VITE_GLOB_API_URL=http://localhost:8080

# 文件上传地址
VITE_GLOB_UPLOAD_URL=http://localhost:8080/sys/common/upload

# WebSocket地址
VITE_GLOB_WEBSOCKET_URL=ws://localhost:8080/websocket

# 是否开启代理
VITE_USE_PROXY=true

# 代理配置前缀
VITE_GLOB_API_URL_PREFIX=/jeecgboot
```

### 3.2 生产环境配置

**文件：`.env.production`**

```bash
# 生产环境配置
NODE_ENV=production

# 后端API地址（生产环境）
VITE_GLOB_API_URL=https://your-domain.com

# 文件上传地址
VITE_GLOB_UPLOAD_URL=https://your-domain.com/sys/common/upload

# WebSocket地址
VITE_GLOB_WEBSOCKET_URL=wss://your-domain.com/websocket

# 关闭代理
VITE_USE_PROXY=false

# API前缀
VITE_GLOB_API_URL_PREFIX=/jeecgboot
```

### 3.3 Docker环境配置

**文件：`.env.docker`**

```bash
# Docker环境配置
NODE_ENV=production

# 使用相对路径，由Nginx代理
VITE_GLOB_API_URL=/jeecgboot
VITE_GLOB_UPLOAD_URL=/jeecgboot/sys/common/upload
VITE_GLOB_WEBSOCKET_URL=ws://localhost/jeecgboot/websocket

VITE_USE_PROXY=false
VITE_GLOB_API_URL_PREFIX=
```

## 4. 代理配置

### 4.1 Vite代理配置

**文件：`vite.config.ts`**

```typescript
import { defineConfig } from 'vite';
import { createProxy } from './build/vite/proxy';

export default defineConfig({
  server: {
    host: true,
    port: 3100,
    // 开发环境代理配置
    proxy: createProxy([
      [
        '/jeecgboot',
        {
          target: 'http://localhost:8080',
          changeOrigin: true,
          ws: true,
          rewrite: (path) => path.replace(/^\/jeecgboot/, ''),
        },
      ],
    ]),
  },
});
```

### 4.2 代理配置文件

**文件：`build/vite/proxy.ts`**

```typescript
import type { ProxyOptions } from 'vite';

type ProxyItem = [string, string];
type ProxyList = ProxyItem[];
type ProxyTargetList = Record<string, ProxyOptions>;

/**
 * 创建代理配置
 * @param list 代理列表
 */
export function createProxy(list: ProxyList = []) {
  const ret: ProxyTargetList = {};
  for (const [prefix, target] of list) {
    const httpsRE = /^https:\/\//;
    const isHttps = httpsRE.test(target);

    ret[prefix] = {
      target: target,
      changeOrigin: true,
      ws: true,
      rewrite: (path: string) => path.replace(new RegExp(`^${prefix}`), ''),
      // https需要配置secure
      ...(isHttps ? { secure: false } : {}),
    };
  }
  return ret;
}
```

## 5. API请求配置

### 5.1 Axios配置

**文件：`src/utils/http/axios/index.ts`**

```typescript
import { VAxios } from './Axios';
import { AxiosTransform } from './axiosTransform';
import { checkStatus } from './checkStatus';
import { joinTimestamp, formatRequestDate } from './helper';
import { RequestEnum, ResultEnum, ContentTypeEnum } from '/@/enums/httpEnum';
import { globSetting } from '/@/settings/globSetting';

// 获取环境变量中的API地址
const { apiUrl, urlPrefix } = globSetting;

/**
 * 创建axios实例
 */
function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    deepMerge(
      {
        // 基础URL
        baseURL: apiUrl,
        // 请求超时时间
        timeout: 10 * 1000,
        // 携带Cookie
        withCredentials: true,
        transform,
        requestOptions: {
          // 是否添加url前缀
          urlPrefix: urlPrefix,
          // 是否添加时间戳
          joinTime: true,
          // 忽略重复请求
          ignoreCancelToken: true,
          // 是否携带token
          withToken: true,
        },
      },
      opt || {}
    )
  );
}

export const defHttp = createAxios();
```

### 5.2 全局设置配置

**文件：`src/settings/globSetting.ts`**

```typescript
import { getAppEnvConfig } from '/@/utils/env';

const {
  VITE_GLOB_API_URL,
  VITE_GLOB_API_URL_PREFIX,
  VITE_GLOB_UPLOAD_URL,
} = getAppEnvConfig();

export const globSetting: GlobConfig = {
  title: 'JeecgBoot',
  // API地址
  apiUrl: VITE_GLOB_API_URL,
  // API前缀
  urlPrefix: VITE_GLOB_API_URL_PREFIX,
  // 上传地址
  uploadUrl: VITE_GLOB_UPLOAD_URL,
};
```

## 6. 不同部署方式的配置

### 6.1 本地开发

```bash
# 启动开发服务器
npm run dev

# 或使用yarn
yarn dev
```

开发环境会自动加载 `.env.development` 配置，通过Vite代理转发请求到后端服务器。

### 6.2 生产环境部署

#### 方式一：直接部署

```bash
# 构建生产版本
npm run build

# 构建后的文件在dist目录
# 需要配置Web服务器（如Nginx）代理API请求
```

#### 方式二：Docker部署

```bash
# 使用Docker环境配置构建
npm run build:docker

# 或直接使用Docker构建
docker build -t jeecgboot-vue3 .
```

### 6.3 Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
    
    # 代理后端API
    location /jeecgboot/ {
        proxy_pass http://backend-server:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # WebSocket代理
    location /jeecgboot/websocket {
        proxy_pass http://backend-server:8080/websocket;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

## 7. 常见问题和解决方案

### 7.1 跨域问题

**问题**：前端请求后端API时出现跨域错误

**解决方案**：
1. 开发环境：使用Vite代理解决
2. 生产环境：配置Nginx代理或后端CORS

### 7.2 API地址配置错误

**问题**：请求404或连接失败

**解决方案**：
1. 检查环境配置文件中的API地址
2. 确认后端服务是否正常运行
3. 检查网络连接和防火墙设置

### 7.3 文件上传失败

**问题**：文件上传接口调用失败

**解决方案**：
1. 检查上传地址配置
2. 确认后端上传目录权限
3. 检查文件大小限制

## 8. 最佳实践

### 8.1 环境隔离

1. **开发环境**：使用本地后端服务，开启代理
2. **测试环境**：使用测试服务器地址
3. **生产环境**：使用生产服务器地址，关闭代理

### 8.2 配置管理

1. 敏感信息使用 `.env.local` 文件（不提交到版本控制）
2. 不同环境使用不同的配置文件
3. 生产环境配置通过CI/CD动态注入

### 8.3 安全考虑

1. 生产环境使用HTTPS
2. 配置合适的CORS策略
3. 不在前端代码中暴露敏感信息

## 9. 总结

JeecgBoot前端提供了灵活的服务器地址配置方式，支持多环境部署。通过合理配置环境变量和代理设置，可以满足不同场景下的部署需求。在实际使用中，建议根据具体的部署环境选择合适的配置方式，确保系统的稳定性和安全性。