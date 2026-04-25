# TipTap Java Library

一个用于处理TipTap编辑器JSON格式的Java库，提供完整的文档对象模型、Markdown转换、文档验证和扩展机制。

## 功能特性

### 🎯 核心功能
- **完整的TipTap文档对象模型**：支持所有标准的TipTap节点和标记类型
- **Markdown双向转换**：支持Markdown与TipTap JSON格式的互相转换
- **文档验证**：提供完整的文档结构和内容验证机制
- **扩展注册系统**：支持自定义节点和标记类型的注册和管理
- **JSON序列化支持**：完全兼容Jackson序列化框架

### 📋 支持的节点类型
- **文本节点**：`TextNode` - 基础文本内容
- **段落节点**：`ParagraphNode` - 段落容器
- **标题节点**：`HeadingNode` - 1-6级标题
- **代码块节点**：`CodeBlockNode` - 代码块，支持语法高亮
- **引用节点**：`BlockquoteNode` - 引用块
- **列表节点**：`BulletListNode`、`OrderedListNode`、`ListItemNode`
- **媒体节点**：`ImageNode` - 图片
- **分割线节点**：`HorizontalRuleNode` - 水平分割线
- **硬换行节点**：`HardBreakNode` - 强制换行

### 🎨 支持的标记类型
- **文本样式**：`BoldMark`（粗体）、`ItalicMark`（斜体）、`StrikeMark`（删除线）
- **代码标记**：`CodeMark` - 行内代码
- **链接标记**：`LinkMark` - 超链接
- **下标上标**：`SubMark`（下标）、`SupMark`（上标）
- **下划线**：`UnderlineMark` - 下划线

## 快速开始

### 1. 添加依赖

在你的`pom.xml`中添加以下依赖：

```xml
<dependencies>
    <!-- Jackson用于JSON处理 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.2</version>
    </dependency>
    
    <!-- Flexmark用于Markdown处理 -->
    <dependency>
        <groupId>com.vladsch.flexmark</groupId>
        <artifactId>flexmark-all</artifactId>
        <version>0.64.8</version>
    </dependency>
    
    <!-- Lombok用于简化代码 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 2. 基本使用

#### 创建TipTap文档

```java
import org.jeecg.modules.content.dto.tiptap.*;
import org.jeecg.modules.content.dto.tiptap.node.*;
import org.jeecg.modules.content.dto.tiptap.mark.*;

// 创建文档
Doc doc = new Doc();

// 创建段落
ParagraphNode paragraph = new ParagraphNode();

// 创建文本节点
TextNode text = new TextNode();
text.setText("Hello, TipTap!");

// 添加粗体标记
BoldMark boldMark = new BoldMark();
text.addMark(boldMark);

// 构建文档结构
paragraph.addContent(text);
doc.addContent(paragraph);
```

#### JSON序列化

```java
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper objectMapper = new ObjectMapper();
String json = objectMapper.writeValueAsString(doc);
System.out.println(json);
```

#### Markdown转换

```java
import org.jeecg.modules.content.dto.tiptap.converter.MarkdownConverter;

MarkdownConverter converter = new MarkdownConverter();

// Markdown转TipTap
String markdown = "# 标题\n\n这是一个**粗体**文本。";
Doc doc = converter.markdownToTipTap(markdown);

// TipTap转Markdown
String convertedMarkdown = converter.tipTapToMarkdown(doc);
```

## 详细使用指南

### 文档结构

TipTap文档采用树形结构，由以下组件构成：

- **Doc**：文档根节点
- **Node**：文档节点，可以包含其他节点或文本内容
- **Mark**：文本标记，用于标识文本样式
- **Attrs**：属性对象，存储节点或标记的配置信息

### 创建复杂文档

```java
// 创建标题
HeadingNode heading = new HeadingNode();
Map<String, Object> headingAttrs = new HashMap<>();
headingAttrs.put("level", 1);
heading.setAttrs(headingAttrs);

TextNode headingText = new TextNode();
headingText.setText("文档标题");
heading.addContent(headingText);

// 创建带链接的段落
ParagraphNode paragraph = new ParagraphNode();

TextNode text1 = new TextNode();
text1.setText("访问 ");
paragraph.addContent(text1);

TextNode linkText = new TextNode();
linkText.setText("官方网站");
LinkMark linkMark = new LinkMark();
Map<String, Object> linkAttrs = new HashMap<>();
linkAttrs.put("href", "https://tiptap.dev");
linkMark.setAttrs(linkAttrs);
linkText.addMark(linkMark);
paragraph.addContent(linkText);

TextNode text2 = new TextNode();
text2.setText(" 了解更多。");
paragraph.addContent(text2);

// 创建代码块
CodeBlockNode codeBlock = new CodeBlockNode();
Map<String, Object> codeAttrs = new HashMap<>();
codeAttrs.put("language", "java");
codeBlock.setAttrs(codeAttrs);

TextNode codeText = new TextNode();
codeText.setText("System.out.println(\"Hello World\");");
codeBlock.addContent(codeText);

// 构建完整文档
Doc doc = new Doc();
doc.addContent(heading);
doc.addContent(paragraph);
doc.addContent(codeBlock);
```

### 文档验证

```java
import org.jeecg.modules.content.dto.tiptap.validator.DocValidator;

DocValidator validator = new DocValidator();
var result = validator.validate(doc);

// 检查验证结果
if (result.isValid()) {
    System.out.println("文档验证通过");
} else {
    System.out.println("验证错误: " + result.getErrors());
    System.out.println("验证警告: " + result.getWarnings());
}

// 获取文档统计信息
var stats = validator.getDocumentStats(doc);
System.out.println("节点总数: " + stats.get("totalNodes"));
System.out.println("文本长度: " + stats.get("textLength"));
```

### 自定义扩展

```java
import org.jeecg.modules.content.dto.tiptap.registry.ExtensionRegistry;

ExtensionRegistry registry = ExtensionRegistry.getInstance();

// 注册自定义节点类型
registry.registerNodeFactory("customHighlight", () -> {
    Node node = new Node() {};
    node.setType("customHighlight");
    Map<String, Object> attrs = new HashMap<>();
    attrs.put("color", "yellow");
    node.setAttrs(attrs);
    return node;
});

// 注册自定义标记类型
registry.registerMarkFactory("customAnnotation", () -> {
    Mark mark = new Mark() {};
    mark.setType("customAnnotation");
    Map<String, Object> attrs = new HashMap<>();
    attrs.put("note", "这是一个注释");
    mark.setAttrs(attrs);
    return mark;
});

// 使用自定义扩展
Node customNode = registry.createNode("customHighlight");
Mark customMark = registry.createMark("customAnnotation");
```

## API参考

### 核心类

#### Doc
文档根对象，包含整个TipTap文档的内容。

```java
public class Doc {
    public void addContent(Node node)          // 添加子节点
    public List<Node> getContent()             // 获取所有子节点
    public void setContent(List<Node> content) // 设置子节点列表
}
```

#### Node
抽象节点基类，所有节点类型的父类。

```java
public abstract class Node {
    public String getType()                    // 获取节点类型
    public void setType(String type)           // 设置节点类型
    public Map<String, Object> getAttrs()     // 获取节点属性
    public void setAttrs(Map<String, Object> attrs) // 设置节点属性
    public List<Node> getContent()             // 获取子节点
    public void addContent(Node node)          // 添加子节点
}
```

#### Mark
抽象标记基类，所有标记类型的父类。

```java
public abstract class Mark {
    public String getType()                    // 获取标记类型
    public void setType(String type)           // 设置标记类型
    public Map<String, Object> getAttrs()     // 获取标记属性
    public void setAttrs(Map<String, Object> attrs) // 设置标记属性
}
```

### 转换器

#### MarkdownConverter
Markdown与TipTap格式的双向转换器。

```java
public class MarkdownConverter {
    public Doc markdownToTipTap(String markdown)     // Markdown转TipTap
    public String tipTapToMarkdown(Doc doc)          // TipTap转Markdown
    public boolean isValidMarkdown(String markdown)  // 验证Markdown语法
    public Map<String, Object> getMarkdownStats(String markdown) // 获取Markdown统计信息
}
```

### 验证器

#### DocValidator
文档验证器，提供文档结构和内容的验证功能。

```java
public class DocValidator {
    public ValidationResult validate(Doc doc)         // 验证文档
    public Map<String, Object> getDocumentStats(Doc doc) // 获取文档统计信息
}
```

### 扩展注册

#### ExtensionRegistry
扩展注册器，管理自定义节点和标记类型。

```java
public class ExtensionRegistry {
    public static ExtensionRegistry getInstance()     // 获取单例实例
    public boolean registerNodeFactory(String type, Supplier<Node> factory) // 注册节点工厂
    public boolean registerMarkFactory(String type, Supplier<Mark> factory) // 注册标记工厂
    public Node createNode(String type)              // 创建节点实例
    public Mark createMark(String type)              // 创建标记实例
    public boolean hasNodeType(String type)          // 检查节点类型是否支持
    public boolean hasMarkType(String type)          // 检查标记类型是否支持
    public Set<String> getRegisteredNodeTypes()      // 获取已注册的节点类型
    public Set<String> getRegisteredMarkTypes()      // 获取已注册的标记类型
}
```

## 示例代码

完整的使用示例请参考：
- `TipTapExamples.java` - 包含所有功能的详细示例代码

运行示例：
```java
TipTapExamples.runAllExamples();
```

## 最佳实践

### 1. 文档构建
- 始终从`Doc`对象开始构建文档
- 使用适当的节点类型来表示不同的内容结构
- 合理使用标记来添加文本样式

### 2. 属性设置
- 为需要属性的节点和标记正确设置`attrs`
- 使用类型安全的方式访问属性值
- 验证属性值的有效性

### 3. 错误处理
- 在转换过程中捕获和处理异常
- 使用文档验证器检查文档的有效性
- 记录详细的错误信息用于调试

### 4. 性能优化
- 对于大型文档，考虑分批处理
- 缓存频繁使用的转换结果
- 使用流式处理处理大量数据

## 常见问题

### Q: 如何处理不支持的节点类型？
A: 使用`ExtensionRegistry`注册自定义节点类型，或者在转换时忽略不支持的节点。

### Q: Markdown转换是否支持所有语法？
A: 支持大部分标准Markdown语法，对于扩展语法可以通过自定义扩展来支持。

### Q: 如何处理嵌套的复杂结构？
A: 使用递归的方式构建和处理嵌套结构，注意控制递归深度避免栈溢出。

### Q: 是否支持自定义序列化格式？
A: 可以通过Jackson的自定义序列化器来实现特定的序列化需求。

## 版本历史

- **v1.0.0** - 初始版本
  - 完整的TipTap文档对象模型
  - Markdown双向转换
  - 文档验证功能
  - 扩展注册机制

## 许可证

本项目采用MIT许可证，详情请参阅LICENSE文件。

## 贡献

欢迎提交Issue和Pull Request来改进这个库。在提交代码前，请确保：

1. 代码符合项目的编码规范
2. 添加了适当的单元测试
3. 更新了相关文档
4. 所有测试都能通过

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交GitHub Issue
- 发送邮件至项目维护者

---

**注意**：本库是JeecgBoot项目的一部分，专门用于处理富文本编辑器的数据格式转换和处理。