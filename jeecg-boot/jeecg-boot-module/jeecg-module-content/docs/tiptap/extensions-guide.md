# Tiptap 扩展系统指南

## 概述

Tiptap 采用模块化的扩展系统，所有功能都通过扩展来实现。扩展分为三种类型：Nodes（节点）、Marks（标记）和 Extensions（功能扩展）。本指南将详细介绍如何使用内置扩展以及如何开发自定义扩展。

## 扩展类型

### 1. Nodes（节点扩展）

Nodes 是块级元素，如段落、标题、列表等。每个 Node 在文档中占据独立的位置。

**常用 Node 扩展：**
- `Paragraph` - 段落
- `Heading` - 标题
- `Blockquote` - 引用块
- `CodeBlock` - 代码块
- `Image` - 图片
- `Table` - 表格
- `TaskList` - 任务列表

### 2. Marks（标记扩展）

Marks 是行内样式，如粗体、斜体、链接等。可以应用到文本的任意部分。

**常用 Mark 扩展：**
- `Bold` - 粗体
- `Italic` - 斜体
- `Strike` - 删除线
- `Underline` - 下划线
- `Link` - 链接
- `Code` - 行内代码
- `Highlight` - 高亮

### 3. Extensions（功能扩展）

Extensions 提供额外的功能，如历史记录、字符统计、占位符等。

**常用功能扩展：**
- `History` - 撤销/重做
- `CharacterCount` - 字符统计
- `Placeholder` - 占位符
- `Focus` - 焦点样式
- `Typography` - 智能排版

## 内置扩展使用

### 1. StarterKit 快速开始

StarterKit 包含了最常用的扩展，适合快速开始项目。

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

**StarterKit 包含的扩展：**
- `Blockquote`
- `Bold`
- `BulletList`
- `Code`
- `CodeBlock`
- `Document`
- `Dropcursor`
- `Gapcursor`
- `HardBreak`
- `Heading`
- `History`
- `HorizontalRule`
- `Italic`
- `ListItem`
- `OrderedList`
- `Paragraph`
- `Strike`
- `Text`

### 2. 单独使用扩展

```javascript
import { Editor } from '@tiptap/core'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Text from '@tiptap/extension-text'
import Bold from '@tiptap/extension-bold'
import Italic from '@tiptap/extension-italic'

const editor = new Editor({
  element: document.querySelector('.element'),
  extensions: [
    Document,
    Paragraph,
    Text,
    Bold,
    Italic,
  ],
  content: '<p>Hello <strong>World</strong>! 🌎️</p>',
})
```

### 3. 扩展配置

大多数扩展都支持配置选项：

```javascript
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import Heading from '@tiptap/extension-heading'
import Link from '@tiptap/extension-link'
import Image from '@tiptap/extension-image'

const editor = new Editor({
  element: document.querySelector('.element'),
  extensions: [
    StarterKit.configure({
      // 禁用 StarterKit 中的 Heading
      heading: false,
    }),
    // 自定义配置 Heading
    Heading.configure({
      levels: [1, 2, 3], // 只允许 h1, h2, h3
    }),
    // 配置 Link 扩展
    Link.configure({
      openOnClick: false,
      HTMLAttributes: {
        class: 'custom-link',
      },
    }),
    // 配置 Image 扩展
    Image.configure({
      inline: true,
      allowBase64: true,
    }),
  ],
})
```

## 常用扩展详解

### 1. 文本格式化扩展

#### Bold（粗体）

```javascript
import Bold from '@tiptap/extension-bold'

// 基本使用
Bold

// 自定义配置
Bold.configure({
  HTMLAttributes: {
    class: 'my-bold-class',
  },
})

// 使用命令
editor.chain().focus().toggleBold().run()

// 检查状态
editor.isActive('bold')
```

#### Link（链接）

```javascript
import Link from '@tiptap/extension-link'

Link.configure({
  openOnClick: false,
  autolink: true,
  defaultProtocol: 'https',
  protocols: ['http', 'https', 'ftp', 'mailto'],
  HTMLAttributes: {
    class: 'custom-link',
    rel: 'noopener noreferrer',
    target: '_blank',
  },
})

// 设置链接
editor.chain().focus().setLink({ href: 'https://example.com' }).run()

// 取消链接
editor.chain().focus().unsetLink().run()

// 检查是否为链接
editor.isActive('link')
```

#### Highlight（高亮）

```javascript
import Highlight from '@tiptap/extension-highlight'

Highlight.configure({
  multicolor: true,
})

// 设置高亮
editor.chain().focus().toggleHighlight().run()

// 设置特定颜色高亮
editor.chain().focus().toggleHighlight({ color: '#ffc078' }).run()

// 取消高亮
editor.chain().focus().unsetHighlight().run()
```

### 2. 块级元素扩展

#### Heading（标题）

```javascript
import Heading from '@tiptap/extension-heading'

Heading.configure({
  levels: [1, 2, 3, 4, 5, 6],
  HTMLAttributes: {
    class: 'custom-heading',
  },
})

// 设置标题级别
editor.chain().focus().toggleHeading({ level: 1 }).run()
editor.chain().focus().toggleHeading({ level: 2 }).run()

// 检查标题级别
editor.isActive('heading', { level: 1 })
```

#### CodeBlock（代码块）

```javascript
import CodeBlock from '@tiptap/extension-code-block'

CodeBlock.configure({
  languageClassPrefix: 'language-',
  exitOnTripleEnter: true,
  exitOnArrowDown: true,
  HTMLAttributes: {
    class: 'my-code-block',
  },
})

// 切换代码块
editor.chain().focus().toggleCodeBlock().run()

// 设置代码块语言
editor.chain().focus().setCodeBlock({ language: 'javascript' }).run()
```

#### Table（表格）

```javascript
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableHeader from '@tiptap/extension-table-header'
import TableCell from '@tiptap/extension-table-cell'

const editor = new Editor({
  extensions: [
    // ... 其他扩展
    Table.configure({
      resizable: true,
    }),
    TableRow,
    TableHeader,
    TableCell,
  ],
})

// 插入表格
editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()

// 添加行/列
editor.chain().focus().addRowBefore().run()
editor.chain().focus().addRowAfter().run()
editor.chain().focus().addColumnBefore().run()
editor.chain().focus().addColumnAfter().run()

// 删除行/列
editor.chain().focus().deleteRow().run()
editor.chain().focus().deleteColumn().run()

// 删除表格
editor.chain().focus().deleteTable().run()
```

### 3. 功能性扩展

#### CharacterCount（字符统计）

```javascript
import CharacterCount from '@tiptap/extension-character-count'

CharacterCount.configure({
  limit: 1000,
})

// 获取字符数
const characterCount = editor.storage.characterCount.characters()
const wordCount = editor.storage.characterCount.words()
const limit = editor.storage.characterCount.limit()

// 检查是否超出限制
const percentage = editor.storage.characterCount.percentage()
const isLimitExceeded = percentage === 100
```

#### Placeholder（占位符）

```javascript
import Placeholder from '@tiptap/extension-placeholder'

Placeholder.configure({
  placeholder: '请输入内容...',
  // 或者根据节点类型设置不同占位符
  placeholder: ({ node }) => {
    if (node.type.name === 'heading') {
      return '请输入标题...'
    }
    return '请输入内容...'
  },
  emptyEditorClass: 'is-editor-empty',
  emptyNodeClass: 'is-empty',
  showOnlyWhenEditable: true,
  showOnlyCurrent: true,
})
```

#### Focus（焦点样式）

```javascript
import Focus from '@tiptap/extension-focus'

Focus.configure({
  className: 'has-focus',
  mode: 'all', // 'all', 'deepest', 'shallowest'
})
```

## 自定义扩展开发

### 1. 创建自定义 Mark 扩展

```javascript
// extensions/CustomHighlight.js
import { Mark, mergeAttributes } from '@tiptap/core'

/**
 * 自定义高亮扩展
 */
export const CustomHighlight = Mark.create({
  name: 'customHighlight',

  // 扩展配置选项
  addOptions() {
    return {
      multicolor: false,
      HTMLAttributes: {},
    }
  },

  // 定义属性
  addAttributes() {
    if (!this.options.multicolor) {
      return {}
    }

    return {
      color: {
        default: null,
        parseHTML: element => element.getAttribute('data-color') || element.style.backgroundColor,
        renderHTML: attributes => {
          if (!attributes.color) {
            return {}
          }

          return {
            'data-color': attributes.color,
            style: `background-color: ${attributes.color}; color: inherit`,
          }
        },
      },
    }
  },

  // 解析 HTML
  parseHTML() {
    return [
      {
        tag: 'mark',
      },
    ]
  },

  // 渲染 HTML
  renderHTML({ HTMLAttributes }) {
    return ['mark', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0]
  },

  // 添加命令
  addCommands() {
    return {
      setHighlight: attributes => ({ commands }) => {
        return commands.setMark(this.name, attributes)
      },
      toggleHighlight: attributes => ({ commands }) => {
        return commands.toggleMark(this.name, attributes)
      },
      unsetHighlight: () => ({ commands }) => {
        return commands.unsetMark(this.name)
      },
    }
  },

  // 添加键盘快捷键
  addKeyboardShortcuts() {
    return {
      'Mod-Shift-h': () => this.editor.commands.toggleHighlight(),
    }
  },
})
```

### 2. 创建自定义 Node 扩展

```javascript
// extensions/CustomCallout.js
import { Node, mergeAttributes } from '@tiptap/core'

/**
 * 自定义提示框扩展
 */
export const CustomCallout = Node.create({
  name: 'customCallout',

  // 扩展配置
  addOptions() {
    return {
      types: ['info', 'warning', 'error', 'success'],
      HTMLAttributes: {},
    }
  },

  // 定义为块级元素
  group: 'block',

  // 可以包含其他块级元素
  content: 'block+',

  // 定义属性
  addAttributes() {
    return {
      type: {
        default: 'info',
        parseHTML: element => element.getAttribute('data-type'),
        renderHTML: attributes => {
          return {
            'data-type': attributes.type,
          }
        },
      },
      title: {
        default: null,
        parseHTML: element => element.getAttribute('data-title'),
        renderHTML: attributes => {
          if (!attributes.title) {
            return {}
          }

          return {
            'data-title': attributes.title,
          }
        },
      },
    }
  },

  // 解析 HTML
  parseHTML() {
    return [
      {
        tag: 'div[data-type="callout"]',
      },
    ]
  },

  // 渲染 HTML
  renderHTML({ HTMLAttributes }) {
    return [
      'div',
      mergeAttributes(
        {
          'data-type': 'callout',
          class: `callout callout-${HTMLAttributes['data-type'] || 'info'}`,
        },
        this.options.HTMLAttributes,
        HTMLAttributes
      ),
      [
        'div',
        { class: 'callout-title' },
        HTMLAttributes['data-title'] || this.getTypeTitle(HTMLAttributes['data-type'])
      ],
      ['div', { class: 'callout-content' }, 0],
    ]
  },

  // 添加命令
  addCommands() {
    return {
      setCallout: attributes => ({ commands }) => {
        return commands.setNode(this.name, attributes)
      },
      toggleCallout: attributes => ({ commands }) => {
        return commands.toggleNode(this.name, 'paragraph', attributes)
      },
    }
  },

  // 添加键盘快捷键
  addKeyboardShortcuts() {
    return {
      'Mod-Shift-c': () => this.editor.commands.toggleCallout({ type: 'info' }),
    }
  },

  // 辅助方法
  getTypeTitle(type) {
    const titles = {
      info: '信息',
      warning: '警告',
      error: '错误',
      success: '成功',
    }
    return titles[type] || '提示'
  },
})
```

### 3. 创建自定义功能扩展

```javascript
// extensions/WordCount.js
import { Extension } from '@tiptap/core'
import { Plugin, PluginKey } from '@tiptap/pm/state'

/**
 * 字数统计扩展
 */
export const WordCount = Extension.create({
  name: 'wordCount',

  // 扩展配置
  addOptions() {
    return {
      limit: null,
      mode: 'textSize', // 'textSize' | 'nodeSize'
    }
  },

  // 添加存储
  addStorage() {
    return {
      characters: () => 0,
      words: () => 0,
    }
  },

  // 添加 ProseMirror 插件
  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey('wordCount'),
        state: {
          init: (_, { doc }) => {
            return this.getWordCount(doc)
          },
          apply: (transaction, value, oldState, newState) => {
            const oldNodeSize = oldState.doc.nodeSize
            const newNodeSize = newState.doc.nodeSize

            if (transaction.docChanged && (oldNodeSize !== newNodeSize)) {
              return this.getWordCount(newState.doc)
            }

            return value
          },
        },
        view: () => ({
          update: (view, prevState) => {
            const pluginState = this.getPluginState(view.state)
            
            if (pluginState) {
              this.storage.characters = () => pluginState.characters
              this.storage.words = () => pluginState.words
            }
          },
        }),
      }),
    ]
  },

  // 获取插件状态
  getPluginState(state) {
    return this.getPlugin(state)?.getState(state)
  },

  // 获取字数统计
  getWordCount(doc) {
    const text = doc.textContent || ''
    const characters = text.length
    const words = text.split(/\s+/).filter(word => word.length > 0).length

    return {
      characters,
      words,
    }
  },
})
```

### 4. 使用自定义扩展

```javascript
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import { CustomHighlight } from './extensions/CustomHighlight'
import { CustomCallout } from './extensions/CustomCallout'
import { WordCount } from './extensions/WordCount'

const editor = new Editor({
  element: document.querySelector('.element'),
  extensions: [
    StarterKit,
    CustomHighlight.configure({
      multicolor: true,
    }),
    CustomCallout.configure({
      types: ['info', 'warning', 'error', 'success'],
    }),
    WordCount.configure({
      limit: 1000,
    }),
  ],
  content: `
    <p>这是一个示例文档。</p>
    <div data-type="callout" data-type="info" data-title="提示">
      <div class="callout-content">
        <p>这是一个信息提示框。</p>
      </div>
    </div>
  `,
})

// 使用自定义命令
editor.chain().focus().toggleHighlight({ color: '#ffc078' }).run()
editor.chain().focus().setCallout({ type: 'warning', title: '注意' }).run()

// 获取字数统计
const characters = editor.storage.wordCount.characters()
const words = editor.storage.wordCount.words()
```

## 扩展组合和配置

### 1. 创建扩展包

```javascript
// extensions/MyExtensionKit.js
import { Extension } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import Link from '@tiptap/extension-link'
import Image from '@tiptap/extension-image'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableHeader from '@tiptap/extension-table-header'
import TableCell from '@tiptap/extension-table-cell'
import { CustomHighlight } from './CustomHighlight'
import { CustomCallout } from './CustomCallout'

/**
 * 自定义扩展包
 */
export const MyExtensionKit = Extension.create({
  name: 'myExtensionKit',

  addExtensions() {
    return [
      StarterKit.configure({
        // 禁用一些不需要的扩展
        strike: false,
        code: false,
      }),
      Link.configure({
        openOnClick: false,
        autolink: true,
      }),
      Image.configure({
        inline: true,
        allowBase64: true,
      }),
      Table.configure({
        resizable: true,
      }),
      TableRow,
      TableHeader,
      TableCell,
      CustomHighlight.configure({
        multicolor: true,
      }),
      CustomCallout,
    ]
  },
})

// 使用扩展包
const editor = new Editor({
  element: document.querySelector('.element'),
  extensions: [
    MyExtensionKit,
  ],
})
```

### 2. 条件加载扩展

```javascript
// utils/extensionLoader.js

/**
 * 扩展加载器
 */
class ExtensionLoader {
  constructor() {
    this.extensions = []
  }

  /**
   * 添加基础扩展
   */
  addBasic() {
    this.extensions.push(
      StarterKit.configure({
        history: false, // 我们会单独配置
      })
    )
    return this
  }

  /**
   * 添加历史记录扩展
   */
  addHistory(options = {}) {
    this.extensions.push(
      History.configure({
        depth: 100,
        newGroupDelay: 500,
        ...options,
      })
    )
    return this
  }

  /**
   * 添加表格支持
   */
  addTable() {
    this.extensions.push(
      Table.configure({ resizable: true }),
      TableRow,
      TableHeader,
      TableCell
    )
    return this
  }

  /**
   * 添加协作支持
   */
  addCollaboration(options) {
    if (options.enabled) {
      this.extensions.push(
        Collaboration.configure({
          document: options.document,
        }),
        CollaborationCursor.configure({
          provider: options.provider,
          user: options.user,
        })
      )
    }
    return this
  }

  /**
   * 添加自定义扩展
   */
  addCustom(extensions) {
    this.extensions.push(...extensions)
    return this
  }

  /**
   * 获取所有扩展
   */
  getExtensions() {
    return this.extensions
  }
}

// 使用扩展加载器
const loader = new ExtensionLoader()

const extensions = loader
  .addBasic()
  .addHistory({ depth: 50 })
  .addTable()
  .addCollaboration({
    enabled: true,
    document: ydoc,
    provider: provider,
    user: { name: 'John Doe', color: '#f783ac' },
  })
  .addCustom([CustomHighlight, CustomCallout])
  .getExtensions()

const editor = new Editor({
  element: document.querySelector('.element'),
  extensions,
})
```

## 扩展开发最佳实践

### 1. 扩展结构规范

```javascript
// 标准扩展结构
export const MyExtension = Extension.create({
  // 扩展名称（必需）
  name: 'myExtension',

  // 扩展类型（可选）
  type: 'extension', // 'node' | 'mark' | 'extension'

  // 默认选项（可选）
  addOptions() {
    return {
      // 默认配置
    }
  },

  // 全局属性（可选）
  addGlobalAttributes() {
    return [
      {
        types: ['paragraph', 'heading'],
        attributes: {
          // 属性定义
        },
      },
    ]
  },

  // 命令（可选）
  addCommands() {
    return {
      // 命令定义
    }
  },

  // 键盘快捷键（可选）
  addKeyboardShortcuts() {
    return {
      // 快捷键定义
    }
  },

  // 输入规则（可选）
  addInputRules() {
    return [
      // 输入规则
    ]
  },

  // 粘贴规则（可选）
  addPasteRules() {
    return [
      // 粘贴规则
    ]
  },

  // ProseMirror 插件（可选）
  addProseMirrorPlugins() {
    return [
      // 插件定义
    ]
  },

  // 存储（可选）
  addStorage() {
    return {
      // 存储定义
    }
  },

  // 生命周期钩子
  onCreate() {
    // 扩展创建时
  },

  onUpdate() {
    // 编辑器更新时
  },

  onSelectionUpdate() {
    // 选择更新时
  },

  onTransaction() {
    // 事务执行时
  },

  onFocus() {
    // 获得焦点时
  },

  onBlur() {
    // 失去焦点时
  },

  onDestroy() {
    // 扩展销毁时
  },
})
```

### 2. 类型定义

```typescript
// types/extensions.ts
import { Extension } from '@tiptap/core'

// 扩展选项接口
interface MyExtensionOptions {
  enabled: boolean
  className: string
  customAttribute: string
}

// 扩展存储接口
interface MyExtensionStorage {
  count: number
  data: any[]
}

// 扩展命令接口
declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    myExtension: {
      /**
       * 设置自定义属性
       */
      setMyAttribute: (attributes: { value: string }) => ReturnType
      /**
       * 切换扩展状态
       */
      toggleMyExtension: () => ReturnType
    }
  }
}

// 扩展定义
export const MyExtension = Extension.create<MyExtensionOptions, MyExtensionStorage>({
  name: 'myExtension',

  addOptions() {
    return {
      enabled: true,
      className: 'my-extension',
      customAttribute: 'default',
    }
  },

  addStorage() {
    return {
      count: 0,
      data: [],
    }
  },

  addCommands() {
    return {
      setMyAttribute: (attributes) => ({ commands }) => {
        // 命令实现
        return true
      },
      toggleMyExtension: () => ({ commands }) => {
        // 命令实现
        return true
      },
    }
  },
})
```

### 3. 测试扩展

```javascript
// tests/MyExtension.test.js
import { Editor } from '@tiptap/core'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Text from '@tiptap/extension-text'
import { MyExtension } from '../src/extensions/MyExtension'

describe('MyExtension', () => {
  let editor

  beforeEach(() => {
    editor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        MyExtension,
      ],
    })
  })

  afterEach(() => {
    editor.destroy()
  })

  it('should be defined', () => {
    expect(MyExtension).toBeDefined()
  })

  it('should have correct name', () => {
    expect(MyExtension.name).toBe('myExtension')
  })

  it('should register commands', () => {
    expect(editor.commands.setMyAttribute).toBeDefined()
    expect(editor.commands.toggleMyExtension).toBeDefined()
  })

  it('should execute commands correctly', () => {
    const result = editor.commands.setMyAttribute({ value: 'test' })
    expect(result).toBe(true)
  })

  it('should have correct storage', () => {
    expect(editor.storage.myExtension.count).toBe(0)
    expect(editor.storage.myExtension.data).toEqual([])
  })

  it('should handle options correctly', () => {
    const customEditor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        MyExtension.configure({
          enabled: false,
          className: 'custom-class',
        }),
      ],
    })

    const extension = customEditor.extensionManager.extensions.find(
      ext => ext.name === 'myExtension'
    )

    expect(extension.options.enabled).toBe(false)
    expect(extension.options.className).toBe('custom-class')

    customEditor.destroy()
  })
})
```

## 扩展发布和分享

### 1. 创建 NPM 包

```json
// package.json
{
  "name": "@mycompany/tiptap-extension-custom",
  "version": "1.0.0",
  "description": "Custom Tiptap extension",
  "main": "dist/index.js",
  "module": "dist/index.esm.js",
  "types": "dist/index.d.ts",
  "files": [
    "dist"
  ],
  "scripts": {
    "build": "rollup -c",
    "dev": "rollup -c -w",
    "test": "jest",
    "lint": "eslint src",
    "prepublishOnly": "npm run build"
  },
  "peerDependencies": {
    "@tiptap/core": "^2.0.0",
    "@tiptap/pm": "^2.0.0"
  },
  "devDependencies": {
    "@tiptap/core": "^2.0.0",
    "@tiptap/pm": "^2.0.0",
    "rollup": "^3.0.0",
    "typescript": "^4.0.0",
    "jest": "^29.0.0"
  },
  "keywords": [
    "tiptap",
    "extension",
    "editor",
    "wysiwyg"
  ],
  "author": "Your Name",
  "license": "MIT"
}
```

### 2. 构建配置

```javascript
// rollup.config.js
import typescript from '@rollup/plugin-typescript'
import { nodeResolve } from '@rollup/plugin-node-resolve'
import commonjs from '@rollup/plugin-commonjs'

export default {
  input: 'src/index.ts',
  output: [
    {
      file: 'dist/index.js',
      format: 'cjs',
      sourcemap: true,
    },
    {
      file: 'dist/index.esm.js',
      format: 'esm',
      sourcemap: true,
    },
  ],
  external: ['@tiptap/core', '@tiptap/pm'],
  plugins: [
    nodeResolve(),
    commonjs(),
    typescript({
      tsconfig: './tsconfig.json',
    }),
  ],
}
```

### 3. 文档示例

```markdown
# @mycompany/tiptap-extension-custom

自定义 Tiptap 扩展，提供 XXX 功能。

## 安装

```bash
npm install @mycompany/tiptap-extension-custom
```

## 使用

```javascript
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import { CustomExtension } from '@mycompany/tiptap-extension-custom'

const editor = new Editor({
  element: document.querySelector('.element'),
  extensions: [
    StarterKit,
    CustomExtension.configure({
      // 配置选项
    }),
  ],
})
```

## 配置选项

| 选项 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| enabled | boolean | true | 是否启用扩展 |
| className | string | 'custom' | CSS 类名 |

## 命令

- `setCustomAttribute(attributes)` - 设置自定义属性
- `toggleCustom()` - 切换扩展状态

## 键盘快捷键

- `Mod-Shift-X` - 切换扩展状态

## 许可证

MIT
```

## 总结

Tiptap 的扩展系统具有以下特点：

1. **模块化设计**：所有功能都通过扩展实现
2. **类型安全**：完整的 TypeScript 支持
3. **高度可配置**：每个扩展都支持自定义配置
4. **易于扩展**：提供完整的 API 用于开发自定义扩展
5. **丰富的生态**：大量内置扩展和社区扩展
6. **性能优化**：基于 ProseMirror 的高性能架构

通过合理使用和组合扩展，可以构建出功能强大且性能优异的富文本编辑器。

---

*下一步：查看 [最佳实践指南](./best-practices.md)*