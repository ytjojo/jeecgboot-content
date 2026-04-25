# Tiptap 样式定制和自定义菜单指南

## 概述

Tiptap 采用 "headless-first" 设计理念，核心扩展不包含任何 UI 或样式，这为开发者提供了完全的样式控制权。本指南将详细介绍如何为 Tiptap 编辑器添加样式，以及如何构建自定义菜单和工具栏。

## 样式定制

### 1. 基础 CSS 样式

#### 编辑器容器样式

```css
/* 基础编辑器样式 */
.tiptap {
  outline: none;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-height: 200px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
}

.tiptap:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 占位符样式 */
.tiptap p.is-editor-empty:first-child::before {
  content: attr(data-placeholder);
  float: left;
  color: #9ca3af;
  pointer-events: none;
  height: 0;
}
```

#### 内容元素样式

```css
/* 标题样式 */
.tiptap h1 {
  font-size: 2.25rem;
  font-weight: 700;
  line-height: 1.2;
  margin: 1.5rem 0 1rem 0;
  color: #1f2937;
}

.tiptap h2 {
  font-size: 1.875rem;
  font-weight: 600;
  line-height: 1.3;
  margin: 1.25rem 0 0.75rem 0;
  color: #1f2937;
}

.tiptap h3 {
  font-size: 1.5rem;
  font-weight: 600;
  line-height: 1.4;
  margin: 1rem 0 0.5rem 0;
  color: #374151;
}

/* 段落样式 */
.tiptap p {
  margin: 0.75rem 0;
  line-height: 1.7;
}

/* 列表样式 */
.tiptap ul {
  list-style-type: disc;
  margin: 1rem 0;
  padding-left: 1.5rem;
}

.tiptap ol {
  list-style-type: decimal;
  margin: 1rem 0;
  padding-left: 1.5rem;
}

.tiptap li {
  margin: 0.25rem 0;
  line-height: 1.6;
}

.tiptap li p {
  margin: 0;
}

/* 引用样式 */
.tiptap blockquote {
  border-left: 4px solid #3b82f6;
  padding-left: 1rem;
  margin: 1.5rem 0;
  font-style: italic;
  color: #6b7280;
  background-color: #f8fafc;
  border-radius: 0 4px 4px 0;
}

/* 代码样式 */
.tiptap code {
  background-color: #f3f4f6;
  color: #e11d48;
  padding: 0.125rem 0.25rem;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875em;
}

.tiptap pre {
  background-color: #1f2937;
  color: #f9fafb;
  padding: 1rem;
  border-radius: 6px;
  margin: 1rem 0;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
}

.tiptap pre code {
  background: none;
  color: inherit;
  padding: 0;
  border-radius: 0;
  font-size: inherit;
}

/* 链接样式 */
.tiptap a {
  color: #3b82f6;
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.2s;
}

.tiptap a:hover {
  color: #1d4ed8;
}

/* 强调样式 */
.tiptap strong {
  font-weight: 700;
  color: #1f2937;
}

.tiptap em {
  font-style: italic;
}

.tiptap u {
  text-decoration: underline;
}

.tiptap s {
  text-decoration: line-through;
}

/* 高亮样式 */
.tiptap mark {
  background-color: #fef08a;
  color: #92400e;
  padding: 0.125rem 0.25rem;
  border-radius: 2px;
}

/* 水平分割线 */
.tiptap hr {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 2rem 0;
}

/* 图片样式 */
.tiptap img {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 1rem 0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* 表格样式 */
.tiptap table {
  border-collapse: collapse;
  width: 100%;
  margin: 1.5rem 0;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.tiptap th,
.tiptap td {
  border: 1px solid #e5e7eb;
  padding: 0.75rem;
  text-align: left;
}

.tiptap th {
  background-color: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.tiptap tr:nth-child(even) {
  background-color: #f9fafb;
}
```

### 2. CSS Modules 样式

```css
/* Editor.module.css */
.editor {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 12px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.toolbarButton {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbarButton:hover {
  background-color: #f3f4f6;
}

.toolbarButton.active {
  background-color: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.toolbarButton:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.content {
  padding: 16px;
  min-height: 200px;
}

/* 确保全局样式应用到编辑器 */
:global(.tiptap) {
  outline: none;
  font-family: inherit;
}
```

```jsx
// React 组件中使用 CSS Modules
import styles from './Editor.module.css'

function TiptapEditor() {
  return (
    <div className={styles.editor}>
      <div className={styles.toolbar}>
        <button className={`${styles.toolbarButton} ${editor?.isActive('bold') ? styles.active : ''}`}>
          粗体
        </button>
      </div>
      <EditorContent editor={editor} className={styles.content} />
    </div>
  )
}
```

### 3. Tailwind CSS 集成

#### 安装和配置

```bash
# 安装 Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# 安装 Tailwind Typography 插件（可选）
npm install -D @tailwindcss/typography
```

```javascript
// tailwind.config.js
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx,vue}",
  ],
  theme: {
    extend: {
      typography: {
        DEFAULT: {
          css: {
            maxWidth: 'none',
            color: '#374151',
            '[class~="lead"]': {
              color: '#6b7280',
            },
            a: {
              color: '#3b82f6',
              textDecoration: 'underline',
              '&:hover': {
                color: '#1d4ed8',
              },
            },
            strong: {
              color: '#1f2937',
              fontWeight: '700',
            },
            code: {
              color: '#e11d48',
              backgroundColor: '#f3f4f6',
              padding: '0.125rem 0.25rem',
              borderRadius: '0.25rem',
              fontWeight: '400',
            },
            'code::before': {
              content: '""',
            },
            'code::after': {
              content: '""',
            },
          },
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}
```

#### 使用 Tailwind 样式

```jsx
// React 组件
function TiptapEditor() {
  return (
    <div className="border border-gray-300 rounded-lg overflow-hidden">
      {/* 工具栏 */}
      <div className="flex flex-wrap gap-1 p-3 bg-gray-50 border-b border-gray-200">
        <button
          className={`px-3 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors ${
            editor?.isActive('bold') 
              ? 'bg-blue-500 text-white border-blue-500' 
              : 'bg-white'
          }`}
          onClick={() => editor?.chain().focus().toggleBold().run()}
        >
          <span className="font-bold">B</span>
        </button>
        
        <button
          className={`px-3 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors ${
            editor?.isActive('italic') 
              ? 'bg-blue-500 text-white border-blue-500' 
              : 'bg-white'
          }`}
          onClick={() => editor?.chain().focus().toggleItalic().run()}
        >
          <span className="italic">I</span>
        </button>
        
        <div className="w-px bg-gray-300 mx-1"></div>
        
        <button
          className={`px-3 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors ${
            editor?.isActive('heading', { level: 1 }) 
              ? 'bg-blue-500 text-white border-blue-500' 
              : 'bg-white'
          }`}
          onClick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}
        >
          H1
        </button>
        
        <button
          className={`px-3 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors ${
            editor?.isActive('heading', { level: 2 }) 
              ? 'bg-blue-500 text-white border-blue-500' 
              : 'bg-white'
          }`}
          onClick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
        >
          H2
        </button>
      </div>
      
      {/* 编辑器内容 */}
      <EditorContent 
        editor={editor} 
        className="prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto p-4 focus:outline-none min-h-[200px]"
      />
    </div>
  )
}
```

```vue
<!-- Vue 组件 -->
<template>
  <div class="border border-gray-300 rounded-lg overflow-hidden">
    <!-- 工具栏 -->
    <div class="flex flex-wrap gap-1 p-3 bg-gray-50 border-b border-gray-200">
      <button
        @click="editor?.chain().focus().toggleBold().run()"
        :class="[
          'px-3 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors',
          editor?.isActive('bold') 
            ? 'bg-blue-500 text-white border-blue-500' 
            : 'bg-white'
        ]"
      >
        <span class="font-bold">B</span>
      </button>
      
      <button
        @click="editor?.chain().focus().toggleItalic().run()"
        :class="[
          'px-3 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors',
          editor?.isActive('italic') 
            ? 'bg-blue-500 text-white border-blue-500' 
            : 'bg-white'
        ]"
      >
        <span class="italic">I</span>
      </button>
    </div>
    
    <!-- 编辑器内容 -->
    <EditorContent 
      :editor="editor" 
      class="prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto p-4 focus:outline-none min-h-[200px]"
    />
  </div>
</template>
```

### 4. 通过扩展添加自定义类

```javascript
// 为段落添加自定义类
import Paragraph from '@tiptap/extension-paragraph'

const CustomParagraph = Paragraph.extend({
  addAttributes() {
    return {
      class: {
        default: 'custom-paragraph',
      },
    }
  },
  
  renderHTML({ HTMLAttributes }) {
    return ['p', { ...HTMLAttributes, class: 'custom-paragraph' }, 0]
  },
})

// 为标题添加自定义类
import Heading from '@tiptap/extension-heading'

const CustomHeading = Heading.extend({
  renderHTML({ node, HTMLAttributes }) {
    const level = node.attrs.level
    const classes = {
      1: 'text-4xl font-bold text-gray-900 mb-4',
      2: 'text-3xl font-semibold text-gray-800 mb-3',
      3: 'text-2xl font-medium text-gray-700 mb-2',
    }
    
    return [
      `h${level}`,
      { ...HTMLAttributes, class: classes[level] || '' },
      0
    ]
  },
})
```

### 5. 编辑器容器属性定制

```javascript
const editor = new Editor({
  extensions: [StarterKit],
  content: '<p>Hello World!</p>',
  editorProps: {
    attributes: {
      class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto focus:outline-none',
      spellcheck: 'false',
      'data-testid': 'editor-content',
    },
  },
})
```

## 自定义菜单开发

### 1. 基础工具栏组件

#### React 工具栏组件

```jsx
// components/Toolbar.jsx
import React from 'react'
import {
  Bold,
  Italic,
  Underline,
  Strikethrough,
  Code,
  Heading1,
  Heading2,
  Heading3,
  List,
  ListOrdered,
  Quote,
  Undo,
  Redo,
  Link,
  Image,
  Table,
} from 'lucide-react'

/**
 * 工具栏按钮组件
 */
const ToolbarButton = ({ 
  onClick, 
  isActive = false, 
  disabled = false, 
  children, 
  title 
}) => {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      title={title}
      className={`
        p-2 rounded hover:bg-gray-100 transition-colors
        ${isActive ? 'bg-blue-500 text-white hover:bg-blue-600' : 'text-gray-700'}
        ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
      `}
    >
      {children}
    </button>
  )
}

/**
 * 工具栏分隔符
 */
const ToolbarDivider = () => {
  return <div className="w-px h-6 bg-gray-300 mx-1" />
}

/**
 * 主工具栏组件
 */
const Toolbar = ({ editor }) => {
  if (!editor) {
    return null
  }

  /**
   * 插入链接
   */
  const insertLink = () => {
    const url = window.prompt('请输入链接地址:')
    if (url) {
      editor.chain().focus().setLink({ href: url }).run()
    }
  }

  /**
   * 插入图片
   */
  const insertImage = () => {
    const url = window.prompt('请输入图片地址:')
    if (url) {
      editor.chain().focus().setImage({ src: url }).run()
    }
  }

  /**
   * 插入表格
   */
  const insertTable = () => {
    editor
      .chain()
      .focus()
      .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
      .run()
  }

  return (
    <div className="flex flex-wrap items-center gap-1 p-2 border-b border-gray-200 bg-gray-50">
      {/* 文本格式 */}
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleBold().run()}
        isActive={editor.isActive('bold')}
        title="粗体 (Ctrl+B)"
      >
        <Bold size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleItalic().run()}
        isActive={editor.isActive('italic')}
        title="斜体 (Ctrl+I)"
      >
        <Italic size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleUnderline().run()}
        isActive={editor.isActive('underline')}
        title="下划线 (Ctrl+U)"
      >
        <Underline size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleStrike().run()}
        isActive={editor.isActive('strike')}
        title="删除线"
      >
        <Strikethrough size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleCode().run()}
        isActive={editor.isActive('code')}
        title="行内代码"
      >
        <Code size={16} />
      </ToolbarButton>
      
      <ToolbarDivider />
      
      {/* 标题 */}
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
        isActive={editor.isActive('heading', { level: 1 })}
        title="一级标题"
      >
        <Heading1 size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
        isActive={editor.isActive('heading', { level: 2 })}
        title="二级标题"
      >
        <Heading2 size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
        isActive={editor.isActive('heading', { level: 3 })}
        title="三级标题"
      >
        <Heading3 size={16} />
      </ToolbarButton>
      
      <ToolbarDivider />
      
      {/* 列表 */}
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleBulletList().run()}
        isActive={editor.isActive('bulletList')}
        title="无序列表"
      >
        <List size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleOrderedList().run()}
        isActive={editor.isActive('orderedList')}
        title="有序列表"
      >
        <ListOrdered size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().toggleBlockquote().run()}
        isActive={editor.isActive('blockquote')}
        title="引用"
      >
        <Quote size={16} />
      </ToolbarButton>
      
      <ToolbarDivider />
      
      {/* 插入元素 */}
      <ToolbarButton
        onClick={insertLink}
        isActive={editor.isActive('link')}
        title="插入链接"
      >
        <Link size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={insertImage}
        title="插入图片"
      >
        <Image size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={insertTable}
        title="插入表格"
      >
        <Table size={16} />
      </ToolbarButton>
      
      <ToolbarDivider />
      
      {/* 撤销重做 */}
      <ToolbarButton
        onClick={() => editor.chain().focus().undo().run()}
        disabled={!editor.can().undo()}
        title="撤销 (Ctrl+Z)"
      >
        <Undo size={16} />
      </ToolbarButton>
      
      <ToolbarButton
        onClick={() => editor.chain().focus().redo().run()}
        disabled={!editor.can().redo()}
        title="重做 (Ctrl+Y)"
      >
        <Redo size={16} />
      </ToolbarButton>
    </div>
  )
}

export default Toolbar
```

#### Vue 工具栏组件

```vue
<!-- components/Toolbar.vue -->
<template>
  <div class="flex flex-wrap items-center gap-1 p-2 border-b border-gray-200 bg-gray-50" v-if="editor">
    <!-- 文本格式 -->
    <ToolbarButton
      @click="editor.chain().focus().toggleBold().run()"
      :is-active="editor.isActive('bold')"
      title="粗体 (Ctrl+B)"
    >
      <BoldIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarButton
      @click="editor.chain().focus().toggleItalic().run()"
      :is-active="editor.isActive('italic')"
      title="斜体 (Ctrl+I)"
    >
      <ItalicIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarButton
      @click="editor.chain().focus().toggleUnderline().run()"
      :is-active="editor.isActive('underline')"
      title="下划线 (Ctrl+U)"
    >
      <UnderlineIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarDivider />
    
    <!-- 标题 -->
    <ToolbarButton
      @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
      :is-active="editor.isActive('heading', { level: 1 })"
      title="一级标题"
    >
      <Heading1Icon :size="16" />
    </ToolbarButton>
    
    <ToolbarButton
      @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
      :is-active="editor.isActive('heading', { level: 2 })"
      title="二级标题"
    >
      <Heading2Icon :size="16" />
    </ToolbarButton>
    
    <ToolbarDivider />
    
    <!-- 列表 -->
    <ToolbarButton
      @click="editor.chain().focus().toggleBulletList().run()"
      :is-active="editor.isActive('bulletList')"
      title="无序列表"
    >
      <ListIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarButton
      @click="editor.chain().focus().toggleOrderedList().run()"
      :is-active="editor.isActive('orderedList')"
      title="有序列表"
    >
      <ListOrderedIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarDivider />
    
    <!-- 插入元素 -->
    <ToolbarButton
      @click="insertLink"
      :is-active="editor.isActive('link')"
      title="插入链接"
    >
      <LinkIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarButton
      @click="insertImage"
      title="插入图片"
    >
      <ImageIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarDivider />
    
    <!-- 撤销重做 -->
    <ToolbarButton
      @click="editor.chain().focus().undo().run()"
      :disabled="!editor.can().undo()"
      title="撤销 (Ctrl+Z)"
    >
      <UndoIcon :size="16" />
    </ToolbarButton>
    
    <ToolbarButton
      @click="editor.chain().focus().redo().run()"
      :disabled="!editor.can().redo()"
      title="重做 (Ctrl+Y)"
    >
      <RedoIcon :size="16" />
    </ToolbarButton>
  </div>
</template>

<script setup>
import { 
  Bold as BoldIcon,
  Italic as ItalicIcon,
  Underline as UnderlineIcon,
  Heading1 as Heading1Icon,
  Heading2 as Heading2Icon,
  List as ListIcon,
  ListOrdered as ListOrderedIcon,
  Link as LinkIcon,
  Image as ImageIcon,
  Undo as UndoIcon,
  Redo as RedoIcon,
} from 'lucide-vue-next'

/**
 * 组件属性
 */
const props = defineProps({
  editor: {
    type: Object,
    required: true,
  },
})

/**
 * 插入链接
 */
const insertLink = () => {
  const url = window.prompt('请输入链接地址:')
  if (url) {
    props.editor.chain().focus().setLink({ href: url }).run()
  }
}

/**
 * 插入图片
 */
const insertImage = () => {
  const url = window.prompt('请输入图片地址:')
  if (url) {
    props.editor.chain().focus().setImage({ src: url }).run()
  }
}
</script>

<script>
/**
 * 工具栏按钮组件
 */
export const ToolbarButton = {
  props: {
    isActive: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: '',
    },
  },
  template: `
    <button
      @click="$emit('click')"
      :disabled="disabled"
      :title="title"
      :class="[
        'p-2 rounded hover:bg-gray-100 transition-colors',
        isActive ? 'bg-blue-500 text-white hover:bg-blue-600' : 'text-gray-700',
        disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'
      ]"
    >
      <slot />
    </button>
  `,
  emits: ['click'],
}

/**
 * 工具栏分隔符组件
 */
export const ToolbarDivider = {
  template: '<div class="w-px h-6 bg-gray-300 mx-1" />',
}
</script>
```

### 2. 气泡菜单（Bubble Menu）

#### React 气泡菜单

```jsx
// components/BubbleMenu.jsx
import React from 'react'
import { BubbleMenu } from '@tiptap/react'
import { Bold, Italic, Underline, Link, Unlink } from 'lucide-react'

const CustomBubbleMenu = ({ editor }) => {
  if (!editor) {
    return null
  }

  const setLink = () => {
    const url = window.prompt('请输入链接地址:')
    if (url) {
      editor.chain().focus().setLink({ href: url }).run()
    }
  }

  const unsetLink = () => {
    editor.chain().focus().unsetLink().run()
  }

  return (
    <BubbleMenu
      editor={editor}
      tippyOptions={{ duration: 100 }}
      className="flex items-center gap-1 p-2 bg-white border border-gray-200 rounded-lg shadow-lg"
    >
      <button
        onClick={() => editor.chain().focus().toggleBold().run()}
        className={`p-2 rounded hover:bg-gray-100 ${
          editor.isActive('bold') ? 'bg-blue-500 text-white' : 'text-gray-700'
        }`}
        title="粗体"
      >
        <Bold size={14} />
      </button>
      
      <button
        onClick={() => editor.chain().focus().toggleItalic().run()}
        className={`p-2 rounded hover:bg-gray-100 ${
          editor.isActive('italic') ? 'bg-blue-500 text-white' : 'text-gray-700'
        }`}
        title="斜体"
      >
        <Italic size={14} />
      </button>
      
      <button
        onClick={() => editor.chain().focus().toggleUnderline().run()}
        className={`p-2 rounded hover:bg-gray-100 ${
          editor.isActive('underline') ? 'bg-blue-500 text-white' : 'text-gray-700'
        }`}
        title="下划线"
      >
        <Underline size={14} />
      </button>
      
      <div className="w-px h-4 bg-gray-300 mx-1" />
      
      {editor.isActive('link') ? (
        <button
          onClick={unsetLink}
          className="p-2 rounded hover:bg-gray-100 text-gray-700"
          title="移除链接"
        >
          <Unlink size={14} />
        </button>
      ) : (
        <button
          onClick={setLink}
          className="p-2 rounded hover:bg-gray-100 text-gray-700"
          title="添加链接"
        >
          <Link size={14} />
        </button>
      )}
    </BubbleMenu>
  )
}

export default CustomBubbleMenu
```

#### Vue 气泡菜单

```vue
<!-- components/BubbleMenu.vue -->
<template>
  <BubbleMenu
    :editor="editor"
    :tippy-options="{ duration: 100 }"
    class="flex items-center gap-1 p-2 bg-white border border-gray-200 rounded-lg shadow-lg"
    v-if="editor"
  >
    <button
      @click="editor.chain().focus().toggleBold().run()"
      :class="[
        'p-2 rounded hover:bg-gray-100',
        editor.isActive('bold') ? 'bg-blue-500 text-white' : 'text-gray-700'
      ]"
      title="粗体"
    >
      <BoldIcon :size="14" />
    </button>
    
    <button
      @click="editor.chain().focus().toggleItalic().run()"
      :class="[
        'p-2 rounded hover:bg-gray-100',
        editor.isActive('italic') ? 'bg-blue-500 text-white' : 'text-gray-700'
      ]"
      title="斜体"
    >
      <ItalicIcon :size="14" />
    </button>
    
    <button
      @click="editor.chain().focus().toggleUnderline().run()"
      :class="[
        'p-2 rounded hover:bg-gray-100',
        editor.isActive('underline') ? 'bg-blue-500 text-white' : 'text-gray-700'
      ]"
      title="下划线"
    >
      <UnderlineIcon :size="14" />
    </button>
    
    <div class="w-px h-4 bg-gray-300 mx-1" />
    
    <button
      v-if="editor.isActive('link')"
      @click="unsetLink"
      class="p-2 rounded hover:bg-gray-100 text-gray-700"
      title="移除链接"
    >
      <UnlinkIcon :size="14" />
    </button>
    
    <button
      v-else
      @click="setLink"
      class="p-2 rounded hover:bg-gray-100 text-gray-700"
      title="添加链接"
    >
      <LinkIcon :size="14" />
    </button>
  </BubbleMenu>
</template>

<script setup>
import { BubbleMenu } from '@tiptap/vue-3'
import { 
  Bold as BoldIcon,
  Italic as ItalicIcon,
  Underline as UnderlineIcon,
  Link as LinkIcon,
  Unlink as UnlinkIcon,
} from 'lucide-vue-next'

const props = defineProps({
  editor: {
    type: Object,
    required: true,
  },
})

const setLink = () => {
  const url = window.prompt('请输入链接地址:')
  if (url) {
    props.editor.chain().focus().setLink({ href: url }).run()
  }
}

const unsetLink = () => {
  props.editor.chain().focus().unsetLink().run()
}
</script>
```

### 3. 浮动菜单（Floating Menu）

```jsx
// React 浮动菜单
import React from 'react'
import { FloatingMenu } from '@tiptap/react'
import { Heading1, Heading2, List, Quote, Image } from 'lucide-react'

const CustomFloatingMenu = ({ editor }) => {
  if (!editor) {
    return null
  }

  const insertImage = () => {
    const url = window.prompt('请输入图片地址:')
    if (url) {
      editor.chain().focus().setImage({ src: url }).run()
    }
  }

  return (
    <FloatingMenu
      editor={editor}
      tippyOptions={{ duration: 100 }}
      className="flex items-center gap-1 p-2 bg-white border border-gray-200 rounded-lg shadow-lg"
    >
      <button
        onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
        className="p-2 rounded hover:bg-gray-100 text-gray-700"
        title="一级标题"
      >
        <Heading1 size={16} />
      </button>
      
      <button
        onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
        className="p-2 rounded hover:bg-gray-100 text-gray-700"
        title="二级标题"
      >
        <Heading2 size={16} />
      </button>
      
      <button
        onClick={() => editor.chain().focus().toggleBulletList().run()}
        className="p-2 rounded hover:bg-gray-100 text-gray-700"
        title="无序列表"
      >
        <List size={16} />
      </button>
      
      <button
        onClick={() => editor.chain().focus().toggleBlockquote().run()}
        className="p-2 rounded hover:bg-gray-100 text-gray-700"
        title="引用"
      >
        <Quote size={16} />
      </button>
      
      <button
        onClick={insertImage}
        className="p-2 rounded hover:bg-gray-100 text-gray-700"
        title="插入图片"
      >
        <Image size={16} />
      </button>
    </FloatingMenu>
  )
}

export default CustomFloatingMenu
```

### 4. 斜杠命令菜单

```jsx
// components/SlashCommand.jsx
import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react'
import { 
  Heading1, 
  Heading2, 
  Heading3, 
  List, 
  ListOrdered, 
  Quote, 
  Code, 
  Image, 
  Table 
} from 'lucide-react'

const SlashCommand = forwardRef((props, ref) => {
  const [selectedIndex, setSelectedIndex] = useState(0)

  const items = [
    {
      title: '一级标题',
      description: '大标题',
      icon: <Heading1 size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .setNode('heading', { level: 1 })
          .run()
      },
    },
    {
      title: '二级标题',
      description: '中等标题',
      icon: <Heading2 size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .setNode('heading', { level: 2 })
          .run()
      },
    },
    {
      title: '三级标题',
      description: '小标题',
      icon: <Heading3 size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .setNode('heading', { level: 3 })
          .run()
      },
    },
    {
      title: '无序列表',
      description: '创建一个简单的无序列表',
      icon: <List size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .toggleBulletList()
          .run()
      },
    },
    {
      title: '有序列表',
      description: '创建一个有序列表',
      icon: <ListOrdered size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .toggleOrderedList()
          .run()
      },
    },
    {
      title: '引用',
      description: '创建一个引用块',
      icon: <Quote size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .toggleBlockquote()
          .run()
      },
    },
    {
      title: '代码块',
      description: '创建一个代码块',
      icon: <Code size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .toggleCodeBlock()
          .run()
      },
    },
    {
      title: '图片',
      description: '插入图片',
      icon: <Image size={16} />,
      command: ({ editor, range }) => {
        const url = window.prompt('请输入图片地址:')
        if (url) {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setImage({ src: url })
            .run()
        }
      },
    },
    {
      title: '表格',
      description: '插入表格',
      icon: <Table size={16} />,
      command: ({ editor, range }) => {
        editor
          .chain()
          .focus()
          .deleteRange(range)
          .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
          .run()
      },
    },
  ]

  const selectItem = (index) => {
    const item = items[index]
    if (item) {
      item.command(props)
    }
  }

  const upHandler = () => {
    setSelectedIndex((selectedIndex + items.length - 1) % items.length)
  }

  const downHandler = () => {
    setSelectedIndex((selectedIndex + 1) % items.length)
  }

  const enterHandler = () => {
    selectItem(selectedIndex)
  }

  useEffect(() => setSelectedIndex(0), [items])

  useImperativeHandle(ref, () => ({
    onKeyDown: ({ event }) => {
      if (event.key === 'ArrowUp') {
        upHandler()
        return true
      }

      if (event.key === 'ArrowDown') {
        downHandler()
        return true
      }

      if (event.key === 'Enter') {
        enterHandler()
        return true
      }

      return false
    },
  }))

  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-lg p-2 max-w-sm">
      {items.map((item, index) => (
        <button
          key={index}
          className={`w-full flex items-center gap-3 p-2 rounded text-left hover:bg-gray-100 ${
            index === selectedIndex ? 'bg-gray-100' : ''
          }`}
          onClick={() => selectItem(index)}
        >
          <div className="flex-shrink-0 text-gray-500">
            {item.icon}
          </div>
          <div className="flex-1 min-w-0">
            <div className="font-medium text-gray-900">{item.title}</div>
            <div className="text-sm text-gray-500 truncate">{item.description}</div>
          </div>
        </button>
      ))}
    </div>
  )
})

SlashCommand.displayName = 'SlashCommand'

export default SlashCommand
```

## 响应式设计

### 移动端适配

```css
/* 移动端工具栏样式 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 8px;
  }
  
  .toolbar-row {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    justify-content: center;
  }
  
  .toolbar-button {
    min-width: 44px;
    min-height: 44px;
    padding: 12px;
  }
  
  .editor-content {
    padding: 12px;
    font-size: 16px; /* 防止 iOS 缩放 */
  }
}

/* 平板端适配 */
@media (min-width: 769px) and (max-width: 1024px) {
  .toolbar {
    padding: 8px;
  }
  
  .toolbar-button {
    padding: 10px;
  }
}
```

### 暗色主题支持

```css
/* 暗色主题样式 */
@media (prefers-color-scheme: dark) {
  .tiptap {
    background-color: #1f2937;
    color: #f9fafb;
    border-color: #374151;
  }
  
  .tiptap:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  }
  
  .toolbar {
    background-color: #111827;
    border-color: #374151;
  }
  
  .toolbar-button {
    background-color: #374151;
    color: #f9fafb;
    border-color: #4b5563;
  }
  
  .toolbar-button:hover {
    background-color: #4b5563;
  }
  
  .toolbar-button.active {
    background-color: #3b82f6;
    border-color: #3b82f6;
  }
  
  .tiptap h1,
  .tiptap h2,
  .tiptap h3 {
    color: #f9fafb;
  }
  
  .tiptap blockquote {
    background-color: #374151;
    color: #d1d5db;
    border-left-color: #3b82f6;
  }
  
  .tiptap code {
    background-color: #374151;
    color: #fbbf24;
  }
  
  .tiptap pre {
    background-color: #111827;
    color: #f9fafb;
  }
}
```

## 可访问性（Accessibility）

### 键盘导航支持

```javascript
// 为工具栏按钮添加键盘导航
const ToolbarButton = ({ onClick, isActive, children, shortcut }) => {
  const handleKeyDown = (event) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      onClick()
    }
  }

  return (
    <button
      onClick={onClick}
      onKeyDown={handleKeyDown}
      className={`toolbar-button ${isActive ? 'active' : ''}`}
      aria-pressed={isActive}
      aria-label={`${children}${shortcut ? ` (${shortcut})` : ''}`}
      tabIndex={0}
    >
      {children}
    </button>
  )
}
```

### ARIA 属性支持

```jsx
const TiptapEditor = () => {
  return (
    <div className="editor-container">
      <div 
        className="toolbar" 
        role="toolbar" 
        aria-label="文本格式化工具栏"
      >
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleBold().run()}
          isActive={editor.isActive('bold')}
          aria-label="粗体"
          shortcut="Ctrl+B"
        >
          粗体
        </ToolbarButton>
        
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleItalic().run()}
          isActive={editor.isActive('italic')}
          aria-label="斜体"
          shortcut="Ctrl+I"
        >
          斜体
        </ToolbarButton>
      </div>
      
      <EditorContent 
        editor={editor} 
        className="editor-content"
        role="textbox"
        aria-multiline="true"
        aria-label="富文本编辑器"
      />
    </div>
  )
}
```

## 总结

Tiptap 的样式定制和菜单开发具有高度的灵活性：

1. **样式定制**：支持原生 CSS、CSS Modules、Tailwind CSS 等多种方式
2. **菜单类型**：提供固定工具栏、气泡菜单、浮动菜单、斜杠命令等多种交互方式
3. **响应式设计**：支持移动端适配和暗色主题
4. **可访问性**：提供完整的键盘导航和 ARIA 支持
5. **自定义扩展**：可以通过扩展系统添加自定义样式和功能

通过合理的样式设计和菜单布局，可以创建出既美观又易用的富文本编辑器。

---

*下一步：查看 [数据持久化指南](./persistence-guide.md)*