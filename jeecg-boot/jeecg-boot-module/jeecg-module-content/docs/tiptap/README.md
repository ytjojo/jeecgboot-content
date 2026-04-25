# Tiptap 编辑器使用指南

## 概述

Tiptap 是一个现代化的富文本编辑器框架，基于 ProseMirror 构建，提供了强大的扩展性和灵活性。它采用"headless-first"的设计理念，让开发者可以完全控制编辑器的外观和行为。

## 核心特性

### 1. 模块化架构
- **无头设计（Headless）**：核心功能与UI完全分离，开发者可以自定义任何样式
- **扩展系统**：通过扩展（Extensions）添加功能，支持 Nodes、Marks 和 Functionality 三种类型
- **框架无关**：支持 Vanilla JavaScript、React、Vue、Svelte 等多种前端框架

### 2. 强大的扩展能力
- **内置扩展**：提供丰富的内置扩展，如 Bold、Italic、Heading、Table 等
- **自定义扩展**：支持开发自定义扩展来满足特定需求
- **StarterKit**：预配置的扩展包，快速启动项目

### 3. 数据持久化
- **多格式支持**：支持 HTML 和 JSON 两种数据格式
- **推荐 JSON 格式**：更灵活、易于解析和处理
- **存储方案**：支持 LocalStorage、数据库等多种存储方式

### 4. 开发体验
- **TypeScript 支持**：完整的类型定义
- **实时协作**：支持多人实时编辑（付费功能）
- **丰富的 API**：提供完整的编程接口

## 技术架构

### 核心组件

```
Tiptap 架构
├── Editor（编辑器核心）
│   ├── Extensions（扩展系统）
│   │   ├── Nodes（块级元素）
│   │   ├── Marks（行内样式）
│   │   └── Functionality（功能扩展）
│   ├── Commands（命令系统）
│   ├── Schema（文档结构）
│   └── State（状态管理）
├── Framework Integrations（框架集成）
│   ├── @tiptap/react
│   ├── @tiptap/vue-3
│   ├── @tiptap/vue-2
│   └── @tiptap/svelte
└── ProseMirror（底层引擎）
    ├── prosemirror-model
    ├── prosemirror-state
    ├── prosemirror-view
    └── prosemirror-transform
```

### 扩展分类

#### 1. Nodes（节点扩展）
块级元素，定义文档的结构：
- `Blockquote`：引用块
- `Heading`：标题（H1-H6）
- `Paragraph`：段落
- `Image`：图片
- `Table`：表格
- `CodeBlock`：代码块

#### 2. Marks（标记扩展）
行内样式，用于文本格式化：
- `Bold`：粗体
- `Italic`：斜体
- `Link`：链接
- `Code`：行内代码
- `Highlight`：高亮
- `Underline`：下划线

#### 3. Functionality（功能扩展）
提供额外功能：
- `History`：撤销/重做
- `Placeholder`：占位符
- `CharacterCount`：字符计数
- `FloatingMenu`：浮动菜单
- `BubbleMenu`：气泡菜单
- `Collaboration`：实时协作

## 支持的框架

| 框架 | 包名 | 状态 |
|------|------|------|
| Vanilla JavaScript | `@tiptap/core` | ✅ 稳定 |
| React | `@tiptap/react` | ✅ 稳定 |
| Next.js | `@tiptap/react` | ✅ 稳定 |
| Vue 3 | `@tiptap/vue-3` | ✅ 稳定 |
| Vue 2 | `@tiptap/vue-2` | ✅ 稳定 |
| Nuxt | `@tiptap/vue-3` | ✅ 稳定 |
| Svelte | `@tiptap/svelte` | ✅ 稳定 |
| Alpine.js | `@tiptap/core` | ✅ 稳定 |
| PHP | `tiptap/tiptap` | ✅ 稳定 |

## 快速开始

### 基本安装

```bash
# 核心包（必需）
npm install @tiptap/core @tiptap/pm

# 选择框架集成包
npm install @tiptap/react      # React
npm install @tiptap/vue-3      # Vue 3
npm install @tiptap/vue-2      # Vue 2

# 基础扩展包（推荐）
npm install @tiptap/starter-kit
```

### 基本使用

```javascript
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'

const editor = new Editor({
  element: document.querySelector('.element'),
  extensions: [
    StarterKit,
  ],
  content: '<p>Hello World! 🌎️</p>',
})
```

## 文档结构

本文档包含以下部分：

1. **[React 集成指南](./react-integration.md)** - React 项目中使用 Tiptap
2. **[Vue 3 集成指南](./vue3-integration.md)** - Vue 3 项目中使用 Tiptap
3. **[样式定制指南](./styling-guide.md)** - 自定义编辑器样式和菜单
4. **[数据持久化指南](./persistence-guide.md)** - 数据存储和恢复方案
5. **[扩展系统指南](./extensions-guide.md)** - 使用和开发扩展
6. **[最佳实践](./best-practices.md)** - 开发建议和常见问题

## 许可证

Tiptap 采用 MIT 许可证，可以免费用于商业项目。部分高级功能（如实时协作）需要付费许可。

## 相关链接

- [官方文档](https://tiptap.dev/)
- [GitHub 仓库](https://github.com/ueberdosis/tiptap)
- [示例和演示](https://tiptap.dev/examples)
- [社区论坛](https://github.com/ueberdosis/tiptap/discussions)

---

*最后更新：2024年1月*