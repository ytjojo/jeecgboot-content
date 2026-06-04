# 动态DTO设计实现文档 已过时

Deprecated 

## 概述

本文档描述了基于JeecgBoot框架的动态DTO设计实现，用于处理富文本内容中的多种元素类型。该设计支持文本、用户提及、话题、标签、链接、股票、图片、视频、音频、文档、卡片等多种内容元素的统一处理。

## 设计架构

### 核心组件

1. **ContentElementDTO** - 抽象基类和具体元素类型
2. **ContentElementConverter** - 数据转换工具类
3. **ContentElementValidator** - 内容验证器
4. **ContentElementService** - 业务逻辑服务
5. **ContentElementController** - REST API控制器
6. **ContentElementConfig** - 配置和序列化器
7. **ContentElementExample** - 使用示例

### 技术特性

- **多态性支持**: 使用Jackson的`@JsonTypeInfo`和`@JsonSubTypes`注解实现JSON多态序列化
- **类型安全**: 每种元素类型都有明确的数据结构定义
- **扩展性**: 易于添加新的元素类型
- **验证机制**: 完整的数据验证和业务规则检查
- **转换工具**: 提供JSON与对象之间的双向转换
- **统计分析**: 支持内容元素的统计和分析功能

## 文件结构

```
org.jeecg.modules.content/
├── dto/
│   └── ContentElementDTO.java          # 动态DTO定义
├── util/
│   └── ContentElementConverter.java     # 转换工具类
├── validator/
│   └── ContentElementValidator.java     # 验证器
├── service/
│   └── ContentElementService.java       # 业务服务
├── controller/
│   └── ContentElementController.java    # REST控制器
├── config/
│   └── ContentElementConfig.java        # 配置类
└── example/
    └── ContentElementExample.java       # 使用示例
```

## 支持的元素类型

### 1. 文本元素 (TextElement)
```json
{
  "type": "text",
  "content": "这是一段文本内容"
}
```

### 2. 用户提及元素 (MentionElement)
```json
{
  "type": "mention",
  "id": 123,
  "name": "张三",
  "avatar": "/avatar/123.jpg"
}
```

### 3. 话题元素 (TopicElement)
```json
{
  "type": "topic",
  "id": 456,
  "name": "技术讨论"
}
```

### 4. 标签元素 (TagElement)
```json
{
  "type": "tag",
  "id": 789,
  "name": "Java"
}
```

### 5. 链接元素 (LinkElement)
```json
{
  "type": "link",
  "content": "点击查看详情",
  "link": "https://example.com"
}
```

### 6. 股票元素 (StockElement)
```json
{
  "type": "stock",
  "id": 1,
  "code": "AAPL",
  "name": "苹果公司"
}
```

### 7. 图片元素 (ImageElement)
```json
{
  "type": "image",
  "id": "img_001",
  "url": "/images/demo.jpg",
  "alt": "演示图片",
  "width": 800,
  "height": 600
}
```

### 8. 视频元素 (VideoElement)
```json
{
  "type": "video",
  "id": "video_001",
  "url": "/videos/demo.mp4",
  "thumbnail": "/thumbnails/demo.jpg",
  "duration": 120
}
```

### 9. 音频元素 (AudioElement)
```json
{
  "type": "audio",
  "id": "audio_001",
  "url": "/audios/demo.mp3",
  "duration": 180
}
```

### 10. 文档元素 (DocumentElement)
```json
{
  "type": "document",
  "id": "doc_001",
  "url": "/documents/report.pdf",
  "name": "年度报告",
  "size": 1024000
}
```

### 11. 卡片元素 (CardElement)
```json
{
  "type": "card",
  "id": "card_001",
  "title": "产品介绍",
  "description": "这是一个优秀的产品",
  "image": "/images/product.jpg",
  "link": "https://product.example.com"
}
```

## 核心功能

### 1. 内容创建和处理
```java
// 创建内容元素列表
List<ContentElementDTO> elements = Arrays.asList(
    createTextElement("Hello "),
    createMentionElement(123L, "张三", "/avatar/123.jpg"),
    createTextElement("!")
);

// 处理内容创建
ContentElementService.ProcessResult result = 
    contentElementService.processContentCreation(
        contentElementConverter.convertToJson(elements)
    );
```

### 2. 内容验证
```java
// 验证内容元素
ContentElementValidator.ValidationResult validationResult = 
    contentElementValidator.validateElements(elements);

if (!validationResult.isValid()) {
    log.error("验证失败: {}", validationResult.getErrors());
}
```

### 3. 内容搜索
```java
// 搜索包含关键词的元素
ContentElementService.SearchResult searchResult = 
    contentElementService.searchElements(elementsJson, "关键词", null);

// 按类型过滤搜索
ContentElementService.SearchResult typeSearchResult = 
    contentElementService.searchElements(elementsJson, null, 
        Set.of("text", "mention"));
```

### 4. 内容统计
```java
// 获取内容统计信息
ContentElementService.ContentStatistics statistics = 
    contentElementService.getContentStatistics(elementsJson);

log.info("总元素数: {}", statistics.getTotalElements());
log.info("文本长度: {}", statistics.getTotalTextLength());
log.info("提及用户数: {}", statistics.getMentionedUserCount());
```

### 5. 数据提取
```java
// 提取纯文本
String plainText = contentElementConverter.extractPlainText(elements);

// 提取提及的用户ID
List<Long> mentionedUserIds = contentElementConverter.extractMentionedUserIds(elements);

// 提取话题ID
List<Long> topicIds = contentElementConverter.extractTopicIds(elements);

// 提取标签名称
List<String> tagNames = contentElementConverter.extractTagNames(elements);
```

## REST API接口

### 1. 验证内容
```http
POST /api/content/elements/validate
Content-Type: application/json

[
  {
    "type": "text",
    "content": "Hello World"
  }
]
```

### 2. 创建内容
```http
POST /api/content/elements/create
Content-Type: application/json

[
  {
    "type": "text",
    "content": "Hello "
  },
  {
    "type": "mention",
    "id": 123,
    "name": "张三"
  }
]
```

### 3. 搜索内容
```http
GET /api/content/elements/search?keyword=关键词&types=text,mention
```

### 4. 获取统计
```http
POST /api/content/elements/statistics
Content-Type: application/json

[
  {
    "type": "text",
    "content": "统计内容"
  }
]
```

### 5. 格式转换
```http
POST /api/content/elements/convert/to-json
POST /api/content/elements/convert/from-json
```

## 配置说明

### Jackson配置
```java
@Configuration
public class ContentElementConfig {
    
    @Bean
    public SimpleModule contentElementModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ContentElementDTO.class, new ContentElementSerializer());
        module.addDeserializer(ContentElementDTO.class, new ContentElementDeserializer());
        return module;
    }
}
```

### 验证规则配置
```java
// ContentElementProperties 已移动到独立的类文件中
// 位置：org.jeecg.modules.content.config.ContentElementProperties
public class ContentElementProperties {
    // 内容长度限制
    public static final int MAX_CONTENT_LENGTH = 10000;
    
    // 媒体文件限制
    public static final int MAX_MEDIA_FILES = 9;
    
    // 支持的文件格式
    public static final String[] SUPPORTED_IMAGE_FORMATS = {"jpg", "jpeg", "png", "gif", "webp"};
    public static final String[] SUPPORTED_VIDEO_FORMATS = {"mp4", "avi", "mov", "wmv", "flv"};
    public static final String[] SUPPORTED_AUDIO_FORMATS = {"mp3", "wav", "aac", "ogg", "flac"};
    public static final String[] SUPPORTED_DOCUMENT_FORMATS = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md"};
    
    // 验证正则表达式
    public static final String URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
    public static final String STOCK_CODE_REGEX = "^[A-Z]{1,5}\\d{4,6}$";
}
```

## 最佳实践

### 1. 性能优化
- 使用缓存存储频繁访问的内容元素
- 对大量元素进行批量处理
- 合理设置验证规则避免过度验证

### 2. 安全考虑
- 对用户输入进行严格验证
- 防止XSS攻击，对HTML内容进行转义
- 限制文件上传大小和类型

### 3. 扩展性设计
- 新增元素类型时，需要更新DTO、验证器、转换器
- 保持向后兼容性，避免破坏现有数据
- 使用版本控制管理API变更

### 4. 错误处理
- 提供详细的验证错误信息
- 使用统一的异常处理机制
- 记录关键操作的日志

## 测试示例

运行示例代码：
```java
@Autowired
private ContentElementExample contentElementExample;

// 运行所有示例
contentElementExample.runAllExamples();
```

示例包含：
- 创建富文本内容
- JSON序列化和反序列化
- 内容搜索和过滤
- 统计信息获取

## 总结

该动态DTO设计提供了一个灵活、可扩展的富文本内容处理方案，支持多种元素类型的统一管理。通过合理的架构设计和完善的工具类，可以满足复杂的业务需求，同时保持代码的可维护性和可扩展性。

## 相关文档

- [内容服务需求文档](./docs/内容服务需求_V2.md)
- [Quill Delta格式说明](./docs/内容服务需求_V2.md#L650-698)
- [JeecgBoot开发规范](https://jeecg.com/doc)