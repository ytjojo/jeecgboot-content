## JeecgBoot配置管理最佳实践：优雅实现个人配置不影响团队开发

针对您提到的文件上传路径配置问题，以下是几种优雅的解决方案：

### 1. 使用Spring Boot配置文件优先级机制（推荐）

**方案一：创建本地配置文件**

在 <mcfile name="application-dev.yml" path="/Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml"></mcfile> 同级目录创建：

```yaml
# application-dev-local.yml (个人本地配置)
jeecg:
  path:
    upload: /Users/yangtengjiao/Documents/j2ee/JeecgBoot/upload
    webapp: /Users/yangtengjiao/Documents/j2ee/JeecgBoot/webapp
```

然后启动时指定profile：
```bash
java -jar app.jar --spring.profiles.active=dev,dev-local
```

**方案二：使用application-local.yml**

创建 `application-local.yml` 文件：
```yaml
# application-local.yml (本地环境配置，优先级最高)
jeecg:
  path:
    upload: /Users/yangtengjiao/Documents/j2ee/JeecgBoot/upload
    webapp: /Users/yangtengjiao/Documents/j2ee/JeecgBoot/webapp
```

### 2. 使用环境变量覆盖（最灵活）

**方案三：环境变量配置**

在IDE或启动脚本中设置环境变量：
```bash
export JEECG_PATH_UPLOAD=/Users/yangtengjiao/Documents/j2ee/JeecgBoot/upload
export JEECG_PATH_WEBAPP=/Users/yangtengjiao/Documents/j2ee/JeecgBoot/webapp
```

或在IDEA中配置Environment variables：
```
JEECG_PATH_UPLOAD=/Users/yangtengjiao/Documents/j2ee/JeecgBoot/upload
JEECG_PATH_WEBAPP=/Users/yangtengjiao/Documents/j2ee/JeecgBoot/webapp
```

### 3. 使用JVM系统属性

**方案四：启动参数配置**

```bash
java -jar app.jar \
  --jeecg.path.upload=/Users/yangtengjiao/Documents/j2ee/JeecgBoot/upload \
  --jeecg.path.webapp=/Users/yangtengjiao/Documents/j2ee/JeecgBoot/webapp
```

### 4. Git忽略配置

**更新.gitignore文件**

在项目根目录的 <mcfile name=".gitignore" path="/Users/yangtengjiao/Documents/j2ee/JeecgBoot/jeecg-boot/.gitignore"></mcfile> 中添加：

```gitignore
# 本地配置文件
**/application-local.yml
**/application-*-local.yml
**/application-local.properties
**/application-*-local.properties
```

### 5. Spring Boot配置文件加载优先级

根据Spring Boot官方文档，配置文件加载优先级（从高到低）：

1. **命令行参数** `--jeecg.path.upload=xxx`
2. **系统环境变量** `JEECG_PATH_UPLOAD=xxx`
3. **application-{profile}-local.yml** (本地profile配置)
4. **application-local.yml** (本地配置)
5. **application-{profile}.yml** (环境配置)
6. **application.yml** (默认配置)

### 6. 推荐的团队开发配置策略

**最佳实践组合：**

1. **团队共享配置**：保持 `application-dev.yml` 中的通用配置
2. **个人本地配置**：创建 `application-local.yml` 覆盖个人路径
3. **Git忽略**：将本地配置文件加入 `.gitignore`
4. **文档说明**：在README中说明本地配置方式

**示例配置结构：**
```
resources/
├── application.yml              # 基础配置
├── application-dev.yml          # 开发环境配置（团队共享）
├── application-test.yml         # 测试环境配置
├── application-prod.yml         # 生产环境配置
└── application-local.yml        # 本地个人配置（git忽略）
```

### 7. 实施步骤

1. **创建本地配置文件** `application-local.yml`
2. **配置个人路径**
3. **更新.gitignore** 忽略本地配置
4. **团队文档** 说明本地配置规范
5. **验证配置** 确保不影响其他开发者

这种方式既保证了个人开发环境的灵活性，又不会影响团队其他成员的配置，是企业级项目配置管理的标准做法。
        