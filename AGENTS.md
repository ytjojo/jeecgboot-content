# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**JeecgBoot** is an enterprise-level AI low-code platform with the following characteristics:
- **Architecture**: Front-end and back-end separation, supporting both monolithic and microservices
- **Business Model**: AI-powered low-code development platform with AI application capabilities
- **Version**: 3.8.2 (SpringBoot3 branch) 
- **Purpose**: Enterprise applications, MIS, OA, ERP, CRM, AI knowledge base systems

## 架构模式
JeecgBoot后端模块部署模式采用单体架构（Monolithic）部署和微服务架构（Microservices）部署两种模式：

### 单体架构（Monolithic）
适用于中小型项目
所有功能模块部署在一个应用中
包含系统管理、示例模块等功能
### 微服务架构（Microservices）
适用于大型企业级项目
各个服务独立部署、独立扩展
通过Nacos进行服务注册与发现
## Technology Stack

### Backend (Java)
- **Framework**: Spring Boot 3.4.5 + Spring Cloud 2024.0.1 + Spring Cloud Alibaba 2023.0.3.2
- **Security**: Shiro 2.0.4 + JWT 4.5.0
- **ORM**: 
  MyBatis-Plus 3.5.12 
  Hibernate 5.6.7 
  Druid 1.2.24: 数据库连接池，提供监控和统计功能
  Dynamic DataSource 4.1.3: 动态数据源，支持多数据源切换
- **Database**: MySQL 8.0+ (default), (PostgreSQL, Oracle, SQL Server, 人大金仓, 达梦 ,support but not use)
- **Cache**: Redis
- **Message Queue**: RabbitMQ / RocketMQ
- **Registry**: Nacos 2.0.4
- **Flow Control**: Sentinel 1.8.3: 流量控制、熔断降级组件
- **API Docs**: Knife4j 4.6.0 (Swagger/OpenAPI)
- **Utility And Supporting Libraries**
  Hutool 5.8.25: Java工具库，提供常用工具方法
  Lombok (Spring Boot 3.5.5 管理): 简化Java代码编写
  FastJSON 2.0.57: 阿里巴巴JSON处理库
  Apache Commons 2.6: Apache通用工具库
- **Other Components**
  XXL-JOB 2.4.1: 分布式任务调度平台
  阿里云 OSS 3.17.3: 阿里云 OSS 对象存储服务(默认)
  MinIO 8.5.7: 对象存储服务
  WebSocket (Spring Boot 3.5.5 管理): 实时通信支持
  Quartz (Spring Boot 3.5.5 管理): 任务调度框架

### Frontend (Vue3)
- **Framework**: Vue 3.5.13 + TypeScript + Vite 6.0.7
- **UI**: Ant Design Vue 4.2.6
- **State Management**: Pinia 2.1.7
- **Build Tools**: pnpm (Node.js 20+)

## Project Structure

### Backend Structure
```
jeecg-boot/
├── db/                          # Database scripts
├── jeecg-boot-base-core/        # Core module (common utilities)
├── jeecg-module-system/         # System management module
│   ├── jeecg-system-api/        # System API definitions
│   ├── jeecg-system-biz/        # System business logic
│   └── jeecg-system-start/      # System startup module
├── jeecg-boot-module/           # Business modules
│   ├── jeecg-boot-module-airag/ # AI-related module
│   ├── jeecg-module-content/    # Content module (e.g., articles, post,note,question,answer,comment,channel,community)
│   └── jeecg-module-demo/       # Demo module
└── jeecg-server-cloud/          # Microservices
    ├── jeecg-cloud-gateway/     # Gateway service
    ├── jeecg-cloud-nacos/       # Configuration center
    ├── jeecg-system-cloud-start/# System service
    └── jeecg-demo-cloud-start/  # Demo service
```

### Frontend Structure
```
jeecgboot-vue3/
├── src/
│   ├── api/                     # API service layer
│   ├── components/              # Vue components
│   ├── views/                   # Page components
│   ├── router/                  # Route configuration
│   ├── store/                   # Pinia stores
│   └── utils/                   # Utility functions
├── build/                       # Build configuration
└── package.json                 # Dependencies
```

## Quick Start Commands

### Backend Development
```bash
# Install dependencies (Maven)
cd jeecg-boot && mvn clean install

# Run monolithic application
cd jeecg-boot/jeecg-module-system/jeecg-system-start
mvn spring-boot:run

# Run microservices (requires Nacos & Redis)
cd jeecg-boot
mvn clean install -P SpringCloud
```

### Frontend Development
```bash
# Install dependencies
cd jeecgboot-vue3
pnpm install

# Development server
pnpm dev

# Production build
pnpm build

# Docker build
pnpm build:docker
```

### Docker Development
```bash
# Start all services (MySQL + Redis + Backend + Frontend)
docker-compose up -d

# Access URLs:
# Frontend: http://localhost
# Backend API: http://localhost:8080/jeecg-boot
# MySQL: localhost:13306
```

## Architecture Patterns
内容社区系统是一个支持用户创建和分享多种类型内容的平台，包括文章、图文帖子、视频和笔记等。该系统将提供内容发布、浏览、互动和管理功能，为用户打造一个丰富的内容创作和分享社区。
目录
jeecg-boot/jeecg-boot-module/jeecg-module-content

### Backend Layer Architecture
```
org.jeecg.modules.content.{module_name}/
├── controller/     # REST API endpoints
├── service/        # Business logic interfaces
├── service/impl/   # Business logic implementations
├── mapper/         # MyBatis mappers
├── entity/         # Database entities
├── model/          # Domain models
├── vo/             # View objects
├── req/            # Request objects
├── req/query/      # Query objects
├── req/create/     # Create objects
├── req/update/     # Update objects
├── dto/            # Data transfer objects
├── config/         # Configuration classes
├── constant/       # Constants
└── util/           # Utility classes
```

### Key Patterns
- **Controller**: RESTful endpoints with @RestController
- **Service**: Business logic with @Service, using MyBatis-Plus
- **Mapper**: MyBatis mappers with XML or annotations
- **VO**: View objects for API responses
- **Req**: Request objects for API requests
- **Req/query**: Query objects for API requests
- **Req/create**: Create objects for API requests
- **Req/update**: Update objects for API requests
- **DTO**: Request/response objects with validation annotations
- **Entity**: Database entities with MyBatis-Plus annotations

## Configuration Files

### Backend Configuration
- **Main config**: `jeecg-system-start/src/main/resources/application.yml`
- **Environment profiles**: `application-dev.yml`, `application-prod.yml`
- **Database**: MySQL (default), configured in application.yml
- **Redis**: Cache and session storage
- **Nacos**: Microservices configuration (when enabled)

### Frontend Configuration
- **Vite config**: `jeecgboot-vue3/vite.config.ts`
- **Environment**: `.env.development`, `.env.production`
- **Proxy**: API proxy configuration in vite.config.ts

## API Design Guidelines

### RESTful Endpoints
- **Base path**: `/jeecg-boot`
- **Version**: `/api/v1/` (version in path)
- **Resources**: Use plural nouns (e.g., `/users`, `/roles`)
- **HTTP Methods**: GET (read), POST (create), PUT (update), DELETE (remove)

### Response Format
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {},
  "timestamp": 1234567890
}
```

## Development Best Practices

### Code Generation
- **Online Code Generator**: Available in admin UI under "Online Development"
- **Templates**: Single table, tree table, one-to-many relationships
- **Features**: Auto-generates CRUD operations, Excel import/export, validation

### Database Design
- **Primary Key**: String type with UUID generation
- **Audit Fields**: `create_by`, `create_time`, `update_by`, `update_time`
- **Soft Delete**: `del_flag` field (0=active, 1=deleted)
- **Tenant**: `tenant_id` for multi-tenant support

### Security
- **Authentication**: JWT token-based authentication
- **Authorization**: Role-based access control (RBAC)
- **Data Permission**: Row-level and column-level permissions
- **API Security**: Rate limiting, input validation

### Testing
- **Unit Tests**: JUnit 5 + Mockito
- **Integration Tests**: Spring Boot Test
- **Test Coverage**: Target 80%+ coverage
- **Test Data**: Use @DataJpaTest for repository tests

## Common Development Tasks

### Adding a New Module
1. Create module structure under `jeecg-boot-module/`
2. Add module to parent pom.xml
3. Create controller, service, mapper, entity classes
4. Add database migrations if needed
5. Generate frontend code using online generator

### Adding a New API Endpoint
1. Create controller class in `controller/` package
2. Define service interface in `service/` package
3. Implement service in `service/impl/` package
4. Create mapper interface in `mapper/` package
5. Add entity class in `entity/` package
6. Add DTO classes for request/response
7. Add Swagger annotations for documentation

### Database Changes
1. Update entity classes with new fields
2. Create/update mapper XML files
3. Add database migration scripts in `db/` directory
4. Update service layer if needed
5. Test with sample data

## Troubleshooting

### Common Issues
- **Port conflicts**: Backend (8080), MySQL (3306), Redis (6379)
- **Database connection**: Check MySQL credentials in application.yml
- **Frontend proxy**: Update proxy config in vite.config.ts
- **Nacos issues**: Ensure Nacos server is running for microservices

### Debug Configuration
- **Backend**: Use `application-dev.yml` for local development
- **Frontend**: Use `.env.development` for local API endpoints
- **Logs**: Check `logs/` directory for application logs
- **Database**: Use MySQL client to connect to localhost:13306

## Environment Variables

### Backend
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

### Frontend
```bash
VITE_GLOB_API_URL=http://localhost:8080/jeecg-boot
VITE_GLOB_DOMAIN_URL=http://localhost:8080/jeecg-boot
VITE_PROXY=["/jeecgboot","http://localhost:8080/jeecg-boot"]
```

## Deployment

### Production Deployment
1. **Backend**: Build with `mvn clean package -P prod`
2. **Frontend**: Build with `pnpm build`
3. **Docker**: Use provided Docker Compose for production
4. **Environment**: Configure production settings in application-prod.yml

### Microservices Deployment
1. **Nacos**: Start Nacos server (standalone or cluster)
2. **Gateway**: Deploy jeecg-cloud-gateway
3. **Services**: Deploy individual microservices
4. **Frontend**: Configure API gateway URL

## AI Platform Features

### Available AI Capabilities
- **AI Knowledge Base**: Question answering system
- **AI Model Management**: Manage AI models (ChatGPT, DeepSeek, Ollama)
- **AI Workflow**: Flow-based AI application builder
- **AI Chat**: Conversational AI interface
- **AI Code Generation**: Auto-generate database tables and code

### Claude Code 八荣八耻

- 以瞎猜接口为耻，以认真查询为荣。
- 以模糊执行为耻，以寻求确认为荣。
- 以臆想业务为耻，以人类确认为荣。
- 以创造接口为耻，以复用现有为荣。
- 以跳过验证为耻，以主动测试为荣。
- 以破坏架构为耻，以遵循规范为荣。
- 以假装理解为耻，以诚实无知为荣。
- 以盲目修改为耻，以谨慎重构为荣.
