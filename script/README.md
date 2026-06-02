# JeecgBoot 运维脚本

macOS 一键全栈启停脚本（Homebrew + Maven + Vite）。每个脚本可独立运行，也可通过 `start_all.sh` / `stop_all.sh` 串行。

## 目录结构

```
script/
├── 00_genesis.sh          # 创世纪：检测/安装 brew 公式与工具链
├── 01_start_services.sh   # 启动 MySQL / Redis / PostgreSQL
├── 02_init_db.sh          # 创建 jeecg-boot 库 + 导入全量 SQL（幂等）
├── 03_start_backend.sh    # 启动 Java 后端（mvn spring-boot:run -P dev）
├── 04_start_frontend.sh   # 启动 Vue3 前端（pnpm dev）
├── start_all.sh           # master：00→04 串行
├── stop_all.sh            # 反向停止：前端 + 后端（第三方服务保留）
├── lib/
│   ├── env.sh             # 配置常量中心（端口/账号/路径/banner 关键字）
│   └── common.sh          # 通用函数（颜色日志/端口探测/进程管理/健康等待）
├── logs/                  # 运行时生成：backend.log / frontend.log / install.log
├── .pid/                  # 运行时生成：backend.pid / frontend.pid
├── start_services.sh      # 旧脚本，保留不维护
└── stop_services.sh       # 旧脚本，保留不维护
```

## 前置依赖

| 依赖 | 用途 | 检测方式 |
|---|---|---|
| Homebrew | 管理 mysql/redis/pg | `brew --version` |
| MySQL | 业务库（`jeecg-boot`） | `mysql --version` |
| Redis | 缓存 | `redis-cli --version` |
| PostgreSQL@17 | 可选，AI RAG 向量库 | `psql --version` |
| Maven 3.x | 后端构建 | `mvn --version` |
| JDK 21 | 后端运行 | `java -version` |
| pnpm | 前端包管理 | `pnpm --version` |
| lsof | 端口/进程探测（macOS 自带） | `lsof -v` |

`00_genesis.sh` 会自动检测以上工具并按需通过 brew 安装缺失项。

## 快速开始

```bash
# 一键全栈启动（首次运行 2-3 分钟，依赖下载 + DB 导入）
./script/start_all.sh

# 一键停止（前端 + 后端，第三方服务保留）
./script/stop_all.sh
```

启动完成后访问：
- 前端：http://localhost:3100
- 后端：http://localhost:8080/jeecg-boot
- Knife4j 接口文档：http://127.0.0.1:8080/jeecg-boot/doc.html

## 分步使用

```bash
./script/00_genesis.sh         # 1. 环境检查与 brew 公式补装
./script/01_start_services.sh  # 2. 启动 mysql/redis/pg
./script/02_init_db.sh         # 3. 初始化库与表（首次导入全量 SQL ~3s）
./script/03_start_backend.sh   # 4. 启动 Java 后端
./script/04_start_frontend.sh  # 5. 启动 Vue3 前端
```

## 端口与账号

修改 `lib/env.sh` 中的常量，**不要散落到子脚本**。

| 配置 | 默认值 | 说明 |
|---|---|---|
| `BACKEND_PORT` | 8080 | Spring Boot Tomcat 端口 |
| `BACKEND_CONTEXT` | `/jeecg-boot` | Servlet context path |
| `FRONTEND_PORT` | 3100 | Vite dev server |
| `MYSQL_HOST/PORT/USER/PASS` | 127.0.0.1:3306 root/root | 与 `application-dev.yml` 对齐 |
| `MYSQL_DB` | `jeecg-boot` | 库名 |
| `REDIS_HOST/PORT` | 127.0.0.1:6379 | |
| `PG_HOST/PORT/USER/PASS` | 127.0.0.1:5432 postgres/postgres | 仅 AI RAG 模块需要 |
| `BACKEND_BOOT_TIMEOUT` | 240s | 首次依赖下载+启动可能很慢 |
| `BACKEND_READY_PATTERN` | `Application Jeecg-Boot is running!` | 启动完成的日志关键字 |

后端启动完成判定：两阶段 —— ① 端口 LISTEN ② 扫描 `logs/backend.log` 命中 banner 关键字。OAuth2 鉴权下 `doc.html` 可能返回 401，**仅作冒烟探测，不作为启动判据**。

## 日志与 PID

```
script/logs/
├── backend.log     # Spring Boot stdout/stderr
├── frontend.log    # Vite dev server stdout/stderr
└── install.log     # brew install 输出（追加）

script/.pid/
├── backend.pid     # mvn spring-boot:run launcher pid
└── frontend.pid    # pnpm dev launcher pid
```

`stop_all.sh` 跑完后会清理 `.pid/` 目录；`logs/` 保留以便排查。

## 故障排查

| 现象 | 排查命令 |
|---|---|
| 后端启动后 banner 不出现 | `tail -n 100 script/logs/backend.log` |
| 端口 8080 仍被占用 | `lsof -nP -iTCP:8080 -sTCP:LISTEN` |
| 前端首页 502/连接拒绝 | 确认 03 已跑完：`curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/jeecg-boot/doc.html` |
| DB 导入失败 | `mysql -uroot -proot -e 'SHOW TABLES FROM jeecg-boot'` 检查表数量；删除库重跑 `02_init_db.sh` |
| 第三方服务未启动 | `brew services list`；手动 `brew services start mysql` |
| brew 安装卡住 | `tail -f script/logs/install.log` |

### 完全重置

```bash
./script/stop_all.sh                       # 停应用
brew services stop mysql redis             # 停第三方（PG 同理）
mysql -uroot -proot -e 'DROP DATABASE IF EXISTS `jeecg-boot`'
rm -rf script/logs/* script/.pid/*         # 清日志/PID
./script/start_all.sh                      # 重新走全流程
```

## 设计取舍

- **DB 初始化用全量 SQL 而非 Flyway**：dev profile 默认 `spring.flyway.enabled=false` 且 `FlywayAutoConfiguration` 被 `@SpringBootApplication` exclude；改用 Flyway 需要打开开关并补充 V3.9.1 之前的基线
- **后端健康探针用 banner 日志关键字 + 端口 LISTEN**：避开 OAuth2 鉴权白名单不确定性
- **第三方服务用 brew services**：与 Homebrew 生态一致，跨项目复用
- **MySQL 密码走 `MYSQL_PWD` 环境变量**：避免 `mysql -uroot -proot` 在 shell history 留痕
- **kill 兜底三重保险**（pattern + pidfile + port）：mvn launcher 杀完有时不杀子 java，必须用关键字再兜一次
- **stop 保留第三方服务**：避免与本地其他项目（如 docker-compose）冲突

## 已知限制

- **仅 macOS**：`00_genesis.sh` line 16 有 `uname -s` 守卫，Linux/Windows 需自行改写
- **设计冗余调用**：03 line 15 内部调 02、02 line 15 内部调 01，start_all 串行调用时 01/02 会被多调一次；但都是幂等操作，仅多几百毫秒日志
- **`set -u` + 全角字符坑**：bash 解析 `$VAR` 紧跟全角括号 `（）`、中文逗号 `，`、中文分号 `；` 等多字节字符时，会把变量名延伸导致 `unbound variable`；本仓库已统一用 `${VAR}` 显式花括号规避
- **后端首次启动 20-30s**：依赖首次下载 + Spring 容器初始化；后续 15s 左右

## 维护要点

- 修改端口/账号/库名 → 改 `lib/env.sh`
- 修改启动命令（如换 profile） → 改 `lib/env.sh` 中 `MVN_PROFILE` 或 `03_start_backend.sh` 中 `MVN_CMD` 数组
- 启动判定逻辑变更 → 改 `BACKEND_READY_PATTERN` 或重写 `03_start_backend.sh` 第 66-86 行
- 新增子脚本 → 按 `NN_name.sh` 命名，追加到 `start_all.sh` 串行列表
