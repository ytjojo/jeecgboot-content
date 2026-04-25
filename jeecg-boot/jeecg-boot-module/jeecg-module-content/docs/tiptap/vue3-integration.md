# Tiptap Vue 3 集成指南

## 概述

Tiptap 为 Vue 3 提供了专门的集成包 `@tiptap/vue-3`，完美支持 Vue 3 的 Composition API 和 `<script setup>` 语法。本指南将详细介绍如何在 Vue 3 项目中集成和使用 Tiptap 编辑器。

## 环境要求

- Node.js 16.0 或更高版本
- Vue 3.0 或更高版本
- Vue CLI 4.5+ 或 Vite 2.0+
- npm、yarn 或 pnpm 包管理器

## 安装依赖

### 基础安装

```bash
# 使用 npm
npm install @tiptap/vue-3 @tiptap/pm @tiptap/starter-kit

# 使用 yarn
yarn add @tiptap/vue-3 @tiptap/pm @tiptap/starter-kit

# 使用 pnpm
pnpm add @tiptap/vue-3 @tiptap/pm @tiptap/starter-kit
```

### 依赖说明

- `@tiptap/vue-3`：Vue 3 集成包，提供 Vue 组件和 Composition API
- `@tiptap/pm`：ProseMirror 核心依赖
- `@tiptap/starter-kit`：基础扩展包，包含常用功能

### 可选扩展

```bash
# 额外的扩展包
npm install @tiptap/extension-table @tiptap/extension-image @tiptap/extension-link
npm install @tiptap/extension-character-count @tiptap/extension-placeholder
```

## 基本使用

### 1. 使用 Composition API

```vue
<!-- components/TiptapEditor.vue -->
<template>
  <div class="tiptap-editor">
    <!-- 工具栏 -->
    <div class="toolbar" v-if="editor">
      <button
        @click="editor.chain().focus().toggleBold().run()"
        :class="{ 'is-active': editor.isActive('bold') }"
      >
        粗体
      </button>
      <button
        @click="editor.chain().focus().toggleItalic().run()"
        :class="{ 'is-active': editor.isActive('italic') }"
      >
        斜体
      </button>
      <button
        @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
        :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"
      >
        H1
      </button>
      <button
        @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
        :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"
      >
        H2
      </button>
      <button
        @click="editor.chain().focus().toggleBulletList().run()"
        :class="{ 'is-active': editor.isActive('bulletList') }"
      >
        无序列表
      </button>
      <button
        @click="editor.chain().focus().toggleOrderedList().run()"
        :class="{ 'is-active': editor.isActive('orderedList') }"
      >
        有序列表
      </button>
      <button
        @click="editor.chain().focus().toggleBlockquote().run()"
        :class="{ 'is-active': editor.isActive('blockquote') }"
      >
        引用
      </button>
      <button @click="editor.chain().focus().undo().run()">
        撤销
      </button>
      <button @click="editor.chain().focus().redo().run()">
        重做
      </button>
    </div>
    
    <!-- 编辑器内容区域 -->
    <EditorContent :editor="editor" class="editor-content" />
    
    <!-- 状态栏 -->
    <div class="status-bar" v-if="editor">
      <span>字符数: {{ characterCount }}</span>
      <span>单词数: {{ wordCount }}</span>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import CharacterCount from '@tiptap/extension-character-count'
import Placeholder from '@tiptap/extension-placeholder'

export default {
  name: 'TiptapEditor',
  components: {
    EditorContent,
  },
  props: {
    /**
     * 初始内容
     */
    modelValue: {
      type: String,
      default: '',
    },
    /**
     * 占位符文本
     */
    placeholder: {
      type: String,
      default: '开始输入...',
    },
    /**
     * 是否只读
     */
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['update:modelValue', 'focus', 'blur'],
  setup(props, { emit }) {
    const editor = ref(null)

    /**
     * 初始化编辑器
     */
    onMounted(() => {
      editor.value = new Editor({
        extensions: [
          StarterKit,
          CharacterCount,
          Placeholder.configure({
            placeholder: props.placeholder,
          }),
        ],
        content: props.modelValue,
        editable: !props.readonly,
        onUpdate: ({ editor }) => {
          // 内容更新时触发 v-model 更新
          const html = editor.getHTML()
          emit('update:modelValue', html)
        },
        onFocus: () => {
          emit('focus')
        },
        onBlur: () => {
          emit('blur')
        },
        editorProps: {
          attributes: {
            class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto focus:outline-none',
          },
        },
      })
    })

    /**
     * 组件销毁时清理编辑器
     */
    onBeforeUnmount(() => {
      if (editor.value) {
        editor.value.destroy()
      }
    })

    /**
     * 监听 modelValue 变化，同步到编辑器
     */
    watch(
      () => props.modelValue,
      (newValue) => {
        if (editor.value && editor.value.getHTML() !== newValue) {
          editor.value.commands.setContent(newValue)
        }
      }
    )

    /**
     * 监听只读状态变化
     */
    watch(
      () => props.readonly,
      (newValue) => {
        if (editor.value) {
          editor.value.setEditable(!newValue)
        }
      }
    )

    /**
     * 计算字符数
     */
    const characterCount = computed(() => {
      return editor.value?.storage.characterCount?.characters() || 0
    })

    /**
     * 计算单词数
     */
    const wordCount = computed(() => {
      return editor.value?.storage.characterCount?.words() || 0
    })

    return {
      editor,
      characterCount,
      wordCount,
    }
  },
}
</script>

<style scoped>
.tiptap-editor {
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

.toolbar button {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.toolbar button:hover {
  background-color: #f3f4f6;
}

.toolbar button.is-active {
  background-color: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.editor-content {
  padding: 16px;
  min-height: 200px;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  padding: 8px 16px;
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
  font-size: 12px;
  color: #6b7280;
}
</style>
```

### 2. 使用 `<script setup>` 语法

```vue
<!-- components/TiptapEditorSetup.vue -->
<template>
  <div class="tiptap-editor">
    <div class="toolbar" v-if="editor">
      <button
        @click="toggleBold"
        :class="{ 'is-active': editor.isActive('bold') }"
      >
        粗体
      </button>
      <button
        @click="toggleItalic"
        :class="{ 'is-active': editor.isActive('italic') }"
      >
        斜体
      </button>
      <button
        @click="toggleHeading(1)"
        :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"
      >
        H1
      </button>
      <button
        @click="toggleHeading(2)"
        :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"
      >
        H2
      </button>
    </div>
    
    <EditorContent :editor="editor" class="editor-content" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'

// 定义组件属性
const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '开始输入...',
  },
})

// 定义事件
const emit = defineEmits(['update:modelValue'])

// 编辑器实例
const editor = ref(null)

/**
 * 初始化编辑器
 */
onMounted(() => {
  editor.value = new Editor({
    extensions: [StarterKit],
    content: props.modelValue,
    onUpdate: ({ editor }) => {
      emit('update:modelValue', editor.getHTML())
    },
  })
})

/**
 * 清理编辑器
 */
onBeforeUnmount(() => {
  editor.value?.destroy()
})

/**
 * 监听内容变化
 */
watch(
  () => props.modelValue,
  (newValue) => {
    if (editor.value && editor.value.getHTML() !== newValue) {
      editor.value.commands.setContent(newValue)
    }
  }
)

/**
 * 工具栏操作函数
 */
const toggleBold = () => {
  editor.value?.chain().focus().toggleBold().run()
}

const toggleItalic = () => {
  editor.value?.chain().focus().toggleItalic().run()
}

const toggleHeading = (level) => {
  editor.value?.chain().focus().toggleHeading({ level }).run()
}
</script>

<style scoped>
/* 样式同上 */
</style>
```

### 3. 在应用中使用编辑器

```vue
<!-- App.vue -->
<template>
  <div class="app">
    <header class="app-header">
      <h1>Tiptap Vue 3 示例</h1>
      <div class="actions">
        <button @click="saveContent">保存内容</button>
        <button @click="loadContent">加载内容</button>
        <button @click="clearContent">清空内容</button>
        <button @click="toggleReadonly">
          {{ readonly ? '启用编辑' : '只读模式' }}
        </button>
      </div>
    </header>
    
    <main class="main-content">
      <TiptapEditor 
        v-model="content" 
        :placeholder="placeholder"
        :readonly="readonly"
        @focus="onEditorFocus"
        @blur="onEditorBlur"
      />
      
      <div class="content-preview" v-if="content">
        <h3>内容预览：</h3>
        <div class="preview-content" v-html="content"></div>
      </div>
      
      <div class="json-output">
        <h3>JSON 输出：</h3>
        <pre>{{ jsonContent }}</pre>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import TiptapEditor from './components/TiptapEditor.vue'

// 响应式数据
const content = ref('<p>欢迎使用 Tiptap Vue 3 编辑器！</p>')
const readonly = ref(false)
const placeholder = ref('请输入内容...')

// 计算属性
const jsonContent = computed(() => {
  try {
    return JSON.stringify(JSON.parse(content.value), null, 2)
  } catch {
    return '无效的 JSON 内容'
  }
})

/**
 * 保存内容到本地存储
 */
const saveContent = () => {
  localStorage.setItem('tiptap-vue-content', content.value)
  alert('内容已保存！')
}

/**
 * 从本地存储加载内容
 */
const loadContent = () => {
  const saved = localStorage.getItem('tiptap-vue-content')
  if (saved) {
    content.value = saved
    alert('内容已加载！')
  } else {
    alert('没有找到保存的内容')
  }
}

/**
 * 清空内容
 */
const clearContent = () => {
  content.value = ''
}

/**
 * 切换只读模式
 */
const toggleReadonly = () => {
  readonly.value = !readonly.value
}

/**
 * 编辑器获得焦点
 */
const onEditorFocus = () => {
  console.log('编辑器获得焦点')
}

/**
 * 编辑器失去焦点
 */
const onEditorBlur = () => {
  console.log('编辑器失去焦点')
}

/**
 * 组件挂载时加载内容
 */
onMounted(() => {
  const saved = localStorage.getItem('tiptap-vue-content')
  if (saved) {
    content.value = saved
  }
})
</script>

<style scoped>
.app {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.app-header {
  text-align: center;
  margin-bottom: 30px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.actions button {
  padding: 10px 20px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.actions button:hover {
  background-color: #2563eb;
}

.main-content {
  display: grid;
  gap: 30px;
}

.content-preview {
  padding: 20px;
  background-color: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.preview-content {
  margin-top: 10px;
  padding: 15px;
  background-color: white;
  border-radius: 6px;
  border: 1px solid #d1d5db;
}

.json-output {
  padding: 20px;
  background-color: #1f2937;
  color: #f9fafb;
  border-radius: 8px;
}

.json-output pre {
  margin: 10px 0 0 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}
</style>
```

## 高级用法

### 1. 自定义扩展配置

```vue
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableHeader from '@tiptap/extension-table-header'
import TableCell from '@tiptap/extension-table-cell'
import Image from '@tiptap/extension-image'
import Link from '@tiptap/extension-link'

const editor = ref(null)

onMounted(() => {
  editor.value = new Editor({
    extensions: [
      StarterKit,
      // 表格扩展配置
      Table.configure({
        resizable: true,
      }),
      TableRow,
      TableHeader,
      TableCell,
      // 图片扩展配置
      Image.configure({
        inline: true,
        allowBase64: true,
      }),
      // 链接扩展配置
      Link.configure({
        openOnClick: false,
        HTMLAttributes: {
          class: 'custom-link',
        },
      }),
    ],
    content: '<p>Hello World!</p>',
  })
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

/**
 * 插入表格
 */
const insertTable = () => {
  editor.value
    ?.chain()
    .focus()
    .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
    .run()
}

/**
 * 插入图片
 */
const insertImage = () => {
  const url = prompt('请输入图片URL:')
  if (url) {
    editor.value?.chain().focus().setImage({ src: url }).run()
  }
}

/**
 * 设置链接
 */
const setLink = () => {
  const url = prompt('请输入链接URL:')
  if (url) {
    editor.value?.chain().focus().setLink({ href: url }).run()
  }
}
</script>
```

### 2. 使用 Provide/Inject 共享编辑器

```vue
<!-- EditorProvider.vue -->
<template>
  <div>
    <slot :editor="editor" />
  </div>
</template>

<script setup>
import { ref, provide, onMounted, onBeforeUnmount } from 'vue'
import { Editor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'

const editor = ref(null)

onMounted(() => {
  editor.value = new Editor({
    extensions: [StarterKit],
    content: '<p>Hello World!</p>',
  })
  
  // 提供编辑器实例给子组件
  provide('editor', editor)
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>
```

```vue
<!-- EditorToolbar.vue -->
<template>
  <div class="toolbar" v-if="editor">
    <button
      @click="editor.chain().focus().toggleBold().run()"
      :class="{ 'is-active': editor.isActive('bold') }"
    >
      粗体
    </button>
    <!-- 其他工具栏按钮 -->
  </div>
</template>

<script setup>
import { inject } from 'vue'

// 注入编辑器实例
const editor = inject('editor')
</script>
```

### 3. 响应式配置

```vue
<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'

const editor = ref(null)

// 响应式配置
const config = reactive({
  placeholder: '请输入内容...',
  editable: true,
  content: '<p>Hello World!</p>',
})

onMounted(() => {
  editor.value = new Editor({
    extensions: [
      StarterKit,
      Placeholder.configure({
        placeholder: config.placeholder,
      }),
    ],
    content: config.content,
    editable: config.editable,
  })
})

// 监听配置变化
watch(
  () => config.editable,
  (newValue) => {
    editor.value?.setEditable(newValue)
  }
)

watch(
  () => config.content,
  (newValue) => {
    if (editor.value && editor.value.getHTML() !== newValue) {
      editor.value.commands.setContent(newValue)
    }
  }
)

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>
```

## 与 Pinia 状态管理集成

### 1. 创建编辑器状态存储

```javascript
// stores/editor.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useEditorStore = defineStore('editor', () => {
  // 状态
  const content = ref('')
  const isEditing = ref(false)
  const history = ref([])
  const currentHistoryIndex = ref(-1)

  // 计算属性
  const canUndo = computed(() => currentHistoryIndex.value > 0)
  const canRedo = computed(() => currentHistoryIndex.value < history.value.length - 1)
  const wordCount = computed(() => {
    const text = content.value.replace(/<[^>]*>/g, '')
    return text.trim().split(/\s+/).filter(word => word.length > 0).length
  })

  // 操作
  const updateContent = (newContent) => {
    content.value = newContent
    addToHistory(newContent)
  }

  const addToHistory = (content) => {
    // 移除当前位置之后的历史记录
    history.value = history.value.slice(0, currentHistoryIndex.value + 1)
    // 添加新的历史记录
    history.value.push(content)
    currentHistoryIndex.value = history.value.length - 1
    
    // 限制历史记录数量
    if (history.value.length > 50) {
      history.value.shift()
      currentHistoryIndex.value--
    }
  }

  const undo = () => {
    if (canUndo.value) {
      currentHistoryIndex.value--
      content.value = history.value[currentHistoryIndex.value]
    }
  }

  const redo = () => {
    if (canRedo.value) {
      currentHistoryIndex.value++
      content.value = history.value[currentHistoryIndex.value]
    }
  }

  const setEditing = (editing) => {
    isEditing.value = editing
  }

  const clearContent = () => {
    content.value = ''
    history.value = ['']
    currentHistoryIndex.value = 0
  }

  return {
    // 状态
    content,
    isEditing,
    history,
    currentHistoryIndex,
    // 计算属性
    canUndo,
    canRedo,
    wordCount,
    // 操作
    updateContent,
    undo,
    redo,
    setEditing,
    clearContent,
  }
})
```

### 2. 在组件中使用状态存储

```vue
<template>
  <div class="editor-with-store">
    <div class="toolbar">
      <button @click="store.undo()" :disabled="!store.canUndo">
        撤销
      </button>
      <button @click="store.redo()" :disabled="!store.canRedo">
        重做
      </button>
      <span class="word-count">字数: {{ store.wordCount }}</span>
    </div>
    
    <EditorContent :editor="editor" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import { useEditorStore } from '@/stores/editor'

const store = useEditorStore()
const editor = ref(null)

onMounted(() => {
  editor.value = new Editor({
    extensions: [StarterKit],
    content: store.content,
    onUpdate: ({ editor }) => {
      store.updateContent(editor.getHTML())
    },
    onFocus: () => {
      store.setEditing(true)
    },
    onBlur: () => {
      store.setEditing(false)
    },
  })
})

// 监听存储中的内容变化
watch(
  () => store.content,
  (newContent) => {
    if (editor.value && editor.value.getHTML() !== newContent) {
      editor.value.commands.setContent(newContent)
    }
  }
)

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>
```

## 常见问题

### 1. 编辑器在 v-if 中不显示

**问题**：编辑器在条件渲染中不显示内容

**解决方案**：
```vue
<template>
  <!-- 错误的做法 -->
  <div v-if="showEditor">
    <EditorContent :editor="editor" />
  </div>
  
  <!-- 正确的做法 -->
  <div>
    <EditorContent v-if="showEditor" :editor="editor" />
  </div>
</template>
```

### 2. 双向绑定不生效

**问题**：v-model 双向绑定不工作

**解决方案**：
```vue
<script setup>
// 确保正确定义 props 和 emits
const props = defineProps({
  modelValue: String,
})

const emit = defineEmits(['update:modelValue'])

// 在 onUpdate 中正确触发事件
const editor = new Editor({
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
  },
})
</script>
```

### 3. 样式不生效

**问题**：编辑器样式显示异常

**解决方案**：
```vue
<style>
/* 确保样式不是 scoped 的，或者使用 :deep() */
.ProseMirror {
  outline: none;
}

/* 或者使用 :deep() */
:deep(.ProseMirror) {
  outline: none;
}
</style>
```

## 总结

Tiptap 与 Vue 3 的集成非常简洁和强大，通过 Composition API 可以轻松管理编辑器状态和生命周期。

关键要点：
1. 使用 `Editor` 类初始化编辑器实例
2. 通过 `EditorContent` 组件渲染编辑器
3. 在 `onMounted` 中创建编辑器，在 `onBeforeUnmount` 中销毁
4. 使用 `watch` 监听属性变化并同步到编辑器
5. 通过 `emit` 实现双向数据绑定
6. 可以与 Pinia 等状态管理库无缝集成

---

*下一步：查看 [样式定制指南](./styling-guide.md)*