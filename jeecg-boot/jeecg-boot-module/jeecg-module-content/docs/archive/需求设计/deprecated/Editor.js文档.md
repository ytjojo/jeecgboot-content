# Editor.js 文档

## 1. 简介

Editor.js 是一个现代化的块式富文本编辑器，它将内容组织为独立的块（blocks），每个块都有特定的类型和数据结构。与传统的富文本编辑器不同，Editor.js 输出的是结构化的 JSON 数据，而不是 HTML 字符串。

### 1.1 核心特点

- **块式编辑**：内容由独立的块组成，每个块都有特定的类型
- **JSON 输出**：输出结构化的 JSON 数据，便于存储和处理
- **插件化架构**：通过插件扩展功能，支持自定义块类型
- **现代化 UI**：简洁直观的用户界面
- **移动端友好**：支持触摸操作和移动端编辑

### 1.2 与传统编辑器的区别

| 特性 | 传统编辑器 | Editor.js |
|------|------------|----------|
| 输出格式 | HTML 字符串 | JSON 对象 |
| 内容结构 | 连续的 HTML | 独立的块 |
| 扩展性 | 插件复杂 | 块插件简单 |
| 数据处理 | 需要解析 HTML | 直接处理 JSON |
| 版本控制 | 困难 | 容易 |

## 2. JSON 数据结构

### 2.1 基本结构

```json
{
  "time": 1672531200000,
  "blocks": [
    {
      "id": "block-id-1",
      "type": "paragraph",
      "data": {
        "text": "这是一个段落"
      }
    }
  ],
  "version": "2.28.2"
}
```

### 2.2 字段说明

- **time**: 文档创建或最后修改的时间戳（毫秒）
- **blocks**: 内容块数组，包含所有的内容块
- **version**: Editor.js 的版本号

### 2.3 块（Block）结构

每个块都包含以下字段：

- **id**: 块的唯一标识符
- **type**: 块的类型（如 paragraph、header、list 等）
- **data**: 块的具体数据，根据块类型而不同

## 3. 常用块类型

### 3.1 段落块（Paragraph）

最基本的文本块，支持基本的文本格式化。

```json
{
  "id": "paragraph-1",
  "type": "paragraph",
  "data": {
    "text": "这是一个<b>粗体</b>和<i>斜体</i>的段落示例。"
  }
}
```

**支持的格式化标签**：
- `<b>` 或 `<strong>`: 粗体
- `<i>` 或 `<em>`: 斜体
- `<u>`: 下划线
- `<s>`: 删除线
- `<code>`: 行内代码
- `<a href="url">`: 链接

### 3.2 标题块（Header）

用于创建不同级别的标题。

```json
{
  "id": "header-1",
  "type": "header",
  "data": {
    "text": "这是一个标题",
    "level": 2
  }
}
```

**参数说明**：
- `text`: 标题文本
- `level`: 标题级别（1-6，对应 h1-h6）

### 3.3 列表块（List）

支持有序列表和无序列表。

```json
{
  "id": "list-1",
  "type": "list",
  "data": {
    "style": "unordered",
    "items": [
      "第一个列表项",
      "第二个列表项",
      "第三个列表项"
    ]
  }
}
```

**参数说明**：
- `style`: 列表样式（"ordered" 或 "unordered"）
- `items`: 列表项数组

### 3.4 引用块（Quote）

用于创建引用内容。

```json
{
  "id": "quote-1",
  "type": "quote",
  "data": {
    "text": "这是一个引用内容",
    "caption": "引用来源",
    "alignment": "left"
  }
}
```

**参数说明**：
- `text`: 引用文本
- `caption`: 引用来源或说明
- `alignment`: 对齐方式（"left", "center", "right"）

### 3.5 图片块（Image）

用于插入图片。

```json
{
  "id": "image-1",
  "type": "image",
  "data": {
    "file": {
      "url": "https://example.com/image.jpg",
      "width": 800,
      "height": 600
    },
    "caption": "图片说明",
    "withBorder": false,
    "withBackground": false,
    "stretched": false
  }
}
```

**参数说明**：
- `file`: 图片文件信息
  - `url`: 图片 URL
  - `width`: 图片宽度
  - `height`: 图片高度
- `caption`: 图片说明
- `withBorder`: 是否显示边框
- `withBackground`: 是否显示背景
- `stretched`: 是否拉伸显示

### 3.6 代码块（Code）

用于显示代码。

```json
{
  "id": "code-1",
  "type": "code",
  "data": {
    "code": "function hello() {\n  console.log('Hello, World!');\n}",
    "language": "javascript"
  }
}
```

**参数说明**：
- `code`: 代码内容
- `language`: 编程语言（可选，用于语法高亮）

### 3.7 表格块（Table）

用于创建表格。

```json
{
  "id": "table-1",
  "type": "table",
  "data": {
    "withHeadings": true,
    "content": [
      ["姓名", "年龄", "职业"],
      ["张三", "25", "工程师"],
      ["李四", "30", "设计师"]
    ]
  }
}
```

**参数说明**：
- `withHeadings`: 第一行是否为表头
- `content`: 表格内容，二维数组

### 3.8 嵌入块（Embed）

用于嵌入外部内容，如视频、音频等。

```json
{
  "id": "embed-1",
  "type": "embed",
  "data": {
    "service": "youtube",
    "source": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "embed": "https://www.youtube.com/embed/dQw4w9WgXcQ",
    "width": 580,
    "height": 320,
    "caption": "视频说明"
  }
}
```

**参数说明**：
- `service`: 服务提供商（如 youtube、vimeo、bilibili 等）
- `source`: 原始 URL
- `embed`: 嵌入 URL
- `width`: 宽度
- `height`: 高度
- `caption`: 说明文字

### 3.9 链接工具块（LinkTool）

用于创建链接预览。

```json
{
  "id": "linktool-1",
  "type": "linkTool",
  "data": {
    "link": "https://example.com",
    "meta": {
      "title": "网站标题",
      "description": "网站描述",
      "image": {
        "url": "https://example.com/image.jpg"
      }
    }
  }
}
```

**参数说明**：
- `link`: 链接 URL
- `meta`: 链接元信息
  - `title`: 页面标题
  - `description`: 页面描述
  - `image`: 预览图片

### 3.10 分隔符块（Delimiter）

用于创建内容分隔符。

```json
{
  "id": "delimiter-1",
  "type": "delimiter",
  "data": {}
}
```

## 4. 完整示例

以下是一个包含多种块类型的完整 JSON 示例：

```json
{
  "time": 1672531200000,
  "blocks": [
    {
      "id": "header-1",
      "type": "header",
      "data": {
        "text": "Editor.js 使用指南",
        "level": 1
      }
    },
    {
      "id": "paragraph-1",
      "type": "paragraph",
      "data": {
        "text": "Editor.js 是一个现代化的<b>块式编辑器</b>，它输出<i>结构化的 JSON 数据</i>。"
      }
    },
    {
      "id": "list-1",
      "type": "list",
      "data": {
        "style": "unordered",
        "items": [
          "块式编辑",
          "JSON 输出",
          "插件化架构"
        ]
      }
    },
    {
      "id": "quote-1",
      "type": "quote",
      "data": {
        "text": "简洁的设计是最高级的复杂。",
        "caption": "达芬奇",
        "alignment": "left"
      }
    },
    {
      "id": "code-1",
      "type": "code",
      "data": {
        "code": "const editor = new EditorJS({\n  holder: 'editorjs',\n  tools: {\n    header: Header,\n    list: List\n  }\n});",
        "language": "javascript"
      }
    },
    {
      "id": "delimiter-1",
      "type": "delimiter",
      "data": {}
    },
    {
      "id": "paragraph-2",
      "type": "paragraph",
      "data": {
        "text": "这就是 Editor.js 的基本用法。"
      }
    }
  ],
  "version": "2.28.2"
}
```

## 5. JSON 格式的优势

### 5.1 结构化存储

- **易于解析**：JSON 格式便于程序解析和处理
- **类型安全**：每个块都有明确的类型定义
- **数据完整性**：包含完整的结构信息

### 5.2 版本控制友好

- **可读性强**：JSON 格式便于人工阅读和比较
- **差异明显**：版本控制系统能够清晰显示内容变化
- **合并简单**：结构化数据便于自动合并

### 5.3 跨平台兼容

- **语言无关**：任何编程语言都能处理 JSON
- **平台通用**：Web、移动端、桌面应用都支持
- **API 友好**：便于通过 API 传输和处理

### 5.4 便于转换

- **HTML 转换**：可以轻松转换为 HTML 显示
- **Markdown 转换**：可以转换为 Markdown 格式
- **其他格式**：可以转换为 PDF、Word 等格式

## 6. 与内容卡片系统的契合

### 6.1 数据结构一致性

Editor.js 的 JSON 输出与我们设计的内容卡片系统高度契合：

```json
{
  "card_type": "article",
  "content": {
    "title": "文章标题",
    "editor_data": {
      "time": 1672531200000,
      "blocks": [...],
      "version": "2.28.2"
    }
  }
}
```

### 6.2 扩展性支持

- **自定义块类型**：可以为特定业务需求创建自定义块
- **插件机制**：通过插件扩展编辑器功能
- **数据验证**：可以对 JSON 数据进行结构验证

### 6.3 性能优化

- **按需渲染**：只渲染可见的块，提高性能
- **懒加载**：支持图片和媒体内容的懒加载
- **缓存友好**：JSON 数据便于缓存和预处理



## 前端保存内容到后端流程
Editor.js 保存内容数据到后端的过程可以概括为以下几步，最终的输出是 JSON 格式的文档：

-----

### 1. 触发保存事件

在 Editor.js 中，当你需要保存内容时，通常会调用 `editor.save()` 方法。这个方法是异步的，所以你需要使用 `.then()` 或 `async/await` 来处理它的返回值。


-----

### 2. 获取 Editor.js 的输出数据

当你调用 `editor.save()` 时，Editor.js 会遍历编辑器中的所有 Block，并根据每个 Block 的配置，将它们的内容转换为一个**标准的 JSON 对象**。这个对象就是 `outputData`。

这个 JSON 对象的结构是固定的，通常包含以下几个关键部分：

  * **`time`**:  保存操作发生的时间戳。
  * **`blocks`**:  一个**数组**，包含了编辑器中所有 Block 的数据。每个数组元素都是一个独立的 JSON 对象，代表一个 Block。
  * **`version`**:  Editor.js 的版本号。

-----

### 3. 每个 Block 的数据结构

在 `blocks` 数组中，每个 Block 的 JSON 数据都包含以下关键字段：

  * **`id`**:  这个 Block 的唯一 ID。
  * **`type`**:  这个 Block 的类型，例如 `"header"`、`"paragraph"`、`"image"`、`"list"` 等。
  * **`data`**:  这个字段包含了这个 Block 的**实际内容**。它的结构会根据 `type` 的不同而变化。
      * 对于 `"paragraph"` 类型，`data` 字段可能包含一个 `text` 属性。
      * 对于 `"header"` 类型，`data` 字段可能包含 `text` 和 `level` 属性。
      * 对于 `"image"` 类型，`data` 字段可能包含 `file`、`caption` 和 `withBorder` 等属性。

-----

### 4. 发送 JSON 数据到后端

### 5. 后端接受 JSON 数据保存到数据库


总而言之，Editor.js 负责生成一个**结构化、标准的 JSON 文档**，而你作为开发者，只需要负责将这个 JSON 文档发送到后端，并在后端进行存储或处理。

### 7.2 数据验证

```javascript
// JSON Schema 验证示例
const editorJsSchema = {
  type: 'object',
  required: ['time', 'blocks', 'version'],
  properties: {
    time: { type: 'number' },
    blocks: {
      type: 'array',
      items: {
        type: 'object',
        required: ['id', 'type', 'data'],
        properties: {
          id: { type: 'string' },
          type: { type: 'string' },
          data: { type: 'object' }
        }
      }
    },
    version: { type: 'string' }
  }
};
```

### 7.3 安全性考虑

- **内容过滤**：对用户输入进行 XSS 过滤
- **HTML 转义**：在渲染时对 HTML 内容进行转义
- **文件上传**：对上传的图片和文件进行安全检查

### 7.4 性能优化

- **数据压缩**：对大型 JSON 数据进行压缩存储
- **索引优化**：为常用查询字段创建索引
- **缓存策略**：对渲染结果进行缓存

## 8. 总结

Editor.js 的 JSON 输出格式为现代内容管理系统提供了强大的基础：

1. **结构化数据**：便于存储、查询和处理
2. **扩展性强**：支持自定义块类型和插件
3. **开发友好**：JSON 格式便于前后端开发
4. **用户体验**：块式编辑提供直观的编辑体验
5. **未来兼容**：结构化数据便于系统升级和迁移

这种设计理念与我们的内容卡片系统完美契合，为构建现代化的内容管理平台奠定了坚实的基础。