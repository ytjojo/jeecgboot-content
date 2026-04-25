# Editor.js 扩展指南

本文档介绍如何在现有的 Editor.js 集成方案中添加新的块类型。

## 架构概述

我们的 Editor.js 集成方案采用以下架构：

- **EditorJsBlockData**: 所有块数据的基础接口
- **具体块数据类**: 实现 EditorJsBlockData 接口的具体类型
- **EditorJsBlockDataFactory**: 负责创建不同类型的块数据对象
- **EditorJsConverter**: 负责 JSON 与 DTO 之间的转换
- **EditorJsContentDTO**: 完整的 Editor.js 内容数据传输对象

## 添加新块类型的步骤

### 1. 创建块数据类

创建一个新的类实现 `EditorJsBlockData` 接口：

```java
package org.jeecg.modules.content.dto.editorjs;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@Schema(description = "自定义块数据")
public class CustomData implements EditorJsBlockData {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "自定义字段", required = true)
    @NotBlank(message = "自定义字段不能为空")
    @JsonProperty("customField")
    private String customField;
    
    @Override
    public String getType() {
        return "custom"; // 返回块类型名称
    }
    
    @Override
    public boolean validate() {
        return customField != null && !customField.trim().isEmpty();
    }
    
    @Override
    public String getDisplayText() {
        return "[自定义] " + (customField != null ? customField : "");
    }
}
```

### 2. 更新 EditorJsBlockData 接口

在 `EditorJsBlockData.java` 的 `@JsonSubTypes` 注解中添加新类型：

```java
@JsonSubTypes({
    // ... 现有类型 ...
    @JsonSubTypes.Type(value = CustomData.class, name = "custom")
})
```

### 3. 更新块数据工厂

在 `EditorJsBlockDataFactory.java` 中注册新类型：

```java
private void initializeCreators() {
    // ... 现有类型 ...
    creators.put("custom", this::createCustomData);
}

private EditorJsBlockData createCustomData(JsonNode dataNode) {
    return objectMapper.convertValue(dataNode, CustomData.class);
}
```

### 4. 更新业务逻辑（可选）

如果需要特殊处理，在 `EditorJsContentService.java` 中添加处理逻辑：

```java
private void processBlocks(List<EditorJsBlock> blocks) {
    for (EditorJsBlock block : blocks) {
        EditorJsBlockData data = block.getData();
        
        switch (data.getType()) {
            // ... 现有类型 ...
            case "custom" -> processCustomBlock((CustomData) data);
            default -> log.debug("处理标准块类型: {}", data.getType());
        }
    }
}

private void processCustomBlock(CustomData customData) {
    log.info("处理自定义块: {}", customData.getCustomField());
    // 添加自定义处理逻辑
}
```

## 现有块类型说明

### Editor.js 标准块类型

| 类型 | 类名 | 说明 |
|------|------|------|
| paragraph | ParagraphData | 段落文本 |
| header | HeaderData | 标题（1-6级） |
| list | ListData | 有序/无序列表 |
| quote | QuoteData | 引用块 |
| image | ImageData | 图片 |
| code | CodeData | 代码块 |
| table | TableData | 表格 |
| embed | EmbedData | 嵌入内容 |
| linkTool | LinkToolData | 链接工具 |
| delimiter | DelimiterData | 分隔符 |

### 系统扩展块类型

| 类型 | 类名 | 说明 |
|------|------|------|
| mention | MentionData | 用户提及 |
| topic | TopicData | 话题标签 |
| tag | TagData | 标签 |
| stock | StockData | 股票信息 |
| card | CardData | 卡片内容 |

## 最佳实践

### 1. 命名规范

- 块类型名称使用小写字母和下划线
- 类名使用 PascalCase，以 "Data" 结尾
- 字段名使用 camelCase

### 2. 验证规则

- 在 `validate()` 方法中实现业务验证逻辑
- 使用 Jakarta Validation 注解进行基础验证
- 确保必填字段不为空

### 3. 序列化配置

- 使用 `@JsonProperty` 指定 JSON 字段名
- 使用 `@Schema` 提供 API 文档描述
- 确保字段可以正确序列化和反序列化

### 4. 显示文本

- `getDisplayText()` 方法应返回用户友好的文本
- 考虑文本长度限制，避免过长的显示文本
- 为空值情况提供默认显示文本

### 5. 错误处理

- 在工厂方法中添加异常处理
- 记录详细的错误日志
- 对于无法解析的数据，返回 null 而不是抛出异常

## 测试建议

### 1. 单元测试

```java
@Test
void testCustomDataSerialization() throws JsonProcessingException {
    CustomData customData = new CustomData().setCustomField("test");
    
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(customData);
    CustomData deserialized = mapper.readValue(json, CustomData.class);
    
    assertEquals("test", deserialized.getCustomField());
    assertEquals("custom", deserialized.getType());
    assertTrue(deserialized.validate());
}
```

### 2. 集成测试

```java
@Test
void testEditorJsConverterWithCustomBlock() throws JsonProcessingException {
    String json = """{
        "time": 1234567890,
        "blocks": [{
            "type": "custom",
            "data": {
                "customField": "test value"
            }
        }]
    }""";
    
    EditorJsContentDTO content = editorJsConverter.fromJson(json);
    assertNotNull(content);
    assertEquals(1, content.getBlocks().size());
    
    EditorJsBlock block = content.getBlocks().get(0);
    assertEquals("custom", block.getType());
    assertTrue(block.getData() instanceof CustomData);
}
```

## 常见问题

### Q: 如何处理复杂的嵌套数据结构？

A: 可以在块数据类中定义嵌套的静态内部类，参考 `ImageData.FileInfo` 的实现。

### Q: 如何支持动态字段？

A: 可以使用 `Map<String, Object>` 类型的字段来存储动态数据，但建议尽量使用强类型字段。

### Q: 如何处理版本兼容性？

A: 在添加新字段时使用 `@JsonProperty` 的 `defaultValue` 属性，并在 `validate()` 方法中处理向后兼容性。

### Q: 如何优化性能？

A: 考虑使用缓存机制缓存解析结果，避免重复解析相同的 JSON 内容。

## 总结

通过遵循上述步骤和最佳实践，您可以轻松地在现有系统中添加新的 Editor.js 块类型。这种设计确保了：

- **类型安全**: 强类型的块数据类
- **扩展性**: 易于添加新的块类型
- **兼容性**: 与 Editor.js 标准格式完全兼容
- **维护性**: 清晰的代码结构和职责分离