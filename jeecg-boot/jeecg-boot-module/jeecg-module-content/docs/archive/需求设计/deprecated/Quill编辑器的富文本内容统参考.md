### 1 概述
本文档定义了基于Quill编辑器的富文本内容统一JSON格式方案，支持多种特殊元素类型的标记和存储。该方案通过扩展Quill的Delta格式，实现对@用户、#话题、标签、股票、链接、文件、图片、视频、音频、卡片等特殊元素的统一处理。

### 2 Quill Delta格式详解

#### 2.1 Delta基础概念
Quill Delta是一种基于JSON的富文本内容描述格式，用于表示文档内容和变更操作。Delta格式具有以下特点：

- **人类可读**：基于JSON格式，结构清晰
- **机器友好**：易于解析和处理
- **操作变换支持**：适用于实时协作编辑
- **无歧义性**：相比HTML更加精确和一致

#### 2.2 Delta基础结构
Delta是一个包含`ops`属性的对象，`ops`是一个操作数组：

```json
{
  "ops": [
    // 操作数组
  ]
}
```

#### 2.3 三种核心操作

##### 2.3.1 Insert操作（插入）
用于插入文本或嵌入元素：

**插入纯文本**
```json
{
  "insert": "Hello World"
}
```

**插入带格式的文本**
```json
{
  "insert": "Hello World",
  "attributes": {
    "bold": true,
    "italic": true,
    "color": "#ff0000"
  }
}
```

**插入嵌入元素**
```json
{
  "insert": {
    "image": "https://example.com/image.jpg"
  },
  "attributes": {
    "width": "300",
    "height": "200"
  }
}
```

**插入换行符**
```json
{
  "insert": "\n",
  "attributes": {
    "header": 1
  }
}
```

##### 2.3.2 Retain操作（保留）
用于保留指定数量的字符，通常用于格式化或跳过内容：

```json
{
  "retain": 5
}
```

**保留并应用格式**
```json
{
  "retain": 5,
  "attributes": {
    "bold": true
  }
}
```

##### 2.3.3 Delete操作（删除）
用于删除指定数量的字符：

```json
{
  "delete": 3
}
```

#### 2.4 常用属性格式

##### 2.4.1 文本格式属性
```json
{
  "attributes": {
    "bold": true,           // 粗体
    "italic": true,         // 斜体
    "underline": true,      // 下划线
    "strike": true,         // 删除线
    "color": "#ff0000",     // 文字颜色
    "background": "#ffff00", // 背景颜色
    "font": "serif",        // 字体
    "size": "18px"          // 字体大小
  }
}
```

##### 2.4.2 块级格式属性
```json
{
  "attributes": {
    "header": 1,            // 标题级别 (1-6)
    "blockquote": true,     // 引用块
    "list": "ordered",      // 有序列表
    "list": "bullet",       // 无序列表
    "indent": 1,            // 缩进级别
    "align": "center",      // 对齐方式
    "direction": "rtl"      // 文本方向
  }
}
```