# Tiptap编辑器功能

## 1. 指南概述

本指南专门为AI编辑智能体提供详细的实现步骤和代码示例，用于在JeecgBoot项目中实现基于Tiptap的富文本编辑器功能。

### 1.1 实现目标
- 创建功能完整的Tiptap富文本编辑器
- 支持Markdown语法和多种扩展
- 实现前后端完整的数据交互
- 提供良好的用户体验和性能

### 1.2 技术要求
- 严格遵循JeecgBoot项目规范
- 使用Vue 3 + TypeScript开发前端
- 使用Spring Boot开发后端API
- 遵循RESTful API设计原则
- **编辑器：Tiptap 3.x版本**，采用新的扩展导入方式和API
- 注意：Tiptap V3相比V2在扩展导入方式上有重大变化，需使用命名导入

## 2. Tiptap V3 重要变更说明

### 2.1 扩展导入方式变更
Tiptap V3采用了新的扩展导入方式，从默认导入改为命名导入：

```typescript
// V2 方式（旧）
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Link from '@tiptap/extension-link'

// V3 方式（新）
import { StarterKit } from '@tiptap/starter-kit'
import { Image } from '@tiptap/extension-image'
import { Link } from '@tiptap/extension-link'
```

### 2.2 核心包依赖变更
V3版本需要额外安装@tiptap/core包：

```bash
# V3 必须安装的核心包
npm install @tiptap/core @tiptap/vue-3 @tiptap/pm
```

### 2.3 扩展配置保持兼容
大部分扩展的配置方式保持不变，只是导入方式发生变化。

## 3. 实现步骤清单

### 阶段一：数据库和后端基础
- [ ] 创建数据库表结构
- [ ] 创建实体类(Entity)
- [ ] 创建VO类和DTO类
- [ ] 创建Mapper接口
- [ ] 创建Service接口和实现类
- [ ] 创建Controller控制器

### 阶段二：前端基础组件
- [ ] 安装Tiptap相关依赖
- [ ] 创建编辑器核心组件
- [ ] 创建工具栏组件
- [ ] 创建扩展管理组合函数
- [ ] 实现基础编辑功能

### 阶段三：高级功能实现
- [ ] 实现文件上传功能
- [ ] 添加Markdown支持
- [ ] 实现内容格式转换
- [ ] 添加模板功能
- [ ] 实现协作编辑

## 3. 详细实现指令

### 3.1 数据库实现指令

**指令1：创建数据库表**
```sql
-- 在jeecg-boot/db/目录下创建editor_tables.sql文件
-- 执行以下SQL语句创建编辑器相关表

-- 编辑器内容表
CREATE TABLE editor_content (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '内容标题',
    content LONGTEXT COMMENT '编辑器内容(JSON格式)',
    markdown_content LONGTEXT COMMENT 'Markdown格式内容',
    html_content LONGTEXT COMMENT 'HTML格式内容',
    content_type VARCHAR(50) DEFAULT 'article' COMMENT '内容类型',
    status INTEGER DEFAULT 0 COMMENT '状态(0:草稿,1:发布,2:归档)',
    tags VARCHAR(500) COMMENT '标签',
    category_id VARCHAR(32) COMMENT '分类ID',
    author_id VARCHAR(32) NOT NULL COMMENT '作者ID',
    word_count INTEGER DEFAULT 0 COMMENT '字数统计',
    reading_time INTEGER DEFAULT 0 COMMENT '预计阅读时间(分钟)',
    is_template INTEGER DEFAULT 0 COMMENT '是否为模板(0:否,1:是)',
    template_name VARCHAR(255) COMMENT '模板名称',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志(0:正常,1:删除)',
    INDEX idx_author_id (author_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='编辑器内容表';

-- 编辑器历史记录表
CREATE TABLE editor_history (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    content LONGTEXT COMMENT '历史内容(JSON格式)',
    version_number INTEGER NOT NULL COMMENT '版本号',
    change_description VARCHAR(500) COMMENT '变更描述',
    operation_type VARCHAR(50) COMMENT '操作类型(create,update,delete)',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_content_id (content_id),
    INDEX idx_version_number (version_number),
    INDEX idx_create_time (create_time)
) COMMENT='编辑器历史记录表';

-- 编辑器模板表
CREATE TABLE editor_template (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    template_name VARCHAR(255) NOT NULL COMMENT '模板名称',
    template_description VARCHAR(500) COMMENT '模板描述',
    template_content LONGTEXT COMMENT '模板内容(JSON格式)',
    template_preview TEXT COMMENT '模板预览图',
    category VARCHAR(100) COMMENT '模板分类',
    is_public INTEGER DEFAULT 0 COMMENT '是否公开(0:私有,1:公开)',
    use_count INTEGER DEFAULT 0 COMMENT '使用次数',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志(0:正常,1:删除)',
    INDEX idx_category (category),
    INDEX idx_is_public (is_public),
    INDEX idx_create_time (create_time)
) COMMENT='编辑器模板表';
```

### 3.2 后端实现指令

**指令2：创建实体类**
```
在路径 jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/entity/ 下创建以下文件：

1. EditorContent.java - 编辑器内容实体类
2. EditorHistory.java - 编辑器历史记录实体类  
3. EditorTemplate.java - 编辑器模板实体类

每个实体类必须包含：
- 完整的字段定义和注解
- Lombok注解(@Data, @TableName等)
- Swagger API文档注解
- 字段验证注解
- 详细的中文注释
```

**指令3：创建VO和DTO类**
```
在路径 jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/vo/ 下创建：

1. EditorContentVO.java - 编辑器内容视图对象
2. req/EditorContentCreateReqVO.java - 创建请求VO
3. req/EditorContentUpdateReqVO.java - 更新请求VO
4. req/EditorContentQueryReqVO.java - 查询请求VO
5. resp/EditorContentRespVO.java - 响应VO

每个VO类必须：
- 包含完整的字段定义
- 添加验证注解(@NotNull, @NotBlank等)
- 添加Swagger文档注解
- 提供详细的中文注释
```

**指令4：创建Mapper接口**
```
在路径 jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/mapper/ 下创建：

1. EditorContentMapper.java
2. EditorHistoryMapper.java
3. EditorTemplateMapper.java

每个Mapper接口必须：
- 继承BaseMapper<T>
- 添加@Mapper注解
- 定义自定义查询方法
- 提供详细的方法注释
```

**指令5：创建Service接口和实现类**
```
在路径 jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/service/ 下创建：

接口文件：
1. IEditorContentService.java
2. IEditorHistoryService.java
3. IEditorTemplateService.java

实现类文件（在impl子目录下）：
1. EditorContentServiceImpl.java
2. EditorHistoryServiceImpl.java
3. EditorTemplateServiceImpl.java

每个Service必须：
- 接口继承IService<T>
- 实现类继承ServiceImpl<M,T>并实现对应接口
- 添加@Service注解
- 实现完整的业务逻辑方法
- 包含事务处理和异常处理
- 提供详细的方法注释
```

**指令6：创建Controller控制器**
```
在路径 jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/controller/ 下创建：

1. EditorController.java - 编辑器内容控制器
2. EditorTemplateController.java - 编辑器模板控制器
3. EditorHistoryController.java - 编辑器历史控制器

每个Controller必须：
- 继承JeecgController<T, S>
- 添加@RestController和@RequestMapping注解
- 添加@Api注解用于Swagger文档
- 实现完整的CRUD操作
- 添加@AutoLog注解用于操作日志
- 包含完整的异常处理
- 提供详细的接口注释
```

### 3.3 前端实现指令

**指令7：安装前端依赖**
```bash
# 在jeecgboot-vue3目录下执行以下命令安装Tiptap V3相关依赖

# 安装Tiptap 3.x核心包
npm install @tiptap/core @tiptap/vue-3 @tiptap/pm @tiptap/starter-kit

# 安装扩展包
npm install @tiptap/extension-image @tiptap/extension-link @tiptap/extension-table @tiptap/extension-table-row @tiptap/extension-table-header @tiptap/extension-table-cell @tiptap/extension-code-block-lowlight @tiptap/extension-highlight @tiptap/extension-typography @tiptap/extension-placeholder @tiptap/extension-character-count @tiptap/extension-task-list @tiptap/extension-task-item @tiptap/extension-mention

# 安装协作和数学公式扩展
npm install @tiptap/extension-collaboration @tiptap/extension-collaboration-cursor @tiptap/extension-mathematics

# 安装Markdown和代码高亮支持
npm install tiptap-markdown lowlight

# 安装类型定义
npm install @types/node --save-dev
```

**指令8：创建编辑器组件目录结构**
```
在jeecgboot-vue3/src/components/目录下创建以下目录结构：

TiptapEditor/
├── index.vue                 # 主编辑器组件
├── components/
│   ├── EditorToolbar.vue     # 工具栏组件
│   ├── EditorSidebar.vue     # 侧边栏组件
│   ├── EditorPreview.vue     # 预览组件
│   └── EditorStatusBar.vue   # 状态栏组件
├── composables/
│   ├── useEditorExtensions.ts # 扩展管理
│   ├── useEditorContent.ts    # 内容管理
│   ├── useEditorHistory.ts    # 历史记录管理
│   └── useEditorUpload.ts     # 文件上传管理
├── types/
│   └── editor.ts             # 类型定义
└── styles/
    └── editor.scss           # 样式文件
```

**指令9：创建编辑器主组件**
```typescript
// 在jeecgboot-vue3/src/components/TiptapEditor/index.vue中创建主编辑器组件
// 必须包含以下功能：
// 1. 编辑器初始化和销毁
// 2. 内容双向绑定
// 3. 扩展插件集成
// 4. 事件处理和回调
// 5. 响应式设计
// 6. 错误处理
// 7. 性能优化
```

**指令10：创建工具栏组件**
```typescript
// 在jeecgboot-vue3/src/components/TiptapEditor/components/EditorToolbar.vue中创建工具栏组件
// 必须包含以下功能：
// 1. 格式化工具（粗体、斜体、删除线等）
// 2. 标题级别选择
// 3. 列表工具（有序、无序、任务列表）
// 4. 插入工具（图片、链接、表格、代码块）
// 5. 撤销重做功能
// 6. 自定义工具扩展
// 7. 响应式布局
```

**指令11：创建扩展管理组合函数**
```typescript
// 在jeecgboot-vue3/src/components/TiptapEditor/composables/useEditorExtensions.ts中创建扩展管理函数
// 必须包含以下扩展：
// 1. StarterKit基础扩展包
// 2. Markdown支持扩展
// 3. 图片扩展（支持上传和拖拽）
// 4. 链接扩展
// 5. 表格扩展
// 6. 代码高亮扩展
// 7. 文本高亮扩展
// 8. 排版增强扩展
// 9. 占位符扩展
// 10. 字符统计扩展
// 11. 任务列表扩展
// 12. 提及功能扩展

// 注意：Tiptap V3中扩展导入方式
// import { StarterKit } from '@tiptap/starter-kit'
// import { Image } from '@tiptap/extension-image'
// import { Link } from '@tiptap/extension-link'
```

### 3.4 API接口实现指令

**指令12：创建API接口文件**
```typescript
// 在jeecgboot-vue3/src/api/content/目录下创建以下文件：

1. editor.ts - 编辑器内容相关API
2. template.ts - 模板相关API
3. upload.ts - 文件上传相关API

每个API文件必须：
- 使用统一的请求封装
- 包含完整的类型定义
- 提供错误处理
- 添加详细的注释
- 支持请求取消
```

**指令13：创建页面组件**
```typescript
// 在jeecgboot-vue3/src/views/content/目录下创建以下页面：

1. editor/
   ├── EditorList.vue        # 编辑器内容列表页
   ├── EditorEdit.vue        # 编辑器编辑页
   └── EditorPreview.vue     # 编辑器预览页

2. template/
   ├── TemplateList.vue      # 模板列表页
   └── TemplateEdit.vue      # 模板编辑页

每个页面必须：
- 使用Element Plus组件
- 实现完整的CRUD操作
- 包含数据验证和错误处理
- 支持响应式设计
- 提供良好的用户体验
```

## 4. 关键实现要点

### 4.1 代码规范要求

1. **命名规范**
   - 类名使用大驼峰命名法
   - 方法名和变量名使用小驼峰命名法
   - 常量使用大写字母和下划线
   - 文件名使用小驼峰命名法

2. **注释规范**
   - 所有类必须有类注释
   - 所有公共方法必须有方法注释
   - 复杂业务逻辑必须有行内注释
   - 使用中文注释，描述清晰准确

3. **异常处理**
   - 使用统一的异常处理机制
   - 自定义业务异常继承RuntimeException
   - 记录详细的错误日志
   - 返回友好的错误信息

### 4.2 性能优化要求

1. **前端优化**
   - 使用虚拟滚动处理大列表
   - 实现组件懒加载
   - 使用防抖和节流优化用户操作
   - 合理使用缓存机制

2. **后端优化**
   - 使用分页查询避免大数据量查询
   - 合理使用数据库索引
   - 实现Redis缓存热点数据
   - 使用异步处理耗时操作

### 4.3 安全要求

1. **数据安全**
   - 对用户输入进行严格验证
   - 防止XSS和SQL注入攻击
   - 敏感数据加密存储
   - 实现操作日志记录

2. **权限控制**
   - 实现基于角色的访问控制
   - 接口级别权限验证
   - 数据级别权限控制
   - 防止越权操作

## 5. 测试要求

### 5.1 单元测试
- 为所有Service方法编写单元测试
- 测试覆盖率要求达到80%以上
- 包含正常和异常场景测试
- 使用Mock对象隔离依赖

### 5.2 集成测试
- 测试API接口的完整性
- 验证数据库操作的正确性
- 测试文件上传功能
- 验证权限控制机制

### 5.3 前端测试
- 组件单元测试
- 用户交互测试
- 浏览器兼容性测试
- 响应式设计测试

## 6. 部署和配置

### 6.1 开发环境配置
```yaml
# application-dev.yml中添加编辑器相关配置
editor:
  upload:
    path: /tmp/uploads/editor/
    max-size: 10MB
    allowed-types: jpg,jpeg,png,gif,webp
  cache:
    enabled: true
    ttl: 3600
  collaboration:
    enabled: false
```

### 6.2 生产环境配置
```yaml
# application-prod.yml中的生产环境配置
editor:
  upload:
    path: /data/uploads/editor/
    max-size: 50MB
    allowed-types: jpg,jpeg,png,gif,webp,svg
  cache:
    enabled: true
    ttl: 7200
  collaboration:
    enabled: true
    websocket-url: ws://localhost:8080/ws/collaboration
```

## 7. 常见问题和解决方案

### 7.1 性能问题
**问题**：大文档编辑卡顿
**解决方案**：
- 实现虚拟滚动
- 使用Web Worker处理大数据
- 优化DOM操作
- 实现内容分页加载

### 7.2 兼容性问题
**问题**：浏览器兼容性
**解决方案**：
- 使用Babel转译ES6+语法
- 添加Polyfill支持
- 实现功能降级
- 提供兼容性检测

### 7.3 安全问题
**问题**：XSS攻击风险
**解决方案**：
- 对用户输入进行HTML转义
- 使用白名单过滤HTML标签
- 实现CSP策略
- 定期安全审计

## 8. 验收标准

### 8.1 功能验收
- [ ] 支持完整的Markdown语法
- [ ] 提供丰富的编辑器扩展
- [ ] 实现文件上传功能
- [ ] 支持内容格式转换
- [ ] 提供模板功能
- [ ] 实现历史版本管理
- [ ] 支持协作编辑
- [ ] 提供导入导出功能

### 8.2 性能验收
- [ ] 编辑器加载时间 < 2秒
- [ ] 大文档编辑流畅度 > 60fps
- [ ] 文件上传成功率 > 99%
- [ ] 内容保存响应时间 < 1秒
- [ ] 支持10万字文档编辑

### 8.3 兼容性验收
- [ ] 支持Chrome 90+
- [ ] 支持Firefox 88+
- [ ] 支持Safari 14+
- [ ] 支持Edge 90+
- [ ] 支持移动端浏览器

### 8.4 安全验收
- [ ] 通过XSS攻击测试
- [ ] 通过SQL注入测试
- [ ] 通过CSRF攻击测试
- [ ] 通过文件上传安全测试
- [ ] 通过权限控制测试

---

**使用说明**：AI智能体在实现功能时，请严格按照本指南的步骤和要求进行开发，确保代码质量和功能完整性。如遇到问题，请参考常见问题和解决方案部分。