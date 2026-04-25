# Tiptap React 集成指南

## 概述

Tiptap 为 React 提供了专门的集成包 `@tiptap/react`，通过 React Hooks 的方式提供了简洁易用的 API。本指南将详细介绍如何在 React 项目中集成和使用 Tiptap 编辑器。

## 环境要求

- Node.js 16.0 或更高版本
- React 16.8 或更高版本（需要 Hooks 支持）
- npm 或 yarn 包管理器

## 安装依赖

### 基础安装

```bash
# 使用 npm
npm install @tiptap/react @tiptap/pm @tiptap/starter-kit

# 使用 yarn
yarn add @tiptap/react @tiptap/pm @tiptap/starter-kit

# 使用 pnpm
pnpm add @tiptap/react @tiptap/pm @tiptap/starter-kit
```

### 依赖说明

- `@tiptap/react`：React 集成包，提供 React 组件和 Hooks
- `@tiptap/pm`：ProseMirror 核心依赖
- `@tiptap/starter-kit`：基础扩展包，包含常用功能

### 可选扩展

```bash
# 额外的扩展包
npm install @tiptap/extension-table @tiptap/extension-image @tiptap/extension-link
```

## 基本使用

### 1. 创建基础编辑器组件

```jsx
// components/TiptapEditor.jsx
import React from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

/**
 * Tiptap 编辑器组件
 * @param {Object} props - 组件属性
 * @param {string} props.content - 初始内容
 * @param {Function} props.onUpdate - 内容更新回调
 */
const TiptapEditor = ({ content = '', onUpdate }) => {
  // 初始化编辑器实例
  const editor = useEditor({
    extensions: [
      StarterKit, // 包含基础功能：Bold, Italic, Paragraph, Heading 等
    ],
    content: content, // 设置初始内容
    onUpdate: ({ editor }) => {
      // 内容更新时的回调函数
      const html = editor.getHTML()
      const json = editor.getJSON()
      onUpdate && onUpdate({ html, json })
    },
    // 编辑器属性配置
    editorProps: {
      attributes: {
        class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto focus:outline-none',
      },
    },
  })

  // 编辑器未初始化时显示加载状态
  if (!editor) {
    return <div>Loading...</div>
  }

  return (
    <div className="tiptap-editor">
      {/* 工具栏 */}
      <div className="toolbar">
        <button
          onClick={() => editor.chain().focus().toggleBold().run()}
          className={editor.isActive('bold') ? 'is-active' : ''}
        >
          粗体
        </button>
        <button
          onClick={() => editor.chain().focus().toggleItalic().run()}
          className={editor.isActive('italic') ? 'is-active' : ''}
        >
          斜体
        </button>
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          className={editor.isActive('heading', { level: 1 }) ? 'is-active' : ''}
        >
          H1
        </button>
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          className={editor.isActive('heading', { level: 2 }) ? 'is-active' : ''}
        >
          H2
        </button>
        <button
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          className={editor.isActive('bulletList') ? 'is-active' : ''}
        >
          无序列表
        </button>
        <button
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          className={editor.isActive('orderedList') ? 'is-active' : ''}
        >
          有序列表
        </button>
      </div>
      
      {/* 编辑器内容区域 */}
      <EditorContent editor={editor} className="editor-content" />
    </div>
  )
}

export default TiptapEditor
```

### 2. 在应用中使用编辑器

```jsx
// App.jsx
import React, { useState } from 'react'
import TiptapEditor from './components/TiptapEditor'
import './App.css'

/**
 * 主应用组件
 */
function App() {
  const [content, setContent] = useState('<p>欢迎使用 Tiptap 编辑器！</p>')
  const [savedContent, setSavedContent] = useState('')

  /**
   * 处理编辑器内容更新
   * @param {Object} data - 更新的数据
   * @param {string} data.html - HTML 格式内容
   * @param {Object} data.json - JSON 格式内容
   */
  const handleEditorUpdate = ({ html, json }) => {
    console.log('HTML:', html)
    console.log('JSON:', json)
    setContent(html)
  }

  /**
   * 保存内容到本地存储
   */
  const saveContent = () => {
    localStorage.setItem('tiptap-content', content)
    setSavedContent(content)
    alert('内容已保存！')
  }

  /**
   * 从本地存储加载内容
   */
  const loadContent = () => {
    const saved = localStorage.getItem('tiptap-content')
    if (saved) {
      setContent(saved)
      setSavedContent(saved)
    }
  }

  return (
    <div className="App">
      <header className="App-header">
        <h1>Tiptap React 示例</h1>
        <div className="actions">
          <button onClick={saveContent}>保存内容</button>
          <button onClick={loadContent}>加载内容</button>
        </div>
      </header>
      
      <main className="main-content">
        <TiptapEditor 
          content={content} 
          onUpdate={handleEditorUpdate}
        />
        
        {savedContent && (
          <div className="saved-content">
            <h3>已保存的内容：</h3>
            <div dangerouslySetInnerHTML={{ __html: savedContent }} />
          </div>
        )}
      </main>
    </div>
  )
}

export default App
```

### 3. 样式文件

```css
/* App.css */
.App {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.App-header {
  text-align: center;
  margin-bottom: 30px;
}

.actions {
  margin-top: 20px;
}

.actions button {
  margin: 0 10px;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.actions button:hover {
  background-color: #0056b3;
}

/* 编辑器样式 */
.tiptap-editor {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 30px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  padding: 10px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ddd;
}

.toolbar button {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.toolbar button:hover {
  background-color: #e9ecef;
}

.toolbar button.is-active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.editor-content {
  padding: 20px;
  min-height: 300px;
}

/* ProseMirror 编辑器样式 */
.ProseMirror {
  outline: none;
  line-height: 1.6;
}

.ProseMirror h1 {
  font-size: 2em;
  font-weight: bold;
  margin: 0.5em 0;
}

.ProseMirror h2 {
  font-size: 1.5em;
  font-weight: bold;
  margin: 0.5em 0;
}

.ProseMirror p {
  margin: 0.5em 0;
}

.ProseMirror ul, .ProseMirror ol {
  padding-left: 20px;
  margin: 0.5em 0;
}

.ProseMirror blockquote {
  border-left: 4px solid #ddd;
  padding-left: 16px;
  margin: 1em 0;
  font-style: italic;
}

.saved-content {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ddd;
}
```

## 高级用法

### 1. 使用 EditorContext

当需要在多个组件中访问编辑器实例时，可以使用 `EditorContext`：

```jsx
// components/EditorProvider.jsx
import React from 'react'
import { EditorProvider } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

/**
 * 编辑器上下文提供者
 * @param {Object} props - 组件属性
 * @param {React.ReactNode} props.children - 子组件
 */
const TiptapEditorProvider = ({ children }) => {
  const extensions = [
    StarterKit,
  ]

  return (
    <EditorProvider 
      extensions={extensions} 
      content="<p>Hello World!</p>"
    >
      {children}
    </EditorProvider>
  )
}

export default TiptapEditorProvider
```

```jsx
// components/EditorToolbar.jsx
import React from 'react'
import { useCurrentEditor } from '@tiptap/react'

/**
 * 编辑器工具栏组件
 */
const EditorToolbar = () => {
  const { editor } = useCurrentEditor()

  if (!editor) {
    return null
  }

  return (
    <div className="toolbar">
      <button
        onClick={() => editor.chain().focus().toggleBold().run()}
        className={editor.isActive('bold') ? 'is-active' : ''}
      >
        粗体
      </button>
      <button
        onClick={() => editor.chain().focus().toggleItalic().run()}
        className={editor.isActive('italic') ? 'is-active' : ''}
      >
        斜体
      </button>
    </div>
  )
}

export default EditorToolbar
```

### 2. 响应编辑器状态变化

```jsx
import React from 'react'
import { useEditor, EditorContent, useEditorState } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

/**
 * 带状态监听的编辑器组件
 */
const StatefulEditor = () => {
  const editor = useEditor({
    extensions: [StarterKit],
    content: '<p>Hello World!</p>',
  })

  // 监听编辑器状态变化
  const { isFocused, isEmpty, characterCount } = useEditorState({
    editor,
    selector: ({ editor }) => ({
      isFocused: editor.isFocused,
      isEmpty: editor.isEmpty,
      characterCount: editor.storage.characterCount?.characters() || 0,
    }),
  })

  return (
    <div>
      <div className="status-bar">
        <span>焦点状态: {isFocused ? '已聚焦' : '未聚焦'}</span>
        <span>内容状态: {isEmpty ? '空' : '非空'}</span>
        <span>字符数: {characterCount}</span>
      </div>
      <EditorContent editor={editor} />
    </div>
  )
}

export default StatefulEditor
```

### 3. SSR 支持（Next.js）

在服务端渲染环境中使用 Tiptap：

```jsx
// components/TiptapEditor.jsx
import React from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

/**
 * 支持 SSR 的编辑器组件
 */
const TiptapEditor = () => {
  const editor = useEditor({
    extensions: [StarterKit],
    content: '<p>Hello World!</p>',
    // 关键配置：禁用立即渲染，避免 SSR 水合问题
    immediatelyRender: false,
  })

  return <EditorContent editor={editor} />
}

export default TiptapEditor
```

## 性能优化

### 1. 避免不必要的重渲染

```jsx
import React, { memo, useCallback } from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

/**
 * 优化后的编辑器组件
 */
const OptimizedEditor = memo(({ initialContent, onUpdate }) => {
  // 使用 useCallback 优化回调函数
  const handleUpdate = useCallback(({ editor }) => {
    const html = editor.getHTML()
    onUpdate?.(html)
  }, [onUpdate])

  const editor = useEditor({
    extensions: [StarterKit],
    content: initialContent,
    onUpdate: handleUpdate,
  })

  return <EditorContent editor={editor} />
})

OptimizedEditor.displayName = 'OptimizedEditor'

export default OptimizedEditor
```

### 2. 延迟加载扩展

```jsx
import React, { useState, useEffect } from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

/**
 * 延迟加载扩展的编辑器
 */
const LazyEditor = () => {
  const [extensions, setExtensions] = useState([StarterKit])

  useEffect(() => {
    // 延迟加载额外的扩展
    const loadExtensions = async () => {
      const { default: Table } = await import('@tiptap/extension-table')
      const { default: TableRow } = await import('@tiptap/extension-table-row')
      const { default: TableCell } = await import('@tiptap/extension-table-cell')
      const { default: TableHeader } = await import('@tiptap/extension-table-header')
      
      setExtensions(prev => [
        ...prev,
        Table.configure({
          resizable: true,
        }),
        TableRow,
        TableHeader,
        TableCell,
      ])
    }

    loadExtensions()
  }, [])

  const editor = useEditor({
    extensions,
    content: '<p>Hello World!</p>',
  })

  return <EditorContent editor={editor} />
}

export default LazyEditor
```

## 常见问题

### 1. 编辑器不显示内容

**问题**：编辑器渲染但不显示内容

**解决方案**：
- 检查是否正确导入了 CSS 样式
- 确保 `EditorContent` 组件正确传入了 `editor` 属性
- 检查初始内容格式是否正确

### 2. 工具栏按钮状态不更新

**问题**：点击工具栏按钮后，按钮的激活状态不更新

**解决方案**：
- 确保使用了 `editor.isActive()` 方法检查状态
- 检查是否在按钮点击时调用了 `editor.chain().focus()`

### 3. 内容更新回调不触发

**问题**：编辑器内容变化时，`onUpdate` 回调不执行

**解决方案**：
- 检查 `onUpdate` 函数是否正确传递给 `useEditor`
- 确保回调函数没有语法错误
- 使用 `useCallback` 包装回调函数避免重复创建

## 总结

Tiptap 的 React 集成提供了强大而灵活的富文本编辑功能。通过合理使用 Hooks 和组件化设计，可以构建出功能丰富、性能优秀的编辑器应用。

关键要点：
1. 使用 `useEditor` Hook 初始化编辑器
2. 通过 `EditorContent` 组件渲染编辑器
3. 利用命令链（Command Chain）实现功能
4. 合理使用 Context 和状态管理
5. 注意 SSR 环境的特殊配置
6. 通过 memo 和 useCallback 优化性能

---

*下一步：查看 [Vue 3 集成指南](./vue3-integration.md)*