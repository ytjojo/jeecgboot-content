# Tiptap 数据持久化指南

## 概述

Tiptap 编辑器支持多种数据持久化方案，可以将编辑器内容保存为 HTML 或 JSON 格式。本指南将详细介绍如何实现数据的保存、恢复和同步，包括本地存储、数据库存储以及实时保存等功能。

## 数据格式

### 1. HTML 格式

HTML 格式适合用于最终展示，但不推荐用于持久化存储。

```javascript
// 获取 HTML 格式内容
const htmlContent = editor.getHTML()
console.log(htmlContent)
// 输出: '<p>Hello <strong>world</strong>!</p>'

// 设置 HTML 格式内容
editor.commands.setContent('<p>Hello <strong>world</strong>!</p>')
```

### 2. JSON 格式（推荐）

JSON 格式更适合持久化存储，因为它保留了完整的文档结构和属性信息。

```javascript
// 获取 JSON 格式内容
const jsonContent = editor.getJSON()
console.log(JSON.stringify(jsonContent, null, 2))
// 输出:
// {
//   "type": "doc",
//   "content": [
//     {
//       "type": "paragraph",
//       "content": [
//         {
//           "type": "text",
//           "text": "Hello "
//         },
//         {
//           "type": "text",
//           "marks": [
//             {
//               "type": "bold"
//             }
//           ],
//           "text": "world"
//         },
//         {
//           "type": "text",
//           "text": "!"
//         }
//       ]
//     }
//   ]
// }

// 设置 JSON 格式内容
editor.commands.setContent(jsonContent)
```

### 3. 纯文本格式

```javascript
// 获取纯文本内容
const textContent = editor.getText()
console.log(textContent)
// 输出: 'Hello world!'
```

## 本地存储方案

### 1. LocalStorage 存储

#### React 实现

```jsx
// hooks/useLocalStorage.js
import { useState, useEffect } from 'react'

/**
 * LocalStorage 自定义 Hook
 * @param {string} key - 存储键名
 * @param {any} initialValue - 初始值
 * @returns {[any, function]} - [值, 设置函数]
 */
export const useLocalStorage = (key, initialValue) => {
  // 从 localStorage 读取初始值
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key)
      return item ? JSON.parse(item) : initialValue
    } catch (error) {
      console.error(`Error reading localStorage key "${key}":`, error)
      return initialValue
    }
  })

  // 设置值到 localStorage
  const setValue = (value) => {
    try {
      // 允许传入函数来更新值
      const valueToStore = value instanceof Function ? value(storedValue) : value
      setStoredValue(valueToStore)
      window.localStorage.setItem(key, JSON.stringify(valueToStore))
    } catch (error) {
      console.error(`Error setting localStorage key "${key}":`, error)
    }
  }

  return [storedValue, setValue]
}
```

```jsx
// components/TiptapEditor.jsx
import React, { useEffect } from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { useLocalStorage } from '../hooks/useLocalStorage'

/**
 * 带本地存储功能的 Tiptap 编辑器
 */
const TiptapEditor = () => {
  // 使用 LocalStorage 存储编辑器内容
  const [content, setContent] = useLocalStorage('tiptap-content', {
    type: 'doc',
    content: [
      {
        type: 'paragraph',
        content: [
          {
            type: 'text',
            text: '开始编写您的内容...'
          }
        ]
      }
    ]
  })

  // 初始化编辑器
  const editor = useEditor({
    extensions: [StarterKit],
    content: content,
    onUpdate: ({ editor }) => {
      // 实时保存内容到 LocalStorage
      const json = editor.getJSON()
      setContent(json)
    },
  })

  // 手动保存功能
  const saveContent = () => {
    if (editor) {
      const json = editor.getJSON()
      setContent(json)
      alert('内容已保存到本地存储')
    }
  }

  // 清空内容
  const clearContent = () => {
    if (editor && window.confirm('确定要清空所有内容吗？')) {
      editor.commands.clearContent()
      setContent({
        type: 'doc',
        content: []
      })
    }
  }

  // 导出内容
  const exportContent = () => {
    if (editor) {
      const json = editor.getJSON()
      const html = editor.getHTML()
      const text = editor.getText()
      
      const exportData = {
        json,
        html,
        text,
        timestamp: new Date().toISOString()
      }
      
      // 创建下载链接
      const blob = new Blob([JSON.stringify(exportData, null, 2)], {
        type: 'application/json'
      })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `tiptap-content-${Date.now()}.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    }
  }

  // 导入内容
  const importContent = (event) => {
    const file = event.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const importData = JSON.parse(e.target.result)
          if (importData.json) {
            editor.commands.setContent(importData.json)
            setContent(importData.json)
            alert('内容导入成功')
          }
        } catch (error) {
          alert('导入失败：文件格式不正确')
        }
      }
      reader.readAsText(file)
    }
  }

  return (
    <div className="editor-container">
      {/* 工具栏 */}
      <div className="toolbar">
        <button onClick={saveContent} className="btn btn-primary">
          保存
        </button>
        <button onClick={clearContent} className="btn btn-secondary">
          清空
        </button>
        <button onClick={exportContent} className="btn btn-secondary">
          导出
        </button>
        <label className="btn btn-secondary">
          导入
          <input
            type="file"
            accept=".json"
            onChange={importContent}
            style={{ display: 'none' }}
          />
        </label>
      </div>
      
      {/* 编辑器 */}
      <EditorContent editor={editor} className="editor-content" />
      
      {/* 状态信息 */}
      <div className="status-bar">
        <span>字符数: {editor?.storage.characterCount?.characters() || 0}</span>
        <span>单词数: {editor?.storage.characterCount?.words() || 0}</span>
        <span>最后保存: {new Date().toLocaleTimeString()}</span>
      </div>
    </div>
  )
}

export default TiptapEditor
```

#### Vue 3 实现

```vue
<!-- composables/useLocalStorage.js -->
<script>
import { ref, watch } from 'vue'

/**
 * LocalStorage 组合式函数
 * @param {string} key - 存储键名
 * @param {any} defaultValue - 默认值
 * @returns {object} - 响应式存储对象
 */
export function useLocalStorage(key, defaultValue) {
  // 从 localStorage 读取初始值
  const read = () => {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue
    } catch (error) {
      console.error(`Error reading localStorage key "${key}":`, error)
      return defaultValue
    }
  }

  // 写入 localStorage
  const write = (value) => {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error(`Error setting localStorage key "${key}":`, error)
    }
  }

  // 创建响应式引用
  const storedValue = ref(read())

  // 监听值变化并自动保存
  watch(
    storedValue,
    (newValue) => {
      write(newValue)
    },
    { deep: true }
  )

  return storedValue
}
</script>
```

```vue
<!-- components/TiptapEditor.vue -->
<template>
  <div class="editor-container">
    <!-- 工具栏 -->
    <div class="toolbar">
      <button @click="saveContent" class="btn btn-primary">
        保存
      </button>
      <button @click="clearContent" class="btn btn-secondary">
        清空
      </button>
      <button @click="exportContent" class="btn btn-secondary">
        导出
      </button>
      <label class="btn btn-secondary">
        导入
        <input
          type="file"
          accept=".json"
          @change="importContent"
          style="display: none"
        />
      </label>
    </div>
    
    <!-- 编辑器 -->
    <EditorContent :editor="editor" class="editor-content" />
    
    <!-- 状态信息 -->
    <div class="status-bar">
      <span>字符数: {{ characterCount }}</span>
      <span>单词数: {{ wordCount }}</span>
      <span>最后保存: {{ lastSaved }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import CharacterCount from '@tiptap/extension-character-count'
import { useLocalStorage } from '../composables/useLocalStorage'

// 使用 LocalStorage 存储编辑器内容
const content = useLocalStorage('tiptap-content', {
  type: 'doc',
  content: [
    {
      type: 'paragraph',
      content: [
        {
          type: 'text',
          text: '开始编写您的内容...'
        }
      ]
    }
  ]
})

// 最后保存时间
const lastSaved = ref(new Date().toLocaleTimeString())

// 初始化编辑器
const editor = useEditor({
  extensions: [
    StarterKit,
    CharacterCount,
  ],
  content: content.value,
  onUpdate: ({ editor }) => {
    // 实时保存内容到 LocalStorage
    content.value = editor.getJSON()
    lastSaved.value = new Date().toLocaleTimeString()
  },
})

// 计算属性
const characterCount = computed(() => {
  return editor.value?.storage.characterCount?.characters() || 0
})

const wordCount = computed(() => {
  return editor.value?.storage.characterCount?.words() || 0
})

/**
 * 手动保存内容
 */
const saveContent = () => {
  if (editor.value) {
    content.value = editor.value.getJSON()
    lastSaved.value = new Date().toLocaleTimeString()
    alert('内容已保存到本地存储')
  }
}

/**
 * 清空内容
 */
const clearContent = () => {
  if (editor.value && window.confirm('确定要清空所有内容吗？')) {
    editor.value.commands.clearContent()
    content.value = {
      type: 'doc',
      content: []
    }
  }
}

/**
 * 导出内容
 */
const exportContent = () => {
  if (editor.value) {
    const json = editor.value.getJSON()
    const html = editor.value.getHTML()
    const text = editor.value.getText()
    
    const exportData = {
      json,
      html,
      text,
      timestamp: new Date().toISOString()
    }
    
    // 创建下载链接
    const blob = new Blob([JSON.stringify(exportData, null, 2)], {
      type: 'application/json'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `tiptap-content-${Date.now()}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }
}

/**
 * 导入内容
 */
const importContent = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const importData = JSON.parse(e.target.result)
        if (importData.json) {
          editor.value.commands.setContent(importData.json)
          content.value = importData.json
          alert('内容导入成功')
        }
      } catch (error) {
        alert('导入失败：文件格式不正确')
      }
    }
    reader.readAsText(file)
  }
}

// 组件卸载时清理
onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
  }
})
</script>
```

### 2. IndexedDB 存储

对于大量数据或需要更复杂查询的场景，可以使用 IndexedDB。

```javascript
// utils/indexedDB.js

/**
 * IndexedDB 工具类
 */
class TiptapDB {
  constructor() {
    this.dbName = 'TiptapDB'
    this.version = 1
    this.db = null
  }

  /**
   * 初始化数据库
   */
  async init() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.version)
      
      request.onerror = () => reject(request.error)
      request.onsuccess = () => {
        this.db = request.result
        resolve(this.db)
      }
      
      request.onupgradeneeded = (event) => {
        const db = event.target.result
        
        // 创建文档存储
        if (!db.objectStoreNames.contains('documents')) {
          const store = db.createObjectStore('documents', { 
            keyPath: 'id', 
            autoIncrement: true 
          })
          store.createIndex('title', 'title', { unique: false })
          store.createIndex('createdAt', 'createdAt', { unique: false })
          store.createIndex('updatedAt', 'updatedAt', { unique: false })
        }
      }
    })
  }

  /**
   * 保存文档
   * @param {object} document - 文档对象
   */
  async saveDocument(document) {
    const transaction = this.db.transaction(['documents'], 'readwrite')
    const store = transaction.objectStore('documents')
    
    const docToSave = {
      ...document,
      updatedAt: new Date().toISOString()
    }
    
    if (!docToSave.createdAt) {
      docToSave.createdAt = docToSave.updatedAt
    }
    
    return new Promise((resolve, reject) => {
      const request = store.put(docToSave)
      request.onsuccess = () => resolve(request.result)
      request.onerror = () => reject(request.error)
    })
  }

  /**
   * 获取文档
   * @param {number} id - 文档ID
   */
  async getDocument(id) {
    const transaction = this.db.transaction(['documents'], 'readonly')
    const store = transaction.objectStore('documents')
    
    return new Promise((resolve, reject) => {
      const request = store.get(id)
      request.onsuccess = () => resolve(request.result)
      request.onerror = () => reject(request.error)
    })
  }

  /**
   * 获取所有文档
   */
  async getAllDocuments() {
    const transaction = this.db.transaction(['documents'], 'readonly')
    const store = transaction.objectStore('documents')
    
    return new Promise((resolve, reject) => {
      const request = store.getAll()
      request.onsuccess = () => resolve(request.result)
      request.onerror = () => reject(request.error)
    })
  }

  /**
   * 删除文档
   * @param {number} id - 文档ID
   */
  async deleteDocument(id) {
    const transaction = this.db.transaction(['documents'], 'readwrite')
    const store = transaction.objectStore('documents')
    
    return new Promise((resolve, reject) => {
      const request = store.delete(id)
      request.onsuccess = () => resolve()
      request.onerror = () => reject(request.error)
    })
  }
}

// 创建全局实例
const tiptapDB = new TiptapDB()

export default tiptapDB
```

```jsx
// React 组件中使用 IndexedDB
import React, { useState, useEffect } from 'react'
import tiptapDB from '../utils/indexedDB'

const DocumentManager = () => {
  const [documents, setDocuments] = useState([])
  const [currentDoc, setCurrentDoc] = useState(null)

  useEffect(() => {
    // 初始化数据库并加载文档列表
    const initDB = async () => {
      try {
        await tiptapDB.init()
        const docs = await tiptapDB.getAllDocuments()
        setDocuments(docs)
      } catch (error) {
        console.error('Failed to initialize database:', error)
      }
    }
    
    initDB()
  }, [])

  /**
   * 保存当前文档
   */
  const saveDocument = async (content, title = '未命名文档') => {
    try {
      const docToSave = {
        id: currentDoc?.id,
        title,
        content,
        wordCount: content.content?.length || 0
      }
      
      const savedId = await tiptapDB.saveDocument(docToSave)
      
      // 更新文档列表
      const docs = await tiptapDB.getAllDocuments()
      setDocuments(docs)
      
      // 更新当前文档
      if (!currentDoc?.id) {
        const newDoc = await tiptapDB.getDocument(savedId)
        setCurrentDoc(newDoc)
      }
      
      alert('文档保存成功')
    } catch (error) {
      console.error('Failed to save document:', error)
      alert('文档保存失败')
    }
  }

  /**
   * 加载文档
   */
  const loadDocument = async (id) => {
    try {
      const doc = await tiptapDB.getDocument(id)
      setCurrentDoc(doc)
      return doc
    } catch (error) {
      console.error('Failed to load document:', error)
      alert('文档加载失败')
    }
  }

  /**
   * 删除文档
   */
  const deleteDocument = async (id) => {
    if (window.confirm('确定要删除这个文档吗？')) {
      try {
        await tiptapDB.deleteDocument(id)
        const docs = await tiptapDB.getAllDocuments()
        setDocuments(docs)
        
        if (currentDoc?.id === id) {
          setCurrentDoc(null)
        }
        
        alert('文档删除成功')
      } catch (error) {
        console.error('Failed to delete document:', error)
        alert('文档删除失败')
      }
    }
  }

  return {
    documents,
    currentDoc,
    saveDocument,
    loadDocument,
    deleteDocument
  }
}

export default DocumentManager
```

## 服务器端存储

### 1. RESTful API 集成

```javascript
// services/documentService.js

/**
 * 文档服务类
 */
class DocumentService {
  constructor(baseURL = '/api') {
    this.baseURL = baseURL
  }

  /**
   * 发送 HTTP 请求
   * @param {string} url - 请求URL
   * @param {object} options - 请求选项
   */
  async request(url, options = {}) {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    }

    try {
      const response = await fetch(`${this.baseURL}${url}`, config)
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      return await response.json()
    } catch (error) {
      console.error('Request failed:', error)
      throw error
    }
  }

  /**
   * 保存文档
   * @param {object} document - 文档对象
   */
  async saveDocument(document) {
    const method = document.id ? 'PUT' : 'POST'
    const url = document.id ? `/documents/${document.id}` : '/documents'
    
    return this.request(url, {
      method,
      body: JSON.stringify(document)
    })
  }

  /**
   * 获取文档
   * @param {string} id - 文档ID
   */
  async getDocument(id) {
    return this.request(`/documents/${id}`)
  }

  /**
   * 获取文档列表
   * @param {object} params - 查询参数
   */
  async getDocuments(params = {}) {
    const queryString = new URLSearchParams(params).toString()
    const url = queryString ? `/documents?${queryString}` : '/documents'
    return this.request(url)
  }

  /**
   * 删除文档
   * @param {string} id - 文档ID
   */
  async deleteDocument(id) {
    return this.request(`/documents/${id}`, {
      method: 'DELETE'
    })
  }

  /**
   * 搜索文档
   * @param {string} query - 搜索关键词
   */
  async searchDocuments(query) {
    return this.request(`/documents/search?q=${encodeURIComponent(query)}`)
  }
}

// 创建全局实例
const documentService = new DocumentService()

export default documentService
```

### 2. 实时保存功能

```jsx
// hooks/useAutoSave.js
import { useEffect, useRef, useCallback } from 'react'
import documentService from '../services/documentService'

/**
 * 自动保存 Hook
 * @param {object} editor - Tiptap 编辑器实例
 * @param {object} document - 当前文档
 * @param {number} delay - 保存延迟（毫秒）
 */
export const useAutoSave = (editor, document, delay = 2000) => {
  const timeoutRef = useRef(null)
  const lastSavedRef = useRef(null)
  const [isSaving, setIsSaving] = useState(false)
  const [lastSaved, setLastSaved] = useState(null)

  /**
   * 保存文档
   */
  const saveDocument = useCallback(async () => {
    if (!editor || !document) return

    const content = editor.getJSON()
    const contentString = JSON.stringify(content)
    
    // 检查内容是否有变化
    if (lastSavedRef.current === contentString) {
      return
    }

    setIsSaving(true)
    
    try {
      const docToSave = {
        ...document,
        content,
        wordCount: editor.storage.characterCount?.words() || 0,
        characterCount: editor.storage.characterCount?.characters() || 0
      }
      
      await documentService.saveDocument(docToSave)
      
      lastSavedRef.current = contentString
      setLastSaved(new Date())
      
      console.log('Document auto-saved successfully')
    } catch (error) {
      console.error('Auto-save failed:', error)
    } finally {
      setIsSaving(false)
    }
  }, [editor, document])

  /**
   * 延迟保存
   */
  const debouncedSave = useCallback(() => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
    }
    
    timeoutRef.current = setTimeout(() => {
      saveDocument()
    }, delay)
  }, [saveDocument, delay])

  // 监听编辑器内容变化
  useEffect(() => {
    if (!editor) return

    const handleUpdate = () => {
      debouncedSave()
    }

    editor.on('update', handleUpdate)

    return () => {
      editor.off('update', handleUpdate)
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [editor, debouncedSave])

  // 页面卸载前保存
  useEffect(() => {
    const handleBeforeUnload = (event) => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
        saveDocument()
        
        // 阻止页面关闭，给保存操作一些时间
        event.preventDefault()
        event.returnValue = ''
      }
    }

    window.addEventListener('beforeunload', handleBeforeUnload)
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
    }
  }, [saveDocument])

  return {
    isSaving,
    lastSaved,
    saveDocument: () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
      saveDocument()
    }
  }
}
```

```jsx
// 在组件中使用自动保存
const TiptapEditor = ({ documentId }) => {
  const [document, setDocument] = useState(null)
  const [loading, setLoading] = useState(true)

  const editor = useEditor({
    extensions: [
      StarterKit,
      CharacterCount,
    ],
    content: document?.content || '',
  })

  // 使用自动保存
  const { isSaving, lastSaved, saveDocument } = useAutoSave(editor, document)

  // 加载文档
  useEffect(() => {
    const loadDocument = async () => {
      if (documentId) {
        try {
          const doc = await documentService.getDocument(documentId)
          setDocument(doc)
          editor?.commands.setContent(doc.content)
        } catch (error) {
          console.error('Failed to load document:', error)
        }
      } else {
        // 创建新文档
        setDocument({
          title: '未命名文档',
          content: null
        })
      }
      setLoading(false)
    }

    loadDocument()
  }, [documentId, editor])

  if (loading) {
    return <div>加载中...</div>
  }

  return (
    <div className="editor-container">
      {/* 状态栏 */}
      <div className="status-bar">
        <span>字符数: {editor?.storage.characterCount?.characters() || 0}</span>
        <span>单词数: {editor?.storage.characterCount?.words() || 0}</span>
        <span className={`save-status ${isSaving ? 'saving' : 'saved'}`}>
          {isSaving ? '保存中...' : lastSaved ? `已保存 ${lastSaved.toLocaleTimeString()}` : '未保存'}
        </span>
        <button onClick={saveDocument} disabled={isSaving}>
          手动保存
        </button>
      </div>
      
      {/* 编辑器 */}
      <EditorContent editor={editor} className="editor-content" />
    </div>
  )
}
```

### 3. 版本控制和历史记录

```javascript
// services/versionService.js

/**
 * 版本控制服务
 */
class VersionService {
  constructor(baseURL = '/api') {
    this.baseURL = baseURL
  }

  /**
   * 创建文档版本
   * @param {string} documentId - 文档ID
   * @param {object} content - 文档内容
   * @param {string} comment - 版本注释
   */
  async createVersion(documentId, content, comment = '') {
    const response = await fetch(`${this.baseURL}/documents/${documentId}/versions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        content,
        comment,
        timestamp: new Date().toISOString()
      })
    })
    
    return response.json()
  }

  /**
   * 获取文档版本列表
   * @param {string} documentId - 文档ID
   */
  async getVersions(documentId) {
    const response = await fetch(`${this.baseURL}/documents/${documentId}/versions`)
    return response.json()
  }

  /**
   * 获取特定版本内容
   * @param {string} documentId - 文档ID
   * @param {string} versionId - 版本ID
   */
  async getVersion(documentId, versionId) {
    const response = await fetch(`${this.baseURL}/documents/${documentId}/versions/${versionId}`)
    return response.json()
  }

  /**
   * 恢复到特定版本
   * @param {string} documentId - 文档ID
   * @param {string} versionId - 版本ID
   */
  async restoreVersion(documentId, versionId) {
    const response = await fetch(`${this.baseURL}/documents/${documentId}/restore/${versionId}`, {
      method: 'POST'
    })
    return response.json()
  }

  /**
   * 比较两个版本
   * @param {string} documentId - 文档ID
   * @param {string} versionId1 - 版本1 ID
   * @param {string} versionId2 - 版本2 ID
   */
  async compareVersions(documentId, versionId1, versionId2) {
    const response = await fetch(
      `${this.baseURL}/documents/${documentId}/compare/${versionId1}/${versionId2}`
    )
    return response.json()
  }
}

const versionService = new VersionService()
export default versionService
```

## 数据同步和冲突解决

### 1. 乐观锁机制

```javascript
// utils/conflictResolution.js

/**
 * 冲突解决工具
 */
class ConflictResolver {
  /**
   * 检测冲突
   * @param {object} localDoc - 本地文档
   * @param {object} serverDoc - 服务器文档
   */
  detectConflict(localDoc, serverDoc) {
    return localDoc.version !== serverDoc.version
  }

  /**
   * 合并文档内容
   * @param {object} localContent - 本地内容
   * @param {object} serverContent - 服务器内容
   * @param {object} baseContent - 基础版本内容
   */
  mergeContent(localContent, serverContent, baseContent) {
    // 简单的三路合并策略
    // 实际项目中可能需要更复杂的合并算法
    
    const merged = JSON.parse(JSON.stringify(serverContent))
    
    // 这里可以实现更复杂的合并逻辑
    // 例如：段落级别的合并、操作转换等
    
    return merged
  }

  /**
   * 显示冲突解决界面
   * @param {object} localDoc - 本地文档
   * @param {object} serverDoc - 服务器文档
   */
  async resolveConflict(localDoc, serverDoc) {
    return new Promise((resolve) => {
      // 创建冲突解决对话框
      const modal = document.createElement('div')
      modal.className = 'conflict-resolution-modal'
      modal.innerHTML = `
        <div class="modal-content">
          <h3>检测到文档冲突</h3>
          <p>文档在其他地方被修改，请选择如何处理：</p>
          <div class="conflict-options">
            <button id="use-local">使用本地版本</button>
            <button id="use-server">使用服务器版本</button>
            <button id="merge">尝试合并</button>
          </div>
        </div>
      `
      
      document.body.appendChild(modal)
      
      // 绑定事件
      modal.querySelector('#use-local').onclick = () => {
        document.body.removeChild(modal)
        resolve({ action: 'use-local', content: localDoc.content })
      }
      
      modal.querySelector('#use-server').onclick = () => {
        document.body.removeChild(modal)
        resolve({ action: 'use-server', content: serverDoc.content })
      }
      
      modal.querySelector('#merge').onclick = () => {
        const merged = this.mergeContent(localDoc.content, serverDoc.content, null)
        document.body.removeChild(modal)
        resolve({ action: 'merge', content: merged })
      }
    })
  }
}

const conflictResolver = new ConflictResolver()
export default conflictResolver
```

### 2. 实时协作支持

```javascript
// services/collaborationService.js

/**
 * 协作服务
 */
class CollaborationService {
  constructor(documentId) {
    this.documentId = documentId
    this.websocket = null
    this.isConnected = false
    this.listeners = new Map()
  }

  /**
   * 连接 WebSocket
   */
  connect() {
    const wsUrl = `ws://localhost:8080/collaboration/${this.documentId}`
    this.websocket = new WebSocket(wsUrl)
    
    this.websocket.onopen = () => {
      this.isConnected = true
      this.emit('connected')
    }
    
    this.websocket.onmessage = (event) => {
      const data = JSON.parse(event.data)
      this.emit(data.type, data.payload)
    }
    
    this.websocket.onclose = () => {
      this.isConnected = false
      this.emit('disconnected')
      
      // 自动重连
      setTimeout(() => {
        this.connect()
      }, 3000)
    }
    
    this.websocket.onerror = (error) => {
      console.error('WebSocket error:', error)
      this.emit('error', error)
    }
  }

  /**
   * 发送操作
   * @param {object} operation - 操作对象
   */
  sendOperation(operation) {
    if (this.isConnected) {
      this.websocket.send(JSON.stringify({
        type: 'operation',
        payload: operation
      }))
    }
  }

  /**
   * 监听事件
   * @param {string} event - 事件名
   * @param {function} callback - 回调函数
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 触发事件
   * @param {string} event - 事件名
   * @param {any} data - 事件数据
   */
  emit(event, data) {
    const callbacks = this.listeners.get(event) || []
    callbacks.forEach(callback => callback(data))
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.websocket) {
      this.websocket.close()
    }
  }
}

export default CollaborationService
```

## 性能优化

### 1. 增量保存

```javascript
// utils/incrementalSave.js

/**
 * 增量保存工具
 */
class IncrementalSave {
  constructor() {
    this.lastSavedContent = null
    this.pendingChanges = []
  }

  /**
   * 计算内容差异
   * @param {object} oldContent - 旧内容
   * @param {object} newContent - 新内容
   */
  calculateDiff(oldContent, newContent) {
    // 简化的差异计算
    // 实际项目中可以使用更高效的差异算法
    return {
      type: 'full-replace',
      content: newContent,
      timestamp: Date.now()
    }
  }

  /**
   * 添加变更
   * @param {object} content - 当前内容
   */
  addChange(content) {
    if (this.lastSavedContent) {
      const diff = this.calculateDiff(this.lastSavedContent, content)
      this.pendingChanges.push(diff)
    } else {
      this.pendingChanges.push({
        type: 'initial',
        content,
        timestamp: Date.now()
      })
    }
  }

  /**
   * 获取待保存的变更
   */
  getPendingChanges() {
    return [...this.pendingChanges]
  }

  /**
   * 标记变更已保存
   * @param {object} content - 已保存的内容
   */
  markSaved(content) {
    this.lastSavedContent = JSON.parse(JSON.stringify(content))
    this.pendingChanges = []
  }

  /**
   * 检查是否有待保存的变更
   */
  hasPendingChanges() {
    return this.pendingChanges.length > 0
  }
}

export default IncrementalSave
```

### 2. 压缩和缓存

```javascript
// utils/compression.js

/**
 * 内容压缩工具
 */
class ContentCompression {
  /**
   * 压缩 JSON 内容
   * @param {object} content - 要压缩的内容
   */
  compress(content) {
    const jsonString = JSON.stringify(content)
    
    // 使用简单的压缩策略
    // 实际项目中可以使用 gzip 或其他压缩算法
    const compressed = this.simpleCompress(jsonString)
    
    return {
      compressed: true,
      data: compressed,
      originalSize: jsonString.length,
      compressedSize: compressed.length
    }
  }

  /**
   * 解压缩内容
   * @param {object} compressedData - 压缩的数据
   */
  decompress(compressedData) {
    if (!compressedData.compressed) {
      return compressedData.data
    }
    
    const decompressed = this.simpleDecompress(compressedData.data)
    return JSON.parse(decompressed)
  }

  /**
   * 简单压缩算法（示例）
   * @param {string} str - 要压缩的字符串
   */
  simpleCompress(str) {
    // 这里只是示例，实际项目中应该使用更好的压缩算法
    return btoa(str)
  }

  /**
   * 简单解压缩算法（示例）
   * @param {string} compressed - 压缩的字符串
   */
  simpleDecompress(compressed) {
    return atob(compressed)
  }
}

const contentCompression = new ContentCompression()
export default contentCompression
```

## 总结

Tiptap 的数据持久化方案具有以下特点：

1. **多种存储方式**：支持 LocalStorage、IndexedDB、服务器存储等
2. **数据格式灵活**：支持 HTML、JSON、纯文本等格式
3. **实时保存**：提供自动保存和手动保存功能
4. **版本控制**：支持文档版本管理和历史记录
5. **冲突解决**：提供冲突检测和解决机制
6. **性能优化**：支持增量保存和内容压缩
7. **协作支持**：可集成实时协作功能

选择合适的持久化方案需要根据具体的业务需求和技术架构来决定。对于简单的单用户应用，LocalStorage 就足够了；对于企业级应用，建议使用服务器端存储配合版本控制和协作功能。

---

*下一步：查看 [扩展系统指南](./extensions-guide.md)*