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
├── src/api/
├── src/components/
├── src/views/
├── src/router/
├── src/store/
└── src/utils/
```

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
